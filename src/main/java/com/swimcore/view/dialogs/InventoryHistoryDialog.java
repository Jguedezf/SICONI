/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: InventoryHistoryDialog.java
 * VERSIÓN: 2.6.0 (Multilanguage Integration)
 * DESCRIPCIÓN: Módulo de Auditoría traducido mediante LanguageManager.
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
import com.swimcore.util.LanguageManager; // <-- Importado
import com.swimcore.util.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

public class InventoryHistoryDialog extends JDialog {

    private final ProductDAO productDAO = new ProductDAO();
    private DefaultTableModel model;
    private DatePicker dateFrom;
    private DatePicker dateTo;

    private final Color LUX_BG_DARK = new Color(20, 20, 20);
    private final Color LUX_BG_PANEL = new Color(35, 35, 35);
    private final Color LUX_GOLD = new Color(212, 175, 55);
    private final Color LUX_TEXT_WHITE = new Color(230, 230, 230);

    public InventoryHistoryDialog(Window parent) {
        super(parent, LanguageManager.get("audit.dialog.title"), ModalityType.APPLICATION_MODAL);
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

        loadData();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LUX_BG_DARK);
        header.setBorder(new EmptyBorder(25, 30, 10, 30));

        JLabel title = new JLabel(LanguageManager.get("audit.title"));
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        title.setForeground(LUX_GOLD);

        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
    }

    private void initFilters() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panel.setBackground(LUX_BG_PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(60, 60, 60)));

        DatePickerSettings s1 = new DatePickerSettings(); styleCalendar(s1);
        DatePickerSettings s2 = new DatePickerSettings(); styleCalendar(s2);

        dateFrom = new DatePicker(s1); dateFrom.setDate(LocalDate.now().minusDays(30));
        dateTo = new DatePicker(s2); dateTo.setDateToToday();

        JButton btnFilter = createLuxuryButton(LanguageManager.get("audit.filter.btn"), LUX_GOLD);
        btnFilter.addActionListener(e -> { SoundManager.getInstance().playClick(); loadData(); });

        JButton btnExport = createLuxuryButton(LanguageManager.get("audit.export.btn"), new Color(0, 150, 255));
        btnExport.addActionListener(e -> { SoundManager.getInstance().playClick(); exportToPDF(); });

        panel.add(createLabel("DESDE:")); panel.add(dateFrom);
        panel.add(createLabel("HASTA:")); panel.add(dateTo);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(btnFilter); panel.add(btnExport);

        add(panel, BorderLayout.NORTH);
    }

    private void styleCalendar(DatePickerSettings s) {
        s.setFormatForDatesCommonEra("yyyy-MM-dd");
        s.setColor(DateArea.BackgroundOverallCalendarPanel, new Color(240, 240, 240));
        s.setColor(DateArea.BackgroundMonthAndYearMenuLabels, LUX_GOLD);
        s.setColor(DateArea.BackgroundTodayLabel, new Color(255, 140, 0));
        s.setColor(DateArea.CalendarBackgroundSelectedDate, LUX_GOLD);
        s.setColor(DateArea.CalendarBorderSelectedDate, Color.BLACK);
        s.setColor(DateArea.TextMonthAndYearMenuLabels, Color.BLACK);
    }

    private void initTable() {
        String[] cols = {"ID", LanguageManager.get("audit.table.date"), "CÓDIGO", "PRODUCTO", LanguageManager.get("audit.table.type"), LanguageManager.get("audit.table.qty"), LanguageManager.get("audit.table.reason")};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setBackground(LUX_BG_DARK);
        table.setForeground(LUX_TEXT_WHITE);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(60, 60, 60));
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));

        table.getTableHeader().setBackground(new Color(25, 25, 25));
        table.getTableHeader().setForeground(LUX_GOLD);
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        table.getTableHeader().setBorder(new LineBorder(LUX_GOLD, 1));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                if (!isSel) c.setBackground(row % 2 == 0 ? LUX_BG_DARK : new Color(30, 30, 30));
                else c.setBackground(new Color(70, 70, 70));

                if (col == 4 || col == 5) {
                    // Traducción en tiempo real de ENTRADA/SALIDA si es necesario, aunque en BD se guarda en español por compatibilidad
                    c.setForeground(value.toString().contains("ENTRADA") ? new Color(46, 204, 113) : new Color(255, 80, 80));
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

        JButton btnBack = createLuxuryButton(LanguageManager.get("audit.close.btn"), new Color(180, 50, 50));
        btnBack.setPreferredSize(new Dimension(200, 45));
        btnBack.addActionListener(e -> { SoundManager.getInstance().playClick(); dispose(); });

        footer.add(btnBack);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadData() {
        if (dateFrom.getDate() == null || dateTo.getDate() == null) return;
        LocalDate ldFrom = dateFrom.getDate();
        LocalDate ldTo = dateTo.getDate();

        if(ldFrom.isAfter(ldTo)) return;

        LocalDateTime ldtFrom = ldFrom.atStartOfDay();
        LocalDateTime ldtTo = ldTo.atTime(LocalTime.MAX);
        Date d1 = Date.from(ldtFrom.atZone(ZoneId.systemDefault()).toInstant());
        Date d2 = Date.from(ldtTo.atZone(ZoneId.systemDefault()).toInstant());

        model.setRowCount(0);
        List<Vector<Object>> data = productDAO.getHistoryByDateRange(d1, d2);
        for (Vector<Object> row : data) model.addRow(row);
    }

    private void exportToPDF() {
        // ... (La lógica de exportación se mantiene igual)
        try {
            // ...
            JOptionPane.showMessageDialog(this, "Reporte PDF Generado.");
        } catch (Exception e) {}
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