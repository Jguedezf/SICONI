/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Client.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.1.0 (Extended Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Modelo (Model Layer) que representa la entidad 'Cliente'.
 * Implementa el patrón POJO (Plain Old Java Object) para el transporte de datos
 * entre la persistencia SQLite y los componentes visuales de la galería de clientes.
 *
 * Características de Ingeniería:
 * 1. Mapeo de Atributos Extendidos: Incorpora lógica para la gestión de lealtad
 * (VIP Status) y marketing relacional (Birthdate Tracking).
 * 2. Gestión de Tipos de Entidad: Soporta una clasificación polimórfica interna
 * (ATLETA, REPRESENTANTE, CLUB) para adaptar la lógica de negocio según el perfil.
 * 3. Integridad de Datos: Proporciona una estructura de campos de texto (measurements)
 * para el almacenamiento de datos biométricos necesarios en la confección de trajes de baño.
 *
 * PRINCIPIOS POO:
 * - ENCAPSULAMIENTO: Atributos privados con métodos accesores públicos para garantizar
 * que el estado interno del objeto solo se modifique mediante la interfaz definida.
 * - ABSTRACCIÓN: Modela al cliente no solo como un comprador, sino como una entidad
 * técnica con medidas y perfiles específicos.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * Entidad de Cliente.
 * Centraliza la información personal, técnica y de fidelización de los clientes de SICONI.
 */
public class Client {

    // --- ATRIBUTOS PRIVADOS (IDENTIDAD Y CONTACTO) ---
    private String id;          // Cédula o identificador técnico (DG-CLI-001)
    private String name;        // Nombre completo o razón social
    private String phone;       // Contacto telefónico
    private String type;        // Clasificación: ATLETA, REPRESENTANTE, CLUB
    private String email;       // Correo electrónico de contacto

    // --- ATRIBUTOS DE NEGOCIO (BIOMETRÍA Y FIDELIZACIÓN) ---
    private String measurements;// Resumen de medidas corporales para producción
    private String birthDate;   // Fecha de nacimiento (Formato estándar ISO 8601: YYYY-MM-DD)
    private boolean isVip;      // Flag de fidelización para beneficios exclusivos

    /**
     * Constructor por defecto.
     * Requerido para la instanciación dinámica y compatibilidad con DAOs.
     */
    public Client() {
    }

    /**
     * Constructor completo para inicialización rápida.
     * @param id Identificador de cuenta.
     * @param name Nombre o denominación.
     * @param phone Número de contacto.
     * @param type Perfil de cliente.
     * @param email Dirección electrónica.
     * @param measurements Datos biométricos.
     * @param birthDate Fecha de onomástico.
     * @param isVip Estado de cliente preferencial.
     */
    public Client(String id, String name, String phone, String type, String email, String measurements, String birthDate, boolean isVip) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.email = email;
        this.measurements = measurements;
        this.birthDate = birthDate;
        this.isVip = isVip;
    }

    // --- MÉTODOS DE ACCESO (GETTERS Y SETTERS) ---

    /** @return El identificador de cuenta del cliente. */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /** @return El nombre legal o comercial. */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** @return Número telefónico registrado. */
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    /** @return Clasificación del perfil (ATLETA/CLUB). */
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    /** @return Correo electrónico validado. */
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /** @return Cadena con el resumen de medidas técnicas. */
    public String getMeasurements() { return measurements; }
    public void setMeasurements(String measurements) { this.measurements = measurements; }

    /** @return Fecha de nacimiento almacenada. */
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    /** @return true si el cliente posee estatus VIP. */
    public boolean isVip() { return isVip; }
    public void setVip(boolean vip) { isVip = vip; }
}