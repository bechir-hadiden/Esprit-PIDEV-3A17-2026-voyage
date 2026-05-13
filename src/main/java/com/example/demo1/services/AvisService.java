package com.example.demo1.services;

import com.example.demo1.controller.dao.AvisDAO;
import com.example.demo1.entity.Avis;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

public class AvisService {

    private final AvisDAO avisDAO;

    public AvisService() {
        this.avisDAO = new AvisDAO();
    }

    // ==================== MÉTHODES DE BASE ====================

    /**
     * Récupère tous les avis de la base de données
     */
    public List<Avis> getAllAvis() {
        return avisDAO.getAllAvis();
    }

    /**
     * Récupère un avis spécifique par son identifiant
     */
    public Avis getAvisById(int id) {
        return avisDAO.getAvisById(id);
    }

    /**
     * Récupère la liste des avis pour un voyage spécifique
     */
    public List<Avis> getAvisByVoyage(int voyageId) {
        return avisDAO.getAvisByVoyage(voyageId);
    }

    /**
     * Ajoute un nouvel avis depuis l'interface client JavaFX
     * Définit le statut sur 'approved' pour la synchronisation Symfony
     */
    public boolean ajouterAvisClient(String nom, String email, int note, String commentaire, int voyageId) {
        if (avisDAO.hasUserReviewed(voyageId, email)) {
            return false;
        }

        Avis avis = new Avis();
        avis.setNomClient(nom);
        avis.setEmail(email);
        avis.setNote(note);
        avis.setCommentaire(commentaire);
        avis.setDateAvis(LocalDate.now());
        avis.setVoyageId(voyageId);

        // --- SYNCHRONISATION SYMFONY ---
        avis.setStatus("approved");

        return avisDAO.addAvis(avis);
    }

    /**
     * Modifier un avis via paramètres individuels
     */
    public boolean modifierAvis(int id, String nom, String email, int note, String commentaire, int voyageId) {
        Avis avis = new Avis();
        avis.setId(id);
        avis.setNomClient(nom);
        avis.setEmail(email);
        avis.setNote(note);
        avis.setCommentaire(commentaire);
        avis.setDateAvis(LocalDate.now());
        avis.setVoyageId(voyageId);

        // On garde le statut "approved" pour qu'il reste visible sur le Web
        avis.setStatus("approved");

        return avisDAO.updateAvis(avis);
    }

    /**
     * MÉTHODE RACCOURCIE : Pour corriger l'erreur de compilation dans AdminAvisController
     * Permet d'appeler modifierAvis(avisObject)
     */
    public boolean modifierAvis(Avis avis) {
        if (avis.getStatus() == null) {
            avis.setStatus("approved");
        }
        if (avis.getDateAvis() == null) {
            avis.setDateAvis(LocalDate.now());
        }
        return avisDAO.updateAvis(avis);
    }

    /**
     * Supprime un avis
     */
    public boolean supprimerAvis(int id) {
        return avisDAO.deleteAvis(id);
    }

    /**
     * Calcule la note moyenne pour un voyage
     */
    public double getNoteMoyenne(int voyageId) {
        return avisDAO.getNoteMoyenne(voyageId);
    }

    /**
     * Vérifie si un email a déjà posté un avis pour un voyage donné
     */
    public boolean hasUserReviewed(int voyageId, String email) {
        return avisDAO.hasUserReviewed(voyageId, email);
    }

    // ==================== STATISTIQUES & RECHERCHE ====================

    /**
     * Génère des statistiques globales pour le tableau de bord
     */
    public Map<String, Object> getStatistiquesGlobales() {
        List<Avis> tousAvis = getAllAvis();
        int totalAvis = tousAvis.size();

        double moyenneGlobale = totalAvis > 0 ?
                tousAvis.stream().mapToInt(Avis::getNote).average().orElse(0) : 0;

        Map<Integer, Long> distributionNotes = tousAvis.stream()
                .collect(Collectors.groupingBy(Avis::getNote, Collectors.counting()));

        for (int i = 1; i <= 5; i++) {
            distributionNotes.putIfAbsent(i, 0L);
        }

        long avisMois = tousAvis.stream()
                .filter(a -> a.getDateAvis() != null &&
                        a.getDateAvis().getMonth() == LocalDate.now().getMonth())
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAvis", totalAvis);
        stats.put("moyenneGlobale", Math.round(moyenneGlobale * 10.0) / 10.0);
        stats.put("distributionNotes", distributionNotes);
        stats.put("avisDernierMois", avisMois);

        return stats;
    }

    /**
     * Recherche filtrée pour l'Administration
     */
    public List<Avis> rechercherAvisAdmin(String recherche, Integer noteFiltre) {
        List<Avis> tous = getAllAvis();

        return tous.stream()
                .filter(a -> recherche == null || recherche.isEmpty() ||
                        a.getNomClient().toLowerCase().contains(recherche.toLowerCase()) ||
                        a.getEmail().toLowerCase().contains(recherche.toLowerCase()))
                .filter(a -> noteFiltre == null || noteFiltre == 0 || a.getNote() == noteFiltre)
                .collect(Collectors.toList());
    }
}