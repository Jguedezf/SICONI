/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ClientCheckInDialog.java
 * VERSIÓN: 4.2.0 (UX Premium + Padding Fix)
 * FECHA: 06 de Febrero de 2026
 * HORA: 10:00 PM (Hora de Venezuela)
 * DESCRIPCIÓN TÉCNICA:
 * Módulo de Gestión Rápida de Clientes (Quick Check-In).
 * Diseñado para entornos de alta concurrencia, permite el registro, búsqueda
 * y edición de clientes en una sola ventana modal optimizada.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.controller.ClientController;
import com.swimcore.model.Client;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * [VISTA - CHECK-IN] Clase que gestiona la interfaz de registro rápido de clientes.
 * [POO - HERENCIA] Extiende de JDialog para operar como un formulario modal.
 * * FUNCIONALIDAD: CRUD simplificado y búsqueda en tiempo real para procesos de venta.
 */
public class ClientCheckInDialog extends JDialog {

    // [MVC - CONTROLADOR] Enlace con la lógica de negocio de clientes.
    private final ClientController clientController;

    // Objeto que almacena el resultado de la operación (Cliente seleccionado/creado).
    private Client selectedClient = null;

    // --- VARIABLES DE ESTADO ---
    // Controlan el comportamiento del formulario (Creación vs Edición).
    private boolean isEditMode = false;
    private String currentClientCode = null;

    // --- COMPONENTES DE INTERFAZ GRÁFICA ---
    private JTextField txtSearch;
    private JPanel pnlForm;
    private SoftButton btnAction; // Botón dinámico (Registrar/Modificar)
    private SoftButton btnDelete;
    private JLabel lblIdDisplay;

    // Campos de entrada de datos
    private JTextField txtName, txtPhone, txtClub, txtEmail;
    private JComboBox<String> cmbCountry; // Selector de prefijo internacional
    private JTextArea txtAddress;

    // Paleta de Colores (Theme Manager)
    private final Color COL_BG = new Color(20, 20, 20);
    private final Color COL_GOLD = new Color(212, 175, 55);
    private final Color COL_INPUT = new Color(40, 40, 40);
    private final Color COL_RED = new Color(200, 50, 50);

    /**
     * Constructor de la clase. Inicializa la ventana y sus componentes.
     */
    public ClientCheckInDialog(Frame parent) {
        super(parent, "Gestión de Clientes", true);
        this.clientController = new ClientController();

        setSize(600, 720); // Dimensiones ajustadas para contenido extendido
        setLocationRelativeTo(parent);

        // Estilización del contenedor principal
        getRootPane().setBorder(new LineBorder(COL_GOLD, 2));
        getContentPane().setBackground(COL_BG);
        setLayout(new BorderLayout());

        // Construcción modular de la UI
        initHeader();
        initContent();
        initFooter();

        limpiarFormulario(); // Inicialización de estado limpio

        // [EVENTO DE VENTANA] Foco automático en el campo de búsqueda al abrir.
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) { txtSearch.requestFocus(); }
        });
    }

    // Metodo de acceso para recuperar el cliente procesado desde la vista padre.
    public Client getSelectedClient() { return selectedClient; }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 30, 10, 30));

        JLabel lblTitle = new JLabel("GESTIÓN RÁPIDA DE CLIENTES", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COL_GOLD);

        header.add(lblTitle, BorderLayout.NORTH);
        add(header, BorderLayout.NORTH);
    }

    private void initContent() {
        JPanel main = new JPanel(null); // Absolute Layout para control preciso
        main.setOpaque(false);

        // --- SECCIÓN 1: BÚSQUEDA ---
        JLabel lblS = new JLabel("BUSCAR (Cédula o Nombre):");
        lblS.setForeground(Color.WHITE); lblS.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblS.setBounds(40, 10, 200, 20); main.add(lblS);

        txtSearch = new JTextField(); styleField(txtSearch);
        txtSearch.setBounds(40, 35, 380, 40);
        txtSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtSearch.addActionListener(e -> buscarCliente());
        main.add(txtSearch);

        SoftButton btnFind = new SoftButton(null); btnFind.setText("🔍");
        btnFind.setBounds(430, 35, 60, 40);
        btnFind.addActionListener(e -> buscarCliente());
        main.add(btnFind);

        // Botón de Limpieza (Reset)
        SoftButton btnClean = new SoftButton(null); btnClean.setText("🧹");
        btnClean.setToolTipText("Limpiar / Nuevo Cliente");
        btnClean.setBounds(500, 35, 50, 40);
        btnClean.setForeground(Color.CYAN);
        btnClean.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            limpiarFormulario();
        });
        main.add(btnClean);

        // --- SECCIÓN 2: FORMULARIO DE DATOS ---
        pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        pnlForm.setBounds(30, 90, 540, 480);
        pnlForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY), " Datos del Cliente ",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.GRAY));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;

        // Visualización de ID / Estado
        lblIdDisplay = new JLabel("NUEVO REGISTRO", SwingConstants.RIGHT);
        lblIdDisplay.setForeground(Color.GREEN);
        lblIdDisplay.setFont(new Font("Consolas", Font.BOLD, 14));
        g.gridwidth=2; g.gridx=0; g.gridy=0; pnlForm.add(lblIdDisplay, g);

        // Campo Nombre
        g.gridwidth=1; g.gridy++;
        pnlForm.add(createLabel("NOMBRE COMPLETO:"), g);
        txtName = new JTextField(); styleField(txtName);
        g.gridx=1; pnlForm.add(txtName, g);

        // --- BLOQUE TELEFÓNICO INTERNACIONAL ---
        g.gridx=0; g.gridy++;
        pnlForm.add(createLabel("NÚMERO DE CONTACTO:"), g);

        JPanel pnlPhone = new JPanel(new BorderLayout(5, 0));
        pnlPhone.setOpaque(false);

        // Selector de País (Prefijos)
        String[] countries = {"🇻🇪 VEN (+58)", "🇺🇸 USA (+1)", "🇨🇴 COL (+57)", "🇧🇷 BRA (+55)", "🇪🇸 ESP (+34)"};
        cmbCountry = new JComboBox<>(countries);
        cmbCountry.setPreferredSize(new Dimension(110, 35));
        cmbCountry.setBackground(COL_INPUT);
        cmbCountry.setForeground(Color.WHITE);
        cmbCountry.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        // Evento: Actualización automática del prefijo en el campo de texto
        cmbCountry.addActionListener(e -> updatePhoneCode());

        txtPhone = new JTextField(); styleField(txtPhone);

        pnlPhone.add(cmbCountry, BorderLayout.WEST);
        pnlPhone.add(txtPhone, BorderLayout.CENTER);

        g.gridx=1; pnlForm.add(pnlPhone, g);
        // -----------------------------------

        // Campo Email
        g.gridx=0; g.gridy++;
        pnlForm.add(createLabel("CORREO:"), g);
        txtEmail = new JTextField(); styleField(txtEmail);
        g.gridx=1; pnlForm.add(txtEmail, g);

        // Campo Dirección (JTextArea con Padding corregido)
        g.gridx=0; g.gridy++;
        pnlForm.add(createLabel("DIRECCIÓN:"), g);
        txtAddress = new JTextArea(2, 20);
        txtAddress.setBackground(COL_INPUT); txtAddress.setForeground(Color.WHITE);
        txtAddress.setCaretColor(COL_GOLD);
        txtAddress.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(5, 10, 5, 10))); // Padding interno
        txtAddress.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g.gridx=1; pnlForm.add(new JScrollPane(txtAddress), g);

        // Campo Club/Organización
        g.gridx=0; g.gridy++;
        pnlForm.add(createLabel("CLUB / ORG:"), g);
        txtClub = new JTextField(); styleField(txtClub);
        g.gridx=1; pnlForm.add(txtClub, g);

        main.add(pnlForm);
        add(main, BorderLayout.CENTER);
    }

    private void initFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0,0,20,0));

        btnDelete = new SoftButton(null);
        btnDelete.setText("ELIMINAR 🗑️");
        btnDelete.setForeground(COL_RED);
        btnDelete.setPreferredSize(new Dimension(130, 45));
        btnDelete.addActionListener(e -> eliminarCliente());
        btnDelete.setVisible(false); // Oculto por defecto en modo creación

        btnAction = new SoftButton(null);
        btnAction.setText("REGISTRAR Y USAR");
        btnAction.setForeground(COL_GOLD);
        btnAction.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAction.setPreferredSize(new Dimension(200, 45));
        btnAction.addActionListener(e -> procesarGuardado());

        SoftButton btnCancel = new SoftButton(null);
        btnCancel.setText("CANCELAR");
        btnCancel.setForeground(Color.GRAY);
        btnCancel.setPreferredSize(new Dimension(100, 45));
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnDelete);
        footer.add(btnAction);
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);
    }

    // ========================================================================================
    //                                  LÓGICA DE NEGOCIO
    // ========================================================================================

    private void updatePhoneCode() {
        String item = (String) cmbCountry.getSelectedItem();
        if (item != null) {
            // Extracción del código numérico del string del combo
            String code = item.substring(item.indexOf("(") + 1, item.indexOf(")"));
            String currentText = txtPhone.getText().trim();

            if (currentText.isEmpty() || currentText.startsWith("+")) {
                txtPhone.setText(code + " ");
            } else {
                txtPhone.setText(code + " " + currentText);
            }
            txtPhone.requestFocus();
        }
    }

    /**
     * Realiza una búsqueda de cliente por coincidencia de nombre o cédula.
     * Si encuentra, carga los datos (Modo Edición).
     * Si no, sugiere crear uno nuevo con el término buscado.
     */
    private void buscarCliente() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) return;

        List<Client> all = clientController.getAllClients();
        Client found = null;
        for (Client c : all) {
            if (c.getFullName().toUpperCase().contains(query.toUpperCase()) ||
                    (c.getIdNumber() != null && c.getIdNumber().contains(query))) {
                found = c;
                break;
            }
        }

        if (found != null) {
            cargarClienteEnFormulario(found);
            SoundManager.getInstance().playClick();
        } else {
            SoundManager.getInstance().playError();
            int opt = JOptionPane.showConfirmDialog(this, "No encontrado. ¿Desea usar '" + query + "' para uno nuevo?", "Aviso", JOptionPane.YES_NO_OPTION);
            if(opt == JOptionPane.YES_OPTION) {
                limpiarFormulario();
                // Si no es un número, asumimos que es un nombre y lo pre-cargamos
                if(query.matches("\\d+")) { /* Es cédula, no la ponemos en nombre */ }
                else { txtName.setText(query.toUpperCase()); }
                txtName.requestFocus();
            }
        }
    }

    private void cargarClienteEnFormulario(Client c) {
        isEditMode = true;
        currentClientCode = c.getCode();

        lblIdDisplay.setText("EDITANDO: " + c.getCode());
        lblIdDisplay.setForeground(COL_GOLD);

        txtName.setText(c.getFullName());
        txtPhone.setText(c.getPhone());
        txtEmail.setText(c.getEmail());
        txtAddress.setText(c.getAddress());
        txtClub.setText(c.getClub());

        // Cambio de estado visual de los botones
        btnAction.setText("MODIFICAR Y USAR");
        btnDelete.setVisible(true);
        pnlForm.repaint();
    }

    private void limpiarFormulario() {
        isEditMode = false;
        currentClientCode = null;

        // Generación de ID visual tentativo
        int nextId = clientController.getAllClients().size() + 1;
        lblIdDisplay.setText("NUEVO ID: DG-" + String.format("%04d", nextId));
        lblIdDisplay.setForeground(Color.GREEN);

        txtName.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtClub.setText("");
        txtSearch.setText("");

        cmbCountry.setSelectedIndex(0);
        txtPhone.setText("+58 ");

        btnAction.setText("REGISTRAR Y USAR");
        btnDelete.setVisible(false);
        txtName.requestFocus();
    }

    /**
     * [METODO DE PERSISTENCIA]
     * Gestiona tanto la creación como la actualización de registros (Upsert lógico).
     * Valida campos obligatorios y delega al controlador.
     */
    private void procesarGuardado() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre obligatorio."); return;
        }

        Client c = new Client();
        c.setFullName(txtName.getText().trim().toUpperCase());
        c.setPhone(txtPhone.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setAddress(txtAddress.getText().trim());
        c.setClub(txtClub.getText().trim());

        // Valores por defecto para campos no utilizados en este contexto rápido
        c.setAthleteName(c.getFullName());
        c.setCategory("General");
        c.setIdType("V");
        c.setInstagram(""); c.setMeasurements(""); c.setBirthDate("");

        // Lógica de asignación de Cédula (Si se buscó por número)
        String searchVal = txtSearch.getText().trim();
        if(searchVal.matches("\\d+") && !isEditMode) c.setIdNumber(searchVal);
        else if (!isEditMode) c.setIdNumber("S/C");
        else {
            // Mantenemos la cédula original en edición
            Client old = clientController.getAllClients().stream().filter(cl->cl.getCode().equals(currentClientCode)).findFirst().orElse(null);
            if(old!=null) c.setIdNumber(old.getIdNumber());
        }

        boolean exito;
        if (isEditMode) {
            c.setCode(currentClientCode);
            exito = clientController.updateClient(c);
        } else {
            c.setCode(String.format("DG-%04d", clientController.getAllClients().size() + 1));
            exito = clientController.saveClient(c);
        }

        if (exito) {
            selectedClient = c;
            SoundManager.getInstance().playClick();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error al procesar base de datos.");
        }
    }

    private void eliminarCliente() {
        if (!isEditMode) return;
        int opt = JOptionPane.showConfirmDialog(this, "¿Seguro de eliminar a " + txtName.getText() + "?", "Eliminar", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            if (clientController.deleteClient(currentClientCode)) {
                JOptionPane.showMessageDialog(this, "Cliente eliminado.");
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.");
            }
        }
    }

    // --- ESTILOS VISUALES (LOOK & FEEL) ---

    private void styleField(JTextField t) {
        t.setBackground(COL_INPUT);
        t.setForeground(Color.WHITE);
        t.setCaretColor(COL_GOLD);

        // Padding interno para mejorar la legibilidad del texto
        t.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(5, 10, 5, 10)
        ));

        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setPreferredSize(new Dimension(200, 35));
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.LIGHT_GRAY);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return l;
    }
}