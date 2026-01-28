/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ClientDAO.java
 * VERSIÓN: 2.5.0 (Full Persistence Upgrade)
 * DESCRIPCIÓN: DAO actualizado para soportar 'profession' y 'phone_alt'.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Client;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public String generateNextCode() {
        String nextCode = "DG-0001";
        String sql = "SELECT code FROM clients ORDER BY id DESC LIMIT 1";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String lastCode = rs.getString("code");
                if (lastCode != null && lastCode.startsWith("DG-")) {
                    try {
                        int num = Integer.parseInt(lastCode.substring(3));
                        nextCode = String.format("DG-%04d", num + 1);
                    } catch (NumberFormatException ignored) {
                        System.err.println("Info: Reiniciando contador de códigos de cliente.");
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return nextCode;
    }

    public boolean saveClient(Client c) {
        // SQL actualizado con profession y phone_alt
        String sql = "INSERT INTO clients (code, id_type, id_number, full_name, phone, email, instagram, is_vip, athlete_name, birth_date, club_name, category, measurements, address, profession, phone_alt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            pst.setString(15, c.getProfession());   // Nuevo
            pst.setString(16, c.getAlternatePhone()); // Nuevo
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error INSERT ClientDAO: " + e.getMessage());
            return false;
        }
    }

    public boolean updateClient(Client c) {
        // SQL actualizado para modificar profession y phone_alt
        String sql = "UPDATE clients SET id_type=?, id_number=?, full_name=?, phone=?, email=?, instagram=?, is_vip=?, athlete_name=?, birth_date=?, club_name=?, category=?, measurements=?, address=?, profession=?, phone_alt=? WHERE code=?";
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
            pst.setString(14, c.getProfession());   // Nuevo
            pst.setString(15, c.getAlternatePhone()); // Nuevo
            pst.setString(16, c.getCode());
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error UPDATE ClientDAO: " + e.getMessage());
            return false;
        }
    }

    public List<Client> getAllClients() {
        List<Client> clientList = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY id DESC";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) clientList.add(mapClient(rs));
        } catch (SQLException e) {
            System.err.println("❌ Error SELECT ALL ClientDAO: " + e.getMessage());
        }
        return clientList;
    }

    public boolean deleteClient(String clientCode) {
        String sql = "DELETE FROM clients WHERE code = ?";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, clientCode);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    public Client getClientByIdNumber(String idNumber) {
        String sql = "SELECT * FROM clients WHERE REPLACE(id_number, '.', '') = REPLACE(?, '.', '')";
        try (Connection con = Conexion.conectar(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, idNumber);
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
                rs.getString("profession"), rs.getString("phone_alt") // <--- Cargamos los nuevos datos
        );
    }
}