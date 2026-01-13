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
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Modelo (Model Layer) que representa la entidad 'Usuario' en el sistema.
 * Actúa como un objeto de transferencia de datos (DTO) para mapear los registros de la
 * tabla 'users' de la base de datos SQLite hacia la lógica de la aplicación.
 *
 * Características de Ingeniería:
 * 1. Estructura POO: Implementa un modelo de datos limpio sin lógica de negocio pesada,
 * facilitando la persistencia y la serialización.
 * 2. Gestión de Identidad: Incluye un identificador único (ID) que corresponde a la
 * Clave Primaria (PK) en la base de datos.
 * 3. Seguridad de Acceso: Almacena las credenciales y el rol jerárquico del usuario
 * para la gestión de permisos en el sistema.
 *
 * PRINCIPIOS POO:
 * - ENCAPSULAMIENTO: Atributos privados con métodos de acceso públicos (Getters/Setters)
 * para controlar la integridad de los datos.
 * - ABSTRACCIÓN: Representa la entidad real "Usuario" mediante propiedades esenciales.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * Entidad de Usuario.
 * Define las propiedades y métodos de acceso para los usuarios del sistema SICONI.
 */
public class User {

    // --- ATRIBUTOS PRIVADOS (DATOS PROTEGIDOS) ---
    private int id;             // Identificador único (Auto-incremental en BD)
    private String username;    // Nombre de usuario para el login (Único)
    private String password;    // Contraseña de acceso
    private String fullName;    // Nombre completo del operador
    private String role;        // Perfil de seguridad (Ej: ADMIN, OPERADOR)

    /**
     * Constructor por defecto (Requerido para frameworks de persistencia).
     */
    public User() {}

    /**
     * Constructor con parámetros.
     * Utilizado para la creación de nuevos usuarios antes de ser persistidos.
     * * @param username Identificador de cuenta.
     * @param password Credencial de acceso.
     * @param fullName Nombre real del usuario.
     * @param role Nivel de privilegios.
     */
    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // --- MÉTODOS DE ACCESO (GETTERS Y SETTERS) ---
    // Implementan la interfaz pública para la manipulación controlada de los atributos.

    /** @return ID único del registro. */
    public int getId() { return id; }
    /** @param id Nuevo ID asignado por la BD. */
    public void setId(int id) { this.id = id; }

    /** @return Nombre de cuenta del usuario. */
    public String getUsername() { return username; }
    /** @param username Define el nombre de cuenta. */
    public void setUsername(String username) { this.username = username; }

    /** @return Contraseña almacenada. */
    public String getPassword() { return password; }
    /** @param password Define la nueva contraseña. */
    public void setPassword(String password) { this.password = password; }

    /** @return Nombre y apellido del usuario. */
    public String getFullName() { return fullName; }
    /** @param fullName Define el nombre completo. */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /** @return El rol o nivel de permiso actual. */
    public String getRole() { return role; }
    /** @param role Define el nuevo rol del usuario. */
    public void setRole(String role) { this.role = role; }
}