package org.example.demo10.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.demo10.dao.UserDAO;
import org.example.demo10.model.User;

import java.util.Optional;

public class LoginDialog {

    // Connexion
    public static Optional<User> showLoginDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Connexion");
        dialog.setHeaderText("Connectez-vous pour voter");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(300);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setFocusTraversable(true); // Important

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setFocusTraversable(true); // Important

        Button btnInscription = new Button("Créer un compte");
        btnInscription.setStyle("-fx-background-color: transparent; -fx-text-fill: #007bff; -fx-underline: true; -fx-cursor: hand;");

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 0, 1);
        grid.add(new Label("Mot de passe:"), 0, 2);
        grid.add(passwordField, 0, 3);
        grid.add(btnInscription, 0, 4);

        dialog.getDialogPane().setContent(grid);

        // Forcer le focus sur le premier champ
        javafx.application.Platform.runLater(() -> emailField.requestFocus());

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        emailField.textProperty().addListener((obs, old, newVal) -> {
            okButton.setDisable(newVal.trim().isEmpty() || passwordField.getText().trim().isEmpty());
        });

        passwordField.textProperty().addListener((obs, old, newVal) -> {
            okButton.setDisable(newVal.trim().isEmpty() || emailField.getText().trim().isEmpty());
        });

        btnInscription.setOnAction(e -> {
            dialog.close();
            showRegisterDialog();
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                UserDAO userDAO = new UserDAO();
                User user = userDAO.getUserByEmail(emailField.getText().trim());
                if (user != null && passwordField.getText().trim().equals(user.getMotDePasse())) {
                    return user;
                } else {
                    showAlert("Erreur", "Email ou mot de passe incorrect");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // Inscription - Version avec Stage améliorée
    private static void showRegisterDialog() {
        Stage stage = new Stage();
        stage.setTitle("Inscription");
        stage.initModality(Modality.APPLICATION_MODAL); // Bloque la fenêtre parente
        stage.setResizable(false); // Empêche le redimensionnement

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(350);
        vbox.setPrefHeight(400);
        vbox.setStyle("-fx-background-color: #ffffff; -fx-padding: 20;");

        // Titre
        Label titleLabel = new Label("📝 Créer un compte");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #007bff;");

        // Champ Nom
        Label lblNom = new Label("Nom complet:");
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        TextField txtNom = new TextField();
        txtNom.setPromptText("Entrez votre nom");
        txtNom.setFocusTraversable(true);
        txtNom.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ced4da; -fx-background-color: white;");

        // Champ Email
        Label lblEmail = new Label("Email:");
        lblEmail.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Entrez votre email");
        txtEmail.setFocusTraversable(true);
        txtEmail.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ced4da; -fx-background-color: white;");

        // Champ Mot de passe
        Label lblPass = new Label("Mot de passe:");
        lblPass.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Créez un mot de passe");
        txtPass.setFocusTraversable(true);
        txtPass.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ced4da; -fx-background-color: white;");

        // Champ Confirmation
        Label lblConfirm = new Label("Confirmation:");
        lblConfirm.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        PasswordField txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Confirmez le mot de passe");
        txtConfirm.setFocusTraversable(true);
        txtConfirm.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ced4da; -fx-background-color: white;");

        // Label d'erreur
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red; -fx-font-size: 12px; -fx-padding: 5 0 0 0;");

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnInscrire = new Button("S'inscrire");
        btnInscrire.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5; -fx-cursor: hand;");
        btnInscrire.setDisable(true);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 5; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(btnInscrire, btnAnnuler);

        // Ajouter tous les éléments au VBox
        vbox.getChildren().addAll(
                titleLabel,
                lblNom, txtNom,
                lblEmail, txtEmail,
                lblPass, txtPass,
                lblConfirm, txtConfirm,
                lblError,
                buttonBox
        );

        // Forcer le focus sur le premier champ après l'affichage
        stage.setOnShown(e -> {
            javafx.application.Platform.runLater(() -> {
                txtNom.requestFocus();
            });
        });

        // Validation
        Runnable validate = () -> {
            boolean nomOk = !txtNom.getText().trim().isEmpty();
            boolean emailOk = !txtEmail.getText().trim().isEmpty();
            boolean passOk = !txtPass.getText().trim().isEmpty();
            boolean confirmOk = !txtConfirm.getText().trim().isEmpty() &&
                    txtPass.getText().equals(txtConfirm.getText());

            btnInscrire.setDisable(!(nomOk && emailOk && passOk && confirmOk));

            if (!txtPass.getText().equals(txtConfirm.getText()) && !txtConfirm.getText().isEmpty()) {
                lblError.setText("❌ Les mots de passe ne correspondent pas");
            } else {
                lblError.setText("");
            }
        };

        txtNom.textProperty().addListener((obs, old, newVal) -> validate.run());
        txtEmail.textProperty().addListener((obs, old, newVal) -> validate.run());
        txtPass.textProperty().addListener((obs, old, newVal) -> validate.run());
        txtConfirm.textProperty().addListener((obs, old, newVal) -> validate.run());

        // Action inscription
        btnInscrire.setOnAction(e -> {
            UserDAO userDAO = new UserDAO();
            String email = txtEmail.getText().trim();

            if (userDAO.getUserByEmail(email) != null) {
                showAlert("Erreur", "Cet email est déjà utilisé");
                return;
            }

            User newUser = new User(
                    txtNom.getText().trim(),
                    email,
                    txtPass.getText().trim()
            );

            if (userDAO.createUser(newUser)) {
                showAlert("Succès", "Inscription réussie !");
                stage.close();
            }
        });

        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // Déconnexion
    public static boolean showLogoutDialog(String userName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        alert.setContentText("Au revoir " + userName + " !");

        ButtonType btnOui = new ButtonType("Oui, me déconnecter");
        ButtonType btnNon = new ButtonType("Non, rester connecté");

        alert.getButtonTypes().setAll(btnOui, btnNon);

        return alert.showAndWait().orElse(btnNon) == btnOui;
    }

    // Alertes
    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}