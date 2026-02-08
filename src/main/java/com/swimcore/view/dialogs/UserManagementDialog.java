/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - DAYANA GUEDEZ SWIMWEAR
 * ARCHIVO: UserManagementDialog.java
 * VERSIÓN: 2.6.5 (Performance Turbo Fix)
 * FECHA: 06 de Febrero de 2026
 * HORA: 03:00 PM (Hora de Venezuela)
 * DESCRIPCIÓN TÉCNICA:
 * Módulo de Gestión de Usuarios (ABM). Permite la administración de credenciales
 * y roles de acceso al sistema. Implementa carga asíncrona para optimizar
 * el tiempo de respuesta de la interfaz gráfica.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.util.LuxuryMessage;
import com.swimcore.util.SoundManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * [VISTA - GESTIÓN DE USUARIOS] Clase que implementa la interfaz para el mantenimiento de usuarios.
 * [POO - HERENCIA] Extiende de JDialog para funcionar como una ventana modal de administración.
 * * FUNCIONALIDAD: Creación, Edición y Eliminación de usuarios del sistema (CRUD).
 */
public class UserManagementDialog extends JDialog {

    // [PATRÓN DAO] Objeto de acceso a datos para la entidad Usuario.
    private final UserDAO userDAO = new UserDAO();

    // Componentes de la tabla de visualización de datos
    private DefaultTableModel model;
    private JTable table;

    // Constantes de diseño (Look & Feel)
    private final Color LUX_GOLD = new Color(212, 175, 55);
    private final Color LUX_BG_DARK = new Color(20, 20, 20);

    /**
     * Constructor de la clase. Configura la interfaz e inicia la carga de datos.
     * @param parent Ventana padre para establecer la modalidad.
     */
    public UserManagementDialog(Window parent) {
        super(parent, LanguageManager.get("user.title"), ModalityType.APPLICATION_MODAL);
        setSize(900, 600);
        setLocationRelativeTo(parent);

        // Configuración del panel de fondo
        try {
            JPanel bg = new ImagePanel("/images/bg_audit.png");
            bg.setLayout(new BorderLayout());
            bg.setBorder(new LineBorder(LUX_GOLD, 2));
            setContentPane(bg);
        } catch (Exception e) {
            JPanel bg = new JPanel(new BorderLayout());
            bg.setBackground(LUX_BG_DARK);
            setContentPane(bg);
        }

        initHeader();
        initTable();
        initButtons();

        // [OPTIMIZACIÓN] Carga de datos en segundo plano (Background Thread).
        loadDataAsync();
    }

    private void initHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 10, 0));

        JLabel lbl = new JLabel(LanguageManager.get("user.header"), SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lbl.setForeground(LUX_GOLD);
        p.add(lbl, BorderLayout.CENTER);
        add(p, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] cols = {
                LanguageManager.get("user.col.login"),
                LanguageManager.get("user.col.name"),
                LanguageManager.get("user.col.role"),
                LanguageManager.get("user.col.pass")
        };

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setBackground(LUX_BG_DARK);
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(60,60,60));

        JTableHeader h = table.getTableHeader();
        h.setBackground(Color.BLACK);
        h.setForeground(LUX_GOLD);
        h.setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, center);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(LUX_BG_DARK);
        scroll.setBorder(new LineBorder(LUX_GOLD, 1));

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(10, 40, 10, 40));
        container.add(scroll);
        add(container, BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        p.setOpaque(false);

        ThreeDButton btnNew = new ThreeDButton(LanguageManager.get("user.btn.create"), new Color(0, 150, 255));
        btnNew.addActionListener(e -> showUserForm(null));

        ThreeDButton btnEdit = new ThreeDButton(LanguageManager.get("user.btn.edit"), new Color(255, 140, 0));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) { LuxuryMessage.show(this, "AVISO", LanguageManager.get("user.msg.select"), true); return; }
            String user = (String) model.getValueAt(row, 0);
            showUserForm(userDAO.findByUsername(user));
        });

        ThreeDButton btnDel = new ThreeDButton(LanguageManager.get("user.btn.delete"), new Color(200, 0, 0));
        btnDel.addActionListener(e -> deleteSelected());

        ThreeDButton btnClose = new ThreeDButton(LanguageManager.get("user.btn.exit"), new Color(80, 80, 80));
        btnClose.addActionListener(e -> dispose());

        p.add(btnNew);
        p.add(btnEdit);
        p.add(btnDel);
        p.add(Box.createHorizontalStrut(20));
        p.add(btnClose);

        add(p, BorderLayout.SOUTH);
    }

    // --- LÓGICA DE CARGA ASÍNCRONA ---

    /**
     * [CONCURRENCIA]
     * Método encargado de cargar los datos de usuarios desde la base de datos
     * en un hilo separado para evitar congelar la interfaz gráfica.
     */
    private void loadDataAsync() {
        model.setRowCount(0);
        // Ejecución de la consulta en un hilo secundario
        new Thread(() -> {
            List<User> list = userDAO.getAllUsers();
            // Actualización de la UI en el hilo de despacho de eventos (EDT)
            SwingUtilities.invokeLater(() -> {
                for(User u : list) {
                    model.addRow(new Object[]{u.getUsername(), u.getFullName(), u.getRole(), "********"});
                }
            });
        }).start();
    }

    // Mantenemos el método original por compatibilidad interna, pero redirigiendo a la versión asíncrona.
    private void loadData() {
        loadDataAsync();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if(row == -1) return;
        String user = (String) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format(LanguageManager.get("user.msg.del.confirm"), user),
                LanguageManager.get("inventory.msg.delete.title"), JOptionPane.YES_NO_OPTION);

        if(confirm == JOptionPane.YES_OPTION) {
            if(userDAO.deleteUser(user)) {
                SoundManager.getInstance().playClick();
                loadData();
            } else {
                LuxuryMessage.show(this, "ERROR", LanguageManager.get("user.msg.del.error"), true);
            }
        }
    }

    private void showUserForm(User userToEdit) {
        String title = (userToEdit == null) ? LanguageManager.get("user.form.new") : LanguageManager.get("user.form.edit");
        JDialog d = new JDialog(this, title, true);
        d.setSize(400, 450);
        d.setLocationRelativeTo(this);
        d.setUndecorated(true);

        JPanel p = new JPanel(new GridLayout(0, 1, 10, 10));
        p.setBackground(new Color(30, 30, 30));
        p.setBorder(new CompoundBorder(new LineBorder(LUX_GOLD, 2), new EmptyBorder(20,20,20,20)));

        JTextField txtUser = new JTextField();
        JTextField txtName = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"VENDEDOR", "ADMIN", "ASISTENTE"});

        if(userToEdit != null) {
            txtUser.setText(userToEdit.getUsername());
            txtUser.setEditable(false);
            txtName.setText(userToEdit.getFullName());
            txtPass.setText(userToEdit.getPassword());
            cbRole.setSelectedItem(userToEdit.getRole());
        }

        p.add(styleLabel(LanguageManager.get("user.form.login"))); p.add(styleField(txtUser));
        p.add(styleLabel(LanguageManager.get("user.form.name"))); p.add(styleField(txtName));
        p.add(styleLabel(LanguageManager.get("user.form.pass"))); p.add(styleField(txtPass));
        p.add(styleLabel(LanguageManager.get("user.form.role"))); p.add(cbRole);

        ThreeDButton btnSave = new ThreeDButton(LanguageManager.get("user.form.save"), LUX_GOLD);
        btnSave.setForeground(Color.BLACK);
        btnSave.addActionListener(e -> {
            if(txtUser.getText().isEmpty() || txtPass.getPassword().length==0) return;

            User u = new User(txtUser.getText(), new String(txtPass.getPassword()), txtName.getText(), (String)cbRole.getSelectedItem());
            boolean ok = (userToEdit == null) ? userDAO.saveUser(u) : userDAO.updateUser(u);

            if(ok) {
                SoundManager.getInstance().playClick();
                d.dispose();
                loadData();
            } else {
                LuxuryMessage.show(d, "ERROR", LanguageManager.get("user.form.error"), true);
            }
        });

        ThreeDButton btnCancel = new ThreeDButton(LanguageManager.get("user.form.cancel"), Color.GRAY);
        btnCancel.addActionListener(e -> d.dispose());

        p.add(new JLabel(""));
        p.add(btnSave);
        p.add(btnCancel);

        d.setContentPane(p);
        d.setVisible(true);
    }

    private JLabel styleLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JComponent styleField(JComponent c) {
        c.setBackground(new Color(50,50,50));
        c.setForeground(Color.WHITE);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if(c instanceof JTextField) ((JTextField)c).setCaretColor(LUX_GOLD);
        c.setBorder(new LineBorder(Color.GRAY));
        return c;
    }

    /**
     * [CLASE INTERNA - UI] Botón personalizado con efecto 3D y degradado.
     * [POO - POLIMORFISMO] Sobreescribe el método paintComponent para renderizado customizado.
     */
    private class ThreeDButton extends JButton {
        private Color baseColor;
        public ThreeDButton(String text, Color color) {
            super(text);
            this.baseColor = color;
            setContentAreaFilled(false); setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = getModel().isPressed() ?
                    new GradientPaint(0, 0, baseColor.darker(), 0, getHeight(), baseColor) :
                    new GradientPaint(0, 0, baseColor.brighter(), 0, getHeight(), baseColor.darker());
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(baseColor.brighter());
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}