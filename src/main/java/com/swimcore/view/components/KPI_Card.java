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
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Componente Visual Personalizado (Custom Swing Component) diseñado para la representación
 * de Indicadores Clave de Desempeño (KPIs).
 *
 * Características de Ingeniería de UI:
 * 1. Renderizado Vectorial Avanzado: Sobreescritura de `paintComponent` para dibujar
 * geometrías no rectangulares (bordes redondeados) y manejar el canal Alpha (transparencias).
 * 2. Composición de Layouts: Combinación de `BorderLayout` y `BoxLayout` para lograr
 * una alineación responsiva de ícono y texto.
 * 3. Reusabilidad: Diseñado como un "Widget" agnóstico que puede ser instanciado múltiples
 * veces con diferentes datos sin duplicar lógica de presentación.
 *
 * PRINCIPIOS POO:
 * - HERENCIA: Extiende de `JPanel` para integrarse en el árbol de componentes de Swing.
 * - ENCAPSULAMIENTO: Configuración visual (colores, fuentes) protegida y centralizada.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Widget gráfico para visualizar métricas de alto nivel (KPIs).
 * Dibuja una tarjeta con fondo translúcido, ícono vectorial y tipografía escalada.
 */
public class KPI_Card extends JPanel {

    // --- CONSTANTES VISUALES (PALETA) ---
    // Uso de colores RGBA (Red, Green, Blue, Alpha) para efectos de translucidez.
    // Alpha 200/255 permite ver sutilmente el fondo del dashboard detrás de la tarjeta.
    private final Color COLOR_CARD_BG = new Color(35, 35, 35, 200);
    private final Color COLOR_BORDER = new Color(220, 0, 115, 150); // Borde Fucsia sutil

    /**
     * Constructor del Widget.
     * @param iconPath Ruta relativa del recurso gráfico (ícono).
     * @param value Valor numérico o texto principal (ej. "$500.00").
     * @param description Etiqueta descriptiva del indicador (ej. "Ventas del Día").
     */
    public KPI_Card(String iconPath, String value, String description) {
        // Configuración base del contenedor
        setOpaque(false); // Desactiva el pintado opaco estándar para permitir bordes redondeados
        setLayout(new BorderLayout(15, 0)); // Gutter (espacio) horizontal de 15px
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding interno
        setPreferredSize(new Dimension(280, 120)); // Tamaño sugerido

        // 1. SECCIÓN VISUAL (ICONO) - ALINEACIÓN IZQUIERDA (WEST)
        try {
            URL url = getClass().getResource(iconPath);
            if(url != null) {
                ImageIcon icon = new ImageIcon(url);
                // Escalado de alta calidad (Smooth) para evitar pixelado
                Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                JLabel lblIcon = new JLabel(new ImageIcon(img));
                add(lblIcon, BorderLayout.WEST);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log de error en caso de fallo de recurso
        }

        // 2. SECCIÓN INFORMATIVA (TEXTO) - ALINEACIÓN CENTRAL
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false); // Transparente para heredar el fondo de la tarjeta
        // BoxLayout vertical (Y_AXIS) para apilar Valor sobre Descripción
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        // Etiqueta de Valor (Tipografía Grande)
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblValue.setForeground(Color.WHITE);

        // Etiqueta de Descripción (Tipografía Secundaria)
        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDescription.setForeground(new Color(180, 180, 180)); // Gris claro

        textPanel.add(lblValue);
        textPanel.add(lblDescription);

        add(textPanel, BorderLayout.CENTER);
    }

    /**
     * Motor de Renderizado Personalizado (Custom Painting).
     * Este método es invocado automáticamente por el sistema de ventanas de Java (EDT)
     * cada vez que el componente necesita ser redibujado.
     * * @param g Contexto gráfico base.
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Casteo a Graphics2D para acceder a primitivas avanzadas de dibujo
        Graphics2D g2d = (Graphics2D) g.create();

        // Activación de Antialiasing (Suavizado de bordes) para eliminar el efecto "dientes de sierra"
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. DIBUJO DEL FONDO
        g2d.setColor(COLOR_CARD_BG);
        // Relleno de rectángulo con esquinas redondeadas (Radio 20px)
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        // 2. DIBUJO DEL BORDE
        g2d.setColor(COLOR_BORDER);
        g2d.setStroke(new BasicStroke(2)); // Grosor de línea de 2px
        // Dibujo del contorno (ajustado -1px para evitar recorte visual)
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        g2d.dispose(); // Liberación de recursos gráficos del sistema operativo
        super.paintComponent(g); // Delegación para pintar los hijos (Labels) encima del fondo
    }
}