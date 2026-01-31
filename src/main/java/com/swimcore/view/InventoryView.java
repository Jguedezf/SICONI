/*
INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
ARCHIVO: InventoryView.java
VERSIÓN: 12.7.0 (Final - Audit Text Area Fix)
FECHA: Enero 2026
*/
package com.swimcore.view;

import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Product;
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.view.components.InventorySidePanel;
import com.swimcore.view.components.SoftButton;
import com.swimcore.view.dialogs.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Locale;

public class InventoryView extends JDialog {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private final ProductDAO productDAO = new ProductDAO();

    // Paleta de Colores Corporativa (SICONI High Contrast)
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_TEXTO = new Color(229, 228, 226);
    private final Color COLOR_VERDE_NEON = new Color(0, 255, 128);
    private final Color COLOR_ROJO_ALERTA = new Color(255, 60, 60);
    private final Color COLOR_FUCSIA_NEON = new Color(255, 0, 127);
    private final Color COLOR_INPUT_BG = new Color(30, 30, 30);
    private final Color COLOR_BG_DIALOG = new Color(18, 18, 18);
    private final Color COLOR_LABEL = new Color(180, 180, 180);

    private SoftButton btnTasa;
    private JTextField txtSearch;
    private boolean filterLowStock = false;

    public InventoryView(JFrame parent) {
        this(parent, false);
    }

    public InventoryView(JFrame parent, boolean showOnlyLowStock) {
        super(parent, LanguageManager.get("inventory.window_title"), true);
        this.filterLowStock = showOnlyLowStock;

        setSize(1280, 720);
        setLocationRelativeTo(parent);

        try {
            setContentPane(new ImagePanel("/images/bg2.png"));
        } catch (Exception e) {
            getContentPane().setBackground(new Color(15, 15, 15));
        }
        setLayout(new BorderLayout());

        initSidePanel();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        initHeader(mainPanel);
        initCenterSection(mainPanel);

        add(mainPanel, BorderLayout.CENTER);
        loadProducts("");

        if (showOnlyLowStock) {
            SwingUtilities.invokeLater(() -> {
                if (productTable.getRowCount() > 0) {
                    productTable.setRowSelectionInterval(0, 0);
                    productTable.scrollRectToVisible(productTable.getCellRect(0, 0, true));
                    productTable.requestFocus();
                }
            });
        }
    }

    private void initSidePanel() {
        InventorySidePanel sidePanel = new InventorySidePanel();
        sidePanel.addButton("/images/icons/icon_add.png", LanguageManager.get("inventory.btn.add"), e -> {
            new AddEditProductDialog(this, null).setVisible(true);
            loadProducts("");
        });
        sidePanel.addButton("/images/icons/icon_edit.png", LanguageManager.get("inventory.btn.edit"), e -> editSelected());
        sidePanel.addButton("/images/icons/icon_delete.png", LanguageManager.get("inventory.btn.delete"), e -> deleteSelected());
        sidePanel.add(Box.createVerticalStrut(30));
        sidePanel.addButton("/images/icons/icon_stock.png", LanguageManager.get("inventory.btn.stock"), e -> abrirGestionExistencias());
        sidePanel.addButton("/images/icons/icon_audit.png", LanguageManager.get("inventory.btn.audit"), e -> new InventoryHistoryDialog(this).setVisible(true));
        sidePanel.addButton("/images/icons/icon_users.png", LanguageManager.get("inventory.btn.suppliers"), e -> new SupplierManagementDialog(this).setVisible(true));
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.addButton("/images/icons/icon_exit.png", LanguageManager.get("inventory.btn.close"), e -> dispose());
        add(sidePanel, BorderLayout.WEST);
    }

    private void initHeader(JPanel parentPanel) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 40, 10, 40));

        JLabel lblTitle = new JLabel(filterLowStock ? "⚠️ CONTROL DE STOCK CRÍTICO" : LanguageManager.get("inventory.title"));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(filterLowStock ? COLOR_ROJO_ALERTA : COLOR_GOLD);

        JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        ratePanel.setOpaque(false);

        btnTasa = new SoftButton(null);
        btnTasa.setText(String.format(Locale.US, LanguageManager.get("inventory.rate_btn"), CurrencyManager.getTasa()));
        btnTasa.setPreferredSize(new Dimension(180, 35));
        btnTasa.setForeground(COLOR_GOLD);
        btnTasa.addActionListener(e -> {
            new CurrencySettingsDialog((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true);
            loadProducts("");
        });

        ratePanel.add(btnTasa);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(ratePanel, BorderLayout.EAST);
        parentPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void initCenterSection(JPanel parentPanel) {
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(10, 40, 30, 40));

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchBarPanel.setOpaque(false);
        searchBarPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        txtSearch = new JTextField(30);
        txtSearch.setPreferredSize(new Dimension(380, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setBackground(COLOR_INPUT_BG);
        txtSearch.setForeground(Color.WHITE);
        txtSearch.setCaretColor(COLOR_GOLD);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(80, 80, 80), 1),
                new EmptyBorder(0, 10, 0, 10)
        ));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadProducts(txtSearch.getText()); }
        });

        SoftButton btnSearch = new SoftButton(null);
        btnSearch.setText("🔍");
        btnSearch.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        btnSearch.setPreferredSize(new Dimension(55, 40));
        btnSearch.addActionListener(e -> loadProducts(txtSearch.getText()));

        searchBarPanel.add(txtSearch);
        searchBarPanel.add(btnSearch);
        centerContainer.add(searchBarPanel, BorderLayout.NORTH);

        String[] cols = { "ID", "CÓDIGO", "PRODUCTO", "CATEGORÍA", "STOCK", "PRECIO", "PROVEEDOR", "min_stock" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return (c == 0 || c == 4 || c == 7) ? Integer.class : String.class; }
        };

        productTable = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(new LineBorder(COLOR_GOLD, 1));
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        productTable.setPreferredScrollableViewportSize(new Dimension(productTable.getPreferredSize().width, 495));

        centerContainer.add(scrollPane, BorderLayout.CENTER);

        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = productTable.columnAtPoint(e.getPoint());
                int row = productTable.rowAtPoint(e.getPoint());
                if (col == 4 && row != -1) {
                    Rectangle rect = productTable.getCellRect(row, col, false);
                    int xRel = e.getX() - rect.x;
                    int mid = rect.width / 2;
                    int id = (int) productTable.getValueAt(row, 0);
                    int actualValue = (int) productTable.getValueAt(row, 4);
                    if (xRel < (mid - 20)) {
                        if (actualValue > 0) { actualValue--; updateStockQuickly(id, -1, row, actualValue); }
                    } else if (xRel > (mid + 20)) {
                        actualValue++; updateStockQuickly(id, 1, row, actualValue);
                    }
                }
            }
        });

        parentPanel.add(centerContainer, BorderLayout.CENTER);
    }

    private void styleTable() {
        productTable.setRowHeight(45);
        productTable.setBackground(new Color(30, 30, 30));
        productTable.setForeground(COLOR_TEXTO);
        productTable.setSelectionBackground(new Color(55, 55, 55));
        productTable.setSelectionForeground(COLOR_GOLD);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.setFocusable(false);
        productTable.setShowVerticalLines(false);
        productTable.setIntercellSpacing(new Dimension(0, 0));
        productTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = productTable.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setBackground(new Color(15, 15, 15));
        header.setForeground(COLOR_GOLD);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        TableColumnModel cm = productTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);
        cm.getColumn(1).setPreferredWidth(100);
        cm.getColumn(2).setPreferredWidth(300);
        cm.getColumn(3).setPreferredWidth(130);
        cm.getColumn(4).setPreferredWidth(160);
        cm.getColumn(5).setPreferredWidth(220);
        cm.getColumn(6).setPreferredWidth(130);
        cm.getColumn(7).setMinWidth(0); cm.getColumn(7).setMaxWidth(0);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            if (i != 2 && i != 4) cm.getColumn(i).setCellRenderer(centerRenderer);
        }
        cm.getColumn(4).setCellRenderer(new PersistentStockRenderer());
    }

    private void loadProducts(String query) {
        tableModel.setRowCount(0);
        String symbol = CurrencyManager.getSymbol();
        productTable.getColumnModel().getColumn(5).setHeaderValue("PRECIO (" + symbol + ")");
        productTable.getTableHeader().repaint();
        if(btnTasa != null) btnTasa.setText(String.format(Locale.US, LanguageManager.get("inventory.rate_btn"), CurrencyManager.getTasa()));

        List<Product> list;
        if (filterLowStock) list = productDAO.getLowStockProducts();
        else if (query != null && !query.isEmpty()) list = productDAO.searchProducts(query);
        else list = productDAO.getAllProducts();

        for (Product p : list) {
            double price = p.getSalePrice();
            int mode = CurrencyManager.getMode();
            double displayPrice = (mode == 2) ? price * CurrencyManager.getTasa() :
                    (mode == 1) ? CurrencyManager.convert(price) : price;
            String cleanPrice = String.format("%,.2f", displayPrice);

            tableModel.addRow(new Object[]{
                    p.getId(), p.getCode(), p.getName(), "General",
                    p.getCurrentStock(), cleanPrice, "S/P", p.getMinStock()
            });
        }
    }

    private void updateStockQuickly(int id, int delta, int row, int newVal) {
        tableModel.setValueAt(newVal, row, 4);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() { productDAO.updateStockDelta(id, delta); return null; }
        }.execute();
    }

    private void editSelected() {
        int r = productTable.getSelectedRow();
        if (r != -1) { new AddEditProductDialog(this, productDAO.getProductById((int) productTable.getValueAt(r, 0))).setVisible(true); loadProducts(""); }
    }

    private void deleteSelected() {
        int r = productTable.getSelectedRow();
        if (r != -1 && JOptionPane.showConfirmDialog(this, LanguageManager.get("inventory.msg.confirm_delete")) == JOptionPane.YES_OPTION) {
            productDAO.delete((int) productTable.getValueAt(r, 0)); loadProducts("");
        }
    }

    private void abrirGestionExistencias() {
        int r = productTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.select"), "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        new AuditStockDialog(this, (int) productTable.getValueAt(r,0), (String) productTable.getValueAt(r,2), (int) productTable.getValueAt(r,4), (int) productTable.getValueAt(r,7)).setVisible(true);
        loadProducts("");
    }

    class PersistentStockRenderer extends JPanel implements TableCellRenderer {
        private final JLabel btnM = new JLabel("−", SwingConstants.CENTER);
        private final JLabel btnP = new JLabel("+", SwingConstants.CENTER);
        private final JLabel val = new JLabel("", SwingConstants.CENTER);
        public PersistentStockRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5)); setOpaque(true);
            btnM.setFont(new Font("Arial", Font.BOLD, 22)); btnM.setForeground(COLOR_FUCSIA_NEON);
            btnP.setFont(new Font("Arial", Font.BOLD, 22)); btnP.setForeground(COLOR_VERDE_NEON);
            val.setFont(new Font("Segoe UI", Font.BOLD, 16)); val.setForeground(Color.WHITE);
            val.setPreferredSize(new Dimension(45, 30)); add(btnM); add(val); add(btnP);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
            val.setText(String.valueOf(v));
            int min = (int) tableModel.getValueAt(r, 7);
            val.setForeground((int)v <= min ? COLOR_ROJO_ALERTA : Color.WHITE);
            setBackground(isSel ? new Color(50, 50, 50) : new Color(30, 30, 30));
            return this;
        }
    }

    // --- DIÁLOGO DE AUDITORÍA: PROFESSIONAL GLASS EDITION ---
    class AuditStockDialog extends JDialog {
        private boolean esEntrada = true;
        private final JLabel lblResultado;
        private final JTextField txtCantidad;
        private final JTextField txtMinAlert;
        private final JTextArea txtObs;
        private final StyledToggleButton btnIn, btnOut;

        public AuditStockDialog(JDialog parent, int id, String nombre, int stockActual, int minStockActual) {
            super(parent, LanguageManager.get("audit.dialog.title"), true);
            // AJUSTE DE TAMAÑO: 700px de alto para que el textArea respire
            setSize(1050, 700);
            setLocationRelativeTo(parent);
            setUndecorated(true);

            // 1. FONDO PRINCIPAL
            try {
                JPanel bgPanel = new ImagePanel("/images/bg_audit.png");
                bgPanel.setLayout(new BorderLayout());
                bgPanel.setBorder(new LineBorder(COLOR_GOLD, 2));
                setContentPane(bgPanel);
            } catch (Exception e) {
                JPanel solidBg = new JPanel(new BorderLayout());
                solidBg.setBackground(COLOR_BG_DIALOG);
                solidBg.setBorder(new LineBorder(COLOR_GOLD, 2));
                setContentPane(solidBg);
            }

            // 2. PANEL DE CRISTAL (CONTENEDOR CENTRAL SEMI-TRANSPARENTE)
            JPanel glassPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Negro al 75% de opacidad para mejorar lectura
                    g2.setColor(new Color(0, 0, 0, 190));
                    g2.fillRoundRect(30, 30, getWidth()-60, getHeight()-60, 40, 40);
                    g2.dispose();
                }
            };
            glassPanel.setOpaque(false);
            glassPanel.setBorder(new EmptyBorder(50, 60, 50, 60));

            // --- HEADER (Dentro del Cristal) ---
            JPanel headerContainer = new JPanel(new BorderLayout());
            headerContainer.setOpaque(false);

            JLabel lblTitle = new JLabel("GESTIÓN DE EXISTENCIAS", SwingConstants.CENTER);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
            lblTitle.setForeground(COLOR_GOLD);
            headerContainer.add(lblTitle, BorderLayout.NORTH);

            // Info Producto
            JPanel infoBlock = new JPanel(new GridLayout(1, 2, 20, 0));
            infoBlock.setOpaque(false);
            infoBlock.setBorder(new EmptyBorder(25, 0, 20, 0));

            infoBlock.add(crearInfoBox("PRODUCTO SELECCIONADO", nombre.toUpperCase(), Color.WHITE, SwingConstants.LEFT));
            infoBlock.add(crearInfoBox("STOCK EN ALMACÉN", String.valueOf(stockActual), COLOR_GOLD, SwingConstants.RIGHT));

            headerContainer.add(infoBlock, BorderLayout.SOUTH);
            glassPanel.add(headerContainer, BorderLayout.NORTH);

            // --- FORMULARIO CENTRAL ---
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 20, 8, 20); // Un poco menos de espacio vertical
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Fila 1: TIPO DE OPERACIÓN
            gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2;
            formPanel.add(crearLabel("TIPO DE OPERACIÓN"), gbc);

            gbc.gridy = 1;
            JPanel pnlToggle = new JPanel(new GridLayout(1, 2, 40, 0));
            pnlToggle.setOpaque(false);
            pnlToggle.setPreferredSize(new Dimension(0, 80));

            btnIn = new StyledToggleButton("ENTRADA (COMPRA/PRODUCCIÓN)", "⬇", COLOR_VERDE_NEON);
            btnOut = new StyledToggleButton("SALIDA (VENTA/MERMA)", "⬆", COLOR_FUCSIA_NEON);

            btnIn.addActionListener(e -> setOp(true, stockActual));
            btnOut.addActionListener(e -> setOp(false, stockActual));

            pnlToggle.add(btnIn); pnlToggle.add(btnOut);
            formPanel.add(pnlToggle, gbc);

            // Fila 2: Etiquetas Cantidad
            gbc.gridy = 2; gbc.gridwidth = 1; gbc.gridx = 0;
            formPanel.add(crearLabel("UNIDADES A MOVER"), gbc);
            gbc.gridx = 1;
            formPanel.add(crearLabel("STOCK MÍNIMO (ALERTA)"), gbc);

            // Fila 3: CONTROLES
            gbc.gridy = 3; gbc.gridx = 0;
            txtCantidad = crearInput(COLOR_VERDE_NEON);
            formPanel.add(txtCantidad, gbc);

            gbc.gridx = 1;
            txtMinAlert = crearInput(COLOR_ROJO_ALERTA);
            txtMinAlert.setText(String.valueOf(minStockActual));
            formPanel.add(crearControlAlertaUnificado(txtMinAlert), gbc);

            // Fila 4: PROYECCIÓN
            gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
            lblResultado = new JLabel("PROYECCIÓN FINAL: " + stockActual, SwingConstants.CENTER);
            lblResultado.setFont(new Font("Segoe UI", Font.BOLD, 28));
            lblResultado.setForeground(COLOR_VERDE_NEON);
            lblResultado.setBorder(new EmptyBorder(15, 0, 15, 0));
            formPanel.add(lblResultado, gbc);

            // Fila 5: MOTIVO
            gbc.gridy = 5;
            formPanel.add(crearLabel("MOTIVO DE LA OPERACIÓN (OBLIGATORIO):"), gbc);

            gbc.gridy = 6;
            // *** AQUÍ ESTÁ EL AJUSTE IMPORTANTE ***
            txtObs = new JTextArea(5, 20); // 5 Filas visibles
            txtObs.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            txtObs.setBackground(new Color(20, 20, 20));
            txtObs.setForeground(Color.WHITE);
            txtObs.setLineWrap(true); txtObs.setWrapStyleWord(true);
            txtObs.setCaretColor(COLOR_GOLD);
            JScrollPane spObs = new JScrollPane(txtObs);
            spObs.setBorder(new LineBorder(new Color(100,100,100)));
            spObs.setPreferredSize(new Dimension(0, 110)); // 110px de altura (mucho más cómodo)
            formPanel.add(spObs, gbc);

            glassPanel.add(formPanel, BorderLayout.CENTER);
            add(glassPanel, BorderLayout.CENTER);

            // --- SIDEBAR (Fuera del Cristal) ---
            JPanel sidebar = new JPanel(new GridLayout(3, 1, 0, 25));
            sidebar.setOpaque(false);
            sidebar.setBorder(new EmptyBorder(80, 10, 80, 30));
            sidebar.setPreferredSize(new Dimension(240, 0));

            SoftButton btnSave = crearBotonLateral("💾", "GUARDAR CAMBIOS", COLOR_VERDE_NEON);
            btnSave.addActionListener(e -> guardar(id, stockActual));

            SoftButton btnClear = crearBotonLateral("🔄", "RESTAURAR", Color.WHITE);
            btnClear.addActionListener(e -> {
                txtCantidad.setText(""); txtObs.setText("");
                txtMinAlert.setText(String.valueOf(minStockActual));
                setOp(true, stockActual);
            });

            SoftButton btnBack = crearBotonLateral("⬅", "CANCELAR", COLOR_GOLD);
            btnBack.addActionListener(e -> dispose());

            sidebar.add(btnSave); sidebar.add(btnClear); sidebar.add(btnBack);
            add(sidebar, BorderLayout.EAST);

            // Listeners
            txtCantidad.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { calc(stockActual); } });
            txtMinAlert.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { calc(stockActual); } });
            setOp(true, stockActual);
        }

        // --- COMPONENTES VISUALES ---

        private JPanel crearControlAlertaUnificado(JTextField field) {
            JPanel p = new JPanel(new BorderLayout(0, 0));
            p.setOpaque(false);
            p.setBorder(new LineBorder(new Color(80,80,80), 1));

            SoftButton btnMinus = new SoftButton(null);
            btnMinus.setText("−");
            btnMinus.setFont(new Font("Arial", Font.BOLD, 28));
            btnMinus.setForeground(COLOR_ROJO_ALERTA);
            btnMinus.setPreferredSize(new Dimension(65, 50));
            btnMinus.setBackground(new Color(40,40,40)); // SOLIDO
            btnMinus.setOpaque(true);
            btnMinus.addActionListener(e -> adjustVal(field, -1));

            SoftButton btnPlus = new SoftButton(null);
            btnPlus.setText("+");
            btnPlus.setFont(new Font("Arial", Font.BOLD, 28));
            btnPlus.setForeground(COLOR_VERDE_NEON);
            btnPlus.setPreferredSize(new Dimension(65, 50));
            btnPlus.setBackground(new Color(40,40,40)); // SOLIDO
            btnPlus.setOpaque(true);
            btnPlus.addActionListener(e -> adjustVal(field, 1));

            field.setHorizontalAlignment(JTextField.CENTER);
            field.setBackground(new Color(20,20,20));
            field.setForeground(Color.WHITE);
            field.setFont(new Font("Segoe UI", Font.BOLD, 28));
            field.setBorder(null);
            field.setPreferredSize(new Dimension(0, 50));
            field.setEditable(false);

            p.add(btnMinus, BorderLayout.WEST);
            p.add(field, BorderLayout.CENTER);
            p.add(btnPlus, BorderLayout.EAST);
            return p;
        }

        private JPanel crearInfoBox(String titulo, String valor, Color colorVal, int align) {
            JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
            JLabel lt = new JLabel(titulo, align); lt.setFont(new Font("Segoe UI", Font.BOLD, 13)); lt.setForeground(COLOR_LABEL);
            JLabel lv = new JLabel(valor, align); lv.setFont(new Font("Segoe UI", Font.BOLD, 26)); lv.setForeground(colorVal);
            p.add(lt, BorderLayout.NORTH); p.add(lv, BorderLayout.CENTER);
            return p;
        }

        private JLabel crearLabel(String t) {
            JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(COLOR_LABEL);
            l.setBorder(new EmptyBorder(0,0,8,0));
            return l;
        }

        private JTextField crearInput(Color c) {
            JTextField t = new JTextField(); t.setFont(new Font("Segoe UI", Font.BOLD, 28));
            t.setHorizontalAlignment(JTextField.CENTER); t.setBackground(new Color(20,20,20));
            t.setForeground(c); t.setCaretColor(c);
            t.setBorder(new LineBorder(new Color(80,80,80)));
            t.setPreferredSize(new Dimension(0, 50));
            t.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e) { t.setBorder(new LineBorder(c, 2)); t.selectAll(); } public void focusLost(FocusEvent e) { t.setBorder(new LineBorder(new Color(80,80,80))); } });
            return t;
        }

        private SoftButton crearBotonLateral(String icon, String text, Color c) {
            SoftButton btn = new SoftButton(null); btn.setLayout(new BorderLayout());
            JLabel lI = new JLabel(icon, SwingConstants.CENTER); lI.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40)); lI.setForeground(c);
            JLabel lT = new JLabel(text, SwingConstants.CENTER); lT.setFont(new Font("Segoe UI", Font.BOLD, 13)); lT.setForeground(c);
            btn.add(lI, BorderLayout.CENTER); btn.add(lT, BorderLayout.SOUTH); btn.setBorder(new EmptyBorder(10,10,10,10));
            return btn;
        }

        private void adjustVal(JTextField f, int d) {
            try {
                int v = Integer.parseInt(f.getText());
                if(v+d >= 0) {
                    f.setText(String.valueOf(v+d));
                    calc(Integer.parseInt(f.getText()));
                    for(KeyListener k : f.getKeyListeners()) k.keyReleased(null);
                }
            } catch(Exception e){}
        }

        private void setOp(boolean in, int act) {
            esEntrada = in; btnIn.setSelected(in); btnOut.setSelected(!in);
            Color activeColor = in ? COLOR_VERDE_NEON : COLOR_FUCSIA_NEON;
            txtCantidad.setForeground(activeColor); txtCantidad.setCaretColor(activeColor);
            calc(act);
        }

        private void calc(int act) {
            try {
                int n = txtCantidad.getText().isEmpty() ? 0 : Integer.parseInt(txtCantidad.getText());
                int min = txtMinAlert.getText().isEmpty() ? 0 : Integer.parseInt(txtMinAlert.getText());
                int res = esEntrada ? act + n : act - n;
                lblResultado.setText("PROYECCIÓN FINAL: " + res);
                if (res <= min) lblResultado.setForeground(COLOR_ROJO_ALERTA);
                else lblResultado.setForeground(esEntrada ? COLOR_VERDE_NEON : COLOR_FUCSIA_NEON);
            } catch(Exception e) { lblResultado.setText("PROYECCIÓN: -"); lblResultado.setForeground(Color.GRAY); }
        }

        private void guardar(int id, int stockActual) {
            try {
                int c = txtCantidad.getText().isEmpty() ? 0 : Integer.parseInt(txtCantidad.getText());
                int newMin = Integer.parseInt(txtMinAlert.getText());
                int delta = esEntrada ? c : -c;
                if (c <= 0 && txtObs.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Indique una cantidad o motivo.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
                if (c > 0) productDAO.auditStock(id, delta, txtObs.getText().isEmpty() ? "AJUSTE INVENTARIO" : txtObs.getText());
                productDAO.updateMinStock(id, newMin);
                dispose();
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Error de datos.", "Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    // --- BOTÓN TOGGLE "GLASS" PARA OPERACIONES ---
    class StyledToggleButton extends SoftButton {
        private final Color baseColor;
        private boolean isSelected = false;

        public StyledToggleButton(String text, String icon, Color c) {
            super(null); this.baseColor = c; setLayout(new BorderLayout());
            JLabel lIcon = new JLabel(icon, SwingConstants.CENTER); lIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            JLabel lText = new JLabel(text, SwingConstants.CENTER); lText.setFont(new Font("Segoe UI", Font.BOLD, 13));
            add(lIcon, BorderLayout.CENTER); add(lText, BorderLayout.SOUTH); setBorder(new EmptyBorder(10,10,10,10)); setSelected(false);
        }

        public void setSelected(boolean s) {
            this.isSelected = s;
            for(Component c : getComponents()) c.setForeground(s ? baseColor : Color.GRAY);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if(isSelected) {
                g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 20));
                g2.fillRoundRect(0,0,getWidth(),getHeight(), 15, 15);
                g2.setColor(baseColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2, 15, 15);
            } else {
                g2.setColor(new Color(30,30,30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(), 15, 15);
                g2.setColor(new Color(60,60,60));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2, 15, 15);
            }
            g2.dispose();
        }
    }
}