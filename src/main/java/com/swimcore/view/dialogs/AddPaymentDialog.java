/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: AddPaymentDialog.java
 * VERSIÓN: 1.0.0 (Initial Release)
 * DESCRIPCIÓN: Diálogo modal para registrar un nuevo pago (abono o pago final)
 * a un pedido existente.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.PaymentDAO;
import com.swimcore.model.Payment;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPaymentDialog extends JDialog {

    private final String saleId;
    private final PaymentDAO paymentDAO;
    private double balanceDue = 0.0;

    // UI Components
    private JTextField txtAmount, txtReference;
    private JComboBox<String> cmbMethod;
    private JTextArea txtNotes;
    private JLabel lblBalance;

    // --- PALETA DE COLORES LUXURY ---
    private final Color COLOR_BG = new Color(25, 25, 25);
    private final Color COLOR_INPUT = new Color(45, 45, 45);
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_GREEN = new Color(0, 200, 83);

    public AddPaymentDialog(Dialog owner, String saleId) {
        super(owner, "Registrar Pago para Pedido: " + saleId, true);
        this.saleId = saleId;
        this.paymentDAO = new PaymentDAO();

        setSize(450, 550);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        getRootPane().setBorder(new LineBorder(COLOR_GOLD, 2));
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        loadInitialData(); // Cargar el saldo pendiente antes de construir la UI

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private void loadInitialData() {
        String sql = "SELECT balance_due_usd FROM sales WHERE id = '" + this.saleId + "'";
        try (Connection conn = com.swimcore.dao.Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                this.balanceDue = rs.getDouble("balance_due_usd");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar datos del pedido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel title = new JLabel("REGISTRAR PAGO", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_GOLD);

        lblBalance = new JLabel(String.format("Saldo Pendiente: $%.2f", this.balanceDue), SwingConstants.CENTER);
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBalance.setForeground(Color.RED);

        header.add(title, BorderLayout.NORTH);
        header.add(lblBalance, BorderLayout.CENTER);
        return header;
    }

    private JPanel createForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        // Monto
        formPanel.add(createLabel("MONTO A ABONAR ($):"), gbc);
        txtAmount = createTextField();
        txtAmount.setFont(new Font("Segoe UI", Font.BOLD, 24));
        txtAmount.setHorizontalAlignment(JTextField.CENTER);
        txtAmount.setText(String.format("%.2f", this.balanceDue)); // Sugiere pagar el total
        formPanel.add(txtAmount, gbc);

        // Método de Pago
        formPanel.add(createLabel("MÉTODO DE PAGO:"), gbc);
        cmbMethod = new JComboBox<>(new String[]{"PAGO MÓVIL", "TRANSFERENCIA", "ZELLE", "EFECTIVO", "OTRO"});
        styleComboBox(cmbMethod);
        formPanel.add(cmbMethod, gbc);

        // Referencia
        formPanel.add(createLabel("N° DE REFERENCIA:"), gbc);
        txtReference = createTextField();
        formPanel.add(txtReference, gbc);

        // Notas
        formPanel.add(createLabel("NOTAS (OPCIONAL):"), gbc);
        txtNotes = new JTextArea(3, 20);
        styleTextArea(txtNotes);
        formPanel.add(new JScrollPane(txtNotes), gbc);

        return formPanel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 20, 20, 20));

        SoftButton btnCancel = new SoftButton(null);
        btnCancel.setText("CANCELAR");
        btnCancel.setPreferredSize(new Dimension(140, 50));
        btnCancel.addActionListener(e -> { SoundManager.getInstance().playClick(); dispose(); });

        SoftButton btnSave = new SoftButton(null);
        btnSave.setText("GUARDAR PAGO");
        btnSave.setPreferredSize(new Dimension(180, 50));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setForeground(COLOR_GREEN);
        btnSave.addActionListener(e -> savePayment());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    private void savePayment() {
        double amount;
        try {
            amount = Double.parseDouble(txtAmount.getText().replace(',', '.'));
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser un número positivo.", "Dato Inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un monto numérico válido.", "Dato Inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String method = (String) cmbMethod.getSelectedItem();
        String reference = txtReference.getText().trim();
        String notes = txtNotes.getText().trim();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        Payment newPayment = new Payment(this.saleId, currentDate, amount, method, reference, notes);

        if (paymentDAO.savePaymentAndUpdateSale(newPayment)) {
            SoundManager.getInstance().playClick();
            JOptionPane.showMessageDialog(this, "¡Pago registrado exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            SoundManager.getInstance().playError();
            JOptionPane.showMessageDialog(this, "Ocurrió un error al guardar el pago en la base de datos.", "Error de Persistencia", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- HELPERS DE ESTILO ---
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.GRAY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(COLOR_INPUT);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(COLOR_GOLD);
        tf.setBorder(new EmptyBorder(8, 8, 8, 8));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return tf;
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(COLOR_INPUT);
        cb.setForeground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setPreferredSize(new Dimension(0, 40));
    }

    private void styleTextArea(JTextArea ta) {
        ta.setBackground(COLOR_INPUT);
        ta.setForeground(Color.WHITE);
        ta.setCaretColor(COLOR_GOLD);
        ta.setBorder(new EmptyBorder(8, 8, 8, 8));
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
    }
}