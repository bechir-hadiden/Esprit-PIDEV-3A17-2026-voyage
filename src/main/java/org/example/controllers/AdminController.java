package org.example.controllers;

import javafx.fxml.FXML;

public class AdminController {

    @FXML
    public void initialize() {
    }

    @FXML
    private void ouvrirListe() {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/vehicule_table_view.fxml", shell.getBtnFleet());
        }
    }

    @FXML
    private void ouvrirListeTypes() {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/type_table_view.fxml", shell.getBtnCategories());
        }
    }

    @FXML
    private void ouvrirStats() {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/admin_stats.fxml", shell.getBtnStats());
        }
    }

    @FXML
    private void handleMonitoringEmail() {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/smart_notify_monitor.fxml", shell.getBtnNotify());
        }
    }
}
