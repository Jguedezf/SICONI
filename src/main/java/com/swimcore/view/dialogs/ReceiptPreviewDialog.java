/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: ReceiptPreviewDialog.java
 * VERSIÓN: 1.0.0 (LUXURY TICKET: Vista Previa + Impresión Real)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.dao.ClientDAO;
import com.swimcore.model.Client;
import com.swimcore.util.ImagePanel; // Usamos tu gestor de imágenes si aplica, o color sólido
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.print.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class ReceiptPreviewDialog extends JDialog {

    private final String saleId;
    private JPanel ticketPanel; // El panel que se imprimirá (Papel Blanco)

    // --- PALETA LUXURY ---
    private final Color COLOR_BG_DARK = new Color(18, 18, 18);
    private final Color COLOR_GOLD = new Color(212, 175, 55);

    public ReceiptPreviewDialog(Dialog owner, String saleId) {
        super(owner, "Vista Previa del Recibo", true);
        this.saleId = saleId;

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
        loadTicketData(); // Llenar con datos reales

        // Scroll por si el ticket es muy largo
        JScrollPane scroll = new JScrollPane(ticketPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_BG_DARK);
        add(scroll, BorderLayout.CENTER);

        // 3. BOTONES DE ACCIÓN
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createTicketVisual() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE); // PAPEL BLANCO
        // Formato Ticket (Ancho fijo simulado con margen)
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));
        return panel;
    }

    private void loadTicketData() {
        ticketPanel.removeAll();

        // --- A. CABECERA ---
        addTicketText("SICONI", 24, true, Color.BLACK);
        addTicketText("TALLER DE CONFECCIÓN", 12, false, Color.DARK_GRAY);
        addTicketText("Puerto Ordaz, Venezuela", 10, false, Color.GRAY);
        addTicketText("Tlf: +58 414-1234567", 10, false, Color.GRAY);
        addSeparator();

        // --- B. DATOS DEL PEDIDO ---
        try (Connection conn = com.swimcore.dao.Conexion.conectar()) {
            String sql = "SELECT * FROM sales WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, saleId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String fecha = rs.getString("date");
                int clientId = rs.getInt("client_id");

                // Buscar Cliente
                ClientDAO clientDao = new ClientDAO();
                Client client = clientDao.getAllClients().stream()
                        .filter(c -> c.getId() == clientId).findFirst().orElse(null);

                addTicketLeftRight("RECIBO N°:", saleId);
                addTicketLeftRight("FECHA:", fecha.substring(0, 10));
                addSeparator();

                if (client != null) {
                    addTicketLeftRight("CLIENTE:", client.getFullName());
                    addTicketLeftRight("TLF:", client.getPhone());
                }
                addSeparator();

                // --- C. ÍTEMS ---
                addTicketText("DETALLE DE PEDIDO", 12, true, Color.BLACK);
                addBox(5);

                String sqlItems = "SELECT p.name, d.quantity, p.price_usd FROM sale_details d JOIN products p ON d.product_id = p.id WHERE d.sale_id = ?";
                PreparedStatement pstItems = conn.prepareStatement(sqlItems);
                pstItems.setString(1, saleId);
                ResultSet rsItems = pstItems.executeQuery();

                while (rsItems.next()) {
                    String prodName = rsItems.getString("name");
                    int qty = rsItems.getInt("quantity");
                    double price = rsItems.getDouble("price_usd");
                    double sub = qty * price;

                    // Formato: "2 x Boxer Basico ($20.00)"
                    addTicketItem(qty + " x " + prodName, String.format("$%.2f", sub));
                }
                addSeparator();

                // --- D. TOTALES ---
                double total = rs.getDouble("total_divisa");
                double abonado = rs.getDouble("amount_paid_usd");
                double resta = rs.getDouble("balance_due_usd");

                addTicketTotal("TOTAL:", total, false);
                addTicketTotal("ABONADO:", abonado, false);
                addTicketTotal("RESTA:", resta, true); // Negrita para la deuda
            }

        } catch (Exception e) {
            addTicketText("ERROR AL CARGAR DATOS", 14, true, Color.RED);
            e.printStackTrace();
        }

        addSeparator();
        addTicketText("¡Gracias por su preferencia!", 12, true, Color.BLACK);
        addTicketText("@siconi.confecciones", 10, false, Color.GRAY);

        ticketPanel.revalidate();
        ticketPanel.repaint();
    }

    // --- HELPERS PARA DIBUJAR EL TICKET ---

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
        p.setMaximumSize(new Dimension(1000, 20));

        JLabel l1 = new JLabel(label);
        l1.setFont(new Font("Monospaced", Font.BOLD, 11));

        JLabel l2 = new JLabel(value);
        l2.setFont(new Font("Monospaced", Font.PLAIN, 11));

        p.add(l1, BorderLayout.WEST);
        p.add(l2, BorderLayout.EAST);
        ticketPanel.add(p);
    }

    private void addTicketItem(String item, String price) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(1000, 20));

        JLabel l1 = new JLabel(item);
        l1.setFont(new Font("Monospaced", Font.PLAIN, 10));

        JLabel l2 = new JLabel(price);
        l2.setFont(new Font("Monospaced", Font.PLAIN, 10));

        p.add(l1, BorderLayout.WEST);
        p.add(l2, BorderLayout.EAST);
        ticketPanel.add(p);
    }

    private void addTicketTotal(String label, double val, boolean highlight) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(1000, 25));

        JLabel l1 = new JLabel(label);
        l1.setFont(new Font("Monospaced", Font.BOLD, 12));

        JLabel l2 = new JLabel(String.format("$%.2f", val));
        l2.setFont(new Font("Monospaced", Font.BOLD, 14));
        if(highlight) l2.setForeground(Color.RED);

        p.add(l1, BorderLayout.WEST);
        p.add(l2, BorderLayout.EAST);
        ticketPanel.add(p);
    }

    private void addSeparator() {
        JLabel l = new JLabel("--------------------------------");
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(l);
        ticketPanel.add(Box.createVerticalStrut(5));
    }

    private void addBox(int height) {
        ticketPanel.add(Box.createVerticalStrut(height));
    }

    // --- FOOTER Y LÓGICA DE IMPRESIÓN ---

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
        btnPrint.setBackground(new Color(57, 255, 20)); // VERDE NEON
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
        job.setJobName("Recibo SICONI " + saleId);

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics pg, PageFormat pf, int pageNum) {
                if (pageNum > 0) return Printable.NO_SUCH_PAGE;

                Graphics2D g2 = (Graphics2D) pg;
                g2.translate(pf.getImageableX(), pf.getImageableY());
                // Escalar el panel para que quepa en la hoja
                double scaleX = pf.getImageableWidth() / ticketPanel.getWidth();
                // Usamos un factor de escala seguro, o 1.0 si es muy pequeño
                if(scaleX < 1.0) g2.scale(scaleX, scaleX);

                ticketPanel.paint(g2);
                return Printable.PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                dispose();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Error de impresión: " + e.getMessage());
            }
        }
    }
}