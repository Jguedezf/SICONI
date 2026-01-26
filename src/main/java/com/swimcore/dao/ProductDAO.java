package com.swimcore.dao;

import com.swimcore.model.Category;
import com.swimcore.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ProductDAO {

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
        } catch (SQLException e) { return false; }
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
        String sqlUpdate = "UPDATE products SET current_stock = current_stock + ? WHERE id = ?";
        String sqlHistory = "INSERT INTO inventory_movements (product_id, quantity, type, observation) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdate);
                 PreparedStatement psHis = conn.prepareStatement(sqlHistory)) {
                psUpd.setInt(1, quantity);
                psUpd.setInt(2, productId);
                psUpd.executeUpdate();
                psHis.setInt(1, productId);
                psHis.setInt(2, Math.abs(quantity));
                psHis.setString(3, (quantity > 0) ? "ENTRADA" : "SALIDA");
                psHis.setString(4, observation.toUpperCase());
                psHis.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) { conn.rollback(); return false; }
        } catch (SQLException e) { return false; }
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products ORDER BY name ASC")) {
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setCode(rs.getString("code"));
                p.setName(rs.getString("name"));
                p.setSalePrice(rs.getDouble("sale_price"));
                p.setCurrentStock(rs.getInt("current_stock"));
                list.add(p);
            }
        } catch (SQLException e) {}
        return list;
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
                    int n = Integer.parseInt(last.split("-")[1]) + 1;
                    return String.format("%s-%03d", prefix, n);
                }
            }
        } catch (Exception e) {}
        return prefix + "-001";
    }

    public Product getProductById(int id) {
        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM products WHERE id = ?")) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Product(rs.getInt("id"), rs.getString("code"), rs.getString("name"), rs.getString("description"),
                        rs.getDouble("cost_price"), rs.getDouble("sale_price"), rs.getInt("current_stock"),
                        rs.getInt("min_stock"), rs.getInt("category_id"), rs.getInt("supplier_id"), rs.getString("image_path"));
            }
        } catch (Exception e) {}
        return null;
    }

    public List<Vector<Object>> getHistoryByDateRange(java.util.Date from, java.util.Date to) {
        List<Vector<Object>> data = new ArrayList<>();
        String sql = "SELECT m.id, m.date, p.code, p.name, m.type, m.quantity, m.observation FROM inventory_movements m " +
                "LEFT JOIN products p ON m.product_id = p.id WHERE m.date BETWEEN ? AND ? ORDER BY m.date DESC";
        try (Connection conn = Conexion.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pstmt.setString(1, sdf.format(from));
            pstmt.setString(2, sdf.format(to));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt(1)); row.add(rs.getString(2)); row.add(rs.getString(3));
                row.add(rs.getString(4)); row.add(rs.getString(5)); row.add(rs.getInt(6)); row.add(rs.getString(7));
                data.add(row);
            }
        } catch (SQLException e) {}
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