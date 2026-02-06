/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Módulo de Auditoría e Inventario
 * ARCHIVO: InventoryHistoryDialog.java
 *
 * AUTORA: Johanna Guedez
 * FECHA: 05 de Febrero de 2026
 * VERSIÓN: 13.0 (PLATINUM: High Header + Fuchsia Title + Clean Footer)
 * -----------------------------------------------------------------------------
 */
package com.swimcore.view.dialogs;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePickerSettings.DateArea;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class InventoryHistoryDialog extends JDialog {

    private final ProductDAO productDAO = new ProductDAO();
    private DefaultTableModel model;
    private DatePicker dateFrom;
    private DatePicker dateTo;
    private JTable table;

    // PALETA DE COLORES DE LUJO
    private final Color LUX_BG_DARK = new Color(20, 20, 20);
    private final Color LUX_GOLD = new Color(212, 175, 55);
    private final Color LUX_VERDE = new Color(0, 255, 128);
    private final Color LUX_ROJO = new Color(255, 0, 127);
    private final Color LUX_TEXT_WHITE = new Color(230, 230, 230);

    public InventoryHistoryDialog(Window parent) {
        super(parent, LanguageManager.get("audit.dialog.title"), ModalityType.APPLICATION_MODAL);
        setSize(1150, 700);
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
        header.setBorder(new EmptyBorder(25, 30, 10, 30));
        JLabel title = new JLabel(LanguageManager.get("audit.report.header.main", "HISTORIAL DE INVENTARIO").toUpperCase());
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
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

        // Fix Negrita
        PropertyChangeListener fontEnforcer = evt -> {
            if ("date".equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(() -> {
                    dateFrom.getComponentDateTextField().setFont(new Font("Segoe UI", Font.BOLD, 15));
                    dateTo.getComponentDateTextField().setFont(new Font("Segoe UI", Font.BOLD, 15));
                });
            }
        };
        dateFrom.addPropertyChangeListener(fontEnforcer);
        dateTo.addPropertyChangeListener(fontEnforcer);

        SoftButton btnFilter = createIconOnlyButton("icon_lupa_hq.png", LUX_GOLD, "Actualizar Búsqueda");
        btnFilter.addActionListener(e -> { SoundManager.getInstance().playClick(); loadData(); });

        SoftButton btnExportCSV = createIconOnlyButton("icon_cvs_hq.png", new Color(0, 150, 255), "Exportar Excel/CSV");
        btnExportCSV.addActionListener(e -> { SoundManager.getInstance().playClick(); exportToCSV(); });

        SoftButton btnReportePDF = createIconOnlyButton("icon_pdf_hq.png", LUX_ROJO, "Generar Reporte Oficial");
        btnReportePDF.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            exportToAestheticPDF();
        });

        panel.add(createLabel("DESDE:")); panel.add(dateFrom);
        panel.add(createLabel("HASTA:")); panel.add(dateTo);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(btnFilter);
        panel.add(btnExportCSV);
        panel.add(btnReportePDF);

        add(container, BorderLayout.NORTH);
    }

    private SoftButton createIconOnlyButton(String imgName, Color hoverColor, String tooltip) {
        SoftButton btn = new SoftButton(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30,30,30, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                } else {
                    g2.setColor(new Color(80,80,80));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                }
                g2.dispose();
            }
        };
        btn.setLayout(new BorderLayout());
        btn.setPreferredSize(new Dimension(75, 75));
        btn.setOpaque(false);
        btn.setBorder(null);
        btn.setToolTipText(tooltip);
        try {
            URL url = getClass().getResource("/images/icons/" + imgName);
            if (url != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                JLabel lIcon = new JLabel(icon, SwingConstants.CENTER);
                btn.add(lIcon, BorderLayout.CENTER);
            }
        } catch (Exception e) {}
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { SoundManager.getInstance().playHover(); btn.repaint(); }
            public void mouseExited(MouseEvent e) { btn.repaint(); }
        });
        return btn;
    }

    private SoftButton createBigActionButton(String text, Color accentColor, String imgName) {
        SoftButton btn = new SoftButton(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40,40,40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BorderLayout());
        btn.setPreferredSize(new Dimension(160, 70));
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(5,5,5,5));

        try {
            URL url = getClass().getResource("/images/icons/" + imgName);
            if (url != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
                JLabel lIcon = new JLabel(icon, SwingConstants.CENTER);
                btn.add(lIcon, BorderLayout.CENTER);
            }
        } catch (Exception e) {}

        JLabel lText = new JLabel(text, SwingConstants.CENTER);
        lText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lText.setForeground(accentColor);
        btn.add(lText, BorderLayout.SOUTH);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { lText.setForeground(Color.WHITE); SoundManager.getInstance().playHover(); }
            public void mouseExited(MouseEvent e) { lText.setForeground(accentColor); }
        });
        return btn;
    }

    private void styleLuxuryField(JTextField f) {
        f.setBackground(new Color(35, 35, 35));
        f.setForeground(Color.WHITE);
        f.setCaretColor(LUX_GOLD);
        f.setBorder(new CompoundBorder(new LineBorder(new Color(100, 100, 100)), new EmptyBorder(0, 10, 0, 10)));
        f.setPreferredSize(new Dimension(140, 40));
        f.setFont(new Font("Segoe UI", Font.BOLD, 15));
    }

    private void styleCalButton(JButton b) {
        b.setText("📅"); b.setBackground(LUX_GOLD); b.setForeground(Color.BLACK);
        b.setPreferredSize(new Dimension(40, 40)); b.setFocusPainted(false); b.setBorder(null);
    }

    private DatePickerSettings createLuxuryDarkSettings() {
        DatePickerSettings s = new DatePickerSettings();
        s.setFormatForDatesCommonEra("dd-MM-yyyy");
        s.setColor(DateArea.BackgroundOverallCalendarPanel, new Color(30, 30, 30));
        s.setColor(DateArea.BackgroundMonthAndYearMenuLabels, LUX_GOLD);
        s.setColor(DateArea.TextMonthAndYearMenuLabels, Color.BLACK);
        s.setColor(DateArea.CalendarTextWeekdays, LUX_GOLD);
        try { s.setColor(DateArea.CalendarTextNormalDates, Color.WHITE); } catch (Throwable t) {}
        s.setColor(DateArea.CalendarBackgroundNormalDates, new Color(30,30,30));
        s.setColor(DateArea.CalendarBackgroundSelectedDate, LUX_GOLD);
        s.setColor(DateArea.BackgroundTodayLabel, LUX_VERDE);
        s.setColor(DateArea.TextTodayLabel, Color.BLACK);
        s.setFontCalendarDateLabels(new Font("Segoe UI", Font.BOLD, 14));
        return s;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(LUX_TEXT_WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return l;
    }

    private void initTable() {
        String[] cols = {
                LanguageManager.get("audit.column.id", "ID"), LanguageManager.get("audit.column.date_time", "FECHA / HORA"),
                LanguageManager.get("audit.column.code", "CÓDIGO"), LanguageManager.get("audit.column.product", "PRODUCTO"),
                LanguageManager.get("audit.column.type", "TIPO"), LanguageManager.get("audit.column.quantity", "CANT."),
                LanguageManager.get("audit.column.observation_reason", "OBSERVACIÓN / MOTIVO")
        };
        model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        table.setRowHeight(40);
        table.setBackground(LUX_BG_DARK);
        table.setForeground(LUX_TEXT_WHITE);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(60, 60, 60));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(25, 25, 25));
        header.setForeground(LUX_GOLD);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 45));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                if (!isSel) c.setBackground(row % 2 == 0 ? LUX_BG_DARK : new Color(35, 35, 35));
                else c.setBackground(new Color(60, 60, 60));
                if (col == 4) {
                    String val = value != null ? value.toString().toUpperCase() : "";
                    if (val.contains("ENTRADA")) c.setForeground(LUX_VERDE);
                    else if (val.contains("SALIDA")) c.setForeground(LUX_ROJO);
                    else c.setForeground(LUX_TEXT_WHITE);
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                } else if (col == 5) {
                    c.setForeground(LUX_GOLD);
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                } else {
                    c.setForeground(LUX_TEXT_WHITE);
                }
                if (col == 3 || col == 6) setHorizontalAlignment(JLabel.LEFT); else setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50); cm.getColumn(1).setPreferredWidth(160);
        cm.getColumn(2).setPreferredWidth(80); cm.getColumn(3).setPreferredWidth(200);
        cm.getColumn(4).setPreferredWidth(100); cm.getColumn(5).setPreferredWidth(60);
        cm.getColumn(6).setPreferredWidth(350);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(LUX_GOLD, 1));
        scroll.getViewport().setBackground(LUX_BG_DARK);
        scroll.setPreferredSize(new Dimension(1000, 445));

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
        try {
            List<Vector<Object>> data = productDAO.getHistoryByDateRange(d1, d2);
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            for (Vector<Object> row : data) {
                Object rawDate = row.get(1);
                try {
                    if (rawDate != null) {
                        String dateStr = rawDate.toString();
                        if (dateStr.contains(".")) dateStr = dateStr.substring(0, dateStr.indexOf("."));
                        Date dateObj = sdfInput.parse(dateStr);
                        row.set(1, sdfOutput.format(dateObj));
                    }
                } catch (Exception ex) {}
                model.addRow(row);
            }
        } catch (Exception e) {
            LuxuryMessage.show(this, "ERROR", "Error al cargar datos: " + e.getMessage(), true);
        }
    }

    private void exportToCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar Reporte CSV");
        String defaultName = "Reporte_Inventario_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".csv";
        File reportDir = new File("Reportes/CSV");
        if (!reportDir.exists()) reportDir.mkdirs();
        fc.setCurrentDirectory(reportDir);
        fc.setSelectedFile(new File(reportDir, defaultName));
        fc.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fc.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".csv")) filePath += ".csv";
            try (FileWriter fw = new FileWriter(filePath)) {
                for (int i = 0; i < table.getColumnCount(); i++) fw.write(table.getColumnName(i) + ",");
                fw.write("\n");
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) fw.write(table.getValueAt(i, j) + ",");
                    fw.write("\n");
                }
                new LuxuryPDFDialog(this, filePath).setVisible(true);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    // --- REPORTE PDF (HEADER BIEN ARRIBA + TITULO FUCSIA + LOGO ABAJO) ---
    private void exportToAestheticPDF() {
        if (model.getRowCount() == 0) {
            LuxuryMessage.show(this, "VACÍO", "No hay datos para exportar.", true);
            return;
        }
        try {
            // AJUSTE: Margen superior reducido a 10 (antes 30 o 40)
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 10, 30);
            String fileName = "Reporte_SICONI_" + System.currentTimeMillis() + ".pdf";
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();

            BaseColor COLOR_GOLD = new BaseColor(212, 175, 55);
            BaseColor COLOR_HEADER_BG = BaseColor.BLACK;
            BaseColor COLOR_HEADER_TXT = new BaseColor(212, 175, 55);
            // AJUSTE: Color Fucsia Vibrante
            BaseColor COLOR_FUCSIA = new BaseColor(255, 0, 128);

            // TÍTULO EN FUCSIA
            com.itextpdf.text.Font fTitle = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 24, com.itextpdf.text.Font.BOLD, COLOR_FUCSIA);
            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, BaseColor.GRAY);

            Paragraph pTitle = new Paragraph("REPORTE DE INVENTARIO", fTitle);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitle);

            com.itextpdf.text.Font fBrand = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD, COLOR_GOLD);
            Paragraph pBrand = new Paragraph("DAYANA GUÉDEZ SWIMWEAR", fBrand);
            pBrand.setAlignment(Element.ALIGN_CENTER);
            pBrand.setSpacingBefore(5);
            doc.add(pBrand);

            String fecha = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date());
            Paragraph pDate = new Paragraph("Generado el: " + fecha, fSub);
            pDate.setAlignment(Element.ALIGN_CENTER);
            pDate.setSpacingBefore(2);
            doc.add(pDate);

            doc.add(new Paragraph(" "));
            PdfPTable lineT = new PdfPTable(1); lineT.setWidthPercentage(100);
            PdfPCell lineC = new PdfPCell(new Phrase(""));
            lineC.setBackgroundColor(COLOR_GOLD);
            lineC.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            lineC.setFixedHeight(4f);
            lineT.addCell(lineC);
            doc.add(lineT);
            doc.add(new Paragraph(" "));

            PdfPTable pTable = new PdfPTable(model.getColumnCount());
            pTable.setWidthPercentage(100);
            pTable.setWidths(new float[]{0.8f, 2.5f, 1.2f, 4f, 1.5f, 1f, 4f});

            for (int i = 0; i < model.getColumnCount(); i++) {
                PdfPCell c = new PdfPCell(new Phrase(model.getColumnName(i), new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, COLOR_HEADER_TXT)));
                c.setBackgroundColor(COLOR_HEADER_BG);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setPadding(8);
                c.setBorderColor(COLOR_GOLD);
                pTable.addCell(c);
            }

            for(int i=0; i<model.getRowCount(); i++) {
                for(int j=0; j<model.getColumnCount(); j++) {
                    String val = model.getValueAt(i, j).toString();
                    PdfPCell c = new PdfPCell(new Phrase(val, new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9)));
                    c.setPadding(6);
                    c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    c.setBackgroundColor((i % 2 == 0) ? BaseColor.WHITE : new BaseColor(245, 245, 245));
                    if (j == 0 || j == 5) c.setHorizontalAlignment(Element.ALIGN_CENTER);
                    if (j == 4) {
                        if (val.contains("ENTRADA")) c.setPhrase(new Phrase(val, new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD, new BaseColor(0,150,50))));
                        if (val.contains("SALIDA")) c.setPhrase(new Phrase(val, new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD, new BaseColor(200,0,0))));
                    }
                    pTable.addCell(c);
                }
            }
            doc.add(pTable);

            // --- AJUSTE: FIN DEL REPORTE ELIMINADO ---
            // Solo ponemos el Logo abajo
            try {
                com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(getClass().getResource("/images/logo.png"));
                img.scaleToFit(150, 80);
                img.setAlignment(Element.ALIGN_CENTER);
                img.setSpacingBefore(30); // Espacio antes del logo firma
                doc.add(img);
            } catch (Exception e) {}

            doc.close();

            new LuxuryPDFDialog(this, fileName).setVisible(true);

        } catch (Exception e) {
            LuxuryMessage.show(this, "ERROR PDF", e.getMessage(), true);
        }
    }

    private class LuxuryPDFDialog extends JDialog {
        public LuxuryPDFDialog(Dialog parent, String filePath) {
            super(parent, true);
            setUndecorated(true);
            setSize(380, 210);
            setLocationRelativeTo(parent);
            setBackground(new Color(0,0,0,0));

            JPanel content = new JPanel(new BorderLayout(0, 15)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(25, 25, 25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                    g2.setColor(LUX_GOLD);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 30, 30);
                    g2.dispose();
                }
            };
            content.setOpaque(false);
            content.setBorder(new EmptyBorder(20, 20, 10, 20));

            JLabel lblTitle = new JLabel("REPORTE GENERADO", SwingConstants.CENTER);
            lblTitle.setForeground(LUX_GOLD);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

            JLabel lblMsg = new JLabel("<html><div style='text-align: center; color: #E0E0E0;'>El archivo ha sido creado con éxito.<br>Seleccione una acción:</div></html>", SwingConstants.CENTER);
            lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            JPanel pBtns = new JPanel(new GridLayout(1, 2, 15, 0));
            pBtns.setOpaque(false);
            pBtns.setBorder(new EmptyBorder(10, 5, 10, 5));

            ThreeDButton btnFolder = new ThreeDButton("VER CARPETA", new Color(200, 150, 0));
            btnFolder.addActionListener(e -> {
                dispose();
                try { Runtime.getRuntime().exec("explorer.exe /select," + filePath); } catch (Exception ex) {}
            });

            ThreeDButton btnOpen = new ThreeDButton("ABRIR AHORA", new Color(0, 180, 150));
            btnOpen.addActionListener(e -> {
                dispose();
                try { Desktop.getDesktop().open(new File(filePath)); } catch (Exception ex) {}
            });

            pBtns.add(btnFolder);
            pBtns.add(btnOpen);

            JLabel lblClose = new JLabel("CERRAR VENTANA", SwingConstants.CENTER);
            lblClose.setForeground(Color.GRAY);
            lblClose.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblClose.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { dispose(); }
                public void mouseEntered(MouseEvent e) { lblClose.setForeground(Color.WHITE); }
                public void mouseExited(MouseEvent e) { lblClose.setForeground(Color.GRAY); }
            });

            JPanel pCenter = new JPanel(new BorderLayout());
            pCenter.setOpaque(false);
            pCenter.add(lblMsg, BorderLayout.NORTH);
            pCenter.add(pBtns, BorderLayout.CENTER);

            content.add(lblTitle, BorderLayout.NORTH);
            content.add(pCenter, BorderLayout.CENTER);
            content.add(lblClose, BorderLayout.SOUTH);

            setContentPane(content);
        }
    }

    private class ThreeDButton extends JButton {
        private Color baseColor;

        public ThreeDButton(String text, Color color) {
            super(text);
            this.baseColor = color;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(new Color(20, 20, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp;
            if (getModel().isPressed()) {
                gp = new GradientPaint(0, 0, baseColor.darker().darker(), 0, getHeight(), baseColor);
            } else {
                gp = new GradientPaint(0, 0, baseColor.brighter(), 0, getHeight(), baseColor.darker());
            }

            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            g2.setColor(baseColor.brighter().brighter());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}