/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * AUTORA: Johanna Gabriela Guédez Flores
 * PROFESORA: Ing. Dubraska Roca
 * ASIGNATURA: Técnicas de Programación III
 * * ARCHIVO: ReportsDAO.java
 * VERSIÓN: 1.2.0 (Product Profitability Logic)
 * FECHA: 06 de Febrero de 2026
 * HORA: 07:25 PM (Hora de Venezuela)
 * * DESCRIPCIÓN TÉCNICA:
 * Clase especializada en la extracción de métricas de rendimiento y analítica.
 * Implementa consultas de agregación avanzadas sobre el esquema relacional para
 * transformar datos transaccionales en indicadores financieros clave (KPIs).
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

/**
 * [DAO - BUSINESS INTELLIGENCE] Controlador de datos orientado a reportes gerenciales.
 * [PATRÓN DE DISEÑO] Data Access Object (DAO) para el aislamiento de lógica analítica.
 * [REQUERIMIENTO FUNCIONAL] Generación de balances financieros y análisis de
 * rentabilidad de productos en ventanas temporales dinámicas.
 */
public class ReportsDAO {

    // ========================================================================================
    //                                  MÓDULO DE ANALÍTICA FINANCIERA
    // ========================================================================================

    /**
     * Calcula un resumen consolidado de la salud financiera del taller.
     * Realiza un cálculo ponderado entre precios de venta y costos de producción (cost_price).
     * * @param startDate Límite inferior de la ventana temporal.
     * @param endDate Límite superior de la ventana temporal.
     * @return Map con los indicadores: "ingresos", "costos" y "ganancias".
     */
    public Map<String, Double> getFinancialSummary(Date startDate, Date endDate) {
        Map<String, Double> summary = new HashMap<>();
        summary.put("ingresos", 0.0);
        summary.put("costos", 0.0);
        summary.put("ganancias", 0.0);

        // Consulta con funciones de agregación SUM y cláusulas JOIN triples.
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
                    summary.put("ganancias", ingresos - costos); // Utilidad Bruta
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summary;
    }

    // ========================================================================================
    //                                  ANÁLISIS DE ROTACIÓN DE STOCK
    // ========================================================================================

    /**
     * Identifica los productos con mayor demanda volumétrica (Top Selling).
     * [TÉCNICO] Implementa GROUP BY para consolidar variantes y ORDER BY para el ranking.
     * * @param limit Cantidad de registros a recuperar (N-Top).
     */
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

    // ========================================================================================
    //                                  CÁLCULO DE RENTABILIDAD NETA
    // ========================================================================================

    /**
     * [NUEVO MÉTODO] Realiza el desglose de margen de utilidad por artículo.
     * [LÓGICA DE NEGOCIO] Cruza el ingreso por subtotal contra el costo unitario de
     * adquisición o manufactura para determinar la rentabilidad real.
     * * @return Lista de arreglos conteniendo: [Nombre, Unidades, Ingresos, Costo, Ganancia].
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