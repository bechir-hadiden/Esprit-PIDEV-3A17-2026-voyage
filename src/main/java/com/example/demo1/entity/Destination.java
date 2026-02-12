package com.example.demo1.entity;

public class Destination {
    private String nom;
    private String pays;
    private String codeIATA;
    private String description;
    private String imageUrl;
    private String categorie; // "populaire", "promo", "nouveau"
    private double prixMin;
    private String devise;
    private boolean isPromo;
    private int reduction; // % de réduction si promo

    public Destination(String nom, String pays, String codeIATA,
                       String description, String imageUrl,
                       String categorie, boolean isPromo, int reduction) {
        this.nom = nom;
        this.pays = pays;
        this.codeIATA = codeIATA;
        this.description = description;
        this.imageUrl = imageUrl;
        this.categorie = categorie;
        this.isPromo = isPromo;
        this.reduction = reduction;
        this.prixMin = 0.0;
        this.devise = "EUR";
    }

    // Getters et Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public String getCodeIATA() { return codeIATA; }
    public void setCodeIATA(String codeIATA) { this.codeIATA = codeIATA; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public double getPrixMin() { return prixMin; }
    public void setPrixMin(double prixMin) { this.prixMin = prixMin; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public boolean isPromo() { return isPromo; }
    public void setPromo(boolean promo) { isPromo = promo; }

    public int getReduction() { return reduction; }
    public void setReduction(int reduction) { this.reduction = reduction; }

    public String getNomComplet() {
        return nom + ", " + pays;
    }

    public String getPrixFormate() {
        if (prixMin > 0) {
            return String.format("%.0f %s", prixMin, devise);
        }
        return "Prix en chargement...";
    }

    public String getLabelCategorie() {
        switch (categorie.toLowerCase()) {
            case "populaire": return "⭐ Populaire";
            case "promo": return "🔥 Promotion";
            case "nouveau": return "🌟 Nouveau";
            default: return "";
        }
    }
}