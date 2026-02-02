/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ReportsView.java
 * VERSIÓN: 3.0.0 (Luxury Fix: Calendars + Real Data Integration)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.swimcore.dao.SaleDAO; // CAMBIO: Usamos SaleDAO
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.LuxuryCalendar; // IMPORTANTE: Tu clase Luxury
import com.swimcore.util.LuxuryMessage; // Mensajes bonitos
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.KPI_Card;
import com.swimcore.view.components.SoftButton;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReportsView extends JDialog {

    private final SaleDAO saleDAO = new SaleDAO(); // Usamos SaleDAO para la data
    private DatePicker dateFrom, dateTo;
    private JComboBox<String> currencySelector;
    private JPanel kpiPanel;
    private ChartPanel chartPanel;
    private JTable profitabilityTable;
    private DefaultTableModel profitabilityTableModel;

    private final Color COLOR_FUCSIA = new Color(220, 0, 115);
    private final Color LUX_GOLD = new Color(218, 165, 32);

    public ReportsView(JFrame parent) {
        super(parent, "SICONI - Módulo de Reportes y Estadísticas", true);
        setSize(1200, 750);
        setLocationRelativeTo(parent);

        setContentPane(new PanelFondo("/images/bg2.png"));
        setLayout(new BorderLayout(0, 10));

        initHeader();
        initComponents();

        SwingUtilities.invokeLater(this::generateReport);
    }

    class PanelFondo extends JPanel {
        private Image imagen;
        public PanelFondo(String ruta) {
            try {
                URL url = getClass().getResource(ruta);
                if (url != null) imagen = new ImageIcon(url).getImage();
            } catch (Exception e) {}
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagen != null) {
                g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(18, 18, 18));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.weightx = 0;
        headerPanel.add(createLabel("Desde:"), gbc);

        gbc.gridx = 1;
        // MODIFICACIÓN: Usar LuxuryCalendar en lugar de createDatePickerSettings
        dateFrom = new DatePicker();
        LuxuryCalendar.applyTo(dateFrom);
        dateFrom.setDate(LocalDate.now().minusMonths(1));
        headerPanel.add(dateFrom, gbc);

        gbc.gridx = 2;
        headerPanel.add(createLabel("Hasta:"), gbc);

        gbc.gridx = 3;
        // MODIFICACIÓN: Usar LuxuryCalendar
        dateTo = new DatePicker();
        LuxuryCalendar.applyTo(dateTo);
        dateTo.setDateToToday();
        headerPanel.add(dateTo, gbc);

        gbc.gridx = 4;
        currencySelector = new JComboBox<>(new String[]{"Dólares ($)", "Euros (€)", "Bolívares (Bs)"});
        currencySelector.setPreferredSize(new Dimension(140, 40));
        currencySelector.setSelectedIndex(CurrencyManager.getMode());
        currencySelector.addActionListener(e -> {
            int mode = currencySelector.getSelectedIndex();
            double tasa = CurrencyManager.getTasa();
            String symbol = (mode == 0) ? "$" : (mode == 1) ? "€" : "Bs.";
            CurrencyManager.setConfig(tasa, symbol, mode);
            generateReport();
        });
        headerPanel.add(currencySelector, gbc);

        gbc.gridx = 5; gbc.weightx = 1.0;
        headerPanel.add(Box.createHorizontalGlue(), gbc);

        gbc.gridx = 6; gbc.weightx = 0;
        SoftButton btnGenerate = new SoftButton(createIcon("/images/icons/icon_report.png", 32, 32));
        btnGenerate.setText("GENERAR");
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGenerate.setPreferredSize(new Dimension(140, 50));
        btnGenerate.addActionListener(e -> generateReport());
        headerPanel.add(btnGenerate, gbc);

        gbc.gridx = 7;
        SoftButton btnExit = new SoftButton(createIcon("/images/icons/icon_exit.png", 32, 32));
        btnExit.setText("CERRAR");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExit.setPreferredSize(new Dimension(140, 50));
        btnExit.addActionListener(e -> dispose());
        headerPanel.add(btnExit, gbc);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void initComponents() {
        kpiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        kpiPanel.setOpaque(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setOpaque(false);

        chartPanel = new ChartPanel(null);
        chartPanel.setOpaque(false);
        tabbedPane.addTab("  Top Productos Vendidos  ", chartPanel);

        tabbedPane.addTab("  Rentabilidad por Producto  ", createProfitabilityTab());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 30, 30, 30));
        centerPanel.add(kpiPanel, BorderLayout.NORTH);
        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JComponent createProfitabilityTab() {
        String[] columns = {"Producto", "Unidades Vendidas", "Ingresos", "Costo Total", "GANANCIA NETA"};
        profitabilityTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        profitabilityTable = new JTable(profitabilityTableModel);

        profitabilityTable.setRowHeight(30);
        profitabilityTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        profitabilityTable.setBackground(new Color(30, 30, 30));
        profitabilityTable.setForeground(Color.WHITE);
        profitabilityTable.setGridColor(new Color(60, 60, 60));
        profitabilityTable.getTableHeader().setBackground(Color.BLACK);
        profitabilityTable.getTableHeader().setForeground(LUX_GOLD);
        profitabilityTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        for(int i = 1; i < columns.length; i++) {
            profitabilityTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(profitabilityTable);
        scrollPane.getViewport().setBackground(new Color(30,30,30));
        return scrollPane;
    }

    private void generateReport() {
        if (dateFrom.getDate() == null || dateTo.getDate() == null) return;
        SoundManager.getInstance().playClick();
        Date startDate = Date.from(dateFrom.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(dateTo.getDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        if (startDate.after(endDate)) {
            LuxuryMessage.show(this, "ERROR FECHAS", "La fecha inicial no puede ser mayor a la final.", true);
            return;
        }

        // CONEXIÓN CON SaleDAO
        updateKPIs(saleDAO.getFinancialReport(startDate, endDate));
        updateChart(saleDAO.getTopSellingProducts(startDate, endDate));
        updateProfitabilityTable(saleDAO.getProductProfitability(startDate, endDate));
    }

    private void updateKPIs(Map<String, Double> summary) {
        kpiPanel.removeAll();

        double rate = (CurrencyManager.getMode() == 2) ? CurrencyManager.getTasa() : 1.0;
        String symbol = CurrencyManager.getSymbol();

        double ingresos = summary.getOrDefault("ingresos", 0.0) * rate;
        double costos = summary.getOrDefault("costos", 0.0) * rate;
        double ganancias = summary.getOrDefault("ganancias", 0.0) * rate;

        kpiPanel.add(new KPI_Card(String.format("%s %,.2f", symbol, ingresos), "Ingresos Totales"));
        kpiPanel.add(new KPI_Card(String.format("%s %,.2f", symbol, costos), "Costo de Ventas"));
        kpiPanel.add(new KPI_Card(String.format("%s %,.2f", symbol, ganancias), "Ganancia Neta"));
        kpiPanel.revalidate();
        kpiPanel.repaint();
    }

    private void updateProfitabilityTable(List<Object[]> data) {
        profitabilityTableModel.setRowCount(0);
        double rate = (CurrencyManager.getMode() == 2) ? CurrencyManager.getTasa() : 1.0;
        String symbol = CurrencyManager.getSymbol();

        for (Object[] row : data) {
            profitabilityTableModel.addRow(new Object[]{
                    row[0],
                    row[1],
                    String.format("%s %,.2f", symbol, (double)row[2] * rate),
                    String.format("%s %,.2f", symbol, (double)row[3] * rate),
                    String.format("%s %,.2f", symbol, (double)row[4] * rate)
            });
        }
    }

    private void updateChart(List<Object[]> topProducts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (topProducts.isEmpty()) {
            dataset.addValue(0, "Sin Datos", "Sin Ventas");
        } else {
            for (Object[] product : topProducts) {
                dataset.addValue((Integer) product[1], "Unidades Vendidas", (String) product[0]);
            }
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Top 5 Productos Más Vendidos", "Producto", "Unidades Vendidas",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        barChart.setBackgroundPaint(new Color(0,0,0,0));
        barChart.getTitle().setPaint(Color.WHITE);
        barChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 24));

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(30, 30, 30, 180));
        plot.setRangeGridlinePaint(new Color(80, 80, 80));
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new GradientPaint(0, 0, COLOR_FUCSIA.brighter(), 0, 400, COLOR_FUCSIA.darker()));
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        domainAxis.setTickLabelPaint(Color.LIGHT_GRAY);
        domainAxis.setLabelPaint(Color.WHITE);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(Color.WHITE);
        rangeAxis.setLabelPaint(Color.LIGHT_GRAY);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        chartPanel.setChart(barChart);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private ImageIcon createIcon(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (Exception e) {}
        return null;
    }
}