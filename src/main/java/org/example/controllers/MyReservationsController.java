package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.awt.Desktop;
import org.example.entities.Reservation;
import org.example.entities.User;
import org.example.services.ReservationService;
import org.example.services.SmartNotifyService;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Controller for viewing user's reservations. */
public class MyReservationsController {

    private User currentUser;
    private ReservationService reservationService = new ReservationService();
    private SmartNotifyService notifyService = new SmartNotifyService();
    private org.example.services.PdfService pdfService = new org.example.services.PdfService();

    @FXML
    private TableView<Reservation> reservationTable;
    @FXML
    private TableColumn<Reservation, String> colId;
    @FXML
    private TableColumn<Reservation, String> colType;
    @FXML
    private TableColumn<Reservation, String> colCompagnie;
    @FXML
    private TableColumn<Reservation, String> colNumero;
    @FXML
    private TableColumn<Reservation, String> colDate;
    @FXML
    private TableColumn<Reservation, String> colStatut;
    @FXML
    private TableColumn<Reservation, String> colPrix;
    @FXML
    private TableColumn<Reservation, Void> colActions;
    @FXML
    private Label totalLabel;

    @FXML
    public void initialize() {
        // Setup table columns
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdReservation())));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTransportType()));
        colCompagnie.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTransportCompagnie()));
        colNumero.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTransportNumero()));
        colDate.setCellValueFactory(data -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(data.getValue().getDateReservation().format(formatter));
        });
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));
        colStatut.setCellFactory(column -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");

                    // Standard color mapping for travel statuses
                    switch (item.toUpperCase()) {
                        case "CONFIRMED":
                        case "COMPLETED":
                            badge.getStyleClass().add("badge-success");
                            break;
                        case "PENDING":
                        case "IN_PROGRESS":
                            badge.getStyleClass().add("badge-warning");
                            break;
                        case "CANCELLED":
                            badge.getStyleClass().add("badge-danger");
                            break;
                        default:
                            badge.getStyleClass().add("badge-info");
                    }

                    javafx.scene.layout.StackPane container = new javafx.scene.layout.StackPane(badge);
                    container.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(container);
                }
            }
        });

        colPrix.setCellValueFactory(
                data -> new SimpleStringProperty(String.format("%.2f DT", data.getValue().getTransportPrix())));

        setupActionButtons();
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final Button pdfBtn = new Button();
            private final Button payBtn = new Button("💳 Payer");
            private final HBox container = new HBox(10, pdfBtn, payBtn, editBtn, deleteBtn);

            {
                // Specialized styling for action buttons to make them feel "mini" and
                // integrated

                // Edit (Update Schedule)
                Region editIcon = new Region();
                editIcon.getStyleClass().addAll("icon", "icon-edit");
                editBtn.setGraphic(editIcon);
                editBtn.setTooltip(new Tooltip("Modifier ma réservation"));
                editBtn.getStyleClass().addAll("action-button", "button-secondary");

                // Download Ticket (PDF)
                Region pdfIcon = new Region();
                pdfIcon.getStyleClass().addAll("icon", "icon-pdf");
                pdfBtn.setGraphic(pdfIcon);
                pdfBtn.setTooltip(new Tooltip("Télécharger mon ticket"));
                pdfBtn.getStyleClass().addAll("action-button", "button-primary");
                pdfBtn.setStyle("-fx-background-color: #0ea5e9;");

                // Cancel (Delete)
                Region deleteIcon = new Region();
                deleteIcon.getStyleClass().addAll("icon", "icon-delete");
                deleteBtn.setGraphic(deleteIcon);
                deleteBtn.setTooltip(new Tooltip("Annuler la réservation"));
                deleteBtn.getStyleClass().addAll("action-button", "button-danger");
                deleteBtn.setStyle("-fx-background-color: #ef4444;");

                container.setAlignment(Pos.CENTER);
                container.setPadding(new javafx.geometry.Insets(4));

                editBtn.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
                pdfBtn.setOnAction(event -> handlePdfExport(getTableView().getItems().get(getIndex())));

                // Pay Button
                payBtn.setStyle(
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12;");
                payBtn.setOnMouseEntered(ev -> payBtn.setStyle(
                        "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12;"));
                payBtn.setOnMouseExited(ev -> payBtn.setStyle(
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12;"));
                payBtn.setOnAction(event -> handlePay(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void handlePay(Reservation r) {
        try {
            javafx.scene.Scene scene = reservationTable.getScene();
            javafx.scene.layout.Pane contentArea = (javafx.scene.layout.Pane) scene.lookup("#contentArea");
            if (contentArea == null)
                contentArea = (javafx.scene.layout.Pane) scene.lookup("#contentContainer");

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transport_payment.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
                TransportPaymentController ctrl = loader.getController();
                ctrl.setReservation(r, currentUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible d'ouvrir l'interface de paiement: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleEdit(Reservation r) {
        // Simple dialog to mock date change for now
        TextInputDialog dialog = new TextInputDialog(r.getDateReservation().toString());
        dialog.setTitle("Modifier");
        dialog.setHeaderText("Nouvelle date");
        dialog.setContentText("Format (AAAA-MM-JJTHH:MM) :");

        dialog.showAndWait().ifPresent(newDateStr -> {
            try {
                r.setDateReservation(java.time.LocalDateTime.parse(newDateStr));
                reservationService.modifier(r);
                loadReservations();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Date invalide.");
                alert.show();
            }
        });
    }

    private void handleDelete(Reservation r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cette réservation ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                reservationService.supprimer(r.getIdReservation());
                // Alerte admin par email
                if (currentUser != null) {
                    notifyService.sendCancellationAlertToAdmin(currentUser, r);
                }
                loadReservations();
            }
        });
    }

    private void handlePdfExport(Reservation r) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le ticket PDF");
        fileChooser.setInitialFileName("Reservation_" + r.getIdReservation() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"));

        File file = fileChooser.showSaveDialog(reservationTable.getScene().getWindow());

        if (file != null) {
            boolean success = pdfService.generateReservationPdf(r, currentUser, file.getAbsolutePath());
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("PDF exporté avec succès.");

                // Optionnel: Ouvrir le fichier automatiquement
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur est survenue lors de la génération du PDF.");
                alert.showAndWait();
            }
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadReservations();
    }

    private void loadReservations() {
        if (currentUser != null) {
            List<Reservation> reservations = reservationService.listerParUser(currentUser.getIdUser());
            reservationTable.getItems().clear();
            reservationTable.getItems().addAll(reservations);
            totalLabel.setText(String.valueOf(reservations.size()));
        }
    }

    @FXML
    private void goBack() {
        try {
            javafx.scene.Scene scene = reservationTable.getScene();
            javafx.scene.layout.Pane contentArea = (javafx.scene.layout.Pane) scene.lookup("#contentArea");
            if (contentArea == null)
                contentArea = (javafx.scene.layout.Pane) scene.lookup("#contentContainer");

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_menu.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
                return;
            }
        } catch (Exception e) {
        }

        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/user_menu.fxml");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            MainShellController.getInstance().setCurrentUser(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);
            Stage stage = (Stage) reservationTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
