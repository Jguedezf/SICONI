package com.swimcore.view.dialogs;

import com.swimcore.controller.ClientController;
import com.swimcore.model.Client;
import javax.swing.*;
import java.awt.*;

/**
 * Pop-up modal para agregar o editar un cliente.
 * Versión final con formulario completo y lógica de guardado.
 */
public class AddEditClientDialog extends JDialog {

    // --- ATRIBUTOS ---
    private final ClientController controller;

    // Componentes del Formulario
    private JTextField txtId, txtName, txtPhone, txtSecondaryPhone, txtEmail, txtAddress, txtBirthDate, txtMeasurements;
    private JComboBox<String> cmbIdType, cmbClientType;
    private JCheckBox chkVip;

    // Colores
    private final Color COLOR_BACKGROUND = new Color(50, 50, 50);
    private final Color COLOR_TEXT_FIELD_BG = new Color(65, 65, 65);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);
    private final Color COLOR_DORADO = new Color(200, 160, 51);

    // --- CONSTRUCTOR ---
    public AddEditClientDialog(Frame owner) {
        super(owner, "Registrar Nuevo Cliente", true);
        this.controller = new ClientController();

        setSize(550, 700);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createLineBorder(COLOR_DORADO, 1));

        add(createFormPanel(), BorderLayout.CENTER);
        add(createActionsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0; // Contador de filas

        // --- SECCIÓN IDENTIFICACIÓN ---
        gbc.gridx = 0; gbc.gridy = y++; panel.add(createLabel("Tipo Documento"), gbc);
        gbc.gridx = 1; panel.add(createLabel("Número Documento (Opcional)"), gbc);

        gbc.gridx = 0; gbc.gridy = y++;
        cmbIdType = new JComboBox<>(new String[]{"Cédula V.", "Cédula E.", "RIF", "Pasaporte", "DNI", "Otro", "Ninguno"});
        panel.add(cmbIdType, gbc);

        gbc.gridx = 1;
        txtId = createTextField();
        panel.add(txtId, gbc);

        // --- SECCIÓN DATOS PERSONALES ---
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2; panel.add(createLabel("Nombre Completo *"), gbc);
        gbc.gridy = y++;
        txtName = createTextField();
        panel.add(txtName, gbc);

        gbc.gridy = y++; panel.add(createLabel("Fecha Nacimiento (YYYY-MM-DD)"), gbc);
        gbc.gridy = y++;
        txtBirthDate = createTextField();
        panel.add(txtBirthDate, gbc);

        // --- SECCIÓN CONTACTO ---
        gbc.gridy = y++; panel.add(createLabel("Contacto Principal"), gbc);
        gbc.gridy = y++;
        txtPhone = createTextField();
        panel.add(txtPhone, gbc);

        gbc.gridy = y++; panel.add(createLabel("Contacto Secundario (Opcional)"), gbc);
        gbc.gridy = y++;
        txtSecondaryPhone = createTextField();
        panel.add(txtSecondaryPhone, gbc);

        gbc.gridy = y++; panel.add(createLabel("Email"), gbc);
        gbc.gridy = y++;
        txtEmail = createTextField();
        panel.add(txtEmail, gbc);

        gbc.gridy = y++; panel.add(createLabel("Dirección"), gbc);
        gbc.gridy = y++;
        txtAddress = createTextField();
        panel.add(txtAddress, gbc);

        // --- SECCIÓN NEGOCIO ---
        gbc.gridy = y++; gbc.gridwidth = 1; panel.add(createLabel("Tipo de Cliente"), gbc);
        gbc.gridy = y++;
        cmbClientType = new JComboBox<>(new String[]{"Atleta", "Representante", "Club"});
        panel.add(cmbClientType, gbc);

        gbc.gridy = y++; gbc.gridwidth = 2;
        chkVip = new JCheckBox("Cliente VIP (Permitir entrega sin pago 100%)");
        chkVip.setOpaque(false);
        chkVip.setForeground(Color.WHITE);
        panel.add(chkVip, gbc);

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(80, 80, 80)));

        JButton btnSave = new JButton("Guardar Cliente");
        btnSave.setBackground(COLOR_FUCSIA);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnCancel = new JButton("Cancelar");

        panel.add(btnCancel);
        panel.add(btnSave);

        // --- LÓGICA DE EVENTOS ---
        btnSave.addActionListener(e -> saveClient());
        btnCancel.addActionListener(e -> dispose());

        return panel;
    }

    private void saveClient() {
        // 1. Recolectar datos del formulario
        Client client = new Client();
        // Si el usuario no ingresa ID, guardamos un identificador único basado en el tiempo.
        String id = txtId.getText().trim().isEmpty() ? "AUTO_" + System.currentTimeMillis() : txtId.getText().trim();
        client.setId(id);
        client.setName(txtName.getText().trim());
        client.setPhone(txtPhone.getText().trim());
        // Aquí guardarías el teléfono secundario en un campo nuevo en la BD si lo agregas
        client.setEmail(txtEmail.getText().trim());
        client.setType((String) cmbClientType.getSelectedItem());
        client.setBirthDate(txtBirthDate.getText().trim());
        client.setVip(chkVip.isSelected());
        // ... (faltarían campos como dirección, etc, que se agregarían igual)

        // 2. Llamar al controlador para guardar
        if (controller.saveClient(client)) {
            JOptionPane.showMessageDialog(this, "Cliente guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Cierra el pop-up de registro
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar el cliente. El Nombre es obligatorio.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Métodos de utilidad ---
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(20);
        textField.setBackground(COLOR_TEXT_FIELD_BG);
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(COLOR_DORADO);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 90, 90)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return textField;
    }
}