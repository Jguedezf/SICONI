/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: ProductDAO.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.8.0 (Audit Reading Added)
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Category;
import com.swimcore.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector; // Import necesario para el retorno del historial

/**
 * Gestor de Persistencia Avanzado para Productos SICONI.
 * Incluye reparador de esquema para errores de tablas o columnas faltantes.
 */
public class ProductDAO {

    /**
     * MÉTODO AÑADIDO PARA VINCULACIÓN INICIAL:
     * Valida la conexión y asegura que la estructura básica exista al arrancar.
     * Esto permite que el Main muestre el mensaje de éxito.
     */
    public boolean testConnection() {
        String sqlAudit = "CREATE TABLE IF NOT EXISTS inventory_movements (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, quantity INTEGER, type TEXT, observation TEXT, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement()) {
            if (conn == null) return false;
            stmt.execute(sqlAudit);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean save(Product p) {
        String sql = (p.getId() == 0)
                ? "INSERT INTO products (code, name, description, cost_price, sale_price, current_stock, min_stock, category_id, supplier_id, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE products SET code=?, name=?, description=?, cost_price=?, sale_price=?, current_stock=?, min_stock=?, category_id=?, supplier_id=?, image_path=? WHERE id=?";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getCode());
            pstmt.setString(2, p.getName());
            pstmt.setString(3, p.getDescription());
            pstmt.setDouble(4, p.getCostPrice());
            pstmt.setDouble(5, p.getSalePrice());
            pstmt.setInt(6, p.getCurrentStock());
            pstmt.setInt(7, p.getMinStock());

            if (p.getCategoryId() > 0) pstmt.setInt(8, p.getCategoryId()); else pstmt.setObject(8, null);
            if (p.getSupplierId() > 0) pstmt.setInt(9, p.getSupplierId()); else pstmt.setObject(9, null);

            pstmt.setString(10, p.getImagePath());
            if (p.getId() != 0) pstmt.setInt(11, p.getId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error Crítico en save: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStockDelta(int id, int delta) {
        String sql = "UPDATE products SET current_stock = current_stock + ? WHERE id = ? AND (current_stock + ?) >= 0";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, delta);
            pstmt.setInt(2, id);
            pstmt.setInt(3, delta);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean auditStock(int productId, int quantity, String observation) {
        // Bloque de Auto-Reparación: Crea la tabla de movimientos si no existe
        String sqlAuditTable = "CREATE TABLE IF NOT EXISTS inventory_movements (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, quantity INTEGER, type TEXT, observation TEXT, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String updateStockSql = "UPDATE products SET current_stock = current_stock + ? WHERE id = ?";
        String insertHistorySql = "INSERT INTO inventory_movements (product_id, quantity, type, observation) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexion.conectar()) {
            conn.setAutoCommit(false);

            // Asegurar que la tabla de movimientos exista físicamente
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlAuditTable);
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateStockSql);
                 PreparedStatement psHistory = conn.prepareStatement(insertHistorySql)) {

                // 1. Actualizar el stock del producto
                psUpdate.setInt(1, quantity);
                psUpdate.setInt(2, productId);
                int updatedRows = psUpdate.executeUpdate();

                if (updatedRows == 0) throw new SQLException("Producto no encontrado.");

                // 2. Registrar el movimiento en el historial
                psHistory.setInt(1, productId);
                psHistory.setInt(2, Math.abs(quantity));
                psHistory.setString(3, (quantity > 0) ? "ENTRADA" : "SALIDA");
                psHistory.setString(4, observation.trim().toUpperCase());
                psHistory.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Falla en Transacción de Auditoría: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) { return false; }
    }

    // --- NUEVO MÉTODO AÑADIDO PARA CONSULTA DE HISTORIAL (AUDITORÍA FASE 1) ---
    /**
     * Recupera el historial de movimientos filtrado por fechas.
     * Utiliza un JOIN para traer el nombre del producto en lugar de solo su ID.
     * @param from Fecha inicio (java.util.Date)
     * @param to Fecha fin (java.util.Date)
     * @return Lista de vectores para llenado directo de JTable.
     */
    public List<Vector<Object>> getHistoryByDateRange(java.util.Date from, java.util.Date to) {
        List<Vector<Object>> data = new ArrayList<>();
        String sql = "SELECT m.id, m.date, p.code, p.name, m.type, m.quantity, m.observation " +
                "FROM inventory_movements m " +
                "LEFT JOIN products p ON m.product_id = p.id " +
                "WHERE m.date BETWEEN ? AND ? " +
                "ORDER BY m.date DESC";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Formateador simple compatible con SQLite (ISO8601)
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Ajuste de rangos horarios para cubrir el día completo (00:00 a 23:59)
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(from);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0);
            String strFrom = sdf.format(cal.getTime());

            cal.setTime(to);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23); cal.set(java.util.Calendar.MINUTE, 59);
            String strTo = sdf.format(cal.getTime());

            pstmt.setString(1, strFrom);
            pstmt.setString(2, strTo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("id"));
                    row.add(rs.getString("date"));
                    row.add(rs.getString("code"));
                    row.add(rs.getString("name"));
                    row.add(rs.getString("type"));
                    row.add(rs.getInt("quantity"));
                    row.add(rs.getString("observation"));
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    // --- FIN DEL BLOQUE AÑADIDO ---

    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"), rs.getString("code"), rs.getString("name"),
                            rs.getString("description"), rs.getDouble("cost_price"), rs.getDouble("sale_price"),
                            rs.getInt("current_stock"), rs.getInt("min_stock"),
                            rs.getInt("category_id"), rs.getInt("supplier_id"), rs.getString("image_path")
                    );
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name ASC";
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }
        } catch (SQLException e) { }
        return list;
    }

    public String generateSmartCode(String prefix) {
        String sql = "SELECT code FROM products WHERE code LIKE ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String lastCode = rs.getString("code");
                    if (lastCode.contains("-")) {
                        int nextNum = Integer.parseInt(lastCode.split("-")[1]) + 1;
                        return String.format("%s-%03d", prefix, nextNum);
                    }
                }
            }
        } catch (Exception e) { }
        return prefix + "-001";
    }
}