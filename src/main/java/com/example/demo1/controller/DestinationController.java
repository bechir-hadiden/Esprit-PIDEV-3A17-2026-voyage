package com.example.demo1.controller;

import com.example.demo1.entity.Destination;
import com.example.demo1.services.DestinationService;
import com.example.demo1.services.QRCodeService;
import com.example.demo1.services.YouTubeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DestinationController {

    // ===== COMPOSANTS FXML =====
    @FXML private GridPane gridDestinations;
    @FXML private Label lblCount;
    @FXML private TextField tfRecherche;
    @FXML private HBox hboxLoading;

    // ===== SERVICES =====
    private DestinationService destinationService;
    private YouTubeService youtubeService;
    private QRCodeService qrCodeService;

    // ===== DONNÉES =====
    private List<Destination> toutesDestinations;

    private static final int COLONNES = 3;

    // ============================================
    // 🚀 INITIALISATION
    // ============================================
    @FXML
    public void initialize() {
        destinationService = new DestinationService();
        youtubeService     = new YouTubeService();
        qrCodeService      = new QRCodeService();
        chargerDestinations();
    }

    // ============================================
    // 📋 CHARGER ET AFFICHER LES DESTINATIONS
    // ============================================
    private void chargerDestinations() {
        if (hboxLoading != null) hboxLoading.setVisible(true);
        gridDestinations.getChildren().clear();

        new Thread(() -> {
            toutesDestinations = destinationService.getAll();
            Platform.runLater(() -> {
                afficherDestinations(toutesDestinations);
                if (lblCount != null)
                    lblCount.setText(toutesDestinations.size() + " destinations");
                if (hboxLoading != null)
                    hboxLoading.setVisible(false);
            });
        }).start();
    }

    // ============================================
    // 🗺️ AFFICHER LES CARTES
    // ============================================
    private void afficherDestinations(List<Destination> destinations) {
        gridDestinations.getChildren().clear();

        if (destinations.isEmpty()) {
            Label aucun = new Label("😕 Aucune destination trouvée");
            aucun.setStyle("-fx-font-size: 18px; -fx-text-fill: #999; -fx-padding: 50;");
            gridDestinations.add(aucun, 0, 0);
            return;
        }

        int col = 0, row = 0;
        for (Destination destination : destinations) {
            VBox carte = creerCarte(destination);
            gridDestinations.add(carte, col, row);
            col++;
            if (col >= COLONNES) { col = 0; row++; }
        }
    }

    // ============================================
    // 🃏 CRÉER UNE CARTE
    // ============================================
    private VBox creerCarte(Destination destination) {
        VBox carte = new VBox(0);
        carte.setPrefWidth(280);
        carte.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        );

        ImageView imgView = new ImageView();
        imgView.setFitWidth(280);
        imgView.setFitHeight(160);
        imgView.setPreserveRatio(false);

        List<String> images = destination.getImages();
        String imageUrl = images.isEmpty() ? destination.getImageUrl() : images.get(0);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image img;
                if (imageUrl.startsWith("/images/")) {
                    var resource = getClass().getResource(imageUrl);
                    if (resource != null) {
                        img = new Image(resource.toExternalForm(), true);
                    } else {
                        File fichier = new File("src/main/resources" + imageUrl);
                        img = fichier.exists() ? new Image(fichier.toURI().toString(), true) : null;
                    }
                } else {
                    img = new Image(imageUrl, true);
                }
                if (img != null) imgView.setImage(img);
            } catch (Exception e) {
                System.err.println("❌ Image non chargée: " + imageUrl);
            }
        }

        StackPane imageContainer = new StackPane(imgView);
        imageContainer.setStyle("-fx-background-color: #667eea; -fx-background-radius: 15 15 0 0;");

        if (destination.getCodeIata() != null && !destination.getCodeIata().isEmpty()) {
            Label badge = new Label(destination.getCodeIata());
            badge.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-padding: 4 8;" +
                            "-fx-background-radius: 5; -fx-font-size: 11px;"
            );
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            StackPane.setMargin(badge, new Insets(10));
            imageContainer.getChildren().add(badge);
        }

        // ✅ Badge nombre d'images
        if (images.size() > 1) {
            Label badgeImages = new Label("📸 " + images.size());
            badgeImages.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white;" +
                            "-fx-font-size: 10px; -fx-padding: 3 7; -fx-background-radius: 5;"
            );
            StackPane.setAlignment(badgeImages, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(badgeImages, new Insets(0, 8, 8, 0));
            imageContainer.getChildren().add(badgeImages);
        }

        VBox contenu = new VBox(8);
        contenu.setPadding(new Insets(15));

        Label lblNom = new Label(destination.getNom());
        lblNom.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        lblNom.setTextFill(Color.valueOf("#333333"));

        Label lblPays = new Label("🌍 " + (destination.getPays() != null ?
                destination.getPays() : "Pays inconnu"));
        lblPays.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Label lblDesc = new Label(destination.getDescription() != null ?
                destination.getDescription() : "Aucune description");
        lblDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxHeight(40);

        Label lblVideo = new Label(
                destination.getVideoUrl() != null && !destination.getVideoUrl().isEmpty()
                        ? "🎬 Vidéo disponible" : "⏳ Vidéo non chargée"
        );
        lblVideo.setStyle(
                destination.getVideoUrl() != null && !destination.getVideoUrl().isEmpty()
                        ? "-fx-font-size: 11px; -fx-text-fill: green;"
                        : "-fx-font-size: 11px; -fx-text-fill: orange;"
        );

        contenu.getChildren().addAll(lblNom, lblPays, lblDesc, lblVideo);

        HBox boutons = new HBox(8);
        boutons.setPadding(new Insets(10, 15, 15, 15));
        boutons.setAlignment(Pos.CENTER);

        Button btnQR = new Button("📱 QR Code");
        btnQR.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;" +
                "-fx-font-size: 12px; -fx-padding: 7 12; -fx-background-radius: 15; -fx-cursor: hand;");
        btnQR.setOnAction(e -> handleQRCode(destination));

        Button btnModifier = new Button("✏️");
        btnModifier.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white;" +
                "-fx-font-size: 12px; -fx-padding: 7 10; -fx-background-radius: 15; -fx-cursor: hand;");
        btnModifier.setOnAction(e -> handleModifier(destination));

        Button btnSupprimer = new Button("🗑️");
        btnSupprimer.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white;" +
                "-fx-font-size: 12px; -fx-padding: 7 10; -fx-background-radius: 15; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> handleSupprimer(destination));

        Region espaceur = new Region();
        HBox.setHgrow(espaceur, Priority.ALWAYS);
        boutons.getChildren().addAll(btnQR, espaceur, btnModifier, btnSupprimer);

        carte.getChildren().addAll(imageContainer, contenu, boutons);
        return carte;
    }

    // ============================================
    // ➕ AJOUTER UNE DESTINATION
    // ✅ FIX : save() ne doit PAS appeler saveImages() en interne
    //          On gère les images ici APRÈS avoir obtenu l'ID
    // ============================================
    @FXML
    private void handleAjouter() {
        Dialog<Destination> dialog = creerDialogFormulaire(null);
        Optional<Destination> result = dialog.showAndWait();
        result.ifPresent(destination -> {
            // ✅ save() stocke directement toutes les images dans image_url
            boolean success = destinationService.save(destination);

            if (success) {
                afficherAlert(Alert.AlertType.INFORMATION, "Succès",
                        "✅ Destination ajoutée avec " + destination.getImages().size() + " image(s) !");
                chargerDestinations();
            } else {
                afficherAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter la destination");
            }
        });
    }

    // ============================================
    // ✏️ MODIFIER UNE DESTINATION
    // ✅ FIX : supprimer les anciennes images PUIS insérer les nouvelles
    // ============================================
    private void handleModifier(Destination destination) {
        Dialog<Destination> dialog = creerDialogFormulaire(destination);
        Optional<Destination> result = dialog.showAndWait();
        result.ifPresent(dest -> {
            dest.setId(destination.getId());

            // ✅ update() stocke directement toutes les images dans image_url
            boolean success = destinationService.update(dest);
            if (success) {
                afficherAlert(Alert.AlertType.INFORMATION, "Succès",
                        "✅ Destination modifiée avec " + dest.getImages().size() + " image(s) !");
                chargerDestinations();
            } else {
                afficherAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier");
            }
        });
    }

    // ============================================
    // 🗑️ SUPPRIMER
    // ============================================
    private void handleSupprimer(Destination destination) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + destination.getNom() + " ?");
        confirm.setContentText("Cette action est irréversible.\nTous les voyages liés seront aussi supprimés !");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = destinationService.delete(destination.getId());
            if (success) {
                afficherAlert(Alert.AlertType.INFORMATION, "Succès", "Destination supprimée !");
                chargerDestinations();
            } else {
                afficherAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer");
            }
        }
    }

    // ============================================
    // 🔍 RECHERCHE
    // ============================================
    @FXML
    private void handleRecherche() {
        if (tfRecherche == null || toutesDestinations == null) return;
        String keyword = tfRecherche.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            afficherDestinations(toutesDestinations);
            if (lblCount != null) lblCount.setText(toutesDestinations.size() + " destinations");
            return;
        }
        List<Destination> filtrees = toutesDestinations.stream()
                .filter(d -> d.getNom().toLowerCase().contains(keyword) ||
                        (d.getPays() != null && d.getPays().toLowerCase().contains(keyword)) ||
                        (d.getCodeIata() != null && d.getCodeIata().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());
        afficherDestinations(filtrees);
        if (lblCount != null) lblCount.setText(filtrees.size() + " résultat(s)");
    }

    // ============================================
    // 🔄 ACTUALISER
    // ============================================
    @FXML
    private void handleActualiser() {
        if (tfRecherche != null) tfRecherche.clear();
        chargerDestinations();
    }

    // ============================================
    // 📝 FORMULAIRE AJOUTER/MODIFIER
    // ============================================
    private Dialog<Destination> creerDialogFormulaire(Destination existante) {
        Dialog<Destination> dialog = new Dialog<>();
        dialog.setTitle(existante == null ? "➕ Ajouter destination" : "✏️ Modifier destination");
        dialog.getDialogPane().setPrefWidth(580);
        dialog.getDialogPane().setPrefHeight(750);

        ButtonType btnSauvegarder = new ButtonType("💾 Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSauvegarder, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #f8f9ff;");

        // HEADER
        VBox header = new VBox(5);
        header.setPadding(new Insets(25, 25, 15, 25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");
        Label titreHeader = new Label(existante == null ? "➕ Nouvelle Destination" : "✏️ Modifier Destination");
        titreHeader.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titreHeader.setTextFill(Color.WHITE);
        Label sousTitreHeader = new Label(existante == null
                ? "Ajoutez une nouvelle destination de voyage"
                : "Modifiez les informations de " + existante.getNom());
        sousTitreHeader.setFont(Font.font("Arial", 13));
        sousTitreHeader.setTextFill(Color.rgb(255, 255, 255, 0.8));
        header.getChildren().addAll(titreHeader, sousTitreHeader);

        String fieldStyle =
                "-fx-background-color: white; -fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-padding: 10 12; -fx-font-size: 14px; -fx-pref-height: 42px;";
        String labelStyle =
                "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-text-fill: #555; -fx-padding: 0 0 3 2;";

        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20, 25, 10, 25));

        // NOM
        VBox nomBox = new VBox(4);
        Label lblNom = new Label("🏙️  Nom de la destination *");
        lblNom.setStyle(labelStyle);
        TextField tfNom = new TextField(existante != null ? existante.getNom() : "");
        tfNom.setStyle(fieldStyle);
        tfNom.setPromptText("Ex: Paris, Rome, Dubai...");
        nomBox.getChildren().addAll(lblNom, tfNom);

        // PAYS + IATA
        HBox paysIataBox = new HBox(15);
        VBox paysBox = new VBox(4);
        Label lblPays = new Label("🌍  Pays");
        lblPays.setStyle(labelStyle);
        TextField tfPays = new TextField(existante != null ? existante.getPays() : "");
        tfPays.setStyle(fieldStyle);
        tfPays.setPromptText("Ex: France, Italie...");
        HBox.setHgrow(paysBox, Priority.ALWAYS);
        paysBox.getChildren().addAll(lblPays, tfPays);

        VBox iataBox = new VBox(4);
        Label lblIata = new Label("✈️  Code IATA");
        lblIata.setStyle(labelStyle);
        TextField tfIata = new TextField(existante != null ? existante.getCodeIata() : "");
        tfIata.setStyle(fieldStyle);
        tfIata.setPromptText("CDG");
        tfIata.setPrefWidth(90);
        tfIata.setMaxWidth(90);
        iataBox.getChildren().addAll(lblIata, tfIata);
        paysIataBox.getChildren().addAll(paysBox, iataBox);

        // ============================================
        // ✅ SECTION IMAGES MULTIPLES
        // ============================================
        List<String> imagesPaths = new ArrayList<>();
        if (existante != null && !existante.getImages().isEmpty()) {
            imagesPaths.addAll(existante.getImages());
        } else if (existante != null && existante.getImageUrl() != null && !existante.getImageUrl().isEmpty()) {
            imagesPaths.add(existante.getImageUrl());
        }

        VBox imagesBox = new VBox(10);
        imagesBox.setPadding(new Insets(12));
        imagesBox.setStyle(
                "-fx-background-color: white; -fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 10; -fx-background-radius: 10;"
        );

        HBox imgHeader = new HBox(10);
        imgHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblImages = new Label("📸  Images (" + imagesPaths.size() + " sélectionnée(s))");
        lblImages.setStyle(labelStyle + "-fx-font-size: 13px;");
        Region imgSpacer = new Region();
        HBox.setHgrow(imgSpacer, Priority.ALWAYS);

        Button btnAjouterImg = new Button("+ Ajouter images");
        btnAjouterImg.setStyle(
                "-fx-background-color: #667eea; -fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-padding: 6 14;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );

        Button btnSupprimerTout = new Button("🗑️ Tout supprimer");
        btnSupprimerTout.setStyle(
                "-fx-background-color: #EF5350; -fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-padding: 6 14;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );

        imgHeader.getChildren().addAll(lblImages, imgSpacer, btnAjouterImg, btnSupprimerTout);

        FlowPane flowApercu = new FlowPane(8, 8);
        flowApercu.setPrefWrapLength(490);
        flowApercu.setMinHeight(80);

        // Afficher aperçus initiaux
        rebuildMiniatures(flowApercu, imagesPaths);
        if (imagesPaths.isEmpty()) {
            Label lblVide = new Label("📷 Aucune image — cliquez sur '+ Ajouter images'");
            lblVide.setStyle("-fx-text-fill: #bbb; -fx-font-size: 12px; -fx-padding: 20;");
            flowApercu.getChildren().add(lblVide);
        }

        // ✅ Ajouter plusieurs images via FileChooser
        btnAjouterImg.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Sélectionner des images");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"
            ));
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            List<File> fichiers = fc.showOpenMultipleDialog(stage);

            if (fichiers != null && !fichiers.isEmpty()) {
                for (File fichier : fichiers) {
                    String chemin = copierImage(fichier);
                    if (chemin != null && !imagesPaths.contains(chemin)) {
                        imagesPaths.add(chemin);
                    }
                }
                // Mettre à jour le compteur
                lblImages.setText("📸  Images (" + imagesPaths.size() + " sélectionnée(s))");
                rebuildMiniatures(flowApercu, imagesPaths);
                System.out.println("📸 Images sélectionnées: " + imagesPaths.size());
            }
        });

        btnSupprimerTout.setOnAction(e -> {
            imagesPaths.clear();
            lblImages.setText("📸  Images (0 sélectionnée)");
            flowApercu.getChildren().clear();
            Label lblVide = new Label("📷 Aucune image — cliquez sur '+ Ajouter images'");
            lblVide.setStyle("-fx-text-fill: #bbb; -fx-font-size: 12px; -fx-padding: 20;");
            flowApercu.getChildren().add(lblVide);
        });

        imagesBox.getChildren().addAll(imgHeader, flowApercu);

        // DESCRIPTION
        VBox descBox = new VBox(4);
        Label lblDesc = new Label("📝  Description");
        lblDesc.setStyle(labelStyle);
        TextArea taDesc = new TextArea(existante != null ? existante.getDescription() : "");
        taDesc.setPrefRowCount(3);
        taDesc.setWrapText(true);
        taDesc.setStyle(
                "-fx-background-color: white; -fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-padding: 10 12; -fx-font-size: 13px;"
        );
        taDesc.setPromptText("Décrivez cette destination...");
        descBox.getChildren().addAll(lblDesc, taDesc);

        formContainer.getChildren().addAll(nomBox, paysIataBox, imagesBox, descBox);

        ScrollPane scroll = new ScrollPane(formContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox mainContainer = new VBox(0);
        mainContainer.getChildren().addAll(header, scroll);
        dialog.getDialogPane().setContent(mainContainer);

        dialog.getDialogPane().lookupButton(btnSauvegarder).setStyle(
                "-fx-background-color: #667eea; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-padding: 10 25;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #f0f0f0; -fx-text-fill: #666;" +
                        "-fx-padding: 10 25; -fx-background-radius: 8; -fx-cursor: hand;"
        );

        // VALIDATION
        dialog.setResultConverter(buttonType -> {
            if (buttonType == btnSauvegarder) {
                String nom = tfNom.getText().trim();
                if (nom.isEmpty()) {
                    afficherAlert(Alert.AlertType.WARNING, "Champ obligatoire", "Le nom est obligatoire !");
                    return null;
                }
                if (nom.length() < 3) {
                    afficherAlert(Alert.AlertType.WARNING, "Nom invalide", "Le nom doit contenir au moins 3 caractères !");
                    return null;
                }

                Destination d = new Destination();
                d.setNom(nom);
                d.setPays(tfPays.getText().trim());
                d.setCodeIata(tfIata.getText().trim().toUpperCase());
                d.setDescription(taDesc.getText().trim());

                // ✅ Image principale = première de la liste
                if (!imagesPaths.isEmpty()) {
                    d.setImageUrl(imagesPaths.get(0));
                    d.setImages(new ArrayList<>(imagesPaths));
                }

                System.out.println("📋 Destination à sauvegarder: " + nom +
                        " avec " + imagesPaths.size() + " image(s)");
                return d;
            }
            return null;
        });

        return dialog;
    }

    // ============================================
    // 🔄 REBUILD MINIATURES
    // ============================================
    private void rebuildMiniatures(FlowPane flow, List<String> paths) {
        flow.getChildren().clear();

        if (paths.isEmpty()) {
            Label lblVide = new Label("📷 Aucune image — cliquez sur '+ Ajouter images'");
            lblVide.setStyle("-fx-text-fill: #bbb; -fx-font-size: 12px; -fx-padding: 20;");
            flow.getChildren().add(lblVide);
            return;
        }

        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);

            StackPane miniature = new StackPane();
            miniature.setPrefSize(110, 75);
            miniature.setStyle("-fx-background-color: #eee; -fx-background-radius: 8;");

            ImageView mini = new ImageView();
            mini.setFitWidth(110);
            mini.setFitHeight(75);
            mini.setPreserveRatio(false);
            try {
                File f = new File("src/main/resources" + path);
                mini.setImage(f.exists()
                        ? new Image(f.toURI().toString())
                        : new Image(path, true));
            } catch (Exception ignored) {}

            Label lblNum = new Label(String.valueOf(i + 1));
            lblNum.setStyle(
                    "-fx-background-color: rgba(102,126,234,0.85); -fx-text-fill: white;" +
                            "-fx-font-size: 10px; -fx-padding: 1 5; -fx-background-radius: 8;"
            );
            StackPane.setAlignment(lblNum, Pos.TOP_LEFT);
            StackPane.setMargin(lblNum, new Insets(3, 0, 0, 3));

            Button btnX = new Button("✕");
            btnX.setStyle(
                    "-fx-background-color: rgba(239,83,80,0.9); -fx-text-fill: white;" +
                            "-fx-font-size: 9px; -fx-padding: 1 4;" +
                            "-fx-background-radius: 8; -fx-cursor: hand;"
            );
            StackPane.setAlignment(btnX, Pos.TOP_RIGHT);
            StackPane.setMargin(btnX, new Insets(3, 3, 0, 0));

            if (i == 0) {
                Label star = new Label("⭐ principale");
                star.setStyle(
                        "-fx-background-color: rgba(102,126,234,0.85); -fx-text-fill: white;" +
                                "-fx-font-size: 9px; -fx-padding: 1 4; -fx-background-radius: 5;"
                );
                StackPane.setAlignment(star, Pos.BOTTOM_LEFT);
                StackPane.setMargin(star, new Insets(0, 0, 3, 3));
                miniature.getChildren().add(star);
            }

            miniature.getChildren().addAll(mini, lblNum, btnX);
            flow.getChildren().add(miniature);

            btnX.setOnAction(ev -> {
                paths.remove(path);
                rebuildMiniatures(flow, paths);
            });
        }
    }

    // ============================================
    // 📂 COPIER IMAGE DANS RESOURCES
    // ============================================
    private String copierImage(File source) {
        try {
            String dossier = "src/main/resources/images/destinations/";
            new File(dossier).mkdirs();
            String nomFichier = System.currentTimeMillis() + "_" + source.getName();
            File dest = new File(dossier + nomFichier);
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Image copiée: /images/destinations/" + nomFichier);
            return "/images/destinations/" + nomFichier;
        } catch (IOException e) {
            System.err.println("❌ Erreur copie image: " + e.getMessage());
            return null;
        }
    }

    // ============================================
    // 📱 QR CODE
    // ============================================
    private void handleQRCode(Destination destination) {
        QRCodeController.afficherPopupQRCode(destination.getId(), destination.getNom());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📱 QR Code - " + destination.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        dialog.getDialogPane().setPrefWidth(400);

        VBox loading = new VBox(15);
        loading.setAlignment(Pos.CENTER);
        loading.setPadding(new Insets(40));
        loading.getChildren().addAll(
                new Label("✈️ " + destination.getNom()) {{ setFont(Font.font("Arial", FontWeight.BOLD, 20)); }},
                new ProgressIndicator() {{ setPrefSize(50, 50); }},
                new Label("🔍 Recherche de la vidéo YouTube...") {{ setStyle("-fx-text-fill: #666;"); }}
        );
        dialog.getDialogPane().setContent(loading);

        new Thread(() -> {
            String videoUrl = youtubeService.getVideoUrl(destination.getId(), destination.getNom());
            Image qrImage = qrCodeService.genererQRCode(videoUrl, 260);

            Platform.runLater(() -> {
                if (qrImage != null) {
                    VBox contenu = new VBox(15);
                    contenu.setAlignment(Pos.CENTER);
                    contenu.setPadding(new Insets(25));

                    Label titre = new Label("✈️ " + destination.getNom());
                    titre.setFont(Font.font("Arial", FontWeight.BOLD, 20));

                    ImageView imgQR = new ImageView(qrImage);
                    imgQR.setFitWidth(260);
                    imgQR.setFitHeight(260);
                    imgQR.setSmooth(false);

                    VBox cadre = new VBox(imgQR);
                    cadre.setAlignment(Pos.CENTER);
                    cadre.setPadding(new Insets(15));
                    cadre.setStyle("-fx-border-color: #667eea; -fx-border-width: 3;" +
                            "-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: white;");

                    Button btnFermer = new Button("✕ Fermer");
                    btnFermer.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;" +
                            "-fx-padding: 10 30; -fx-background-radius: 25; -fx-cursor: hand;");
                    btnFermer.setOnAction(ev ->
                            ((Stage) dialog.getDialogPane().getScene().getWindow()).close());

                    contenu.getChildren().addAll(titre, cadre, btnFermer);
                    dialog.getDialogPane().setContent(contenu);
                    chargerDestinations();
                }
            });
        }).start();

        dialog.showAndWait();
    }

    // ============================================
    // 💬 ALERT
    // ============================================
    private void afficherAlert(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}