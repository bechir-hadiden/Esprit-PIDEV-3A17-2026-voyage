package com.example.demo1.controller;

import com.example.demo1.entity.Voyage;
import com.example.demo1.services.TripMapService;
import com.example.demo1.services.WeatherService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DestinationDetailController {

    // ===== FXML =====
    @FXML private Label lblTitreDestination;

    // ===== CARROUSEL =====
    @FXML private AnchorPane carouselPane;
    @FXML private VBox imagePreviewContainer;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private HBox hboxIndicateurs;
    @FXML private Label lblCompteur;

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

    // ===== ÉTAT CARROUSEL =====
    private List<String> listeImages = new ArrayList<>();
    private int indexImageCourant = 0;
    private ImageView imageView;

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

        if (nomDest != null && !nomDest.isEmpty()) {
            nomDest = nomDest.substring(0, 1).toUpperCase() + nomDest.substring(1).toLowerCase();
        }

        lblTitreDestination.setText("🌍 " + nomDest);

        // ✅ Initialiser le carrousel
        initialiserCarrousel(voyage);

        afficherInfosVoyage(voyage, nomDest);
        chargerMeteo(nomDest);
        chargerPointsInteret(nomDest);
    }

    // ============================================
    // 🎠 INITIALISER LE CARROUSEL
    // ============================================
    private void initialiserCarrousel(Voyage voyage) {
        // Préparer ImageView
        imageView = new ImageView();
        imageView.setFitHeight(250);
        imageView.setFitWidth(560);
        imageView.setPreserveRatio(false);

        // Récupérer toutes les images de la destination
        listeImages = recupererImages(voyage);
        indexImageCourant = 0;

        System.out.println("🖼️ Carrousel popup: " + listeImages.size() + " image(s)");

        if (listeImages.isEmpty()) {
            // Placeholder si aucune image
            afficherPlaceholder(voyage);
            cacherBoutons();
            return;
        }

        // Afficher la première image
        afficherImageIndex(0);

        // Boutons visibles seulement si plusieurs images
        boolean multi = listeImages.size() > 1;
        btnPrev.setVisible(multi);
        btnPrev.setManaged(multi);
        btnNext.setVisible(multi);
        btnNext.setManaged(multi);

        mettreAJourIndicateurs();
        mettreAJourCompteur();
    }

    // ============================================
    // 🖼️ AFFICHER IMAGE À L'INDEX
    // ============================================
    private void afficherImageIndex(int index) {
        imagePreviewContainer.getChildren().clear();

        String imageUrl = listeImages.get(index);
        System.out.println("📷 Image [" + index + "] : " + imageUrl);

        // ✅ Lier la taille de l'ImageView au carouselPane
        imageView.setFitHeight(260);
        imageView.fitWidthProperty().bind(carouselPane.widthProperty());
        imageView.setPreserveRatio(false);

        try {
            Image image = null;

            if (imageUrl.startsWith("/images/")) {
                // Essai 1 : chemin fichier
                File f = new File("src/main/resources" + imageUrl);
                System.out.println("📁 Fichier existe: " + f.exists() + " → " + f.getAbsolutePath());
                if (f.exists()) {
                    image = new Image(f.toURI().toString());
                } else {
                    // Essai 2 : classpath resource
                    var res = getClass().getResource(imageUrl);
                    System.out.println("🔗 Resource URL: " + res);
                    if (res != null) image = new Image(res.toExternalForm());
                }
            } else if (imageUrl.startsWith("http")) {
                image = new Image(imageUrl, true);
            } else {
                File f = new File(imageUrl);
                if (f.exists()) image = new Image(f.toURI().toString());
            }

            if (image != null && !image.isError()) {
                imageView.setImage(image);
                imagePreviewContainer.getChildren().add(imageView);
                System.out.println("✅ Image chargée avec succès !");
            } else {
                System.err.println("❌ Image null ou erreur pour: " + imageUrl);
                afficherPlaceholderLabel();
            }

        } catch (Exception e) {
            System.err.println("❌ Exception image: " + e.getMessage());
            afficherPlaceholderLabel();
        }
    }

    // ============================================
    // ◀ PRÉCÉDENT
    // ============================================
    @FXML
    private void handlePrev() {
        if (listeImages.isEmpty()) return;
        indexImageCourant = (indexImageCourant - 1 + listeImages.size()) % listeImages.size();
        afficherImageIndex(indexImageCourant);
        mettreAJourIndicateurs();
        mettreAJourCompteur();
    }

    // ============================================
    // ▶ SUIVANT
    // ============================================
    @FXML
    private void handleNext() {
        if (listeImages.isEmpty()) return;
        indexImageCourant = (indexImageCourant + 1) % listeImages.size();
        afficherImageIndex(indexImageCourant);
        mettreAJourIndicateurs();
        mettreAJourCompteur();
    }

    // ============================================
    // 🔵 INDICATEURS POINTS
    // ============================================
    private void mettreAJourIndicateurs() {
        hboxIndicateurs.getChildren().clear();
        for (int i = 0; i < listeImages.size(); i++) {
            Circle point = new Circle(5);
            point.setFill(i == indexImageCourant
                    ? Color.web("#667eea")
                    : Color.web("#cccccc"));
            point.setStyle("-fx-cursor: hand;");
            final int idx = i;
            point.setOnMouseClicked(e -> {
                indexImageCourant = idx;
                afficherImageIndex(indexImageCourant);
                mettreAJourIndicateurs();
                mettreAJourCompteur();
            });
            hboxIndicateurs.getChildren().add(point);
        }
    }

    private void mettreAJourCompteur() {
        if (listeImages.size() > 1) {
            lblCompteur.setText((indexImageCourant + 1) + " / " + listeImages.size());
        } else {
            lblCompteur.setText("");
        }
    }

    private void cacherBoutons() {
        btnPrev.setVisible(false);
        btnPrev.setManaged(false);
        btnNext.setVisible(false);
        btnNext.setManaged(false);
        hboxIndicateurs.getChildren().clear();
        lblCompteur.setText("");
    }

    // ============================================
    // 📷 PLACEHOLDERS
    // ============================================
    private void afficherPlaceholder(Voyage voyage) {
        imagePreviewContainer.getChildren().clear();
        imagePreviewContainer.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                        "-fx-background-radius: 12;");
        String nom = voyage.getDestinationObj() != null
                ? voyage.getDestinationObj().getNom() : voyage.getDestination();
        String initiale = (nom != null && !nom.isEmpty())
                ? String.valueOf(nom.charAt(0)).toUpperCase() : "✈";
        Label li = new Label(initiale);
        li.setStyle("-fx-font-size: 60px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label ln = new Label(nom != null ? nom : "Destination");
        ln.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.85); -fx-font-weight: bold;");
        VBox vb = new VBox(8, li, ln);
        vb.setAlignment(Pos.CENTER);
        imagePreviewContainer.getChildren().add(vb);
    }

    private void afficherPlaceholderLabel() {
        imagePreviewContainer.getChildren().clear();
        Label lbl = new Label("📷 Image non disponible");
        lbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;");
        imagePreviewContainer.getChildren().add(lbl);
    }

    // ============================================
    // ✅ RÉCUPÉRER LES IMAGES DU VOYAGE
    // ============================================
    private List<String> recupererImages(Voyage voyage) {
        List<String> images = new ArrayList<>();

        if (voyage.getDestinationObj() != null) {
            String destUrl = voyage.getDestinationObj().getImageUrl();
            if (destUrl != null && !destUrl.isEmpty()) {

                // ✅ Format JSON ["url1","url2"]
                if (destUrl.trim().startsWith("[")) {
                    destUrl = destUrl.replaceAll("[\\[\\]\"]", "");
                    Arrays.stream(destUrl.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .forEach(images::add);
                } else {
                    // ✅ Séparateur | OU ;
                    Arrays.stream(destUrl.split("[|;]"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .forEach(images::add);
                }
                if (!images.isEmpty()) return images;
            }
        }

        // Fallback : imagePath du voyage
        String imagePath = voyage.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            Arrays.stream(imagePath.split("[|;]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(images::add);
        }

        return images;
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
    // 🌤️ MÉTÉO
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
    // 🏛️ POINTS D'INTÉRÊT
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
                    vboxPointsInteret.getChildren().add(creerItemPointInteret(point));
                }
            });
        }).start();
    }

    private HBox creerItemPointInteret(TripMapService.PointInteret point) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setStyle("-fx-background-color: #f8f9ff; -fx-background-radius: 8;" +
                "-fx-border-color: #e8ecf4; -fx-border-radius: 8; -fx-border-width: 1;");
        Label lblEmoji = new Label(point.emoji);
        lblEmoji.setStyle("-fx-font-size: 22px;");
        VBox infos = new VBox(2);
        Label lblNom = new Label(point.nom);
        lblNom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label lblCategorie = new Label(point.categorie);
        lblCategorie.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        infos.getChildren().addAll(lblNom, lblCategorie);
        item.getChildren().addAll(lblEmoji, infos);
        if (point.rating > 0) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label lblRating = new Label(getEtoiles(point.rating));
            lblRating.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFA726;");
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