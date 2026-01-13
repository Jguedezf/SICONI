/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: SupplierDAO.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Acceso a Datos (Data Access Object) para la entidad 'Proveedor'.
 * Centraliza la lógica de persistencia utilizando la API JDBC para interactuar con SQLite.
 *
 * Responsabilidades de Ingeniería:
 * 1. Operaciones CRUD (Create, Read, Update, Delete): Implementa la lógica completa para el
 * ciclo de vida de los datos de proveedores en la base de datos.
 * 2. Sentencias Parametrizadas: Utiliza `PreparedStatement` para garantizar la seguridad
 * contra ataques de Inyección SQL y optimizar la ejecución de consultas.
 * 3. Mapeo de Datos (Hydration): Transforma registros relacionales (ResultSet) en objetos
 * de dominio (Supplier), incluyendo campos modernos de contacto digital.
 *
 * PRINCIPIOS POO:
 * - ABSTRACCIÓN: Aísla la complejidad de las sentencias SQL (INSERT, UPDATE, DELETE)
 * de la interfaz de usuario.
 * - ENCAPSULAMIENTO: Gestiona de forma interna la apertura y cierre de recursos JDBC
 * mediante el patrón "Try-with-resources".
 *
 * PATRONES DE DISEÑO IMPLEMENTADOS:
 * - DAO (Data Access Object): Provee una interfaz abstracta para la capa de persistencia.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Proveedor de servicios de datos para la entidad Proveedor.
 * Gestiona el almacenamiento y recuperación de información corporativa y social.
 */
public class SupplierDAO {

    /**
     * Operación de Persistencia Dual (Create / Update).
     * Determina automáticamente si debe realizar una inserción o una actualización
     * basándose en el identificador del objeto.
     * * @param s Instancia del proveedor a procesar.
     * @return true si la operación en la base de datos fue exitosa.
     */
    public boolean save(Supplier s) {
        String sql;
        // Lógica de decisión: ID 0 indica un registro nuevo (Auto-increment)
        if (s.getId() == 0) {
            // DML: Inserción de 7 campos transaccionales
            sql = "INSERT INTO suppliers (company, contact, phone, email, address, instagram, whatsapp) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            // DML: Actualización basada en Clave Primaria (PK)
            sql = "UPDATE suppliers SET company=?, contact=?, phone=?, email=?, address=?, instagram=?, whatsapp=? WHERE id=?";
        }

        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Mapeo de parámetros comunes (1 al 7)
            pstmt.setString(1, s.getCompany());
            pstmt.setString(2, s.getContact());
            pstmt.setString(3, s.getPhone());
            pstmt.setString(4, s.getEmail());
            pstmt.setString(5, s.getAddress());
            pstmt.setString(6, s.getInstagram());
            pstmt.setString(7, s.getWhatsapp());

            // En caso de UPDATE, se añade el parámetro de filtrado (ID) en la posición 8
            if (s.getId() != 0) {
                pstmt.setInt(8, s.getId());
            }

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // Log de trazabilidad para depuración
            return false;
        }
    }

    /**
     * Operación de Eliminación (Delete).
     * Remueve físicamente un registro de la tabla basándose en su ID.
     * * @param id Identificador único del proveedor.
     * @return true si el registro fue eliminado correctamente.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Operación de Recuperación Masiva (Read All).
     * Consulta todos los proveedores registrados, ordenándolos alfabéticamente.
     * * @return Lista de objetos Supplier poblada con datos de la BD.
     */
    public List<Supplier> getAll() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY company";

        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Reconstrucción de objetos (Data Hydration)
                // Se incluyen los campos de redes sociales para asegurar la integridad del modelo
                list.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("company"),
                        rs.getString("contact"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("instagram"),
                        rs.getString("whatsapp")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}