/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: User.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Modelo (Model Layer). Define la estructura
 * de datos para la entidad 'Usuario', facilitando el transporte de información
 * entre la base de datos SQLite y la lógica de autenticación y autorización
 * del sistema.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * [MODELO - ENTIDAD] Representación lógica del usuario del sistema.
 * [POO - ENCAPSULAMIENTO] Implementa el acceso controlado a los atributos mediante
 * métodos de acceso (accessors) y mutadores (mutators), protegiendo la integridad
 * del estado del objeto.
 * [DISEÑO] Actúa como un POJO (Plain Old Java Object) para el mapeo objeto-relacional.
 */
public class User {

    // ========================================================================================
    //                                  ATRIBUTOS (DATOS PROTEGIDOS)
    // ========================================================================================

    // Identificador único de la entidad (Mapeado a la Clave Primaria en BD).
    private int id;

    // Identificador de cuenta utilizado para el proceso de Login.
    private String username;

    // Credencial de seguridad del usuario.
    private String password;

    // Representación nominal completa del operador del sistema.
    private String fullName;

    // Clasificación jerárquica para el control de acceso (ej: ADMIN, OPERADOR).
    private String role;

    // ========================================================================================
    //                                  CONSTRUCTORES
    // ========================================================================================

    /**
     * Constructor por defecto.
     * Requerido para la instanciación dinámica y compatibilidad con herramientas
     * de persistencia de datos.
     */
    public User() {}

    /**
     * Constructor con parámetros para la inicialización completa del modelo.
     * Utilizado comúnmente en la creación de nuevos usuarios antes de la persistencia inicial.
     * * @param username Credencial identificadora de cuenta.
     * @param password Clave de acceso.
     * @param fullName Nombre completo del usuario.
     * @param role Nivel de privilegios asignado.
     */
    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // ========================================================================================
    //                                  MÉTODOS DE ACCESO (GETTERS Y SETTERS)
    // ========================================================================================

    /** @return Identificador único del registro. */
    public int getId() { return id; }
    /** @param id Nuevo identificador asignado por el motor de BD. */
    public void setId(int id) { this.id = id; }

    /** @return El nombre de cuenta configurado. */
    public String getUsername() { return username; }
    /** @param username Establece el nombre identificador de cuenta. */
    public void setUsername(String username) { this.username = username; }

    /** @return La credencial de acceso. */
    public String getPassword() { return password; }
    /** @param password Establece la nueva contraseña de acceso. */
    public void setPassword(String password) { this.password = password; }

    /** @return La identidad nominal completa del usuario. */
    public String getFullName() { return fullName; }
    /** @param fullName Define el nombre y apellido del usuario. */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /** @return El nivel jerárquico o perfil de seguridad. */
    public String getRole() { return role; }
    /** @param role Asigna un rol específico para la gestión de permisos. */
    public void setRole(String role) { this.role = role; }
}