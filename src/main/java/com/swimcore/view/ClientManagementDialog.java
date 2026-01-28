/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ClientManagementDialog.java
 * VERSIÓN: 4.0.0 (Command Center UI)
 * FECHA: January 27, 2026 - 11:55PM
 * DESCRIPCIÓN: Rediseño completo a "Centro de Mando".
 * 1. Barra de búsqueda inteligente con lupa y filtro en tiempo real.
 * 2. Panel de acciones contextual (Editar/Eliminar solo al seleccionar).
 * 3. Lógica de visibilidad corregida para "Modo Selección" vs "Modo Gestión".
 * 4. Botón "Volver" reubicado en el footer para mejor UX.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.controller.ClientController;
import com.swimcore.model.Client;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.util.LuxuryMessage;
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
    private Client selectedClient = null;
    private final boolean isSelectionMode;

    private JTextField txtSearch;
    private JLabel lblTitle;
    private SoftButton btnNew, btnEdit, btnDelete, btnConfirm;

    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);
    private final String PLACEHOLDER_TEXT = LanguageManager.get("clients.search") + "...";

    public ClientManagementDialog(Frame owner, boolean isSelectionMode) {
        super(owner, LanguageManager.get("clients.title"), true);
        this.controller = new ClientController();
        this.isSelectionMode = isSelectionMode;

        setSize(1150, 700); // Ancho para que quepa todo
        setLocationRelativeTo(owner);
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(COLOR_GOLD, 2));

        JPanel backgroundPanel = new ImagePanel("/images/bg2.png");
        backgroundPanel.setBackground(new Color(40, 40, 40));
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.EAST);
        add(createFooter(), BorderLayout.SOUTH);

        loadAllClients();
        refreshClientCards(allClients);
        updateButtonStates(); // Lógica centralizada para botones
    }

    public ClientManagementDialog(Frame owner) { this(owner, false); }

    public Client getSelectedClient() { return selectedClient; }

    // --- CONSTRUCCIÓN DE LA INTERFAZ ---

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 0, 10, 0));

        lblTitle = new JLabel();
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(COLOR_GOLD);

        headerPanel.add(lblTitle);
        return headerPanel;
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(10, 30, 20, 20));

        // 1. Panel de Búsqueda (Nuevo y Mejorado)
        mainPanel.add(createSearchPanel(), BorderLayout.NORTH);

        // 2. Panel de Tarjetas
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        cardsPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchContainer.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(500, 45));

        txtSearch = new JTextField(PLACEHOLDER_TEXT);
        txtSearch.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setCaretColor(COLOR_GOLD);
        txtSearch.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(0, 15, 0, 15)
        ));

        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(PLACEHOLDER_TEXT)) {
                    txtSearch.setText("");
                    txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                    txtSearch.setForeground(Color.WHITE);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText(PLACEHOLDER_TEXT);
                }
            }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterClients(txtSearch.getText());
            }
        });

        SoftButton btnSearch = new SoftButton(null);
        btnSearch.setText("🔍");
        btnSearch.setPreferredSize(new Dimension(55, 45));
        btnSearch.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);

        searchContainer.add(searchPanel);
        return searchContainer;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(30, 10, 20, 30));
        actionPanel.setPreferredSize(new Dimension(240, 0));

        // 1. Nuevo Cliente
        btnNew = createActionButton(LanguageManager.get("clients.btn.new"), "/images/icons/icon_add_user_gold.png");
        btnNew.addActionListener(e -> openAddEditDialog(null));
        actionPanel.add(btnNew);
        actionPanel.add(Box.createVerticalStrut(15));

        // 2. Editar Cliente
        btnEdit = createActionButton(LanguageManager.get("clients.btn.edit"), "/images/icons/icon_edit_gold.png");
        btnEdit.addActionListener(e -> {
            if (selectedClient != null) openAddEditDialog(selectedClient);
        });
        actionPanel.add(btnEdit);
        actionPanel.add(Box.createVerticalStrut(15));

        // 3. Eliminar Cliente
        btnDelete = createActionButton(LanguageManager.get("clients.btn.delete"), "/images/icons/icon_delete_gold.png");
        btnDelete.addActionListener(e -> deleteSelectedClient());
        actionPanel.add(btnDelete);
        actionPanel.add(Box.createVerticalStrut(15));

        // 4. Botón de Confirmar (solo para modo selección)
        btnConfirm = createActionButton(LanguageManager.get("clients.btn.confirm"), "/images/icons/icon_check_gold.png");
        btnConfirm.setForeground(COLOR_FUCSIA);
        btnConfirm.addActionListener(e -> {
            if(selectedClient != null) dispose();
        });
        actionPanel.add(btnConfirm);

        actionPanel.add(Box.createVerticalGlue()); // Empuja el resto hacia arriba
        return actionPanel;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(0, 0, 20, 30));

        SoftButton btnBack = createActionButton(LanguageManager.get("clients.btn.back"), "/images/icons/icon_cancel_gold.png");
        btnBack.addActionListener(e -> {
            selectedClient = null;
            dispose();
        });

        footerPanel.add(btnBack);
        return footerPanel;
    }

    private SoftButton createActionButton(String text, String iconPath) {
        ImageIcon icon = createIcon(iconPath, 32, 32);
        SoftButton btn = new SoftButton(icon);
        btn.setText(text.toUpperCase());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(200, 65));
        btn.setMaximumSize(new Dimension(200, 65));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private ImageIcon createIcon(String path, int w, int h) {
        try {
            URL url = getClass().getResource(path);
            if(url == null) url = getClass().getResource(path.replace("_gold", ""));
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {}
        return null;
    }

    // --- LÓGICA DE LA APLICACIÓN ---

    private void updateButtonStates() {
        boolean clientIsSelected = (selectedClient != null);

        if (isSelectionMode) {
            lblTitle.setText(LanguageManager.get("clients.title.select"));
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
            btnConfirm.setVisible(true);
            btnConfirm.setEnabled(clientIsSelected);
        } else {
            lblTitle.setText(LanguageManager.get("clients.title"));
            btnEdit.setVisible(true);
            btnDelete.setVisible(true);
            btnConfirm.setVisible(false);
            btnEdit.setEnabled(clientIsSelected);
            btnDelete.setEnabled(clientIsSelected);
        }
    }

    private void onCardSelected(Client client) {
        this.selectedClient = client;
        updateButtonStates();
        refreshClientCards(allClients); // Refresca para mostrar el borde seleccionado
    }

    private void openAddEditDialog(Client clientToEdit) {
        new AddEditClientDialog((Frame) this.getOwner(), clientToEdit);

        // Refrescar todo al volver
        loadAllClients();
        String currentSearch = txtSearch.getText();
        if(!currentSearch.equals(PLACEHOLDER_TEXT) && !currentSearch.isEmpty()){
            filterClients(currentSearch);
        } else {
            refreshClientCards(allClients);
        }

        selectedClient = null;
        updateButtonStates();
    }

    private void deleteSelectedClient() {
        if (selectedClient == null) return;

        String msg = String.format(LanguageManager.get("clients.msg.delete.confirm"), selectedClient.getFullName());
        int opt = JOptionPane.showConfirmDialog(this, msg, LanguageManager.get("clients.msg.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            if (controller.deleteClient(selectedClient.getCode())) {
                LuxuryMessage.show("Éxito", LanguageManager.get("clients.msg.delete.success"), false);
                selectedClient = null;
                loadAllClients();
                filterClients(txtSearch.getText()); // Re-filtrar
                updateButtonStates();
            } else {
                LuxuryMessage.show("Error", LanguageManager.get("clients.msg.delete.error"), true);
            }
        }
    }

    private void loadAllClients() {
        this.allClients = controller.getAllClients();
    }

    private void filterClients(String query) {
        String lowerCaseQuery = query.toLowerCase().trim();
        if (lowerCaseQuery.isEmpty() || lowerCaseQuery.equals(PLACEHOLDER_TEXT.toLowerCase())) {
            refreshClientCards(allClients);
        } else {
            List<Client> filteredList = allClients.stream()
                    .filter(client ->
                            client.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                                    client.getAthleteName().toLowerCase().contains(lowerCaseQuery) ||
                                    (client.getCode() != null && client.getCode().toLowerCase().contains(lowerCaseQuery)) ||
                                    (client.getIdNumber() != null && client.getIdNumber().contains(lowerCaseQuery))
                    )
                    .collect(Collectors.toList());
            refreshClientCards(filteredList);
        }
    }

    private void refreshClientCards(List<Client> clients) {
        cardsPanel.removeAll();
        if (clients.isEmpty()) {
            JLabel emptyLabel = new JLabel(LanguageManager.get("clients.msg.empty"));
            emptyLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            emptyLabel.setForeground(Color.GRAY);
            cardsPanel.add(emptyLabel);
        } else {
            for (Client client : clients) {
                boolean isSelected = (selectedClient != null && client.getId() == selectedClient.getId());
                ClientCard card = new ClientCard(client, isSelected);

                card.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        onCardSelected(client);
                        if (e.getClickCount() == 2) {
                            if (isSelectionMode) {
                                dispose(); // Selecciona y cierra
                            } else {
                                openAddEditDialog(client); // Abre para editar
                            }
                        }
                    }
                });
                cardsPanel.add(card);
            }
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
}