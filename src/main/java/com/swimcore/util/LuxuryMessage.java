/*
 * -----------------------------------------------------------------------------
 * ARCHIVO: LuxuryMessage.java
 * VERSIÓN: 6.0 (FIX FINAL: Logo Corporativo + Compatibilidad Total)
 * -----------------------------------------------------------------------------
 */
package com.swimcore.util;

import com.swimcore.view.components.SoftButton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class LuxuryMessage extends JDialog {

    private static final Color BG_DARK = new Color(28, 28, 28);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color ERROR_RED = new Color(220, 50, 50);
    private static final Color TEXT_WHITE = new Color(230, 230, 230);

    // Constructor privado
    private LuxuryMessage(Window parent, String title, String message, boolean isError) {
        super(parent, ModalityType.APPLICATION_MODAL);

        // Fondo transparente para que el redondeado se vea suave
        setBackground(new Color(0,0,0,0));

        Color accentColor = isError ? ERROR_RED : GOLD;

        // PANEL PRINCIPAL (DIBUJO MANUAL PARA BORDES SUAVES)
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo oscuro
                g2.setColor(BG_DARK);
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 25, 25);

                // Borde Neón/Dorado fino
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 25, 25);

                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setLayout(new BorderLayout(20, 10));

        // --- ICONO ---
        JLabel iconLabel = new JLabel();

        // LÓGICA DE ICONOS CORREGIDA:
        if (isError) {
            // Error: Usamos Emoji Rojo (Limpio y directo)
            iconLabel.setText("❌");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
            iconLabel.setForeground(ERROR_RED);
        } else {
            // Éxito: Usamos TU LOGO (logo.png)
            ImageIcon icon = loadIcon("logo.png", 60);
            if (icon != null) {
                iconLabel.setIcon(icon);
            } else {
                // Fallback si no encuentra el logo: Check Dorado
                iconLabel.setText("✅");
                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
                iconLabel.setForeground(GOLD);
            }
        }
        iconLabel.setVerticalAlignment(SwingConstants.TOP);

        // --- CONTENIDO ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
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
        txtMsg.setSize(new Dimension(350, 10));
        txtMsg.setBorder(null);

        centerPanel.add(lblTitle, BorderLayout.NORTH);
        centerPanel.add(txtMsg, BorderLayout.CENTER);

        // --- BOTÓN ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);

        SoftButton btnOk = new SoftButton(null);
        btnOk.setText("ACEPTAR");
        btnOk.setPreferredSize(new Dimension(110, 35));
        btnOk.setForeground(Color.BLACK);
        btnOk.setBackground(accentColor);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOk.addActionListener(e -> dispose());

        btnPanel.add(btnOk);

        // ARMADO
        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack(); // Ajuste automático

        int w = Math.max(400, Math.min(getWidth(), 600));
        int h = getHeight();
        setSize(w, h);

        if (parent != null) setLocationRelativeTo(parent);
        else setLocationRelativeTo(null);
    }

    // CARGA DE IMAGEN CORREGIDA (Busca en /images/ directamente)
    private ImageIcon loadIcon(String name, int size) {
        try {
            // CAMBIO CLAVE: Quitamos "icons/" para que busque en la raíz de imagenes
            URL url = getClass().getResource("/images/" + name);
            if (url != null) {
                // Usamos java.awt.Image explícitamente para evitar conflictos
                return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {}
        return null;
    }

    // =================================================================================
    // MÉTODOS ESTÁTICOS (SOBRECARGA PARA COMPATIBILIDAD)
    // =================================================================================

    /** MODO 1: Con padre (ej: this) - Recomendado */
    public static void show(Component parentComponent, String title, String msg, boolean isError) {
        Window window = SwingUtilities.getWindowAncestor(parentComponent);
        if (window == null && parentComponent instanceof Window) {
            window = (Window) parentComponent;
        }
        new LuxuryMessage(window, title, msg, isError).setVisible(true);
    }

    /** MODO 2: Sin padre - ARREGLA ERRORES EN CÓDIGO VIEJO */
    public static void show(String title, String msg, boolean isError) {
        new LuxuryMessage(null, title, msg, isError).setVisible(true);
    }
}