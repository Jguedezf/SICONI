/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: Sale.java
 * VERSIÓN: 2.2.0 (Delivery Date Fix)
 * DESCRIPCIÓN: Se agregó deliveryDate.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

public class Sale {

    private String id;
    private String date;
    private String clientId;
    private double totalAmountUSD;
    private double amountPaid;
    private double balanceDue;
    private double totalAmountBs;
    private double exchangeRate;
    private String currency;
    private String paymentMethod;
    private String reference;
    private String status;
    private String observations;

    // --- CAMPO NUEVO QUE FALTABA ---
    private String deliveryDate;

    public Sale() {}

    public Sale(String id, String date, String clientId, double totalAmountUSD,
                double amountPaid, double exchangeRate, String paymentMethod,
                String reference, String status, String observations) {
        this.id = id;
        this.date = date;
        this.clientId = clientId;
        this.totalAmountUSD = totalAmountUSD;
        this.amountPaid = amountPaid;
        this.balanceDue = totalAmountUSD - amountPaid;
        this.exchangeRate = exchangeRate;
        this.totalAmountBs = totalAmountUSD * exchangeRate;
        this.currency = "USD";
        this.paymentMethod = paymentMethod;
        this.reference = reference;
        this.status = status;
        this.observations = observations;
    }

    // --- GETTERS Y SETTERS ---
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

    public double getTotalAmountBs() { return totalAmountBs; }
    public void setTotalAmountBs(double totalAmountBs) { this.totalAmountBs = totalAmountBs; }

    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    // --- MÉTODOS PARA FECHA DE ENTREGA ---
    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }
}