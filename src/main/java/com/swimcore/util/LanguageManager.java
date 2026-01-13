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
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
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
        bundle = ResourceBundle.getBundle("messages", currentLocale);
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
}