package com.example.demo1.entity;

import java.time.LocalDateTime;

public class Reclamation {
    private int id;
    private String nom;
    private String email;
    private String telephone;
    private String message;
    private LocalDateTime dateCreation;
    private String statut; // "NOUVEAU", "EN_COURS", "TRAITE"

    // Constructeurs
    public Reclamation() {
        this.dateCreation = LocalDateTime.now();
        this.statut = "NOUVEAU";
    }

    public Reclamation(String nom, String email, String telephone, String message) {
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.message = message;
        this.dateCreation = LocalDateTime.now();
        this.statut = "NOUVEAU";
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", message='" + message + '\'' +
                ", dateCreation=" + dateCreation +
                ", statut='" + statut + '\'' +
                '}';
    }
}
