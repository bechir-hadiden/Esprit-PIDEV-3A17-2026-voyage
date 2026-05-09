package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
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
    @FXML
    private Button suggestBtn;
    @FXML
    private ComboBox<String> priorityCombo;
    @FXML
    private ComboBox<String> timeContextCombo;

    private AIService aiService = new AIService();
    private ReservationService reservationService = new ReservationService();
    private UserService userService = new UserService();
    private SmartNotifyService notifyService = new SmartNotifyService();
    private User currentUser;
    private BaseVehicule currentRecommendedVehicule;

    private ContextMenu departureSuggestions = new ContextMenu();
    private ContextMenu arrivalSuggestions = new ContextMenu();
    private java.util.List<String> tunisianCities = java.util.Arrays.asList(
            "Tunis", "Sfax", "Sousse", "Kairouan", "Metline", "Bizerte", "Ariana", "Ben Arous",
            "Gafsa", "Monastir", "Gabès", "Nabeul", "Hammamet", "Kasserine", "Djerba", "Tataouine",
            "Beja", "Jendouba", "Mahdia");

    @FXML
    public void initialize() {
        setupAutocomplete(departureField, departureSuggestions);
        setupAutocomplete(arrivalField, arrivalSuggestions);

        priorityCombo.getItems().addAll("💰 Économique (Budget)", "⚡ Rapide (Gain de temps)", "💎 Confort Plus (VIP)");
        priorityCombo.getSelectionModel().select(0);

        timeContextCombo.getItems().addAll("⏰ Maintenant", "📅 Dans 1 heure", "🌙 Ce soir");
        timeContextCombo.getSelectionModel().select(0);

        // Professional Focus States
        setupFieldFocus(departureField);
        setupFieldFocus(arrivalField);
    }

    private void setupFieldFocus(TextField field) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: white; -fx-border-color: #2563eb; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14; -fx-font-size: 15px; -fx-effect: dropshadow(three-pass-box, rgba(37, 99, 235, 0.1), 10, 0, 0, 0);");
            } else {
                field.setStyle(
                        "-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14; -fx-font-size: 15px;");
            }
        });
    }

    private void setupAutocomplete(TextField field, ContextMenu menu) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 1) {
                menu.hide();
                return;
            }
            java.util.List<String> filtered = tunisianCities.stream()
                    .filter(city -> city.toLowerCase().contains(newVal.toLowerCase()))
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList());

            if (filtered.isEmpty()) {
                menu.hide();
            } else {
                menu.getItems().clear();
                for (String city : filtered) {
                    MenuItem item = new MenuItem(city);
                    item.setOnAction(e -> {
                        field.setText(city);
                        menu.hide();
                    });
                    menu.getItems().add(item);
                }
                if (!menu.isShowing()) {
                    menu.show(field, javafx.geometry.Side.BOTTOM, 0, 0);
                }
            }
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleGetSuggestion() {
        String start = departureField.getText();
        String end = arrivalField.getText();
        String priority = priorityCombo.getSelectionModel().getSelectedItem();
        String context = timeContextCombo.getSelectionModel().getSelectedItem();

        if (start.isEmpty() || end.isEmpty()) {
            return;
        }

        // UX: Clear previous result and show interactive "thinking" state
        resultContainer.setVisible(false);
        suggestBtn.setDisable(true);

        // Multi-step AI analysis animation
        String[] steps = {
                "🔍 Analyse de la demande...",
                "🚛 Comparaison de la flotte...",
                "⚡ Calcul de l'itinéraire optimal...",
                "🤖 Recommandation finale..."
        };

        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        for (int i = 0; i < steps.length; i++) {
            final int index = i;
            javafx.animation.KeyFrame frame = new javafx.animation.KeyFrame(
                    javafx.util.Duration.seconds(i * 0.8),
                    e -> suggestBtn.setText(steps[index]));
            timeline.getKeyFrames().add(frame);
        }

        timeline.setOnFinished(event -> {
            AIService.AIRecommendation reco = aiService.getDetailedRecommendation(start, end, priority, context);
            suggestBtn.setDisable(false);
            suggestBtn.setText("Obtenir une recommandation intelligente");

            if (reco != null) {
                showRecommendation(reco);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Désolé");
                alert.setHeaderText(null);
                alert.setContentText("Aucun véhicule disponible pour le moment.");
                alert.show();
            }
        });
        timeline.play();
    }

    private void showRecommendation(AIService.AIRecommendation reco) {
        recoCardContainer.getChildren().clear();
        this.currentRecommendedVehicule = reco.vehicule;
        BaseVehicule v = reco.vehicule;

        VBox card = new VBox(25);
        card.setPadding(new Insets(40));
        card.setMaxWidth(800);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 30; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(15, 23, 42, 0.08), 50, 0, 0, 25); " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 30;");

        // AI Match Badge
        Label badge = new Label("SÉLECTION OPTIMALE");
        badge.setStyle(
                "-fx-background-color: #f0f9ff; -fx-text-fill: #0369a1; -fx-font-weight: 900; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 11px;");

        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        Label title = new Label(v.getCompagnie());
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");
        Label subTitle = new Label("Immatriculation : " + v.getNumero());
        subTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");
        header.getChildren().addAll(title, subTitle);

        HBox grid = new HBox(20);
        grid.setAlignment(Pos.CENTER);
        grid.getChildren().addAll(
                createStatCard("Type", v.getType(),
                        "M18.5,11H6V6H18.5V11M17,17A1.5,1.5 0 0,1 15.5,15.5A1.5,1.5 0 0,1 17,14A1.5,1.5 0 0,1 18.5,15.5A1.5,1.5 0 0,1 17,17M8,17A1.5,1.5 0 0,1 6.5,15.5A1.5,1.5 0 0,1 8,14A1.5,1.5 0 0,1 9.5,15.5A1.5,1.5 0 0,1 8,17M19.5,13V15.5A2.5,2.5 0 0,1 17,18H16.41C15.95,19.17 14.82,20 13.5,20H11.5C10.18,20 9.05,19.17 8.59,18H8A2.5,2.5 0 0,1 5.5,15.5V13L4.34,11.07C3.81,10.2 3.5,9.15 3.5,8.08V7A2,2 0 0,1 5.5,5H19.5A2,2 0 0,1 21.5,7V8.08C21.5,9.15 21.19,10.2 20.66,11.07L19.5,13Z"),
                createStatCard("Tarif", v.getPrix() + " DT",
                        "M12,2C6.48,2 2,6.48 2,12C2,17.52 6.48,22 12,22C17.52,22 22,17.52 22,12C22,6.48 17.52,2 12,2M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20M12.35,7H10V9H9V10H10V14.5A2.5,2.5 0 0,0 12.5,17H14V15H12.5A0.5,0.5 0 0,1 12,14.5V10H14V9H12.5V7H12.35Z"),
                createStatCard("Places", String.valueOf(v.getCapacite()),
                        "M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"));

        VBox reasonBox = new VBox(15);
        reasonBox.setPadding(new Insets(25));
        reasonBox.setStyle(
                "-fx-background-color: #f8fafc; -fx-background-radius: 20; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 20;");
        reasonBox.setMaxWidth(600);

        Label reasonText = new Label(reco.reason);
        reasonText.setWrapText(true);
        reasonText.setStyle(
                "-fx-font-size: 15px; -fx-text-fill: #334155; -fx-line-spacing: 6; -fx-text-alignment: CENTER; -fx-font-weight: 500;");
        reasonBox.getChildren().add(reasonText);

        Button reserveBtn = new Button("Confirmer et Réserver");
        reserveBtn.setMaxWidth(400);
        reserveBtn.setStyle(
                "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 800; -fx-padding: 18 40; -fx-background-radius: 12; -fx-cursor: hand;");
        reserveBtn.setOnAction(e -> handleReserve());

        card.getChildren().addAll(badge, header, grid, reasonBox, reserveBtn);

        recoCardContainer.getChildren().add(card);
        resultContainer.setVisible(true);
        resultContainer.setManaged(true);
    }

    private VBox createStatCard(String label, String value, String svgPath) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setMinWidth(160);
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #f1f5f9; -fx-border-width: 1.5; -fx-border-radius: 20;");

        StackPane iconCircle = new StackPane();
        iconCircle.setStyle(
                "-fx-background-color: #eff6ff; -fx-background-radius: 50; -fx-min-width: 45; -fx-min-height: 45;");
        SVGPath path = new SVGPath();
        path.setContent(svgPath);
        path.setStyle("-fx-fill: #2563eb; -fx-scale-x: 1.1; -fx-scale-y: 1.1;");
        iconCircle.getChildren().add(path);

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-weight: 700; -fx-text-transform: uppercase;");

        box.getChildren().addAll(iconCircle, v, l);
        return box;
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
        try {
            javafx.scene.Scene scene = departureField.getScene();
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
}
