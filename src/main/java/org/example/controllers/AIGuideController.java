package org.example.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.entities.BaseVehicule;
import org.example.entities.Reservation;
import org.example.entities.User;
import org.example.services.AIService;
import org.example.services.ReservationService;
import org.example.services.SmartNotifyService;
import org.example.services.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

public class AIGuideController {

    @FXML
    private TextField departureField;
    @FXML
    private TextField arrivalField;
    @FXML
    private VBox resultContainer;
    @FXML
    private VBox recoCardContainer;

    private AIService aiService = new AIService();
    private ReservationService reservationService = new ReservationService();
    private UserService userService = new UserService();
    private SmartNotifyService notifyService = new SmartNotifyService();
    private User currentUser;
    private BaseVehicule currentRecommendedVehicule;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleGetSuggestion() {
        String start = departureField.getText();
        String end = arrivalField.getText();

        if (start.isEmpty() || end.isEmpty()) {
            return;
        }

        AIService.AIRecommendation reco = aiService.getDetailedRecommendation(start, end);

        if (reco != null) {
            showRecommendation(reco);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Désolé");
            alert.setHeaderText(null);
            alert.setContentText("Aucun véhicule disponible pour le moment.");
            alert.show();
        }
    }

    private void showRecommendation(AIService.AIRecommendation reco) {
        recoCardContainer.getChildren().clear();
        this.currentRecommendedVehicule = reco.vehicule;
        BaseVehicule v = reco.vehicule;

        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));
        card.setMaxWidth(600);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 15;");

        Label title = new Label(v.getCompagnie() + " - " + v.getNumero());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label type = new Label("Type: " + v.getType());
        type.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");

        Label reason = new Label("AI Thinking... \n" + reco.reason);
        reason.setWrapText(true);
        reason.setStyle(
                "-fx-font-style: italic; -fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-text-alignment: center;");

        Button reserveBtn = new Button("Réserver ce véhicule");
        reserveBtn.getStyleClass().add("button-primary");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        reserveBtn.setOnAction(e -> handleReserve());

        card.getChildren().addAll(title, type, reason, reserveBtn);

        recoCardContainer.getChildren().add(card);
        resultContainer.setVisible(true);
        resultContainer.setManaged(true);
    }

    private void handleReserve() {
        if (currentRecommendedVehicule == null || currentUser == null)
            return;

        TextInputDialog dialog = new TextInputDialog(currentUser.getEmail());
        dialog.setTitle("Confirmation de réservation");
        dialog.setHeaderText("Confirmez votre e-mail pour " + currentRecommendedVehicule.getCompagnie());
        dialog.setContentText("Adresse e-mail :");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String email = result.get();
            if (email.isEmpty())
                return;

            if (!email.equals(currentUser.getEmail())) {
                currentUser.setEmail(email);
                userService.updateEmail(currentUser.getIdUser(), email);
            }

            Reservation r = new Reservation();
            r.setIdUser(currentUser.getIdUser());
            r.setTypeTransport(currentRecommendedVehicule.getType());
            r.setIdVehicule(currentRecommendedVehicule.getId());
            r.setDateReservation(LocalDateTime.now());
            r.setStatut("CONFIRMED");

            boolean success = reservationService.ajouter(r);
            if (success) {
                notifyService.sendAIConfirmation(currentUser, currentRecommendedVehicule, r);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Félicitations !");
                alert.setContentText("Votre réservation conseillée par l'AI a été effectuée avec succès.");
                alert.show();
                goBack();
            }
        }
    }

    @FXML
    private void goBack() {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/user_menu.fxml", shell.getBtnDashboard());
        }
    }
}
