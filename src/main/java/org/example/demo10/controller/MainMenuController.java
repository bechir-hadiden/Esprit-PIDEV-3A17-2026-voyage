package org.example.demo10.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class MainMenuController {

    @FXML
    private void handleAdminMode(ActionEvent event) {
        try {
            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            // Ouvrir l'interface admin
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/demo10/admin-avis.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Administration - Gestion des Avis");
            stage.setScene(new Scene(root, 900, 650));
            stage.show();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir l'interface admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClientMode(ActionEvent event) {
        try {
            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            // Ouvrir l'interface client
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/demo10/client-avis.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Espace Client - Avis");
            stage.setScene(new Scene(root, 800, 550));
            stage.show();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir l'interface client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleQuit() {
        System.exit(0);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}