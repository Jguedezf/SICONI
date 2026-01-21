/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: SoundManager.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.1.0 (Login Ambient Added)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Gestor de Feedback Auditivo (Singleton).
 * Proporciona la capa de experiencia de usuario (UX) mediante efectos de sonido.
 * Incluye soporte para pistas de ambiente largas (Login) y efectos cortos (UI).
 *
 * PRINCIPIOS DE PROGRAMACIÓN ORIENTADA A OBJETOS (POO):
 * 1. ENCAPSULAMIENTO: Control estricto de la instancia única mediante constructor privado.
 * 2. ABSTRACCIÓN: Oculta la complejidad de la librería `javax.sound` detrás de métodos simples como `playClick`.
 *
 * PATRONES DE DISEÑO:
 * - Singleton: Garantiza que solo exista un manejador de audio en toda la ejecución
 *   para evitar superposición de hilos o consumo excesivo de memoria.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Motor de Audio para la Interfaz Gráfica.
 * Gestiona los efectos de sonido 'Luxury' del sistema de forma asíncrona.
 */
public class SoundManager {

    // --- ATRIBUTOS CONSTANTES (RUTAS DE RECURSOS) ---
    private static final String SOUND_HOVER = "/sounds/hover.wav";
    private static final String SOUND_CLICK = "/sounds/click.wav";
    private static final String SOUND_ERROR = "/sounds/error.wav";
    // TU NUEVO SONIDO DE LOGIN (Echo Hit - Ambient Transition)
    private static final String SOUND_LOGIN = "/sounds/login_success.wav";

    // --- ATRIBUTOS DE INSTANCIA ---
    private static SoundManager instance; // Instancia estática única
    private boolean muted = false;        // Estado global del audio

    /**
     * Constructor Privado.
     * Evita la instanciación directa desde otras clases.
     */
    private SoundManager() {}

    /**
     * Método de Acceso Global (Singleton).
     * ENTRADA: N/A.
     * PROCESO: Verifica si la instancia existe; si no, la crea (Lazy Initialization).
     * SALIDA: La instancia única de SoundManager.
     */
    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // --- MÉTODOS PÚBLICOS DE INTERACCIÓN ---

    /**
     * Reproduce el sonido de interacción "Hover".
     * ENTRADA: Ninguna.
     * PROCESO: Invoca al motor de audio con la ruta del sonido corto.
     * SALIDA: Audio (0.1s).
     */
    public void playHover() {
        playSound(SOUND_HOVER);
    }

    /**
     * Reproduce el sonido de confirmación "Click".
     * ENTRADA: Ninguna.
     * PROCESO: Invoca al motor de audio con la ruta del sonido de acción.
     * SALIDA: Audio (0.3s).
     */
    public void playClick() {
        playSound(SOUND_CLICK);
    }

    /**
     * Reproduce el sonido de alerta o error.
     * ENTRADA: Ninguna.
     * PROCESO: Invoca al motor de audio con la ruta del sonido de fallo.
     * SALIDA: Audio de alerta.
     */
    public void playError() {
        playSound(SOUND_ERROR);
    }

    /**
     * Reproduce el sonido ambiental de bienvenida.
     * ENTRADA: Ninguna.
     * PROCESO: Dispara la pista "Echo Hit" para la transición al Dashboard.
     * SALIDA: Audio ambiental (21s).
     */
    public void playLoginSuccess() {
        playSound(SOUND_LOGIN);
    }

    /**
     * Configura el estado de silencio global.
     * @param muted true para silenciar, false para activar sonido.
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    // --- MOTOR DE AUDIO (MÉTODO PRIVADO) ---

    /**
     * Motor de reproducción en hilo secundario.
     * ENTRADA: Ruta del archivo de audio (path).
     * VALIDACIONES:
     * 1. Verifica si el sistema está muteado.
     * 2. Verifica si el recurso existe en el classpath.
     * PROCESO: Crea un nuevo hilo (Thread), carga el Clip y lo reproduce.
     * SALIDA: Reproducción auditiva asíncrona.
     */
    private void playSound(String path) {
        // VALIDACIÓN 1: Estado Mute
        if (muted) return;

        new Thread(() -> {
            try {
                // VALIDACIÓN 2: Existencia del recurso
                URL url = getClass().getResource(path);
                if (url != null) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                }
            } catch (Exception e) {
                // Silencioso si no encuentra el archivo para no romper el flujo
                // e.printStackTrace();
            }
        }).start();
    }
}