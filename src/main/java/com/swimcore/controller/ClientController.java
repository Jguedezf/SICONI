/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ClientController.java
 * VERSIÓN: 2.2.0 (CRUD Enabled)
 * DESCRIPCIÓN: Controlador actualizado. Ahora expone metodos para editar.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.ClientDAO;
import com.swimcore.model.Client;
import java.util.List;

public class ClientController {

    private final ClientDAO clientDAO;

    public ClientController() {
        this.clientDAO = new ClientDAO();
    }

    public boolean saveClient(Client client) {
        // REGLA DE NEGOCIO 1: NOMBRE OBLIGATORIO
        if (client.getFullName() == null || client.getFullName().trim().isEmpty()) {
            System.err.println("Validación Fallida: El nombre es obligatorio.");
            return false;
        }

        // REGLA DE NEGOCIO 2: GENERACIÓN DE ID
        if (client.getCode() == null || client.getCode().trim().isEmpty()) {
            String newCode = clientDAO.generateNextCode();
            client.setCode(newCode);
        }

        return clientDAO.saveClient(client);
    }

    // --- NUEVO MÉTODO PARA EDITAR ---
    public boolean updateClient(Client client) {
        // Validación: Debe tener código para saber a quién editar
        if (client.getCode() == null || client.getCode().trim().isEmpty()) {
            return false;
        }
        return clientDAO.updateClient(client);
    }

    public boolean deleteClient(String clientCode) {
        if (clientCode == null || clientCode.trim().isEmpty()) {
            return false;
        }
        return clientDAO.deleteClient(clientCode);
    }

    public List<Client> getAllClients() {
        return clientDAO.getAllClients();
    }

    public Client findClientByDNI(String term) {
        if (term == null || term.trim().isEmpty()) return null;
        return clientDAO.getClientByIdNumber(term);
    }
}