package com.example.demo1.controller.admin;

import com.example.demo1.controller.dao.UserDAO;
import com.example.demo1.entity.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UserDialogController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label passwordLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label confirmPasswordLabel;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private CheckBox blockedCheckBox;
    @FXML
    private Label errorLabel;

    private User user;
    private boolean isEditMode = false;
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Setup role dropdown
        roleComboBox.setItems(javafx.collections.FXCollections.observableArrayList(
            "CLIENT", "ADMIN"
        ));
        roleComboBox.getSelectionModel().selectFirst();

        // Hide password fields in edit mode by default
        updatePasswordFieldsVisibility();
        
        // Clear error on input
        fullNameField.textProperty().addListener((obs, old, val) -> hideError());
        usernameField.textProperty().addListener((obs, old, val) -> hideError());
        emailField.textProperty().addListener((obs, old, val) -> hideError());
        passwordField.textProperty().addListener((obs, old, val) -> hideError());
        confirmPasswordField.textProperty().addListener((obs, old, val) -> hideError());
    }

    /**
     * Initialize dialog for adding a new user
     */
    public void initForAdd() {
        isEditMode = false;
        user = new User();
        titleLabel.setText("Add New User");
        
        // Show password fields (required for new user)
        passwordLabel.setVisible(true);
        passwordLabel.setManaged(true);
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        confirmPasswordLabel.setVisible(true);
        confirmPasswordLabel.setManaged(true);
        confirmPasswordField.setVisible(true);
        confirmPasswordField.setManaged(true);
        
        // Default: not blocked
        blockedCheckBox.setSelected(false);
    }

    /**
     * Initialize dialog for editing existing user
     */
    public void initForEdit(User existingUser) {
        isEditMode = true;
        user = existingUser;
        titleLabel.setText("Edit User");

        // Fill fields with user data
        fullNameField.setText(user.getFullName());
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        roleComboBox.setValue(user.getRole());
        blockedCheckBox.setSelected(user.isBlocked());

        // Username is read-only in edit mode
        usernameField.setEditable(false);
        usernameField.setStyle("-fx-opacity: 0.6;");

        // Hide password fields by default in edit mode (optional)
        passwordLabel.setVisible(false);
        passwordLabel.setManaged(false);
        passwordField.setVisible(false);
        passwordField.setManaged(false);
        confirmPasswordLabel.setVisible(false);
        confirmPasswordLabel.setManaged(false);
        confirmPasswordField.setVisible(false);
        confirmPasswordField.setManaged(false);
    }

    private void updatePasswordFieldsVisibility() {
        // Only used if we want to toggle password fields dynamically
    }

    @FXML
    public void handleSave() {
        if (!validateInput()) {
            return;
        }

        // Set user data from form
        user.setFullName(fullNameField.getText().trim());
        if (!isEditMode) {
            user.setUsername(usernameField.getText().trim());
        }
        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());
        user.setRole(roleComboBox.getValue());
        user.setBlocked(blockedCheckBox.isSelected());

        boolean success;
        if (isEditMode) {
            // Update existing user
            success = userDAO.updateUser(user);
        } else {
            // Create new user
            String password = passwordField.getText();
            success = userDAO.createUser(user, password);
        }

        if (success) {
            closeDialogWithSuccess();
        } else {
            showError(isEditMode ? 
                "Failed to update user. Please try again." : 
                "Failed to create user. Username or email may already exist.");
        }
    }

    private boolean validateInput() {
        // Full Name validation
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            showError("Full name is required");
            fullNameField.requestFocus();
            return false;
        }
        if (!isValidFullName(fullName)) {
            showError("Full name must contain only letters and spaces");
            fullNameField.requestFocus();
            return false;
        }

        // Username validation (only for new users)
        if (!isEditMode) {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                showError("Username is required");
                usernameField.requestFocus();
                return false;
            }
            if (username.length() < 3) {
                showError("Username must be at least 3 characters");
                usernameField.requestFocus();
                return false;
            }
        }

        // Email validation
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Email is required");
            emailField.requestFocus();
            return false;
        }
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address (must contain @ and domain)");
            emailField.requestFocus();
            return false;
        }

        // Role validation
        if (roleComboBox.getValue() == null) {
            showError("Please select a role");
            roleComboBox.requestFocus();
            return false;
        }

        // Password validation (only for new users)
        if (!isEditMode) {
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            if (password.isEmpty()) {
                showError("Password is required");
                passwordField.requestFocus();
                return false;
            }
            if (password.length() < 6) {
                showError("Password must be at least 6 characters");
                passwordField.requestFocus();
                return false;
            }
            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match");
                confirmPasswordField.requestFocus();
                return false;
            }
        }

        return true;
    }

    private boolean isValidFullName(String fullName) {
        // Only letters and spaces allowed
        return fullName.matches("^[a-zA-Z\\s]+$");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }

    private void closeDialogWithSuccess() {
        // Close the dialog
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Get the saved/updated user
     */
    public User getUser() {
        return user;
    }

    /**
     * Check if save was successful (user can check if getUser() returns non-null)
     */
    public boolean isSaved() {
        return user != null && user.getId() != null;
    }
}
