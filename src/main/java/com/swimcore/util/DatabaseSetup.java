/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: DatabaseSetup.java
 * VERSIÓN: 4.1.0 (CRITICAL FIX: Size Column Added)
 * FECHA: 04 de Febrero de 2026 - 04:40 PM
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import com.swimcore.dao.Conexion;
import java.sql.*;

public class DatabaseSetup {

    public static void inicializarBD() {
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement()) {

            System.out.println("--- SICONI: VERIFICANDO INTEGRIDAD DE DATOS ---");

            // 1. CREACIÓN DE TABLAS
            stmt.execute("CREATE TABLE IF NOT EXISTS clubs (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, full_name TEXT, role TEXT)");

            // CORRECCIÓN: Agregado 'size' a la definición para instalaciones nuevas
            stmt.execute("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, id_type TEXT, id_number TEXT, full_name TEXT NOT NULL, phone TEXT, email TEXT, address TEXT, instagram TEXT, is_vip INTEGER DEFAULT 0, athlete_name TEXT, birth_date TEXT, club_name TEXT, category TEXT, measurements TEXT, profession TEXT, phone_alt TEXT, size TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, name TEXT NOT NULL, description TEXT, cost_price REAL, sale_price REAL, current_stock INTEGER DEFAULT 0, min_stock INTEGER DEFAULT 5, category_id INTEGER, supplier_id INTEGER, image_path TEXT, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (id TEXT PRIMARY KEY, date TEXT, client_id TEXT, total_divisa REAL, amount_paid_usd REAL, balance_due_usd REAL, total_bs REAL, rate REAL, currency TEXT, payment_method TEXT, reference_number TEXT, status TEXT, observations TEXT, delivery_date TEXT, invoice_nro TEXT, control_nro TEXT, bank TEXT, payment_date TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_details (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT, product_id INTEGER, quantity INTEGER, unit_price REAL, subtotal REAL, FOREIGN KEY(sale_id) REFERENCES sales(id), FOREIGN KEY (product_id) REFERENCES products(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS payments (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT NOT NULL, payment_date TEXT NOT NULL, amount_usd REAL NOT NULL, payment_method TEXT, reference TEXT, notes TEXT, FOREIGN KEY(sale_id) REFERENCES sales(id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_movements (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER, quantity INTEGER, type TEXT, observation TEXT, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2. MIGRACIONES (Esto repara tu base de datos actual agregando lo que falta)
            actualizarEstructura(conn);

            // 3. DATOS INICIALES
            if (isTableEmpty(conn, "clubs")) {
                stmt.execute("INSERT INTO clubs (name) VALUES ('Sin Club / Particular'), ('CIMOS'), ('CIVG'), ('Tiburones de Bauxilum'), ('Los Raudales'), ('Delfines de Lourdes'), ('CVG Tritones'), ('La Laja'), ('Angostura'), ('Atlantis'), ('Academia Obdulio Villazana')");
            }
            if (isTableEmpty(conn, "categories")) {
                stmt.execute("INSERT INTO categories (name, description) VALUES ('TRAJES DE BAÑO', 'Modelos terminados'), ('ROPA DEPORTIVA', 'Licras, tops, etc.'), ('INSUMOS', 'Telas, hilos, elásticas'), ('EQUIPAMIENTO', 'Lentes, gorros, aletas'), ('ACTIVOS', 'Maquinaria y mobiliario')");
            }

            System.out.println("✅ SICONI LISTO: Base de datos sincronizada.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void actualizarEstructura(Connection conn) {
        // CORRECCIÓN: Agrega la columna 'size' si no existe
        checkAndAddColumn(conn, "clients", "size", "TEXT");
        checkAndAddColumn(conn, "clients", "phone_alt", "TEXT");
        checkAndAddColumn(conn, "clients", "profession", "TEXT");
        checkAndAddColumn(conn, "suppliers", "instagram", "TEXT");
        checkAndAddColumn(conn, "suppliers", "whatsapp", "TEXT");
        checkAndAddColumn(conn, "sales", "invoice_nro", "TEXT");
        checkAndAddColumn(conn, "sales", "control_nro", "TEXT");
    }

    // Método auxiliar seguro para agregar columnas
    private static void checkAndAddColumn(Connection conn, String table, String column, String type) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, table, column);
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    System.out.println("🔧 Migrando BD: Agregando columna '" + column + "' a tabla '" + table + "'");
                    stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type + " DEFAULT ''");
                }
            }
        } catch (SQLException e) {
            System.err.println("Aviso migración: " + e.getMessage());
        }
    }

    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }
}