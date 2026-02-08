/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * AUTORA: Johanna Gabriela Guédez Flores
 * PROFESORA: Ing. Dubraska Roca
 * ASIGNATURA: Técnicas de Programación III
 * * ARCHIVO: ReportPDF.java
 * VERSIÓN: 2.7.0 (Core Reporting Engine)
 * FECHA: 06 de Febrero de 2026
 * HORA: 04:30 PM (Hora de Venezuela)
 * * DESCRIPCIÓN TÉCNICA:
 * Clase de utilidad encargada de la generación de documentos electrónicos en
 * formato PDF. Utiliza la librería iText para consolidar métricas financieras
 * y operativas en un reporte gerencial formal.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.Desktop;
import java.io.File;

/**
 * [UTILIDAD - REPORTES] Clase especializada en la persistencia documental.
 * [DEPENDENCIA] iText PDF Library: Motor de renderizado de documentos vectoriales.
 * [REQUERIMIENTO FUNCIONAL] Generación de estados de cuenta y reportes de producción.
 */
public class ReportPDF {

    // ========================================================================================
    //                                  LÓGICA DE GENERACIÓN (API)
    // ========================================================================================

    /**
     * [MÉTODO ESTÁTICO] Coordina la creación del documento PDF.
     * Implementa un algoritmo de construcción secuencial: Configuración -> Apertura ->
     * Inyección de Datos -> Cierre -> Visualización.
     * * @param rango Descripción del periodo de tiempo analizado.
     * @param ingresos Sumatoria total de cobros efectivos.
     * @param deuda Saldo pendiente por cobrar (Cuentas por Cobrar).
     * @param pedidosPendientes Contador de órdenes sin pago completado.
     * @param enTaller Unidades actualmente en proceso de manufactura.
     */
    public static void generateReport(String rango, double ingresos, double deuda, int pedidosPendientes, int enTaller) {
        // [ESTRUCTURA] Instanciación del documento iText y definición de nombre dinámico basado en timestamp.
        Document document = new Document();
        String fileName = "Reporte_SICONI_" + System.currentTimeMillis() + ".pdf";

        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // --- CONFIGURACIÓN DE TIPOGRAFÍA ---
            // Se definen fuentes constantes para garantizar la uniformidad visual del documento.
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font redFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.RED);

            // --- SECCIÓN: ENCABEZADO ---
            Paragraph title = new Paragraph("SICONI - REPORTE GERENCIAL", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Inserción de metadatos del reporte (Fecha y rango)
            document.add(new Paragraph("Fecha de Emisión: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())));
            document.add(new Paragraph("Rango Analizado: " + rango));
            document.add(new Chunk(new LineSeparator())); // Separador visual
            document.add(new Paragraph("\n"));

            // --- SECCIÓN: RESUMEN FINANCIERO ---
            // [COMPONENTE] PdfPTable: Estructura tabular de dos columnas para datos financieros.
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addCell(table, "INGRESOS TOTALES (COBRADO):", headerFont);
            addCell(table, String.format("$ %,.2f", ingresos), dataFont);

            addCell(table, "CUENTAS POR COBRAR (DEUDA):", headerFont);
            addCell(table, String.format("$ %,.2f", deuda), redFont);

            document.add(table);
            document.add(new Paragraph("\n"));

            // --- SECCIÓN: RESUMEN OPERATIVO ---
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

            // --- SECCIÓN: CIERRE (FOOTER) ---
            document.add(new Paragraph("\n\n\n"));
            Paragraph footer = new Paragraph("Generado automáticamente por SICONI System", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            // [INTEGRACIÓN CON SO] Apertura automática del archivo generado utilizando la clase Desktop.
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(fileName));
            }

        } catch (Exception e) {
            // Manejo de excepciones en operaciones de I/O y renderizado de PDF.
            e.printStackTrace();
        }
    }

    // ========================================================================================
    //                                  MÉTODOS AUXILIARES (HELPERS)
    // ========================================================================================

    /**
     * [MÉTODO PRIVADO] Encapsula la lógica de creación de celdas para las tablas.
     * Estandariza el padding, color de bordes y alineación de los datos.
     * * @param table Referencia a la tabla iText.
     * @param text Contenido de la celda.
     * @param font Estilo de fuente a aplicar.
     */
    private static void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }
}