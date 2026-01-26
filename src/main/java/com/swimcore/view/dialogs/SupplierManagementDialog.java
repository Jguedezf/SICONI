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
 * VERSIÓN: 2.1.0 (Black & Gold Edition)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Diálogo modal para la gestión del directorio de proveedores.
 * - MEJORA: Rediseño visual completo "Luxury" (Undecorated, Borde Dorado, Fondo Oscuro).
 * - LÓGICA: Gestión CRUD optimizada con feedback visual y sonoro.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.SupplierDAO;
import com.swimcore.model.Supplier;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.URL;

public class SupplierManagementDialog extends JDialog {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private DefaultTableModel model;
    private JTable table;

    private JTextField txtCompany, txtContact, txtPhone, txtEmail, txtAddress, txtInstagram, txtWhatsapp;
    private int selectedId = 0;

    // --- PALETA LUXURY ---
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Color COLOR_BG_APP = new Color(20, 20, 20); // Negro profundo
    private static final Color COLOR_BG_CARD = new Color(35, 35, 35); // Gris oscuro panel
    private static final Color COLOR_GOLD = new Color(212, 175, 55); // Dorado SICONI
    private static final Color COLOR_TEXT = new Color(230, 230, 230); // Platino

    public SupplierManagementDialog(Window parent) {
        super(parent, "Proveedores SICONI", ModalityType.APPLICATION_MODAL);
        setSize(1150, 700);
        setLocationRelativeTo(parent);
        setUndecorated(true); // Estilo Luxury sin bordes Windows
        getRootPane().setBorder(new LineBorder(COLOR_GOLD, 2)); // Borde dorado

        // Fondo oscuro sólido para consistencia
        getContentPane().setBackground(COLOR_BG_APP);
        setLayout(new BorderLayout());

        initUI();

        table.getSelectionModel().addListSelectionListener(e -> updateFieldsFromSelection());
        loadSuppliersData();
    }

    private void initUI() {
        // Encabezado
        JLabel title = new JLabel("DIRECTORIO DE PROVEEDORES", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(COLOR_GOLD);
        title.setBorder(new EmptyBorder(25, 0, 15, 0));
        add(title, BorderLayout.NORTH);

        // Panel Central
        JPanel panel = new JPanel(new BorderLayout(25, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 30, 5, 30));

        panel.add(buildTablePanel(), BorderLayout.CENTER);
        panel.add(buildFormPanel(), BorderLayout.EAST);
        add(panel, BorderLayout.CENTER);

        // Barra Inferior
        add(buildBottomToolbar(), BorderLayout.SOUTH);
    }

    private JComponent buildTablePanel() {
        model = new DefaultTableModel(new String[]{"ID", "EMPRESA", "INSTAGRAM"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(COLOR_GOLD, 1));
        scroll.getViewport().setBackground(COLOR_BG_CARD);
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
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.insets = new Insets(5, 0, 5, 0); gbc.gridy = 0;

        txtCompany   = buildField(form, "EMPRESA / RAZÓN SOCIAL", gbc);
        txtContact   = buildField(form, "PERSONA DE CONTACTO", gbc);
        txtPhone     = buildField(form, "TELÉFONO DE OFICINA", gbc);
        txtEmail     = buildField(form, "CORREO ELECTRÓNICO", gbc);
        txtWhatsapp  = buildSocialField(form, "WHATSAPP", "https://wa.me/", gbc, new Color(37, 211, 102));
        txtInstagram = buildSocialField(form, "INSTAGRAM (@)", "https://instagram.com/", gbc, new Color(220, 0, 115));
        txtAddress   = buildField(form, "DIRECCIÓN FÍSICA", gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null); scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    private JComponent buildBottomToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        bar.setOpaque(false);

        SoftButton btnNew = createSoftButton("NUEVO", "/images/icons/icon_add.png", e -> resetForm());
        SoftButton btnDelete = createSoftButton("ELIMINAR", "/images/icons/icon_delete.png", e -> performDelete());
        SoftButton btnSave = createSoftButton("GUARDAR", "/images/icons/icon_save.png", e -> performSave()); // Asegurar icono save
        SoftButton btnExit = createSoftButton("CERRAR", "/images/icons/icon_exit.png", e -> dispose());

        bar.add(btnNew);
        bar.add(btnDelete);
        bar.add(btnSave);
        bar.add(btnExit);

        return bar;
    }

    private SoftButton createSoftButton(String tooltip, String iconPath, java.awt.event.ActionListener al) {
        ImageIcon icon = null;
        try {
            URL url = getClass().getResource(iconPath);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        } catch (Exception e) {}

        SoftButton btn = new SoftButton(icon);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(80, 60)); // Botones más cómodos
        btn.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            al.actionPerformed(e);
        });
        return btn;
    }

    private JTextField buildField(JPanel p, String label, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(label); lbl.setFont(FONT_LABEL); lbl.setForeground(Color.GRAY);
        p.add(lbl, gbc); gbc.gridy++;
        JTextField txt = createTextField();
        p.add(txt, gbc); gbc.gridy++;
        return txt;
    }

    private JTextField buildSocialField(JPanel p, String label, String prefix, GridBagConstraints gbc, Color c) {
        JLabel lbl = new JLabel(label); lbl.setFont(FONT_LABEL); lbl.setForeground(Color.GRAY);
        p.add(lbl, gbc); gbc.gridy++;
        JPanel row = new JPanel(new BorderLayout(8, 0)); row.setOpaque(false);
        JTextField txt = createTextField();

        JButton go = new JButton("↗");
        go.setPreferredSize(new Dimension(45, 34));
        go.setBackground(c);
        go.setForeground(Color.WHITE);
        go.setBorderPainted(false);
        go.setFocusPainted(false);
        go.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        go.addActionListener(e -> openLink(prefix, txt.getText()));

        row.add(txt, BorderLayout.CENTER); row.add(go, BorderLayout.EAST);
        p.add(row, gbc); gbc.gridy++;
        return txt;
    }

    private JTextField createTextField() {
        JTextField t = new JTextField(); t.setFont(FONT_INPUT);
        t.setBackground(new Color(50, 50, 50));
        t.setForeground(Color.WHITE); t.setCaretColor(COLOR_GOLD);
        t.setBorder(new LineBorder(new Color(80, 80, 80)));
        return t;
    }

    private void styleTable() {
        table.setRowHeight(40);
        table.setBackground(COLOR_BG_CARD);
        table.setForeground(COLOR_TEXT);
        table.setSelectionBackground(new Color(60, 60, 60));
        table.setSelectionForeground(COLOR_GOLD);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        table.getTableHeader().setBackground(new Color(25, 25, 25));
        table.getTableHeader().setForeground(COLOR_GOLD);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer c = new DefaultTableCellRenderer();
        c.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(c);
    }

    private void loadSuppliersData() {
        model.setRowCount(0);
        supplierDAO.getAll().forEach(s -> model.addRow(new Object[]{s.getId(), s.getCompany(), s.getInstagram()}));
    }

    private void updateFieldsFromSelection() {
        int r = table.getSelectedRow();
        if (r >= 0) {
            selectedId = (int) model.getValueAt(r, 0);
            supplierDAO.getAll().stream().filter(s -> s.getId() == selectedId).findFirst()
                    .ifPresent(s -> {
                        txtCompany.setText(s.getCompany()); txtContact.setText(s.getContact());
                        txtPhone.setText(s.getPhone()); txtEmail.setText(s.getEmail());
                        txtAddress.setText(s.getAddress()); txtInstagram.setText(s.getInstagram());
                        txtWhatsapp.setText(s.getWhatsapp());
                    });
        }
    }

    private void performSave() {
        if(txtCompany.getText().trim().isEmpty()) return;
        Supplier s = new Supplier(selectedId, txtCompany.getText(), txtContact.getText(),
                txtPhone.getText(), txtEmail.getText(), txtAddress.getText(),
                txtInstagram.getText(), txtWhatsapp.getText());
        if(supplierDAO.save(s)) { loadSuppliersData(); resetForm(); }
    }

    private void performDelete() {
        if(selectedId != 0 && JOptionPane.showConfirmDialog(this, "¿Eliminar Proveedor?") == JOptionPane.YES_OPTION) {
            if(supplierDAO.delete(selectedId)) { loadSuppliersData(); resetForm(); }
        }
    }

    private void resetForm() {
        selectedId = 0; txtCompany.setText(""); txtContact.setText(""); txtPhone.setText("");
        txtEmail.setText(""); txtAddress.setText(""); txtInstagram.setText(""); txtWhatsapp.setText("");
        table.clearSelection();
    }

    private void openLink(String prefix, String value) {
        try { if(!value.trim().isEmpty()) Desktop.getDesktop().browse(new URI(prefix + value.trim().replace("@", ""))); }
        catch (Exception ignored) {}
    }
}