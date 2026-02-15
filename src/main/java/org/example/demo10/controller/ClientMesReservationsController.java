package org.example.demo10.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.demo10.model.Reservation;
import org.example.demo10.model.User;
import org.example.demo10.service.ReservationService;
import org.example.demo10.service.VoyageService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ClientMesReservationsController {

    @FXML private TableView<Reservation> tableViewReservations;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, String> colDestination;
    @FXML private TableColumn<Reservation, LocalDate> colDate;
    @FXML private TableColumn<Reservation, Integer> colPersonnes;
    @FXML private TableColumn<Reservation, String> colStatut;
    @FXML private TableColumn<Reservation, String> colCommentaire;

    @FXML private Label lblTotalReservations;
    @FXML private Label lblEnAttente;
    @FXML private Label lblConfirmees;
    @FXML private Label lblAnnulees;

    private ReservationService reservationService;
    private VoyageService voyageService;
    private ObservableList<Reservation> reservationList;
    private User utilisateurConnecte;

    @FXML
    public void initialize() {
        reservationService = new ReservationService();
        voyageService = new VoyageService();
        reservationList = FXCollections.observableArrayList();

        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDestination.setCellValueFactory(cellData -> {
            int voyageId = cellData.getValue().getVoyageId();
            String destination = voyageService.getVoyageById(voyageId) != null
                    ? voyageService.getVoyageById(voyageId).getDestination()
                    : "Voyage #" + voyageId;
            return new SimpleStringProperty(destination);
        });
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));
        colPersonnes.setCellValueFactory(new PropertyValueFactory<>("nombrePersonnes"));
        colStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatutAvecCouleur()));
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

        // Formatage de la colonne date
        colDate.setCellFactory(column -> new TableCell<Reservation, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Coloration du statut
        colStatut.setCellFactory(column -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    if (statut.contains("Confirmée")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (statut.contains("Annulée")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tableViewReservations.setItems(reservationList);
    }

    public void setUtilisateurConnecte(User user) {
        this.utilisateurConnecte = user;
        chargerReservations();
    }

    private void chargerReservations() {
        if (utilisateurConnecte == null) return;

        reservationList.clear();
        reservationList.addAll(reservationService.getReservationsByClient(utilisateurConnecte.getEmail()));

        // Mettre à jour les statistiques
        long total = reservationList.size();
        long enAttente = reservationList.stream().filter(r -> "en_attente".equals(r.getStatut())).count();
        long confirmees = reservationList.stream().filter(r -> "confirmée".equals(r.getStatut())).count();
        long annulees = reservationList.stream().filter(r -> "annulée".equals(r.getStatut())).count();

        lblTotalReservations.setText(String.valueOf(total));
        lblEnAttente.setText(String.valueOf(enAttente));
        lblConfirmees.setText(String.valueOf(confirmees));
        lblAnnulees.setText(String.valueOf(annulees));
    }

    @FXML
    private void annulerReservation() {
        Reservation selected = tableViewReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez une réservation.");
            return;
        }

        if ("annulée".equals(selected.getStatut())) {
            showAlert("Information", "Cette réservation est déjà annulée.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler la réservation #" + selected.getId());
        confirm.setContentText("Voulez-vous vraiment annuler cette réservation ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean success = reservationService.changerStatut(selected.getId(), "annulée");
            if (success) {
                showAlert("Succès", "Réservation annulée.");
                chargerReservations();
            } else {
                showAlert("Erreur", "Échec de l'annulation.");
            }
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