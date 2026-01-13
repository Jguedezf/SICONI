/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ClientManagementDialog.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Vista (View) encargada del módulo de gestión de clientes.
 * A diferencia del inventario (que usa JTable), esta vista implementa una interfaz
 * de tipo "Galería" o "Grid", renderizando cada registro como una tarjeta visual
 * independiente (ClientCard) dentro de un contenedor de flujo dinámico.
 *
 * Características de Ingeniería:
 * 1. Renderizado Dinámico de Componentes: Instanciación de objetos visuales en tiempo de ejecución
 * basados en la colección de datos recuperada por el controlador.
 * 2. Diseño Fluido: Uso de `FlowLayout` dentro de un `JScrollPane` para permitir
 * que las tarjetas se acomoden automáticamente según el ancho de la ventana.
 * 3. Integración MVC: Comunicación directa con `ClientController` para la obtención de datos.
 *
 * PRINCIPIOS POO:
 * - COMPOSICIÓN: La ventana se compone de múltiples instancias de `ClientCard`.
 * - HERENCIA: Extiende de `JDialog` para comportamiento modal/secundario.
 *
 * PATRONES DE DISEÑO:
 * - Composite: Manejo de una jerarquía de componentes (Panel > Scroll > Panel > Tarjetas).
 * - Controller (MVC): Delega la lógica de negocio al `ClientController`.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.controller.ClientController;
import com.swimcore.model.Client;
import com.swimcore.view.components.ClientCard;
import com.swimcore.view.dialogs.AddEditClientDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Ventana "Galería de Clientes".
 * Muestra los clientes registrados como tarjetas visuales interactivas.
 * Permite la búsqueda y la navegación hacia la creación/edición de registros.
 */
public class ClientManagementDialog extends JDialog {

    // Dependencia del Controlador (Patrón MVC)
    private final ClientController controller;

    // Contenedor dinámico para las tarjetas
    private JPanel cardsPanel;

    // --- PALETA DE COLORES ---
    private final Color COLOR_BACKGROUND = new Color(40, 40, 40);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);

    /**
     * Constructor.
     * @param owner Ventana propietaria (Dashboard) para mantener la jerarquía de ventanas.
     */
    public ClientManagementDialog(Frame owner) {
        super(owner, "Gestión de Clientes", true); // 'true' indica modalidad (bloquea ventana padre)
        this.controller = new ClientController();

        // Configuración inicial del contenedor
        setSize(1200, 700);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Decoración de borde para resaltar la ventana activa
        getRootPane().setBorder(BorderFactory.createLineBorder(COLOR_FUCSIA, 2));

        // 1. BARRA DE HERRAMIENTAS (Norte)
        add(createToolbar(), BorderLayout.NORTH);

        // 2. ÁREA DE CONTENIDO (Centro)
        // Panel con FlowLayout alineado a la izquierda para organizar las tarjetas
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        cardsPanel.setBackground(COLOR_BACKGROUND);

        // ScrollPane para permitir desplazamiento cuando hay muchos clientes
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(null); // Eliminar borde por defecto del scroll
        add(scrollPane, BorderLayout.CENTER);

        // Carga inicial de datos (Data Binding)
        refreshClientCards();
    }

    /**
     * Construye la barra superior con buscador y botón de agregar.
     * @return JPanel configurado.
     */
    private JPanel createToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(30, 30, 30));

        // Campo de búsqueda (Funcionalidad visual por ahora)
        JTextField searchField = new JTextField("Buscar por nombre o ID...", 30);

        // Botón de Acción Principal (Call to Action)
        JButton btnAdd = new JButton("+ Agregar Nuevo Cliente");
        btnAdd.setBackground(COLOR_FUCSIA);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panel.add(searchField);
        panel.add(Box.createHorizontalStrut(20)); // Espaciador rígido
        panel.add(btnAdd);

        // Evento: Abrir diálogo de registro
        btnAdd.addActionListener(e -> {
            // Instancia del diálogo de edición en modo "Nuevo"
            AddEditClientDialog addDialog = new AddEditClientDialog( (Frame) this.getOwner() );
            addDialog.setVisible(true);

            // Callback: Al cerrarse el pop-up, actualizamos la galería para mostrar cambios
            refreshClientCards();
        });

        return panel;
    }

    /**
     * Método de refresco de UI (UI Refresh Pattern).
     * 1. Limpia el contenedor.
     * 2. Solicita la lista actualizada al controlador.
     * 3. Re-instancia las tarjetas visuales.
     * 4. Fuerza el repintado de la interfaz.
     */
    private void refreshClientCards() {
        cardsPanel.removeAll(); // Limpieza de componentes obsoletos

        // Solicitud al Controlador (Backend logic)
        List<Client> clients = controller.getAllClients();

        // Iteración y construcción de componentes dinámicos (Composite Pattern)
        for (Client client : clients) {
            // ClientCard es un componente personalizado (JPanel) que visualiza un objeto Client
            ClientCard card = new ClientCard(client);
            cardsPanel.add(card);
        }

        // Validación de jerarquía y repintado (Swing Thread safety convention)
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
}