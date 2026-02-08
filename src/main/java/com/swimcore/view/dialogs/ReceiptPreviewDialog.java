/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ReceiptPreviewDialog.java
 * VERSIÓN: 6.1.0 (TicketItem Visibility Fix)
 * FECHA: 06 de Febrero de 2026
 * HORA: 09:00 PM (Hora de Venezuela)
 * DESCRIPCIÓN TÉCNICA:
 * Módulo de Visualización de Recibos. Genera una representación gráfica (Preview)
 * del comprobante de venta antes de su impresión física. Implementa el patrón
 * de "Transferencia de Estado" recibiendo los datos ya procesados, evitando
 * consultas redundantes a la base de datos.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.print.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.io.File;
import java.awt.Desktop;

/**
 * [VISTA - RECIBO] Clase que gestiona la previsualización e impresión de tickets.
 * [POO - HERENCIA] Extiende de JDialog para mostrarse como una ventana modal.
 * * FUNCIONALIDAD: Renderizado visual de datos de venta e interfaz con la impresora.
 */
public class ReceiptPreviewDialog extends JDialog {

    // ========================================================================================
    //                                  ESTRUCTURAS DE DATOS (DTO)
    // ========================================================================================

    /**
     * [CLASE INTERNA ESTÁTICA - DTO]
     * Objeto de Transferencia de Datos para los detalles del recibo.
     * Permite encapsular la información de cada ítem (Nombre, Cantidad, Subtotal)
     * para su transporte seguro entre la vista de ventas y el diálogo de impresión.
     * [MODIFICADOR DE ACCESO] Se declara 'public' para permitir visibilidad externa.
     */
    public static class TicketItem {
        public String name;
        public int qty;
        public double subtotal;

        public TicketItem(String name, int qty, double subtotal) {
            this.name = name;
            this.qty = qty;
            this.subtotal = subtotal;
        }
    }

    // ========================================================================================
    //                                  ATRIBUTOS DE LA CLASE
    // ========================================================================================

    // Variables finales (inmutables) que almacenan el estado del recibo.
    private final String saleCode;
    private final String dateStr;
    private final String clientName;
    private final String clientPhone;
    private final List<TicketItem> items; // Lista de ítems a renderizar
    private final double total;
    private final double paid;
    private final double balance;

    // Componente visual principal que contendrá el diseño del ticket.
    private JPanel ticketPanel;

    // Constantes de diseño (Identidad Visual)
    private final Color COLOR_BG_DARK = new Color(18, 18, 18);
    private final Color COLOR_GOLD = new Color(212, 175, 55);

    // ========================================================================================
    //                                  CONSTRUCTOR
    // ========================================================================================

    /**
     * Constructor principal.
     * Recibe la totalidad de los datos necesarios para generar el recibo,
     * eliminando la necesidad de realizar consultas SQL adicionales (Optimización).
     *
     * @param owner Ventana propietaria (Modalidad).
     * @param saleCode Código único de la venta.
     * @param dateStr Fecha formateada.
     * @param clientName Nombre del cliente.
     * @param clientPhone Teléfono de contacto.
     * @param items Lista de objetos TicketItem con el detalle.
     * @param total Monto total de la transacción.
     * @param paid Monto abonado.
     * @param balance Saldo pendiente.
     */
    public ReceiptPreviewDialog(Window owner,
                                String saleCode, String dateStr,
                                String clientName, String clientPhone,
                                List<TicketItem> items,
                                double total, double paid, double balance) {

        super(owner, "Vista Previa del Recibo", ModalityType.APPLICATION_MODAL);

        // Inicialización de atributos con validación básica de nulos (Fail-Safe)
        this.saleCode = saleCode;
        this.dateStr = dateStr;
        this.clientName = (clientName == null || clientName.isEmpty()) ? "MOSTRADOR" : clientName;
        this.clientPhone = (clientPhone == null) ? "" : clientPhone;
        this.items = (items == null) ? new ArrayList<>() : items;
        this.total = total;
        this.paid = paid;
        this.balance = balance;

        // Configuración de la ventana
        setSize(500, 700);
        setLocationRelativeTo(owner);

        getRootPane().setBorder(new LineBorder(COLOR_GOLD, 2));
        getContentPane().setBackground(COLOR_BG_DARK);
        setLayout(new BorderLayout());

        // 1. HEADER (Título Visual)
        JLabel lblTitle = new JLabel("VISTA PREVIA DEL RECIBO", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_GOLD);
        lblTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // 2. CUERPO DEL TICKET (Renderizado)
        ticketPanel = createTicketVisual();

        JScrollPane scroll = new JScrollPane(ticketPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_BG_DARK);

        // Wrapper para centrar el panel del ticket
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setBackground(COLOR_BG_DARK);
        wrapper.add(ticketPanel);
        scroll.setViewportView(wrapper);

        add(scroll, BorderLayout.CENTER);

        // 3. INYECCIÓN DE DATOS (Rendering)
        renderTicketData();

        // 4. FOOTER (Controles de Impresión)
        add(createFooter(), BorderLayout.SOUTH);
    }

    // ========================================================================================
    //                                  LÓGICA DE RENDERIZADO VISUAL
    // ========================================================================================

    private JPanel createTicketVisual() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Layout vertical para simular rollo de papel
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40)); // Márgenes internos
        panel.setPreferredSize(new Dimension(380, 500));
        return panel;
    }

    /**
     * Construye dinámicamente el contenido del ticket agregando componentes Swing (JLabel, JPanel)
     * al panel principal en orden secuencial.
     */
    private void renderTicketData() {
        ticketPanel.removeAll();

        // --- SECCIÓN A: CABECERA CORPORATIVA ---
        addTicketText("SICONI", 24, true, Color.BLACK);
        addTicketText("TALLER DE CONFECCIÓN", 12, false, Color.DARK_GRAY);
        addTicketText("Puerto Ordaz, Venezuela", 10, false, Color.GRAY);
        addTicketText("R.I.F: V-14089807-1", 10, false, Color.GRAY);
        addSeparator();

        // --- SECCIÓN B: DATOS DE LA TRANSACCIÓN ---
        addTicketLeftRight("RECIBO N°:", saleCode);
        addTicketLeftRight("FECHA:", (dateStr.length() > 10 ? dateStr.substring(0, 10) : dateStr));
        addSeparator();

        addTicketLeftRight("CLIENTE:", clientName);
        if(!clientPhone.isEmpty()) addTicketLeftRight("TLF:", clientPhone);
        addSeparator();

        // --- SECCIÓN C: DETALLE DE PRODUCTOS (Iteración de Lista) ---
        addTicketText("DETALLE DE PEDIDO", 12, true, Color.BLACK);
        addBox(5);

        if (items.isEmpty()) {
            addTicketText("(Sin detalles)", 10, false, Color.GRAY);
        } else {
            for (TicketItem item : items) {
                // Formato de línea: "CANT x PRODUCTO (PRECIO)"
                String precioFmt = String.format(Locale.US, "$%.2f", item.subtotal);
                addTicketItem(item.qty + " x " + item.name, precioFmt);
            }
        }

        addSeparator();

        // --- SECCIÓN D: RESUMEN FINANCIERO ---
        addTicketTotal("TOTAL:", total, false);
        addTicketTotal("ABONADO:", paid, false);
        addTicketTotal("RESTA:", balance, true); // Resaltado en rojo si hay deuda

        addSeparator();
        addTicketText("¡Gracias por su preferencia!", 12, true, Color.BLACK);
        addTicketText("@siconi.confecciones", 10, false, Color.GRAY);

        // Refresco de la interfaz
        ticketPanel.revalidate();
        ticketPanel.repaint();
    }

    // ========================================================================================
    //                                  MÉTODOS AUXILIARES DE UI (HELPERS)
    // ========================================================================================

    private void addTicketText(String text, int size, boolean bold, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(l);
        ticketPanel.add(Box.createVerticalStrut(2));
    }

    private void addTicketLeftRight(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(1000, 18));
        JLabel l1 = new JLabel(label); l1.setFont(new Font("Monospaced", Font.BOLD, 11));
        JLabel l2 = new JLabel(value); l2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        p.add(l1, BorderLayout.WEST); p.add(l2, BorderLayout.EAST);
        ticketPanel.add(p);
    }

    private void addTicketItem(String item, String price) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(1000, 16));
        JLabel l1 = new JLabel(item); l1.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JLabel l2 = new JLabel(price); l2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        p.add(l1, BorderLayout.WEST); p.add(l2, BorderLayout.EAST);
        ticketPanel.add(p);
    }

    private void addTicketTotal(String label, double val, boolean highlight) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(1000, 20));
        JLabel l1 = new JLabel(label); l1.setFont(new Font("Monospaced", Font.BOLD, 12));
        JLabel l2 = new JLabel(String.format(Locale.US, "$%.2f", val));
        l2.setFont(new Font("Monospaced", Font.BOLD, 14));
        if(highlight && val > 0.01) l2.setForeground(Color.RED);
        p.add(l1, BorderLayout.WEST); p.add(l2, BorderLayout.EAST);
        ticketPanel.add(p);
    }

    private void addSeparator() {
        JLabel l = new JLabel("--------------------------------");
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(l);
        ticketPanel.add(Box.createVerticalStrut(5));
    }

    private void addBox(int height) { ticketPanel.add(Box.createVerticalStrut(height)); }

    // ========================================================================================
    //                                  IMPRESIÓN FÍSICA (JAVA PRINT API)
    // ========================================================================================

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        footer.setOpaque(false);

        SoftButton btnClose = new SoftButton(null);
        btnClose.setText("CERRAR");
        btnClose.setBackground(Color.BLACK);
        btnClose.setForeground(Color.WHITE);
        btnClose.setPreferredSize(new Dimension(120, 45));
        btnClose.addActionListener(e -> dispose());

        SoftButton btnPrint = new SoftButton(null);
        btnPrint.setText("IMPRIMIR");
        btnPrint.setBackground(new Color(57, 255, 20));
        btnPrint.setForeground(Color.BLACK);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPrint.setPreferredSize(new Dimension(120, 45));
        btnPrint.addActionListener(e -> printJob());

        footer.add(btnClose);
        footer.add(btnPrint);
        return footer;
    }

    /**
     * [FUNCIONALIDAD: IMPRESIÓN]
     * Inicia un trabajo de impresión utilizando la clase PrinterJob.
     * Renderiza el contenido del 'ticketPanel' como un gráfico vectorial (Graphics2D)
     * y lo envía al controlador de impresión del sistema operativo.
     */
    private void printJob() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Recibo SICONI " + saleCode);

        // Implementación de la interfaz Printable
        job.setPrintable(new Printable() {
            public int print(Graphics pg, PageFormat pf, int pageNum) {
                if (pageNum > 0) return Printable.NO_SUCH_PAGE;

                Graphics2D g2 = (Graphics2D) pg;
                g2.translate(pf.getImageableX(), pf.getImageableY());

                // Escalado automático para ajustar al ancho de la página
                double scale = pf.getImageableWidth() / ticketPanel.getWidth();
                if(scale < 1.0) g2.scale(scale, scale);

                ticketPanel.printAll(g2); // Renderizado del componente Swing
                return Printable.PAGE_EXISTS;
            }
        });

        // Diálogo nativo de selección de impresora
        if (job.printDialog()) { try { job.print(); dispose(); } catch (PrinterException e) {} }
    }
}