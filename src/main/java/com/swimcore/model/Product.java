/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Product.java
 * ... (Encabezado original mantenido) ...
 */

package com.swimcore.model;

public class Product {

    // --- ATRIBUTOS DESCRIPTIVOS ---
    private int id;
    private String code;
    private String name;
    private String description;

    // --- ATRIBUTOS FINANCIEROS ---
    private double costPrice;
    private double salePrice;

    // --- ATRIBUTOS DE INVENTARIO ---
    private int currentStock;
    private int minStock;

    // --- ATRIBUTOS DE RELACIÓN ---
    private int categoryId;
    private int supplierId;

    // --- EXTRAS ---
    private String imagePath;

    // Campos auxiliares para mostrar nombres en tablas (No se guardan en BD Product, se llenan con JOINs)
    private String categoryName;
    private String supplierName;

    public Product() { }

    public Product(int id, String code, String name, String description, double costPrice,
                   double salePrice, int currentStock, int minStock, int categoryId,
                   int supplierId, String imagePath) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.imagePath = imagePath;
    }

    // --- GETTERS Y SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }

    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // Getters/Setters Auxiliares
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    // --- LÓGICA DE DOMINIO ---
    public boolean isLowStock() {
        return this.currentStock <= this.minStock;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}