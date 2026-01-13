/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: SaleController.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Controlador (Controller Layer).
 * Actúa como orquestador de la lógica transaccional de ventas, coordinando
 * múltiples modelos y DAOs de forma atómica.
 *
 * Características de Ingeniería:
 * 1. Gestión de Transacciones (ACID): Implementa control manual de commits para asegurar
 * que la venta y la actualización de stock se ejecuten como una sola unidad de trabajo.
 * 2. Integridad de Datos: Aplica mecanismos de Rollback en caso de excepciones SQL,
 * previniendo inconsistencias entre la facturación y el inventario físico.
 * 3. Orquestación Multitabla: Impacta simultáneamente las entidades 'sales',
 * 'sale_details' y 'products'.
 *
 * PRINCIPIOS POO:
 * - ABSTRACCIÓN: El controlador oculta la complejidad del proceso de venta tras
 * un método simplificado `registerSale`.
 * - ENCAPSULAMIENTO: Gestiona el estado de la conexión JDBC de forma interna.
 *
 * PATRONES DE DISEÑO:
 * - Controller (MVC): Separa la lógica de negocio de la interfaz de usuario.
 * - Transaction Script: Organiza la lógica de negocio por procedimientos que
 * manejan solicitudes desde la vista.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.Conexion;
import com.swimcore.model.Product;
import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Controlador de Ventas.
 * Gestiona el ciclo de vida de una transacción comercial, desde el registro
 * de factura hasta el egreso de inventario.
 */
public class SaleController {

    /**
     * Registra una venta completa bajo una transacción atómica.
     * Realiza tres operaciones críticas:
     * 1. Inserta cabecera de venta.
     * 2. Inserta detalles de renglones.
     * 3. Actualiza existencias en almacén.
     *
     * @param sale Objeto con los datos maestros de la factura.
     * @param details Lista de productos y cantidades transaccionadas.
     * @return true si la transacción se completó y confirmó exitosamente.
     */
    public boolean registerSale(Sale sale, List<SaleDetail> details) {
        Connection con = null;
        try {
            con = Conexion.conectar();

            // INGENIERÍA DE TRANSACCIONES:
            // Desactivamos el auto-commit para asegurar la atomicidad de la operación.
            con.setAutoCommit(false);

            // 1. PERSISTENCIA DE CABECERA (Factura)
            String sqlSale = "INSERT INTO sales(id, date, client_id, total_divisa, currency, rate, total_bs, payment_method) VALUES(?,?,?,?,?,?,?,?)";
            try (PreparedStatement pst = con.prepareStatement(sqlSale)) {
                pst.setString(1, sale.getId());
                pst.setString(2, sale.getDate());
                pst.setString(3, sale.getClientId());
                pst.setDouble(4, sale.getTotalAmountUSD());
                pst.setString(5, sale.getCurrency());
                pst.setDouble(6, sale.getExchangeRate());
                pst.setDouble(7, sale.getTotalAmountBs());
                pst.setString(8, sale.getPaymentMethod());
                pst.executeUpdate();
            }

            // 2. PERSISTENCIA DE DETALLES Y ACTUALIZACIÓN DE STOCK (DML Secuencial)
            String sqlDetail = "INSERT INTO sale_details(sale_id, product_id, price) VALUES(?,?,?)";
            String sqlStock = "UPDATE products SET stock = stock - ? WHERE id = ?";

            try (PreparedStatement pstDetail = con.prepareStatement(sqlDetail);
                 PreparedStatement pstStock = con.prepareStatement(sqlStock)) {

                for (SaleDetail det : details) {
                    // Registro de renglón individual
                    pstDetail.setString(1, sale.getId());
                    pstDetail.setString(2, det.getProductId());
                    pstDetail.setDouble(3, det.getUnitPrice());
                    pstDetail.executeUpdate();

                    // EGRESO DE INVENTARIO:
                    // Resta la cantidad vendida de la columna stock en la tabla productos.
                    pstStock.setInt(1, det.getQuantity());
                    pstStock.setString(2, det.getProductId());
                    pstStock.executeUpdate();
                }
            }

            // CONFIRMACIÓN DE LA TRANSACCIÓN:
            // Solo si todas las sentencias previas fueron exitosas.
            con.commit();
            System.out.println("✅ Transacción completada: Venta registrada y Stock actualizado.");
            return true;

        } catch (SQLException e) {
            // MANEJO DE FALLOS CRÍTICOS:
            // Si ocurre cualquier error, se deshacen todos los cambios (Rollback)
            // para mantener la consistencia de la base de datos.
            System.err.println("❌ Error en transacción de venta: " + e.getMessage());
            if (con != null) {
                try {
                    con.rollback();
                    System.err.println("🔄 Rollback ejecutado: Base de datos restaurada.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            // RESTAURACIÓN DEL ESTADO DE CONEXIÓN
            try {
                if (con != null) con.setAutoCommit(true);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}