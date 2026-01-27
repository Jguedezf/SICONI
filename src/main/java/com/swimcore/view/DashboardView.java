/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: DashboardView.java
 * VERSIÓN: 2.5.1 (SalesView Integration)
 * DESCRIPCIÓN: Se integra el flujo completo de Clientes y Pedidos a los
 * botones del menú principal, creando un flujo de trabajo lógico para el taller.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.util.CurrencyManager;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;
import com.swimcore.view.dialogs.CurrencySettingsDialog;
import com.swimcore.view.dialogs.SupplierManagementDialog;
import com.swimcore.view.dialogs.ClientCheckInDialog;
import com.swimcore.model.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Locale;

public class DashboardView extends JFrame {

    private final Color COLOR_BG = new Color(18, 18, 18);
    private final Color COLOR_CARD = new Color(30, 30, 30);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);
    private final Color COLOR_TEXTO = new Color(240, 240, 240);
    private JLabel lblRateValue;

    public DashboardView() {
        setTitle("SICONI - Panel Principal");
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
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("SICONI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createHorizontalGlue());

        JPanel rateWidget = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rateWidget.setOpaque(false);

        lblRateValue = new JLabel(String.format(Locale.US, "TASA BCV: Bs. %.2f", CurrencyManager.getTasa()));
        lblRateValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRateValue.setForeground(Color.LIGHT_GRAY);

        SoftButton btnEditRate = new SoftButton(createIcon("/images/icons/icon_edit.png", 20, 20));
        btnEditRate.setPreferredSize(new Dimension(40, 40));
        btnEditRate.addActionListener(e -> {
            SoundManager.getInstance().playClick();
            new CurrencySettingsDialog(this).setVisible(true);
            lblRateValue.setText(String.format(Locale.US, "TASA BCV: Bs. %.2f", CurrencyManager.getTasa()));
        });

        rateWidget.add(lblRateValue);
        rateWidget.add(btnEditRate);
        headerPanel.add(rateWidget);
        return headerPanel;
    }

    private JPanel createCentralMenu() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(2, 3, 25, 25));
        grid.setOpaque(false);

        // 1. CLIENTES (ACCIÓN ACTUALIZADA)
        grid.add(createBigButton("CLIENTES", "Registro y Atletas", "/images/client.png", e -> {
            new ClientManagementDialog(this).setVisible(true);
        }));

        // 2. INVENTARIO
        grid.add(createBigButton("INVENTARIO", "Insumos y Catálogo", "/images/inventory.png", e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    new InventoryView(DashboardView.this).setVisible(true);
                    return null;
                }
                @Override protected void done() { setCursor(Cursor.getDefaultCursor()); }
            }.execute();
        }));

        // 3. PEDIDOS (LÓGICA DE TALLER)
        grid.add(createBigButton("PEDIDOS", "Ventas y Producción", "/images/orders.png", e -> {
            // 1. Abrir selector de clientes
            ClientCheckInDialog checkIn = new ClientCheckInDialog(this);
            checkIn.setVisible(true);

            Client clienteSeleccionado = checkIn.getSelectedClient();

            // 2. Si se selecciona un cliente, abrir la ventana de pedidos para él
            if (clienteSeleccionado != null) {
                JDialog frameVentas = new JDialog(this, "SICONI - Gestión de Pedidos (" + clienteSeleccionado.getFullName() + ")", true);
                frameVentas.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                frameVentas.setSize(1200, 750);
                frameVentas.setLocationRelativeTo(null);
                frameVentas.setContentPane(new SalesView(clienteSeleccionado));
                frameVentas.setVisible(true);
            }
        }));

        // 4. REPORTES
        grid.add(createBigButton("REPORTES", "Estadísticas", "/images/reports.png", e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    new ReportsView(DashboardView.this).setVisible(true);
                    return null;
                }
                @Override protected void done() { setCursor(Cursor.getDefaultCursor()); }
            }.execute();
        }));

        // 5. PROVEEDORES
        grid.add(createBigButton("PROVEEDORES", "Gestión de Contactos", "/images/settings.png", e -> {
            new SupplierManagementDialog(this).setVisible(true);
        }));

        // 6. SALIR
        JButton btnExit = createBigButton("SALIR", "Cerrar Sesión", "/images/logout.png", null);
        btnExit.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnExit.putClientProperty("hoverColor", new Color(220, 20, 60));
                btnExit.putClientProperty("hover", true);
                btnExit.repaint();
                SoundManager.getInstance().playHover();
            }
            public void mouseExited(MouseEvent e) {
                btnExit.putClientProperty("hoverColor", null);
                btnExit.putClientProperty("hover", false);
                btnExit.repaint();
            }
            public void mousePressed(MouseEvent e) { SoundManager.getInstance().playClick(); }
        });
        btnExit.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "¿Desea cerrar el sistema?", "SICONI", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
        });
        grid.add(btnExit);

        container.add(grid);
        return container;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0,0,10,0));
        JLabel lbl = new JLabel("© 2026 Desarrollado por Johanna Guédez - Ingeniería Informática UNEG");
        lbl.setForeground(Color.BLACK);
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

    private JButton createBigButton(String title, String subtitle, String iconPath, java.awt.event.ActionListener action) {
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
                lblIcon.setText("🔹"); lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 80)); lblIcon.setForeground(COLOR_FUCSIA);
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
            public void mouseEntered(MouseEvent e) {
                btn.putClientProperty("hover", true);
                btn.repaint();
                SoundManager.getInstance().playHover();
            }
            public void mouseExited(MouseEvent e) {
                btn.putClientProperty("hover", false);
                btn.repaint();
            }
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