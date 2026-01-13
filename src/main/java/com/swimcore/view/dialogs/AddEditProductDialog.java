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
 * VERSIÓN: 1.3.0 (Rectangular Button Standard & Screen Fit)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Diálogo modal para la creación y edición de productos.
 * Implementa botones rectangulares estandarizados y optimización de
 * altura para visualización completa en resoluciones estándar.
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

public class AddEditProductDialog extends JDialog {

    private final ProductDAO productDAO = new ProductDAO();
    private Product productToEdit = null;
    private final Map<String, String> categoryPrefixes = new HashMap<>();

    private JTextField txtCode, txtName, txtCostPrice, txtSalePrice, txtStock, txtMinStock;
    private JTextArea txtDesc;
    private JComboBox<Category> cmbCategory;

    // --- PALETA CORPORATIVA SICONI 8K ---
    private final Color COLOR_BG = new Color(20, 20, 20);
    private final Color COLOR_INPUT_BG = new Color(45, 45, 45);
    private final Color COLOR_TEXTO = new Color(240, 240, 240);
    private final Color COLOR_VERDE_NEON = new Color(0, 255, 128);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);

    private final Font FONT_INPUT = new Font("Tahoma", Font.PLAIN, 16);
    private final Font FONT_LABEL = new Font("Tahoma", Font.BOLD, 13);

    public AddEditProductDialog(InventoryView parent, Product product) {
        super(parent, product == null ? "Registrar Nuevo Ítem" : "Editar Ítem", true);
        this.productToEdit = product;

        setupPrefixes();

        // AJUSTE: Altura reducida a 650 para garantizar visibilidad de la botonera
        setSize(1000, 650);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // --- HEADER ---
        String titulo = (product == null) ? "REGISTRAR NUEVO ÍTEM" : "EDITAR: " + product.getCode();
        JLabel lblTitle = new JLabel(titulo, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(255, 140, 0));
        lblTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        // --- FORMULARIO ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(5, 60, 5, 60));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 15, 6, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        // Fila 1: Categoría y Código
        gbc.gridy = 0; gbc.gridx = 0; formPanel.add(crearLabel("CATEGORÍA (Define Código)"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("CÓDIGO (Automático)"), gbc);

        gbc.gridy = 1; gbc.gridx = 0;
        cmbCategory = new JComboBox<>();
        cmbCategory.setFont(FONT_INPUT);
        cmbCategory.setPreferredSize(new Dimension(250, 38));
        cargarCategorias();
        cmbCategory.addActionListener(e -> generarCodigo());
        formPanel.add(cmbCategory, gbc);

        gbc.gridx = 1;
        txtCode = crearInput(false);
        txtCode.setForeground(COLOR_VERDE_NEON);
        txtCode.setFont(new Font("Tahoma", Font.BOLD, 16));
        formPanel.add(txtCode, gbc);

        // Fila 2: Nombre y Detalles
        gbc.gridy = 2; gbc.gridx = 0; formPanel.add(crearLabel("NOMBRE DEL PRODUCTO"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("DESCRIPCIÓN / DETALLES"), gbc);

        gbc.gridy = 3; gbc.gridx = 0; formPanel.add(txtName = crearInput(true), gbc);

        gbc.gridx = 1;
        txtDesc = new JTextArea(2, 20);
        txtDesc.setFont(FONT_INPUT);
        txtDesc.setBackground(COLOR_INPUT_BG);
        txtDesc.setForeground(COLOR_TEXTO);
        txtDesc.setCaretColor(Color.WHITE);
        txtDesc.setBorder(new LineBorder(new Color(80, 80, 80)));
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setPreferredSize(new Dimension(250, 42));
        formPanel.add(scrollDesc, gbc);

        // Fila 3: Precios
        gbc.gridy = 4; gbc.gridx = 0; formPanel.add(crearLabel("COSTO (Ref. Divisa €/$)"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("VENTA (Ref. Divisa €/$)"), gbc);

        gbc.gridy = 5; gbc.gridx = 0; formPanel.add(txtCostPrice = crearInput(true), gbc);
        gbc.gridx = 1; formPanel.add(txtSalePrice = crearInput(true), gbc);

        // Fila 4: Stocks
        gbc.gridy = 6; gbc.gridx = 0; formPanel.add(crearLabel("EXISTENCIA ACTUAL"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("ALERTA STOCK MÍNIMO"), gbc);

        gbc.gridy = 7; gbc.gridx = 0; formPanel.add(txtStock = crearInput(true), gbc);
        gbc.gridx = 1; formPanel.add(txtMinStock = crearInput(true), gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- BOTONERA (Standard Rectangular Style) ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        btnPanel.add(crearBotonRectangular("LIMPIAR", new Color(70, 70, 70), e -> limpiar()));
        btnPanel.add(crearBotonRectangular("CANCELAR", new Color(190, 45, 45), e -> dispose()));
        btnPanel.add(crearBotonRectangular("GUARDAR", COLOR_FUCSIA, e -> guardar()));
        btnPanel.add(crearBotonRectangular("SALIR", new Color(55, 55, 55), e -> dispose()));

        add(btnPanel, BorderLayout.SOUTH);

        if (productToEdit != null) llenarCampos();
        else { txtStock.setText("0"); txtMinStock.setText("5"); generarCodigo(); }
    }

    private void setupPrefixes() {
        categoryPrefixes.put("Modelos Referencia", "MOD");
        categoryPrefixes.put("Textiles", "MAT");
        categoryPrefixes.put("Mercería", "MER");
        categoryPrefixes.put("Branding", "BRA");
        categoryPrefixes.put("Equipos Taller", "ACT");
        categoryPrefixes.put("Accesorios", "REV");
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

    /**
     * Factory Method: Crea botones rectangulares unificados con el módulo
     * de proveedores para optimizar espacio en pantalla.
     */
    private JButton crearBotonRectangular(String t, Color c, ActionListener a) {
        JButton b = new JButton(t);
        b.setPreferredSize(new Dimension(130, 42));
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(c.brighter(), 1));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(a);
        return b;
    }

    private void generarCodigo() {
        if (productToEdit != null) return;
        Category sel = (Category) cmbCategory.getSelectedItem();
        if (sel != null) {
            String prefix = categoryPrefixes.getOrDefault(sel.getName(), "PROD");
            txtCode.setText(productDAO.generateSmartCode(prefix));
        }
    }

    private void guardar() {
        try {
            Product p = (productToEdit == null) ? new Product() : productToEdit;
            p.setCode(txtCode.getText());
            p.setName(txtName.getText());
            p.setDescription(txtDesc.getText());
            p.setCostPrice(Double.parseDouble(txtCostPrice.getText().replace(",", ".")));
            p.setSalePrice(Double.parseDouble(txtSalePrice.getText().replace(",", ".")));
            p.setCurrentStock(Integer.parseInt(txtStock.getText()));
            p.setMinStock(Integer.parseInt(txtMinStock.getText()));
            p.setCategoryId(((Category)cmbCategory.getSelectedItem()).getId());

            if (productDAO.save(p)) {
                JOptionPane.showMessageDialog(this, "Datos sincronizados correctamente.");
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Verifique los datos numéricos.");
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
            if(cmbCategory.getItemAt(i).getId() == productToEdit.getCategoryId()) cmbCategory.setSelectedIndex(i);
        }
    }

    private void limpiar() {
        txtName.setText(""); txtDesc.setText(""); txtCostPrice.setText(""); txtSalePrice.setText("");
        txtStock.setText("0"); txtMinStock.setText("5"); generarCodigo();
    }

    private void cargarCategorias() {
        for(Category c : productDAO.getAllCategories()) cmbCategory.addItem(c);
    }
}