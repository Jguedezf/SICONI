/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ReceiptGenerator.java
 * DESCRIPCIÓN: Generador de Recibos PDF (Formato Ticket / Carta).
 * VERSIÓN: 1.0.0 (Dayana Guedez Style)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.swimcore.model.Client;
import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReceiptGenerator {

    private static final String FOLDER_PATH = "Recibos_SICONI";
    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_SUBTITLE = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_BODY = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC);

    public static void generateReceipt(Sale sale, List<SaleDetail> details, Client client) {
        try {
            // 1. Crear carpeta si no existe
            File folder = new File(FOLDER_PATH);
            if (!folder.exists()) folder.mkdir();

            // 2. Nombre del archivo (Ej: PED-174001_Cliente.pdf)
            String fileName = FOLDER_PATH + "/" + sale.getId() + "_" + (client != null ? client.getFullName() : "Cliente") + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // --- ENCABEZADO ---
            Paragraph title = new Paragraph("DAYANA GUEDEZ SWIMWEAR", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph slogan = new Paragraph("Confección de Trajes de Baño y Ropa Deportiva", FONT_SMALL);
            slogan.setAlignment(Element.ALIGN_CENTER);
            document.add(slogan);

            document.add(new Paragraph(" ")); // Espacio
            document.add(new Paragraph(" "));

            // --- DATOS DEL PEDIDO ---
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            infoTable.addCell(getCell("Nro Control: " + sale.getId(), FONT_SUBTITLE, false));
            infoTable.addCell(getCell("Fecha: " + sale.getDate(), FONT_BODY, true));

            if (client != null) {
                infoTable.addCell(getCell("Cliente: " + client.getFullName(), FONT_BODY, false));
                infoTable.addCell(getCell("Cédula/RIF: " + client.getIdNumber(), FONT_BODY, true));
                // Datos del Atleta (Importante para el taller)
                infoTable.addCell(getCell("ATLETA: " + client.getAthleteName(), FONT_BODY, false));
                infoTable.addCell(getCell("CLUB: " + client.getClub(), FONT_BODY, true));
            } else {
                infoTable.addCell(getCell("Cliente: CONTADO / MOSTRADOR", FONT_BODY, false));
                infoTable.addCell(getCell("", FONT_BODY, true));
            }

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // --- TABLA DE PRODUCTOS ---
            PdfPTable table = new PdfPTable(4); // Cant, Desc, Unit, Total
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 4, 2, 2});

            // Encabezados
            table.addCell(getCell("CANT", FONT_SUBTITLE, true));
            table.addCell(getCell("DESCRIPCIÓN / TALLA", FONT_SUBTITLE, false));
            table.addCell(getCell("PRECIO", FONT_SUBTITLE, true));
            table.addCell(getCell("TOTAL", FONT_SUBTITLE, true));

            // Filas
            for (SaleDetail d : details) {
                table.addCell(getCell(String.valueOf(d.getQuantity()), FONT_BODY, true));
                table.addCell(getCell(d.getProductName(), FONT_BODY, false)); // Aquí sale el Modelo + Talla
                table.addCell(getCell(String.format("$%.2f", d.getUnitPrice()), FONT_BODY, true));
                table.addCell(getCell(String.format("$%.2f", d.getSubtotal()), FONT_BODY, true));
            }
            document.add(table);

            // --- TOTALES ---
            document.add(new Paragraph(" "));
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totalTable.addCell(getCell("TOTAL:", FONT_SUBTITLE, false));
            totalTable.addCell(getCell(String.format("$ %.2f", sale.getTotalAmountUSD()), FONT_SUBTITLE, true));

            totalTable.addCell(getCell("ABONADO:", FONT_BODY, false));
            totalTable.addCell(getCell(String.format("$ %.2f", sale.getAmountPaid()), FONT_BODY, true));

            totalTable.addCell(getCell("RESTA:", FONT_TITLE, false));
            totalTable.addCell(getCell(String.format("$ %.2f", sale.getBalanceDue()), FONT_TITLE, true));

            document.add(totalTable);

            // --- PIE DE PÁGINA ---
            document.add(new Paragraph(" "));
            document.add(new Paragraph("----------------------------------------------------------------", FONT_SMALL));
            document.add(new Paragraph("ESTADO: " + sale.getStatus(), FONT_SUBTITLE));
            document.add(new Paragraph("Observaciones: " + sale.getObservations().replace("\n", " / "), FONT_SMALL));
            document.add(new Paragraph("Gracias por preferir a Dayana Guedez Swimwear!", FONT_SMALL));

            document.close();

            // 4. Abrir automáticamente
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(fileName));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper para celdas limpias
    private static PdfPCell getCell(String text, Font font, boolean alignRight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        if (alignRight) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}