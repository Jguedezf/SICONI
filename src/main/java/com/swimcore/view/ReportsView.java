/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ReportsView.java
 * VERSIÓN: 17.0.0 (FINAL: Visible KPIs + Neon Numbers + Fixed Labels)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.swimcore.dao.SaleDAO;
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.LuxuryMessage;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.BevelBorder;
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
import java.util.*;
import java.util.List;
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

    // COLORES NEÓN PUROS
    private final Color LUX_GOLD = new Color(212, 175, 55);
    private final Color DARK_BG = new Color(18, 18, 18);
    private final Color SIDEBAR_BG = new Color(12, 12, 12);

    // ESTADOS
    private final Color NEON_GREEN = new Color(0, 255, 65);
    private final Color NEON_RED = new Color(255, 0, 85);
    private final Color NEON_BLUE = new Color(0, 255, 255);

    // PALETA MULTICOLOR
    private final Color[] BAR_COLORS = {
            new Color(255, 215, 0),   // Oro
            new Color(0, 255, 255),   // Cian
            new Color(255, 0, 255),   // Magenta
            new Color(57, 255, 20),   // Verde Matrix
            new Color(255, 100, 0)    // Naranja
    };

    public ReportsView(JFrame parent) {
        super(parent, "SICONI - Dashboard Gerencial", true);
        setSize(1180, 710);
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
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 20, 5, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        // FECHAS
        gbc.gridx = 0; headerPanel.add(createLabel("DESDE:"), gbc);
        gbc.gridx = 1; headerPanel.add(create3DCalendarWrapper(dateFrom = createLuxuryDatePicker(false)), gbc);

        gbc.gridx = 2; headerPanel.add(createLabel("HASTA:"), gbc);
        gbc.gridx = 3; headerPanel.add(create3DCalendarWrapper(dateTo = createLuxuryDatePicker(true)), gbc);

        // MONEDA
        gbc.gridx = 4;
        btnCurrencyToggle = new SoftButton(null);
        btnCurrencyToggle.setPreferredSize(new Dimension(160, 42));
        btnCurrencyToggle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCurrencyToggle.setBackground(new Color(30, 30, 30));
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
        headerPanel.add(btnCurrencyToggle, gbc);

        gbc.gridx = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lblStatus = new JLabel(" ", SwingConstants.RIGHT);
        headerPanel.add(lblStatus, gbc);

        // BOTONES
        gbc.gridx = 6; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        SoftButton btnGenerate = new SoftButton(null);
        btnGenerate.setText("GENERAR");
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGenerate.setPreferredSize(new Dimension(130, 42));
        btnGenerate.addActionListener(e -> generateReport());
        actionPanel.add(btnGenerate);

        SoftButton btnExit = new SoftButton(null);
        btnExit.setText("CERRAR");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExit.setPreferredSize(new Dimension(110, 42));
        btnExit.setBackground(new Color(120, 20, 20));
        btnExit.addActionListener(e -> dispose());
        actionPanel.add(btnExit);

        headerPanel.add(actionPanel, gbc);
        add(headerPanel, BorderLayout.NORTH);
    }

    private JPanel create3DCalendarWrapper(DatePicker dp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(10, 10, 10));
        p.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(60,60,60), new Color(20,20,20)));
        p.add(dp, BorderLayout.CENTER);
        return p;
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        // SIDEBAR
        kpiContainer = new JPanel();
        kpiContainer.setLayout(new BoxLayout(kpiContainer, BoxLayout.Y_AXIS));
        kpiContainer.setOpaque(true);
        kpiContainer.setBackground(SIDEBAR_BG);
        kpiContainer.setBorder(new LineBorder(new Color(40, 40, 40), 1));
        kpiContainer.setPreferredSize(new Dimension(260, 0));

        contentPanel.add(kpiContainer, BorderLayout.WEST);

        // CENTRO
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // BARRAS
        barPanel = new ChartPanel(null);
        barPanel.setOpaque(false);
        barPanel.setPreferredSize(new Dimension(0, 300));
        centerPanel.add(createChartWrapper(barPanel, true), BorderLayout.NORTH);

        // DONUT + TABLA
        JPanel bottomSplit = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomSplit.setOpaque(false);

        donutPanel = new ChartPanel(null);
        donutPanel.setOpaque(false);
        bottomSplit.add(createChartWrapper(donutPanel, true));

        bottomSplit.add(createProfitabilityTable());

        centerPanel.add(bottomSplit, BorderLayout.CENTER);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    // --- REEMPLAZO TOTAL DE LA TARJETA KPI (PARA QUE SE VEA EL NÚMERO) ---
    private JPanel createManualKPICard(String title, String value, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.BLACK);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(40, 40, 40), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(220, 100));
        card.setPreferredSize(new Dimension(220, 100));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.LIGHT_GRAY);
        lblTitle.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Número Grande
        lblValue.setForeground(valueColor); // Color Neón pasado por parámetro
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    private JPanel createChartWrapper(ChartPanel chart, boolean darkBackground) {
        JPanel p = new JPanel(new BorderLayout());
        if (darkBackground) {
            p.setBackground(new Color(15, 15, 15));
            p.setOpaque(true);
        } else {
            p.setOpaque(false);
        }
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60,60,60), 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    private JScrollPane createProfitabilityTable() {
        String[] columns = {"PRODUCTO", "UNIDADES", "GANANCIA"};
        profitabilityTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        profitabilityTable = new JTable(profitabilityTableModel);

        profitabilityTable.setRowHeight(40);
        profitabilityTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        profitabilityTable.setForeground(Color.WHITE);
        profitabilityTable.setGridColor(new Color(40, 40, 40));
        profitabilityTable.setShowVerticalLines(false);

        JTableHeader header = profitabilityTable.getTableHeader();
        header.setBackground(new Color(10, 10, 10));
        header.setForeground(LUX_GOLD);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 45));

        DefaultTableCellRenderer luxuryRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(12, 12, 12) : new Color(30, 30, 30));
                }

                if (column == 0 && row < BAR_COLORS.length) {
                    c.setForeground(BAR_COLORS[row]);
                    c.setFont(new Font("Segoe UI", Font.BOLD, 16));
                } else {
                    c.setForeground(Color.WHITE);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                }

                if (column == 1) setHorizontalAlignment(JLabel.CENTER);
                else if (column > 1) {
                    setHorizontalAlignment(JLabel.RIGHT);
                    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                }
                return c;
            }
        };

        for (int i = 0; i < 3; i++) profitabilityTable.getColumnModel().getColumn(i).setCellRenderer(luxuryRenderer);

        profitabilityTable.getColumnModel().getColumn(0).setPreferredWidth(230);
        profitabilityTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        profitabilityTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(profitabilityTable);
        scrollPane.getViewport().setBackground(new Color(12, 12, 12));
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
                    List<Object[]> finalData = prepareUnifiedData(data.profitability);
                    updateHorizontalBarChart(finalData);
                    updateProfitabilityTable(finalData);
                    updateKPIs(data.financials, finalData);
                    updateDonutChart(data.financials);
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

    private List<Object[]> prepareUnifiedData(List<Object[]> realData) {
        List<Object[]> combined = new ArrayList<>(realData);
        // Si hay muy pocos datos (menos de 3), añadimos demos para que no se vea vacío
        if (combined.size() < 3) {
            combined.add(new Object[]{ "Demo: Bikini Neon", 5, 100.0, 30.0, 70.0 });
            combined.add(new Object[]{ "Demo: Enterizo", 3, 80.0, 40.0, 40.0 });
        }
        combined.sort((o1, o2) -> Integer.compare((Integer)o2[1], (Integer)o1[1]));
        return combined;
    }

    private void updateKPIs(Map<String, Double> summary, List<Object[]> tableData) {
        kpiContainer.removeAll();
        kpiContainer.add(Box.createVerticalStrut(25));

        double rate = (CurrencyManager.getMode() == 2) ? CurrencyManager.getTasa() : 1.0;
        String symbol = CurrencyManager.getSymbol();

        double ingresos = summary.getOrDefault("ingresos", 0.0);
        // Sumar demos si existen
        for (Object[] row : tableData) {
            String name = row[0].toString();
            if (name.startsWith("Demo:")) ingresos += (double) row[2];
        }

        double costos = ingresos * 0.70;
        double ganancias = ingresos * 0.30;

        ingresos *= rate;
        costos *= rate;
        ganancias *= rate;

        // AQUÍ ESTABA EL DETALLE: Usamos la nueva función createManualKPICard
        kpiContainer.add(createManualKPICard("INGRESOS", String.format(Locale.US, "%s %,.2f", symbol, ingresos), NEON_GREEN));
        kpiContainer.add(Box.createVerticalStrut(25));
        kpiContainer.add(createManualKPICard("COSTOS", String.format(Locale.US, "%s %,.2f", symbol, costos), NEON_RED));
        kpiContainer.add(Box.createVerticalStrut(25));
        kpiContainer.add(createManualKPICard("GANANCIA", String.format(Locale.US, "%s %,.2f", symbol, ganancias), LUX_GOLD));
        kpiContainer.add(Box.createVerticalGlue());

        kpiContainer.revalidate();
        kpiContainer.repaint();
    }

    private void updateProfitabilityTable(List<Object[]> combinedData) {
        profitabilityTableModel.setRowCount(0);
        double rate = (CurrencyManager.getMode() == 2) ? CurrencyManager.getTasa() : 1.0;
        String symbol = CurrencyManager.getSymbol();

        profitabilityTable.getColumnModel().getColumn(2).setHeaderValue("GANANCIA (" + symbol + ")");
        profitabilityTable.getTableHeader().repaint();

        for (Object[] row : combinedData) {
            double ganancia = (row.length > 4) ? (double)row[4] : 0.0;
            profitabilityTableModel.addRow(new Object[]{
                    row[0].toString(),
                    row[1],
                    String.format(Locale.US, "%,.2f", ganancia * rate)
            });
        }
    }

    private void updateDonutChart(Map<String, Double> financials) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("COBRADO", 60);
        dataset.setValue("POR COBRAR", 30);
        dataset.setValue("EN PRODUCCIÓN", 10);

        JFreeChart chart = ChartFactory.createRingChart(
                "ESTATUS DE PEDIDOS", dataset, true, true, false);

        chart.setBackgroundPaint(new Color(0,0,0,0));
        TextTitle title = chart.getTitle();
        title.setPaint(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        LegendTitle legend = chart.getLegend();
        legend.setBackgroundPaint(new Color(15,15,15));
        legend.setItemPaint(Color.WHITE);
        legend.setFrame(BlockBorder.NONE);
        legend.setPosition(RectangleEdge.BOTTOM);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(0,0,0,0));
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(null);
        plot.setSectionDepth(0.35);

        plot.setSectionPaint("COBRADO", NEON_GREEN);
        plot.setSectionPaint("POR COBRAR", NEON_RED);
        plot.setSectionPaint("EN PRODUCCIÓN", NEON_BLUE);
        plot.setSectionOutlinesVisible(false);
        plot.setShadowPaint(null);

        donutPanel.setChart(chart);
    }

    private void updateHorizontalBarChart(List<Object[]> combinedData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int limit = Math.min(combinedData.size(), 5);
        for(int i=0; i<limit; i++) {
            Object[] row = combinedData.get(i);
            dataset.addValue((Integer)row[1], "Ventas", (String)row[0]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "TOP PRODUCTOS (UNIDADES)", "", "", dataset,
                PlotOrientation.HORIZONTAL, false, true, false);

        chart.setBackgroundPaint(new Color(0,0,0,0));
        TextTitle title = chart.getTitle();
        title.setPaint(LUX_GOLD);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(30, 30, 30, 150));
        plot.setOutlineVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelPaint(Color.GRAY);
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVisible(false);

        MultiColorRenderer renderer = new MultiColorRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{1}", NumberFormat.getIntegerInstance()));
        renderer.setDefaultItemLabelsVisible(true);
        // AJUSTE: Letra un poco más pequeña para que quepa en barras cortas
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        renderer.setDefaultItemLabelPaint(new Color(20, 20, 20));
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));

        plot.setRenderer(renderer);
        barPanel.setChart(chart);
    }

    class MultiColorRenderer extends BarRenderer {
        @Override
        public Paint getItemPaint(int row, int column) {
            return BAR_COLORS[column % BAR_COLORS.length];
        }
    }

    private DatePicker createLuxuryDatePicker(boolean initialToday) {
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("dd-MM-yyyy");

        settings.setColor(DatePickerSettings.DateArea.TextFieldBackgroundValidDate, new Color(20, 20, 20));
        settings.setColor(DatePickerSettings.DateArea.DatePickerTextValidDate, Color.WHITE);

        Color bgDark = new Color(30, 30, 30);
        settings.setColor(DatePickerSettings.DateArea.CalendarBackgroundNormalDates, bgDark);
        settings.setColor(DatePickerSettings.DateArea.BackgroundMonthAndYearMenuLabels, bgDark);
        settings.setColor(DatePickerSettings.DateArea.BackgroundTodayLabel, bgDark);
        settings.setColor(DatePickerSettings.DateArea.CalendarTextNormalDates, Color.WHITE);
        settings.setColor(DatePickerSettings.DateArea.CalendarTextWeekdays, LUX_GOLD);

        settings.setAllowKeyboardEditing(false);

        DatePicker dp = new DatePicker(settings);
        dp.setPreferredSize(new Dimension(140, 35));
        dp.getComponentDateTextField().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dp.getComponentDateTextField().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if (initialToday) dp.setDateToToday();
        else dp.setDate(LocalDate.now().minusMonths(1));

        JButton btn = dp.getComponentToggleCalendarButton();
        btn.setText("📅");
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        try { btn.setIcon(null); } catch(Exception e){}

        btn.setBackground(LUX_GOLD);
        btn.setBorder(BorderFactory.createEmptyBorder());
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.LIGHT_GRAY);
        return label;
    }
}