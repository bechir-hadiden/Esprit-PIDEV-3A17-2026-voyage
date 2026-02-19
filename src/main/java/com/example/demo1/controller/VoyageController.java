package com.example.demo1.controller;

import com.example.demo1.entity.Destination;
import com.example.demo1.services.DestinationService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class VoyageController {

    @FXML private FlowPane destinationsGrid;
    @FXML private ComboBox<String> cbFiltre;
    @FXML private TextField tfOrigine;

    private DestinationService destinationService; // ✅ Déclaration
    private List<Destination> toutesLesDestinations;

    // ============================================
    // 🚀 INITIALISATION
    // ============================================
    @FXML
    public void initialize() {
        System.out.println("🌍 Initialisation de la page Voyages...");

        // ⬅️ AJOUT : Initialiser le service
        destinationService = new DestinationService();

        // Charger les destinations de manière asynchrone
        chargerDestinationsEnArrierePlan("TUN");

        // Listener sur le filtre
        if (cbFiltre != null) {
            cbFiltre.setOnAction(e -> appliquerFiltre());
        }
    }

    // ============================================
    // 📋 CHARGER LES DESTINATIONS
    // ============================================
    private void chargerDestinationsEnArrierePlan(String origine) {
        // Afficher un indicateur de chargement
        destinationsGrid.getChildren().clear();
        Label chargement = new Label("🔄 Chargement des destinations depuis l'API Amadeus...");
        chargement.setStyle("-fx-font-size: 18px; -fx-padding: 50;");
        destinationsGrid.getChildren().add(chargement);

        Task<List<Destination>> task = new Task<>() {
            @Override
            protected List<Destination> call() {
                return destinationService.getAll();  // ⬅️ Utiliser getAll()
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                toutesLesDestinations = task.getValue();
                if (toutesLesDestinations.isEmpty()) {
                    destinationsGrid.getChildren().clear();
                    Label erreur = new Label("❌ Aucune destination trouvée. Vérifiez votre connexion API.");
                    erreur.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-padding: 50;");
                    destinationsGrid.getChildren().add(erreur);
                } else {
                    afficherDestinations(toutesLesDestinations);
                    System.out.println("✅ " + toutesLesDestinations.size() + " destinations chargées");
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                destinationsGrid.getChildren().clear();
                Label erreur = new Label("❌ Erreur lors du chargement des destinations");
                erreur.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-padding: 50;");
                destinationsGrid.getChildren().add(erreur);
            });
        });

        new Thread(task).start();
    }

    public void refreshVoyages() {
        System.out.println("🔄 Rafraîchissement des voyages...");

        if (destinationsGrid != null) {
            chargerDestinationsEnArrierePlan("TUN");
        } else {
            System.err.println("❌ destinationsGrid est null, impossible de rafraîchir");
        }
    }

    private void afficherDestinations(List<Destination> destinations) {
        destinationsGrid.getChildren().clear();

        for (Destination dest : destinations) {
            VBox card = creerCarteDestination(dest);
            destinationsGrid.getChildren().add(card);
        }
    }

    private VBox creerCarteDestination(Destination dest) {
        VBox card = new VBox(15);
        card.setPrefSize(320, 450);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("destination-card");
        card.setPadding(new Insets(0));

        // Image
        VBox imageContainer = new VBox();
        imageContainer.setPrefHeight(200);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("-fx-background-color: #f0f0f0;");

        try {
            ImageView imageView = new ImageView(new Image(dest.getImageUrl(), true));
            imageView.setFitWidth(320);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(false);
            imageContainer.getChildren().add(imageView);
        } catch (Exception e) {
            Label placeholder = new Label("📷 " + dest.getNom());
            placeholder.setStyle("-fx-font-size: 20px;");
            imageContainer.getChildren().add(placeholder);
        }

        // Label catégorie (badge)
        if (!dest.getLabelCategorie().isEmpty()) {
            Label badge = new Label(dest.getLabelCategorie());
            badge.getStyleClass().add("badge");
            if (dest.isPromo()) {
                badge.getStyleClass().add("badge-promo");
            } else if (dest.getCategorie().equals("populaire")) {
                badge.getStyleClass().add("badge-populaire");
            } else {
                badge.getStyleClass().add("badge-nouveau");
            }
            StackPane badgeContainer = new StackPane(badge);
            badgeContainer.setAlignment(Pos.TOP_RIGHT);
            badgeContainer.setPadding(new Insets(10));

            StackPane imageWithBadge = new StackPane(imageContainer, badgeContainer);
            card.getChildren().add(imageWithBadge);
        } else {
            card.getChildren().add(imageContainer);
        }

        // Contenu
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.TOP_LEFT);

        // Titre
        Label titre = new Label(dest.getNomComplet());
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Description
        Label description = new Label(dest.getDescription());
        description.setWrapText(true);
        description.setMaxWidth(290);
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        // Prix
        HBox prixBox = new HBox(5);
        prixBox.setAlignment(Pos.CENTER_LEFT);

        Label prixLabel = new Label("À partir de");
        prixLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");

        Label prix = new Label(dest.getPrixFormate());
        prix.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        if (dest.isPromo() && dest.getReduction() > 0) {
            Label reduction = new Label("-" + dest.getReduction() + "%");
            reduction.setStyle("-fx-font-size: 14px; -fx-text-fill: #f44336; -fx-font-weight: bold;");
            prixBox.getChildren().addAll(prixLabel, prix, reduction);
        } else {
            prixBox.getChildren().addAll(prixLabel, prix);
        }

        // ⬅️ AJOUT : Bouton QR Code
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER);

        // Bouton Rechercher
        Button btnRechercher = new Button("Rechercher des vols");
        btnRechercher.getStyleClass().add("btn-rechercher");
        btnRechercher.setOnAction(e -> rechercherVolsPourDestination(dest));

        // Bouton QR Code
        Button btnQR = new Button("📱 QR Code");
        btnQR.setStyle(
                "-fx-background-color: #667eea;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );
        btnQR.setOnAction(e -> afficherQRCode(dest));

        boutons.getChildren().addAll(btnRechercher, btnQR);

        content.getChildren().addAll(titre, description, prixBox, boutons);
        card.getChildren().add(content);

        return card;
    }

    // ============================================
    // 📱 AFFICHER LE QR CODE
    // ============================================
    // Dans VoyageController.java

    private void afficherQRCode(Destination destination) {
        try {
            // ⬅️ APPEL AVEC LES BONS PARAMÈTRES
            QRCodeController.afficherPopupQRCode(
                    destination.getId(),      // int
                    destination.getNom()      // String
            );
        } catch (Exception e) {
            System.err.println("❌ Erreur affichage QR Code: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'afficher le QR Code");
            alert.showAndWait();
        }
    }

    @FXML
    private void actualiserPrix() {
        chargerDestinationsEnArrierePlan("TUN");
    }

    @FXML
    private void appliquerFiltre() {
        if (toutesLesDestinations == null) return;

        String filtre = cbFiltre.getValue();
        if (filtre == null || filtre.equals("Toutes")) {
            afficherDestinations(toutesLesDestinations);
            return;
        }

        List<Destination> filtrees = toutesLesDestinations.stream()
                .filter(d -> {
                    switch (filtre) {
                        case "Promotions": return d.isPromo();
                        case "Populaires": return d.getCategorie().equals("populaire");
                        case "Nouveautés": return d.getCategorie().equals("nouveau");
                        default: return true;
                    }
                })
                .collect(Collectors.toList());

        afficherDestinations(filtrees);
    }

    private void rechercherVolsPourDestination(Destination dest) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RechercheVols.fxml")
            );
            Parent root = loader.load();

            // ⬅️ CORRECTION : Utilisez le bon nom
            RechercheVolsAmelioreController controller = loader.getController();
            controller.preRemplirDestination(dest.getCodeIata());

            Stage stage = new Stage();
            stage.setTitle("Vols vers " + dest.getNom());
            stage.setScene(new Scene(root, 1000, 600));
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur ouverture recherche vols: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir la recherche de vols");
            alert.showAndWait();
        }
    }

    @FXML
    private void ouvrirRecherchePersonnalisee(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RechercheVols.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Recherche Personnalisée");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}