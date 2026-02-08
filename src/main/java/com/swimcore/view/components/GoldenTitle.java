/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: GoldenTitle.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.1.0 (Custom UI Component - Luxury Theme)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Componente gráfico personalizado (Custom Widget) para la renderización de títulos.
 * Implementa la API Java 2D para aplicar efectos avanzados de degradado metálico
 * (GradientPaint) y proyección de sombras (Drop Shadow), cumpliendo con la
 * identidad visual corporativa del sistema SICONI.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * [VISTA - COMPONENTE] Clase especializada para la visualización de textos destacados.
 * [POO - HERENCIA] Extiende de javax.swing.JLabel para aprovechar la gestión de texto base,
 * pero altera su comportamiento visual mediante Polimorfismo (Override de paintComponent).
 */
public class GoldenTitle extends JLabel {

    // ========================================================================================
    //                                  ATRIBUTOS (CONSTANTES DE ESTILO)
    // ========================================================================================

    // [ENCAPSULAMIENTO] Definición de la paleta cromática para el efecto de oro.
    // Se definen como constantes inmutables (final) para garantizar la consistencia visual.
    private final Color GOLD_LIGHT = new Color(255, 223, 0);   // Tono: Oro Brillante (Highlighter)
    private final Color GOLD_DARK  = new Color(184, 134, 11);  // Tono: Oro Viejo (Shadow)

    // Parámetro de configuración para el desplazamiento de la sombra (Profundidad).
    private final int shadowDistance = 2;

    // ========================================================================================
    //                                  CONSTRUCTOR
    // ========================================================================================

    /**
     * Constructor del componente.
     * Inicializa las propiedades tipográficas y de alineación del componente Swing base.
     * @param text Cadena de caracteres a mostrar como título.
     * @param size Tamaño de la fuente (puntos) para determinar la jerarquía visual.
     */
    public GoldenTitle(String text, int size) {
        super(text);
        // Configuración de la fuente base. Se utiliza una familia con serifa (Serif)
        // para acentuar el efecto clásico/elegante del degradado.
        setFont(new Font("Serif", Font.BOLD, size));
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    // ========================================================================================
    //                                  MOTOR DE RENDERIZADO (Graphics2D)
    // ========================================================================================

    /**
     * [POO - POLIMORFISMO] Sobreescritura del método de pintado del componente.
     * Intercepta el ciclo de dibujo de Swing para inyectar lógica de renderizado personalizada
     * utilizando la API Graphics2D.
     * * ALGORITMO DE RENDERIZADO:
     * 1. Activación de Anti-aliasing para suavizado de bordes.
     * 2. Cálculo de métricas de fuente para centrado preciso.
     * 3. Renderizado de capa inferior (Sombra).
     * 4. Renderizado de capa superior (Texto con Degradado).
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Conversión del contexto gráfico a Graphics2D para acceso a funciones avanzadas.
        Graphics2D g2 = (Graphics2D) g.create();

        // 1. OPTIMIZACIÓN DE RENDERIZADO
        // Se activan las "Rendering Hints" para maximizar la calidad visual del texto y las formas.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String text = getText();
        if (text == null) return; // Validación de nulidad (Fail-safe)

        // CÁLCULO DE GEOMETRÍA
        // Se obtienen las métricas de la fuente actual para calcular las coordenadas (x, y)
        // exactas para centrar el texto en el área disponible del componente.
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2; // Centrado horizontal
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent(); // Centrado vertical (Baseline)

        // 2. CAPA DE SOMBRA (DROP SHADOW)
        // Se dibuja el texto en color negro con canal alfa (transparencia) desplazado
        // por 'shadowDistance' para generar contraste y legibilidad sobre fondos complejos.
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(text, x + shadowDistance, y + shadowDistance);

        // 3. CAPA DE TEXTO PRINCIPAL (EFECTO ORO)
        // Se crea un objeto GradientPaint que interpola verticalmente entre los colores
        // GOLD_LIGHT y GOLD_DARK, simulando el reflejo metálico sobre la superficie de la letra.
        GradientPaint goldGradient = new GradientPaint(
                0, y - fm.getAscent(), GOLD_LIGHT, // Punto inicial (Tope de la letra)
                0, y, GOLD_DARK                    // Punto final (Base de la letra)
        );

        g2.setPaint(goldGradient); // Aplicación del shader de degradado
        g2.drawString(text, x, y); // Dibujado final del texto

        g2.dispose(); // Liberación de recursos del contexto gráfico
    }
}