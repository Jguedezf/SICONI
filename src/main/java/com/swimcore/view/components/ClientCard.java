/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ClientCard.java
 * VERSIÓN: 5.0.0 (Colores por Tipo + Nombre Ajustado)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import com.swimcore.model.Client;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClientCard extends JPanel {

    private final Color COLOR_CARD_BG = new Color(35, 35, 35);
    private final boolean isSelected;

    public ClientCard(Client client, boolean isSelected) {
        this.isSelected = isSelected;
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_CARD_BG);
        setPreferredSize(new Dimension(250, 110));

        // COLORES SEGÚN TIPO
        Color typeColor;
        String type = client.getIdType() != null ? client.getIdType().toUpperCase() : "V";
        if (type.equals("J")) typeColor = new Color(255, 215, 0); // Dorado
        else if (type.equals("E")) typeColor = new Color(0, 200, 200); // Cian
        else typeColor = new Color(30, 144, 255); // Azul V

        // BORDES
        Border defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(typeColor, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 5)
        );
        Border selectedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 3),
                BorderFactory.createEmptyBorder(3, 8, 3, 3)
        );
        setBorder(isSelected ? selectedBorder : defaultBorder);

        // ICONO
        JLabel iconLabel = new JLabel(type.equals("J") ? "🏢" : "👤");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 38));
        iconLabel.setForeground(typeColor);
        add(iconLabel, BorderLayout.WEST);

        // TEXTO
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        // NOMBRE CON HTML WRAP
        String htmlName = String.format("<html><div style='width: 125px;'>%s</div></html>", client.getFullName());
        JLabel lblName = new JLabel(htmlName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(Color.WHITE);

        textPanel.add(lblName);

        // CLUB (Solo si existe)
        String club = client.getClub();
        if (club != null && !club.isEmpty() && !club.equalsIgnoreCase("Sin Club")) {
            textPanel.add(Box.createVerticalStrut(5));
            JLabel lblClub = new JLabel(club);
            lblClub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblClub.setForeground(typeColor);
            textPanel.add(lblClub);
        }

        add(textPanel, BorderLayout.CENTER);

        // CODIGO
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        JLabel lblCode = new JLabel(client.getCode());
        lblCode.setFont(new Font("Consolas", Font.BOLD, 12));
        lblCode.setForeground(Color.GRAY);
        rightPanel.add(lblCode, BorderLayout.NORTH);
        add(rightPanel, BorderLayout.EAST);

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!isSelected) setBackground(new Color(50, 50, 50)); }
            public void mouseExited(MouseEvent e) { if (!isSelected) setBackground(COLOR_CARD_BG); }
        });
    }
}