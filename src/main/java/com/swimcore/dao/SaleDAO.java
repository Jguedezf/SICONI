/*
 * -----------------------------------------------------------------------------
 * ARCHIVO: SaleDAO.java
 * VERSIÓN: 2.0.3 (Anti-Lock & Transactional Stability)
 * FECHA: January 30, 2026
 *
 * CAMBIOS CRÍTICOS:
 * 1. Unificada la lógica de inventario dentro de la misma conexión de la venta.
 * 2. Se eliminó la llamada externa a ProductDAO para evitar bloqueos de SQLite.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
}