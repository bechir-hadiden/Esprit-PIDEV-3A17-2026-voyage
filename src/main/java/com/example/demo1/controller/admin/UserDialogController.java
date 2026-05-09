package com.example.demo1.controller.admin;

import com.example.demo1.controller.dao.UserDAO;
import com.example.demo1.entity.User;
import com.example.demo1.Utils.BCryptWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserDialogController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordLabel;
    @FXML private Label errorLabel;

    private User editingUser;
    private boolean saved = false;
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        roleComboBox.setItems(javafx.collections.FXCollections.observableArrayList("CLIENT", "ADMIN"));
        roleComboBox.getSelectionModel().selectFirst();
    }

    public void initForAdd() {
        editingUser = null;
        passwordField.setVisible(true);
        passwordLabel.setVisible(true);
    }

    public void initForEdit(User user) {
        editingUser = user;
        usernameField.setText(user.getUsername());
        usernameField.setDisable(true);
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        roleComboBox.setValue(user.getRole());
        passwordField.setVisible(false);
        passwordLabel.setVisible(false);
    }

    @FXML
    public void handleSave() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = roleComboBox.getValue();
        String password = passwordField.getText();

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            showError("Username, full name, and email are required.");
            return;
        }

        if (editingUser == null) {
            // Create new user
            if (password.isEmpty()) {
                showError("Password is required for new users.");
                return;
            }
            if (userDAO.getUserByUsername(username) != null) {
                showError("Username already exists.");
                return;
            }
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setRole(role);
            newUser.setAvatar("https://ui-avatars.com/api/?name=" + fullName.replace(" ", "+") + "&background=2563EB&color=fff");

            if (userDAO.createUser(newUser, password)) {
                saved = true;
                closeDialog();
            } else {
                showError("Failed to create user.");
            }
        } else {
            // Update existing user
            editingUser.setFullName(fullName);
            editingUser.setEmail(email);
            editingUser.setPhone(phone);
            editingUser.setRole(role);
            if (userDAO.updateUser(editingUser)) {
                saved = true;
                closeDialog();
            } else {
                showError("Failed to update user.");
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void closeDialog() {
        errorLabel.getScene().getWindow().hide();
    }
}
