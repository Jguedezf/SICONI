/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Product.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Modelo (Model Layer) que representa la entidad 'Producto'.
 * Actúa como un POJO (Plain Old Java Object) que encapsula tanto los atributos
 * descriptivos como la lógica de estado del inventario.
 *
 * Características de Ingeniería:
 * 1. Mapeo Objeto-Relacional (ORM): Estructura alineada estrictamente con el esquema
 * de la tabla 'products' en SQLite, incluyendo claves foráneas (`categoryId`, `supplierId`).
 * 2. Lógica de Negocio Embebida: Implementa métodos de evaluación de estado (`isLowStock`)
 * para facilitar la toma de decisiones en la Capa de Vista.
 * 3. Gestión Financiera: Diferenciación entre precio de costo y venta para el cálculo
 * posterior de márgenes de utilidad.
 *
 * PRINCIPIOS POO:
 * - ENCAPSULAMIENTO: Atributos privados con acceso controlado mediante Getters y Setters.
 * - ABSTRACCIÓN: Modela un artículo del mundo real mediante sus propiedades esenciales
 * para el control de stock y ventas.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * Entidad de Producto.
 * Centraliza la información técnica, comercial y de inventario de los artículos de SICONI.
 */
public class Product {

    // --- ATRIBUTOS DESCRIPTIVOS (Identidad) ---
    private int id;             // Identificador interno autoincremental
    private String code;        // Código SKU personalizado (Ej: BIK-001)
    private String name;        // Denominación comercial
    private String description; // Detalles técnicos o de diseño

    // --- ATRIBUTOS FINANCIEROS (Gestión Económica) ---
    private double costPrice;   // Inversión unitaria por producto
    private double salePrice;   // Precio de oferta al consumidor final

    // --- ATRIBUTOS DE INVENTARIO (Control de Stock) ---
    private int currentStock;   // Existencia física actual en almacén
    private int minStock;       // Umbral crítico para reposición

    // --- ATRIBUTOS DE RELACIÓN (Foreign Keys) ---
    private int categoryId;     // Vinculación jerárquica con Categoría
    private int supplierId;     // Vinculación comercial con Proveedor

    private String imagePath;   // Referencia al recurso gráfico en disco

    /**
     * Constructor por defecto.
     */
    public Product() { }

    /**
     * Constructor completo para la instanciación de productos desde la base de datos.
     */
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

    // --- MÉTODOS DE ACCESO (GETTERS Y SETTERS) ---

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

    // --- LÓGICA DE DOMINIO ---

    /**
     * Evalúa si el producto se encuentra en niveles de stock crítico.
     * @return true si la existencia actual es menor o igual al stock mínimo configurado.
     */
    public boolean isLowStock() {
        return this.currentStock <= this.minStock;
    }

    /**
     * Representación textual de la entidad.
     * @return Cadena formateada para su visualización en componentes de lista.
     */
    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}