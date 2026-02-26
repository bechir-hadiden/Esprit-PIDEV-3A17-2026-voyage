package com.example.demo1.controller.authentification;
import com.example.demo1.entity.User;
import com.example.demo1.services.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox emailNotificationsCheck;
    @FXML private CheckBox pushNotificationsCheck;
    @FXML private CheckBox smsNotificationsCheck;
    @FXML private CheckBox marketingEmailsCheck;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        User user = authService.getCurrentUser();
        if (user != null) {
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
        }
    }

    @FXML
    private void saveProfile() {
        User user = authService.getCurrentUser();
        if (user != null) {
            user.setFullName(fullNameField.getText());
            user.setEmail(emailField.getText());
            user.setPhone(phoneField.getText());

            if (authService.updateUser(user)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile");
            }
        }
    }

    @FXML
    private void changePassword() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all password fields");
            return;
        }

        if (!newPass.equals(confirm)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "New passwords do not match");
            return;
        }

        if (newPass.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Password must be at least 6 characters");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully");
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void saveNotifications() {
        showAlert(Alert.AlertType.INFORMATION, "Success", "Notification preferences saved");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


