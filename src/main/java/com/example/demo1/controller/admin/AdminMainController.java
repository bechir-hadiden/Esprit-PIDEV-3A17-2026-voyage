package com.example.demo1.controller.admin;

import com.example.demo1.HelloApplication;
import com.example.demo1.services.AuthService;
import com.example.demo1.services.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

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
        loadContent("/fxml/admin/AdminTransport.fxml");
        setActiveButton(transportButton);
    }

    @FXML
    private void showOffers() {
        try {
            // 1. Charger ton fichier FXML de gestion des offres
            // Assure-toi que le fichier est bien dans ce dossier dans resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AfficherOffres.fxml"));
            Node node = loader.load();

            // 2. Mettre à jour le titre en haut de la page (si applicable)
            // pageTitle.setText("Promotion Management");

            // 3. Remplacer le contenu central par ton interface
            // Remplace 'contentArea' par le nom exact du conteneur dans leur code
            contentArea.getChildren().setAll(node);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la gestion des offres : " + e.getMessage());
            e.printStackTrace();
        }
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
