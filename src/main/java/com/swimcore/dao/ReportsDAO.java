/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ReportsDAO.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.1.0 (SQL Query Hotfix)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Data Access Object (DAO) especializado en la generación de reportes y analíticas.
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

        // --- CONSULTA SQL ---
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
        // --- CONSULTA SQL ---
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
}