/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: OrderManagementView.java
 * VERSIÓN: 1.2.0 (UI Polish & i18n)
 * DESCRIPCIÓN: Vista principal para la gestión de pedidos del taller.
 * Se añade botón de cierre y se integra la internacionalización.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.dao.ClientDAO;
import com.swimcore.dao.PaymentDAO;
import com.swimcore.model.Client;
import com.swimcore.model.Payment;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.view.components.SoftButton;
import com.swimcore.view.dialogs.AddPaymentDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

public class OrderManagementView extends JDialog {

    private final ClientDAO clientDAO = new ClientDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    private DefaultTableModel tableModel;
    private JTable ordersTable;
    private JPanel detailsPanel;
    private JLabel lblClientName, lblClientContact, lblTotalValue, lblPaidValue, lblDueValue, lblStatus;
    private JTextArea paymentHistoryArea;

    private List<Client> clientCache;
    private String selectedSaleId = null;

    private final Color COLOR_PANEL = new Color(30, 30, 30);
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_GREEN = new Color(0, 200, 83);
    private final Color COLOR_RED = new Color(213, 0, 0);

    public OrderManagementView(Frame owner) {
        super(owner, LanguageManager.get("workshop.title"), true);
        setSize(1280, 720);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        clientCache = clientDAO.getAllClients();

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadOrders();
    }

    private JPanel createHeader() {
        JPanel header = new ImagePanel("/images/bg_header.png");
        header.setLayout(new BorderLayout()); // BorderLayout para alinear el botón de cierre
        header.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel title = new JLabel(LanguageManager.get("workshop.header"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(COLOR_GOLD);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.CENTER);

        // --- BOTÓN DE CIERRE ---
        SoftButton btnClose = new SoftButton(null);
        btnClose.setText(LanguageManager.get("workshop.button.close"));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.setPreferredSize(new Dimension(180, 40));
        btnClose.addActionListener(e -> dispose());
        header.add(btnClose, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setOpaque(true);
        mainPanel.setBackground(new Color(20,20,20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createDetailsPanel(), BorderLayout.EAST);

        return mainPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {
                LanguageManager.get("workshop.table.orderNo"), LanguageManager.get("workshop.table.client"),
                LanguageManager.get("workshop.table.deliveryDate"), LanguageManager.get("workshop.table.total"),
                LanguageManager.get("workshop.table.due"), LanguageManager.get("workshop.table.status")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ordersTable = new JTable(tableModel);
        styleTable();

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = ordersTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedSaleId = (String) tableModel.getValueAt(selectedRow, 0);
                    updateDetailsPanel(selectedSaleId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.getViewport().setBackground(COLOR_PANEL);
        scrollPane.setBorder(new LineBorder(Color.DARK_GRAY));
        return scrollPane;
    }

    private JPanel createDetailsPanel() {
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setPreferredSize(new Dimension(350, 0));
        detailsPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(COLOR_PANEL);
        infoPanel.setBorder(createTitledBorder(LanguageManager.get("workshop.details.title")));

        lblClientName = createInfoLabel(LanguageManager.get("workshop.details.select"), 16, Color.WHITE);
        lblClientContact = createInfoLabel("", 12, Color.LIGHT_GRAY);
        infoPanel.add(lblClientName);
        infoPanel.add(lblClientContact);
        infoPanel.add(Box.createVerticalStrut(20));

        lblTotalValue = createInfoLabel(LanguageManager.get("workshop.details.total") + " $0.00", 20, COLOR_GOLD);
        lblPaidValue = createInfoLabel(LanguageManager.get("workshop.details.paid") + " $0.00", 18, COLOR_GREEN);
        lblDueValue = createInfoLabel(LanguageManager.get("workshop.details.due") + " $0.00", 24, COLOR_RED);
        infoPanel.add(lblTotalValue);
        infoPanel.add(lblPaidValue);
        infoPanel.add(lblDueValue);
        infoPanel.add(Box.createVerticalStrut(10));

        lblStatus = createInfoLabel(LanguageManager.get("workshop.details.status") + " -", 14, Color.CYAN);
        infoPanel.add(lblStatus);

        paymentHistoryArea = new JTextArea(LanguageManager.get("workshop.paymentHistory.empty"));
        paymentHistoryArea.setEditable(false);
        paymentHistoryArea.setBackground(new Color(35, 35, 35));
        paymentHistoryArea.setForeground(Color.WHITE);
        paymentHistoryArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        paymentHistoryArea.setBorder(new EmptyBorder(10,10,10,10));
        JScrollPane scrollHistory = new JScrollPane(paymentHistoryArea);
        scrollHistory.setBorder(createTitledBorder(LanguageManager.get("workshop.paymentHistory.title")));

        JPanel actionsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        SoftButton btnAddPayment = new SoftButton(null);
        btnAddPayment.setText(LanguageManager.get("workshop.button.addPayment"));
        btnAddPayment.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddPayment.addActionListener(e -> openAddPaymentDialog());

        SoftButton btnPrintReceipt = new SoftButton(null);
        btnPrintReceipt.setText(LanguageManager.get("workshop.button.printReceipt"));
        btnPrintReceipt.setFont(new Font("Segoe UI", Font.BOLD, 14));

        actionsPanel.add(btnAddPayment);
        actionsPanel.add(btnPrintReceipt);

        detailsPanel.add(infoPanel, BorderLayout.NORTH);
        detailsPanel.add(scrollHistory, BorderLayout.CENTER);
        detailsPanel.add(actionsPanel, BorderLayout.SOUTH);

        return detailsPanel;
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
                String clientName = clientCache.stream()
                        .filter(c -> c.getId() == clientId)
                        .map(Client::getFullName)
                        .findFirst()
                        .orElse("N/A");
                row.add(clientName);
                row.add(rs.getString("delivery_date"));
                row.add(rs.getDouble("total_divisa"));
                row.add(rs.getDouble("balance_due_usd"));
                row.add(rs.getString("status"));
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDetailsPanel(String saleId) {
        String sql = "SELECT * FROM sales WHERE id = '" + saleId + "'";
        try (Connection conn = com.swimcore.dao.Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int clientId = rs.getInt("client_id");
                Client client = clientCache.stream().filter(c -> c.getId() == clientId).findFirst().orElse(null);
                if (client != null) {
                    lblClientName.setText(client.getFullName());
                    lblClientContact.setText("Tlf: " + client.getPhone());
                } else {
                    lblClientName.setText("Cliente no encontrado");
                    lblClientContact.setText("");
                }
                lblTotalValue.setText(String.format(LanguageManager.get("workshop.details.total") + " $%.2f", rs.getDouble("total_divisa")));
                lblPaidValue.setText(String.format(LanguageManager.get("workshop.details.paid") + " $%.2f", rs.getDouble("amount_paid_usd")));
                lblDueValue.setText(String.format(LanguageManager.get("workshop.details.due") + " $%.2f", rs.getDouble("balance_due_usd")));
                lblStatus.setText(LanguageManager.get("workshop.details.status") + " " + rs.getString("status"));
                List<Payment> payments = paymentDAO.getPaymentsForSale(saleId);
                StringBuilder history = new StringBuilder(LanguageManager.get("workshop.paymentHistory.title") + "\n---------------------\n");
                if (payments.isEmpty()){
                    history.append(LanguageManager.get("workshop.paymentHistory.empty"));
                } else {
                    for(Payment p : payments) {
                        history.append(String.format("%s | %s | $%.2f | Ref: %s\n",
                                p.getPaymentDate().substring(0, 10), p.getPaymentMethod(), p.getAmountUSD(), p.getReference()));
                    }
                }
                paymentHistoryArea.setText(history.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openAddPaymentDialog() {
        if (selectedSaleId == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un pedido de la lista.", "Acción Requerida", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        AddPaymentDialog dialog = new AddPaymentDialog(this, selectedSaleId);
        dialog.setVisible(true);
        loadOrders();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(selectedSaleId)) {
                ordersTable.setRowSelectionInterval(i, i);
                updateDetailsPanel(selectedSaleId);
                break;
            }
        }
    }

    private void styleTable() {
        ordersTable.setRowHeight(35);
        ordersTable.setBackground(COLOR_PANEL);
        ordersTable.setForeground(Color.WHITE);
        ordersTable.setSelectionBackground(COLOR_GOLD);
        ordersTable.setSelectionForeground(Color.BLACK);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ordersTable.getTableHeader().setBackground(new Color(10, 10, 10));
        ordersTable.getTableHeader().setForeground(COLOR_GOLD);
        ordersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        ordersTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    Object dueValue = table.getValueAt(row, 4);
                    double due = (dueValue instanceof Number) ? ((Number) dueValue).doubleValue() : 0.0;
                    if (due > 0.01) {
                        setBackground(new Color(50, 30, 30));
                    } else {
                        setBackground(new Color(30, 50, 30));
                    }
                    setForeground(Color.WHITE);
                }
                if(col == 3 || col == 4){
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    if (value instanceof Number) {
                        setText(String.format("$%.2f", (Double) value));
                    }
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return this;
            }
        });
    }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder b = BorderFactory.createTitledBorder(
                new LineBorder(Color.DARK_GRAY), " " + title + " ");
        b.setTitleColor(Color.WHITE);
        b.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    private JLabel createInfoLabel(String text, int fontSize, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        label.setForeground(color);
        label.setBorder(new EmptyBorder(5, 10, 5, 10));
        return label;
    }
}