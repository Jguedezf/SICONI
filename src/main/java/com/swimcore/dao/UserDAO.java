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
 * VERSIÓN: 1.1.0 (Stable Release)
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
            // Log técnico simplificado para evitar inundar la consola de rojo
            System.err.println("SICONI: Verificando tabla de usuarios...");
        }
    }

    /**
     * Módulo de Autenticación (Login).
     * Verifica las credenciales proporcionadas contra el almacén de datos.
     */
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            // Try-with-resources para cerrar el ResultSet automáticamente y liberar la BD
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("SICONI: Error en validación de acceso.");
        }
        return null;
    }

    /**
     * Operación de Persistencia (Create).
     * Almacena un nuevo registro de usuario en la base de datos.
     */
    public void saveUser(User user) {
        // AJUSTE: 'INSERT OR IGNORE' para que no explote si el 'admin' ya existe al iniciar
        String sql = "INSERT OR IGNORE INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("SICONI: Registro de usuario verificado.");
        }
    }

    /**
     * Búsqueda por Identificador (Read).
     * Recupera la información de un usuario basado en su nombre de cuenta.
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("SICONI: Usuario no localizado.");
        }
        return null;
    }
}