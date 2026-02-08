/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Payment.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Febrero 2026
 * VERSIÓN: 1.0.0 (Initial Financial Model)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Modelo (Model Layer). Representa un registro
 * atómico de transacción financiera vinculado a una orden de venta. Permite
 * el seguimiento detallado de abonos parciales y liquidaciones totales,
 * facilitando el control de cuentas por cobrar en el sistema.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * [MODELO - ENTIDAD] Representación lógica de un ingreso monetario por pedido.
 * [POO - ENCAPSULAMIENTO] Define de forma privada los atributos de la transacción,
 * garantizando la integridad de los datos financieros mediante métodos de acceso
 * y construcción normalizados.
 * [DISEÑO] Actúa como una entidad dependiente en la relación uno-a-muchos (1:N)
 * con respecto a la cabecera de la venta (Sale).
 */
public class Payment {

    // ========================================================================================
    //                                  ATRIBUTOS (DATOS PROTEGIDOS)
    // ========================================================================================

    // Identificador único del registro de pago (PK en base de datos).
    private int id;

    // Referencia al pedido asociado (FK hacia la tabla 'sales').
    private String saleId;

    // Estampa de tiempo de la ejecución de la transacción.
    private String paymentDate;

    // Valor nominal del ingreso expresado en divisa base (USD).
    private double amountUSD;

    // Clasificación del canal de ingreso (ej: PAGO MÓVIL, ZELLE, EFECTIVO).
    private String paymentMethod;

    // Código de validación o número de comprobante de la entidad bancaria.
    private String reference;

    // Información contextual o metadatos adicionales del pago.
    private String notes;

    // ========================================================================================
    //                                  CONSTRUCTORES
    // ========================================================================================

    /**
     * Constructor por defecto.
     * Requerido para el mapeo objeto-relacional y la instanciación dinámica
     * mediante la API de reflexión o JDBC.
     */
    public Payment() {
    }

    /**
     * Constructor parametrizado para la creación de nuevos registros.
     * Permite la inicialización completa de la entidad antes de su persistencia.
     * * @param saleId Identificador único del pedido origen.
     * @param paymentDate Fecha y hora del proceso.
     * @param amountUSD Monto total de la operación.
     * @param paymentMethod Modalidad de transferencia.
     * @param reference Nro de confirmación transaccional.
     * @param notes Observaciones de auditoría.
     */
    public Payment(String saleId, String paymentDate, double amountUSD, String paymentMethod, String reference, String notes) {
        this.saleId = saleId;
        this.paymentDate = paymentDate;
        this.amountUSD = amountUSD;
        this.paymentMethod = paymentMethod;
        this.reference = reference;
        this.notes = notes;
    }

    // ========================================================================================
    //                                  MÉTODOS DE ACCESO (GETTERS Y SETTERS)
    // ========================================================================================

    /** @return Identificador primario del pago. */
    public int getId() { return id; }
    /** @param id Asignación manual del ID (usualmente manejado por la BD). */
    public void setId(int id) { this.id = id; }

    /** @return El código del pedido vinculado. */
    public String getSaleId() { return saleId; }
    /** @param saleId Establece la relación con la venta maestra. */
    public void setSaleId(String saleId) { this.saleId = saleId; }

    /** @return La marca temporal del pago. */
    public String getPaymentDate() { return paymentDate; }
    /** @param paymentDate Define la fecha cronológica del registro. */
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    /** @return El volumen monetario de la transacción. */
    public double getAmountUSD() { return amountUSD; }
    /** @param amountUSD Define el monto percibido en dólares. */
    public void setAmountUSD(double amountUSD) { this.amountUSD = amountUSD; }

    /** @return El canal de pago utilizado. */
    public String getPaymentMethod() { return paymentMethod; }
    /** @param paymentMethod Clasifica la modalidad financiera. */
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    /** @return El código de referencia bancaria. */
    public String getReference() { return reference; }
    /** @param reference Establece la validación externa del pago. */
    public void setReference(String reference) { this.reference = reference; }

    /** @return Notas complementarias. */
    public String getNotes() { return notes; }
    /** @param notes Agrega información descriptiva adicional. */
    public void setNotes(String notes) { this.notes = notes; }
}