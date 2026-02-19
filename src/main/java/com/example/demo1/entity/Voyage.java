package com.example.demo1.entity;

import java.time.LocalDate;

public class Voyage {

    private int id;
    private String destination;      // ← ancien champ texte (garder pour compatibilité)
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double prix;
    private String imagePath;
    private String description;
    private int destinationId;       // ← nouveau champ
    private Destination destinationObj; // ← objet destination complet

    // ============ Constructeur vide ============
    public Voyage() {}

    // ============ Constructeur complet ============
    public Voyage(int id, String destination, LocalDate dateDebut,
                  LocalDate dateFin, double prix, String imagePath,
                  String description, int destinationId) {
        this.id = id;
        this.destination = destination;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prix = prix;
        this.imagePath = imagePath;
        this.description = description;
        this.destinationId = destinationId;
    }

    // ============ Getters ============
    public int getId() { return id; }
    public String getDestination() { return destination; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public double getPrix() { return prix; }
    public String getImagePath() { return imagePath; }
    public String getDescription() { return description; }
    public int getDestinationId() { return destinationId; }
    public Destination getDestinationObj() { return destinationObj; }

    // ============ Setters ============
    public void setId(int id) { this.id = id; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setDescription(String description) { this.description = description; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }
    public void setDestinationObj(Destination destinationObj) {
        this.destinationObj = destinationObj;
    }

    @Override
    public String toString() {
        return "Voyage{id=" + id + ", destination='" + destination +
                "', prix=" + prix + "}";
    }
}