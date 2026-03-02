package com.example.demo1.entity;

import java.sql.Date;

public class Offre {
    private int id_offre;
    private String titre;
    private String description;
    private int taux_remise;
    private Date date_debut;
    private Date date_fin;
    private String statut;

    // Clés étrangères vers les autres modules
    private int id_voyage;      // Membre 1
    private Integer id_hotel;    // Membre 2 (Integer pour accepter null)
    private Long id_vol;         // Membre 6 (Long car BigInt en SQL)
    private Integer id_vehicule; // Membre 6 (Integer pour accepter null)

    private String category;     // VOYAGE, HOTEL, VOL, TRANSPORT
    private boolean is_local_support;
    private String image_url;
    private String destination;// Champ calculé par jointure SQL (Nom du lieu/hôtel/véhicule)
    private double prix_initial;
    // --- Constructeurs ---

    public Offre() {
    }

    // 1. Constructeur COMPLET (Utile pour le Service.afficher avec JOIN) - 16 paramètres
    public Offre(int id_offre, String titre, String description, int taux_remise, Date date_debut, Date date_fin, String statut, int id_voyage, Integer id_hotel, Long id_vol, Integer id_vehicule, String category, boolean is_local_support, String image_url, String destination, double prix_initial) {
        this.id_offre = id_offre;
        this.titre = titre;
        this.description = description;
        this.taux_remise = taux_remise;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.statut = statut;
        this.id_voyage = id_voyage;
        this.id_hotel = id_hotel;
        this.id_vol = id_vol;
        this.id_vehicule = id_vehicule;
        this.category = category;
        this.is_local_support = is_local_support;
        this.image_url = image_url;
        this.destination = destination;
        this.prix_initial = prix_initial;
    }

    // 2. Constructeur pour l'AJOUT (Sans ID et sans champs de join) - 13 paramètres
    public Offre(String titre, String description, int taux_remise, Date date_debut, Date date_fin, String statut, int id_voyage, Integer id_hotel, Long id_vol, Integer id_vehicule, String category, boolean is_local_support, String image_url) {
        this.titre = titre;
        this.description = description;
        this.taux_remise = taux_remise;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.statut = statut;
        this.id_voyage = id_voyage;
        this.id_hotel = id_hotel;
        this.id_vol = id_vol;
        this.id_vehicule = id_vehicule;
        this.category = category;
        this.is_local_support = is_local_support;
        this.image_url = image_url;
    }

    public Offre(int id_offre, String titre, String description, int taux_remise, Date date_debut, Date date_fin, String statut, int id_voyage, Integer id_hotel, Long id_vol, Integer id_vehicule, String category, boolean is_local_support, String image_url) {
        this.id_offre = id_offre;
        this.titre = titre;
        this.description = description;
        this.taux_remise = taux_remise;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.statut = statut;
        this.id_voyage = id_voyage;
        this.id_hotel = id_hotel;
        this.id_vol = id_vol;
        this.id_vehicule = id_vehicule;
        this.category = category;
        this.is_local_support = is_local_support;
        this.image_url = image_url;
    }

    // --- Getters et Setters ---

    public int getId_offre() { return id_offre; }
    public void setId_offre(int id_offre) { this.id_offre = id_offre; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTaux_remise() { return taux_remise; }
    public void setTaux_remise(int taux_remise) { this.taux_remise = taux_remise; }

    public Date getDate_debut() { return date_debut; }
    public void setDate_debut(Date date_debut) { this.date_debut = date_debut; }

    public Date getDate_fin() { return date_fin; }
    public void setDate_fin(Date date_fin) { this.date_fin = date_fin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getId_voyage() { return id_voyage; }
    public void setId_voyage(int id_voyage) { this.id_voyage = id_voyage; }

    public Integer getId_hotel() { return id_hotel; }
    public void setId_hotel(Integer id_hotel) { this.id_hotel = id_hotel; }

    public Long getId_vol() { return id_vol; }
    public void setId_vol(Long id_vol) { this.id_vol = id_vol; }

    public Integer getId_vehicule() { return id_vehicule; }
    public void setId_vehicule(Integer id_vehicule) { this.id_vehicule = id_vehicule; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isIs_local_support() { return is_local_support; }
    public void setIs_local_support(boolean is_local_support) { this.is_local_support = is_local_support; }

    public String getImage_url() { return image_url; }
    public void setImage_url(String image_url) { this.image_url = image_url; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getPrix_initial() {
        return prix_initial;
    }

    public void setPrix_initial(double prix_initial) {
        this.prix_initial = prix_initial;
    }

    @Override
    public String toString() {
        return "Offre{" +
                "id=" + id_offre +
                ", titre='" + titre + '\'' +
                ", catégorie='" + category + '\'' +
                ", remise=" + taux_remise + "%" +
                ", destination='" + destination + '\'' +
                "}\n";
    }
}