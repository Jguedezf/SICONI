/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Sale.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.2.0 (Finance & Invoice Extended)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Modelo (Model Layer). Representa la entidad
 * maestra 'Venta' u 'Orden de Pedido'. Actúa como un Objeto de Transferencia
 * de Datos (DTO) central que consolida la información comercial, financiera
 * y fiscal de una transacción en el sistema SICONI.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * [MODELO - ENTIDAD] Representación lógica de una transacción de venta.
 * [POO - ENCAPSULAMIENTO] Define la estructura de los atributos de cabecera de venta,
 * gestionando de forma privada el estado financiero (saldos y abonos) y los metadatos
 * de facturación legal.
 * [DISEÑO] Actúa como el eje central del módulo de facturación y cuentas por cobrar.
 */
public class Sale {

    // ========================================================================================
    //                                  ATRIBUTOS (DATOS PROTEGIDOS)
    // ========================================================================================

    // Atributos de Identificación y Trazabilidad Temporal
    private String id, date, clientId;

    // Atributos de Gestión de Pago y Conciliación Bancaria
    private String paymentMethod, reference, bank, paymentDate;

    // Atributos de Control Operativo y Observaciones
    private String status, observations, deliveryDate;

    // Atributos de Facturación Legal (Control Fiscal)
    private String invoiceNumber, controlNumber;

    // Atributos Financieros y de Conversión Monetaria
    // [TÉCNICO] Se utiliza precisión double para el manejo de divisas (USD) y tasas (BS).
    private double totalAmountUSD, amountPaid, balanceDue, exchangeRate;

    // ========================================================================================
    //                                  CONSTRUCTORES
    // ========================================================================================

    /**
     * Constructor por defecto.
     * Requerido para procesos de instanciación dinámica y mapeo de datos vía JDBC.
     */
    public Sale() {}

    /**
     * Constructor parametrizado para la lógica de negocio inicial.
     * Calcula automáticamente el balance pendiente (balanceDue) basándose en el
     * monto total y el abono inicial proporcionado.
     * * @param id Identificador único de la venta (Nro de Control).
     * @param date Fecha de creación del registro.
     * @param clientId Referencia al cliente asociado.
     * @param totalAmountUSD Monto total bruto de la transacción.
     * @param amountPaid Monto abonado al momento del registro.
     * @param exchangeRate Tasa de cambio aplicada (USD/BS).
     * @param paymentMethod Modalidad de pago inicial.
     * @param reference Nro de referencia de la transacción bancaria.
     * @param status Estado operativo (PAGADO, PENDIENTE, EN TALLER).
     * @param observations Notas adicionales sobre el pedido.
     */
    public Sale(String id, String date, String clientId, double totalAmountUSD,
                double amountPaid, double exchangeRate, String paymentMethod,
                String reference, String status, String observations) {
        this.id = id;
        this.date = date;
        this.clientId = clientId;
        this.totalAmountUSD = totalAmountUSD;
        this.amountPaid = amountPaid;
        // [LÓGICA DE INTEGRIDAD] Cálculo automático de deuda remanente.
        this.balanceDue = totalAmountUSD - amountPaid;
        this.exchangeRate = exchangeRate;
        this.paymentMethod = paymentMethod;
        this.reference = reference;
        this.status = status;
        this.observations = observations;
    }

    // ========================================================================================
    //                                  MÉTODOS DE ACCESO (GETTERS Y SETTERS)
    // ========================================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public double getTotalAmountUSD() { return totalAmountUSD; }
    public void setTotalAmountUSD(double totalAmountUSD) { this.totalAmountUSD = totalAmountUSD; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public double getBalanceDue() { return balanceDue; }
    public void setBalanceDue(double balanceDue) { this.balanceDue = balanceDue; }

    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getControlNumber() { return controlNumber; }
    public void setControlNumber(String controlNumber) { this.controlNumber = controlNumber; }

    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
}