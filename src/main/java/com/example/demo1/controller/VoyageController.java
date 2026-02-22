package com.example.demo1.controller;
import javafx.stage.Modality;
import com.example.demo1.entity.Destination;
import com.example.demo1.entity.Voyage;
import com.example.demo1.services.DestinationService;
import com.example.demo1.services.VoyageServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VoyageController {

    // ===== FXML =====
    @FXML private VBox vboxVoyages;
    @FXML private ComboBox<String> cbFiltre;
    @FXML private TextField tfRecherche;
    @FXML private Label lblCount;

    // ===== SERVICES =====
    private VoyageServices voyageService;
    private DestinationService destinationService;

    // ===== DONNÉES =====
    private List<Voyage> tousLesVoyages;

    // ============================================
    // 🚀 INITIALISATION
    // ============================================
    @FXML
    public void initialize() {
        System.out.println("🌍 Initialisation VoyageController...");

        voyageService = new VoyageServices();
        destinationService = new DestinationService();

        if (cbFiltre != null) {
            cbFiltre.setItems(FXCollections.observableArrayList(
                    "Tous", "Prix croissant", "Prix décroissant"
            ));
            cbFiltre.getStyleClass().add("filtre-combo");
            cbFiltre.setOnAction(e -> appliquerFiltre());
        }

        if (tfRecherche != null) {
            tfRecherche.getStyleClass().add("search-field");
        }

        chargerVoyages();
    }

    // ============================================
    // 📋 CHARGER LES VOYAGES
    // ============================================
    private void chargerVoyages() {
        if (vboxVoyages != null) {
            vboxVoyages.getChildren().clear();
            Label chargement = new Label("🔄 Chargement des voyages...");
            chargement.getStyleClass().add("lbl-chargement");
            vboxVoyages.getChildren().add(chargement);
        }

        Task<List<Voyage>> task = new Task<>() {
            @Override
            protected List<Voyage> call() {
                return voyageService.getAllVoyages();
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                tousLesVoyages = task.getValue();
                afficherVoyages(tousLesVoyages);
                if (lblCount != null) {
                    lblCount.setText(tousLesVoyages.size() + " voyage(s)");
                    lblCount.getStyleClass().add("lbl-count");
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                vboxVoyages.getChildren().clear();
                Label erreur = new Label("❌ Erreur lors du chargement");
                erreur.getStyleClass().add("lbl-erreur");
                vboxVoyages.getChildren().add(erreur);
            });
        });

        new Thread(task).start();
    }

    // ============================================
    // 🗺️ AFFICHER LES VOYAGES - 3 par ligne
    // ============================================
    private void afficherVoyages(List<Voyage> voyages) {
        vboxVoyages.getChildren().clear();

        if (voyages.isEmpty()) {
            Label aucun = new Label("😕 Aucun voyage trouvé");
            aucun.getStyleClass().add("lbl-vide");
            vboxVoyages.getChildren().add(aucun);
            return;
        }

        HBox ligne = null;
        for (int i = 0; i < voyages.size(); i++) {
            if (i % 3 == 0) {
                ligne = new HBox(10);
                ligne.setPadding(new Insets(0, 0, 10, 0));
                // ✅ La ligne prend toute la largeur
                ligne.setFillHeight(true);
                vboxVoyages.getChildren().add(ligne);
            }

            VBox carte = creerCarteVoyage(voyages.get(i));

            // ✅ Chaque carte prend exactement 1/3
            HBox.setHgrow(carte, Priority.ALWAYS);
            carte.setMaxWidth(Double.MAX_VALUE);
            carte.setMinWidth(0);

            ligne.getChildren().add(carte);
        }

        // Remplir la dernière ligne si incomplète
        if (ligne != null && ligne.getChildren().size() < 3) {
            int manquants = 3 - ligne.getChildren().size();
            for (int i = 0; i < manquants; i++) {
                VBox vide = new VBox();
                HBox.setHgrow(vide, Priority.ALWAYS);
                vide.setMaxWidth(Double.MAX_VALUE);
                vide.setMinWidth(0);
                ligne.getChildren().add(vide);
            }
        }
    }

    // ============================================
    // 🃏 CRÉER CARTE VOYAGE - Style vertical
    // ============================================
    private VBox creerCarteVoyage(Voyage voyage) {
        VBox carte = new VBox(0);
        carte.getStyleClass().add("carte-voyage");
        carte.setMinWidth(0);
        carte.setMaxWidth(Double.MAX_VALUE);

        // ✅ Clip dynamique qui s'adapte à la taille réelle
        carte.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            Rectangle clip = new Rectangle(newVal.getWidth(), newVal.getHeight());
            clip.setArcWidth(28);
            clip.setArcHeight(28);
            carte.setClip(clip);
        });

        // ---- IMAGE ----
        StackPane imagePane = new StackPane();
        imagePane.setPrefHeight(200);
        imagePane.setMinHeight(200);
        imagePane.setMaxHeight(200);
        imagePane.setStyle("-fx-background-color: #eeeeee;");

        ImageView imgView = new ImageView();
        imgView.setFitHeight(200);
        imgView.setPreserveRatio(false);
        // ✅ Image s'adapte à la largeur de la carte
        imgView.fitWidthProperty().bind(carte.widthProperty());

        String imageUrl = voyage.getImagePath();
        if (voyage.getDestinationObj() != null &&
                voyage.getDestinationObj().getImageUrl() != null) {
            imageUrl = voyage.getDestinationObj().getImageUrl();
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image img;
                if (imageUrl.startsWith("/images/")) {
                    File f = new File("src/main/resources" + imageUrl);
                    img = f.exists()
                            ? new Image(f.toURI().toString())
                            : new Image(imageUrl, true);
                } else {
                    img = new Image(imageUrl, true);
                }
                imgView.setImage(img);
                imagePane.getChildren().add(imgView);
            } catch (Exception e) {
                ajouterPlaceholder(imagePane, voyage);
            }
        } else {
            ajouterPlaceholder(imagePane, voyage);
        }

        // ---- CONTENU ----
        VBox contenu = new VBox(6);
        contenu.setPadding(new Insets(15, 15, 10, 15));
        VBox.setVgrow(contenu, Priority.ALWAYS);

        String nomDest = voyage.getDestinationObj() != null
                ? voyage.getDestinationObj().getNom()
                : voyage.getDestination();

        Label lblDest = new Label("✈️ " + nomDest);
        lblDest.getStyleClass().add("lbl-destination");

        String pays = voyage.getDestinationObj() != null
                && voyage.getDestinationObj().getPays() != null
                ? "🌍 " + voyage.getDestinationObj().getPays() : "";
        Label lblPays = new Label(pays);
        lblPays.getStyleClass().add("lbl-pays");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dates = voyage.getDateDebut() != null && voyage.getDateFin() != null
                ? "📅 " + voyage.getDateDebut().format(fmt)
                + "  →  " + voyage.getDateFin().format(fmt) : "";
        Label lblDates = new Label(dates);
        lblDates.getStyleClass().add("lbl-dates");

        contenu.getChildren().addAll(lblDest, lblPays, lblDates);

        // Pays de départ
        String paysDepart = voyage.getPaysDepart() != null
                ? "🛫 Depuis: " + voyage.getPaysDepart() : "";
        if (!paysDepart.isEmpty()) {
            Label lblDepart = new Label(paysDepart);
            lblDepart.getStyleClass().add("lbl-depart");
            contenu.getChildren().add(lblDepart);
        }

        if (voyage.getDescription() != null && !voyage.getDescription().isEmpty()) {
            Label lblDesc = new Label(voyage.getDescription());
            lblDesc.getStyleClass().add("lbl-description");
            lblDesc.setWrapText(true);
            contenu.getChildren().add(lblDesc);
        }

        // ---- SÉPARATEUR ----
        Separator sep = new Separator();
        sep.getStyleClass().add("separator");

        // ---- FOOTER ----
        HBox footer = new HBox(8);
        footer.setPadding(new Insets(10, 12, 12, 12));
        footer.setAlignment(Pos.CENTER_LEFT);

        Label lblPrix = new Label(String.format("%.0f TND", voyage.getPrix()));
        lblPrix.getStyleClass().add("lbl-prix");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.getStyleClass().add("btn-modifier");
        btnModifier.setOnAction(e -> handleModifier(voyage));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.getStyleClass().add("btn-supprimer");
        btnSupprimer.setOnAction(e -> handleSupprimer(voyage));

        Button btnDetails = new Button("🔍 Détails");
        btnDetails.getStyleClass().add("btn-details");
        btnDetails.setOnAction(e -> ouvrirPopupInfo(voyage));

        footer.getChildren().addAll(lblPrix, spacer, btnModifier, btnSupprimer, btnDetails);

        carte.getChildren().addAll(imagePane, contenu, sep, footer);
        return carte;
    }

    // ============================================
    // 🖼️ PLACEHOLDER IMAGE
    // ============================================
    private void ajouterPlaceholder(StackPane imagePane, Voyage voyage) {
        imagePane.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);" +
                        "-fx-background-radius: 14 14 0 0;"
        );

        String nomDest = voyage.getDestinationObj() != null
                ? voyage.getDestinationObj().getNom()
                : voyage.getDestination();

        String initiale = (nomDest != null && !nomDest.isEmpty())
                ? String.valueOf(nomDest.charAt(0)).toUpperCase()
                : "✈";

        Label lblInitiale = new Label(initiale);
        lblInitiale.getStyleClass().add("image-placeholder-label");

        Label lblNom = new Label(nomDest != null ? nomDest : "Voyage");
        lblNom.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-weight: bold;");
        lblNom.setWrapText(true);
        lblNom.setMaxWidth(160);
        lblNom.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox vbox = new VBox(5, lblInitiale, lblNom);
        vbox.setAlignment(Pos.CENTER);
        imagePane.getChildren().add(vbox);
    }

    // ============================================
    // ➕ AJOUTER
    // ============================================
    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/AddVoyage.fxml")
            );
            Parent root = loader.load();

            AddVoyageController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("➕ Ajouter un voyage");
            stage.setScene(new Scene(root, 550, 700));
            stage.setOnHidden(e -> chargerVoyages());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Impossible d'ouvrir le formulaire:\n" + e.getMessage()).showAndWait();
        }
    }

    // ============================================
    // ✏️ MODIFIER
    // ============================================
    private void handleModifier(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/AddVoyage.fxml")
            );
            Parent root = loader.load();

            AddVoyageController controller = loader.getController();
            controller.preRemplir(voyage);

            Stage stage = new Stage();
            stage.setTitle("✏️ Modifier le voyage");
            stage.setScene(new Scene(root, 550, 700));
            stage.setOnHidden(e -> chargerVoyages());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Impossible d'ouvrir le formulaire").showAndWait();
        }
    }

    // ============================================
    // 🗑️ SUPPRIMER
    // ============================================
    private void handleSupprimer(Voyage voyage) {
        String nom = voyage.getDestinationObj() != null
                ? voyage.getDestinationObj().getNom()
                : voyage.getDestination();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce voyage ?");
        confirm.setContentText("Voyage vers " + nom + "\nCette action est irréversible !");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = voyageService.deleteVoyage(voyage.getId());
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "✅ Voyage supprimé !").showAndWait();
                chargerVoyages();
            } else {
                new Alert(Alert.AlertType.ERROR, "❌ Impossible de supprimer").showAndWait();
            }
        }
    }

    // ============================================
    // 🔍 RECHERCHE
    // ============================================
    @FXML
    private void handleRecherche() {
        if (tousLesVoyages == null || tfRecherche == null) return;

        String keyword = tfRecherche.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            afficherVoyages(tousLesVoyages);
            if (lblCount != null)
                lblCount.setText(tousLesVoyages.size() + " voyage(s)");
            return;
        }

        List<Voyage> filtres = tousLesVoyages.stream()
                .filter(v -> {
                    String dest = v.getDestinationObj() != null
                            ? v.getDestinationObj().getNom()
                            : v.getDestination();
                    return dest != null && dest.toLowerCase().contains(keyword);
                })
                .collect(Collectors.toList());

        afficherVoyages(filtres);
        if (lblCount != null)
            lblCount.setText(filtres.size() + " résultat(s)");
    }

    // ============================================
    // 🔄 FILTRER
    // ============================================
    @FXML
    private void appliquerFiltre() {
        if (tousLesVoyages == null || cbFiltre == null
                || cbFiltre.getValue() == null) return;

        List<Voyage> filtres;
        switch (cbFiltre.getValue()) {
            case "Prix croissant":
                filtres = tousLesVoyages.stream()
                        .sorted((a, b) -> Double.compare(a.getPrix(), b.getPrix()))
                        .collect(Collectors.toList());
                break;
            case "Prix décroissant":
                filtres = tousLesVoyages.stream()
                        .sorted((a, b) -> Double.compare(b.getPrix(), a.getPrix()))
                        .collect(Collectors.toList());
                break;
            default:
                filtres = tousLesVoyages;
        }
        afficherVoyages(filtres);
    }

    // ============================================
    // 🔄 ACTUALISER
    // ============================================
    @FXML
    private void handleActualiser() {
        if (tfRecherche != null) tfRecherche.clear();
        chargerVoyages();
    }

    // ============================================
    // 🔍 RECHERCHE VOLS PERSONNALISÉE
    // ============================================
    @FXML
    private void ouvrirRecherchePersonnalisee(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RechercheVols.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Recherche de vols");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshVoyages() {
        chargerVoyages();
    }


    private void ouvrirPopupInfo(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/DestinationDetail.fxml")
            );
            Parent root = loader.load();

            DestinationDetailController controller = loader.getController();
            controller.initialiser(voyage);  // ← passe le voyage

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("🌍 Détails destination");
            popup.setScene(new Scene(root, 620, 700));
            popup.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}