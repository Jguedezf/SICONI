/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ClientCard.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Micro-componente de la Capa de Vista diseñado para representar una entidad 'Cliente'
 * de forma visual y resumida (Tarjeta/Card).
 * Se utiliza dentro del contenedor de flujo (FlowLayout) del módulo de gestión de clientes.
 *
 * Características de Ingeniería de UI:
 * 1. Diseño Atómico: Encapsula la lógica de presentación de un solo cliente, facilitando
 * su reutilización en listas o galerías.
 * 2. Feedback Visual (UX): Implementa escuchas de eventos de ratón (MouseListener) para
 * alterar el estado del borde al pasar el cursor (Hover Effect), mejorando la interactividad.
 * 3. Carga Condicional de Recursos: Selecciona el ícono a mostrar basándose en el estado
 * del modelo de datos (Atleta vs Club).
 *
 * PRINCIPIOS POO:
 * - HERENCIA: Extiende de `javax.swing.JPanel` para comportarse como un contenedor.
 * - ENCAPSULAMIENTO: Define constantes de estilo (colores y bordes) como `private final`.
 * - POLIMORFISMO: Sobreescribe los métodos de la interfaz `MouseListener` mediante una
 * clase anónima (`MouseAdapter`).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.components;

import com.swimcore.model.Client;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * Componente visual (Widget) que renderiza la información de un Cliente.
 * Implementa interactividad básica mediante cambio de bordes y cursor.
 */
public class ClientCard extends JPanel {

    // --- CONSTANTES DE ESTILO (PALETA DARK MODE) ---
    private final Color COLOR_CARD_BG = new Color(45, 45, 45);
    // Borde estático (Estado Normal)
    private final Border BORDER_DEFAULT = BorderFactory.createLineBorder(new Color(80, 80, 80));
    // Borde dinámico (Estado Hover) - Fucsia con grosor de 2px
    private final Border BORDER_HOVER = BorderFactory.createLineBorder(new Color(220, 0, 115), 2);

    /**
     * Constructor del Componente.
     * Construye la jerarquía visual de la tarjeta basada en los datos del modelo.
     * @param client Objeto de dominio (Model) que contiene los datos a mostrar.
     */
    public ClientCard(Client client) {
        // Configuración del contenedor principal (La Tarjeta)
        setLayout(new BorderLayout(10, 10)); // Espaciado interno (Gaps)
        setBackground(COLOR_CARD_BG);
        setBorder(BORDER_DEFAULT);
        setPreferredSize(new Dimension(220, 100)); // Dimensiones fijas para consistencia en la grilla

        // 1. SECCIÓN DE INFORMACIÓN TEXTUAL (CENTRO)
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false); // Transparencia para heredar fondo de la tarjeta
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS)); // Apilamiento vertical
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding interno

        // Etiqueta Nombre (Jerarquía visual alta)
        JLabel lblName = new JLabel(client.getName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(Color.WHITE);

        // Etiqueta Teléfono (Jerarquía visual media)
        JLabel lblPhone = new JLabel(client.getPhone());
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPhone.setForeground(Color.LIGHT_GRAY);

        textPanel.add(lblName);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espaciador rígido
        textPanel.add(lblPhone);

        add(textPanel, BorderLayout.CENTER);

        // 2. SECCIÓN VISUAL / ICONOGRAFÍA (IZQUIERDA)
        // Lógica de negocio visual: Selección de ícono según tipo de cliente
        String iconPath = "/images/client.png"; // Fallback por defecto
        if ("Atleta".equalsIgnoreCase(client.getType())) {
            iconPath = "/images/client.png";
        } else if ("Club".equalsIgnoreCase(client.getType())) {
            iconPath = "/images/inventory.png"; // Diferenciación visual para Clubes
        }

        // Carga segura de recursos gráficos
        try {
            URL url = getClass().getResource(iconPath);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                // Escalado de imagen (Thumbnail)
                Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                JLabel lblIcon = new JLabel(new ImageIcon(img));
                lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                add(lblIcon, BorderLayout.WEST);
            }
        } catch (Exception e) { e.printStackTrace(); }


        // 3. CAPA DE INTERACTIVIDAD (EVENT LISTENER)
        // Implementación del patrón Observer para detectar eventos del ratón
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Estado Activo: Cambia borde a Fucsia y cursor a Mano
                setBorder(BORDER_HOVER);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Estado Inactivo: Restaura borde gris y cursor por defecto
                setBorder(BORDER_DEFAULT);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
}