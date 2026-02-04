/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ClientController.java
 * VERSIÓN: 3.2.1 (SICONI Integration Verified)
 * FECHA: 04 de Febrero de 2026 - 04:40 PM
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
        if (client.getFullName() == null || client.getFullName().trim().isEmpty()) {
            System.err.println("Validación Fallida: El nombre es obligatorio.");
            return false;
        }

        if (client.getCode() == null || client.getCode().trim().isEmpty()) {
            client.setCode(clientDAO.generateNextCode());
        }

        return clientDAO.saveClient(client);
    }

    public String generateNextCode() {
        return clientDAO.generateNextCode();
    }

    public boolean updateClient(Client client) {
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
        String cleanTerm = term.replace(".", "").trim();
        return clientDAO.getClientByIdNumber(cleanTerm);
    }
}