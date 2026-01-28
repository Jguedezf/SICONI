/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ReportsDAO.java
 * VERSIÓN: 1.2.0 (Product Profitability Logic)
 * DESCRIPCIÓN: Data Access Object para reportes. Se añade lógica para
 * calcular la rentabilidad neta por producto en un rango de fechas.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsDAO {

    public Map<String, Double> getFinancialSummary(Date startDate, Date endDate) {
        Map<String, Double> summary = new HashMap<>();
        summary.put("ingresos", 0.0);
        summary.put("costos", 0.0);
        summary.put("ganancias", 0.0);

        String sql = "SELECT SUM(sd.unit_price * sd.quantity) as total_ingresos, " +
                "       SUM(p.cost_price * sd.quantity) as total_costos " +
                "FROM sales s " +
                "JOIN sale_details sd ON s.id = sd.sale_id " +
                "JOIN products p ON sd.product_id = p.id " +
                "WHERE s.date BETWEEN ? AND ?";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sdf.format(startDate));
            pstmt.setString(2, sdf.format(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double ingresos = rs.getDouble("total_ingresos");
                    double costos = rs.getDouble("total_costos");
                    summary.put("ingresos", ingresos);
                    summary.put("costos", costos);
                    summary.put("ganancias", ingresos - costos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summary;
    }

    public List<Object[]> getTopSellingProducts(Date startDate, Date endDate, int limit) {
        List<Object[]> topProducts = new ArrayList<>();
        String sql = "SELECT p.name, SUM(sd.quantity) as total_vendido " +
                "FROM sales s " +
                "JOIN sale_details sd ON s.id = sd.sale_id " +
                "JOIN products p ON sd.product_id = p.id " +
                "WHERE s.date BETWEEN ? AND ? " +
                "GROUP BY p.name " +
                "ORDER BY total_vendido DESC " +
                "LIMIT ?";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sdf.format(startDate));
            pstmt.setString(2, sdf.format(endDate));
            pstmt.setInt(3, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("name");
                    int quantitySold = rs.getInt("total_vendido");
                    topProducts.add(new Object[]{productName, quantitySold});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topProducts;
    }

    /**
     * NUEVO MÉTODO: Calcula la rentabilidad de cada producto vendido en un rango de fechas.
     */
    public List<Object[]> getProductProfitability(Date startDate, Date endDate) {
        List<Object[]> profitabilityData = new ArrayList<>();
        String sql = "SELECT p.name, " +
                "       SUM(sd.quantity) as unidades_vendidas, " +
                "       SUM(sd.subtotal) as ingresos_totales, " +
                "       SUM(p.cost_price * sd.quantity) as costo_total, " +
                "       (SUM(sd.subtotal) - SUM(p.cost_price * sd.quantity)) as ganancia_neta " +
                "FROM sales s " +
                "JOIN sale_details sd ON s.id = sd.sale_id " +
                "JOIN products p ON sd.product_id = p.id " +
                "WHERE s.date BETWEEN ? AND ? " +
                "GROUP BY p.name " +
                "ORDER BY ganancia_neta DESC";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sdf.format(startDate));
            pstmt.setString(2, sdf.format(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    profitabilityData.add(new Object[]{
                            rs.getString("name"),
                            rs.getInt("unidades_vendidas"),
                            rs.getDouble("ingresos_totales"),
                            rs.getDouble("costo_total"),
                            rs.getDouble("ganancia_neta")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profitabilityData;
    }
}