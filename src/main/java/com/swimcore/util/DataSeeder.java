/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: DataSeeder.java
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: 06 de Febrero de 2026
 * VERSIÓN: 3.0 (Simulación de Escenarios Realistas)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Componente de utilidad encargado de la persistencia de datos de prueba (Mock Data).
 * Implementa una lógica de generación aleatoria ponderada para simular el
 * comportamiento transaccional en el contexto económico venezolano, permitiendo
 * el estrés y la validación de los módulos de Reportes y Dashboards.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import com.swimcore.dao.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * [UTILIDAD - PERSISTENCIA] Clase para la inicialización y sembrado de la base de datos.
 * [POO - ABSTRACCIÓN] Centraliza la creación de registros de prueba en un único método.
 * [REQUERIMIENTO FUNCIONAL] Validación de integridad: Asegura un entorno controlado
 * para demostraciones técnicas y auditorías de software.
 */
public class DataSeeder {

    // ========================================================================================
    //                                  LÓGICA DE SEMBRADO (DATA SEEDING)
    // ========================================================================================

    /**
     * [MÉTODO ESTÁTICO] Ejecuta una rutina integral de limpieza e inyección masiva de datos.
     * Implementa el uso de Batch Processing para optimizar el rendimiento de inserción.
     */
    public static void reiniciarYSembrar() {
        System.out.println("🇻🇪 SICONI: APLICANDO REALISMO ECONÓMICO...");

        // [JDBC] Apertura de conexión con gestión de transacciones manual (AutoCommit = false)
        try (Connection conn = Conexion.conectar()) {
            if (conn == null) return;
            conn.setAutoCommit(false); // Garantiza la atomicidad de la operación integral.

            // --- FASE 1: LIMPIEZA DE TABLAS (DML) ---
            // Se eliminan registros existentes para evitar conflictos de llaves primarias.
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM payments");
                st.executeUpdate("DELETE FROM sale_details");
                st.executeUpdate("DELETE FROM sales");
            }

            // --- FASE 2: RECUPERACIÓN DE ENTIDADES RELACIONADAS ---
            // Se cargan en memoria los IDs existentes de productos y clientes para mantener
            // la integridad referencial (FK) durante el proceso de inyección.
            List<Integer> productIds = new ArrayList<>();
            List<Double> productPrices = new ArrayList<>();
            List<String> clientIds = new ArrayList<>();

            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id, sale_price FROM products")) {
                while (rs.next()) {
                    productIds.add(rs.getInt("id"));
                    // Normalización de precios para propósitos de demostración.
                    double precio = rs.getDouble("sale_price");
                    if (precio > 40) precio = 35.0;
                    productPrices.add(precio);
                }
            }
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id FROM clients")) {
                while (rs.next()) clientIds.add(rs.getString(1));
            }

            if (productIds.isEmpty() || clientIds.isEmpty()) return;

            // --- FASE 3: INYECCIÓN DINÁMICA (ALGORITMO PONDERADO) ---
            String sqlSale = "INSERT INTO sales (id, date, client_id, total_divisa, amount_paid_usd, balance_due_usd, rate, currency, status, payment_method, observations) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlDet = "INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement psSale = conn.prepareStatement(sqlSale);
            PreparedStatement psDet = conn.prepareStatement(sqlDet);
            Random rand = new Random();
            LocalDate hoy = LocalDate.now();

            // Simulación de 45 transacciones en una ventana temporal de 60 días.
            for (int i = 1; i <= 45; i++) {
                String idVenta = "PED-" + String.format("%04d", i);
                String fecha = hoy.minusDays(rand.nextInt(60)).toString() + " 10:00:00";
                String cliente = clientIds.get(rand.nextInt(clientIds.size()));

                // Composición de la venta (Lógica de 1 o 2 ítems por pedido).
                int items = rand.nextInt(2) + 1;
                double totalVenta = 0;

                for (int j = 0; j < items; j++) {
                    int idxProd = rand.nextInt(productIds.size());
                    int cantidad = 1;
                    double precio = productPrices.get(idxProd);
                    double subtotal = precio * cantidad;

                    psDet.setString(1, idVenta);
                    psDet.setInt(2, productIds.get(idxProd));
                    psDet.setInt(3, cantidad);
                    psDet.setDouble(4, precio);
                    psDet.setDouble(5, subtotal);
                    psDet.addBatch(); // Encolado para ejecución por lotes.
                    totalVenta += subtotal;
                }

                // --- LÓGICA DE ESTADOS FINANCIEROS (PROBABILIDAD) ---
                // Simulación estadística: 75% Pagado, 15% Pendiente, 10% Abono parcial.
                String status = "PAGADO";
                double pagado = totalVenta;
                double deuda = 0;

                double azar = rand.nextDouble();
                if (azar < 0.15) {
                    status = "PENDIENTE";
                    pagado = 0;
                    deuda = totalVenta;
                } else if (azar < 0.25) {
                    status = "EN PROCESO";
                    pagado = totalVenta * 0.5;
                    deuda = totalVenta * 0.5;
                }

                // Configuración de metadatos de la venta
                psSale.setString(1, idVenta);
                psSale.setString(2, fecha);
                psSale.setString(3, cliente);
                psSale.setDouble(4, totalVenta);
                psSale.setDouble(5, pagado);
                psSale.setDouble(6, deuda);
                psSale.setDouble(7, 65.00); // Tasa referencial BCV simulada.
                psSale.setString(8, "USD");
                psSale.setString(9, status);
                psSale.setString(10, "PAGO MÓVIL");
                psSale.setString(11, "Venta registrada por Seeder");
                psSale.addBatch();
            }

            // [PROCESAMIENTO] Ejecución masiva de los lotes preparados.
            psSale.executeBatch();
            psDet.executeBatch();
            conn.commit(); // Confirmación de la transacción atómica.
            System.out.println("✅ REALISMO APLICADO: Ventas ajustadas a economía local.");

        } catch (Exception e) {
            // Gestión de errores en la persistencia de datos.
            e.printStackTrace();
        }
    }
}