/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: KPI_Card.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.3.0 (Clean Minimalist Design)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Componente Visual Personalizado para la representación de KPIs.
 * - MEJORA: Diseño minimalista centrado, eliminando iconos internos para
 *   una visualización de datos más limpia y directa.
 * - CORRECCIÓN: Ajuste de layout (GridBagLayout) para centrado absoluto de textos.
 *
 * PRINCIPIOS POO:
 * - HERENCIA: Extiende de `JPanel`.
 * - ENCAPSULAMIENTO: Configuración visual protegida y centralizada.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * Widget gráfico para visualizar métricas de alto nivel (KPIs).
 * Dibuja una tarjeta con fondo translúcido y tipografía centrada.
 */
public class KPI_Card extends JPanel {

    private final Color COLOR_CARD_BG = new Color(35, 35, 35, 200);
    private final Color COLOR_BORDER = new Color(220, 0, 115, 150);

    /**
     * Constructor del Widget.
     * ENTRADA: Valor numérico o texto principal (ej. "$500.00") y descripción.
     * PROCESO: Configura el panel con GridBagLayout para centrar el texto.
     * SALIDA: Una tarjeta KPI con texto visible y centrado.
     * @param value Valor numérico o texto principal (ej. "$500.00").
     * @param description Etiqueta descriptiva del indicador (ej. "Ventas del Día").
     */
    public KPI_Card(String value, String description) {
        setOpaque(false);
        setLayout(new GridBagLayout()); // Usamos GridBagLayout para centrado total
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(280, 120));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Centrado horizontal
        gbc.fill = GridBagConstraints.NONE;

        // Etiqueta de Valor
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 42)); // Aumentado a 42 para impacto
        lblValue.setForeground(Color.WHITE);

        // Evita truncamiento en textos largos forzando tamaño mínimo
        lblValue.setMinimumSize(new Dimension(1, 1));

        add(lblValue, gbc);

        // Etiqueta de Descripción
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 0, 0); // Separación vertical

        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDescription.setForeground(new Color(180, 180, 180));

        add(lblDescription, gbc);
    }

    /**
     * Constructor de compatibilidad para llamadas antiguas que incluían iconPath.
     * ENTRADA: iconPath (ignorado), value, description.
     * PROCESO: Llama al constructor principal de dos parámetros.
     * SALIDA: Una tarjeta KPI con texto visible y centrado.
     * @param iconPath Ruta del ícono (será ignorado en esta versión).
     * @param value Valor numérico o texto principal.
     * @param description Etiqueta descriptiva.
     */
    public KPI_Card(String iconPath, String value, String description) {
        this(value, description); // Llama al constructor principal
    }

    /**
     * Motor de Renderizado Personalizado.
     * Dibuja el fondo translúcido y el borde fucsia de la tarjeta.
     * @param g Contexto gráfico base.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(COLOR_CARD_BG);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        g2d.setColor(COLOR_BORDER);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        g2d.dispose();
        super.paintComponent(g); // Delega el pintado de los hijos (textos)
    }
}