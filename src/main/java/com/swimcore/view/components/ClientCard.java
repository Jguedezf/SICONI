/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * ARCHIVO: ClientCard.java
 * VERSIÓN: 5.0.0 (Colores por Tipo + Nombre Ajustado)
 * FECHA: Enero 2026
 *
 * DESCRIPCIÓN TÉCNICA:
 * Componente visual personalizado (Widget) que representa la entidad 'Cliente'
 * en las listas de selección. Implementa lógica de renderizado condicional
 * para diferenciar visualmente los tipos de personería (Jurídica/Natural)
 * y estados de selección.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import com.swimcore.model.Client;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * [VISTA - COMPONENTE] Tarjeta gráfica de presentación de datos de Cliente.
 * [POO - HERENCIA] Extiende de JPanel para integrarse en layouts de tipo Grid o Flow.
 * * REQUERIMIENTO: Visualización rápida y distinción de categorías de clientes.
 */
public class ClientCard extends JPanel {

    // ========================================================================================
    //                                  ATRIBUTOS DE ESTADO Y ESTILO
    // ========================================================================================

    // Constante de estilo para el fondo base (Dark Theme).
    private final Color COLOR_CARD_BG = new Color(35, 35, 35);

    // Estado interno que determina si la tarjeta debe renderizarse como "activa" o "seleccionada".
    private final boolean isSelected;

    // ========================================================================================
    //                                  CONSTRUCTOR
    // ========================================================================================

    /**
     * Constructor del componente.
     * Recibe el modelo de datos (Client) y configura la representación visual.
     * @param client Objeto del modelo con la información a mostrar.
     * @param isSelected Booleano que indica el estado de foco del componente.
     */
    public ClientCard(Client client, boolean isSelected) {
        this.isSelected = isSelected;

        // Configuración del Layout (BorderLayout) para distribución espacial:
        // WEST: Icono | CENTER: Datos Principales | EAST: Código
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_CARD_BG);
        setPreferredSize(new Dimension(250, 110));

        // --- LÓGICA DE PRESENTACIÓN (Colorimetría Semántica) ---
        // Se determina el color de acento basándose en el atributo 'IdType' del modelo.
        // J = Jurídico (Dorado), E = Extranjero (Cian), V = Venezolano (Azul).
        Color typeColor;
        String type = client.getIdType() != null ? client.getIdType().toUpperCase() : "V";
        if (type.equals("J")) typeColor = new Color(255, 215, 0); // Dorado
        else if (type.equals("E")) typeColor = new Color(0, 200, 200); // Cian
        else typeColor = new Color(30, 144, 255); // Azul V

        // --- GESTIÓN DE BORDES (Factory Pattern) ---
        // Se utiliza BorderFactory para componer bordes complejos.
        // Si está seleccionado, se aplica un borde dorado grueso; si no, un borde sutil del color del tipo.
        Border defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(typeColor, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 5)
        );
        Border selectedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 3),
                BorderFactory.createEmptyBorder(3, 8, 3, 3)
        );
        setBorder(isSelected ? selectedBorder : defaultBorder);

        // --- COMPONENTE 1: ICONOGRAFÍA (WEST) ---
        // Uso de Emojis Unicode como iconos vectoriales ligeros.
        JLabel iconLabel = new JLabel(type.equals("J") ? "🏢" : "👤");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 38));
        iconLabel.setForeground(typeColor);
        add(iconLabel, BorderLayout.WEST);

        // --- COMPONENTE 2: DATOS TEXTUALES (CENTER) ---
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        // [TRUCO TÉCNICO] Uso de HTML dentro de JLabel para permitir
        // el ajuste de línea automático (Word Wrapping) en nombres largos.
        String htmlName = String.format("<html><div style='width: 125px;'>%s</div></html>", client.getFullName());
        JLabel lblName = new JLabel(htmlName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(Color.WHITE);

        textPanel.add(lblName);

        // Renderizado condicional del Club/Organización
        String club = client.getClub();
        if (club != null && !club.isEmpty() && !club.equalsIgnoreCase("Sin Club")) {
            textPanel.add(Box.createVerticalStrut(5));
            JLabel lblClub = new JLabel(club);
            lblClub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblClub.setForeground(typeColor);
            textPanel.add(lblClub);
        }

        add(textPanel, BorderLayout.CENTER);

        // --- COMPONENTE 3: IDENTIFICADOR (EAST) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        JLabel lblCode = new JLabel(client.getCode());
        lblCode.setFont(new Font("Consolas", Font.BOLD, 12));
        lblCode.setForeground(Color.GRAY);
        rightPanel.add(lblCode, BorderLayout.NORTH);
        add(rightPanel, BorderLayout.EAST);

        // --- INTERACTIVIDAD (OBSERVER PATTERN) ---
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Listener anónimo para efectos de Hover (Feedback visual al pasar el mouse).
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!isSelected) setBackground(new Color(50, 50, 50)); }
            public void mouseExited(MouseEvent e) { if (!isSelected) setBackground(COLOR_CARD_BG); }
        });
    }
}