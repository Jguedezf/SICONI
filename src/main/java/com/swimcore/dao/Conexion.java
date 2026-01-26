package com.swimcore.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor centralizado de conexión SQLite (Patrón Singleton).
 * Sincronizado para trabajar con el Main de Johanna Guedez.
 */
public class Conexion {

    // Cambiamos a una única constante para la URL
    private static final String URL = "jdbc:sqlite:siconi.db";
    private static Connection con = null;

    // Constructor privado para evitar instancias con 'new'
    private Conexion() {}

    public static synchronized Connection conectar() {
        // Silencia logs innecesarios
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");

        try {
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(URL);

                // IMPORTANTE: Activar Foreign Keys en SQLite (por defecto vienen apagadas)
                try (Statement stmt = con.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }

                // Confirmación visual profesional
                System.out.println("💾 SICONI: CONECTADA A BASE DE DATOS SQLITE [OK]");
            }
        } catch (SQLException e) {
            System.err.println("❌ Fallo crítico de vinculación: " + e.getMessage());
        }
        return con;
    }

    public static void cerrar() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("🔒 SICONI: Conexión cerrada con éxito.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}