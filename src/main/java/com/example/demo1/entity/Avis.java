package com.example.demo1.entity;

import java.time.LocalDate;

public class Avis {
    private int id;
    private String nomClient;
    private String email;
    private int note;
    private String commentaire;
    private LocalDate dateAvis;
    private int voyageId;

    public Avis() {}

    public Avis(int id, String nomClient, String email, int note,
                String commentaire, LocalDate dateAvis, int voyageId) {
        this.id = id;
        this.nomClient = nomClient;
        this.email = email;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
        this.voyageId = voyageId;
    }

    // Getters
    public int getId() { return id; }
    public String getNomClient() { return nomClient; }
    public String getEmail() { return email; }
    public int getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public LocalDate getDateAvis() { return dateAvis; }
    public int getVoyageId() { return voyageId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public void setEmail(String email) { this.email = email; }
    public void setNote(int note) { this.note = note; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public void setDateAvis(LocalDate dateAvis) { this.dateAvis = dateAvis; }
    public void setVoyageId(int voyageId) { this.voyageId = voyageId; }

    @Override
    public String toString() {
        return "Avis{" +
                "id=" + id +
                ", nomClient='" + nomClient + '\'' +
                ", email='" + email + '\'' +
                ", note=" + note +
                ", voyageId=" + voyageId +
                '}';
    }
}