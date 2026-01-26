/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ClientDAO.java
 * VERSIÓN: 2.3.0 (CRUD Full + Address)
 * DESCRIPCIÓN: DAO Potenciado. Soporta Insertar, Editar, Eliminar y Leer
 * incluyendo el nuevo campo de dirección.
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
                    String numberPart = lastCode.substring(3);
                    try {
                        int currentNum = Integer.parseInt(numberPart);
                        int nextNum = currentNum + 1;
                        nextCode = String.format("DG-%04d", nextNum);
                    } catch (NumberFormatException e) {
                        System.err.println("Info: Reiniciando contador.");
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return nextCode;
    }

    // --- GUARDAR (INSERT) ---
    public boolean saveClient(Client c) {
        String sql = "INSERT INTO clients (code, id_type, id_number, full_name, phone, email, instagram, is_vip, athlete_name, birth_date, club_name, category, measurements, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, c.getCode());
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
            pst.setString(14, c.getAddress()); // <--- CAMPO NUEVO

            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error INSERT: " + e.getMessage());
            return false;
        }
    }

    // --- EDITAR (UPDATE) - ¡NUEVO! ---
    public boolean updateClient(Client c) {
        // Actualiza todo basándose en el código único
        String sql = "UPDATE clients SET id_type=?, id_number=?, full_name=?, phone=?, email=?, instagram=?, is_vip=?, " +
                "athlete_name=?, birth_date=?, club_name=?, category=?, measurements=?, address=? WHERE code=?";

        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

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
            pst.setString(13, c.getAddress()); // <--- CAMPO NUEVO

            // EL WHERE
            pst.setString(14, c.getCode());

            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error UPDATE: " + e.getMessage());
            return false;
        }
    }

    // --- LEER TODOS (SELECT) ---
    public List<Client> getAllClients() {
        List<Client> clientList = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY id DESC";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                clientList.add(mapClient(rs)); // Usamos el helper
            }
        } catch (SQLException e) {
            System.err.println("❌ Error SELECT ALL: " + e.getMessage());
        }
        return clientList;
    }

    // --- ELIMINAR (DELETE) ---
    public boolean deleteClient(String clientCode) {
        String sql = "DELETE FROM clients WHERE code = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, clientCode);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    // --- BUSCAR UNO ---
    public Client getClientByIdNumber(String idNumber) {
        String sql = "SELECT * FROM clients WHERE id_number = ? OR code = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, idNumber);
            pst.setString(2, idNumber);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapClient(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // --- HELPER PARA NO REPETIR CÓDIGO ---
    private Client mapClient(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("id_type"),
                rs.getString("id_number"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("instagram"),
                rs.getInt("is_vip") == 1,
                rs.getString("athlete_name"),
                rs.getString("birth_date"),
                rs.getString("club_name"),
                rs.getString("category"),
                rs.getString("measurements"),
                rs.getString("address") // <--- RECUPERAMOS ADDRESS
        );
    }
}