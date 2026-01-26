package com.swimcore.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * Componente personalizado para textos de Títulos con efecto Dorado Luxury.
 * Usa degradados (GradientPaint) y sombras para un acabado premium.
 */
public class GoldenTitle extends JLabel {

    // Paleta de Oro SICONI
    private final Color GOLD_LIGHT = new Color(255, 223, 0);   // Oro Brillante
    private final Color GOLD_DARK  = new Color(184, 134, 11);  // Oro Viejo
    private final int shadowDistance = 2; // Distancia de la sombra negra

    public GoldenTitle(String text, int size) {
        super(text);
        // Usamos una fuente con serifa o gruesa para que se note el degradado
        // Si tienes una fuente custom, cárgala aquí. Si no, Serif o SansSerif BOLD sirven.
        setFont(new Font("Serif", Font.BOLD, size));
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // 1. Activar Anti-aliasing (Suavizado de bordes) para que no se vea pixelado
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String text = getText();
        if (text == null) return;

        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2; // Centrado horizontal
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent(); // Centrado vertical

        // 2. DIBUJAR SOMBRA NEGRA (Para que se lea sobre cualquier fondo)
        g2.setColor(new Color(0, 0, 0, 180)); // Negro con transparencia
        g2.drawString(text, x + shadowDistance, y + shadowDistance);

        // 3. DIBUJAR EL TEXTO DORADO (DEGRADADO)
        // Crea un degradado vertical que va de oro claro a oro oscuro
        GradientPaint goldGradient = new GradientPaint(
                0, y - fm.getAscent(), GOLD_LIGHT,
                0, y, GOLD_DARK
        );

        g2.setPaint(goldGradient);
        g2.drawString(text, x, y);

        g2.dispose();
    }
}