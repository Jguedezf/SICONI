package com.swimcore.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.Desktop;
import java.io.File;

public class ReportPDF {

    public static void generateReport(String rango, double ingresos, double deuda, int pedidosPendientes, int enTaller) {
        Document document = new Document();
        String fileName = "Reporte_SICONI_" + System.currentTimeMillis() + ".pdf";

        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // FUENTES
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font redFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.RED);

            // ENCABEZADO
            Paragraph title = new Paragraph("SICONI - REPORTE GERENCIAL", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Fecha de Emisión: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())));
            document.add(new Paragraph("Rango Analizado: " + rango));
            document.add(new Chunk(new LineSeparator()));
            document.add(new Paragraph("\n"));

            // TABLA RESUMEN FINANCIERO
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addCell(table, "INGRESOS TOTALES (COBRADO):", headerFont);
            addCell(table, String.format("$ %,.2f", ingresos), dataFont);

            addCell(table, "CUENTAS POR COBRAR (DEUDA):", headerFont);
            addCell(table, String.format("$ %,.2f", deuda), redFont);

            document.add(table);
            document.add(new Paragraph("\n"));

            // TABLA RESUMEN OPERATIVO
            Paragraph subtitulo = new Paragraph("ESTADO OPERATIVO (TALLER)", headerFont);
            document.add(subtitulo);
            document.add(new Paragraph("\n"));

            PdfPTable tableOp = new PdfPTable(2);
            tableOp.setWidthPercentage(100);

            addCell(tableOp, "PEDIDOS EN TALLER / PROCESO:", headerFont);
            addCell(tableOp, String.valueOf(enTaller) + " Unidades", dataFont);

            addCell(tableOp, "PEDIDOS PENDIENTES DE PAGO:", headerFont);
            addCell(tableOp, String.valueOf(pedidosPendientes) + " Pedidos", redFont);

            document.add(tableOp);

            // PIE DE PÁGINA
            document.add(new Paragraph("\n\n\n"));
            Paragraph footer = new Paragraph("Generado automáticamente por SICONI System", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            // ABRIR AUTOMÁTICAMENTE EL PDF
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(fileName));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }
}