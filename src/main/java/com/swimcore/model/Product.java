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
 * VERSIÓN: 1.1.0 (Business Logic & UI Sync)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Modelo (Model Layer). Representa la entidad
 * principal del catálogo de SICONI. Actúa como un POJO (Plain Old Java Object)
 * que encapsula las propiedades físicas, financieras y logísticas de los
 * artículos, facilitando su gestión en el inventario y las ventas.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * [MODELO - ENTIDAD] Representación lógica de un producto comercializable.
 * [POO - ENCAPSULAMIENTO] Implementa métodos de acceso y mutación para salvaguardar
 * la integridad de los datos de costos, precios y niveles de stock.
 * [LÓGICA DE NEGOCIO] Incluye métodos de evaluación de estado (Low Stock) para el
 * control preventivo de inventario.
 */
public class Product {

    // ========================================================================================
    //                                  ATRIBUTOS (DATOS PROTEGIDOS)
    // ========================================================================================

    // Identificadores de Sistema y Negocio
    private int id;             // PK en base de datos.
    private String code;        // Código SKU (Stock Keeping Unit).

    // Propiedades Descriptivas
    private String name;
    private String description;
    private String imagePath;   // Referencia al recurso gráfico.

    // Atributos Financieros (Precisión Double)
    private double costPrice;
    private double salePrice;

    // Atributos de Control de Inventario
    private int currentStock;
    private int minStock;       // Umbral para alertas de reposición.

    // Relaciones (Foreign Keys e Información Denormalizada)
    private int categoryId;
    private int supplierId;
    private String categoryName;
    private String supplierName;

    // ========================================================================================
    //                                  CONSTRUCTORES
    // ========================================================================================

    /**
     * Constructor por defecto.
     * Requerido para la instanciación dinámica y compatibilidad con drivers JDBC.
     */
    public Product() { }

    /**
     * Constructor parametrizado para la inicialización completa de la entidad.
     * Utilizado en la carga de datos desde la capa de persistencia (DAO).
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

    // ========================================================================================
    //                                  MÉTODOS DE ACCESO (GETTERS Y SETTERS)
    // ========================================================================================

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

    // --- MÉTODOS PUENTE (UI COMPATIBILITY) ---
    /** @return El nivel de existencia actual. Alias para integración con SalesView. */
    public int getStock() { return this.currentStock; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    // ========================================================================================
    //                                  LÓGICA DE ESTADO (BUSINESS LOGIC)
    // ========================================================================================

    /**
     * Evalúa si el producto se encuentra bajo el umbral de stock de seguridad.
     * @return true si la existencia actual es menor o igual al stock mínimo.
     */
    public boolean isLowStock() { return this.currentStock <= this.minStock; }

    /**
     * [POLIMORFISMO - SOBREESCRITURA] Redefine la representación textual del objeto.
     * Facilita la identificación del producto en componentes visuales como JComboBox.
     */
    @Override
    public String toString() { return name + " (" + code + ")"; }
}