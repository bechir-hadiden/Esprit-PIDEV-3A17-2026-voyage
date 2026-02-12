package com.example.demo1.controller;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;  // ← AJOUTE CETTE LIGNE
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;
public class HomeAccueilController {

    @FXML
    public void initialize() {
        System.out.println("✅ Page d'accueil chargée");
    }

    @FXML
    public void navigateToVoyages() {
        System.out.println("Navigation vers Voyages");
        // Cette méthode sera appelée par le parent HomeController
    }
    @FXML
    private void ouvrirRechercheVols(ActionEvent event) {
        try {
            System.out.println("🔍 Ouverture de la recherche de vols...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RechercheVolsAmeliore.fxml"));
            Parent root = loader.load();

            // Remplacer la scène actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("✈️ Recherche de Vols");

            System.out.println("✅ Page de recherche chargée !");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement:");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir la recherche de vols");
            alert.showAndWait();
        }
    }
    @FXML
    public void showContact() {
        System.out.println("Affichage du formulaire de contact");
        // TODO: Implémenter le formulaire de contact
    }
}