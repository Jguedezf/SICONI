/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ClientCard.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 2.0.0 (Data Model Update)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Micro-componente de la Capa de Vista para representar una entidad 'Cliente'.
 * - ACTUALIZACIÓN v2.0.0: Renderiza los nuevos atributos del modelo:
 * Nombre del Atleta, Club y Nombre del Representante.
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

    // Estilos Dark Mode
    private final Color COLOR_CARD_BG = new Color(45, 45, 45);
    private final Border BORDER_DEFAULT = BorderFactory.createLineBorder(new Color(80, 80, 80));
    private final Border BORDER_HOVER = BorderFactory.createLineBorder(new Color(220, 0, 115), 2);

    public ClientCard(Client client) {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_CARD_BG);
        setBorder(BORDER_DEFAULT);
        setPreferredSize(new Dimension(220, 100));

        // 1. SECCIÓN DE INFORMACIÓN TEXTUAL (CENTRO)
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // -- CORRECCIÓN DE MÉTODOS --

        // Atleta (Jerarquía Alta)
        // Usamos getAthleteName() en lugar de getName()
        JLabel lblAthlete = new JLabel(client.getAthleteName());
        lblAthlete.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAthlete.setForeground(Color.WHITE);

        // Club y Categoría
        // Usamos getClub() en lugar de getType()
        String clubInfo = (client.getClub() != null ? client.getClub() : "Sin Club");
        JLabel lblClub = new JLabel(clubInfo);
        lblClub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblClub.setForeground(new Color(200, 160, 50)); // Dorado

        // Representante (Pie)
        JLabel lblRep = new JLabel("Rep: " + client.getFullName());
        lblRep.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblRep.setForeground(Color.GRAY);

        textPanel.add(lblAthlete);
        textPanel.add(lblClub);
        textPanel.add(Box.createVerticalGlue());
        textPanel.add(lblRep);

        add(textPanel, BorderLayout.CENTER);

        // 2. CÓDIGO ID (DERECHA)
        JLabel lblCode = new JLabel(client.getCode());
        lblCode.setFont(new Font("Consolas", Font.BOLD, 12));
        lblCode.setForeground(Color.CYAN);
        lblCode.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));

        // Si es VIP, le ponemos una estrellita
        if (client.isVip()) {
            lblCode.setText("★ " + client.getCode());
            lblCode.setForeground(new Color(255, 215, 0));
        }

        add(lblCode, BorderLayout.EAST);

        // 3. INTERACTIVIDAD
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                setBorder(BORDER_HOVER);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e) {
                setBorder(BORDER_DEFAULT);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
}