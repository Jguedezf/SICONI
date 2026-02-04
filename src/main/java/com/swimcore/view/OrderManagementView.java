/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: OrderManagementView.java
 * VERSIÓN: 14.0.0 (FINAL GOLD: Payment Logic + Receipt Printing Integrated)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.dao.ClientDAO;
import com.swimcore.dao.PaymentDAO;
import com.swimcore.dao.SaleDAO;
import com.swimcore.model.Client;
import com.swimcore.model.Payment;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.view.components.SoftButton;
import com.swimcore.view.dialogs.AddPaymentDialog;
import com.swimcore.view.dialogs.ReceiptPreviewDialog; // IMPORTANTE: Importamos el Recibo

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

public class OrderManagementView extends JDialog {

    private final ClientDAO clientDAO = new ClientDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    private DefaultTableModel tableModel;
    private JTable ordersTable;
    private JPanel detailsPanel;

    private JLabel lblClientName, lblClientContact;
    private JLabel lblTotalValue, lblPaidValue, lblDueValue, lblStatus;

    private DefaultTableModel productsModel;
    private JTable productsTable;

    private JTextArea paymentHistoryArea;

    private List<Client> clientCache;
    private String selectedSaleId = null;

    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_GOLD_BRIGHT = new Color(255, 215, 0);
    private final Color COLOR_BG_DARK = new Color(20, 20, 20);
    private final Color COLOR_TABLE_BG_1 = new Color(25, 25, 25);
    private final Color COLOR_TABLE_BG_2 = new Color(35, 35, 35);
    private final Color COLOR_GREEN_NEON = new Color(57, 255, 20);
    private final Color COLOR_RED_ALERT = new Color(255, 0, 0);
    private final Color COLOR_PANEL_SOLID = new Color(18, 18, 18);

    public OrderManagementView(Frame owner) {
        super(owner, LanguageManager.get("workshop.title"), true);
        setSize(1280, 720);
        setLocationRelativeTo(owner);


        try {
            setContentPane(new ImagePanel("/images/bg_taller.png"));
        } catch (Exception e) {
            try { setContentPane(new ImagePanel("/images/bg2.png")); }
            catch(Exception ex) { getContentPane().setBackground(COLOR_BG_DARK); }
        }

        setLayout(new BorderLayout());
        ((JPanel)getContentPane()).setBorder(new LineBorder(COLOR_GOLD, 2));

        clientCache = clientDAO.getAllClients();

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadOrders();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new CompoundBorder(
                new EmptyBorder(20, 30, 10, 30),
                new MatteBorder(0, 0, 1, 0, new Color(212, 175, 55, 100))
        ));

        JLabel title = new JLabel(LanguageManager.get("workshop.header").toUpperCase());
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(COLOR_GOLD);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.CENTER);

        SoftButton btnClose = createButton("CERRAR", Color.WHITE, null);
        btnClose.setBackground(new Color(150, 40, 40));
        btnClose.setPreferredSize(new Dimension(120, 40));
        btnClose.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(btnClose);
        header.add(btnPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 30, 30, 30));

        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createDetailsPanel(), BorderLayout.EAST);

        return mainPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"N° PEDIDO", "CLIENTE", "ENTREGA", "TOTAL", "RESTA", "ESTADO"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        ordersTable = new JTable(tableModel);
        styleTable();
        initLuxuryContextMenu();

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = ordersTable.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = ordersTable.convertRowIndexToModel(selectedRow);
                    selectedSaleId = (String) tableModel.getValueAt(modelRow, 0);
                    updateDetailsPanel(selectedSaleId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.getViewport().setBackground(new Color(20,20,20));
        scrollPane.setBorder(new LineBorder(COLOR_GOLD, 1));
        return scrollPane;
    }

    private void initLuxuryContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBackground(new Color(25, 25, 25));
        popupMenu.setBorder(new LineBorder(COLOR_GOLD, 1));

        JMenuItem itemStatus = createLuxuryMenuItem("MARCAR ENTREGADO", COLOR_GREEN_NEON);
        itemStatus.addActionListener(e -> changeStatus("ENTREGADO"));

        JMenuItem itemPending = createLuxuryMenuItem("MARCAR PENDIENTE", Color.WHITE);
        itemPending.addActionListener(e -> changeStatus("PENDIENTE"));

        JMenuItem itemDelete = createLuxuryMenuItem("ELIMINAR PEDIDO", COLOR_RED_ALERT);
        itemDelete.addActionListener(e -> deleteSelectedOrder());

        popupMenu.add(itemStatus);
        popupMenu.add(itemPending);
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(itemDelete);

        ordersTable.setComponentPopupMenu(popupMenu);
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = ordersTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < ordersTable.getRowCount()) {
                        ordersTable.setRowSelectionInterval(row, row);
                    }
                }
            }
        });
    }

    private JMenuItem createLuxuryMenuItem(String text, Color textColor) {
        JMenuItem item = new JMenuItem(text);
        item.setForeground(textColor);
        item.setFont(new Font("Segoe UI", Font.BOLD, 12));
        item.setUI(new LuxuryMenuItemUI());
        return item;
    }

    private static class LuxuryMenuItemUI extends BasicMenuItemUI {
        @Override
        protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
            if (menuItem.isArmed() || menuItem.isSelected()) {
                g.setColor(new Color(60, 60, 60));
                g.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
                g.setColor(new Color(212, 175, 55));
                g.drawRect(0, 0, menuItem.getWidth()-1, menuItem.getHeight()-1);
            } else {
                g.setColor(new Color(25, 25, 25));
                g.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
            }
        }
    }

    private void deleteSelectedOrder() {
        if (selectedSaleId == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿ELIMINAR PEDIDO " + selectedSaleId + "?\nEsta acción borrará todo el historial.",
                "CONFIRMACIÓN SICONI", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (saleDAO.deleteSale(selectedSaleId)) {
                loadOrders();
                selectedSaleId = null;
                clearDetailsPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearDetailsPanel() {
        lblClientName.setText("SELECCIONE PEDIDO");
        lblClientContact.setText("");
        lblTotalValue.setText("$0.00");
        lblPaidValue.setText("$0.00");
        lblDueValue.setText("$0.00");
        productsModel.setRowCount(0);
        paymentHistoryArea.setText("");
        lblStatus.setText("-");
    }

    private void changeStatus(String newStatus) {
        if (selectedSaleId == null) return;
        saleDAO.updateSaleStatus(selectedSaleId, newStatus);
        loadOrders();
        for(int i=0; i<tableModel.getRowCount(); i++) {
            if(tableModel.getValueAt(i,0).equals(selectedSaleId)) {
                ordersTable.setRowSelectionInterval(i,i);
                break;
            }
        }
    }

    private JPanel createDetailsPanel() {
        detailsPanel = new JPanel(new BorderLayout(0, 0));
        detailsPanel.setPreferredSize(new Dimension(380, 0));
        detailsPanel.setBackground(COLOR_PANEL_SOLID);
        detailsPanel.setOpaque(true);
        detailsPanel.setBorder(new LineBorder(COLOR_GOLD, 1));

        JPanel contentInfo = new JPanel();
        contentInfo.setLayout(new BoxLayout(contentInfo, BoxLayout.Y_AXIS));
        contentInfo.setOpaque(false);
        contentInfo.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTituloCliente = createInfoLabel("CLIENTE:", 12, COLOR_GOLD);
        lblTituloCliente.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentInfo.add(lblTituloCliente);

        lblClientName = createInfoLabel("SELECCIONE PEDIDO", 18, Color.WHITE);
        lblClientName.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentInfo.add(lblClientName);

        lblClientContact = createInfoLabel("", 12, Color.LIGHT_GRAY);
        lblClientContact.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentInfo.add(lblClientContact);

        contentInfo.add(Box.createVerticalStrut(20));

        JLabel lblProdTitle = createInfoLabel("ITEMS DEL PEDIDO:", 12, COLOR_GOLD);
        lblProdTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentInfo.add(lblProdTitle);
        contentInfo.add(Box.createVerticalStrut(5));

        String[] prodCols = {"PRODUCTO", "CANT."};
        productsModel = new DefaultTableModel(prodCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        productsTable = new JTable(productsModel);
        styleMiniTable(productsTable);

        JScrollPane scrollProd = new JScrollPane(productsTable);
        scrollProd.setPreferredSize(new Dimension(0, 140));
        scrollProd.setBorder(new LineBorder(Color.DARK_GRAY));
        scrollProd.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentInfo.add(scrollProd);
        contentInfo.add(Box.createVerticalStrut(20));

        JPanel pnlMontos = new JPanel(new GridBagLayout());
        pnlMontos.setOpaque(false);
        pnlMontos.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 0, 2, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        pnlMontos.add(createLabelMonto("TOTAL: "), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        lblTotalValue = createValueMonto("$0.00", COLOR_GOLD);
        pnlMontos.add(lblTotalValue, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        pnlMontos.add(createLabelMonto("ABONADO: "), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        lblPaidValue = createValueMonto("$0.00", COLOR_GREEN_NEON);
        pnlMontos.add(lblPaidValue, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        pnlMontos.add(createLabelMonto("RESTA: "), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        lblDueValue = createValueMonto("$0.00", COLOR_RED_ALERT);
        pnlMontos.add(lblDueValue, gbc);

        contentInfo.add(pnlMontos);
        contentInfo.add(Box.createVerticalStrut(20));

        lblStatus = createInfoLabel("ESTADO: -", 14, Color.CYAN);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentInfo.add(lblStatus);
        contentInfo.add(Box.createVerticalStrut(20));

        JLabel lblHist = createInfoLabel("HISTORIAL DE PAGOS:", 12, COLOR_GOLD);
        lblHist.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentInfo.add(lblHist);
        contentInfo.add(Box.createVerticalStrut(5));

        paymentHistoryArea = new JTextArea("Sin pagos.");
        paymentHistoryArea.setEditable(false);
        paymentHistoryArea.setBackground(Color.BLACK);
        paymentHistoryArea.setForeground(COLOR_GREEN_NEON);
        paymentHistoryArea.setFont(new Font("Consolas", Font.BOLD, 14));
        paymentHistoryArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollHistory = new JScrollPane(paymentHistoryArea);
        scrollHistory.setPreferredSize(new Dimension(0, 100));
        scrollHistory.setBorder(new LineBorder(Color.DARK_GRAY));
        scrollHistory.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentInfo.add(scrollHistory);

        JPanel actionsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        SoftButton btnAddPayment = createButton("REGISTRAR ABONO", Color.BLACK, "/images/icons/icon_money.png");
        btnAddPayment.setBackground(COLOR_GREEN_NEON);
        btnAddPayment.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddPayment.addActionListener(e -> openAddPaymentDialog());

        SoftButton btnPrintReceipt = createButton("IMPRIMIR RECIBO", Color.BLACK, "/images/icons/icon_print.png");
        btnPrintReceipt.setBackground(new Color(200, 200, 200));
        // CONECTADO: AHORA LLAMA AL MÉTODO DE IMPRESIÓN
        btnPrintReceipt.addActionListener(e -> openPrintReceiptDialog());

        actionsPanel.add(btnAddPayment);
        actionsPanel.add(btnPrintReceipt);

        detailsPanel.add(contentInfo, BorderLayout.CENTER);
        detailsPanel.add(actionsPanel, BorderLayout.SOUTH);

        return detailsPanel;
    }

    private void styleMiniTable(JTable t) {
        t.setBackground(Color.BLACK);
        t.setForeground(Color.WHITE);
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0,0));

        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(10,10,10));
        h.setForeground(COLOR_GOLD);
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ((DefaultTableCellRenderer)h.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(Color.BLACK);
        centerRenderer.setForeground(Color.WHITE);

        t.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        t.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        t.getColumnModel().getColumn(0).setPreferredWidth(200);
        t.getColumnModel().getColumn(1).setPreferredWidth(60);
    }

    private void updateDetailsPanel(String saleId) {
        String sql = "SELECT * FROM sales WHERE id = '" + saleId + "'";
        try (Connection conn = com.swimcore.dao.Conexion.conectar()) {

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    int clientId = rs.getInt("client_id");
                    Client client = clientCache.stream().filter(c -> c.getId() == clientId).findFirst().orElse(null);
                    if (client != null) {
                        lblClientName.setText(client.getFullName().toUpperCase());
                        lblClientContact.setText("TLF: " + client.getPhone());
                    } else {
                        lblClientName.setText("CLIENTE DESCONOCIDO");
                    }

                    double total = rs.getDouble("total_divisa");
                    double paid = rs.getDouble("amount_paid_usd");
                    double due = rs.getDouble("balance_due_usd");

                    lblTotalValue.setText(String.format("$%,.2f", total));
                    lblPaidValue.setText(String.format("$%,.2f", paid));
                    lblDueValue.setText(String.format("$%,.2f", due));

                    String statusBD = rs.getString("status");
                    String statusMostrar = "PENDIENTE";
                    Color colorStatus = COLOR_GOLD;

                    if(due <= 0.01) {
                        statusMostrar = "PAGADO";
                        colorStatus = COLOR_GREEN_NEON;
                    } else {
                        statusMostrar = "PENDIENTE";
                        colorStatus = COLOR_RED_ALERT;
                    }
                    if("ENTREGADO".equalsIgnoreCase(statusBD)) {
                        statusMostrar = "ENTREGADO";
                        colorStatus = COLOR_GREEN_NEON;
                    }

                    lblStatus.setText("ESTADO: " + statusMostrar);
                    lblStatus.setForeground(colorStatus);
                }
            }

            productsModel.setRowCount(0);
            String sqlItems = "SELECT p.name, d.quantity FROM sale_details d JOIN products p ON d.product_id = p.id WHERE d.sale_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(sqlItems)) {
                pst.setString(1, saleId);
                ResultSet rsItems = pst.executeQuery();
                while(rsItems.next()) {
                    productsModel.addRow(new Object[]{
                            rsItems.getString("name"),
                            rsItems.getInt("quantity")
                    });
                }
            }

            List<Payment> payments = paymentDAO.getPaymentsForSale(saleId);
            StringBuilder history = new StringBuilder();

            if (payments.isEmpty()){
                history.append("Sin pagos.");
            } else {
                for(Payment p : payments) {
                    String rawDate = p.getPaymentDate();
                    String fechaBonita = rawDate;
                    try {
                        String[] parts = rawDate.split(" ")[0].split("-");
                        fechaBonita = parts[2] + "/" + parts[1] + "/" + parts[0];
                    } catch(Exception e) {}

                    history.append(String.format("%s | Ref:%s | $%.2f\n",
                            fechaBonita,
                            p.getReference(),
                            p.getAmountUSD()));
                }
            }
            paymentHistoryArea.setText(history.toString());
            paymentHistoryArea.setCaretPosition(0);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadOrders() {
        tableModel.setRowCount(0);
        String sql = "SELECT id, client_id, delivery_date, total_divisa, balance_due_usd, status FROM sales ORDER BY date DESC";
        try (Connection conn = com.swimcore.dao.Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("id"));
                int clientId = rs.getInt("client_id");
                String clientName = clientCache.stream().filter(c -> c.getId() == clientId)
                        .map(Client::getFullName).findFirst().orElse("N/A");
                row.add(clientName);

                String rawDate = rs.getString("delivery_date");
                String fechaBonita = rawDate;
                try {
                    String[] parts = rawDate.split("-");
                    fechaBonita = parts[2] + "/" + parts[1] + "/" + parts[0];
                } catch(Exception e) {}
                row.add(fechaBonita);

                row.add(rs.getDouble("total_divisa"));
                row.add(rs.getDouble("balance_due_usd"));

                String statusBD = rs.getString("status");
                double due = rs.getDouble("balance_due_usd");
                String shortStatus = "PENDIENTE";
                if(due <= 0.01) shortStatus = "PAGADO";
                if("ENTREGADO".equalsIgnoreCase(statusBD)) shortStatus = "ENTREGADO";
                row.add(shortStatus);

                tableModel.addRow(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- MÉTODOS PARA DIÁLOGOS (Pago e Impresión) ---

    private void openAddPaymentDialog() {
        if (selectedSaleId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel glass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glass.setOpaque(false);
        this.setGlassPane(glass);
        glass.setVisible(true);

        AddPaymentDialog dialog = new AddPaymentDialog(this, selectedSaleId);
        dialog.setVisible(true);

        glass.setVisible(false);

        loadOrders();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(selectedSaleId)) {
                ordersTable.setRowSelectionInterval(i, i);
                updateDetailsPanel(selectedSaleId);
                break;
            }
        }
    }

    // --- NUEVO: METODO PARA IMPRIMIR RECIBO ---
    private void openPrintReceiptDialog() {
        if (selectedSaleId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para imprimir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Efecto Oscuro
        JPanel glass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glass.setOpaque(false);
        this.setGlassPane(glass);
        glass.setVisible(true);

        // 2. Abrir Vista Previa (ReceiptPreviewDialog)
        ReceiptPreviewDialog dialog = new ReceiptPreviewDialog(this, selectedSaleId);
        dialog.setVisible(true);

        // 3. Quitar Oscuro
        glass.setVisible(false);
    }

    private void styleTable() {
        ordersTable.setRowHeight(45);
        ordersTable.setBackground(COLOR_TABLE_BG_1);
        ordersTable.setForeground(Color.WHITE);
        ordersTable.setSelectionBackground(COLOR_GOLD_BRIGHT);
        ordersTable.setSelectionForeground(Color.BLACK);
        ordersTable.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ordersTable.setShowVerticalLines(false);
        ordersTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = ordersTable.getTableHeader();
        header.setBackground(new Color(10, 10, 10));
        header.setForeground(COLOR_GOLD);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(new LineBorder(COLOR_GOLD, 1));

        TableColumnModel cm = ordersTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(140);
        cm.getColumn(1).setPreferredWidth(220);
        cm.getColumn(2).setPreferredWidth(100);
        cm.getColumn(3).setPreferredWidth(80);
        cm.getColumn(4).setPreferredWidth(80);
        cm.getColumn(5).setPreferredWidth(130);

        ordersTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_TABLE_BG_1 : COLOR_TABLE_BG_2);
                    c.setForeground(Color.WHITE);
                }

                if (col == 1) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                if (col == 3 || col == 4) {
                    if (value instanceof Number) setText(String.format("%,.2f", (Double) value));
                    if (col == 4 && !isSelected) {
                        double due = (value instanceof Number) ? ((Number) value).doubleValue() : 0.0;
                        setForeground(due > 0.01 ? COLOR_RED_ALERT : COLOR_GREEN_NEON);
                    }
                }
                else if (col == 5) {
                    if(!isSelected) {
                        String status = value.toString();
                        if(status.equalsIgnoreCase("ENTREGADO") || status.equalsIgnoreCase("PAGADO")) setForeground(COLOR_GREEN_NEON);
                        else setForeground(COLOR_RED_ALERT);
                    }
                }

                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    private JLabel createInfoLabel(String text, int fontSize, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    private JLabel createLabelMonto(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(Color.GRAY);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    private JLabel createValueMonto(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(c);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    private SoftButton createButton(String text, Color textColor, String iconPath) {
        SoftButton btn = new SoftButton(null);
        btn.setText(text);
        btn.setForeground(textColor);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btn.setContentAreaFilled(false);
        btn.setOpaque(false);

        try {
            if(iconPath != null) {
                URL url = getClass().getResource(iconPath);
                if (url != null) btn.setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH)));
            }
        } catch (Exception e) {}
        return btn;
    }
}