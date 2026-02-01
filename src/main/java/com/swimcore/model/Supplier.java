/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: UNEG - SICONI
 * ARCHIVO: Supplier.java
 * DESCRIPCIÓN: Modelo de datos para proveedores con redes sociales.
 * -----------------------------------------------------------------------------
 */
package com.swimcore.model;

public class Supplier {
    private int id;
    private String company;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private String instagram;
    private String whatsapp;

    public Supplier() { }

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

    // Getters y Setters
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

    @Override
    public String toString() { return company; }
}