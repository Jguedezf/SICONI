package com.swimcore.dao;

import com.swimcore.model.Client;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Client.
 * Encapsula toda la lógica de acceso a la tabla 'clientes'.
 */
public class ClientDAO {

    /**
     * Guarda un nuevo cliente en la base de datos.
     * @param client El objeto Client a guardar.
     * @return true si se guardó correctamente, false en caso de error.
     */
    public boolean saveClient(Client client) {
        String sql = "INSERT INTO clientes(id_cliente, nombre, telefono, email, tipo, fecha_nacimiento, es_vip, medidas_resumen) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, client.getId());
            pst.setString(2, client.getName());
            pst.setString(3, client.getPhone());
            pst.setString(4, client.getEmail());
            pst.setString(5, client.getType());
            pst.setString(6, client.getBirthDate());
            pst.setBoolean(7, client.isVip());
            pst.setString(8, client.getMeasurements());

            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error guardando cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza los datos de un cliente existente.
     * @param client El objeto Client con los datos actualizados.
     * @return true si se actualizó correctamente, false en caso de error.
     */
    public boolean updateClient(Client client) {
        String sql = "UPDATE clientes SET nombre = ?, telefono = ?, email = ?, tipo = ?, fecha_nacimiento = ?, es_vip = ?, medidas_resumen = ? WHERE id_cliente = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, client.getName());
            pst.setString(2, client.getPhone());
            pst.setString(3, client.getEmail());
            pst.setString(4, client.getType());
            pst.setString(5, client.getBirthDate());
            pst.setBoolean(6, client.isVip());
            pst.setString(7, client.getMeasurements());
            pst.setString(8, client.getId());

            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error actualizando cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un cliente de la base de datos por su ID.
     * @param clientId El ID del cliente a eliminar.
     * @return true si se eliminó correctamente, false en caso de error.
     */
    public boolean deleteClient(String clientId) {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, clientId);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error eliminando cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una lista con todos los clientes de la base de datos.
     * @return Una lista de objetos Client.
     */
    public List<Client> getAllClients() {
        List<Client> clientList = new ArrayList<>();
        String sql = "SELECT * FROM clientes";
        try (Connection con = Conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Client client = new Client();
                client.setId(rs.getString("id_cliente"));
                client.setName(rs.getString("nombre"));
                client.setPhone(rs.getString("telefono"));
                client.setEmail(rs.getString("email"));
                client.setType(rs.getString("tipo"));
                client.setBirthDate(rs.getString("fecha_nacimiento"));
                client.setVip(rs.getBoolean("es_vip"));
                client.setMeasurements(rs.getString("medidas_resumen"));
                clientList.add(client);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo todos los clientes: " + e.getMessage());
        }
        return clientList;
    }
}