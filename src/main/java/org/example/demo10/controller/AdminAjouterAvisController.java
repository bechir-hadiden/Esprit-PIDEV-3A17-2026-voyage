package org.example.demo10.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.demo10.MainApp;
import org.example.demo10.service.AvisService;

public class AdminAjouterAvisController {

    @FXML private TextField txtNomClient;
    @FXML private TextField txtEmail;
    @FXML private ChoiceBox<Integer> choiceNote;
    @FXML private TextArea txtCommentaire;
    @FXML private TextField txtVoyageId;
    @FXML private Button btnAjouter;
    @FXML private Button btnAnnuler;

    private AvisService avisService;
    private MainApp mainApp;

    @FXML
    public void initialize() {
        avisService = new AvisService();

        // Initialiser le ChoiceBox
        choiceNote.getItems().addAll(1, 2, 3, 4, 5);

        // Configuration des boutons
        btnAjouter.setOnAction(e -> ajouterAvis());
        btnAnnuler.setOnAction(e -> annuler());
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private void ajouterAvis() {
        try {
            String nom = txtNomClient.getText().trim();
            String email = txtEmail.getText().trim();
            Integer note = choiceNote.getValue();
            String commentaire = txtCommentaire.getText().trim();
            int voyageId = Integer.parseInt(txtVoyageId.getText().trim());

            if (nom.isEmpty() || email.isEmpty() || note == null || commentaire.isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs.");
                return;
            }

            boolean success = avisService.ajouterAvisClient(nom, email, note, commentaire, voyageId);

            if (success) {
                showAlert("Succès", "Avis ajouté avec succès!");
                closeWindow();
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout de l'avis.");
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un ID de voyage valide.");
        }
    }

    private void annuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}