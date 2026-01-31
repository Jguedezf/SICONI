/*
INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
ARCHIVO: AddEditProductDialog.java
VERSIÓN: 12.6.3 (Fix Signo Mas Manual)
DISEÑO: Johanna Guédez | SICONI Professional
*/
package com.swimcore.view.dialogs;

import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Product;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class AddEditProductDialog extends JDialog {

    private final ProductDAO productDAO = new ProductDAO();
    private Product productToEdit = null;
    private final Map<String, String> categoryPrefixes = new HashMap<>();
    private JTextField txtCode, txtName, txtCostPrice, txtSalePrice, txtStock, txtMinStock;
    private JTextArea txtDesc;
    private JComboBox<String> cmbCategory;

    // Colores Luxury SICONI
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_NEON_V = new Color(0, 255, 128);
    private final Color COLOR_NEON_M = new Color(255, 0, 127);
    private final Color COLOR_BG_CAPSULE = new Color(25, 25, 25, 240);

    public AddEditProductDialog(Window parent, Product product) {
        super(parent, "SICONI - PRODUCT MASTER CONTROL", ModalityType.APPLICATION_MODAL);
        this.productToEdit = product;

        setupNomenclature();

        setSize(1150, 750);
        setLocationRelativeTo(parent);
        setUndecorated(true);

        try {
            JPanel bg = new ImagePanel("/images/bg_registro.png");
            if (bg == null) bg = new ImagePanel("/images/bg2.png");
            bg.setLayout(new BorderLayout());
            bg.setBorder(new LineBorder(COLOR_GOLD, 2));
            setContentPane(bg);
        } catch (Exception e) {
            getContentPane().setBackground(new Color(10, 10, 10));
        }

        initUI();

        if (productToEdit != null) llenarCampos();
        else { resetForm(); generarCodigo(); }
    }

    private void setupNomenclature() {
        categoryPrefixes.put("TRAJES DE BAÑO", "SWI");
        categoryPrefixes.put("ROPA DEPORTIVA", "DEP");
        categoryPrefixes.put("INSUMOS", "INS");
        categoryPrefixes.put("EQUIPAMIENTO", "EQU");
    }

    private void initUI() {
        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 50, 10, 50));

        JLabel lblTitle = new JLabel((productToEdit == null ? "REGISTRO MAESTRO DE PRODUCTO" : "GESTIÓN DE ARTÍCULO").toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(COLOR_GOLD);

        JPanel spacer = new JPanel(); spacer.setOpaque(false); spacer.setPreferredSize(new Dimension(50, 45));

        SoftButton btnX = new SoftButton(null);
        btnX.setText("✕");
        btnX.setPreferredSize(new Dimension(50, 45));
        btnX.addActionListener(e -> dispose());

        header.add(spacer, BorderLayout.WEST);
        header.add(lblTitle, BorderLayout.CENTER);
        header.add(btnX, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- PANEL CUERPO ---
        JPanel body = new JPanel(new BorderLayout(40, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 50, 40, 50));

        // A. FORMULARIO
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(COLOR_BG_CAPSULE);
        formGrid.setBorder(new LineBorder(new Color(70,70,70), 1));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0; gbc.gridx = 0; formGrid.add(crearLabel("CATEGORÍA DE NEGOCIO:"), gbc);
        gbc.gridx = 1; formGrid.add(crearLabel("CÓDIGO SKU (AUTO):"), gbc);

        gbc.gridy = 1; gbc.gridx = 0;
        cmbCategory = new JComboBox<>(new String[]{"TRAJES DE BAÑO", "ROPA DEPORTIVA", "INSUMOS", "EQUIPAMIENTO"});
        cmbCategory.setPreferredSize(new Dimension(320, 45));
        cmbCategory.setBackground(new Color(15,15,15));
        cmbCategory.setForeground(Color.WHITE);
        cmbCategory.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cmbCategory.addActionListener(e -> generarCodigo());
        formGrid.add(cmbCategory, gbc);

        gbc.gridx = 1;
        txtCode = crearInput(false, 18, COLOR_GOLD);
        formGrid.add(txtCode, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        formGrid.add(crearLabel("NOMBRE DEL MODELO / ARTÍCULO:"), gbc);
        gbc.gridy = 3;
        txtName = crearInput(true, 18, Color.WHITE);
        formGrid.add(txtName, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 4; gbc.gridx = 0; formGrid.add(crearLabel("COSTO OPERATIVO ($):"), gbc);
        gbc.gridx = 1; formGrid.add(crearLabel("P.V.P SUGERIDO ($):"), gbc);
        gbc.gridy = 5; gbc.gridx = 0; formGrid.add(txtCostPrice = crearInput(true, 22, Color.WHITE), gbc);
        gbc.gridx = 1; formGrid.add(txtSalePrice = crearInput(true, 22, COLOR_GOLD), gbc);

        gbc.gridy = 6; gbc.gridx = 0; formGrid.add(crearLabel("EXISTENCIA ACTUAL:"), gbc);
        gbc.gridx = 1; formGrid.add(crearLabel("ALERTA MÍNIMA (RED ZONE):"), gbc);

        gbc.gridy = 7; gbc.gridx = 0;
        formGrid.add(crearNumericSelector(txtStock = crearInput(true, 40, COLOR_NEON_V), COLOR_NEON_V), gbc);
        gbc.gridx = 1;
        formGrid.add(crearNumericSelector(txtMinStock = crearInput(true, 40, new Color(255, 180, 0)), new Color(255, 180, 0)), gbc);

        gbc.gridwidth = 2;
        gbc.gridy = 8; gbc.gridx = 0; formGrid.add(crearLabel("DESCRIPCIÓN Y ESPECIFICACIONES TÉCNICAS:"), gbc);
        gbc.gridy = 9;
        txtDesc = new JTextArea(4, 20);
        txtDesc.setBackground(new Color(10,10,10));
        txtDesc.setForeground(new Color(200,200,200));
        txtDesc.setLineWrap(true); txtDesc.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(txtDesc); sp.setPreferredSize(new Dimension(650, 90));
        sp.setBorder(new LineBorder(new Color(60,60,60)));
        formGrid.add(sp, gbc);

        body.add(formGrid, BorderLayout.CENTER);

        // --- BARRA LATERAL CON ICONOS CORREGIDOS ---
        JPanel sideButtons = new JPanel(new GridLayout(4, 1, 0, 20));
        sideButtons.setOpaque(false);
        sideButtons.setPreferredSize(new Dimension(220, 0));

        // Botones construidos con el método de "Icono Arriba"
        SoftButton btnSave = crearBotonLateral((productToEdit == null ? "✚" : "💾"), (productToEdit == null ? "REGISTRAR" : "ACTUALIZAR"), COLOR_NEON_V);
        btnSave.addActionListener(e -> guardar());

        SoftButton btnClear = crearBotonLateral("🔄", "LIMPIAR", Color.WHITE);
        btnClear.addActionListener(e -> resetForm());

        SoftButton btnDel = crearBotonLateral("🗑", "ELIMINAR", new Color(255, 80, 80));
        btnDel.setVisible(productToEdit != null);
        btnDel.addActionListener(e -> eliminar());

        SoftButton btnBack = crearBotonLateral("⬅", "VOLVER", COLOR_GOLD);
        btnBack.addActionListener(e -> dispose());

        sideButtons.add(btnSave);
        sideButtons.add(btnClear);
        if(productToEdit != null) sideButtons.add(btnDel);
        sideButtons.add(btnBack);

        body.add(sideButtons, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);
    }

    // MÉTODO MODIFICADO: Dibuja el signo + manualmente si detecta el símbolo "✚"
    private SoftButton crearBotonLateral(String icono, String texto, Color color) {
        SoftButton btn = new SoftButton(null);
        btn.setLayout(new BorderLayout());

        // SOLUCIÓN: Si es el símbolo de registrar, lo dibujamos manualmente
        if (icono.equals("✚")) {
            JPanel iconPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);

                    int w = getWidth();
                    int h = getHeight();
                    int thickness = 4; // Grosor del signo
                    int size = 22;     // Tamaño del signo (Largo)

                    // Línea Horizontal
                    g2.fillRect((w - size) / 2, (h - thickness) / 2, size, thickness);
                    // Línea Vertical
                    g2.fillRect((w - thickness) / 2, (h - size) / 2, thickness, size);
                }
            };
            iconPanel.setOpaque(false);
            btn.add(iconPanel, BorderLayout.CENTER);
        } else {
            // Para el resto de iconos (Limpiar, Volver, etc) usamos la fuente normal
            JLabel lblIcon = new JLabel(icono, SwingConstants.CENTER);
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            lblIcon.setForeground(color);
            btn.add(lblIcon, BorderLayout.CENTER);
        }

        JLabel lblText = new JLabel(texto, SwingConstants.CENTER);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblText.setForeground(color);

        btn.add(lblText, BorderLayout.SOUTH);
        btn.setBorder(new EmptyBorder(10,10,10,10));

        return btn;
    }

    private JPanel crearNumericSelector(JTextField field, Color color) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        SoftButton btnMinus = new SoftButton(null);
        btnMinus.setText("−"); btnMinus.setFont(new Font("Arial", Font.BOLD, 25));
        btnMinus.setPreferredSize(new Dimension(50, 50)); btnMinus.setForeground(COLOR_NEON_M);
        btnMinus.addActionListener(e -> {
            try { int v = Integer.parseInt(field.getText()); if(v > 0) field.setText(String.valueOf(v - 1)); } catch(Exception ex) {}
        });

        SoftButton btnPlus = new SoftButton(null);
        btnPlus.setText("+"); btnPlus.setFont(new Font("Arial", Font.BOLD, 25));
        btnPlus.setPreferredSize(new Dimension(50, 50)); btnPlus.setForeground(COLOR_NEON_V);
        btnPlus.addActionListener(e -> {
            try { int v = Integer.parseInt(field.getText()); field.setText(String.valueOf(v + 1)); } catch(Exception ex) {}
        });

        p.add(btnMinus, BorderLayout.WEST); p.add(field, BorderLayout.CENTER); p.add(btnPlus, BorderLayout.EAST);
        return p;
    }

    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(180, 180, 180)); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JTextField crearInput(boolean ed, int size, Color color) {
        JTextField f = new JTextField();
        f.setHorizontalAlignment(JTextField.CENTER); f.setBackground(new Color(10, 10, 10));
        f.setForeground(color); f.setCaretColor(COLOR_GOLD);
        f.setFont(new Font("Segoe UI", Font.BOLD, size)); f.setEditable(ed);
        f.setBorder(new LineBorder(new Color(70,70,70)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBorder(new LineBorder(COLOR_GOLD, 1)); f.selectAll(); }
            public void focusLost(FocusEvent e) { f.setBorder(new LineBorder(new Color(70,70,70))); }
        });
        return f;
    }

    private void generarCodigo() {
        if (productToEdit != null) return;
        String prefix = categoryPrefixes.getOrDefault(cmbCategory.getSelectedItem(), "GEN");
        txtCode.setText(productDAO.generateSmartCode(prefix));
    }

    private void resetForm() {
        txtName.setText(""); txtDesc.setText("");
        txtCostPrice.setText("0.00"); txtSalePrice.setText("0.00");
        txtStock.setText("0"); txtMinStock.setText("5");
        generarCodigo();
    }

    private void guardar() {
        try {
            if (txtName.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Debe ingresar el nombre."); return; }
            Product p = (productToEdit == null) ? new Product() : productToEdit;
            p.setCode(txtCode.getText());
            p.setName(txtName.getText().trim());
            p.setDescription(txtDesc.getText().trim());
            p.setCostPrice(Double.parseDouble(txtCostPrice.getText().replace(",", ".")));
            p.setSalePrice(Double.parseDouble(txtSalePrice.getText().replace(",", ".")));
            p.setCurrentStock(Integer.parseInt(txtStock.getText()));
            p.setMinStock(Integer.parseInt(txtMinStock.getText()));
            p.setCategoryId(cmbCategory.getSelectedIndex() + 1);
            if (productDAO.save(p)) { SoundManager.getInstance().playClick(); dispose(); }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error de datos."); }
    }

    private void eliminar() {
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar producto?", "SICONI", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (productDAO.delete(productToEdit.getId())) dispose();
        }
    }

    private void llenarCampos() {
        txtCode.setText(productToEdit.getCode());
        txtName.setText(productToEdit.getName());
        txtDesc.setText(productToEdit.getDescription());
        txtCostPrice.setText(String.format(Locale.US, "%.2f", productToEdit.getCostPrice()));
        txtSalePrice.setText(String.format(Locale.US, "%.2f", productToEdit.getSalePrice()));
        txtStock.setText(String.valueOf(productToEdit.getCurrentStock()));
        txtMinStock.setText(String.valueOf(productToEdit.getMinStock()));
        cmbCategory.setSelectedIndex(productToEdit.getCategoryId() - 1);
    }
}