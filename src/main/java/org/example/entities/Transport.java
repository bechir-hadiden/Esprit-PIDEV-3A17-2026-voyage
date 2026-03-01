package org.example.entities;

/** Entity class for Transport data. */
public class Transport {
    private int idTransport;
    private String type;
    private String compagnie;
    private String numero;
    private int capacite;
    private String imageUrl;
    private String description;
    private double prix;

    public Transport() {
    }

    public Transport(int idTransport, String type, String compagnie, String numero, int capacite) {
        this.idTransport = idTransport;
        this.type = type;
        this.compagnie = compagnie;
        this.numero = numero;
        this.capacite = capacite;
    }

    public int getIdTransport() {
        return idTransport;
    }

    public void setIdTransport(int idTransport) {
        this.idTransport = idTransport;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompagnie() {
        return compagnie;
    }

    public void setCompagnie(String compagnie) {
        this.compagnie = compagnie;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }
}
