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
    @FXML
    private Button paymentsButton;
    @FXML
    private Button reclamtionButton;

    // ========== NOUVEAU BOUTON AVIS ==========
    @FXML
    private Button avisButton;
    // ========================================

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
        contentTitle.setText("Tableaux de bord");
    }

    @FXML
    private void showHotels() {
        loadContent("/fxml/admin/AdminHotels.fxml");
        setActiveButton(hotelsButton);
        contentTitle.setText("Hébergements");
    }

    @FXML
    private void showTransport() {
        loadContent("/fxml/admin/AdminTransport.fxml");
        setActiveButton(transportButton);
        contentTitle.setText("Flotte & Déplacements");
    }

    @FXML
    private void showReclmation() {
        loadContent("/fxml/gestion-reclamation.fxml");
        setActiveButton(reclamtionButton);
        contentTitle.setText("Réclamation");
    }

    @FXML
    private void showOffers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AfficherOffres.fxml"));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
            setActiveButton(offersButton);
            contentTitle.setText("Offres de Voyage");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la gestion des offres : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showPlans() {
        loadComingSoon("Plans");
        setActiveButton(plansButton);
        contentTitle.setText("Abonnements");
    }

    @FXML
    private void showPayments() {
        loadContent("/fxml/admin/AdminPayments.fxml");
        setActiveButton(paymentsButton);
        contentTitle.setText("Paiements");
    }

    // ========== NOUVELLE MÉTHODE POUR AVIS ==========
    @FXML
    private void showAvis() {
        loadContent("/fxml/admin/admin-avis.fxml");
        setActiveButton(avisButton);
        contentTitle.setText("Gestion des Avis");
    }
    // ===============================================

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
        }
    }

    private void loadComingSoon(String sectionName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/AdminComingSoon.fxml"));
            Parent content = loader.load();

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
        if (paymentsButton != null) paymentsButton.getStyleClass().remove("active");
        if (reclamtionButton != null) reclamtionButton.getStyleClass().remove("active");
        if (avisButton != null) avisButton.getStyleClass().remove("active");

        // Add active class to the selected button
        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }
}