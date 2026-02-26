package com.example.demo1.controller.client;


import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ComingSoonController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label messageLabel;

    private String featureName = "Feature";

    @FXML
    public void initialize() {
        updateLabels();
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
        updateLabels();
    }

    private void updateLabels() {
        if (titleLabel != null) {
            titleLabel.setText(featureName + " - Coming Soon");
        }
        if (messageLabel != null) {
            messageLabel.setText("The " + featureName
                    + " feature is currently under development and will be available in a future update.");
        }
    }
}