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
        VBox card = new VBox(0);
        card.getStyleClass().add("admin-card");
        card.setStyle("-fx-padding: 0;"); // Reset padding to allow edge-to-edge footer
        card.setPrefWidth(320);

        // ── Card Body ────────────────────────────────────────────────
        VBox body = new VBox(16);
        body.setStyle("-fx-padding: 24;");

        // Emoji & colour per type name
        String nomLc = t.getNom() != null ? t.getNom().toLowerCase() : "";
        String typeEmoji = nomLc.contains("bus") ? "🚌"
                : nomLc.contains("taxi") ? "🚕"
                        : nomLc.contains("voiture") ? "🚗"
                                : nomLc.contains("scooter") ? "🛵"
                                        : "🚐";
        String colour = nomLc.contains("bus") ? "#3b82f6"
                : nomLc.contains("taxi") ? "#f59e0b"
                        : nomLc.contains("voiture") ? "#10b981"
                                : nomLc.contains("scooter") ? "#8b5cf6"
                                        : "#64748b";

        // Type badge
        HBox badgeRow = new HBox();
        badgeRow.setAlignment(Pos.CENTER_LEFT);
        Label typeBadge = new Label(typeEmoji + "  " + t.getNom());
        typeBadge.setStyle("-fx-background-color: " + colour + "22; -fx-text-fill: " + colour + "; " +
                "-fx-padding: 5 14; -fx-background-radius: 30; -fx-font-weight: bold; -fx-font-size: 13px;");
        badgeRow.getChildren().add(typeBadge);

        // Category ID label
        Label lblId = new Label("Catégorie #" + t.getIdType());
        lblId.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        // Image preview (if available)
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
            imgView.setFitWidth(320); // Spans full card width
            imgView.setFitHeight(140);
            imgView.setPreserveRatio(true);

            javafx.scene.layout.StackPane imgContainer = new javafx.scene.layout.StackPane(imgView);
            imgContainer.setPrefSize(320, 140);
            imgContainer.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 10 10 0 0;");
            // Remove padding from card body for the image to truly go edge-to-edge

            try {
                String path = "/images/" + t.getImage();
                java.net.URL res = getClass().getResource(path);
                if (res != null) {
                    imgView.setImage(new javafx.scene.image.Image(res.toExternalForm()));
                } else {
                    java.io.File file = new java.io.File(t.getImage());
                    if (file.exists())
                        imgView.setImage(new javafx.scene.image.Image(file.toURI().toString()));
                }
            } catch (Exception ex) {
                /* silently skip */ }

            // Notice: the badge and title should probably go *below* the image now
            card.getChildren().add(imgContainer); // Add image container directly to card
            body.getChildren().addAll(badgeRow, lblId); // Add badge and label to body
        } else {
            body.getChildren().addAll(badgeRow, lblId);
        }

        // Price
        HBox priceRow = new HBox(6);
        priceRow.setAlignment(Pos.BASELINE_LEFT);
        Label priceVal = new Label(String.format("%.2f DT", t.getPrixDepart()));
        priceVal.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label priceUnit = new Label("de départ");
        priceUnit.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        priceRow.getChildren().addAll(priceVal, priceUnit);
        body.getChildren().add(priceRow);

        // ── Action Bar ────────────────────────────────────────────────
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER);
        actionBar.setStyle(
                "-fx-padding: 16 24; -fx-background-color: #f8fafc; -fx-background-radius: 0 0 14 14; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Button editBtn = new Button();
        javafx.scene.layout.Region editIcon = new javafx.scene.layout.Region();
        editIcon.getStyleClass().addAll("icon", "icon-edit");
        editBtn.setGraphic(editIcon);
        editBtn.setTooltip(new Tooltip("Modifier cette catégorie"));
        editBtn.getStyleClass().add("admin-button-secondary");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(editBtn, javafx.scene.layout.Priority.ALWAYS);
        editBtn.setOnAction(e -> loadForm(t));

        Button deleteBtn = new Button();
        javafx.scene.layout.Region deleteIcon = new javafx.scene.layout.Region();
        deleteIcon.getStyleClass().addAll("icon", "icon-delete");
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setTooltip(new Tooltip("Supprimer cette catégorie"));
        deleteBtn.getStyleClass().add("admin-button-danger");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteBtn, javafx.scene.layout.Priority.ALWAYS);
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous vraiment supprimer la catégorie \"" + t.getNom() + "\" ?");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    typeService.supprimer(t.getIdType());
                    refresh();
                }
            });
        });

        actionBar.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(body, actionBar);
        return card;
    }

    @FXML
    private void openAddForm() {
        loadForm(null);
    }

    private void loadForm(TransportType t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/type_form.fxml"));
            javafx.scene.Parent view = loader.load();
            TransportTypeFormController controller = loader.getController();
            controller.setType(t);

            javafx.scene.layout.Pane contentArea = (javafx.scene.layout.Pane) cardsContainer.getScene()
                    .lookup("#contentContainer");
            if (contentArea == null)
                contentArea = (javafx.scene.layout.Pane) cardsContainer.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            } else {
                // Fallback: standalone window (old-style shell)
                MainShellController shell = MainShellController.getInstance();
                if (shell != null) {
                    shell.getContentArea().getChildren().setAll(view);
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
