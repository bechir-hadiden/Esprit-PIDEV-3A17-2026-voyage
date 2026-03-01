package com.example.demo1.controller.admin;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.example.demo1.entity.Offre;
import com.example.demo1.services.OffreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherOffresController {

    @FXML private TableView<Offre> tableOffres;
    @FXML private TableColumn<Offre, Integer> colId;
    @FXML private TableColumn<Offre, String> colTitre;
    @FXML private TableColumn<Offre, Integer> colRemise;
    @FXML private TableColumn<Offre, String> colStatut;
    @FXML private TableColumn<Offre, Boolean> colODD;
    @FXML private TextField txtRecherche;

    private OffreService os = new OffreService();
    private ObservableList<Offre> masterData = FXCollections.observableArrayList();

    public void initialize() {
        try {
            // 1. Liaison des données (Assure-toi que Offre.java a bien "statut")
            colId.setCellValueFactory(new PropertyValueFactory<>("id_offre"));
            colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
            colRemise.setCellValueFactory(new PropertyValueFactory<>("taux_remise"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
            colODD.setCellValueFactory(new PropertyValueFactory<>("is_local_support"));

            // 2. SUPPRIMER LA BARRE GRISE (Tableau Responsive)
            tableOffres.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // 3. BADGE POUR LE STATUT (Version Robuste)
            colStatut.setCellFactory(column -> new TableCell<Offre, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.trim().isEmpty()) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Label badge = new Label(item.toUpperCase());
                        // Style "Pill" direct pour plus de sécurité pour demain
                        if (item.equalsIgnoreCase("ACTIVE")) {
                            badge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 12;");
                        } else {
                            badge.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; " +
                                    "-fx-background-radius: 20; -fx-padding: 4 12;");
                        }
                        setGraphic(badge);
                    }
                }
            });

            // 4. BADGE POUR L'IMPACT ODD 8
            colODD.setCellFactory(column -> new TableCell<Offre, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        Label badge = new Label(item ? "Soutien Local" : "Standard");
                        if (item) {
                            badge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #166534; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 12;");
                        } else {
                            badge.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; " +
                                    "-fx-background-radius: 20; -fx-padding: 4 12;");
                        }
                        setGraphic(badge);
                    }
                }
            });

            // 5. Alignements et finitions
            colRemise.setStyle("-fx-alignment: CENTER;");
            colId.setStyle("-fx-alignment: CENTER;");

            // 6. Chargement des données
            refreshTable();
            tableOffres.setPlaceholder(new Label("Aucune offre promotionnelle trouvée."));

        } catch (SQLException e) {
            System.err.println("Erreur d'affichage : " + e.getMessage());
        }
    }
    public void refreshTable() throws SQLException {
        masterData.setAll(os.afficher());
        FilteredList<Offre> filteredData = new FilteredList<>(masterData, b -> true);

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(offre -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return offre.getTitre().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Offre> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableOffres.comparatorProperty());
        tableOffres.setItems(sortedData);
    }

    @FXML
    private void handleGererCodes() {
        Offre selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/GestionCodes.fxml"));
                Parent root = loader.load();
                GestionCodesController controller = loader.getController();
                controller.setOffre(selected);
                Stage stage = new Stage();
                stage.setTitle("Gestion des Coupons : " + selected.getTitre());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Sélection requise", "Veuillez sélectionner une offre.");
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AjouterOffres.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Offre - SmartTrip");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifier() {
        Offre selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ModifierOffres.fxml"));
                Parent root = loader.load();
                ModifierOffreController controller = loader.getController();
                controller.setData(selected);
                Stage stage = new Stage();
                stage.setTitle("Modifier l'Offre - SmartTrip");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();
                refreshTable();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Sélection requise", "Veuillez sélectionner une offre à modifier.");
        }
    }

    @FXML
    private void handleSupprimer() {
        Offre selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer l'offre : " + selected.getTitre());
            alert.setContentText("Voulez-vous vraiment supprimer cette offre ?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    os.supprimer(selected.getId_offre());
                    refreshTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            showAlert("Sélection requise", "Veuillez sélectionner une offre à supprimer.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}