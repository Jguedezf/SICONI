/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: AddEditClientDialog.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 2.1.0 (Luxury UI & Compact Layout)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Ventana Modal para el Registro y Edición de Clientes.
 * - ACTUALIZACIÓN v2.1.0: Diseño "Undecorated" (Sin bordes de OS) con estética
 * Black & Gold. Distribución compacta para agilizar la carga de datos.
 * - Incluye auto-foco en el primer campo para eficiencia tipo POS.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.controller.ClientController;
import com.swimcore.model.Client;
import com.swimcore.util.SoundManager; // Opcional: Para sonido al guardar

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AddEditClientDialog extends JDialog {

    private final ClientController controller;

    // --- COMPONENTES DEL FORMULARIO ---

    // 1. Representante (Pagador)
    private JComboBox<String> cmbIdType;
    private JTextField txtIdNumber;
    private JTextField txtFullName;
    private JTextField txtPhone;
    private JTextField txtInstagram;
    private JCheckBox chkVip;

    // 2. Atleta (Usuario)
    private JTextField txtAthleteName;
    private JTextField txtBirthDate;
    private JComboBox<String> cmbClub;
    private JComboBox<String> cmbCategory;

    // Colores Luxury (Sincronizados con SalesView)
    private final Color COLOR_BG = new Color(20, 20, 20); // Negro más profundo
    private final Color COLOR_PANEL = new Color(30, 30, 30);
    private final Color COLOR_INPUT = new Color(45, 45, 45);
    private final Color COLOR_TEXT = new Color(229, 228, 226); // Platino
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);

    public AddEditClientDialog(Frame owner) {
        super(owner, "Ficha de Cliente y Atleta", true);
        this.controller = new ClientController();

        // Configuración de Ventana Flotante (Tipo POS)
        setSize(700, 650); // Un poco más ancha, menos alta
        setLocationRelativeTo(owner);
        setUndecorated(true); // Quitamos borde de Windows
        getRootPane().setBorder(BorderFactory.createLineBorder(COLOR_GOLD, 2)); // Borde Dorado

        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(new JScrollPane(createFormPanel()) {
            { setBorder(null); getViewport().setBackground(COLOR_BG); }
        }, BorderLayout.CENTER);
        add(createActionsPanel(), BorderLayout.SOUTH);

        // Truco de Eficiencia: Auto-foco en Cédula al abrir
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) { txtIdNumber.requestFocus(); }
        });
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_BG);
        p.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel lbl = new JLabel("NUEVO CLIENTE / ATLETA", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(COLOR_GOLD);

        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JPanel createFormPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // --- BLOQUE 1: REPRESENTANTE (El que paga) ---
        JPanel pnlRep = createSectionPanel("DATOS DEL REPRESENTANTE (PAGADOR)");
        pnlRep.setLayout(new GridLayout(4, 2, 15, 10)); // Grid más compacto

        // Fila 1: ID
        JPanel pnlId = new JPanel(new BorderLayout(5,0)); pnlId.setOpaque(false);
        cmbIdType = new JComboBox<>(new String[]{"V", "E", "J", "P"}); styleCombo(cmbIdType);
        txtIdNumber = createTextField();
        pnlId.add(cmbIdType, BorderLayout.WEST);
        pnlId.add(txtIdNumber, BorderLayout.CENTER);

        pnlRep.add(createLabelFieldPair("Cédula / RIF:", pnlId));

        // Fila 1b: Nombre
        txtFullName = createTextField();
        pnlRep.add(createLabelFieldPair("Nombre Completo:", txtFullName));

        // Fila 2: Teléfono
        txtPhone = createTextField();
        pnlRep.add(createLabelFieldPair("WhatsApp / Tlf:", txtPhone));

        // Fila 2b: Instagram
        txtInstagram = createTextField();
        pnlRep.add(createLabelFieldPair("Instagram (@):", txtInstagram));

        // Fila 3: VIP (Solo ocupa espacio)
        chkVip = new JCheckBox("Cliente VIP (Descuento)");
        chkVip.setOpaque(false);
        chkVip.setForeground(COLOR_GOLD);
        chkVip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlRep.add(chkVip);
        pnlRep.add(new JLabel("")); // Espacio vacío

        // --- BLOQUE 2: ATLETA (El que nada) ---
        JPanel pnlAth = createSectionPanel("PERFIL DEL ATLETA (USUARIO)");
        pnlAth.setLayout(new GridLayout(3, 2, 15, 10));

        // Fila 1
        txtAthleteName = createTextField();
        pnlAth.add(createLabelFieldPair("Nombre Atleta:", txtAthleteName));

        txtBirthDate = createTextField();
        pnlAth.add(createLabelFieldPair("F. Nacimiento:", txtBirthDate));

        // Fila 2
        String[] clubes = {
                "Sin Club / Particular", "CIVG", "Cimos", "Tiburones de Bauxilum",
                "Los Raudales", "Delfines del Lourdes", "Tritones de CVG",
                "La Laja", "Angostura"
        };
        cmbClub = new JComboBox<>(clubes); styleCombo(cmbClub);
        pnlAth.add(createLabelFieldPair("Club de Natación:", cmbClub));

        String[] cats = {"Escuela", "Infantil A", "Infantil B", "Juvenil A", "Juvenil B", "Máster", "Federado", "Asociado"};
        cmbCategory = new JComboBox<>(cats); styleCombo(cmbCategory);
        pnlAth.add(createLabelFieldPair("Categoría:", cmbCategory));

        // AGREGAR AL PANEL PRINCIPAL
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(pnlRep, gbc);

        gbc.gridy = 1;
        mainPanel.add(Box.createVerticalStrut(15), gbc); // Separador

        gbc.gridy = 2;
        mainPanel.add(pnlAth, gbc);

        return mainPanel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0,0,10,20));

        JButton btnCancel = new JButton("CANCELAR");
        styleButton(btnCancel, new Color(80, 80, 80)); // Gris Oscuro

        JButton btnSave = new JButton("GUARDAR FICHA");
        styleButton(btnSave, COLOR_FUCSIA);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveClient());

        panel.add(btnCancel);
        panel.add(btnSave);
        return panel;
    }

    private void saveClient() {
        // VALIDACIÓN DE NEGOCIO
        if (txtFullName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del Representante es obligatorio.", "Faltan Datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // CREAR OBJETO CLIENTE (Mapeo a la nueva estructura)
        Client c = new Client();

        // Datos Representante
        c.setIdType((String) cmbIdType.getSelectedItem());
        c.setIdNumber(txtIdNumber.getText().trim());
        c.setFullName(txtFullName.getText().trim());
        c.setPhone(txtPhone.getText().trim());
        c.setInstagram(txtInstagram.getText().trim());
        c.setVip(chkVip.isSelected());

        // Datos Atleta (Si está vacío, asume que es el mismo representante)
        String atleta = txtAthleteName.getText().trim().isEmpty() ? txtFullName.getText().trim() : txtAthleteName.getText().trim();
        c.setAthleteName(atleta);
        c.setBirthDate(txtBirthDate.getText().trim());
        c.setClub((String) cmbClub.getSelectedItem());
        c.setCategory((String) cmbCategory.getSelectedItem());
        c.setMeasurements(""); // Se llenan en el pedido

        // GUARDAR (Delegar al Controller)
        if (controller.saveClient(c)) {
            try { SoundManager.getInstance().playClick(); } catch(Exception e){}
            JOptionPane.showMessageDialog(this, "¡Cliente Registrado Exitosamente!");
            dispose();
        } else {
            try { SoundManager.getInstance().playError(); } catch(Exception e){}
            JOptionPane.showMessageDialog(this, "Error al guardar. Verifique los datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- ESTILOS VISUALES ---

    // Helper para crear un panel con Label arriba y Campo abajo (Compacto)
    private JPanel createLabelFieldPair(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.GRAY);

        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60,60,60)), title);
        border.setTitleColor(COLOR_GOLD);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        p.setBorder(border);
        return p;
    }

    private JTextField createTextField() {
        JTextField t = new JTextField();
        t.setBackground(COLOR_INPUT);
        t.setForeground(Color.WHITE);
        t.setCaretColor(COLOR_GOLD);
        t.setBorder(BorderFactory.createLineBorder(new Color(80,80,80)));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setPreferredSize(new Dimension(200, 35)); // Un poco más altos para tacto
        return t;
    }

    private void styleCombo(JComboBox box) {
        box.setBackground(COLOR_INPUT);
        box.setForeground(Color.WHITE);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 45));
    }
}