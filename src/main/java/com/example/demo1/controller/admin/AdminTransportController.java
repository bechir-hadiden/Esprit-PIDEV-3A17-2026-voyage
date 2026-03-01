package com.example.demo1.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

public class AdminTransportController {

    @FXML
    private VBox rootContainer;

    @FXML
    private void manageVehicles() {
        loadView("/fxml/vehicule_table_view.fxml");
    }

    @FXML
    private void manageTypes() {
        loadView("/fxml/type_table_view.fxml");
    }

    @FXML
    private void showStats() {
        loadView("/fxml/admin_stats.fxml");
    }

    @FXML
    private void showSmartNotify() {
        loadView("/fxml/smart_notify_monitor.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Find the contentArea stack pane from AdminMain layout
            // We search up the hierarchy or via scene lookup
            StackPane contentArea = (StackPane) rootContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (Exception e) {
            System.err.println("Error loading transport sub-view: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
