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
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Modelo (Model Layer). Define la estructura
 * de datos para la entidad 'Categoría', permitiendo la clasificación taxonómica
 * de los productos en el catálogo de SICONI. Facilita la organización lógica
 * y la recuperación segmentada de información en la base de datos relacional.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * [MODELO - ENTIDAD] Representación lógica de una categoría de productos.
 * [POO - ENCAPSULAMIENTO] Implementa el acceso controlado a sus propiedades mediante
 * el uso de atributos privados y métodos de acceso públicos (Getters/Setters).
 * [DISEÑO] Actúa como un objeto DTO (Data Transfer Object) para el mapeo entre
 * la persistencia SQLite y los componentes de selección en la interfaz de usuario.
 */
public class Category {

    // ========================================================================================
    //                                  ATRIBUTOS (DATOS PROTEGIDOS)
    // ========================================================================================

    // Identificador único de la entidad (Mapeado a la Clave Primaria en la tabla 'categories').
    private int id;

    // Denominación nominal de la clasificación (ej: TRAJES DE BAÑO, INSUMOS).
    private String name;

    // Información contextual adicional sobre el alcance de la categoría.
    private String description;

    // ========================================================================================
    //                                  CONSTRUCTORES
    // ========================================================================================

    /**
     * Constructor por defecto.
     * Requerido para la instanciación dinámica y la compatibilidad con motores de
     * persistencia y serialización de objetos.
     */
    public Category() {}

    /**
     * Constructor parametrizado para la inicialización completa de la entidad.
     * Utilizado para la recuperación de registros existentes desde la capa DAO.
     * * @param id Identificador único asignado por el motor de base de datos.
     * @param name Denominación de la categoría.
     * @param description Detalle funcional de la clasificación.
     */
    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // ========================================================================================
    //                                  MÉTODOS DE ACCESO (GETTERS Y SETTERS)
    // ========================================================================================

    /** @return Identificador primario de la categoría en el sistema. */
    public int getId() { return id; }
    /** @param id Establece el nuevo ID del registro. */
    public void setId(int id) { this.id = id; }

    /** @return El nombre descriptivo de la categoría. */
    public String getName() { return name; }
    /** @param name Define la denominación de la clasificación. */
    public void setName(String name) { this.name = name; }

    /** @return La descripción detallada de la clasificación. */
    public String getDescription() { return description; }
    /** @param description Establece el detalle funcional de la categoría. */
    public void setDescription(String description) { this.description = description; }

    // ========================================================================================
    //                                  POLIMORFISMO Y RENDERIZADO
    // ========================================================================================

    /**
     * [POO - SOBREESCRITURA] Redefine la representación textual del objeto.
     * Esencial para la interoperabilidad con componentes visuales de Swing (ej: JComboBox),
     * permitiendo que el componente muestre el nombre de la categoría sin necesidad
     * de renderizadores personalizados externos.
     * * @return El nombre de la categoría (atributo 'name').
     */
    @Override
    public String toString() {
        return name;
    }
}
