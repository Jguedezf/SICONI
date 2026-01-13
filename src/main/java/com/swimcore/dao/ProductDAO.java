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
 * VERSIÓN: 1.1.0 (Refactored Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Acceso a Datos (DAO) para la entidad 'Producto'.
 * Actúa como el puente de persistencia principal del sistema, orquestando la
 * comunicación entre el Modelo de Objetos y la base de datos relacional SQLite.
 *
 * Características de Ingeniería:
 * 1. Implementación de Lógica Dual (Save/Update): Centraliza la persistencia
 * mediante una estructura condicional que decide entre instrucciones DML INSERT o UPDATE.
 * 2. Algoritmo de Generación de Identificadores: Incluye un generador de códigos
 * incrementales inteligentes (SmartCodes) para optimizar el registro de catálogo.
 * 3. Gestión de Integridad Referencial: Maneja nulidad y vinculación de Claves
 * Foráneas (FK) hacia Categorías y Proveedores.
 *
 * PRINCIPIOS POO:
 * - ABSTRACCIÓN: Aísla la complejidad de las consultas SQL de las vistas de inventario.
 * - ENCAPSULAMIENTO: Gestiona de forma privada el ciclo de vida de los objetos
 * `Connection`, `PreparedStatement` y `ResultSet`.
 *
 * PATRONES DE DISEÑO IMPLEMENTADOS:
 * - DAO (Data Access Object): Proporciona una interfaz limpia para la persistencia.
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
 * Proporciona servicios avanzados para la administración del catálogo de SICONI.
 */
public class ProductDAO {

    /**
     * Persistencia Persistente (Create / Update).
     * Sincroniza el estado de un objeto Product con su registro correspondiente en la BD.
     * @param p Instancia del producto a guardar o actualizar.
     * @return true si la transacción SQL fue exitosa.
     */
    public boolean save(Product p) {
        String sql;
        // Lógica de decisión basada en la existencia del ID (Clave Primaria)
        if (p.getId() == 0) {
            // Operación DDL: Inserción de nuevo registro
            sql = "INSERT INTO products (code, name, description, cost_price, sale_price, current_stock, min_stock, category_id, supplier_id, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            // Operación DDL: Actualización de registro existente
            sql = "UPDATE products SET code=?, name=?, description=?, cost_price=?, sale_price=?, current_stock=?, min_stock=?, category_id=?, supplier_id=?, image_path=? WHERE id=?";
        }

        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Mapeo de parámetros transaccionales (Protección contra Inyección SQL)
            pstmt.setString(1, p.getCode());
            pstmt.setString(2, p.getName());
            pstmt.setString(3, p.getDescription());
            pstmt.setDouble(4, p.getCostPrice());
            pstmt.setDouble(5, p.getSalePrice());
            pstmt.setInt(6, p.getCurrentStock());
            pstmt.setInt(7, p.getMinStock());

            // Gestión de Relaciones Opcionales (Manejo de Nulos en FK)
            if (p.getCategoryId() > 0) pstmt.setInt(8, p.getCategoryId()); else pstmt.setObject(8, null);
            if (p.getSupplierId() > 0) pstmt.setInt(9, p.getSupplierId()); else pstmt.setObject(9, null);

            pstmt.setString(10, p.getImagePath());

            // En caso de UPDATE, se inyecta el ID como parámetro de filtrado (posición 11)
            if (p.getId() != 0) {
                pstmt.setInt(11, p.getId());
            }

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error en persistencia de producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Eliminación de Registro (Delete).
     * @param id Identificador único del producto a remover.
     * @return true si el registro fue eliminado correctamente.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Recuperación por Identificador (Read by ID).
     * @param id Clave primaria a consultar.
     * @return Objeto Product poblado o null si no se encuentra.
     */
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Mapeo Objeto-Relacional (Hydration)
                return new Product(
                        rs.getInt("id"), rs.getString("code"), rs.getString("name"),
                        rs.getString("description"), rs.getDouble("cost_price"), rs.getDouble("sale_price"),
                        rs.getInt("current_stock"), rs.getInt("min_stock"),
                        rs.getInt("category_id"), rs.getInt("supplier_id"), rs.getString("image_path")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Recuperación Maestra de Categorías.
     * Utilizado para poblar componentes de selección (ComboBoxes) en la UI.
     * @return Lista de entidades Category registradas.
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
     * Analiza el último código registrado con un prefijo dado y genera el correlativo siguiente.
     * Ejemplo: Si recibe "BIK" y existe "BIK-005", retorna "BIK-006".
     * @param prefix Siglas identificadoras (Ej: BIK, TEXT).
     * @return Nuevo código formateado.
     */
    public String generateSmartCode(String prefix) {
        // Consulta del último registro con patrón LIKE
        String sql = "SELECT code FROM products WHERE code LIKE ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String lastCode = rs.getString("code");
                // Lógica de parsing y parsing incremental
                if (lastCode.contains("-")) {
                    String numberPart = lastCode.split("-")[1];
                    int nextNum = Integer.parseInt(numberPart) + 1;
                    return String.format("%s-%03d", prefix, nextNum); // Formato con ceros a la izquierda
                }
            }
        } catch (Exception e) { }
        // Fallback: Generación del primer correlativo del prefijo
        return prefix + "-001";
    }
}