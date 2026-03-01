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
        VBox card = new VBox(15);
        card.getStyleClass().add("item-card");

        // Header: Icon + Company Name
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Region typeIcon = new Region();
        typeIcon.getStyleClass().addAll("icon", "icon-add"); // Placeholder icon
        typeIcon.setStyle("-fx-background-color: #2563eb;");

        StackPane iconCircle = new StackPane(typeIcon);
        iconCircle.getStyleClass().add("card-icon-container");

        VBox titleBox = new VBox(2);
        Label lblCompagnie = new Label(v.getCompagnie());
        lblCompagnie.getStyleClass().add("card-title");
        Label lblType = new Label(v.getType());
        lblType.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(lblCompagnie, lblType);

        header.getChildren().addAll(iconCircle, titleBox);

        // Content: Info Badges
        FlowPane infoContainer = new FlowPane(10, 10);

        Label badgeId = new Label("#ID " + v.getId());
        badgeId.getStyleClass().add("card-badge");

        Label badgeNum = new Label("Num: " + v.getNumero());
        badgeNum.getStyleClass().add("card-badge");

        Label badgeCap = new Label(v.getCapacite() + " Places");
        badgeCap.getStyleClass().add("card-badge");

        Label badgeStatus = new Label(v.isDisponible() ? "Disponible" : "Indisponible");
        badgeStatus.setStyle("-fx-background-color: " + (v.isDisponible() ? "#10b981" : "#ef4444") + "; " +
                "-fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label badgeVille = new Label(
                v.getVille() != null && !v.getVille().isEmpty() ? v.getVille() : "Ville non spécifiée");
        badgeVille.setStyle(
                "-fx-background-color: #64748b; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");

        infoContainer.getChildren().addAll(badgeId, badgeNum, badgeCap, badgeStatus, badgeVille);

        // Price Section
        HBox priceBox = new HBox(5);
        priceBox.setAlignment(Pos.BASELINE_LEFT);
        Label lblPriceValue = new Label(String.format("%.2f", v.getPrix()));
        lblPriceValue.getStyleClass().add("card-price");
        Label lblPriceUnit = new Label("DT / trajet");
        lblPriceUnit.getStyleClass().add("card-price-unit");
        priceBox.getChildren().addAll(lblPriceValue, lblPriceUnit);

        // Actions
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("card-actions");

        Button editBtn = new Button();
        editBtn.getStyleClass().addAll("button-primary", "button-icon");
        Region editIcon = new Region();
        editIcon.getStyleClass().addAll("icon", "icon-edit");
        editBtn.setGraphic(editIcon);
        editBtn.setOnAction(e -> loadForm(v));

        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().addAll("button-danger", "button-icon");
        Region deleteIcon = new Region();
        deleteIcon.getStyleClass().addAll("icon", "icon-delete");
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setOnAction(e -> {
            vehiculeService.supprimer(v.getType(), v.getId());
            rafraichir();
        });

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(header, infoContainer, priceBox, actions);
        return card;
    }

    @FXML
    private void ajouterVehicule() {
        loadForm(null);
    }

    private void loadForm(BaseVehicule v) {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vehicule_form.fxml"));
                Parent view = loader.load();
                VehiculeFormController controller = loader.getController();
                controller.setVehicule(v);
                shell.getContentArea().getChildren().setAll(view);
                shell.updateActiveButton(shell.getBtnFleet());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goBack() {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/admin.fxml", shell.getBtnDashboard());
        }
    }
}
