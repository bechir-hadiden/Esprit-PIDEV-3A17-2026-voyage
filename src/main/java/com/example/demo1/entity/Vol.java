package com.example.demo1.entity;

public class Vol {
    private Long id; // ⚠️ AJOUTÉ pour la base de données
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


    public Vol() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public void setCompagnie(String compagnie) {
        this.compagnie = compagnie;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public void setArrivee(String arrivee) {
        this.arrivee = arrivee;
    }

    public void setDateDepart(String dateDepart) {
        this.dateDepart = dateDepart;
    }

    public void setDateArrivee(String dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public void setHeureDepart(String heureDepart) {
        this.heureDepart = heureDepart;
    }

    public void setHeureArrivee(String heureArrivee) {
        this.heureArrivee = heureArrivee;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public void setEscales(int escales) {
        this.escales = escales;
    }

    public void setDuree(String duree) {
        this.duree = duree;
    }
}