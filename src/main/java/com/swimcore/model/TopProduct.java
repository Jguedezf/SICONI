/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: TopProduct.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Reporting Model)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Modelo (Model Layer). Actúa como una entidad
 * de datos especializada (DTO - Data Transfer Object) diseñada para representar
 * métricas de agregación, específicamente para el ranking de productos con
 * mayor rotación en el inventario.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * [MODELO - ANALÍTICA] Representación lógica de un ítem de alto rendimiento.
 * [POO - ENCAPSULAMIENTO] Define una estructura inmutable para el transporte de
 * resultados desde las consultas SQL de agregación hacia los componentes visuales
 * del Dashboard (KPIs y Gráficos).
 * [DISEÑO] Objeto ligero de solo lectura para la representación de estadísticas.
 */
public class TopProduct {

    // ========================================================================================
    //                                  ATRIBUTOS (DATOS PROTEGIDOS)
    // ========================================================================================

    // Denominación nominal del producto.
    private String name;

    // Volumen acumulado de unidades comercializadas (Métrica de rendimiento).
    private int quantity;

    // ========================================================================================
    //                                  CONSTRUCTOR
    // ========================================================================================

    /**
     * Constructor parametrizado para la instanciación de registros estadísticos.
     * Facilita la creación de objetos directamente desde el mapeo de resultados (ResultSet)
     * en consultas de tipo "GROUP BY" y "SUM".
     * * @param name Identificador nominal del producto.
     * @param quantity Sumatoria de unidades vendidas en el periodo analizado.
     */
    public TopProduct(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    // ========================================================================================
    //                                  MÉTODOS DE ACCESO (GETTERS)
    // ========================================================================================

    /** * [MÉTODO DE LECTURA] Retorna el nombre del artículo.
     * @return String con el nombre del producto.
     */
    public String getName() { return name; }

    /** * [MÉTODO DE LECTURA] Retorna el volumen de ventas.
     * @return Entero representativo de la cantidad total transada.
     */
    public int getQuantity() { return quantity; }
}