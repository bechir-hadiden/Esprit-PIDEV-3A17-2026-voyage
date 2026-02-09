package com.example.demo1.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;  // ← Ajoutez cet import

import java.io.IOException;

public class HomeController {

    @FXML
    private StackPane contentArea;

    @FXML
    private ScrollPane accueilPane;  //

    @FXML
    private Button btnAccueil;

    @FXML
    private Button btnVoyages;

    @FXML
    private Button btnOffres;

    @FXML
    private Button btnOmra;

    @FXML
    public void initialize() {
        System.out.println("✅ HomeController initialisé");
        // La page d'accueil est déjà visible par défaut dans le FXML
//        loadContent("/fxml/voyages.fxml");

    }

    @FXML
    public void showAccueil() {
        System.out.println("📍 Affichage Accueil");
        loadContent("/fxml/homeAccueil.fxml");
        setActiveButton(btnAccueil);
    }


    @FXML
    public void showVoyages() {
        System.out.println("📍 Chargement des Voyages");
        loadContent("/fxml/voyages.fxml");
        setActiveButton(btnVoyages);
    }

    @FXML
    public void showOffres() {
        System.out.println("📍 Page Offres - À implémenter");
        setActiveButton(btnOffres);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Offres Spéciales");
        alert.setHeaderText("Nos meilleures offres");
        alert.setContentText("Cette section sera bientôt disponible !");
        alert.showAndWait();
    }

    @FXML
    public void showOmra() {
        System.out.println("📍 Page Omra - À implémenter");
        setActiveButton(btnOmra);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Voyages Omra");
        alert.setHeaderText("Omra et Hajj");
        alert.setContentText("Cette section sera bientôt disponible !");
        alert.showAndWait();
    }

    @FXML
    public void showAdmin() {
        System.out.println("📍 Mode Admin - Voyages");
        showVoyages();
    }

    @FXML
    public void showContact() {
        System.out.println("📞 Formulaire de contact");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contactez-nous");
        alert.setHeaderText("Informations de contact");
        alert.setContentText(
                "📧 Email: contact@voyages-excellence.com\n" +
                        "📞 Téléphone: +216 XX XXX XXX\n" +
                        "📍 Adresse: Tunis, Tunisie\n\n" +
                        "Nous sommes à votre disposition du lundi au vendredi de 9h à 18h."
        );
        alert.showAndWait();
    }

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
}