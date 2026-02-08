/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: InventorySidePanel.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 2.0.1 (Professional Documentation Update)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Componente de interfaz gráfica (GUI) que implementa una barra de herramientas
 * lateral vertical. Actúa como un contenedor especializado que administra
 * la disposición y el comportamiento de los botones de navegación del módulo.
 *
 * PRINCIPIOS POO Y PATRONES:
 * - HERENCIA: Extiende de javax.swing.JPanel.
 * - COMPOSICIÓN: Agrega y gestiona instancias de la clase 'SoftButton'.
 * - FACTORY METHOD (Interno): Centraliza la creación compleja de botones.
 * - OBSERVER: Implementa escuchadores de eventos para la interacción (Mouse/Action).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import com.swimcore.util.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * [VISTA - COMPONENTE] Panel lateral de navegación para módulos del sistema.
 * Implementa un diseño vertical (BoxLayout) y gestiona la estética de los controles.
 */
public class InventorySidePanel extends JPanel {

    // ========================================================================================
    //                                  ATRIBUTOS (CONSTANTES)
    // ========================================================================================

    // [ENCAPSULAMIENTO] Definición inmutable del color de fondo del panel.
    // Garantiza la consistencia visual con el resto del tema oscuro de la aplicación.
    private final Color COLOR_PANEL_BG = new Color(22, 22, 22);

    // ========================================================================================
    //                                  CONSTRUCTOR
    // ========================================================================================

    /**
     * Constructor de la clase.
     * Configura las propiedades del contenedor: Gestor de diseño (Layout Manager),
     * color de fondo, bordes (Padding) y dimensiones preferidas.
     */
    public InventorySidePanel() {
        setBackground(COLOR_PANEL_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 10, 20, 10)); // Margen interno
        setPreferredSize(new Dimension(80, 0)); // Ancho fijo, alto dinámico
    }

    // ========================================================================================
    //                                  MÉTODOS PÚBLICOS (API)
    // ========================================================================================

    /**
     * Método para la adición dinámica de controles al panel.
     * Abstrae la complejidad de la creación del botón, permitiendo al cliente
     * (la Vista principal) agregar funcionalidades con una sola línea de código.
     * * @param iconPath Ruta relativa del recurso gráfico (ícono).
     * @param tooltip Texto de ayuda flotante (UX).
     * @param action Implementación de la interfaz ActionListener (Comportamiento).
     */
    public void addButton(String iconPath, String tooltip, ActionListener action) {
        // Delegación de la creación al método fábrica interno
        SoftButton button = createSoftButton(iconPath, tooltip);

        // Asignación del comportamiento (Controlador)
        button.addActionListener(action);

        // Adición al árbol de componentes visuales
        add(button);
        add(Box.createRigidArea(new Dimension(0, 15))); // Espaciado vertical (Gap)
    }

    // ========================================================================================
    //                                  MÉTODOS PRIVADOS (FACTORY)
    // ========================================================================================

    /**
     * [FACTORY METHOD - INTERNAL]
     * Centraliza la lógica de instanciación y configuración de los objetos 'SoftButton'.
     * Realiza la carga de recursos, el escalado de imágenes y la vinculación
     * con el sistema de sonido (SoundManager).
     * * @param iconPath Ubicación del archivo de imagen.
     * @param tooltip Descripción textual del botón.
     * @return Una instancia configurada de SoftButton lista para ser añadida.
     */
    private SoftButton createSoftButton(String iconPath, String tooltip) {
        ImageIcon icon = null;

        // Bloque Try-Catch para manejo robusto de recursos externos (Imágenes)
        try {
            URL url = getClass().getResource(iconPath);
            if (url != null) {
                // Procesamiento de imagen: Escalado suave (SCALE_SMOOTH) para alta calidad
                Image img = new ImageIcon(url).getImage().getScaledInstance(52, 52, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        } catch (Exception e) {
            // Manejo de error silencioso en consola (Fail-safe)
            System.err.println("Icono no encontrado: " + iconPath);
        }

        // Instanciación del componente personalizado
        SoftButton btn = new SoftButton(icon);
        btn.setToolTipText(tooltip);

        // Restricción de dimensiones para mantener uniformidad en el layout
        btn.setPreferredSize(new Dimension(60, 60));
        btn.setMinimumSize(new Dimension(60, 60));
        btn.setMaximumSize(new Dimension(60, 60));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // [INTEGRACIÓN] Vinculación con el Singleton SoundManager mediante MouseAdapter.
        // Provee feedback auditivo (UX) al presionar o pasar el mouse sobre el botón.
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                SoundManager.getInstance().playClick();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                SoundManager.getInstance().playHover();
            }
        });

        return btn;
    }
}