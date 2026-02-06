/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: AlertCard.java
 * VERSIÓN: 2.1.0 (Compact & Interactive)
 * DESCRIPCIÓN: Ancho ajustado a 320px para no tapar el logo.
 * Soporta clic para redirigir a productos específicos.
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
    private Runnable actionOnClick;

    // --- PALETA DE COLORES DE ALERTA ---
    private final Color COLOR_BG_START = new Color(50, 42, 30); // Marrón oscuro
    private final Color COLOR_BG_END = new Color(35, 30, 25);
    private final Color COLOR_BORDER = new Color(255, 193, 7); // Ámbar/Dorado
    private final Color COLOR_BORDER_HOVER = new Color(255, 224, 130); // Ámbar claro

    public AlertCard(String icon, String initialMessage, Runnable onClick) {
        this.actionOnClick = onClick;

        setOpaque(false);
        setLayout(new GridBagLayout());

        // CORRECCIÓN: Ancho 320px (Compacto) para respetar el logo de fondo
        setPreferredSize(new Dimension(320, 60));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText("Clic para gestionar stock crítico");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;

        // Icono
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblIcon.setForeground(COLOR_BORDER);
        add(lblIcon, gbc);

        // Mensaje
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Texto adaptable (HTML) con fuente ajustada
        lblMessage = new JLabel("<html>" + initialMessage + "</html>");
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
            @Override
            public void mouseClicked(MouseEvent e) {
                if (actionOnClick != null) actionOnClick.run();
            }
        });
    }

    public void setMessage(String newMessage) {
        lblMessage.setText("<html>" + newMessage + "</html>");
        this.revalidate();
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo degradado
        GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, getHeight(), COLOR_BG_END);
        g2d.setPaint(gp);
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

        // Borde
        g2d.setColor(hover ? COLOR_BORDER_HOVER : COLOR_BORDER);
        g2d.setStroke(new BasicStroke(hover ? 2.5f : 1.5f));
        g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 18, 18));

        g2d.dispose();
        super.paintComponent(g);
    }
}