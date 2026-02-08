/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * AUTORA: Johanna Gabriela Guédez Flores
 * PROFESORA: Ing. Dubraska Roca
 * ASIGNATURA: Técnicas de Programación III
 * * ARCHIVO: WelcomeView.java
 * VERSIÓN: 4.0.0 (ZERO LAG - BACKGROUND LOADING)
 * FECHA: 06 de Febrero de 2026
 * HORA: 06:30 PM (Hora de Venezuela)
 * * DESCRIPCIÓN TÉCNICA:
 * Pantalla de bienvenida (Splash Screen) con arquitectura de pre-carga.
 * Implementa Hilos (Threads) para la instanciación asíncrona del Dashboard,
 * optimizando el tiempo de respuesta percibido por el usuario.
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

/**
 * [VISTA] Clase que gestiona la pantalla de carga inicial.
 * [POO - HERENCIA] Extiende de JWindow para la visualización de una ventana sin bordes (Splash).
 */
public class WelcomeView extends JWindow {

    // [CONCURRENCIA] Variable 'volatile' para garantizar la visibilidad de cambios
    // de estado entre el hilo de la UI y el hilo de carga (Sincronización de Memoria).
    private volatile boolean skipped = false;

    // [HILO] Referencia al proceso de ejecución en segundo plano.
    private Thread transitionThread;

    // Constante de duración del audio introductorio (milisegundos)
    private final long MUSIC_DURATION = 20000;

    /**
     * Constructor de la vista. Se encarga de la configuración de UI e inicio del hilo de carga.
     */
    public WelcomeView() {
        setSize(960, 540);
        setLocationRelativeTo(null);

        // Reproducción de audio (Manejo de excepciones para recursos externos)
        try {
            SoundManager.getInstance().playLoginSuccess();
        } catch (Exception e) {}

        // [POO - CLASE ANÓNIMA] Personalización del panel para el renderizado de la imagen de fondo.
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

        // 1. TÍTULO PRINCIPAL (Componente personalizado GoldenTitle)
        GoldenTitle lblBienvenida = new GoldenTitle(LanguageManager.get("welcome.title"), 42);
        lblBienvenida.setBounds(0, 30, width, 60);
        textPanel.add(lblBienvenida);

        // 2. ELEMENTOS INFORMATIVOS INFERIORES
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

        // Barra de progreso indeterminada (Feedback visual de actividad)
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBackground(new Color(10, 10, 10));
        progressBar.setForeground(new Color(212, 175, 55));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(getWidth(), 6));
        content.add(progressBar, BorderLayout.SOUTH);

        // --- ESTRATEGIA DE OPTIMIZACIÓN (BACKGROUND LOADING) ---
        // Se instancia un hilo independiente para la carga pesada del Dashboard,
        // evitando el bloqueo del hilo de despacho de eventos (EDT).
        transitionThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            DashboardView preloadedDashboard = null;

            try {
                // 1. INSTANCIACIÓN EN SEGUNDO PLANO
                // Se inicializa el Dashboard mientras se visualiza la imagen de bienvenida.
                // Esta técnica elimina la latencia visual (pantalla negra) en la transición.
                preloadedDashboard = new DashboardView();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 2. CÁLCULO DE TIEMPO TRANSCURRIDO
            long timeElapsed = System.currentTimeMillis() - startTime;
            long timeLeft = MUSIC_DURATION - timeElapsed;

            // 3. SINCRONIZACIÓN TEMPORAL
            // Se mantiene la espera únicamente si la carga finaliza antes que la duración del audio.
            if (timeLeft > 0) {
                long endTime = System.currentTimeMillis() + timeLeft;
                // Ciclo de espera activo con verificación de bandera de interrupción.
                while (System.currentTimeMillis() < endTime && !skipped) {
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                }
            }

            // 4. TRANSICIÓN A LA VISTA PRINCIPAL
            // Se procede al despliegue del objeto previamente instanciado.
            abrirDashboard(preloadedDashboard);
        });
        transitionThread.start();

        // EVENTO DE INTERRUPCIÓN MANUAL (SKIP)
        // Permite al usuario omitir la espera mediante interacción directa.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!skipped) {
                    skipped = true;
                    // La modificación de esta bandera interrumpe el ciclo 'while' del hilo secundario.
                }
            }
        });
    }

    // Método auxiliar (Factory Method) para la creación estandarizada de etiquetas.
    private JLabel createCleanLabel(String text, int size, boolean bold) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(new Color(255, 215, 0));
        return label;
    }

    // Método encargado de realizar la transición visual al Dashboard.
    private void abrirDashboard(DashboardView preloadedDash) {
        // [CONCURRENCIA SWING] Se asegura que la manipulación de componentes UI ocurra en el EDT.
        SwingUtilities.invokeLater(() -> {
            dispose(); // Liberación de recursos de la ventana actual.

            // Lógica de respaldo (Fallback):
            // Si la precarga falló, se instancia un nuevo objeto en el momento.
            // Si fue exitosa, se utiliza el objeto cargado en memoria.
            if (preloadedDash != null) {
                preloadedDash.setVisible(true);
            } else {
                new DashboardView().setVisible(true);
            }
        });
    }
}