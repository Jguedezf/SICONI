/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: CurrencyManager.java
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 3.0.0 (Global Currency Centralization)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Módulo utilitario encargado de la lógica de conversión monetaria y gestión
 * de tasas de cambio. Implementa un sistema de persistencia basado en registros
 * del sistema (Preferences API) para garantizar que la configuración financiera
 * sea transversal a todos los módulos de la aplicación (Dashboard, Ventas, Inventario).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import java.util.prefs.Preferences;

/**
 * [UTILIDAD - FINANZAS] Gestor de Divisas con Persistencia de Datos.
 * [POO - ABSTRACCIÓN] Centraliza la complejidad de los cálculos de conversión
 * y el formateo de moneda en una API estática reutilizable.
 * [REQUERIMIENTO NO FUNCIONAL] Persistencia: Mantiene los valores de tasa y
 * símbolos monetarios incluso tras el reinicio de la aplicación.
 */
public class CurrencyManager {

    // ========================================================================================
    //                                  ATRIBUTOS DE PERSISTENCIA
    // ========================================================================================

    // [API JAVA PREFERENCES] Nodo de almacenamiento para guardar configuraciones de usuario.
    private static final Preferences prefs = Preferences.userNodeForPackage(CurrencyManager.class);

    // Claves identificadoras para el acceso al registro de datos.
    private static final String KEY_RATE = "exchange_rate_siconi";
    private static final String KEY_SYMBOL = "currency_symbol_siconi";
    private static final String KEY_MODE = "currency_mode_siconi"; // Codificación: 0=USD, 1=EUR, 2=VES

    // Valores predeterminados (Fallback) en caso de ausencia de datos previos.
    private static final double RATE_DEFAULT = 35.00;
    private static final String SYMBOL_DEFAULT = "$";
    private static final int MODE_DEFAULT = 0;

    // ========================================================================================
    //                                  MÉTODOS DE ACCESO (GETTERS)
    // ========================================================================================

    /**
     * Recupera la tasa de cambio almacenada en el sistema.
     * @return Valor double de la tasa de conversión.
     */
    public static double getTasa() {
        return prefs.getDouble(KEY_RATE, RATE_DEFAULT);
    }

    /**
     * Recupera el identificador visual de la moneda.
     * @return String con el símbolo monetario (ej: "$", "Bs.").
     */
    public static String getSymbol() {
        return prefs.get(KEY_SYMBOL, SYMBOL_DEFAULT);
    }

    /**
     * Obtiene el modo operativo de moneda seleccionado por el usuario.
     * @return Entero representativo (0: USD, 1: EUR, 2: Bolívares).
     */
    public static int getMode() {
        return prefs.getInt(KEY_MODE, MODE_DEFAULT);
    }

    // ========================================================================================
    //                                  MÉTODOS DE CONFIGURACIÓN (SETTERS)
    // ========================================================================================

    /**
     * Actualiza y persiste la configuración financiera global.
     * @param tasa Nueva tasa de cambio.
     * @param symbol Nuevo símbolo visual.
     * @param mode Modo de operación seleccionado.
     */
    public static void setConfig(double tasa, String symbol, int mode) {
        prefs.putDouble(KEY_RATE, tasa);
        prefs.put(KEY_SYMBOL, symbol);
        prefs.putInt(KEY_MODE, mode);
    }

    // ========================================================================================
    //                                  LÓGICA DE CONVERSIÓN Y NEGOCIO
    // ========================================================================================

    /**
     * [ALGORITMO DE CONVERSIÓN] Transforma un monto base a la moneda de visualización activa.
     * Implementa una estructura de control switch para determinar el factor de conversión
     * basándose en el modo recuperado de la persistencia.
     * * @param amountInUSD El monto original en la divisa base del sistema (Dólares).
     * @return El monto equivalente en la moneda seleccionada.
     */
    public static double convert(double amountInUSD) {
        int mode = getMode();
        double tasa = getTasa();

        switch (mode) {
            case 0: return amountInUSD;        // Relación 1:1 (USD -> USD)
            case 1: return amountInUSD;        // Relación 1:1 (USD -> EUR) - Configurable a futuro
            case 2: return amountInUSD * tasa; // Multiplicación por tasa de cambio (USD -> Bs.)
            default: return amountInUSD;
        }
    }

    /**
     * Provee una representación textual formateada de un valor monetario.
     * Aplica la conversión dinámica y concatena el símbolo correspondiente con precisión decimal.
     * * @param amountInUSD Precio base para formatear.
     * @return String formateado (ej: "Bs. 1.250,50").
     */
    public static String formatPrice(double amountInUSD) {
        double finalAmount = convert(amountInUSD);
        String symbol = getSymbol();
        return String.format("%s %.2f", symbol, finalAmount);
    }
}