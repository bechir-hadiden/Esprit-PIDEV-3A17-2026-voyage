package com.example.demo1.controller.admin;
import com.example.demo1.services.AuthService;
import com.example.demo1.entity.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AdminSettingsController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField avatarField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label statusLabel;
    @FXML
    private Label passwordStatusLabel;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
            avatarField.setText(currentUser.getAvatar());
        }
    }

    @FXML
    private void handleSaveProfile() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            currentUser.setFullName(fullNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPhone(phoneField.getText().trim());
            currentUser.setAvatar(avatarField.getText().trim());

            if (authService.updateUser(currentUser)) {
                showStatus(statusLabel, "Profile updated successfully!", "-fx-text-fill: green;");
            } else {
                showStatus(statusLabel, "Failed to update profile", "-fx-text-fill: red;");
            }
        }
    }

    @FXML
    private void handleReset() {
        loadProfile();
        showStatus(statusLabel, "Profile reset to current values", "-fx-text-fill: blue;");
    }

    @FXML
    private void handleChangePassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showStatus(passwordStatusLabel, "Please fill in both password fields", "-fx-text-fill: red;");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showStatus(passwordStatusLabel, "Passwords do not match", "-fx-text-fill: red;");
            return;
        }

        if (newPassword.length() < 4) {
            showStatus(passwordStatusLabel, "Password must be at least 4 characters", "-fx-text-fill: red;");
            return;
        }

        if (authService.updatePassword(newPassword)) {
            showStatus(passwordStatusLabel, "Password updated successfully!", "-fx-text-fill: green;");
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showStatus(passwordStatusLabel, "Failed to update password", "-fx-text-fill: red;");
        }
    }

    private void showStatus(Label label, String message, String style) {
        label.setText(message);
        label.setStyle(style);
        label.setVisible(true);
        label.setManaged(true);
    }
}
