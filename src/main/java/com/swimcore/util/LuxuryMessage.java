/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * ARCHIVO: LuxuryMessage.java
 * VERSIÓN: 6.1 (FINAL TOUCH: Verde Neón + Tamaño Botón)
 * FECHA: 05 de Febrero de 2026 - 13:15 PM (VENEZUELA)
 * DESCRIPCIÓN: Mensajes "Luxury" rediseñados con SoftButton y fondos
 * personalizados. Ahora con el botón de acción principal en Verde Neón.
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

    private static final Color BG_DARK = new Color(20, 20, 20); // Negro Profundo (Elegante)
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color ERROR_RED = new Color(220, 50, 50);
    private static final Color TEXT_WHITE = new Color(230, 230, 230);

    // Constructor Privado
    private LuxuryMessage(Window parent, String title, String message, boolean isError) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setBackground(new Color(0,0,0,0)); // Para que la ventana sea transparente

        Color accentColor = isError ? ERROR_RED : new Color(57, 255, 20); // ¡VERDE NEÓN!

        // PANEL PRINCIPAL (Bordes redondeados y degradado)
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo oscuro degradado
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, 0, getHeight(), BG_DARK.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 35, 35); // Bordes redondos
                // Borde Dorado (Opciónal)
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 35, 35);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setLayout(new BorderLayout(20, 10));

        // --- Icono (Si hay) ---
        JLabel iconLabel = new JLabel();
        if (isError) {
            iconLabel.setText("❌");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
            iconLabel.setForeground(ERROR_RED);
        } else {
            ImageIcon icon = loadIcon("logo.png", 60);
            if (icon != null) {
                iconLabel.setIcon(icon);
            } else {
                // Si no encuentra el logo
                iconLabel.setText("✅");
                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
                iconLabel.setForeground(GOLD);
            }
        }
        iconLabel.setVerticalAlignment(SwingConstants.TOP);

        // --- CONTENIDO DEL MENSAJE ---
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
        txtMsg.setBorder(null);
        centerPanel.add(lblTitle, BorderLayout.NORTH);
        centerPanel.add(txtMsg, BorderLayout.CENTER);

        // --- BOTÓN (Con SoftButton y el color "PRO") ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);

        SoftButton btnOk = new SoftButton(null);
        btnOk.setText("ACEPTAR");
        btnOk.setPreferredSize(new Dimension(150, 50)); // ¡BOTÓN MÁS GRANDE!
        btnOk.setForeground(Color.BLACK); // Texto negro
        btnOk.setBackground(accentColor);  // ¡Verde Neón!
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOk.addActionListener(e -> dispose());
        btnPanel.add(btnOk);

        // --- ENSAMBLAJE (Orden correcto) ---
        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack(); // Ajusta el tamaño automáticamente
        if (parent != null) setLocationRelativeTo(parent);
        else setLocationRelativeTo(null);
    }

    // CARGA DE IMAGEN (Busca en /images/)
    private ImageIcon loadIcon(String name, int size) {
        try {
            URL url = getClass().getResource("/images/" + name);
            if (url != null) {
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