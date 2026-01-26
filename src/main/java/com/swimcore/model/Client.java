/*
 * -----------------------------------------------------------------------------
 * INSTITUCIÓN: Universidad Nacional Experimental de Guayana (UNEG)
 * ARCHIVO: Client.java
 * VERSIÓN: 2.1.0 (Address Field Added)
 * DESCRIPCIÓN: Se agregó el campo 'address' (Dirección) al modelo.
 * -----------------------------------------------------------------------------
 */

package com.swimcore.model;

public class Client {

    // --- IDENTIFICACIÓN DEL SISTEMA ---
    private int id;
    private String code;

    // --- DATOS DEL REPRESENTANTE ---
    private String idType;
    private String idNumber;
    private String fullName;
    private String phone;
    private String email;
    private String instagram;
    private boolean isVip;
    private String address; // <--- NUEVO CAMPO AGREGADO

    // --- DATOS DEL ATLETA ---
    private String athleteName;
    private String birthDate;
    private String club;
    private String category;

    // --- DATOS TÉCNICOS ---
    private String measurements;

    public Client() {
    }

    // Constructor Actualizado con Address
    public Client(int id, String code, String idType, String idNumber, String fullName,
                  String phone, String email, String instagram, boolean isVip,
                  String athleteName, String birthDate, String club, String category,
                  String measurements, String address) { // <--- Recibimos Address
        this.id = id;
        this.code = code;
        this.idType = idType;
        this.idNumber = idNumber;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.instagram = instagram;
        this.isVip = isVip;
        this.athleteName = athleteName;
        this.birthDate = birthDate;
        this.club = club;
        this.category = category;
        this.measurements = measurements;
        this.address = address; // <--- Asignamos
    }

    // --- GETTERS Y SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }

    public boolean isVip() { return isVip; }
    public void setVip(boolean vip) { isVip = vip; }

    public String getAthleteName() { return athleteName; }
    public void setAthleteName(String athleteName) { this.athleteName = athleteName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getClub() { return club; }
    public void setClub(String club) { this.club = club; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMeasurements() { return measurements; }
    public void setMeasurements(String measurements) { this.measurements = measurements; }

    // Nuevo Getter y Setter para Dirección
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}