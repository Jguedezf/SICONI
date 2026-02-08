/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * ARCHIVO: SupplierManagementDialog.java
 * VERSIÓN: 8.1.0 (Fix: TextArea Address + Rounded Social Buttons)
 * FECHA: 06 de Febrero de 2026
 * HORA: 08:30 PM (Hora de Venezuela)
 * DESCRIPCIÓN TÉCNICA:
 * Módulo de Gestión de Proveedores. Permite el registro, consulta y modificación
 * de datos de contacto empresarial. Integra accesos directos a plataformas
 * externas (WhatsApp, Instagram) y manejo de direcciones extendidas.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.SupplierDAO;
import com.swimcore.model.Supplier;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URI;
import java.net.URL;

/**
 * [VISTA - PROVEEDORES] Clase que implementa la interfaz de administración de proveedores.
 * [POO - HERENCIA] Extiende de JDialog para funcionar como un submódulo independiente.
 * * FUNCIONALIDAD: CRUD de empresas proveedoras y agenda de contactos.
 */
public class SupplierManagementDialog extends JDialog {

    // [PATRÓN DAO] Acceso a la capa de datos para la entidad Supplier.
    private final SupplierDAO supplierDAO = new SupplierDAO();

    // Componentes de la tabla de listado
    private DefaultTableModel model;
    private JTable table;

    // Campos del formulario de edición
    private JTextField txtCompany, txtContact, txtPhone, txtEmail, txtInstagram, txtWhatsapp;
    // [COMPONENTE UI] JTextArea para permitir direcciones físicas extensas (multilínea).
    private JTextArea txtAddress;

    // Variable de estado para controlar la selección actual (ID del proveedor).
    private int selectedId = 0;

    // Constantes de diseño (Identidad Visual SICONI)
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_VERDE_NEON = new Color(0, 255, 128);
    private final Color COLOR_FUCSIA_NEON = new Color(255, 0, 127);
    private final Color COLOR_TEXTO = new Color(230, 230, 230);
    private final Color COLOR_INPUT_BG = new Color(25, 25, 25);
    private final Color COLOR_BG_DIALOG = new Color(18, 18, 18);

    /**
     * Constructor de la clase. Inicializa la ventana modal y carga los datos.
     */
    public SupplierManagementDialog(Window parent) {
        super(parent, "SICONI - PROVEEDORES", ModalityType.APPLICATION_MODAL);
        // Ajuste de dimensiones para compatibilidad con resoluciones estándar (1366x768).
        setSize(1250, 680);
        setLocationRelativeTo(parent);
        setUndecorated(false); // Ventana sin bordes del sistema operativo (Estilo personalizado).

        // Configuración del panel de fondo con imagen o color sólido (Fallback).
        try {
            JPanel bgPanel = new ImagePanel("/images/bg_audit.png");
            bgPanel.setLayout(new BorderLayout());
            bgPanel.setBorder(new LineBorder(COLOR_GOLD, 2));
            setContentPane(bgPanel);
        } catch (Exception e) {
            JPanel solidBg = new JPanel(new BorderLayout());
            solidBg.setBackground(COLOR_BG_DIALOG);
            solidBg.setBorder(new LineBorder(COLOR_GOLD, 2));
            setContentPane(solidBg);
        }

        initUI();

        // [EVENTO DE SELECCIÓN] Vinculación de la tabla con el formulario de edición.
        table.getSelectionModel().addListSelectionListener(e -> updateFieldsFromSelection());

        // Carga inicial de datos desde la base de datos.
        loadSuppliersData();
    }

    private void initUI() {
        // 1. ENCABEZADO (HEADER)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 0, 10, 0));
        JLabel title = new JLabel("DIRECTORIO DE PROVEEDORES", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(COLOR_GOLD);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // 2. CONTENEDOR CENTRAL (Split Layout: Tabla | Formulario)
        JPanel centerSplit = new JPanel(new GridBagLayout());
        centerSplit.setOpaque(false);
        centerSplit.setBorder(new EmptyBorder(5, 30, 20, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Panel Izquierdo: Tabla de Listado (40% del ancho)
        gbc.gridx = 0; gbc.weightx = 0.40;
        centerSplit.add(buildTablePanel(), gbc);

        // Panel Derecho: Formulario de Datos (60% del ancho)
        gbc.gridx = 1; gbc.weightx = 0.60;
        gbc.insets = new Insets(0, 25, 0, 0);
        centerSplit.add(buildFormPanel(), gbc);

        add(centerSplit, BorderLayout.CENTER);

        // 3. BARRA LATERAL (SIDEBAR)
        add(buildSidebar(), BorderLayout.EAST);
    }

    private JComponent buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(new LineBorder(COLOR_GOLD), "LISTADO DE EMPRESAS", 0, 0, new Font("Segoe UI", Font.BOLD, 12), COLOR_GOLD));

        model = new DefaultTableModel(new String[]{"ID", "EMPRESA / RAZÓN SOCIAL"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30,30,30));
        scroll.setBorder(null);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildFormPanel() {
        // Panel con efecto de cristal ahumado (Transparencia Alpha).
        JPanel glassPanel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.insets = new Insets(4, 5, 4, 5); gbc.gridy = 0;

        JLabel lblDatos = new JLabel("FICHA TÉCNICA");
        lblDatos.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDatos.setForeground(COLOR_GOLD);
        lblDatos.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridwidth = 2; glassPanel.add(lblDatos, gbc); gbc.gridy++;

        JSeparator sep = new JSeparator(); sep.setForeground(COLOR_GOLD);
        glassPanel.add(sep, gbc); gbc.gridy++;

        // Generación dinámica de campos de texto
        txtCompany = buildField(glassPanel, "EMPRESA / RAZÓN SOCIAL", gbc, 2);
        txtContact = buildField(glassPanel, "PERSONA CONTACTO", gbc, 2);
        txtPhone = buildField(glassPanel, "TELÉFONO / MÓVIL", gbc, 2);
        txtEmail = buildField(glassPanel, "CORREO ELECTRÓNICO", gbc, 2);

        // Integración de Redes Sociales (Botones de enlace directo)
        gbc.gridwidth = 1; gbc.weightx = 0.5;
        gbc.gridx = 0;
        txtWhatsapp = buildSocialField(glassPanel, "WHATSAPP", "https://wa.me/", gbc, "icon_link_green.png");

        gbc.gridx = 1;
        txtInstagram = buildSocialField(glassPanel, "INSTAGRAM", "https://instagram.com/", gbc, "icon_link_pink.png");

        gbc.gridy++; gbc.gridx = 0;

        // [COMPONENTE UI] Configuración especial para la Dirección Fiscal.
        // Se utiliza JTextArea dentro de un JScrollPane para soportar texto largo.
        gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.2;

        JLabel lblAddr = new JLabel("DIRECCIÓN FISCAL");
        lblAddr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAddr.setForeground(new Color(180,180,180));
        glassPanel.add(lblAddr, gbc); gbc.gridy++;

        txtAddress = new JTextArea(3, 20);
        txtAddress.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtAddress.setBackground(COLOR_INPUT_BG);
        txtAddress.setForeground(Color.WHITE);
        txtAddress.setCaretColor(COLOR_GOLD);
        txtAddress.setBorder(new EmptyBorder(5,5,5,5));
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);

        JScrollPane scrollAddr = new JScrollPane(txtAddress);
        scrollAddr.setBorder(new LineBorder(new Color(80, 80, 80)));
        // Listener para feedback visual (cambio de color de borde) al enfocar.
        txtAddress.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { scrollAddr.setBorder(new LineBorder(COLOR_GOLD, 2)); }
            public void focusLost(FocusEvent e) { scrollAddr.setBorder(new LineBorder(new Color(80, 80, 80))); }
        });

        glassPanel.add(scrollAddr, gbc);

        return glassPanel;
    }

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel(new GridLayout(4, 1, 0, 15));
        sidebar.setOpaque(false);
        sidebar.setBorder(new EmptyBorder(20, 10, 40, 30));
        sidebar.setPreferredSize(new Dimension(200, 0));

        // Botones de acción principales (CRUD + Salir)
        sidebar.add(createBigImgButton("NUEVO", "btn_add.png", e -> resetForm()));
        sidebar.add(createBigImgButton("GUARDAR", "btn_save.png", e -> performSave()));
        sidebar.add(createBigImgButton("ELIMINAR", "btn_delete.png", e -> performDelete()));
        sidebar.add(createBigImgButton("SALIR", "btn_back.png", e -> dispose()));

        return sidebar;
    }

    // --- MÉTODOS DE CONSTRUCCIÓN DE COMPONENTES VISUALES ---

    private SoftButton createBigImgButton(String text, String imageName, ActionListener al) {
        // [POO - CLASE ANÓNIMA] Personalización del pintado del botón.
        SoftButton btn = new SoftButton(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BorderLayout());

        JLabel lIcon = new JLabel();
        lIcon.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            URL url = getClass().getResource("/images/icons/" + imageName);
            if (url != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH));
                lIcon.setIcon(icon);
            }
        } catch (Exception e) {}

        JLabel lText = new JLabel(text, SwingConstants.CENTER);
        lText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lText.setForeground(Color.WHITE);

        btn.add(lIcon, BorderLayout.CENTER);
        btn.add(lText, BorderLayout.SOUTH);

        btn.setBorder(new EmptyBorder(5, 5, 5, 5));
        btn.setOpaque(false);

        btn.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            al.actionPerformed(e);
        });

        return btn;
    }

    private JTextField buildField(JPanel p, String label, GridBagConstraints gbc, int width) {
        gbc.gridwidth = width;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(180,180,180));
        p.add(lbl, gbc); gbc.gridy++;
        JTextField txt = createTextField();
        p.add(txt, gbc); gbc.gridy++;
        return txt;
    }

    private JTextField buildSocialField(JPanel p, String label, String prefix, GridBagConstraints gbc, String iconName) {
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(180,180,180));
        p.add(lbl, gbc); gbc.gridy++;

        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        JTextField txt = createTextField();

        // Botón de enlace externo con bordes redondeados manualmente.
        SoftButton go = new SoftButton(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40,40,40));
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.setColor(new Color(80,80,80));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        go.setPreferredSize(new Dimension(50, 45));
        go.setOpaque(false);
        go.setBorder(new EmptyBorder(5,5,5,5));

        try {
            URL url = getClass().getResource("/images/icons/" + iconName);
            if (url != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
                go.setIcon(icon);
            }
        } catch(Exception e){}

        // Acción de apertura de enlace en el navegador predeterminado.
        go.addActionListener(e -> openLink(prefix, txt.getText()));

        row.add(txt, BorderLayout.CENTER);
        row.add(go, BorderLayout.EAST);

        p.add(row, gbc);
        return txt;
    }

    private JTextField createTextField() {
        JTextField t = new JTextField();
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t.setBackground(COLOR_INPUT_BG);
        t.setForeground(Color.WHITE);
        t.setCaretColor(COLOR_GOLD);
        t.setBorder(new LineBorder(new Color(80, 80, 80)));
        t.setPreferredSize(new Dimension(0, 45));
        t.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { t.setBorder(new LineBorder(COLOR_GOLD, 2)); }
            public void focusLost(FocusEvent e) { t.setBorder(new LineBorder(new Color(80, 80, 80))); }
        });
        return t;
    }

    private void styleTable() {
        table.setRowHeight(45);
        table.setBackground(new Color(30, 30, 30));
        table.setForeground(COLOR_TEXTO);
        table.setSelectionBackground(new Color(50, 50, 50));
        table.setSelectionForeground(COLOR_GOLD);
        table.setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(60,60,60));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setBackground(Color.BLACK);
        header.setForeground(COLOR_GOLD);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer left = new DefaultTableCellRenderer(); left.setHorizontalAlignment(JLabel.LEFT);

        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
                if(v instanceof Integer) v = String.format("%03d", v);
                Component comp = super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);
                setHorizontalAlignment(JLabel.CENTER); return comp;
            }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setCellRenderer(left);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
    }

    // --- LÓGICA DE NEGOCIO ---

    private void loadSuppliersData() {
        model.setRowCount(0);
        supplierDAO.getAll().forEach(s -> model.addRow(new Object[]{s.getId(), s.getCompany()}));
    }

    private void updateFieldsFromSelection() {
        int r = table.getSelectedRow();
        if (r >= 0) {
            selectedId = (int) model.getValueAt(r, 0);
            supplierDAO.getAll().stream().filter(s -> s.getId() == selectedId).findFirst()
                    .ifPresent(s -> {
                        txtCompany.setText(s.getCompany()); txtContact.setText(s.getContact());
                        txtPhone.setText(s.getPhone()); txtEmail.setText(s.getEmail());
                        txtAddress.setText(s.getAddress());
                        txtInstagram.setText(s.getInstagram());
                        txtWhatsapp.setText(s.getWhatsapp());
                    });
        }
    }

    private void performSave() {
        if(txtCompany.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Nombre de empresa obligatorio.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        // Creación del objeto Supplier con los datos del formulario.
        Supplier s = new Supplier(selectedId, txtCompany.getText(), txtContact.getText(), txtPhone.getText(), txtEmail.getText(), txtAddress.getText(), txtInstagram.getText(), txtWhatsapp.getText());
        if(supplierDAO.save(s)) { loadSuppliersData(); resetForm(); SoundManager.getInstance().playClick(); }
    }

    private void performDelete() {
        if(selectedId != 0 && JOptionPane.showConfirmDialog(this, "¿Eliminar proveedor?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if(supplierDAO.delete(selectedId)) { loadSuppliersData(); resetForm(); SoundManager.getInstance().playClick(); }
        }
    }

    private void resetForm() {
        selectedId = 0; txtCompany.setText(""); txtContact.setText(""); txtPhone.setText(""); txtEmail.setText("");
        txtAddress.setText("");
        txtInstagram.setText(""); txtWhatsapp.setText("");
        table.clearSelection(); txtCompany.requestFocus();
    }

    /**
     * [FUNCIONALIDAD EXTERNA] Abre enlaces web en el navegador del sistema.
     */
    private void openLink(String prefix, String value) {
        try { if(!value.trim().isEmpty()) Desktop.getDesktop().browse(new URI(prefix + value.trim().replace("@", ""))); } catch (Exception ignored) {}
    }
}