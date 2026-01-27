/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: DatabaseSetup.java
 * VERSIÓN: 2.7.1 (Atelier Schema & Smart Migration)
 * DESCRIPCIÓN: Centraliza la creación y migración de tablas, y la inyección
 * de datos maestros para el negocio de confección a medida.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import com.swimcore.dao.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    public static void inicializarBD() {
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement()) {

            System.out.println("--- INICIANDO VERIFICACIÓN DE BASE DE DATOS SICONI ---");

            // --- BLOQUE DDL (DEFINICIÓN DE ESTRUCTURA) ---
            // Se usa "CREATE TABLE IF NOT EXISTS" para no borrar datos si ya existen.

            // 1. TABLAS MAESTRAS
            stmt.execute("CREATE TABLE IF NOT EXISTS clubs (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, full_name TEXT, role TEXT)");

            // 2. TABLA DE CLIENTES (ATELIER)
            stmt.execute("CREATE TABLE IF NOT EXISTS clients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, id_type TEXT, id_number TEXT UNIQUE, " +
                    "full_name TEXT NOT NULL, phone TEXT, email TEXT, address TEXT, instagram TEXT, is_vip INTEGER DEFAULT 0, " +
                    "athlete_name TEXT, birth_date TEXT, club_name TEXT, category TEXT, measurements TEXT)");

            // 3. TABLA DE PRODUCTOS
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, name TEXT NOT NULL, description TEXT, cost_price REAL, sale_price REAL, current_stock INTEGER DEFAULT 0, min_stock INTEGER DEFAULT 5, category_id INTEGER, supplier_id INTEGER, image_path TEXT, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");

            // 4. TABLA DE PEDIDOS (VENTAS)
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (id TEXT PRIMARY KEY, date TEXT, client_id TEXT, total_divisa REAL, amount_paid_usd REAL, balance_due_usd REAL, total_bs REAL, rate REAL, currency TEXT, payment_method TEXT, reference_number TEXT, status TEXT, observations TEXT, delivery_date TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS sale_details (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT, product_id INTEGER, quantity INTEGER, unit_price REAL, subtotal REAL, FOREIGN KEY(sale_id) REFERENCES sales(id), FOREIGN KEY(product_id) REFERENCES products(id))");

            // --- PARCHE DE MIGRACIÓN AUTOMÁTICA ---
            // Intenta agregar columnas que podrían faltar en una versión antigua de la BD.
            try { stmt.execute("ALTER TABLE clients ADD COLUMN address TEXT"); } catch (SQLException e) { /* Ignorar si ya existe */ }
            try { stmt.execute("ALTER TABLE clients ADD COLUMN email TEXT"); } catch (SQLException e) { /* Ignorar si ya existe */ }
            try { stmt.execute("ALTER TABLE sales ADD COLUMN amount_paid_usd REAL"); } catch (SQLException e) { /* Ignorar si ya existe */ }
            try { stmt.execute("ALTER TABLE sales ADD COLUMN balance_due_usd REAL"); } catch (SQLException e) { /* Ignorar si ya existe */ }
            try { stmt.execute("ALTER TABLE sales ADD COLUMN status TEXT"); } catch (SQLException e) { /* Ignorar si ya existe */ }
            try { stmt.execute("ALTER TABLE sales ADD COLUMN observations TEXT"); } catch (SQLException e) { /* Ignorar si ya existe */ }
            try { stmt.execute("ALTER TABLE sales ADD COLUMN delivery_date TEXT"); } catch (SQLException e) { /* Ignorar si ya existe */ }

            // --- DATA SEEDING (INYECCIÓN DE DATOS MAESTROS) ---

            // INYECTAR CLUBES (Solo si la tabla está vacía)
            if (isTableEmpty(conn, "clubs")) {
                System.out.println("Inyectando lista de Clubes...");
                stmt.execute("INSERT INTO clubs (name) VALUES ('Sin Club / Particular'), ('Club Deportivo Cimos'), ('Centro Ítalo Venezolano de Guayana (CIVG)'), ('Club Tiburones de Bauxilum'), ('Los Raudales Swim Academy'), ('Club Delfines de Lourdes'), ('Club CVG Tritones'), ('Club de Natación La Laja'), ('Club Deportes Acuáticos Angostura'), ('Club Atlantis'), ('Academia Obdulio Villazana')");
            }

            // INYECTAR CATEGORÍAS (Solo si la tabla está vacía)
            if (isTableEmpty(conn, "categories")) {
                System.out.println("Inyectando categorías de productos...");
                stmt.execute("INSERT INTO categories (name) VALUES ('Dama - Clásico Tiro Normal'), ('Dama - Clásico Tiro Delgado'), ('Dama - Clásico Tiro Cruzado'), ('Dama - Colegial'), ('Dama - Enterizo Deportivo'), ('Dama - Kneeskin'), ('Dama - Racing Back'), ('Dama - Fastskin'), ('Dama - Bikini Deportivo'), ('Dama - Monokini'), ('Dama - Talla Grande'), ('Caballero - Jammer'), ('Caballero - Boxer'), ('Caballero - Tanga'), ('Accesorios - Gorros'), ('Insumos - Telas y Lycras'), ('Insumos - Mercería'), ('Activos')");
            }

            System.out.println("✅ OK: Verificación de Base de Datos completada.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica si una tabla está vacía.
     */
    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }
}