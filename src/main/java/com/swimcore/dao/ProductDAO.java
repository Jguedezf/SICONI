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
 * VERSIÓN: 1.1.2 (Persistent Delta Integration)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Acceso a Datos (DAO) para la entidad 'Producto'.
 * Implementa la persistencia integral mediante SQLite, soportando operaciones
 * CRUD, ajustes incrementales de stock (Delta Logic) y automatización de catálogos.
 *
 * PRINCIPIOS POO APLICADOS:
 * - ABSTRACCIÓN: Aísla la complejidad de las consultas SQL del resto del sistema.
 * - ENCAPSULAMIENTO: Gestiona de forma privada el ciclo de vida de los objetos
 * Connection y PreparedStatement mediante try-with-resources.
 * - RESPONSABILIDAD ÚNICA: La clase se dedica exclusivamente a la persistencia de productos.
 *
 * PATRONES DE DISEÑO:
 * - Data Access Object (DAO): Proporciona una interfaz limpia para el acceso a datos.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.dao;

import com.swimcore.model.Category;
import com.swimcore.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de Persistencia para Productos.
 * Proporciona servicios avanzados para la administración del inventario de SICONI.
 */
public class ProductDAO {

    /**
     * Persistencia Dual (Create / Update).
     * Decide automáticamente entre INSERT o UPDATE basándose en la existencia del ID.
     * @param p Instancia del producto a persistir.
     * @return true si la transacción SQL fue exitosa.
     */
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

            // Manejo de Claves Foráneas opcionales (Integridad Referencial)
            if (p.getCategoryId() > 0) pstmt.setInt(8, p.getCategoryId()); else pstmt.setObject(8, null);
            if (p.getSupplierId() > 0) pstmt.setInt(9, p.getSupplierId()); else pstmt.setObject(9, null);

            pstmt.setString(10, p.getImagePath());

            if (p.getId() != 0) {
                pstmt.setInt(11, p.getId());
            }

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error en save ProductDAO: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ajuste incremental de existencias (Delta Logic).
     * Permite sumar o restar unidades directamente en la BD para mayor concurrencia.
     * @param id Identificador del producto.
     * @param delta Valor entero (positivo para sumar, negativo para restar).
     * @return true si la operación respeta la restricción de stock no negativo (>=0).
     */
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

    /**
     * Recuperación por Identificador.
     * @param id Clave primaria a consultar.
     * @return Objeto Product mapeado o null si no existe.
     */
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Product(
                        rs.getInt("id"), rs.getString("code"), rs.getString("name"),
                        rs.getString("description"), rs.getDouble("cost_price"), rs.getDouble("sale_price"),
                        rs.getInt("current_stock"), rs.getInt("min_stock"),
                        rs.getInt("category_id"), rs.getInt("supplier_id"), rs.getString("image_path")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Eliminación Física de Registro.
     * @param id Identificador del producto a remover.
     * @return true si el registro fue eliminado correctamente.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /**
     * Consulta Maestra de Categorías.
     * @return Lista de objetos Category registrados.
     */
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = Conexion.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }
        } catch (SQLException e) { }
        return list;
    }

    /**
     * Algoritmo de Generación de SmartCodes.
     * Genera automáticamente el siguiente código correlativo basado en un prefijo.
     * @param prefix Siglas de la categoría (Ej: MOD, INS).
     * @return Nuevo código formateado (Ej: MOD-005).
     */
    public String generateSmartCode(String prefix) {
        String sql = "SELECT code FROM products WHERE code LIKE ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String lastCode = rs.getString("code");
                if (lastCode.contains("-")) {
                    int nextNum = Integer.parseInt(lastCode.split("-")[1]) + 1;
                    return String.format("%s-%03d", prefix, nextNum);
                }
            }
        } catch (Exception e) { }
        return prefix + "-001";
    }
}