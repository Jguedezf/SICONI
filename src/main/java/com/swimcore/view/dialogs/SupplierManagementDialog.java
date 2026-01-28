/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: SupplierManagementDialog.java
 * VERSIÓN: 2.2.0 (Multilanguage Integration)
 * DESCRIPCIÓN: Diálogo modal para la gestión de proveedores.
 * Todos los textos fijos han sido reemplazados por LanguageManager.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.SupplierDAO;
import com.swimcore.model.Supplier;
import com.swimcore.util.LanguageManager; // <-- Importado
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

    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Color COLOR_BG_APP = new Color(20, 20, 20);
    private static final Color COLOR_BG_CARD = new Color(35, 35, 35);
    private static final Color COLOR_GOLD = new Color(212, 175, 55);
    private static final Color COLOR_TEXT = new Color(230, 230, 230);

    public SupplierManagementDialog(Window parent) {
        super(parent, LanguageManager.get("supplier.title"), ModalityType.APPLICATION_MODAL);
        setSize(1150, 700);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        getRootPane().setBorder(new LineBorder(COLOR_GOLD, 2));

        getContentPane().setBackground(COLOR_BG_APP);
        setLayout(new BorderLayout());

        initUI();

        table.getSelectionModel().addListSelectionListener(e -> updateFieldsFromSelection());
        loadSuppliersData();
    }

    private void initUI() {
        JLabel title = new JLabel(LanguageManager.get("supplier.title"), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(COLOR_GOLD);
        title.setBorder(new EmptyBorder(25, 0, 15, 0));
        add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout(25, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 30, 5, 30));

        panel.add(buildTablePanel(), BorderLayout.CENTER);
        panel.add(buildFormPanel(), BorderLayout.EAST);
        add(panel, BorderLayout.CENTER);

        add(buildBottomToolbar(), BorderLayout.SOUTH);
    }

    private JComponent buildTablePanel() {
        model = new DefaultTableModel(new String[]{"ID", LanguageManager.get("supplier.company"), LanguageManager.get("supplier.instagram")}, 0) {
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

        txtCompany   = buildField(form, LanguageManager.get("supplier.company"), gbc);
        txtContact   = buildField(form, LanguageManager.get("supplier.contact"), gbc);
        txtPhone     = buildField(form, LanguageManager.get("supplier.phone"), gbc);
        txtEmail     = buildField(form, LanguageManager.get("supplier.email"), gbc);
        txtWhatsapp  = buildSocialField(form, LanguageManager.get("supplier.whatsapp"), "https://wa.me/", gbc, new Color(37, 211, 102));
        txtInstagram = buildSocialField(form, LanguageManager.get("supplier.instagram"), "https://instagram.com/", gbc, new Color(220, 0, 115));
        txtAddress   = buildField(form, LanguageManager.get("supplier.address"), gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null); scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    private JComponent buildBottomToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        bar.setOpaque(false);

        SoftButton btnNew = createSoftButton(LanguageManager.get("supplier.btn.new"), "/images/icons/icon_add.png", e -> resetForm());
        SoftButton btnDelete = createSoftButton(LanguageManager.get("supplier.btn.delete"), "/images/icons/icon_delete.png", e -> performDelete());
        SoftButton btnSave = createSoftButton(LanguageManager.get("supplier.btn.save"), "/images/icons/icon_save.png", e -> performSave());
        SoftButton btnExit = createSoftButton(LanguageManager.get("supplier.btn.close"), "/images/icons/icon_exit.png", e -> dispose());

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
        btn.setPreferredSize(new Dimension(80, 60));
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
        if(selectedId != 0 && JOptionPane.showConfirmDialog(this, LanguageManager.get("supplier.msg.delete")) == JOptionPane.YES_OPTION) {
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