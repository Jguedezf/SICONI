/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: LuxuryMessage.java
 * VERSIÓN: 9.0 (FINAL FIX - NO DECORATION ERROR)
 * DESCRIPCIÓN: Solución definitiva al error "The dialog is decorated".
 * -----------------------------------------------------------------------------
 */
package com.swimcore.util;

import com.swimcore.view.components.SoftButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class LuxuryMessage extends JDialog {

    private static final Color BG_DARK = new Color(20, 20, 20);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color ERROR_RED = new Color(220, 50, 50);
    private static final Color SUCCESS_GREEN = new Color(57, 255, 20);
    private static final Color TEXT_WHITE = new Color(230, 230, 230);

    private LuxuryMessage(Window parent, String title, String message, boolean isError) {
        // CORRECCIÓN CLAVE: Pasamos el padre directamente al superconstructor
        super(parent, ModalityType.APPLICATION_MODAL);

        // CORRECCIÓN CLAVE: setUndecorated DEBE ir antes de cualquier otra cosa
        try {
            setUndecorated(true);
        } catch (Exception e) {
            // Si falla (muy raro), continuamos con bordes normales
        }

        setBackground(new Color(0,0,0,0));

        Color accentColor = isError ? ERROR_RED : SUCCESS_GREEN;

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, 0, getHeight(), BG_DARK.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 25, 25);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setLayout(new BorderLayout(20, 10));

        // Icono
        JLabel iconLabel = new JLabel();
        if (isError) {
            iconLabel.setText("❌");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            iconLabel.setForeground(ERROR_RED);
        } else {
            ImageIcon icon = loadIcon("logo.png", 50);
            if (icon != null) iconLabel.setIcon(icon);
            else {
                iconLabel.setText("✅");
                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
                iconLabel.setForeground(GOLD);
            }
        }
        iconLabel.setVerticalAlignment(SwingConstants.TOP);

        // Contenido
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(accentColor);

        JTextArea txtMsg = new JTextArea(message);
        txtMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMsg.setForeground(TEXT_WHITE);
        txtMsg.setOpaque(false);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setLineWrap(true);
        txtMsg.setEditable(false);
        txtMsg.setFocusable(false);
        txtMsg.setBorder(null);
        txtMsg.setSize(new Dimension(300, 1));

        centerPanel.add(lblTitle, BorderLayout.NORTH);
        centerPanel.add(txtMsg, BorderLayout.CENTER);

        // Botón
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);

        SoftButton btnOk = new SoftButton(null);
        btnOk.setText("ACEPTAR");
        btnOk.setPreferredSize(new Dimension(120, 40));
        btnOk.setForeground(Color.BLACK);
        btnOk.setBackground(accentColor);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.addActionListener(e -> dispose());
        btnPanel.add(btnOk);

        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        if (getWidth() < 350) setSize(350, getHeight());
        setLocationRelativeTo(parent);
    }

    private ImageIcon loadIcon(String name, int size) {
        try {
            URL url = getClass().getResource("/images/" + name);
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, 4));
        } catch (Exception e) {}
        return null;
    }

    // --- MÉTODOS ESTÁTICOS ---
    public static void show(Component parent, String title, String msg, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            try {
                Window win = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
                new LuxuryMessage(win, title, msg, isError).setVisible(true);
            } catch (Exception e) {
                // Fallback seguro
                JOptionPane.showMessageDialog(parent, msg, title, isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public static void show(String title, String msg, boolean isError) {
        show(null, title, msg, isError);
    }
}