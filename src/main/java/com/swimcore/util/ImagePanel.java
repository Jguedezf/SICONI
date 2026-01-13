/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ImagePanel.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase utilitaria que extiende de `javax.swing.JPanel` para proporcionar capacidades
 * de renderizado de imágenes de fondo (Background Images).
 * * Características de Ingeniería de UI:
 * 1. Escalado Dinámico: Implementa lógica de dibujo que adapta las dimensiones de la
 * imagen a las dimensiones actuales del contenedor en tiempo real.
 * 2. Gestión de Recursos: Utiliza el ClassLoader para localizar activos gráficos
 * dentro del JAR o sistema de archivos de forma independiente del entorno.
 * 3. Sobreescritura del Pipeline Gráfico: Intercepta el método `paintComponent`
 * para inyectar la capa de imagen antes de que se dibujen los componentes hijos.
 *
 * PRINCIPIOS POO:
 * - HERENCIA: Especializa un `JPanel` estándar para añadir una propiedad de estado (`backgroundImage`).
 * - POLIMORFISMO: Sobreescritura (Override) de métodos protegidos de la superclase `JComponent`.
 * - ENCAPSULAMIENTO: Oculta la complejidad del manejo del contexto gráfico (`Graphics`).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Un JPanel personalizado con soporte para imágenes de fondo.
 * Diseñado para mejorar la experiencia de usuario (UX) mediante fondos decorativos.
 */
public class ImagePanel extends JPanel {

    // Almacenamiento en memoria del recurso gráfico
    private Image backgroundImage;

    /**
     * Constructor de la clase.
     * @param imagePath Ruta absoluta o relativa dentro del classpath (recursos).
     */
    public ImagePanel(String imagePath) {
        try {
            // Localización del recurso mediante la URL del sistema de archivos o JAR
            URL url = getClass().getResource(imagePath);
            if (url != null) {
                // Carga del mapa de bits en el objeto Image
                backgroundImage = new ImageIcon(url).getImage();
            } else {
                // Registro de error en el flujo de diagnóstico estándar (System.err)
                System.err.println("Imagen de fondo no encontrada: " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configuración de Layout Manager por defecto para permitir la adición de hijos
        setLayout(new BorderLayout());
    }

    /**
     * Método central del ciclo de renderizado de Swing.
     * Se invoca automáticamente cuando el componente requiere actualización visual.
     * @param g El contexto gráfico proporcionado por el motor de dibujo de Java.
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Ejecución obligatoria de la superclase para mantener la integridad del fondo
        super.paintComponent(g);

        if (backgroundImage != null) {
            // Algoritmo de estiramiento: Dibuja la imagen escalándola desde (0,0)
            // hasta las coordenadas (getWidth, getHeight) del panel.
            // Esto garantiza que la imagen cubra siempre el 100% del área visible.
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}