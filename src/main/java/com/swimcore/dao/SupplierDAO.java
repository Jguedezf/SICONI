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
 * FECHA: 06 de Febrero de 2026
 * HORA: 07:21 PM (Hora de Venezuela)
 * VERSIÓN: 1.0.1 (Stable Persistence Layer)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Acceso a Datos (DAO). Centraliza la lógica
 * transaccional para la entidad 'Proveedor' (Supplier). Gestiona el ciclo de
 * vida de los registros en la base de datos SQLite, permitiendo la integración
 * de la cadena de suministros con el inventario del sistema SICONI.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * [DAO - PERSISTENCIA] Objeto de Acceso a Datos para la gestión de Proveedores.
 * [POO - ABSTRACCIÓN] Aisla las consultas SQL del resto de la aplicación,
 * proporcionando una interfaz limpia para las operaciones CRUD.
 * [SEGURIDAD] Implementa PreparedStatements para garantizar la integridad de
 * las consultas y prevenir ataques de Inyección SQL.
 */
public class SupplierDAO {

    // ========================================================================================
    //                                  OPERACIONES DE PERSISTENCIA
    // ========================================================================================

    /**
     * [UPSERT] Persiste o actualiza un registro de proveedor.
     * Implementa una lógica de bifurcación basada en el estado del identificador (id):
     * - Si el ID es 0, se interpreta como una entidad nueva (INSERT).
     * - Si el ID es mayor a 0, se interpreta como una entidad existente (UPDATE).
     * * @param s Instancia de la entidad Supplier a procesar.
     * @return true si la operación en la base de datos fue exitosa.
     */
    public boolean save(Supplier s) {
        String sql;
        if (s.getId() == 0) {
            // Operación de Inserción: Se omiten las columnas autoincrementales.
            sql = "INSERT INTO suppliers (company, contact, phone, email, address, instagram, whatsapp) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            // Operación de Actualización: Filtrado por Clave Primaria (PK).
            sql = "UPDATE suppliers SET company=?, contact=?, phone=?, email=?, address=?, instagram=?, whatsapp=? WHERE id=?";
        }

        // [TÉCNICO] Uso de try-with-resources para garantizar el cierre automático de conexiones.
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getCompany());
            pstmt.setString(2, s.getContact());
            pstmt.setString(3, s.getPhone());
            pstmt.setString(4, s.getEmail());
            pstmt.setString(5, s.getAddress());
            pstmt.setString(6, s.getInstagram());
            pstmt.setString(7, s.getWhatsapp());

            if (s.getId() != 0) {
                pstmt.setInt(8, s.getId());
            }

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Registro de errores en la consola de diagnóstico para depuración de la capa DAO.
            e.printStackTrace();
            return false;
        }
    }

    /**
     * [READ ALL] Recupera la lista completa de proveedores registrados.
     * @return List con objetos Supplier mapeados desde el ResultSet.
     */
    public List<Supplier> getAll() {
        List<Supplier> list = new ArrayList<>();
        // [REQUERIMIENTO] Ordenamiento explícito por ID para mantener la consistencia en UI.
        String sql = "SELECT * FROM suppliers ORDER BY id ASC";

        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Supplier s = new Supplier();
                s.setId(rs.getInt("id"));
                s.setCompany(rs.getString("company"));
                s.setContact(rs.getString("contact"));
                s.setPhone(rs.getString("phone"));
                s.setEmail(rs.getString("email"));
                s.setAddress(rs.getString("address"));
                s.setInstagram(rs.getString("instagram"));
                s.setWhatsapp(rs.getString("whatsapp"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * [DELETE] Elimina un registro de forma física mediante su identificador.
     * @param id Identificador único del proveedor a eliminar.
     * @return true si el registro fue localizado y removido satisfactoriamente.
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
     * [READ BY ID] Localiza un proveedor específico para operaciones de edición o consulta.
     * @param id Identificador a buscar.
     * @return Objeto Supplier poblado o null si no existe coincidencia en la BD.
     */
    public Supplier getById(int id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Supplier(
                        rs.getInt("id"), rs.getString("company"), rs.getString("contact"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("address"),
                        rs.getString("instagram"), rs.getString("whatsapp")
                );
            }
        } catch (SQLException e) {}
        return null;
    }
}