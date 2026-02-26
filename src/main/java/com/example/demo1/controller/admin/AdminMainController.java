package com.example.demo1.controller.admin;

import com.example.demo1.HelloApplication;
import com.example.demo1.services.AuthService;
import com.example.demo1.services.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class AdminMainController {

    @FXML
    private Label adminNameLabel;
    @FXML
    private Label contentTitle;
    @FXML
    private StackPane contentArea;

    @FXML
    private Button dashboardButton;
    @FXML
    private Button hotelsButton;
    @FXML
    private Button transportButton;
    @FXML
    private Button offersButton;
    @FXML
    private Button plansButton;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        // Set admin name
        if (authService.getCurrentUser() != null) {
            adminNameLabel.setText(authService.getCurrentUser().getFullName());
        }

        // Show dashboard by default
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        loadContent("/fxml/admin/AdminDashboard.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML
    private void showHotels() {
        loadContent("/fxml/admin/AdminHotels.fxml");
        setActiveButton(hotelsButton);
    }

    @FXML
    private void showTransport() {
        loadComingSoon("Transport");
        setActiveButton(transportButton);
    }

    @FXML
    private void showOffers() {
        loadComingSoon("Offers");
        setActiveButton(offersButton);
    }

    @FXML
    private void showPlans() {
        loadComingSoon("Plans");
        setActiveButton(plansButton);
    }

    @FXML
    private void showSettings() {
        loadContent("/fxml/admin/AdminSettings.fxml");
        contentTitle.setText("Settings");
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        HelloApplication.showView(SessionManager.View.SIGN_IN);
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (Exception e) {
            System.err.println("Error loading content: " + fxmlPath);
            e.printStackTrace();
            // Show error message or fallback content
        }
    }

    private void loadComingSoon(String sectionName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/AdminComingSoon.fxml"));
            Parent content = loader.load();

            // Set section name in controller
            AdminComingSoonController controller = loader.getController();
            if (controller != null) {
                controller.setSectionName(sectionName);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (Exception e) {
            System.err.println("Error loading coming soon view");
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        dashboardButton.getStyleClass().remove("active");
        hotelsButton.getStyleClass().remove("active");
        transportButton.getStyleClass().remove("active");
        offersButton.getStyleClass().remove("active");
        plansButton.getStyleClass().remove("active");

        // Add active class to the selected button
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }
}

