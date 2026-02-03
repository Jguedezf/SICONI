/*
 * -----------------------------------------------------------------------------
 * ARCHIVO: InventoryHistoryDialog.java
 * VERSIÓN: 15.0 (FIX FINAL: Ambigüedad resuelta + Logo correcto)
 * -----------------------------------------------------------------------------
 */
package com.swimcore.view.dialogs;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePickerSettings.DateArea;
import com.itextpdf.text.*; // iText básico
import com.itextpdf.text.pdf.*; // iText PDF
import com.swimcore.dao.ProductDAO;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.util.LuxuryMessage;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*; // AWT Gráfico
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class InventoryHistoryDialog extends JDialog {

    private final ProductDAO productDAO = new ProductDAO();
    private DefaultTableModel model;
    private DatePicker dateFrom;
    private DatePicker dateTo;

    private final Color LUX_BG_DARK = new Color(20, 20, 20);
    private final Color LUX_GOLD = new Color(212, 175, 55);
    private final Color LUX_VERDE = new Color(0, 255, 128);
    private final Color LUX_ROJO = new Color(255, 0, 127);
    private final Color LUX_TEXT_WHITE = new Color(230, 230, 230);

    public InventoryHistoryDialog(Window parent) {
        super(parent, LanguageManager.get("audit.dialog.title"), ModalityType.APPLICATION_MODAL);
        setSize(1250, 750);
        setLocationRelativeTo(parent);


        try {
            JPanel bgPanel = new ImagePanel("/images/bg_audit.png");
            bgPanel.setLayout(new BorderLayout());
            bgPanel.setBorder(new LineBorder(LUX_GOLD, 2));
            setContentPane(bgPanel);
        } catch (Exception e) {
            JPanel solidBg = new JPanel(new BorderLayout());
            solidBg.setBackground(LUX_BG_DARK);
            solidBg.setBorder(new LineBorder(LUX_GOLD, 2));
            setContentPane(solidBg);
        }

        initHeader();
        initFilters();
        initTable();
        initFooter();
        loadData();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 30, 10, 30));
        JLabel title = new JLabel(LanguageManager.get("audit.window.title").toUpperCase());
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32));
        title.setForeground(LUX_GOLD);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
    }

    private void initFilters() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,180));
                g2.fillRoundRect(0,0,getWidth(),getHeight(), 20,20);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setOpaque(false);
        container.add(panel);

        DatePickerSettings s1 = createLuxuryDarkSettings();
        DatePickerSettings s2 = createLuxuryDarkSettings();

        dateFrom = new DatePicker(s1); dateFrom.setDate(LocalDate.now().minusDays(30));
        dateTo = new DatePicker(s2); dateTo.setDateToToday();

        styleLuxuryField(dateFrom.getComponentDateTextField());
        styleLuxuryField(dateTo.getComponentDateTextField());
        styleCalButton(dateFrom.getComponentToggleCalendarButton());
        styleCalButton(dateTo.getComponentToggleCalendarButton());

        SoftButton btnFilter = createBigActionButton("BUSCAR", LUX_GOLD, "btn_refresh.png");
        btnFilter.addActionListener(e -> { SoundManager.getInstance().playClick(); loadData(); });

        SoftButton btnExport = createBigActionButton("EXPORTAR", new Color(0, 150, 255), "btn_save.png");
        btnExport.addActionListener(e -> { SoundManager.getInstance().playClick(); exportToPDF(); });

        panel.add(createLabel("DESDE:")); panel.add(dateFrom);
        panel.add(createLabel("HASTA:")); panel.add(dateTo);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(btnFilter); panel.add(btnExport);

        add(container, BorderLayout.NORTH);
    }

    private void styleLuxuryField(JTextField f) {
        f.setBackground(new Color(35, 35, 35));
        f.setForeground(Color.WHITE);
        f.setCaretColor(LUX_GOLD);
        f.setBorder(new CompoundBorder(new LineBorder(new Color(100, 100, 100)), new EmptyBorder(0, 10, 0, 10)));
        f.setPreferredSize(new Dimension(140, 40));
        f.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
    }

    private void styleCalButton(JButton b) {
        b.setText("📅"); b.setBackground(LUX_GOLD); b.setForeground(Color.BLACK);
        b.setPreferredSize(new Dimension(40, 40)); b.setFocusPainted(false); b.setBorder(null);
    }

    private DatePickerSettings createLuxuryDarkSettings() {
        DatePickerSettings s = new DatePickerSettings();
        s.setFormatForDatesCommonEra("yyyy-MM-dd");
        s.setColor(DateArea.BackgroundOverallCalendarPanel, new Color(30, 30, 30));
        s.setColor(DateArea.BackgroundMonthAndYearMenuLabels, LUX_GOLD);
        s.setColor(DateArea.TextMonthAndYearMenuLabels, Color.BLACK);
        s.setColor(DateArea.CalendarTextWeekdays, LUX_GOLD);
        try { s.setColor(DateArea.CalendarTextNormalDates, Color.WHITE); } catch (Throwable t) {}
        s.setColor(DateArea.CalendarBackgroundNormalDates, new Color(30,30,30));
        s.setColor(DateArea.CalendarBackgroundSelectedDate, LUX_GOLD);
        s.setColor(DateArea.BackgroundTodayLabel, LUX_VERDE);
        s.setColor(DateArea.TextTodayLabel, Color.BLACK);
        s.setFontCalendarDateLabels(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        return s;
    }

    private void initTable() {
        String[] cols = {"ID", "FECHA / HORA", "CÓDIGO", "PRODUCTO", "TIPO", "CANT.", "OBSERVACIÓN / MOTIVO"};
        model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setBackground(LUX_BG_DARK);
        table.setForeground(LUX_TEXT_WHITE);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(60, 60, 60));
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(25, 25, 25));
        header.setForeground(LUX_GOLD);
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 45));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                if (!isSel) c.setBackground(row % 2 == 0 ? LUX_BG_DARK : new Color(35, 35, 35));
                else c.setBackground(new Color(60, 60, 60));
                if (col == 4) {
                    String valStr = value != null ? value.toString().toUpperCase() : "";
                    if (valStr.contains("ENTRADA")) c.setForeground(LUX_VERDE);
                    else if (valStr.contains("SALIDA")) c.setForeground(LUX_ROJO);
                    setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                } else if (col == 5) {
                    c.setForeground(LUX_GOLD);
                    setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                } else c.setForeground(LUX_TEXT_WHITE);
                if (col == 3 || col == 6) setHorizontalAlignment(JLabel.LEFT); else setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);
        cm.getColumn(1).setPreferredWidth(160);
        cm.getColumn(2).setPreferredWidth(80);
        cm.getColumn(3).setPreferredWidth(200);
        cm.getColumn(4).setPreferredWidth(100);
        cm.getColumn(5).setPreferredWidth(60);
        cm.getColumn(6).setPreferredWidth(350);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(LUX_GOLD, 1));
        scroll.getViewport().setBackground(LUX_BG_DARK);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false); p.setBorder(new EmptyBorder(10, 40, 10, 40));
        p.add(scroll);
        add(p, BorderLayout.CENTER);
    }

    private void initFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 30, 0));
        SoftButton btnBack = createBigActionButton("CERRAR VENTANA", Color.WHITE, "btn_back.png");
        btnBack.setPreferredSize(new Dimension(220, 70));
        btnBack.addActionListener(e -> { SoundManager.getInstance().playClick(); dispose(); });
        footer.add(btnBack);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadData() {
        if (dateFrom.getDate() == null || dateTo.getDate() == null) return;
        LocalDate ldFrom = dateFrom.getDate(); LocalDate ldTo = dateTo.getDate();
        if(ldFrom.isAfter(ldTo)) {
            LuxuryMessage.show(this, "ERROR DE FECHAS", "La fecha 'Desde' no puede ser posterior a 'Hasta'.", true);
            return;
        }
        LocalDateTime ldtFrom = ldFrom.atStartOfDay();
        LocalDateTime ldtTo = ldTo.atTime(LocalTime.MAX);
        Date d1 = Date.from(ldtFrom.atZone(ZoneId.systemDefault()).toInstant());
        Date d2 = Date.from(ldtTo.atZone(ZoneId.systemDefault()).toInstant());
        model.setRowCount(0);
        List<Vector<Object>> data = productDAO.getHistoryByDateRange(d1, d2);
        for (Vector<Object> row : data) model.addRow(row);
    }

    // =========================================================================
    // EXPORTAR PDF (SIN CONFLICTOS DE NOMBRES)
    // =========================================================================
    private void exportToPDF() {
        try {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
            String fileName = "SICONI_Reporte_" + System.currentTimeMillis() + ".pdf";
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();

            BaseColor COLOR_GOLD_PDF = new BaseColor(212, 175, 55);
            BaseColor COLOR_DARK_PDF = new BaseColor(20, 20, 20);
            BaseColor COLOR_GREEN_PDF = new BaseColor(0, 150, 50);
            BaseColor COLOR_RED_PDF = new BaseColor(200, 0, 0);
            BaseColor COLOR_GRAY_PDF = new BaseColor(240, 240, 240);

            com.itextpdf.text.Font fontTitle = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 24, com.itextpdf.text.Font.BOLD, COLOR_DARK_PDF);
            com.itextpdf.text.Font fontSubtitle = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.GRAY);
            com.itextpdf.text.Font fontHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, COLOR_GOLD_PDF);
            com.itextpdf.text.Font fontCell = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 3f});

            // 1. LOGO (NOMBRE CALIFICADO PARA EVITAR CONFLICTO AWT)
            PdfPCell cellLogo = new PdfPCell();
            cellLogo.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            try {
                // BUSCA EL ARCHIVO "logo.png" en "/images/"
                com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(getClass().getResource("/images/logo.png"));
                img.scaleToFit(80, 80);
                cellLogo.addElement(img);
            } catch (Exception e) {
                cellLogo.addElement(new Paragraph("SICONI", fontTitle)); // Fallback
            }
            headerTable.addCell(cellLogo);

            PdfPCell cellTitles = new PdfPCell();
            cellTitles.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            cellTitles.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellTitles.addElement(new Paragraph("REPORTE DE AUDITORÍA DE INVENTARIO", fontTitle));
            cellTitles.addElement(new Paragraph("Generado el: " + new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date()), fontSubtitle));
            headerTable.addCell(cellTitles);

            doc.add(headerTable);

            PdfPTable lineTable = new PdfPTable(1);
            lineTable.setWidthPercentage(100);
            PdfPCell lineCell = new PdfPCell(new Phrase(""));
            lineCell.setBackgroundColor(COLOR_GOLD_PDF);
            lineCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            lineCell.setFixedHeight(3f);
            lineTable.addCell(lineCell);
            doc.add(new Paragraph(" "));
            doc.add(lineTable);
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(model.getColumnCount());
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 2.5f, 1.5f, 4f, 1.5f, 1f, 4f});

            for (int i = 0; i < model.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(model.getColumnName(i).toUpperCase(), fontHeader));
                cell.setBackgroundColor(COLOR_DARK_PDF);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPaddingTop(8); cell.setPaddingBottom(8);
                cell.setBorderColor(COLOR_GOLD_PDF);
                table.addCell(cell);
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    String cellText = model.getValueAt(i, j) != null ? model.getValueAt(i, j).toString() : "";
                    com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(fontCell);
                    if (j == 4) {
                        if (cellText.toUpperCase().contains("ENTRADA")) cellFont.setColor(COLOR_GREEN_PDF);
                        else if (cellText.toUpperCase().contains("SALIDA")) cellFont.setColor(COLOR_RED_PDF);
                        cellFont.setStyle(com.itextpdf.text.Font.BOLD);
                    }
                    PdfPCell cell = new PdfPCell(new Phrase(cellText, cellFont));
                    if (i % 2 != 0) cell.setBackgroundColor(COLOR_GRAY_PDF);
                    else cell.setBackgroundColor(BaseColor.WHITE);
                    cell.setPadding(6);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    if (j == 0 || j == 5) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    else cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setBorderColor(BaseColor.LIGHT_GRAY);
                    table.addCell(cell);
                }
            }
            doc.add(table);

            doc.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Fin del reporte. Total: " + model.getRowCount(), fontSubtitle);
            footer.setAlignment(Element.ALIGN_RIGHT);
            doc.add(footer);

            doc.close();

            LuxuryMessage.show(this, "REPORTE GENERADO", "Documento creado:\n" + fileName, false);
            try { Desktop.getDesktop().open(new File(fileName)); } catch (Exception ex) {}

        } catch (Exception e) {
            LuxuryMessage.show(this, "ERROR PDF", "No se pudo crear el reporte:\n" + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private SoftButton createBigActionButton(String text, Color accentColor, String imgName) {
        SoftButton btn = new SoftButton(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40,40,40)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(accentColor); g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BorderLayout());
        btn.setPreferredSize(new Dimension(160, 70));
        btn.setOpaque(false); btn.setBorder(new EmptyBorder(5,5,5,5));
        try {
            URL url = getClass().getResource("/images/icons/" + imgName);
            if (url != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH));
                JLabel lIcon = new JLabel(icon, SwingConstants.CENTER);
                btn.add(lIcon, BorderLayout.CENTER);
            }
        } catch (Exception e) {}
        JLabel lText = new JLabel(text, SwingConstants.CENTER);
        lText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lText.setForeground(accentColor);
        btn.add(lText, BorderLayout.SOUTH);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { lText.setForeground(Color.WHITE); SoundManager.getInstance().playHover(); }
            public void mouseExited(MouseEvent e) { lText.setForeground(accentColor); }
        });
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(LUX_TEXT_WHITE);
        l.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        return l;
    }
}