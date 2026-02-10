/*
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * PROYECTO: SICONI
 * ARCHIVO: ProductDAO.java
 * VERSIÓN: 2.0 (FINAL STABLE - MERGED)
 * DESCRIPCIÓN: Acceso a datos unificado. Corrige errores de duplicidad y
 * asegura la consistencia entre las alertas del menú y el inventario real.
 */

package com.swimcore.dao;

import com.swimcore.model.Category;
import com.swimcore.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ProductDAO {

    /**
     * Cuenta cuántos productos tienen stock crítico para el Botón de Alerta.
     * Lógica: Stock Actual <= Stock Mínimo.
     */
    public int getAlertCount() {
        String sql = "SELECT COUNT(*) FROM products WHERE current_stock <= min_stock";
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error contando alertas: " + e.getMessage());
        }
        return 0;
    }

    public List<Product> getLowStockProducts() {
        List<Product> list = new ArrayList<>();
        // Aseguramos el mismo criterio que getAlertCount
        String sql = "SELECT * FROM products WHERE current_stock <= min_stock ORDER BY current_stock ASC";
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
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

            // Manejo de nulos para claves foráneas
            if (p.getCategoryId() > 0) pstmt.setInt(8, p.getCategoryId());
            else pstmt.setObject(8, null);

            if (p.getSupplierId() > 0) pstmt.setInt(9, p.getSupplierId());
            else pstmt.setObject(9, null);

            pstmt.setString(10, p.getImagePath());

            if (p.getId() != 0) pstmt.setInt(11, p.getId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStockDelta(int id, int delta) {
        // Validación en SQL: No permite stock negativo
        String sql = "UPDATE products SET current_stock = current_stock + ? WHERE id = ? AND (current_stock + ?) >= 0";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, delta);
            pstmt.setInt(2, id);
            pstmt.setInt(3, delta);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * AUDITORÍA STOCK: Transacción segura para modificar stock y guardar historial.
     */
    public boolean auditStock(int productId, int quantity, String observation) {
        String sqlTable = "CREATE TABLE IF NOT EXISTS inventory_movements (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER, quantity INTEGER, type TEXT, observation TEXT, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String sqlUpdate = "UPDATE products SET current_stock = current_stock + ? WHERE id = ?";
        String sqlHistory = "INSERT INTO inventory_movements (product_id, quantity, type, observation) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexion.conectar()) {
            conn.setAutoCommit(false); // Inicio Transacción

            try (Statement stmt = conn.createStatement()) { stmt.execute(sqlTable); }

            try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdate);
                 PreparedStatement psHis = conn.prepareStatement(sqlHistory)) {

                // 1. Actualizar Producto
                psUpd.setInt(1, quantity);
                psUpd.setInt(2, productId);
                int rows = psUpd.executeUpdate();

                if (rows == 0) throw new SQLException("Producto no encontrado o error de escritura.");

                // 2. Insertar Historial
                psHis.setInt(1, productId);
                psHis.setInt(2, Math.abs(quantity));
                psHis.setString(3, (quantity > 0) ? "ENTRADA" : "SALIDA");
                psHis.setString(4, observation == null ? "MOVIMIENTO MANUAL" : observation.toUpperCase());
                psHis.executeUpdate();

                conn.commit(); // Guardar cambios
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Cancelar si falla algo
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMinStock(int id, int newMinStock) {
        String sql = "UPDATE products SET min_stock = ? WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newMinStock);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Product> searchProducts(String term) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE id = ? OR name LIKE ? OR code LIKE ?";
        try (Connection conn = Conexion.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, term);
            pstmt.setString(2, "%" + term + "%");
            pstmt.setString(3, "%" + term + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapProduct(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products ORDER BY name ASC")) {
            while (rs.next()) list.add(mapProduct(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Product getProductById(int id) {
        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM products WHERE id = ?")) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return mapProduct(rs);
        } catch (Exception e) {}
        return null;
    }

    // Método auxiliar para no repetir código de mapeo
    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCode(rs.getString("code"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setCostPrice(rs.getDouble("cost_price"));
        p.setSalePrice(rs.getDouble("sale_price"));
        p.setCurrentStock(rs.getInt("current_stock"));
        p.setMinStock(rs.getInt("min_stock")); // Si es NULL, Java lo convierte a 0
        p.setCategoryId(rs.getInt("category_id"));
        p.setSupplierId(rs.getInt("supplier_id"));
        p.setImagePath(rs.getString("image_path"));
        return p;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM categories ORDER BY name ASC")) {
            while (rs.next()) list.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
        } catch (SQLException e) {}
        return list;
    }

    public String generateSmartCode(String prefix) {
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement("SELECT code FROM products WHERE code LIKE ? ORDER BY id DESC LIMIT 1")) {
            pstmt.setString(1, prefix + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String last = rs.getString("code");
                if (last.contains("-")) {
                    try {
                        int n = Integer.parseInt(last.split("-")[1]) + 1;
                        return String.format("%s-%03d", prefix, n);
                    } catch(Exception ex) { return prefix + "-001"; }
                }
            }
        } catch (Exception e) {}
        return prefix + "-001";
    }

    public List<Vector<Object>> getHistoryByDateRange(java.util.Date from, java.util.Date to) {
        List<Vector<Object>> data = new ArrayList<>();
        String sql = "SELECT m.id, m.date, p.code, p.name, m.type, m.quantity, m.observation " +
                "FROM inventory_movements m " +
                "LEFT JOIN products p ON m.product_id = p.id " +
                "WHERE m.date BETWEEN ? AND ? " +
                "ORDER BY m.date DESC, m.id DESC";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pstmt.setString(1, sdf.format(from));
            pstmt.setString(2, sdf.format(to));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt(1));
                row.add(rs.getString(2));
                row.add(rs.getString(3));
                row.add(rs.getString(4));
                row.add(rs.getString(5));
                row.add(rs.getInt(6));
                row.add(rs.getString(7));
                data.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    public boolean delete(int id) {
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}