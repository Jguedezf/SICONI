/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: DatabaseSetup.java
 * VERSIÓN: 4.0.0 (Real Data Injection - Puerto Ordaz Edition)
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

            // 1. CREACIÓN DE TABLAS (SI NO EXISTEN)
            stmt.execute("CREATE TABLE IF NOT EXISTS clubs (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");

            // Tabla Suppliers con campos sociales
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, full_name TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, id_type TEXT, id_number TEXT, full_name TEXT NOT NULL, phone TEXT, email TEXT, address TEXT, instagram TEXT, is_vip INTEGER DEFAULT 0, athlete_name TEXT, birth_date TEXT, club_name TEXT, category TEXT, measurements TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, name TEXT NOT NULL, description TEXT, cost_price REAL, sale_price REAL, current_stock INTEGER DEFAULT 0, min_stock INTEGER DEFAULT 5, category_id INTEGER, supplier_id INTEGER, image_path TEXT, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (id TEXT PRIMARY KEY, date TEXT, client_id TEXT, total_divisa REAL, amount_paid_usd REAL, balance_due_usd REAL, total_bs REAL, rate REAL, currency TEXT, payment_method TEXT, reference_number TEXT, status TEXT, observations TEXT, delivery_date TEXT, invoice_nro TEXT, control_nro TEXT, bank TEXT, payment_date TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_details (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT, product_id INTEGER, quantity INTEGER, unit_price REAL, subtotal REAL, FOREIGN KEY(sale_id) REFERENCES sales(id), FOREIGN KEY(product_id) REFERENCES products(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS payments (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT NOT NULL, payment_date TEXT NOT NULL, amount_usd REAL NOT NULL, payment_method TEXT, reference TEXT, notes TEXT, FOREIGN KEY(sale_id) REFERENCES sales(id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_movements (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER, quantity INTEGER, type TEXT, observation TEXT, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2. MIGRACIONES (COLUMNAS NUEVAS)
            actualizarEstructura(conn);

            // 3. INYECCIÓN DE DATOS REALES (GUAYANA)

            // A. Proveedores Reales
            if (isTableEmpty(conn, "suppliers")) {
                System.out.println("Cargando Directorio de Proveedores de Guayana...");
                String sqlSup = "INSERT INTO suppliers (company, contact, phone, email, address, instagram, whatsapp) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlSup)) {
                    // TRAMAS
                    insertSupplier(ps, "TRAMAS VENEZUELA", "Atención al Cliente", "(0424) 404.93.79", "tramastextiles@gmail.com", "Av. Guayana, Urb. Los Samanes, Edif. Tramas, Pzo", "@tramasVzla", "+584143404663");
                    // EL CASTILLO
                    insertSupplier(ps, "EL CASTILLO (TU CENTRO TEXTIL)", "Gerencia Puerto Ordaz", "(0286) 923.45.67", "contacto@elcastillo.com", "CC. Centro Traki, Paseo Las Americas, Pzo", "@elcastillo.tucentrotextil", "");
                    // EL GRAN COSER
                    insertSupplier(ps, "EL GRAN COSER C.A. (SINGER)", "Distribuidor Singer", "0414-9978638", "", "Av. Principal de Castillito, Local 50, Pzo", "", "04149978638");
                    // COMERCIAL BORINQUEN
                    insertSupplier(ps, "COMERCIAL BORINQUEN", "Ventas Mostrador", "0286-9224837", "", "C.C. Falcón, PB Carrera Upata, Puerto Ordaz", "@borinquen_c.a", "");
                    // TIENDAS TAG
                    insertSupplier(ps, "TIENDAS TAG (TAGUAPIRE)", "Sede Biblos Unare", "(0286) 953.53.13", "info@tagmerceria.com", "C.C. Biblos Unare II, Puerto Ordaz", "@tagmerceria", "");

                    ps.executeBatch();
                }
            }

            // B. Clubes de Natación Locales
            if (isTableEmpty(conn, "clubs")) {
                stmt.execute("INSERT INTO clubs (name) VALUES ('Sin Club / Particular'), ('CIMOS'), ('CIVG'), ('Tiburones de Bauxilum'), ('Los Raudales'), ('Delfines de Lourdes'), ('CVG Tritones'), ('La Laja'), ('Angostura'), ('Atlantis'), ('Academia Obdulio Villazana')");
            }

            // C. Categorías de Producto
            if (isTableEmpty(conn, "categories")) {
                stmt.execute("INSERT INTO categories (name, description) VALUES ('TRAJES DE BAÑO', 'Modelos terminados'), ('ROPA DEPORTIVA', 'Licras, tops, etc.'), ('INSUMOS', 'Telas, hilos, elásticas'), ('EQUIPAMIENTO', 'Lentes, gorros, aletas'), ('ACTIVOS', 'Maquinaria y mobiliario')");
            }

            System.out.println("✅ SICONI LISTO: Base de datos sincronizada y cargada.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertSupplier(PreparedStatement ps, String cia, String cto, String tlf, String mail, String dir, String insta, String what) throws SQLException {
        ps.setString(1, cia); ps.setString(2, cto); ps.setString(3, tlf); ps.setString(4, mail); ps.setString(5, dir); ps.setString(6, insta); ps.setString(7, what);
        ps.addBatch();
    }

    private static void actualizarEstructura(Connection conn) {
        String[] colsSales = {"invoice_nro", "control_nro", "bank", "payment_date"};
        try (Statement stmt = conn.createStatement()) {
            DatabaseMetaData meta = conn.getMetaData();
            for (String col : colsSales) {
                try (ResultSet rs = meta.getColumns(null, null, "sales", col)) {
                    if (!rs.next()) stmt.execute("ALTER TABLE sales ADD COLUMN " + col + " TEXT DEFAULT ''");
                }
            }
            // Verificar columnas de proveedores por si acaso es una BD vieja
            try (ResultSet rs = meta.getColumns(null, null, "suppliers", "instagram")) {
                if (!rs.next()) stmt.execute("ALTER TABLE suppliers ADD COLUMN instagram TEXT DEFAULT ''");
            }
            try (ResultSet rs = meta.getColumns(null, null, "suppliers", "whatsapp")) {
                if (!rs.next()) stmt.execute("ALTER TABLE suppliers ADD COLUMN whatsapp TEXT DEFAULT ''");
            }
        } catch (SQLException e) {}
    }

    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }
}