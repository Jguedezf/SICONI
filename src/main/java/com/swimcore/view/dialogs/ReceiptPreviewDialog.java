/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ReceiptPreviewDialog.java
 * VERSIÓN: 6.1.0 (TicketItem Visibility Fix)
 * DESCRIPCIÓN: Recibo que recibe los datos directamente de la vista principal.
 * Se corrige visibilidad de TicketItem para acceso externo.
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
import java.io.File; // Necesario para File
import java.awt.Desktop; // Necesario para Desktop.getDesktop().open()

public class ReceiptPreviewDialog extends JDialog {

    // --- CLASE INTERNA PARA TRANSPORTAR LOS DETALLES ---
    public static class TicketItem {
        // !!! MODIFICACIÓN CLAVE: Hacer las variables públicas !!!
        public String name;
        public int qty;
        public double subtotal;

        public TicketItem(String name, int qty, double subtotal) {
            this.name = name;
            this.qty = qty;
            this.subtotal = subtotal;
        }
    }

    // --- DATOS DEL RECIBO ---
    private final String saleCode;
    private final String dateStr;
    private final String clientName;
    private final String clientPhone;
    private final List<TicketItem> items;
    private final double total;
    private final double paid;
    private final double balance;

    private JPanel ticketPanel; // El panel que se imprimirá

    // --- PALETA LUXURY ---
    private final Color COLOR_BG_DARK = new Color(18, 18, 18);
    private final Color COLOR_GOLD = new Color(212, 175, 55);

    /**
     * CONSTRUCTOR NUEVO: Recibe TODOS los datos necesarios.
     */
    public ReceiptPreviewDialog(Window owner,
                                String saleCode, String dateStr,
                                String clientName, String clientPhone,
                                List<TicketItem> items,
                                double total, double paid, double balance) {

        super(owner, "Vista Previa del Recibo", ModalityType.APPLICATION_MODAL);

        // Asignamos los datos recibidos
        this.saleCode = saleCode;
        this.dateStr = dateStr;
        this.clientName = (clientName == null || clientName.isEmpty()) ? "MOSTRADOR" : clientName;
        this.clientPhone = (clientPhone == null) ? "" : clientPhone;
        this.items = (items == null) ? new ArrayList<>() : items;
        this.total = total;
        this.paid = paid;
        this.balance = balance;

        setSize(500, 700);
        setLocationRelativeTo(owner);

        getRootPane().setBorder(new LineBorder(COLOR_GOLD, 2));
        getContentPane().setBackground(COLOR_BG_DARK);
        setLayout(new BorderLayout());

        // 1. TÍTULO DE LA VENTANA
        JLabel lblTitle = new JLabel("VISTA PREVIA DEL RECIBO", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_GOLD);
        lblTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // 2. EL TICKET (VISUAL)
        ticketPanel = createTicketVisual();

        JScrollPane scroll = new JScrollPane(ticketPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_BG_DARK);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setBackground(COLOR_BG_DARK);
        wrapper.add(ticketPanel);
        scroll.setViewportView(wrapper);

        add(scroll, BorderLayout.CENTER);

        // 3. PINTAR DATOS (Directo de variables, CERO SQL)
        renderTicketData();

        // 4. BOTONES
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createTicketVisual() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));
        panel.setPreferredSize(new Dimension(380, 500));
        return panel;
    }

    private void renderTicketData() {
        ticketPanel.removeAll();

        // --- A. CABECERA ---
        addTicketText("SICONI", 24, true, Color.BLACK);
        addTicketText("TALLER DE CONFECCIÓN", 12, false, Color.DARK_GRAY);
        addTicketText("Puerto Ordaz, Venezuela", 10, false, Color.GRAY);
        addTicketText("R.I.F: V-14089807-1", 10, false, Color.GRAY);
        addSeparator();

        // --- B. DATOS GENERALES ---
        addTicketLeftRight("RECIBO N°:", saleCode);
        addTicketLeftRight("FECHA:", (dateStr.length() > 10 ? dateStr.substring(0, 10) : dateStr));
        addSeparator();

        addTicketLeftRight("CLIENTE:", clientName);
        if(!clientPhone.isEmpty()) addTicketLeftRight("TLF:", clientPhone);
        addSeparator();

        // --- C. ÍTEMS (Iteramos la lista que recibimos) ---
        addTicketText("DETALLE DE PEDIDO", 12, true, Color.BLACK);
        addBox(5);

        if (items.isEmpty()) {
            addTicketText("(Sin detalles)", 10, false, Color.GRAY);
        } else {
            for (TicketItem item : items) {
                // Formato: "1 x Pantalon ($20.00)"
                String precioFmt = String.format(Locale.US, "$%.2f", item.subtotal);
                addTicketItem(item.qty + " x " + item.name, precioFmt);
            }
        }

        addSeparator();

        // --- D. TOTALES ---
        addTicketTotal("TOTAL:", total, false);
        addTicketTotal("ABONADO:", paid, false);
        addTicketTotal("RESTA:", balance, true);

        addSeparator();
        addTicketText("¡Gracias por su preferencia!", 12, true, Color.BLACK);
        addTicketText("@siconi.confecciones", 10, false, Color.GRAY);

        ticketPanel.revalidate();
        ticketPanel.repaint();
    }

    // --- HELPERS VISUALES ---

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

    // --- FOOTER E IMPRESIÓN ---

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

    private void printJob() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Recibo SICONI " + saleCode);
        job.setPrintable(new Printable() {
            public int print(Graphics pg, PageFormat pf, int pageNum) {
                if (pageNum > 0) return Printable.NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) pg;
                g2.translate(pf.getImageableX(), pf.getImageableY());
                double scale = pf.getImageableWidth() / ticketPanel.getWidth();
                if(scale < 1.0) g2.scale(scale, scale);
                ticketPanel.printAll(g2);
                return Printable.PAGE_EXISTS;
            }
        });
        if (job.printDialog()) { try { job.print(); dispose(); } catch (PrinterException e) {} }
    }
}