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
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.stage.FileChooser;
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

    // ===== COLONNES DANS LA GRILLE =====
    private static final int COLONNES = 3;

    // ============================================
    // 🚀 INITIALISATION
    // ============================================
    @FXML
    public void initialize() {
        System.out.println("🌍 Initialisation DestinationController");

        destinationService = new DestinationService();
        youtubeService = new YouTubeService();
        qrCodeService = new QRCodeService();

        chargerDestinations();
    }

    // ============================================
    // 📋 CHARGER ET AFFICHER LES DESTINATIONS
    // ============================================
    private void chargerDestinations() {
        if (hboxLoading != null) {
            hboxLoading.setVisible(true);
        }
        gridDestinations.getChildren().clear();

        new Thread(() -> {
            toutesDestinations = destinationService.getAll();

            Platform.runLater(() -> {
                afficherDestinations(toutesDestinations);
                if (lblCount != null) {
                    lblCount.setText(toutesDestinations.size() + " destinations");
                }
                if (hboxLoading != null) {
                    hboxLoading.setVisible(false);
                }
            });
        }).start();
    }

    // ============================================
    // 🗺️ AFFICHER LES CARTES DANS LA GRILLE
    // ============================================
    private void afficherDestinations(List<Destination> destinations) {
        gridDestinations.getChildren().clear();

        if (destinations.isEmpty()) {
            Label aucun = new Label("😕 Aucune destination trouvée");
            aucun.setStyle("-fx-font-size: 18px; -fx-text-fill: #999; -fx-padding: 50;");
            gridDestinations.add(aucun, 0, 0);
            return;
        }

        int col = 0;
        int row = 0;

        for (Destination destination : destinations) {
            VBox carte = creerCarte(destination);
            gridDestinations.add(carte, col, row);

            col++;
            if (col >= COLONNES) {
                col = 0;
                row++;
            }
        }
    }

    // ============================================
    // 🃏 CRÉER UNE CARTE DESTINATION
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

        // ---- IMAGE ----
        ImageView imgView = new ImageView();
        imgView.setFitWidth(280);
        imgView.setFitHeight(160);
        imgView.setPreserveRatio(false);

        if (destination.getImageUrl() != null && !destination.getImageUrl().isEmpty()) {
            try {
                String imageUrl = destination.getImageUrl();
                Image img;

                // Si c'est un chemin local (commence par /images/)
                if (imageUrl.startsWith("/images/")) {
                    // Chercher dans les resources
                    var resource = getClass().getResource(imageUrl);
                    if (resource != null) {
                        img = new Image(resource.toExternalForm(), true);
                    } else {
                        // Chercher dans src/main/resources
                        File fichier = new File("src/main/resources" + imageUrl);
                        if (fichier.exists()) {
                            img = new Image(fichier.toURI().toString(), true);
                        } else {
                            img = null;
                        }
                    }
                } else {
                    // URL externe (http://)
                    img = new Image(imageUrl, true);
                }

                if (img != null) {
                    imgView.setImage(img);
                }

            } catch (Exception e) {
                System.err.println("❌ Image non chargée: " + destination.getImageUrl());
            }
        }

        StackPane imageContainer = new StackPane(imgView);
        imageContainer.setStyle("-fx-background-color: #667eea; -fx-background-radius: 15 15 0 0;");

        // Badge code IATA
        if (destination.getCodeIata() != null && !destination.getCodeIata().isEmpty()) {
            Label badge = new Label(destination.getCodeIata());
            badge.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.6);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 4 8;" +
                            "-fx-background-radius: 5;" +
                            "-fx-font-size: 11px;"
            );
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            StackPane.setMargin(badge, new Insets(10));
            imageContainer.getChildren().add(badge);
        }

        // ---- CONTENU ----
        VBox contenu = new VBox(8);
        contenu.setPadding(new Insets(15));

        // Nom
        Label lblNom = new Label(destination.getNom());
        lblNom.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        lblNom.setTextFill(Color.valueOf("#333333"));

        // Pays
        Label lblPays = new Label("🌍 " + (destination.getPays() != null ?
                destination.getPays() : "Pays inconnu"));
        lblPays.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        // Description
        Label lblDesc = new Label(destination.getDescription() != null ?
                destination.getDescription() : "Aucune description");
        lblDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxHeight(40);

        // Indicateur vidéo
        Label lblVideo = new Label(
                destination.getVideoUrl() != null && !destination.getVideoUrl().isEmpty()
                        ? "🎬 Vidéo disponible"
                        : "⏳ Vidéo non chargée"
        );
        lblVideo.setStyle(
                destination.getVideoUrl() != null && !destination.getVideoUrl().isEmpty()
                        ? "-fx-font-size: 11px; -fx-text-fill: green;"
                        : "-fx-font-size: 11px; -fx-text-fill: orange;"
        );

        contenu.getChildren().addAll(lblNom, lblPays, lblDesc, lblVideo);

        // ---- BOUTONS ----
        HBox boutons = new HBox(8);
        boutons.setPadding(new Insets(10, 15, 15, 15));
        boutons.setAlignment(Pos.CENTER);

        // Bouton QR Code
        Button btnQR = new Button("📱 QR Code");
        btnQR.setStyle(
                "-fx-background-color: #667eea;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 7 12;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;"
        );
        btnQR.setOnAction(e -> handleQRCode(destination));

        // Bouton Modifier
        Button btnModifier = new Button("✏️");
        btnModifier.setStyle(
                "-fx-background-color: #FFA726;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 7 10;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;"
        );
        btnModifier.setOnAction(e -> handleModifier(destination));

        // Bouton Supprimer
        Button btnSupprimer = new Button("🗑️");
        btnSupprimer.setStyle(
                "-fx-background-color: #EF5350;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 7 10;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;"
        );
        btnSupprimer.setOnAction(e -> handleSupprimer(destination));

        Region espaceur = new Region();
        HBox.setHgrow(espaceur, Priority.ALWAYS);

        boutons.getChildren().addAll(btnQR, espaceur, btnModifier, btnSupprimer);

        carte.getChildren().addAll(imageContainer, contenu, boutons);
        return carte;
    }

    // ============================================
    // 📱 AFFICHER QR CODE
    // ============================================
    private void handleQRCode(Destination destination) {
        System.out.println("📱 QR Code pour: " + destination.getNom());

        QRCodeController.afficherPopupQRCode(
                destination.getId(),      // int
                destination.getNom()      // String
        );

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📱 QR Code - " + destination.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        dialog.getDialogPane().setPrefWidth(400);

        // Afficher loading
        VBox loading = new VBox(15);
        loading.setAlignment(Pos.CENTER);
        loading.setPadding(new Insets(40));
        loading.getChildren().addAll(
                new Label("✈️ " + destination.getNom()) {{
                    setFont(Font.font("Arial", FontWeight.BOLD, 20));
                }},
                new ProgressIndicator() {{ setPrefSize(50, 50); }},
                new Label("🔍 Recherche de la vidéo YouTube...") {{
                    setStyle("-fx-text-fill: #666;");
                }}
        );
        dialog.getDialogPane().setContent(loading);

        // Générer QR Code dans un thread
        new Thread(() -> {
            String videoUrl = youtubeService.getVideoUrl(
                    destination.getId(),
                    destination.getNom()
            );

            Image qrImage = qrCodeService.genererQRCode(videoUrl, 260);

            Platform.runLater(() -> {
                if (qrImage != null) {
                    // Contenu QR Code
                    VBox contenu = new VBox(15);
                    contenu.setAlignment(Pos.CENTER);
                    contenu.setPadding(new Insets(25));

                    Label titre = new Label("✈️ " + destination.getNom());
                    titre.setFont(Font.font("Arial", FontWeight.BOLD, 20));

                    Label sousTitre = new Label("Scannez pour voir la vidéo !");
                    sousTitre.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

                    // Image QR
                    ImageView imgQR = new ImageView(qrImage);
                    imgQR.setFitWidth(260);
                    imgQR.setFitHeight(260);
                    imgQR.setSmooth(false);

                    VBox cadre = new VBox(imgQR);
                    cadre.setAlignment(Pos.CENTER);
                    cadre.setPadding(new Insets(15));
                    cadre.setStyle(
                            "-fx-border-color: #667eea;" +
                                    "-fx-border-width: 3;" +
                                    "-fx-border-radius: 10;" +
                                    "-fx-background-radius: 10;" +
                                    "-fx-background-color: white;"
                    );

                    // Instructions
                    VBox instructions = new VBox(6);
                    instructions.setAlignment(Pos.CENTER_LEFT);
                    instructions.setPadding(new Insets(12));
                    instructions.setStyle(
                            "-fx-background-color: #f0f4ff;" +
                                    "-fx-background-radius: 10;"
                    );
                    for (String step : new String[]{
                            "1️⃣  Ouvrez l'appareil photo",
                            "2️⃣  Pointez vers le QR Code",
                            "3️⃣  La vidéo YouTube s'ouvre !"
                    }) {
                        Label l = new Label(step);
                        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
                        instructions.getChildren().add(l);
                    }

                    // Bouton fermer
                    Button btnFermer = new Button("✕ Fermer");
                    btnFermer.setStyle(
                            "-fx-background-color: #667eea;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-padding: 10 30;" +
                                    "-fx-background-radius: 25;" +
                                    "-fx-cursor: hand;"
                    );
                    btnFermer.setOnAction(ev -> {
                        Stage stage = (Stage) dialog.getDialogPane()
                                .getScene().getWindow();
                        stage.close();
                    });

                    contenu.getChildren().addAll(
                            titre, sousTitre, cadre, instructions, btnFermer
                    );
                    dialog.getDialogPane().setContent(contenu);

                    // Mettre à jour l'indicateur vidéo sur la carte
                    chargerDestinations();
                }
            });
        }).start();

        dialog.showAndWait();
    }

    // ============================================
    // ➕ AJOUTER UNE DESTINATION
    // ============================================
    @FXML
    private void handleAjouter() {
        Dialog<Destination> dialog = creerDialogFormulaire(null);
        Optional<Destination> result = dialog.showAndWait();

        result.ifPresent(destination -> {
            boolean success = destinationService.save(destination);
            if (success) {
                afficherAlert(Alert.AlertType.INFORMATION,
                        "Succès", "Destination ajoutée avec succès !");
                chargerDestinations();
            } else {
                afficherAlert(Alert.AlertType.ERROR,
                        "Erreur", "Impossible d'ajouter la destination");
            }
        });
    }

    // ============================================
    // ✏️ MODIFIER UNE DESTINATION
    // ============================================
    private void handleModifier(Destination destination) {
        Dialog<Destination> dialog = creerDialogFormulaire(destination);
        Optional<Destination> result = dialog.showAndWait();

        result.ifPresent(dest -> {
            dest.setId(destination.getId());
            boolean success = destinationService.update(dest);
            if (success) {
                afficherAlert(Alert.AlertType.INFORMATION,
                        "Succès", "Destination modifiée avec succès !");
                chargerDestinations();
            } else {
                afficherAlert(Alert.AlertType.ERROR,
                        "Erreur", "Impossible de modifier la destination");
            }
        });
    }

    // ============================================
    // 🗑️ SUPPRIMER UNE DESTINATION
    // ============================================
    private void handleSupprimer(Destination destination) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + destination.getNom() + " ?");
        confirm.setContentText(
                "Cette action est irréversible.\n" +
                        "Tous les voyages liés seront aussi supprimés !"
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = destinationService.delete(destination.getId());
            if (success) {
                afficherAlert(Alert.AlertType.INFORMATION,
                        "Succès", "Destination supprimée !");
                chargerDestinations();
            } else {
                afficherAlert(Alert.AlertType.ERROR,
                        "Erreur", "Impossible de supprimer");
            }
        }
    }

    // ============================================
    // 🔍 RECHERCHE EN TEMPS RÉEL
    // ============================================
    @FXML
    private void handleRecherche() {
        if (tfRecherche == null || toutesDestinations == null) return;

        String keyword = tfRecherche.getText().toLowerCase().trim();

        if (keyword.isEmpty()) {
            afficherDestinations(toutesDestinations);
            if (lblCount != null) {
                lblCount.setText(toutesDestinations.size() + " destinations");
            }
            return;
        }

        List<Destination> filtrees = toutesDestinations.stream()
                .filter(d ->
                        d.getNom().toLowerCase().contains(keyword) ||
                                (d.getPays() != null && d.getPays().toLowerCase().contains(keyword)) ||
                                (d.getCodeIata() != null && d.getCodeIata().toLowerCase().contains(keyword))
                )
                .collect(Collectors.toList());

        afficherDestinations(filtrees);
        if (lblCount != null) {
            lblCount.setText(filtrees.size() + " résultat(s)");
        }
    }

    // ============================================
    // 🔄 ACTUALISER
    // ============================================
    @FXML
    private void handleActualiser() {
        if (tfRecherche != null) {
            tfRecherche.clear();
        }
        chargerDestinations();
    }

    // ============================================
    // 📝 FORMULAIRE AJOUTER/MODIFIER
    // ============================================
    private Dialog<Destination> creerDialogFormulaire(Destination existante) {
        Dialog<Destination> dialog = new Dialog<>();
        dialog.setTitle(existante == null ? "➕ Ajouter destination" : "✏️ Modifier destination");
        dialog.getDialogPane().setPrefWidth(560);
        dialog.getDialogPane().setPrefHeight(700);

        // ✅ Bouton personnalisé
        ButtonType btnSauvegarder = new ButtonType(
                "💾 Sauvegarder", ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(btnSauvegarder, ButtonType.CANCEL);

        // ✅ Style du DialogPane
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #f8f9ff;" +
                        "-fx-border-color: transparent;"
        );

        // ============================================
        // 🎨 HEADER
        // ============================================
        VBox header = new VBox(5);
        header.setPadding(new Insets(25, 25, 15, 25));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                        "-fx-background-radius: 0;"
        );

        Label titreHeader = new Label(existante == null ? "➕ Nouvelle Destination" : "✏️ Modifier Destination");
        titreHeader.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titreHeader.setTextFill(Color.WHITE);

        Label sousTitreHeader = new Label(existante == null
                ? "Ajoutez une nouvelle destination de voyage"
                : "Modifiez les informations de " + existante.getNom());
        sousTitreHeader.setFont(Font.font("Arial", 13));
        sousTitreHeader.setTextFill(Color.rgb(255, 255, 255, 0.8));

        header.getChildren().addAll(titreHeader, sousTitreHeader);

        // ============================================
        // 📝 FORMULAIRE
        // ============================================
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20, 25, 10, 25));

        // Style commun des champs
        String fieldStyle =
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 12;" +
                        "-fx-font-size: 14px;" +
                        "-fx-pref-height: 42px;";

        String labelStyle =
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #555;" +
                        "-fx-padding: 0 0 3 2;";

        // ---- NOM ----
        VBox nomBox = new VBox(4);
        Label lblNom = new Label("🏙️  Nom de la destination *");
        lblNom.setStyle(labelStyle);
        TextField tfNom = new TextField(existante != null ? existante.getNom() : "");
        tfNom.setStyle(fieldStyle);
        tfNom.setPromptText("Ex: Paris, Rome, Dubai...");
        nomBox.getChildren().addAll(lblNom, tfNom);

        // ---- PAYS + IATA sur la même ligne ----
        HBox paysIataBox = new HBox(15);

        VBox paysBox = new VBox(4);
        Label lblPays = new Label("🌍  Pays");
        lblPays.setStyle(labelStyle);
        TextField tfPays = new TextField(existante != null ? existante.getPays() : "");
        tfPays.setStyle(fieldStyle);
        tfPays.setPromptText("Ex: France, Italie...");
        HBox.setHgrow(tfPays, Priority.ALWAYS);
        paysBox.getChildren().addAll(lblPays, tfPays);
        HBox.setHgrow(paysBox, Priority.ALWAYS);

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

        // ---- IMAGE ----
        VBox imageBox = new VBox(4);
        Label lblImage = new Label("🖼️  Image de la destination");
        lblImage.setStyle(labelStyle);

        TextField tfImage = new TextField(existante != null ? existante.getImageUrl() : "");
        tfImage.setEditable(false);
        tfImage.setStyle(fieldStyle + "-fx-text-fill: #888;");
        tfImage.setPromptText("Cliquez sur le bouton pour choisir...");
        HBox.setHgrow(tfImage, Priority.ALWAYS);

        Button btnChoisirImage = new Button("📁 Parcourir");
        btnChoisirImage.setStyle(
                "-fx-background-color: #667eea;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 10 18;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
        );

        HBox imageFieldBox = new HBox(10, tfImage, btnChoisirImage);
        imageFieldBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tfImage, Priority.ALWAYS);

        // ---- PREVIEW IMAGE ----
        ImageView preview = new ImageView();
        preview.setFitWidth(480);
        preview.setFitHeight(150);
        preview.setPreserveRatio(true);

        // Charger image existante
        if (existante != null && existante.getImageUrl() != null
                && !existante.getImageUrl().isEmpty()) {
            try {
                preview.setImage(new Image(existante.getImageUrl(), true));
            } catch (Exception ignored) {}
        }

        StackPane previewContainer = new StackPane(preview);
        previewContainer.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-border-color: #ddd;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-min-height: 120px;"
        );
        previewContainer.setPrefHeight(150);

        Label previewLabel = new Label("📷 Aperçu de l'image");
        previewLabel.setStyle("-fx-text-fill: #bbb; -fx-font-size: 13px;");
        previewContainer.getChildren().add(previewLabel);
        StackPane.setAlignment(previewLabel, Pos.CENTER);

        // Action bouton parcourir
        btnChoisirImage.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"
                    )
            );

            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            File fichier = fileChooser.showOpenDialog(stage);

            if (fichier != null) {
                try {
                    String dossierImages = "src/main/resources/images/destinations/";
                    new File(dossierImages).mkdirs();

                    String nomFichier = System.currentTimeMillis() + "_" + fichier.getName();
                    File dest = new File(dossierImages + nomFichier);
                    Files.copy(fichier.toPath(), dest.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);

                    String cheminImage = "/images/destinations/" + nomFichier;
                    tfImage.setText(cheminImage);

                    Image img = new Image(fichier.toURI().toString());
                    preview.setImage(img);
                    previewLabel.setVisible(false);

                    System.out.println("✅ Image copiée: " + cheminImage);

                } catch (Exception ex) {
                    afficherAlert(Alert.AlertType.ERROR,
                            "Erreur", "Impossible de copier l'image:\n" + ex.getMessage());
                }
            }
        });

        imageBox.getChildren().addAll(lblImage, imageFieldBox, previewContainer);

        // ---- DESCRIPTION ----
        VBox descBox = new VBox(4);
        Label lblDesc = new Label("📝  Description");
        lblDesc.setStyle(labelStyle);
        TextArea taDesc = new TextArea(existante != null ? existante.getDescription() : "");
        taDesc.setPrefRowCount(3);
        taDesc.setWrapText(true);
        taDesc.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 12;" +
                        "-fx-font-size: 13px;"
        );
        taDesc.setPromptText("Décrivez cette destination...");
        descBox.getChildren().addAll(lblDesc, taDesc);

        formContainer.getChildren().addAll(nomBox, paysIataBox, imageBox, descBox);

        // ============================================
        // 🏗️ ASSEMBLAGE FINAL
        // ============================================
        VBox mainContainer = new VBox(0);
        mainContainer.getChildren().addAll(header, formContainer);

        dialog.getDialogPane().setContent(mainContainer);

        // ✅ Style des boutons du dialog
        dialog.getDialogPane().lookupButton(btnSauvegarder).setStyle(
                "-fx-background-color: #667eea;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 25;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-text-fill: #666;" +
                        "-fx-padding: 10 25;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        // ============================================
        // ✅ VALIDATION
        // ============================================
        dialog.setResultConverter(buttonType -> {
            if (buttonType == btnSauvegarder) {
                String nom = tfNom.getText().trim();

                if (nom.isEmpty()) {
                    afficherAlert(Alert.AlertType.WARNING,
                            "Champ obligatoire", "Le nom est obligatoire !");
                    return null;
                }
                if (nom.length() < 3) {
                    afficherAlert(Alert.AlertType.WARNING,
                            "Nom invalide", "Le nom doit contenir au moins 3 caractères !");
                    return null;
                }
                if (nom.matches("^(.)\\1+$")) {
                    afficherAlert(Alert.AlertType.WARNING,
                            "Nom invalide",
                            "❌ Ce nom n'est pas valide !\nExemple correct: Paris, Rome, Dubai");
                    return null;
                }
                if (!nom.toLowerCase().matches(".*[aeiouy].*")) {
                    afficherAlert(Alert.AlertType.WARNING,
                            "Nom invalide",
                            "❌ Ce nom n'est pas valide !\nLe nom doit contenir des voyelles.");
                    return null;
                }

                Destination d = new Destination();
                d.setNom(nom);
                d.setPays(tfPays.getText().trim());
                d.setCodeIata(tfIata.getText().trim().toUpperCase());
                d.setImageUrl(tfImage.getText().trim());
                d.setDescription(taDesc.getText().trim());
                return d;
            }
            return null;
        });

        return dialog;
    }

    // ============================================
    // 💬 AFFICHER UNE ALERTE
    // ============================================
    private void afficherAlert(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}