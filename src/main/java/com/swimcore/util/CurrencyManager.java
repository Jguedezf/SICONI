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
 * VERSIÓN: 2.0.0 (Persistent Exchange Rate System)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase utilitaria encargada de la lógica de conversión monetaria y gestión de tasas.
 * Se ha implementado PERSISTENCIA mediante Java Preferences API para asegurar
 * que la Tasa BCV sobreviva al cierre del sistema.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import java.util.prefs.Preferences;

/**
 * Gestor de Divisas con Persistencia de Datos.
 * Garantiza que la tasa de cambio se mantenga sincronizada entre sesiones.
 */
public class CurrencyManager {

    // Nodo de preferencias para guardar datos en el almacenamiento del sistema
    private static final Preferences prefs = Preferences.userNodeForPackage(CurrencyManager.class);
    private static final String TASA_KEY = "tasa_bcv_siconi";

    // Valor por defecto en caso de que no exista registro previo
    private static final double TASA_DEFAULT = 60.00;

    /**
     * Recupera la tasa de cambio actual desde el almacenamiento persistente.
     * Si no hay una tasa guardada, retorna el valor por defecto (60.00).
     * @return Valor guardado de la tasa (double).
     */
    public static double getTasa() {
        return prefs.getDouble(TASA_KEY, TASA_DEFAULT);
    }

    /**
     * Actualiza la tasa de cambio y la guarda físicamente en el sistema.
     * @param nuevaTasa El nuevo valor de conversión ingresado por el usuario.
     */
    public static void setTasa(double nuevaTasa) {
        prefs.putDouble(TASA_KEY, nuevaTasa);
    }

    /**
     * Metodo de utilidad para el formateo visual de precios en la interfaz.
     * @param precioEnDivisa El monto base en moneda extranjera.
     * @return String con el formato: "€ X.XX (Bs. X.XX)"
     */
    public static String formatPrice(double precioEnDivisa) {
        double tasaActual = getTasa();
        double enBolivares = precioEnDivisa * tasaActual;

        // Formateo profesional con 2 decimales
        return String.format("€ %.2f  (Bs. %.2f)", precioEnDivisa, enBolivares);
    }
}