package com.example.demo1.controller;

import com.example.demo1.entity.Voyage;
import com.example.demo1.services.VoyageServices;
import javafx.application.Platform;
import com.example.demo1.HelloApplication;
import com.example.demo1.services.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HomeController {

    @FXML private StackPane contentArea;
    @FXML private ScrollPane accueilScrollPane;   // ← le scrollpane de l'accueil dans home.fxml
    @FXML private Button btnAccueil;
    @FXML private Button btnVoyages;
    @FXML private Button btnOffres;
    @FXML private Button btnOmra;
    @FXML private HBox hboxDestinations;

    private final VoyageServices voyageServices = new VoyageServices();

    @FXML
    public void initialize() {
        chargerTop3Voyages();
    }

    // ============================================
    // 🌍 CHARGER LES 3 PREMIERS VOYAGES
    // ============================================
    private void chargerTop3Voyages() {
        if (hboxDestinations == null) return;

        new Thread(() -> {
            List<Voyage> voyages = voyageServices.getAllVoyages();
            List<Voyage> top3 = voyages.stream().limit(3).toList();

            Platform.runLater(() -> {
                hboxDestinations.getChildren().clear();
                for (int i = 0; i < top3.size(); i++) {
                    VBox carte = creerCarteVoyage(top3.get(i), i);
                    HBox.setHgrow(carte, Priority.ALWAYS);
                    carte.setMaxWidth(Double.MAX_VALUE);
                    hboxDestinations.getChildren().add(carte);
                }
            });
        }).start();
    }

    @FXML
    public void ouvrirWhatsApp() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/whatsapp.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("WhatsApp Business - SmartTrip");
        stage.setScene(new Scene(root, 520, 700));
        stage.show();
    }
    @FXML
    public void ouvrirConvertisseur() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/currencyConverter.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Convertisseur de Devises");
            stage.setScene(new Scene(root, 480, 620));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
    // ============================================
    // 🃏 CRÉER UNE CARTE VOYAGE
    // ============================================
    private VBox creerCarteVoyage(Voyage voyage, int index) {
        boolean estAccentuee = (index == 2);

        // ---- CARTE ----
        VBox carte = new VBox(0);
        carte.setMaxWidth(Double.MAX_VALUE);
        carte.setStyle(estAccentuee
                ? "-fx-background-color: #083D77; -fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(8,61,119,0.35), 20, 0, 0, 6);"
                : "-fx-background-color: white; -fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(8,61,119,0.1), 14, 0, 0, 4);");

        // ---- ZONE IMAGE (186px fixe) ----
        StackPane imageZone = new StackPane();
        imageZone.setPrefHeight(186);
        imageZone.setMinHeight(186);
        imageZone.setMaxHeight(186);

        // Fond dégradé toujours présent
        String gradient = estAccentuee
                ? "linear-gradient(to bottom, rgba(10,108,241,0.25), rgba(8,61,119,0.95))," +
                "linear-gradient(135deg, #0A6CF1, #083D77)"
                : "linear-gradient(to bottom, rgba(8,61,119,0.1), rgba(8,61,119,0.88))," +
                "linear-gradient(135deg, #1a4a8a, #0d2d5e)";

        VBox fondColore = new VBox();
        fondColore.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        fondColore.setStyle("-fx-background-color: " + gradient + ";" +
                "-fx-background-radius: 14 14 0 0;");
        imageZone.getChildren().add(fondColore);

        // ✅ Image depuis imagePath du Voyage
        String imageUrl = voyage.getImagePath();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                ImageView imgView = new ImageView();
                imgView.setFitWidth(320);
                imgView.setFitHeight(186);
                imgView.setPreserveRatio(false);

                Image img;
                if (imageUrl.startsWith("/images/") || imageUrl.startsWith("/")) {
                    File f = new File("src/main/resources" + imageUrl);
                    img = f.exists()
                            ? new Image(f.toURI().toString())
                            : new Image(imageUrl, true);
                } else {
                    img = new Image(imageUrl, true);
                }
                imgView.setImage(img);

                Rectangle clip = new Rectangle(320, 186);
                clip.setArcWidth(28);
                clip.setArcHeight(28);
                imgView.setClip(clip);
                imageZone.getChildren().add(imgView);

                VBox overlay = new VBox();
                overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                overlay.setStyle("-fx-background-color: " +
                        "linear-gradient(to bottom, rgba(0,0,0,0.0) 30%, rgba(0,0,0,0.72) 100%);" +
                        "-fx-background-radius: 14 14 0 0;");
                imageZone.getChildren().add(overlay);

            } catch (Exception ignored) {}
        }

        // Numéro éditorial
        Label lblNum = new Label(String.format("%02d", index + 1));
        lblNum.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 52px;" +
                "-fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.15);" +
                "-fx-padding: 6 0 0 12;");
        StackPane.setAlignment(lblNum, Pos.TOP_LEFT);
        imageZone.getChildren().add(lblNum);

        // Badge N1 POPULAIRE sur la 3ème carte
        if (estAccentuee) {
            Label badge = new Label("N1 POPULAIRE");
            badge.setStyle("-fx-background-color: white; -fx-text-fill: #0A6CF1;" +
                    "-fx-font-size: 8px; -fx-font-weight: bold; -fx-letter-spacing: 1px;" +
                    "-fx-padding: 5 10; -fx-background-radius: 0 14 0 8;");
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            imageZone.getChildren().add(badge);
        }

        // ✅ Nom + pays EN BAS de l'image
        VBox nomPaysBox = new VBox(2);
        nomPaysBox.setAlignment(Pos.BOTTOM_LEFT);
        nomPaysBox.setPadding(new Insets(0, 12, 12, 14));
        StackPane.setAlignment(nomPaysBox, Pos.BOTTOM_LEFT);

        String nomText = (voyage.getDestinationObj() != null && voyage.getDestinationObj().getNom() != null)
                ? voyage.getDestinationObj().getNom()
                : (voyage.getDestination() != null ? voyage.getDestination() : "Voyage");

        Label lblNom = new Label(nomText);
        lblNom.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 22px;" +
                "-fx-font-weight: bold; -fx-text-fill: white;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 4, 0, 0, 2);");
        lblNom.setMaxWidth(260);

        String paysText = "";
        if (voyage.getDestinationObj() != null && voyage.getDestinationObj().getPays() != null)
            paysText = voyage.getDestinationObj().getPays().toUpperCase();
        else if (voyage.getPaysDepart() != null)
            paysText = voyage.getPaysDepart().toUpperCase();

        Label lblPays = new Label(paysText);
        lblPays.setStyle("-fx-font-size: 9px; -fx-letter-spacing: 3px;" +
                "-fx-text-fill: rgba(255,255,255,0.75);" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 3, 0, 0, 1);");

        nomPaysBox.getChildren().addAll(lblNom, lblPays);
        imageZone.getChildren().add(nomPaysBox);

        // ---- CONTENU TEXTE ----
        VBox contenu = new VBox(10);
        contenu.setPadding(new Insets(16, 16, 18, 16));
        VBox.setVgrow(contenu, Priority.ALWAYS);

        // ✅ Dates du voyage
        String datesText = "";
        if (voyage.getDateDebut() != null && voyage.getDateFin() != null)
            datesText = voyage.getDateDebut() + "  →  " + voyage.getDateFin();
        Label lblDates = new Label(datesText);
        lblDates.setStyle(estAccentuee
                ? "-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.5);"
                : "-fx-font-size: 11px; -fx-text-fill: #aab8cc;");

        // Description
        String descText = voyage.getDescription();
        if (descText != null && descText.length() > 80)
            descText = descText.substring(0, 77) + "...";
        Label lblDesc = new Label(descText != null ? descText : "");
        lblDesc.setStyle(estAccentuee
                ? "-fx-font-family: 'Georgia'; -fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.6);"
                : "-fx-font-family: 'Georgia'; -fx-font-size: 12px; -fx-text-fill: #6b7f99;");
        lblDesc.setWrapText(true);

        // ---- PRIX + BOUTON ----
        HBox prixBox = new HBox(0);
        prixBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(prixBox, Priority.NEVER);

        VBox prixVBox = new VBox(1);
        Label lblAPartirDe = new Label("A partir de");
        lblAPartirDe.setStyle(estAccentuee
                ? "-fx-font-size: 9px; -fx-text-fill: rgba(255,255,255,0.4);"
                : "-fx-font-size: 9px; -fx-text-fill: #aab8cc;");

        String prixAffiche = voyage.getPrix() > 0
                ? String.format("%.0f TND", voyage.getPrix())
                : "Sur demande";

        Label lblPrix = new Label(prixAffiche);
        lblPrix.setStyle(estAccentuee
                ? "-fx-font-family: 'Georgia'; -fx-font-size: 20px;" +
                "-fx-font-weight: bold; -fx-text-fill: white;"
                : "-fx-font-family: 'Georgia'; -fx-font-size: 20px;" +
                "-fx-font-weight: bold; -fx-text-fill: #0A6CF1;");
        prixVBox.getChildren().addAll(lblAPartirDe, lblPrix);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnReserver = new Button("Réserver");
        btnReserver.setStyle(estAccentuee
                ? "-fx-background-color: #0A6CF1; -fx-text-fill: white;" +
                "-fx-font-family: 'Georgia'; -fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 8 18; -fx-background-radius: 6; -fx-cursor: hand;"
                : "-fx-background-color: #EAF3FF; -fx-text-fill: #0A6CF1;" +
                "-fx-font-family: 'Georgia'; -fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 8 18; -fx-background-radius: 6; -fx-cursor: hand;");
        btnReserver.setOnAction(e -> showVoyages());

        prixBox.getChildren().addAll(prixVBox, spacer, btnReserver);
        contenu.getChildren().addAll(lblDates, lblDesc, prixBox);

        carte.getChildren().addAll(imageZone, contenu);
        return carte;
    }

    // ============================================
    // 🧭 NAVIGATION
    // ============================================

    /**
     * ✅ ACCUEIL — remet le ScrollPane original au lieu de charger un nouveau FXML
     * C'est la clé : on ne charge pas homeAccueil.fxml, on réaffiche accueilScrollPane
     */
    @FXML
    public void showAccueil() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(accueilScrollPane);
        accueilScrollPane.setVvalue(0); // remonter en haut
        setActiveButton(btnAccueil);
    }

    @FXML
    public void showdestination() {
        loadContent("/fxml/destination.fxml");
        setActiveButton(btnOffres);
    }

    @FXML
    private void ouvrirRechercheVols(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RechercheVolsAmeliore.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Recherche de Vols");
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }


    @FXML
    public void showContact() {
        loadContent("/fxml/contact.fxml");
        // Optionnel : tu peux mettre un bouton actif si tu veux
    }


    @FXML
    public void showVoyages() {
        System.out.println("📍 Chargement des Voyages");
        loadContent("/fxml/voyages.fxml");
        setActiveButton(btnVoyages);
    }

    @FXML
    public void showOffres() {
        setActiveButton(btnOffres);
        new Alert(Alert.AlertType.INFORMATION, "Cette section sera bientôt disponible !").showAndWait();
    }

    @FXML
    public void showOmra() {
        setActiveButton(btnOmra);
        new Alert(Alert.AlertType.INFORMATION, "Cette section sera bientôt disponible !").showAndWait();
    }

    @FXML
    public void showAdmin() { showVoyages(); }
    public void showAdmin() {
        System.out.println("📍 Mode Admin - Voyages");
        showVoyages();
    }

    @FXML
    public void showSignIn() {
        System.out.println("📍 Navigation vers Sign In");
        HelloApplication.showView(SessionManager.View.SIGN_IN);
    }

    @FXML
    public void showSignUp() {
        System.out.println("📍 Navigation vers Sign Up");
        HelloApplication.showView(SessionManager.View.SIGN_UP);
    }

//    @FXML
//    public void showContact() {
//        System.out.println("📞 Formulaire de contact");
//
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Contactez-nous");
//        alert.setHeaderText("Informations de contact");
//        alert.setContentText(
//                "📧 Email: contact@voyages-excellence.com\n" +
//                        "📞 Téléphone: +216 XX XXX XXX\n" +
//                        "📍 Adresse: Tunis, Tunisie\n\n" +
//                        "Nous sommes à votre disposition du lundi au vendredi de 9h à 18h."
//        );
//        alert.showAndWait();
//    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

            System.out.println("✅ Contenu chargé: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        btnAccueil.getStyleClass().remove("nav-button-active");
        btnVoyages.getStyleClass().remove("nav-button-active");
        btnOffres.getStyleClass().remove("nav-button-active");
        btnOmra.getStyleClass().remove("nav-button-active");

        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    public void ouvrirChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chatbot.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Assistant Voyages");
            stage.setScene(new Scene(root, 450, 600));
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur chatbot: " + e.getMessage());
        }
    }

    // ============================================
    // 🔧 HELPERS
    // ============================================
    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        if (btnAccueil != null) btnAccueil.getStyleClass().remove("nav-button-active");
        if (btnVoyages != null) btnVoyages.getStyleClass().remove("nav-button-active");
        if (btnOffres  != null) btnOffres.getStyleClass().remove("nav-button-active");
        if (btnOmra    != null) btnOmra.getStyleClass().remove("nav-button-active");
        if (activeButton != null && !activeButton.getStyleClass().contains("nav-button-active"))
            activeButton.getStyleClass().add("nav-button-active");
    }
}

}