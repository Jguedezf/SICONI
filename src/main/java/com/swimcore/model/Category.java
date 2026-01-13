/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Category.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Modelo (Model Layer) que representa la entidad 'Categoría'.
 * Actúa como un objeto de transferencia de datos (DTO) diseñado para clasificar
 * de forma lógica los productos dentro del inventario.
 *
 * Características de Ingeniería:
 * 1. Mapeo Objeto-Relacional: Estructura de datos que espeja la tabla 'categories'
 * de la base de datos SQLite.
 * 2. Despliegue en UI: Implementa una representación textual optimizada para
 * componentes de selección (ComboBoxes) mediante la sobreescritura de métodos base.
 * 3. Consistencia de Datos: Centraliza los atributos de clasificación (nombre y
 * descripción) para mantener la integridad en las relaciones relacionales.
 *
 * PRINCIPIOS POO:
 * - ENCAPSULAMIENTO: Atributos privados con métodos de acceso (Getters/Setters)
 * para garantizar la integridad de las propiedades.
 * - HERENCIA: Sobreescritura (Override) del método `toString()` de la clase `Object`.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * Entidad de Categoría.
 * Define la estructura para la clasificación de productos en el sistema SICONI.
 */
public class Category {

    // --- ATRIBUTOS PRIVADOS ---
    private int id;             // Identificador único (Clave Primaria en BD)
    private String name;        // Nombre de la categoría (Ej: Bikinis, Telas)
    private String description; // Detalle funcional de la clasificación

    /**
     * Constructor por defecto.
     * Requerido por la arquitectura para instanciación dinámica.
     */
    public Category() {}

    /**
     * Constructor sobrecargado para inicialización completa.
     * @param id Identificador único asignado por el motor de base de datos.
     * @param name Denominación de la categoría.
     * @param description Detalle descriptivo.
     */
    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // --- MÉTODOS DE ACCESO (GETTERS Y SETTERS) ---

    /** @return El identificador de la categoría. */
    public int getId() { return id; }
    /** @param id Define el ID de la categoría. */
    public void setId(int id) { this.id = id; }

    /** @return El nombre descriptivo de la categoría. */
    public String getName() { return name; }
    /** @param name Define el nombre de la categoría. */
    public void setName(String name) { this.name = name; }

    /** @return La descripción detallada. */
    public String getDescription() { return description; }
    /** @param description Define la descripción. */
    public void setDescription(String description) { this.description = description; }

    /**
     * Representación textual de la entidad.
     * Fundamental para el renderizado automático en componentes JComboBox.
     * @return El nombre de la categoría.
     */
    @Override
    public String toString() {
        return name;
    }
}