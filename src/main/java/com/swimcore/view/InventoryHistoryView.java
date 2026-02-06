/*
 * -----------------------------------------------------------------------------
 * ARCHIVO: InventoryHistoryView.java
 * VERSIÓN: 4.0 (GOLDEN EDITION)
 * CAMBIOS: Icono PDF Agregado + Diagnóstico de Base de Datos
 * -----------------------------------------------------------------------------
 */
package com.swimcore.view;

import com.itextpdf.text.*; // PDF Core
import com.itextpdf.text.pdf.*; // PDF Tables
import com.swimcore.dao.Conexion;
import com.swimcore.util.ImagePanel;
import com.swimcore.util.LanguageManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle; // AWT Rectangle
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class InventoryHistoryView extends JDialog {

    private JTable table;
    private DefaultTableModel model;

    public InventoryHistoryView(Window owner) {
        super(owner, LanguageManager.get("audit.dialog.title"), ModalityType.APPLICATION_MODAL);
        setSize(1000, 700);
        setLocationRelativeTo(owner);

        JPanel main = new ImagePanel("/images/bg2.png");
        main.setLayout(new BorderLayout());
        main.setBorder(new LineBorder(new Color(212, 175, 55), 2));
        setContentPane(main);

        // --- HEADER ---
        JLabel lbl = new JLabel(LanguageManager.get("audit.report.header.main"), SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lbl.setForeground(new Color(212, 175, 55));
        lbl.setBorder(new EmptyBorder(20,0,20,0));
        main.add(lbl, BorderLayout.NORTH);

        // --- TABLA ---
        String[] columns = {
                LanguageManager.get("audit.column.id"),
                LanguageManager.get("audit.column.date_time"),
                LanguageManager.get("audit.column.code"),
                LanguageManager.get("audit.column.product"),
                LanguageManager.get("audit.column.type"),
                LanguageManager.get("audit.column.quantity"),
                LanguageManager.get("audit.column.observation_reason")
        };

        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        styleTable(table);

        // CARGAMOS LOS DATOS (Aquí es donde ocurre la magia)
        loadData(model);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30,30,30));
        scroll.setBorder(new EmptyBorder(10,20,10,20));
        main.add(scroll, BorderLayout.CENTER);

        // --- PANEL DE BOTONES ---
        JPanel pBtn = new JPanel();
        pBtn.setOpaque(false);
        pBtn.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // 1. EXPORTAR CSV
        SoftButton btnExportarCSV = new SoftButton(null);
        btnExportarCSV.setText(LanguageManager.get("audit.report.button.export_csv"));
        btnExportarCSV.setPreferredSize(new Dimension(150, 40));
        btnExportarCSV.addActionListener(e -> exportarCSV());
        pBtn.add(btnExportarCSV);

        // 2. REPORTE PDF (¡AHORA CON ICONO!)
        // Intentamos cargar el icono
        ImageIcon iconPDF = null;
        try {
            // Buscamos el mismo icono que usas en otras pantallas
            URL url = getClass().getResource("/images/icons/icon_pdf_hq.png");
            if (url != null) {
                // Lo escalamos un poquito para que entre bien
                Image img = new ImageIcon(url).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                iconPDF = new ImageIcon(img);
            }
        } catch (Exception ex) { System.out.println("No se encontró icono PDF"); }

        SoftButton btnGenerarPDF = new SoftButton(iconPDF); // <--- Aquí pasamos el icono
        btnGenerarPDF.setText(LanguageManager.get("audit.report.button.generate_pdf"));
        btnGenerarPDF.setPreferredSize(new Dimension(220, 40)); // Un poco más ancho para que quepa el icono
        btnGenerarPDF.addActionListener(e -> exportToAestheticPDF());
        pBtn.add(btnGenerarPDF);

        // 3. CERRAR
        SoftButton btnCerrar = new SoftButton(null);
        btnCerrar.setText(LanguageManager.get("audit.report.button.close"));
        btnCerrar.setPreferredSize(new Dimension(100, 40));
        btnCerrar.addActionListener(e -> dispose());
        pBtn.add(btnCerrar);

        main.add(pBtn, BorderLayout.SOUTH);
    }

    // --- CARGAR DATOS DE BASE DE DATOS ---
    private void loadData(DefaultTableModel model) {
        // SQL simple para ver todo el historial
        String sql = "SELECT m.id, m.date, p.id as product_code, p.name, m.type, m.quantity, m.observation " +
                "FROM inventory_movements m " +
                "JOIN products p ON m.product_id = p.id " +
                "ORDER BY m.date DESC LIMIT 100"; // Limité a 100 para probar rápido

        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            boolean hayDatos = false;
            while(rs.next()) {
                hayDatos = true;
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("date"),
                        "D-" + rs.getString("product_code"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("quantity"),
                        rs.getString("observation")
                });
            }

            if (!hayDatos) {
                // Si conecta pero no hay filas
                System.out.println("Conexión exitosa, pero la tabla 'inventory_movements' está vacía.");
            }

        } catch (Exception e) {
            // ¡ESTO ES IMPORTANTE! Si falla, te saldrá una ventana avisando.
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar datos de la BD:\n" + e.getMessage(),
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(30,30,30)); t.setForeground(Color.WHITE);
        t.setRowHeight(30); t.getTableHeader().setBackground(Color.BLACK);
        t.getTableHeader().setForeground(new Color(212, 175, 55));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);

                // Colorear Entrada/Salida
                if(column == 4) {
                    String texto = value != null ? value.toString().toUpperCase() : "";
                    if(texto.contains("SALIDA")) setForeground(Color.RED);
                    else if(texto.contains("ENTRADA")) setForeground(Color.GREEN);
                    else setForeground(Color.WHITE);
                } else {
                    setForeground(Color.WHITE);
                }

                if (isSelected) setBackground(new Color(60,60,60));
                else setBackground(row % 2 == 0 ? new Color(30,30,30) : new Color(40,40,40));

                return c;
            }
        });
    }

    // --- MÉTODOS DE EXPORTACIÓN (CSV y PDF) ---
    private void exportarCSV() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile() + ".csv")) {
                for(int i=0; i<table.getColumnCount(); i++) fw.write(table.getColumnName(i) + ",");
                fw.write("\n");
                for(int i=0; i<table.getRowCount(); i++) {
                    for(int j=0; j<table.getColumnCount(); j++) fw.write(table.getValueAt(i, j) + ",");
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(this, "CSV Exportado.");
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    private void exportToAestheticPDF() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
            String name = "Reporte_" + System.currentTimeMillis() + ".pdf";
            PdfWriter.getInstance(doc, new FileOutputStream(name));
            doc.open();

            // Usamos nombres completos para evitar el error de ambigüedad
            com.itextpdf.text.Font fTitle = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 22, com.itextpdf.text.Font.BOLD);

            // Header con Logo
            PdfPTable hTable = new PdfPTable(2);
            hTable.setWidthPercentage(100); hTable.setWidths(new float[]{1f, 4f});

            PdfPCell cLogo = new PdfPCell();
            cLogo.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); // <--- SOLUCIÓN ERROR AMBIGUO
            try {
                // Carga logo
                com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(getClass().getResource("/images/logo.png"));
                img.scaleToFit(100, 60);
                cLogo.addElement(img);
            } catch(Exception e) { cLogo.addElement(new Paragraph("SICONI")); }
            hTable.addCell(cLogo);

            PdfPCell cTxt = new PdfPCell(new Paragraph("REPORTE DE AUDITORÍA\nGenerado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fTitle));
            cTxt.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            cTxt.setHorizontalAlignment(Element.ALIGN_RIGHT);
            hTable.addCell(cTxt);

            doc.add(hTable);
            doc.add(new Paragraph(" ")); // Espacio

            // Tabla PDF
            PdfPTable pTable = new PdfPTable(model.getColumnCount());
            pTable.setWidthPercentage(100);

            // Cabeceras
            for(int i=0; i<model.getColumnCount(); i++) {
                PdfPCell c = new PdfPCell(new Phrase(model.getColumnName(i)));
                c.setBackgroundColor(BaseColor.BLACK);
                c.setPadding(5);
                pTable.addCell(c); // El texto saldrá negro por defecto, si quieres blanco avísame
            }

            // Datos
            for(int i=0; i<model.getRowCount(); i++) {
                for(int j=0; j<model.getColumnCount(); j++) {
                    pTable.addCell(model.getValueAt(i,j).toString());
                }
            }
            doc.add(pTable);
            doc.close();

            Desktop.getDesktop().open(new File(name));

        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Error PDF: " + e.getMessage());
        }
    }
}