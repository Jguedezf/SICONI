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
 * Componente Visual Personalizado (Custom Widget) para la representación de
 * Indicadores Clave de Desempeño (KPIs). Implementa un diseño minimalista
 * centrado utilizando GridBagLayout para garantizar la adaptabilidad.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * [VISTA - COMPONENTE] Clase que define un widget gráfico reutilizable.
 * [POO - HERENCIA] Extiende de javax.swing.JPanel para heredar las propiedades
 * de un contenedor ligero, permitiendo su integración en cualquier Layout Manager.
 * * REQUERIMIENTO NO FUNCIONAL: Interfaz Gráfica de Usuario (GUI) intuitiva y estética.
 */
public class KPI_Card extends JPanel {

    // ========================================================================================
    //                                  ATRIBUTOS (ENCAPSULAMIENTO)
    // ========================================================================================

    // [CONSTANTES DE ESTILO] Atributos inmutables (final) y privados para definir
    // la identidad visual del componente, asegurando consistencia en toda la aplicación.
    private final Color COLOR_CARD_BG = new Color(35, 35, 35, 200);
    private final Color COLOR_BORDER = new Color(220, 0, 115, 150);

    // ========================================================================================
    //                                  CONSTRUCTORES
    // ========================================================================================

    /**
     * [CONSTRUCTOR PRINCIPAL]
     * Inicializa el componente, configura el gestor de distribución (Layout Manager)
     * y compone los elementos internos (JLabels) para la visualización de datos.
     * * @param value String que representa el valor métrico principal (ej. "$500.00").
     * @param description String que describe el indicador (ej. "Ventas del Día").
     */
    public KPI_Card(String value, String description) {
        setOpaque(false); // Permite que el método paintComponent maneje la transparencia.

        // [LAYOUT MANAGER] Se utiliza GridBagLayout para un control preciso del centrado
        // vertical y horizontal de los elementos de texto.
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(280, 120));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Alineación central absoluta
        gbc.fill = GridBagConstraints.NONE;

        // Composición: Etiqueta de Valor
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 42)); // Tipografía de alto impacto
        lblValue.setForeground(Color.WHITE);

        // Prevención de colapso de layout en textos vacíos
        lblValue.setMinimumSize(new Dimension(1, 1));

        add(lblValue, gbc);

        // Composición: Etiqueta de Descripción
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 0, 0); // Margen superior (Padding)

        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDescription.setForeground(new Color(180, 180, 180));

        add(lblDescription, gbc);
    }

    /**
     * [POO - SOBRECARGA] Constructor secundario para compatibilidad (Legacy Support).
     * Permite instanciar la clase utilizando una firma antigua que incluía un ícono,
     * redirigiendo la lógica al constructor principal mediante `this()`.
     * * @param iconPath Ruta del recurso gráfico (Deprecado/Ignorado en esta versión).
     * @param value Valor métrico.
     * @param description Descripción del indicador.
     */
    public KPI_Card(String iconPath, String value, String description) {
        this(value, description); // Delegación al constructor principal
    }

    // ========================================================================================
    //                                  RENDERIZADO GRÁFICO
    // ========================================================================================

    /**
     * [POO - POLIMORFISMO] Sobreescritura del método paintComponent.
     * Utiliza la API Java 2D (Graphics2D) para dibujar vectores personalizados,
     * aplicando antialiasing para suavizar bordes y transparencias (Alpha Channel).
     * * @param g Contexto gráfico proporcionado por el sistema de ventanas.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Activación de suavizado de bordes (Rendering Hints)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibujado del fondo con esquinas redondeadas
        g2d.setColor(COLOR_CARD_BG);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        // Dibujado del borde estético
        g2d.setColor(COLOR_BORDER);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        g2d.dispose(); // Liberación de recursos gráficos
        super.paintComponent(g); // Delegación para el pintado de componentes hijos (JLabels)
    }
}