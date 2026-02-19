package com.example.demo1.entity;

public class Destination {

    // ============ CHAMPS BDD ============
    private int id;
    private String nom;
    private String pays;
    private String codeIata;
    private String description;
    private String imageUrl;
    private String videoUrl;

    // ============ CHAMPS ADDITIONNELS (optionnels) ============
    private String categorie;        // "populaire", "nouveau", "promo"
    private double prixMin;
    private String devise;
    private boolean promo;
    private int reduction;
    private String labelCategorie;

    // ============ CONSTRUCTEURS ============
    public Destination() {
        this.categorie = "";
        this.devise = "EUR";
        this.prixMin = 0.0;
        this.promo = false;
        this.reduction = 0;
        this.labelCategorie = "";
    }

    public Destination(String nom, String pays, String codeIata,
                       String description, String imageUrl,
                       String categorie, boolean promo, int reduction) {
        this.nom = nom;
        this.pays = pays;
        this.codeIata = codeIata;
        this.description = description;
        this.imageUrl = imageUrl;
        this.categorie = categorie;
        this.promo = promo;
        this.reduction = reduction;
        this.devise = "EUR";
        this.prixMin = 0.0;
        this.labelCategorie = "";
    }

    // ============ GETTERS BDD ============
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPays() { return pays; }
    public String getCodeIata() { return codeIata; }
    public String getDescription() {
        return description != null ? description : "Découvrez cette magnifique destination";
    }
    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "https://via.placeholder.com/320x200?text=" + nom;
    }
    public String getVideoUrl() { return videoUrl; }

    // ============ GETTERS ADDITIONNELS ============
    public String getCategorie() { return categorie; }
    public double getPrixMin() { return prixMin; }
    public String getDevise() { return devise; }
    public boolean isPromo() { return promo; }
    public int getReduction() { return reduction; }
    public String getLabelCategorie() { return labelCategorie; }

    // ============ GETTERS CALCULÉS ============

    /**
     * Nom complet avec pays
     */
    public String getNomComplet() {
        if (pays != null && !pays.isEmpty()) {
            return nom + ", " + pays;
        }
        return nom;
    }

    /**
     * Prix formaté avec devise
     */
    public String getPrixFormate() {
        if (prixMin > 0) {
            return String.format("%.0f %s", prixMin, devise);
        }
        return "Prix non disponible";
    }

    // ============ SETTERS BDD ============
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPays(String pays) { this.pays = pays; }
    public void setCodeIata(String codeIata) { this.codeIata = codeIata; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    // ============ SETTERS ADDITIONNELS ============
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setPrixMin(double prixMin) { this.prixMin = prixMin; }
    public void setDevise(String devise) { this.devise = devise; }
    public void setPromo(boolean promo) { this.promo = promo; }
    public void setReduction(int reduction) { this.reduction = reduction; }
    public void setLabelCategorie(String labelCategorie) { this.labelCategorie = labelCategorie; }

    // ============ MÉTHODES UTILITAIRES ============

    /**
     * Définir automatiquement le label selon la catégorie
     */
    public void setAutoLabel() {
        if (promo && reduction > 0) {
            labelCategorie = "-" + reduction + "%";
        } else if (categorie.equals("populaire")) {
            labelCategorie = "🔥 Populaire";
        } else if (categorie.equals("nouveau")) {
            labelCategorie = "✨ Nouveau";
        } else {
            labelCategorie = "";
        }
    }

    @Override
    public String toString() {
        return nom + " (" + pays + ") - " + codeIata;
    }
}