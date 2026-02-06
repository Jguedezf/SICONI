/*
 * -----------------------------------------------------------------------------
 * ARCHIVO: SaleController.java
 * VERSIÓN: 2.2.0 (Receipt Flow Fix & Snapshot Bridge)
 * CAMBIOS:
 * 1. Eliminada la llamada automática a generación de PDF en registerSale.
 * 2. Añadidos métodos de soporte para la SalesView (getSaleById, getDetailsFromSnapshot).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.ClientDAO;
import com.swimcore.dao.SaleDAO;
import com.swimcore.model.Client;
import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import com.swimcore.util.ReceiptGenerator;
import com.swimcore.view.dialogs.ReceiptPreviewDialog; // Para el TicketItem
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaleController {

    private final SaleDAO saleDAO;
    private final ClientDAO clientDAO;

    public SaleController() {
        this.saleDAO = new SaleDAO();
        this.clientDAO = new ClientDAO();
    }

    // --- MÉTODOS DE CONSECUTIVOS (NO CAMBIAN) ---

    public int getNextOrderNumber() {
        return saleDAO.getTotalSaleCount() + 1;
    }

    public String getNextInvoiceNumber() {
        String lastInvoice = saleDAO.getLastInvoiceAndControlNumbers()[0];
        try {
            int num = Integer.parseInt(lastInvoice.split("-")[1]);
            return String.format("FAC-%04d", num + 1);
        } catch (Exception e) {
            return "FAC-0001"; // Si hay error o es el primero
        }
    }

    public String getNextControlNumber() {
        String lastControl = saleDAO.getLastInvoiceAndControlNumbers()[1];
        String year = new SimpleDateFormat("yyyy").format(new Date());
        try {
            String lastYear = lastControl.split("-")[1];
            int num = Integer.parseInt(lastControl.split("-")[2]);
            if (year.equals(lastYear)) {
                return String.format("CTRL-%s-%04d", year, num + 1);
            }
        } catch (Exception e) {
            // No hacer nada, se genera uno nuevo abajo
        }
        return "CTRL-" + year + "-0001"; // Si hay error, es año nuevo o es el primero
    }


    public boolean registerSale(Sale sale, List<SaleDetail> details) {
        if (sale == null || details == null || details.isEmpty()) return false;
        boolean success = saleDAO.registerSale(sale, details);

        // ⛔ SE ELIMINÓ LA LLAMADA AUTOMÁTICA DEL RECIBO AQUÍ

        return success;
    }

    // --- MÉTODOS DE SOPORTE PARA RECEIPT FLOW ---

    /**
     * Recupera el objeto Sale completo por su ID (Necesario en SalesView para generar PDF).
     * NOTA: Este método asume que SaleDAO tiene el método getSaleById(String saleId).
     */
    public Sale getSaleById(String saleId) {
        return saleDAO.getSaleById(saleId);
    }

    /**
     * Reconstruye la lista de SaleDetail a partir del Snapshot de datos de la Vista.
     */
    public List<SaleDetail> getDetailsFromSnapshot(List<ReceiptPreviewDialog.TicketItem> items, String saleId) {
        List<SaleDetail> details = new ArrayList<>();
        // No tenemos el product_id original, pero el generador solo necesita el nombre, cantidad y precio.
        for (ReceiptPreviewDialog.TicketItem item : items) {
            // Creamos un SaleDetail temporal (el productId se deja como "0" o similar si no es esencial)
            SaleDetail d = new SaleDetail();
            d.setSaleId(saleId);
            d.setProductName(item.name);
            d.setQuantity(item.qty);
            d.setSubtotal(item.subtotal);
            d.setUnitPrice(item.subtotal / item.qty);
            // El product_id se deja vacío o en "0" ya que no lo tenemos del Snapshot
            d.setProductId("0");
            details.add(d);
        }
        return details;
    }

    // --- MÉTODOS ANALÍTICOS (NO CAMBIAN) ---

    public double getCustomerTotalSpending(String clientId) {
        return saleDAO.getAllSales().stream()
                .filter(s -> s.getClientId().equals(clientId))
                .mapToDouble(Sale::getTotalAmountUSD)
                .sum();
    }

    public int getCustomerOrderCount(String clientId) {
        if (clientId.isEmpty()) { // Si el ID es vacío, contamos todos
            return saleDAO.getTotalSaleCount();
        }
        return (int) saleDAO.getAllSales().stream()
                .filter(s -> s.getClientId().equals(clientId))
                .count();
    }

    // NOTA: Este método privado ya no se usa, pero lo dejamos si es necesario para otros módulos.
    private void generarReciboPDF(Sale sale, List<SaleDetail> details) {
        try {
            Client client = clientDAO.getAllClients().stream()
                    .filter(c -> String.valueOf(c.getId()).equals(sale.getClientId()))
                    .findFirst().orElse(null);
            if(client != null) {
                // Aquí se llama a la función anterior que abría el PDF automáticamente,
                // pero ahora se reemplaza por la nueva lógica controlada.
                ReceiptGenerator.generateReceipt(sale, details, client, false);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}