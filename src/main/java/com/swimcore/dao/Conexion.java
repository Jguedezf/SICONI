package com.swimcore.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion {

    private static final String URL = "jdbc:sqlite:siconi.db";
    private static Connection con = null;

    public static Connection conectar() {
        try {
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(URL);
                Statement stmt = con.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON;");

                // PASO CRÍTICO: Crear tablas si no existen
                crearTablasSiNoExisten(stmt);

                // PASO CRÍTICO 2: Verificar si están vacías e insertar datos
                if (estaVaciaLaTablaProductos(stmt)) {
                    System.out.println("✨ Tabla productos vacía. INYECTANDO INVENTARIO DG SWIMWEAR...");
                    insertarDatosIniciales();
                }

                stmt.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error conectando: " + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    public static void cerrar() {
        try {
            if (con != null && !con.isClosed()) con.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void crearTablasSiNoExisten(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password_hash TEXT, full_name TEXT, role TEXT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS clientes (id_cliente TEXT PRIMARY KEY, nombre TEXT, telefono TEXT, email TEXT, tipo TEXT, fecha_nacimiento TEXT, es_vip INTEGER, medidas_resumen TEXT)");

        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos (" +
                "id_producto TEXT PRIMARY KEY, " +
                "nombre TEXT, " +
                "descripcion TEXT, " +
                "categoria TEXT, " +
                "precio_venta REAL, " +
                "stock INTEGER, " +
                "stock_minimo INTEGER, "+
                "proveedor TEXT, " +
                "ruta_imagen TEXT)";
        stmt.execute(sqlProductos);

        stmt.execute("CREATE TABLE IF NOT EXISTS ventas (id_venta INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT, id_cliente TEXT, total REAL, metodo_pago TEXT, estado TEXT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS detalle_venta (id_detalle INTEGER PRIMARY KEY AUTOINCREMENT, id_venta INTEGER, id_producto TEXT, cantidad INTEGER, precio_unitario REAL, personalizacion TEXT)");
    }

    private static boolean estaVaciaLaTablaProductos(Statement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM productos")) {
            if (rs.next()) {
                return rs.getInt(1) == 0; // Devuelve true si la cuenta es 0
            }
        }
        return false;
    }

    // --- CARGA MASIVA DEL INVENTARIO REAL ---
    private static void insertarDatosIniciales() {
        String sql = "INSERT INTO productos (id_producto, nombre, descripcion, categoria, precio_venta, stock, stock_minimo, proveedor, ruta_imagen) VALUES (?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            con.setAutoCommit(false);

            // 1. EQUIPAMIENTO (Taller)
            addItem(pst, "EQP-TIJ1", "Tijera de Sastre Premium", "Acero Inox 8\", Mango Ergonómico", "EQUIPAMIENTO", 0, 2, 1, "Ferretería Industrial", "/images/scissors.png");
            addItem(pst, "EQP-COR1", "Cortador Rotativo 45mm", "Para cortes precisos en Lycra", "EQUIPAMIENTO", 0, 1, 1, "Mercería Online", "/images/scissors.png");
            addItem(pst, "EQP-MAQ1", "Máquina Recta Industrial", "Jack A4 - Activo Principal", "EQUIPAMIENTO", 0, 3, 0, "Casa del Costurero", "/images/inventory.png");

            // 2. INSUMOS (Consumibles)
            addItem(pst, "INS-H01", "Hilo Poliéster Blanco", "Cono industrial 5000 yds", "INSUMO", 0, 10, 3, "Castillo de las Telas", "/images/spool.png");
            addItem(pst, "INS-H02", "Hilo Poliéster Negro", "Cono industrial 5000 yds", "INSUMO", 0, 8, 3, "Castillo de las Telas", "/images/spool.png");
            addItem(pst, "INS-LYC1", "Tela Lycra Power Negra", "Rollo de 50mts, Alto Rendimiento", "INSUMO", 0, 50, 10, "Texland Import", "/images/inventory.png");
            addItem(pst, "INS-DTF1", "Lámina Logos DG (DTF)", "Carta, 20 logos por lámina", "INSUMO", 0, 15, 5, "Titanes Gráficos", "/images/inventory.png");
            addItem(pst, "INS-BOL1", "Bolsas Boutique DG", "Plásticas, biodegradables, con asa", "INSUMO", 0, 100, 20, "Imprenta Digital", "/images/inventory.png");

            // 3. PRODUCTOS TERMINADOS (Catálogo)
            addItem(pst, "TRA-D01", "Enterizo Dama Clásico", "Espalda cruzada, forro completo", "PRODUCTO_TERMINADO", 35.0, 10, 2, "Taller DG", "/images/swimsuit.png");
            addItem(pst, "TRA-BA01", "Boxer Competición Adulto", "Lycra resistente al cloro, ajuste pro", "PRODUCTO_TERMINADO", 25.0, 15, 3, "Taller DG", "/images/swimsuit.png");
            addItem(pst, "TRA-FAS1", "Fastskin Elite (Kneeskin)", "Tecnología de compresión avanzada", "PRODUCTO_TERMINADO", 120.0, 2, 1, "Taller DG", "/images/swimsuit.png");
            addItem(pst, "TRA-NIN1", "Traje Niña Estampado", "Colores vibrantes, tallas 4-12", "PRODUCTO_TERMINADO", 28.0, 8, 2, "Taller DG", "/images/swimsuit.png");

            pst.executeBatch();
            con.commit();
            con.setAutoCommit(true);
            System.out.println("✅ ¡Inventario DG cargado exitosamente!");
        } catch (SQLException e) {
            e.printStackTrace();
            try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private static void addItem(PreparedStatement pst, String id, String nom, String desc, String cat, double pre, int stk, int min, String prov, String img) throws SQLException {
        pst.setString(1, id); pst.setString(2, nom); pst.setString(3, desc); pst.setString(4, cat);
        pst.setDouble(5, pre); pst.setInt(6, stk); pst.setInt(7, min); pst.setString(8, prov); pst.setString(9, img);
        pst.addBatch();
    }
}