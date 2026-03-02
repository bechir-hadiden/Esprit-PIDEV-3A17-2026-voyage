package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.Reservation;
import org.example.entities.Transport;
import org.example.entities.User;
import org.example.services.ReservationService;
import org.example.services.SmartNotifyService;
import org.example.services.TransportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Controller for user transport reservation interface. */
public class UserController {

    private User currentUser;

    @FXML
    private FlowPane transportCardsPane;
    @FXML
    private TableView<Reservation> reservationTable;
    @FXML
    private TableColumn<Reservation, String> colResType;
    @FXML
    private TableColumn<Reservation, String> colResCompagnie;
    @FXML
    private TableColumn<Reservation, String> colResNumero;
    @FXML
    private TableColumn<Reservation, String> colResDate;
    @FXML
    private TableColumn<Reservation, String> colResStatut;

    private TransportService transportService = new TransportService();
    private ReservationService reservationService = new ReservationService();
    private SmartNotifyService smartNotifyService = new SmartNotifyService();

    @FXML
    public void initialize() {
        // Setup reservation table columns
        colResType.setCellValueFactory(new PropertyValueFactory<>("transportType"));
        colResCompagnie.setCellValueFactory(new PropertyValueFactory<>("transportCompagnie"));
        colResNumero.setCellValueFactory(new PropertyValueFactory<>("transportNumero"));
        colResDate.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getDateReservation();
            String formatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
        colResStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Load all transports initially
        showAllTransports();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserReservations();
    }

    @FXML
    private void filterByType(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String type = "";

        if (btn.getText().contains("Bus"))
            type = "Bus";
        else if (btn.getText().contains("Taxi"))
            type = "Taxi";
        else if (btn.getText().contains("Voiture"))
            type = "Voiture";

        List<Transport> transports = transportService.listerParType(type);
        displayTransportsAsCards(transports);
    }

    @FXML
    private void showAllTransports() {
        List<Transport> transports = transportService.lister();
        displayTransportsAsCards(transports);
    }

    private void displayTransportsAsCards(List<Transport> transports) {
        transportCardsPane.getChildren().clear();

        for (Transport t : transports) {
            VBox card = createTransportCard(t);
            transportCardsPane.getChildren().add(card);
        }
    }

    private VBox createTransportCard(Transport transport) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("admin-card");
        card.setPrefWidth(220);
        card.setPrefHeight(250);

        // Transport icon based on type
        String icon = getIconForType(transport.getType());
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");

        // Transport details
        Label typeLabel = new Label(transport.getType());
        typeLabel.getStyleClass().add("stats-section-title");

        Label compagnieLabel = new Label(transport.getCompagnie());
        compagnieLabel.getStyleClass().add("stats-section-sub");

        Label numeroLabel = new Label("N° " + transport.getNumero());
        numeroLabel.getStyleClass().add("stats-section-sub");
        numeroLabel.setStyle("-fx-font-size: 11px;");

        Label capaciteLabel = new Label("Capacité: " + transport.getCapacite() + " places");
        capaciteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #10b981; -fx-font-weight: 700;");

        // Reserve button
        Button reserveBtn = new Button("Réserver");
        reserveBtn.getStyleClass().add("admin-button-primary");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        reserveBtn.setOnAction(e -> handleReservation(transport));

        card.getChildren().addAll(iconLabel, typeLabel, compagnieLabel, numeroLabel, capaciteLabel, reserveBtn);
        return card;
    }

    private String getIconForType(String type) {
        switch (type.toLowerCase()) {
            case "bus":
                return "🚌";
            case "taxi":
                return "🚕";
            case "voiture":
                return "🚗";
            case "train":
                return "🚊";
            case "avion":
                return "✈️";
            default:
                return "🚗";
        }
    }

    private void handleReservation(Transport transport) {
        if (currentUser == null) {
            showAlert("Erreur", "Utilisateur non connecté");
            return;
        }

        Reservation reservation = new Reservation();
        reservation.setIdUser(currentUser.getIdUser());
        reservation.setIdTransport(transport.getIdTransport());
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut("CONFIRMED");

        reservationService.ajouter(reservation);
        loadUserReservations();

        // Trigger SmartNotify Confirmation
        smartNotifyService.sendConfirmationSMS(currentUser, transport, reservation);

        showAlert("Succès", "Réservation effectuée avec succès!\n\n" +
                transport.getType() + " - " + transport.getCompagnie());

        // Visual SMS feedback
        showSMSAlert(currentUser, transport, reservation);
    }

    private void showSMSAlert(User user, Transport transport, Reservation reservation) {
        String dateStr = reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📱 SmartNotify - Nouveau Message");
        alert.setHeaderText("SMS de confirmation envoyé à " + user.getTelephone());
        alert.setContentText("Bonjour " + user.getUsername() + ",\n" +
                "Votre réservation de " + transport.getType() + " (" + transport.getCompagnie() + ") " +
                "pour le " + dateStr + " est confirmée ! ✅");

        // Add a mobile icon graphic
        Label mobileIcon = new Label("📱");
        mobileIcon.setStyle("-fx-font-size: 40px;");
        alert.setGraphic(mobileIcon);

        alert.show();
    }

    private void loadUserReservations() {
        if (currentUser != null) {
            List<Reservation> reservations = reservationService.listerParUser(currentUser.getIdUser());
            reservationTable.getItems().clear();
            reservationTable.getItems().addAll(reservations);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
            Stage stage = (Stage) transportCardsPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
