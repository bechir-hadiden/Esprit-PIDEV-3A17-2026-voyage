package org.example.demo10.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.example.demo10.model.Avis;
import org.example.demo10.service.AvisService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientStatistiquesController {

    // ÉLÉMENTS EXISTANTS
    @FXML private Label lblNoteMoyenne;
    @FXML private Label lblTotalAvis;
    @FXML private Label lblTauxRecommandation;
    @FXML private Label lblNoteMax;
    @FXML private Label lblNoteMin;
    @FXML private ProgressBar progressBarNote;
    @FXML private TextArea txtGraphiqueNotes;
    @FXML private TextArea txtDerniersAvis;
    @FXML private TextField txtVoyageId;
    @FXML private Button btnVoirStats;
    @FXML private VBox boxDistribution;
    @FXML private Label lbl5Etoiles;
    @FXML private Label lbl4Etoiles;
    @FXML private Label lbl3Etoiles;
    @FXML private Label lbl2Etoiles;
    @FXML private Label lbl1Etoile;
    @FXML private ProgressBar bar5Etoiles;
    @FXML private ProgressBar bar4Etoiles;
    @FXML private ProgressBar bar3Etoiles;
    @FXML private ProgressBar bar2Etoiles;
    @FXML private ProgressBar bar1Etoile;

    // NOUVEAUX ÉLÉMENTS POUR STATISTIQUES CLIENT
    private String emailClient; // À définir quand l'utilisateur se connecte
    private TextFlow messageBienvenue;

    private AvisService avisService;

    @FXML
    public void initialize() {
        avisService = new AvisService();
        btnVoirStats.setOnAction(e -> chargerStatistiques());

        // Amélioration: Message d'accueil personnalisé
        txtDerniersAvis.setText("""
                🌟 ANALYSEUR D'AVIS VOYAGEURS
                
                📊 Cet outil vous permet d'analyser :
                • La satisfaction des voyageurs
                • Les notes détaillées
                • Les avis récents
                
                💡 Comment utiliser :
                1. Entrez l'ID du voyage
                2. Cliquez sur "Voir les statistiques"
                3. Découvrez l'analyse complète
                
                ✨ Les statistiques incluent :
                - Note moyenne et distribution
                - Taux de recommandation
                - Avis détaillés des voyageurs
                - Analyse qualitative
                """);
    }

    /**
     * Définit l'email du client connecté pour des statistiques personnalisées
     */
    public void setEmailClient(String email) {
        this.emailClient = email;
    }

    private void chargerStatistiques() {
        try {
            int voyageId = Integer.parseInt(txtVoyageId.getText().trim());
            Map<String, Object> stats = avisService.getStatistiquesClient(voyageId);

            int totalAvis = ((Number) stats.get("totalAvis")).intValue();

            if (totalAvis == 0) {
                afficherMessageVideAmeliore(stats);
                return;
            }

            afficherStatistiquesAmeliorees(stats);
            afficherAnalyseQualitative(stats);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un ID de voyage valide.");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    private void afficherMessageVideAmeliore(Map<String, Object> stats) {
        lblNoteMoyenne.setText("0.0/5");
        lblTotalAvis.setText("0");
        lblTauxRecommandation.setText("0%");
        lblNoteMax.setText("0");
        lblNoteMin.setText("0");
        progressBarNote.setProgress(0);
        progressBarNote.setStyle("-fx-accent: #6c757d;");

        txtGraphiqueNotes.setText("""
                📊 AUCUNE DONNÉE DISPONIBLE
                ═══════════════════════════

                Ce voyage n'a pas encore d'avis.

                💡 POURQUOI ?
                • Voyage récemment ajouté
                • Nouvelle destination
                • Avis en cours de collecte

                🎯 QUE FAIRE ?
                • Soyez le premier à donner votre avis
                • Contactez notre service client
                • Consultez des voyages similaires

                ⭐ En attendant, découvrez nos voyages
                les mieux notés dans d'autres destinations !
                """);

        txtDerniersAvis.setText("""
                🌟 PREMIER VOYAGEUR ATTENDU !

                Vous pouvez être le premier à :
                ✅ Partager votre expérience
                ✅ Noter ce voyage
                ✅ Aider d'autres voyageurs

                Après votre voyage, n'oubliez pas de :
                📝 Laisser un avis détaillé
                ⭐ Donner une note sincère
                📸 Partager vos photos

                "Le meilleur guide, c'est l'expérience des autres voyageurs"
                """);

        // Réinitialiser les barres
        bar5Etoiles.setProgress(0);
        bar4Etoiles.setProgress(0);
        bar3Etoiles.setProgress(0);
        bar2Etoiles.setProgress(0);
        bar1Etoile.setProgress(0);

        bar5Etoiles.setStyle("-fx-accent: #6c757d;");
        bar4Etoiles.setStyle("-fx-accent: #6c757d;");
        bar3Etoiles.setStyle("-fx-accent: #6c757d;");
        bar2Etoiles.setStyle("-fx-accent: #6c757d;");
        bar1Etoile.setStyle("-fx-accent: #6c757d;");

        lbl5Etoiles.setText("★★★★★ (0%)");
        lbl4Etoiles.setText("★★★★☆ (0%)");
        lbl3Etoiles.setText("★★★☆☆ (0%)");
        lbl2Etoiles.setText("★★☆☆☆ (0%)");
        lbl1Etoile.setText("★☆☆☆☆ (0%)");
    }

    private void afficherStatistiquesAmeliorees(Map<String, Object> stats) {
        try {
            double noteMoyenne = ((Number) stats.get("noteMoyenne")).doubleValue();
            int totalAvis = ((Number) stats.get("totalAvis")).intValue();
            int tauxRecommandation = ((Number) stats.get("tauxRecommandation")).intValue();
            int noteMax = ((Number) stats.get("noteMax")).intValue();
            int noteMin = ((Number) stats.get("noteMin")).intValue();

            // 1. Mise à jour des labels
            lblNoteMoyenne.setText(String.format("%.1f/5", noteMoyenne));
            lblTotalAvis.setText(String.valueOf(totalAvis));
            lblTauxRecommandation.setText(tauxRecommandation + "%");
            lblNoteMax.setText(noteMax + "/5");
            lblNoteMin.setText(noteMin + "/5");
            progressBarNote.setProgress(noteMoyenne / 5.0);

            // 2. Style dynamique selon la note
            String couleurNote;
            String couleurProgress;

            if (noteMoyenne >= 4.5) {
                couleurNote = "#28a745"; // Vert foncé
                couleurProgress = "#28a745";
            } else if (noteMoyenne >= 4.0) {
                couleurNote = "#20c997"; // Vert clair
                couleurProgress = "#20c997";
            } else if (noteMoyenne >= 3.0) {
                couleurNote = "#ffc107"; // Jaune
                couleurProgress = "#ffc107";
            } else if (noteMoyenne >= 2.0) {
                couleurNote = "#fd7e14"; // Orange
                couleurProgress = "#fd7e14";
            } else {
                couleurNote = "#dc3545"; // Rouge
                couleurProgress = "#dc3545";
            }

            lblNoteMoyenne.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + couleurNote + ";");
            progressBarNote.setStyle("-fx-accent: " + couleurProgress + ";");

            // 3. Graphique texte amélioré
            int voyageId = ((Number) stats.get("voyageId")).intValue();
            String graphique = avisService.genererGraphiqueNotes(voyageId);

            String interpretation = getInterpretationNote(noteMoyenne);
            String conseil = getConseilVoyage(noteMoyenne, tauxRecommandation);

            String graphiqueAmeliore = String.format("""
                    📊 ANALYSE DU VOYAGE #%d
                    ═══════════════════════

                    NOTE MOYENNE : %.1f/5
                    TAUX DE RECOMMANDATION : %d%%
                    NOMBRE D'AVIS : %d

                    📈 DISTRIBUTION DES NOTES :
                    %s

                    📝 INTERPRÉTATION :
                    %s

                    💡 CONSEIL :
                    %s
                    """,
                    voyageId, noteMoyenne, tauxRecommandation, totalAvis,
                    graphique, interpretation, conseil);

            txtGraphiqueNotes.setText(graphiqueAmeliore);

            // 4. Derniers avis améliorés
            afficherDerniersAvisAmeliores((List<Avis>) stats.get("derniersAvis"), totalAvis);

            // 5. Distribution détaillée
            Map<String, Long> distribution = (Map<String, Long>) stats.get("distributionSimple");
            afficherDistributionDetailleeAmelioree(distribution, totalAvis);

        } catch (Exception e) {
            showAlert("Erreur", "Erreur d'affichage des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getInterpretationNote(double note) {
        if (note >= 4.5) {
            return "⭐ EXCEPTIONNEL : Ce voyage dépasse toutes les attentes !\n" +
                    "Les voyageurs sont unanimes : une expérience inoubliable.";
        } else if (note >= 4.0) {
            return "👍 TRÈS BON : Un voyage de qualité qui satisfait la grande majorité.\n" +
                    "Peu de points négatifs, beaucoup de satisfaction.";
        } else if (note >= 3.5) {
            return "👌 BON : Un voyage correct qui offre une bonne expérience.\n" +
                    "Quelques points pourraient être améliorés.";
        } else if (note >= 3.0) {
            return "🤔 MOYEN : Des avis partagés, expérience mitigée.\n" +
                    "Lisez attentivement les commentaires pour plus de détails.";
        } else if (note >= 2.0) {
            return "⚠️ DÉCEVANT : Plusieurs voyageurs ont été déçus.\n" +
                    "Vérifiez les points négatifs avant de réserver.";
        } else {
            return "❌ TRÈS DÉCEVANT : Expérience négative pour la plupart.\n" +
                    "Nous déconseillons ce voyage en l'état.";
        }
    }

    private String getConseilVoyage(double note, int tauxRecommandation) {
        if (tauxRecommandation >= 90) {
            return "🌟 Réservez en toute confiance ! Ce voyage est très apprécié.";
        } else if (tauxRecommandation >= 75) {
            return "👍 Bon choix ! La plupart des voyageurs recommandent ce voyage.";
        } else if (tauxRecommandation >= 50) {
            return "🤔 À considérer avec attention. Lisez les avis récents.";
        } else if (tauxRecommandation >= 25) {
            return "⚠️ Risqué. Seulement " + tauxRecommandation + "% des voyageurs recommandent.";
        } else {
            return "❌ À éviter selon la majorité des voyageurs.";
        }
    }

    private void afficherDerniersAvisAmeliores(List<Avis> derniersAvis, int totalAvis) {
        if (derniersAvis == null || derniersAvis.isEmpty()) {
            txtDerniersAvis.setText("""
                    🌟 AUCUN AVIS RÉCENT
                    ════════════════════

                    Les avis pour ce voyage
                    seront bientôt disponibles.

                    💡 Revenez plus tard ou
                    soyez le premier à partager
                    votre expérience !
                    """);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🌟 DERNIERS TÉMOIGNAGES (").append(totalAvis).append(" avis)\n");
        sb.append("═══════════════════════════════\n\n");

        int compteur = 0;
        for (Avis avis : derniersAvis) {
            if (compteur >= 5) break;

            // En-tête de l'avis
            sb.append("👤 ").append(avis.getNomClient()).append("\n");

            // Étoiles avec indication de la note
            sb.append("★".repeat(avis.getNote()))
                    .append("☆".repeat(5 - avis.getNote()))
                    .append("  ").append(avis.getNote()).append("/5\n");

            // Commentaire
            String commentaire = avis.getCommentaire();
            if (commentaire.length() > 100) {
                sb.append("« ").append(commentaire, 0, 97).append("... »\n");
            } else {
                sb.append("« ").append(commentaire).append(" »\n");
            }

            // Date relative
            if (avis.getDateAvis() != null) {
                String dateRelative = getDateRelative(avis.getDateAvis());
                sb.append("📅 ").append(dateRelative).append("\n");
            }

            sb.append("─".repeat(40)).append("\n\n");
            compteur++;
        }

        if (totalAvis > 5) {
            sb.append("📊 ").append(totalAvis - 5).append(" avis supplémentaires disponibles\n");
        }

        txtDerniersAvis.setText(sb.toString());
    }

    private void afficherAnalyseQualitative(Map<String, Object> stats) {
        List<Avis> tousAvis = (List<Avis>) stats.get("derniersAvis");
        if (tousAvis == null || tousAvis.isEmpty()) return;

        // Analyse des mots-clés dans les commentaires
        Map<String, Long> motsCles = analyserMotsCles(tousAvis);

        // Ajouter l'analyse qualitative à txtGraphiqueNotes
        String analyse = "\n📊 ANALYSE QUALITATIVE DES COMMENTAIRES\n" +
                "═══════════════════════════════════\n";

        // Mots positifs fréquents
        long motsPositifs = motsCles.entrySet().stream()
                .filter(e -> e.getKey().matches(".*(magnifique|excellent|super|parfait|incroyable|beau|bien|top).*"))
                .mapToLong(Map.Entry::getValue)
                .sum();

        // Mots négatifs fréquents
        long motsNegatifs = motsCles.entrySet().stream()
                .filter(e -> e.getKey().matches(".*(déçu|problème|mauvais|pas bien|cher|dommage).*"))
                .mapToLong(Map.Entry::getValue)
                .sum();

        analyse += "✅ Sentiment positif : " + motsPositifs + " mentions\n";
        analyse += "❌ Sentiment négatif : " + motsNegatifs + " mentions\n";

        if (motsPositifs > motsNegatifs * 2) {
            analyse += "🎯 VERDICT : Très bonne réputation\n";
        } else if (motsPositifs > motsNegatifs) {
            analyse += "🎯 VERDICT : Réputation positive\n";
        } else if (motsPositifs == motsNegatifs) {
            analyse += "🎯 VERDICT : Avis mitigés\n";
        } else {
            analyse += "🎯 VERDICT : Points d'amélioration identifiés\n";
        }

        txtGraphiqueNotes.appendText(analyse);
    }

    private Map<String, Long> analyserMotsCles(List<Avis> avis) {
        return avis.stream()
                .flatMap(a -> List.of(a.getCommentaire().toLowerCase().split(" ")).stream())
                .filter(mot -> mot.length() > 3)
                .collect(Collectors.groupingBy(mot -> mot, Collectors.counting()));
    }

    private String getDateRelative(LocalDate date) {
        long jours = ChronoUnit.DAYS.between(date, LocalDate.now());

        if (jours == 0) return "Aujourd'hui";
        if (jours == 1) return "Hier";
        if (jours < 7) return "Il y a " + jours + " jours";
        if (jours < 30) return "Il y a " + (jours / 7) + " semaine" + ((jours / 7) > 1 ? "s" : "");
        if (jours < 365) return "Il y a " + (jours / 30) + " mois";
        return "Il y a " + (jours / 365) + " an" + ((jours / 365) > 1 ? "s" : "");
    }

    private void afficherDistributionDetailleeAmelioree(Map<String, Long> distribution, int total) {
        if (total == 0 || distribution == null) return;

        try {
            // 5 étoiles
            Long count5Obj = distribution.get("excellents");
            long count5 = count5Obj != null ? count5Obj : 0L;
            double percent5 = total > 0 ? (count5 * 100.0) / total : 0;
            bar5Etoiles.setProgress(percent5 / 100.0);
            bar5Etoiles.setStyle("-fx-accent: #28a745;");
            lbl5Etoiles.setText(String.format("★★★★★ %d (%.0f%%)", count5, percent5));

            // 4 étoiles
            Long count4Obj = distribution.get("bons");
            long count4 = count4Obj != null ? count4Obj : 0L;
            double percent4 = total > 0 ? (count4 * 100.0) / total : 0;
            bar4Etoiles.setProgress(percent4 / 100.0);
            bar4Etoiles.setStyle("-fx-accent: #20c997;");
            lbl4Etoiles.setText(String.format("★★★★☆ %d (%.0f%%)", count4, percent4));

            // 3 étoiles
            Long count3Obj = distribution.get("moyens");
            long count3 = count3Obj != null ? count3Obj : 0L;
            double percent3 = total > 0 ? (count3 * 100.0) / total : 0;
            bar3Etoiles.setProgress(percent3 / 100.0);
            bar3Etoiles.setStyle("-fx-accent: #ffc107;");
            lbl3Etoiles.setText(String.format("★★★☆☆ %d (%.0f%%)", count3, percent3));

            // 2 étoiles
            Long count2Obj = distribution.get("passables");
            long count2 = count2Obj != null ? count2Obj : 0L;
            double percent2 = total > 0 ? (count2 * 100.0) / total : 0;
            bar2Etoiles.setProgress(percent2 / 100.0);
            bar2Etoiles.setStyle("-fx-accent: #fd7e14;");
            lbl2Etoiles.setText(String.format("★★☆☆☆ %d (%.0f%%)", count2, percent2));

            // 1 étoile
            Long count1Obj = distribution.get("mauvais");
            long count1 = count1Obj != null ? count1Obj : 0L;
            double percent1 = total > 0 ? (count1 * 100.0) / total : 0;
            bar1Etoile.setProgress(percent1 / 100.0);
            bar1Etoile.setStyle("-fx-accent: #dc3545;");
            lbl1Etoile.setText(String.format("★☆☆☆☆ %d (%.0f%%)", count1, percent1));

        } catch (Exception e) {
            System.err.println("Erreur dans afficherDistributionDetaillee: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}