/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ReportsView.java
 * VERSIÓN: FINAL OPTIMIZADA (Tabla Limpia + Branding SICONI)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePickerSettings.DateArea;
import com.swimcore.dao.SaleDAO;
import com.swimcore.model.Sale;
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.LuxuryMessage;
import com.swimcore.util.ReportPDF;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
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
import javax.swing.border.MatteBorder; // Importante para la línea dorada
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

public class ReportsView extends JDialog {

    private final SaleDAO saleDAO = new SaleDAO();
    private DatePicker dateFrom, dateTo;
    private SoftButton btnCurrencyToggle;

    private JPanel kpiContainer;
    private ChartPanel donutPanel;
    private ChartPanel barPanel;
    private JTable operationalTable;
    private DefaultTableModel operationalTableModel;
    private JLabel lblStatus;

    // DATOS EN MEMORIA
    private double currentIngresos = 0;
    private double currentDeuda = 0;
    private int currentPendientesTotal = 0;
    private int currentEnTaller = 0;

    // COLORES DE MARCA
    private final Color SIDEBAR_BG = new Color(15, 15, 15);
    private final Color DARK_BG = new Color(18, 18, 18);

    // NEÓN DE ALTA INTENSIDAD
    private final Color INTENSE_GREEN = new Color(0, 255, 0);
    private final Color INTENSE_RED = new Color(255, 0, 0);
    private final Color INTENSE_GOLD = new Color(255, 215, 0);
    private final Color INTENSE_BLUE = new Color(0, 220, 255);

    private final Color[] BAR_COLORS = {
            new Color(255, 215, 0),   // Oro
            new Color(0, 255, 255),   // Cian
            new Color(255, 0, 255),   // Magenta
            new Color(57, 255, 20),   // Verde
            new Color(255, 100, 0)    // Naranja
    };

    public ReportsView(JFrame parent) {
        super(parent, "SICONI - Reportes Gerenciales", true);
        setSize(1100, 680);
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
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagen != null) g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
            else { g.setColor(DARK_BG); g.fillRect(0, 0, getWidth(), getHeight()); }
        }
    }

    private void initHeader() {
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        // 1. BARRA DE MARCA CON "SICONI"
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        brandPanel.setBackground(new Color(10, 10, 10, 200));
        brandPanel.setBorder(new MatteBorder(0, 0, 1, 0, INTENSE_GOLD));

        // AGREGADO: "SICONI | " AL PRINCIPIO
        JLabel lblBrand = new JLabel("SICONI  |  DAYANA GUÉDEZ SWIMWEAR - DASHBOARD DE PRODUCCIÓN");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBrand.setForeground(INTENSE_GOLD);
        brandPanel.add(lblBrand);
        topContainer.add(brandPanel, BorderLayout.NORTH);

        // 2. CONTROLES
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setOpaque(false);
        controlsPanel.setBorder(new EmptyBorder(10, 20, 5, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 0, 8); gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; controlsPanel.add(createLabel("DESDE:"), gbc);
        gbc.gridx = 1; controlsPanel.add(create3DCalendarWrapper(dateFrom = createLuxuryDatePicker(false)), gbc);
        gbc.gridx = 2; controlsPanel.add(createLabel("HASTA:"), gbc);
        gbc.gridx = 3; controlsPanel.add(create3DCalendarWrapper(dateTo = createLuxuryDatePicker(true)), gbc);

        gbc.gridx = 4;
        btnCurrencyToggle = new SoftButton(null);
        btnCurrencyToggle.setPreferredSize(new Dimension(140, 38));
        btnCurrencyToggle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCurrencyToggle.setBackground(new Color(25, 25, 25));
        btnCurrencyToggle.setForeground(INTENSE_GOLD);
        btnCurrencyToggle.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(60,60,60), new Color(10,10,10)));
        updateCurrencyButtonText();
        btnCurrencyToggle.addActionListener(e -> {
            int nextMode = (CurrencyManager.getMode() + 1) % 3;
            CurrencyManager.setConfig(CurrencyManager.getTasa(), CurrencyManager.getSymbol(), nextMode);
            updateCurrencyButtonText();
            generateReport();
        });
        controlsPanel.add(btnCurrencyToggle, gbc);

        gbc.gridx = 5; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        lblStatus = new JLabel(" ", SwingConstants.RIGHT);
        lblStatus.setForeground(INTENSE_GOLD);
        controlsPanel.add(lblStatus, gbc);

        // BOTONES
        gbc.gridx = 6; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); actionPanel.setOpaque(false);

        SoftButton btnGenerate = new SoftButton(null);
        btnGenerate.setText("ACTUALIZAR");
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGenerate.setPreferredSize(new Dimension(110, 38));
        btnGenerate.addActionListener(e -> generateReport());
        actionPanel.add(btnGenerate);

        SoftButton btnPrint = new SoftButton(null);
        btnPrint.setText("PDF");
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPrint.setPreferredSize(new Dimension(80, 38));
        btnPrint.setBackground(new Color(0, 100, 200));
        btnPrint.addActionListener(e -> imprimirReportePDF());
        actionPanel.add(btnPrint);

        SoftButton btnExit = new SoftButton(null);
        btnExit.setText("SALIR");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnExit.setPreferredSize(new Dimension(80, 38));
        btnExit.setBackground(new Color(150, 20, 20));
        btnExit.addActionListener(e -> dispose());
        actionPanel.add(btnExit);

        controlsPanel.add(actionPanel, gbc);
        topContainer.add(controlsPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);
    }

    private void imprimirReportePDF() {
        SoundManager.getInstance().playClick();
        String rango = dateFrom.getText() + " al " + dateTo.getText();
        try {
            ReportPDF.generateReport(rango, currentIngresos, currentDeuda, currentPendientesTotal, currentEnTaller);
            LuxuryMessage.show("Éxito", "Reporte PDF generado correctamente.", false);
        } catch (Exception e) {
            LuxuryMessage.show("Error", "No se pudo generar el PDF.", true);
        }
    }

    private JPanel create3DCalendarWrapper(DatePicker dp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(10, 10, 10));
        p.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(50,50,50), new Color(10,10,10)));
        p.add(dp, BorderLayout.CENTER);
        return p;
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false); contentPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        kpiContainer = new JPanel();
        kpiContainer.setLayout(new BoxLayout(kpiContainer, BoxLayout.Y_AXIS));
        kpiContainer.setOpaque(true);
        kpiContainer.setBackground(SIDEBAR_BG);
        kpiContainer.setBorder(new EmptyBorder(0,0,0,0));
        kpiContainer.setPreferredSize(new Dimension(280, 0));
        contentPanel.add(kpiContainer, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        barPanel = new ChartPanel(null);
        barPanel.setOpaque(false);
        barPanel.setPreferredSize(new Dimension(0, 280));
        centerPanel.add(createChartWrapper(barPanel, true), BorderLayout.NORTH);

        JPanel bottomSplit = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomSplit.setOpaque(false);

        donutPanel = new ChartPanel(null);
        donutPanel.setOpaque(false);
        JPanel donutWrapper = createChartWrapper(donutPanel, true);
        donutWrapper.setBackground(Color.BLACK);
        bottomSplit.add(donutWrapper);

        bottomSplit.add(createOperationalTable());
        centerPanel.add(bottomSplit, BorderLayout.CENTER);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JScrollPane createOperationalTable() {
        // CABECERA SIMPLIFICADA "CANT"
        String[] columns = {"CONCEPTO OPERATIVO", "CANT", "TOTAL ESTIMADO"};
        operationalTableModel = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        operationalTable = new JTable(operationalTableModel);

        operationalTable.setRowHeight(45);
        operationalTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        operationalTable.setForeground(Color.WHITE);
        operationalTable.setBackground(new Color(5, 5, 5));
        operationalTable.setGridColor(new Color(30, 30, 30));
        operationalTable.setShowVerticalLines(false);
        operationalTable.setShowHorizontalLines(true);

        // --- AJUSTE DE ANCHOS PARA QUE QUEPA TODO ---
        // Redujimos la columna 1 (Cantidad) para darle espacio a la 2 (Total)
        operationalTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Concepto
        operationalTable.getColumnModel().getColumn(1).setPreferredWidth(60);  // Cant (Antes 80)
        operationalTable.getColumnModel().getColumn(2).setPreferredWidth(160); // Total (Antes 120, ahora cabe sobrado)

        JTableHeader header = operationalTable.getTableHeader();
        header.setBackground(new Color(20, 20, 20));
        header.setForeground(INTENSE_GOLD);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(new Color(5, 5, 5));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rightRenderer.setBackground(new Color(5, 5, 5));
        rightRenderer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        operationalTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(new Color(5, 5, 5));
                if (row == 0) c.setForeground(INTENSE_RED);
                else if (row == 1) c.setForeground(INTENSE_BLUE);
                else c.setForeground(INTENSE_GREEN);
                c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                return c;
            }
        });

        operationalTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        operationalTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(operationalTable);
        scrollPane.getViewport().setBackground(new Color(5, 5, 5));
        scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(40,40,40), new Color(10,10,10)));
        return scrollPane;
    }

    private JPanel createSunkenKPICard(String title, String value, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(10, 10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(40,40,40), new Color(0,0,0)),
                new EmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(240, 100));
        card.setPreferredSize(new Dimension(240, 100));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(valueColor);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChartWrapper(ChartPanel chart, boolean darkBackground) {
        JPanel p = new JPanel(new BorderLayout());
        if (darkBackground) {
            p.setBackground(new Color(10, 10, 10));
            p.setOpaque(true);
        } else {
            p.setOpaque(false);
        }
        p.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(50,50,50), new Color(0,0,0)));
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    private void generateReport() {
        if (dateFrom.getDate() == null || dateTo.getDate() == null) return;
        SoundManager.getInstance().playClick();

        Date startDate = Date.from(dateFrom.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(dateTo.getDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<ReportData, Void>() {
            @Override
            protected ReportData doInBackground() throws Exception {
                ReportData data = new ReportData();
                data.profitability = saleDAO.getProductProfitability(startDate, endDate);
                List<Sale> allSales = saleDAO.getAllSales();

                data.stats = new OperationalStats();
                currentIngresos = 0; currentDeuda = 0; currentPendientesTotal = 0; currentEnTaller = 0;

                for (Sale s : allSales) {
                    try {
                        double total = s.getTotalAmountUSD();
                        double paid = s.getAmountPaid();
                        double deudaReal = total - paid;
                        if (deudaReal < 0.1) deudaReal = 0;

                        if (deudaReal > 0) {
                            data.stats.amountPending += deudaReal;
                            if ("EN PROCESO".equalsIgnoreCase(s.getStatus()) || paid > 0) {
                                data.stats.countInProcess++;
                            }
                            data.stats.countPending++;
                        }
                        data.stats.amountPaid += paid;
                        if (paid >= total && total > 0) {
                            data.stats.countPaid++;
                        }
                        data.stats.totalSales++;
                    } catch (Exception e) {}
                }
                currentIngresos = data.stats.amountPaid;
                currentDeuda = data.stats.amountPending;
                currentPendientesTotal = data.stats.countPending;
                currentEnTaller = data.stats.countInProcess;
                return data;
            }
            @Override
            protected void done() {
                try {
                    ReportData data = get();
                    List<Object[]> unifiedData = prepareUnifiedData(data.profitability);
                    updateHorizontalBarChart(unifiedData);
                    updateKPIs(unifiedData);
                    updateDonutChart(data.stats);
                    updateOperationalTable(data.stats);
                    lblStatus.setText("Datos al " + LocalDate.now());
                } catch (Exception e) {
                    e.printStackTrace();
                    LuxuryMessage.show("Error", "Error procesando reporte.", true);
                }
                finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private static class OperationalStats {
        int totalSales=0; int countPending=0; int countInProcess=0; int countPaid=0; double amountPending=0; double amountPaid=0;
    }

    private static class ReportData {
        List<Object[]> profitability;
        OperationalStats stats;
    }

    private List<Object[]> prepareUnifiedData(List<Object[]> realData) {
        realData.sort((o1, o2) -> Integer.compare((Integer)o2[1], (Integer)o1[1]));
        return realData;
    }

    private void updateKPIs(List<Object[]> tableData) {
        kpiContainer.removeAll();
        kpiContainer.add(Box.createVerticalStrut(20));

        int mode = CurrencyManager.getMode();
        String[] safeSymbols = {"$", "€", "Bs."};
        String symbol = safeSymbols[mode];
        double rate = (mode == 2) ? CurrencyManager.getTasa() : 1.0;

        double sumIngresos = currentIngresos * rate;
        double sumDeuda = currentDeuda * rate;
        double sumGanancias = sumIngresos * 0.40;

        kpiContainer.add(createSunkenKPICard("INGRESOS (COBRADO)", String.format(Locale.US, "%s %,.2f", symbol, sumIngresos), INTENSE_GREEN));
        kpiContainer.add(Box.createVerticalStrut(20));
        kpiContainer.add(createSunkenKPICard("POR COBRAR (DEUDA)", String.format(Locale.US, "%s %,.2f", symbol, sumDeuda), INTENSE_RED));
        kpiContainer.add(Box.createVerticalStrut(20));
        kpiContainer.add(createSunkenKPICard("UTILIDAD NETA", String.format(Locale.US, "%s %,.2f", symbol, sumGanancias), INTENSE_GOLD));
        kpiContainer.add(Box.createVerticalGlue());
        kpiContainer.revalidate();
        kpiContainer.repaint();
    }

    private void updateOperationalTable(OperationalStats stats) {
        operationalTableModel.setRowCount(0);
        int mode = CurrencyManager.getMode();
        double rate = (mode == 2) ? CurrencyManager.getTasa() : 1.0;

        // AQUÍ ELIMINAMOS "Pedidos" y "Unidades" PARA AHORRAR ESPACIO
        operationalTableModel.addRow(new Object[]{"POR COBRAR (DEUDA)", stats.countPending, String.format(Locale.US, "%,.2f", stats.amountPending * rate)});
        operationalTableModel.addRow(new Object[]{"EN TALLER (PRODUCCIÓN)", stats.countInProcess, "---"});
        operationalTableModel.addRow(new Object[]{"COBRADO Y ENTREGADO", stats.countPaid, String.format(Locale.US, "%,.2f", stats.amountPaid * rate)});
    }

    private void updateDonutChart(OperationalStats stats) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        int pendientesPuros = stats.countPending - stats.countInProcess;
        if(pendientesPuros < 0) pendientesPuros = 0;

        dataset.setValue("COMPLETADOS", stats.countPaid);
        dataset.setValue("EN TALLER", stats.countInProcess);
        dataset.setValue("PENDIENTES", pendientesPuros);

        JFreeChart chart = ChartFactory.createRingChart("EFICIENCIA DE ENTREGAS", dataset, true, true, false);
        chart.setBackgroundPaint(Color.BLACK);
        chart.getTitle().setPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 15));
        chart.getLegend().setVisible(true);
        chart.getLegend().setBackgroundPaint(Color.BLACK);
        chart.getLegend().setItemPaint(Color.LIGHT_GRAY);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setPosition(RectangleEdge.BOTTOM);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.BLACK);
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(null);
        plot.setSectionDepth(0.35);
        plot.setSeparatorsVisible(true);
        plot.setSeparatorPaint(Color.BLACK);
        plot.setSectionPaint("COMPLETADOS", INTENSE_GREEN);
        plot.setSectionPaint("EN TALLER", INTENSE_BLUE);
        plot.setSectionPaint("PENDIENTES", INTENSE_RED);
        plot.setShadowPaint(null);
        donutPanel.setChart(chart);
    }

    private void updateHorizontalBarChart(List<Object[]> combinedData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int limit = Math.min(combinedData.size(), 5);
        for(int i=0; i<limit; i++) dataset.addValue((Integer)combinedData.get(i)[1], "Ventas", combinedData.get(i)[0].toString());

        JFreeChart chart = ChartFactory.createBarChart("TOP PRODUCTOS", "", "", dataset, PlotOrientation.HORIZONTAL, false, true, false);
        chart.setBackgroundPaint(new Color(0,0,0,0));
        chart.getTitle().setPaint(INTENSE_GOLD);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(30, 30, 30, 150));
        plot.setOutlineVisible(false);
        plot.getDomainAxis().setVisible(false);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setVisible(false);

        BarRenderer renderer = new BarRenderer() {
            @Override public Paint getItemPaint(int row, int col) { return BAR_COLORS[col % BAR_COLORS.length]; }
        };
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{1} - {0}", NumberFormat.getIntegerInstance()));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 15));
        renderer.setDefaultItemLabelPaint(Color.BLACK);
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));

        plot.setRenderer(renderer);
        barPanel.setChart(chart);
    }

    private DatePicker createLuxuryDatePicker(boolean initialToday) {
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("dd-MM-yyyy");
        settings.setColor(DateArea.TextFieldBackgroundValidDate, new Color(20, 20, 20));
        settings.setColor(DateArea.DatePickerTextValidDate, Color.WHITE);
        Font fontBold = new Font("Segoe UI", Font.BOLD, 14);
        settings.setFontValidDate(fontBold);
        settings.setFontInvalidDate(fontBold);
        settings.setFontVetoedDate(fontBold);
        Color bgDark = new Color(30, 30, 30);
        settings.setColor(DateArea.CalendarBackgroundNormalDates, bgDark);
        settings.setColor(DateArea.BackgroundMonthAndYearMenuLabels, bgDark);
        settings.setColor(DateArea.BackgroundTodayLabel, bgDark);
        settings.setColor(DateArea.CalendarTextNormalDates, Color.WHITE);
        settings.setColor(DateArea.CalendarTextWeekdays, INTENSE_GOLD);
        settings.setAllowKeyboardEditing(false);
        DatePicker dp = new DatePicker(settings);
        dp.setPreferredSize(new Dimension(140, 35));
        dp.getComponentDateTextField().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dp.getComponentDateTextField().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        if (initialToday) dp.setDateToToday(); else dp.setDate(LocalDate.now().minusMonths(2));
        JButton btn = dp.getComponentToggleCalendarButton();
        btn.setText("📅"); btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btn.setBackground(INTENSE_GOLD);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setPreferredSize(new Dimension(35, 35));
        btn.setFocusPainted(false);
        return dp;
    }

    private void updateCurrencyButtonText() {
        int m = CurrencyManager.getMode();
        btnCurrencyToggle.setText(m==0?"MONEDA: $":m==1?"MONEDA: €":"MONEDA: Bs.");
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(Color.LIGHT_GRAY); return l;
    }
}