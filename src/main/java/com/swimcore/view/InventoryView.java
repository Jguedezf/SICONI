/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: InventoryView.java
 * VERSIÓN: 11.1.0 (Audit Dialog Translation)
 * DESCRIPCIÓN: Inventario v11.1. Se completa la traducción de la ventana
 * emergente de ajuste de stock (AuditStockDialog).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.dao.Conexion;
import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Product;
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.LanguageManager;
import com.swimcore.view.components.InventorySidePanel;
import com.swimcore.view.components.SoftButton;
import com.swimcore.view.dialogs.AddEditProductDialog;
import com.swimcore.view.dialogs.CurrencySettingsDialog;
import com.swimcore.view.dialogs.InventoryHistoryDialog;
import com.swimcore.view.dialogs.SupplierManagementDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

public class InventoryView extends JDialog {

    private JTable productTable;
    private DefaultTableModel tableModel;
    private final ProductDAO productDAO = new ProductDAO();

    private final Color COLOR_BG_MAIN = new Color(20, 20, 20);
    private final Color COLOR_TABLE_BG = new Color(30, 30, 30);
    private final Color COLOR_HEADER_BG = new Color(10, 10, 10);
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_TEXTO = new Color(229, 228, 226);

    private final Color COLOR_VERDE_NEON = new Color(0, 255, 128);
    private final Color COLOR_FUCSIA_NEON = new Color(255, 0, 127);

    private SoftButton btnTasa;
    private JComboBox<String> cmbMoneda;

    public InventoryView(JFrame parent) {
        super(parent, LanguageManager.get("inventory.window_title"), true);
        setSize(1280, 720);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG_MAIN);

        initSidePanel();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        initHeader(mainPanel);
        initCenterSection(mainPanel);
        add(mainPanel, BorderLayout.CENTER);

        loadProducts("");
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

        JLabel lblTitle = new JLabel(LanguageManager.get("inventory.title"));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(COLOR_GOLD);

        JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        ratePanel.setOpaque(false);

        String[] monedas = {
                LanguageManager.get("inventory.currency.usd"),
                LanguageManager.get("inventory.currency.eur"),
                LanguageManager.get("inventory.currency.bs")
        };
        cmbMoneda = new JComboBox<>(monedas);
        cmbMoneda.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbMoneda.setBackground(new Color(50,50,50));
        cmbMoneda.setForeground(Color.WHITE);
        cmbMoneda.setSelectedIndex(CurrencyManager.getMode());
        cmbMoneda.addActionListener(e -> {
            CurrencyManager.setConfig(CurrencyManager.getTasa(), "$", cmbMoneda.getSelectedIndex());
            loadProducts("");
        });

        btnTasa = new SoftButton(null);
        btnTasa.setText(String.format(Locale.US, LanguageManager.get("inventory.rate_btn"), CurrencyManager.getTasa()));
        btnTasa.setPreferredSize(new Dimension(180, 35));
        btnTasa.setForeground(COLOR_GOLD);
        btnTasa.addActionListener(e -> {
            new CurrencySettingsDialog((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true);
            loadProducts("");
        });

        ratePanel.add(cmbMoneda);
        ratePanel.add(btnTasa);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(ratePanel, BorderLayout.EAST);
        parentPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void initCenterSection(JPanel parentPanel) {
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(10, 40, 40, 40));

        JTextField txtSearch = new JTextField(30);
        txtSearch.setToolTipText(LanguageManager.get("inventory.search_placeholder"));
        txtSearch.setPreferredSize(new Dimension(400, 42));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtSearch.setBackground(COLOR_TABLE_BG);
        txtSearch.setForeground(COLOR_TEXTO);
        txtSearch.setCaretColor(COLOR_GOLD);
        txtSearch.setBorder(new LineBorder(new Color(60,60,60)));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) { loadProducts(txtSearch.getText()); }
        });

        JPanel searchPanel = new JPanel();
        searchPanel.setOpaque(false);
        searchPanel.add(txtSearch);
        centerContainer.add(searchPanel, BorderLayout.NORTH);

        String[] cols = {
                LanguageManager.get("inventory.table.id"),
                LanguageManager.get("inventory.table.code"),
                LanguageManager.get("inventory.table.product"),
                LanguageManager.get("inventory.table.category"),
                LanguageManager.get("inventory.table.stock"),
                LanguageManager.get("inventory.table.price"),
                LanguageManager.get("inventory.table.supplier")
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return (c == 0 || c == 4) ? Integer.class : String.class; }
        };

        productTable = new JTable(tableModel);
        styleTable();

        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = productTable.columnAtPoint(e.getPoint());
                int row = productTable.rowAtPoint(e.getPoint());
                if (col == 4 && row != -1) {
                    Rectangle rect = productTable.getCellRect(row, col, false);
                    int xRelativo = e.getX() - rect.x;
                    int id = (int) productTable.getValueAt(row, 0);
                    if (xRelativo > 20 && xRelativo < 55) { productDAO.updateStockDelta(id, -1); e.consume(); }
                    else if (xRelativo > 115 && xRelativo < 150) { productDAO.updateStockDelta(id, 1); e.consume(); }
                    loadProducts("");
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(new LineBorder(COLOR_GOLD, 1));
        scrollPane.getViewport().setBackground(COLOR_TABLE_BG);
        centerContainer.add(scrollPane, BorderLayout.CENTER);
        parentPanel.add(centerContainer, BorderLayout.CENTER);
    }

    private void styleTable() {
        productTable.setRowHeight(50);
        productTable.setBackground(COLOR_TABLE_BG);
        productTable.setForeground(COLOR_TEXTO);
        productTable.setSelectionBackground(new Color(50, 50, 50));
        productTable.setSelectionForeground(COLOR_GOLD);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.setFocusable(false);
        JTableHeader header = productTable.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(COLOR_GOLD);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            if (i != 2 && i != 4) productTable.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        productTable.getColumnModel().getColumn(4).setCellRenderer(new PersistentStockRenderer());
    }

    private void loadProducts(String search) {
        tableModel.setRowCount(0);
        if(btnTasa != null) btnTasa.setText(String.format(Locale.US, LanguageManager.get("inventory.rate_btn"), CurrencyManager.getTasa()));
        String sql = "SELECT p.id, p.code, p.name, c.name AS category_name, p.current_stock, p.sale_price, s.company FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id LEFT JOIN suppliers s ON p.supplier_id = s.id ";
        if (search != null && !search.trim().isEmpty()) {
            String kw = search.toLowerCase();
            sql += "WHERE LOWER(p.name) LIKE '%"+kw+"%' OR LOWER(p.code) LIKE '%"+kw+"%' OR LOWER(s.company) LIKE '%"+kw+"%'";
        }
        try (Connection conn = Conexion.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                double precio = rs.getDouble("sale_price");
                double tasa = CurrencyManager.getTasa();
                String precioVis = (CurrencyManager.getMode() == 2) ? String.format("Bs. %,.2f", precio * tasa) : String.format("%,.2f  |  Bs. %,.2f", precio, precio * tasa);
                tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("code"), rs.getString("name"), rs.getString("category_name"), rs.getInt("current_stock"), precioVis, rs.getString("company")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void abrirGestionExistencias() {
        int r = productTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, LanguageManager.get("inventory.msg.select"), "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        int id = (int) productTable.getValueAt(r, 0);
        String nombre = (String) productTable.getValueAt(r, 2);
        int stock = (int) productTable.getValueAt(r, 4);
        new AuditStockDialog(this, id, nombre, stock).setVisible(true);
        loadProducts("");
    }

    private void editSelected() {
        int r = productTable.getSelectedRow();
        if (r != -1) { Product p = productDAO.getProductById((int) productTable.getValueAt(r, 0)); new AddEditProductDialog(this, p).setVisible(true); loadProducts(""); }
    }

    private void deleteSelected() {
        int r = productTable.getSelectedRow();
        if (r != -1 && JOptionPane.showConfirmDialog(this, LanguageManager.get("inventory.msg.confirm_delete")) == JOptionPane.YES_OPTION) {
            productDAO.delete((int) productTable.getValueAt(r, 0));
            loadProducts("");
        }
    }

    // --- RENDERIZADORES ---
    class PersistentStockRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel btnM = new JLabel("−", SwingConstants.CENTER);
        private final JLabel btnP = new JLabel("+", SwingConstants.CENTER);
        private final JLabel val = new JLabel("", SwingConstants.CENTER);
        public PersistentStockRenderer() {
            setLayout(new BorderLayout(10, 0)); setOpaque(true); setBorder(new EmptyBorder(12, 25, 12, 25));
            btnM.setFont(new Font("Arial", Font.BOLD, 22)); btnM.setForeground(COLOR_FUCSIA_NEON); btnM.setPreferredSize(new Dimension(25, 32));
            btnP.setFont(new Font("Arial", Font.BOLD, 22)); btnP.setForeground(COLOR_VERDE_NEON); btnP.setPreferredSize(new Dimension(25, 32));
            val.setFont(new Font("Segoe UI", Font.BOLD, 16)); val.setForeground(Color.WHITE);
            add(btnM, BorderLayout.WEST); add(val, BorderLayout.CENTER); add(btnP, BorderLayout.EAST);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
            int stock = (v instanceof Integer) ? (int) v : 0;
            val.setText(String.valueOf(stock));
            val.setIcon(new StockSphere(stock == 0 ? Color.RED : stock <= 5 ? Color.ORANGE : COLOR_VERDE_NEON));
            setBackground(isSel ? new Color(50, 50, 50) : t.getBackground());
            return this;
        }
    }

    class StockSphere implements Icon {
        private final Color color;
        public StockSphere(Color c) { this.color = c; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); g2.fillOval(x, y + 4, 12, 12); g2.dispose(); }
        @Override public int getIconWidth() { return 15; }
        @Override public int getIconHeight() { return 20; }
    }

    // --- DIÁLOGO DE AUDITORÍA (Traducido) ---
    class AuditStockDialog extends JDialog {
        private boolean esEntrada = true;
        private final JLabel lblResultado;
        private final JTextField txtCantidad;
        private final JTextArea txtObs;
        private final Control3DBtn btnIn, btnOut;

        public AuditStockDialog(JDialog parent, int id, String nombre, int stockActual) {
            super(parent, LanguageManager.get("audit.dialog.title"), true);
            setSize(440, 580);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(new Color(18, 18, 18));

            JPanel pnlHeader = new JPanel(new GridLayout(2, 1));
            pnlHeader.setOpaque(false);
            pnlHeader.setBorder(new EmptyBorder(30, 20, 15, 20));

            JLabel lblProd = new JLabel(nombre.toUpperCase(), SwingConstants.CENTER);
            lblProd.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
            lblProd.setForeground(Color.WHITE);

            // "EXISTENCIA ACTUAL: 10"
            JLabel lblActual = new JLabel(String.format(LanguageManager.get("audit.dialog.current"), stockActual), SwingConstants.CENTER);
            lblActual.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblActual.setForeground(new Color(150, 150, 150));

            pnlHeader.add(lblProd);
            pnlHeader.add(lblActual);

            JPanel pnlCentro = new JPanel();
            pnlCentro.setLayout(new BoxLayout(pnlCentro, BoxLayout.Y_AXIS));
            pnlCentro.setOpaque(false);
            pnlCentro.setBorder(new EmptyBorder(0, 50, 0, 50));

            JPanel pnlOp3D = new JPanel(new GridLayout(1, 2, 5, 0));
            pnlOp3D.setOpaque(false);
            pnlOp3D.setMaximumSize(new Dimension(240, 60));

            btnIn = new Control3DBtn(LanguageManager.get("audit.dialog.in"), COLOR_VERDE_NEON);
            btnOut = new Control3DBtn(LanguageManager.get("audit.dialog.out"), Color.GRAY);
            btnIn.setSelected(true);

            btnIn.addActionListener(e -> setOp(true, stockActual));
            btnOut.addActionListener(e -> setOp(false, stockActual));

            pnlOp3D.add(btnIn);
            pnlOp3D.add(btnOut);

            txtCantidad = new JTextField();
            txtCantidad.setFont(new Font("Segoe UI", Font.BOLD, 42));
            txtCantidad.setHorizontalAlignment(JTextField.CENTER);
            txtCantidad.setBackground(new Color(28, 28, 28));
            txtCantidad.setForeground(Color.WHITE);
            txtCantidad.setCaretColor(COLOR_VERDE_NEON);
            txtCantidad.setMaximumSize(new Dimension(240, 90));
            txtCantidad.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(60, 60, 60)));

            lblResultado = new JLabel(String.format(LanguageManager.get("audit.dialog.projection"), stockActual), SwingConstants.CENTER);
            lblResultado.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblResultado.setForeground(Color.ORANGE);
            lblResultado.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblResultado.setBorder(new EmptyBorder(20, 0, 20, 0));

            txtObs = new JTextArea(3, 20);
            txtObs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtObs.setBackground(new Color(25, 25, 25));
            txtObs.setForeground(new Color(220, 220, 220));
            txtObs.setLineWrap(true);
            txtObs.setWrapStyleWord(true);
            JScrollPane scrollObs = new JScrollPane(txtObs);
            scrollObs.setBorder(BorderFactory.createTitledBorder(new LineBorder(new Color(45, 45, 45)), LanguageManager.get("audit.table.reason"), 0, 0, null, Color.GRAY));
            scrollObs.setMaximumSize(new Dimension(320, 100));

            pnlCentro.add(pnlOp3D);
            pnlCentro.add(txtCantidad);
            pnlCentro.add(lblResultado);
            pnlCentro.add(scrollObs);

            txtCantidad.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) { calc(stockActual); }
            });

            JPanel pnlSouth = new JPanel();
            pnlSouth.setOpaque(false);
            pnlSouth.setBorder(new EmptyBorder(25, 0, 35, 0));

            JButton btnSave = new JButton(LanguageManager.get("audit.dialog.update"));
            btnSave.setPreferredSize(new Dimension(320, 55));
            btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btnSave.setBackground(new Color(220, 0, 115));
            btnSave.setForeground(Color.WHITE);
            btnSave.setFocusPainted(false);
            btnSave.setBorder(BorderFactory.createEmptyBorder());
            btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnSave.addActionListener(e -> {
                String obsStr = txtObs.getText().trim();
                try {
                    int c = Integer.parseInt(txtCantidad.getText());
                    if(c <= 0) { JOptionPane.showMessageDialog(this, "Número debe ser positivo."); return; }
                    int delta = esEntrada ? c : -c;
                    if(productDAO.auditStock(id, delta, obsStr)) {
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al actualizar.");
                    }
                } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Cantidad inválida."); }
            });
            pnlSouth.add(btnSave);

            add(pnlHeader, BorderLayout.NORTH);
            add(pnlCentro, BorderLayout.CENTER);
            add(pnlSouth, BorderLayout.SOUTH);
        }

        private void setOp(boolean in, int act) {
            esEntrada = in;
            btnIn.setSelected(in);
            btnIn.setBaseColor(in ? COLOR_VERDE_NEON : Color.GRAY);
            btnOut.setSelected(!in);
            btnOut.setBaseColor(!in ? COLOR_FUCSIA_NEON : Color.GRAY);
            calc(act);
        }

        private void calc(int act) {
            try {
                int n = Integer.parseInt(txtCantidad.getText());
                int proj = esEntrada ? act + n : act - n;
                lblResultado.setText(String.format(LanguageManager.get("audit.dialog.projection"), proj));
            } catch(Exception e) {
                lblResultado.setText(String.format(LanguageManager.get("audit.dialog.projection"), act));
            }
        }
    }

    class Control3DBtn extends JButton {
        private Color baseColor;
        private boolean isSelected = false;
        public Control3DBtn(String t, Color c) {
            super(t);
            this.baseColor = c;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        public void setBaseColor(Color c) { this.baseColor = c; repaint(); }
        public void setSelected(boolean s) { this.isSelected = s; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) g2.translate(0, 2);
            if (isSelected) {
                GradientPaint gp = new GradientPaint(0, 0, baseColor, 0, getHeight(), baseColor.darker());
                g2.setPaint(gp);
            } else {
                g2.setColor(new Color(40, 40, 40));
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(isSelected ? Color.BLACK : Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}