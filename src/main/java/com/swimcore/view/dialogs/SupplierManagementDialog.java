/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: SupplierManagementDialog.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.4.5 (Rectangular Button Standard & Compact UI)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.SupplierDAO;
import com.swimcore.model.Supplier;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.util.List;

public class SupplierManagementDialog extends JDialog {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private DefaultTableModel model;
    private JTable table;

    private JTextField txtCompany, txtContact, txtPhone, txtEmail, txtAddress, txtInstagram, txtWhatsapp;
    private int selectedId = 0;

    // --- CONFIGURACIÓN VISUAL CORPORATIVA ---
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Color COLOR_BG_APP = new Color(18, 18, 18);
    private static final Color COLOR_BG_CARD = new Color(28, 28, 28);
    private static final Color COLOR_ACCENT = new Color(255, 140, 0);
    private static final Color COLOR_FUCSIA = new Color(220, 0, 115);

    public SupplierManagementDialog(Window parent) {
        super(parent, "Gestión de Proveedores", ModalityType.APPLICATION_MODAL);
        setSize(1150, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG_APP);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomToolbar(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> updateFieldsFromSelection());
        loadSuppliersData();
    }

    private JComponent buildHeader() {
        JLabel title = new JLabel("DIRECTORIO DE PROVEEDORES & CONEXIÓN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(COLOR_ACCENT);
        title.setBorder(new EmptyBorder(20, 0, 10, 0));
        return title;
    }

    private JComponent buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 30, 5, 30));

        panel.add(buildTablePanel(), BorderLayout.CENTER);
        panel.add(buildFormPanel(), BorderLayout.EAST);

        return panel;
    }

    private JComponent buildTablePanel() {
        model = new DefaultTableModel(new String[]{"ID", "EMPRESA", "INSTAGRAM"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(60, 60, 60)));
        return scroll;
    }

    private JComponent buildFormPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setPreferredSize(new Dimension(380, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(COLOR_BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 60)),
                new EmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy = 0;

        txtCompany   = buildSimpleField(form, "Empresa", gbc);
        txtContact   = buildSimpleField(form, "Contacto", gbc);
        txtPhone     = buildSimpleField(form, "Teléfono", gbc);
        txtEmail     = buildSimpleField(form, "Email", gbc);
        txtWhatsapp  = buildSocialField(form, "WhatsApp", "https://wa.me/", gbc, new Color(37, 211, 102));
        txtInstagram = buildSocialField(form, "Instagram", "https://instagram.com/", gbc, COLOR_FUCSIA);
        txtAddress   = buildSimpleField(form, "Dirección", gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    /**
     * Barra inferior con botones rectangulares estandarizados.
     */
    private JComponent buildBottomToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bar.setOpaque(false);

        bar.add(createStandardButton("NUEVO", new Color(70, 70, 70), e -> resetForm()));
        bar.add(createStandardButton("ELIMINAR", new Color(190, 45, 45), e -> performDelete()));
        bar.add(createStandardButton("GUARDAR", COLOR_ACCENT, e -> performSave()));
        bar.add(createStandardButton("SALIR", new Color(55, 55, 55), e -> dispose()));

        return bar;
    }

    /**
     * Crea botones con estilo rectangular uniforme para todo el sistema.
     */
    private JButton createStandardButton(String text, Color bg, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(130, 42));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(bg.brighter(), 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    private JTextField buildSimpleField(JPanel p, String label, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(Color.LIGHT_GRAY);
        p.add(lbl, gbc); gbc.gridy++;
        JTextField txt = createTextField();
        p.add(txt, gbc); gbc.gridy++;
        return txt;
    }

    private JTextField buildSocialField(JPanel p, String label, String prefix, GridBagConstraints gbc, Color c) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(Color.LIGHT_GRAY);
        p.add(lbl, gbc); gbc.gridy++;
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JTextField txt = createTextField();
        JButton go = new JButton("↗");
        go.setPreferredSize(new Dimension(40, 34));
        go.setBackground(c);
        go.setForeground(Color.WHITE);
        go.setBorderPainted(false);
        go.addActionListener(e -> openLink(prefix, txt.getText()));
        row.add(txt, BorderLayout.CENTER);
        row.add(go, BorderLayout.EAST);
        p.add(row, gbc); gbc.gridy++;
        return txt;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(FONT_INPUT);
        txt.setPreferredSize(new Dimension(260, 34));
        txt.setBackground(new Color(45, 45, 45));
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setBorder(new LineBorder(new Color(85, 85, 85)));
        return txt;
    }

    private void styleTable() {
        table.setRowHeight(38);
        table.setBackground(new Color(32, 32, 32));
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(COLOR_FUCSIA);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    private void loadSuppliersData() {
        model.setRowCount(0);
        List<Supplier> suppliers = supplierDAO.getAll();
        suppliers.forEach(s -> model.addRow(new Object[]{s.getId(), s.getCompany(), s.getInstagram()}));
    }

    private void updateFieldsFromSelection() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int id = (int) model.getValueAt(row, 0);
            selectedId = id;
            supplierDAO.getAll().stream().filter(s -> s.getId() == selectedId).findFirst()
                    .ifPresent(s -> {
                        txtCompany.setText(s.getCompany()); txtContact.setText(s.getContact());
                        txtPhone.setText(s.getPhone()); txtEmail.setText(s.getEmail());
                        txtAddress.setText(s.getAddress()); txtInstagram.setText(s.getInstagram());
                        txtWhatsapp.setText(s.getWhatsapp());
                    });
        }
    }

    private void resetForm() {
        selectedId = 0;
        txtCompany.setText(""); txtContact.setText(""); txtPhone.setText("");
        txtEmail.setText(""); txtAddress.setText(""); txtInstagram.setText(""); txtWhatsapp.setText("");
        table.clearSelection();
    }

    private void performSave() {
        if(txtCompany.getText().trim().isEmpty()) return;
        Supplier s = new Supplier(selectedId, txtCompany.getText(), txtContact.getText(),
                txtPhone.getText(), txtEmail.getText(), txtAddress.getText(),
                txtInstagram.getText(), txtWhatsapp.getText());
        if(supplierDAO.save(s)) { loadSuppliersData(); resetForm(); }
    }

    private void performDelete() {
        if(selectedId != 0 && supplierDAO.delete(selectedId)) { loadSuppliersData(); resetForm(); }
    }

    private void openLink(String prefix, String value) {
        try { if(!value.trim().isEmpty()) Desktop.getDesktop().browse(new URI(prefix + value.trim())); }
        catch (Exception ignored) {}
    }
}