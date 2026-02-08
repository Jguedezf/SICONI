/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * AUTORA: Johanna Gabriela Guédez Flores
 * PROFESORA: Ing. Dubraska Roca
 * ASIGNATURA: Técnicas de Programación III
 * * ARCHIVO: SoundManager.java
 * VERSIÓN: 2.7.0 (Core Audio Engine)
 * FECHA: 06 de Febrero de 2026
 * HORA: 04:30 PM (Hora de Venezuela)
 * * DESCRIPCIÓN TÉCNICA:
 * Motor de gestión de recursos de audio del sistema. Centraliza la ejecución
 * de efectos sonoros para la interfaz de usuario, garantizando una respuesta
 * auditiva no bloqueante mediante el uso de hilos secundarios.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * [UTILIDAD - AUDIO] Clase encargada de la administración y reproducción de sonidos.
 * [PATRÓN DE DISEÑO: SINGLETON] Implementa una instancia única global para evitar
 * el consumo excesivo de memoria y colisiones en el hardware de audio.
 * [REQUERIMIENTO NO FUNCIONAL] Usabilidad: Provee feedback auditivo al usuario (UX).
 */
public class SoundManager {

    // ========================================================================================
    //                                  ATRIBUTOS Y CONSTANTES
    // ========================================================================================

    // Rutas relativas a los recursos de audio integrados en el paquete (JAR resources).
    private static final String SOUND_HOVER = "/sounds/hover.wav";
    private static final String SOUND_CLICK = "/sounds/click.wav";
    private static final String SOUND_ERROR = "/sounds/error.wav";
    private static final String SOUND_LOGIN = "/sounds/login_success.wav";

    // Instancia estática única (Patrón Singleton).
    private static SoundManager instance;

    // Estado de control para la supresión de audio (Mute logic).
    private boolean muted = false;

    // ========================================================================================
    //                                  CONSTRUCTOR Y ACCESO
    // ========================================================================================

    /**
     * Constructor privado para restringir la instanciación externa.
     * Garantiza el cumplimiento del patrón Singleton.
     */
    private SoundManager() {}

    /**
     * Punto de acceso global a la instancia de la clase.
     * Implementa sincronización (synchronized) para garantizar la seguridad en hilos (Thread-Safe).
     * @return La instancia única de SoundManager.
     */
    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // ========================================================================================
    //                                  MÉTODOS DE REPRODUCCIÓN (API)
    // ========================================================================================

    // Ejecuta el sonido de transición de puntero (Hover).
    public void playHover() { playSound(SOUND_HOVER); }

    // Ejecuta el sonido de confirmación de acción (Click).
    public void playClick() { playSound(SOUND_CLICK); }

    // Ejecuta el sonido de advertencia o falla (Error).
    public void playError() { playSound(SOUND_ERROR); }

    // Ejecuta el sonido de éxito en el proceso de autenticación.
    public void playLoginSuccess() { playSound(SOUND_LOGIN); }

    /**
     * Modifica el estado de reproducción del motor de audio.
     * @param muted Verdadero para silenciar, falso para activar.
     */
    public void setMuted(boolean muted) { this.muted = muted; }

    // ========================================================================================
    //                                  LÓGICA INTERNA DE AUDIO
    // ========================================================================================

    /**
     * [CONCURRENCIA] Método interno para el procesamiento de archivos WAV.
     * Instancia un nuevo hilo (Thread) por cada ejecución para evitar que la carga
     * y reproducción de audio bloqueen el hilo de despacho de eventos (EDT) de la UI.
     * * @param path Ruta del recurso de audio a procesar.
     */
    private void playSound(String path) {
        if (muted) return; // Validación de estado

        // Ejecución asíncrona mediante expresión Lambda
        new Thread(() -> {
            try {
                URL url = getClass().getResource(path);
                if (url != null) {
                    // [API JAVA SOUND] Recuperación y apertura del flujo de audio.
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                }
            } catch (Exception e) {
                // Manejo de error silencioso para no interrumpir el flujo de la aplicación.
            }
        }).start();
    }
}