/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: SoftButton.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.1.0 (Realistic 3D Press Effect)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Componente de botón personalizado con renderizado avanzado.
 * - MEJORA: El efecto de hundimiento ahora usa sombras y luces internas para un
 *   realismo 3D superior, evitando el desplazamiento del ícono.
 *
 * PRINCIPIOS POO APLICADOS:
 * - HERENCIA: Extiende de JButton para heredar su comportamiento y listeners.
 * - POLIMORFISMO: Sobreescribe 'paintComponent' para tomar control total del dibujado.
 * - ENCAPSULAMIENTO: Atributos de estado (hover, pressed) son privados y gestionados internamente.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Botón "Soft Touch" con renderizado 3D y efectos de interacción mejorados.
 * Dibuja su propia apariencia ignorando el Look & Feel del sistema operativo.
 */
public class SoftButton extends JButton {

    // --- ATRIBUTOS (Estado Interno) ---
    private boolean hover = false;
    private boolean pressed = false;

    // --- CONSTANTES DE ESTILO (Paleta Corporativa) ---
    private final Color COLOR_BASE = new Color(35, 35, 35);
    private final Color COLOR_HOVER_GLOW = new Color(220, 0, 115); // Fucsia SICONI

    /**
     * Constructor.
     * ENTRADA: Un objeto Icon que se mostrará en el centro del botón.
     * PROCESO: Inicializa el botón sin la apariencia estándar de Swing y
     *          asigna los listeners para detectar los estados del mouse.
     * SALIDA: Una instancia del botón lista para ser agregada a un panel.
     */
    public SoftButton(Icon icon) {
        super(icon); // Llama al constructor de la superclase (JButton)

        // Configuración para permitir el pintado personalizado
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Asignación de listeners para controlar los estados visuales
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint(); // Solicita un redibujado para mostrar el efecto hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint(); // Solicita un redibujado para quitar el efecto hover
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint(); // Solicita un redibujado para el efecto de hundimiento
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint(); // Solicita un redibujado para restaurar el botón
            }
        });
    }

    /**
     * Motor de Renderizado Personalizado (Sobrescritura Polimórfica).
     * ENTRADA: El contexto gráfico (Graphics g) proporcionado por Swing.
     * PROCESO: Dibuja la forma redondeada, el gradiente, el borde de neón (si está en hover)
     *          y el efecto de sombra/luz (si está presionado), y finalmente el ícono.
     * SALIDA: Representación visual del botón en la pantalla.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Activación de antialiasing para bordes suaves
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int arc = 20; // Nivel de redondez de las esquinas

        Shape shape = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);

        // --- VALIDACIÓN DE ESTADO: PRESIONADO ---
        if (pressed) {
            // Fondo ligeramente más oscuro para simular profundidad
            g2.setColor(COLOR_BASE.darker());
            g2.fill(shape);

            // Sombra interna en la parte superior para crear el efecto 3D
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new RoundRectangle2D.Float(2, 2, width - 4, height - 4, arc, arc));
        } else {
            // Estado Normal: Efecto "Acolchado" con Gradiente
            GradientPaint gp = new GradientPaint(0, 0, COLOR_BASE.brighter(), 0, height, COLOR_BASE.darker());
            g2.setPaint(gp);
            g2.fill(shape);
        }

        // --- VALIDACIÓN DE ESTADO: HOVER ---
        if (hover) {
            // Dibuja un borde de neón fucsia si el mouse está encima
            g2.setColor(COLOR_HOVER_GLOW);
            g2.setStroke(new BasicStroke(2));
            g2.draw(shape);
        } else {
            // Dibuja un borde sutil en estado normal
            g2.setColor(COLOR_BASE.brighter());
            g2.setStroke(new BasicStroke(1));
            g2.draw(shape);
        }

        // El ícono se dibuja al final, centrado. El efecto 3D lo da el fondo.
        Icon icon = getIcon();
        if (icon != null) {
            int x = (width - icon.getIconWidth()) / 2;
            int y = (height - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, x, y);
        }

        g2.dispose(); // Liberación de recursos gráficos
    }
}