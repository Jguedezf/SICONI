/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Técnicas de Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Conexion.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: 06 de Febrero de 2026
 * VERSIÓN: 3.0.0 (TURBO: WAL Mode + Cache Tuning + Foreign Keys)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Módulo central de conectividad para el motor de base de datos relacional
 * SQLite. Implementa una arquitectura de conexión optimizada que garantiza la
 * persistencia de datos mediante el controlador JDBC, incorporando directivas
 * de bajo nivel para maximizar el rendimiento de entrada/salida (I/O).
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * [DAO - INFRAESTRUCTURA] Gestor centralizado de conexión SQLite.
 * [PATRÓN DE DISEÑO: SINGLETON] Provee un único punto de acceso sincronizado
 * a la instancia de conexión, optimizando el consumo de recursos de memoria.
 * [INGENIERÍA DE RENDIMIENTO] Implementa optimizaciones de motor para
 * concurrencia y velocidad de escritura.
 */
public class Conexion {

    // ========================================================================================
    //                                  ATRIBUTOS DE CONFIGURACIÓN
    // ========================================================================================

    // Cadena de conexión JDBC para el archivo de base de datos local.
    private static final String URL = "jdbc:sqlite:siconi.db";

    // Instancia persistente de la conexión (Objeto Singleton).
    private static Connection con = null;

    /**
     * Constructor privado para restringir la instanciación externa.
     * Garantiza el cumplimiento del patrón Singleton.
     */
    private Conexion() {}

    // ========================================================================================
    //                                  GESTIÓN DE CONECTIVIDAD
    // ========================================================================================

    /**
     * [MÉTODO SINCRONIZADO] Establece y configura la conexión con el motor de base de datos.
     * Realiza una inyección de parámetros PRAGMA para elevar el rendimiento del motor SQLite
     * por encima de los valores predeterminados.
     * * @return Objeto Connection activo y configurado.
     */
    public static synchronized Connection conectar() {
        // Supresión de logs redundantes para limpieza del flujo de diagnóstico.
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");

        try {
            // Evaluación del estado de la conexión para reutilización o reapertura.
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(URL);

                // --- OPTIMIZACIÓN DE MOTOR (MODO ALTO RENDIMIENTO) ---
                // Se ejecutan directivas específicas de SQLite para mejorar la experiencia de usuario.
                try (Statement stmt = con.createStatement()) {

                    // 1. INTEGRIDAD REFERENCIAL: Activa la validación de llaves foráneas (FK).
                    stmt.execute("PRAGMA foreign_keys = ON;");

                    // 2. MODO WAL (Write-Ahead Logging): Mejora la concurrencia permitiendo
                    // lecturas y escrituras simultáneas sin bloqueos de archivo.
                    stmt.execute("PRAGMA journal_mode = WAL;");

                    // 3. SYNCHRONOUS NORMAL: Optimiza los ciclos de escritura en disco duro.
                    stmt.execute("PRAGMA synchronous = NORMAL;");

                    // 4. MEMORY CACHE: Incrementa el tamaño de la caché de páginas en RAM
                    // (10,000 páginas) para reducir la latencia de acceso al disco.
                    stmt.execute("PRAGMA cache_size = 10000;");
                }

                System.out.println("🚀 SICONI: Base de Datos conectada en MODO ALTO RENDIMIENTO.");
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR CRÍTICO DE CONEXIÓN: " + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    /**
     * Libera los recursos de conexión de forma controlada.
     */
    public static void cerrar() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("🔒 SICONI: Conexión cerrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}