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
 * VERSIÓN: 1.2.0 (Stable Persistence Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase Principal (Entry Point) encargada de la inicialización del sistema.
 * Implementa lógica de persistencia protegida para asegurar que los cambios
 * realizados por el usuario no se pierdan entre sesiones de ejecución.
 *
 * PRINCIPIOS POO:
 * - ABSTRACCIÓN: Delega la complejidad de persistencia a métodos auxiliares.
 * - ENCAPSULAMIENTO: Métodos de configuración privados para proteger la lógica de arranque.
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
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Clase ejecutora del sistema SICONI.
 */
public class Main {

    /**
     * Punto de entrada de la JVM.
     */
    public static void main(String[] args) {
        // 1. CONFIGURACIÓN VISUAL (LOOK AND FEEL)
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 14);
        } catch (Exception e) { e.printStackTrace(); }

        // 2. CAPA DE PERSISTENCIA: VERIFICACIÓN E INICIALIZACIÓN
        Connection conn = Conexion.conectar();

        if (conn != null) {
            // Ejecuta la rutina de verificación y carga de productos
            createInventoryTables(conn);

            // Verificación del usuario administrador
            UserDAO userDAO = new UserDAO();
            if (userDAO.findByUsername("admin") == null) {
                userDAO.saveUser(new User("admin", "1234", "Johanna Guedez", "ADMIN"));
            }
        }

        // 3. INICIO DEL HILO DE EVENTOS DE UI
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }

    /**
     * Define el esquema DDL y realiza una carga de datos condicionada (DML).
     * @param conn Objeto Connection activo.
     */
    private static void createInventoryTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            // --- BLOQUE DDL (Data Definition Language) ---
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, name TEXT NOT NULL, description TEXT, cost_price REAL, sale_price REAL, current_stock INTEGER DEFAULT 0, min_stock INTEGER DEFAULT 5, category_id INTEGER, supplier_id INTEGER, image_path TEXT, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");

            // --- BLOQUE DML PROTEGIDO (DATA SEEDING) ---
            // CORRECCIÓN: Eliminamos el DELETE para activar la persistencia.
            // Solo insertamos si la tabla de productos está totalmente vacía.
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("🚀 SICONI: Base de datos vacía. Inyectando inventario inicial...");

                // Inserción de Categorías
                stmt.execute("INSERT OR IGNORE INTO categories (name) VALUES ('Modelos'), ('Textiles'), ('Insumos'), ('Activos'), ('Mercería')");

                // Inserción de Proveedores
                stmt.execute("INSERT OR IGNORE INTO suppliers (company) VALUES ('SICONI Producción'), ('Telas El Castillo'), ('Insumos Guayana'), ('Singer Service')");

                // --- PRODUCTOS: MODELOS DAYANA GUÉDEZ ---
                insertProduct(conn, "MOD-001", "Traje de Baño Dama", "Clásico", 15, 35, 10, 1, 1);
                insertProduct(conn, "MOD-002", "Enterizo Manga Corta", "Colección Invierno", 20, 45, 8, 1, 1);
                insertProduct(conn, "MOD-003", "Bikini Triángulo Neón", "Verano 2026", 12, 28, 15, 1, 1);
                insertProduct(conn, "MOD-004", "Boxer Caballero", "Lycra Deportiva", 10, 22, 12, 1, 1);
                insertProduct(conn, "MOD-005", "Faski Dama", "Ajuste Pro", 18, 38, 5, 1, 1);
                insertProduct(conn, "MOD-006", "Faski Caballero", "Compresión Alta", 18, 38, 7, 1, 1);
                insertProduct(conn, "MOD-007", "Traje de Baño Monokini", "Corte Asimétrico", 22, 50, 4, 1, 1);

                // --- PRODUCTOS: INSUMOS Y HERRAMIENTAS ---
                insertProduct(conn, "INS-001", "Tiza de Sastre", "Marcaje textil", 0.5, 1, 50, 3, 3);
                insertProduct(conn, "INS-002", "Tijera Profesional", "Acero Inoxidable", 15, 0, 6, 3, 3);
                insertProduct(conn, "INS-003", "Cortador de Tela", "Circular 45mm", 25, 0, 3, 3, 3);
                insertProduct(conn, "INS-004", "Agujas Stretch #9", "Paquete x10", 5, 0, 20, 3, 3);
                insertProduct(conn, "INS-005", "Agujas Universal #11", "Paquete x10", 4, 0, 15, 3, 3);

                // --- PRODUCTOS: MERCERÍA Y TEXTILES ---
                insertProduct(conn, "MER-001", "Hilos Negro", "Cono 4000yd", 3, 0, 25, 5, 2);
                insertProduct(conn, "MER-002", "Hilos Blanco", "Cono 4000yd", 3, 0, 25, 5, 2);
                insertProduct(conn, "MAT-001", "Tela Lycra (Metro)", "Fucsia Neón", 6, 12, 40, 2, 2);
                insertProduct(conn, "MAT-002", "Tela Forro (Metro)", "Microfibra", 3.5, 7, 60, 2, 2);

                // --- PRODUCTOS: ACTIVOS (MÁQUINAS) ---
                insertProduct(conn, "ACT-001", "Máquina Singer Ind.", "Recta", 450, 0, 1, 4, 4);
                insertProduct(conn, "ACT-002", "Overlock Industrial", "4 Hilos", 600, 0, 1, 4, 4);
                insertProduct(conn, "ACT-003", "Máquina Collaretera", "Recubridora", 750, 0, 1, 4, 4);

                System.out.println("✅ SICONI: Inventario inicial cargado exitosamente.");
            } else {
                System.out.println("💾 SICONI: Cargando base de datos persistente.");
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Metodo utilitario para inserción segura de productos.
     */
    private static void insertProduct(Connection conn, String code, String name, String desc, double cost, double sale, int stock, int catId, int supId) throws Exception {
        String sql = "INSERT INTO products (code, name, description, cost_price, sale_price, current_stock, min_stock, category_id, supplier_id, image_path) VALUES (?, ?, ?, ?, ?, ?, 5, ?, ?, '')";
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, name);
            pstmt.setString(3, desc);
            pstmt.setDouble(4, cost);
            pstmt.setDouble(5, sale);
            pstmt.setInt(6, stock);
            pstmt.setInt(7, catId);
            pstmt.setInt(8, supId);
            pstmt.executeUpdate();
        }
    }
}