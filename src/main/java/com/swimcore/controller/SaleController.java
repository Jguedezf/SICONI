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
 * FECHA: 06 de Febrero de 2026
 * VERSIÓN: 2.2.0 (Receipt Flow Fix & Snapshot Bridge)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Controlador. Actúa como mediador entre los componentes de
 * la interfaz de usuario (SalesView) y la capa de acceso a datos (SaleDAO).
 * Coordina la lógica de negocio para la generación de correlativos fiscales,
 * el registro transaccional de ventas y el flujo de generación de comprobantes.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.ClientDAO;
import com.swimcore.dao.SaleDAO;
import com.swimcore.model.Client;
import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import com.swimcore.util.ReceiptGenerator;
import com.swimcore.view.dialogs.ReceiptPreviewDialog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * [CONTROLLER] Orquestador de procesos comerciales y transaccionales.
 * [DISEÑO] Sigue el principio de separación de responsabilidades (MVC), evitando
 * que la vista maneje directamente la lógica de base de datos o cálculos financieros.
 * [LOGICA] Gestiona la integridad de los números de control y factura.
 */
public class SaleController {

    // ========================================================================================
    //                                  ATRIBUTOS (DEPENDENCIAS)
    // ========================================================================================

    // Referencias a la capa de persistencia mediante Inyección por Constructor.
    private final SaleDAO saleDAO;
    private final ClientDAO clientDAO;

    /**
     * Constructor de la clase.
     * Inicializa los objetos de acceso a datos necesarios para la operación del módulo.
     */
    public SaleController() {
        this.saleDAO = new SaleDAO();
        this.clientDAO = new ClientDAO();
    }

    // ========================================================================================
    //                                  GESTIÓN DE CORRELATIVOS FISCALES
    // ========================================================================================

    /** @return Siguiente número correlativo de orden basado en el conteo histórico. */
    public int getNextOrderNumber() {
        return saleDAO.getTotalSaleCount() + 1;
    }

    /**
     * Implementa la lógica incremental para la numeración de facturas (FAC-XXXX).
     * @return String con el siguiente formato de factura.
     */
    public String getNextInvoiceNumber() {
        String lastInvoice = saleDAO.getLastInvoiceAndControlNumbers()[0];
        try {
            int num = Integer.parseInt(lastInvoice.split("-")[1]);
            return String.format("FAC-%04d", num + 1);
        } catch (Exception e) {
            return "FAC-0001"; // Fallback para primer registro o error de formato.
        }
    }

    /**
     * Genera el número de control fiscal basado en el año en curso.
     * Detecta el cambio de año para reiniciar el correlativo (CTRL-YYYY-XXXX).
     */
    public String getNextControlNumber() {
        String lastControl = saleDAO.getLastInvoiceAndControlNumbers()[1];
        String year = new SimpleDateFormat("yyyy").format(new Date());
        try {
            String lastYear = lastControl.split("-")[1];
            int num = Integer.parseInt(lastControl.split("-")[2]);
            if (year.equals(lastYear)) {
                return String.format("CTRL-%s-%04d", year, num + 1);
            }
        } catch (Exception e) { /* Manejo de excepción para inicialización */ }
        return "CTRL-" + year + "-0001";
    }

    // ========================================================================================
    //                                  PROCESAMIENTO TRANSACCIONAL
    // ========================================================================================

    /**
     * Delega el registro de la venta al componente DAO.
     * [CAMBIO V2.2] Se desacopló la generación automática de PDF para permitir
     * una previsualización controlada por el usuario en la vista.
     */
    public boolean registerSale(Sale sale, List<SaleDetail> details) {
        if (sale == null || details == null || details.isEmpty()) return false;
        return saleDAO.registerSale(sale, details);
    }

    /**
     * Recupera la entidad Sale completa desde la persistencia.
     * @param saleId Identificador único de la transacción.
     */
    public Sale getSaleById(String saleId) {
        return saleDAO.getSaleById(saleId);
    }

    /**
     * [SNAPSHOT BRIDGE] Reconstruye objetos de modelo SaleDetail a partir de
     * estructuras de datos de la interfaz de usuario (TicketItem).
     * Permite la generación de comprobantes sin realizar re-consultas innecesarias a la BD.
     */
    public List<SaleDetail> getDetailsFromSnapshot(List<ReceiptPreviewDialog.TicketItem> items, String saleId) {
        List<SaleDetail> details = new ArrayList<>();
        for (ReceiptPreviewDialog.TicketItem item : items) {
            SaleDetail d = new SaleDetail();
            d.setSaleId(saleId);
            d.setProductName(item.name);
            d.setQuantity(item.qty);
            d.setSubtotal(item.subtotal);
            d.setUnitPrice(item.subtotal / item.qty);
            d.setProductId("0"); // Identificador dummy para datos volátiles de previsualización.
            details.add(d);
        }
        return details;
    }

    // ========================================================================================
    //                                  MÉTODOS ANALÍTICOS (REPORTING)
    // ========================================================================================

    /** Calcula el gasto total acumulado de un cliente específico en USD. */
    public double getCustomerTotalSpending(String clientId) {
        return saleDAO.getAllSales().stream()
                .filter(s -> s.getClientId().equals(clientId))
                .mapToDouble(Sale::getTotalAmountUSD)
                .sum();
    }

    /** Contabiliza la recurrencia de compra (frecuencia) de un cliente. */
    public int getCustomerOrderCount(String clientId) {
        if (clientId.isEmpty()) {
            return saleDAO.getTotalSaleCount();
        }
        return (int) saleDAO.getAllSales().stream()
                .filter(s -> s.getClientId().equals(clientId))
                .count();
    }
}