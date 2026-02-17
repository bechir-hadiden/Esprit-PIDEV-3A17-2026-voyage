package com.example.demo1.controller;

import com.example.demo1.entity.Vol;
import com.example.demo1.services.AmadeusService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class VolsController {

    @FXML private TextField tfOrigine;
    @FXML private TextField tfDestination;
    @FXML private DatePicker dpDepart;
    @FXML private TextField tfAdultes;
    @FXML private TableView<Vol> tableVols;
    @FXML private TableColumn<Vol, String> colCompagnie;
    @FXML private TableColumn<Vol, String> colTrajet;
    @FXML private TableColumn<Vol, String> colDate;
    @FXML private TableColumn<Vol, String> colHoraires;
    @FXML private TableColumn<Vol, String> colDuree;
    @FXML private TableColumn<Vol, Integer> colEscales;
    @FXML private TableColumn<Vol, Double> colPrix;
    private AmadeusService amadeusService;  // ⬅️ DÉCLARER

    @FXML
    public void initialize() {
        colCompagnie.setCellValueFactory(new PropertyValueFactory<>("compagnie"));
        colTrajet.setCellValueFactory(new PropertyValueFactory<>("trajet"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        colHoraires.setCellValueFactory(new PropertyValueFactory<>("horaires"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colEscales.setCellValueFactory(new PropertyValueFactory<>("escales"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));

        colPrix.setCellFactory(col -> new TableCell<Vol, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    Vol vol = getTableView().getItems().get(getIndex());
                    setText(String.format("%.2f %s", price, vol.getDevise()));
                }
            }
        });
    }



    @FXML
    private void rechercherVols() {
        String origine = tfOrigine.getText().trim().toUpperCase();
        String destination = tfDestination.getText().trim().toUpperCase();

        if (origine.isEmpty() || destination.isEmpty() || dpDepart.getValue() == null) {
            afficherAlerte("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.ERROR);
            return;
        }

        int adultes;
        try {
            adultes = Integer.parseInt(tfAdultes.getText().trim());
            if (adultes < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "Nombre d'adultes invalide", Alert.AlertType.ERROR);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateDepart = dpDepart.getValue().format(formatter);

        tableVols.setItems(FXCollections.observableArrayList());

        Task<List<Vol>> task = new Task<>() {
            @Override
            protected List<Vol> call() {
                return amadeusService.rechercherVols(origine, destination, dateDepart, adultes);
            }
        };

        task.setOnSucceeded(e -> {
            List<Vol> vols = task.getValue();
            tableVols.setItems(FXCollections.observableArrayList(vols));
            if (vols.isEmpty()) {
                afficherAlerte("Info", "Aucun vol trouvé", Alert.AlertType.INFORMATION);
            }
        });

        task.setOnFailed(e -> {
            afficherAlerte("Erreur", "Erreur lors de la recherche", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void preRemplirDestination(String codeIATA) {
        if (tfDestination != null) {
            tfDestination.setText(codeIATA);
        }

        if (tfOrigine != null && tfOrigine.getText().isEmpty()) {
            tfOrigine.setText("TUN"); // Origine par défaut depuis Tunis
        }

        if (dpDepart != null && dpDepart.getValue() == null) {
            // Date par défaut : dans 30 jours
            dpDepart.setValue(java.time.LocalDate.now().plusDays(30));
        }

        if (tfAdultes != null && tfAdultes.getText().isEmpty()) {
            tfAdultes.setText("1");
        }

        System.out.println("✅ Formulaire pré-rempli pour destination: " + codeIATA);
    }


}