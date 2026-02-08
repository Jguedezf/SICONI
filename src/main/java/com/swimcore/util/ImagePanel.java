/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ImagePanel.java
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase utilitaria que extiende la funcionalidad base de javax.swing.JPanel para
 * permitir la renderización de mapas de bits como fondo de contenedor.
 * Implementa un algoritmo de escalado dinámico que garantiza el ajuste de la
 * imagen a la geometría del componente en tiempo real.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * [UTILIDAD - INTERFAZ] Contenedor especializado con soporte para Background Imaging.
 * [POO - HERENCIA] Especializa la clase JPanel para añadir un estado de imagen persistente.
 * [REQUERIMIENTO NO FUNCIONAL] Estética y Usabilidad: Provee una base visual inmersiva
 * para los módulos del sistema.
 */
public class ImagePanel extends JPanel {

    // ========================================================================================
    //                                  ATRIBUTOS (ENCAPSULAMIENTO)
    // ========================================================================================

    // Referencia al objeto de imagen cargado en el heap de la JVM.
    private Image backgroundImage;

    // ========================================================================================
    //                                  CONSTRUCTOR
    // ========================================================================================

    /**
     * Inicializa el panel realizando la carga del recurso gráfico desde el classpath.
     * Utiliza la API de ClassLoader para asegurar la portabilidad del activo en
     * entornos empaquetados (JAR).
     * * @param imagePath Ruta relativa del recurso (ej: "/images/bg.png").
     */
    public ImagePanel(String imagePath) {
        try {
            // Localización del activo mediante URL para compatibilidad multiplataforma.
            URL url = getClass().getResource(imagePath);
            if (url != null) {
                // Instanciación del icono y extracción de la instancia de imagen.
                backgroundImage = new ImageIcon(url).getImage();
            } else {
                // Manejo de error en flujo de diagnóstico (Standard Error).
                System.err.println("Imagen de fondo no encontrada: " + imagePath);
            }
        } catch (Exception e) {
            // Captura de excepciones en tiempo de ejecución durante la carga de I/O.
            e.printStackTrace();
        }

        // Definición de BorderLayout para permitir la composición de componentes hijos
        // manteniendo el orden de renderizado (Fondo -> Componentes).
        setLayout(new BorderLayout());
    }

    // ========================================================================================
    //                                  MOTOR DE RENDERIZADO (SWING PIPELINE)
    // ========================================================================================

    /**
     * [POO - POLIMORFISMO] Sobreescritura del método paintComponent.
     * Intercepta el flujo de dibujo nativo de Java para inyectar la capa de imagen.
     * Implementa un escalado proporcional al ancho y alto del contenedor (Stretch logic).
     * * @param g Contexto gráfico (Graphics) inyectado por el motor de dibujo.
     */
    @Override
    protected void paintComponent(Graphics g) {
        // [IMPORTANTE] Invocación a la superclase para procesar propiedades nativas
        // (como la opacidad y el borrado del buffer previo).
        super.paintComponent(g);

        if (backgroundImage != null) {
            // [API AWT] Renderizado de la imagen utilizando las dimensiones dinámicas
            // obtenidas mediante los métodos de acceso getWidth() y getHeight().
            // El observador (this) garantiza que la imagen se redibuje si cambia de estado.
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}