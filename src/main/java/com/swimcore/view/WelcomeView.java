/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: WelcomeView.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Splash Screen Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Pantalla de Carga (Splash Screen) que se muestra después de una autenticación exitosa.
 * Su propósito es mejorar la experiencia de usuario (UX) y reforzar la identidad de marca
 * mientras se cargan los módulos principales en segundo plano.
 *
 * PRINCIPIOS POO APLICADOS:
 * - HERENCIA: Extiende de JWindow para una ventana sin bordes ni controles.
 * - COMPOSICIÓN: Contiene un JPanel personalizado para el fondo y una JProgressBar.
 * - ABSTRACCIÓN: Oculta la complejidad del manejo de hilos (Threads) para la transición.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.view;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Ventana de Bienvenida (Splash Screen).
 * Proporciona una transición visual elegante entre el Login y el Dashboard.
 */
public class WelcomeView extends JWindow {

    /**
     * Constructor.
     * ENTRADA: Ninguna.
     * PROCESO: Renderiza la imagen de fondo, muestra una barra de carga animada
     *          y utiliza un hilo para temporizar la transición al Dashboard.
     * SALIDA: Ventana de bienvenida visible por 4 segundos.
     */
    public WelcomeView() {
        // Configuración del tamaño de la ventana (ajusta a la proporción de tu imagen)
        setSize(960, 540);
        setLocationRelativeTo(null); // Centrado en pantalla

        // Panel de Fondo con tu imagen corporativa
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    // Carga la imagen desde la carpeta de recursos
                    URL url = getClass().getResource("/images/fondo_dashboard.png");
                    if (url != null) {
                        Image img = new ImageIcon(url).getImage();
                        // Dibuja la imagen para que cubra todo el panel
                        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        // Si no encuentra la imagen, muestra un fondo negro sólido
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                } catch (Exception e) {
                    // Manejo de error silencioso para no interrumpir al usuario
                }
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        // Barra de Carga con estilo Fucsia Neón
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); // Animación infinita (estilo "cargando...")
        progressBar.setBackground(new Color(20, 20, 20));
        progressBar.setForeground(new Color(220, 0, 115)); // Color Fucsia SICONI
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(getWidth(), 5)); // Barra delgada y elegante

        content.add(progressBar, BorderLayout.SOUTH);

        // Lógica de Transición Asíncrona
        // Un nuevo hilo se encarga de la espera para no congelar la interfaz
        new Thread(() -> {
            try {
                // Espera 4 segundos para que se aprecie la imagen y la música
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Cierra esta ventana de bienvenida
            dispose();

            // Llama a la apertura del Dashboard principal de forma segura
            SwingUtilities.invokeLater(() -> new DashboardView().setVisible(true));
        }).start();
    }
}