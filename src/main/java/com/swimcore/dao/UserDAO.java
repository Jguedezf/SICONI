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
 * FECHA: 06 de Febrero de 2026
 * HORA: 12:05 PM (Hora de Venezuela)
 * VERSIÓN: 2.5.0 (Admin Module Enabled)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Acceso a Datos (DAO). Centraliza todas las operaciones
 * transaccionales sobre la entidad 'Usuario' en el motor SQLite. Implementa
 * la lógica de persistencia CRUD y los mecanismos de autenticación del sistema.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * [DAO - PERSISTENCIA] Proveedor de servicios de datos para la entidad Usuario.
 * [DISEÑO] Implementa la separación de intereses (SoC) al aislar el código SQL
 * de la lógica de negocio y la interfaz de usuario.
 * [SEGURIDAD] Utiliza consultas parametrizadas para mitigar vulnerabilidades de
 * Inyección SQL durante los procesos de autenticación y registro.
 */
public class UserDAO {

    /**
     * Constructor de la clase.
     * Dispara la verificación de la infraestructura de tablas al instanciarse.
     */
    public UserDAO() {
        createTable();
    }

    /**
     * [DDL] Garantiza la existencia de la tabla 'users' en el esquema relacional.
     * Define restricciones de integridad como claves primarias y unicidad en nombres de cuenta.
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
            System.err.println("SICONI: Verificando tabla de usuarios...");
        }
    }

    // ========================================================================================
    //                                  LÓGICA DE AUTENTICACIÓN
    // ========================================================================================

    /**
     * Valida las credenciales de un usuario contra los registros persistidos.
     * @param username Identificador de cuenta.
     * @param password Credencial de acceso.
     * @return Objeto User si la validación es exitosa; null en caso contrario.
     */
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

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

    // ========================================================================================
    //                                  OPERACIONES CRUD (CREATE, READ, UPDATE, DELETE)
    // ========================================================================================

    /**
     * [CREATE] Persiste un nuevo objeto Usuario en la base de datos.
     * Utiliza la instrucción 'INSERT OR IGNORE' para evitar duplicidad de nombres de usuario.
     * @param user Instancia del modelo a registrar.
     * @return true si se insertó el registro exitosamente.
     */
    public boolean saveUser(User user) {
        String sql = "INSERT OR IGNORE INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("SICONI: Error registrando usuario.");
            return false;
        }
    }

    /**
     * [READ] Localiza un usuario específico basándose en su nombre de cuenta único.
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

    /**
     * [UPDATE] Actualiza las propiedades de un usuario existente en la base de datos.
     * Utiliza el nombre de usuario (username) como criterio de búsqueda para la modificación.
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET password = ?, full_name = ?, role = ? WHERE username = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getUsername());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("SICONI: Error actualizando usuario.");
            return false;
        }
    }

    /**
     * [DELETE] Elimina de forma física un registro de usuario.
     * [REGLA DE NEGOCIO] Implementa una restricción de seguridad para evitar la eliminación
     * del usuario administrador por defecto del sistema.
     */
    public boolean deleteUser(String username) {
        if ("admin".equalsIgnoreCase(username)) return false;

        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("SICONI: Error eliminando usuario.");
            return false;
        }
    }

    /**
     * [READ ALL] Recupera la colección completa de usuarios registrados.
     * @return List con objetos User mapeados desde la base de datos.
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("role")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}