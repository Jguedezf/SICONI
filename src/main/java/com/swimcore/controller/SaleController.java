/*
 * -----------------------------------------------------------------------------
 * ARCHIVO: SaleController.java
 * VERSIÓN: 2.1.0 (Sequence Methods Bridge)
 * CAMBIOS:
 * 1. Métodos públicos para obtener los próximos N° de Pedido/Factura/Control.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.ClientDAO;
import com.swimcore.dao.SaleDAO;
import com.swimcore.model.Client;
import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import com.swimcore.util.ReceiptGenerator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SaleController {

    private final SaleDAO saleDAO;
    private final ClientDAO clientDAO;

    public SaleController() {
        this.saleDAO = new SaleDAO();
        this.clientDAO = new ClientDAO();
    }

    // --- NUEVOS MÉTODOS PARA CONSECUTIVOS ---

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
        if (success) { generarReciboPDF(sale, details); }
        return success;
    }

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

    private void generarReciboPDF(Sale sale, List<SaleDetail> details) {
        try {
            Client client = clientDAO.getAllClients().stream()
                    .filter(c -> String.valueOf(c.getId()).equals(sale.getClientId()))
                    .findFirst().orElse(null);
            if(client != null) {
                ReceiptGenerator.generateReceipt(sale, details, client);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}