/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: SalesView.java
 * VERSIÓN: 12.0.0 (Emergency Rescue Edition)
 * DESCRIPCIÓN: Interfaz Flexible para Atelier.
 * - Precio Editable manualmente.
 * - Muestra datos del Atleta y Club en cabecera.
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SalesView extends JPanel {

    private final Client currentClient;
    private final SaleController saleController;
    private final ProductDAO productDAO;
    private final List<Product> productList;
    private final DefaultTableModel tableModel;
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

    public SalesView(Client client) {
        this.currentClient = client;
        this.saleController = new SaleController();
        this.productDAO = new ProductDAO();
        this.productList = productDAO.getAllProducts();

        setLayout(new BorderLayout());
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(30, 30, 30));
        add(main, BorderLayout.CENTER);

        // 1. DATOS ATLETA (CABECERA)
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(20, 20, 20));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel l1 = new JLabel("CLIENTE: " + currentClient.getFullName().toUpperCase());
        l1.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l1.setForeground(new Color(212, 175, 55));

        JLabel l2 = new JLabel("ATLETA: " + currentClient.getAthleteName() + "  |  CLUB: " + currentClient.getClub());
        l2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l2.setForeground(Color.WHITE);

        header.add(l1); header.add(l2);
        main.add(header, BorderLayout.NORTH);

        // 2. CENTRO (Agregar y Tabla)
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10); g.fill = GridBagConstraints.BOTH;

        // Panel Agregar
        JPanel pnlAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlAdd.setOpaque(false);
        pnlAdd.setBorder(createBorder("AGREGAR PRENDA"));

        cmbProducts = new JComboBox<>();
        cmbProducts.setPreferredSize(new Dimension(250, 30));
        cmbProducts.addItem("- SELECCIONAR PRENDA -");
        for(Product p : productList) cmbProducts.addItem(p.getName());

        // Al seleccionar, busca el precio sugerido pero DEJA EDITAR
        cmbProducts.addActionListener(e -> {
            int idx = cmbProducts.getSelectedIndex();
            if(idx > 0) {
                Product p = productList.get(idx - 1);
                txtPrice.setText(String.valueOf(p.getSalePrice()));
            }
        });

        txtPrice = new JTextField(6);
        txtPrice.setFont(new Font("Consolas", Font.BOLD, 14));
        txtQuantity = new JTextField("1", 3);
        cmbSize = new JComboBox<>(new String[]{"S", "M", "L", "XL", "UNICA", "A MEDIDA"});

        JButton btnAdd = new JButton("AGREGAR");
        btnAdd.setBackground(new Color(0, 150, 0));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addToCart());

        pnlAdd.add(new JLabel("Prenda:")); pnlAdd.add(cmbProducts);
        pnlAdd.add(new JLabel("Talla:")); pnlAdd.add(cmbSize);
        pnlAdd.add(new JLabel("Precio $:")); pnlAdd.add(txtPrice);
        pnlAdd.add(new JLabel("Cant:")); pnlAdd.add(txtQuantity);
        pnlAdd.add(btnAdd);

        g.gridx=0; g.gridy=0; g.weightx=1.0; g.weighty=0.0;
        center.add(pnlAdd, g);

        // Tabla
        String[] cols = {"Producto", "Talla", "Cant", "Precio Unit.", "Subtotal"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);
        g.gridy=1; g.weighty=1.0;
        center.add(new JScrollPane(table), g);

        // Observaciones
        JPanel pnlObs = new JPanel(new BorderLayout());
        pnlObs.setOpaque(false);
        pnlObs.setBorder(createBorder("MEDIDAS / AJUSTES"));
        txtObservations = new JTextArea(4, 20);
        txtObservations.setText("CINTURA:\nCADERA:\nBUSTO:\nOBSERVACIONES:");
        pnlObs.add(new JScrollPane(txtObservations));
        g.gridy=2; g.weighty=0.3;
        center.add(pnlObs, g);

        main.add(center, BorderLayout.CENTER);

        // 3. DERECHA (Totales)
        main.add(createRightPanel(), BorderLayout.EAST);
    }

    private JPanel createRightPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setPreferredSize(new Dimension(320, 0));
        p.setBackground(new Color(45, 45, 45));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(10, 0, 10, 0); g.gridx=0; g.gridy=0;

        lblTotal = new JLabel("TOTAL: $ 0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTotal.setForeground(Color.GREEN);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(lblTotal, g);

        g.gridy++;
        p.add(new JLabel("ABONO ($):"), g);
        txtAbono = new JTextField("0.00");
        txtAbono.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtAbono.setHorizontalAlignment(SwingConstants.RIGHT);
        txtAbono.addActionListener(e -> updateCalculations());
        p.add(txtAbono, g);

        g.gridy++;
        lblResta = new JLabel("RESTA: $ 0.00");
        lblResta.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblResta.setForeground(Color.RED);
        lblResta.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(lblResta, g);

        g.gridy++;
        p.add(new JSeparator(), g);

        g.gridy++;
        p.add(new JLabel("FECHA ENTREGA:"), g);
        txtDatePromised = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        p.add(txtDatePromised, g);

        g.gridy++;
        p.add(new JLabel("MÉTODO PAGO:"), g);
        cmbPayMethod = new JComboBox<>(new String[]{"PAGO MÓVIL", "EFECTIVO", "ZELLE", "TRANSFERENCIA"});
        p.add(cmbPayMethod, g);

        g.gridy++;
        p.add(new JLabel("REFERENCIA:"), g);
        txtRef = new JTextField();
        p.add(txtRef, g);

        g.gridy++; g.weighty=1.0; g.anchor = GridBagConstraints.SOUTH;
        JButton btnSave = new JButton("REGISTRAR PEDIDO");
        btnSave.setBackground(new Color(212, 175, 55));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnSave.setPreferredSize(new Dimension(0, 60));
        btnSave.addActionListener(e -> saveOrder());
        p.add(btnSave, g);

        return p;
    }

    private void addToCart() {
        if(cmbProducts.getSelectedIndex() <= 0) return;
        try {
            String prodName = (String) cmbProducts.getSelectedItem();
            // Buscar ID real (simple lookup)
            String prodId = "0";
            for(Product p : productList) if(p.getName().equals(prodName)) prodId = String.valueOf(p.getId());

            double price = Double.parseDouble(txtPrice.getText()); // Precio manual
            int qty = Integer.parseInt(txtQuantity.getText());
            String size = (String) cmbSize.getSelectedItem();
            double sub = price * qty;

            tableModel.addRow(new Object[]{prodName, size, qty, price, sub});

            SaleDetail det = new SaleDetail("0", prodId, prodName + " ["+size+"]", qty, price);
            cartDetails.add(det);

            totalAmount += sub;
            updateCalculations();
        } catch(Exception e) { JOptionPane.showMessageDialog(this, "Revise el precio."); }
    }

    private void updateCalculations() {
        lblTotal.setText(String.format("TOTAL: $ %.2f", totalAmount));
        try {
            double abono = Double.parseDouble(txtAbono.getText().replace(",", "."));
            double resta = totalAmount - abono;
            lblResta.setText(String.format("RESTA: $ %.2f", resta));
        } catch(Exception e) {}
    }

    private void saveOrder() {
        if(cartDetails.isEmpty()) { JOptionPane.showMessageDialog(this, "Carrito vacío"); return; }
        try {
            double abono = Double.parseDouble(txtAbono.getText().replace(",", "."));
            double resta = totalAmount - abono;
            String status = (resta <= 0.1) ? "PAGADO / EN PRODUCCIÓN" : "ABONADO / EN PRODUCCIÓN";

            Sale sale = new Sale(
                    "PED-" + System.currentTimeMillis(),
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                    String.valueOf(currentClient.getId()),
                    totalAmount, abono, CurrencyManager.getTasa(),
                    (String) cmbPayMethod.getSelectedItem(),
                    txtRef.getText(),
                    status,
                    txtObservations.getText()
            );
            sale.setDeliveryDate(txtDatePromised.getText());
            sale.setBalanceDue(resta);

            if(saleController.registerSale(sale, cartDetails)) {
                try{SoundManager.getInstance().playClick();}catch(Exception ex){}
                JOptionPane.showMessageDialog(this, "✅ PEDIDO REGISTRADO\nEstado: " + status);
                SwingUtilities.getWindowAncestor(this).dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar.");
            }
        } catch(Exception e) { JOptionPane.showMessageDialog(this, "Error numérico en el pago."); }
    }

    private TitledBorder createBorder(String t) {
        TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), t);
        b.setTitleColor(Color.WHITE);
        return b;
    }
}