/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - DAYANA GUEDEZ SWIMWEAR
 * ARCHIVO: WelcomeView.java
 * VERSIÓN: 4.0.0 (ZERO LAG - BACKGROUND LOADING)
 * DESCRIPCIÓN: Splash Screen con pre-carga inteligente del Dashboard.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.util.LanguageManager;
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.GoldenTitle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class WelcomeView extends JWindow {

    private volatile boolean skipped = false; // 'volatile' para sincronización de hilos
    private Thread transitionThread;

    // TIEMPO TOTAL DE LA MÚSICA (20 Segundos)
    // Si cortas la música después, solo cambia este número a 10000
    private final long MUSIC_DURATION = 20000;

    public WelcomeView() {
        setSize(960, 540);
        setLocationRelativeTo(null);

        // Reproducir música
        try {
            SoundManager.getInstance().playLoginSuccess();
        } catch (Exception e) {}

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

        JPanel textPanel = new JPanel();
        textPanel.setLayout(null);
        textPanel.setOpaque(false);

        int width = getWidth();

        // 1. TÍTULO PRINCIPAL
        GoldenTitle lblBienvenida = new GoldenTitle(LanguageManager.get("welcome.title"), 42);
        lblBienvenida.setBounds(0, 30, width, 60);
        textPanel.add(lblBienvenida);

        // 2. TEXTOS INFERIORES
        JLabel lblDefinicion = createCleanLabel(LanguageManager.get("welcome.system"), 18, true);
        lblDefinicion.setBounds(0, 400, width, 30);
        textPanel.add(lblDefinicion);

        JLabel lblSlogan1 = createCleanLabel(LanguageManager.get("welcome.slogan1"), 14, false);
        lblSlogan1.setBounds(0, 435, width, 20);
        textPanel.add(lblSlogan1);

        JLabel lblSlogan2 = createCleanLabel(LanguageManager.get("welcome.slogan2"), 14, false);
        lblSlogan2.setBounds(0, 455, width, 20);
        textPanel.add(lblSlogan2);

        JLabel lblSkip = new JLabel(LanguageManager.get("welcome.skip"), SwingConstants.CENTER);
        lblSkip.setForeground(new Color(200, 200, 200));
        lblSkip.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblSkip.setBounds(0, 495, width, 20);
        textPanel.add(lblSkip);

        content.add(textPanel, BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBackground(new Color(10, 10, 10));
        progressBar.setForeground(new Color(212, 175, 55));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(getWidth(), 6));
        content.add(progressBar, BorderLayout.SOUTH);

        // --- AQUÍ ESTÁ LA MAGIA (HILO DE CARGA INTELIGENTE) ---
        transitionThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            DashboardView preloadedDashboard = null;

            try {
                // 1. CARGAMOS EL DASHBOARD EN SILENCIO
                // Mientras el usuario ve la imagen, esto trabaja por detrás.
                // Esto elimina el "hueco" negro al final.
                preloadedDashboard = new DashboardView();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 2. CALCULAMOS EL TIEMPO RESTANTE
            long timeElapsed = System.currentTimeMillis() - startTime;
            long timeLeft = MUSIC_DURATION - timeElapsed;

            // 3. ESPERAMOS SOLO SI SOBRA TIEMPO DE MÚSICA
            if (timeLeft > 0) {
                long endTime = System.currentTimeMillis() + timeLeft;
                // Bucle de espera que revisa si diste click a "Saltar"
                while (System.currentTimeMillis() < endTime && !skipped) {
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                }
            }

            // 4. ABRIMOS EL DASHBOARD (Que ya está listo en memoria)
            abrirDashboard(preloadedDashboard);
        });
        transitionThread.start();

        // EVENTO DE SALTAR (CLICK)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!skipped) {
                    skipped = true;
                    // Al poner skipped en true, el bucle while del hilo se rompe
                    // y pasa directo a abrirDashboard.
                }
            }
        });
    }

    private JLabel createCleanLabel(String text, int size, boolean bold) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(new Color(255, 215, 0));
        return label;
    }

    // Método modificado para recibir el Dashboard ya cargado
    private void abrirDashboard(DashboardView preloadedDash) {
        // Ejecutamos la transición visual en el hilo de Swing para evitar parpadeos
        SwingUtilities.invokeLater(() -> {
            dispose(); // Cerramos bienvenida

            // Si por alguna razón falló la precarga (muy raro), creamos uno nuevo.
            // Si todo salió bien, usamos el que ya cargamos en memoria.
            if (preloadedDash != null) {
                preloadedDash.setVisible(true);
            } else {
                new DashboardView().setVisible(true);
            }
        });
    }
}