package com.example.vozdelas;

public class SupportOrganization {
    private String name;
    private String description;
    private String address;
    private String phone;
    private String website;

    public SupportOrganization() {
        // Construtor vazio necessário para Firebase Firestore
    }

    public SupportOrganization(String name, String description, String address, String phone, String website) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.website = website;
    }

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}