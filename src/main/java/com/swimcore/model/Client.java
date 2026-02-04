/*
 * -----------------------------------------------------------------------------
 * ARCHIVO: Client.java
 * VERSIÓN: 3.3.0 (Sincronizado con Talla)
 * FECHA: 04 de Febrero de 2026
 * -----------------------------------------------------------------------------
 */
package com.swimcore.model;

public class Client {
    private int id;
    private String code, idType, idNumber, fullName, phone, email, instagram;
    private boolean isVip;
    private String athleteName, birthDate, club, category, measurements, address, profession, alternatePhone;
    private String size; // <--- CAMPO DE TALLA AGREGADO

    public Client() {}

    public Client(int id, String code, String idType, String idNumber, String fullName,
                  String phone, String email, String instagram, boolean isVip,
                  String athleteName, String birthDate, String club, String category,
                  String measurements, String address, String profession, String alternatePhone, String size) {
        this.id = id; this.code = code; this.idType = idType; this.idNumber = idNumber;
        this.fullName = fullName; this.phone = phone; this.email = email;
        this.instagram = instagram; this.isVip = isVip; this.athleteName = athleteName;
        this.birthDate = birthDate; this.club = club; this.category = category;
        this.measurements = measurements; this.address = address;
        this.profession = profession; this.alternatePhone = alternatePhone;
        this.size = size; // <--- INICIALIZADO
    }

    // --- Getters y Setters ---
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

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
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }
}