/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * ARCHIVO: AddEditClientDialog.java
 * VERSIÓN: 38.0.1 (STABLE RELEASE: Focus & Controller Fix)
 * FECHA: 07 de Febrero de 2026
 * DESCRIPCIÓN TÉCNICA:
 * Formulario maestro para la gestión de clientes (Alta y Modificación).
 * Se corrigió la gestión de hilos en la inicialización y el conflicto de foco
 * al cerrar la ventana modal.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePickerSettings.DateArea;
import com.swimcore.controller.ClientController;
import com.swimcore.dao.Conexion;
import com.swimcore.model.Client;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Vector;

/**
 * [VISTA - CLIENTES] Clase que representa la interfaz gráfica para el registro y edición de clientes.
 * [POO - HERENCIA] Extiende de JDialog para operar como ventana modal independiente.
 */
public class AddEditClientDialog extends JDialog {

    // ========================================================================================
    //                                  ATRIBUTOS Y DEPENDENCIAS
    // ========================================================================================

    private ClientController controller;
    private final Client clientToEdit;
    private Client currentClient;

    // Componentes de la Interfaz Gráfica
    private JComboBox<String> cmbIdType, cmbClub, cmbCategory, cmbSize, cmbCountryCode;
    private JTextField txtIdNumber, txtFullName, txtProfession, txtPhone, txtPhoneAlt,
            txtEmail, txtInstagram, txtAthleteName;
    private DatePicker datePickerBirth;
    private JCheckBox chkVip;
    private JTextArea txtAddress, txtObservations;
    private JLabel lblFlagIcon, lblSystemCode;
    private SoftButton btnSave, btnDelete, btnClear, btnCancel;

    // Constantes de Diseño
    private final int INPUT_HEIGHT = 38;
    private final int TEXT_AREA_H = 65;

    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_INPUT_BG = new Color(45, 45, 45);
    private final Color COLOR_TEXT_INPUT = new Color(255, 255, 255);
    private final Color COLOR_LABEL = new Color(230, 230, 230);

    private final Font FONT_INPUT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_TITLE_BORDER = new Font("Segoe UI", Font.BOLD, 12);

    private final String[] SIZES = {"4", "6", "8", "10", "12", "14", "16", "S", "M", "L", "XL", "A MEDIDA"};
    private final String[][] COUNTRY_DATA = {
            {"VEN", "+58", "ven.png"}, {"USA", "+1", "usa.png"}, {"BRA", "+55", "bra.png"},
            {"ARG", "+54", "arg.png"}, {"COL", "+57", "col.png"}, {"PER", "+51", "per.png"},
            {"CHI", "+56", "chi.png"}, {"ESP", "+34", "esp.png"}
    };

    /**
     * Constructor principal.
     */
    public AddEditClientDialog(Frame owner, Client clientToEdit) {
        super(owner, "FICHA TÉCNICA - SICONI", true);
        this.clientToEdit = clientToEdit;

        // [CORRECCIÓN CRÍTICA] Inicialización inmediata del controlador para evitar NullPointer
        this.controller = new ClientController();

        setSize(1120, 680);
        setLocationRelativeTo(null);
        setUndecorated(false);
        setResizable(false);

        JPanel backgroundPanel = new ImagePanel("/images/bg3.png");
        backgroundPanel.setBackground(new Color(30, 30, 30));
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(new LineBorder(COLOR_GOLD, 2));
        setContentPane(backgroundPanel);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainLayout(), BorderLayout.CENTER);
        add(createActionsPanel(), BorderLayout.SOUTH);

        initBackgroundProcess();

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                if(clientToEdit == null) txtIdNumber.requestFocusInWindow();
            }
        });

        // Configuración para cierre con ESC
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public AddEditClientDialog(Window owner) { this((Frame)null, null); }

    // ========================================================================================
    //                                  LÓGICA ASÍNCRONA
    // ========================================================================================

    private void initBackgroundProcess() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Vector<String>, Void>() {
            @Override
            protected Vector<String> doInBackground() {
                // Ya no instanciamos el controlador aquí, solo consultamos datos auxiliares
                Vector<String> clubs = new Vector<>();
                clubs.add("Sin Club / Particular");
                try (Connection con = Conexion.conectar();
                     Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT name FROM clubs ORDER BY name ASC")) {
                    while(rs.next()) clubs.add(rs.getString("name"));
                } catch (Exception e) { System.err.println("Error BD: " + e.getMessage()); }
                return clubs;
            }

            @Override
            protected void done() {
                try {
                    cmbClub.setModel(new DefaultComboBoxModel<>(get()));
                    if (clientToEdit != null) {
                        currentClient = clientToEdit;
                        fillDataFromObject(clientToEdit);
                        btnDelete.setVisible(true);
                    } else {
                        refreshSystemCode();
                        btnDelete.setVisible(false);
                        cmbCountryCode.setSelectedIndex(0);
                        updateFlagIcon();
                    }
                } catch (Exception e) { e.printStackTrace(); }
                setCursor(Cursor.getDefaultCursor());
            }
        }.execute();
    }

    // ========================================================================================
    //                                  CONSTRUCCIÓN DE INTERFAZ
    // ========================================================================================

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(15, 40, 5, 40));

        JLabel title = new JLabel("FICHA TÉCNICA DEL ATLETA");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(COLOR_GOLD);

        lblSystemCode = new JLabel("CARGANDO...");
        lblSystemCode.setFont(new Font("Consolas", Font.BOLD, 20));
        lblSystemCode.setForeground(new Color(0, 255, 128));

        p.add(title, BorderLayout.WEST);
        p.add(lblSystemCode, BorderLayout.EAST);
        return p;
    }

    private JPanel createMainLayout() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(5, 40, 5, 40));

        p.add(createTopIdentityRow());
        p.add(Box.createVerticalStrut(15));

        JPanel gridBody = new JPanel(new GridLayout(1, 2, 30, 0));
        gridBody.setOpaque(false);
        gridBody.add(createContactBlock());
        gridBody.add(createAthleteBlock());
        p.add(gridBody);
        p.add(Box.createVerticalGlue());

        return p;
    }

    private JPanel createTopIdentityRow() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(createTitledBorder("IDENTIFICACIÓN PRINCIPAL"));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(2, 10, 5, 10);

        g.gridy = 0;
        g.gridx = 0; g.weightx = 0.0; p.add(createLabel(LanguageManager.get("clients.form.id")), g);
        g.gridx = 1; g.weightx = 1.0; p.add(createLabel(LanguageManager.get("clients.form.name")), g);
        g.gridx = 2; g.weightx = 0.0; p.add(createLabel("PROFESIÓN / OFICIO:"), g);

        g.gridy = 1;
        g.gridx = 0; g.weightx = 0.0;
        JPanel pId = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pId.setOpaque(false);

        cmbIdType = new JComboBox<>(new String[]{"V", "E", "J"}); styleCombo(cmbIdType, 60);

        txtIdNumber = createTextField();
        fixSize(txtIdNumber, 130, INPUT_HEIGHT);
        txtIdNumber.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { formatIdNumber(); }});

        SoftButton bSearch = new SoftButton(null);
        bSearch.setText("🔍"); bSearch.setBorder(null); fixSize(bSearch, 45, INPUT_HEIGHT);
        bSearch.addActionListener(e -> searchClientByDNI());

        pId.add(cmbIdType); pId.add(txtIdNumber); pId.add(bSearch);
        JPanel pIdWrap = new JPanel(new BorderLayout()); pIdWrap.setOpaque(false); pIdWrap.add(pId, BorderLayout.WEST);
        p.add(pIdWrap, g);

        g.gridx = 1; g.weightx = 1.0;
        txtFullName = createTextField(); txtFullName.setPreferredSize(new Dimension(10, INPUT_HEIGHT));
        p.add(txtFullName, g);

        g.gridx = 2; g.weightx = 0.0;
        txtProfession = createTextField(); fixSize(txtProfession, 240, INPUT_HEIGHT);
        p.add(txtProfession, g);

        return p;
    }

    private JPanel createContactBlock() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(createTitledBorder("CONTACTO & UBICACIÓN"));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.gridx = 0;
        g.insets = new Insets(4, 8, 4, 8);

        JPanel pPhoneRow = new JPanel(new BorderLayout(0, 5));
        pPhoneRow.setOpaque(false);
        pPhoneRow.add(createLabel("TELÉFONO PRINCIPAL Y ALTERNATIVO:"), BorderLayout.NORTH);

        JPanel pInputGroup = new JPanel(new BorderLayout(8, 0));
        pInputGroup.setOpaque(false);

        JPanel pPrefix = new JPanel(new BorderLayout());
        pPrefix.setOpaque(false);

        lblFlagIcon = new JLabel();
        lblFlagIcon.setHorizontalAlignment(SwingConstants.CENTER);
        fixSize(lblFlagIcon, 45, INPUT_HEIGHT);

        Vector<String> codes = new Vector<>();
        for(String[] d : COUNTRY_DATA) codes.add(d[0]);
        cmbCountryCode = new JComboBox<>(codes);
        styleCombo(cmbCountryCode, 75);
        cmbCountryCode.addActionListener(e -> updateFlagIcon());

        pPrefix.add(lblFlagIcon, BorderLayout.WEST);
        pPrefix.add(cmbCountryCode, BorderLayout.CENTER);

        txtPhone = createTextField();
        txtPhoneAlt = new JTextField();

        pInputGroup.add(pPrefix, BorderLayout.WEST);
        pInputGroup.add(txtPhone, BorderLayout.CENTER);

        pPhoneRow.add(pInputGroup, BorderLayout.CENTER);
        p.add(pPhoneRow, g);

        p.add(createLabel(LanguageManager.get("clients.form.email")), g);
        txtEmail = createTextField();
        p.add(txtEmail, g);

        p.add(createLabel(LanguageManager.get("clients.form.instagram")), g);
        txtInstagram = createTextField();
        p.add(txtInstagram, g);

        p.add(createLabel(LanguageManager.get("clients.form.address")), g);
        txtAddress = createTextArea(2);
        JScrollPane sc = new JScrollPane(txtAddress);
        sc.setOpaque(false);
        sc.getViewport().setOpaque(false);
        fixSize(sc, 200, TEXT_AREA_H);
        p.add(sc, g);

        return p;
    }

    private JPanel createAthleteBlock() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false); p.setBorder(createTitledBorder("PERFIL DEL ATLETA"));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0; g.gridx = 0;
        g.insets = new Insets(4, 8, 4, 8);

        p.add(createLabel(LanguageManager.get("clients.form.athlete")), g);
        txtAthleteName = createTextField(); p.add(txtAthleteName, g);

        JPanel rowTriple = new JPanel(new GridBagLayout()); rowTriple.setOpaque(false);
        GridBagConstraints g2 = new GridBagConstraints(); g2.fill = GridBagConstraints.HORIZONTAL; g2.gridy = 0;

        g2.gridx=0; g2.weightx=0.32; g2.insets = new Insets(0, 0, 0, 10);
        JPanel p1 = new JPanel(new BorderLayout()); p1.setOpaque(false);
        p1.add(createLabel(LanguageManager.get("clients.form.birth")), BorderLayout.NORTH);

        datePickerBirth = createLuxuryDatePicker();
        p1.add(datePickerBirth, BorderLayout.CENTER);
        rowTriple.add(p1, g2);

        g2.gridx=1; g2.weightx=0.18; g2.insets = new Insets(0, 0, 0, 10);
        JPanel p2 = new JPanel(new BorderLayout()); p2.setOpaque(false);
        p2.add(createLabel("TALLA:"), BorderLayout.NORTH);
        cmbSize = new JComboBox<>(SIZES); styleCombo(cmbSize, 0); p2.add(cmbSize, BorderLayout.CENTER);
        rowTriple.add(p2, g2);

        g2.gridx=2; g2.weightx=0.50; g2.insets = new Insets(0, 0, 0, 0);
        JPanel p3 = new JPanel(new BorderLayout()); p3.setOpaque(false);
        p3.add(createLabel(LanguageManager.get("clients.form.category")), BorderLayout.NORTH);
        cmbCategory = new JComboBox<>(new String[]{"Escuela", "Infantil", "Juvenil", "Máster", "Federado"});
        styleCombo(cmbCategory, 0); p3.add(cmbCategory, BorderLayout.CENTER);
        rowTriple.add(p3, g2);
        p.add(rowTriple, g);

        JPanel rowClub = new JPanel(new BorderLayout(15, 0)); rowClub.setOpaque(false);
        JPanel pClub = new JPanel(new BorderLayout()); pClub.setOpaque(false);
        pClub.add(createLabel(LanguageManager.get("clients.form.club")), BorderLayout.NORTH);
        cmbClub = new JComboBox<>(); styleCombo(cmbClub, 0); pClub.add(cmbClub, BorderLayout.CENTER);

        chkVip = new JCheckBox("VIP"); chkVip.setOpaque(false); chkVip.setForeground(COLOR_GOLD);
        chkVip.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JPanel pVip = new JPanel(new GridBagLayout()); pVip.setOpaque(false); pVip.add(chkVip);

        rowClub.add(pClub, BorderLayout.CENTER); rowClub.add(pVip, BorderLayout.EAST);
        p.add(rowClub, g);

        p.add(createLabel(LanguageManager.get("clients.form.notes")), g);
        txtObservations = createTextArea(2);
        JScrollPane sc = new JScrollPane(txtObservations); sc.setOpaque(false); sc.getViewport().setOpaque(false);
        fixSize(sc, 200, TEXT_AREA_H);
        p.add(sc, g);

        return p;
    }

    private JPanel createActionsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false); p.setBorder(new EmptyBorder(10, 40, 20, 40));
        Dimension d = new Dimension(135, 50);

        btnCancel = createSoftBtn("/images/icons/icon_cancel_gold.png", Color.DARK_GRAY, "VOLVER", d);
        // [CORRECCIÓN CRÍTICA] Liberación de foco global antes de cerrar
        btnCancel.addActionListener(e -> {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
            dispose();
        });

        JPanel pR = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); pR.setOpaque(false);
        btnDelete = createSoftBtn("/images/icons/icon_delete_gold.png", new Color(140, 30, 30), "ELIMINAR", d);
        btnDelete.addActionListener(e -> deleteAction());

        btnClear = createSoftBtn("/images/icons/icon_broom_gold.png", new Color(60, 80, 100), "LIMPIAR", d);
        btnClear.addActionListener(e -> clearFormFields());

        btnSave = createSoftBtn("/images/icons/icon_save_gold.png", new Color(20, 20, 20), "GUARDAR", d);
        btnSave.setBorder(new LineBorder(COLOR_GOLD, 2));
        btnSave.addActionListener(e -> saveClientAction());

        pR.add(btnDelete); pR.add(btnClear); pR.add(btnSave);
        p.add(btnCancel, BorderLayout.WEST); p.add(pR, BorderLayout.EAST);
        return p;
    }

    private DatePicker createLuxuryDatePicker() {
        DatePickerSettings s = new DatePickerSettings();
        s.setFormatForDatesCommonEra("dd/MM/yyyy");
        s.setColor(DateArea.BackgroundOverallCalendarPanel, new Color(30,30,30));
        s.setColor(DateArea.BackgroundMonthAndYearMenuLabels, COLOR_GOLD);
        s.setColor(DateArea.TextMonthAndYearMenuLabels, Color.BLACK);
        s.setColor(DateArea.CalendarTextWeekdays, COLOR_GOLD);
        s.setColor(DateArea.CalendarBackgroundNormalDates, new Color(30,30,30));
        s.setColor(DateArea.CalendarBackgroundSelectedDate, new Color(220, 0, 115));
        s.setColor(DateArea.CalendarBorderSelectedDate, Color.WHITE);
        s.setColor(DateArea.BackgroundTodayLabel, COLOR_GOLD);
        s.setColor(DateArea.TextTodayLabel, Color.BLACK);
        try { s.setColor(DateArea.CalendarTextNormalDates, Color.WHITE); } catch (Throwable t) {}
        s.setFontCalendarDateLabels(new Font("Segoe UI", Font.BOLD, 14));
        DatePicker dp = new DatePicker(s);
        JTextField f = dp.getComponentDateTextField();
        f.setBackground(COLOR_INPUT_BG);
        f.setForeground(COLOR_TEXT_INPUT);
        f.setFont(FONT_INPUT);
        f.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(0, 8, 0, 8)));
        f.setPreferredSize(new Dimension(100, INPUT_HEIGHT));
        JButton b = dp.getComponentToggleCalendarButton();
        b.setText("📅");
        b.setBackground(COLOR_GOLD);
        b.setForeground(Color.BLACK);
        b.setBorder(null);
        b.setPreferredSize(new Dimension(INPUT_HEIGHT, INPUT_HEIGHT));
        return dp;
    }

    private void saveClientAction() {
        if (txtFullName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnSave.setText("GUARDANDO...");
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Client c = (currentClient != null) ? currentClient : new Client();

        c.setIdType((String) cmbIdType.getSelectedItem());
        c.setIdNumber(txtIdNumber.getText().replace(".", "").trim());
        c.setFullName(txtFullName.getText().trim().toUpperCase());
        c.setProfession(txtProfession.getText().trim().toUpperCase());
        c.setPhone(txtPhone.getText().trim());
        c.setAlternatePhone(txtPhoneAlt.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setInstagram(txtInstagram.getText().trim());
        c.setAddress(txtAddress.getText().trim().toUpperCase());
        c.setAthleteName(txtAthleteName.getText().trim().isEmpty() ? c.getFullName() : txtAthleteName.getText().trim().toUpperCase());
        c.setClub((String) cmbClub.getSelectedItem());
        c.setCategory((String) cmbCategory.getSelectedItem());
        c.setSize((String) cmbSize.getSelectedItem());
        c.setVip(chkVip.isSelected());
        c.setMeasurements(txtObservations.getText().trim().toUpperCase());

        try {
            if(datePickerBirth.getDate() != null)
                c.setBirthDate(datePickerBirth.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch(Exception e){}

        Client finalC = c;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (finalC.getCode() == null || finalC.getCode().isEmpty()) {
                    if (controller != null) finalC.setCode(controller.generateNextCode());
                }
                if (controller != null) {
                    if (currentClient == null) return controller.saveClient(finalC);
                    else return controller.updateClient(finalC);
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    setCursor(Cursor.getDefaultCursor());
                    btnSave.setText("GUARDAR");
                    btnSave.setEnabled(true);
                    btnCancel.setEnabled(true);

                    if (exito) {
                        JOptionPane.showMessageDialog(AddEditClientDialog.this,
                                "¡Cliente guardado exitosamente!",
                                "SICONI", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(AddEditClientDialog.this,
                                "Error al guardar. Verifique conexión.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AddEditClientDialog.this, "Error crítico: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void searchClientByDNI() {
        if(controller == null) return;
        String rawDni = txtIdNumber.getText().trim().replace(".", "");
        if (rawDni.isEmpty()) return;
        formatIdNumber();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Client, Void>() {
            @Override
            protected Client doInBackground() {
                return controller.findClientByDNI(rawDni);
            }
            @Override
            protected void done() {
                try {
                    Client f = get();
                    if (f != null) {
                        currentClient = f; fillDataFromObject(f); btnDelete.setVisible(true);
                        JOptionPane.showMessageDialog(AddEditClientDialog.this, "Cliente encontrado: " + f.getFullName());
                    } else {
                        JOptionPane.showMessageDialog(AddEditClientDialog.this, "No se encontró el cliente.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {}
                setCursor(Cursor.getDefaultCursor());
            }
        }.execute();
    }

    private void fillDataFromObject(Client client) {
        lblSystemCode.setText("CÓDIGO: " + client.getCode());
        cmbIdType.setSelectedItem(client.getIdType());
        txtIdNumber.setText(client.getIdNumber()); formatIdNumber();
        txtFullName.setText(client.getFullName());
        txtProfession.setText(client.getProfession());
        txtPhone.setText(client.getPhone());
        txtEmail.setText(client.getEmail());
        txtInstagram.setText(client.getInstagram());
        txtAthleteName.setText(client.getAthleteName());
        cmbClub.setSelectedItem(client.getClub());
        cmbCategory.setSelectedItem(client.getCategory());
        cmbSize.setSelectedItem(client.getSize());
        txtAddress.setText(client.getAddress());
        txtObservations.setText(client.getMeasurements());
        chkVip.setSelected(client.isVip());
        try {
            if (client.getBirthDate() != null && !client.getBirthDate().isEmpty())
                try {
                    datePickerBirth.setDate(LocalDate.parse(client.getBirthDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } catch (Exception ex) {
                    datePickerBirth.setDate(LocalDate.parse(client.getBirthDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
        } catch(Exception e){}
    }

    private void clearFormFields() {
        currentClient = null; refreshSystemCode();
        txtIdNumber.setText(""); txtFullName.setText(""); txtProfession.setText("");
        txtPhone.setText(""); txtPhoneAlt.setText(""); txtEmail.setText("");
        txtInstagram.setText(""); txtAthleteName.setText(""); txtAddress.setText("");
        txtObservations.setText(""); datePickerBirth.clear(); chkVip.setSelected(false);
        btnDelete.setVisible(false); txtIdNumber.requestFocus();
    }

    private void deleteAction() {
        if (controller != null && JOptionPane.showConfirmDialog(this, "¿Eliminar permanentemente?", "SICONI", JOptionPane.YES_NO_OPTION) == 0) {
            new SwingWorker<Boolean, Void>(){
                protected Boolean doInBackground() { return controller.deleteClient(currentClient.getCode()); }
                protected void done() { dispose(); }
            }.execute();
        }
    }

    private void refreshSystemCode() {
        if(controller == null) return;
        new SwingWorker<String, Void>() {
            protected String doInBackground() { return controller.generateNextCode(); }
            protected void done() {
                try { lblSystemCode.setText("CÓDIGO: " + get()); } catch(Exception e){}
            }
        }.execute();
    }

    private void updateFlagIcon() {
        try {
            int idx = cmbCountryCode.getSelectedIndex();
            URL u = getClass().getResource("/images/flags/" + COUNTRY_DATA[idx][2]);
            if(u != null) lblFlagIcon.setIcon(new ImageIcon(new ImageIcon(u).getImage().getScaledInstance(35, 22, 4)));
        } catch(Exception e) { lblFlagIcon.setIcon(null); }
    }

    private void formatIdNumber() {
        String t = txtIdNumber.getText().replace(".", "").trim();
        if(t.matches("\\d+")) {
            try { txtIdNumber.setText(new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.getDefault())).format(Long.parseLong(t)).replace(",", "."));
            } catch(Exception e){}
        }
    }

    private JTextField createTextField() {
        JTextField t = new JTextField(); t.setBackground(COLOR_INPUT_BG); t.setForeground(COLOR_TEXT_INPUT);
        t.setCaretColor(COLOR_GOLD); t.setFont(FONT_INPUT);
        t.setBorder(new CompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(0, 8, 0, 8)));
        ((AbstractDocument) t.getDocument()).setDocumentFilter(new UppercaseFilter());
        return t;
    }

    private JTextArea createTextArea(int r) {
        JTextArea t = new JTextArea(r, 10); t.setBackground(COLOR_INPUT_BG); t.setForeground(COLOR_TEXT_INPUT);
        t.setCaretColor(COLOR_GOLD); t.setFont(FONT_INPUT); t.setLineWrap(true); t.setWrapStyleWord(true);
        ((AbstractDocument) t.getDocument()).setDocumentFilter(new UppercaseFilter());
        return t;
    }

    private void styleCombo(JComboBox<?> b, int w) {
        b.setBackground(COLOR_INPUT_BG); b.setForeground(COLOR_TEXT_INPUT); b.setFont(FONT_INPUT);
        if(w > 0) fixSize(b, w, INPUT_HEIGHT);
    }

    private SoftButton createSoftBtn(String icon, Color bg, String text, Dimension d) {
        SoftButton b = new SoftButton(null); b.setText(text); b.setBackground(bg); fixSize(b, d.width, d.height);
        try { b.setIcon(new ImageIcon(new ImageIcon(getClass().getResource(icon)).getImage().getScaledInstance(24, 24, 4))); } catch(Exception e){}
        return b;
    }

    private void fixSize(JComponent c, int w, int h) { c.setPreferredSize(new Dimension(w, h)); c.setMinimumSize(new Dimension(w, h)); }
    private JLabel createLabel(String t) { JLabel l = new JLabel(t); l.setForeground(COLOR_LABEL); l.setFont(FONT_LABEL); return l; }
    private TitledBorder createTitledBorder(String t) { TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), t); b.setTitleColor(COLOR_GOLD); b.setTitleFont(FONT_TITLE_BORDER); return b; }

    static class UppercaseFilter extends DocumentFilter {
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException { fb.insertString(offset, text.toUpperCase(), attr); }
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException { fb.replace(offset, length, text.toUpperCase(), attrs); }
    }
}