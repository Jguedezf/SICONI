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
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Modelo (Model Layer) que representa la entidad 'Venta' (Cabecera).
 * Actúa como un objeto de transferencia de datos (DTO) que encapsula la información
 * general de una transacción comercial en el sistema.
 *
 * Características de Ingeniería:
 * 1. Soporte Multimoneda: Almacena de forma explícita la divisa, la tasa de cambio
 * y los totales en ambas monedas para garantizar la trazabilidad histórica.
 * 2. Integridad de Datos: Mantiene referencias (Foreign Keys conceptuales) hacia
 * la entidad Cliente y vincula el método de pago utilizado.
 * 3. Mapeo Relacional: Diseñada para representar el lado '1' en una relación
 * de uno a muchos (1:N) con la clase `SaleDetail`.
 *
 * PRINCIPIOS POO:
 * - ENCAPSULAMIENTO: Implementa el acceso a datos mediante métodos públicos
 * (Getters/Setters) protegiendo el estado interno de los atributos privados.
 * - ABSTRACCIÓN: Representa un concepto de negocio ("Factura de Venta") mediante
 * sus propiedades esenciales de auditoría y finanzas.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * Entidad de Venta (Cabecera).
 * Define la estructura de datos para el registro de facturas en el sistema SICONI.
 */
public class Sale {

    // --- ATRIBUTOS PRIVADOS (IDENTIDAD Y AUDITORÍA) ---
    private String id;              // Identificador de factura (Ej: FACT-001)
    private String date;            // Marca de tiempo de la operación
    private String clientId;        // Referencia al cliente (ID o Cédula/RIF)

    // --- ATRIBUTOS FINANCIEROS Y DE CONVERSIÓN ---
    private double totalAmountUSD;  // Monto base en moneda extranjera
    private String currency;        // Identificador de divisa (USD, EUR)
    private double exchangeRate;    // Valor de la tasa de cambio al momento de la venta
    private double totalAmountBs;   // Monto calculado en moneda nacional (Bs)
    private String paymentMethod;   // Clasificación del pago (Efectivo, Transferencia, Zelle)

    /**
     * Constructor sobrecargado para la inicialización completa de la venta.
     * @param id Nro. de factura correlativo.
     * @param date Fecha de registro.
     * @param clientId Identificador del cliente receptor.
     * @param totalAmountUSD Total en divisas.
     * @param currency Tipo de divisa aplicada.
     * @param exchangeRate Factor de conversión (Tasa BCV/Mercado).
     * @param totalAmountBs Total convertido a Bolívares.
     * @param paymentMethod Modalidad de pago seleccionada.
     */
    public Sale(String id, String date, String clientId, double totalAmountUSD,
                String currency, double exchangeRate, double totalAmountBs, String paymentMethod) {
        this.id = id;
        this.date = date;
        this.clientId = clientId;
        this.totalAmountUSD = totalAmountUSD;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.totalAmountBs = totalAmountBs;
        this.paymentMethod = paymentMethod;
    }

    // --- MÉTODOS DE ACCESO (GETTERS Y SETTERS) ---
    // Proporcionan la interfaz pública para la manipulación de la entidad.

    /** @return El número de factura. */
    public String getId() { return id; }
    /** @param id Define el número de factura. */
    public void setId(String id) { this.id = id; }

    /** @return La fecha de la transacción. */
    public String getDate() { return date; }
    /** @param date Define la fecha de la transacción. */
    public void setDate(String date) { this.date = date; }

    /** @return El identificador del cliente asociado. */
    public String getClientId() { return clientId; }
    /** @param clientId Vincula el ID de un cliente. */
    public void setClientId(String clientId) { this.clientId = clientId; }

    /** @return El monto total en divisa extranjera. */
    public double getTotalAmountUSD() { return totalAmountUSD; }
    /** @param totalAmountUSD Define el monto total en USD/EUR. */
    public void setTotalAmountUSD(double totalAmountUSD) { this.totalAmountUSD = totalAmountUSD; }

    /** @return El tipo de moneda utilizada. */
    public String getCurrency() { return currency; }
    /** @param currency Define la moneda (USD/EUR). */
    public void setCurrency(String currency) { this.currency = currency; }

    /** @return La tasa de cambio aplicada en la operación. */
    public double getExchangeRate() { return exchangeRate; }
    /** @param exchangeRate Define la tasa de cambio. */
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }

    /** @return El monto total convertido a moneda nacional. */
    public double getTotalAmountBs() { return totalAmountBs; }
    /** @param totalAmountBs Define el monto total en Bs. */
    public void setTotalAmountBs(double totalAmountBs) { this.totalAmountBs = totalAmountBs; }

    /** @return El método de pago registrado. */
    public String getPaymentMethod() { return paymentMethod; }
    /** @param paymentMethod Define el método de pago. */
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}