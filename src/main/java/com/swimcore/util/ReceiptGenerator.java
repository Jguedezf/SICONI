/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ReceiptGenerator.java
 * VERSIÓN: 1.2.0 (FIXED: Font Ambiguity & Variable Scope)
 * FECHA: 04 de Febrero de 2026 - 11:10 PM (Venezuela)
 * DESCRIPCIÓN: Generador de Recibos PDF e Impresión Física.
 *              Se resolvieron conflictos de librerías Font y variables nulas.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

// --- IMPORTS ESPECÍFICOS PARA EVITAR AMBIGÜEDAD ---
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Rectangle;

import com.swimcore.model.Client;
import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptGenerator {

    public static final String FOLDER_PATH = "Recibos_SICONI";

    // Usamos nombres completos para evitar el error de ambigüedad
    private static final com.itextpdf.text.Font PDF_FONT_TITLE = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
    private static final com.itextpdf.text.Font PDF_FONT_SUBTITLE = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
    private static final com.itextpdf.text.Font PDF_FONT_BODY = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL);
    private static final com.itextpdf.text.Font PDF_FONT_SMALL = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.ITALIC);

    // Variables para soporte de impresión Swing
    private JPanel ticketPanel;
    private String saleCode;

    // Constructor para cuando se usa como componente de impresión
    public ReceiptGenerator(JPanel ticketPanel, String saleCode) {
        this.ticketPanel = ticketPanel;
        this.saleCode = saleCode;
    }

    public static void generateReceipt(Sale sale, List<SaleDetail> details, Client client, boolean openFile) {
        try {
            File folder = new File(FOLDER_PATH);
            if (!folder.exists()) folder.mkdir();

            String safeClientName = (client != null ? client.getFullName() : "Cliente")
                    .replaceAll("[^a-zA-Z0-9.-]", "_");
            String fileName = FOLDER_PATH + "/" + sale.getId() + "_" + safeClientName + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // --- ENCABEZADO ---
            Paragraph title = new Paragraph("SICONI", PDF_FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph slogan = new Paragraph("TALLER DE CONFECCIÓN", PDF_FONT_SUBTITLE);
            slogan.setAlignment(Element.ALIGN_CENTER);
            document.add(slogan);

            document.add(new Paragraph("Puerto Ordaz, Venezuela", PDF_FONT_BODY));
            document.add(new Paragraph("R.I.F: V-14089807-1", PDF_FONT_BODY));
            document.add(new Paragraph(" "));

            // --- DATOS GENERALES ---
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            infoTable.addCell(getPdfCell("Nro Control: " + sale.getId(), PDF_FONT_SUBTITLE, false));

            // Corrección de variable dateStr no definida
            String displayDate = (sale.getDate() != null && sale.getDate().length() > 10)
                    ? sale.getDate().substring(0, 10)
                    : new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            infoTable.addCell(getPdfCell("Fecha: " + displayDate, PDF_FONT_BODY, true));

            if (client != null) {
                infoTable.addCell(getPdfCell("Cliente: " + client.getFullName(), PDF_FONT_BODY, false));
                infoTable.addCell(getPdfCell("Cédula/RIF: " + client.getIdNumber(), PDF_FONT_BODY, true));
            } else {
                infoTable.addCell(getPdfCell("Cliente: CONTADO / MOSTRADOR", PDF_FONT_BODY, false));
                infoTable.addCell(getPdfCell("", PDF_FONT_BODY, true));
            }
            document.add(infoTable);
            document.add(new Paragraph(" "));

            // --- TABLA DE PRODUCTOS ---
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 4, 2, 2});

            table.addCell(getPdfCell("CANT", PDF_FONT_SUBTITLE, true));
            table.addCell(getPdfCell("DESCRIPCIÓN / TALLA", PDF_FONT_SUBTITLE, false));
            table.addCell(getPdfCell("PRECIO", PDF_FONT_SUBTITLE, true));
            table.addCell(getPdfCell("TOTAL", PDF_FONT_SUBTITLE, true));

            for (SaleDetail d : details) {
                table.addCell(getPdfCell(String.valueOf(d.getQuantity()), PDF_FONT_BODY, true));
                table.addCell(getPdfCell(d.getProductName(), PDF_FONT_BODY, false));
                double unitPrice = (d.getQuantity() > 0) ? (d.getSubtotal() / d.getQuantity()) : 0;
                table.addCell(getPdfCell(String.format("$%.2f", unitPrice), PDF_FONT_BODY, true));
                table.addCell(getPdfCell(String.format("$%.2f", d.getSubtotal()), PDF_FONT_BODY, true));
            }
            document.add(table);

            // --- TOTALES ---
            document.add(new Paragraph(" "));
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totalTable.addCell(getPdfCell("TOTAL:", PDF_FONT_SUBTITLE, false));
            totalTable.addCell(getPdfCell(String.format("$ %.2f", sale.getTotalAmountUSD()), PDF_FONT_SUBTITLE, true));

            totalTable.addCell(getPdfCell("ABONADO:", PDF_FONT_BODY, false));
            totalTable.addCell(getPdfCell(String.format("$ %.2f", sale.getAmountPaid()), PDF_FONT_BODY, true));

            totalTable.addCell(getPdfCell("RESTA:", PDF_FONT_TITLE, false));
            totalTable.addCell(getPdfCell(String.format("$ %.2f", sale.getBalanceDue()), PDF_FONT_TITLE, true));

            document.add(totalTable);
            document.close();

            if (openFile && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(fileName));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PdfPCell getPdfCell(String text, com.itextpdf.text.Font font, boolean alignRight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        if (alignRight) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    // --- MÉTODOS PARA IMPRESIÓN FÍSICA (SWING) ---

    public void addTicketText(String text, int size, boolean bold, Color color) {
        if (ticketPanel == null) return;
        JLabel l = new JLabel(text);
        // Aquí usamos java.awt.Font explícitamente
        l.setFont(new java.awt.Font("Monospaced", bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(l);
        ticketPanel.add(Box.createVerticalStrut(2));
    }

    public void printJob() {
        if (ticketPanel == null) return;
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Recibo SICONI " + saleCode);
        job.setPrintable(new Printable() {
            public int print(Graphics pg, PageFormat pf, int pageNum) {
                if (pageNum > 0) return Printable.NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) pg;
                g2.translate(pf.getImageableX(), pf.getImageableY());
                double scale = pf.getImageableWidth() / ticketPanel.getWidth();
                if(scale < 1.0) g2.scale(scale, scale);
                ticketPanel.printAll(g2);
                return Printable.PAGE_EXISTS;
            }
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }
}