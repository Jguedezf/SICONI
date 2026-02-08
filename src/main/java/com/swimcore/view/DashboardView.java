/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * AUTORA: Johanna Gabriela Guédez Flores
 * PROFESORA: Ing. Dubraska Roca
 * ASIGNATURA: Técnicas de Programación III
 * * ARCHIVO: DashboardView.java
 * VERSIÓN: 4.0.0 (ANTI-FREEZE: Worker Implementation)
 * FECHA: 07 de Febrero de 2026
 * HORA: 04:45 PM (Hora de Venezuela)
 * * DESCRIPCIÓN: Panel de control principal (Dashboard).
 * SE HA OPTIMIZADO LA CARGA DE VENTANAS PARA EVITAR CONGELAMIENTOS.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.dao.ProductDAO;
import com.swimcore.model.Product;
import com.swimcore.util.CurrencyManager;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.AlertCard;
import com.swimcore.view.components.SoftButton;
import com.swimcore.view.dialogs.CurrencySettingsDialog;
import com.swimcore.model.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 * [VISTA - MVC] Clase principal que actúa como contenedor de la interfaz de usuario.
 * [POO - HERENCIA] Hereda de JFrame para la gestión de la ventana principal.
 * * FUNCIONALIDAD: Orquestador central de módulos (Ventas, Inventario, Reportes).
 */
public class DashboardView extends JFrame {

    // [POO - COMPOSICIÓN] Se instancia el DAO para consultas de inventario en tiempo real.
    private final ProductDAO productDAO = new ProductDAO();

    // [POO - ENCAPSULAMIENTO] Atributos privados para manejo de componentes y constantes visuales.
    private AlertCard alertCardStock;
    private final Color COLOR_BG = new Color(18, 18, 18);
    private final Color COLOR_CARD = new Color(30, 30, 30);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);
    private final Color COLOR_TEXTO = new Color(240, 240, 240);
    private JLabel lblRateValue;

    /**
     * Constructor del Dashboard.
     * Se encarga de la inicialización de componentes y la configuración del Layout.
     */
    public DashboardView() {
        setTitle(LanguageManager.get("dashboard.title"));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel;
        try { mainPanel = new ImagePanel("/images/bg.png"); }
        catch(Exception e) { mainPanel = new JPanel(); mainPanel.setBackground(COLOR_BG); }

        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // [POO - ABSTRACCIÓN] Se divide la construcción de la UI en métodos especializados.
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createCentralMenu(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        // Inicio del hilo secundario para verificación de stock.
        updateStockAlert();
    }

    /**
     * Construye el encabezado superior con Branding y Alertas.
     * Incluye el Widget de Tasa de Cambio (BCV).
     */
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel lblLogo = new JLabel();
        try {
            URL url = getClass().getResource("/images/logo_small.png");
            if (url != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(150, -1, Image.SCALE_SMOOTH));
                lblLogo.setIcon(icon);
            }
        } catch (Exception e) {}

        leftPanel.add(lblLogo);
        leftPanel.add(Box.createVerticalStrut(10));

        // [REQUERIMIENTO FUNCIONAL 2 - INVENTARIO]: Tarjeta de Alerta de Stock.
        alertCardStock = new AlertCard("⚠️", LanguageManager.get("dashboard.alert.calc"), () -> {
            SoundManager.getInstance().playClick();

            // FIX: Carga asíncrona para evitar congelamiento
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() {
                    InventoryView inventoryView = new InventoryView(DashboardView.this);
                    try { inventoryView.mostrarSoloBajoStock(); } catch (Exception ex) {}
                    inventoryView.setVisible(true);
                    return null;
                }
                @Override protected void done() { setCursor(Cursor.getDefaultCursor()); updateStockAlert(); }
            }.execute();
        });

        leftPanel.add(alertCardStock);

        // [REQUERIMIENTO FUNCIONAL 3 - VENTAS]: Widget de Tasa BCV para cálculo multimoneda.
        JPanel rateWidget = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rateWidget.setOpaque(false);

        lblRateValue = new JLabel(String.format(Locale.US, LanguageManager.get("dashboard.rate"), CurrencyManager.getTasa()));
        lblRateValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRateValue.setForeground(Color.LIGHT_GRAY);

        SoftButton btnEditRate = new SoftButton(createIcon("/images/icons/icon_edit.png", 20, 20));
        btnEditRate.setPreferredSize(new Dimension(40, 40));
        btnEditRate.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            new CurrencySettingsDialog(this).setVisible(true);
            lblRateValue.setText(String.format(Locale.US, LanguageManager.get("dashboard.rate"), CurrencyManager.getTasa()));
        });

        rateWidget.add(lblRateValue);
        rateWidget.add(btnEditRate);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rateWidget, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * [CONCURRENCIA - SWINGWORKER]
     * Método que ejecuta la consulta de productos con bajo stock en un hilo secundario.
     */
    private void updateStockAlert() {
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                // [DAO] Consulta al modelo de datos
                List<Product> allProducts = productDAO.getAllProducts();
                return allProducts.stream().filter(Product::isLowStock).count();
            }
            @Override
            protected void done() {
                try {
                    long lowStockCount = get();
                    if (lowStockCount > 0) {
                        String msg = String.format("<b>%d</b> productos requieren atención", lowStockCount);
                        alertCardStock.setMessage(msg);
                        alertCardStock.setVisible(true);
                    } else { alertCardStock.setVisible(false); }
                } catch (Exception e) { alertCardStock.setVisible(false); }
            }
        }.execute();
    }

    private JPanel createCentralMenu() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        JPanel grid = new JPanel(new GridLayout(2, 3, 25, 25));
        grid.setOpaque(false);

        // --- MÓDULO 1: GESTIÓN DE VENTAS (POS) ---
        grid.add(createBigButton(LanguageManager.get("dashboard.btn.newOrder"), LanguageManager.get("dashboard.btn.newOrder.sub"), "/images/orders.png", "🛒", e -> {

            // 1. Selección de Cliente (Rápida)
            ClientManagementDialog selector = new ClientManagementDialog(this, true);
            selector.setVisible(true);
            Client clienteSeleccionado = selector.getSelectedClient();

            if (clienteSeleccionado != null) {
                // FIX CRÍTICO: Usamos SwingWorker para construir la ventana de Ventas en segundo plano
                // Esto evita que el Dashboard se congele (el "limbo")
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                new SwingWorker<JDialog, Void>() {
                    @Override
                    protected JDialog doInBackground() {
                        // Construcción pesada ocurre aquí, sin bloquear la UI
                        JDialog frameVentas = new JDialog(DashboardView.this, "SICONI - NUEVO PEDIDO", true);
                        frameVentas.setUndecorated(false);
                        frameVentas.setSize(1280, 750);
                        frameVentas.setLocationRelativeTo(DashboardView.this);

                        try {
                            ImagePanel background = new ImagePanel("/images/bgdg.png");
                            background.setLayout(new BorderLayout());
                            background.add(new SalesView(clienteSeleccionado), BorderLayout.CENTER);
                            frameVentas.setContentPane(background);
                        } catch (Exception ex) {
                            frameVentas.add(new SalesView(clienteSeleccionado));
                        }
                        return frameVentas;
                    }

                    @Override
                    protected void done() {
                        try {
                            // Mostrar la ventana ya construida instantáneamente
                            get().setVisible(true);
                            updateStockAlert();
                        } catch(Exception ex) { ex.printStackTrace(); }
                        finally { setCursor(Cursor.getDefaultCursor()); }
                    }
                }.execute();
            }
        }));

        // --- MÓDULO 2: GESTIÓN DE INVENTARIO ---
        grid.add(createBigButton(LanguageManager.get("dashboard.btn.inventory"), LanguageManager.get("dashboard.btn.inventory.sub"), "/images/inventory.png", "📦", e -> {
            // FIX: Carga asíncrona del Inventario
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() {
                    new InventoryView(DashboardView.this).setVisible(true);
                    return null;
                }
                @Override protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    updateStockAlert();
                }
            }.execute();
        }));

        // --- MÓDULO 3: TALLER / MANUFACTURA ---
        grid.add(createBigButton(LanguageManager.get("dashboard.btn.workshop"), LanguageManager.get("dashboard.btn.workshop.sub"), "/images/workshop.png", "✂️", e -> {
            new OrderManagementView(this).setVisible(true);
        }));

        // --- MÓDULO 4: GESTIÓN DE CLIENTES / ATLETAS ---
        grid.add(createBigButton(LanguageManager.get("dashboard.btn.clients"), LanguageManager.get("dashboard.btn.clients.sub"), "/images/client.png", "👥", e -> {
            new ClientManagementDialog(this, false).setVisible(true);
        }));

        // --- MÓDULO 5: REPORTES GERENCIALES ---
        grid.add(createBigButton(LanguageManager.get("dashboard.btn.reports"), LanguageManager.get("dashboard.btn.reports.sub"), "/images/reports.png", "📊", e -> {
            // FIX: Carga asíncrona de Reportes
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() {
                    new ReportsView(DashboardView.this).setVisible(true);
                    return null;
                }
                @Override protected void done() { setCursor(Cursor.getDefaultCursor()); }
            }.execute();
        }));

        // Botón de Salida
        JButton btnExit = createBigButton(LanguageManager.get("dashboard.btn.exit"), LanguageManager.get("dashboard.btn.exit.sub"), "/images/logout.png", "🚪", null);
        btnExit.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnExit.putClientProperty("hoverColor", new Color(220, 20, 60)); btnExit.putClientProperty("hover", true); btnExit.repaint(); SoundManager.getInstance().playHover(); }
            public void mouseExited(MouseEvent e) { btnExit.putClientProperty("hoverColor", null); btnExit.putClientProperty("hover", false); btnExit.repaint(); }
            public void mousePressed(MouseEvent e) { SoundManager.getInstance().playClick(); }
        });
        btnExit.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, LanguageManager.get("dashboard.exit.msg"), LanguageManager.get("dashboard.exit.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
        });
        grid.add(btnExit);

        container.add(grid);
        return container;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0,0,10,0));
        JLabel lbl = new JLabel(LanguageManager.get("dashboard.footer"));
        lbl.setForeground(Color.DARK_GRAY);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(lbl);
        return footer;
    }

    private ImageIcon createIcon(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (Exception e) {}
        return null;
    }

    /**
     * Método auxiliar para la creación de botones principales del Dashboard.
     * [POO]: Implementa personalización gráfica mediante sobreescritura del método paint.
     */
    private JButton createBigButton(String title, String subtitle, String iconPath, String fallbackIcon, java.awt.event.ActionListener action) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setPreferredSize(new Dimension(320, 200));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);

        JLabel lblIcon = new JLabel();
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            URL url = getClass().getResource(iconPath);
            if (url != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH));
                lblIcon.setIcon(icon);
            } else {
                lblIcon.setText(fallbackIcon);
                lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
                lblIcon.setForeground(COLOR_FUCSIA);
            }
        } catch (Exception e) { }

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TEXTO);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);

        textPanel.add(lblTitle); textPanel.add(lblSub);
        btn.add(lblIcon, BorderLayout.CENTER);
        btn.add(textPanel, BorderLayout.SOUTH);

        if(action != null) btn.addActionListener(action);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.putClientProperty("hover", true); btn.repaint(); SoundManager.getInstance().playHover(); }
            public void mouseExited(MouseEvent e) { btn.putClientProperty("hover", false); btn.repaint(); }
            public void mousePressed(MouseEvent e) { SoundManager.getInstance().playClick(); }
        });

        // [POO - POLIMORFISMO] Renderizado customizado para efecto 'Glass/Card'.
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isHover = Boolean.TRUE.equals(c.getClientProperty("hover"));
                Color hoverColor = (Color) c.getClientProperty("hoverColor");
                int arc = 35;
                if (isHover) {
                    g2.setColor(new Color(50, 50, 50));
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), arc, arc);
                    g2.setColor(hoverColor != null ? hoverColor : COLOR_FUCSIA);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, arc, arc);
                } else {
                    g2.setColor(COLOR_CARD);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), arc, arc);
                }
                g2.dispose();
                super.paint(g, c);
            }
        });
        return btn;
    }
}