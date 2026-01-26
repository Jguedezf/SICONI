/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: CurrencyManager.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 3.0.0 (Global Currency Centralization)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase utilitaria encargada de la lógica de conversión monetaria y gestión de tasas.
 * Implementación de "Modo de Moneda Global". Ahora el sistema
 * puede cambiar dinámicamente entre USD, EUR y Bs, persistiendo la elección
 * del usuario y el símbolo monetario asociado.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import java.util.prefs.Preferences;

/**
 * Gestor de Divisas con Persistencia de Datos.
 * Actúa como el "Cerebro Financiero" de SICONI, permitiendo que un cambio
 * en la configuración afecte a todas las ventanas del sistema (Inventario, Ventas, Reportes).
 */
public class CurrencyManager {

    // --- ATRIBUTOS (Claves de Persistencia) ---
    private static final Preferences prefs = Preferences.userNodeForPackage(CurrencyManager.class);

    // Claves para guardar en la memoria del sistema
    private static final String KEY_RATE = "exchange_rate_siconi";
    private static final String KEY_SYMBOL = "currency_symbol_siconi";
    private static final String KEY_MODE = "currency_mode_siconi"; // 0=USD, 1=EUR, 2=VES

    // --- VALORES POR DEFECTO ---
    private static final double RATE_DEFAULT = 35.00;
    private static final String SYMBOL_DEFAULT = "$";
    private static final int MODE_DEFAULT = 0; // Por defecto arranca en Dólares (0)

    // --- GETTERS (Lectura de Memoria) ---

    public static double getTasa() {
        return prefs.getDouble(KEY_RATE, RATE_DEFAULT);
    }

    public static String getSymbol() {
        return prefs.get(KEY_SYMBOL, SYMBOL_DEFAULT);
    }

    /**
     * Obtiene el modo de moneda actual.
     * @return 0 para USD, 1 para EUR, 2 para Bolívares.
     */
    public static int getMode() {
        return prefs.getInt(KEY_MODE, MODE_DEFAULT);
    }

    // --- SETTERS (Escritura y Configuración) ---

    /**
     * Guarda la configuración global de moneda para todo el sistema.
     * @param tasa La tasa de cambio del día.
     * @param symbol El símbolo visual (Ej: "Bs.", "$").
     * @param mode El modo operativo (0, 1, 2).
     */
    public static void setConfig(double tasa, String symbol, int mode) {
        prefs.putDouble(KEY_RATE, tasa);
        prefs.put(KEY_SYMBOL, symbol);
        prefs.putInt(KEY_MODE, mode);
    }

    // --- LÓGICA DE NEGOCIO Y CONVERSIÓN ---

    /**
     * Convierte un monto base (asumido en USD) a la moneda seleccionada actualmente.
     * Este es el metodo que usarán las tablas para saber qué mostrar.
     * * @param amountInUSD El precio base del producto en Dólares.
     * @return El monto convertido (o el mismo si es USD).
     */
    public static double convert(double amountInUSD) {
        int mode = getMode();
        double tasa = getTasa();

        switch (mode) {
            case 0: return amountInUSD;        // USD -> USD (Base)
            case 1: return amountInUSD;        // USD -> EUR (Asumimos paridad 1:1 o se ajustará a futuro)
            case 2: return amountInUSD * tasa; // USD -> BS (Multiplicación por Tasa BCV)
            default: return amountInUSD;
        }
    }

    /**
     * Formatea un precio automáticamente según la configuración global.
     * Útil para etiquetas y reportes.
     * * @param amountInUSD Precio base en dólares.
     * @return Texto formateado (Ej: "Bs. 500.00" o "$ 15.00").
     */
    public static String formatPrice(double amountInUSD) {
        double finalAmount = convert(amountInUSD);
        String symbol = getSymbol();
        return String.format("%s %.2f", symbol, finalAmount);
    }
}