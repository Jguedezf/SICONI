/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: SoftButton.java
 * DESCRIPCIÓN: Botón con renderizado de Texto corregido.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class SoftButton extends JButton {

    private boolean hover = false;
    private boolean pressed = false;
    private final Color COLOR_BASE = new Color(35, 35, 35);
    private final Color COLOR_HOVER_GLOW = new Color(220, 0, 115);

    public SoftButton(Icon icon) {
        super(icon);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setForeground(Color.WHITE); // Texto blanco por defecto

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
            public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 20;

        Shape shape = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);

        // Fondo
        if (pressed) {
            g2.setColor(COLOR_BASE.darker());
            g2.fill(shape);
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, arc, arc));
        } else {
            GradientPaint gp = new GradientPaint(0, 0, COLOR_BASE.brighter(), 0, h, COLOR_BASE.darker());
            g2.setPaint(gp);
            g2.fill(shape);
        }

        // Borde Neón
        if (hover) {
            g2.setColor(COLOR_HOVER_GLOW);
            g2.setStroke(new BasicStroke(2));
            g2.draw(shape);
        } else {
            g2.setColor(COLOR_BASE.brighter());
            g2.setStroke(new BasicStroke(1));
            g2.draw(shape);
        }

        // --- CORRECCIÓN: PINTAR EL TEXTO MANUALMENTE ---
        // Al sobreescribir paintComponent con setContentAreaFilled(false),
        // a veces Swing no pinta el texto si el fondo es oscuro. Lo forzamos aquí.

        // 1. Pintar Icono (si existe)
        Icon icon = getIcon();
        int iconWidth = 0;
        if (icon != null) {
            iconWidth = icon.getIconWidth();
            // Si hay texto, movemos el icono a la izquierda
            int x = (getText() != null && !getText().isEmpty()) ? 10 : (w - iconWidth) / 2;
            int y = (h - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, x, y);
        }

        // 2. Pintar Texto
        String text = getText();
        if (text != null && !text.isEmpty()) {
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(text);
            int textH = fm.getAscent();

            // Centrado (ajustado si hay icono)
            int x = (w - textW) / 2;
            if (icon != null) x += (iconWidth / 2) + 5;

            int y = (h + textH) / 2 - 2; // Ajuste fino vertical
            g2.drawString(text, x, y);
        }

        g2.dispose();
    }
}