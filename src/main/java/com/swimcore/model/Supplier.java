/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Programación III / Proyecto de Software
 *
 * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Supplier.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Enero 2026
 * VERSIÓN: 1.0.0 (Stable Release)
 *
 * DESCRIPCIÓN TÉCNICA:
 * Clase de la Capa de Modelo (Model Layer) que representa la entidad 'Proveedor'.
 * Actúa como un POJO (Plain Old Java Object) diseñado para el transporte de datos
 * entre la base de datos SQLite y la interfaz de usuario.
 *
 * Características de Ingeniería:
 * 1. Mapeo Objeto-Relacional (ORM Manual): Estructura diseñada para coincidir con
 * el esquema de la tabla 'suppliers', facilitando la extracción y persistencia de datos.
 * 2. Extensión de Atributos: Incluye campos para redes sociales, permitiendo una
 * integración omnicanal en la gestión de contactos.
 * 3. Sobreescritura de Métodos: Implementa `toString()` para facilitar el renderizado
 * en componentes Swing como JComboBox.
 *
 * PRINCIPIOS POO:
 * - ENCAPSULAMIENTO: Uso estricto de modificadores de acceso `private` y métodos
 * descriptivos para la mutación y acceso a los datos.
 * - HERENCIA: Sobreescritura (Override) del método `toString` de la clase base `Object`.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * Entidad de Proveedor.
 * Centraliza la información corporativa y de contacto de los aliados comerciales.
 */
public class Supplier {

    // --- ATRIBUTOS PRIVADOS ---
    private int id;
    private String company;    // Razón Social / Empresa
    private String contact;    // Persona de contacto
    private String phone;
    private String email;
    private String address;

    // ATRIBUTOS DE CONECTIVIDAD SOCIAL (Valor agregado al modelo)
    private String instagram;
    private String whatsapp;

    /**
     * Constructor por defecto.
     * Necesario para la instanciación dinámica y frameworks de persistencia.
     */
    public Supplier() {}

    /**
     * Constructor sobrecargado para inicialización completa.
     * Utilizado para la recuperación de registros existentes desde la base de datos.
     * * @param id Identificador único (PK).
     * @param company Nombre de la empresa.
     * @param contact Nombre del enlace directo.
     * @param phone Número telefónico.
     * @param email Correo electrónico corporativo.
     * @param address Ubicación física.
     * @param instagram Identificador de red social.
     * @param whatsapp Enlace o número de mensajería instantánea.
     */
    public Supplier(int id, String company, String contact, String phone, String email, String address, String instagram, String whatsapp) {
        this.id = id;
        this.company = company;
        this.contact = contact;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.instagram = instagram;
        this.whatsapp = whatsapp;
    }

    // --- MÉTODOS DE ACCESO (GETTERS Y SETTERS) ---

    /** @return El ID autonumérico del proveedor. */
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    /** @return Nombre comercial de la empresa. */
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    /** @return Nombre de la persona encargada de ventas/atención. */
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    /**
     * Sobreescritura del metodo toString.
     * Esencial para que los componentes JComboBox muestren el nombre de la empresa
     * en lugar del espacio de memoria del objeto.
     * * @return El nombre de la compañía.
     */
    @Override
    public String toString() {
        return company;
    }
}