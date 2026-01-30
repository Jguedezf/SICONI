package com.swimcore.model;

public class Sale {
    private String id, date, clientId, paymentMethod, reference, status, observations, deliveryDate, invoiceNumber, controlNumber, bank, paymentDate;
    private double totalAmountUSD, amountPaid, balanceDue, exchangeRate;

    public Sale() {}

    public Sale(String id, String date, String clientId, double totalAmountUSD,
                double amountPaid, double exchangeRate, String paymentMethod,
                String reference, String status, String observations) {
        this.id = id; this.date = date; this.clientId = clientId; this.totalAmountUSD = totalAmountUSD;
        this.amountPaid = amountPaid; this.balanceDue = totalAmountUSD - amountPaid; this.exchangeRate = exchangeRate;
        this.paymentMethod = paymentMethod; this.reference = reference; this.status = status; this.observations = observations;
    }

    // Getters y Setters
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