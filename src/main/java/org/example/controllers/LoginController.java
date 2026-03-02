package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.entities.User;
import org.example.services.UserService;

/** Controller for login authentication. */
public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Ensure default users exist
        userService.createDefaultUsers();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }

        if (username.length() > 50) {
            errorLabel.setText("Nom d'utilisateur trop long (max 50)");
            errorLabel.setVisible(true);
            return;
        }

        if (password.length() > 100) {
            errorLabel.setText("Mot de passe trop long (max 100)");
            errorLabel.setVisible(true);
            return;
        }

        User user = userService.authenticate(username, password);

        if (user != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_shell.fxml"));
                Scene scene = new Scene(loader.load(), 1200, 800);

                MainShellController controller = loader.getController();
                controller.setCurrentUser(user);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.centerOnScreen();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Chargement de l'interface échoué");
            }
        } else {
            errorLabel.setText("Identifiants incorrects");
            errorLabel.setVisible(true);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
