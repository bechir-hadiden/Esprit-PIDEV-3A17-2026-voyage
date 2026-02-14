package org.example.demo10.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane; // AJOUT DE CET IMPORT
import javafx.stage.Stage;
import org.example.demo10.model.Avis;
import org.example.demo10.service.AvisService;
import java.io.IOException;
import java.time.LocalDate;

public class AdminAvisController {

    @FXML private TableView<Avis> tableViewAvis;
    @FXML private TableColumn<Avis, Integer> colId;
    @FXML private TableColumn<Avis, String> colNom;
    @FXML private TableColumn<Avis, String> colEmail;
    @FXML private TableColumn<Avis, Integer> colNote;
    @FXML private TableColumn<Avis, String> colCommentaire;
    @FXML private TableColumn<Avis, LocalDate> colDate;
    @FXML private TableColumn<Avis, Integer> colVoyageId;

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboCritere;
    @FXML private Button btnRechercher;
    @FXML private Button btnResetRecherche;

    @FXML private Button btnSupprimer;
    @FXML private Button btnModifier;
    @FXML private Button btnAjouter;
    @FXML private Button btnActualiser;
    @FXML private Button btnStatistiques;

    private AvisService avisService;
    private ObservableList<Avis> avisList;
    private ObservableList<Avis> avisListFiltree;

    @FXML
    public void initialize() {
        avisService = new AvisService();
        avisList = FXCollections.observableArrayList();
        avisListFiltree = FXCollections.observableArrayList();

        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateAvis"));
        colVoyageId.setCellValueFactory(new PropertyValueFactory<>("voyageId"));

        // Configuration du ComboBox de recherche
        comboCritere.setItems(FXCollections.observableArrayList(
                "Tout", "Nom", "Email", "Commentaire", "Note", "Voyage ID"
        ));
        comboCritere.setValue("Tout");

        tableViewAvis.setItems(avisListFiltree);

        // Charger les données
        chargerAvis();

        // Configuration des boutons
        btnAjouter.setOnAction(e -> ouvrirAjouterAvis());
        btnModifier.setOnAction(e -> modifierAvis());
        btnSupprimer.setOnAction(e -> supprimerAvis());
        btnActualiser.setOnAction(e -> chargerAvis());
        btnRechercher.setOnAction(e -> rechercherAvis());
        btnResetRecherche.setOnAction(e -> resetRecherche());
        btnStatistiques.setOnAction(e -> ouvrirStatistiques());
    }

    private void chargerAvis() {
        avisList.clear();
        avisList.addAll(avisService.getAllAvis());
        avisListFiltree.setAll(avisList);
    }

    private void rechercherAvis() {
        String recherche = txtRecherche.getText().trim();
        String critere = comboCritere.getValue();

        if (recherche.isEmpty()) {
            avisListFiltree.setAll(avisList);
            return;
        }

        avisListFiltree.clear();
        avisListFiltree.addAll(avisService.rechercherAvisAdmin(recherche, critere));

        if (avisListFiltree.isEmpty()) {
            showAlert("Information", "Aucun avis trouvé pour votre recherche.");
        }
    }

    private void resetRecherche() {
        txtRecherche.clear();
        comboCritere.setValue("Tout");
        avisListFiltree.setAll(avisList);
    }

    private void ouvrirAjouterAvis() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo10/admin-ajouter-avis.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Avis");
            stage.setScene(new Scene(root, 500, 400));
            stage.showAndWait();
            chargerAvis();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface d'ajout: " + e.getMessage());
        }
    }

    private void modifierAvis() {
        Avis selected = tableViewAvis.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Créer une boîte de dialogue simple pour la modification
            Dialog<Avis> dialog = new Dialog<>();
            dialog.setTitle("Modifier Avis #" + selected.getId());
            dialog.setHeaderText("Modifier l'avis de " + selected.getNomClient());

            // Créer les champs du formulaire
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            TextField nomField = new TextField(selected.getNomClient());
            TextField emailField = new TextField(selected.getEmail());
            TextField noteField = new TextField(String.valueOf(selected.getNote()));
            TextArea commentaireField = new TextArea(selected.getCommentaire());
            commentaireField.setPrefRowCount(3);
            commentaireField.setPrefWidth(300);
            TextField voyageIdField = new TextField(String.valueOf(selected.getVoyageId()));
            DatePicker datePicker = new DatePicker(selected.getDateAvis());

            grid.add(new Label("Nom:"), 0, 0);
            grid.add(nomField, 1, 0);
            grid.add(new Label("Email:"), 0, 1);
            grid.add(emailField, 1, 1);
            grid.add(new Label("Note (1-5):"), 0, 2);
            grid.add(noteField, 1, 2);
            grid.add(new Label("Commentaire:"), 0, 3);
            grid.add(commentaireField, 1, 3);
            grid.add(new Label("Date:"), 0, 4);
            grid.add(datePicker, 1, 4);
            grid.add(new Label("Voyage ID:"), 0, 5);
            grid.add(voyageIdField, 1, 5);

            dialog.getDialogPane().setContent(grid);

            // Ajouter les boutons
            ButtonType modifierButton = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(modifierButton, ButtonType.CANCEL);

            // Convertir le résultat
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == modifierButton) {
                    try {
                        selected.setNomClient(nomField.getText());
                        selected.setEmail(emailField.getText());
                        selected.setNote(Integer.parseInt(noteField.getText()));
                        selected.setCommentaire(commentaireField.getText());
                        selected.setDateAvis(datePicker.getValue());
                        selected.setVoyageId(Integer.parseInt(voyageIdField.getText()));
                        return selected;
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "Veuillez entrer des valeurs valides.");
                        return null;
                    }
                }
                return null;
            });

            // Afficher et traiter le résultat
            dialog.showAndWait().ifPresent(modifiedAvis -> {
                boolean success = avisService.modifierAvis(modifiedAvis);
                if (success) {
                    showAlert("Succès", "Avis modifié avec succès!");
                    chargerAvis();
                } else {
                    showAlert("Erreur", "Erreur lors de la modification.");
                }
            });
        } else {
            showAlert("Attention", "Veuillez sélectionner un avis à modifier.");
        }
    }

    private void supprimerAvis() {
        Avis selected = tableViewAvis.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer l'avis");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer l'avis de " + selected.getNomClient() + " ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                boolean success = avisService.supprimerAvis(selected.getId());
                if (success) {
                    showAlert("Succès", "Avis supprimé avec succès!");
                    chargerAvis();
                } else {
                    showAlert("Erreur", "Erreur lors de la suppression.");
                }
            }
        } else {
            showAlert("Attention", "Veuillez sélectionner un avis à supprimer.");
        }
    }

    private void ouvrirStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo10/admin-statistiques.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Statistiques - Administration");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les statistiques.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}