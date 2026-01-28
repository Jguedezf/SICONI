/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: AddEditProductDialog.java
 * VERSIÓN: 3.2.0 (Multilanguage Integration)
 * DESCRIPCIÓN: Formulario de producto adaptado para cargar etiquetas
 * dinámicamente según el idioma seleccionado.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Category;
import com.swimcore.model.Product;
import com.swimcore.util.LanguageManager; // <-- Importado
import com.swimcore.view.InventoryView;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AddEditProductDialog extends JDialog {

    private final ProductDAO productDAO = new ProductDAO();
    private Product productToEdit = null;
    private final Map<String, String> categoryPrefixes = new HashMap<>();

    private JTextField txtCode, txtName, txtCostPrice, txtSalePrice, txtStock, txtMinStock;
    private JTextArea txtDesc;
    private JComboBox<Category> cmbCategory;

    private final Color COLOR_BG_MAIN = new Color(20, 20, 20);
    private final Color COLOR_INPUT_BG = new Color(40, 40, 40);
    private final Color COLOR_TEXTO = new Color(229, 228, 226);
    private final Color COLOR_GOLD = new Color(212, 175, 55);

    private final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);

    public AddEditProductDialog(InventoryView parent, Product product) {
        super(parent, product == null ? LanguageManager.get("product.title.new") : String.format(LanguageManager.get("product.title.edit"), product.getCode()), true);
        this.productToEdit = product;

        setupRealPrefixes();

        setSize(900, 600);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout());

        initHeader();
        initFormFields();
        initControlButtons();

        if (productToEdit != null) {
            llenarCampos();
        } else {
            txtStock.setText("0");
            txtMinStock.setText("5");
            generarCodigo();
        }
    }

    private void setupRealPrefixes() {
        categoryPrefixes.put("Dama", "DAM");
        categoryPrefixes.put("Caballero", "CAB");
        categoryPrefixes.put("Accesorios", "ACC");
        categoryPrefixes.put("Insumos", "INS");
        categoryPrefixes.put("Telas", "TEL");
        categoryPrefixes.put("Niños", "KID");
    }

    private void initHeader() {
        String titulo = (productToEdit == null) ? LanguageManager.get("product.title.new") : String.format(LanguageManager.get("product.title.edit"), productToEdit.getCode());
        JLabel lblTitle = new JLabel(titulo, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_GOLD);
        lblTitle.setBorder(new EmptyBorder(25, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);
    }

    private void initFormFields() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(5, 50, 5, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        // Fila 1
        gbc.gridy = 0; gbc.gridx = 0; formPanel.add(crearLabel(LanguageManager.get("product.category")), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel(LanguageManager.get("product.code")), gbc);

        gbc.gridy = 1; gbc.gridx = 0;
        cmbCategory = new JComboBox<>();
        cmbCategory.setFont(FONT_INPUT);
        cmbCategory.setBackground(COLOR_INPUT_BG);
        cmbCategory.setForeground(COLOR_TEXTO);
        cargarCategorias();
        cmbCategory.addActionListener(e -> generarCodigo());
        formPanel.add(cmbCategory, gbc);

        gbc.gridx = 1;
        txtCode = crearInput(false);
        txtCode.setForeground(COLOR_GOLD);
        txtCode.setFont(new Font("Consolas", Font.BOLD, 14));
        formPanel.add(txtCode, gbc);

        // Fila 2
        gbc.gridy = 2; gbc.gridx = 0; formPanel.add(crearLabel(LanguageManager.get("product.name")), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel(LanguageManager.get("product.desc")), gbc);

        gbc.gridy = 3; gbc.gridx = 0; formPanel.add(txtName = crearInput(true), gbc);

        gbc.gridx = 1;
        txtDesc = new JTextArea(2, 20);
        txtDesc.setFont(FONT_INPUT);
        txtDesc.setBackground(COLOR_INPUT_BG);
        txtDesc.setForeground(COLOR_TEXTO);
        txtDesc.setCaretColor(COLOR_GOLD);
        txtDesc.setBorder(new LineBorder(new Color(80, 80, 80)));
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setPreferredSize(new Dimension(250, 35));
        formPanel.add(scrollDesc, gbc);

        // Fila 3
        gbc.gridy = 4; gbc.gridx = 0; formPanel.add(crearLabel(LanguageManager.get("product.cost")), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel(LanguageManager.get("product.price")), gbc);

        gbc.gridy = 5; gbc.gridx = 0; formPanel.add(txtCostPrice = crearInput(true), gbc);
        gbc.gridx = 1; formPanel.add(txtSalePrice = crearInput(true), gbc);

        // Fila 4
        gbc.gridy = 6; gbc.gridx = 0; formPanel.add(crearLabel(LanguageManager.get("product.stock")), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel(LanguageManager.get("product.minStock")), gbc);

        gbc.gridy = 7; gbc.gridx = 0; formPanel.add(txtStock = crearInput(true), gbc);
        gbc.gridx = 1; formPanel.add(txtMinStock = crearInput(true), gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void initControlButtons() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setOpaque(false);

        SoftButton btnCancel = new SoftButton(null);
        btnCancel.setText(LanguageManager.get("product.btn.cancel"));
        btnCancel.setPreferredSize(new Dimension(120, 45));
        btnCancel.setForeground(Color.GRAY);
        btnCancel.addActionListener(e -> dispose());

        SoftButton btnSave = new SoftButton(null);
        btnSave.setText(LanguageManager.get("product.btn.save"));
        btnSave.setPreferredSize(new Dimension(150, 45));
        btnSave.setForeground(COLOR_GOLD);
        btnSave.addActionListener(e -> guardar());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.GRAY);
        l.setFont(FONT_LABEL);
        return l;
    }

    private JTextField crearInput(boolean ed) {
        JTextField t = new JTextField();
        t.setPreferredSize(new Dimension(250, 35));
        t.setBackground(COLOR_INPUT_BG);
        t.setForeground(COLOR_TEXTO);
        t.setCaretColor(COLOR_GOLD);
        t.setFont(FONT_INPUT);
        t.setEditable(ed);
        t.setBorder(new LineBorder(new Color(80, 80, 80)));
        return t;
    }

    private void generarCodigo() {
        if (productToEdit != null) return;
        Category sel = (Category) cmbCategory.getSelectedItem();
        if (sel != null) {
            String prefix = "GEN";
            for (Map.Entry<String, String> entry : categoryPrefixes.entrySet()) {
                if (sel.getName().contains(entry.getKey())) {
                    prefix = entry.getValue();
                    break;
                }
            }
            txtCode.setText(productDAO.generateSmartCode(prefix));
        }
    }

    private void guardar() {
        try {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("product.msg.error.name"));
                return;
            }
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
                JOptionPane.showMessageDialog(this, LanguageManager.get("product.msg.success"));
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("product.msg.error.num"));
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
}