/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ClientController.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Controlador (Controller Layer).
 * Actúa como mediador entre la interfaz de gestión de clientes (ClientManagementDialog)
 * y la persistencia de datos (ClientDAO).
 *
 * Características de Ingeniería:
 * 1. Lógica de Validación: Implementa reglas de integridad de negocio antes de
 * permitir que los datos alcancen la capa de persistencia.
 * 2. Desacoplamiento: Sigue el patrón MVC al evitar que la vista manipule
 * directamente la base de datos, facilitando el mantenimiento y las pruebas unitarias.
 * 3. Gestión de Flujo: Coordina las operaciones CRUD (Create, Read, Delete)
 * solicitadas por el usuario a través de la interfaz visual.
 *
 * PRINCIPIOS POO:
 * - ENCAPSULAMIENTO: Protege el acceso al DAO mediante métodos funcionales.
 * - RESPONSABILIDAD ÚNICA (SRP): Se enfoca exclusivamente en la orquestación
 * de la entidad 'Cliente'.
 *
 * PATRONES DE DISEÑO:
 * - MVC (Model-View-Controller): Implementa el rol de controlador para la
 * gestión de clientes.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.ClientDAO;
import com.swimcore.model.Client;
import java.util.List;

/**
 * Controlador de Gestión de Clientes.
 * Proporciona los servicios de validación y mediación para el registro de atletas y clubes.
 */
public class ClientController {

    // Dependencia de la Capa de Acceso a Datos
    private final ClientDAO clientDAO;

    /**
     * Constructor.
     * Inicializa la conexión con el DAO de clientes.
     */
    public ClientController() {
        this.clientDAO = new ClientDAO();
    }

    /**
     * Procesa la solicitud para persistir un cliente.
     * Implementa una capa de validación preventiva (Sanitización).
     * * @param client Objeto con los datos capturados en el formulario.
     * @return true si los datos son válidos y la persistencia fue confirmada por el DAO.
     */
    public boolean saveClient(Client client) {
        // --- REGLA DE NEGOCIO: VALIDACIÓN DE INTEGRIDAD ---
        // Se valida que el nombre no sea nulo ni consista únicamente en espacios en blanco.
        if (client.getName() == null || client.getName().trim().isEmpty()) {
            System.err.println("Validación de Negocio Fallida: El nombre del cliente es un campo obligatorio.");
            return false;
        }

        // Delegación de la operación a la Capa de Datos (DAO)
        return clientDAO.saveClient(client);
    }

    /**
     * Ejecuta la solicitud de baja de un cliente.
     * * @param clientId Identificador único (Cédula/ID) del cliente a remover.
     * @return true si el proceso de eliminación fue exitoso.
     */
    public boolean deleteClient(String clientId) {
        // Validación de parámetro de búsqueda
        if (clientId == null || clientId.trim().isEmpty()) {
            return false;
        }
        return clientDAO.deleteClient(clientId);
    }

    /**
     * Recupera el catálogo completo de clientes registrados.
     * * @return Lista de objetos Client recuperada desde la persistencia SQLite.
     */
    public List<Client> getAllClients() {
        return clientDAO.getAllClients();
    }
}