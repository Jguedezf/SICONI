/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ClientManagementDialog.java
 * VERSIÓN: 3.1.1 (Unified Directory & Selection Mode)
 * DESCRIPCIÓN: Directorio unificado que ahora también funciona como selector
 * de cliente para iniciar un nuevo pedido, eliminando ventanas duplicadas.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.controller.ClientController;
import com.swimcore.model.Client;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.ClientCard;
import com.swimcore.view.components.SoftButton;
import com.swimcore.view.dialogs.AddEditClientDialog;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class ClientManagementDialog extends JDialog {

    private final ClientController controller;
    private List<Client> allClients;
    private JPanel cardsPanel;
    private Client selectedClient = null; // Cliente seleccionado actualmente
    private final boolean isSelectionMode; // Nuevo campo para el modo Pedidos

    // Botones de acción
    private SoftButton btnEdit, btnDelete, btnSelectClient;

    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);
    private final String PLACEHOLDER_TEXT = "Buscar por nombre, atleta o código...";

    // Constructor con modo Selección
    public ClientManagementDialog(Frame owner, boolean isSelectionMode) {
        super(owner, "Directorio de Clientes", true);
        this.controller = new ClientController();
        this.isSelectionMode = isSelectionMode;

        setSize(1100, 650);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(COLOR_GOLD, 2));

        JPanel backgroundPanel = new ImagePanel("/images/bg3.png");
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        add(createHeader(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createSideActionPanel(), BorderLayout.EAST);

        loadAllClients();
        refreshClientCards(allClients);
        updateSelectionModeVisibility();
    }

    // Constructor de compatibilidad (Modo Gestión por defecto)
    public ClientManagementDialog(Frame owner) {
        this(owner, false);
    }

    // Nuevo método para que el Dashboard pueda recuperar el cliente seleccionado
    public Client getSelectedClient() {
        return selectedClient;
    }

    private void updateSelectionModeVisibility() {
        // En modo selección, solo se ve el botón de "SELECCIONAR CLIENTE"
        if (isSelectionMode) {
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
            // El botón "NUEVO CLIENTE" se mantiene por si no lo encuentra.
            btnSelectClient.setVisible(true);
            btnSelectClient.setText("CONFIRMAR PEDIDO");

            JLabel title = (JLabel) ((JPanel)getComponent(0)).getComponent(0);
            title.setText("SELECCIONAR CLIENTE PARA PEDIDO");
        } else {
            // Modo Gestión
            btnSelectClient.setVisible(false);
        }
    }


    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 0, 5, 0));
        JLabel title = new JLabel("DIRECTORIO DE CLIENTES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(COLOR_GOLD);
        headerPanel.add(title);
        return headerPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 20, 20, 10));

        // --- Buscador ---
        JTextField searchField = new JTextField(PLACEHOLDER_TEXT);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setForeground(Color.GRAY);
        searchField.setPreferredSize(new Dimension(0, 45));
        searchField.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(0, 15, 0, 0)
        ));

        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(PLACEHOLDER_TEXT)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText(PLACEHOLDER_TEXT);
                }
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterClients(searchField.getText()); }
        });

        centerPanel.add(searchField, BorderLayout.NORTH);

        // --- Galería de Tarjetas ---
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        cardsPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel createSideActionPanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setOpaque(false);
        sidePanel.setBorder(new EmptyBorder(60, 10, 20, 20));
        sidePanel.setPreferredSize(new Dimension(220, 0));

        // --- Botones de Gestión (Visibles en Modo Gestión) ---
        SoftButton btnNew = createActionButton("NUEVO CLIENTE", "/images/icons/icon_add_user.png", new Color(0, 153, 76));
        btnNew.addActionListener(e -> openAddEditDialog(null));
        sidePanel.add(btnNew);
        sidePanel.add(Box.createVerticalStrut(15));

        btnEdit = createActionButton("EDITAR", "/images/icons/icon_edit.png", new Color(0, 102, 204));
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(e -> {
            if (selectedClient != null) openAddEditDialog(selectedClient);
        });
        sidePanel.add(btnEdit);
        sidePanel.add(Box.createVerticalStrut(15));

        btnDelete = createActionButton("ELIMINAR", "/images/icons/icon_delete.png", new Color(204, 0, 0));
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteSelectedClient());
        sidePanel.add(btnDelete);
        sidePanel.add(Box.createVerticalStrut(15));

        // --- Botón de Selección de Pedido (Visible en Modo Selección) ---
        btnSelectClient = createActionButton("SELECCIONAR", "/images/icons/icon_check.png", COLOR_FUCSIA);
        btnSelectClient.setEnabled(false);
        btnSelectClient.addActionListener(e -> {
            if(selectedClient != null) {
                // Cierra la ventana, permitiendo que el Dashboard tome el selectedClient
                dispose();
            }
        });
        sidePanel.add(btnSelectClient);

        sidePanel.add(Box.createVerticalGlue());

        // Botón Volver
        SoftButton btnBack = createActionButton("VOLVER AL MENÚ", "/images/icons/icon_exit.png", new Color(80, 80, 80));
        btnBack.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            dispose();
        });
        sidePanel.add(btnBack);

        return sidePanel;
    }

    private SoftButton createActionButton(String text, String iconPath, Color bg) {
        ImageIcon icon = null;
        try {
            URL url = getClass().getResource(iconPath);
            if (url != null) icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        } catch (Exception e) {}

        SoftButton btn = new SoftButton(icon);
        btn.setText(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(190, 60));
        btn.setMaximumSize(new Dimension(190, 60));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void onCardSelected(Client client) {
        this.selectedClient = client;
        btnEdit.setEnabled(true);
        btnDelete.setEnabled(true);
        if(isSelectionMode) {
            btnSelectClient.setEnabled(true);
        }
        refreshClientCards(allClients);
    }

    private void openAddEditDialog(Client clientToEdit) {
        SoundManager.getInstance().playClick();
        AddEditClientDialog dialog = new AddEditClientDialog((Frame) this.getOwner(), clientToEdit);

        loadAllClients();
        refreshClientCards(allClients);
        selectedClient = null;
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        if(isSelectionMode) btnSelectClient.setEnabled(false);
    }

    private void deleteSelectedClient() {
        if (selectedClient == null) return;
        int opt = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + selectedClient.getFullName() + " del sistema?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            if (controller.deleteClient(selectedClient.getCode())) {
                SoundManager.getInstance().playClick();
                selectedClient = null;
                btnEdit.setEnabled(false);
                btnDelete.setEnabled(false);
                loadAllClients();
                refreshClientCards(allClients);
            } else {
                SoundManager.getInstance().playError();
            }
        }
    }

    private void loadAllClients() { this.allClients = controller.getAllClients(); }

    private void filterClients(String query) {
        String lowerCaseQuery = query.toLowerCase().trim();
        if (lowerCaseQuery.isEmpty() || lowerCaseQuery.equals(PLACEHOLDER_TEXT.toLowerCase())) {
            refreshClientCards(allClients);
        } else {
            List<Client> filteredList = allClients.stream()
                    .filter(client ->
                            client.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                                    client.getAthleteName().toLowerCase().contains(lowerCaseQuery) ||
                                    (client.getCode() != null && client.getCode().toLowerCase().contains(lowerCaseQuery))
                    )
                    .collect(Collectors.toList());
            refreshClientCards(filteredList);
        }
    }

    private void refreshClientCards(List<Client> clients) {
        cardsPanel.removeAll();
        for (Client client : clients) {
            ClientCard card = new ClientCard(client);
            // Selección visual
            if (selectedClient != null && client.getId() == selectedClient.getId()) {
                card.setBorder(BorderFactory.createLineBorder(COLOR_GOLD, 3));
            }
            // Evento doble clic para editar/seleccionar
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    onCardSelected(client);
                    if (e.getClickCount() == 2) {
                        if (isSelectionMode) {
                            selectedClient = client;
                            dispose(); // Cierra y devuelve el cliente
                        } else {
                            openAddEditDialog(client);
                        }
                    }
                }
            });
            cardsPanel.add(card);
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
}