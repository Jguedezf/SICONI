/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: ClientCard.java
 * VERSIÓN: 2.1.0 (Persistent Selection State)
 * DESCRIPCIÓN: Se añade un estado de selección para que el borde se
 * mantenga visible incluso después de mover el mouse.
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

    private final Color COLOR_CARD_BG = new Color(45, 45, 45);
    private final Border BORDER_DEFAULT = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(5, 10, 5, 5)
    );
    private final Border BORDER_HOVER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 0, 115), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 5)
    );
    private final Border BORDER_SELECTED = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(212, 175, 55), 3),
            BorderFactory.createEmptyBorder(5, 10, 5, 5)
    );

    private final boolean isSelected;

    public ClientCard(Client client, boolean isSelected) {
        this.isSelected = isSelected;
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_CARD_BG);
        setPreferredSize(new Dimension(240, 110));

        setBorder(isSelected ? BORDER_SELECTED : BORDER_DEFAULT);

        JLabel iconLabel = new JLabel("👤");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(Color.GRAY);
        add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblAthlete = new JLabel(client.getAthleteName());
        lblAthlete.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAthlete.setForeground(Color.WHITE);

        String clubInfo = (client.getClub() != null && !client.getClub().isEmpty()) ? client.getClub() : "Sin Club";
        JLabel lblClub = new JLabel(clubInfo);
        lblClub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblClub.setForeground(new Color(200, 160, 50));

        JLabel lblRep = new JLabel("Rep: " + client.getFullName());
        lblRep.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblRep.setForeground(Color.GRAY);

        textPanel.add(lblAthlete);
        textPanel.add(lblClub);
        textPanel.add(Box.createVerticalGlue());
        textPanel.add(lblRep);
        add(textPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        JLabel lblCode = new JLabel(client.getCode());
        lblCode.setFont(new Font("Consolas", Font.BOLD, 12));
        lblCode.setForeground(Color.CYAN);

        if (client.isVip()) {
            lblCode.setText("★ " + client.getCode());
            lblCode.setForeground(new Color(255, 215, 0));
        }

        rightPanel.add(lblCode, BorderLayout.NORTH);
        add(rightPanel, BorderLayout.EAST);

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!isSelected) {
                    setBorder(BORDER_HOVER);
                }
            }
            public void mouseExited(MouseEvent e) {
                setBorder(isSelected ? BORDER_SELECTED : BORDER_DEFAULT);
            }
        });
    }
}