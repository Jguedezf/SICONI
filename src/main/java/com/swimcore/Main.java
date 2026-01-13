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
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase Principal (Entry Point) de la aplicación SICONI.
 * Actúa como orquestador inicial del ciclo de vida del software. Sus responsabilidades incluyen:
 * 1. Inicialización del motor de interfaz gráfica (FlatLaf) para la gestión del Look & Feel.
 * 2. Verificación de conectividad con la capa de persistencia (SQLite) mediante patrón Singleton.
 * 3. Ejecución de rutinas DDL (Data Definition Language) para la construcción del esquema relacional.
 * 4. Ejecución de rutinas DML (Data Manipulation Language) para la inyección de datos semilla (Data Seeding).
 * 5. Despacho del hilo de eventos (EDT) para garantizar la seguridad de hilos (Thread-Safety) en Swing.
 *
 * PRINCIPIOS DE PROGRAMACIÓN ORIENTADA A OBJETOS (POO):
 * 1. COMPOSICIÓN: Compone el flujo inicial instanciando objetos de acceso a datos (UserDAO) y Vistas (LoginView).
 * 2. ENCAPSULAMIENTO: Los métodos de configuración de base de datos son `private static`,
 * ocultando la complejidad de la implementación SQL al contexto global.
 * 3. ABSTRACCIÓN: Delega la lógica de conexión a la clase utilitaria `Conexion` y la autenticación a `UserDAO`.
 *
 * PATRONES DE DISEÑO IMPLEMENTADOS:
 * - Facade (Implícito): Simplifica el subsistema de arranque (UI + DB + Auth) en una única llamada.
 * -----------------------------------------------------------------------------
 */

package com.swimcore;

import com.formdev.flatlaf.FlatDarkLaf;
import com.swimcore.dao.Conexion;
import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;
import com.swimcore.view.LoginView;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Clase ejecutora del sistema.
 * Contiene el método main() y las rutinas de inicialización de infraestructura.
 */
public class Main {

    /**
     * Punto de entrada de la JVM (Java Virtual Machine).
     * @param args Argumentos de línea de comandos (sin uso en esta versión).
     */
    public static void main(String[] args) {
        // ---------------------------------------------------------------------
        // 1. CAPA DE PRESENTACIÓN: CONFIGURACIÓN VISUAL (LOOK AND FEEL)
        // ---------------------------------------------------------------------
        try {
            // Inyección del Look and Feel FlatDark para modernizar los componentes Swing.
            UIManager.setLookAndFeel(new FlatDarkLaf());

            // Personalización de propiedades globales del UIManager (Arcos y Scrollbars)
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 12);
        } catch (Exception e) {
            // Manejo silencioso de excepciones de UI
        }

        // ---------------------------------------------------------------------
        // 2. CAPA DE PERSISTENCIA: VERIFICACIÓN E INICIALIZACIÓN
        // ---------------------------------------------------------------------
        Connection conn = Conexion.conectar();

        if (conn != null) {
            // Inicialización de esquema de base de datos (DDL) y datos maestros
            createInventoryTables(conn);

            // Instancia del DAO para verificación de existencia de usuarios
            UserDAO userDAO = new UserDAO();

            // Lógica de Negocio: Creación del Superusuario (Admin) por defecto si no existe (Data Seeding)
            if (userDAO.findByUsername("admin") == null) {
                userDAO.saveUser(new User("admin", "1234", "Johanna Guedez", "ADMIN"));
            }
        }

        // ---------------------------------------------------------------------
        // 3. INICIO DEL CICLO DE VIDA DE LA UI (THREAD SAFETY)
        // ---------------------------------------------------------------------
        // Swing no es Thread-Safe. Todas las manipulaciones de UI deben hacerse en el
        // Event Dispatch Thread (EDT). 'invokeLater' encola esta tarea en el EDT.
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }

    /**
     * Método auxiliar privado para la definición del esquema de base de datos (DDL).
     * Verifica la existencia de tablas y las crea si es necesario.
     * También inyecta datos maestros iniciales (Categorías, Proveedores).
     *
     * @param conn Objeto Connection activo.
     */
    private static void createInventoryTables(Connection conn) {
        // Uso de try-with-resources para asegurar el cierre del Statement
        try (Statement stmt = conn.createStatement()) {

            // --- BLOQUE DDL (Data Definition Language) ---

            // Tabla Categorías
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");

            // Tabla Proveedores
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");

            // Tabla Productos (Relacional con FKs)
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, name TEXT NOT NULL, description TEXT, cost_price REAL, sale_price REAL, current_stock INTEGER DEFAULT 0, min_stock INTEGER DEFAULT 5, category_id INTEGER, supplier_id INTEGER, image_path TEXT, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");

            // --- BLOQUE DML (Data Manipulation Language) - DATA SEEDING ---

            // Verificación: Solo insertar si la tabla está vacía para evitar duplicados
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("⚡ Creando datos maestros...");

                // Inserción de Categorías Base
                stmt.execute("INSERT INTO categories (name) VALUES ('Modelos Referencia'), ('Textiles'), ('Mercería'), ('Branding'), ('Equipos Taller'), ('Accesorios')");

                // Inserción de Proveedores Base
                stmt.execute("INSERT INTO suppliers (company) VALUES ('SICONI Producción'), ('Telas El Castillo'), ('Tramas'), ('Taguapire'), ('Titanes Gráficos'), ('Singer Servicio'), ('Importadora Sport')");

                // Inserción de Productos Base usando método auxiliar seguro
                insertar(conn, "MOD-001", "Traje de Baño Dama", "Clásico", 15, 35, 3, 1, 1);
                insertar(conn, "MAT-001", "Tela Lycra (Metro)", "Alta densidad", 4, 0, 50, 2, 2);
                insertar(conn, "MER-001", "Hilos (Cono)", "Poliéster", 2.5, 0, 20, 3, 4);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Método utilitario para inserción segura de registros.
     * Utiliza PreparedStatement para prevenir inyección SQL.
     */
    private static void insertar(Connection conn, String c, String n, String d, double cost, double sale, int st, int cat, int sup) throws Exception {
        String sql = "INSERT INTO products (code, name, description, cost_price, sale_price, current_stock, min_stock, category_id, supplier_id, image_path) VALUES (?, ?, ?, ?, ?, ?, 5, ?, ?, '')";

        try (java.sql.PreparedStatement p = conn.prepareStatement(sql)) {
            // Mapeo de parámetros
            p.setString(1, c);
            p.setString(2, n);
            p.setString(3, d);
            p.setDouble(4, cost);
            p.setDouble(5, sale);
            p.setInt(6, st);
            p.setInt(7, cat);
            p.setInt(8, sup);

            // Ejecución
            p.executeUpdate();
        }
    }
}