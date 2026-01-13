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
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Vista (View) responsable de la gestión visual del inventario.
 * Implementa un JDialog modal que presenta los datos en una estructura tabular (JTable)
 * con capacidades de filtrado en tiempo real y conversión monetaria dinámica.
 *
 * Funcionalidades de Ingeniería:
 * 1. Visualización de Datos Avanzada: Implementación de Renderers personalizados (StockTrafficLightRenderer)
 * para indicadores visuales de estado crítico (Semáforo de Stock).
 * 2. Manipulación de Datos (DML): Orquestación de operaciones CRUD mediante DAOs y SQL directo para reportes.
 * 3. Interacción SQL: Consumo de consultas complejas (LEFT JOINs) para enriquecer la vista con datos relacionales.
 *
 * PRINCIPIOS POO:
 * - POLIMORFISMO: Implementación de Renderers personalizados extendiendo `DefaultTableCellRenderer`
 * y sobreescribiendo el metodo `getTableCellRendererComponent`.
 * - ENCAPSULAMIENTO: Gestión privada del modelo de tabla (`DefaultTableModel`) y componentes internos.
 * - HERENCIA: Extiende de `javax.swing.JDialog` para heredar comportamiento de ventana secundaria.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.dao.Conexion;
import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Product;
import com.swimcore.util.CurrencyManager;
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

/**
 * Vista de Gestión de Inventario.
 * Presenta el catálogo de productos, niveles de stock y precios en múltiples divisas.
 */
public class InventoryView extends JDialog {

    private JTable table;
    private DefaultTableModel model;
    private final ProductDAO productDAO = new ProductDAO();

    // --- DEFINICIÓN DE CONSTANTES DE ESTILO (PALETA DARK MODE) ---
    private final Color COLOR_BG_MAIN = new Color(15, 15, 15);
    private final Color COLOR_TABLE_BG = new Color(30, 30, 30);
    private final Color COLOR_HEADER_BG = new Color(220, 0, 115); // Fucsia Corporativo (Brand Identity)
    private final Color COLOR_HEADER_TEXT = Color.WHITE;
    private final Color COLOR_TEXTO = new Color(240, 240, 240);
    private final Color COLOR_VERDE_NEON = new Color(0, 255, 128);

    private JButton btnTasa;
    private JComboBox<String> cmbMoneda;

    /**
     * Constructor.
     * @param parent Ventana padre (Dashboard) para mantener la jerarquía modal.
     */
    public InventoryView(JFrame parent) {
        super(parent, "Gestión de Inventario SICONI", true); // Modalidad 'true' bloquea la interacción con la ventana padre
        setSize(1300, 780);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG_MAIN);

        // ================= HEADER (ENCABEZADO) =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 40, 10, 40));

        JLabel lblTitle = new JLabel("INVENTARIO & PRECIOS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitle.setForeground(COLOR_TEXTO);

        // Panel de Control Financiero (Tasa y Moneda)
        JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        ratePanel.setOpaque(false);

        String[] monedas = {"VER EN EUROS (€)", "VER EN DÓLARES ($)", "VER EN BOLÍVARES (Bs)"};
        cmbMoneda = new JComboBox<>(monedas);
        cmbMoneda.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbMoneda.setFocusable(false);

        // Botón indicador de Tasa BCV (Conexión con CurrencyManager)
        btnTasa = new JButton("🔄 TASA BCV: " + CurrencyManager.getTasa());
        btnTasa.setBackground(new Color(40, 40, 40));
        btnTasa.setForeground(COLOR_VERDE_NEON);
        btnTasa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTasa.setBorder(new LineBorder(COLOR_VERDE_NEON, 1, true));
        btnTasa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTasa.addActionListener(e -> cambiarTasa());

        ratePanel.add(cmbMoneda);
        ratePanel.add(btnTasa);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(ratePanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ================= CONTENIDO CENTRAL (DATA GRID) =================
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(10, 40, 0, 40));

        // BUSCADOR (FILTERING)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setOpaque(false);
        JTextField txtSearch = new JTextField(30);
        txtSearch.setPreferredSize(new Dimension(400, 45));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍 Buscar producto, código o proveedor...");

        // Listener de eventos de teclado para búsqueda en tiempo real (Live Search)
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) { cargarProductos(txtSearch.getText()); }
        });
        searchPanel.add(txtSearch);
        centerContainer.add(searchPanel, BorderLayout.NORTH);

        // CONFIGURACIÓN DE TABLA (JTABLE)
        String[] cols = {"ID", "CÓDIGO", "PRODUCTO", "CATEGORÍA", "STOCK", "PRECIO", "PROVEEDOR"};

        // Sobreescritura anónima del modelo para definir tipos de datos y restricciones de edición
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; } // Read-Only
            public Class<?> getColumnClass(int c) { return (c == 4) ? Integer.class : String.class; }
        };

        table = new JTable(model);
        estilizarTabla(); // Aplicación de Look & Feel personalizado

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        scrollPane.getViewport().setBackground(COLOR_TABLE_BG);

        JPanel corner = new JPanel();
        corner.setBackground(COLOR_HEADER_BG);
        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);

        personalizarScroll(scrollPane); // Scrollbars vectoriales

        centerContainer.add(scrollPane, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // ================= BOTONES INFERIORES (CRUD ACTIONS) =================
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(25, 0, 50, 0));

        // Botón CREATE
        actionPanel.add(crearBotonPastilla("NUEVO", new Color(220, 0, 115), e -> {
            new AddEditProductDialog(this, null).setVisible(true);
            cargarProductos(""); // Refresco automático post-inserción
        }));

        // Botón UPDATE
        actionPanel.add(crearBotonPastilla("EDITAR", new Color(60, 60, 60), e -> editarSeleccionado()));
        // Botón DELETE
        actionPanel.add(crearBotonPastilla("ELIMINAR", new Color(200, 50, 50), e -> eliminarSeleccionado()));
        // Navegación a Proveedores
        actionPanel.add(crearBotonPastilla("PROVEEDORES", new Color(255, 140, 0), e ->
                new SupplierManagementDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true)
        ));

        // Botón CLOSE
        JButton btnCerrar = crearBotonPastilla("SALIR", Color.GRAY, e -> dispose());
        actionPanel.add(btnCerrar);

        add(actionPanel, BorderLayout.SOUTH);

        // Carga inicial de datos (Bootstrapping)
        cargarProductos("");
    }

    // --- MÉTODOS DE ESTILIZACIÓN VISUAL (UI RENDERING) ---

    private void estilizarTabla() {
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setBackground(COLOR_TABLE_BG);
        table.setForeground(COLOR_TEXTO);
        table.setSelectionBackground(new Color(220, 0, 115)); // Selección Fucsia
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(50, 50, 50));

        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(COLOR_HEADER_TEXT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 50));

        // Alineación de celdas
        DefaultTableCellRenderer center = new DefaultTableCellRenderer(); center.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer(); left.setHorizontalAlignment(JLabel.LEFT); left.setBorder(new EmptyBorder(0, 15, 0, 0));

        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(left);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        // Inyección del Renderer de Semáforo (Custom Component)
        table.getColumnModel().getColumn(4).setCellRenderer(new StockTrafficLightRenderer());

        table.getColumnModel().getColumn(5).setCellRenderer(center);
        table.getColumnModel().getColumn(6).setCellRenderer(center);

        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);
    }

    /**
     * Factory Method para botones con estilo "Pastilla" (Rounded Borders).
     */
    private JButton crearBotonPastilla(String texto, Color color, java.awt.event.ActionListener action) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Antialiasing para bordes suaves
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40); // Radio 40px
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(180, 55));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(action);
        return btn;
    }

    /**
     * Renderer Personalizado: Implementa la lógica visual del Semáforo de Inventario.
     * Analiza el nivel de stock y renderiza un icono de color dinámico.
     */
    class StockTrafficLightRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);

            try {
                int stock = Integer.parseInt(value.toString());
                c.setText("  " + value);
                c.setHorizontalAlignment(JLabel.CENTER);

                // Lógica de Negocio Visual:
                // 0 = Rojo (Crítico) | <= 5 = Naranja (Alerta) | > 5 = Verde (Óptimo)
                if (stock == 0) {
                    c.setForeground(new Color(255, 80, 80));
                    c.setIcon(new StockIcon(Color.RED));
                } else if (stock <= 5) {
                    c.setForeground(new Color(255, 200, 0));
                    c.setIcon(new StockIcon(Color.ORANGE));
                } else {
                    c.setForeground(COLOR_VERDE_NEON);
                    c.setIcon(new StockIcon(Color.GREEN));
                }

                if (isSel) c.setForeground(Color.WHITE);

            } catch (Exception e) { }
            return c;
        }
    }

    /**
     * Clase auxiliar para dibujar iconos vectoriales (Círculos) en memoria.
     */
    class StockIcon implements Icon {
        private Color c; public StockIcon(Color c) { this.c = c; }
        public int getIconWidth() { return 10; } public int getIconHeight() { return 10; }
        public void paintIcon(Component cmp, Graphics g, int x, int y) {
            g.setColor(c); g.fillOval(x, y, 10, 10);
        }
    }

    private void personalizarScroll(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() { this.thumbColor = new Color(80, 80, 80); this.trackColor = COLOR_TABLE_BG; }
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        });
    }
    private JButton createZeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }

    /**
     * Lógica para actualización dinámica de la tasa de cambio en memoria (Singleton CurrencyManager).
     */
    private void cambiarTasa() {
        String input = JOptionPane.showInputDialog(this, "Nueva Tasa BCV:", CurrencyManager.getTasa());
        if (input != null && !input.isEmpty()) {
            try {
                CurrencyManager.setTasa(Double.parseDouble(input.replace(",", ".")));
                btnTasa.setText("🔄 TASA BCV: Bs. " + CurrencyManager.getTasa());
                cargarProductos(""); // Recalcular precios en la vista
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Número inválido"); }
        }
    }

    // --- ACCESO A DATOS Y LÓGICA DE NEGOCIO ---

    /**
     * Carga de datos optimizada utilizando SQL Joins para traer descripciones legibles.
     * @param busqueda Cadena para filtrado SQL (WHERE clause).
     */
    private void cargarProductos(String busqueda) {
        model.setRowCount(0);

        // CORRECCIÓN IMPORTANTE: Uso de ALIAS SQL ('cat_name') para resolver ambigüedad
        // entre el nombre del producto (p.name) y el nombre de la categoría (c.name).
        String sql = "SELECT p.id, p.code, p.name, c.name AS cat_name, p.current_stock, p.sale_price, s.company FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " + // Join para obtener nombre de categoría
                "LEFT JOIN suppliers s ON p.supplier_id = s.id ";   // Join para obtener nombre de proveedor

        // Inyección de parámetros de búsqueda
        if (!busqueda.isEmpty()) sql += "WHERE p.name LIKE '%" + busqueda + "%' OR p.code LIKE '%" + busqueda + "%' OR s.company LIKE '%" + busqueda + "%'";

        try (Connection conn = Conexion.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("cat_name"), // Recuperación correcta del ALIAS de categoría
                        rs.getInt("current_stock"),
                        CurrencyManager.formatPrice(rs.getDouble("sale_price")), // Formateo monetario
                        rs.getString("company")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void editarSeleccionado() {
        int r = table.getSelectedRow();
        if (r != -1) {
            Product p = productDAO.getProductById((int) table.getValueAt(r, 0));
            if (p != null) { new AddEditProductDialog(this, p).setVisible(true); cargarProductos(""); }
        } else JOptionPane.showMessageDialog(this, "Selecciona un producto.");
    }

    private void eliminarSeleccionado() {
        int r = table.getSelectedRow();
        if (r != -1 && JOptionPane.showConfirmDialog(this, "¿Eliminar?", "Confirma", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            productDAO.delete((int) table.getValueAt(r, 0)); cargarProductos("");
        } else JOptionPane.showMessageDialog(this, "Selecciona un producto.");
    }
}