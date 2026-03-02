package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.example.entities.TransportType;
import org.example.entities.User;
import org.example.services.TransportTypeService;
import com.example.demo1.services.AuthService;
import java.util.List;

public class TransportTypeSelectorController {

    private User currentUser;
    private TransportTypeService typeService = new TransportTypeService();

    @FXML
    private FlowPane cardsContainer;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        loadCurrentUser();
        loadTransportTypes();
    }

    private void loadCurrentUser() {
        AuthService auth = AuthService.getInstance();
        if (auth.getCurrentUser() != null) {
            if (this.currentUser == null) {
                this.currentUser = new User();
                try {
                    this.currentUser.setIdUser(Integer.parseInt(auth.getCurrentUser().getId()));
                } catch (Exception e) {
                }
                this.currentUser.setUsername(auth.getCurrentUser().getUsername());
                this.currentUser.setEmail(auth.getCurrentUser().getEmail());
                this.currentUser.setTelephone(auth.getCurrentUser().getTelephone());
                this.currentUser.setRole_string(auth.getCurrentUser().getRole());
            }
        }
    }

    private void loadTransportTypes() {
        cardsContainer.getChildren().clear();
        List<TransportType> types = typeService.lister();
        for (TransportType type : types) {
            cardsContainer.getChildren().add(createSimpleCard(type));
        }
    }

    private VBox createSimpleCard(TransportType type) {
        boolean isAll = type.getNom().equals("Tous les Transports");

        VBox cardRoot = new VBox(0);
        cardRoot.getStyleClass().add("admin-card");
        cardRoot.setStyle("-fx-padding: 0; -fx-cursor: hand;");
        cardRoot.setPrefWidth(300);

        // Add hover effect via event handlers since it's dynamically created
        cardRoot.setOnMouseEntered(e -> cardRoot.setStyle(
                "-fx-padding: 0; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(14, 165, 233, 0.18), 18, 0, 0, 5); -fx-border-color: #bae6fd; -fx-translate-y: -3;"));
        cardRoot.setOnMouseExited(e -> cardRoot.setStyle(
                "-fx-padding: 0; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.07), 10, 0, 0, 3); -fx-border-color: #e8edf3; -fx-translate-y: 0;"));

        // Image Section
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14 14 0 0;");
        imageContainer.setMinHeight(160);
        ImageView bgView = new ImageView();
        try {
            boolean loaded = false;
            if (isAll) {
                String path = "/images/all_transports.png";
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    bgView.setImage(new Image(is));
                    loaded = true;
                }
            } else if (type.getImage() != null && !type.getImage().isEmpty()) {
                String path = "/images/" + type.getImage();
                java.net.URL res = getClass().getResource(path);
                if (res != null) {
                    bgView.setImage(new Image(res.toExternalForm()));
                    loaded = true;
                }
            }
            if (!loaded && !isAll) {
                String imgName = type.getNom().toLowerCase() + ".png";
                java.io.InputStream is = getClass().getResourceAsStream("/images/" + imgName);
                if (is != null) {
                    bgView.setImage(new Image(is));
                }
            }
            bgView.setFitWidth(300);
            bgView.setFitHeight(160);
            bgView.setPreserveRatio(false);

            // Clip image to match card top borders
            Rectangle clip = new Rectangle(300, 160);
            clip.setArcWidth(28);
            clip.setArcHeight(28);
            bgView.setClip(clip);
            imageContainer.getChildren().add(bgView);
        } catch (Exception e) {
        }

        // Content Body
        VBox body = new VBox(8);
        body.setStyle("-fx-padding: 20 24 24 24;");

        String nomLc = type.getNom() != null ? type.getNom().toLowerCase() : "";
        String typeEmoji = isAll ? "🌍"
                : nomLc.contains("bus") ? "🚌"
                        : nomLc.contains("taxi") ? "🚕"
                                : nomLc.contains("voiture") ? "🚗" : nomLc.contains("scooter") ? "🛵" : "🚐";

        Label nameLabel = new Label(typeEmoji + "  " + type.getNom());
        nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        nameLabel.setWrapText(true);

        body.getChildren().add(nameLabel);

        if (!isAll) {
            javafx.scene.layout.HBox priceRow = new javafx.scene.layout.HBox(6);
            priceRow.setAlignment(Pos.BASELINE_LEFT);
            Label priceVal = new Label(String.format("%.2f DT", type.getPrixDepart()));
            priceVal.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #0ea5e9;");
            Label priceUnit = new Label("de départ");
            priceUnit.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
            priceRow.getChildren().addAll(priceVal, priceUnit);
            body.getChildren().add(priceRow);
        } else {
            Label subLabel = new Label("Voir tout le catalogue");
            subLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            body.getChildren().add(subLabel);
        }

        cardRoot.getChildren().addAll(imageContainer, body);

        cardRoot.setOnMouseClicked(event -> {
            navigateToTransportList(isAll ? "" : type.getNom());
        });

        return cardRoot;
    }

    private void navigateToTransportList(String type) {
        try {
            javafx.scene.Scene scene = cardsContainer.getScene();
            Pane contentArea = (Pane) scene.lookup("#contentArea");
            if (contentArea == null)
                contentArea = (Pane) scene.lookup("#contentContainer");

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transport_list.fxml"));
                javafx.scene.Parent view = loader.load();
                TransportListController controller = loader.getController();
                controller.setCurrentUser(currentUser);
                controller.setTransportType(type);
                contentArea.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            javafx.scene.Scene scene = cardsContainer.getScene();
            Pane contentArea = (Pane) scene.lookup("#contentArea");
            if (contentArea == null)
                contentArea = (Pane) scene.lookup("#contentContainer");

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_menu.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
