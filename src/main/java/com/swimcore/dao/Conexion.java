/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: Conexion.java
 * VERSIÓN: 3.0.0 (TURBO: WAL Mode + Cache Tuning + Foreign Keys)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor centralizado de conexión SQLite (Patrón Singleton OPTIMIZADO).
 * Incluye modo WAL y Caché para máxima velocidad en SICONI.
 */
public class Conexion {

    private static final String URL = "jdbc:sqlite:siconi.db";
    private static Connection con = null;

    // Constructor privado
    private Conexion() {}

    public static synchronized Connection conectar() {
        // Silencia logs de drivers externos
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");

        try {
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(URL);

                // --- OPTIMIZACIÓN DE RENDIMIENTO (EL "MODO TURBO") ---
                try (Statement stmt = con.createStatement()) {
                    // 1. Integridad Referencial (Obligatorio)
                    stmt.execute("PRAGMA foreign_keys = ON;");

                    // 2. Modo WAL (Write-Ahead Logging): Permite leer y escribir simultáneamente. ¡VELOCIDAD PURA!
                    stmt.execute("PRAGMA journal_mode = WAL;");

                    // 3. Synchronous NORMAL: Escribe en disco de forma segura pero sin frenar la UI.
                    stmt.execute("PRAGMA synchronous = NORMAL;");

                    // 4. Caché en Memoria: Aumentamos la memoria de trabajo para no leer tanto del disco lento.
                    stmt.execute("PRAGMA cache_size = 10000;");
                }

                System.out.println("🚀 SICONI: Base de Datos conectada en MODO ALTO RENDIMIENTO.");
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR CRÍTICO DE CONEXIÓN: " + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    public static void cerrar() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("🔒 SICONI: Conexión cerrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}