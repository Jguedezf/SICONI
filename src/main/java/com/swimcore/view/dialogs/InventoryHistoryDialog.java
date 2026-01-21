/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: InventoryHistoryDialog.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 2.4.0 (Final Audit Report & Filter Fix)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Módulo de Auditoría Visual Avanzada.
 * Implementa el patrón "Dark Luxury" (Negro Mate + Dorado).
 * - CORRECCIÓN CRÍTICA: Lógica de carga de Logo y escalado proporcional.
 * - MEJORA: Precisión de filtrado de fechas (Start-End Day).
 * - UI: Centrado estricto de elementos en reporte PDF.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePickerSettings.DateArea;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import com.swimcore.dao.ProductDAO;
import com.swimcore.util.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
// Importaciones de Eventos
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Pantalla de Auditoría con estilo "Luxury" y funciones forenses.
 */
public class InventoryHistoryDialog extends JDialog {

    private final ProductDAO productDAO = new ProductDAO();
    private DefaultTableModel model;
    private DatePicker dateFrom;
    private DatePicker dateTo;

    // --- PALETA 3D LUXURY ---
    private final Color LUX_BG_DARK = new Color(18, 18, 18);
    private final Color LUX_BG_PANEL = new Color(30, 30, 30);
    private final Color LUX_GOLD = new Color(218, 165, 32);
    private final Color LUX_TEXT_WHITE = new Color(240, 240, 240);

    public InventoryHistoryDialog(Window parent) {
        super(parent, "AUDITORÍA CORPORATIVA - SICONI", ModalityType.APPLICATION_MODAL);
        setSize(1100, 700);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        getContentPane().setBackground(LUX_BG_DARK);
        setLayout(new BorderLayout());
        getRootPane().setBorder(new LineBorder(LUX_GOLD, 2));

        initHeader();
        initFilters();
        initTable();
        initFooter();

        loadData(); // Carga inicial
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LUX_BG_DARK);
        header.setBorder(new EmptyBorder(25, 30, 10, 30));

        JLabel title = new JLabel("BITÁCORA DE MOVIMIENTOS");
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        title.setForeground(LUX_GOLD);

        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
    }

    private void initFilters() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panel.setBackground(LUX_BG_PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(60, 60, 60)));

        DatePickerSettings s1 = new DatePickerSettings();
        styleCalendar(s1);

        DatePickerSettings s2 = new DatePickerSettings();
        styleCalendar(s2);

        dateFrom = new DatePicker(s1);
        dateFrom.setDate(LocalDate.now().minusDays(30));

        dateTo = new DatePicker(s2);
        dateTo.setDateToToday();

        JButton btnFilter = createLuxuryButton("FILTRAR DATOS", LUX_GOLD);
        btnFilter.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            loadData();
        });

        JButton btnExport = createLuxuryButton("EXPORTAR PDF", new Color(0, 150, 255));
        btnExport.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            exportToPDF();
        });

        panel.add(createLabel("DESDE:"));
        panel.add(dateFrom);
        panel.add(createLabel("HASTA:"));
        panel.add(dateTo);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(btnFilter);
        panel.add(btnExport);

        add(panel, BorderLayout.NORTH);
    }

    private void styleCalendar(DatePickerSettings s) {
        s.setFormatForDatesCommonEra("yyyy-MM-dd");
        s.setColor(DateArea.BackgroundOverallCalendarPanel, new Color(230, 230, 230));
        s.setColor(DateArea.BackgroundMonthAndYearMenuLabels, LUX_GOLD);
        s.setColor(DateArea.BackgroundTodayLabel, new Color(255, 140, 0));
        s.setColor(DateArea.CalendarBackgroundSelectedDate, LUX_GOLD);
        s.setColor(DateArea.CalendarBorderSelectedDate, Color.BLACK);
        s.setColor(DateArea.TextMonthAndYearMenuLabels, Color.BLACK);
    }

    private void initTable() {
        String[] cols = {"ID", "FECHA / HORA", "CÓDIGO", "PRODUCTO", "TIPO", "CANT.", "JUSTIFICACIÓN"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setBackground(LUX_BG_DARK);
        table.setForeground(LUX_TEXT_WHITE);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(50, 50, 50));
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));

        table.getTableHeader().setBackground(new Color(25, 25, 25));
        table.getTableHeader().setForeground(LUX_GOLD);
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        table.getTableHeader().setBorder(new LineBorder(LUX_GOLD, 1));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                if (!isSel) c.setBackground(row % 2 == 0 ? LUX_BG_DARK : new Color(25, 25, 25));
                else c.setBackground(new Color(60, 60, 60));

                String tipo = (String) model.getValueAt(row, 4);
                if (col == 4 || col == 5) {
                    if ("ENTRADA".equals(tipo)) c.setForeground(new Color(46, 204, 113));
                    else c.setForeground(new Color(255, 0, 100));
                    setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                } else {
                    c.setForeground(LUX_TEXT_WHITE);
                }

                if (col == 3 || col == 6) setHorizontalAlignment(JLabel.LEFT);
                else setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(10, 30, 10, 30));
        scroll.getViewport().setBackground(LUX_BG_DARK);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private void initFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(LUX_BG_DARK);
        footer.setBorder(new EmptyBorder(10, 0, 20, 0));

        JButton btnBack = createLuxuryButton("VOLVER AL INVENTARIO", new Color(180, 0, 0));
        btnBack.setPreferredSize(new Dimension(250, 45));
        btnBack.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            dispose();
        });

        footer.add(btnBack);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Lógica de filtrado de alta precisión.
     * Convierte LocalDate (Selector) -> LocalDateTime (00:00 - 23:59) -> Date (JDBC).
     */
    private void loadData() {
        if (dateFrom.getDate() == null || dateTo.getDate() == null) return;

        LocalDate ldFrom = dateFrom.getDate();
        LocalDate ldTo = dateTo.getDate();

        // VALIDACIÓN DE NEGOCIO: Rango coherente
        if(ldFrom.isAfter(ldTo)) {
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser mayor que 'Hasta'.");
            return;
        }

        // Definición de límites del día (Start of Day vs End of Day)
        LocalDateTime ldtFrom = ldFrom.atStartOfDay(); // 00:00:00
        LocalDateTime ldtTo = ldTo.atTime(LocalTime.MAX); // 23:59:59.999999

        Date d1 = Date.from(ldtFrom.atZone(ZoneId.systemDefault()).toInstant());
        Date d2 = Date.from(ldtTo.atZone(ZoneId.systemDefault()).toInstant());

        model.setRowCount(0);
        List<Vector<Object>> data = productDAO.getHistoryByDateRange(d1, d2);

        for (Vector<Object> row : data) model.addRow(row);
    }

    /**
     * Genera un reporte PDF con diseño corporativo "SICONI".
     */
    private void exportToPDF() {
        try {
            String fileName = "Reporte_Auditoria_SICONI.pdf";
            Document doc = new Document(PageSize.A4.rotate());

            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            writer.setViewerPreferences(PdfWriter.PageLayoutSinglePage | PdfWriter.FitWindow);

            doc.open();

            // Zoom automático al ancho de la página
            PdfAction action = PdfAction.gotoLocalPage(1, new PdfDestination(PdfDestination.FITH, doc.top()), writer);
            writer.setOpenAction(action);

            // --- 1. LOGO CORPORATIVO (Escalado Grande) ---
            try {
                // Busca en: src/main/resources/images/logo.png
                URL logoUrl = getClass().getResource("/images/logo.png");
                if (logoUrl != null) {
                    Image logo = Image.getInstance(logoUrl);
                    // ESCALA AUMENTADA: 300px ancho x 150px alto
                    logo.scaleToFit(300, 150);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    doc.add(logo);
                } else {
                    // Fallback si no encuentra la imagen (Para que sepas que pasó)
                    System.err.println("⚠ ALERTA: No se encontró logo.png en /resources/images/");
                }
            } catch (Exception ex) { ex.printStackTrace(); }

            doc.add(new Paragraph(" "));

            // --- 2. TÍTULOS ---
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(220, 0, 115)); // Fucsia
            Paragraph pTitle = new Paragraph("SICONI - SISTEMA DE CONTROL DE NEGOCIO E INVENTARIO", titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitle);

            Font sloganFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, BaseColor.DARK_GRAY);
            Paragraph pSlogan = new Paragraph("Solución Tecnológica de control de negocio e inventario, optimizada para la gestión administrativa de Pequeñas y Medianas Empresas (PyMEs)", sloganFont);
            pSlogan.setAlignment(Element.ALIGN_CENTER);
            doc.add(pSlogan);

            doc.add(new Paragraph(" "));

            // --- 3. METADATOS ---
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Paragraph pMeta = new Paragraph("REPORTE OFICIAL DE MOVIMIENTOS | Generado: " + new Date(), metaFont);
            pMeta.setAlignment(Element.ALIGN_CENTER);
            doc.add(pMeta);

            Paragraph pRango = new Paragraph("Rango de Auditoría: " + dateFrom.getText() + " al " + dateTo.getText(), metaFont);
            pRango.setAlignment(Element.ALIGN_CENTER);
            doc.add(pRango);

            doc.add(new Paragraph(" "));

            // --- 4. TABLA DE DATOS ---
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{5, 12, 10, 25, 8, 5, 25});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
            BaseColor headerBg = new BaseColor(30, 30, 30);

            String[] headers = {"ID", "FECHA", "CÓDIGO", "PRODUCTO", "TIPO", "CANT", "JUSTIFICACIÓN"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    String val = model.getValueAt(i, j).toString();
                    PdfPCell cell = new PdfPCell(new Phrase(val, dataFont));
                    cell.setPadding(4);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    // ALINEACIÓN CENTRADA PARA ID Y CANTIDAD
                    if (j == 0 || j == 5) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    else cell.setHorizontalAlignment(Element.ALIGN_LEFT);

                    if (j == 4) { // Columna Tipo
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        if ("ENTRADA".equals(val)) cell.setBackgroundColor(new BaseColor(220, 255, 220));
                        else cell.setBackgroundColor(new BaseColor(255, 220, 220));
                    }
                    table.addCell(cell);
                }
            }

            doc.add(table);

            // Footer
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
            Paragraph footerP = new Paragraph("\nDocumento oficial de uso interno | Dayana Guédez Swimwear", footerFont);
            footerP.setAlignment(Element.ALIGN_CENTER);
            doc.add(footerP);

            doc.close();

            JOptionPane.showMessageDialog(this, "✅ REPORTE GENERADO CON ÉXITO.\nArchivo: " + fileName, "SICONI PDF", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            SoundManager.getInstance().playError();
            JOptionPane.showMessageDialog(this, "Error generando reporte PDF: " + e.getMessage());
        }
    }

    private JButton createLuxuryButton(String text, Color accentColor) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(160, 35));
        btn.setBackground(LUX_BG_DARK);
        btn.setForeground(accentColor);
        btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(accentColor, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(accentColor);
                btn.setForeground(Color.BLACK);
                SoundManager.getInstance().playHover();
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(LUX_BG_DARK);
                btn.setForeground(accentColor);
            }
        });
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(LUX_TEXT_WHITE);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        return l;
    }
}