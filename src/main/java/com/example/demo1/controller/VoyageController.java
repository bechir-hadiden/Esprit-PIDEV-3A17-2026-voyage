package com.example.demo1.controller;

import com.example.demo1.entity.Voyage;
import com.example.demo1.services.VoyageServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;  // ← AJOUTEZ CET IMPORT

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class VoyageController {
    @FXML
    private FlowPane flowPane;

    @FXML
    private Button btnAdd;

    private VoyageServices voyageService;

    @FXML
    public void initialize() {
        voyageService = new VoyageServices();
        loadVoyages();
        System.out.println("✅ VoyageController initialisé");
    }

    private void loadVoyages() {
        flowPane.getChildren().clear();

        List<Voyage> voyages = voyageService.getAllVoyages();

        if (voyages == null || voyages.isEmpty()) {
            Label emptyLabel = new Label("Aucun voyage disponible");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #999;");
            flowPane.getChildren().add(emptyLabel);
        } else {
            for (Voyage voyage : voyages) {
                addVoyageCard(voyage);
            }
        }
    }

    public void handleEditVoyage(Voyage voyage) {
        try {
            System.out.println("📂 Chargement de editVoyage.fxml...");

            URL fxmlUrl = getClass().getResource("/fxml/editVoyage.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ editVoyage.fxml introuvable!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            System.out.println("✅ FXML chargé");

            EditVoyageController editController = loader.getController();
            editController.setVoyageController(this);
            editController.setVoyage(voyage); // Charger les données du voyage

            Stage stage = new Stage();
            stage.setTitle("Modifier le voyage - " + voyage.getDestination());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 550, 750));
            stage.setResizable(true);

            System.out.println("✅ Affichage de la fenêtre d'édition...");
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'ouverture du formulaire d'édition");
            e.printStackTrace();
        }
        }

    private void addVoyageCard(Voyage voyage) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(250);

        ImageView image = new ImageView();
        image.setFitWidth(250);
        image.setFitHeight(150);
        image.setPreserveRatio(false);

        try {
            String imagePath = voyage.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl != null) {
                    image.setImage(new Image(imageUrl.toExternalForm()));
                } else {
                    image.setStyle("-fx-background-color: #e0e0e0;");
                }
            } else {
                image.setStyle("-fx-background-color: #e0e0e0;");
            }
        } catch (Exception e) {
            image.setStyle("-fx-background-color: #e0e0e0;");
        }

        Label title = new Label(voyage.getDestination());
        title.getStyleClass().add("card-title");

        Label dateLabel = new Label("📅 " + voyage.getFormattedPeriod());

        Label priceLabel = new Label("💰 " + voyage.getPrix() + " €");
        priceLabel.getStyleClass().add("price");

        card.getChildren().addAll(image, title, dateLabel, priceLabel);

        card.setOnMouseClicked(event -> {
            System.out.println("Voyage sélectionné: " + voyage.getDestination());
        });

        flowPane.getChildren().add(card);


        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                // Double-clic : ouvrir le formulaire de modification
                System.out.println("✏️ Double-clic - Modification de: " + voyage.getDestination());
                handleEditVoyage(voyage);
            } else {
                // Simple clic : juste afficher dans la console
                System.out.println("Voyage sélectionné: " + voyage.getDestination());
            }
        });

        // Changer le curseur au survol pour indiquer que c'est cliquable
        card.setOnMouseEntered(e -> card.setStyle("-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-cursor: default;"));
        // ============= FIN MODIFICATION =============
    }

    // MÉTHODE IMPORTANTE - Vérifiez qu'elle existe
    @FXML
    public void handleAddVoyage() {
        System.out.println("========================================");
        System.out.println("BOUTON CLIQUÉ !");
        System.out.println("========================================");

        try {
            // Test 1: Vérifier si le fichier existe
            URL fxmlUrl = getClass().getResource("/fxml/addVoyage.fxml");
            System.out.println("URL du FXML: " + fxmlUrl);

            if (fxmlUrl == null) {
                System.err.println("❌ LE FICHIER addVoyage.fxml N'EXISTE PAS!");
                System.err.println("Créez le fichier dans: src/main/resources/fxml/addVoyage.fxml");

                // Afficher une alerte à l'utilisateur
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Fichier introuvable");
                alert.setContentText("Le fichier addVoyage.fxml est introuvable dans resources/fxml/");
                alert.showAndWait();
                return;
            }

            System.out.println("✅ Fichier trouvé, chargement...");

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            System.out.println("✅ FXML chargé");

            AddVoyageController addController = loader.getController();
            addController.setVoyageController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un voyage");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 550, 700));
            stage.setResizable(true);

            System.out.println("✅ Affichage de la fenêtre...");
            stage.showAndWait();
            System.out.println("✅ Fenêtre fermée");

        } catch (IOException e) {
            System.err.println("❌ ERREUR IOException:");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("❌ ERREUR Exception:");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur inattendue");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void refreshVoyages() {
        System.out.println("🔄 Rafraîchissement des voyages...");
        loadVoyages();
    }
}