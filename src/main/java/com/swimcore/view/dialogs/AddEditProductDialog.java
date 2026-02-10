/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: AddEditProductDialog.java
 * VERSIÓN: 14.0.0 (Big Icons + Fucsia/Gold Theme)
 * FECHA: Enero 2026
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;
import java.awt.Window;
import javax.swing.SwingUtilities;
import com.swimcore.util.LuxuryMessage;
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
import java.net.URL;
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

    // PALETA LUXURY SICONI
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_VERDE_NEON = new Color(0, 255, 128);
    private final Color COLOR_FUCSIA_NEON = new Color(255, 0, 127); // Fucsia protagonista
    private final Color COLOR_BG_CAPSULE = new Color(25, 25, 25, 240);
    private final Color COLOR_INPUT_BG = new Color(30, 30, 30);

    public AddEditProductDialog(Window parent, Product product) {
        super(parent, "SICONI - CONTROL MAESTRO DE PRODUCTOS", ModalityType.APPLICATION_MODAL);
        this.productToEdit = product;
        setupNomenclature();

        // Tamaño ajustado para evitar cortes
        setSize(1150, 700);
        setLocationRelativeTo(parent);
        setUndecorated(false);

        try {
            JPanel bg = new ImagePanel("/images/bg_registro.png");
            if (bg == null) bg = new ImagePanel("/images/bg2.png");
            bg.setLayout(new BorderLayout());
            bg.setBorder(new LineBorder(COLOR_GOLD, 2));
            setContentPane(bg);
        } catch (Exception e) { getContentPane().setBackground(new Color(10, 10, 10)); }

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
        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 50, 10, 50));

        JLabel lblTitle = new JLabel((productToEdit == null ? "REGISTRO MAESTRO DE PRODUCTO" : "GESTIÓN DE ARTÍCULO").toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(COLOR_GOLD);
        header.add(lblTitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // CONTENEDOR CENTRAL
        JPanel body = new JPanel(new BorderLayout(30, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 40, 40, 20));

        // FORMULARIO
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(COLOR_BG_CAPSULE);
        // Borde dorado sutil
        formGrid.setBorder(new LineBorder(new Color(100, 80, 20), 1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0
        gbc.gridy = 0; gbc.gridx = 0; formGrid.add(crearLabel("CATEGORÍA DE NEGOCIO:"), gbc);
        gbc.gridx = 1; formGrid.add(crearLabel("CÓDIGO SKU (AUTO):"), gbc);

        // Fila 1
        gbc.gridy = 1; gbc.gridx = 0;
        cmbCategory = new JComboBox<>(new String[]{"TRAJES DE BAÑO", "ROPA DEPORTIVA", "INSUMOS", "EQUIPAMIENTO"});
        cmbCategory.setPreferredSize(new Dimension(320, 45));
        cmbCategory.setBackground(COLOR_INPUT_BG);
        cmbCategory.setForeground(Color.WHITE);
        cmbCategory.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cmbCategory.addActionListener(e -> generarCodigo());
        formGrid.add(cmbCategory, gbc);

        gbc.gridx = 1;
        txtCode = crearInput(false, 20, COLOR_GOLD); // Código en Dorado
        formGrid.add(txtCode, gbc);

        // Fila 2
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        formGrid.add(crearLabel("NOMBRE DEL MODELO / ARTÍCULO:"), gbc);

        // Fila 3
        gbc.gridy = 3;
        txtName = crearInput(true, 18, Color.WHITE);
        formGrid.add(txtName, gbc);

        gbc.gridwidth = 1;
        // Fila 4
        gbc.gridy = 4; gbc.gridx = 0; formGrid.add(crearLabel("COSTO OPERATIVO ($):"), gbc);
        gbc.gridx = 1; formGrid.add(crearLabel("P.V.P SUGERIDO ($):"), gbc);

        // Fila 5
        gbc.gridy = 5; gbc.gridx = 0; formGrid.add(txtCostPrice = crearInput(true, 22, Color.WHITE), gbc);
        gbc.gridx = 1; formGrid.add(txtSalePrice = crearInput(true, 22, COLOR_GOLD), gbc);

        // Fila 6
        gbc.gridy = 6; gbc.gridx = 0; formGrid.add(crearLabel("EXISTENCIA ACTUAL:"), gbc);
        gbc.gridx = 1; formGrid.add(crearLabel("ALERTA MÍNIMA (RED ZONE):"), gbc);

        // Fila 7 (Controles numéricos)
        gbc.gridy = 7; gbc.gridx = 0;
        formGrid.add(crearNumericSelector(txtStock = crearInput(true, 32, COLOR_VERDE_NEON), COLOR_VERDE_NEON), gbc);
        gbc.gridx = 1;
        // Alerta en Fucsia/Rojo para destacar
        formGrid.add(crearNumericSelector(txtMinStock = crearInput(true, 32, COLOR_FUCSIA_NEON), COLOR_FUCSIA_NEON), gbc);

        // Fila 8
        gbc.gridwidth = 2;
        gbc.gridy = 8; gbc.gridx = 0; formGrid.add(crearLabel("DESCRIPCIÓN Y ESPECIFICACIONES TÉCNICAS:"), gbc);

        // Fila 9
        gbc.gridy = 9;
        txtDesc = new JTextArea(3, 20);
        txtDesc.setBackground(COLOR_INPUT_BG);
        txtDesc.setForeground(new Color(200,200,200));
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDesc.setLineWrap(true); txtDesc.setWrapStyleWord(true);
        txtDesc.setCaretColor(COLOR_GOLD);

        JScrollPane sp = new JScrollPane(txtDesc);
        sp.setPreferredSize(new Dimension(650, 80));
        sp.setBorder(new LineBorder(new Color(80,80,80)));
        // Focus listener para el borde del scroll
        txtDesc.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { sp.setBorder(new LineBorder(COLOR_GOLD, 1)); }
            public void focusLost(FocusEvent e) { sp.setBorder(new LineBorder(new Color(80,80,80))); }
        });

        formGrid.add(sp, gbc);

        body.add(formGrid, BorderLayout.CENTER);

        // --- SIDEBAR CON ICONOS GIGANTES ---
        JPanel sideButtons = new JPanel(new GridLayout(4, 1, 0, 15));
        sideButtons.setOpaque(false);
        sideButtons.setPreferredSize(new Dimension(200, 0));

        // Botón Registrar (Icono grande)
        SoftButton btnSave = crearBigImgButton((productToEdit == null ? "REGISTRAR" : "ACTUALIZAR"), "btn_save.png", COLOR_VERDE_NEON);
        btnSave.addActionListener(e -> guardar());

        // Botón Limpiar (AHORA EN FUCSIA, como pediste)
        SoftButton btnClear = crearBigImgButton("LIMPIAR", "btn_clean.png", COLOR_FUCSIA_NEON);
        btnClear.addActionListener(e -> resetForm());

        // Botón Eliminar
        SoftButton btnDel = crearBigImgButton("ELIMINAR", "btn_delete.png", new Color(255, 50, 50));
        btnDel.setVisible(productToEdit != null);
        btnDel.addActionListener(e -> eliminar());

        // Botón Volver
        SoftButton btnBack = crearBigImgButton("VOLVER", "btn_back.png", Color.WHITE);
        btnBack.addActionListener(e -> dispose());

        sideButtons.add(btnSave);
        sideButtons.add(btnClear);
        if(productToEdit != null) sideButtons.add(btnDel);
        sideButtons.add(btnBack);

        body.add(sideButtons, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);
    }

    // MÉTODO MAESTRO PARA ICONOS GIGANTES (110px) Y COLOR PERSONALIZADO
    private SoftButton crearBigImgButton(String texto, String imgName, Color textColor) {
        SoftButton btn = new SoftButton(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 30)); // Fondo oscuro
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Redondeado perfecto
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BorderLayout());

        JLabel lIcon = new JLabel();
        lIcon.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            URL url = getClass().getResource("/images/icons/" + imgName);
            if (url != null) {
                // ESCALADO 110x110
                ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH));
                lIcon.setIcon(icon);
            }
        } catch (Exception e) {}

        JLabel lblText = new JLabel(texto, SwingConstants.CENTER);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Usamos el color pasado como argumento (Fucsia para limpiar, Verde para guardar)
        lblText.setForeground(textColor);

        btn.add(lIcon, BorderLayout.CENTER);
        btn.add(lblText, BorderLayout.SOUTH);
        btn.setBorder(new EmptyBorder(5,5,5,5));
        btn.setOpaque(false); // CRUCIAL para evitar el cuadrado
        return btn;
    }

    private JPanel crearNumericSelector(JTextField field, Color color) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);

        // Botones +/- más estilizados
        SoftButton btnMinus = createSmallButton("−", color);
        btnMinus.addActionListener(e -> { try { int v = Integer.parseInt(field.getText()); if(v > 0) field.setText(String.valueOf(v - 1)); } catch(Exception ex) {} });

        SoftButton btnPlus = createSmallButton("+", color);
        btnPlus.addActionListener(e -> { try { int v = Integer.parseInt(field.getText()); field.setText(String.valueOf(v + 1)); } catch(Exception ex) {} });

        p.add(btnMinus, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        p.add(btnPlus, BorderLayout.EAST);
        return p;
    }

    private SoftButton createSmallButton(String text, Color c) {
        SoftButton btn = new SoftButton(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40,40,40));
                g2.fillRoundRect(0,0,getWidth(),getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setText(text);
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        btn.setForeground(c);
        btn.setPreferredSize(new Dimension(50, 45));
        btn.setOpaque(false);
        btn.setBorder(null);
        return btn;
    }

    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(180, 180, 180));
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JTextField crearInput(boolean ed, int size, Color color) {
        JTextField f = new JTextField();
        f.setHorizontalAlignment(JTextField.CENTER);
        f.setBackground(COLOR_INPUT_BG);
        f.setForeground(color);
        f.setCaretColor(COLOR_GOLD);
        f.setFont(new Font("Segoe UI", Font.BOLD, size));
        f.setEditable(ed);
        f.setBorder(new LineBorder(new Color(80,80,80)));
        f.setPreferredSize(new Dimension(0, 45)); // Altura cómoda
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBorder(new LineBorder(COLOR_GOLD, 2)); f.selectAll(); }
            public void focusLost(FocusEvent e) { f.setBorder(new LineBorder(new Color(80,80,80))); }
        });
        return f;
    }

    private void generarCodigo() {
        if (productToEdit != null) return;
        String prefix = categoryPrefixes.getOrDefault(cmbCategory.getSelectedItem(), "GEN");
        txtCode.setText(productDAO.generateSmartCode(prefix));
    }

    private void resetForm() {
        txtName.setText(""); txtDesc.setText(""); txtCostPrice.setText("0.00"); txtSalePrice.setText("0.00"); txtStock.setText("0"); txtMinStock.setText("5"); generarCodigo();
    }
    private void guardar() {
        try {
            if (txtName.getText().trim().isEmpty()) {
                // Usamos tu LuxuryMessage para la validación
                LuxuryMessage.show(this, "VALIDACIÓN", "Debe ingresar el nombre del producto.", true);
                return;
            }

            Product p = (productToEdit == null) ? new Product() : productToEdit;
            p.setCode(txtCode.getText());
            p.setName(txtName.getText().trim());
            p.setDescription(txtDesc.getText().trim());
            p.setCostPrice(Double.parseDouble(txtCostPrice.getText().replace(",", ".")));
            p.setSalePrice(Double.parseDouble(txtSalePrice.getText().replace(",", ".")));
            p.setCurrentStock(Integer.parseInt(txtStock.getText()));
            p.setMinStock(Integer.parseInt(txtMinStock.getText()));
            p.setCategoryId(cmbCategory.getSelectedIndex() + 1);

            if (productDAO.save(p)) {
                SoundManager.getInstance().playClick();

                // --- CORRECCIÓN DE FLUJO ---
                // Capturamos la ventana principal antes de cerrar el formulario
                Window parent = SwingUtilities.getWindowAncestor(this);
                dispose();

                // Mostramos el mensaje de éxito después de cerrar, usando la ventana principal como padre
                LuxuryMessage.show(parent, "SICONI - ÉXITO", "El registro se completó correctamente.", false);
            }
        } catch (Exception ex) {
            // Mensaje de error personalizado en lugar del JOptionPane
            LuxuryMessage.show(this, "ERROR DE DATOS", "Verifique que los precios y stock sean numéricos.", true);
        }
    }
    private void eliminar() {
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar producto?", "SICONI", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (productDAO.delete(productToEdit.getId())) dispose();
        }
    }

    private void llenarCampos() {
        txtCode.setText(productToEdit.getCode()); txtName.setText(productToEdit.getName()); txtDesc.setText(productToEdit.getDescription());
        txtCostPrice.setText(String.format(Locale.US, "%.2f", productToEdit.getCostPrice())); txtSalePrice.setText(String.format(Locale.US, "%.2f", productToEdit.getSalePrice()));
        txtStock.setText(String.valueOf(productToEdit.getCurrentStock())); txtMinStock.setText(String.valueOf(productToEdit.getMinStock()));
        cmbCategory.setSelectedIndex(productToEdit.getCategoryId() - 1);
    }
}