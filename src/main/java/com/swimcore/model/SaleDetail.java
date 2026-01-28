package com.swimcore.model;

public class SaleDetail {

    private int id;
    private String saleId;
    private String productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public SaleDetail() {}

    public SaleDetail(String saleId, String productId, String productName, int quantity, double unitPrice) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSaleId() { return saleId; }
    public void setSaleId(String saleId) { this.saleId = saleId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    // --- PUENTE PARA SALESVIEW ---
    public double getPrice() { return unitPrice; } // <--- ESTO QUITA EL ERROR ROJO
}