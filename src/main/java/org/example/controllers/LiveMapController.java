package org.example.controllers;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.example.entities.BaseVehicule;
import org.example.entities.User;
import org.example.services.VehiculeService;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class LiveMapController {

    @FXML
    private WebView webView;
    @FXML
    private Label statusLabel;

    private WebEngine webEngine;
    private VehiculeService vehiculeService = new VehiculeService();
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        webEngine = webView.getEngine();

        // Load the HTML file
        URL url = getClass().getResource("/html/live_map.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            System.err.println("Could not find live_map.html");
        }

        // Wait for page to load
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                // Bridge Java to Javascript
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaController", this);

                loadVehiclesOnMap();
            }
        });
    }

    private void loadVehiclesOnMap() {
        List<BaseVehicule> vehicules = vehiculeService.listerTous();
        webEngine.executeScript("clearMarkers();");

        for (BaseVehicule v : vehicules) {
            // Escape single quotes in strings for javascript
            String compagnie = v.getCompagnie() != null ? v.getCompagnie().replace("'", "\\'") : "Inconnu";
            String type = v.getType() != null ? v.getType().replace("'", "\\'") : "Inconnu";

            String script = String.format("setVehicleMarker(%d, '%s', '%s', %f, %f, %b);",
                    v.getId(),
                    type,
                    compagnie,
                    v.getLatitude(),
                    v.getLongitude(),
                    v.isDisponible());
            webEngine.executeScript(script);
        }
    }

    @FXML
    private void handleRefresh() {
        loadVehiclesOnMap();
        statusLabel.setText("Carte rafraîchie avec succès.");
        statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-style: italic;");
    }

    @FXML
    private void handleBack() {
        try {
            String targetFxml = "/fxml/user_menu.fxml";
            if (currentUser != null && currentUser.isAdmin()) {
                targetFxml = "/fxml/admin.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
            Stage stage = (Stage) webView.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
