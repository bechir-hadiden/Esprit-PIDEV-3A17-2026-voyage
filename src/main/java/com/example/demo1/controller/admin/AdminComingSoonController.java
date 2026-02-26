package com.example.demo1.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminComingSoonController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label messageLabel;

    public void setSectionName(String sectionName) {
        titleLabel.setText(sectionName + " - Coming Soon");
        messageLabel.setText("The " + sectionName
                + " management feature is under development and will be available in a future update.");
    }
}
