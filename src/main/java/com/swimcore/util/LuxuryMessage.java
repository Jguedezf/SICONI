/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: LuxuryMessage.java
 * VERSIÓN: 3.0.0 (Elegant Toast Bar)
 * DESCRIPCIÓN: Notificación horizontal compacta, estilo barra moderna.
 * Fondo oscuro, borde dorado, todo en una línea.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import com.swimcore.view.components.SoftButton;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LuxuryMessage extends JDialog {

    public LuxuryMessage(String title, String message, boolean isError) {
        setUndecorated(true);
        // Hacemos la ventana ancha y baja (Estilo Notificación)
        setSize(550, 90);
        setLocationRelativeTo(null);
        setModal(true);

        // Forma redondeada
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

        // Panel Principal con Fondo Oscuro (Simulando 8K dark)
        JPanel mainPanel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado Negro a Gris muy oscuro
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 20), 0, getHeight(), new Color(45, 45, 45));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Borde Dorado dibujado manualmente
                g2.setColor(new Color(212, 175, 55));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 1. ICONO (Izquierda)
        JLabel lblIcon = new JLabel(isError ? "❌" : "✨"); // Puedes cambiar por imagen si prefieres
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        mainPanel.add(lblIcon, BorderLayout.WEST);

        // 2. TEXTO (Centro - Una sola línea)
        JLabel lblMsg = new JLabel(message.toUpperCase());
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMsg.setForeground(new Color(240, 240, 240));
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblMsg, BorderLayout.CENTER);

        // 3. BOTÓN (Derecha - Pequeño y sutil)
        SoftButton btnOk = new SoftButton(null);
        btnOk.setText("OK");
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.setForeground(isError ? Color.RED : new Color(212, 175, 55));
        btnOk.setPreferredSize(new Dimension(80, 40));
        btnOk.addActionListener(e -> dispose());

        // Panel para el botón para que no se estire
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBtn.setOpaque(false);
        pBtn.add(btnOk);

        mainPanel.add(pBtn, BorderLayout.EAST);

        setContentPane(mainPanel);
    }

    public static void show(String title, String message, boolean isError) {
        new LuxuryMessage(title, message, isError).setVisible(true);
    }
}