package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.example.entities.Notification;
import org.example.services.NotificationService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SmartNotifyMonitorController {

    @FXML
    private TableView<Notification> notificationTable;
    @FXML
    private TableColumn<Notification, String> colId;
    @FXML
    private TableColumn<Notification, String> colDate;
    @FXML
    private TableColumn<Notification, String> colMessage;
    @FXML
    private TableColumn<Notification, String> colType;
    @FXML
    private TableColumn<Notification, Void> colActions;

    private NotificationService notificationService = new NotificationService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colDate.setCellValueFactory(data -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(data.getValue().getDateSent().format(formatter));
        });
        colMessage.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));

        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colType.setCellFactory(column -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if ("CANCELLATION".equals(item)) {
                        badge.getStyleClass().add("badge-danger");
                    } else {
                        badge.getStyleClass().add("badge-info");
                    }
                    javafx.scene.layout.StackPane container = new javafx.scene.layout.StackPane(badge);
                    container.setPadding(new javafx.geometry.Insets(0));
                    setGraphic(container);
                }
            }
        });

        setupActionButtons();
        loadNotifications();
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button();
            {
                deleteBtn.setText("🗑️ Annuler");
                deleteBtn.getStyleClass().addAll("action-button", "button-danger");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-font-size: 11px; -fx-padding: 4 8;");
                deleteBtn.setTooltip(new Tooltip("Supprimer l'alerte"));

                deleteBtn.setOnAction(event -> {
                    Notification n = getTableView().getItems().get(getIndex());
                    handleDelete(n);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    @FXML
    private void loadNotifications() {
        List<Notification> list = notificationService.lister();
        notificationTable.getItems().clear();
        notificationTable.getItems().addAll(list);
    }

    private void handleDelete(Notification n) {
        if (notificationService.supprimer(n.getId())) {
            loadNotifications();
        }
    }

    @FXML
    private void goBack() {
        try {
            StackPane contentArea = (StackPane) notificationTable.getScene().lookup("#contentArea");
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminTransport.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
            } else {
                MainShellController shell = MainShellController.getInstance();
                if (shell != null) {
                    shell.loadView("/fxml/admin.fxml", shell.getBtnDashboard());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
