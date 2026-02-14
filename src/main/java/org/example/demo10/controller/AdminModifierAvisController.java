package org.example.demo10.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.demo10.MainApp;
import org.example.demo10.model.Avis;
import org.example.demo10.service.AvisService;
import java.time.LocalDate;

public class AdminModifierAvisController {

    @FXML private TextField txtNomClient;
    @FXML private TextField txtEmail;
    @FXML private ChoiceBox<Integer> choiceNote;
    @FXML private TextArea txtCommentaire;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtVoyageId;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;

    private AvisService avisService;
    private int avisId;
    private MainApp mainApp;

    @FXML
    public void initialize() {
        avisService = new AvisService();

        // Initialiser le ChoiceBox des notes
        choiceNote.getItems().addAll(1, 2, 3, 4, 5);

        // Configuration des boutons
        btnModifier.setOnAction(e -> modifierAvis());
        btnAnnuler.setOnAction(e -> annuler());
    }

    public void setAvisId(int id) {
        this.avisId = id;
        chargerAvis();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private void chargerAvis() {
        Avis avis = avisService.getAvisById(avisId);
        if (avis != null) {
            txtNomClient.setText(avis.getNomClient());
            txtEmail.setText(avis.getEmail());
            choiceNote.setValue(avis.getNote());
            txtCommentaire.setText(avis.getCommentaire());
            datePicker.setValue(avis.getDateAvis());
            txtVoyageId.setText(String.valueOf(avis.getVoyageId()));
        }
    }

    private void modifierAvis() {
        try {
            String nom = txtNomClient.getText().trim();
            String email = txtEmail.getText().trim();
            Integer note = choiceNote.getValue();
            String commentaire = txtCommentaire.getText().trim();
            LocalDate date = datePicker.getValue();
            int voyageId = Integer.parseInt(txtVoyageId.getText().trim());

            if (nom.isEmpty() || email.isEmpty() || note == null || commentaire.isEmpty() || date == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs.");
                return;
            }

            boolean success = avisService.modifierAvis(avisId, nom, email, note, commentaire, date, voyageId);

            if (success) {
                showAlert("Succès", "Avis modifié avec succès!");
                closeWindow();
            } else {
                showAlert("Erreur", "Erreur lors de la modification de l'avis.");
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