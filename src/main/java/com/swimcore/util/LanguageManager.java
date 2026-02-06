/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: LanguageManager.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: 05 de Febrero de 2026 - 12:20 PM
 * VERSIÓN: 1.0.1 (Hotfix / Maintenance Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase utilitaria encargada de la gestión de Internacionalización (i18n) del sistema.
 * Centraliza la carga de recursos lingüísticos dinámicos mediante el uso de la API
 * estándar de Java para localización.
 *
 * Responsabilidades Técnicas:
 * 1. Gestión de Estado Global: Mantiene la configuración regional (Locale) activa
 * durante el ciclo de vida de la ejecución.
 * 2. Mapeo de Diccionarios: Vincula claves de texto con sus traducciones correspondientes
 * alojadas en archivos de propiedades (.properties).
 * 3. Fallback Mechanism: Implementa una gestión de errores para evitar el colapso
 * de la UI si una clave de idioma no existe.
 *
 * PRINCIPIOS POO:
 * - ABSTRACCIÓN: Oculta la complejidad de la búsqueda de archivos y carga de flujos
 * detrás de un método estático simple `get()`.
 * - ENCAPSULAMIENTO: Protege las variables `currentLocale` y `bundle` mediante
 * modificadores de acceso privados.
 * - POLIMORFISMO (Estático): Implementa sobrecarga de métodos en la función `get`
 * para manejar valores por defecto.
 *
 * PATRONES DE DISEÑO:
 * - Singleton (Variación Estática): Proporciona un único punto de acceso global
 * para la traducción de toda la interfaz.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Motor de Internacionalización del Sistema SICONI.
 * Permite el cambio de idioma en tiempo de ejecución (Hot-Swapping) mediante
 * el refresco de los Resource Bundles.
 */
public class LanguageManager {

    // Configuración regional activa. Por defecto: Español (ISO 639-1: "es")
    private static Locale currentLocale = new Locale("es");

    // Contenedor de recursos cargado en memoria
    private static ResourceBundle bundle;

    // BLOQUE ESTÁTICO: Se ejecuta una sola vez al cargar la clase en la JVM
    static {
        loadBundle();
    }

    /**
     * Define el nuevo idioma y fuerza la recarga de los diccionarios.
     * @param locale Nuevo objeto Locale (ej. new Locale("en"))
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        loadBundle();
    }

    /**
     * Recupera el Locale configurado actualmente.
     * @return Locale activo.
     */
    public static Locale getLocale() {
        return currentLocale;
    }

    /**
     * Realiza la carga física del archivo de propiedades correspondiente.
     * El sistema buscará archivos con el patrón: messages_XX.properties
     * (donde XX es el código del idioma).
     */
    private static void loadBundle() {
        // ResourceBundle busca automáticamente en el classpath
        // el nombre base "messages" combinado con el locale actual.
        // Se añade control de errores por seguridad.
        try {
            bundle = ResourceBundle.getBundle("messages", currentLocale);
        } catch (Exception e) {
            System.err.println("SICONI WARNING: No se pudo cargar el idioma " + currentLocale);
            // Fallback de seguridad a Locale por defecto del sistema si falla
            bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        }
    }

    /**
     * Obtiene la traducción asociada a una clave específica.
     * @param key Identificador de la cadena (ej. "login.title")
     * @return Texto traducido o mensaje de error si la clave es inválida.
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            // Gestión de fallos: Retorna la clave para identificar errores en la vista.
            return "Key not found: " + key;
        }
    }

    /**
     * SOBRECARGA DE METODO: Obtiene la traducción o usa un valor de respaldo.
     * Soluciona el error de compilación cuando la vista envía un texto por defecto.
     *
     * @param key Identificador de la cadena.
     * @param defaultValue Texto a mostrar si no se encuentra la traducción o el bundle falla.
     * @return Texto traducido o valor por defecto.
     */
    public static String get(String key, String defaultValue) {
        try {
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getString(key);
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}