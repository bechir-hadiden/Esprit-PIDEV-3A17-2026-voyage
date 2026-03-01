package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.example.entities.User;

import java.io.IOException;

public class MainShellController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Label roleBadge;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnFleet;
    @FXML
    private Button btnMyReservations;
    @FXML
    private Button btnAIGuide;
    @FXML
    private Button btnCategories;
    @FXML
    private Button btnNotify;
    @FXML
    private Button btnStats;

    private static MainShellController instance;
    private User currentUser;
    private Button currentActiveButton;

    public static MainShellController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Role-based visibility
        if (!user.isAdmin()) {
            roleBadge.setText("VOYAGEUR");
            roleBadge.setStyle(
                    "-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #10b981; -fx-padding: 2 10; -fx-background-radius: 5; -fx-font-size: 10px; -fx-font-weight: bold;");

            btnFleet.setText("Transports");
            btnNotify.setDisable(true);
            btnNotify.setVisible(false);
            btnNotify.setManaged(false);
            btnStats.setDisable(true);
            btnStats.setVisible(false);
            btnStats.setManaged(false);
            btnCategories.setDisable(true);
            btnCategories.setVisible(false);
            btnCategories.setManaged(false);

            loadView("/fxml/user_menu.fxml", btnDashboard);
            btnDashboard.setText("Type de Transport");

            btnFleet.setDisable(true);
            btnFleet.setVisible(false);
            btnFleet.setManaged(false);
        } else {
            roleBadge.setText("ADMINISTRATEUR");
            roleBadge.setStyle(
                    "-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #ef4444; -fx-padding: 2 10; -fx-background-radius: 5; -fx-font-size: 10px; -fx-font-weight: bold;");

            btnMyReservations.setDisable(true);
            btnMyReservations.setVisible(false);
            btnMyReservations.setManaged(false);
            btnAIGuide.setDisable(true);
            btnAIGuide.setVisible(false);
            btnAIGuide.setManaged(false);
            loadView("/fxml/admin.fxml", btnDashboard);
        }
    }

    @FXML
    private void showMyReservations(ActionEvent event) {
        loadView("/fxml/my_reservations.fxml", btnMyReservations);
    }

    @FXML
    private void showAIGuide(ActionEvent event) {
        loadView("/fxml/ai_guide.fxml", btnAIGuide);
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        if (currentUser != null && !currentUser.isAdmin()) {
            loadView("/fxml/user_menu.fxml", btnDashboard);
        } else {
            loadView("/fxml/admin.fxml", btnDashboard);
        }
    }

    @FXML
    private void showFleet(ActionEvent event) {
        if (currentUser != null && currentUser.isAdmin()) {
            loadView("/fxml/vehicule_table_view.fxml", btnFleet);
        } else {
            loadView("/fxml/transport_list.fxml", btnFleet);
        }
    }

    @FXML
    private void showCategories(ActionEvent event) {
        loadView("/fxml/type_table_view.fxml", btnCategories);
    }

    @FXML
    private void showNotify(ActionEvent event) {
        loadView("/fxml/smart_notify_monitor.fxml", btnNotify);
    }

    @FXML
    private void showStats(ActionEvent event) {
        loadView("/fxml/admin_stats.fxml", btnStats);
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        instance = null;
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(root));
        stage.centerOnScreen();
        stage.show();
    }

    public void loadView(String fxmlPath, Button navBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Pass user to child controllers if they need it
            Object controller = loader.getController();
            if (controller instanceof UserMenuController) {
                ((UserMenuController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof TransportListController) {
                ((TransportListController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof MyReservationsController) {
                ((MyReservationsController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof AIGuideController) {
                ((AIGuideController) controller).setCurrentUser(currentUser);
            }

            contentArea.getChildren().setAll(view);

            // UI Feedback for Sidebar
            if (currentActiveButton != null) {
                currentActiveButton.getStyleClass().remove("nav-button-active");
            }
            if (navBtn != null) {
                navBtn.getStyleClass().add("nav-button-active");
                currentActiveButton = navBtn;
            }

        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public Button getBtnFleet() {
        return btnFleet;
    }

    public Button getBtnDashboard() {
        return btnDashboard;
    }

    public Button getBtnMyReservations() {
        return btnMyReservations;
    }

    public Button getBtnAIGuide() {
        return btnAIGuide;
    }

    public Button getBtnNotify() {
        return btnNotify;
    }

    public Button getBtnCategories() {
        return btnCategories;
    }

    public Button getBtnStats() {
        return btnStats;
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    public void updateActiveButton(Button navBtn) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
        }
        if (navBtn != null) {
            navBtn.getStyleClass().add("nav-button-active");
            currentActiveButton = navBtn;
        }
    }
}
