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
 * VERSIÓN: 2.0.0 (SoftButton Integration)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Componente de UI reutilizable que implementa una barra de herramientas vertical.
 * - MEJORA: Reemplazo de JButton estándar por el componente personalizado 'SoftButton'
 *   para lograr un efecto 3D acolchado y una mejor experiencia de usuario.
 *
 * PRINCIPIOS POO APLICADOS:
 * - HERENCIA: Extiende de JPanel.
 * - COMPOSICIÓN: Contiene una colección de 'SoftButton'.
 * - ABSTRACCIÓN: Simplifica la creación de una barra de herramientas compleja.
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
 * Panel lateral de navegación para el módulo de inventario.
 * Utiliza botones 'SoftButton' personalizados para una interfaz hiperrealista.
 */
public class InventorySidePanel extends JPanel {

    // --- PALETA DE COLORES (LUXURY) ---
    private final Color COLOR_PANEL_BG = new Color(22, 22, 22);

    /**
     * Constructor.
     * Construye la barra lateral.
     */
    public InventorySidePanel() {
        setBackground(COLOR_PANEL_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 10, 20, 10)); // Padding
        setPreferredSize(new Dimension(80, 0)); // Ancho fijo
    }

    /**
     * Agrega un nuevo SoftButton a la barra lateral.
     * @param iconPath Ruta del ícono 3D.
     * @param tooltip Texto descriptivo.
     * @param action Acción a ejecutar.
     */
    public void addButton(String iconPath, String tooltip, ActionListener action) {
        SoftButton button = createSoftButton(iconPath, tooltip);
        button.addActionListener(action);
        add(button);
        add(Box.createRigidArea(new Dimension(0, 15))); // Espacio vertical
    }

    /**
     * Fábrica de Botones Personalizados.
     * Crea una instancia de SoftButton y le asigna el ícono escalado.
     */
    private SoftButton createSoftButton(String iconPath, String tooltip) {
        ImageIcon icon = null;
        try {
            URL url = getClass().getResource(iconPath);
            if (url != null) {
                // Escala el ícono para que sea grande y vistoso
                Image img = new ImageIcon(url).getImage().getScaledInstance(52, 52, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        } catch (Exception e) {
            // Manejo de error si el ícono no se encuentra
            System.err.println("Icono no encontrado: " + iconPath);
        }

        SoftButton btn = new SoftButton(icon);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(60, 60));
        btn.setMinimumSize(new Dimension(60, 60));
        btn.setMaximumSize(new Dimension(60, 60));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // La lógica de hover, press y sonido ahora está encapsulada dentro de SoftButton
        // Se añade un listener adicional solo para los sonidos, ya que SoftButton es agnóstico al sonido.
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