package com.example.demo1.controller.client;

import com.example.demo1.entity.User;
import com.example.demo1.services.AuthService;
import com.example.demo1.services.WalletService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;

public class MyWalletController {

    @FXML
    private Label balanceLabel;
    @FXML
    private Label pointsLabel;
    @FXML
    private Label walletStatusLabel;
    @FXML
    private Label forecastSummaryLabel;
    @FXML
    private Label forecastDateLabel;
    @FXML
    private Label forecastRateLabel;
    @FXML
    private TextField topUpAmountField;
    @FXML
    private TextField transferRecipientField;
    @FXML
    private TextField transferAmountField;

    private final AuthService authService = AuthService.getInstance();
    private final WalletService walletService = new WalletService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        refreshWalletUI();
    }

    @FXML
    private void handleTopUp() {
        User user = authService.getCurrentUser();
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "No active user session.");
            return;
        }
        int userId = parseUserId(user);
        if (userId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "Invalid user identifier.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(topUpAmountField.getText().trim());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Enter a valid amount.");
            return;
        }

        WalletService.WalletActionResult result = walletService.topUp(userId, amount);
        if (!result.isSuccess()) {
            showAlert(Alert.AlertType.ERROR, "Top-up Failed", result.getMessage());
            return;
        }

        user.setWalletBalance(result.getNewBalance());
        user.setLoyaltyPoints(result.getNewPoints());
        topUpAmountField.clear();
        refreshWalletUI();
        walletStatusLabel.setText("Top-up successful: +" + formatAmount(amount) + " DT, +" + result.getEarnedPoints() + " pts.");
        showAlert(Alert.AlertType.INFORMATION, "Top-up Complete", result.getMessage());
    }

    @FXML
    private void handleTransfer() {
        User user = authService.getCurrentUser();
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "No active user session.");
            return;
        }
        int userId = parseUserId(user);
        if (userId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "Invalid user identifier.");
            return;
        }

        String recipient = transferRecipientField.getText() != null ? transferRecipientField.getText().trim() : "";
        if (recipient.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Recipient", "Enter recipient email or username.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(transferAmountField.getText().trim());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Enter a valid transfer amount.");
            return;
        }

        WalletService.WalletActionResult result = walletService.transfer(userId, recipient, amount);
        if (!result.isSuccess()) {
            showAlert(Alert.AlertType.ERROR, "Transfer Failed", result.getMessage());
            return;
        }

        user.setWalletBalance(result.getNewBalance());
        user.setLoyaltyPoints(result.getNewPoints());
        transferRecipientField.clear();
        transferAmountField.clear();
        refreshWalletUI();
        walletStatusLabel.setText("Transfer successful: -" + formatAmount(amount) + " DT, +" + result.getEarnedPoints() + " pts.");
        showAlert(Alert.AlertType.INFORMATION, "Transfer Complete", result.getMessage());
    }

    private void refreshWalletUI() {
        User user = authService.getCurrentUser();
        if (user == null) {
            balanceLabel.setText("0.00 DT");
            pointsLabel.setText("0 pts");
            walletStatusLabel.setText("Please sign in to use wallet features.");
            forecastSummaryLabel.setText("Forecast unavailable.");
            forecastDateLabel.setText("Estimated empty date: --");
            forecastRateLabel.setText("Average net burn/day: --");
            return;
        }

        balanceLabel.setText(formatAmount(user.getWalletBalance()) + " DT");
        pointsLabel.setText(user.getLoyaltyPoints() + " pts");
        walletStatusLabel.setText("Recharge to earn points. Transfer money to friends and earn loyalty rewards.");

        int userId = parseUserId(user);
        WalletService.WalletForecastResult forecast = walletService.predictBalanceDepletion(userId, user.getWalletBalance());
        updateForecastUI(forecast);
    }

    private void updateForecastUI(WalletService.WalletForecastResult forecast) {
        if (forecast == null) {
            forecastSummaryLabel.setText("Forecast unavailable.");
            forecastDateLabel.setText("Estimated empty date: --");
            forecastRateLabel.setText("Average net burn/day: --");
            return;
        }

        if (!forecast.isPredictionAvailable()) {
            forecastSummaryLabel.setText(forecast.getMessage());
            forecastDateLabel.setText("Estimated empty date: --");
            forecastRateLabel.setText("Average net burn/day: --");
            return;
        }

        if (forecast.getEstimatedDaysToEmpty() <= 0) {
            forecastSummaryLabel.setText("Wallet is already empty.");
            forecastDateLabel.setText("Estimated empty date: Today");
            forecastRateLabel.setText("Average net burn/day: " + formatAmount(forecast.getAverageDailyOutflow()) + " DT");
            return;
        }

        forecastSummaryLabel.setText("Estimated depletion in " + forecast.getEstimatedDaysToEmpty() + " day(s).");
        if (forecast.getEstimatedEmptyDate() != null) {
            forecastDateLabel.setText("Estimated empty date: " + DATE_FMT.format(forecast.getEstimatedEmptyDate()));
        } else {
            forecastDateLabel.setText("Estimated empty date: --");
        }
        forecastRateLabel.setText(
                "Average net burn/day (" + forecast.getWindowDays() + "d): "
                        + formatAmount(forecast.getAverageDailyOutflow()) + " DT"
                        + " | actions: " + forecast.getActionCount()
        );
    }

    private static int parseUserId(User user) {
        try {
            return Integer.parseInt(user.getId());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formatAmount(double amount) {
        return String.format("%.2f", amount);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
