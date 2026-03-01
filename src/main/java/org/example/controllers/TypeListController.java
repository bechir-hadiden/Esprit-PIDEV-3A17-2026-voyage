package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.TransportType;
import org.example.services.TransportTypeService;

import java.io.IOException;
import java.util.List;

public class TypeListController {

    @FXML
    private FlowPane cardsContainer;

    private TransportTypeService typeService = new TransportTypeService();

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    private void refresh() {
        List<TransportType> list = typeService.lister();
        renderCards(list);
    }

    private void renderCards(List<TransportType> types) {
        cardsContainer.getChildren().clear();
        for (TransportType t : types) {
            cardsContainer.getChildren().add(createTypeCard(t));
        }
    }

    private VBox createTypeCard(TransportType t) {
        VBox card = new VBox(15);
        card.getStyleClass().add("item-card");

        // Header: Image/Icon + Type Name
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane mediaContainer = new StackPane();
        mediaContainer.getStyleClass().add("card-icon-container");

        if (t.getImage() != null && !t.getImage().isEmpty()) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
            imgView.setFitWidth(50);
            imgView.setFitHeight(50);
            imgView.setPreserveRatio(true);

            try {
                String path = "/images/" + t.getImage();
                java.net.URL res = getClass().getResource(path);
                if (res != null) {
                    imgView.setImage(new javafx.scene.image.Image(res.toExternalForm()));
                } else {
                    java.io.File file = new java.io.File(t.getImage());
                    if (file.exists()) {
                        imgView.setImage(new javafx.scene.image.Image(file.toURI().toString()));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading type image: " + t.getImage());
            }
            mediaContainer.getChildren().add(imgView);
        } else {
            Region typeIcon = new Region();
            typeIcon.getStyleClass().addAll("icon", "icon-add");
            typeIcon.setStyle("-fx-background-color: #2563eb;");
            mediaContainer.getChildren().add(typeIcon);
        }

        VBox titleBox = new VBox(2);
        Label lblNom = new Label(t.getNom());
        lblNom.getStyleClass().add("card-title");
        Label lblId = new Label("Catégorie #" + t.getIdType());
        lblId.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(lblNom, lblId);

        header.getChildren().addAll(mediaContainer, titleBox);

        // Price Section
        VBox priceContainer = new VBox(5);
        Label lblPriceLabel = new Label("Prix de départ");
        lblPriceLabel.getStyleClass().add("card-subtitle");

        HBox priceBox = new HBox(5);
        priceBox.setAlignment(Pos.BASELINE_LEFT);
        Label lblPriceValue = new Label(String.format("%.2f", t.getPrixDepart()));
        lblPriceValue.getStyleClass().add("card-price");
        Label lblPriceUnit = new Label("DT");
        lblPriceUnit.getStyleClass().add("card-price-unit");
        priceBox.getChildren().addAll(lblPriceValue, lblPriceUnit);

        priceContainer.getChildren().addAll(lblPriceLabel, priceBox);

        // Actions
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("card-actions");

        Button editBtn = new Button();
        editBtn.getStyleClass().addAll("button-primary", "button-icon");
        Region editIcon = new Region();
        editIcon.getStyleClass().addAll("icon", "icon-edit");
        editBtn.setGraphic(editIcon);
        editBtn.setOnAction(e -> loadForm(t));

        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().addAll("button-danger", "button-icon");
        Region deleteIcon = new Region();
        deleteIcon.getStyleClass().addAll("icon", "icon-delete");
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setOnAction(e -> {
            typeService.supprimer(t.getIdType());
            refresh();
        });

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(header, priceContainer, actions);
        return card;
    }

    @FXML
    private void openAddForm() {
        loadForm(null);
    }

    private void loadForm(TransportType t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/type_form.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            TransportTypeFormController controller = loader.getController();
            controller.setType(t);
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
