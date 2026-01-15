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

    private static final String URL = "jdbc:sqlite:siconi.db";
    private static Connection con = null;

    public static synchronized Connection conectar() {
        // Silencia las letras rojas de SLF4J para una consola limpia
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");

        try {
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(URL);
                Statement stmt = con.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON;");

                // Confirmación visual profesional
                System.out.println("💾 SICONI: CONECTADA A BASE DE DATOS SQLITE [OK]");

                crearTablasBase(stmt);
                stmt.close();
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
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void crearTablasBase(Statement stmt) throws SQLException {
        // Estas tablas son las que tu Main usa para el INSERT OR IGNORE
        stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");

        // Tabla de productos con la estructura exacta que pide tu método insertProduct
        stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT UNIQUE, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "cost_price REAL, " +
                "sale_price REAL, " +
                "current_stock INTEGER DEFAULT 0, " +
                "min_stock INTEGER DEFAULT 5, " +
                "category_id INTEGER, " +
                "supplier_id INTEGER, " +
                "image_path TEXT, " +
                "FOREIGN KEY (category_id) REFERENCES categories(id), " +
                "FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");

        // Tabla de usuarios para tu UserDAO
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, full_name TEXT, role TEXT)");
    }
}