/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: AddEditClientDialog.java
 * VERSIÓN: 11.5.0 (Ultimate Transparency & Calendar Fix)
 * FECHA: January 28, 2026 - 12:15AM
 * DESCRIPCIÓN:
 * 1. TRANSPARENCIA TOTAL: Todos los paneles internos setOpaque(false) para ver bg3.png.
 * 2. CALENDARIO: Fuente Bold 18 en input y Bold 16 en el menú desplegable.
 * 3. LAYOUT: Nombre pegado a lupa y campo Teléfono Alternativo añadido.
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
import com.swimcore.util.LuxuryMessage;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Vector;

public class AddEditClientDialog extends JDialog {

    private final ClientController controller;
    private Client currentClient;

    private JComboBox<String> cmbIdType, cmbClub, cmbCategory, cmbSize, cmbCountryCode;
    private JTextField txtIdNumber, txtFullName, txtProfession, txtPhone, txtPhoneAlt, txtEmail, txtInstagram, txtAthleteName;
    private DatePicker datePickerBirth;
    private JCheckBox chkVip;
    private JTextArea txtAddress, txtObservations;
    private JLabel lblFlagIcon;
    private JLabel lblSystemCode;
    private SoftButton btnSave, btnDelete, btnClear, btnCancel;

    private final int INPUT_HEIGHT = 45;
    private final int TEXT_AREA_HEIGHT = 85;

    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_TEXT_INPUT = new Color(255, 255, 255);
    private final Color COLOR_LABEL = new Color(235, 235, 235);
    private final Color COLOR_INPUT_BG = new Color(45, 45, 45);

    private final Font FONT_INPUT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_TITLE_BORDER = new Font("Segoe UI", Font.BOLD, 12);

    private final String[] SIZES = {"4", "6", "8", "10", "12", "14", "16", "S", "M", "L", "XL", "A MEDIDA"};
    private final String[][] COUNTRY_DATA = {
            {"VEN", "+58", "ven.png"}, {"USA", "+1",  "usa.png"}, {"BRA", "+55", "bra.png"},
            {"ARG", "+54", "arg.png"}, {"COL", "+57", "col.png"}, {"PER", "+51", "per.png"},
            {"CHI", "+56", "chi.png"}, {"ESP", "+34", "esp.png"}
    };

    public AddEditClientDialog(Frame owner, Client clientToEdit) {
        super(owner, "FICHA TÉCNICA", true);
        this.controller = new ClientController();
        this.currentClient = clientToEdit;

        setSize(1150, 680);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        getRootPane().setBorder(new LineBorder(COLOR_GOLD, 2));

        // PANEL DE FONDO PRINCIPAL
        JPanel backgroundPanel = new ImagePanel("/images/bg3.png");
        backgroundPanel.setBackground(new Color(30, 30, 30));
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        add(createHeader(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(createContentPanel());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
        add(createActionsPanel(), BorderLayout.SOUTH);

        loadClubs();
        updateSystemCode();

        if (currentClient != null) {
            fillData(currentClient);
            btnDelete.setVisible(true);
        } else {
            cmbCountryCode.setSelectedIndex(0);
            updateFlagIcon();
            btnDelete.setVisible(false);
        }

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                if(currentClient == null) txtIdNumber.requestFocusInWindow();
            }
        });
        setVisible(true);
    }

    public AddEditClientDialog(Frame owner) { this(owner, null); }

    private void updateSystemCode() {
        String displayCode = (currentClient != null) ? currentClient.getCode() : controller.generateNextCode();
        lblSystemCode.setText("CÓDIGO: " + displayCode);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false); // TRANSPARENTE
        header.setBorder(new EmptyBorder(15, 30, 5, 30));

        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pRight.setOpaque(false);
        pRight.setPreferredSize(new Dimension(200, 40));

        lblSystemCode = new JLabel("DG-XXXX");
        lblSystemCode.setFont(new Font("Consolas", Font.BOLD, 22));
        lblSystemCode.setForeground(new Color(0, 255, 128));
        pRight.add(lblSystemCode);

        JPanel pLeft = new JPanel();
        pLeft.setOpaque(false);
        pLeft.setPreferredSize(new Dimension(200, 40));

        JLabel lblTitle = new JLabel("FICHA TÉCNICA DEL ATLETA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(COLOR_GOLD);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(pLeft, BorderLayout.WEST);
        header.add(lblTitle, BorderLayout.CENTER);
        header.add(pRight, BorderLayout.EAST);

        return header;
    }

    private JPanel createContentPanel() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false); // TRANSPARENTE
        main.setBorder(new EmptyBorder(5, 30, 5, 30));

        // 1. IDENTIDAD
        JPanel identityPanel = new JPanel(new GridBagLayout());
        identityPanel.setOpaque(false); // TRANSPARENTE
        identityPanel.setBorder(createTitledBorder("IDENTIFICACIÓN PRINCIPAL"));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;

        // Títulos
        g.gridx = 0; g.gridy = 0; g.weightx = 0.0; g.insets = new Insets(5, 10, 5, 0);
        identityPanel.add(createLabel(LanguageManager.get("clients.form.id")), g);

        g.gridx = 1; g.weightx = 1.0; g.insets = new Insets(5, 5, 5, 5);
        identityPanel.add(createLabel(LanguageManager.get("clients.form.name")), g);

        g.gridx = 2; g.weightx = 0.0; g.insets = new Insets(5, 5, 5, 10);
        identityPanel.add(createLabel("PROFESIÓN / OFICIO:"), g);

        // Campos
        g.gridx = 0; g.gridy = 1; g.weightx = 0.0; g.insets = new Insets(5, 10, 5, 0);
        JPanel pId = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pId.setOpaque(false);

        cmbIdType = new JComboBox<>(new String[]{"V", "E", "J"});
        styleCombo(cmbIdType); fixSize(cmbIdType, 70, INPUT_HEIGHT);

        txtIdNumber = createTextField(); fixSize(txtIdNumber, 130, INPUT_HEIGHT);
        txtIdNumber.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { formatIdNumber(); }
        });

        SoftButton btnSearch = new SoftButton(null);
        btnSearch.setText("🔍"); fixSize(btnSearch, 55, INPUT_HEIGHT);
        btnSearch.addActionListener(e -> searchClientByDNI());

        pId.add(cmbIdType); pId.add(txtIdNumber); pId.add(btnSearch);
        identityPanel.add(pId, g);

        g.gridx = 1; g.weightx = 1.0; g.insets = new Insets(5, 5, 5, 5);
        txtFullName = createTextField();
        identityPanel.add(txtFullName, g);

        g.gridx = 2; g.weightx = 0.0; g.insets = new Insets(5, 5, 5, 10);
        txtProfession = createTextField(); fixSize(txtProfession, 180, INPUT_HEIGHT);
        identityPanel.add(txtProfession, g);

        main.add(identityPanel);
        main.add(Box.createVerticalStrut(15));

        // 2. DETALLES
        JPanel detailsGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        detailsGrid.setOpaque(false); // TRANSPARENTE
        detailsGrid.add(createContactPanel());
        detailsGrid.add(createAthletePanel());
        main.add(detailsGrid);

        return main;
    }

    private JPanel createContactPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false); // TRANSPARENTE
        p.setBorder(createTitledBorder("CONTACTO & UBICACIÓN"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 5, 6, 5); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0; g.gridx = 0; g.gridy = 0;

        // Fila Teléfonos
        JPanel pLabels = new JPanel(new GridLayout(1, 2, 10, 0)); pLabels.setOpaque(false);
        pLabels.add(createLabel("TELÉFONO PRINCIPAL:")); pLabels.add(createLabel("TELÉFONO ALTERNATIVO:"));
        p.add(pLabels, g);

        g.gridy++;
        JPanel pPhones = new JPanel(new GridLayout(1, 2, 10, 0)); pPhones.setOpaque(false);

        JPanel pPhone1 = new JPanel(new BorderLayout(5, 0)); pPhone1.setOpaque(false);
        Vector<String> codes = new Vector<>();
        for(String[] data : COUNTRY_DATA) codes.add(data[0]);
        cmbCountryCode = new JComboBox<>(codes); styleCombo(cmbCountryCode); fixSize(cmbCountryCode, 85, INPUT_HEIGHT);
        cmbCountryCode.addActionListener(e -> updatePhoneCode());

        lblFlagIcon = new JLabel(); fixSize(lblFlagIcon, 55, INPUT_HEIGHT);
        lblFlagIcon.setHorizontalAlignment(SwingConstants.CENTER);

        txtPhone = createTextField();
        JPanel pLeft = new JPanel(new BorderLayout()); pLeft.setOpaque(false);
        pLeft.add(lblFlagIcon, BorderLayout.WEST); pLeft.add(cmbCountryCode, BorderLayout.CENTER);
        pPhone1.add(pLeft, BorderLayout.WEST); pPhone1.add(txtPhone, BorderLayout.CENTER);

        txtPhoneAlt = createTextField();
        pPhones.add(pPhone1); pPhones.add(txtPhoneAlt);
        p.add(pPhones, g);

        g.gridy++; p.add(createLabel(LanguageManager.get("clients.form.email")), g);
        g.gridy++; txtEmail = createTextField(); p.add(txtEmail, g);

        g.gridy++; p.add(createLabel(LanguageManager.get("clients.form.instagram")), g);
        g.gridy++; txtInstagram = createTextField(); p.add(txtInstagram, g);

        g.gridy++; p.add(createLabel(LanguageManager.get("clients.form.address")), g);
        g.gridy++; txtAddress = createTextArea();
        JScrollPane scrollAddr = new JScrollPane(txtAddress); fixSize(scrollAddr, 200, TEXT_AREA_HEIGHT);
        scrollAddr.setBorder(new LineBorder(Color.GRAY)); scrollAddr.getViewport().setOpaque(false); scrollAddr.setOpaque(false);
        p.add(scrollAddr, g);

        return p;
    }

    private JPanel createAthletePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false); // TRANSPARENTE
        p.setBorder(createTitledBorder("PERFIL DEL ATLETA"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 5, 6, 5); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0; g.gridx = 0; g.gridy = 0;

        p.add(createLabel(LanguageManager.get("clients.form.athlete")), g);
        g.gridy++; txtAthleteName = createTextField(); p.add(txtAthleteName, g);

        g.gridy++;
        JPanel rowMix = new JPanel(new GridBagLayout()); rowMix.setOpaque(false);
        GridBagConstraints g2 = new GridBagConstraints(); g2.fill = GridBagConstraints.HORIZONTAL;

        g2.gridx=0; g2.weightx=0.45; g2.insets = new Insets(0, 0, 0, 20);
        JPanel pDate = new JPanel(new BorderLayout()); pDate.setOpaque(false);
        pDate.add(createLabel(LanguageManager.get("clients.form.birth")), BorderLayout.NORTH);

        DatePickerSettings ds = new DatePickerSettings();
        ds.setFormatForDatesCommonEra("dd/MM/yyyy");
        ds.setColor(DateArea.BackgroundOverallCalendarPanel, new Color(40,40,40));
        ds.setColor(DateArea.BackgroundMonthAndYearMenuLabels, COLOR_GOLD);
        ds.setColor(DateArea.TextMonthAndYearMenuLabels, Color.BLACK);
        ds.setColor(DateArea.CalendarBackgroundSelectedDate, new Color(220, 0, 115));
        ds.setColor(DateArea.TextFieldBackgroundValidDate, COLOR_INPUT_BG);
        ds.setColor(DateArea.TextFieldBackgroundInvalidDate, COLOR_INPUT_BG);

        // FUENTE DEL POPUP CALENDARIO
        Font calF = new Font("Segoe UI", Font.BOLD, 16);
        ds.setFontCalendarDateLabels(calF); ds.setFontCalendarWeekdayLabels(calF);
        ds.setFontMonthAndYearMenuLabels(calF); ds.setFontTodayLabel(calF);

        datePickerBirth = new DatePicker(ds); styleDatePicker(datePickerBirth);
        pDate.add(datePickerBirth, BorderLayout.CENTER);
        rowMix.add(pDate, g2);

        g2.gridx=1; g2.weightx=0.35; g2.insets = new Insets(0, 0, 0, 0);
        JPanel pSize = new JPanel(new BorderLayout()); pSize.setOpaque(false);
        pSize.add(createLabel(LanguageManager.get("clients.form.size")), BorderLayout.NORTH);
        cmbSize = new JComboBox<>(SIZES); styleCombo(cmbSize); pSize.add(cmbSize, BorderLayout.CENTER);
        rowMix.add(pSize, g2);

        g2.gridx=2; g2.weightx=0.2; g2.insets = new Insets(30, 10, 0, 0);
        chkVip = new JCheckBox("VIP"); chkVip.setOpaque(false); chkVip.setForeground(COLOR_GOLD); chkVip.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rowMix.add(chkVip, g2);
        p.add(rowMix, g);

        g.gridy++;
        JPanel row2 = new JPanel(new GridLayout(1, 2, 20, 0)); row2.setOpaque(false);
        JPanel pClub = new JPanel(new BorderLayout()); pClub.setOpaque(false);
        pClub.add(createLabel(LanguageManager.get("clients.form.club")), BorderLayout.NORTH);
        cmbClub = new JComboBox<>(); styleCombo(cmbClub); pClub.add(cmbClub, BorderLayout.CENTER);

        JPanel pCat = new JPanel(new BorderLayout()); pCat.setOpaque(false);
        pCat.add(createLabel(LanguageManager.get("clients.form.category")), BorderLayout.NORTH);
        cmbCategory = new JComboBox<>(new String[]{"Escuela", "Infantil", "Juvenil", "Máster", "Federado"});
        styleCombo(cmbCategory); pCat.add(cmbCategory, BorderLayout.CENTER);
        row2.add(pClub); row2.add(pCat); p.add(row2, g);

        g.gridy++; p.add(createLabel(LanguageManager.get("clients.form.notes")), g);
        g.gridy++; txtObservations = createTextArea();
        JScrollPane scrollObs = new JScrollPane(txtObservations); fixSize(scrollObs, 200, TEXT_AREA_HEIGHT);
        scrollObs.setBorder(new LineBorder(Color.GRAY)); scrollObs.getViewport().setOpaque(false); scrollObs.setOpaque(false);
        p.add(scrollObs, g);

        return p;
    }

    private JPanel createActionsPanel() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false); p.setBorder(new EmptyBorder(5, 40, 20, 40));
        Dimension sqSize = new Dimension(60, 60);

        btnCancel = createSquareButton("/images/icons/icon_cancel_gold.png", Color.DARK_GRAY, "Volver", sqSize);
        btnCancel.addActionListener(e -> dispose());
        JPanel pLeft = new JPanel(new FlowLayout(FlowLayout.LEFT)); pLeft.setOpaque(false); pLeft.add(btnCancel);

        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0)); pRight.setOpaque(false);
        btnDelete = createSquareButton("/images/icons/icon_delete_gold.png", new Color(140, 30, 30), "Eliminar", sqSize);
        btnDelete.setVisible(false); btnDelete.addActionListener(e -> deleteCurrentClient());
        btnClear = createSquareButton("/images/icons/icon_broom_gold.png", new Color(60, 80, 100), "Limpiar", sqSize);
        btnClear.addActionListener(e -> clearForm());
        btnSave = createSquareButton("/images/icons/icon_save_gold.png", new Color(20, 20, 20), "Guardar", sqSize);
        btnSave.setBorder(new LineBorder(COLOR_GOLD, 2)); btnSave.addActionListener(e -> saveClient());

        pRight.add(btnDelete); pRight.add(btnClear); pRight.add(btnSave);
        p.add(pLeft, BorderLayout.WEST); p.add(pRight, BorderLayout.EAST);
        return p;
    }

    // --- MÉTODOS DE ESTILO ---
    private void styleDatePicker(DatePicker dp) {
        JTextField f = dp.getComponentDateTextField();
        f.setBackground(COLOR_INPUT_BG); f.setForeground(COLOR_TEXT_INPUT); f.setFont(new Font("Segoe UI", Font.BOLD, 18));
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(0,10,0,0)));
        fixSize(f, 150, INPUT_HEIGHT);
        JButton b = dp.getComponentToggleCalendarButton(); b.setText("📅"); fixSize(b, INPUT_HEIGHT, INPUT_HEIGHT);
    }

    private void styleCombo(JComboBox box) {
        box.setBackground(COLOR_INPUT_BG); box.setForeground(COLOR_TEXT_INPUT); box.setFont(FONT_INPUT);
        fixSize(box, 200, INPUT_HEIGHT);
    }

    private JTextField createTextField() {
        JTextField t = new JTextField(); t.setBackground(COLOR_INPUT_BG); t.setForeground(COLOR_TEXT_INPUT);
        t.setCaretColor(COLOR_GOLD); t.setFont(FONT_INPUT);
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(0, 10, 0, 10)));
        fixSize(t, 200, INPUT_HEIGHT);
        ((AbstractDocument) t.getDocument()).setDocumentFilter(new UppercaseFilter());
        return t;
    }

    private JTextArea createTextArea() {
        JTextArea t = new JTextArea(); t.setBackground(COLOR_INPUT_BG); t.setForeground(COLOR_TEXT_INPUT);
        t.setCaretColor(COLOR_GOLD); t.setFont(FONT_INPUT); t.setLineWrap(true); t.setWrapStyleWord(true);
        ((AbstractDocument) t.getDocument()).setDocumentFilter(new UppercaseFilter());
        return t;
    }

    private void fixSize(JComponent c, int w, int h) { Dimension d = new Dimension(w, h); c.setPreferredSize(d); c.setMinimumSize(d); }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), title);
        b.setTitleColor(COLOR_GOLD); b.setTitleFont(FONT_TITLE_BORDER); return b;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text); l.setForeground(COLOR_LABEL); l.setFont(FONT_LABEL); return l;
    }

    private SoftButton createSquareButton(String iconPath, Color bg, String tooltip, Dimension size) {
        ImageIcon icon = createIcon(iconPath, 48, 48);
        SoftButton btn = new SoftButton(icon); btn.setText(""); btn.setBackground(bg);
        fixSize(btn, size.width, size.height); btn.setToolTipText(tooltip); return btn;
    }

    private ImageIcon createIcon(String path, int w, int h) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                String f = path.substring(path.lastIndexOf("/") + 1);
                url = getClass().getResource("/images/" + f);
                if (url == null) url = getClass().getResource("/images/icons/" + f);
            }
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {}
        return null;
    }

    // --- LÓGICA DE NEGOCIO ---
    private void formatIdNumber() {
        String text = txtIdNumber.getText().replace(".", "").trim();
        if (text.matches("\\d+")) {
            try {
                long val = Long.parseLong(text);
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
                symbols.setGroupingSeparator('.');
                DecimalFormat df = new DecimalFormat("#,###", symbols);
                txtIdNumber.setText(df.format(val));
            } catch (Exception e) {}
        }
    }

    private void updatePhoneCode() {
        int idx = cmbCountryCode.getSelectedIndex();
        if (idx >= 0 && idx < COUNTRY_DATA.length) {
            updateFlagIcon();
            txtPhone.setText(COUNTRY_DATA[idx][1] + " ");
            txtPhone.requestFocus();
        }
    }

    private void updateFlagIcon() {
        int idx = cmbCountryCode.getSelectedIndex();
        if (idx >= 0 && idx < COUNTRY_DATA.length) {
            try {
                URL url = getClass().getResource("/images/" + COUNTRY_DATA[idx][2]);
                if (url == null) url = getClass().getResource("/images/flags/" + COUNTRY_DATA[idx][2]);
                if (url != null) lblFlagIcon.setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(50, 35, Image.SCALE_SMOOTH)));
            } catch(Exception e) { lblFlagIcon.setIcon(null); }
        }
    }

    private void searchClientByDNI() {
        String rawDni = txtIdNumber.getText().trim().replace(".", "");
        if(rawDni.isEmpty()) return;
        formatIdNumber();
        Client found = controller.findClientByDNI(rawDni);
        if(found != null) {
            this.currentClient = found; fillData(found);
            btnDelete.setVisible(true); updateSystemCode();
            LuxuryMessage.show("SICONI", LanguageManager.get("clients.form.found"), false);
        } else {
            LuxuryMessage.show("Info", LanguageManager.get("clients.form.notfound"), true);
        }
    }

    private void clearForm() {
        this.currentClient = null;
        txtIdNumber.setText(""); txtFullName.setText(""); txtProfession.setText("");
        txtPhone.setText(""); txtPhoneAlt.setText(""); txtEmail.setText(""); txtInstagram.setText(""); txtAthleteName.setText("");
        txtAddress.setText(""); txtObservations.setText(""); datePickerBirth.clear();
        btnDelete.setVisible(false); updateSystemCode(); txtIdNumber.requestFocus();
    }

    private void deleteCurrentClient() {
        if (currentClient == null) return;
        if(JOptionPane.showConfirmDialog(this, "¿Eliminar permanentemente?", "SICONI", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if(controller.deleteClient(currentClient.getCode())) {
                LuxuryMessage.show("SICONI", LanguageManager.get("clients.form.msg.deleted"), false);
                dispose();
            }
        }
    }

    private void saveClient() {
        if (txtFullName.getText().trim().isEmpty()) {
            LuxuryMessage.show("Info", LanguageManager.get("clients.form.msg.missing"), true);
            return;
        }
        Client c = (currentClient != null) ? currentClient : new Client();
        if (currentClient == null) c.setCode(controller.generateNextCode());
        c.setIdType((String) cmbIdType.getSelectedItem());
        c.setIdNumber(txtIdNumber.getText().replace(".", "").trim());
        c.setFullName(txtFullName.getText().trim());
        c.setPhone(txtPhone.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setAddress(txtAddress.getText().trim());
        c.setInstagram(txtInstagram.getText().trim());
        c.setVip(chkVip.isSelected());
        c.setAthleteName(txtAthleteName.getText().trim().isEmpty() ? txtFullName.getText().trim() : txtAthleteName.getText().trim());
        c.setBirthDate((datePickerBirth.getDate() != null) ? datePickerBirth.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        c.setClub((String) cmbClub.getSelectedItem());
        c.setCategory((String) cmbCategory.getSelectedItem());
        c.setMeasurements(txtObservations.getText().trim());

        c.setProfession(txtProfession.getText().trim());
        c.setAlternatePhone(txtPhoneAlt.getText().trim());

        if ((currentClient == null ? controller.saveClient(c) : controller.updateClient(c))) {
            LuxuryMessage.show("SICONI", LanguageManager.get("clients.form.msg.success"), false);
            dispose();
        } else {
            LuxuryMessage.show("Error", LanguageManager.get("clients.form.msg.error"), true);
        }
    }

    private void fillData(Client client) {
        if (client == null) return;
        txtIdNumber.setText(client.getIdNumber()); formatIdNumber();
        txtFullName.setText(client.getFullName());
        txtProfession.setText(client.getProfession());
        txtPhone.setText(client.getPhone());
        txtPhoneAlt.setText(client.getAlternatePhone());
        txtEmail.setText(client.getEmail());
        txtInstagram.setText(client.getInstagram());
        chkVip.setSelected(client.isVip());
        txtAthleteName.setText(client.getAthleteName());
        cmbClub.setSelectedItem(client.getClub());
        cmbCategory.setSelectedItem(client.getCategory());
        txtAddress.setText(client.getAddress());
        txtObservations.setText(client.getMeasurements());
        try { if (client.getBirthDate() != null && !client.getBirthDate().isEmpty()) datePickerBirth.setDate(LocalDate.parse(client.getBirthDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))); } catch(Exception ignored) {}
    }

    private void loadClubs() {
        Vector<String> clubs = new Vector<>(); clubs.add("Sin Club / Particular");
        try (Connection conn = Conexion.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT name FROM clubs ORDER BY name ASC")) {
            while(rs.next()) clubs.add(rs.getString("name")); cmbClub.setModel(new DefaultComboBoxModel<>(clubs));
        } catch (Exception e) {}
    }

    static class UppercaseFilter extends DocumentFilter {
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException { fb.insertString(offset, text.toUpperCase(), attr); }
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException { fb.replace(offset, length, text.toUpperCase(), attrs); }
    }
}