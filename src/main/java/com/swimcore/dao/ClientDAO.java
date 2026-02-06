/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ClientDAO.java
 * VERSIÓN: 2.8.0 (RECOVERY: Restored deleteClient + Receipt Support)
 * FECHA: 05 de Febrero de 2026 - 12:20 AM (Venezuela)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public String generateNextCode() {
        String nextCode = "DG-0001";
        String sql = "SELECT code FROM clients ORDER BY id DESC LIMIT 1";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String lastCode = rs.getString("code");
                if (lastCode != null && lastCode.startsWith("DG-")) {
                    try {
                        int num = Integer.parseInt(lastCode.substring(3));
                        nextCode = String.format("DG-%04d", num + 1);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return nextCode;
    }

    public boolean saveClient(Client c) {
        String sql = "INSERT INTO clients (code, id_type, id_number, full_name, phone, email, instagram, is_vip, athlete_name, birth_date, club_name, category, measurements, address, profession, phone_alt, size) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, c.getCode() != null ? c.getCode() : generateNextCode());
            pst.setString(2, c.getIdType());
            pst.setString(3, c.getIdNumber());
            pst.setString(4, c.getFullName());
            pst.setString(5, c.getPhone());
            pst.setString(6, c.getEmail());
            pst.setString(7, c.getInstagram());
            pst.setInt(8, c.isVip() ? 1 : 0);
            pst.setString(9, c.getAthleteName());
            pst.setString(10, c.getBirthDate());
            pst.setString(11, c.getClub());
            pst.setString(12, c.getCategory());
            pst.setString(13, c.getMeasurements());
            pst.setString(14, c.getAddress());
            pst.setString(15, c.getProfession());
            pst.setString(16, c.getAlternatePhone());
            pst.setString(17, c.getSize());
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateClient(Client c) {
        String sql = "UPDATE clients SET id_type=?, id_number=?, full_name=?, phone=?, email=?, instagram=?, is_vip=?, athlete_name=?, birth_date=?, club_name=?, category=?, measurements=?, address=?, profession=?, phone_alt=?, size=? WHERE code=?";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, c.getIdType());
            pst.setString(2, c.getIdNumber());
            pst.setString(3, c.getFullName());
            pst.setString(4, c.getPhone());
            pst.setString(5, c.getEmail());
            pst.setString(6, c.getInstagram());
            pst.setInt(7, c.isVip() ? 1 : 0);
            pst.setString(8, c.getAthleteName());
            pst.setString(9, c.getBirthDate());
            pst.setString(10, c.getClub());
            pst.setString(11, c.getCategory());
            pst.setString(12, c.getMeasurements());
            pst.setString(13, c.getAddress());
            pst.setString(14, c.getProfession());
            pst.setString(15, c.getAlternatePhone());
            pst.setString(16, c.getSize());
            pst.setString(17, c.getCode());
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Client> getAllClients() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY id DESC";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(mapClient(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- EL MÉTODO QUE FALTABA (RESTAURADO) ---
    public boolean deleteClient(String code) {
        String sql = "DELETE FROM clients WHERE code = ?";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, code);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Client getClientById(int id) {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return mapClient(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Client getClientByCode(String code) {
        String sql = "SELECT * FROM clients WHERE code = ?";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, code);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return mapClient(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Client getClientByIdNumber(String id) {
        String sql = "SELECT * FROM clients WHERE REPLACE(id_number, '.', '') = REPLACE(?, '.', '')";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return mapClient(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private Client mapClient(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"), rs.getString("code"), rs.getString("id_type"),
                rs.getString("id_number"), rs.getString("full_name"), rs.getString("phone"),
                rs.getString("email"), rs.getString("instagram"), rs.getInt("is_vip") == 1,
                rs.getString("athlete_name"), rs.getString("birth_date"), rs.getString("club_name"),
                rs.getString("category"), rs.getString("measurements"), rs.getString("address"),
                rs.getString("profession"), rs.getString("phone_alt"),
                rs.getString("size")
        );
    }
}