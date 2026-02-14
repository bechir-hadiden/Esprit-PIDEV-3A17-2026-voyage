package org.example.demo10;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Tester la connexion à la base de données
        testDatabaseConnection();

        // Charger et afficher le menu principal
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/demo10/main-menu.fxml"));
            Scene scene = new Scene(root, 600, 400);

            primaryStage.setTitle("Agence de Voyage");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            // En cas d'erreur, afficher un message simple
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de démarrer l'application");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private void testDatabaseConnection() {
        System.out.println("=== Test de connexion à la base de données ===");
        try {
            if (DBConnection.getConnection() != null) {
                System.out.println("✅ Connexion à MySQL établie avec succès!");
            } else {
                System.out.println("❌ Échec de la connexion à MySQL");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur de connexion: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Démarrage de l'application ===");
        launch(args);
    }
}