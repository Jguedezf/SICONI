/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: SaleDetail.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.1.0 (Business Logic Sync)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Modelo (Model Layer). Representa la línea
 * individual de una transacción comercial, actuando como el enlace entre la
 * cabecera de la venta (Sale) y los productos del inventario (Product).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * [MODELO - ENTIDAD] Representación lógica del detalle de un ítem facturado.
 * [POO - ENCAPSULAMIENTO] Define la estructura de los atributos de línea de venta,
 * asegurando la integridad de los cálculos de subtotalización mediante métodos
 * de acceso y construcción.
 * [DISEÑO] Implementa el concepto de composición dentro del sistema de facturación.
 */
public class SaleDetail {

    // ========================================================================================
    //                                  ATRIBUTOS (DATOS PROTEGIDOS)
    // ========================================================================================

    // Identificador único del registro en la persistencia (PK).
    private int id;

    // Identificadores de relación (FK) hacia la venta y el producto.
    private String saleId;
    private String productId;

    // Denominación nominal del producto para persistencia histórica.
    private String productName;

    // Métricas cuantitativas y financieras de la transacción.
    private int quantity;
    private double unitPrice;
    private double subtotal;

    // ========================================================================================
    //                                  CONSTRUCTORES
    // ========================================================================================

    /**
     * Constructor por defecto.
     * Requerido para la instanciación dinámica y compatibilidad con drivers JDBC.
     */
    public SaleDetail() {}

    /**
     * Constructor parametrizado para la lógica de negocio.
     * Automatiza el cálculo del subtotal al momento de la instanciación para
     * evitar inconsistencias aritméticas.
     * * @param saleId Identificador de la venta maestra.
     * @param productId Identificador único del producto.
     * @param productName Nombre del producto al momento de la venta.
     * @param quantity Cantidad de unidades transadas.
     * @param unitPrice Precio pactado por unidad.
     */
    public SaleDetail(String saleId, String productId, String productName, int quantity, double unitPrice) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice; // Cálculo de integridad automático
    }

    // ========================================================================================
    //                                  MÉTODOS DE ACCESO (GETTERS Y SETTERS)
    // ========================================================================================

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

    // ========================================================================================
    //                                  MÉTODOS DE COMPATIBILIDAD (BRIDGE)
    // ========================================================================================

    /** * Método puente para la sincronización con los componentes de la vista SalesView.
     * [TÉCNICO] Provee un alias para el atributo unitPrice, asegurando la interoperabilidad
     * con componentes de terceros o vistas que requieren la firma getPrice().
     * @return Valor del precio unitario.
     */
    public double getPrice() { return unitPrice; }
}