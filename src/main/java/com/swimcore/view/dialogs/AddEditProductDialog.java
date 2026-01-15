/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: AddEditProductDialog.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.3.1 (Persistent SmartCode & POO Standard)
 * * DESCRIPCIÓN TÉCNICA:
 * Diálogo modal (JDialog) para la persistencia transaccional de productos.
 * Implementa un algoritmo de generación de códigos basado en taxonomía de
 * categorías y validación de tipos de datos en tiempo de ejecución.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Category;
import com.swimcore.model.Product;
import com.swimcore.view.InventoryView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase encargada de la lógica de creación y edición de entidades Producto.
 * Aplica principios de Encapsulamiento y Abstracción de datos SQL.
 */
public class AddEditProductDialog extends JDialog {

    private final ProductDAO productDAO = new ProductDAO();
    private Product productToEdit = null;

    // PATRÓN: Diccionario de mapeo para prefijos de códigos inteligentes
    private final Map<String, String> categoryPrefixes = new HashMap<>();

    // Componentes de entrada de datos
    private JTextField txtCode, txtName, txtCostPrice, txtSalePrice, txtStock, txtMinStock;
    private JTextArea txtDesc;
    private JComboBox<Category> cmbCategory;

    // --- PALETA CORPORATIVA SICONI (ESTÁNDAR DARK MODE) ---
    private final Color COLOR_BG = new Color(20, 20, 20);
    private final Color COLOR_INPUT_BG = new Color(45, 45, 45);
    private final Color COLOR_TEXTO = new Color(240, 240, 240);
    private final Color COLOR_VERDE_NEON = new Color(0, 255, 128);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);

    private final Font FONT_INPUT = new Font("Tahoma", Font.PLAIN, 16);
    private final Font FONT_LABEL = new Font("Tahoma", Font.BOLD, 13);

    /**
     * Constructor: Configura el comportamiento modal y el chasis visual.
     * @param parent Vista de inventario para bloqueo de foco modal.
     * @param product Instancia de producto a editar (null si es registro nuevo).
     */
    public AddEditProductDialog(InventoryView parent, Product product) {
        super(parent, product == null ? "Registrar Nuevo Ítem" : "Editar Ítem", true);
        this.productToEdit = product;

        setupPrefixes(); // Inicializa lógica de SmartCodes

        // Configuración de resolución y centrado
        setSize(1000, 650);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        initHeader();
        initFormFields();
        initControlButtons();

        // Lógica condicional POO para inicialización de estado
        if (productToEdit != null) {
            llenarCampos();
        } else {
            txtStock.setText("0");
            txtMinStock.setText("5");
            generarCodigo();
        }
    }

    /**
     * Define el catálogo de siglas según la arquitectura de inventario.
     */
    private void setupPrefixes() {
        categoryPrefixes.put("Modelos Referencia", "MOD");
        categoryPrefixes.put("Textiles", "MAT");
        categoryPrefixes.put("Mercería", "MER");
        categoryPrefixes.put("Branding", "BRA");
        categoryPrefixes.put("Equipos Taller", "ACT");
        categoryPrefixes.put("Accesorios", "REV");
    }

    private void initHeader() {
        String titulo = (productToEdit == null) ? "REGISTRAR NUEVO ÍTEM" : "EDITAR PRODUCTO: " + productToEdit.getCode();
        JLabel lblTitle = new JLabel(titulo, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(255, 140, 0));
        lblTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);
    }

    private void initFormFields() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(5, 60, 5, 60));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 15, 6, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        // Fila 1: Categoría y Código Automático
        gbc.gridy = 0; gbc.gridx = 0; formPanel.add(crearLabel("CATEGORÍA (Define Prefijo)"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("CÓDIGO INTELIGENTE (Auto)"), gbc);

        gbc.gridy = 1; gbc.gridx = 0;
        cmbCategory = new JComboBox<>();
        cmbCategory.setFont(FONT_INPUT);
        cargarCategorias();
        cmbCategory.addActionListener(e -> generarCodigo());
        formPanel.add(cmbCategory, gbc);

        gbc.gridx = 1;
        txtCode = crearInput(false); // Inmutable para garantizar correlativo
        txtCode.setForeground(COLOR_VERDE_NEON);
        txtCode.setFont(new Font("Tahoma", Font.BOLD, 16));
        formPanel.add(txtCode, gbc);

        // Fila 2: Nombre y Descripción
        gbc.gridy = 2; gbc.gridx = 0; formPanel.add(crearLabel("NOMBRE DEL PRODUCTO"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("DESCRIPCIÓN TÉCNICA"), gbc);

        gbc.gridy = 3; gbc.gridx = 0; formPanel.add(txtName = crearInput(true), gbc);

        gbc.gridx = 1;
        txtDesc = new JTextArea(2, 20);
        txtDesc.setFont(FONT_INPUT);
        txtDesc.setBackground(COLOR_INPUT_BG);
        txtDesc.setForeground(COLOR_TEXTO);
        txtDesc.setLineWrap(true);
        txtDesc.setBorder(new LineBorder(new Color(80, 80, 80)));
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setPreferredSize(new Dimension(250, 42));
        formPanel.add(scrollDesc, gbc);

        // Fila 3: Finanzas (Formatos Decimales)
        gbc.gridy = 4; gbc.gridx = 0; formPanel.add(crearLabel("COSTO UNITARIO (€/$)"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("PRECIO VENTA (€/$)"), gbc);

        gbc.gridy = 5; gbc.gridx = 0; formPanel.add(txtCostPrice = crearInput(true), gbc);
        gbc.gridx = 1; formPanel.add(txtSalePrice = crearInput(true), gbc);

        // Fila 4: Control de Existencias
        gbc.gridy = 6; gbc.gridx = 0; formPanel.add(crearLabel("STOCK INICIAL"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("STOCK MÍNIMO (Alerta)"), gbc);

        gbc.gridy = 7; gbc.gridx = 0; formPanel.add(txtStock = crearInput(true), gbc);
        gbc.gridx = 1; formPanel.add(txtMinStock = crearInput(true), gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void initControlButtons() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        btnPanel.add(crearBotonRectangular("LIMPIAR", new Color(70, 70, 70), e -> limpiar()));
        btnPanel.add(crearBotonRectangular("CANCELAR", new Color(190, 45, 45), e -> dispose()));
        btnPanel.add(crearBotonRectangular("GUARDAR", COLOR_FUCSIA, e -> guardar()));

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.LIGHT_GRAY);
        l.setFont(FONT_LABEL);
        return l;
    }

    private JTextField crearInput(boolean ed) {
        JTextField t = new JTextField();
        t.setPreferredSize(new Dimension(250, 38));
        t.setBackground(COLOR_INPUT_BG);
        t.setForeground(COLOR_TEXTO);
        t.setFont(FONT_INPUT);
        t.setEditable(ed);
        t.setBorder(new LineBorder(new Color(80, 80, 80)));
        return t;
    }

    private JButton crearBotonRectangular(String t, Color c, ActionListener a) {
        JButton b = new JButton(t);
        b.setPreferredSize(new Dimension(150, 45));
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(c.brighter(), 1));
        b.addActionListener(a);
        return b;
    }

    /**
     * Lógica de Negocio: Algoritmo de generación de códigos basado en persistencia DAO.
     */
    private void generarCodigo() {
        if (productToEdit != null) return;
        Category sel = (Category) cmbCategory.getSelectedItem();
        if (sel != null) {
            String prefix = categoryPrefixes.getOrDefault(sel.getName(), "PROD");
            txtCode.setText(productDAO.generateSmartCode(prefix));
        }
    }

    /**
     * Persistencia: Valida y transfiere los datos de la UI al motor SQLite.
     */
    private void guardar() {
        try {
            // Sanitización básica de entradas obligatorias
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del producto es obligatorio.");
                return;
            }

            Product p = (productToEdit == null) ? new Product() : productToEdit;
            p.setCode(txtCode.getText());
            p.setName(txtName.getText().trim());
            p.setDescription(txtDesc.getText().trim());

            // Reemplazo de coma por punto para asegurar compatibilidad con Double.parseDouble
            p.setCostPrice(Double.parseDouble(txtCostPrice.getText().replace(",", ".")));
            p.setSalePrice(Double.parseDouble(txtSalePrice.getText().replace(",", ".")));

            p.setCurrentStock(Integer.parseInt(txtStock.getText().trim()));
            p.setMinStock(Integer.parseInt(txtMinStock.getText().trim()));
            p.setCategoryId(((Category)cmbCategory.getSelectedItem()).getId());

            // Delegación a la Capa de Datos
            if (productDAO.save(p)) {
                JOptionPane.showMessageDialog(this, "Registro sincronizado exitosamente.");
                dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: Verifique que los campos numéricos (Precios y Stock) sean válidos.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado al guardar: " + ex.getMessage());
        }
    }

    private void cargarCategorias() {
        for(Category c : productDAO.getAllCategories()) {
            cmbCategory.addItem(c);
        }
    }

    private void llenarCampos() {
        txtCode.setText(productToEdit.getCode());
        txtName.setText(productToEdit.getName());
        txtDesc.setText(productToEdit.getDescription());
        txtCostPrice.setText(String.valueOf(productToEdit.getCostPrice()));
        txtSalePrice.setText(String.valueOf(productToEdit.getSalePrice()));
        txtStock.setText(String.valueOf(productToEdit.getCurrentStock()));
        txtMinStock.setText(String.valueOf(productToEdit.getMinStock()));
        for(int i=0; i<cmbCategory.getItemCount(); i++) {
            if(cmbCategory.getItemAt(i).getId() == productToEdit.getCategoryId()) {
                cmbCategory.setSelectedIndex(i);
            }
        }
    }

    private void limpiar() {
        txtName.setText(""); txtDesc.setText("");
        txtCostPrice.setText(""); txtSalePrice.setText("");
        txtStock.setText("0"); txtMinStock.setText("5");
        generarCodigo();
    }
}