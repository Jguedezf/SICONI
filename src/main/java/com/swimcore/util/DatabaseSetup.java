/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: DatabaseSetup.java
 * VERSIÓN: 3.2.0 (SAFE CATALOG INJECTION)
 * DESCRIPCIÓN: Se implementa una inyección de datos segura que solo añade
 * productos si no existen, preservando los datos existentes del usuario.
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

            // --- ESTRUCTURA DE TABLAS ---
            stmt.execute("CREATE TABLE IF NOT EXISTS clubs (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, full_name TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, id_type TEXT, id_number TEXT, full_name TEXT NOT NULL, phone TEXT, email TEXT, address TEXT, instagram TEXT, is_vip INTEGER DEFAULT 0, athlete_name TEXT, birth_date TEXT, club_name TEXT, category TEXT, measurements TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, name TEXT NOT NULL, description TEXT, cost_price REAL, sale_price REAL, current_stock INTEGER DEFAULT 0, min_stock INTEGER DEFAULT 5, category_id INTEGER, supplier_id INTEGER, image_path TEXT, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (id TEXT PRIMARY KEY, date TEXT, client_id TEXT, total_divisa REAL, amount_paid_usd REAL, balance_due_usd REAL, total_bs REAL, rate REAL, currency TEXT, payment_method TEXT, reference_number TEXT, status TEXT, observations TEXT, delivery_date TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_details (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT, product_id INTEGER, quantity INTEGER, unit_price REAL, subtotal REAL, FOREIGN KEY(sale_id) REFERENCES sales(id), FOREIGN KEY(product_id) REFERENCES products(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS payments (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT NOT NULL, payment_date TEXT NOT NULL, amount_usd REAL NOT NULL, payment_method TEXT, reference TEXT, notes TEXT, FOREIGN KEY(sale_id) REFERENCES sales(id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_movements (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER, quantity INTEGER, type TEXT, observation TEXT, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // --- INYECCIÓN DE DATOS INTELIGENTE (DATA SEEDING) ---

            // CLUBES
            if (isTableEmpty(conn, "clubs")) {
                System.out.println("Inyectando lista de Clubes...");
                stmt.execute("INSERT INTO clubs (name) VALUES ('Sin Club / Particular'), ('CIMOS'), ('CIVG'), ('Tiburones de Bauxilum'), ('Los Raudales'), ('Delfines de Lourdes'), ('CVG Tritones'), ('La Laja'), ('Angostura'), ('Atlantis'), ('Academia Obdulio Villazana')");
            }

            // CATEGORÍAS
            if (isTableEmpty(conn, "categories")) {
                System.out.println("Inyectando categorías de productos...");
                stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama Competencia', 'Modelos de alto rendimiento'), ('Dama Entrenamiento', 'Modelos para uso diario y entrenamiento'), ('Caballero', 'Modelos masculinos'), ('Niños/Junior', 'Modelos infantiles y juveniles'), ('Accesorios', 'Gorros, lentes, etc.'), ('Insumos', 'Telas, hilos, etc.')");
            }

            // PRODUCTOS (CATÁLOGO COMPLETO)
            System.out.println("Verificando catálogo de productos...");
            String insertProd = "INSERT OR IGNORE INTO products (code, name, description, cost_price, sale_price, current_stock, category_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertProd)) {
                // DAMA
                insertItem(ps, "D-001", "Clásico Tiro Normal", "Entrenamiento", 15, 25, 50, 2);
                insertItem(ps, "D-002", "Clásico Tiro Delgado", "Entrenamiento", 15, 25, 50, 2);
                insertItem(ps, "D-003", "Clásico Tiro Cruzado", "Entrenamiento", 16, 28, 50, 2);
                insertItem(ps, "D-004", "Colegial", "Básico Escolar", 12, 20, 100, 4);
                insertItem(ps, "D-005", "Enterizo Deportivo", "Protección Solar", 20, 35, 30, 2);
                insertItem(ps, "D-006", "Kneeskin", "Competencia", 25, 45, 20, 1);
                insertItem(ps, "D-007", "Racing Back", "Competencia Pro", 22, 40, 25, 1);
                insertItem(ps, "D-008", "Fastskin", "Alta Competencia", 30, 60, 15, 1);
                insertItem(ps, "D-009", "Bikini Deportivo (2 piezas)", "Entrenamiento", 18, 30, 40, 2);
                insertItem(ps, "D-010", "Monokini Deportivo", "Diseño", 18, 32, 20, 2);
                insertItem(ps, "D-011", "Talla Grande (Plus)", "Confort", 20, 35, 30, 2);
                insertItem(ps, "D-012", "Espalda Abierta", "Entrenamiento", 15, 25, 40, 2);
                insertItem(ps, "D-013", "Espalda Cerrada", "Resistencia", 18, 30, 20, 2);
                insertItem(ps, "D-014", "Espalda Nadadora", "Clásico", 15, 25, 60, 2);

                // CABALLERO
                insertItem(ps, "C-001", "Jammer", "Competencia/Entrenamiento", 15, 25, 50, 3);
                insertItem(ps, "C-002", "Boxer", "Entrenamiento", 12, 20, 60, 3);
                insertItem(ps, "C-003", "Tanga", "Competencia", 10, 18, 40, 3);

                // ACCESORIOS
                insertItem(ps, "A-001", "Gorro de Natación", "Silicona/Tela", 3, 8, 100, 5);

                ps.executeBatch();
            }

            System.out.println("✅ OK: Verificación de Base de Datos completada.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertItem(PreparedStatement ps, String code, String name, String desc, double cost, double price, int stock, int catId) throws SQLException {
        ps.setString(1, code);
        ps.setString(2, name);
        ps.setString(3, desc);
        ps.setDouble(4, cost);
        ps.setDouble(5, price);
        ps.setInt(6, stock);
        ps.setInt(7, catId);
        ps.addBatch();
    }

    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }
}