/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG
 * ARCHIVO: LuxuryCalendar.java
 * DESCRIPCIÓN: Utilidad para aplicar estilo Dark/Gold a los calendarios.
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

public class LuxuryCalendar {

    // Paleta de Colores
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color FUCSIA = new Color(220, 0, 115);
    private static final Color TEXT_WHITE = new Color(230, 230, 230);

    /**
     * Aplica el estilo Luxury a un DatePicker existente.
     */
    public static void applyTo(DatePicker dp) {
        if (dp == null) return;

        // 1. Configuración interna (Colores del popup)
        DatePickerSettings s = new DatePickerSettings();
        s.setFormatForDatesCommonEra("yyyy-MM-dd");

        s.setColor(DateArea.BackgroundOverallCalendarPanel, BG_DARK);
        s.setColor(DateArea.BackgroundMonthAndYearMenuLabels, GOLD);
        s.setColor(DateArea.TextMonthAndYearMenuLabels, Color.BLACK);
        s.setColor(DateArea.CalendarTextWeekdays, GOLD);
        s.setColor(DateArea.CalendarBackgroundNormalDates, BG_DARK);

        // Selección y Hoy
        s.setColor(DateArea.CalendarBackgroundSelectedDate, FUCSIA);
        s.setColor(DateArea.CalendarBorderSelectedDate, Color.WHITE);
        s.setColor(DateArea.BackgroundTodayLabel, GOLD);
        s.setColor(DateArea.TextTodayLabel, Color.BLACK);

        try {
            s.setColor(DateArea.CalendarTextNormalDates, Color.WHITE);
        } catch (Throwable t) {}

        s.setFontCalendarDateLabels(new Font("Segoe UI", Font.BOLD, 14));
        dp.setSettings(s);

        // 2. Estilo del Input (Campo de texto)
        JTextField field = dp.getComponentDateTextField();
        field.setBackground(new Color(40, 40, 40));
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(GOLD);
        field.setFont(new Font("Segoe UI", Font.BOLD, 14));
        field.setBorder(new CompoundBorder(
                new LineBorder(GOLD, 1),
                new EmptyBorder(0, 10, 0, 10)
        ));
        field.setPreferredSize(new Dimension(150, 40));

        // 3. Estilo del Botón
        JButton btn = dp.getComponentToggleCalendarButton();
        btn.setText("📅");
        btn.setBackground(GOLD);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
    }
}