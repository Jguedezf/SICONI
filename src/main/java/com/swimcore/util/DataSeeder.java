/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: DataSeeder.java
 * VERSIÓN: 3.0 (REALISMO VENEZUELA + ESTADOS VARIADOS)
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

public class DataSeeder {

    public static void reiniciarYSembrar() {
        System.out.println("🇻🇪 SICONI: APLICANDO REALISMO ECONÓMICO...");

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) return;
            conn.setAutoCommit(false);

            // 1. LIMPIEZA
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM payments");
                st.executeUpdate("DELETE FROM sale_details");
                st.executeUpdate("DELETE FROM sales");
            }

            // 2. OBTENER DATOS REALES
            List<Integer> productIds = new ArrayList<>();
            List<Double> productPrices = new ArrayList<>();
            List<String> clientIds = new ArrayList<>();

            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id, sale_price FROM products")) {
                while (rs.next()) {
                    productIds.add(rs.getInt("id"));
                    // AJUSTE DE PRECIOS: Si el precio es muy alto, lo bajamos para la demo
                    double precio = rs.getDouble("sale_price");
                    if (precio > 40) precio = 35.0;
                    productPrices.add(precio);
                }
            }
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id FROM clients")) {
                while (rs.next()) clientIds.add(rs.getString(1));
            }

            if (productIds.isEmpty() || clientIds.isEmpty()) return;

            // 3. INYECCIÓN (MENOS VENTAS, MONTOS REALISTAS)
            String sqlSale = "INSERT INTO sales (id, date, client_id, total_divisa, amount_paid_usd, balance_due_usd, rate, currency, status, payment_method, observations) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlDet = "INSERT INTO sale_details (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement psSale = conn.prepareStatement(sqlSale);
            PreparedStatement psDet = conn.prepareStatement(sqlDet);
            Random rand = new Random();
            LocalDate hoy = LocalDate.now();

            // Solo 45 ventas en 60 días (aprox 1 diaria o interdiaria)
            for (int i = 1; i <= 45; i++) {
                String idVenta = "PED-" + String.format("%04d", i);
                String fecha = hoy.minusDays(rand.nextInt(60)).toString() + " 10:00:00";
                String cliente = clientIds.get(rand.nextInt(clientIds.size()));

                // PRODUCTOS: La mayoría compra 1 o 2 piezas, no 10.
                int items = rand.nextInt(2) + 1;
                double totalVenta = 0;

                for (int j = 0; j < items; j++) {
                    int idxProd = rand.nextInt(productIds.size());
                    int cantidad = 1; // Casi siempre 1
                    double precio = productPrices.get(idxProd);
                    double subtotal = precio * cantidad;

                    psDet.setString(1, idVenta);
                    psDet.setInt(2, productIds.get(idxProd));
                    psDet.setInt(3, cantidad);
                    psDet.setDouble(4, precio);
                    psDet.setDouble(5, subtotal);
                    psDet.addBatch();
                    totalVenta += subtotal;
                }

                // ESTADOS VARIADOS (REALISMO)
                // 15% Pendiente (Deuda), 10% En Proceso (Taller), 75% Pagado
                String status = "PAGADO";
                double pagado = totalVenta;
                double deuda = 0;

                double azar = rand.nextDouble();
                if (azar < 0.15) { // 15% Deuda
                    status = "PENDIENTE";
                    pagado = 0; // No ha pagado nada
                    deuda = totalVenta;
                } else if (azar < 0.25) { // 10% En Proceso (Abonó la mitad)
                    status = "EN PROCESO";
                    pagado = totalVenta * 0.5;
                    deuda = totalVenta * 0.5;
                }

                psSale.setString(1, idVenta);
                psSale.setString(2, fecha);
                psSale.setString(3, cliente);
                psSale.setDouble(4, totalVenta);
                psSale.setDouble(5, pagado);
                psSale.setDouble(6, deuda);
                psSale.setDouble(7, 65.00); // Tasa BS
                psSale.setString(8, "USD");
                psSale.setString(9, status);
                psSale.setString(10, "PAGO MÓVIL");
                psSale.setString(11, "Venta registrada");
                psSale.addBatch();
            }

            psSale.executeBatch();
            psDet.executeBatch();
            conn.commit();
            System.out.println("✅ REALISMO APLICADO: Ventas ajustadas a economía local.");

        } catch (Exception e) { e.printStackTrace(); }
    }
}