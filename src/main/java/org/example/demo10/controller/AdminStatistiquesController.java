package org.example.demo10.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import org.example.demo10.service.AvisService;
import java.util.Map;

public class AdminStatistiquesController {

    @FXML private Label lblTotalAvis;
    @FXML private Label lblMoyenneGlobale;
    @FXML private Label lblAvisDernierMois;
    @FXML private Label lblAvisAujourdhui;

    @FXML private BarChart<String, Number> barChartNotes;
    @FXML private PieChart pieChartDistribution;

    @FXML private TextArea txtRapport;
    @FXML private ComboBox<Integer> comboVoyageStats;
    @FXML private Button btnGenererRapport;
    @FXML private Button btnActualiserStats;

    private AvisService avisService;

    @FXML
    public void initialize() {
        avisService = new AvisService();

        // Initialiser le ComboBox des voyages
        initialiserComboVoyages();

        // Charger les statistiques générales
        chargerStatistiquesGenerales();

        // Configuration des boutons
        btnGenererRapport.setOnAction(e -> genererRapportComplet());
        btnActualiserStats.setOnAction(e -> {
            initialiserComboVoyages();
            chargerStatistiquesGenerales();
        });

        // Événement pour le ComboBox
        comboVoyageStats.setOnAction(e -> chargerStatistiquesVoyage());
    }

    private void initialiserComboVoyages() {
        try {
            Map<String, Object> stats = avisService.getStatistiquesAdmin();
            if (stats != null && stats.containsKey("avisParVoyage")) {
                Map<Integer, Long> avisParVoyage = (Map<Integer, Long>) stats.get("avisParVoyage");
                comboVoyageStats.setItems(FXCollections.observableArrayList(avisParVoyage.keySet()));
                if (!avisParVoyage.isEmpty()) {
                    comboVoyageStats.setValue(avisParVoyage.keySet().iterator().next());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du ComboBox: " + e.getMessage());
        }
    }

    private void chargerStatistiquesGenerales() {
        try {
            Map<String, Object> stats = avisService.getStatistiquesAdmin();

            if (stats == null) {
                txtRapport.setText("Aucune donnée disponible.");
                return;
            }

            // Mettre à jour les labels
            lblTotalAvis.setText(String.valueOf(stats.get("totalAvis")));
            lblMoyenneGlobale.setText(stats.get("moyenneGlobale") + "/5");
            lblAvisDernierMois.setText(String.valueOf(stats.get("avisDernierMois")));
            lblAvisAujourdhui.setText(String.valueOf(stats.get("avisAujourdhui")));

            // Mettre à jour les graphiques
            mettreAJourGraphiqueNotes(stats);
            mettreAJourPieChart(stats);

        } catch (Exception e) {
            txtRapport.setText("Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerStatistiquesVoyage() {
        Integer voyageId = comboVoyageStats.getValue();
        if (voyageId != null) {
            try {
                Map<String, Object> stats = avisService.getStatistiquesParVoyageAdmin(voyageId);
                afficherStatistiquesVoyage(stats);
            } catch (Exception e) {
                txtRapport.setText("Erreur pour le voyage #" + voyageId + ": " + e.getMessage());
            }
        }
    }

    private void mettreAJourGraphiqueNotes(Map<String, Object> stats) {
        try {
            barChartNotes.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Distribution des notes");

            Map<Integer, Long> distribution = (Map<Integer, Long>) stats.get("distributionNotes");

            if (distribution != null) {
                for (int i = 1; i <= 5; i++) {
                    long count = distribution.getOrDefault(i, 0L);
                    series.getData().add(new XYChart.Data<>(i + " étoiles", count));
                }
            }

            barChartNotes.getData().add(series);
            barChartNotes.setTitle("Distribution des notes (" + stats.get("totalAvis") + " avis)");

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du graphique: " + e.getMessage());
        }
    }

    private void mettreAJourPieChart(Map<String, Object> stats) {
        try {
            pieChartDistribution.getData().clear();

            Map<Integer, Long> distribution = (Map<Integer, Long>) stats.get("distributionNotes");
            long total = ((Number) stats.get("totalAvis")).longValue();

            if (total > 0 && distribution != null) {
                for (int i = 5; i >= 1; i--) {
                    long count = distribution.getOrDefault(i, 0L);
                    double pourcentage = (count * 100.0) / total;
                    PieChart.Data slice = new PieChart.Data(
                            i + "★ (" + String.format("%.1f", pourcentage) + "%)",
                            count
                    );
                    pieChartDistribution.getData().add(slice);
                }
            }

            pieChartDistribution.setTitle("Répartition des notes");

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du PieChart: " + e.getMessage());
        }
    }

    private void afficherStatistiquesVoyage(Map<String, Object> stats) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("=== STATISTIQUES VOYAGE #").append(stats.get("voyageId")).append(" ===\n\n");

            int totalAvis = ((Number) stats.get("totalAvis")).intValue();
            sb.append("Total d'avis: ").append(totalAvis).append("\n");
            sb.append("Note moyenne: ").append(stats.get("noteMoyenne")).append("/5\n");

            if (stats.containsKey("tauxRecommandation")) {
                sb.append("Taux de recommandation: ").append(stats.get("tauxRecommandation")).append("%\n");
            }

            if (stats.containsKey("meilleurNote")) {
                sb.append("Meilleure note: ").append(stats.get("meilleurNote")).append("/5\n");
            }

            if (stats.containsKey("pireNote")) {
                sb.append("Moins bonne note: ").append(stats.get("pireNote")).append("/5\n");
            }

            sb.append("\nDISTRIBUTION:\n");

            if (stats.containsKey("distributionNotes")) {
                Map<Integer, Long> distribution = (Map<Integer, Long>) stats.get("distributionNotes");
                for (int i = 5; i >= 1; i--) {
                    long count = distribution.getOrDefault(i, 0L);
                    double pourcentage = totalAvis > 0 ?
                            (count * 100.0 / totalAvis) : 0;
                    sb.append(i).append(" étoiles: ").append(count)
                            .append(" (").append(String.format("%.1f", pourcentage)).append("%)\n");
                }
            }

            txtRapport.setText(sb.toString());

        } catch (Exception e) {
            txtRapport.setText("Erreur d'affichage: " + e.getMessage());
        }
    }

    private void genererRapportComplet() {
        try {
            String rapport = avisService.genererRapportComplet();
            txtRapport.setText(rapport);
        } catch (Exception e) {
            txtRapport.setText("Erreur lors de la génération du rapport: " + e.getMessage());
        }
    }
}