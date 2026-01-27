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
 * VERSIÓN: 2.0.1 (Refactored to Centralized DB Setup)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase ejecutora. Se refactorizó para delegar la creación y migración
 * de la base de datos a la clase utilitaria 'DatabaseSetup', siguiendo
 * el principio de Responsabilidad Única.
 * -----------------------------------------------------------------------------
 */

package com.swimcore;

import com.formdev.flatlaf.FlatDarkLaf;
import com.swimcore.dao.Conexion;
import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;
import com.swimcore.util.DatabaseSetup; // <-- IMPORTAMOS LA NUEVA CLASE
import com.swimcore.view.LoginView;
import javax.swing.*;
import java.sql.Connection;

public class Main {

    public static void main(String[] args) {
        // 1. CONFIGURACIÓN VISUAL (LOOK AND FEEL)
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 14);
            ToolTipManager.sharedInstance().setInitialDelay(0);
        } catch (Exception e) { e.printStackTrace(); }

        // 2. CAPA DE PERSISTENCIA
        // Obtenemos la conexión para asegurar que la BD está activa
        Connection conn = Conexion.conectar();
        if (conn != null) {
            // Llamada única a la clase encargada de la base de datos
            DatabaseSetup.inicializarBD();

            // Verificación del usuario administrador (se mantiene aquí)
            UserDAO userDAO = new UserDAO();
            if (userDAO.findByUsername("admin") == null) {
                userDAO.saveUser(new User("admin", "1234", "Johanna Guedez", "ADMIN"));
            }
        }

        // Hook para cerrar la conexión de forma segura al salir del programa
        Runtime.getRuntime().addShutdownHook(new Thread(Conexion::cerrar));

        // 3. ARRANQUE DE LA INTERFAZ GRÁFICA
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}