/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: AlertCard.java
 * VERSIÓN: 1.0.0 (Initial Release)
 * DESCRIPCIÓN: Componente visual reutilizable para mostrar notificaciones o
 * alertas importantes en el Dashboard principal.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class AlertCard extends JPanel {

    private JLabel lblMessage;
    private boolean hover = false;

    // --- PALETA DE COLORES DE ALERTA ---
    private final Color COLOR_BG_START = new Color(50, 42, 30); // Marrón oscuro
    private final Color COLOR_BG_END = new Color(35, 30, 25);
    private final Color COLOR_BORDER = new Color(255, 193, 7); // Ámbar/Dorado
    private final Color COLOR_BORDER_HOVER = new Color(255, 224, 130); // Ámbar claro

    public AlertCard(String icon, String initialMessage) {
        setOpaque(false);
        setLayout(new GridBagLayout()); // Para centrar perfectamente
        setPreferredSize(new Dimension(300, 60));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;

        // Icono de la alerta
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblIcon.setForeground(COLOR_BORDER);
        add(lblIcon, gbc);

        // Mensaje de la alerta
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Para que el texto ocupe el espacio restante
        lblMessage = new JLabel(initialMessage);
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMessage.setForeground(Color.WHITE);
        add(lblMessage, gbc);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    /**
     * Actualiza el texto que se muestra en la tarjeta de alerta.
     * @param newMessage El nuevo mensaje a mostrar.
     */
    public void setMessage(String newMessage) {
        lblMessage.setText(newMessage);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo con degradado sutil
        GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, getHeight(), COLOR_BG_END);
        g2d.setPaint(gp);
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

        // Borde
        g2d.setColor(hover ? COLOR_BORDER_HOVER : COLOR_BORDER);
        g2d.setStroke(new BasicStroke(hover ? 3f : 2f));
        g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 18, 18));

        g2d.dispose();
        super.paintComponent(g);
    }
}