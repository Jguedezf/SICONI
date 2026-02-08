/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: LanguageManager.java
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: 05 de Febrero de 2026 - 12:20 PM
 * VERSIÓN: 1.0.1 (Hotfix / Maintenance Release)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase de utilidad encargada de la gestión de Internacionalización (i18n) del
 * sistema. Centraliza la carga de recursos lingüísticos dinámicos mediante el
 * uso de la API estándar de Java para localización (ResourceBundle).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * [UTILIDAD - INTERNACIONALIZACIÓN] Motor de traducción del Sistema SICONI.
 * [PATRÓN DE DISEÑO] Singleton Estático: Proporciona un punto de acceso único global.
 * [REQUERIMIENTO NO FUNCIONAL] Localización: Permite la adaptabilidad del software
 * a diferentes configuraciones regionales sin modificar el código fuente.
 */
public class LanguageManager {

    // ========================================================================================
    //                                  ATRIBUTOS (ENCAPSULAMIENTO)
    // ========================================================================================

    // Objeto que define la configuración regional activa (Idioma y País).
    // Inicializado por defecto en Español (ISO 639-1: "es").
    private static Locale currentLocale = new Locale("es");

    // Contenedor de recursos que almacena el par clave-valor de las traducciones en memoria.
    private static ResourceBundle bundle;

    // ========================================================================================
    //                                  BLOQUES DE INICIALIZACIÓN
    // ========================================================================================

    // [BLOQUE ESTÁTICO] Garantiza que el motor de idiomas esté listo al cargar la clase
    // en la Máquina Virtual de Java (JVM), invocando la carga inicial del bundle.
    static {
        loadBundle();
    }

    // ========================================================================================
    //                                  MÉTODOS DE CONFIGURACIÓN
    // ========================================================================================

    /**
     * Define una nueva configuración regional y dispara la recarga del diccionario.
     * Permite el cambio de idioma en tiempo de ejecución (Hot-Swapping).
     * @param locale Objeto Locale destino.
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        loadBundle();
    }

    /**
     * Recupera la instancia regional que rige la sesión actual.
     * @return Locale configurado.
     */
    public static Locale getLocale() {
        return currentLocale;
    }

    /**
     * [LÓGICA INTERNA] Realiza el acceso al sistema de archivos para cargar
     * el archivo .properties correspondiente al idioma seleccionado.
     * Implementa un mecanismo de Fallback para garantizar la estabilidad de la interfaz.
     */
    private static void loadBundle() {
        try {
            // El motor busca automáticamente el patrón de nombre: "messages_XX.properties"
            bundle = ResourceBundle.getBundle("messages", currentLocale);
        } catch (Exception e) {
            System.err.println("SICONI WARNING: No se pudo cargar el idioma " + currentLocale);
            // Mecanismo de seguridad: Fallback al idioma predeterminado del sistema operativo.
            bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        }
    }

    // ========================================================================================
    //                                  RECUPERACIÓN DE DATOS (i18n API)
    // ========================================================================================

    /**
     * Recupera el texto traducido asociado a una clave única identificadora.
     * @param key Identificador de la cadena de texto definida en el diccionario.
     * @return La traducción correspondiente o un indicador de error si la clave no existe.
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            // Gestión de fallos: Retorna la clave cruda para diagnóstico visual en la UI.
            return "Key not found: " + key;
        }
    }

    /**
     * [POO - POLIMORFISMO ESTÁTICO] Sobrecarga del método get.
     * Permite especificar un valor de respaldo para manejar inconsistencias en los
     * archivos de propiedades y evitar punteros nulos en la vista.
     * * @param key Identificador de la cadena.
     * @param defaultValue Texto a retornar si la clave no se encuentra o el sistema falla.
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