/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: PaymentDAO.java
 * VERSIÓN: 1.0.0 (Transactional Save)
 * DESCRIPCIÓN: Capa de Acceso a Datos para la entidad Payment. Implementa
 * lógica transaccional para asegurar la consistencia entre las tablas
 * 'payments' y 'sales'.
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

public class PaymentDAO {

    /**
     * Registra un nuevo pago y actualiza el saldo del pedido correspondiente
     * dentro de una única transacción ACID.
     *
     * @param payment El objeto Payment con los datos del pago a registrar.
     * @return true si la transacción completa fue exitosa.
     */
    public boolean savePaymentAndUpdateSale(Payment payment) {
        String sqlInsertPayment = "INSERT INTO payments (sale_id, payment_date, amount_usd, payment_method, reference, notes) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlUpdateSale = "UPDATE sales SET amount_paid_usd = amount_paid_usd + ?, balance_due_usd = balance_due_usd - ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = Conexion.conectar();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar el registro del nuevo pago
            try (PreparedStatement psPayment = conn.prepareStatement(sqlInsertPayment)) {
                psPayment.setString(1, payment.getSaleId());
                psPayment.setString(2, payment.getPaymentDate());
                psPayment.setDouble(3, payment.getAmountUSD());
                psPayment.setString(4, payment.getPaymentMethod());
                psPayment.setString(5, payment.getReference());
                psPayment.setString(6, payment.getNotes());
                psPayment.executeUpdate();
            }

            // 2. Actualizar los montos en la tabla de ventas (sales)
            try (PreparedStatement psSale = conn.prepareStatement(sqlUpdateSale)) {
                psSale.setDouble(1, payment.getAmountUSD()); // Sumar al monto pagado
                psSale.setDouble(2, payment.getAmountUSD()); // Restar del saldo deudor
                psSale.setString(3, payment.getSaleId());
                psSale.executeUpdate();
            }

            // 3. Opcional: Actualizar el estado del pedido si el saldo es cero
            updateSaleStatusIfPaid(conn, payment.getSaleId());

            conn.commit(); // Confirmar todos los cambios si no hubo errores
            return true;

        } catch (SQLException e) {
            System.err.println("Error en transacción de pago. Realizando rollback...");
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Deshacer todos los cambios si algo falló
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar modo de autocommit
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Obtiene todos los pagos registrados para un pedido específico.
     *
     * @param saleId El ID del pedido.
     * @return Una lista de objetos Payment.
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

    /**
     * Metodo auxiliar para cambiar el estado de un pedido a "PAGADO" si su
     * saldo deudor llega a ser menor o igual a cero.
     *
     * @param conn La conexión activa de la transacción.
     * @param saleId El ID del pedido a verificar.
     * @throws SQLException si ocurre un error en la consulta.
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

        // Si el saldo es 0 o negativo (pago de más), se actualiza el estado.
        if (balance <= 0.01) {
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStatus)) {
                psUpdate.setString(1, saleId);
                psUpdate.executeUpdate();
                System.out.println("INFO: Pedido " + saleId + " ha sido marcado como PAGADO.");
            }
        }
    }
}