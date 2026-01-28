/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: SalesView.java
 * VERSIÓN: 24.1.0 (Master Workshop POS - final Fix)
 * FECHA: January 28, 2026 - 06:50AM
 * DESCRIPCIÓN: Terminal bimonetario corregido. Se unifican colores y fuentes
 * para evitar errores de compilación en clases internas.
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
import com.swimcore.util.LanguageManager;
import com.swimcore.util.LuxuryMessage;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesView extends JPanel {

    private final Client currentClient;
    private final SaleController saleController;
    private final ProductDAO productDAO;
    private final List<Product> productList;
    private final DefaultTableModel tableModel;
    private final List<SaleDetail> cartDetails = new ArrayList<>();

    private JComboBox<String> cmbProducts, cmbSize, cmbPayMethod;
    private JTextField txtProductCode, txtPrice, txtQuantity, txtAbono, txtRef, txtDiscount, txtDeliveryDate;
    private JTable table;
    private JTextArea txtObservations;
    private JLabel lblTotal, lblTotalBs, lblAbonado, lblAbonadoBs, lblResta, lblRestaBs;

    private double totalAmount = 0.0;
    private final double TASA_ACTUAL = CurrencyManager.getTasa();

    // --- CONSTANTES DE ESTILO (GLOBALES PARA ESTA CLASE) ---
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_NEON = new Color(57, 255, 20);
    private final Color COLOR_RED = new Color(255, 80, 80);
    private final Color COLOR_INPUT_BG = new Color(45, 45, 45);
    private final Font FONT_STD_BOLD = new Font("Segoe UI", Font.BOLD, 15);
    private final Font FONT_OLED_USD = new Font("Consolas", Font.BOLD, 32);
    private final Font FONT_OLED_BS = new Font("Consolas", Font.BOLD, 16);

    public SalesView(Client client) {
        this.currentClient = client;
        this.saleController = new SaleController();
        this.productDAO = new ProductDAO();
        this.productList = productDAO.getAllProducts();

        setOpaque(false);
        setLayout(new BorderLayout(15, 10));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        tableModel = new DefaultTableModel(new String[]{"PRODUCTO", "TALLA", "CANT", "PRECIO", "TOTAL", "X"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        add(createClientBanner(), BorderLayout.NORTH);
        add(createWorkStation(), BorderLayout.CENTER);
        add(createFinancePanel(), BorderLayout.EAST);
        add(createActionHub(), BorderLayout.SOUTH);

        setupKeyBindings();
    }

    private void setupKeyBindings() {
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "save");
        this.getActionMap().put("save", new AbstractAction() { public void actionPerformed(ActionEvent e) { saveOrder(); } });
    }

    private JPanel createClientBanner() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        String html = String.format("<html><body style='color:white; font-family:Segoe UI;'>" +
                        "<font size='6' color='#D4AF37'><b>ORDEN DE PRODUCCIÓN</b></font><br>" +
                        "<font size='5'><b>CLIENTE:</b> %s | <b>CÓDIGO:</b> <font color='#00FF80'>%s</font></font><br>" +
                        "<font size='4' color='#AAAAAA'><b>TLF:</b> %s | <b>DIRECCIÓN:</b> %s</font></body></html>",
                currentClient.getFullName(), currentClient.getCode(), currentClient.getPhone(), currentClient.getAddress());
        JLabel lblInfo = new JLabel(html);
        JPanel pRight = new JPanel(new GridLayout(2, 1)); pRight.setOpaque(false);
        JLabel lblDate = new JLabel("FECHA: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()), 4);
        lblDate.setForeground(Color.LIGHT_GRAY); lblDate.setFont(FONT_STD_BOLD);
        JLabel lblTasa = new JLabel(String.format("TASA BCV: Bs. %.2f", TASA_ACTUAL), 4);
        lblTasa.setForeground(COLOR_NEON); lblTasa.setFont(FONT_STD_BOLD);
        pRight.add(lblDate); pRight.add(lblTasa);
        p.add(lblInfo, BorderLayout.WEST); p.add(pRight, BorderLayout.EAST);
        return p;
    }

    private JPanel createWorkStation() {
        JPanel main = new JPanel(new GridBagLayout()); main.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints(); g.fill = 2; g.insets = new Insets(0, 0, 10, 0);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)); bar.setOpaque(false);
        txtProductCode = new JTextField(); styleInput(txtProductCode, "CÓDIGO", 80);
        cmbProducts = new JComboBox<>(); cmbProducts.addItem("- Producto -");
        for(Product p : productList) cmbProducts.addItem(p.getName()); styleCombo(cmbProducts, 220);
        cmbSize = new JComboBox<>(new String[]{"4","6","8","10","12","14","16","S","M","L","XL","A MEDIDA"}); styleCombo(cmbSize, 100);
        txtPrice = new JTextField("0.00"); styleInput(txtPrice, "PRECIO", 100);
        JPanel pQty = new JPanel(new BorderLayout()); pQty.setOpaque(false);
        txtQuantity = new JTextField("1"); styleInput(txtQuantity, "", 50); txtQuantity.setHorizontalAlignment(0);
        SoftButton bM = new SoftButton(null); bM.setText("-"); bM.setPreferredSize(new Dimension(35,0)); bM.addActionListener(e -> adjustQty(-1));
        SoftButton bP = new SoftButton(null); bP.setText("+"); bP.setPreferredSize(new Dimension(35,0)); bP.addActionListener(e -> adjustQty(1));
        pQty.add(bM, "West"); pQty.add(txtQuantity, "Center"); pQty.add(bP, "East"); pQty.setPreferredSize(new Dimension(120, 42));
        SoftButton bAdd = new SoftButton(null); bAdd.setText("AÑADIR"); bAdd.setBackground(new Color(0, 120, 60)); bAdd.setPreferredSize(new Dimension(100, 42)); bAdd.addActionListener(e -> addToCart());
        bar.add(txtProductCode); bar.add(cmbProducts); bar.add(cmbSize); bar.add(txtPrice); bar.add(pQty); bar.add(bAdd);
        g.gridy=0; main.add(bar, g);
        table = new JTable(tableModel); styleTable(table);
        JScrollPane scrollT = new JScrollPane(table); scrollT.getViewport().setBackground(new Color(20,20,20)); scrollT.setPreferredSize(new Dimension(0, 250));
        g.gridy=1; g.weighty=1.0; g.fill=1; main.add(scrollT, g);
        JPanel bottomRow = new JPanel(new BorderLayout(15, 0)); bottomRow.setOpaque(false);
        txtObservations = new JTextArea(2, 20); styleTextArea(txtObservations);
        JScrollPane scrollO = new JScrollPane(txtObservations); scrollO.setBorder(createTitledBorder("NOTAS DE TALLER / MEDIDAS"));
        JPanel pDelivery = new JPanel(new GridLayout(2, 1, 0, 5)); pDelivery.setOpaque(false); pDelivery.setBorder(createTitledBorder("FECHA DE ENTREGA"));
        txtDeliveryDate = new JTextField(); styleInput(txtDeliveryDate, "", 150); txtDeliveryDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        pDelivery.add(txtDeliveryDate); bottomRow.add(scrollO, BorderLayout.CENTER); bottomRow.add(pDelivery, BorderLayout.EAST);
        g.gridy=2; g.weighty=0; g.fill=2; main.add(bottomRow, g);
        return main;
    }

    private JPanel createFinancePanel() {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); p.setPreferredSize(new Dimension(340, 0));
        GridBagConstraints g = new GridBagConstraints(); g.fill = 2; g.insets = new Insets(2, 0, 2, 0); g.gridx = 0; g.weightx = 1.0;
        lblTotalBs = new JLabel("Bs. 0.00"); lblAbonadoBs = new JLabel("Bs. 0.00"); lblRestaBs = new JLabel("Bs. 0.00");
        g.gridy=0; p.add(createOledBox("TOTAL A PAGAR", lblTotal = new JLabel("$ 0.00"), lblTotalBs, COLOR_NEON), g);
        g.gridy=1; p.add(createOledBox("MONTO ABONADO", lblAbonado = new JLabel("$ 0.00"), lblAbonadoBs, Color.CYAN), g);
        g.gridy=2; p.add(createOledBox("RESTA PENDIENTE", lblResta = new JLabel("$ 0.00"), lblRestaBs, Color.ORANGE), g);
        JPanel form = new JPanel(new GridLayout(0, 1, 3, 3)); form.setOpaque(false); form.setBorder(createTitledBorder("CIERRE Y PAGO"));
        txtDiscount = new JTextField("0"); styleInput(txtDiscount, "DESC %", 0); txtDiscount.getDocument().addDocumentListener(new DocH()); form.add(txtDiscount);
        txtAbono = new JTextField("0.00"); styleInput(txtAbono, "ABONAR $", 0); txtAbono.getDocument().addDocumentListener(new DocH()); form.add(txtAbono);
        cmbPayMethod = new JComboBox<>(new String[]{"PAGO MÓVIL", "EFECTIVO", "ZELLE", "TRANSFERENCIA"}); styleCombo(cmbPayMethod, 0); form.add(cmbPayMethod);
        txtRef = new JTextField(); styleInput(txtRef, "REF / LOTE", 0); form.add(txtRef);
        g.gridy=3; p.add(form, g); return p;
    }

    private JPanel createActionHub() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); p.setOpaque(false);
        Dimension s = new Dimension(150, 50);
        SoftButton bEx = createHubBtn("SALIR", "/images/icons/icon_cancel_gold.png", s); bEx.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        SoftButton bCl = createHubBtn("LIMPIAR", "/images/icons/icon_broom_gold.png", s); bCl.addActionListener(e -> clearForm());
        SoftButton bGr = createHubBtn("REPORTES", "/images/icons/icon_reports_gold.png", s);
        SoftButton bRe = createHubBtn("RECIBO", "/images/icons/icon_check_gold.png", s);
        SoftButton bSa = createHubBtn("GUARDAR", "/images/icons/icon_save_gold.png", s); bSa.setForeground(COLOR_GOLD); bSa.addActionListener(e -> saveOrder());
        p.add(bEx); p.add(bCl); p.add(bGr); p.add(bRe); p.add(bSa); return p;
    }

    private void styleInput(JTextField t, String title, int w) {
        t.setBackground(COLOR_INPUT_BG); t.setForeground(Color.WHITE); t.setCaretColor(COLOR_GOLD); t.setFont(new Font("Segoe UI", 1, 14));
        t.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), title, 0, 0, new Font("Segoe UI", 0, 9), Color.GRAY));
        if(w > 0) t.setPreferredSize(new Dimension(w, 42));
        t.addFocusListener(new FocusAdapter() { @Override public void focusGained(FocusEvent e) { t.selectAll(); } });
    }
    private void styleCombo(JComboBox b, int w) { b.setBackground(COLOR_INPUT_BG); b.setForeground(Color.WHITE); if(w > 0) b.setPreferredSize(new Dimension(w, 42)); }
    private void styleTextArea(JTextArea t) { t.setBackground(COLOR_INPUT_BG); t.setForeground(Color.WHITE); t.setLineWrap(true); t.setBorder(new EmptyBorder(8,8,8,8)); }
    private TitledBorder createTitledBorder(String t) { TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(Color.DARK_GRAY), t); b.setTitleColor(COLOR_GOLD); b.setTitleFont(new Font("Segoe UI", 1, 10)); return b; }

    private JPanel createOledBox(String t, JLabel usd, JLabel bs, Color c) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(Color.BLACK);
        p.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(5,10,5,10)));
        JLabel title = new JLabel(t); title.setForeground(Color.GRAY); title.setFont(new Font("Segoe UI", 1, 9));
        usd.setFont(FONT_OLED_USD); usd.setForeground(c); usd.setHorizontalAlignment(4);
        bs.setFont(FONT_OLED_BS); bs.setForeground(Color.GRAY); bs.setHorizontalAlignment(4);
        JPanel pV = new JPanel(new GridLayout(2,1)); pV.setOpaque(false); pV.add(usd); pV.add(bs);
        p.add(title, "North"); p.add(pV, "Center"); return p;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(35); t.setBackground(new Color(30,30,30)); t.setForeground(Color.WHITE);
        t.getTableHeader().setBackground(Color.BLACK); t.getTableHeader().setForeground(COLOR_GOLD);
        t.getTableHeader().setFont(new Font("Segoe UI", 1, 12));
        DefaultTableCellRenderer c = new DefaultTableCellRenderer(); c.setHorizontalAlignment(0); c.setOpaque(false);
        DefaultTableCellRenderer r = new DefaultTableCellRenderer(); r.setHorizontalAlignment(4); r.setOpaque(false);
        t.getColumnModel().getColumn(1).setCellRenderer(c); t.getColumnModel().getColumn(2).setCellRenderer(c);
        t.getColumnModel().getColumn(3).setCellRenderer(r); t.getColumnModel().getColumn(4).setCellRenderer(r);
        TableColumn x = t.getColumnModel().getColumn(5); x.setMaxWidth(40); x.setCellRenderer(new XRender());
        t.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if(t.columnAtPoint(e.getPoint())==5) removeFromCart(t.rowAtPoint(e.getPoint())); } });
    }

    // --- CLASES INTERNAS ---
    class XRender extends DefaultTableCellRenderer {
        public XRender() { setHorizontalAlignment(0); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText("X");
            setForeground(COLOR_RED); // <--- AHORA SÍ ENCUENTRA LA VARIABLE
            setFont(new Font("Arial", 1, 16));
            return this;
        }
    }

    private class DocH implements DocumentListener { public void changedUpdate(DocumentEvent e){updateCalculations();} public void removeUpdate(DocumentEvent e){updateCalculations();} public void insertUpdate(DocumentEvent e){updateCalculations();} }

    // --- LÓGICA ---
    private void adjustQty(int v) { try { int q = Integer.parseInt(txtQuantity.getText()); if(q+v>0) txtQuantity.setText(String.valueOf(q+v)); } catch(Exception e){ txtQuantity.setText("1"); } }
    private void clearForm() { tableModel.setRowCount(0); cartDetails.clear(); updateCalculations(); txtDiscount.setText("0"); txtAbono.setText("0.00"); txtRef.setText(""); }
    private void addProductByCode() {
        try {
            Product p = productDAO.getProductById(Integer.parseInt(txtProductCode.getText().trim()));
            if(p != null) { cmbProducts.setSelectedItem(p.getName()); addToCart(); txtProductCode.setText(""); }
        } catch(Exception e){ LuxuryMessage.show("Error", "CÓDIGO INVÁLIDO", true); }
    }
    private void addToCart() {
        if(cmbProducts.getSelectedIndex() <= 0) return;
        Product p = productList.get(cmbProducts.getSelectedIndex()-1);
        int q = Integer.parseInt(txtQuantity.getText());
        double pr = Double.parseDouble(txtPrice.getText().replace(",","."));
        String sz = (String) cmbSize.getSelectedItem();
        for(int i=0; i<tableModel.getRowCount(); i++) {
            if(tableModel.getValueAt(i,0).equals(p.getName()) && tableModel.getValueAt(i,1).equals(sz)) {
                int nQ = (int)tableModel.getValueAt(i,2) + q;
                tableModel.setValueAt(nQ, i, 2); tableModel.setValueAt(String.format(Locale.US, "%.2f", pr*nQ), i, 4);
                cartDetails.get(i).setQuantity(nQ); updateCalculations(); return;
            }
        }
        tableModel.addRow(new Object[]{p.getName(), sz, q, String.format("%.2f", pr), String.format("%.2f", pr*q), "X"});
        cartDetails.add(new SaleDetail("0", String.valueOf(p.getId()), p.getName()+" ["+sz+"]", q, pr));
        updateCalculations();
    }
    private void removeFromCart(int r) { if(r>=0) { cartDetails.remove(r); tableModel.removeRow(r); updateCalculations(); } }
    private void updateCalculations() {
        double sub = cartDetails.stream().mapToDouble(d -> d.getPrice() * d.getQuantity()).sum();
        double d = 0; try { d = Double.parseDouble(txtDiscount.getText()); } catch (Exception e){}
        double net = sub * (1 - (d/100.0));
        double a = 0; try { a = Double.parseDouble(txtAbono.getText().replace(",",".")); } catch(Exception e){}
        totalAmount = net;
        lblTotal.setText(String.format(Locale.US, "$ %.2f", net)); lblTotalBs.setText(String.format(Locale.US, "Bs. %.2f", net * TASA_ACTUAL));
        lblAbonado.setText(String.format(Locale.US, "$ %.2f", a)); lblAbonadoBs.setText(String.format(Locale.US, "Bs. %.2f", a * TASA_ACTUAL));
        lblResta.setText(String.format(Locale.US, "$ %.2f", net - a)); lblRestaBs.setText(String.format(Locale.US, "Bs. %.2f", (net - a) * TASA_ACTUAL));
    }
    private void saveOrder() {
        if(cartDetails.isEmpty()){ LuxuryMessage.show("Aviso", "Pedido vacío", true); return; }
        double ab = Double.parseDouble(txtAbono.getText().replace(",","."));
        Sale s = new Sale("PED-"+System.currentTimeMillis(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), String.valueOf(currentClient.getId()), totalAmount, ab, TASA_ACTUAL, (String)cmbPayMethod.getSelectedItem(), txtRef.getText(), "EN PRODUCCIÓN", txtObservations.getText());
        s.setBalanceDue(totalAmount - ab);
        if(saleController.registerSale(s, cartDetails)){ LuxuryMessage.show("SICONI", "ORDEN GUARDADA", false); SwingUtilities.getWindowAncestor(this).dispose(); }
    }
    private SoftButton createHubBtn(String t, String i, Dimension s) { SoftButton b = new SoftButton(createIcon(i, 22, 22)); b.setText(t); b.setFont(new Font("Segoe UI", 1, 11)); b.setPreferredSize(s); return b; }
    private ImageIcon createIcon(String p, int w, int h) { try { URL u = getClass().getResource(p); if(u!=null) return new ImageIcon(new ImageIcon(u).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)); } catch(Exception e){} return null; }
}