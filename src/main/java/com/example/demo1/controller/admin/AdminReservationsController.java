package com.example.demo1.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.example.entities.Reservation;
import org.example.services.ReservationService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.entities.Transport;
import org.example.entities.User;
import org.example.services.SmartNotifyService;
import org.example.services.TransportService;
import org.example.services.UserService;

public class AdminReservationsController {

    @FXML
    private TableView<Reservation> reservationTable;
    @FXML
    private TableColumn<Reservation, Integer> colId;
    @FXML
    private TableColumn<Reservation, String> colUser;
    @FXML
    private TableColumn<Reservation, String> colTransport;
    @FXML
    private TableColumn<Reservation, String> colDate;
    @FXML
    private TableColumn<Reservation, String> colStatus;
    @FXML
    private TableColumn<Reservation, Void> colActions;

    private final ReservationService reservationService = new ReservationService();
    private final SmartNotifyService notifyService = new SmartNotifyService();
    private final UserService userService = new UserService();
    private final TransportService transportService = new TransportService();
    private final org.example.services.VehiculeService vehiculeService = new org.example.services.VehiculeService();

    @FXML
    public void initialize() {
        setupTable();
        loadReservations();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReservation"));
        colUser.setCellValueFactory(cellData -> new SimpleStringProperty("Utilisateur #" + cellData.getValue().getIdUser()));
        colTransport.setCellValueFactory(new PropertyValueFactory<>("typeTransport"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colDate.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(
                cellData.getValue().getDateReservation().format(formatter)
            );
        });

        setupStatusColumn();
        setupActionsColumn();
    }

    private void setupStatusColumn() {
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    String style = "-fx-padding: 6 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: white;";
                    if (item.equalsIgnoreCase("CONFIRMED")) {
                        style += "-fx-background-color: #059669;";
                    } else if (item.equalsIgnoreCase("PENDING")) {
                        style += "-fx-background-color: #d97706;";
                    } else if (item.equalsIgnoreCase("CANCELLED")) {
                        style += "-fx-background-color: #dc2626;";
                    } else {
                        style += "-fx-background-color: #475569;";
                    }
                    badge.setStyle(style);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Reservation, Void>, TableCell<Reservation, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Reservation, Void> call(final TableColumn<Reservation, Void> param) {
                return new TableCell<>() {
                    private final Button btnConfirm = new Button("✅");
                    private final Button btnCancel = new Button("❌");
                    private final HBox pane = new HBox(10, btnConfirm, btnCancel);

                    {
                        pane.setAlignment(Pos.CENTER);
                        btnConfirm.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 15; -fx-font-size: 14px;");
                        btnCancel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 15; -fx-font-size: 14px;");

                        btnConfirm.setOnAction(event -> {
                            Reservation res = getTableView().getItems().get(getIndex());
                            updateReservationStatus(res, "CONFIRMED");
                        });

                        btnCancel.setOnAction(event -> {
                            Reservation res = getTableView().getItems().get(getIndex());
                            updateReservationStatus(res, "CANCELLED");
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void updateReservationStatus(Reservation res, String newStatus) {
        res.setStatut(newStatus);
        reservationService.modifier(res);
        
        // Notify User
        try {
            User user = userService.getUserById(res.getIdUser());
            
            // Manage vehicle availability
            if ("CANCELLED".equals(newStatus)) {
                vehiculeService.updateDisponibilite(res.getIdVehicule(), true);
            } else if ("CONFIRMED".equals(newStatus)) {
                // Already set to false when created, but ensure consistency
                vehiculeService.updateDisponibilite(res.getIdVehicule(), false);
                
                // Send notifications only on confirmation (as per current logic)
                Transport mockTransport = new Transport();
                mockTransport.setCompagnie(res.getTypeTransport() != null ? res.getTypeTransport() : "Transport");
                notifyService.sendConfirmationEmail(user, mockTransport, res);
                notifyService.sendConfirmationSMS(user, mockTransport, res);
            }
        } catch (Exception e) {
            System.err.println("Error processing status change: " + e.getMessage());
            e.printStackTrace();
        }

        loadReservations();
    }

    private void loadReservations() {
        try {
            List<Reservation> reservations = reservationService.listerToutes();
            reservationTable.setItems(FXCollections.observableArrayList(reservations));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshReservations() {
        loadReservations();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminTransport.fxml"));
            Parent view = loader.load();
            StackPane contentArea = (StackPane) reservationTable.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
