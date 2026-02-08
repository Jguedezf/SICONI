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
 * 1. Inicializa el Look & Feel (FlatLaf) para la estética "Luxury".
 * 2. Verifica la conexión a BD y crea tablas si no existen (Persistencia).
 * 3. Garantiza que exista al menos un usuario ADMIN (Seguridad Fail-Safe).
 * 4. Lanza la interfaz gráfica (LoginView) en el hilo de despacho de eventos.
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

/**
 * Clase principal que contiene el metodo main.
 * Actúa como punto de entrada para la ejecución de la JVM.
 */
public class Main {

    /**
     * Metodo principal de ejecución.
     * Coordina la inicialización de capas: Vista, Persistencia y Lógica de Negocio.
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {

        // 1. CONFIGURACIÓN VISUAL (LOOK AND FEEL)
        // Se establece la librería FlatLaf Dark para cumplir con la identidad visual "Luxury".
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Estilos redondeados globales para componentes Swing
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 14);
            ToolTipManager.sharedInstance().setInitialDelay(0);
        } catch (Exception e) { e.printStackTrace(); }

        // 2. CAPA DE PERSISTENCIA Y SEGURIDAD
        // Se verifica la conectividad con SQLite antes de lanzar la interfaz.
        Connection conn = Conexion.conectar();
        if (conn != null) {
            // Inicialización de Tablas: Se asegura que el esquema relacional exista.
            DatabaseSetup.inicializarBD();

            // -------------------------------------------------------
            // ⚠ ZONA DE CARGA DE DATOS (Data Seeder) ⚠
            // Descomentar SOLO si necesitas borrar todo y crear datos desde cero.
            // -------------------------------------------------------

            // DataSeeder.reiniciarYSembrar();

            // -------------------------------------------------------

            // GARANTÍA DE ACCESO (FAIL-SAFE):
            // Se consulta al DAO para verificar si existen usuarios.
            // Si la BD está vacía, se inyecta un Admin por defecto para evitar bloqueo del sistema.
            UserDAO userDAO = new UserDAO();
            if (userDAO.findByUsername("admin") == null) {
                System.out.println("SICONI: Creando usuario 'admin' por defecto...");
                // Creamos el usuario con el ROL exacto que usa tu nuevo sistema
                userDAO.saveUser(new User("admin", "1234", "Johanna Guedez", "ADMIN"));
            }
        }

        // [GESTIÓN DE RECURSOS] Hook para cerrar la conexión de forma segura al apagar la JVM.
        Runtime.getRuntime().addShutdownHook(new Thread(Conexion::cerrar));

        // 3. ARRANQUE DE LA INTERFAZ GRÁFICA
        // [CONCURRENCIA] Se utiliza invokeLater para garantizar que la UI se ejecute en el Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}