/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * ARCHIVO: AlertCard.java
 * VERSIÓN: 2.1.0 (Compact & Interactive)
 * FECHA: 06 de Febrero de 2026
 * HORA: 11:45 PM (Hora de Venezuela)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Componente visual dinámico (Custom Widget) diseñado para la notificación
 * de eventos críticos en tiempo real (ej. Stock bajo).
 * * MEJORAS DE INGENIERÍA:
 * 1. DIMENSIONAMIENTO: Optimización del ancho a 320px para garantizar la
 * visibilidad del branding corporativo en el fondo de la aplicación.
 * 2. INTERACTIVIDAD: Implementación de callbacks para la redirección funcional
 * mediante eventos de usuario.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * [VISTA - COMPONENTE] Tarjeta de alerta interactiva.
 * [POO - HERENCIA] Extiende de JPanel para la integración en contenedores Swing.
 * [PATRÓN ESTRATEGIA] Utiliza un objeto Runnable (actionOnClick) para definir
 * comportamientos dinámicos al ser activado por el usuario.
 */
public class AlertCard extends JPanel {

    // ========================================================================================
    //                                  ATRIBUTOS DE ESTADO Y UI
    // ========================================================================================

    // Componente interno para la renderización de texto con soporte HTML.
    private JLabel lblMessage;

    // Flag de estado para el control del feedback visual (Hover effect).
    private boolean hover = false;

    // Interfaz funcional para el manejo de acciones asíncronas post-clic.
    private Runnable actionOnClick;

    // --- PALETA SEMÁNTICA DE ALERTA (DESIGN SYSTEM) ---
    // Definición de colores institucionales para estados de advertencia.
    private final Color COLOR_BG_START = new Color(50, 42, 30); // Base Marrón/Ámbar
    private final Color COLOR_BG_END = new Color(35, 30, 25);
    private final Color COLOR_BORDER = new Color(255, 193, 7); // Ámbar/Dorado sólido
    private final Color COLOR_BORDER_HOVER = new Color(255, 224, 130); // Highlight de interacción

    // ========================================================================================
    //                                  CONSTRUCTOR
    // ========================================================================================

    /**
     * Inicializa el widget de alerta con configuración de layout y eventos.
     * @param icon Símbolo o emoji representativo de la alerta.
     * @param initialMessage Contenido textual de la notificación.
     * @param onClick Procedimiento a ejecutar al detectar el evento de clic.
     */
    public AlertCard(String icon, String initialMessage, Runnable onClick) {
        this.actionOnClick = onClick;

        // Configuración de transparencia para renderizado de bordes redondeados.
        setOpaque(false);
        setLayout(new GridBagLayout());

        // [REQUERIMIENTO DE DISEÑO] Dimensionamiento compacto (320x60)
        setPreferredSize(new Dimension(320, 60));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText("Clic para gestionar stock crítico");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;

        // Renderizado del Icono (Unicode/Emoji)
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblIcon.setForeground(COLOR_BORDER);
        add(lblIcon, gbc);

        // Renderizado del Mensaje
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Uso de renderizado HTML para soportar ajuste de texto multilínea automático.
        lblMessage = new JLabel("<html>" + initialMessage + "</html>");
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMessage.setForeground(Color.WHITE);
        add(lblMessage, gbc);

        // [PATRÓN OBSERVER] Suscripción a eventos de entrada y acción del mouse.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                // Ejecución del callback definido en el controlador o vista padre.
                if (actionOnClick != null) actionOnClick.run();
            }
        });
    }

    /**
     * Actualiza el contenido de la alerta dinámicamente.
     * @param newMessage Nuevo texto a mostrar.
     */
    public void setMessage(String newMessage) {
        lblMessage.setText("<html>" + newMessage + "</html>");
        this.revalidate();
        this.repaint();
    }

    // ========================================================================================
    //                                  MOTOR DE DIBUJO (Graphics2D)
    // ========================================================================================

    /**
     * Sobreescritura del ciclo de pintado para aplicar estilos vectoriales avanzados.
     * Implementa un fondo degradado (GradientPaint) y trazos suaves (Antialiasing).
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Optimización de calidad gráfica.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Dibujado de Fondo con Degradado Vertical.
        GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, getHeight(), COLOR_BG_END);
        g2d.setPaint(gp);
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

        // 2. Dibujado de Borde Dinámico (Feedback visual).
        g2d.setColor(hover ? COLOR_BORDER_HOVER : COLOR_BORDER);
        g2d.setStroke(new BasicStroke(hover ? 2.5f : 1.5f));
        g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 18, 18));

        g2d.dispose();
        super.paintComponent(g); // Delega el pintado de componentes hijos (Labels).
    }
}