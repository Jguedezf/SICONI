/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * CARRERA: Ingeniería en Informática
 * ASIGNATURA: Técnicas de Programación III
 * * PROYECTO: GESTIÓN DE INVENTARIO DE UNA TIENDA (SICONI)
 * ARCHIVO: Supplier.java
 *
 * AUTORA: Johanna Guedez - V14089807
 * PROFESORA: Ing. Dubraska Roca
 * FECHA: Febrero 2026
 * VERSIÓN: 1.0.1 (Data Model Extension)
 * -----------------------------------------------------------------------------
 * DESCRIPCIÓN TÉCNICA:
 * Clase perteneciente a la Capa de Modelo (Model Layer) que representa la entidad
 * 'Proveedor'. Actúa como un contenedor de datos (Data Carrier) para facilitar
 * el flujo de información entre el motor de persistencia SQLite y la lógica
 * de adquisición de suministros.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

/**
 * [MODELO - ENTIDAD] Representación lógica de un proveedor de insumos o productos.
 * [POO - ENCAPSULAMIENTO] Implementa la protección de datos mediante el uso de
 * atributos privados y métodos de acceso públicos, garantizando la integridad
 * de la información de contacto y canales digitales.
 */
public class Supplier {

    // ========================================================================================
    //                                  ATRIBUTOS (DATOS PROTEGIDOS)
    // ========================================================================================

    // Identificador único del registro (Clave Primaria).
    private int id;

    // Razón social o nombre comercial de la organización proveedora.
    private String company;

    // Persona de contacto o representante legal.
    private String contact;

    // Datos de contacto telefónico y electrónico.
    private String phone;
    private String email;

    // Ubicación geográfica de la sede o depósito.
    private String address;

    // Canales de comunicación digital y redes sociales (Requerimiento de conectividad).
    private String instagram;
    private String whatsapp;

    // ========================================================================================
    //                                  CONSTRUCTORES
    // ========================================================================================

    /**
     * Constructor por defecto.
     * Requerido para procesos de instanciación dinámica y mapeo relacional.
     */
    public Supplier() { }

    /**
     * Constructor parametrizado para la inicialización completa de la entidad.
     * Facilita la creación de objetos de transferencia de datos (DTO) desde la base de datos.
     *
     * @param id Identificador único.
     * @param company Razón social.
     * @param contact Nombre del contacto.
     * @param phone Teléfono principal.
     * @param email Correo electrónico.
     * @param address Dirección física.
     * @param instagram Enlace a red social.
     * @param whatsapp Número de mensajería instantánea.
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

    // ========================================================================================
    //                                  MÉTODOS DE ACCESO (GETTERS Y SETTERS)
    // ========================================================================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

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
     * [POLIMORFISMO - SOBREESCRITURA] Redefine el comportamiento del objeto
     * al ser representado como cadena de texto, retornando el nombre comercial.
     * Esencial para la correcta visualización en componentes visuales (JComboBox).
     */
    @Override
    public String toString() { return company; }
}