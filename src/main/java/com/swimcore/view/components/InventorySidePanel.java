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
 * Componente de UI reutilizable que implementa una barra de herramientas vertical.
 * Utiliza el componente personalizado 'SoftButton' para lograr un efecto 3D acolchado y
 * una experiencia de usuario (UX) de alta gama.
 *
 * PRINCIPIOS POO APLICADOS:
 * - HERENCIA: Extiende de JPanel para comportarse como un contenedor Swing.
 * - COMPOSICIÓN: Contiene una colección de componentes 'SoftButton'.
 * - ABSTRACCIÓN: Simplifica la creación de una barra de herramientas compleja en un único componente.
 * - ENCAPSULAMIENTO: Las constantes de estilo y la lógica de creación de botones son privadas.
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
 * Panel lateral de navegación para módulos.
 * Utiliza botones 'SoftButton' personalizados para una interfaz hiperrealista.
 */
public class InventorySidePanel extends JPanel {

    // --- ATRIBUTOS (Constantes de Estilo) ---
    private final Color COLOR_PANEL_BG = new Color(22, 22, 22);

    /**
     * Constructor.
     * ENTRADA: Ninguna.
     * PROCESO: Configura el layout, color de fondo, borde y tamaño preferido del panel.
     * SALIDA: Una instancia del panel lista para recibir botones.
     */
    public InventorySidePanel() {
        setBackground(COLOR_PANEL_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 10, 20, 10)); // Padding
        setPreferredSize(new Dimension(80, 0)); // Ancho fijo
    }

    /**
     * Metodo público para agregar botones de acción al panel.
     * ENTRADA: Ruta del ícono, texto para el tooltip y la acción a ejecutar.
     * PROCESO: Delega la creación del botón al metodo factory 'createSoftButton',
     *          asigna la acción y lo añade al layout vertical.
     * SALIDA: Ninguna (modifica el estado interno del panel).
     * @param iconPath Ruta del ícono 3D (ej. "/images/icons/icon_add.png").
     * @param tooltip Texto descriptivo que aparece al pasar el mouse.
     * @param action La acción (lambda expression) a ejecutar al hacer clic.
     */
    public void addButton(String iconPath, String tooltip, ActionListener action) {
        SoftButton button = createSoftButton(iconPath, tooltip);
        button.addActionListener(action);
        add(button);
        add(Box.createRigidArea(new Dimension(0, 15))); // Espacio vertical entre botones
    }

    /**
     * Fábrica de Botones Personalizados (Factory Method).
     * ENTRADA: Ruta del ícono y texto para el tooltip.
     * PROCESO: Carga la imagen, la escala a un tamaño vistoso, y la asigna a una
     *          nueva instancia del componente 'SoftButton'. También añade los
     *          listeners para los efectos de sonido.
     * SALIDA: Una instancia de 'SoftButton' configurada.
     * VALIDACIÓN: Si no encuentra el ícono en la ruta especificada, falla de
     *             forma segura mostrando un mensaje en consola.
     */
    private SoftButton createSoftButton(String iconPath, String tooltip) {
        ImageIcon icon = null;
        try {
            URL url = getClass().getResource(iconPath);
            if (url != null) {
                // Escala el ícono para que sea grande y vistoso (52x52 en un botón de 60x60)
                Image img = new ImageIcon(url).getImage().getScaledInstance(52, 52, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        } catch (Exception e) {
            System.err.println("Icono no encontrado: " + iconPath);
        }

        SoftButton btn = new SoftButton(icon);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(60, 60));
        btn.setMinimumSize(new Dimension(60, 60));
        btn.setMaximumSize(new Dimension(60, 60));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Se añade un listener adicional para conectar con el SoundManager,
        // ya que SoftButton es agnóstico a la lógica de sonido.
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