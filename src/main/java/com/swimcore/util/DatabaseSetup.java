/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: DatabaseSetup.java
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: 04 de Febrero de 2026 - 04:40 PM
 * VERSIÓN: 4.1.0 (Critical Fix: Schema Evolution)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Módulo de configuración y mantenimiento del esquema relacional. Se encarga de
 * la orquestación de la base de datos SQLite, asegurando la creación de tablas,
 * la gestión de restricciones de integridad y la migración de columnas para
 * soportar la evolución del software sin pérdida de información.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import com.swimcore.dao.Conexion;
import java.sql.*;

/**
 * [UTILIDAD - PERSISTENCIA] Clase responsable de la infraestructura de datos.
 * [POO - ABSTRACCIÓN] Centraliza la lógica de definición de datos (DDL) en un
 * único punto de entrada para el sistema.
 * [REQUERIMIENTO FUNCIONAL] Integridad de Datos: Implementa llaves primarias,
 * foráneas y migraciones de esquema automáticas.
 */
public class DatabaseSetup {

    // ========================================================================================
    //                                  INICIALIZACIÓN DEL MOTOR (DDL)
    // ========================================================================================

    /**
     * [MÉTODO ESTÁTICO] Ejecuta la rutina de verificación y creación de la BD.
     * Implementa la sentencia 'CREATE TABLE IF NOT EXISTS' para garantizar la
     * idempotencia del proceso en cada arranque del sistema.
     */
    public static void inicializarBD() {
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement()) {

            System.out.println("--- SICONI: VERIFICANDO INTEGRIDAD DE DATOS ---");

            // --- FASE 1: DEFINICIÓN DE ENTIDADES MAESTRAS ---
            stmt.execute("CREATE TABLE IF NOT EXISTS clubs (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INTEGER PRIMARY KEY AUTOINCREMENT, company TEXT NOT NULL, contact TEXT, phone TEXT, email TEXT, address TEXT, instagram TEXT, whatsapp TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, full_name TEXT, role TEXT)");

            // [EVOLUCIÓN] Definición de Clientes con soporte extendido para perfiles de atletas (tallas y medidas).
            stmt.execute("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, id_type TEXT, id_number TEXT, full_name TEXT NOT NULL, phone TEXT, email TEXT, address TEXT, instagram TEXT, is_vip INTEGER DEFAULT 0, athlete_name TEXT, birth_date TEXT, club_name TEXT, category TEXT, measurements TEXT, profession TEXT, phone_alt TEXT, size TEXT)");

            // --- FASE 2: DEFINICIÓN DE INVENTARIO Y MOVIMIENTOS ---
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE, name TEXT NOT NULL, description TEXT, cost_price REAL, sale_price REAL, current_stock INTEGER DEFAULT 0, min_stock INTEGER DEFAULT 5, category_id INTEGER, supplier_id INTEGER, image_path TEXT, FOREIGN KEY (category_id) REFERENCES categories(id), FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_movements (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER, quantity INTEGER, type TEXT, observation TEXT, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // --- FASE 3: DEFINICIÓN DE TRANSACCIONES Y PAGOS ---
            // Se establecen llaves foráneas y borrados en cascada para mantener la consistencia operativa.
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (id TEXT PRIMARY KEY, date TEXT, client_id TEXT, total_divisa REAL, amount_paid_usd REAL, balance_due_usd REAL, total_bs REAL, rate REAL, currency TEXT, payment_method TEXT, reference_number TEXT, status TEXT, observations TEXT, delivery_date TEXT, invoice_nro TEXT, control_nro TEXT, bank TEXT, payment_date TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_details (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT, product_id INTEGER, quantity INTEGER, unit_price REAL, subtotal REAL, FOREIGN KEY(sale_id) REFERENCES sales(id), FOREIGN KEY (product_id) REFERENCES products(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS payments (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id TEXT NOT NULL, payment_date TEXT NOT NULL, amount_usd REAL NOT NULL, payment_method TEXT, reference TEXT, notes TEXT, FOREIGN KEY(sale_id) REFERENCES sales(id) ON DELETE CASCADE)");

            // --- FASE 4: MIGRACIONES DINÁMICAS ---
            // Ejecuta lógica de parcheo para bases de datos preexistentes.
            actualizarEstructura(conn);

            // --- FASE 5: SEMBRADO DE CATÁLOGOS (SEEDING) ---
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

    // ========================================================================================
    //                                  GESTIÓN DE MIGRACIONES
    // ========================================================================================

    /**
     * Valida la existencia de campos críticos en instalaciones antiguas.
     * Provee compatibilidad hacia atrás mediante la inyección controlada de columnas.
     */
    private static void actualizarEstructura(Connection conn) {
        checkAndAddColumn(conn, "clients", "size", "TEXT");
        checkAndAddColumn(conn, "clients", "phone_alt", "TEXT");
        checkAndAddColumn(conn, "clients", "profession", "TEXT");
        checkAndAddColumn(conn, "suppliers", "instagram", "TEXT");
        checkAndAddColumn(conn, "suppliers", "whatsapp", "TEXT");
        checkAndAddColumn(conn, "sales", "invoice_nro", "TEXT");
        checkAndAddColumn(conn, "sales", "control_nro", "TEXT");
    }

    /**
     * [MÉTODO AUXILIAR] Utiliza DatabaseMetaData para inspeccionar el esquema en caliente.
     * Si la columna no se encuentra en el ResultSet de metadatos, se procede al ALTER TABLE.
     * * @param table Nombre de la entidad.
     * @param column Nombre del nuevo atributo.
     * @param type Tipo de dato SQL.
     */
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

    /**
     * Verifica si una entidad contiene registros para determinar la necesidad de Seeding.
     */
    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }
}