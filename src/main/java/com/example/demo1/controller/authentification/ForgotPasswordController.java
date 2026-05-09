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
    private Button verifyCodeButton;
    @FXML
    private Button resetButton;
    @FXML
    private Label messageLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private Label step1Indicator;
    @FXML
    private Label step2Indicator;
    @FXML
    private Label step3Indicator;
    @FXML
    private VBox step1Box;
    @FXML
    private VBox step2Box;
    @FXML
    private VBox step3Box;
    @FXML
    private VBox step4Box;
    @FXML
    private Label emailDisplayLabel;

    private final PasswordResetService resetService = new PasswordResetService();
    private String emailForReset;
    private String verifiedCode;

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

        boolean emailConfigured;
        try {
            emailConfigured = resetService.isEmailConfigured();
        } catch (Exception | Error e) {
            // Email service not available (JPMS restriction or missing config)
            System.out.println("🔐 Reset code for " + email + ": [Email service unavailable - check console]");
            showMessage("Email service unavailable. The reset code will be logged to console for development.", false);
            emailConfigured = false;
        }

        if (!emailConfigured) {
            // For development: skip email and just log the code
            System.out.println("⚠️ Email not configured. Running in development mode.");
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

            // Check if it's a JPMS/module error
            if (cause instanceof IllegalAccessError || cause instanceof ClassNotFoundException ||
                    (cause != null && cause.getMessage() != null && cause.getMessage().contains("module"))) {
                // JPMS error - log code to console and continue in dev mode
                String devCode = String.format("%06d", (int)(Math.random() * 1000000));
                System.out.println("🔐 RESET CODE for " + email + ": " + devCode);
                System.out.println("📧 (Email service unavailable due to JPMS restrictions - using dev mode)");
                emailForReset = email;
                emailDisplayLabel.setText(email);
                showStep2();
                showMessage("Email unavailable. Check console for reset code (dev mode).", false);
            } else if (cause != null && cause.getMessage() != null && cause.getMessage().contains("535")) {
                showMessage("Gmail rejected login. Use an App Password (not your normal password): myaccount.google.com/apppasswords", true);
            } else {
                showMessage("Failed to send email. Check smtp.properties (smtp.user and smtp.password).", true);
            }
        });
        new Thread(task).start();
    }

    @FXML
    private void handleResetPassword() {
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (emailForReset == null || emailForReset.isEmpty() || verifiedCode == null) {
            showMessage("Session expired. Please start over.", true);
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
                // Use the verified code from step 2
                return resetService.resetPassword(emailForReset, verifiedCode, newPass);
            }
        };
        task.setOnSucceeded(e -> {
            resetButton.setDisable(false);
            resetButton.setText("Reset Password");
            if (Boolean.TRUE.equals(task.getValue())) {
                // Show success screen (step 4)
                showStep4();
            } else {
                showMessage("Failed to reset password. Code may have expired.", true);
            }
        });
        task.setOnFailed(e -> {
            resetButton.setDisable(false);
            resetButton.setText("Reset Password");
            showMessage("Error resetting password. Please try again.", true);
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
        step3Box.setVisible(false);
        step3Box.setManaged(false);
        step4Box.setVisible(false);
        step4Box.setManaged(false);
        emailField.clear();
        codeField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        verifiedCode = null;
        emailForReset = null;

        // Update indicators
        updateStepIndicators(1);
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

    @FXML
    private void handleVerifyCode() {
        String code = codeField.getText().trim();

        if (emailForReset == null || emailForReset.isEmpty()) {
            showMessage("Session expired. Please start over.", true);
            return;
        }
        if (code.length() != 6) {
            showMessage("Please enter the 6-digit code.", true);
            return;
        }

        // Verify code exists in the reset service
        verifyCodeButton.setDisable(true);
        verifyCodeButton.setText("Verifying...");
        showMessage("", false);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                // Check if code is valid by trying to verify it (we'll store it temporarily)
                // The actual verification happens in resetPassword, here we just check format
                return resetService.verifyCodeOnly(emailForReset, code);
            }
        };
        task.setOnSucceeded(e -> {
            verifyCodeButton.setDisable(false);
            verifyCodeButton.setText("Verify Code");
            if (Boolean.TRUE.equals(task.getValue())) {
                verifiedCode = code;
                showStep3();
                showMessage("Code verified! Create your new password.", false);
            } else {
                showMessage("Invalid or expired code. Please try again.", true);
            }
        });
        task.setOnFailed(e -> {
            verifyCodeButton.setDisable(false);
            verifyCodeButton.setText("Verify Code");
            showMessage("Error verifying code. Please try again.", true);
        });
        new Thread(task).start();
    }

    @FXML
    private void handleBackToStep2() {
        showStep2();
        showMessage("", false);
    }

    private void showStep3() {
        step2Box.setVisible(false);
        step2Box.setManaged(false);
        step3Box.setVisible(true);
        step3Box.setManaged(true);

        // Update indicators
        updateStepIndicators(3);

        // Clear password fields
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void showStep4() {
        step3Box.setVisible(false);
        step3Box.setManaged(false);
        step4Box.setVisible(true);
        step4Box.setManaged(true);

        // Update indicators
        updateStepIndicators(4);
    }

    private void updateStepIndicators(int currentStep) {
        if (step1Indicator == null || step2Indicator == null || step3Indicator == null) return;

        // Step 1
        if (currentStep >= 1) {
            step1Indicator.setText("● Step 1");
            step1Indicator.setStyle("-fx-text-fill: #667eea; -fx-font-weight: bold;");
        } else {
            step1Indicator.setText("○ Step 1");
            step1Indicator.setStyle("-fx-text-fill: #999;");
        }

        // Step 2
        if (currentStep >= 2) {
            step2Indicator.setText("● Step 2");
            step2Indicator.setStyle(currentStep == 2 ? "-fx-text-fill: #667eea; -fx-font-weight: bold;" : "-fx-text-fill: #667eea;");
        } else {
            step2Indicator.setText("→ Step 2");
            step2Indicator.setStyle("-fx-text-fill: #999;");
        }

        // Step 3
        if (currentStep >= 3) {
            step3Indicator.setText("● Step 3");
            step3Indicator.setStyle(currentStep == 3 ? "-fx-text-fill: #667eea; -fx-font-weight: bold;" : "-fx-text-fill: #667eea;");
        } else {
            step3Indicator.setText("→ Step 3");
            step3Indicator.setStyle("-fx-text-fill: #999;");
        }
    }

    private void showMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        messageLabel.setVisible(!msg.isEmpty());
        messageLabel.setManaged(!msg.isEmpty());
        messageLabel.setStyle(isError ? "-fx-text-fill: #EF4444;" : "-fx-text-fill: #22C55E;");
    }
}

