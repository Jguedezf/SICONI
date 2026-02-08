/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * ARCHIVO: CurrencySettingsDialog.java
 * VERSIÓN: 2.1 (UX Fix: Auto-Select Text)
 * FECHA: 06 de Febrero de 2026
 * HORA: 09:30 PM (Hora de Venezuela)
 * DESCRIPCIÓN TÉCNICA:
 * Diálogo modal para la parametrización global del sistema monetario.
 * Permite establecer la tasa de cambio (BCV) y la moneda base de referencia,
 * actualizando el estado global del sistema a través del CurrencyManager.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view.dialogs;

import com.swimcore.util.CurrencyManager;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.SoftButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

/**
 * [VISTA - CONFIGURACIÓN] Clase que gestiona la interfaz de control cambiario.
 * [POO - HERENCIA] Extiende de JDialog para garantizar la atención del usuario (Modalidad).
 * * FUNCIONALIDAD: Ajuste de parámetros económicos transversales a todo el sistema.
 */
public class CurrencySettingsDialog extends JDialog {

    // [ENCAPSULAMIENTO] Componentes de interfaz para entrada de datos.
    // Se definen como privados para proteger el acceso directo desde otras clases.
    private JTextField txtRate;
    private JComboBox<String> cmbCurrency;

    /**
     * Constructor de la clase. Inicializa la interfaz y carga los valores actuales.
     * @param owner Ventana padre (Frame) sobre la cual se centra el diálogo.
     */
    public CurrencySettingsDialog(Frame owner) {
        super(owner, "Configuración de Tasa de Cambio", true);
        setSize(400, 350);
        setLocationRelativeTo(owner);

        // Estilización del borde de la ventana (Identidad Visual Corporativa)
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(220, 0, 115), 2));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. TÍTULO DE LA SECCIÓN
        JLabel lblTitle = new JLabel("CONTROL CAMBIARIO", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // 2. FORMULARIO DE DATOS
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        centerPanel.setOpaque(false);

        JLabel lblMoneda = new JLabel("Moneda Principal:");
        lblMoneda.setForeground(Color.GRAY);
        lblMoneda.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Selector de Moneda Base
        cmbCurrency = new JComboBox<>(new String[]{"DÓLAR ($)", "EURO (€)", "BOLÍVAR (Bs.)"});
        cmbCurrency.setBackground(new Color(45, 45, 45));
        cmbCurrency.setForeground(Color.WHITE);
        cmbCurrency.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // [PATRÓN SINGLETON] Recuperación del estado actual desde el gestor global.
        // Se consulta al CurrencyManager para pre-seleccionar la configuración vigente.
        cmbCurrency.setSelectedIndex(CurrencyManager.getMode());

        JLabel lblTasa = new JLabel("Tasa de Cambio (BCV):");
        lblTasa.setForeground(Color.GRAY);
        lblTasa.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Campo de entrada de la tasa (Formato numérico)
        txtRate = new JTextField(String.format(Locale.US, "%.2f", CurrencyManager.getTasa()));
        txtRate.setFont(new Font("Segoe UI", Font.BOLD, 28));
        txtRate.setHorizontalAlignment(JTextField.CENTER);
        txtRate.setBackground(new Color(45, 45, 45));
        txtRate.setForeground(Color.WHITE);
        txtRate.setCaretColor(new Color(220, 0, 115));
        txtRate.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        centerPanel.add(lblMoneda);
        centerPanel.add(cmbCurrency);
        centerPanel.add(lblTasa);
        centerPanel.add(txtRate);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. BOTÓN DE ACCIÓN (Persistencia)
        SoftButton btnSave = new SoftButton(null);
        btnSave.setText("GUARDAR CONFIGURACIÓN");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setForeground(new Color(0, 255, 128));
        btnSave.setPreferredSize(new Dimension(80, 50));

        // [EVENT LISTENER] Asignación de comportamiento al botón mediante expresión Lambda.
        btnSave.addActionListener(e -> saveRate());

        mainPanel.add(btnSave, BorderLayout.SOUTH);

        // [MANEJO DE EVENTOS - UX]
        // Se implementa un listener para seleccionar automáticamente el texto al abrir.
        // Esto facilita la edición rápida sin necesidad de borrar manualmente el valor anterior.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                txtRate.requestFocus();
                txtRate.selectAll(); // Esto sombrea todo el texto al abrir
            }
        });
        // ----------------------------------------

        setContentPane(mainPanel);
    }

    /**
     * [LÓGICA DE NEGOCIO]
     * Método encargado de validar, parsear y persistir la nueva configuración.
     * Actualiza el estado del Singleton CurrencyManager y maneja excepciones de formato.
     */
    private void saveRate() {
        try {
            // Normalización de formato decimal (coma por punto) para evitar errores de parseo.
            double newRate = Double.parseDouble(txtRate.getText().replace(',', '.'));

            if (newRate > 0) {
                int modo = cmbCurrency.getSelectedIndex();
                String simbolo = (modo == 0) ? "$" : (modo == 1) ? "€" : "Bs.";

                // [PATRÓN SINGLETON] Actualización global del sistema.
                // Los cambios se reflejarán instantáneamente en todas las vistas que consuman este gestor.
                CurrencyManager.setConfig(newRate, simbolo, modo);

                try { SoundManager.getInstance().playClick(); } catch (Exception ex){}

                JOptionPane.showMessageDialog(this, "Configuración actualizada.");
                dispose(); // Cierre del diálogo y liberación de recursos.
            } else {
                try { SoundManager.getInstance().playError(); } catch (Exception ex){}
                JOptionPane.showMessageDialog(this, "La tasa debe ser positiva.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            // Manejo de errores de entrada de usuario (Input Validation).
            try { SoundManager.getInstance().playError(); } catch (Exception ex){}
            JOptionPane.showMessageDialog(this, "Número inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}