/*
 * -----------------------------------------------------------------------------
 * ARCHIVO: SoundManager.java
 * DESCRIPCIÓN: Motor de Audio Luxury (Singleton).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class SoundManager {

    // --- RUTAS DE SONIDO ---
    private static final String SOUND_HOVER = "/sounds/hover.wav";
    private static final String SOUND_CLICK = "/sounds/click.wav";
    private static final String SOUND_ERROR = "/sounds/error.wav";
    private static final String SOUND_LOGIN = "/sounds/login_success.wav";

    private static SoundManager instance;
    private boolean muted = false;

    private SoundManager() {}

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void playHover() { playSound(SOUND_HOVER); }
    public void playClick() { playSound(SOUND_CLICK); }
    public void playError() { playSound(SOUND_ERROR); }

    // Este es el importante para la bienvenida
    public void playLoginSuccess() { playSound(SOUND_LOGIN); }

    public void setMuted(boolean muted) { this.muted = muted; }

    private void playSound(String path) {
        if (muted) return;
        new Thread(() -> {
            try {
                URL url = getClass().getResource(path);
                if (url != null) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                }
            } catch (Exception e) {
                // Error silencioso
            }
        }).start();
    }
}