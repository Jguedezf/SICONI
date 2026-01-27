/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: SalesView.java
 * VERSIÓN: 12.1.1 (Final Variable Hotfix)
 * DESCRIPCIÓN: Interfaz Flexible para Taller de Confección.
 * - Corregido error de compilación "cannot assign a value to final variable".
 * - Estilo visual unificado "Dark/Gold".
 * - Sugerencia automática del 50% de abono.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.controller.SaleController;
import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Client;
import com.swimcore.model.Product;
import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SalesView extends JPanel {

    private final Client currentClient;
    private final SaleController saleController;
    private final ProductDAO productDAO;
    private final List<Product> productList;
    private final DefaultTableModel tableModel; // Ahora se inicializa en el constructor
    private final List<SaleDetail> cartDetails = new ArrayList<>();

    // UI
    private JComboBox<String> cmbProducts;
    private JTextField txtPrice; // EDITABLE
    private JTextField txtQuantity;
    private JComboBox<String> cmbSize;
    private JTable table;
    private JTextField txtAbono;
    private JLabel lblTotal, lblResta;
    private JTextField txtDatePromised;
    private JComboBox<String> cmbPayMethod;
    private JTextField txtRef;
    private JTextArea txtObservations;

    private double totalAmount = 0.0;

    // --- PALETA DE COLORES LUXURY ---
    private final Color COLOR_BG = new Color(30, 30, 30);
    private final Color COLOR_HEADER = new Color(20, 20, 20);
    private final Color COLOR_PANEL_RIGHT = new Color(45, 45, 45);
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_INPUT_BG = new Color(45, 45, 45);

    public SalesView(Client client) {
        this.currentClient = client;
        this.saleController = new SaleController();
        this.productDAO = new ProductDAO();
        this.productList = productDAO.getAllProducts();

        // --- CORRECCIÓN: INICIALIZAR EL tableModel AQUÍ ---
        String[] cols = {"Producto", "Talla", "Cant", "Precio Unit.", "Subtotal"};
        this.tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        // --------------------------------------------------

        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(COLOR_HEADER);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel l1 = new JLabel("CLIENTE: " + currentClient.getFullName().toUpperCase());
        l1.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l1.setForeground(COLOR_GOLD);

        JLabel l2 = new JLabel("ATLETA: " + currentClient.getAthleteName() + "  |  CLUB: " + currentClient.getClub());
        l2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l2.setForeground(Color.WHITE);

        header.add(l1);
        header.add(l2);
        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.BOTH;

        // --- Panel para agregar prendas ---
        JPanel pnlAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlAdd.setOpaque(false);
        pnlAdd.setBorder(createTitledBorder("AGREGAR PRENDA"));

        cmbProducts = new JComboBox<>();
        styleComboBox(cmbProducts, new Dimension(250, 35));
        cmbProducts.addItem("- SELECCIONAR PRENDA -");
        for (Product p : productList) cmbProducts.addItem(p.getName());
        cmbProducts.addActionListener(e -> {
            int idx = cmbProducts.getSelectedIndex();
            if (idx > 0) {
                Product p = productList.get(idx - 1);
                txtPrice.setText(String.format("%.2f", p.getSalePrice()));
            }
        });

        txtPrice = createTextField(6);
        txtQuantity = createTextField(3);
        txtQuantity.setText("1");
        cmbSize = new JComboBox<>(new String[]{"S", "M", "L", "XL", "UNICA", "A MEDIDA"});
        styleComboBox(cmbSize, new Dimension(100, 35));

        JButton btnAdd = createStyledButton("AGREGAR", new Color(0, 150, 0));
        btnAdd.addActionListener(e -> addToCart());

        pnlAdd.add(new JLabel("Prenda:")); pnlAdd.add(cmbProducts);
        pnlAdd.add(new JLabel("Talla:")); pnlAdd.add(cmbSize);
        pnlAdd.add(new JLabel("Precio $:")); pnlAdd.add(txtPrice);
        pnlAdd.add(new JLabel("Cant:")); pnlAdd.add(txtQuantity);
        pnlAdd.add(btnAdd);

        g.gridx = 0; g.gridy = 0; g.weightx = 1.0; g.weighty = 0.0;
        center.add(pnlAdd, g);

        // --- Tabla de productos del pedido ---
        table = new JTable(tableModel); // Se usa el tableModel ya inicializado
        styleTable(table);
        g.gridy = 1; g.weighty = 1.0;
        center.add(new JScrollPane(table), g);

        // --- Panel de observaciones y medidas ---
        JPanel pnlObs = new JPanel(new BorderLayout());
        pnlObs.setOpaque(false);
        pnlObs.setBorder(createTitledBorder("MEDIDAS / AJUSTES"));
        txtObservations = createTextArea(4);
        txtObservations.setText("CINTURA:\nCADERA:\nBUSTO:\nOBSERVACIONES:");
        pnlObs.add(new JScrollPane(txtObservations), BorderLayout.CENTER);
        g.gridy = 2; g.weighty = 0.3;
        center.add(pnlObs, g);

        return center;
    }

    private JPanel createRightPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setPreferredSize(new Dimension(320, 0));
        p.setBackground(COLOR_PANEL_RIGHT);
        p.setBorder(new EmptyBorder(10, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(10, 0, 10, 0); g.gridx = 0; g.gridy = GridBagConstraints.RELATIVE;

        lblTotal = new JLabel("TOTAL: $ 0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTotal.setForeground(Color.GREEN);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(lblTotal, g);

        p.add(createLabel("ABONO ($):"));
        txtAbono = createTextField(10);
        txtAbono.setText("0.00");
        txtAbono.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtAbono.setHorizontalAlignment(SwingConstants.RIGHT);
        txtAbono.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { updateCalculations(); }
        });
        p.add(txtAbono, g);

        lblResta = new JLabel("RESTA: $ 0.00");
        lblResta.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblResta.setForeground(Color.RED);
        lblResta.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(lblResta, g);

        p.add(new JSeparator(), g);

        p.add(createLabel("FECHA ENTREGA:"));
        txtDatePromised = createTextField(10);
        txtDatePromised.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        p.add(txtDatePromised, g);

        p.add(createLabel("MÉTODO PAGO:"));
        cmbPayMethod = new JComboBox<>(new String[]{"PAGO MÓVIL", "EFECTIVO", "ZELLE", "TRANSFERENCIA"});
        styleComboBox(cmbPayMethod, null);
        p.add(cmbPayMethod, g);

        p.add(createLabel("REFERENCIA:"));
        txtRef = createTextField(10);
        p.add(txtRef, g);

        g.weighty = 1.0; g.anchor = GridBagConstraints.SOUTH;
        JButton btnSave = createStyledButton("REGISTRAR PEDIDO", COLOR_GOLD);
        btnSave.setPreferredSize(new Dimension(0, 60));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnSave.addActionListener(e -> saveOrder());
        p.add(btnSave, g);

        return p;
    }

    private void addToCart() {
        if (cmbProducts.getSelectedIndex() <= 0) return;
        try {
            String prodName = (String) cmbProducts.getSelectedItem();
            Product selectedProduct = productList.stream().filter(p -> p.getName().equals(prodName)).findFirst().orElse(null);
            if (selectedProduct == null) return;

            double price = Double.parseDouble(txtPrice.getText().replace(",","."));
            int qty = Integer.parseInt(txtQuantity.getText());
            String size = (String) cmbSize.getSelectedItem();
            double sub = price * qty;

            tableModel.addRow(new Object[]{prodName, size, qty, String.format("%.2f", price), String.format("%.2f", sub)});

            SaleDetail det = new SaleDetail("0", String.valueOf(selectedProduct.getId()), prodName + " [" + size + "]", qty, price);
            cartDetails.add(det);

            totalAmount += sub;
            updateCalculations();
            txtAbono.setText(String.format("%.2f", totalAmount / 2)); // Sugiere el 50%
            updateCalculations();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: Verifique que el precio y la cantidad sean números válidos.", "Dato Inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCalculations() {
        lblTotal.setText(String.format("TOTAL: $ %.2f", totalAmount));
        try {
            double abono = Double.parseDouble(txtAbono.getText().replace(",", "."));
            double resta = totalAmount - abono;
            lblResta.setText(String.format("RESTA: $ %.2f", resta));
        } catch (NumberFormatException e) {
            lblResta.setText("RESTA: $ --.--");
        }
    }

    private void saveOrder() {
        if (cartDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El pedido está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double abono = Double.parseDouble(txtAbono.getText().replace(",", "."));
            double resta = totalAmount - abono;
            String status = (resta <= 0.01) ? "PAGADO / EN PRODUCCIÓN" : "ABONADO / EN PRODUCCIÓN";

            Sale sale = new Sale(
                    "PED-" + System.currentTimeMillis(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    String.valueOf(currentClient.getId()),
                    totalAmount, abono, CurrencyManager.getTasa(),
                    (String) cmbPayMethod.getSelectedItem(),
                    txtRef.getText(),
                    status,
                    txtObservations.getText()
            );
            sale.setDeliveryDate(txtDatePromised.getText());
            sale.setBalanceDue(resta);

            if (saleController.registerSale(sale, cartDetails)) {
                SoundManager.getInstance().playClick();
                JOptionPane.showMessageDialog(this, "✅ PEDIDO REGISTRADO EXITOSAMENTE\nEstado: " + status, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                SwingUtilities.getWindowAncestor(this).dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos.", "Error de Persistencia", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: El monto del abono no es un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- MÉTODOS DE ESTILO Y HELPERS ---

    private TitledBorder createTitledBorder(String title) {
        TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), " " + title + " ");
        b.setTitleColor(Color.WHITE);
        b.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }

    private JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setBackground(COLOR_INPUT_BG);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(COLOR_GOLD);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                new EmptyBorder(5, 5, 5, 5)
        ));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return tf;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 20);
        ta.setBackground(COLOR_INPUT_BG);
        ta.setForeground(Color.WHITE);
        ta.setCaretColor(COLOR_GOLD);
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                new EmptyBorder(5, 5, 5, 5)
        ));
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    private void styleComboBox(JComboBox<String> cb, Dimension d) {
        cb.setBackground(COLOR_INPUT_BG);
        cb.setForeground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (d != null) cb.setPreferredSize(d);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(bgColor.equals(COLOR_GOLD) ? Color.BLACK : Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setBackground(COLOR_BG);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(80, 80, 80));
        table.getTableHeader().setBackground(Color.BLACK);
        table.getTableHeader().setForeground(COLOR_GOLD);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
}