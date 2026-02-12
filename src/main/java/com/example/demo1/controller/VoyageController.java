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
    @FXML private TextField tfOrigine; // Nouveau : pour changer l'origine

    private List<Destination> toutesLesDestinations;

    @FXML
    public void initialize() {
        System.out.println("🌍 Initialisation de la page Voyages...");

        // Charger les destinations de manière asynchrone
        chargerDestinationsEnArrierePlan("TUN");

        // Listener sur le filtre
        if (cbFiltre != null) {
            cbFiltre.setOnAction(e -> appliquerFiltre());
        }
    }

    private void chargerDestinationsEnArrierePlan(String origine) {
        // Afficher un indicateur de chargement
        destinationsGrid.getChildren().clear();
        Label chargement = new Label("🔄 Chargement des destinations depuis l'API Amadeus...");
        chargement.setStyle("-fx-font-size: 18px; -fx-padding: 50;");
        destinationsGrid.getChildren().add(chargement);

        Task<List<Destination>> task = new Task<>() {
            @Override
            protected List<Destination> call() {
                return DestinationService.getDestinationsDepuis(origine);
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
            // Réinitialiser et recharger
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

        // Bouton
        Button btnRechercher = new Button("Rechercher des vols");
        btnRechercher.getStyleClass().add("btn-rechercher");
        btnRechercher.setMaxWidth(Double.MAX_VALUE);
        btnRechercher.setOnAction(e -> rechercherVolsPourDestination(dest));

        content.getChildren().addAll(titre, description, prixBox, btnRechercher);
        card.getChildren().add(content);

        return card;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RechercheVols.fxml"));
            Parent root = loader.load();

            VolsController controller = loader.getController();
            controller.preRemplirDestination(dest.getCodeIATA());

            Stage stage = new Stage();
            stage.setTitle("Vols vers " + dest.getNom());
            stage.setScene(new Scene(root, 1000, 600));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
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