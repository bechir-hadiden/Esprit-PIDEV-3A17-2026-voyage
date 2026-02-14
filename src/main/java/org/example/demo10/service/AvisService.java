package org.example.demo10.service;

import org.example.demo10.dao.AvisDAO;
import org.example.demo10.model.Avis;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AvisService {
    private AvisDAO avisDAO;

    public AvisService() {
        this.avisDAO = new AvisDAO();
    }

    // ==================== STATISTIQUES POUR ADMINISTRATION ====================

    /**
     * Statistiques générales pour l'administration
     */
    public Map<String, Object> getStatistiquesAdmin() {
        List<Avis> tousAvis = getAllAvis();
        int totalAvis = tousAvis.size();

        // Calcul de la note moyenne globale
        double moyenneGlobale = totalAvis > 0 ?
                tousAvis.stream().mapToInt(Avis::getNote).average().orElse(0) : 0;

        // Distribution des notes
        Map<Integer, Long> distributionNotes = tousAvis.stream()
                .collect(Collectors.groupingBy(Avis::getNote, Collectors.counting()));

        // Initialiser toutes les notes de 1 à 5
        for (int i = 1; i <= 5; i++) {
            distributionNotes.putIfAbsent(i, 0L);
        }

        // Nombre d'avis par voyage
        Map<Integer, Long> avisParVoyage = tousAvis.stream()
                .collect(Collectors.groupingBy(Avis::getVoyageId, Collectors.counting()));

        // Voyages les mieux notés (note moyenne > 4)
        Map<Integer, Double> notesMoyennesParVoyage = new HashMap<>();
        avisParVoyage.keySet().forEach(voyageId -> {
            notesMoyennesParVoyage.put(voyageId, getNoteMoyenneVoyage(voyageId));
        });

        // Avis du dernier mois
        long avisDernierMois = tousAvis.stream()
                .filter(avis -> avis.getDateAvis().isAfter(LocalDate.now().minusDays(30)))
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("totalAvis", totalAvis);
        result.put("moyenneGlobale", Math.round(moyenneGlobale * 10.0) / 10.0);
        result.put("distributionNotes", distributionNotes);
        result.put("avisParVoyage", avisParVoyage);
        result.put("meilleursVoyages", notesMoyennesParVoyage.entrySet().stream()
                .filter(e -> e.getValue() >= 4.0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        result.put("avisDernierMois", avisDernierMois);
        result.put("avisAujourdhui", tousAvis.stream()
                .filter(avis -> avis.getDateAvis().equals(LocalDate.now()))
                .count());

        return result;
    }

    /**
     * Statistiques détaillées par voyage pour l'admin
     */
    public Map<String, Object> getStatistiquesParVoyageAdmin(int voyageId) {
        List<Avis> avisVoyage = getAvisByVoyage(voyageId);
        int totalAvis = avisVoyage.size();

        if (totalAvis == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("voyageId", voyageId);
            result.put("totalAvis", 0);
            result.put("noteMoyenne", 0.0);
            result.put("message", "Aucun avis pour ce voyage");
            return result;
        }

        double noteMoyenne = getNoteMoyenneVoyage(voyageId);

        // Distribution des notes
        Map<Integer, Long> distribution = avisVoyage.stream()
                .collect(Collectors.groupingBy(Avis::getNote, Collectors.counting()));

        // Initialiser toutes les notes
        for (int i = 1; i <= 5; i++) {
            distribution.putIfAbsent(i, 0L);
        }

        // Dernier avis
        Avis dernierAvis = avisVoyage.stream()
                .max((a1, a2) -> a1.getDateAvis().compareTo(a2.getDateAvis()))
                .orElse(null);

        // Avis les plus récents (5 derniers)
        List<Avis> avisRecents = avisVoyage.stream()
                .sorted((a1, a2) -> a2.getDateAvis().compareTo(a1.getDateAvis()))
                .limit(5)
                .collect(Collectors.toList());

        // Pourcentage de recommandation (notes 4-5)
        long recommandations = avisVoyage.stream()
                .filter(avis -> avis.getNote() >= 4)
                .count();
        double tauxRecommandation = (double) recommandations / totalAvis * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("voyageId", voyageId);
        result.put("totalAvis", totalAvis);
        result.put("noteMoyenne", noteMoyenne);
        result.put("distributionNotes", distribution);
        result.put("dernierAvis", dernierAvis);
        result.put("avisRecents", avisRecents);
        result.put("tauxRecommandation", Math.round(tauxRecommandation * 10.0) / 10.0);
        result.put("meilleurNote", avisVoyage.stream().mapToInt(Avis::getNote).max().orElse(0));
        result.put("pireNote", avisVoyage.stream().mapToInt(Avis::getNote).min().orElse(0));

        return result;
    }

    /**
     * Rapport complet pour l'administration
     */
    public String genererRapportComplet() {
        Map<String, Object> stats = getStatistiquesAdmin();
        StringBuilder rapport = new StringBuilder();

        rapport.append("=== RAPPORT COMPLET DES AVIS ===\n\n");
        rapport.append("STATISTIQUES GÉNÉRALES:\n");
        rapport.append("-----------------------\n");
        rapport.append("Total d'avis: ").append(stats.get("totalAvis")).append("\n");
        rapport.append("Note moyenne globale: ").append(stats.get("moyenneGlobale")).append("/5\n");
        rapport.append("Avis ajoutés ce mois: ").append(stats.get("avisDernierMois")).append("\n");
        rapport.append("Avis ajoutés aujourd'hui: ").append(stats.get("avisAujourdhui")).append("\n\n");

        rapport.append("DISTRIBUTION DES NOTES:\n");
        rapport.append("----------------------\n");
        Map<Integer, Long> distribution = (Map<Integer, Long>) stats.get("distributionNotes");
        long totalAvis = (long) stats.get("totalAvis");
        for (int i = 5; i >= 1; i--) {
            long count = distribution.getOrDefault(i, 0L);
            double pourcentage = totalAvis > 0 ?
                    (count * 100.0 / totalAvis) : 0;
            rapport.append(i).append(" étoiles: ").append(count)
                    .append(" (").append(String.format("%.1f", pourcentage)).append("%)\n");
        }

        rapport.append("\nVOYAGES LES MIEUX NOTÉS (≥4/5):\n");
        rapport.append("--------------------------------\n");
        Map<Integer, Double> meilleursVoyages = (Map<Integer, Double>) stats.get("meilleursVoyages");
        if (meilleursVoyages.isEmpty()) {
            rapport.append("Aucun voyage n'a une note moyenne ≥4/5\n");
        } else {
            List<Map.Entry<Integer, Double>> sortedEntries = meilleursVoyages.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .collect(Collectors.toList());

            for (Map.Entry<Integer, Double> entry : sortedEntries) {
                rapport.append("Voyage #").append(entry.getKey())
                        .append(": ").append(String.format("%.1f", entry.getValue()))
                        .append("/5\n");
            }
        }

        return rapport.toString();
    }

    // ==================== STATISTIQUES POUR CLIENT ====================

    /**
     * Statistiques simples pour les clients
     */
    public Map<String, Object> getStatistiquesClient(int voyageId) {
        List<Avis> avisVoyage = getAvisByVoyage(voyageId);
        int totalAvis = avisVoyage.size();

        if (totalAvis == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("voyageId", voyageId);
            result.put("totalAvis", 0);
            result.put("noteMoyenne", 0.0);
            result.put("message", "Soyez le premier à donner votre avis !");
            return result;
        }

        double noteMoyenne = getNoteMoyenneVoyage(voyageId);

        // Distribution des notes simplifiée
        Map<String, Long> distributionSimple = new HashMap<>();
        distributionSimple.put("excellents", avisVoyage.stream().filter(a -> a.getNote() == 5).count());
        distributionSimple.put("bons", avisVoyage.stream().filter(a -> a.getNote() == 4).count());
        distributionSimple.put("moyens", avisVoyage.stream().filter(a -> a.getNote() == 3).count());
        distributionSimple.put("passables", avisVoyage.stream().filter(a -> a.getNote() == 2).count());
        distributionSimple.put("mauvais", avisVoyage.stream().filter(a -> a.getNote() == 1).count());

        // Taux de recommandation
        long recommandations = avisVoyage.stream().filter(a -> a.getNote() >= 4).count();
        int tauxRecommandation = totalAvis > 0 ? (int) ((recommandations * 100) / totalAvis) : 0;

        // Derniers avis (3 maximum)
        List<Avis> derniersAvis = avisVoyage.stream()
                .sorted((a1, a2) -> a2.getDateAvis().compareTo(a1.getDateAvis()))
                .limit(3)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("voyageId", voyageId);
        result.put("totalAvis", totalAvis);
        result.put("noteMoyenne", Math.round(noteMoyenne * 10.0) / 10.0);
        result.put("distributionSimple", distributionSimple);
        result.put("tauxRecommandation", Math.round(tauxRecommandation));
        result.put("derniersAvis", derniersAvis);
        result.put("noteMax", avisVoyage.stream().mapToInt(Avis::getNote).max().orElse(0));
        result.put("noteMin", avisVoyage.stream().mapToInt(Avis::getNote).min().orElse(0));

        return result;
    }

    /**
     * Recherche par mot-clé dans les commentaires (client)
     */
    public List<Avis> rechercherAvisParMotCle(int voyageId, String motCle) {
        List<Avis> avisVoyage = getAvisByVoyage(voyageId);

        if (motCle == null || motCle.trim().isEmpty()) {
            return avisVoyage;
        }

        final String motCleFinal = motCle.toLowerCase().trim();

        return avisVoyage.stream()
                .filter(avis -> avis.getCommentaire().toLowerCase().contains(motCleFinal))
                .collect(Collectors.toList());
    }

    /**
     * Génère un graphique simple de distribution des notes
     */
    public String genererGraphiqueNotes(int voyageId) {
        Map<String, Object> stats = getStatistiquesClient(voyageId);
        Map<String, Long> distribution = (Map<String, Long>) stats.get("distributionSimple");
        int total = (int) stats.get("totalAvis");

        if (total == 0) {
            return "Aucune donnée disponible";
        }

        StringBuilder graphique = new StringBuilder();
        graphique.append("Distribution des notes:\n");
        graphique.append("★★★★★ ").append(barrePourcentage(distribution.get("excellents"), total)).append("\n");
        graphique.append("★★★★☆ ").append(barrePourcentage(distribution.get("bons"), total)).append("\n");
        graphique.append("★★★☆☆ ").append(barrePourcentage(distribution.get("moyens"), total)).append("\n");
        graphique.append("★★☆☆☆ ").append(barrePourcentage(distribution.get("passables"), total)).append("\n");
        graphique.append("★☆☆☆☆ ").append(barrePourcentage(distribution.get("mauvais"), total)).append("\n");

        return graphique.toString();
    }

    private String barrePourcentage(long count, int total) {
        int pourcentage = total > 0 ? (int) ((count * 100) / total) : 0;
        int barLength = pourcentage / 5; // Chaque 5% = un caractère
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            bar.append(i < barLength ? "█" : "░");
        }
        return bar.append(" ").append(pourcentage).append("% (").append(count).append(")").toString();
    }

    // ==================== MÉTHODES PRINCIPALES ====================

    public List<Avis> getAllAvis() {
        return avisDAO.getAllAvis();
    }

    public Avis getAvisById(int id) {
        return avisDAO.getAvisById(id);
    }

    public boolean modifierAvis(int id, String nomClient, String email, int note,
                                String commentaire, LocalDate dateAvis, int voyageId) {
        Avis avis = new Avis(id, nomClient, email, note, commentaire, dateAvis, voyageId);
        return avisDAO.updateAvis(avis);
    }

    public boolean modifierAvis(Avis avis) {
        return avisDAO.updateAvis(avis);
    }

    public boolean supprimerAvis(int id) {
        return avisDAO.deleteAvis(id);
    }

    // ==================== RECHERCHE POUR ADMIN ====================

    /**
     * Recherche avancée pour l'administration
     */
    public List<Avis> rechercherAvisAdmin(String recherche, String critere) {
        List<Avis> tousAvis = getAllAvis();

        if (recherche == null || recherche.trim().isEmpty()) {
            return tousAvis;
        }

        final String rechercheFinale = recherche.toLowerCase().trim();
        final String critereFinal = critere.toLowerCase();

        switch (critereFinal) {
            case "nom":
                return tousAvis.stream()
                        .filter(avis -> avis.getNomClient().toLowerCase().contains(rechercheFinale))
                        .collect(Collectors.toList());

            case "email":
                return tousAvis.stream()
                        .filter(avis -> avis.getEmail().toLowerCase().contains(rechercheFinale))
                        .collect(Collectors.toList());

            case "commentaire":
                return tousAvis.stream()
                        .filter(avis -> avis.getCommentaire().toLowerCase().contains(rechercheFinale))
                        .collect(Collectors.toList());

            case "note":
                try {
                    final int note = Integer.parseInt(rechercheFinale);
                    return tousAvis.stream()
                            .filter(avis -> avis.getNote() == note)
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    return List.of();
                }

            case "voyage":
                try {
                    final int voyageId = Integer.parseInt(rechercheFinale);
                    return tousAvis.stream()
                            .filter(avis -> avis.getVoyageId() == voyageId)
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    return List.of();
                }

            default: // Recherche globale
                return tousAvis.stream()
                        .filter(avis -> avis.getNomClient().toLowerCase().contains(rechercheFinale) ||
                                avis.getEmail().toLowerCase().contains(rechercheFinale) ||
                                avis.getCommentaire().toLowerCase().contains(rechercheFinale) ||
                                String.valueOf(avis.getNote()).contains(rechercheFinale) ||
                                String.valueOf(avis.getVoyageId()).contains(rechercheFinale))
                        .collect(Collectors.toList());
        }
    }

    // ==================== FONCTIONNALITÉS POUR LES CLIENTS ====================

    /**
     * Ajouter un avis client avec envoi d'email à l'admin
     */
    public boolean ajouterAvisClient(String nomClient, String email, int note,
                                     String commentaire, int voyageId) {
        Avis avis = new Avis();
        avis.setNomClient(nomClient);
        avis.setEmail(email);
        avis.setNote(note);
        avis.setCommentaire(commentaire);
        avis.setDateAvis(LocalDate.now());
        avis.setVoyageId(voyageId);

        boolean success = avisDAO.addAvis(avis);

        // Si l'avis a été ajouté avec succès, envoyer un email
        if (success) {
            try {
                // Récupérer l'avis complet avec son ID
                Avis nouvelAvis = avisDAO.getAvisByDetails(nomClient, email, voyageId);
                if (nouvelAvis != null) {
                    // Envoyer l'email dans un thread séparé pour ne pas bloquer l'application
                    new Thread(() -> {
                        System.out.println("📧 Tentative d'envoi d'email pour l'avis #" + nouvelAvis.getId());
                        EmailService.envoyerNotificationNouvelAvis(nouvelAvis);
                    }).start();
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de l'envoi de l'email: " + e.getMessage());
                // Ne pas bloquer l'ajout si l'email échoue
            }
        }

        return success;
    }

    public List<Avis> getAvisByVoyage(int voyageId) {
        return avisDAO.getAvisByVoyage(voyageId);
    }

    // ==================== RECHERCHE POUR CLIENT ====================

    /**
     * Recherche simple pour les clients (par voyage et note)
     */
    public List<Avis> rechercherAvisClient(int voyageId, Integer noteMin, Integer noteMax) {
        final Integer minFinal = noteMin;
        final Integer maxFinal = noteMax;

        List<Avis> avisVoyage = getAvisByVoyage(voyageId);

        return avisVoyage.stream()
                .filter(avis -> (minFinal == null || avis.getNote() >= minFinal) &&
                        (maxFinal == null || avis.getNote() <= maxFinal))
                .collect(Collectors.toList());
    }

    public double getNoteMoyenneVoyage(int voyageId) {
        double moyenne = avisDAO.getNoteMoyenne(voyageId);
        return Math.round(moyenne * 10.0) / 10.0;
    }

    public int getNombreAvisTotal() {
        return getAllAvis().size();
    }

    public int getNombreAvisParVoyage(int voyageId) {
        return getAvisByVoyage(voyageId).size();
    }
}