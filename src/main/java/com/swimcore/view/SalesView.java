/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: SalesView.java
 * VERSIÓN: 63.0 (RIGHT PANEL OVERHAUL: Better Layout, Bigger Notes, Styled % Buttons)
 * FECHA: February 2026
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePickerSettings.DateArea;
import com.swimcore.controller.SaleController;
import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Client;
import com.swimcore.model.Product;
import com.swimcore.model.Sale;
import com.swimcore.model.SaleDetail;
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.LuxuryMessage;
import com.swimcore.view.components.SoftButton;
import com.swimcore.view.dialogs.CurrencySettingsDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesView extends JPanel {

    // --- VARIABLES DE CLASE ---
    private final Client currentClient;
    private final SaleController saleController;
    private final ProductDAO productDAO;
    private final List<Product> productList;
    private final DefaultTableModel tableModel;
    private final List<SaleDetail> cartDetails = new ArrayList<>();

    private JComboBox<String> cmbProducts, cmbSize, cmbPayMethod, cmbBank;
    private JTextField txtProductCode, txtPrice, txtQuantity, txtAbono, txtRef, txtDiscount;
    private JTextField txtInvoice, txtControl;
    private JCheckBox chkInvoice;
    private DatePicker dateDelivery, datePayment;
    private JTable table;
    private JTextArea txtObservations;

    private JLabel lblTotal, lblTotalBs, lblAbonado, lblAbonadoBs, lblResta, lblRestaBs;
    private SoftButton btnTasa, btnPay50, btnPay100;
    private SoftButton btnReceipt;
    private JLabel lblOrderNum;

    private double totalAmount = 0.0;
    private double currentTasa = CurrencyManager.getTasa();
    private String lastOrderIdSaved = null;

    // COLORES LUXURY
    private static final Color COLOR_GOLD = new Color(212, 175, 55);
    private static final Color COLOR_NEON = new Color(57, 255, 20);
    private static final Color COLOR_INPUT_BG = new Color(45, 45, 45);
    private static final Color COLOR_VIP_DOT = new Color(0, 255, 0);

    private final Font FONT_BOLD_INPUT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_BOLD_TABLE = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_OLED_USD = new Font("Consolas", Font.BOLD, 24);
    private final Font FONT_OLED_BS = new Font("Consolas", Font.BOLD, 13);

    // --- CONSTRUCTOR ---
    public SalesView(Client client) {
        this.currentClient = client;
        this.saleController = new SaleController();
        this.productDAO = new ProductDAO();
        this.productList = productDAO.getAllProducts();

        setOpaque(false);
        setLayout(new BorderLayout(15, 5));
        setBorder(new EmptyBorder(10, 15, 20, 15));

        tableModel = new DefaultTableModel(new String[]{"PRODUCTO", "TALLA", "CANTIDAD", "PRECIO", "TOTAL"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        add(createHeaderStack(), BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new GridLayout(1, 2, 20, 0));
        mainSplit.setOpaque(false);
        mainSplit.add(createLeftPanel());
        mainSplit.add(createRightPanel());

        add(mainSplit, BorderLayout.CENTER);

        setupKeyBindings();
        generarConsecutivoPedido();
        calcularFechaEntregaInteligente();
    }

    // --- MÉTODOS DE INICIALIZACIÓN UI ---

    private void setupKeyBindings() {
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "save");
        this.getActionMap().put("save", new AbstractAction() { public void actionPerformed(ActionEvent e) { saveOrder(); } });
    }

    private void calcularFechaEntregaInteligente() {
        if (dateDelivery == null) return;
        LocalDate fecha = LocalDate.now();
        int diasAgregados = 0;
        while (diasAgregados < 2) {
            fecha = fecha.plusDays(1);
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasAgregados++;
            }
        }
        dateDelivery.setDate(fecha);
    }

    private void generarConsecutivoPedido() {
        int nextId = saleController.getNextOrderNumber();
        String formattedId = String.format("PED-%04d", nextId);
        if(lblOrderNum != null) lblOrderNum.setText("# " + formattedId);
    }

    private JPanel createHeaderStack() {
        JPanel stack = new JPanel(new BorderLayout());
        stack.setOpaque(false);
        stack.setBorder(new EmptyBorder(5, 5, 10, 5));

        JPanel pLeft = new JPanel(new GridLayout(2, 1)); pLeft.setOpaque(false);

        // --- LÓGICA VIP ---
        boolean isVip = (currentClient.getId() < 10);

        String nameText = "CLIENTE: " + currentClient.getFullName().toUpperCase();
        JLabel lblName = new JLabel(nameText);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));

        if (isVip) {
            lblName.setIcon(new StatusDot(COLOR_VIP_DOT, 14));
            lblName.setText(nameText + "  (RECOMPENSA DISPONIBLE)");
            lblName.setForeground(COLOR_GOLD);
            lblName.setIconTextGap(12);
        } else {
            lblName.setForeground(Color.WHITE);
            lblName.setIcon(null);
        }

        String info = String.format("C.I: %s | TLF: %s | EMAIL: %s",
                currentClient.getIdNumber(), currentClient.getPhone(), currentClient.getEmail());
        JLabel lblInfo = new JLabel(info);
        lblInfo.setForeground(Color.LIGHT_GRAY); lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));

        pLeft.add(lblName); pLeft.add(lblInfo);

        JPanel pRight = new JPanel(new GridLayout(2, 1)); pRight.setOpaque(false);
        JLabel lblCode = new JLabel(String.format("CÓDIGO: DG-%04d", currentClient.getId()), SwingConstants.RIGHT);
        lblCode.setForeground(COLOR_NEON); lblCode.setFont(new Font("Segoe UI", Font.BOLD, 24));

        btnTasa = new SoftButton(null);
        btnTasa.setText(String.format("TASA: Bs. %.2f (Editar)", currentTasa));
        btnTasa.setForeground(COLOR_GOLD);
        btnTasa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTasa.setHorizontalAlignment(SwingConstants.RIGHT);

        btnTasa.addActionListener(e -> {
            new CurrencySettingsDialog((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true);
            currentTasa = CurrencyManager.getTasa();
            btnTasa.setText(String.format("TASA: Bs. %.2f (Editar)", currentTasa));
            updateCalculations();
        });

        pRight.add(lblCode); pRight.add(btnTasa);

        stack.add(pLeft, BorderLayout.CENTER);
        stack.add(pRight, BorderLayout.EAST);
        return stack;
    }

    // --- PANEL IZQUIERDO (INTACTO) ---
    private JPanel createLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setOpaque(false);

        // 1. Barra de Inputs (Arriba)
        JPanel bar = new JPanel(new GridBagLayout()); bar.setOpaque(false);
        GridBagConstraints gb = new GridBagConstraints(); gb.fill = 2; gb.insets = new Insets(0, 2, 0, 2);

        txtProductCode = new JTextField(); txtProductCode.addActionListener(e -> addProductByCode());
        bar.add(createLabeledInput("CÓDIGO", txtProductCode, 60), setGrid(gb,0,0.15));

        cmbProducts = new JComboBox<>(); cmbProducts.addItem("- Seleccionar -");
        for(Product pr : productList) cmbProducts.addItem(pr.getName());
        cmbProducts.addActionListener(e -> {
            int idx = cmbProducts.getSelectedIndex();
            if (idx > 0) {
                Product prod = productList.get(idx - 1);
                txtPrice.setText(String.format(Locale.US, "%.2f", prod.getSalePrice()));
                txtProductCode.setText(String.format("%03d", prod.getId()));
            }
        });
        bar.add(createLabeledInput("PRODUCTO", cmbProducts, 180), setGrid(gb,1,0.4));

        cmbSize = new JComboBox<>(new String[]{"4","6","8","10","12","14","16","S","M","L","XL","A MEDIDA"});
        bar.add(createLabeledInput("TALLA", cmbSize, 70), setGrid(gb,2,0.15));

        txtPrice = new JTextField("0.00");
        bar.add(createLabeledInput("PRECIO $", txtPrice, 70), setGrid(gb,3,0.15));

        JPanel pQty = new JPanel(new BorderLayout()); pQty.setOpaque(false);
        txtQuantity = new JTextField("1"); styleField(txtQuantity); txtQuantity.setHorizontalAlignment(0);

        SoftButton bM = new SoftButton(null); bM.setText("-"); bM.setPreferredSize(new Dimension(25,0));
        bM.addActionListener(e -> adjustQty(-1));

        SoftButton bP = new SoftButton(null); bP.setText("+"); bP.setPreferredSize(new Dimension(25,0));
        bP.addActionListener(e -> adjustQty(1));

        pQty.add(bM, "West"); pQty.add(txtQuantity, "Center"); pQty.add(bP, "East");
        JPanel pQtyWrap = new JPanel(new BorderLayout()); pQtyWrap.setOpaque(false);
        pQtyWrap.add(pQty, BorderLayout.CENTER);

        bar.add(createLabeledInput("CANTIDAD", pQtyWrap, 90), setGrid(gb,4,0.15));

        SoftButton bAdd = new SoftButton(null); bAdd.setText("AÑADIR"); bAdd.setBackground(new Color(0, 100, 50));
        bAdd.setPreferredSize(new Dimension(70, 35)); bAdd.addActionListener(e -> addToCart());

        JPanel pBtn = new JPanel(new BorderLayout()); pBtn.setOpaque(false);
        pBtn.add(new JLabel(" "), "North");
        pBtn.add(bAdd, "Center");

        bar.add(pBtn, setGrid(gb,5,0));

        p.add(bar, BorderLayout.NORTH);

        // 2. Tabla (Compacta 50% - 160px)
        table = new JTable(tableModel); styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(20,20,20));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(0, 160));

        p.add(scroll, BorderLayout.CENTER);

        // 3. Botones de Acción (ESTILO CUADRADO LUXURY)
        JPanel pActions = new JPanel(new GridLayout(1, 3, 15, 0));
        pActions.setOpaque(false);
        pActions.setBorder(new EmptyBorder(10, 0, 10, 0));

        pActions.add(createSquareIconButton("/images/icons/sq_back.png", "Salir", e -> SwingUtilities.getWindowAncestor(this).dispose()));
        pActions.add(createSquareIconButton("/images/icons/sq_clean.png", "Limpiar", e -> clearForm()));
        pActions.add(createSquareIconButton("/images/icons/sq_delete.png", "Eliminar Ítem", e -> {
            int r = table.getSelectedRow();
            if (r >= 0) removeFromCart(r);
            else LuxuryMessage.show("Aviso", "Seleccione un ítem de la tabla.", true);
        }));

        p.add(pActions, BorderLayout.SOUTH);

        return p;
    }

    // --- PANEL DERECHO (REDISEÑADO Y MEJORADO) ---
    private JPanel createRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Inputs Superiores (Factura/Control)
        JPanel pTop = new JPanel(new GridBagLayout()); pTop.setOpaque(false);
        GridBagConstraints gTop = new GridBagConstraints(); gTop.fill = 2; gTop.insets = new Insets(0, 5, 0, 5);

        chkInvoice = new JCheckBox("FACTURA");
        chkInvoice.setForeground(COLOR_GOLD); chkInvoice.setOpaque(false); chkInvoice.setFont(new Font("Segoe UI", Font.BOLD, 12));

        chkInvoice.addActionListener(e -> {
            boolean en = chkInvoice.isSelected();
            toggleInvoice(en);
            if(en) {
                txtInvoice.setText(saleController.getNextInvoiceNumber());
                txtControl.setText(saleController.getNextControlNumber());
            }
        });

        txtInvoice = new JTextField(); styleField(txtInvoice); txtInvoice.setEnabled(false);
        txtControl = new JTextField(); styleField(txtControl); txtControl.setEnabled(false);

        gTop.gridx=0; gTop.weightx=0.15; pTop.add(chkInvoice, gTop);
        gTop.gridx=1; gTop.weightx=0.25; pTop.add(createLabeledInput("NRO FACTURA", txtInvoice, 0), gTop);
        gTop.gridx=2; gTop.weightx=0.30; pTop.add(createLabeledInput("NRO CONTROL", txtControl, 0), gTop);

        lblOrderNum = new JLabel("# PED-????");
        lblOrderNum.setForeground(COLOR_NEON); lblOrderNum.setFont(new Font("Consolas", Font.BOLD, 16));
        lblOrderNum.setHorizontalAlignment(SwingConstants.RIGHT);

        gTop.gridx=3; gTop.weightx=0.30; pTop.add(createLabeledInput(" ", lblOrderNum, 0), gTop);

        p.add(pTop, BorderLayout.NORTH);

        // 2. Panel Central Dividido (Formulario Pagos vs Resumen/Notas)
        JPanel pMid = new JPanel(new GridLayout(1, 2, 15, 0)); pMid.setOpaque(false);

        // A) Formulario Izquierdo (Pagos)
        JPanel pForm = new JPanel(new GridBagLayout()); pForm.setOpaque(false);
        pForm.setBorder(createTitledBorder("CIERRE DE OPERACIÓN"));
        GridBagConstraints gL = new GridBagConstraints(); gL.fill = 2; gL.insets = new Insets(4, 2, 4, 2); gL.weightx = 1.0;

        txtAbono = new JTextField("0.00"); styleField(txtAbono); txtAbono.getDocument().addDocumentListener(new DocH());
        txtDiscount = new JTextField("0"); styleField(txtDiscount); txtDiscount.getDocument().addDocumentListener(new DocH());

        // Botones de % Mejorados (Estilo Chip)
        btnPay50 = new SoftButton(null); btnPay50.setText("50%"); btnPay50.setPreferredSize(new Dimension(60, 25));
        btnPay50.setFont(new Font("Segoe UI", Font.BOLD, 11)); btnPay50.setForeground(Color.CYAN);
        btnPay50.setBackground(new Color(0, 50, 50));
        btnPay50.addActionListener(e -> setQuickPay(0.5));

        btnPay100 = new SoftButton(null); btnPay100.setText("100%"); btnPay100.setPreferredSize(new Dimension(60, 25));
        btnPay100.setFont(new Font("Segoe UI", Font.BOLD, 11)); btnPay100.setForeground(Color.MAGENTA);
        btnPay100.setBackground(new Color(50, 0, 50));
        btnPay100.addActionListener(e -> setQuickPay(1.0));

        JPanel pBtnMix = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0)); pBtnMix.setOpaque(false);
        pBtnMix.add(btnPay50); pBtnMix.add(btnPay100);

        gL.gridy=0; gL.gridx=0; gL.weightx=0.6; pForm.add(createLabeledInput("ABONO $", txtAbono, 0), gL);
        gL.gridx=1; gL.weightx=0.4; pForm.add(createLabeledInput("DESC %", txtDiscount, 0), gL);
        gL.gridy=1; gL.gridx=0; gL.gridwidth=2; pForm.add(pBtnMix, gL);

        cmbPayMethod = new JComboBox<>(new String[]{"PAGO MÓVIL", "EFECTIVO", "ZELLE", "TRANSFERENCIA"}); styleCombo(cmbPayMethod);
        gL.gridy=2; gL.gridx=0; gL.gridwidth=2; pForm.add(createLabeledInput("MÉTODO DE PAGO", cmbPayMethod, 0), gL);

        cmbBank = new JComboBox<>(new String[]{"-", "BANESCO", "MERCANTIL", "VENEZUELA", "PROVINCIAL", "BNC", "BANCAMIGA", "TESORO", "DEL SUR"});
        styleCombo(cmbBank); cmbBank.setEnabled(false);
        gL.gridy=3; pForm.add(createLabeledInput("BANCO", cmbBank, 0), gL);

        txtRef = new JTextField(); styleField(txtRef);
        gL.gridy=4; pForm.add(createLabeledInput("REFERENCIA", txtRef, 0), gL);

        DatePickerSettings dsPay = createDarkDateSettings(true);
        datePayment = new DatePicker(dsPay); styleDatePicker(datePayment); datePayment.setDateToToday();
        gL.gridy=5; pForm.add(createLabeledInput("FECHA PAGO", datePayment, 0), gL);

        pMid.add(pForm);

        // B) Columna Derecha (OLEDs + Notas Expandidas)
        JPanel pOleds = new JPanel(new GridBagLayout()); pOleds.setOpaque(false);
        GridBagConstraints gR = new GridBagConstraints(); gR.fill=2; gR.insets=new Insets(0,0,8,0); gR.weightx=1; gR.gridx=0;

        lblTotalBs = new JLabel("Bs. 0.00"); lblAbonadoBs = new JLabel("Bs. 0.00"); lblRestaBs = new JLabel("Bs. 0.00");

        gR.gridy=0; pOleds.add(createOled("TOTAL A PAGAR", lblTotal = new JLabel("$ 0.00"), lblTotalBs, COLOR_NEON), gR);
        gR.gridy=1; pOleds.add(createOled("TOTAL ABONADO", lblAbonado = new JLabel("$ 0.00"), lblAbonadoBs, Color.CYAN), gR);
        gR.gridy=2; pOleds.add(createOled("RESTA PAGAR", lblResta = new JLabel("$ 0.00"), lblRestaBs, Color.ORANGE), gR);

        // AQUÍ EL CAMBIO CLAVE: Notas más grandes
        txtObservations = new JTextArea(); styleTextArea(txtObservations);
        JScrollPane sO = new JScrollPane(txtObservations);
        // Altura aumentada a 120px para llenar el vacío
        sO.setPreferredSize(new Dimension(0, 120));
        sO.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        gR.gridy=3; gR.insets=new Insets(10,0,0,0);
        pOleds.add(createLabeledInput("NOTAS DE TALLER / MEDIDAS", sO, 0), gR);

        DatePickerSettings dsDel = createDarkDateSettings(false);
        dateDelivery = new DatePicker(dsDel); styleDatePicker(dateDelivery);
        gR.gridy=4; pOleds.add(createLabeledInput("FECHA ENTREGA", dateDelivery, 0), gR);

        pMid.add(pOleds);

        p.add(pMid, BorderLayout.CENTER);

        // 3. Botones Finales (Sin texto, alineados abajo)
        JPanel pSaveActions = new JPanel(new GridLayout(1, 2, 10, 0));
        pSaveActions.setOpaque(false);
        pSaveActions.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnReceipt = createSquareIconButton("/images/icons/sq_invoice.png", "Ver Comprobante", e -> handleReceiptAction());
        if(btnReceipt.getIcon() == null) btnReceipt = createSquareIconButton("/images/icons/icon_report.png", "Ver Comprobante", e -> handleReceiptAction());

        btnReceipt.setEnabled(false);
        btnReceipt.addPropertyChangeListener("enabled", evt -> {
            btnReceipt.setForeground(btnReceipt.isEnabled() ? Color.WHITE : Color.GRAY);
        });

        pSaveActions.add(btnReceipt);

        SoftButton bSa = createSquareIconButton("/images/icons/sq_save.png", "Guardar Pedido", e -> saveOrder());
        if(bSa.getIcon() == null) bSa = createSquareIconButton("/images/icons/btn_save.png", "Guardar Pedido", e -> saveOrder());
        bSa.setForeground(COLOR_GOLD);
        pSaveActions.add(bSa);

        p.add(pSaveActions, BorderLayout.SOUTH);

        cmbPayMethod.addActionListener(e -> {
            String m = (String)cmbPayMethod.getSelectedItem();
            cmbBank.setEnabled("TRANSFERENCIA".equals(m) || "PAGO MÓVIL".equals(m));
        });

        return p;
    }

    // --- LÓGICA DE NEGOCIO ---

    private void handleReceiptAction() {
        if(lastOrderIdSaved == null) return;
        new ReceiptOptionDialog((Frame)SwingUtilities.getWindowAncestor(this), lastOrderIdSaved).setVisible(true);
    }

    private void saveOrder() {
        if(cartDetails.isEmpty()){
            LuxuryMessage.show("Aviso", "No hay productos en el carrito.", true);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirmar el procesamiento del pedido?", "SICONI", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;

        double ab = Double.parseDouble(txtAbono.getText().replace(",","."));
        String delDate = (dateDelivery.getDate() != null) ? dateDelivery.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        String payDate = (datePayment.getDate() != null) ? datePayment.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

        String orderId = lblOrderNum.getText().replace("# ", "").trim();

        Sale s = new Sale(orderId,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                String.valueOf(currentClient.getId()),
                totalAmount, ab, currentTasa,
                (String)cmbPayMethod.getSelectedItem(),
                txtRef.getText(),
                "EN PRODUCCIÓN",
                txtObservations.getText());

        s.setBalanceDue(totalAmount - ab);
        s.setInvoiceNumber(chkInvoice.isSelected() ? txtInvoice.getText() : "");
        s.setControlNumber(chkInvoice.isSelected() ? txtControl.getText() : "");
        s.setDeliveryDate(delDate);
        s.setPaymentDate(payDate);
        s.setBank((String)cmbBank.getSelectedItem());

        if(saleController.registerSale(s, cartDetails)){
            lastOrderIdSaved = orderId;
            btnReceipt.setEnabled(true);
            LuxuryMessage.show("SICONI", "¡Pedido guardado exitosamente!", false);
            new ReceiptOptionDialog((Frame)SwingUtilities.getWindowAncestor(this), orderId).setVisible(true);
            clearForm();
        } else {
            LuxuryMessage.show("Error de Guardado", "No se pudo registrar el pedido en la base de datos.", true);
        }
    }

    private void runPrintSimulation(boolean openFile) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Imprimiendo...", true);
        d.setUndecorated(true);
        d.setSize(350, 120);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.BLACK);
        p.setBorder(new LineBorder(COLOR_NEON, 2));

        JLabel lStatus = new JLabel("Conectando con servicio de impresión...", SwingConstants.CENTER);
        lStatus.setForeground(COLOR_NEON);
        lStatus.setFont(new Font("Consolas", Font.BOLD, 14));

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBackground(Color.BLACK);
        bar.setForeground(COLOR_NEON);
        bar.setBorderPainted(false);

        p.add(lStatus, BorderLayout.CENTER);
        p.add(bar, BorderLayout.SOUTH);
        d.add(p);

        Timer t1 = new Timer(800, e -> lStatus.setText("Generando documento PDF..."));
        t1.setRepeats(false);

        Timer t2 = new Timer(1800, e -> lStatus.setText("Enviando a cola de impresión..."));
        t2.setRepeats(false);

        Timer tEnd = new Timer(2500, e -> {
            d.dispose();
            if(openFile) openPDF(lastOrderIdSaved);
            else openFolder();
        });
        tEnd.setRepeats(false);

        t1.start(); t2.start(); tEnd.start();
        d.setVisible(true);
    }

    private void openPDF(String id) {
        try {
            File pdf = new File("recibo_" + id + ".pdf");
            if(pdf.exists()) Desktop.getDesktop().open(pdf);
            else LuxuryMessage.show("Info", "El archivo PDF se generó en la carpeta del sistema.", false);
        } catch(Exception ex){}
    }

    private void openFolder() {
        try {
            File currentDir = new File(".").getCanonicalFile();
            Desktop.getDesktop().open(currentDir);
        } catch(Exception ex) {
            LuxuryMessage.show("Error", "No se pudo abrir la carpeta.", true);
        }
    }

    private void clearForm() {
        tableModel.setRowCount(0);
        cartDetails.clear();
        txtAbono.setText("0.00");
        txtRef.setText("");
        txtDiscount.setText("0");
        txtObservations.setText("");
        cmbPayMethod.setSelectedIndex(0);
        cmbBank.setSelectedIndex(0);
        cmbBank.setEnabled(false);
        chkInvoice.setSelected(false);
        toggleInvoice(false);
        btnReceipt.setEnabled(false);
        calcularFechaEntregaInteligente();
        generarConsecutivoPedido();
        updateCalculations();
        txtProductCode.requestFocus();
    }

    // --- MÉTODOS AUXILIARES ---

    private JPanel createLabeledInput(String title, JComponent c, int width) {
        JPanel p = new JPanel(new BorderLayout(0, 3)); p.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setForeground(COLOR_GOLD);
        l.setFont(FONT_BOLD_INPUT);
        p.add(l, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        if (width > 0) p.setPreferredSize(new Dimension(width, 50));
        return p;
    }

    private GridBagConstraints setGrid(GridBagConstraints g, int x, double w) {
        g.gridx = x; g.weightx = w; return g;
    }

    private DatePickerSettings createDarkDateSettings(boolean isGold) {
        DatePickerSettings ds = new DatePickerSettings();
        ds.setFormatForDatesCommonEra("dd/MM/yyyy");
        ds.setColor(DateArea.BackgroundOverallCalendarPanel, new Color(40,40,40));
        ds.setColor(DateArea.CalendarBackgroundSelectedDate, isGold ? COLOR_GOLD : new Color(220, 0, 115));
        ds.setColor(DateArea.TextFieldBackgroundValidDate, Color.WHITE);
        ds.setColor(DateArea.TextFieldBackgroundInvalidDate, Color.WHITE);
        Font dateFont = new Font("Segoe UI", Font.BOLD, 16);
        ds.setFontValidDate(dateFont);
        ds.setFontInvalidDate(dateFont);
        ds.setColor(DateArea.DatePickerTextValidDate, Color.BLACK);
        ds.setFontCalendarDateLabels(new Font("Segoe UI", Font.BOLD, 14));
        return ds;
    }

    private void styleDatePicker(DatePicker dp) {
        JTextField f = dp.getComponentDateTextField();
        f.setBackground(Color.WHITE);
        f.setForeground(Color.BLACK);
        f.setFont(new Font("Segoe UI", Font.BOLD, 16));
        f.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
        JButton b = dp.getComponentToggleCalendarButton();
        b.setText("📅");
        b.setPreferredSize(new Dimension(30,30));
    }

    private void styleField(JTextField t) {
        t.setBackground(COLOR_INPUT_BG); t.setForeground(Color.WHITE); t.setCaretColor(COLOR_GOLD);
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(2, 5, 2, 5)));
        t.setPreferredSize(new Dimension(0, 35));
        t.addFocusListener(new FocusAdapter() { @Override public void focusGained(FocusEvent e) { t.selectAll(); } });
    }

    private void styleCombo(JComboBox b) { b.setBackground(COLOR_INPUT_BG); b.setForeground(Color.WHITE); b.setPreferredSize(new Dimension(0, 35)); b.setFont(new Font("Segoe UI", Font.BOLD, 12)); }
    private void styleTextArea(JTextArea t) { t.setBackground(COLOR_INPUT_BG); t.setForeground(Color.WHITE); t.setLineWrap(true); t.setBorder(new EmptyBorder(5,5,5,5)); t.setFont(new Font("Segoe UI", Font.BOLD, 12)); }
    private TitledBorder createTitledBorder(String t) { TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(COLOR_GOLD), t); b.setTitleColor(COLOR_GOLD); b.setTitleFont(new Font("Segoe UI", Font.BOLD, 11)); return b; }

    private JPanel createOled(String t, JLabel u, JLabel b, Color c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.BLACK);
        p.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(2,8,2,8)));
        p.setPreferredSize(new Dimension(0, 65));
        JLabel title = new JLabel(t); title.setForeground(Color.GRAY); title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        u.setFont(FONT_OLED_USD); u.setForeground(c); u.setHorizontalAlignment(4);
        b.setFont(FONT_OLED_BS); b.setForeground(Color.GRAY); b.setHorizontalAlignment(4);
        JPanel pV = new JPanel(new GridLayout(2,1)); pV.setOpaque(false); pV.add(u); pV.add(b);
        p.add(title, "North"); p.add(pV, "Center"); return p;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(30); t.setBackground(new Color(30,30,30)); t.setForeground(Color.WHITE);
        t.getTableHeader().setBackground(Color.BLACK); t.getTableHeader().setForeground(COLOR_GOLD);
        t.getTableHeader().setFont(FONT_BOLD_TABLE);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, val, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? new Color(40,40,40) : new Color(50,50,50));
                if(col == 0) setHorizontalAlignment(JLabel.LEFT); else setHorizontalAlignment(JLabel.CENTER);
                setFont(FONT_BOLD_TABLE);
                return c;
            }
        });
        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { int r = t.rowAtPoint(e.getPoint()); if(r >= 0) t.setRowSelectionInterval(r, r); }
        });
    }

    private SoftButton createSquareIconButton(String i, String tooltip, ActionListener a) {
        SoftButton b = new SoftButton(null);
        b.setText("");
        ImageIcon icon = createIcon(i, 100, 100);
        if(icon != null) b.setIcon(icon);
        b.setToolTipText(tooltip);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setVerticalAlignment(SwingConstants.CENTER);
        b.addActionListener(a);
        return b;
    }

    private ImageIcon createIcon(String p, int w, int h) {
        try {
            URL u = getClass().getResource(p);
            if(u!=null) return new ImageIcon(new ImageIcon(u).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch(Exception e){}
        return null;
    }

    private void adjustQty(int v) { try { int q = Integer.parseInt(txtQuantity.getText()); if(q+v>0) txtQuantity.setText(String.valueOf(q+v)); } catch(Exception e){ txtQuantity.setText("1"); } }

    private void addProductByCode() {
        try { Product p = productDAO.getProductById(Integer.parseInt(txtProductCode.getText().trim())); if(p != null) { cmbProducts.setSelectedItem(p.getName()); addToCart(); txtProductCode.setText(""); } } catch(Exception e){}
    }

    private void removeFromCart(int r) { if(r>=0) { cartDetails.remove(r); tableModel.removeRow(r); updateCalculations(); } }

    private void updateCalculations() {
        double sub = cartDetails.stream().mapToDouble(d -> d.getPrice() * d.getQuantity()).sum();
        double d = 0; try { d = Double.parseDouble(txtDiscount.getText()); } catch (Exception e){}
        double net = sub * (1 - (d/100.0));
        double a = 0; try { a = Double.parseDouble(txtAbono.getText().replace(",",".")); } catch(Exception e){}
        totalAmount = net;
        lblTotal.setText(String.format(Locale.US, "$ %.2f", net));
        lblTotalBs.setText(String.format(Locale.US, "Bs. %.2f", net * currentTasa));
        lblAbonado.setText(String.format(Locale.US, "$ %.2f", a));
        lblAbonadoBs.setText(String.format(Locale.US, "Bs. %.2f", a * currentTasa));
        lblResta.setText(String.format(Locale.US, "$ %.2f", net - a));
        lblRestaBs.setText(String.format(Locale.US, "Bs. %.2f", (net - a) * currentTasa));
    }

    private void setQuickPay(double percentage) {
        double amountToPay = totalAmount * percentage;
        txtAbono.setText(String.format(Locale.US, "%.2f", amountToPay));
    }

    private void toggleInvoice(boolean enable) {
        txtInvoice.setEnabled(enable);
        txtControl.setEnabled(enable);
        if(!enable) { txtInvoice.setText(""); txtControl.setText(""); }
    }

    private void addToCart() {
        if(cmbProducts.getSelectedIndex() <= 0) return;
        Product p = productList.get(cmbProducts.getSelectedIndex()-1);
        int cantidadPedida = Integer.parseInt(txtQuantity.getText());
        if (p.getCurrentStock() < cantidadPedida) {
            LuxuryMessage.show("STOCK INSUFICIENTE", "Solo quedan " + p.getCurrentStock(), true);
            return;
        }
        double pr = Double.parseDouble(txtPrice.getText().replace(",","."));
        String sz = (String) cmbSize.getSelectedItem();
        tableModel.addRow(new Object[]{p.getName(), sz, cantidadPedida, String.format("%.2f", pr), String.format("%.2f", pr*cantidadPedida)});
        cartDetails.add(new SaleDetail("0", String.valueOf(p.getId()), p.getName()+" ["+sz+"]", cantidadPedida, pr));
        updateCalculations();
    }

    // --- CLASES INTERNAS ---

    private static class StatusDot implements Icon {
        private final Color color;
        private final int size;
        public StatusDot(Color c, int s) { this.color = c; this.size = s; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.setColor(Color.WHITE);
            g2.fillOval(x+size/4, y+size/4, size/4, size/4);
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    private class ReceiptOptionDialog extends JDialog {
        public ReceiptOptionDialog(Frame parent, String orderId) {
            super(parent, true);

            setSize(450, 220);
            setLocationRelativeTo(parent);

            JPanel p = new JPanel(new BorderLayout(0, 20));
            p.setBackground(new Color(30,30,30));
            p.setBorder(new CompoundBorder(new LineBorder(COLOR_GOLD, 2), new EmptyBorder(20,20,20,20)));

            JLabel lblTitle = new JLabel("GESTIÓN DE RECIBO", SwingConstants.CENTER);
            lblTitle.setForeground(COLOR_GOLD); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

            JLabel lblMsg = new JLabel("<html><div style='text-align: center;'>Pedido <b>" + orderId + "</b> procesado.<br>¿Cómo desea visualizar el comprobante?</div></html>", SwingConstants.CENTER);
            lblMsg.setForeground(Color.WHITE); lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JPanel pBtns = new JPanel(new GridLayout(1, 2, 15, 0));
            pBtns.setOpaque(false);

            SoftButton btnFolder = new SoftButton(null);
            btnFolder.setText("📂 VER EN CARPETA");
            btnFolder.setBackground(new Color(60,60,60));
            btnFolder.setForeground(Color.WHITE);
            btnFolder.addActionListener(e -> { dispose(); runPrintSimulation(false); });

            SoftButton btnOpen = new SoftButton(null);
            btnOpen.setText("📄 ABRIR PDF");
            btnOpen.setBackground(COLOR_NEON);
            btnOpen.setForeground(Color.BLACK);
            btnOpen.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnOpen.addActionListener(e -> { dispose(); runPrintSimulation(true); });

            pBtns.add(btnFolder);
            pBtns.add(btnOpen);

            SoftButton btnClose = new SoftButton(null);
            btnClose.setText("CERRAR");
            btnClose.setPreferredSize(new Dimension(80, 30));
            btnClose.setBackground(new Color(100,0,0));
            btnClose.setForeground(Color.WHITE);
            btnClose.addActionListener(e -> dispose());

            JPanel pBottom = new JPanel(new BorderLayout(0, 10)); pBottom.setOpaque(false);
            pBottom.add(pBtns, BorderLayout.CENTER);
            pBottom.add(btnClose, BorderLayout.SOUTH);

            p.add(lblTitle, BorderLayout.NORTH);
            p.add(lblMsg, BorderLayout.CENTER);
            p.add(pBottom, BorderLayout.SOUTH);

            setContentPane(p);
        }
    }

    private class DocH implements DocumentListener { public void changedUpdate(DocumentEvent e){updateCalculations();} public void removeUpdate(DocumentEvent e){updateCalculations();} public void insertUpdate(DocumentEvent e){updateCalculations();} }
}