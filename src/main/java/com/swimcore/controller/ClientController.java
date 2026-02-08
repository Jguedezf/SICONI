/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Técnicas de Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ClientController.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: 06 de Febrero de 2026
 * VERSIÓN: 3.2.2 (Certified for SICONI v8.0)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Controlador (Controller Layer). Actúa como el motor de
 * reglas de negocio para la gestión de clientes y atletas. Media entre la
 * interfaz de usuario (ClientManagementDialog) y la persistencia (ClientDAO),
 * asegurando que los datos cumplan con las restricciones de integridad antes
 * de su almacenamiento.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.ClientDAO;
import com.swimcore.model.Client;
import java.util.List;

/**
 * [CONTROLLER - CRM] Controlador de Gestión de Clientes.
 * [DISEÑO] Implementa el patrón MVC, encapsulando la lógica operativa y
 * garantizando el desacoplamiento entre la vista y el motor de base de datos.
 * [REQUERIMIENTO FUNCIONAL] Gestiona la validación de campos obligatorios, la
 * normalización de documentos de identidad (DNI) y la asignación de correlativos.
 */
public class ClientController {

    // ========================================================================================
    //                                  ATRIBUTOS (DEPENDENCIAS)
    // ========================================================================================

    // Referencia a la capa de persistencia mediante Composición.
    private final ClientDAO clientDAO;

    /**
     * Constructor de la clase.
     * Inicializa el objeto de acceso a datos necesario para las operaciones del módulo.
     */
    public ClientController() {
        this.clientDAO = new ClientDAO();
    }

    // ========================================================================================
    //                                  LÓGICA DE NEGOCIO (CLIENTES)
    // ========================================================================================

    /**
     * [CREATE] Gestiona el registro de un nuevo cliente con validación previa.
     * Verifica la obligatoriedad del nombre y automatiza la generación del código
     * correlativo (DG-XXXX) si este se encuentra ausente.
     * * @param client Instancia de la entidad Client a procesar.
     * @return booleano: true si el registro superó las validaciones y fue persistido.
     */
    public boolean saveClient(Client client) {
        // [VALIDACIÓN] Verificación de precondiciones de datos obligatorios.
        if (client.getFullName() == null || client.getFullName().trim().isEmpty()) {
            System.err.println("Validación Fallida: El nombre es obligatorio.");
            return false;
        }

        // [AUTOMATIZACIÓN] Asignación de identificador de negocio si no existe.
        if (client.getCode() == null || client.getCode().trim().isEmpty()) {
            client.setCode(clientDAO.generateNextCode());
        }

        return clientDAO.saveClient(client);
    }

    /**
     * [READ] Delega la generación del siguiente correlativo de cliente a la capa DAO.
     */
    public String generateNextCode() {
        return clientDAO.generateNextCode();
    }

    /**
     * [UPDATE] Coordina la actualización de perfiles de clientes existentes.
     * Valida que el código identificador esté presente para asegurar la integridad de la operación.
     */
    public boolean updateClient(Client client) {
        if (client.getCode() == null || client.getCode().trim().isEmpty()) {
            return false;
        }
        return clientDAO.updateClient(client);
    }

    /**
     * [DELETE] Gestiona la eliminación física de registros de clientes.
     * @param clientCode Código único del cliente a remover del sistema.
     */
    public boolean deleteClient(String clientCode) {
        if (clientCode == null || clientCode.trim().isEmpty()) {
            return false;
        }
        return clientDAO.deleteClient(clientCode);
    }

    /** [READ ALL] Recupera la nómina completa de clientes registrados. */
    public List<Client> getAllClients() {
        return clientDAO.getAllClients();
    }

    /**
     * [SEARCH] Implementa la lógica de búsqueda inteligente por Documento de Identidad.
     * [NORMALIZACIÓN] Realiza un "sanitizado" del término de búsqueda eliminando
     * puntos para coincidir con el formato de almacenamiento de la base de datos.
     * * @param term Número de identificación ingresado (DNI/RIF).
     * @return Objeto Client localizado o null si no hay coincidencia.
     */
    public Client findClientByDNI(String term) {
        if (term == null || term.trim().isEmpty()) return null;

        // Se eliminan los separadores de miles para estandarizar la búsqueda.
        String cleanTerm = term.replace(".", "").trim();
        return clientDAO.getClientByIdNumber(cleanTerm);
    }
}