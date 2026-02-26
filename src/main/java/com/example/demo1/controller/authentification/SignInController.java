package com.example.demo1.controller.authentification;
import com.example.demo1.HelloApplication;
import com.example.demo1.services.AuthService;
import com.example.demo1.services.SessionManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class SignInController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberMeCheck;
    @FXML
    private Button signInButton;
    @FXML
    private Label errorLabel;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        // Clear error on input
        emailField.textProperty().addListener((obs, old, val) -> hideError());
        passwordField.textProperty().addListener((obs, old, val) -> hideError());

        // Enter key to submit
        passwordField.setOnAction(e -> handleSignIn());
    }

    @FXML
    private void handleSignIn() {
        String username = emailField.getText().trim(); // Now using username field
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        // Show loading state
        signInButton.setText("Signing in...");
        signInButton.setDisable(true);

        // Simulate async login
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate network delay

                boolean success = authService.login(username, password);

                Platform.runLater(() -> {
                    if (success) {
                        // Role-based routing
                        if (authService.isAdmin()) {
                            // Route to admin dashboard
                            System.out.println("Routing to admin dashboard...");
                            try {
                                HelloApplication.showAdminView();
                            } catch (Exception e) {
                                // Fallback if admin view not yet created
                                System.err.println("Admin view not available yet: " + e.getMessage());
                                HelloApplication.showView(SessionManager.View.DASHBOARD);
                            }
                        } else {
                            // Route to client dashboard
                            HelloApplication.showView(SessionManager.View.DASHBOARD);
                        }
                    } else {
                        showError("Invalid username or password");
                        signInButton.setText("Sign In");
                        signInButton.setDisable(false);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleSignUp() {
        HelloApplication.showView(SessionManager.View.SIGN_UP);
    }

    @FXML
    private void handleForgotPassword() {
        HelloApplication.showView(SessionManager.View.FORGOT_PASSWORD);
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

