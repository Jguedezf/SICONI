package com.swimcore.view.dialogs;

import com.swimcore.dao.SupplierDAO;
import com.swimcore.model.Supplier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.util.List;

public class SupplierManagementDialog extends JDialog {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private DefaultTableModel model;
    private JTable table;

    private JTextField txtCompany, txtContact, txtPhone, txtEmail, txtAddress;
    // NUEVOS CAMPOS SOCIALES
    private JTextField txtInstagram, txtWhatsapp;

    private int selectedId = 0;

    public SupplierManagementDialog(Window parent) {
        super(parent, "Gestión de Proveedores", ModalityType.APPLICATION_MODAL);
        setSize(1000, 650);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(20, 20, 20)); // Fondo oscuro
        setLayout(new BorderLayout());

        // --- TÍTULO ---
        JLabel lblTitle = new JLabel("DIRECTORIO DE PROVEEDORES & CONEXIÓN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(255, 140, 0)); // Naranja
        lblTitle.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(new Color(20, 20, 20));
        centerPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        // --- IZQUIERDA: TABLA ---
        String[] cols = {"ID", "EMPRESA", "INSTAGRAM"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int row, int col) { return false; } };
        table = new JTable(model);
        table.setRowHeight(30);
        table.getSelectionModel().addListSelectionListener(e -> cargarDatosDeFila());
        centerPanel.add(new JScrollPane(table));

        // --- DERECHA: FORMULARIO ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(35, 35, 35));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;

        txtCompany = addSimpleField(formPanel, "Empresa:", gbc);
        txtContact = addSimpleField(formPanel, "Contacto:", gbc);
        txtPhone = addSimpleField(formPanel, "Teléfono (Local):", gbc);

        // --- WHATSAPP CON BOTÓN ---
        txtWhatsapp = addSocialField(formPanel, "WhatsApp (Ej: 58414...):", "https://wa.me/", gbc, new Color(37, 211, 102));

        // --- INSTAGRAM CON BOTÓN ---
        txtInstagram = addSocialField(formPanel, "Instagram (Usuario):", "https://instagram.com/", gbc, new Color(225, 48, 108));

        txtAddress = addSimpleField(formPanel, "Dirección:", gbc);

        // BOTONES DE ACCIÓN
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setOpaque(false);

        JButton btnSave = new JButton("GUARDAR");
        btnSave.setBackground(new Color(255, 140, 0));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> guardar());

        JButton btnNew = new JButton("NUEVO");
        btnNew.addActionListener(e -> limpiarFormulario());

        JButton btnDel = new JButton("ELIMINAR");
        btnDel.setBackground(Color.RED);
        btnDel.setForeground(Color.WHITE);
        btnDel.addActionListener(e -> eliminar());

        btnPanel.add(btnNew); btnPanel.add(btnDel); btnPanel.add(btnSave);
        gbc.gridy++; formPanel.add(btnPanel, gbc);

        centerPanel.add(formPanel);
        add(centerPanel, BorderLayout.CENTER);

        cargarTabla();
    }

    // Auxiliar para campos normales
    private JTextField addSimpleField(JPanel p, String label, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(label); lbl.setForeground(Color.GRAY);
        p.add(lbl, gbc);
        gbc.gridy++;
        JTextField txt = new JTextField(); txt.setPreferredSize(new Dimension(200, 30));
        p.add(txt, gbc);
        gbc.gridy++;
        return txt;
    }

    // Auxiliar para campos con botón de enlace (WAOOO)
    private JTextField addSocialField(JPanel p, String label, String urlPrefix, GridBagConstraints gbc, Color btnColor) {
        JLabel lbl = new JLabel(label); lbl.setForeground(Color.GRAY);
        p.add(lbl, gbc);
        gbc.gridy++;

        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);

        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(150, 30));

        JButton btnLink = new JButton("🔗 IR");
        btnLink.setBackground(btnColor);
        btnLink.setForeground(Color.WHITE);
        btnLink.setPreferredSize(new Dimension(60, 30));
        btnLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ACCIÓN: ABRIR NAVEGADOR
        btnLink.addActionListener(e -> {
            String val = txt.getText().trim();
            if(!val.isEmpty()) {
                try {
                    Desktop.getDesktop().browse(new URI(urlPrefix + val));
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "No se pudo abrir el enlace."); }
            }
        });

        row.add(txt, BorderLayout.CENTER);
        row.add(btnLink, BorderLayout.EAST);
        p.add(row, gbc);
        gbc.gridy++;
        return txt;
    }

    private void cargarTabla() {
        model.setRowCount(0);
        List<Supplier> list = supplierDAO.getAll();
        for (Supplier s : list) model.addRow(new Object[]{s.getId(), s.getCompany(), s.getInstagram()});
    }

    private void cargarDatosDeFila() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int id = (int) model.getValueAt(row, 0);
            selectedId = id;
            List<Supplier> list = supplierDAO.getAll();
            for(Supplier s : list) {
                if(s.getId() == id) {
                    txtCompany.setText(s.getCompany());
                    txtContact.setText(s.getContact());
                    txtPhone.setText(s.getPhone());
                    txtEmail.setText(s.getEmail());
                    txtAddress.setText(s.getAddress());
                    // CARGAMOS LOS DATOS SOCIALES
                    txtInstagram.setText(s.getInstagram());
                    txtWhatsapp.setText(s.getWhatsapp());
                    break;
                }
            }
        }
    }

    private void limpiarFormulario() {
        selectedId = 0;
        txtCompany.setText(""); txtContact.setText(""); txtPhone.setText(""); txtEmail.setText(""); txtAddress.setText("");
        txtInstagram.setText(""); txtWhatsapp.setText("");
        table.clearSelection();
    }

    private void guardar() {
        // AQUÍ SE SOLUCIONA EL ERROR: PASAMOS LOS 8 DATOS AL CONSTRUCTOR
        Supplier s = new Supplier(selectedId, txtCompany.getText(), txtContact.getText(), txtPhone.getText(), txtEmail.getText(), txtAddress.getText(), txtInstagram.getText(), txtWhatsapp.getText());

        if(supplierDAO.save(s)) {
            JOptionPane.showMessageDialog(this, "Guardado!");
            cargarTabla();
            limpiarFormulario();
        }
    }

    private void eliminar() {
        if(selectedId != 0 && JOptionPane.showConfirmDialog(this, "¿Eliminar?") == JOptionPane.YES_OPTION) {
            supplierDAO.delete(selectedId);
            cargarTabla();
            limpiarFormulario();
        }
    }
}