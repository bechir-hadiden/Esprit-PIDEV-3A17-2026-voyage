package com.example.demo1.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Destination {

    // ============ CHAMPS BDD ============
    private int id;
    private String nom;
    private String pays;
    private String codeIata;
    private String description;
    private String imageUrl;
    private String videoUrl;

    // ✅ LISTE D'IMAGES
    private List<String> images = new ArrayList<>();

    // ============ CHAMPS ADDITIONNELS ============
    private String categorie;
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
        this.images = new ArrayList<>();
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
        this.images = new ArrayList<>();
    }

    // ============ GETTERS BDD ============
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPays() {
        return pays;
    }

    public String getCodeIata() {
        return codeIata;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getDescription() {
        return description != null
                ? description
                : "Découvrez cette magnifique destination";
    }

    public String getImageUrl() {
        return imageUrl != null
                ? imageUrl
                : "https://via.placeholder.com/320x200?text=" + nom;
    }

    // ============ GETTERS LISTE IMAGES ============

    /**
     * ✅ Retourne toutes les images disponibles
     * Si la liste est vide, retourne imageUrl comme fallback
     */
    public List<String> getImages() {

        List<String> result = new ArrayList<>();

        if (imageUrl == null || imageUrl.isEmpty()) {
            return result;
        }

        String raw = imageUrl.trim();

        // ✅ Format JSON ["url1","url2"]
        if (raw.startsWith("[")) {

            raw = raw.replaceAll("[\\[\\]\"]", "");

            Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(result::add);

            return result;
        }

        // ✅ Séparateur | OU ;
        Arrays.stream(raw.split("[|;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(result::add);

        return result;
    }

    /**
     * ✅ Retourne la première image disponible
     */
    public String getPremierImage() {
        List<String> imgs = getImages();
        return imgs.isEmpty() ? null : imgs.get(0);
    }

    /**
     * ✅ Nombre d'images disponibles
     */
    public int getNombreImages() {
        return getImages().size();
    }

    // ============ GETTERS ADDITIONNELS ============
    public String getCategorie() {
        return categorie;
    }

    public double getPrixMin() {
        return prixMin;
    }

    public String getDevise() {
        return devise;
    }

    public boolean isPromo() {
        return promo;
    }

    public int getReduction() {
        return reduction;
    }

    public String getLabelCategorie() {
        return labelCategorie;
    }

    // ============ GETTERS CALCULÉS ============
    public String getNomComplet() {

        if (pays != null && !pays.isEmpty()) {
            return nom + ", " + pays;
        }

        return nom;
    }

    // ============ SETTERS BDD ============
    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public void setCodeIata(String codeIata) {
        this.codeIata = codeIata;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    // ============ SETTERS LISTE IMAGES ============
    public void setImages(List<String> images) {
        this.images = images;
    }

    public void addImage(String imageUrl) {

        if (this.images == null) {
            this.images = new ArrayList<>();
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            this.images.add(imageUrl);
        }
    }

    public void clearImages() {

        if (this.images != null) {
            this.images.clear();
        }
    }

    // ============ SETTERS ADDITIONNELS ============
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setPrixMin(double prixMin) {
        this.prixMin = prixMin;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public void setPromo(boolean promo) {
        this.promo = promo;
    }

    public void setReduction(int reduction) {
        this.reduction = reduction;
    }

    public void setLabelCategorie(String labelCategorie) {
        this.labelCategorie = labelCategorie;
    }

    // ============ MÉTHODES UTILITAIRES ============
    public void setAutoLabel() {

        if (promo && reduction > 0) {

            labelCategorie = "-" + reduction + "%";

        } else if ("populaire".equals(categorie)) {

            labelCategorie = "Populaire";

        } else if ("nouveau".equals(categorie)) {

            labelCategorie = "Nouveau";

        } else {

            labelCategorie = "";
        }
    }

    @Override
    public String toString() {
        return nom + " (" + pays + ") - " + codeIata;
    }
}