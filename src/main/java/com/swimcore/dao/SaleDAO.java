/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: SaleDAO.java
 * VERSIÓN: 3.0.0 (Atelier Logic)
 * DESCRIPCIÓN: Guarda ventas con soporte para abonos parciales, estatus de
 * producción y fechas de entrega.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import java.sql.*;
import java.util.List;

public class SaleDAO {

    public boolean registerSale(Sale sale, List<SaleDetail> details) {

        // El SQL incluye 'delivery_date' al final
        String sqlSale = "INSERT INTO sales (id, date, client_id, " +
                "total_divisa, amount_paid_usd, balance_due_usd, " +
                "total_bs, rate, currency, payment_method, " +
                "reference_number, status, observations, delivery_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlDetail = "INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = Conexion.conectar();
            conn.setAutoCommit(false);

            PreparedStatement psSale = conn.prepareStatement(sqlSale);
            psSale.setString(1, sale.getId());
            psSale.setString(2, sale.getDate());
            psSale.setString(3, sale.getClientId());
            psSale.setDouble(4, sale.getTotalAmountUSD());
            psSale.setDouble(5, sale.getAmountPaid());
            psSale.setDouble(6, sale.getBalanceDue());
            psSale.setDouble(7, sale.getTotalAmountBs());
            psSale.setDouble(8, sale.getExchangeRate());
            psSale.setString(9, sale.getCurrency());
            psSale.setString(10, sale.getPaymentMethod());
            psSale.setString(11, sale.getReference());
            psSale.setString(12, sale.getStatus());
            psSale.setString(13, sale.getObservations());

            // AHORA SÍ FUNCIONARÁ ESTA LÍNEA PORQUE ACTUALIZASTE EL MODELO
            psSale.setString(14, sale.getDeliveryDate());

            psSale.executeUpdate();

            // Insertar Detalles
            ProductDAO productDAO = new ProductDAO();
            PreparedStatement psDetail = conn.prepareStatement(sqlDetail);

            for (SaleDetail detail : details) {
                psDetail.setString(1, sale.getId());
                psDetail.setInt(2, Integer.parseInt(detail.getProductId()));
                psDetail.setInt(3, detail.getQuantity());
                psDetail.setDouble(4, detail.getUnitPrice());
                psDetail.setDouble(5, detail.getSubtotal());
                psDetail.addBatch();

                // Restar stock
                productDAO.auditStock(Integer.parseInt(detail.getProductId()), -detail.getQuantity(), "PEDIDO " + sale.getId());
            }

            psDetail.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }
}