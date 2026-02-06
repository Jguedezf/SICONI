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
 * FECHA: 06 de Febrero de 2026 - 01:15 PM
 * VERSIÓN: 2.5.0 (FINAL RUN - Security Enabled)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase ejecutora (Entry Point).
 * 1. Inicializa el Look & Feel (FlatLaf).
 * 2. Verifica la conexión a BD y crea tablas si no existen.
 * 3. Garantiza que exista al menos un usuario ADMIN.
 * 4. Lanza la interfaz gráfica (LoginView).
 * -----------------------------------------------------------------------------
 */

package com.swimcore;

import com.formdev.flatlaf.FlatDarkLaf;
import com.swimcore.dao.Conexion;
import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;
import com.swimcore.util.DatabaseSetup;
// import com.swimcore.util.DataSeeder; // Mantener comentado salvo para resetear
import com.swimcore.view.LoginView;
import javax.swing.*;
import java.sql.Connection;

public class Main {

    public static void main(String[] args) {
        // 1. CONFIGURACIÓN VISUAL (LOOK AND FEEL)
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Estilos redondeados globales
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 14);
            ToolTipManager.sharedInstance().setInitialDelay(0);
        } catch (Exception e) { e.printStackTrace(); }

        // 2. CAPA DE PERSISTENCIA Y SEGURIDAD
        Connection conn = Conexion.conectar();
        if (conn != null) {
            // Inicializar Tablas
            DatabaseSetup.inicializarBD();

            // -------------------------------------------------------
            // ⚠ ZONA DE CARGA DE DATOS (Data Seeder) ⚠
            // Descomentar SOLO si necesitas borrar todo y crear datos desde cero.
            // -------------------------------------------------------

            // DataSeeder.reiniciarYSembrar();

            // -------------------------------------------------------

            // GARANTÍA DE ACCESO: Crear Admin si la base de datos está vacía
            UserDAO userDAO = new UserDAO();
            if (userDAO.findByUsername("admin") == null) {
                System.out.println("SICONI: Creando usuario 'admin' por defecto...");
                // Creamos el usuario con el ROL exacto que usa tu nuevo sistema
                userDAO.saveUser(new User("admin", "1234", "Johanna Guedez", "ADMIN"));
            }
        }

        // Hook para cerrar la conexión de forma segura al apagar
        Runtime.getRuntime().addShutdownHook(new Thread(Conexion::cerrar));

        // 3. ARRANQUE DE LA INTERFAZ GRÁFICA
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}