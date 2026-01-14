/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: InventoryView.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.1.0 (Clean Currency Rows & Dynamic Header)
 * * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Vista (MVC). Gestiona la interfaz principal
 * del inventario implementando renderizadores personalizados para JTable,
 * lógica multimoneda en tiempo real y componentes de UI enriquecidos.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.dao.Conexion;
import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Product;
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.LanguageManager;
import com.swimcore.view.dialogs.AddEditProductDialog;
import com.swimcore.view.dialogs.SupplierManagementDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

/**
 * Vista maestra de Gestión de Inventario y Control Administrativo.
 * Aplica principios de Abstracción y Encapsulamiento para manejar la UI.
 */
public class InventoryView extends JDialog {

    // Componentes de la Capa de Datos y UI
    private JTable productTable;
    private DefaultTableModel tableModel;
    private final ProductDAO productDAO = new ProductDAO();

    // --- PALETA DE COLORES CORPORATIVA (DARK MODE) ---
    private final Color COLOR_BG_MAIN = new Color(15, 15, 15);
    private final Color COLOR_TABLE_BG = new Color(30, 30, 30);
    private final Color COLOR_HEADER_BG = new Color(220, 0, 115); // Fucsia SICONI
    private final Color COLOR_TEXTO = new Color(240, 240, 240);
    private final Color COLOR_VERDE_NEON = new Color(0, 255, 128);

    private JButton btnTasa;
    private JComboBox<String> cmbMoneda;

    /**
     * Constructor principal: Inicializa el chasis visual y carga los recursos.
     * @param parent Frame de referencia para la modalidad del diálogo.
     */
    public InventoryView(JFrame parent) {
        super(parent, LanguageManager.get("inventory.window_title"), true);

        // Configuración de ventana (Layout Manager: BorderLayout)
        setSize(1250, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG_MAIN);

        initHeader();
        initCenterSection();
        initFooter();

        loadProducts(""); // Carga inicial de datos desde SQLite
    }

    /**
     * Inicializa el encabezado dinámico con selectores de divisa y tasa BCV.
     */
    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 40, 10, 40));

        JLabel lblTitle = new JLabel("GESTIÓN ESTRATÉGICA DE INVENTARIO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(COLOR_TEXTO);

        // Panel de control de tasas (Alineación derecha)
        JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        ratePanel.setOpaque(false);

        String[] monedas = {"VER EN EUROS (€)", "VER EN DÓLARES ($)", "VER EN BOLÍVARES (Bs.)"};
        cmbMoneda = new JComboBox<>(monedas);
        cmbMoneda.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbMoneda.addActionListener(e -> actualizarTituloColumnaPrecio());

        btnTasa = new JButton(String.format(Locale.US, "TASA BCV: %.2f", CurrencyManager.getTasa()));
        btnTasa.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        btnTasa.setForeground(Color.WHITE);
        btnTasa.setBackground(new Color(55, 55, 55));
        btnTasa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTasa.addActionListener(e -> updateExchangeRate());

        ratePanel.add(cmbMoneda);
        ratePanel.add(btnTasa);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(ratePanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    /**
     * Configura la sección central: barra de búsqueda y la tabla de productos.
     */
    private void initCenterSection() {
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(10, 40, 0, 40));

        // Barra de búsqueda con placeholder
        JTextField txtSearch = new JTextField(30);
        txtSearch.setPreferredSize(new Dimension(400, 42));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtSearch.putClientProperty("JTextField.placeholderText", LanguageManager.get("inventory.search_placeholder"));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) { loadProducts(txtSearch.getText()); }
        });

        JPanel searchPanel = new JPanel();
        searchPanel.setOpaque(false);
        searchPanel.add(txtSearch);
        centerContainer.add(searchPanel, BorderLayout.NORTH);

        // Definición del Modelo de Tabla (Inmutabilidad de celdas)
        tableModel = new DefaultTableModel(new String[]{
                LanguageManager.get("table.id"), LanguageManager.get("table.code"), LanguageManager.get("table.name"),
                LanguageManager.get("table.category"), LanguageManager.get("table.stock"),
                "PRECIO (€)", LanguageManager.get("table.supplier")
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return (c == 4) ? Integer.class : String.class; }
        };

        productTable = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        scrollPane.getViewport().setBackground(COLOR_TABLE_BG);
        personalizarScroll(scrollPane);

        centerContainer.add(scrollPane, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * Aplica la lógica de encabezados dinámicos según la divisa seleccionada.
     */
    private void actualizarTituloColumnaPrecio() {
        String seleccion = (String) cmbMoneda.getSelectedItem();
        String simbolo = seleccion.contains("$") ? " ($)" : seleccion.contains("Bs.") ? " (Bs.)" : " (€)";

        productTable.getColumnModel().getColumn(5).setHeaderValue("PRECIO" + simbolo);
        productTable.getTableHeader().repaint();
        loadProducts("");
    }

    /**
     * Capa de Negocio: Carga datos desde SQLite aplicando filtros de búsqueda y conversión de moneda.
     * @param search Texto de filtrado para búsqueda por nombre, código o proveedor.
     */
    private void loadProducts(String search) {
        tableModel.setRowCount(0);
        String sql = "SELECT p.id, p.code, p.name, c.name AS category_name, p.current_stock, p.sale_price, s.company FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id LEFT JOIN suppliers s ON p.supplier_id = s.id ";

        if (search != null && !search.trim().isEmpty()) {
            String kw = search.toLowerCase();
            sql += "WHERE LOWER(p.name) LIKE '%"+kw+"%' OR LOWER(p.code) LIKE '%"+kw+"%' OR LOWER(s.company) LIKE '%"+kw+"%'";
        }

        try (Connection conn = Conexion.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            double tasa = CurrencyManager.getTasa();
            String seleccion = (String) cmbMoneda.getSelectedItem();
            Locale locDivisa = Locale.US;
            Locale locBs = new Locale("es", "VE");

            while (rs.next()) {
                double precioEuro = rs.getDouble("sale_price");
                String precioFormateado;

                if (seleccion.contains("EUROS") || seleccion.contains("DÓLARES")) {
                    precioFormateado = String.format(locDivisa, "%.2f ", precioEuro) +
                            String.format(locBs, "(Bs. %.2f)", precioEuro * tasa);
                } else {
                    precioFormateado = String.format(locBs, "%.2f", precioEuro * tasa);
                }

                tableModel.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("code"), rs.getString("name"),
                        rs.getString("category_name"), rs.getInt("current_stock"),
                        precioFormateado, rs.getString("company")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Aplica renderizadores de diseño (Semáforo de stock) y estilos a la JTable.
     */
    private void styleTable() {
        productTable.setRowHeight(55);
        productTable.setBackground(COLOR_TABLE_BG);
        productTable.setForeground(COLOR_TEXTO);
        productTable.setSelectionBackground(COLOR_HEADER_BG);
        productTable.setSelectionForeground(Color.WHITE);
        productTable.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));

        JTableHeader header = productTable.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            if (i != 2) productTable.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        // Aplicación del Patrón de Diseño: Renderizador específico para la columna Stock
        productTable.getColumnModel().getColumn(4).setCellRenderer(new StockTrafficLightRenderer());
    }

    /**
     * Configura la botonera inferior con estilos unificados.
     */
    private void initFooter() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(15, 0, 25, 0));

        actionPanel.add(crearBotonRectangular("btn.new", new Color(0, 170, 95), e -> {
            new AddEditProductDialog(this, null).setVisible(true);
            loadProducts("");
        }));
        actionPanel.add(crearBotonRectangular("btn.edit", new Color(60, 120, 200), e -> editSelected()));
        actionPanel.add(crearBotonRectangular("btn.delete", new Color(200, 50, 50), e -> deleteSelected()));
        actionPanel.add(crearBotonRectangular("PROVEEDORES", new Color(255, 140, 0), e -> new SupplierManagementDialog(this).setVisible(true)));
        actionPanel.add(crearBotonRectangular("btn.exit", new Color(80, 80, 80), e -> dispose()));

        add(actionPanel, BorderLayout.SOUTH);
    }

    /**
     * Factory Method: Crea botones con estilos de borde neón y tipografía uniforme.
     */
    private JButton crearBotonRectangular(String langKey, Color bg, java.awt.event.ActionListener al) {
        String texto = langKey.startsWith("btn.") ? LanguageManager.get(langKey).toUpperCase() : langKey;
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(185, 45));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(bg.brighter(), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    /**
     * Lógica para actualizar la tasa de cambio global del sistema.
     */
    private void updateExchangeRate() {
        String val = JOptionPane.showInputDialog(this, "ACTUALIZAR TASA BCV (Use punto decimal)",
                String.format(Locale.US, "%.2f", CurrencyManager.getTasa()));
        if (val != null) {
            try {
                double nuevaTasa = Double.parseDouble(val.replace(",", "."));
                CurrencyManager.setTasa(nuevaTasa);
                btnTasa.setText(String.format(Locale.US, "TASA BCV: %.2f", nuevaTasa));
                loadProducts("");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: Ingrese un valor numérico válido.");
            }
        }
    }

    private void editSelected() {
        int r = productTable.getSelectedRow();
        if (r != -1) {
            Product p = productDAO.getProductById((int) productTable.getValueAt(r, 0));
            new AddEditProductDialog(this, p).setVisible(true);
            loadProducts("");
        } else {
            JOptionPane.showMessageDialog(this, LanguageManager.get("msg.select_product"));
        }
    }

    private void deleteSelected() {
        int r = productTable.getSelectedRow();
        if (r != -1 && JOptionPane.showConfirmDialog(this, LanguageManager.get("msg.confirm_delete")) == JOptionPane.YES_OPTION) {
            productDAO.delete((int) productTable.getValueAt(r, 0));
            loadProducts("");
        }
    }

    private void personalizarScroll(JScrollPane scrollPane) {
        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        vBar.setPreferredSize(new Dimension(14, Integer.MAX_VALUE));
        vBar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = COLOR_HEADER_BG;
                this.trackColor = COLOR_TABLE_BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
            @Override protected JButton createIncreaseButton(int o) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
        });
    }

    /**
     * Renderizador visual: Implementa el "Semáforo" de existencias mediante iconos de esfera.
     */
    class StockTrafficLightRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            int stock = (v instanceof Integer) ? (Integer) v : Integer.parseInt(v.toString());
            l.setHorizontalAlignment(JLabel.CENTER);
            l.setText("  " + stock);

            if (stock == 0) l.setIcon(new StockSphere(Color.RED));
            else if (stock <= 5) l.setIcon(new StockSphere(Color.ORANGE));
            else l.setIcon(new StockSphere(COLOR_VERDE_NEON));
            return l;
        }
    }

    /**
     * Componente Icono: Dibuja una esfera neón con antialiasing para el semáforo de stock.
     */
    class StockSphere implements Icon {
        private final Color color;
        public StockSphere(Color color) { this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y + 4, 12, 12);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 15; }
        @Override public int getIconHeight() { return 20; }
    }
}