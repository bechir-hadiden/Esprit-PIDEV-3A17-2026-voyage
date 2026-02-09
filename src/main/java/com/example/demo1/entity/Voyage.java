package com.example.demo1.entity;

import java.time.LocalDate;

public class Voyage {
    private int id;
    private String destination;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double prix;
    private String imagePath;
    private String description;

    // Constructeurs
    public Voyage() {
    }

    public Voyage(int id, String destination, LocalDate dateDebut, LocalDate dateFin,
                  double prix, String imagePath, String description) {
        this.id = id;
        this.destination = destination;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prix = prix;
        this.imagePath = imagePath;
        this.description = description;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Méthode utilitaire pour formater la période
    public String getFormattedPeriod() {
        if (dateDebut != null && dateFin != null) {
            return dateDebut.getDayOfMonth() + " - " + dateFin.getDayOfMonth() + " " +
                    dateDebut.getMonth().toString();
        }
        return "";
    }

    @Override
    public String toString() {
        return "Voyage{" +
                "id=" + id +
                ", destination='" + destination + '\'' +
                ", prix=" + prix +
                '}';
    }

}