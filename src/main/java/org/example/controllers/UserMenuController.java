package org.example.controllers;

import javafx.fxml.FXMLLoader;

import javafx.fxml.FXML;
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
import javafx.scene.shape.Rectangle;
import org.example.entities.TransportType;
import org.example.entities.User;
import org.example.services.TransportTypeService;

import java.util.List;

/** Simplified User Menu Controller. */
public class UserMenuController {

    private User currentUser;
    private TransportTypeService typeService = new TransportTypeService();

    @FXML
    private BorderPane rootPane;
    @FXML
    private FlowPane cardsContainer;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        loadTransportTypes();
    }

    private void loadTransportTypes() {
        cardsContainer.getChildren().clear();

        // Add specific types from database
        List<TransportType> types = typeService.lister();
        for (TransportType type : types) {
            cardsContainer.getChildren().add(createSimpleCard(type));
        }
    }

    private StackPane createSimpleCard(TransportType type) {
        boolean isAll = type.getNom().equals("Tous les Transports");

        StackPane cardRoot = new StackPane();
        cardRoot.getStyleClass().add("transport-card");
        cardRoot.setPrefSize(300, 250);
        cardRoot.setMaxSize(300, 250);

        // 1. Background Image
        ImageView bgView = new ImageView();
        try {
            boolean loaded = false;
            if (isAll) {
                // Fallback for All Transports
                String path = "/images/all_transports.png";
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    bgView.setImage(new Image(is));
                    loaded = true;
                }
            } else if (type.getImage() != null && !type.getImage().isEmpty()) {
                try {
                    String path = "/images/" + type.getImage();
                    java.net.URL res = getClass().getResource(path);
                    if (res != null) {
                        bgView.setImage(new Image(res.toExternalForm()));
                        loaded = true;
                    } else {
                        java.io.File file = new java.io.File(type.getImage());
                        if (file.exists()) {
                            bgView.setImage(new Image(file.toURI().toString()));
                            loaded = true;
                        }
                    }
                } catch (Exception e) {
                }
            }

            if (!loaded && !isAll) {
                String imgName = type.getNom().toLowerCase() + ".png";
                String path = "/images/" + imgName;
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    bgView.setImage(new Image(is));
                    loaded = true;
                }
            }

            bgView.setFitWidth(300);
            bgView.setFitHeight(250);
            bgView.setPreserveRatio(false);
        } catch (Exception e) {
        }

        // 2. Rounded corners
        Rectangle clip = new Rectangle(300, 250);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        cardRoot.setClip(clip);

        // 3. Overlay
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        // 4. Content
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Label nameLabel = new Label(type.getNom());
        nameLabel.setStyle(
                "-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: 900; -fx-text-alignment: center;");
        nameLabel.setWrapText(true);

        content.getChildren().add(nameLabel);
        if (!isAll) {
            Label priceLabel = new Label("À partir de " + type.getPrixDepart() + " DT");
            priceLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #10b981; -fx-font-weight: 800;");
            content.getChildren().add(priceLabel);
        }

        cardRoot.getChildren().addAll(bgView, overlay, content);
        cardRoot.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        cardRoot.setOnMouseClicked(event -> {
            if (isAll)
                navigateToTransportList(""); // Empty type means all
            else
                navigateToTransportList(type.getNom());
        });

        return cardRoot;
    }

    private void navigateToTransportList(String type) {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transport_list.fxml"));
                javafx.scene.Parent view = loader.load();

                TransportListController controller = loader.getController();
                controller.setCurrentUser(currentUser);
                controller.setTransportType(type);

                shell.getContentArea().getChildren().setAll(view);
                shell.updateActiveButton(shell.getBtnFleet());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void showMyReservations() {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/my_reservations.fxml", shell.getBtnMyReservations());
        }
    }

    @FXML
    private void showAIGuide() {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/ai_guide.fxml", shell.getBtnAIGuide());
        }
    }

}
