/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI - Sistema de Control de Negocio e Inventario | DG SWIMWEAR
 * ARCHIVO: LuxuryCalendar.java
 * VERSIÓN: 1.0.0 (UI Enhancement Utility)
 * FECHA: 06 de Febrero de 2026
 * HORA: 07:10 PM (Venezuela)
 * * DESCRIPCIÓN TÉCNICA:
 * Clase de utilidad (Helper Class) encargada de la inyección de estilos
 * visuales personalizados a componentes de fecha externos. Garantiza la
 * homogeneidad estética del Design System "Luxury" de SICONI.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePickerSettings.DateArea;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * [UTILIDAD - INTERFAZ] Centraliza la configuración visual de los selectores de fecha.
 * [DEPENDENCIA] LGoodDatePicker Library: Implementación base de calendarios Swing.
 * [REQUERIMIENTO NO FUNCIONAL] Estética y Usabilidad: Aplicación de tema Dark/Gold.
 */
public class LuxuryCalendar {

    // ========================================================================================
    //                                  ATRIBUTOS (PALETA SEMÁNTICA)
    // ========================================================================================

    // Definición inmutable de la paleta cromática corporativa para el componente calendario.
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color FUCSIA = new Color(220, 0, 115);
    private static final Color TEXT_WHITE = new Color(230, 230, 230);

    // ========================================================================================
    //                                  LÓGICA DE APLICACIÓN (API)
    // ========================================================================================

    /**
     * [MÉTODO ESTÁTICO - DECORATOR]
     * Recibe una instancia de DatePicker y modifica sus propiedades internas
     * para adaptarlas al estilo del sistema.
     * * @param dp Objeto DatePicker a decorar con el estilo institucional.
     */
    public static void applyTo(DatePicker dp) {
        if (dp == null) return; // Validación de integridad (Fail-safe)

        // --- SECCIÓN 1: CONFIGURACIÓN DE SETTINGS (POO - COMPOSICIÓN) ---
        // Se instancia la clase de configuración de la librería para definir
        // el comportamiento y colorimetría del panel emergente (Popup).
        DatePickerSettings s = new DatePickerSettings();
        s.setFormatForDatesCommonEra("yyyy-MM-dd");

        // Definición de colores de áreas específicas (DateArea)
        s.setColor(DateArea.BackgroundOverallCalendarPanel, BG_DARK);
        s.setColor(DateArea.BackgroundMonthAndYearMenuLabels, GOLD);
        s.setColor(DateArea.TextMonthAndYearMenuLabels, Color.BLACK);
        s.setColor(DateArea.CalendarTextWeekdays, GOLD);
        s.setColor(DateArea.CalendarBackgroundNormalDates, BG_DARK);

        // Feedback Visual: Estados de Selección y Fecha Actual
        s.setColor(DateArea.CalendarBackgroundSelectedDate, FUCSIA);
        s.setColor(DateArea.CalendarBorderSelectedDate, Color.WHITE);
        s.setColor(DateArea.BackgroundTodayLabel, GOLD);
        s.setColor(DateArea.TextTodayLabel, Color.BLACK);

        try {
            // Manejo de renderizado de fuentes para fechas estándar
            s.setColor(DateArea.CalendarTextNormalDates, Color.WHITE);
        } catch (Throwable t) {}

        // Tipografía estandarizada para legibilidad aumentada
        s.setFontCalendarDateLabels(new Font("Segoe UI", Font.BOLD, 14));
        dp.setSettings(s); // Persistencia de la configuración en el componente

        // --- SECCIÓN 2: DECORACIÓN DEL INPUT (JTextField) ---
        // Acceso al componente hijo para aplicar estilos de borde y relleno.
        JTextField field = dp.getComponentDateTextField();
        field.setBackground(new Color(40, 40, 40));
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(GOLD);
        field.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Uso de CompoundBorder para combinar un borde de línea dorada con padding interno.
        field.setBorder(new CompoundBorder(
                new LineBorder(GOLD, 1),
                new EmptyBorder(0, 10, 0, 10)
        ));
        field.setPreferredSize(new Dimension(150, 40));

        // --- SECCIÓN 3: DECORACIÓN DEL CONTROLADOR (JButton) ---
        // Estilización del botón disparador del calendario emergente.
        JButton btn = dp.getComponentToggleCalendarButton();
        btn.setText("📅"); // Inyección de ícono mediante glifo Unicode
        btn.setBackground(GOLD);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
    }
}