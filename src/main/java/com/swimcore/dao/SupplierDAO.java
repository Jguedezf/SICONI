/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: SupplierDAO.java
 * DESCRIPCIÓN: Controlador de base de datos para proveedores.
 * -----------------------------------------------------------------------------
 */
package com.swimcore.dao;

import com.swimcore.model.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public boolean save(Supplier s) {
        String sql;
        if (s.getId() == 0) {
            sql = "INSERT INTO suppliers (company, contact, phone, email, address, instagram, whatsapp) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE suppliers SET company=?, contact=?, phone=?, email=?, address=?, instagram=?, whatsapp=? WHERE id=?";
        }

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
            e.printStackTrace();
            return false;
        }
    }

    public List<Supplier> getAll() {
        List<Supplier> list = new ArrayList<>();
        // CORRECCIÓN CLAVE: Ordenar por ID ascendente (1, 2, 3...)
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