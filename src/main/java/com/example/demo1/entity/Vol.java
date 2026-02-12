package com.example.demo1.entity;

public class Vol {
    private String compagnie;
    private String depart;
    private String arrivee;
    private String dateDepart;
    private String dateArrivee;
    private String heureDepart;
    private String heureArrivee;
    private double prix;
    private String devise;
    private int escales;
    private String duree;

    public Vol(String compagnie, String depart, String arrivee,
               String dateDepart, String dateArrivee,
               String heureDepart, String heureArrivee,
               double prix, String devise, int escales, String duree) {
        this.compagnie = compagnie;
        this.depart = depart;
        this.arrivee = arrivee;
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.heureDepart = heureDepart;
        this.heureArrivee = heureArrivee;
        this.prix = prix;
        this.devise = devise;
        this.escales = escales;
        this.duree = duree;
    }

    // Getters
    public String getCompagnie() {
        return compagnie;
    }

    public String getDepart() {
        return depart;
    }

    public String getArrivee() {
        return arrivee;
    }

    public String getDateDepart() {
        return dateDepart;
    }

    public String getDateArrivee() {
        return dateArrivee;
    }

    public String getHeureDepart() {
        return heureDepart;
    }

    public String getHeureArrivee() {
        return heureArrivee;
    }

    public double getPrix() {
        return prix;
    }

    public String getDevise() {
        return devise;
    }

    public int getEscales() {
        return escales;
    }

    public String getDuree() {
        return duree;
    }

    public String getTrajet() {
        return depart + " → " + arrivee;
    }

    public String getHoraires() {
        return heureDepart + " - " + heureArrivee;
    }
}