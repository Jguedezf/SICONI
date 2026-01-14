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
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase utilitaria encargada de la lógica de conversión monetaria y gestión de tasas de cambio.
 * Centraliza el factor de conversión (Tasa BCV) para garantizar que todos los módulos
 * (Inventario, Ventas, Reportes) utilicen un valor unificado.
 *
 * Características de Ingeniería:
 * 1. Gestión de Estado Global: Utiliza una variable estática para almacenar la tasa,
 * permitiendo actualizaciones en tiempo real que se reflejan instantáneamente en toda la UI.
 * 2. Formateo de Datos: Implementa algoritmos de representación visual para mostrar
 * precios duales (Divisa/Bolívares) con precisión decimal.
 * 3. Abstracción Financiera: Aisla la lógica de cálculo matemático de los componentes
 * de la interfaz, siguiendo el principio de responsabilidad única.
 *
 * PRINCIPIOS POO:
 * - ENCAPSULAMIENTO: Protege la variable `tasaBCV` mediante métodos accesores (Getter/Setter).
 * - ABSTRACCIÓN: El sistema consume `formatPrice` sin necesidad de conocer la fórmula interna.
 *
 * PATRONES DE DISEÑO:
 * - Singleton (Variación Estática): Proporciona un único punto de control para la
 * configuración financiera del sistema.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

/**
 * Gestor de Divisas y Conversión Monetaria.
 * Proporciona las herramientas necesarias para manejar la dualidad de precios en el sistema.
 */
public class CurrencyManager {

    // Tasa de cambio base (referencia BCV).
    // Se define como estática para que persista durante la sesión de la aplicación.
    private static double tasaBCV = 60.00;

    /**
     * Recupera la tasa de cambio actual.
     * @return Valor actual de la tasa (double).
     */
    public static double getTasa() {
        return tasaBCV;
    }

    /**
     * Actualiza la tasa de cambio en tiempo de ejecución.
     * @param nuevaTasa El nuevo valor de conversión.
     */
    public static void setTasa(double nuevaTasa) {
        tasaBCV = nuevaTasa;
    }

    /**
     * Metodo de utilidad para el formateo visual de precios en la interfaz.
     * Realiza la conversión aritmética y retorna una cadena formateada con 2 decimales.
     * * @param precioEnDivisa El monto base en moneda extranjera.
     * @return String con el formato: "€ X.XX (Bs. X.XX)"
     */
    public static String formatPrice(double precioEnDivisa) {
        // Cálculo del valor equivalente en moneda local
        double enBolivares = precioEnDivisa * tasaBCV;

        // Uso de especificadores de formato (%.2f) para asegurar la precisión monetaria.
        // Se utiliza el símbolo de Euro (€) como moneda base según los requerimientos del sistema.
        return String.format("€ %.2f  (Bs. %.2f)", precioEnDivisa, enBolivares);
    }
}