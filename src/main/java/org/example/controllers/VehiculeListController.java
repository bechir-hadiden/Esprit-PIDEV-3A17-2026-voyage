package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.entities.BaseVehicule;
import org.example.services.VehiculeService;
import org.example.services.TransportTypeService;
import java.io.IOException;
import java.util.List;

public class VehiculeListController {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private ComboBox<String> filterTypeComboBox;

    @FXML
    private ComboBox<String> sortComboBox;

    private VehiculeService vehiculeService = new VehiculeService();
    private TransportTypeService typeService = new TransportTypeService();

    @FXML
    public void initialize() {
        populateFilterOptions();
        populateSortOptions();
        rafraichir();
    }

    private void populateFilterOptions() {
        List<String> types = typeService.listerNoms();
        ObservableList<String> options = FXCollections.observableArrayList("Tous les types");
        options.addAll(types);
        filterTypeComboBox.setItems(options);
        filterTypeComboBox.getSelectionModel().select(0);
    }

    private void populateSortOptions() {
        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList(
                    "Défaut",
                    "Prix (Croissant)",
                    "Prix (Décroissant)",
                    "Capacité",
                    "Compagnie"));
            sortComboBox.getSelectionModel().select(0);
        }
    }

    public void setFilterItems(ObservableList<String> items) {
        filterTypeComboBox.setItems(items);
        filterTypeComboBox.getSelectionModel().select(0);
    }

    @FXML
    private void rafraichir() {
        String typeFilter = filterTypeComboBox.getSelectionModel().getSelectedItem();
        List<BaseVehicule> list;

        if (typeFilter == null || typeFilter.equals("Tous les types")) {
            list = vehiculeService.listerTous();
        } else {
            list = vehiculeService.listerParType(typeFilter);
        }

        // Apply Sorting
        String sortOption = sortComboBox.getSelectionModel().getSelectedItem();
        if (sortOption != null) {
            switch (sortOption) {
                case "Prix (Croissant)":
                    list.sort((v1, v2) -> Double.compare(v1.getPrix(), v2.getPrix()));
                    break;
                case "Prix (Décroissant)":
                    list.sort((v1, v2) -> Double.compare(v2.getPrix(), v1.getPrix()));
                    break;
                case "Capacité":
                    list.sort((v1, v2) -> Integer.compare(v2.getCapacite(), v1.getCapacite())); // Higher capacity first
                    break;
                case "Compagnie":
                    list.sort((v1, v2) -> v1.getCompagnie().compareToIgnoreCase(v2.getCompagnie()));
                    break;
            }
        }

        renderCards(list);
    }

    @FXML
    private void filtrer() {
        rafraichir();
    }

    private void renderCards(List<BaseVehicule> vehicules) {
        cardsContainer.getChildren().clear();
        for (BaseVehicule v : vehicules) {
            cardsContainer.getChildren().add(createVehiculeCard(v));
        }
    }

    private VBox createVehiculeCard(BaseVehicule v) {
        VBox card = new VBox(0);
        card.getStyleClass().add("admin-card");
        card.setStyle("-fx-padding: 0;");
        card.setPrefWidth(320);

        // ── Card Body ──────────────────────────────────────────────────────
        VBox body = new VBox(16);
        body.setStyle("-fx-padding: 24;");

        // Header: Type badge + Company
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Type colour
        String colour = switch (v.getType() != null ? v.getType().toLowerCase() : "") {
            case "bus" -> "#3b82f6";
            case "taxi" -> "#f59e0b";
            case "voiture" -> "#10b981";
            case "scooter" -> "#8b5cf6";
            default -> "#64748b";
        };
        String typeEmoji = switch (v.getType() != null ? v.getType().toLowerCase() : "") {
            case "bus" -> "🚌";
            case "taxi" -> "🚕";
            case "voiture" -> "🚗";
            case "scooter" -> "🛵";
            default -> "🚐";
        };

        Label typeBadge = new Label(typeEmoji + "  " + (v.getType() != null ? v.getType() : "—"));
        typeBadge.setStyle("-fx-background-color: " + colour + "22; -fx-text-fill: " + colour + "; " +
                "-fx-padding: 5 12; -fx-background-radius: 30; -fx-font-weight: bold; -fx-font-size: 12px;");

        header.getChildren().add(typeBadge);

        // Company & Number
        Label lblCompagnie = new Label(v.getCompagnie());
        lblCompagnie.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");

        Label lblNum = new Label("N° " + v.getNumero());
        lblNum.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        // Info row ──────────────────────────────────────────────
        HBox infoRow = new HBox(8);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.setStyle("-fx-flex-wrap: wrap;");

        // Disponibility pill
        boolean dispo = v.isDisponible();
        Label statusPill = new Label(dispo ? "✔  Disponible" : "✖  Indisponible");
        statusPill.setStyle("-fx-background-color: " + (dispo ? "#d1fae5" : "#fee2e2") + "; " +
                "-fx-text-fill: " + (dispo ? "#065f46" : "#991b1b") + "; " +
                "-fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label capPill = new Label("👥 " + v.getCapacite() + " places");
        capPill.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                "-fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;");

        Label villePill = new Label("📍 " + (v.getVille() != null && !v.getVille().isEmpty() ? v.getVille() : "N/A"));
        villePill.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                "-fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;");

        infoRow.getChildren().addAll(statusPill, capPill, villePill);

        // Price
        HBox priceRow = new HBox(6);
        priceRow.setAlignment(Pos.BASELINE_LEFT);
        Label priceVal = new Label(String.format("%.2f DT", v.getPrix()));
        priceVal.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label priceUnit = new Label("/ trajet");
        priceUnit.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");
        priceRow.getChildren().addAll(priceVal, priceUnit);

        body.getChildren().addAll(header, lblCompagnie, lblNum, infoRow, priceRow);

        // ── Action Bar ────────────────────────────────────────────────────
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER);
        actionBar.setStyle(
                "-fx-padding: 16 24; -fx-background-color: #f8fafc; -fx-background-radius: 0 0 14 14; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Button editBtn = new Button();
        javafx.scene.layout.Region editIcon = new javafx.scene.layout.Region();
        editIcon.getStyleClass().addAll("icon", "icon-edit");
        editBtn.setGraphic(editIcon);
        editBtn.setTooltip(new Tooltip("Modifier ce véhicule"));
        editBtn.getStyleClass().add("admin-button-secondary");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(editBtn, javafx.scene.layout.Priority.ALWAYS);
        editBtn.setOnAction(e -> loadForm(v));

        Button deleteBtn = new Button();
        javafx.scene.layout.Region deleteIcon = new javafx.scene.layout.Region();
        deleteIcon.getStyleClass().addAll("icon", "icon-delete");
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setTooltip(new Tooltip("Supprimer ce véhicule"));
        deleteBtn.getStyleClass().add("admin-button-danger");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteBtn, javafx.scene.layout.Priority.ALWAYS);
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment supprimer " + v.getCompagnie() + " (" + v.getNumero() + ") ?");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    vehiculeService.supprimer(v.getType(), v.getId());
                    rafraichir();
                }
            });
        });

        actionBar.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(body, actionBar);
        return card;
    }

    @FXML
    private void ajouterVehicule() {
        loadForm(null);
    }

    private void loadForm(BaseVehicule v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vehicule_form.fxml"));
            Parent view = loader.load();
            VehiculeFormController controller = loader.getController();
            controller.setVehicule(v);

            javafx.scene.layout.Pane contentArea = (javafx.scene.layout.Pane) cardsContainer.getScene()
                    .lookup("#contentContainer");
            if (contentArea == null)
                contentArea = (javafx.scene.layout.Pane) cardsContainer.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            } else {
                MainShellController shell = MainShellController.getInstance();
                if (shell != null) {
                    shell.getContentArea().getChildren().setAll(view);
                    shell.updateActiveButton(shell.getBtnFleet());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            javafx.scene.layout.Pane contentArea = (javafx.scene.layout.Pane) cardsContainer.getScene()
                    .lookup("#contentContainer");
            if (contentArea == null)
                contentArea = (javafx.scene.layout.Pane) cardsContainer.getScene().lookup("#contentArea");

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminTransport.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
            } else {
                MainShellController shell = MainShellController.getInstance();
                if (shell != null) {
                    shell.loadView("/fxml/admin/AdminTransport.fxml");
                    shell.updateActiveButton(shell.getBtnCategories());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
