package com.example.demo1.controller.authentification;
import com.example.demo1.HelloApplication;
import com.example.demo1.services.AuthService;
import com.example.demo1.services.SessionManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class SignUpController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private CheckBox termsCheck;
    @FXML
    private Button signUpButton;
    @FXML
    private Label errorLabel;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        // Clear error on input
        fullNameField.textProperty().addListener((obs, old, val) -> hideError());
        emailField.textProperty().addListener((obs, old, val) -> hideError());
        passwordField.textProperty().addListener((obs, old, val) -> hideError());
        confirmPasswordField.textProperty().addListener((obs, old, val) -> hideError());
    }

    @FXML
    private void handleSignUp() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        if (!termsCheck.isSelected()) {
            showError("Please agree to the Terms of Service and Privacy Policy");
            return;
        }

        // Show loading state
        signUpButton.setText("Creating account...");
        signUpButton.setDisable(true);

        // Simulate async registration
        new Thread(() -> {
            try {
                Thread.sleep(500);

                // Generate username from email (part before @)
                String username = email.substring(0, email.indexOf('@'));

                boolean success = authService.register(fullName, email, username, password);

                Platform.runLater(() -> {
                    if (success) {
                        HelloApplication.showView(SessionManager.View.DASHBOARD);
                    } else {
                        showError("An account with this email or username already exists");
                        signUpButton.setText("Create Account");
                        signUpButton.setDisable(false);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleSignIn() {
        HelloApplication.showView(SessionManager.View.SIGN_IN);
    }

    @FXML
    private void handleTerms() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Terms of Service");
        alert.setHeaderText("SmartTrip Terms of Service");
        alert.setContentText(
                "By using SmartTrip, you agree to our terms of service which include responsible use of the platform and adherence to booking policies.");
        alert.showAndWait();
    }

    @FXML
    private void handlePrivacy() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Privacy Policy");
        alert.setHeaderText("SmartTrip Privacy Policy");
        alert.setContentText(
                "We value your privacy. Your personal information is securely stored and only used to provide our travel booking services.");
        alert.showAndWait();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
