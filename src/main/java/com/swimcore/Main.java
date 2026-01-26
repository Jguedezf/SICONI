/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Main.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.7.0 (Full Database Migration)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase ejecutora.
 * - MIGRACIÓN COMPLETA: Se agregaron bloques para actualizar tanto la tabla
 * 'clients' como la tabla 'sales' sin perder datos existentes.
 * -----------------------------------------------------------------------------
 */

package com.swimcore;

import com.formdev.flatlaf.FlatDarkLaf;
import com.swimcore.dao.Conexion;
import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;
import com.swimcore.view.LoginView;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        // 1. CONFIGURACIÓN VISUAL (LOOK AND FEEL)
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 14);
            ToolTipManager.sharedInstance().setInitialDelay(0);
        } catch (Exception e) { e.printStackTrace(); }

        // 2. CAPA DE PERSISTENCIA
        Connection conn = Conexion.conectar();

        if (conn != null) {
            createInventoryTables(conn);

            UserDAO userDAO = new UserDAO();
            if (userDAO.findByUsername("admin") == null) {
                userDAO.saveUser(new User("admin", "1234", "Johanna Guedez", "ADMIN"));
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(Conexion::cerrar));

        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }

    private static void createInventoryTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            // --- BLOQUE DDL (Creación de Tablas Nuevas) ---

            // 1. CLIENTES
            stmt.execute("CREATE TABLE IF NOT EXISTS clients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "code TEXT, " +
                    "id_type TEXT, " +
                    "id_number TEXT, " +
                    "full_name TEXT, " +
                    "phone TEXT, " +
                    "email TEXT, " +
                    "address TEXT, " +
                    "instagram TEXT, " +
                    "is_vip INTEGER, " +
                    "athlete_name TEXT, " +
                    "birth_date TEXT, " +
                    "club_name TEXT, " +
                    "category TEXT, " +
                    "measurements TEXT)");

            // 2. OTRAS TABLAS
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, name TEXT NOT NULL, description TEXT, cost_price REAL, sale_price REAL, current_stock INTEGER DEFAULT 0, min_stock INTEGER DEFAULT 5, category_id INTEGER, supplier_id INTEGER, image_path TEXT, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");

            // 3. VENTAS (Definición Completa)
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "id TEXT PRIMARY KEY, " +
                    "date TEXT, " +
                    "client_id TEXT, " +
                    "total_divisa REAL, " +
                    "amount_paid_usd REAL, " + // Campo Nuevo
                    "balance_due_usd REAL, " + // Campo Nuevo
                    "total_bs REAL, " +
                    "rate REAL, " +
                    "currency TEXT, " +
                    "payment_method TEXT, " +
                    "reference_number TEXT, " +
                    "status TEXT, " +          // Campo Nuevo
                    "observations TEXT, " +    // Campo Nuevo
                    "delivery_date TEXT)");    // Campo Nuevo

            stmt.execute("CREATE TABLE IF NOT EXISTS sale_details (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT, product_id INTEGER, quantity INTEGER, unit_price REAL, subtotal REAL, FOREIGN KEY(sale_id) REFERENCES sales(id), FOREIGN KEY(product_id) REFERENCES products(id))");

            // --- PARCHE DE MIGRACIÓN AUTOMÁTICA (CRÍTICO) ---
            // Intenta agregar columnas faltantes a tablas existentes.

            // MIGRACIÓN CLIENTES
            try { stmt.execute("ALTER TABLE clients ADD COLUMN address TEXT"); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE clients ADD COLUMN email TEXT"); } catch (Exception e) {}

            // MIGRACIÓN VENTAS (NUEVO - Para que funcione el módulo Atelier)
            try { stmt.execute("ALTER TABLE sales ADD COLUMN amount_paid_usd REAL"); System.out.println("🔧 MIGRACIÓN: amount_paid_usd agregado."); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE sales ADD COLUMN balance_due_usd REAL"); System.out.println("🔧 MIGRACIÓN: balance_due_usd agregado."); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE sales ADD COLUMN status TEXT"); System.out.println("🔧 MIGRACIÓN: status agregado."); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE sales ADD COLUMN observations TEXT"); System.out.println("🔧 MIGRACIÓN: observations agregado."); } catch (Exception e) {}
            try { stmt.execute("ALTER TABLE sales ADD COLUMN delivery_date TEXT"); System.out.println("🔧 MIGRACIÓN: delivery_date agregado."); } catch (Exception e) {}

            // --- DATA SEEDING ---
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT OR IGNORE INTO categories (name) VALUES ('Modelos'), ('Textiles'), ('Insumos'), ('Activos'), ('Mercería'), ('Accesorios')");
                stmt.execute("INSERT OR IGNORE INTO suppliers (company) VALUES ('SICONI Producción'), ('Telas El Castillo'), ('Insumos Guayana'), ('Singer Service')");

                insertProduct(conn, "MOD-001", "Traje de Baño Dama", "Clásico", 15.00, 35.00, 10, 1, 1);
                insertProduct(conn, "MOD-002", "Enterizo Manga Corta", "Colección Invierno", 20.00, 45.00, 8, 1, 1);
                insertProduct(conn, "MOD-003", "Bikini Triángulo Neón", "Verano 2026", 12.00, 28.00, 15, 1, 1);
                insertProduct(conn, "ACC-001", "Gorro de Natación", "Silicona Profesional", 5.00, 12.00, 20, 6, 2);

                System.out.println("✅ SICONI: Inventario inicial cargado exitosamente.");
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void insertProduct(Connection conn, String code, String name, String desc, double cost, double sale, int stock, int catId, int supId) throws Exception {
        String sql = "INSERT INTO products (code, name, description, cost_price, sale_price, current_stock, min_stock, category_id, supplier_id, image_path) VALUES (?, ?, ?, ?, ?, ?, 5, ?, ?, '')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code); pstmt.setString(2, name); pstmt.setString(3, desc);
            pstmt.setDouble(4, cost); pstmt.setDouble(5, sale); pstmt.setInt(6, stock);
            pstmt.setInt(7, catId); pstmt.setInt(8, supId);
            pstmt.executeUpdate();
        }
    }
}