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
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Controlador (Controller Layer).
 * Funge como el orquestador de seguridad encargado de validar el acceso al
 * ecosistema SICONI. Coordina la comunicación entre la vista de entrada
 * (LoginView) y el proveedor de datos (UserDAO) para verificar la identidad
 * de los operadores del sistema.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.controller;

import com.swimcore.dao.UserDAO;
import com.swimcore.model.User;

/**
 * [CONTROLLER - SEGURIDAD] Controlador para la Gestión de Autenticación.
 * [DISEÑO] Implementa el patrón MVC al aislar la lógica de validación de
 * identidad de los componentes de la interfaz de usuario.
 * [INGENIERÍA] Sigue el principio de Responsabilidad Única (SRP), delegando
 * la recuperación de registros al objeto DAO y limitándose a la lógica de comparación.
 */
public class LoginController {

    // ========================================================================================
    //                                  ATRIBUTOS (DEPENDENCIAS)
    // ========================================================================================

    // Dependencia de la Capa de Persistencia (Composición).
    private final UserDAO userDAO = new UserDAO();

    // ========================================================================================
    //                                  LÓGICA DE NEGOCIO (SECURITY)
    // ========================================================================================

    /**
     * [ALGORITMO DE VALIDACIÓN] Verifica las credenciales de acceso suministradas.
     * Realiza una búsqueda por nombre de usuario y evalúa la paridad de la contraseña
     * en memoria para determinar el acceso a la sesión.
     * * @param username Identificador nominal de la cuenta.
     * @param password Credencial secreta de acceso suministrada en el formulario.
     * @return booleano: true si el perfil existe y las credenciales son íntegras.
     */
    public boolean validateCredentials(String username, String password) {

        // Fase de Recuperación: Se consulta la entidad 'User' a través de la capa de persistencia.
        User user = userDAO.findByUsername(username);

        // Fase de Verificación: Se evalúa la existencia del registro y la coincidencia de claves.
        if (user != null) {

            /* * TÉCNICO: Se realiza una comparación de cadenas (String Comparison).
             * NOTA DE ESCALABILIDAD: La arquitectura permite sustituir esta lógica por
             * funciones de Hashing (BCrypt/SHA) sin alterar la capa de Vista.
             */
            return password.equals(user.getPassword());
        }

        // Flujo Alterno: Fallo de autenticación por usuario inexistente o credenciales erróneas.
        return false;
    }
}