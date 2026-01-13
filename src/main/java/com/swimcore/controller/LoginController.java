/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: LoginController.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Controlador (Controller Layer).
 * Actúa como mediador entre la interfaz de autenticación (LoginView) y la capa
 * de persistencia (UserDAO). Su función principal es gestionar la lógica de
 * verificación de identidad y acceso al sistema.
 *
 * Características de Ingeniería:
 * 1. Desacoplamiento: Aísla la lógica de validación de la interfaz gráfica,
 * siguiendo el principio de Responsabilidad Única (SRP).
 * 2. Gestión de Flujo: Coordina la recuperación de la entidad 'User' y realiza
 * la comparación de credenciales en memoria.
 * 3. Escalabilidad: Estructura preparada para integrar procesos de encriptación
 * o hashing de contraseñas sin afectar otras capas.
 *
 * PRINCIPIOS POO:
 * - COMPOSICIÓN: Utiliza una instancia de `UserDAO` para delegar el acceso a datos.
 * - ENCAPSULAMIENTO: Centraliza la lógica de negocio del inicio de sesión.
 *
 * PATRONES DE DISEÑO:
 * - MVC (Model-View-Controller): Implementa el rol de controlador para la gestión
 * de estados de autenticación.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;

/**
 * Controlador de Seguridad.
 * Proporciona los servicios de validación de credenciales para el acceso al sistema SICONI.
 */
public class LoginController {

    // Dependencia inyectada de la Capa de Datos
    private final UserDAO userDAO = new UserDAO();

    /**
     * Valida las credenciales proporcionadas por el usuario.
     * 1. Consulta la existencia del usuario en la base de datos.
     * 2. Compara la integridad de la contraseña suministrada.
     *
     * @param username Identificador de cuenta.
     * @param password Credencial de acceso ingresada.
     * @return true si las credenciales coinciden con el registro de persistencia.
     */
    public boolean validateCredentials(String username, String password) {
        // Recuperación de la entidad desde el almacén de datos
        User user = userDAO.findByUsername(username);

        if (user != null) {
            // LÓGICA DE VALIDACIÓN:
            // Comparamos el texto ingresado con el atributo del modelo.
            // Nota: En versiones futuras, se recomienda implementar BCrypt o SHA aquí.
            return password.equals(user.getPassword());
        }

        // Caso: El usuario no existe o las credenciales son inválidas
        return false;
    }
}