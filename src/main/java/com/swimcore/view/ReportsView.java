/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ReportsView.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 2.6.0 (Currency Manager V3 Integration Fix)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Vista principal del Módulo de Reportes y Analíticas.
 * Se actualizó la lógica del Selector de Moneda para ser compatible
 * con CurrencyManager V3.0.0 (Uso de getMode/setConfig en lugar de Strings).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePickerSettings.DateArea;
import com.swimcore.dao.ReportsDAO;
import com.swimcore.util.CurrencyManager;
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
import java.awt.*;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsView extends JDialog {

    private final ReportsDAO reportsDAO = new ReportsDAO();
    private DatePicker dateFrom, dateTo;
    private JComboBox<String> currencySelector;
    private JPanel kpiPanel;
    private ChartPanel chartPanel;

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
        dateFrom = new DatePicker(createDatePickerSettings());
        dateFrom.getComponentDateTextField().setPreferredSize(new Dimension(150, 40));
        dateFrom.getComponentToggleCalendarButton().setPreferredSize(new Dimension(40, 40));
        dateFrom.setDate(LocalDate.now().minusMonths(1));
        headerPanel.add(dateFrom, gbc);

        gbc.gridx = 2;
        headerPanel.add(createLabel("Hasta:"), gbc);

        gbc.gridx = 3;
        dateTo = new DatePicker(createDatePickerSettings());
        dateTo.getComponentDateTextField().setPreferredSize(new Dimension(150, 40));
        dateTo.getComponentToggleCalendarButton().setPreferredSize(new Dimension(40, 40));
        dateTo.setDateToToday();
        headerPanel.add(dateTo, gbc);

        gbc.gridx = 4;
        // --- CORRECCIÓN PARA COMPATIBILIDAD CON CurrencyManager V3.0 ---
        // Orden alineado con los modos del Manager: 0=USD, 1=EUR, 2=BS
        currencySelector = new JComboBox<>(new String[]{"Dólares ($)", "Euros (€)", "Bolívares (Bs)"});
        currencySelector.setPreferredSize(new Dimension(120, 35));

        // Usamos getMode() (int) en lugar de getPreferredCurrency() (String)
        currencySelector.setSelectedIndex(CurrencyManager.getMode());

        currencySelector.addActionListener(e -> {
            // Actualizamos la configuración global usando setConfig
            int mode = currencySelector.getSelectedIndex();
            double tasa = CurrencyManager.getTasa();
            String symbol = (mode == 0) ? "$" : (mode == 1) ? "€" : "Bs.";

            CurrencyManager.setConfig(tasa, symbol, mode);
            generateReport();
        });
        headerPanel.add(currencySelector, gbc);
        // -------------------------------------------------------------

        gbc.gridx = 5; gbc.weightx = 1.0;
        headerPanel.add(Box.createHorizontalGlue(), gbc);

        gbc.gridx = 6; gbc.weightx = 0;
        SoftButton btnGenerate = new SoftButton(createIcon("/images/icons/icon_report.png", 40, 40));
        btnGenerate.setToolTipText("Generar Reporte");
        btnGenerate.setPreferredSize(new Dimension(90, 70));
        btnGenerate.addActionListener(e -> generateReport());
        headerPanel.add(btnGenerate, gbc);

        gbc.gridx = 7;
        SoftButton btnExit = new SoftButton(createIcon("/images/icons/icon_exit.png", 40, 40));
        btnExit.setToolTipText("Volver al Dashboard");
        btnExit.setPreferredSize(new Dimension(90, 70));
        btnExit.addActionListener(e -> dispose());
        headerPanel.add(btnExit, gbc);

        add(headerPanel, BorderLayout.NORTH);
    }

    private DatePickerSettings createDatePickerSettings() {
        DatePickerSettings settings = new DatePickerSettings();

        settings.setColor(DateArea.BackgroundOverallCalendarPanel, new Color(40,40,40));
        settings.setColor(DateArea.BackgroundMonthAndYearMenuLabels, LUX_GOLD);
        settings.setColor(DateArea.TextMonthAndYearMenuLabels, Color.BLACK);
        settings.setColor(DateArea.CalendarBackgroundSelectedDate, COLOR_FUCSIA);
        settings.setColor(DateArea.BackgroundTodayLabel, LUX_GOLD);
        settings.setColor(DateArea.TextTodayLabel, Color.BLACK);

        return settings;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private void initComponents() {
        kpiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        kpiPanel.setOpaque(false);

        chartPanel = new ChartPanel(null);
        chartPanel.setOpaque(false);
        chartPanel.setBorder(new EmptyBorder(10, 30, 30, 30));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(kpiPanel, BorderLayout.NORTH);
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void generateReport() {
        if (dateFrom.getDate() == null || dateTo.getDate() == null) return;

        SoundManager.getInstance().playClick();

        Date startDate = Date.from(dateFrom.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(dateTo.getDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        Map<String, Double> summary = reportsDAO.getFinancialSummary(startDate, endDate);
        updateKPIs(summary);

        List<Object[]> topProducts = reportsDAO.getTopSellingProducts(startDate, endDate, 5);
        updateChart(topProducts);
    }

    private void updateKPIs(Map<String, Double> summary) {
        kpiPanel.removeAll();

        double rate = CurrencyManager.getTasa();
        String currency = (String) currencySelector.getSelectedItem();
        NumberFormat nf = NumberFormat.getCurrencyInstance(currency != null && currency.contains("€") ? Locale.GERMANY : Locale.US);

        double ingresosBase = summary.getOrDefault("ingresos", 0.0);
        double costosBase = summary.getOrDefault("costos", 0.0);
        double gananciasBase = summary.getOrDefault("ganancias", 0.0);

        String ingresosStr, costosStr, gananciasStr;

        if (currency != null && currency.contains("Bs")) {
            ingresosStr = "Bs " + String.format("%,.2f", ingresosBase * rate);
            costosStr = "Bs " + String.format("%,.2f", costosBase * rate);
            gananciasStr = "Bs " + String.format("%,.2f", gananciasBase * rate);
        } else {
            // Para Dólares y Euros, usamos el formateador estándar
            // NOTA: Si quisieras conversión a Euros real, aquí aplicarías la lógica,
            // pero por ahora asumimos Base USD.
            ingresosStr = nf.format(ingresosBase);
            costosStr = nf.format(costosBase);
            gananciasStr = nf.format(gananciasBase);
        }

        kpiPanel.add(new KPI_Card(ingresosStr, "Ingresos Totales"));
        kpiPanel.add(new KPI_Card(costosStr, "Costo de Ventas"));
        kpiPanel.add(new KPI_Card(gananciasStr, "Ganancia Neta"));

        kpiPanel.revalidate();
        kpiPanel.repaint();
    }

    private void updateChart(List<Object[]> topProducts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Object[] product : topProducts) {
            String name = (String) product[0];
            Integer quantity = (Integer) product[1];
            dataset.addValue(quantity, "Unidades Vendidas", name);
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

        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                return new GradientPaint(0, 0, COLOR_FUCSIA.brighter(), 0, (float) chartPanel.getHeight(), COLOR_FUCSIA.darker());
            }
        };
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(true);
        plot.setRenderer(renderer);

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
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private ImageIcon createIcon(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {}
        return null;
    }
}