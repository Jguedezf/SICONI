/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: Payment.java
 * VERSIÓN: 1.0.0 (Initial Release)
 * DESCRIPCIÓN: Modelo de datos que representa un registro de pago individual
 * asociado a un pedido del taller de confección.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * Entidad de Pago.
 * Representa una transacción financiera (abono o pago final) vinculada a una venta.
 */
public class Payment {

    private int id;                 // ID único del pago
    private String saleId;          // ID del pedido al que pertenece (FK)
    private String paymentDate;     // Fecha del pago (Ej: "2026-01-15 10:30:00")
    private double amountUSD;       // Monto del pago en USD
    private String paymentMethod;   // Método (Ej: PAGO MÓVIL, ZELLE)
    private String reference;       // Referencia de la transacción
    private String notes;           // Observaciones adicionales

    /**
     * Constructor por defecto.
     */
    public Payment() {
    }

    /**
     * Constructor completo para crear una instancia de pago.
     * @param saleId ID del pedido asociado.
     * @param paymentDate Fecha y hora del pago.
     * @param amountUSD Monto pagado.
     * @param paymentMethod Método de pago utilizado.
     * @param reference Número de referencia.
     * @param notes Notas adicionales.
     */
    public Payment(String saleId, String paymentDate, double amountUSD, String paymentMethod, String reference, String notes) {
        this.saleId = saleId;
        this.paymentDate = paymentDate;
        this.amountUSD = amountUSD;
        this.paymentMethod = paymentMethod;
        this.reference = reference;
        this.notes = notes;
    }

    // --- GETTERS Y SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public double getAmountUSD() {
        return amountUSD;
    }

    public void setAmountUSD(double amountUSD) {
        this.amountUSD = amountUSD;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}