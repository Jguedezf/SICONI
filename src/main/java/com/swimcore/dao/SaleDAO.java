/*
 * -----------------------------------------------------------------------------
 * ARCHIVO: SaleDAO.java
 * VERSIÓN: 2.5.0 (Analytics Engine Added)
 * FECHA: January 30, 2026
 * -----------------------------------------------------------------------------
 */

        package com.swimcore.dao;

import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleDAO {

    public int getTotalSaleCount() {
        String sql = "SELECT COUNT(*) FROM sales";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String[] getLastInvoiceAndControlNumbers() {
        String[] numbers = {"FAC-0000", "CTRL-" + new SimpleDateFormat("yyyy").format(new Date()) + "-0000"};
        String sql = "SELECT invoice_nro, control_nro FROM sales WHERE invoice_nro != '' AND invoice_nro IS NOT NULL ORDER BY id DESC LIMIT 1";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                numbers[0] = rs.getString("invoice_nro");
                numbers[1] = rs.getString("control_nro");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numbers;
    }

    public boolean registerSale(Sale sale, List<SaleDetail> details) {
        String sqlSale = "INSERT INTO sales (id, date, client_id, total_divisa, amount_paid_usd, " +
                "balance_due_usd, total_bs, rate, currency, payment_method, reference_number, " +
                "status, observations, delivery_date, invoice_nro, control_nro, bank, payment_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlDetail = "INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";

        // SQL para actualizar stock directamente sin abrir otra conexión
        String sqlUpdateStock = "UPDATE products SET current_stock = current_stock - ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = Conexion.conectar();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Guardar Cabecera de la Venta
            PreparedStatement psSale = conn.prepareStatement(sqlSale);
            psSale.setString(1, sale.getId());
            psSale.setString(2, sale.getDate());
            psSale.setString(3, sale.getClientId());
            psSale.setDouble(4, sale.getTotalAmountUSD());
            psSale.setDouble(5, sale.getAmountPaid());
            psSale.setDouble(6, sale.getBalanceDue());
            psSale.setDouble(7, sale.getTotalAmountUSD() * sale.getExchangeRate());
            psSale.setDouble(8, sale.getExchangeRate());
            psSale.setString(9, "USD");
            psSale.setString(10, sale.getPaymentMethod());
            psSale.setString(11, sale.getReference());
            psSale.setString(12, sale.getStatus());
            psSale.setString(13, sale.getObservations());
            psSale.setString(14, sale.getDeliveryDate());
            psSale.setString(15, sale.getInvoiceNumber());
            psSale.setString(16, sale.getControlNumber());
            psSale.setString(17, sale.getBank());
            psSale.setString(18, sale.getPaymentDate());
            psSale.executeUpdate();

            // 2. Guardar Detalles y Actualizar Stock (TODO EN LA MISMA CONEXIÓN)
            PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
            PreparedStatement psStock = conn.prepareStatement(sqlUpdateStock);

            for (SaleDetail d : details) {
                // Insertar detalle
                psDetail.setString(1, sale.getId());
                psDetail.setInt(2, Integer.parseInt(d.getProductId()));
                psDetail.setInt(3, d.getQuantity());
                psDetail.setDouble(4, d.getUnitPrice());
                psDetail.setDouble(5, d.getSubtotal());
                psDetail.addBatch();

                // Descontar stock
                psStock.setInt(1, d.getQuantity());
                psStock.setInt(2, Integer.parseInt(d.getProductId()));
                psStock.addBatch();
            }

            psDetail.executeBatch();
            psStock.executeBatch();

            conn.commit(); // Guardar todo permanentemente
            return true;

        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {} }
        }
    }

    public List<Sale> getAllSales() {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT * FROM sales";
        try (Connection con = Conexion.conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Sale s = new Sale();
                s.setId(rs.getString("id"));
                s.setClientId(rs.getString("client_id"));
                s.setTotalAmountUSD(rs.getDouble("total_divisa"));
                s.setAmountPaid(rs.getDouble("amount_paid_usd"));
                s.setExchangeRate(rs.getDouble("rate"));
                s.setStatus(rs.getString("status"));
                list.add(s);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ========================================================================
    // MÉTODOS DE ANALÍTICA (AGREGADOS PARA EL MÓDULO DE REPORTES)
    // ========================================================================

    public Map<String, Double> getFinancialReport(Date startDate, Date endDate) {
        Map<String, Double> financials = new HashMap<>();
        String start = new SimpleDateFormat("yyyy-MM-dd").format(startDate) + " 00:00:00";
        String end = new SimpleDateFormat("yyyy-MM-dd").format(endDate) + " 23:59:59";

        String sql = "SELECT SUM(total_divisa) FROM sales WHERE date BETWEEN ? AND ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, start);
            pst.setString(2, end);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                double ingresos = rs.getDouble(1);
                financials.put("ingresos", ingresos);
                // Estimación temporal (70% costo / 30% ganancia)
                financials.put("costos", ingresos * 0.70);
                financials.put("ganancias", ingresos * 0.30);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return financials;
    }

    public List<Object[]> getTopSellingProducts(Date startDate, Date endDate) {
        List<Object[]> topList = new ArrayList<>();
        String start = new SimpleDateFormat("yyyy-MM-dd").format(startDate) + " 00:00:00";
        String end = new SimpleDateFormat("yyyy-MM-dd").format(endDate) + " 23:59:59";

        String sql = "SELECT p.name, SUM(d.quantity) as total_qty " +
                "FROM sale_details d JOIN products p ON d.product_id = p.id JOIN sales s ON d.sale_id = s.id " +
                "WHERE s.date BETWEEN ? AND ? GROUP BY p.name ORDER BY total_qty DESC LIMIT 5";

        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, start); pst.setString(2, end);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) topList.add(new Object[]{rs.getString(1), rs.getInt(2)});
        } catch (SQLException e) { e.printStackTrace(); }
        return topList;
    }
    // --- NUEVO: MÉTODO PARA ELIMINAR PEDIDO (CRUD COMPLETO) ---
    public boolean deleteSale(String saleId) {
        String sqlDetails = "DELETE FROM sale_details WHERE sale_id = ?";
        String sqlPayments = "DELETE FROM payments WHERE sale_id = ?";
        String sqlSale = "DELETE FROM sales WHERE id = ?";

        Connection conn = null;
        try {
            conn = Conexion.conectar();
            conn.setAutoCommit(false); // Transacción segura

            // 1. Borrar detalles (items)
            try (PreparedStatement pst = conn.prepareStatement(sqlDetails)) {
                pst.setString(1, saleId);
                pst.executeUpdate();
            }
            // 2. Borrar pagos asociados
            try (PreparedStatement pst = conn.prepareStatement(sqlPayments)) {
                pst.setString(1, saleId);
                pst.executeUpdate();
            }
            // 3. Borrar la venta principal
            try (PreparedStatement pst = conn.prepareStatement(sqlSale)) {
                pst.setString(1, saleId);
                pst.executeUpdate();
            }

            conn.commit(); // Confirmar cambios
            return true;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {} }
        }
    }

    // --- NUEVO: CAMBIAR ESTADO RÁPIDO ---
    public void updateSaleStatus(String saleId, String newStatus) {
        String sql = "UPDATE sales SET status = ? WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, newStatus);
            pst.setString(2, saleId);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Object[]> getProductProfitability(Date startDate, Date endDate) {
        List<Object[]> data = new ArrayList<>();
        String start = new SimpleDateFormat("yyyy-MM-dd").format(startDate) + " 00:00:00";
        String end = new SimpleDateFormat("yyyy-MM-dd").format(endDate) + " 23:59:59";

        String sql = "SELECT p.name, SUM(d.quantity), SUM(d.subtotal) " +
                "FROM sale_details d JOIN products p ON d.product_id = p.id JOIN sales s ON d.sale_id = s.id " +
                "WHERE s.date BETWEEN ? AND ? GROUP BY p.name ORDER BY SUM(d.subtotal) DESC";

        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, start); pst.setString(2, end);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String prod = rs.getString(1);
                int cant = rs.getInt(2);
                double ingreso = rs.getDouble(3);
                data.add(new Object[]{prod, cant, ingreso, ingreso * 0.70, ingreso * 0.30});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;

    }
}