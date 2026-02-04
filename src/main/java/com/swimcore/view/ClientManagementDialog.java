/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ClientManagementDialog.java
 * VERSIÓN: 8.0.0 (ANTI-FREEZE: Asynchronous Loading)
 * DESCRIPCIÓN:
 * 1. RENDIMIENTO: Toda la carga de datos (inicial y refresco) se hace en un hilo
 *    secundario (SwingWorker), eliminando el "botón hundido" y la lentitud.
 * 2. UX: Se muestra un mensaje de "Cargando..." y los botones se desactivan
 *    mientras se conecta a la base de datos.
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClientManagementDialog extends JDialog {

    private ClientController controller; // Se inicializará en segundo plano
    private List<Client> allClients = Collections.emptyList(); // Inicia como lista vacía
    private JPanel cardsPanel;
    private Client selectedClient = null;
    private final boolean isSelectionMode;

    private JTextField txtSearch;
    private JLabel lblTitle;
    private SoftButton btnNew, btnEdit, btnDelete, btnConfirm, btnBack;

    private final Color COLOR_GOLD = new Color(212, 175, 55);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);
    private final String PLACEHOLDER_TEXT = LanguageManager.get("clients.search") + "...";

    public ClientManagementDialog(Frame owner, boolean isSelectionMode) {
        super(owner, LanguageManager.get("clients.title"), true);
        this.isSelectionMode = isSelectionMode;

        setSize(1150, 700);
        setLocationRelativeTo(owner);

        getRootPane().setBorder(BorderFactory.createLineBorder(COLOR_GOLD, 2));

        JPanel backgroundPanel = new ImagePanel("/images/bg2.png");
        backgroundPanel.setBackground(new Color(40, 40, 40));
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.EAST);
        add(createFooter(), BorderLayout.SOUTH);

        // --- INICIAMOS LA CARGA ASÍNCRONA ---
        loadAndRefreshClientsAsync();
    }

    public ClientManagementDialog(Frame owner) { this(owner, false); }

    public Client getSelectedClient() { return selectedClient; }

    // --- MÉTODO CLAVE: CARGA DE DATOS SIN CONGELAR LA UI ---
    private void loadAndRefreshClientsAsync() {
        // 1. Desactivar botones y mostrar "Cargando..."
        setActionsEnabled(false);
        showLoadingState();

        // 2. Crear el hilo de trabajo
        new SwingWorker<List<Client>, Void>() {
            @Override
            protected List<Client> doInBackground() throws Exception {
                // Esto se ejecuta en un hilo secundario
                if (controller == null) {
                    controller = new ClientController(); // La primera vez, se conecta a la BD aquí
                }
                return controller.getAllClients(); // La consulta pesada se hace aquí
            }

            @Override
            protected void done() {
                try {
                    // Esto se ejecuta en el hilo principal cuando termina lo anterior
                    allClients = get(); // Obtenemos la lista de clientes
                    refreshClientCards(allClients); // Actualizamos las tarjetas
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorState("Error al cargar datos.");
                } finally {
                    // 3. Reactivar los botones
                    setActionsEnabled(true);
                    updateButtonStates();
                }
            }
        }.execute();
    }

    private void showLoadingState() {
        cardsPanel.removeAll();
        JLabel loadingLabel = new JLabel("Cargando clientes...");
        loadingLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loadingLabel.setForeground(Color.GRAY);
        cardsPanel.add(loadingLabel);
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void showErrorState(String message) {
        cardsPanel.removeAll();
        JLabel errorLabel = new JLabel(message);
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        errorLabel.setForeground(COLOR_FUCSIA);
        cardsPanel.add(errorLabel);
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void setActionsEnabled(boolean enabled) {
        btnNew.setEnabled(enabled);
        btnBack.setEnabled(enabled);
        // Edit y Delete dependen de la selección, se manejan en updateButtonStates
    }

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

        mainPanel.add(createSearchPanel(), BorderLayout.NORTH);

        cardsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(10, 10, 120, 10));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(cardsPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
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

        btnNew = createActionButton(LanguageManager.get("clients.btn.new"), "/images/icons/icon_add_user_gold.png");
        btnNew.addActionListener(e -> openAddEditDialog(null));
        actionPanel.add(btnNew);
        actionPanel.add(Box.createVerticalStrut(15));

        btnEdit = createActionButton(LanguageManager.get("clients.btn.edit"), "/images/icons/icon_edit_gold.png");
        btnEdit.addActionListener(e -> {
            if (selectedClient != null) openAddEditDialog(selectedClient);
        });
        actionPanel.add(btnEdit);
        actionPanel.add(Box.createVerticalStrut(15));

        btnDelete = createActionButton(LanguageManager.get("clients.btn.delete"), "/images/icons/icon_delete_gold.png");
        btnDelete.addActionListener(e -> deleteSelectedClient());
        actionPanel.add(btnDelete);
        actionPanel.add(Box.createVerticalStrut(15));

        btnConfirm = createActionButton(LanguageManager.get("clients.btn.confirm"), "/images/icons/icon_check_gold.png");
        btnConfirm.setForeground(COLOR_FUCSIA);
        btnConfirm.addActionListener(e -> {
            if(selectedClient != null) dispose();
        });
        actionPanel.add(btnConfirm);

        actionPanel.add(Box.createVerticalGlue());
        return actionPanel;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(0, 0, 20, 30));

        btnBack = createActionButton(LanguageManager.get("clients.btn.back"), "/images/icons/icon_cancel_gold.png");
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
        refreshClientCards(allClients); // Refresca visualmente la selección
    }

    // --- MÉTODO MODIFICADO ---
    private void openAddEditDialog(Client clientToEdit) {
        // La ventana AddEditClientDialog ya es rápida por sí misma
        AddEditClientDialog dialog = new AddEditClientDialog((Frame) this.getOwner(), clientToEdit);
        dialog.setVisible(true); // Se queda aquí hasta que se cierre

        // Al volver, recargamos la lista de forma asíncrona para no congelar
        loadAndRefreshClientsAsync();

        selectedClient = null;
        updateButtonStates();
    }

    private void deleteSelectedClient() {
        if (selectedClient == null) return;

        int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar a " + selectedClient.getFullName() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            if (controller.deleteClient(selectedClient.getCode())) {
                LuxuryMessage.show("Éxito", LanguageManager.get("clients.msg.delete.success"), false);
                selectedClient = null;
                loadAndRefreshClientsAsync(); // Refresco asíncrono
            } else {
                LuxuryMessage.show("Error", LanguageManager.get("clients.msg.delete.error"), true);
            }
        }
    }

    private void filterClients(String query) {
        String lowerCaseQuery = query.toLowerCase().trim();
        if (lowerCaseQuery.isEmpty() || lowerCaseQuery.equals(PLACEHOLDER_TEXT.toLowerCase())) {
            refreshClientCards(allClients);
        } else {
            List<Client> filteredList = allClients.stream()
                    .filter(client ->
                            client.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                                    client.getCode().toLowerCase().contains(lowerCaseQuery)
                    )
                    .collect(Collectors.toList());
            refreshClientCards(filteredList);
        }
    }

    private void refreshClientCards(List<Client> clients) {
        cardsPanel.removeAll();
        if (clients.isEmpty() && controller != null) { // Evita mostrar "no hay" mientras carga
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
                                dispose();
                            } else {
                                openAddEditDialog(client);
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