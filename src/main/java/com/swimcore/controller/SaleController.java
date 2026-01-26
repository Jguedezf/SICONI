/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: SaleController.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 2.1.0 (PDF Receipt Integration)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Controlador (Controller Layer).
 * Actúa como intermediario puro entre la Interfaz Gráfica (View) y la Capa de
 * Acceso a Datos (DAO).
 *
 * Características de Ingeniería:
 * 1. Desacoplamiento (High Cohesion): Se eliminó toda lógica SQL de esta clase.
 * 2. Validación de Entrada: Asegura integridad de datos previos al procesamiento.
 * 3. Automatización de Salida: Genera automáticamente el recibo PDF tras el
 * registro exitoso de la transacción.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.ClientDAO;
import com.swimcore.dao.SaleDAO;
import com.swimcore.model.Client;
import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import com.swimcore.util.ReceiptGenerator; // Importante: Generador de Recibos
import java.util.List;

/**
 * Controlador de Gestión de Ventas.
 * Orquestador de las operaciones comerciales. Coordina la comunicación entre
 * la pantalla de facturación, el motor de base de datos y el generador de reportes.
 */
public class SaleController {

    // ATRIBUTOS: Instancias de DAO (Colaboración entre clases)
    private final SaleDAO saleDAO;
    private final ClientDAO clientDAO; // Necesario para buscar datos del recibo

    /**
     * Constructor.
     * Inicializa las dependencias necesarias.
     */
    public SaleController() {
        this.saleDAO = new SaleDAO();
        this.clientDAO = new ClientDAO();
    }

    /**
     * Procesa la solicitud de registro de una nueva venta.
     *
     * ENTRADA:
     * @param sale Objeto 'Sale' con la metadata de la factura.
     * @param details Lista de objetos 'SaleDetail' con los productos.
     *
     * PROCESO:
     * 1. Valida integridad de datos.
     * 2. Delega persistencia al DAO.
     * 3. Si es exitoso, invoca la generación del Recibo PDF.
     *
     * SALIDA:
     * @return true si la operación fue exitosa en la base de datos.
     */
    public boolean registerSale(Sale sale, List<SaleDetail> details) {
        // VALIDACIÓN DE INTEGRIDAD (Validación Previa - Tu lógica original)
        if (sale == null || details == null || details.isEmpty()) {
            System.err.println("❌ Error de Validación: Intento de venta vacía o nula.");
            return false;
        }

        // DELEGACIÓN (Llamada al DAO)
        // El controlador pasa la responsabilidad de la transacción ACID al DAO.
        boolean success = saleDAO.registerSale(sale, details);

        // LÓGICA DE CIERRE: Generación de Recibo (Solo si guardó bien)
        if (success) {
            generarReciboPDF(sale, details);
        }

        return success;
    }

    /**
     * Método auxiliar privado para manejar la generación del PDF.
     * Busca al cliente completo para que el recibo tenga todos los datos (Club, Atleta).
     */
    private void generarReciboPDF(Sale sale, List<SaleDetail> details) {
        try {
            Client client = null;
            // Intentamos recuperar los datos del cliente usando el ID guardado en la venta
            try {
                String clientIdStr = sale.getClientId();
                // Buscamos usando el método del ClientDAO (puede ser por ID numérico o código)
                // Aquí asumimos que sale.getClientId() tiene el ID numérico de la BD
                int id = Integer.parseInt(clientIdStr);

                // Buscamos en la lista completa (Estrategia segura sin modificar DAO)
                List<Client> allClients = clientDAO.getAllClients();
                for (Client c : allClients) {
                    if (c.getId() == id) {
                        client = c;
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo vincular cliente al PDF (Venta Anónima o Error ID): " + e.getMessage());
            }

            // Invocamos al Generador
            ReceiptGenerator.generateReceipt(sale, details, client);

        } catch (Exception e) {
            System.err.println("❌ Error generando el PDF: " + e.getMessage());
            e.printStackTrace();
            // No retornamos false porque la venta SI se guardó en BD.
            // Solo falló el papelito.
        }
    }
}