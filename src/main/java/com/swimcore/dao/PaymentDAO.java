/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * AUTORA: Johanna Gabriela Guédez Flores
 * PROFESORA: Ing. Dubraska Roca
 * ASIGNATURA: Técnicas de Programación III
 * * ARCHIVO: PaymentDAO.java
 * VERSIÓN: 1.0.0 (Transactional Save)
 * FECHA: 06 de Febrero de 2026
 * HORA: 07:26 PM (Hora de Venezuela)
 * * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Acceso a Datos (DAO) especializada en la gestión de
 * transacciones financieras. Implementa lógica transaccional atómica para
 * garantizar la sincronización entre los abonos realizados y el saldo deudor
 * de las órdenes de venta.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Payment;
import com.swimcore.model.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * [DAO - DATA ACCESS OBJECT] Centraliza la persistencia de pagos.
 * [INTEGRIDAD DE DATOS] Implementa el manejo manual de transacciones (Commit/Rollback)
 * para asegurar que el registro de un pago y la actualización de la deuda en la
 * cabecera de la venta se ejecuten de forma indivisible.
 */
public class PaymentDAO {

    // ========================================================================================
    //                                  OPERACIONES TRANSACCIONALES
    // ========================================================================================

    /**
     * [TRANSACCIÓN ATÓMICA] Registra un pago y actualiza el balance de la venta.
     * Implementa los principios ACID: Si alguna de las operaciones falla, se realiza
     * un Rollback para evitar que el pago se registre sin descontar de la deuda.
     * * @param payment Objeto Payment con los datos de la transacción financiera.
     * @return true si la operación integral fue confirmada satisfactoriamente.
     */
    public boolean savePaymentAndUpdateSale(Payment payment) {
        String sqlInsertPayment = "INSERT INTO payments (sale_id, payment_date, amount_usd, payment_method, reference, notes) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlUpdateSale = "UPDATE sales SET amount_paid_usd = amount_paid_usd + ?, balance_due_usd = balance_due_usd - ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = Conexion.conectar();
            conn.setAutoCommit(false); // Desactivación de autocommit para control de transacción

            // 1. Persistencia del registro de pago
            try (PreparedStatement psPayment = conn.prepareStatement(sqlInsertPayment)) {
                psPayment.setString(1, payment.getSaleId());
                psPayment.setString(2, payment.getPaymentDate());
                psPayment.setDouble(3, payment.getAmountUSD());
                psPayment.setString(4, payment.getPaymentMethod());
                psPayment.setString(5, payment.getReference());
                psPayment.setString(6, payment.getNotes());
                psPayment.executeUpdate();
            }

            // 2. Sincronización de montos en la tabla maestra (Sales)
            try (PreparedStatement psSale = conn.prepareStatement(sqlUpdateSale)) {
                psSale.setDouble(1, payment.getAmountUSD()); // Incremento del monto cobrado
                psSale.setDouble(2, payment.getAmountUSD()); // Reducción del saldo por cobrar
                psSale.setString(3, payment.getSaleId());
                psSale.executeUpdate();
            }

            // 3. Verificación de reglas de negocio: Actualización de estado operativo
            updateSaleStatusIfPaid(conn, payment.getSaleId());

            conn.commit(); // Consolidación definitiva de los cambios (ACID Commit)
            return true;

        } catch (SQLException e) {
            // Manejo de fallos en el motor de base de datos
            System.err.println("Error en transacción de pago. Realizando rollback...");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Reversión total ante inconsistencia detectada
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restauración del comportamiento estándar de conexión
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ========================================================================================
    //                                  MÉTODOS DE RECUPERACIÓN (READ)
    // ========================================================================================

    /**
     * Recupera el historial cronológico de pagos vinculados a una orden de pedido.
     * @param saleId Identificador de la venta a consultar.
     * @return List con objetos Payment mapeados desde la base de datos.
     */
    public List<Payment> getPaymentsForSale(String saleId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE sale_id = ? ORDER BY payment_date DESC";

        try (Connection conn = Conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setId(rs.getInt("id"));
                    p.setSaleId(rs.getString("sale_id"));
                    p.setPaymentDate(rs.getString("payment_date"));
                    p.setAmountUSD(rs.getDouble("amount_usd"));
                    p.setPaymentMethod(rs.getString("payment_method"));
                    p.setReference(rs.getString("reference"));
                    p.setNotes(rs.getString("notes"));
                    payments.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    // ========================================================================================
    //                                  LÓGICA DE NEGOCIO AUXILIAR
    // ========================================================================================

    /**
     * [MÉTODO PRIVADO] Evalúa el saldo remanente para actualizar el estado del pedido.
     * Automatiza el flujo de trabajo al marcar el pedido como 'PAGADO' cuando la
     * deuda es solventada satisfactoriamente.
     */
    private void updateSaleStatusIfPaid(Connection conn, String saleId) throws SQLException {
        String sqlCheckBalance = "SELECT balance_due_usd FROM sales WHERE id = ?";
        String sqlUpdateStatus = "UPDATE sales SET status = 'PAGADO / LISTO PARA ENTREGA' WHERE id = ?";

        double balance = 0;
        try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckBalance)) {
            psCheck.setString(1, saleId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    balance = rs.getDouble("balance_due_usd");
                }
            }
        }

        // Tolerancia de precisión decimal (0.01) para la validación de saldo solventado.
        if (balance <= 0.01) {
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStatus)) {
                psUpdate.setString(1, saleId);
                psUpdate.executeUpdate();
                System.out.println("INFO: Pedido " + saleId + " ha sido marcado como PAGADO.");
            }
        }
    }
}