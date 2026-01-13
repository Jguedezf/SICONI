package com.swimcore.view.dialogs;

import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Category;
import com.swimcore.model.Product;
import com.swimcore.view.InventoryView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    // Colores
    private final Color COLOR_BG = new Color(20, 20, 20);
    private final Color COLOR_INPUT = new Color(40, 40, 40);
    private final Color COLOR_TEXTO = new Color(240, 240, 240);
    private final Color COLOR_VERDE = new Color(0, 255, 128);

    public AddEditProductDialog(InventoryView parent, Product product) {
        super(parent, product == null ? "Registrar Nuevo Ítem" : "Editar Ítem", true);
        this.productToEdit = product;

        // Prefijos Inteligentes
        categoryPrefixes.put("Modelos Referencia", "MOD");
        categoryPrefixes.put("Textiles", "MAT");
        categoryPrefixes.put("Mercería", "MER");
        categoryPrefixes.put("Branding", "BRA");
        categoryPrefixes.put("Equipos Taller", "ACT");
        categoryPrefixes.put("Accesorios", "REV");

        setSize(950, 680);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // Header
        String titulo = (product == null) ? "REGISTRAR NUEVO ÍTEM" : "EDITAR: " + product.getCode();
        JLabel lblTitle = new JLabel(titulo, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(255, 140, 0));
        lblTitle.setBorder(new EmptyBorder(25, 0, 25, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 50, 0, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        // Fila 1
        gbc.gridy = 0; gbc.gridx = 0; formPanel.add(crearLabel("CATEGORÍA (Define Código)"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("CÓDIGO (Automático)"), gbc);

        gbc.gridy = 1; gbc.gridx = 0;
        cmbCategory = new JComboBox<>();
        cargarCategorias();
        cmbCategory.addActionListener(e -> generarCodigo());
        cmbCategory.setPreferredSize(new Dimension(200, 40));
        formPanel.add(cmbCategory, gbc);

        gbc.gridx = 1;
        txtCode = crearInput(false);
        txtCode.setForeground(COLOR_VERDE);
        txtCode.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(txtCode, gbc);

        // Fila 2
        gbc.gridy = 2; gbc.gridx = 0; formPanel.add(crearLabel("NOMBRE"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("DESCRIPCIÓN"), gbc);

        gbc.gridy = 3; gbc.gridx = 0; formPanel.add(txtName = crearInput(true), gbc);

        gbc.gridx = 1;
        txtDesc = new JTextArea(2, 20);
        txtDesc.setBackground(COLOR_INPUT); txtDesc.setForeground(COLOR_TEXTO);
        txtDesc.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        formPanel.add(new JScrollPane(txtDesc), gbc);

        // Fila 3
        gbc.gridy = 4; gbc.gridx = 0; formPanel.add(crearLabel("COSTO (Ref. Divisa)"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("VENTA (Ref. Divisa)"), gbc);

        gbc.gridy = 5; gbc.gridx = 0; formPanel.add(txtCostPrice = crearInput(true), gbc);
        gbc.gridx = 1; formPanel.add(txtSalePrice = crearInput(true), gbc);

        // Fila 4
        gbc.gridy = 6; gbc.gridx = 0; formPanel.add(crearLabel("STOCK"), gbc);
        gbc.gridx = 1; formPanel.add(crearLabel("ALERTA MÍNIMA"), gbc);

        gbc.gridy = 7; gbc.gridx = 0; formPanel.add(txtStock = crearInput(true), gbc);
        gbc.gridx = 1; formPanel.add(txtMinStock = crearInput(true), gbc);

        add(formPanel, BorderLayout.CENTER);

        // Botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 30));
        btnPanel.setOpaque(false);
        btnPanel.add(crearBotonPastilla("LIMPIAR", Color.GRAY, e -> limpiar()));
        btnPanel.add(crearBotonPastilla("CANCELAR", new Color(255, 60, 60), e -> dispose()));
        btnPanel.add(crearBotonPastilla("GUARDAR", new Color(220, 0, 115), e -> guardar()));
        add(btnPanel, BorderLayout.SOUTH);

        if (productToEdit != null) llenarCampos();
        else { txtStock.setText("0"); txtMinStock.setText("2"); generarCodigo(); }
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
            p.setCostPrice(Double.parseDouble(txtCostPrice.getText()));
            p.setSalePrice(Double.parseDouble(txtSalePrice.getText()));
            p.setCurrentStock(Integer.parseInt(txtStock.getText()));
            p.setMinStock(Integer.parseInt(txtMinStock.getText()));
            p.setCategoryId(cmbCategory.getItemAt(cmbCategory.getSelectedIndex()).getId());

            if (productDAO.save(p)) {
                JOptionPane.showMessageDialog(this, "Guardado con éxito");
                dispose();
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error en datos"); }
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
        txtStock.setText("0"); txtMinStock.setText("2"); generarCodigo();
    }

    private void cargarCategorias() {
        for(Category c : productDAO.getAllCategories()) cmbCategory.addItem(c);
    }

    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t); l.setForeground(Color.GRAY); return l;
    }

    private JTextField crearInput(boolean ed) {
        JTextField t = new JTextField(); t.setPreferredSize(new Dimension(200, 40));
        t.setBackground(COLOR_INPUT); t.setForeground(COLOR_TEXTO); t.setEditable(ed);
        return t;
    }

    private JButton crearBotonPastilla(String t, Color c, ActionListener a) {
        JButton b = new JButton(t) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0,0,getWidth(),getHeight(),40,40);
                super.paintComponent(g);
            }
        };
        b.setPreferredSize(new Dimension(140, 45)); b.setBackground(c); b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false); b.setFocusPainted(false); b.setBorderPainted(false);
        b.addActionListener(a);
        return b;
    }
}