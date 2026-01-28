/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - DAYANA GUEDEZ SWIMWEAR
 * ARCHIVO: WelcomeView.java
 * VERSIÓN: 3.3.0 (i18n & Layout Fix)
 * DESCRIPCIÓN: Splash Screen internacionalizado. Se mejora la legibilidad
 * de los textos inferiores y se ajusta la posición del título.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import com.swimcore.util.LanguageManager; // <-- Importado
import com.swimcore.util.SoundManager;
import com.swimcore.view.components.GoldenTitle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class WelcomeView extends JWindow {

    private boolean skipped = false;
    private Thread transitionThread;

    public WelcomeView() {
        setSize(960, 540);
        setLocationRelativeTo(null);

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

        // 1. TÍTULO PRINCIPAL (Usamos GoldenTitle porque es grande y se ve bien)
        // Subido a Y=30 para dar más espacio
        GoldenTitle lblBienvenida = new GoldenTitle(LanguageManager.get("welcome.title"), 42);
        lblBienvenida.setBounds(0, 30, width, 60);
        textPanel.add(lblBienvenida);

        // 2. TEXTOS INFERIORES (Usamos JLabel normal para legibilidad)
        // Fuente Segoe UI, color dorado sólido, sin sombras extrañas.

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
        lblSkip.setForeground(new Color(200, 200, 200)); // Gris claro para el aviso
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

        transitionThread = new Thread(() -> {
            try { Thread.sleep(20000); } catch (InterruptedException e) {}
            if (!skipped) abrirDashboard();
        });
        transitionThread.start();

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

    // Método helper para crear etiquetas limpias y legibles
    private JLabel createCleanLabel(String text, int size, boolean bold) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(new Color(255, 215, 0)); // Dorado sólido (Gold)
        // Añadimos una sombra muy sutil (negra simple) para contraste con el fondo
        // usando HTML básico de Swing si es necesario, pero el color sólido suele bastar.
        return label;
    }

    private void abrirDashboard() {
        dispose();
        SwingUtilities.invokeLater(() -> new DashboardView().setVisible(true));
    }
}