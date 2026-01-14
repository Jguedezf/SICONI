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
 * VERSIÓN: 1.1.0 (Persistent Social Data & POO Build)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Acceso a Datos (DAO). Gestiona el ciclo de vida (CRUD)
 * de la entidad Proveedor en SQLite. Implementa lógica de mapeo relacional
 * para capturar datos de contacto digital y geográfico.
 * * PRINCIPIOS POO APLICADOS:
 * - ABSTRACCIÓN: El sistema consume métodos como 'save' o 'getAll' sin conocer
 * las consultas SQL subyacentes.
 * - ENCAPSULAMIENTO: Centraliza el manejo de conexiones JDBC protegiendo la
 * integridad de la base de datos mediante PreparedStatement.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de Persistencia para Proveedores.
 * Proporciona una interfaz abstracta para la administración de la base de datos.
 */
public class SupplierDAO {

    /**
     * Persistencia Persistente (Create / Update).
     * Sincroniza el objeto Supplier con la tabla física mediante lógica dual.
     * @param s Instancia del proveedor a procesar.
     * @return true si la operación SQL se completó sin errores.
     */
    public boolean save(Supplier s) {
        String sql;
        // Lógica de decisión: Si ID es 0, es un registro nuevo.
        if (s.getId() == 0) {
            sql = "INSERT INTO suppliers (company, contact, phone, email, address, instagram, whatsapp) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE suppliers SET company=?, contact=?, phone=?, email=?, address=?, instagram=?, whatsapp=? WHERE id=?";
        }

        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Mapeo de parámetros transaccionales
            pstmt.setString(1, s.getCompany());
            pstmt.setString(2, s.getContact());
            pstmt.setString(3, s.getPhone());
            pstmt.setString(4, s.getEmail());
            pstmt.setString(5, s.getAddress());
            pstmt.setString(6, s.getInstagram());
            pstmt.setString(7, s.getWhatsapp());

            // Inyección de clave primaria para operaciones de actualización
            if (s.getId() != 0) {
                pstmt.setInt(8, s.getId());
            }

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error en persistencia SupplierDAO: " + e.getMessage());
            return false;
        }
    }

    /**
     * Eliminación de Registro (Delete).
     * @param id Clave primaria del proveedor a remover.
     * @return true si la fila fue eliminada exitosamente.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Recuperación Maestra de Proveedores (Read All).
     * Implementa el patrón Data Hydration para reconstruir objetos desde ResultSet.
     * @return Lista de entidades Supplier ordenadas alfabéticamente.
     */
    public List<Supplier> getAll() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY company ASC";

        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Reconstrucción del objeto de dominio (Mapeo Objeto-Relacional manual)
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