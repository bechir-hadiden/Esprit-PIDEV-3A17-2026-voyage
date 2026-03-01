package org.example.entities;

/** Entity class for Transport Type (simplified). */
public class TransportType {
    private int idType;
    private String nom;
    private double prixDepart;

    private String image;

    public TransportType() {
    }

    public TransportType(int idType, String nom, double prixDepart) {
        this.idType = idType;
        this.nom = nom;
        this.prixDepart = prixDepart;
    }

    public TransportType(int idType, String nom, double prixDepart, String image) {
        this.idType = idType;
        this.nom = nom;
        this.prixDepart = prixDepart;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getIdType() {
        return idType;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrixDepart() {
        return prixDepart;
    }

    public void setPrixDepart(double prixDepart) {
        this.prixDepart = prixDepart;
    }

    @Override
    public String toString() {
        return nom;
    }
}
