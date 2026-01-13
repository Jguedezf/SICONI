/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: UserDAO.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Acceso a Datos (Data Access Object).
 * Centraliza todas las operaciones de persistencia relacionadas con la entidad 'Usuario'
 * utilizando la API JDBC para la comunicación con el motor SQLite.
 *
 * Responsabilidades de Ingeniería:
 * 1. Gestión de Esquema: Implementa rutinas DDL para asegurar la existencia de la tabla
 * de usuarios al instanciarse la clase.
 * 2. Seguridad en Consultas: Implementa consultas parametrizadas (`PreparedStatement`)
 * para mitigar vulnerabilidades de Inyección SQL durante el proceso de login.
 * 3. Mapeo Objeto-Relacional (ORM Manual): Transforma los conjuntos de resultados (ResultSet)
 * en instancias de la clase de modelo `User`.
 *
 * PRINCIPIOS POO:
 * - ABSTRACCIÓN: Separa la complejidad de las consultas SQL de la lógica de la interfaz.
 * - ENCAPSULAMIENTO: Centraliza la lógica de persistencia, exponiendo solo métodos
 * funcionales de alto nivel.
 *
 * PATRONES DE DISEÑO IMPLEMENTADOS:
 * - DAO (Data Access Object): Proporciona una interfaz abstracta para la base de datos.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Proveedor de servicios de datos para la entidad Usuario.
 */
public class UserDAO {

    /**
     * Constructor.
     * Garantiza que la infraestructura de tablas esté lista antes de cualquier operación.
     */
    public UserDAO() {
        createTable();
    }

    /**
     * Rutina DDL (Data Definition Language).
     * Crea la tabla 'users' con restricciones de integridad (PRIMARY KEY, UNIQUE, NOT NULL).
     */
    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "full_name TEXT, " +
                "role TEXT)";
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Módulo de Autenticación (Login).
     * Verifica las credenciales proporcionadas contra el almacén de datos.
     * * @param username Identificador de cuenta.
     * @param password Credencial de acceso.
     * @return Instancia del usuario autenticado o null si la validación falla.
     */
    public User login(String username, String password) {
        // Uso de comodines (?) para parametrización de seguridad
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Mapeo manual de ResultSet a Objeto de Modelo
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("role")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Feedback negativo para la Capa de Vista
    }

    /**
     * Operación de Persistencia (Create).
     * Almacena un nuevo registro de usuario en la base de datos.
     * * @param user Objeto con los datos a persistir.
     */
    public void saveUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Búsqueda por Identificador (Read).
     * Recupera la información de un usuario basado en su nombre de cuenta.
     * * @param username Nombre de usuario a buscar.
     * @return Instancia de User poblada con datos o null.
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("role")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}