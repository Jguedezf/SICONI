/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: DashboardView.java
 * VERSIÓN: 3.2.1 (Window Width Expansion & Responsive Fix)
 * DESCRIPCIÓN:
 * 1. Ventana de ventas ensanchada a 1280x750 para corregir cortes visuales.
 * 2. Ajuste de layout para asegurar que los paneles internos se expandan.
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

public class DashboardView extends JFrame {

    private final ProductDAO productDAO = new ProductDAO();
    private AlertCard alertCardStock;
    private final Color COLOR_BG = new Color(18, 18, 18);
    private final Color COLOR_CARD = new Color(30, 30, 30);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);
    private final Color COLOR_TEXTO = new Color(240, 240, 240);
    private JLabel lblRateValue;

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

        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createCentralMenu(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        updateStockAlert();
    }

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
            } else {
                lblLogo.setText("SICONI");
                lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
                lblLogo.setForeground(Color.WHITE);
            }
        } catch (Exception e) {}

        leftPanel.add(lblLogo);
        leftPanel.add(Box.createVerticalStrut(10));

        alertCardStock = new AlertCard("⚠️", LanguageManager.get("dashboard.alert.calc"));
        alertCardStock.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SoundManager.getInstance().playClick();
                InventoryView inventoryView = new InventoryView(DashboardView.this);
                inventoryView.setVisible(true);
            }
        });
        leftPanel.add(alertCardStock);

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

    private void updateStockAlert() {
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                List<Product> allProducts = productDAO.getAllProducts();
                return allProducts.stream().filter(Product::isLowStock).count();
            }
            @Override
            protected void done() {
                try {
                    long lowStockCount = get();
                    if (lowStockCount > 0) {
                        alertCardStock.setMessage(String.format(LanguageManager.get("dashboard.alert.msg"), lowStockCount));
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

        // --- 1. NUEVO PEDIDO (ANCHO AUMENTADO Y LAYOUT MEJORADO) ---
        grid.add(createBigButton(LanguageManager.get("dashboard.btn.newOrder"), LanguageManager.get("dashboard.btn.newOrder.sub"), "/images/orders.png", "🛒", e -> {
            ClientManagementDialog selector = new ClientManagementDialog(this, true);
            selector.setVisible(true);
            Client clienteSeleccionado = selector.getSelectedClient();

            if (clienteSeleccionado != null) {
                JDialog frameVentas = new JDialog(this, "SICONI - NUEVO PEDIDO", true);

                // --- MODIFICACIÓN DE TAMAÑO PARA PANTALLA ANCHA ---
                frameVentas.setUndecorated(false);
                frameVentas.setSize(1280, 750); // Ancho aumentado a 1280px para evitar cortes
                frameVentas.setLocationRelativeTo(this);

                ImagePanel background = new ImagePanel("/images/bg3.png");
                background.setLayout(new BorderLayout()); // Asegura que SalesView ocupe todo el espacio
                background.add(new SalesView(clienteSeleccionado), BorderLayout.CENTER);

                frameVentas.setContentPane(background);
                frameVentas.setVisible(true);

                updateStockAlert();
            }
        }));

        // ... (El resto de los botones se mantienen igual) ...
        grid.add(createBigButton(LanguageManager.get("dashboard.btn.inventory"), LanguageManager.get("dashboard.btn.inventory.sub"), "/images/inventory.png", "📦", e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new InventoryView(this).setVisible(true);
            setCursor(Cursor.getDefaultCursor());
            updateStockAlert();
        }));

        grid.add(createBigButton(LanguageManager.get("dashboard.btn.workshop"), LanguageManager.get("dashboard.btn.workshop.sub"), "/images/workshop.png", "✂️", e -> {
            new OrderManagementView(this).setVisible(true);
        }));

        grid.add(createBigButton(LanguageManager.get("dashboard.btn.clients"), LanguageManager.get("dashboard.btn.clients.sub"), "/images/client.png", "👥", e -> {
            new ClientManagementDialog(this, false).setVisible(true);
        }));

        grid.add(createBigButton(LanguageManager.get("dashboard.btn.reports"), LanguageManager.get("dashboard.btn.reports.sub"), "/images/reports.png", "📊", e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new ReportsView(this).setVisible(true);
            setCursor(Cursor.getDefaultCursor());
        }));

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