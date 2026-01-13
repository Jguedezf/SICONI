/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: DatabaseSetup.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de utilidad encargada de la configuración e inicialización del motor de base de datos.
 * Utiliza el motor embebido SQLite para la persistencia de datos local.
 *
 * Responsabilidades de Ingeniería:
 * 1. Definición del Esquema (DDL): Crea la estructura de tablas relacionales si no existen
 * en el sistema de archivos local.
 * 2. Integración de Constraints: Implementa Claves Primarias (PK) y Claves Foráneas (FK)
 * para garantizar la Integridad Referencial.
 * 3. Data Seeding: Inyecta registros maestros iniciales (Categorías) mediante la instrucción
 * `INSERT OR IGNORE` para evitar colisiones de datos únicos.
 * 4. Gestión de Recursos JDBC: Implementa el patrón "Try-with-resources" para asegurar
 * el cierre automático de conexiones y liberar recursos del sistema operativo.
 *
 * PRINCIPIOS POO:
 * - ABSTRACCIÓN: Simplifica el proceso complejo de configuración de BD en un único método `inicializarBD()`.
 * - ENCAPSULAMIENTO: Centraliza la cadena de conexión (URL) como una constante privada.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Orquestador de la Infraestructura de Datos.
 * Gestiona el despliegue automático de tablas y datos semilla al arrancar el sistema.
 */
public class DatabaseSetup {

    // Identificador de conexión JDBC para SQLite (Base de datos local en archivo .db)
    private static final String URL = "jdbc:sqlite:siconi.db";

    /**
     * Inicializa la Base de Datos.
     * Realiza las rutinas de verificación y creación de objetos de base de datos.
     */
    public static void inicializarBD() {
        // Uso de try-with-resources para garantizar el cierre del Statement y la Conexión
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            System.out.println("--- VERIFICANDO BASE DE DATOS ---");

            // 1. DEFINICIÓN DE TABLA: CATEGORIAS (Muestra de Entidad Independiente)
            String sqlCategorias = "CREATE TABLE IF NOT EXISTS categorias (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL UNIQUE, " +
                    "descripcion TEXT)";
            stmt.execute(sqlCategorias);

            // 2. DEFINICIÓN DE TABLA: PROVEEDORES (Directorio de Entidades Externas)
            String sqlProveedores = "CREATE TABLE IF NOT EXISTS proveedores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "empresa TEXT NOT NULL, " +
                    "contacto TEXT, " +
                    "telefono TEXT, " +
                    "email TEXT, " +
                    "direccion TEXT)";
            stmt.execute(sqlProveedores);

            // 3. DEFINICIÓN DE TABLA: PRODUCTOS (Entidad Relacional / Dependiente)
            // Implementa Integridad Referencial mediante FOREIGN KEYs hacia Categorías y Proveedores.
            String sqlProductos = "CREATE TABLE IF NOT EXISTS productos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "codigo TEXT UNIQUE, " +
                    "nombre TEXT NOT NULL, " +
                    "descripcion TEXT, " +
                    "precio_costo REAL, " +
                    "precio_venta REAL, " +
                    "stock_actual INTEGER DEFAULT 0, " +
                    "stock_minimo INTEGER DEFAULT 5, " +
                    "id_categoria INTEGER, " +
                    "id_proveedor INTEGER, " +
                    "ruta_imagen TEXT, " +
                    "FOREIGN KEY (id_categoria) REFERENCES categorias(id), " +
                    "FOREIGN KEY (id_proveedor) REFERENCES proveedores(id))";
            stmt.execute(sqlProductos);

            // 4. INYECCIÓN DE DATOS SEMILLA (DATA SEEDING)
            // La instrucción 'INSERT OR IGNORE' es una extensión de SQLite que garantiza
            // la idempotencia de la operación (no duplica si el nombre ya existe).
            stmt.execute("INSERT OR IGNORE INTO categorias (nombre) VALUES ('Bikinis')");
            stmt.execute("INSERT OR IGNORE INTO categorias (nombre) VALUES ('Enterizos')");
            stmt.execute("INSERT OR IGNORE INTO categorias (nombre) VALUES ('Ropa de Playa')");
            stmt.execute("INSERT OR IGNORE INTO categorias (nombre) VALUES ('Insumos/Telas')");

            System.out.println("--- BASE DE DATOS LISTA Y ACTUALIZADA ---");

        } catch (Exception e) {
            // Manejo de excepciones SQL y registro de trazabilidad de errores
            System.out.println("ERROR CREANDO BASE DE DATOS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}