/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: CurrencySettingsDialog.java
 * DESCRIPCIÓN: Diálogo modal para la configuración global de moneda.
 * - CAMBIOS V2.1: Se agrega auto-selección de texto (selectAll) al abrir.
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

public class CurrencySettingsDialog extends JDialog {

    private JTextField txtRate;
    private JComboBox<String> cmbCurrency;

    public CurrencySettingsDialog(Frame owner) {
        super(owner, "Configuración de Tasa de Cambio", true);
        setSize(400, 350);
        setLocationRelativeTo(owner);

        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(220, 0, 115), 2));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. TÍTULO
        JLabel lblTitle = new JLabel("CONTROL CAMBIARIO", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // 2. PANEL CENTRAL
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        centerPanel.setOpaque(false);

        JLabel lblMoneda = new JLabel("Moneda Principal:");
        lblMoneda.setForeground(Color.GRAY);
        lblMoneda.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cmbCurrency = new JComboBox<>(new String[]{"DÓLAR ($)", "EURO (€)", "BOLÍVAR (Bs.)"});
        cmbCurrency.setBackground(new Color(45, 45, 45));
        cmbCurrency.setForeground(Color.WHITE);
        cmbCurrency.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cmbCurrency.setSelectedIndex(CurrencyManager.getMode());

        JLabel lblTasa = new JLabel("Tasa de Cambio (BCV):");
        lblTasa.setForeground(Color.GRAY);
        lblTasa.setFont(new Font("Segoe UI", Font.BOLD, 12));

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

        // 3. BOTÓN
        SoftButton btnSave = new SoftButton(null);
        btnSave.setText("GUARDAR CONFIGURACIÓN");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setForeground(new Color(0, 255, 128));
        btnSave.setPreferredSize(new Dimension(80, 50));
        btnSave.addActionListener(e -> saveRate());

        mainPanel.add(btnSave, BorderLayout.SOUTH);

        // --- AQUÍ ESTÁ EL TRUCO PARA SOMBREAR ---
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

    private void saveRate() {
        try {
            double newRate = Double.parseDouble(txtRate.getText().replace(',', '.'));

            if (newRate > 0) {
                int modo = cmbCurrency.getSelectedIndex();
                String simbolo = (modo == 0) ? "$" : (modo == 1) ? "€" : "Bs.";

                CurrencyManager.setConfig(newRate, simbolo, modo);

                try { SoundManager.getInstance().playClick(); } catch (Exception ex){}

                JOptionPane.showMessageDialog(this, "Configuración actualizada.");
                dispose();
            } else {
                try { SoundManager.getInstance().playError(); } catch (Exception ex){}
                JOptionPane.showMessageDialog(this, "La tasa debe ser positiva.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            try { SoundManager.getInstance().playError(); } catch (Exception ex){}
            JOptionPane.showMessageDialog(this, "Número inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}