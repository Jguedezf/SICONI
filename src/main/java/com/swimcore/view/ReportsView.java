/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ReportsView.java
 * VERSIÓN: 8.1.0 (FIXED: JFreeChart 1.5 Compatibility + Dark Calendar)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.swimcore.dao.SaleDAO;
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.LuxuryMessage;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.KPI_Card;
import com.swimcore.view.components.SoftButton;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.TextAnchor; // <--- CORREGIDO (Antes era org.jfree.ui)
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ReportsView extends JDialog {

    private final SaleDAO saleDAO = new SaleDAO();
    private DatePicker dateFrom, dateTo;
    private SoftButton btnCurrencyToggle;

    private JPanel kpiContainer;
    private ChartPanel donutPanel;
    private ChartPanel barPanel;
    private JTable profitabilityTable;
    private DefaultTableModel profitabilityTableModel;
    private JLabel lblStatus;

    // COLORES
    private final Color LUX_GOLD = new Color(212, 175, 55);
    private final Color DARK_BG = new Color(18, 18, 18);
    private final Color COL_COBRADO = new Color(46, 204, 113);
    private final Color COL_PENDIENTE = new Color(231, 76, 60);
    private final Color COL_PRODUCCION = new Color(52, 152, 219);

    public ReportsView(JFrame parent) {
        super(parent, "SICONI - DASHBOARD GERENCIAL", true);
        setSize(1280, 760);
        setLocationRelativeTo(parent);

        setContentPane(new PanelFondo("/images/bg2.png"));
        setLayout(new BorderLayout(0, 0));

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
                g.setColor(DARK_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        // FECHAS
        headerPanel.add(createLabel("DESDE:"));
        dateFrom = createLuxuryDatePicker();
        dateFrom.setDate(LocalDate.now().minusMonths(1));
        headerPanel.add(dateFrom);

        headerPanel.add(createLabel("HASTA:"));
        dateTo = createLuxuryDatePicker();
        dateTo.setDateToToday();
        headerPanel.add(dateTo);

        // MONEDA
        btnCurrencyToggle = new SoftButton(null);
        btnCurrencyToggle.setPreferredSize(new Dimension(160, 40));
        btnCurrencyToggle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCurrencyToggle.setBackground(new Color(40, 40, 40));
        btnCurrencyToggle.setForeground(LUX_GOLD);
        btnCurrencyToggle.setBorder(new LineBorder(LUX_GOLD, 1));
        updateCurrencyButtonText();
        btnCurrencyToggle.addActionListener(e -> {
            int current = CurrencyManager.getMode();
            int next = (current + 1) % 3;
            String symbol = (next == 0) ? "$" : (next == 1) ? "€" : "Bs.";
            CurrencyManager.setConfig(CurrencyManager.getTasa(), symbol, next);
            updateCurrencyButtonText();
            generateReport();
        });
        headerPanel.add(btnCurrencyToggle);

        // ESTADO
        lblStatus = new JLabel(" ", SwingConstants.RIGHT);
        lblStatus.setPreferredSize(new Dimension(150, 40));
        headerPanel.add(lblStatus);

        SoftButton btnGenerate = new SoftButton(null);
        btnGenerate.setText("GENERAR");
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGenerate.setPreferredSize(new Dimension(120, 40));
        btnGenerate.addActionListener(e -> generateReport());
        headerPanel.add(btnGenerate);

        SoftButton btnExit = new SoftButton(null);
        btnExit.setText("CERRAR");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExit.setPreferredSize(new Dimension(100, 40));
        btnExit.setBackground(new Color(100, 20, 20));
        btnExit.addActionListener(e -> dispose());
        headerPanel.add(btnExit);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. SIDEBAR IZQUIERDO
        kpiContainer = new JPanel();
        kpiContainer.setLayout(new BoxLayout(kpiContainer, BoxLayout.Y_AXIS));
        kpiContainer.setOpaque(false);
        kpiContainer.setPreferredSize(new Dimension(280, 0));

        kpiContainer.add(Box.createVerticalStrut(10));
        kpiContainer.add(createKPICard("INGRESOS", "$ 0.00"));
        kpiContainer.add(Box.createVerticalStrut(20));
        kpiContainer.add(createKPICard("COSTOS", "$ 0.00"));
        kpiContainer.add(Box.createVerticalStrut(20));
        kpiContainer.add(createKPICard("GANANCIA", "$ 0.00"));
        kpiContainer.add(Box.createVerticalGlue());

        contentPanel.add(kpiContainer, BorderLayout.WEST);

        // 2. CENTRO
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // ARRIBA: BARRAS
        barPanel = new ChartPanel(null);
        barPanel.setOpaque(false);
        barPanel.setPreferredSize(new Dimension(0, 300));
        centerPanel.add(createChartWrapper(barPanel), BorderLayout.NORTH);

        // ABAJO: DONUT + TABLA
        JPanel bottomSplit = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomSplit.setOpaque(false);

        donutPanel = new ChartPanel(null);
        donutPanel.setOpaque(false);
        bottomSplit.add(createChartWrapper(donutPanel));

        bottomSplit.add(createProfitabilityTable());

        centerPanel.add(bottomSplit, BorderLayout.CENTER);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private KPI_Card createKPICard(String title, String value) {
        KPI_Card card = new KPI_Card(value, title);
        card.setMaximumSize(new Dimension(280, 120));
        return card;
    }

    private JPanel createChartWrapper(ChartPanel chart) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60,60,60), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    private JScrollPane createProfitabilityTable() {
        String[] columns = {"PROD.", "UNID.", "GANANCIA"};
        profitabilityTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        profitabilityTable = new JTable(profitabilityTableModel);

        profitabilityTable.setRowHeight(35);
        profitabilityTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        profitabilityTable.setForeground(Color.WHITE);
        profitabilityTable.setGridColor(new Color(50, 50, 50));
        profitabilityTable.setShowVerticalLines(false);

        JTableHeader header = profitabilityTable.getTableHeader();
        header.setBackground(new Color(20, 20, 20));
        header.setForeground(LUX_GOLD);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(new Color(30, 30, 30));
        centerRenderer.setForeground(Color.WHITE);

        for (int i = 0; i < 3; i++) profitabilityTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(profitabilityTable);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.setBorder(BorderFactory.createLineBorder(LUX_GOLD, 1));
        return scrollPane;
    }

    private void generateReport() {
        if (dateFrom.getDate() == null || dateTo.getDate() == null) return;
        SoundManager.getInstance().playClick();

        Date startDate = Date.from(dateFrom.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(dateTo.getDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        lblStatus.setText("Generando...");

        new SwingWorker<ReportData, Void>() {
            @Override
            protected ReportData doInBackground() throws Exception {
                ReportData data = new ReportData();
                data.financials = saleDAO.getFinancialReport(startDate, endDate);
                data.topProducts = saleDAO.getTopSellingProducts(startDate, endDate);
                data.profitability = saleDAO.getProductProfitability(startDate, endDate);
                return data;
            }

            @Override
            protected void done() {
                try {
                    ReportData data = get();
                    updateKPIs(data.financials);
                    updateDonutChart(data.financials);
                    updateHorizontalBarChart(data.topProducts);
                    updateProfitabilityTable(data.profitability);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    lblStatus.setText(" ");
                }
            }
        }.execute();
    }

    private static class ReportData {
        Map<String, Double> financials;
        List<Object[]> topProducts;
        List<Object[]> profitability;
    }

    private void updateKPIs(Map<String, Double> summary) {
        kpiContainer.removeAll();
        kpiContainer.add(Box.createVerticalStrut(10));

        double rate = (CurrencyManager.getMode() == 2) ? CurrencyManager.getTasa() : 1.0;
        String symbol = CurrencyManager.getSymbol();

        double ingresos = summary.getOrDefault("ingresos", 0.0) * rate;
        double costos = summary.getOrDefault("costos", 0.0) * rate;
        double ganancias = summary.getOrDefault("ganancias", 0.0) * rate;

        kpiContainer.add(createKPICard("INGRESOS", String.format("%s %,.0f", symbol, ingresos)));
        kpiContainer.add(Box.createVerticalStrut(20));
        kpiContainer.add(createKPICard("COSTOS", String.format("%s %,.0f", symbol, costos)));
        kpiContainer.add(Box.createVerticalStrut(20));
        kpiContainer.add(createKPICard("GANANCIA", String.format("%s %,.0f", symbol, ganancias)));
        kpiContainer.add(Box.createVerticalGlue());

        kpiContainer.revalidate();
        kpiContainer.repaint();
    }

    private void updateProfitabilityTable(List<Object[]> data) {
        profitabilityTableModel.setRowCount(0);
        double rate = (CurrencyManager.getMode() == 2) ? CurrencyManager.getTasa() : 1.0;
        String symbol = CurrencyManager.getSymbol();

        for (Object[] row : data) {
            profitabilityTableModel.addRow(new Object[]{
                    row[0].toString(),
                    row[1],
                    String.format("%s %,.0f", symbol, (double)row[4] * rate)
            });
        }
    }

    private void updateDonutChart(Map<String, Double> financials) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("COBRADO", 60);
        dataset.setValue("POR COBRAR", 30);
        dataset.setValue("EN PRODUCCIÓN", 10);

        JFreeChart chart = ChartFactory.createRingChart(
                "ESTADO DE CUENTAS", dataset, false, true, false);

        chart.setBackgroundPaint(new Color(0,0,0,0));
        TextTitle title = chart.getTitle();
        title.setPaint(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(0,0,0,0));
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(new Color(0,0,0,150));

        plot.setSectionPaint("COBRADO", COL_COBRADO);
        plot.setSectionPaint("POR COBRAR", COL_PENDIENTE);
        plot.setSectionPaint("EN PRODUCCIÓN", COL_PRODUCCION);

        plot.setSectionOutlinesVisible(false);
        plot.setShadowPaint(null);

        donutPanel.setChart(chart);
    }

    private void updateHorizontalBarChart(List<Object[]> topProducts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int count = 0;
        for (Object[] product : topProducts) {
            dataset.addValue((Integer) product[1], "Ventas", (String) product[0]);
            count++;
        }
        if (count < 5) dataset.addValue(1, "Ventas", "Producto Demo");

        JFreeChart chart = ChartFactory.createBarChart(
                "TOP PRODUCTOS (UNIDADES)", "", "", dataset,
                PlotOrientation.HORIZONTAL, false, true, false);

        chart.setBackgroundPaint(new Color(0,0,0,0));
        TextTitle title = chart.getTitle();
        title.setPaint(LUX_GOLD);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(30, 30, 30, 150));
        plot.setOutlineVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelPaint(Color.GRAY);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVisible(false);

        BarRenderer renderer = new BarRenderer();
        renderer.setSeriesPaint(0, new GradientPaint(0, 0, LUX_GOLD, 0, 0, new Color(150, 110, 20)));
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);

        // --- AQUÍ ESTÁ EL ARREGLO DE VERSIONADO JFreeChart 1.5 ---
        // Usamos 'setDefault...' en lugar de 'setBase...'
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{1}", NumberFormat.getIntegerInstance()));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        renderer.setDefaultItemLabelPaint(Color.BLACK);

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
            @Override
            public String generateLabel(org.jfree.data.category.CategoryDataset dataset, int row, int column) {
                String name = (String) dataset.getColumnKey(column);
                Number val = dataset.getValue(row, column);
                return name + " (" + val + ")";
            }
        });
        renderer.setDefaultItemLabelPaint(new Color(20, 20, 20));
        // Posicionamiento
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));

        plot.setRenderer(renderer);
        barPanel.setChart(chart);
    }

    private DatePicker createLuxuryDatePicker() {
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("dd-MM-yyyy");

        settings.setColor(DatePickerSettings.DateArea.TextFieldBackgroundValidDate, new Color(40, 40, 40));
        settings.setColor(DatePickerSettings.DateArea.DatePickerTextValidDate, Color.WHITE);

        // Configuración de colores oscuros para el calendario
        Color bgDark = new Color(30, 30, 30);
        settings.setColor(DatePickerSettings.DateArea.CalendarBackgroundNormalDates, bgDark);
        settings.setColor(DatePickerSettings.DateArea.BackgroundMonthAndYearMenuLabels, bgDark);
        settings.setColor(DatePickerSettings.DateArea.BackgroundTodayLabel, bgDark);
        settings.setColor(DatePickerSettings.DateArea.CalendarTextNormalDates, Color.WHITE);
        settings.setColor(DatePickerSettings.DateArea.CalendarTextWeekdays, LUX_GOLD);

        settings.setAllowKeyboardEditing(false);

        DatePicker dp = new DatePicker(settings);
        dp.setPreferredSize(new Dimension(150, 35));
        dp.getComponentDateTextField().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dp.getComponentDateTextField().setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(LUX_GOLD, 1), new EmptyBorder(5, 5, 5, 5)));

        JButton btn = dp.getComponentToggleCalendarButton();
        btn.setText("📅");
        btn.setBackground(LUX_GOLD);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btn.setPreferredSize(new Dimension(35, 35));
        btn.setFocusPainted(false);

        return dp;
    }

    private void updateCurrencyButtonText() {
        int mode = CurrencyManager.getMode();
        String text = (mode == 0) ? "MONEDA: $" : (mode == 1) ? "MONEDA: €" : "MONEDA: Bs.";
        btnCurrencyToggle.setText(text);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(Color.LIGHT_GRAY);
        return label;
    }
}