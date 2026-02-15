package org.example.demo10.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class MainMenuController {

    /**
     * Ouvre l'interface Administrateur (gestion des avis)
     */
    @FXML
    private void handleAdminMode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo10/admin-avis.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Administration - Gestion des Avis");
            stage.setScene(new Scene(root, 1200, 700));
            stage.show();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir l'interface administrateur.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ouvre l'interface Admin Réservations (NOUVEAU)
     */
    @FXML
    private void handleAdminReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo10/admin-reservation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("🏨 Administration - Gestion des Réservations");
            stage.setScene(new Scene(root, 1300, 800));
            stage.show();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la gestion des réservations.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ouvre l'interface Client (gestion des avis)
     */
    @FXML
    private void handleClientMode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo10/client-avis.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Espace Client - Avis");
            stage.setScene(new Scene(root, 1200, 700));
            stage.show();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir l'interface client.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ouvre la liste des voyages pour les clients
     */
    @FXML
    private void handleVoyagesClient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo10/client-voyages.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("✈️ Nos Voyages");
            stage.setScene(new Scene(root, 1200, 700));
            stage.show();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la liste des voyages.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ouvre les statistiques globales
     */
    @FXML
    private void handleStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo10/admin-statistiques.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📊 Statistiques Globales");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();

        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir les statistiques.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Quitte l'application avec confirmation
     */
    @FXML
    private void handleQuit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Quitter l'application");
        alert.setContentText("Êtes-vous sûr de vouloir quitter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}