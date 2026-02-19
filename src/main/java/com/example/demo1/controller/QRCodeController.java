package com.example.demo1.controller;

import com.example.demo1.services.QRCodeService;
import com.example.demo1.services.YouTubeService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class QRCodeController {

    // ============================================
    // 📱 AFFICHER LA POPUP QR CODE (MÉTHODE STATIQUE)
    // ============================================
    public static void afficherPopupQRCode(int destinationId, String nomDestination) {
        System.out.println("📱 Affichage popup QR Code: " + nomDestination);
        System.out.println("   Destination ID: " + destinationId);

        Dialog<Void> dialog = creerDialog(nomDestination);
        afficherChargement(dialog, nomDestination);

        // Générer QR Code dans un thread séparé
        new Thread(() -> {
            YouTubeService youtubeService = new YouTubeService();
            QRCodeService qrCodeService = new QRCodeService();

            // Obtenir l'URL YouTube
            String videoUrl = youtubeService.getVideoUrl(destinationId, nomDestination);

            // Générer le QR Code
            Image qrImage = qrCodeService.genererQRCode(videoUrl, 280);

            // Mettre à jour l'interface
            Platform.runLater(() -> {
                if (qrImage != null && videoUrl != null) {
                    afficherQRCode(dialog, nomDestination, qrImage, videoUrl);
                } else {
                    afficherErreur(dialog);
                }
            });
        }).start();

        dialog.showAndWait();
    }

    // ============================================
    // 🏗️ CRÉER LE DIALOG
    // ============================================
    private static Dialog<Void> creerDialog(String nomDestination) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📱 QR Code - " + nomDestination);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        dialog.getDialogPane().setPrefWidth(420);   // ✅ augmenté
        dialog.getDialogPane().setPrefHeight(650);
        return dialog;// ✅ AJOUTÉ        return dialog;
    }

    // ============================================
    // ⏳ AFFICHER LE CHARGEMENT
    // ============================================
    private static void afficherChargement(Dialog<Void> dialog, String nomDestination) {
        VBox chargement = new VBox(20);
        chargement.setAlignment(Pos.CENTER);
        chargement.setPadding(new Insets(40));

        Label titre = new Label("✈️ " + nomDestination);
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.valueOf("#333333"));

        ProgressIndicator loader = new ProgressIndicator();
        loader.setPrefSize(60, 60);

        Label texte = new Label("🔍 Recherche de la vidéo YouTube...");
        texte.setFont(Font.font("Arial", 14));
        texte.setTextFill(Color.valueOf("#666666"));

        chargement.getChildren().addAll(titre, loader, texte);
        dialog.getDialogPane().setContent(chargement);
    }

    // ============================================
    // ✅ AFFICHER LE QR CODE
    // ============================================
    private static void afficherQRCode(Dialog<Void> dialog, String nomDestination,
                                       Image qrImage, String videoUrl) {
        VBox contenu = new VBox(15);
        contenu.setAlignment(Pos.CENTER);
        contenu.setPadding(new Insets(25));

        // ---- TITRE ----
        Label titre = new Label("✈️ " + nomDestination);
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titre.setTextFill(Color.valueOf("#333333"));

        // ---- SOUS TITRE ----
        Label sousTitre = new Label("Scannez pour voir la vidéo de " + nomDestination);
        sousTitre.setFont(Font.font("Arial", 13));
        sousTitre.setTextFill(Color.valueOf("#666666"));
        sousTitre.setWrapText(true);
        sousTitre.setMaxWidth(350);
        sousTitre.setTextAlignment(TextAlignment.CENTER);

        // ---- QR CODE ----
        ImageView imageView = new ImageView(qrImage);
        imageView.setFitWidth(280);
        imageView.setFitHeight(280);
        imageView.setSmooth(false);

        // Cadre autour du QR Code
        VBox cadreQR = new VBox(imageView);
        cadreQR.setAlignment(Pos.CENTER);
        cadreQR.setPadding(new Insets(15));
        cadreQR.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #667eea;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 15, 0, 0, 5);"
        );

        // ---- URL INFO ----
        Label urlLabel = new Label("🔗 " + videoUrl);
        urlLabel.setFont(Font.font("Arial", 11));
        urlLabel.setTextFill(Color.valueOf("#999999"));
        urlLabel.setWrapText(true);
        urlLabel.setMaxWidth(350);
        urlLabel.setTextAlignment(TextAlignment.CENTER);

        // ---- INSTRUCTIONS ----
        VBox instructions = new VBox(8);
        instructions.setAlignment(Pos.CENTER_LEFT);
        instructions.setStyle(
                "-fx-background-color: #f0f4ff;" +
                        "-fx-padding: 15;" +
                        "-fx-background-radius: 10;"
        );

        Label step1 = new Label("1️⃣  Ouvrez l'appareil photo de votre téléphone");
        Label step2 = new Label("2️⃣  Pointez vers le QR Code");
        Label step3 = new Label("3️⃣  La vidéo YouTube s'ouvre automatiquement !");

        for (Label step : new Label[]{step1, step2, step3}) {
            step.setFont(Font.font("Arial", 13));
            step.setTextFill(Color.valueOf("#555555"));
        }

        instructions.getChildren().addAll(step1, step2, step3);

        // ---- BOUTON FERMER ----
        Button btnFermer = new Button("✕ Fermer");
        btnFermer.setStyle(
                "-fx-background-color: #667eea;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 30;" +
                        "-fx-background-radius: 25;" +
                        "-fx-cursor: hand;"
        );

        btnFermer.setOnAction(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.close();
        });

        // ---- ASSEMBLER LE CONTENU ----
        contenu.getChildren().addAll(
                titre,
                sousTitre,
                cadreQR,
                urlLabel,
                instructions,
                btnFermer
        );

        dialog.getDialogPane().setContent(contenu);
    }

    // ============================================
    // ❌ AFFICHER UNE ERREUR
    // ============================================
    private static void afficherErreur(Dialog<Void> dialog) {
        VBox erreur = new VBox(20);
        erreur.setAlignment(Pos.CENTER);
        erreur.setPadding(new Insets(40));

        Label message = new Label("❌ Impossible de générer le QR Code");
        message.setFont(Font.font("Arial", 16));
        message.setTextFill(Color.RED);

        Label conseil = new Label(
                "Vérifiez :\n" +
                        "• Votre connexion internet\n" +
                        "• Votre clé API YouTube\n" +
                        "• Que la destination existe en BDD"
        );
        conseil.setFont(Font.font("Arial", 12));
        conseil.setTextFill(Color.valueOf("#666666"));
        conseil.setTextAlignment(TextAlignment.CENTER);

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle(
                "-fx-background-color: #EF5350;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10 30;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );
        btnFermer.setOnAction(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.close();
        });

        erreur.getChildren().addAll(message, conseil, btnFermer);
        dialog.getDialogPane().setContent(erreur);
    }
}