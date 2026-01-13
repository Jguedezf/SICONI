/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: DashboardView.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Vista (View) que funge como Contenedor Principal (Main Container) del sistema.
 * Implementa una arquitectura de navegación centralizada ("Hub & Spoke"), actuando como
 * el nodo central desde el cual se instancian y visualizan los módulos funcionales.
 *
 * Características de Ingeniería de UI:
 * 1. Diseño Responsivo: Implementación de `GridBagLayout` y `BorderLayout` anidados para centrado dinámico.
 * 2. Custom Rendering: Sobreescritura del delegado de UI (`BasicButtonUI`) para renderizado
 * vectorial personalizado de botones (bordes redondeados, efectos hover).
 * 3. Gestión de Recursos: Carga dinámica de assets gráficos mediante ClassLoader.
 *
 * PRINCIPIOS DE PROGRAMACIÓN ORIENTADA A OBJETOS (POO):
 * 1. HERENCIA: Extiende de `javax.swing.JFrame` para heredar propiedades de ventana de sistema.
 * 2. POLIMORFISMO: Sobreescritura (Override) del método `paint()` en componentes Swing anónimos
 * para alterar su comportamiento gráfico estándar.
 * 3. COMPOSICIÓN: Construcción de la interfaz compleja mediante la agregación de paneles y componentes.
 *
 * PATRONES DE DISEÑO IMPLEMENTADOS:
 * - Composite: Estructura jerárquica de componentes Swing (Paneles dentro de Paneles).
 * - Command: Encapsulamiento de las acciones de navegación en los Listeners de los botones.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.util.ImagePanel;
import com.swimcore.view.dialogs.SupplierManagementDialog;
// import com.swimcore.view.ClientManagementDialog; // Comentado temporalmente para evitar errores (Dependencia futura)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * Vista Principal (Dashboard).
 * Centraliza el acceso a los subsistemas mediante un menú de rejilla.
 */
public class DashboardView extends JFrame {

    // --- DEFINICIÓN DE CONSTANTES DE ESTILO (PALETA DARK MODE) ---
    private final Color COLOR_BG = new Color(18, 18, 18);
    private final Color COLOR_CARD = new Color(30, 30, 30);
    private final Color COLOR_FUCSIA = new Color(220, 0, 115);
    private final Color COLOR_TEXTO = new Color(240, 240, 240);

    /**
     * Constructor.
     * Inicializa el contenedor raíz, configura la estrategia de Layout y carga los componentes.
     */
    public DashboardView() {
        // Configuración de propiedades del Frame
        setTitle("SICONI - Panel Principal");
        setSize(1100, 680); // Resolución optimizada
        setLocationRelativeTo(null); // Centrado en viewport
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel;
        try {
            // Intento de instanciación del panel con imagen de fondo (ImagePanel)
            mainPanel = new ImagePanel("/images/bg.png");
        } catch(Exception e) {
            // Manejo de excepción: Fallback a panel sólido si el recurso no carga
            mainPanel = new JPanel();
            mainPanel.setBackground(COLOR_BG);
        }

        // Estrategia de Layout: BorderLayout para dividir Norte, Centro y Sur
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // 1. ESPACIO SUPERIOR (Spacer para margen visual)
        JPanel headerSpacer = new JPanel();
        headerSpacer.setOpaque(false);
        headerSpacer.setPreferredSize(new Dimension(1, 40));
        mainPanel.add(headerSpacer, BorderLayout.NORTH);

        // 2. CENTRO (Inyección del Menú de Módulos)
        mainPanel.add(createCentralMenu(), BorderLayout.CENTER);

        // 3. PIE DE PÁGINA (Footer informativo)
        mainPanel.add(createFooter(), BorderLayout.SOUTH);
    }

    /**
     * Construye el panel central contenedor de la matriz de navegación.
     * Utiliza anidamiento de layouts (GridBagLayout) para centrado absoluto.
     *
     * @return JPanel configurado con la rejilla de botones.
     */
    private JPanel createCentralMenu() {
        // Contenedor intermedio con GridBagLayout para centrado vertical/horizontal
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        // REJILLA: 2 Filas, 3 Columnas, con Gaps de 25px
        JPanel grid = new JPanel(new GridLayout(2, 3, 25, 25));
        grid.setOpaque(false);

        // --- INSTANCIACIÓN Y MAPEO DE BOTONES ---

        // 1. CLIENTES (Mensaje temporal - Placeholder)
        grid.add(createBigButton("CLIENTES", "Registro y Atletas", "/images/client.png", e -> {
            JOptionPane.showMessageDialog(this, "Módulo Clientes en construcción");
            // new ClientManagementDialog(this).setVisible(true);
        }));

        // 2. INVENTARIO (Módulo Core)
        // Navegación hacia InventoryView inyectando 'this' como dependencia padre (Modalidad)
        grid.add(createBigButton("INVENTARIO", "Insumos y Catálogo", "/images/inventory.png", e -> {
            // Ahora abre InventoryView correctamente
            new InventoryView(this).setVisible(true);
        }));

        // 3. PEDIDOS
        grid.add(createBigButton("PEDIDOS", "Ventas y Producción", "/images/orders.png", null));

        // 4. REPORTES
        grid.add(createBigButton("REPORTES", "Estadísticas", "/images/reports.png", null));

        // 5. PROVEEDORES
        grid.add(createBigButton("AJUSTES", "Proveedores y Dólar", "/images/settings.png", e -> {
            new SupplierManagementDialog(this).setVisible(true);
        }));

        // 6. SALIR (Control de Sesión)
        JButton btnExit = createBigButton("SALIR", "Cerrar Sesión", "/images/logout.png", null);

        // Listener anónimo para gestión de estado Hover (MouseOver) personalizado
        btnExit.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnExit.putClientProperty("hoverColor", new Color(220, 20, 60)); // Rojo Alerta
                btnExit.putClientProperty("hover", true);
                btnExit.repaint(); // Solicitud de repintado al EDT
            }
            public void mouseExited(MouseEvent e) {
                btnExit.putClientProperty("hoverColor", null);
                btnExit.putClientProperty("hover", false);
                btnExit.repaint();
            }
        });

        // Lógica de terminación de la aplicación (System Exit)
        btnExit.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "¿Desea cerrar el sistema?", "SICONI", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
        });
        grid.add(btnExit);

        container.add(grid);
        return container;
    }

    /**
     * Genera el pie de página.
     * @return JPanel con etiquetas de créditos.
     */
    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0,0,10,0));
        JLabel lbl = new JLabel("© 2026 Desarrollado por Johanna Guédez - Ingeniería Informática UNEG");
        lbl.setForeground(Color.GRAY);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(lbl);
        return footer;
    }

    // --- MÉTODOS FACTORY PARA COMPONENTES UI (Visuales) ---

    /**
     * Método Factory para la creación de botones personalizados (Custom Components).
     * Sobreescribe el delegado UI para dibujar formas vectoriales en lugar de los botones nativos.
     *
     * @param title Título del botón.
     * @param subtitle Subtítulo descriptivo.
     * @param iconPath Ruta relativa del recurso gráfico.
     * @param action Implementación funcional de la acción (ActionListener).
     * @return JButton configurado.
     */
    private JButton createBigButton(String title, String subtitle, String iconPath, java.awt.event.ActionListener action) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setPreferredSize(new Dimension(320, 200)); // Restricción de dimensiones
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); // Desactiva el pintado por defecto de Swing

        // Configuración de Icono Central
        JLabel lblIcon = new JLabel();
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            URL url = getClass().getResource(iconPath);
            if (url != null) {
                // Escalado de imagen con algoritmo Smooth para evitar aliasing
                ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH));
                lblIcon.setIcon(icon);
            } else {
                // Fallback visual en caso de error de carga
                lblIcon.setText("🔹"); lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 80)); lblIcon.setForeground(COLOR_FUCSIA);
            }
        } catch (Exception e) { }

        // Panel de Textos (Sur)
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TEXTO);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);

        textPanel.add(lblTitle); textPanel.add(lblSub);
        btn.add(lblIcon, BorderLayout.CENTER);
        btn.add(textPanel, BorderLayout.SOUTH);

        // Vinculación del Action Listener
        if(action != null) btn.addActionListener(action);

        // Listeners para efectos visuales de interacción (Hover)
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.putClientProperty("hover", true); btn.repaint(); }
            public void mouseExited(MouseEvent e) { btn.putClientProperty("hover", false); btn.repaint(); }
        });

        // SOBREESCRITURA DEL UI DELEGATE (CUSTOM PAINTING)
        // Dibuja el fondo redondeado y el borde dinámico directamente en el Graphics2D
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Activación de Antialiasing para bordes suaves
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Recuperación de estado desde propiedades del cliente
                boolean isHover = Boolean.TRUE.equals(c.getClientProperty("hover"));
                Color hoverColor = (Color) c.getClientProperty("hoverColor");
                int arc = 35; // Radio de curvatura del borde

                if (isHover) {
                    // Estado Hover: Fondo gris claro + Borde de color (Fucsia o Rojo)
                    g2.setColor(new Color(50, 50, 50));
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), arc, arc);
                    g2.setColor(hoverColor != null ? hoverColor : COLOR_FUCSIA);
                    g2.setStroke(new BasicStroke(3)); // Borde grueso
                    g2.drawRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, arc, arc);
                } else {
                    // Estado Normal: Fondo oscuro (Card Color)
                    g2.setColor(COLOR_CARD);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), arc, arc);
                }
                g2.dispose();
                super.paint(g, c); // Dibuja los hijos (Icono y Texto) encima del fondo
            }
        });
        return btn;
    }
}