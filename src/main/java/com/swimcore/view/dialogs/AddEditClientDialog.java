/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: AddEditClientDialog.java
 * VERSIÓN: 3.0.0 (LGoodDatePicker & Size Selector)
 * DESCRIPCIÓN: Formulario optimizado con selector de fecha, tallas predefinidas
 * y soporte para edición de registros existentes.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.swimcore.controller.ClientController;
import com.swimcore.dao.Conexion;
import com.swimcore.model.Client;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class AddEditClientDialog extends JDialog {

    private final ClientController controller;
    private final Client clientToEdit; // Cliente a editar (null si es nuevo)

    // Componentes
    private JComboBox<String> cmbIdType, cmbClub, cmbCategory, cmbSize; // cmbSize nuevo
    private JTextField txtIdNumber, txtFullName, txtPhone, txtEmail, txtInstagram, txtAthleteName;
    private DatePicker datePickerBirth; // Reemplaza a txtBirthDate
    private JCheckBox chkVip;
    private JTextArea txtAddress, txtObservations; // txtMeasurements ahora es Observations

    private final Color COLOR_BG = new Color(20, 20, 20);
    private final Color COLOR_INPUT = new Color(45, 45, 45);
    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);

    // Lista de Tallas
    private final String[] SIZES = {"4", "6", "8", "10", "12", "14", "16", "S", "M", "L", "XL", "A MEDIDA"};

    public AddEditClientDialog(Frame owner, Client clientToEdit) {
        super(owner, clientToEdit == null ? "Nuevo Cliente" : "Editar Cliente", true);
        this.controller = new ClientController();
        this.clientToEdit = clientToEdit;

        setSize(950, 620);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(COLOR_GOLD, 2));
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createActionsPanel(), BorderLayout.SOUTH);

        loadClubs();

        // Si estamos editando, cargar datos
        if (clientToEdit != null) {
            fillData();
        }

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                if(clientToEdit == null) txtIdNumber.requestFocusInWindow();
            }
        });

        setVisible(true);
    }

    // Constructor de compatibilidad para llamadas antiguas sin argumentos
    public AddEditClientDialog(Frame owner) {
        this(owner, null);
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 15, 0));
        String title = (clientToEdit == null) ? "NUEVO CLIENTE / ATLETA" : "EDITAR FICHA: " + clientToEdit.getFullName();
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lbl.setForeground(COLOR_GOLD);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JPanel createFormPanel() {
        JPanel formContainer = new JPanel(new GridLayout(1, 2, 30, 0));
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(10, 30, 10, 30));

        formContainer.add(createLeftPanel());
        formContainer.add(createRightPanel());

        return formContainer;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JPanel pnlRep = createSectionPanel("DATOS DEL REPRESENTANTE");
        pnlRep.add(createLabelFieldPair("Cédula / RIF:", createIdPanel()));
        pnlRep.add(Box.createVerticalStrut(10));
        pnlRep.add(createLabelFieldPair("Nombre Completo:", txtFullName = createTextField()));
        pnlRep.add(Box.createVerticalStrut(10));
        pnlRep.add(createLabelFieldPair("WhatsApp / Tlf:", txtPhone = createTextField()));
        pnlRep.add(Box.createVerticalStrut(10));
        pnlRep.add(createLabelFieldPair("Correo Electrónico:", txtEmail = createTextField()));
        pnlRep.add(Box.createVerticalStrut(10));
        pnlRep.add(createLabelFieldPair("Instagram (@):", txtInstagram = createTextField()));
        pnlRep.add(Box.createVerticalStrut(15));
        pnlRep.add(chkVip = createVipCheckBox());

        leftPanel.add(pnlRep);
        leftPanel.add(Box.createVerticalGlue());
        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JPanel pnlAth = createSectionPanel("PERFIL DEL ATLETA");
        pnlAth.add(createLabelFieldPair("Nombre Atleta:", txtAthleteName = createTextField()));
        pnlAth.add(Box.createVerticalStrut(10));

        // DatePicker Configuración
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("dd/MM/yyyy");
        dateSettings.setAllowKeyboardEditing(false);
        datePickerBirth = new DatePicker(dateSettings);
        datePickerBirth.setPreferredSize(new Dimension(200, 35));
        pnlAth.add(createLabelFieldPair("F. Nacimiento:", datePickerBirth));

        pnlAth.add(Box.createVerticalStrut(10));
        pnlAth.add(createLabelFieldPair("Club de Natación:", cmbClub = new JComboBox<>()));
        pnlAth.add(Box.createVerticalStrut(10));

        // Panel horizontal para Categoría y Talla
        JPanel rowCatSize = new JPanel(new GridLayout(1, 2, 10, 0));
        rowCatSize.setOpaque(false);

        JPanel pnlCat = new JPanel(new BorderLayout(0,5)); pnlCat.setOpaque(false);
        pnlCat.add(new JLabel("Categoría:"), BorderLayout.NORTH);
        cmbCategory = new JComboBox<>(new String[]{"Escuela", "Infantil A", "Infantil B", "Juvenil A", "Juvenil B", "Máster", "Federado", "Asociado"});
        styleCombo(cmbCategory);
        pnlCat.add(cmbCategory, BorderLayout.CENTER);

        JPanel pnlSize = new JPanel(new BorderLayout(0,5)); pnlSize.setOpaque(false);
        pnlSize.add(new JLabel("Talla:"), BorderLayout.NORTH);
        cmbSize = new JComboBox<>(SIZES);
        styleCombo(cmbSize);
        pnlSize.add(cmbSize, BorderLayout.CENTER);

        rowCatSize.add(pnlCat);
        rowCatSize.add(pnlSize);

        pnlAth.add(rowCatSize); // Agregamos la fila combinada
        styleCombo(cmbClub);

        rightPanel.add(pnlAth);
        rightPanel.add(Box.createVerticalStrut(15));

        JPanel pnlExtra = createSectionPanel("DETALLES");
        pnlExtra.add(createLabelFieldPair("Dirección:", new JScrollPane(txtAddress = createTextArea(2))));
        pnlExtra.add(Box.createVerticalStrut(10));
        pnlExtra.add(createLabelFieldPair("Observaciones / Medidas:", new JScrollPane(txtObservations = createTextArea(3))));

        rightPanel.add(pnlExtra);
        rightPanel.add(Box.createVerticalGlue());
        return rightPanel;
    }

    private void fillData() {
        if (clientToEdit == null) return;
        txtIdNumber.setText(clientToEdit.getIdNumber());
        txtFullName.setText(clientToEdit.getFullName());
        txtPhone.setText(clientToEdit.getPhone());
        txtEmail.setText(clientToEdit.getEmail());
        txtInstagram.setText(clientToEdit.getInstagram());
        chkVip.setSelected(clientToEdit.isVip());
        txtAthleteName.setText(clientToEdit.getAthleteName());
        cmbClub.setSelectedItem(clientToEdit.getClub());
        cmbCategory.setSelectedItem(clientToEdit.getCategory());
        txtAddress.setText(clientToEdit.getAddress());
        txtObservations.setText(clientToEdit.getMeasurements()); // Usamos este campo para obs

        // Parsear fecha
        try {
            if (clientToEdit.getBirthDate() != null && !clientToEdit.getBirthDate().isEmpty()) {
                datePickerBirth.setDate(LocalDate.parse(clientToEdit.getBirthDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        } catch(Exception e) {}
    }

    private void saveClient() {
        if (txtFullName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del Representante es obligatorio.", "Faltan Datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Client c = (clientToEdit != null) ? clientToEdit : new Client(); // Reusar ID si editamos

        c.setIdType((String) cmbIdType.getSelectedItem());
        c.setIdNumber(txtIdNumber.getText().trim());
        c.setFullName(txtFullName.getText().trim());
        c.setPhone(txtPhone.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setAddress(txtAddress.getText().trim());
        c.setInstagram(txtInstagram.getText().trim());
        c.setVip(chkVip.isSelected());

        String atleta = txtAthleteName.getText().trim().isEmpty() ? txtFullName.getText().trim() : txtAthleteName.getText().trim();
        c.setAthleteName(atleta);

        // Guardar fecha desde DatePicker
        String fechaNac = (datePickerBirth.getDate() != null) ? datePickerBirth.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        c.setBirthDate(fechaNac);

        c.setClub((String) cmbClub.getSelectedItem());
        c.setCategory((String) cmbCategory.getSelectedItem());

        // Guardar Talla en Observaciones si no hay campo específico en BD aun,
        // o concatenarlo. Por ahora, asumimos que 'measurements' guarda todo.
        String talla = (String) cmbSize.getSelectedItem();
        String obs = "Talla: " + talla + " | " + txtObservations.getText().trim();
        c.setMeasurements(obs);

        boolean success;
        if (clientToEdit == null) {
            success = controller.saveClient(c);
        } else {
            success = controller.updateClient(c);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "¡Operación Exitosa!");
            dispose();
        } else {
            SoundManager.getInstance().playError();
            JOptionPane.showMessageDialog(this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ... (El resto de métodos de estilo y carga de clubes se mantiene igual) ...
    private void loadClubs() {
        Vector<String> clubs = new Vector<>();
        clubs.add("Sin Club / Particular");
        try (Connection conn = Conexion.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT name FROM clubs ORDER BY name ASC")) {
            while(rs.next()) clubs.add(rs.getString("name"));
            cmbClub.setModel(new DefaultComboBoxModel<>(clubs));
        } catch (Exception e) {
            cmbClub.setModel(new DefaultComboBoxModel<>(new String[]{"Error al cargar clubes"}));
        }
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 20, 30));
        SoftButton btnCancel = new SoftButton(null); btnCancel.setText("CANCELAR"); styleButton(btnCancel);
        SoftButton btnSave = new SoftButton(null); btnSave.setText("GUARDAR FICHA"); btnSave.setForeground(COLOR_FUCSIA.brighter()); styleButton(btnSave);
        btnCancel.addActionListener(e -> { SoundManager.getInstance().playClick(); dispose(); });
        btnSave.addActionListener(e -> { SoundManager.getInstance().playClick(); saveClient(); });
        panel.add(btnCancel); panel.add(btnSave);
        return panel;
    }

    private JPanel createIdPanel() {
        JPanel pnlId = new JPanel(new BorderLayout(8, 0)); pnlId.setOpaque(false);
        cmbIdType = new JComboBox<>(new String[]{"V", "E", "J", "P"}); styleCombo(cmbIdType); cmbIdType.setPreferredSize(new Dimension(60, 35));
        txtIdNumber = createTextField();
        pnlId.add(cmbIdType, BorderLayout.WEST); pnlId.add(txtIdNumber, BorderLayout.CENTER); return pnlId;
    }

    private JCheckBox createVipCheckBox() { JCheckBox chk = new JCheckBox("Cliente VIP"); chk.setOpaque(false); chk.setForeground(COLOR_GOLD); chk.setFont(new Font("Segoe UI", Font.BOLD, 12)); return chk; }

    private JPanel createLabelFieldPair(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5)); p.setOpaque(false);
        JLabel lbl = new JLabel(labelText); lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbl.setForeground(Color.GRAY);
        p.add(lbl, BorderLayout.NORTH); p.add(field, BorderLayout.CENTER); return p;
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(60,60,60)), " " + title + " ");
        border.setTitleColor(COLOR_GOLD); border.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        p.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(10, 10, 10, 10))); return p;
    }

    private JTextField createTextField() {
        JTextField t = new JTextField(); t.setBackground(COLOR_INPUT); t.setForeground(Color.WHITE); t.setCaretColor(COLOR_GOLD);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14)); t.setPreferredSize(new Dimension(200, 35));
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(80,80,80)), new EmptyBorder(0, 10, 0, 0))); return t;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea t = new JTextArea(rows, 20); t.setBackground(COLOR_INPUT); t.setForeground(Color.WHITE); t.setCaretColor(COLOR_GOLD);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(80,80,80)), new EmptyBorder(5, 10, 5, 10)));
        t.setLineWrap(true); t.setWrapStyleWord(true); return t;
    }

    private void styleCombo(JComboBox<String> box) {
        box.setBackground(COLOR_INPUT); box.setForeground(Color.WHITE); box.setFont(new Font("Segoe UI", Font.PLAIN, 14)); box.setPreferredSize(new Dimension(200, 35));
    }

    private void styleButton(SoftButton btn) { btn.setPreferredSize(new Dimension(160, 45)); btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); }
}