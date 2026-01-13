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
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Modelo (Model Layer) que representa el desglose de una transacción.
 * Funciona como una entidad de detalle (Weak Entity) que depende directamente de una
 * cabecera de venta para existir.
 *
 * Características de Ingeniería:
 * 1. Lógica de Cálculo Derivada: Implementa el cálculo automático del `subtotal` en
 * el constructor, garantizando la integridad financiera desde la instanciación.
 * 2. Trazabilidad Transaccional: Mantiene referencias cruzadas (`saleId` y `productId`)
 * para permitir auditorías y reportes de inventario precisos.
 * 3. Mapeo Relacional: Estructura optimizada para representar una relación 1:N
 * (Una factura tiene muchos detalles).
 *
 * PRINCIPIOS POO:
 * - ENCAPSULAMIENTO: Atributos privados con métodos accesores para proteger la
 * consistencia de los datos transaccionales.
 * - ABSTRACCIÓN: Modela la realidad de un "ítem de factura" abstrayendo propiedades
 * como cantidad, precio y producto.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * Entidad de Detalle de Venta.
 * Representa un renglón individual dentro del histórico de ventas del sistema SICONI.
 */
public class SaleDetail {

    // --- ATRIBUTOS PRIVADOS ---
    private int id;             // Identificador único del registro en base de datos
    private String saleId;      // Identificador de la factura (Cabecera) - Foreign Key
    private String productId;   // Identificador técnico del producto - Foreign Key
    private String productName; // Denominación del producto (Desnormalización para históricos)
    private int quantity;       // Unidades transaccionadas
    private double unitPrice;   // Precio de venta pactado al momento de la operación
    private double subtotal;    // Valor total del renglón (Cálculo derivado)

    /**
     * Constructor sobrecargado para la creación de detalles de venta.
     * Automatiza el cálculo del subtotal para evitar discrepancias aritméticas.
     * * @param saleId Vinculación con la factura principal.
     * @param productId Código del producto vendido.
     * @param productName Nombre descriptivo del producto.
     * @param quantity Cantidad de artículos.
     * @param unitPrice Precio unitario de venta.
     */
    public SaleDetail(String saleId, String productId, String productName, int quantity, double unitPrice) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;

        // Regla de Negocio: El subtotal siempre es el producto de cantidad por precio.
        this.subtotal = quantity * unitPrice;
    }

    // --- MÉTODOS DE ACCESO (GETTERS Y SETTERS) ---

    /** @return ID correlativo del detalle. */
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    /** @return El ID de la venta asociada. */
    public String getSaleId() { return saleId; }
    public void setSaleId(String saleId) { this.saleId = saleId; }

    /** @return El código único del producto. */
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    /** @return Nombre del producto registrado en la venta. */
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    /** @return Cantidad de unidades. */
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /** @return Precio unitario aplicado. */
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    /** @return El monto total calculado para este ítem. */
    public double getSubtotal() { return subtotal; }
    // El subtotal suele ser de solo lectura para mantener la integridad,
    // pero se incluye el setter para compatibilidad con DAOs.
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}