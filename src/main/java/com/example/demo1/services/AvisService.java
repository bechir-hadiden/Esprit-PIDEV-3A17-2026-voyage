package com.example.demo1.services;

import com.example.demo1.controller.dao.AvisDAO;
import com.example.demo1.entity.Avis;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AvisService {

    private final AvisDAO avisDAO;

    public AvisService() {
        this.avisDAO = new AvisDAO();
    }

    // ==================== MÉTHODES DE BASE ====================

    // Récupérer tous les avis
    public List<Avis> getAllAvis() {
        return avisDAO.getAllAvis();
    }

    // Récupérer un avis par ID
    public Avis getAvisById(int id) {
        return avisDAO.getAvisById(id);
    }

    // Récupérer les avis par voyage
    public List<Avis> getAvisByVoyage(int voyageId) {
        return avisDAO.getAvisByVoyage(voyageId);
    }

    // Ajouter un avis
    public boolean ajouterAvisClient(String nom, String email, int note, String commentaire, int voyageId) {
        // Vérifier si l'utilisateur a déjà donné un avis pour ce voyage
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

        return avisDAO.addAvis(avis);
    }

    // Modifier un avis
    public boolean modifierAvis(Avis avis) {
        avis.setDateAvis(LocalDate.now());
        return avisDAO.updateAvis(avis);
    }

    // Modifier un avis par ID
    public boolean modifierAvis(int id, String nom, String email, int note, String commentaire, int voyageId) {
        Avis avis = new Avis();
        avis.setId(id);
        avis.setNomClient(nom);
        avis.setEmail(email);
        avis.setNote(note);
        avis.setCommentaire(commentaire);
        avis.setDateAvis(LocalDate.now());
        avis.setVoyageId(voyageId);
        return avisDAO.updateAvis(avis);
    }

    // Supprimer un avis
    public boolean supprimerAvis(int id) {
        return avisDAO.deleteAvis(id);
    }

    // Note moyenne d'un voyage
    public double getNoteMoyenne(int voyageId) {
        return avisDAO.getNoteMoyenne(voyageId);
    }

    // Vérifier si l'utilisateur a déjà donné un avis
    public boolean hasUserReviewed(int voyageId, String email) {
        return avisDAO.hasUserReviewed(voyageId, email);
    }

    // ==================== STATISTIQUES POUR ADMIN ====================

    // Récupérer les statistiques globales
    public Map<String, Object> getStatistiquesGlobales() {
        List<Avis> tousAvis = getAllAvis();
        int totalAvis = tousAvis.size();

        double moyenneGlobale = totalAvis > 0 ?
                tousAvis.stream().mapToInt(Avis::getNote).average().orElse(0) : 0;

        Map<Integer, Long> distributionNotes = tousAvis.stream()
                .collect(Collectors.groupingBy(Avis::getNote, Collectors.counting()));

        // Initialiser les notes manquantes
        for (int i = 1; i <= 5; i++) {
            distributionNotes.putIfAbsent(i, 0L);
        }

        // Nombre d'avis par voyage
        Map<Integer, Long> avisParVoyage = tousAvis.stream()
                .collect(Collectors.groupingBy(Avis::getVoyageId, Collectors.counting()));

        // Avis du dernier mois
        long avisDernierMois = tousAvis.stream()
                .filter(avis -> avis.getDateAvis().isAfter(LocalDate.now().minusDays(30)))
                .count();

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalAvis", totalAvis);
        result.put("moyenneGlobale", Math.round(moyenneGlobale * 10.0) / 10.0);
        result.put("distributionNotes", distributionNotes);
        result.put("avisParVoyage", avisParVoyage);
        result.put("avisDernierMois", avisDernierMois);

        return result;
    }

    // Récupérer les statistiques par voyage
    public Map<String, Object> getStatistiquesParVoyage(int voyageId) {
        List<Avis> avisVoyage = getAvisByVoyage(voyageId);
        int totalAvis = avisVoyage.size();

        if (totalAvis == 0) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("voyageId", voyageId);
            result.put("totalAvis", 0);
            result.put("noteMoyenne", 0.0);
            result.put("message", "Aucun avis pour ce voyage");
            return result;
        }

        double noteMoyenne = getNoteMoyenne(voyageId);

        Map<Integer, Long> distribution = avisVoyage.stream()
                .collect(Collectors.groupingBy(Avis::getNote, Collectors.counting()));

        for (int i = 1; i <= 5; i++) {
            distribution.putIfAbsent(i, 0L);
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("voyageId", voyageId);
        result.put("totalAvis", totalAvis);
        result.put("noteMoyenne", Math.round(noteMoyenne * 10.0) / 10.0);
        result.put("distributionNotes", distribution);
        result.put("meilleurNote", avisVoyage.stream().mapToInt(Avis::getNote).max().orElse(0));
        result.put("pireNote", avisVoyage.stream().mapToInt(Avis::getNote).min().orElse(0));

        return result;
    }

    // ==================== RECHERCHE POUR ADMIN ====================

    // Recherche avancée
    public List<Avis> rechercherAvisAdmin(String recherche, Integer noteFiltre, Integer voyageId, LocalDate dateDebut, LocalDate dateFin) {
        List<Avis> resultats = getAllAvis();

        if (recherche != null && !recherche.trim().isEmpty()) {
            String rechercheLower = recherche.toLowerCase();
            resultats = resultats.stream()
                    .filter(a -> a.getNomClient().toLowerCase().contains(rechercheLower) ||
                            a.getEmail().toLowerCase().contains(rechercheLower) ||
                            a.getCommentaire().toLowerCase().contains(rechercheLower))
                    .collect(Collectors.toList());
        }

        if (noteFiltre != null && noteFiltre > 0) {
            resultats = resultats.stream()
                    .filter(a -> a.getNote() == noteFiltre)
                    .collect(Collectors.toList());
        }

        if (voyageId != null && voyageId > 0) {
            resultats = resultats.stream()
                    .filter(a -> a.getVoyageId() == voyageId)
                    .collect(Collectors.toList());
        }

        if (dateDebut != null) {
            resultats = resultats.stream()
                    .filter(a -> !a.getDateAvis().isBefore(dateDebut))
                    .collect(Collectors.toList());
        }

        if (dateFin != null) {
            resultats = resultats.stream()
                    .filter(a -> !a.getDateAvis().isAfter(dateFin))
                    .collect(Collectors.toList());
        }

        return resultats;
    }
}