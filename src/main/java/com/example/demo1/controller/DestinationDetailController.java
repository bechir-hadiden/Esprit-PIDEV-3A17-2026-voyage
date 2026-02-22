package com.example.demo1.controller;

import com.example.demo1.entity.Voyage;
import com.example.demo1.services.TripMapService;
import com.example.demo1.services.WeatherService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 🌍 DestinationDetailController
 * Popup affichant météo + points d'intérêt pour un voyage
 */
public class DestinationDetailController {

    // ===== FXML =====
    @FXML private Label lblTitreDestination;

    // Météo
    @FXML private Label lblMeteoChargement;
    @FXML private HBox  hboxMeteo;
    @FXML private Label lblMeteoEmoji;
    @FXML private Label lblMeteoDesc;
    @FXML private Label lblMeteoTemp;
    @FXML private Label lblMeteoMinMax;
    @FXML private Label lblMeteoHumidite;
    @FXML private Label lblMeteoVent;
    @FXML private Label lblMeteoErreur;

    // Points d'intérêt
    @FXML private VBox vboxPointsInteret;

    // Infos voyage
    @FXML private Label lblInfoDates;
    @FXML private Label lblInfoPrix;
    @FXML private Label lblInfoDepart;
    @FXML private Label lblInfoDesc;

    // ===== SERVICES =====
    private final WeatherService weatherService = new WeatherService();
    private final TripMapService tripMapService  = new TripMapService();

    // ===== VOYAGE =====
    private Voyage voyage;

    // ============================================
    // 🚀 INITIALISER AVEC LE VOYAGE
    // ============================================
    public void initialiser(Voyage voyage) {
        this.voyage = voyage;

        String nomDest = voyage.getDestinationObj() != null
                ? voyage.getDestinationObj().getNom()
                : voyage.getDestination();


        // ✅ Capitaliser : "dubai" → "Dubai"
        if (nomDest != null && !nomDest.isEmpty()) {
            nomDest = nomDest.substring(0, 1).toUpperCase() + nomDest.substring(1).toLowerCase();
        }
        // Titre
        lblTitreDestination.setText("🌍 " + nomDest);

        // Infos voyage
        afficherInfosVoyage(voyage, nomDest);

        // Charger météo + points d'intérêt en arrière-plan
        chargerMeteo(nomDest);
        chargerPointsInteret(nomDest);
    }

    // ============================================
    // ✈️ INFOS VOYAGE
    // ============================================
    private void afficherInfosVoyage(Voyage voyage, String nomDest) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (voyage.getDateDebut() != null && voyage.getDateFin() != null) {
            lblInfoDates.setText("📅 Du " + voyage.getDateDebut().format(fmt)
                    + " au " + voyage.getDateFin().format(fmt));
        }

        lblInfoPrix.setText("💰 Prix : " + String.format("%.0f TND", voyage.getPrix())
                + " par personne");

        if (voyage.getPaysDepart() != null && !voyage.getPaysDepart().isEmpty()) {
            lblInfoDepart.setText("🛫 Départ depuis : " + voyage.getPaysDepart());
        } else {
            lblInfoDepart.setVisible(false);
            lblInfoDepart.setManaged(false);
        }

        if (voyage.getDescription() != null && !voyage.getDescription().isEmpty()) {
            lblInfoDesc.setText("📝 " + voyage.getDescription());
        } else {
            lblInfoDesc.setVisible(false);
            lblInfoDesc.setManaged(false);
        }
    }

    // ============================================
    // 🌤️ CHARGER LA MÉTÉO
    // ============================================
    private void chargerMeteo(String ville) {
        new Thread(() -> {
            WeatherService.WeatherData meteo = weatherService.getMeteo(ville);
            Platform.runLater(() -> {
                if (meteo != null) {
                    afficherMeteo(meteo);
                } else {
                    lblMeteoChargement.setVisible(false);
                    lblMeteoChargement.setManaged(false);
                    lblMeteoErreur.setText("❌ Météo indisponible pour cette destination");
                    lblMeteoErreur.setVisible(true);
                    lblMeteoErreur.setManaged(true);
                }
            });
        }).start();
    }

    private void afficherMeteo(WeatherService.WeatherData meteo) {
        lblMeteoChargement.setVisible(false);
        lblMeteoChargement.setManaged(false);

        lblMeteoEmoji.setText(meteo.emoji);
        lblMeteoDesc.setText(meteo.description);
        lblMeteoTemp.setText(String.format("%.0f°C", meteo.temperature));
        lblMeteoMinMax.setText("🌡️ Min " + String.format("%.0f°C", meteo.temperatureMin)
                + " / Max " + String.format("%.0f°C", meteo.temperatureMax));
        lblMeteoHumidite.setText("💧 Humidité : " + meteo.humidite + "%");
        lblMeteoVent.setText("💨 Vent : " + String.format("%.0f", meteo.vitesseVent) + " km/h");

        hboxMeteo.setVisible(true);
        hboxMeteo.setManaged(true);
    }

    // ============================================
    // 🏛️ CHARGER LES POINTS D'INTÉRÊT
    // ============================================
    private void chargerPointsInteret(String ville) {
        new Thread(() -> {
            List<TripMapService.PointInteret> points =
                    tripMapService.getPointsInteret(ville, 8);

            Platform.runLater(() -> {
                vboxPointsInteret.getChildren().clear();

                if (points.isEmpty()) {
                    Label aucun = new Label("😕 Aucun point d'intérêt trouvé");
                    aucun.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");
                    vboxPointsInteret.getChildren().add(aucun);
                    return;
                }

                for (TripMapService.PointInteret point : points) {
                    HBox item = creerItemPointInteret(point);
                    vboxPointsInteret.getChildren().add(item);
                }
            });
        }).start();
    }

    private HBox creerItemPointInteret(TripMapService.PointInteret point) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setStyle(
                "-fx-background-color: #f8f9ff;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e8ecf4;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );

        // Emoji catégorie
        Label lblEmoji = new Label(point.emoji);
        lblEmoji.setStyle("-fx-font-size: 22px;");

        // Nom + catégorie
        VBox infos = new VBox(2);
        Label lblNom = new Label(point.nom);
        lblNom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label lblCategorie = new Label(point.categorie);
        lblCategorie.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        infos.getChildren().addAll(lblNom, lblCategorie);

        // Étoiles rating
        Label lblRating = new Label(getEtoiles(point.rating));
        lblRating.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFA726;");

        item.getChildren().addAll(lblEmoji, infos);

        if (point.rating > 0) {
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            item.getChildren().addAll(spacer, lblRating);
        }

        return item;
    }

    private String getEtoiles(int rating) {
        if (rating >= 3) return "⭐⭐⭐";
        if (rating >= 2) return "⭐⭐";
        if (rating >= 1) return "⭐";
        return "";
    }

    // ============================================
    // ❌ FERMER
    // ============================================
    @FXML
    private void fermer() {
        Stage stage = (Stage) lblTitreDestination.getScene().getWindow();
        stage.close();
    }
}