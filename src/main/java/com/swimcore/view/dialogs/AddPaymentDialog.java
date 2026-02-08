/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: AddPaymentDialog.java
 * VERSIÓN: 5.0.0 (FINAL FIX: Focus & Colors)
 * FECHA: 06 de Febrero de 2026
 * HORA: 10:30 PM (Hora de Venezuela)
 * DESCRIPCIÓN TÉCNICA:
 * Diálogo modal para el registro de transacciones financieras (Abonos).
 * Implementa lógica de validación de montos y actualización atómica del saldo
 * deudor de la orden de venta asociada.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.PaymentDAO;
import com.swimcore.model.Payment;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * [VISTA - PAGOS] Clase que gestiona el formulario de ingreso de pagos.
 * [POO - HERENCIA] Extiende de JDialog para garantizar la modalidad (Atención exclusiva).
 * * FUNCIONALIDAD: Procesamiento de cobros y actualización de estados financieros.
 */
public class AddPaymentDialog extends JDialog {

    // Identificador único de la venta asociada (FK Lógica)
    private final String saleId;

    // [PATRÓN DAO] Acceso a la capa de datos de pagos.
    private final PaymentDAO paymentDAO;

    // Estado financiero actual de la orden (Deuda pendiente)
    private double balanceDue = 0.0;

    // Componentes de la interfaz gráfica
    private JTextField txtAmount, txtReference;
    private JComboBox<String> cmbMethod;
    private JTextArea txtNotes;
    private JLabel lblBalance;

    // Constantes de diseño (Identidad Visual)
    private final Color COLOR_BG_DARK = new Color(18, 18, 18);
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_INPUT_BG = new Color(10, 10, 10);
    private final Color COLOR_TEXT_WHITE = new Color(240, 240, 240);
    private final Color COLOR_RED_ALERT = new Color(255, 0, 0);
    private final Color COLOR_GREEN_NEON = new Color(57, 255, 20);

    /**
     * Constructor de la clase. Inicializa la ventana y recupera el saldo actual.
     * @param owner Ventana padre.
     * @param saleId Código de la venta a abonar.
     */
    public AddPaymentDialog(Dialog owner, String saleId) {
        super(owner, "SICONI - Registrar Pago", true);
        this.saleId = saleId;
        this.paymentDAO = new PaymentDAO();

        // Configuración de dimensiones optimizadas
        setSize(500, 620);
        setLocationRelativeTo(owner);

        getRootPane().setBorder(new LineBorder(COLOR_GOLD, 2));

        // Fondo personalizado con manejo de excepciones
        try {
            setContentPane(new ImagePanel("/images/bg_modal_luxury.png"));
        } catch (Exception e) {
            getContentPane().setBackground(COLOR_BG_DARK);
        }

        setLayout(new BorderLayout());

        // Recuperación de datos desde la BD
        loadInitialData();

        // Construcción de la UI
        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        // [CORRECCIÓN UX: ANTI-BLUE FOCUS]
        // Se utiliza invokeLater para garantizar que el foco se establezca
        // DESPUÉS de que Swing haya terminado de renderizar la ventana.
        // Esto evita que el texto aparezca seleccionado (azul) automáticamente.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    txtAmount.requestFocusInWindow();
                    // Coloca el cursor al final del texto pre-cargado
                    txtAmount.setCaretPosition(txtAmount.getText().length());
                });
            }
        });
    }

    /**
     * [CONSULTA SQL] Recupera el saldo pendiente (balance_due_usd) directamente de la tabla de ventas.
     * Garantiza que el usuario vea el monto exacto a pagar.
     */
    private void loadInitialData() {
        String sql = "SELECT balance_due_usd FROM sales WHERE id = '" + this.saleId + "'";
        try (Connection conn = com.swimcore.dao.Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                this.balanceDue = rs.getDouble("balance_due_usd");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 20, 5, 20));

        JLabel title = new JLabel("REGISTRAR PAGO", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(COLOR_GOLD);

        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        balancePanel.setOpaque(false);
        JLabel lblText = new JLabel("DEUDA ACTUAL: ");
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblText.setForeground(Color.LIGHT_GRAY);

        lblBalance = new JLabel(String.format("$%.2f", this.balanceDue));
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblBalance.setForeground(COLOR_RED_ALERT);

        balancePanel.add(lblText);
        balancePanel.add(lblBalance);

        header.add(title, BorderLayout.NORTH);
        header.add(balancePanel, BorderLayout.CENTER);

        return header;
    }

    private JPanel createForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(5, 40, 5, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        // Campo de Monto (Resaltado visualmente)
        formPanel.add(createLabel("MONTO A ABONAR ($):"), gbc);
        txtAmount = new JTextField();
        styleBigInput(txtAmount);
        txtAmount.setText(String.format("%.2f", this.balanceDue)); // Pre-carga de la deuda total
        formPanel.add(txtAmount, gbc);

        // Selector de Método de Pago
        formPanel.add(createLabel("MÉTODO DE PAGO:"), gbc);
        cmbMethod = new JComboBox<>(new String[]{"PAGO MÓVIL", "TRANSFERENCIA", "ZELLE", "EFECTIVO ($)", "EFECTIVO (Bs)", "OTRO"});
        styleComboBox(cmbMethod);
        formPanel.add(cmbMethod, gbc);

        // Campo de Referencia
        formPanel.add(createLabel("REFERENCIA / RECIBO:"), gbc);
        txtReference = new JTextField();
        styleNormalInput(txtReference);
        formPanel.add(txtReference, gbc);

        // Campo de Notas (JTextArea con Scroll)
        formPanel.add(createLabel("NOTAS O DETALLES:"), gbc);
        txtNotes = new JTextArea(5, 20);
        styleTextArea(txtNotes);

        JScrollPane scrollNotes = new JScrollPane(txtNotes);
        scrollNotes.setBorder(new LineBorder(new Color(60,60,60)));
        scrollNotes.setPreferredSize(new Dimension(0, 90));
        scrollNotes.setMinimumSize(new Dimension(0, 90));

        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollNotes, gbc);

        return formPanel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new GridLayout(1, 2, 20, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 40, 30, 40));

        // Botón Cancelar
        SoftButton btnCancel = new SoftButton(null);
        btnCancel.setText("CANCELAR");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBackground(Color.BLACK);
        btnCancel.setPreferredSize(new Dimension(0, 55));
        btnCancel.setContentAreaFilled(false);
        btnCancel.setOpaque(false);
        btnCancel.setBorder(new LineBorder(new Color(150, 40, 40), 2));
        btnCancel.addActionListener(e -> { SoundManager.getInstance().playClick(); dispose(); });

        // Botón Confirmar (Estilo Neón)
        SoftButton btnSave = new SoftButton(null);
        btnSave.setText("PROCESAR PAGO");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setForeground(COLOR_GREEN_NEON);
        btnSave.setBackground(Color.BLACK);
        btnSave.setPreferredSize(new Dimension(0, 55));
        btnSave.setContentAreaFilled(false);
        btnSave.setOpaque(false);
        btnSave.setBorder(new LineBorder(COLOR_GREEN_NEON, 2));
        btnSave.addActionListener(e -> savePayment());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    /**
     * [LÓGICA TRANSACCIONAL]
     * Valida la entrada de datos, construye el objeto Payment y solicita
     * al DAO la persistencia y actualización del saldo de la venta.
     */
    private void savePayment() {
        double amount;
        try {
            // Normalización de separadores decimales
            amount = Double.parseDouble(txtAmount.getText().replace(',', '.'));

            // Regla de Negocio 1: Montos positivos
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser positivo.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Regla de Negocio 2: Advertencia sobre pago excedente
            if (amount > this.balanceDue + 0.99) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "El monto supera la deuda. ¿Registrar como saldo a favor?",
                        "Exceso", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto inválido.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String method = (String) cmbMethod.getSelectedItem();
        String reference = txtReference.getText().trim();
        if(reference.isEmpty()) reference = "S/R"; // Valor por defecto "Sin Referencia"

        String notes = txtNotes.getText().trim();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // Creación del DTO
        Payment newPayment = new Payment(this.saleId, currentDate, amount, method, reference, notes);

        // Delegación al DAO (Operación Atómica)
        if (paymentDAO.savePaymentAndUpdateSale(newPayment)) {
            SoundManager.getInstance().playClick();
            JOptionPane.showMessageDialog(this, "¡PAGO REGISTRADO CON ÉXITO!", "SICONI", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            SoundManager.getInstance().playError();
            JOptionPane.showMessageDialog(this, "Error de base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- MÉTODOS DE ESTILIZACIÓN (LOOK & FEEL) ---

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(COLOR_GOLD);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }

    private void styleBigInput(JTextField tf) {
        tf.setBackground(COLOR_INPUT_BG);
        tf.setForeground(COLOR_GREEN_NEON);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.BOLD, 36));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setPreferredSize(new Dimension(0, 60));
        tf.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, COLOR_GOLD),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleNormalInput(JTextField tf) {
        tf.setBackground(COLOR_INPUT_BG);
        tf.setForeground(COLOR_TEXT_WHITE);
        tf.setCaretColor(COLOR_GOLD);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setPreferredSize(new Dimension(0, 40));
        tf.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Color.GRAY),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(COLOR_INPUT_BG);
        cb.setForeground(COLOR_TEXT_WHITE);
        cb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cb.setBorder(new LineBorder(Color.DARK_GRAY));
        cb.setPreferredSize(new Dimension(0, 40));
        ((JComponent) cb.getRenderer()).setOpaque(true);
    }

    private void styleTextArea(JTextArea ta) {
        ta.setBackground(COLOR_INPUT_BG);
        ta.setForeground(COLOR_TEXT_WHITE);
        ta.setCaretColor(COLOR_GOLD);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(10, 10, 10, 10));
    }
}