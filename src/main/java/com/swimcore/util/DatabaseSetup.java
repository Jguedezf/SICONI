/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: DatabaseSetup.java
 * VERSIÓN: 2.6.0 (Clubs & Clients Schema)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import com.swimcore.dao.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    public static void inicializarBD() {
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement()) {

            System.out.println("--- ACTUALIZANDO BASE DE DATOS SICONI ---");

            // 1. TABLA: CLUBES DE NATACIÓN (Lista Fija)
            stmt.execute("CREATE TABLE IF NOT EXISTS clubs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE)");

            // 2. TABLA: CLIENTES (Estructura Representante/Atleta)
            // Se elimina la anterior si existe para evitar conflictos de columnas
            // (ADVERTENCIA: Esto borra clientes previos, ideal para fase desarrollo)
            stmt.execute("DROP TABLE IF EXISTS clients");

            stmt.execute("CREATE TABLE IF NOT EXISTS clients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "code TEXT UNIQUE, " +          // DG-0001
                    "id_type TEXT, " +              // V, E, J
                    "id_number TEXT UNIQUE, " +     // Cédula
                    "full_name TEXT NOT NULL, " +   // Representante
                    "phone TEXT, " +
                    "instagram TEXT, " +
                    "is_vip INTEGER DEFAULT 0, " +
                    "athlete_name TEXT, " +         // Atleta
                    "birth_date TEXT, " +
                    "club_name TEXT, " +
                    "category TEXT, " +
                    "measurements TEXT)");

            // 3. TABLAS DEL SISTEMA (Sin cambios)
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact_person TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, name TEXT NOT NULL, description TEXT, cost_price REAL, sale_price REAL, current_stock INTEGER DEFAULT 0, min_stock INTEGER DEFAULT 5, category_id INTEGER, supplier_id INTEGER, image_path TEXT, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (id INTEGER PRIMARY KEY AUTOINCREMENT, invoice_number TEXT UNIQUE, sale_date TEXT NOT NULL, client_id INTEGER, total_amount_usd REAL, amount_paid_usd REAL, balance_due_usd REAL, total_amount_bs REAL, exchange_rate REAL, currency TEXT, payment_method TEXT, reference_number TEXT, status TEXT, observations TEXT, FOREIGN KEY (client_id) REFERENCES clients(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_details (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id INTEGER, product_id INTEGER, product_name TEXT, quantity INTEGER, unit_price REAL, subtotal REAL, FOREIGN KEY (sale_id) REFERENCES sales(id), FOREIGN KEY (product_id) REFERENCES products(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, full_name TEXT, role TEXT)");

            // --- DATA SEEDING (SEMILLA DE DATOS) ---

            // Admin
            stmt.execute("INSERT OR IGNORE INTO users (username, password, full_name, role) VALUES ('admin', 'admin123', 'Administrador SICONI', 'ADMIN')");

            // CLUBES (Tu Lista)
            stmt.execute("DELETE FROM clubs");
            stmt.execute("INSERT INTO clubs (name) VALUES ('CIVG')");
            stmt.execute("INSERT INTO clubs (name) VALUES ('Cimos')");
            stmt.execute("INSERT INTO clubs (name) VALUES ('Tiburones de Bauxilum')");
            stmt.execute("INSERT INTO clubs (name) VALUES ('Los Raudales')");
            stmt.execute("INSERT INTO clubs (name) VALUES ('Delfines del Lourdes')");
            stmt.execute("INSERT INTO clubs (name) VALUES ('Tritones de CVG')");
            stmt.execute("INSERT INTO clubs (name) VALUES ('La Laja')");
            stmt.execute("INSERT INTO clubs (name) VALUES ('Angostura')");

            // CATEGORÍAS (Swimwear)
            stmt.execute("DELETE FROM categories");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Clásico Tiro Normal', 'Corte estándar')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Clásico Tiro Delgado', 'Tirante fino')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Clásico Tiro Cruzado', 'Espalda cruzada')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Colegial', 'Reglamentario escolar')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Enterizo Deportivo', 'Traje completo')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Kneeskin', 'Competencia rodilla')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Racing Back', 'Espalda de competencia')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Fastskin', 'Tecnología compresión')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Bikini Deportivo', 'Dos piezas')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Monokini', 'Cortes laterales')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Dama - Talla Grande', 'Plus Size')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Caballero - Jammer', 'Short largo')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Caballero - Boxer', 'Short corto')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Caballero - Tanga', 'Brief')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Accesorios - Gorros', 'Gorros')");
            stmt.execute("INSERT INTO categories (name, description) VALUES ('Insumos - Telas', 'Materia prima')");

            System.out.println("--- OK: ESTRUCTURA DE DATOS ACTUALIZADA ---");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}