package com.example.demo1.controller;

import com.example.demo1.HelloApplication;
import com.example.demo1.services.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.scene.Scene;

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
    private void ouvrirRechercheVols(ActionEvent event) {  // ✅ Avec paramètre
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RechercheVolsAmeliore.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);

            stage.setScene(scene);
            stage.setTitle("Recherche de Vols");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
            e.printStackTrace();
        }
    }

//    @FXML
//    private void ouvrirRechercheVols(ActionEvent event) {
//        try {
//            System.out.println("🔍 Ouverture de la recherche de vols...");
//
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RechercheVolsAmeliore.fxml"));
//            Parent root = loader.load();
//
//            // Remplacer la scène actuelle
//            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//            stage.setScene(new Scene(root, 1200, 700));
//            stage.setTitle("✈️ Recherche de Vols");
//
//            System.out.println("✅ Page de recherche chargée !");
//
//        } catch (IOException e) {
//            System.err.println("❌ Erreur lors du chargement:");
//            e.printStackTrace();
//
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Erreur");
//            alert.setContentText("Impossible d'ouvrir la recherche de vols");
//            alert.showAndWait();
//        }
//    }

}