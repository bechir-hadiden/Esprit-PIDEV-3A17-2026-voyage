package com.example.demo1.controller.authentification;
import com.example.demo1.HelloApplication;
import com.example.demo1.services.PasswordResetService;
import com.example.demo1.services.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button sendCodeButton;
    @FXML
    private Button resetButton;
    @FXML
    private Label messageLabel;
    @FXML
    private VBox step1Box;
    @FXML
    private VBox step2Box;
    @FXML
    private Label emailDisplayLabel;

    private final PasswordResetService resetService = new PasswordResetService();
    private String emailForReset;

    @FXML
    public void initialize() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showMessage("Please enter your email address", true);
            return;
        }
        if (!resetService.isEmailConfigured()) {
            showMessage("Email not configured. Add smtp.properties with Gmail and App Password. See smtp.properties.example.", true);
            return;
        }

        sendCodeButton.setDisable(true);
        sendCodeButton.setText("Sending...");
        showMessage("", false);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return resetService.requestReset(email);
            }
        };
        task.setOnSucceeded(e -> {
            sendCodeButton.setDisable(false);
            sendCodeButton.setText("Send Reset Code");
            if (Boolean.TRUE.equals(task.getValue())) {
                emailForReset = email;
                emailDisplayLabel.setText(email);
                showStep2();
                showMessage("Code sent! Check your inbox.", false);
            } else {
                showMessage("No account found with this email address.", true);
            }
        });
        task.setOnFailed(e -> {
            sendCodeButton.setDisable(false);
            sendCodeButton.setText("Send Reset Code");
            Throwable cause = task.getException();
            if (cause != null && cause.getCause() != null) cause = cause.getCause();
            String msg = cause != null && cause.getMessage() != null && cause.getMessage().contains("535")
                    ? "Gmail rejected login. Use an App Password (not your normal password): myaccount.google.com/apppasswords"
                    : "Failed to send email. Check smtp.properties (smtp.user and smtp.password).";
            showMessage(msg, true);
        });
        new Thread(task).start();
    }

    @FXML
    private void handleResetPassword() {
        String code = codeField.getText().trim();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (emailForReset == null || emailForReset.isEmpty()) {
            showMessage("Session expired. Please start over.", true);
            return;
        }
        if (code.length() != 6) {
            showMessage("Please enter the 6-digit code.", true);
            return;
        }
        if (newPass.length() < 6) {
            showMessage("Password must be at least 6 characters.", true);
            return;
        }
        if (!newPass.equals(confirm)) {
            showMessage("Passwords do not match.", true);
            return;
        }

        resetButton.setDisable(true);
        resetButton.setText("Resetting...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return resetService.resetPassword(emailForReset, code, newPass);
            }
        };
        task.setOnSucceeded(e -> {
            resetButton.setDisable(false);
            resetButton.setText("Reset Password");
            if (Boolean.TRUE.equals(task.getValue())) {
                showMessage("Password updated! Redirecting to login...", false);
                messageLabel.setStyle("-fx-text-fill: #22C55E;");
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        Platform.runLater(() -> HelloApplication.showView(SessionManager.View.SIGN_IN));
                    } catch (InterruptedException ignored) { }
                }).start();
            } else {
                showMessage("Invalid or expired code. Request a new one.", true);
            }
        });
        task.setOnFailed(e -> {
            resetButton.setDisable(false);
            resetButton.setText("Reset Password");
            showMessage("Invalid or expired code.", true);
        });
        new Thread(task).start();
    }

    @FXML
    private void handleBackToSignIn() {
        HelloApplication.showView(SessionManager.View.SIGN_IN);
    }

    @FXML
    private void handleBackToStep1() {
        step1Box.setVisible(true);
        step1Box.setManaged(true);
        step2Box.setVisible(false);
        step2Box.setManaged(false);
        emailField.clear();
        codeField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        emailForReset = null;
        showMessage("", false);
    }

    private void showStep2() {
        step1Box.setVisible(false);
        step1Box.setManaged(false);
        step2Box.setVisible(true);
        step2Box.setManaged(true);
        codeField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void showMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        messageLabel.setVisible(!msg.isEmpty());
        messageLabel.setManaged(!msg.isEmpty());
        messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #22C55E;");
    }
}

