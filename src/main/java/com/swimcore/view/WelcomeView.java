/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - DAYANA GUEDEZ SWIMWEAR
 * ARCHIVO: WelcomeView.java
 * DESCRIPCIÓN: Splash Screen V3.2 (Layout Corregido - Texto Arriba y Abajo)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.util.SoundManager;
import com.swimcore.view.components.GoldenTitle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class WelcomeView extends JWindow {

    // Variable para controlar si el usuario saltó la intro
    private boolean skipped = false;
    private Thread transitionThread;

    public WelcomeView() {
        // Tamaño HD
        setSize(960, 540);
        setLocationRelativeTo(null);

        // 1. INICIAR AUDIO
        try {
            SoundManager.getInstance().playLoginSuccess();
        } catch (Exception e) {
            System.err.println("Error de audio: " + e.getMessage());
        }

        // 2. PANEL DE FONDO
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    URL url = getClass().getResource("/images/fondo_dashboard.png");
                    if (url != null) {
                        Image img = new ImageIcon(url).getImage();
                        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g.setColor(new Color(20, 20, 20));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                } catch (Exception e) {}
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        // 3. CAPA DE TEXTO (LAYOUT DISTRIBUIDO)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(null);
        textPanel.setOpaque(false);

        int width = getWidth();

        // --- A. BIENVENIDA (ARRIBA - Y=60) ---
        // Se coloca en la parte superior para dejar el centro libre al logo
        GoldenTitle lblBienvenida = new GoldenTitle("TE DAMOS LA BIENVENIDA", 42);
        lblBienvenida.setBounds(0, 60, width, 50);
        textPanel.add(lblBienvenida);

        // --- B. BLOQUE INFERIOR (DESCRIPCIÓN) ---

        // Título del Sistema (Y=400)
        GoldenTitle lblDefinicion = new GoldenTitle("SISTEMA DE CONTROL DE NEGOCIO E INVENTARIO", 18);
        lblDefinicion.setBounds(0, 400, width, 30);
        textPanel.add(lblDefinicion);

        // Slogan Línea 1 (Y=435)
        GoldenTitle lblSlogan1 = new GoldenTitle("Solución Tecnológica de control de negocio e inventario,", 14);
        lblSlogan1.setBounds(0, 435, width, 20);
        textPanel.add(lblSlogan1);

        // Slogan Línea 2 (Y=455)
        GoldenTitle lblSlogan2 = new GoldenTitle("optimizada para la gestión administrativa de Pequeñas y Medianas Empresas (PyMEs)", 14);
        lblSlogan2.setBounds(0, 455, width, 20);
        textPanel.add(lblSlogan2);

        // Aviso de Click (Y=490 - Al final)
        JLabel lblSkip = new JLabel("(Click para iniciar)", SwingConstants.CENTER);
        lblSkip.setForeground(new Color(212, 175, 55));
        lblSkip.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSkip.setBounds(0, 490, width, 20);
        textPanel.add(lblSkip);

        content.add(textPanel, BorderLayout.CENTER);

        // 4. BARRA DE CARGA
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBackground(new Color(10, 10, 10));
        progressBar.setForeground(new Color(212, 175, 55));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(getWidth(), 6));
        content.add(progressBar, BorderLayout.SOUTH);

        // 5. LÓGICA DE TRANSICIÓN
        transitionThread = new Thread(() -> {
            try {
                Thread.sleep(20000); // 20 Segundos de música
            } catch (InterruptedException e) {
            }

            if (!skipped) {
                abrirDashboard();
            }
        });
        transitionThread.start();

        // 6. EVENTO CLICK (SALTAR)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!skipped) {
                    skipped = true;
                    transitionThread.interrupt();
                    abrirDashboard();
                }
            }
        });
    }

    private void abrirDashboard() {
        dispose(); // Cerrar bienvenida
        SwingUtilities.invokeLater(() -> new DashboardView().setVisible(true));
    }
}