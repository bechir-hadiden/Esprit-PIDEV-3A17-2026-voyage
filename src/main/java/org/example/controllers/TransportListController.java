package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.*;
import org.example.services.ReservationService;
import org.example.services.SmartNotifyService;
import org.example.services.UserService;
import org.example.services.VehiculeService;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Simplified Transport List Controller with City Suggestions. */
public class TransportListController {

    private User currentUser;
    private String transportType;
    private Timeline autoRefreshTimeline;

    @FXML
    private Label titleLabel;
    @FXML
    private Label mainTitleLabel;
    @FXML
    private TextField searchField;
    @FXML
    private FlowPane transportCardsPane;

    private ContextMenu suggestionsMenu = new ContextMenu();
    private final List<String> tunisianCities = Arrays.asList(
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Bizerte", "Nabeul", "Hammamet",
            "Borj Cedria", "Sousse", "Monastir", "Mahdia", "Sfax", "Kairouan", "Kasserine",
            "Sidi Bouzid", "Gafsa", "Tozeur", "Kebili", "Gabès", "Médenine", "Tataouine",
            "Béja", "Jendouba", "Le Kef", "Siliana", "Zaghouan");

    private VehiculeService vehiculeService = new VehiculeService();
    private ReservationService reservationService = new ReservationService();
    private UserService userService = new UserService();
    private SmartNotifyService notifyService = new SmartNotifyService();

    @FXML
    public void initialize() {
        setupCitySuggestions();
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        // Try to get user from com.example.demo1 session if available
        try {
            com.example.demo1.services.AuthService auth = com.example.demo1.services.AuthService.getInstance();
            com.example.demo1.entity.User authUser = auth.getCurrentUser();
            if (authUser != null) {
                this.currentUser = new User();
                this.currentUser.setIdUser(Integer.parseInt(authUser.getId()));
                this.currentUser.setUsername(authUser.getUsername());
                this.currentUser.setEmail(authUser.getEmail());
                this.currentUser.setTelephone(authUser.getTelephone());
                this.currentUser.setRole(authUser.getRole());
            }
        } catch (NoClassDefFoundError | Exception e) {
            // Fallback or ignore if not in demo1 context
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void setupCitySuggestions() {
        if (searchField == null)
            return;

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                suggestionsMenu.hide();
                loadVehicules();
                return;
            }

            List<String> filtered = tunisianCities.stream()
                    .filter(city -> city.toLowerCase().contains(newVal.toLowerCase()))
                    .limit(5)
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                suggestionsMenu.hide();
            } else {
                showSuggestions(filtered);
            }
        });

        // Hide menu when clicking outside
        searchField.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus)
                suggestionsMenu.hide();
        });
    }

    private void showSuggestions(List<String> cities) {
        suggestionsMenu.getItems().clear();
        for (String city : cities) {
            MenuItem item = new MenuItem(city);
            item.setOnAction(e -> {
                searchField.setText(city);
                loadVehicules();
                suggestionsMenu.hide();
            });
            suggestionsMenu.getItems().add(item);
        }

        if (!suggestionsMenu.isShowing()) {
            suggestionsMenu.show(searchField, javafx.geometry.Side.BOTTOM, 0, 0);
        }
    }

    public void setTransportType(String type) {
        this.transportType = (type == null) ? "" : type;
        if (this.transportType.isEmpty()) {
            titleLabel.setText("Tous les Transports Disponibles");
        } else {
            titleLabel.setText("Véhicules : " + this.transportType);
        }
        loadVehicules();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        if (autoRefreshTimeline != null)
            autoRefreshTimeline.stop();
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> loadVehicules()));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void loadVehicules() {
        String city = searchField != null ? searchField.getText().trim() : "";
        displayVehiculesAsCards(vehiculeService.listerParVille(transportType, city));

        // Update UI Text
        String filterText = transportType.isEmpty() ? "Tous les Transports" : transportType + "s";
        if (!city.isEmpty()) {
            mainTitleLabel.setText(filterText + " à " + city);
        } else {
            mainTitleLabel.setText(filterText + " Disponibles");
        }
    }

    @FXML
    private void handleSearch() {
        loadVehicules();
    }

    @FXML
    private void handleDetectLocation() {
        // En Tunisie, la géolocalisation par IP est très imprécise (les FAI renvoient
        // souvent vers Sousse).
        // Pour les besoins de la démonstration, on simule la position exacte de
        // l'utilisateur.
        javafx.application.Platform.runLater(() -> {
            searchField.setText("Ariana");
            loadVehicules();
        });
    }

    @FXML
    private void rafraichir() {
        loadVehicules();
    }

    private void displayVehiculesAsCards(List<BaseVehicule> vehicules) {
        transportCardsPane.getChildren().clear();
        if (vehicules.isEmpty()) {
            Label noResults = new Label("Aucun véhicule trouvé pour cette recherche.");
            noResults.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8; -fx-padding: 50;");
            transportCardsPane.getChildren().add(noResults);
            return;
        }
        for (BaseVehicule v : vehicules) {
            if (v != null) {
                transportCardsPane.getChildren().add(createSimpleVehiculeCard(v));
            }
        }
    }

    private VBox createSimpleVehiculeCard(BaseVehicule vehicule) {
        VBox cardRoot = new VBox(0);
        cardRoot.getStyleClass().add("admin-card");
        cardRoot.setStyle("-fx-padding: 0;");
        cardRoot.setPrefWidth(300);

        // Hover Effect
        cardRoot.setOnMouseEntered(e -> cardRoot.setStyle(
                "-fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(14, 165, 233, 0.18), 18, 0, 0, 5); -fx-border-color: #bae6fd; -fx-translate-y: -3;"));
        cardRoot.setOnMouseExited(e -> cardRoot.setStyle(
                "-fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.07), 10, 0, 0, 3); -fx-border-color: #e8edf3; -fx-translate-y: 0;"));

        // 1. Image Container (Top)
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14 14 0 0;");
        imageContainer.setMinHeight(180);

        ImageView bgView = new ImageView();
        try {
            if (vehicule.getImage() != null && !vehicule.getImage().isEmpty()) {
                String path = "/images/" + vehicule.getImage();
                java.net.URL res = getClass().getResource(path);
                if (res != null) {
                    bgView.setImage(new Image(res.toExternalForm()));
                } else {
                    File file = new File("src/main/resources/images/" + vehicule.getImage());
                    if (file.exists()) {
                        bgView.setImage(new Image(file.toURI().toString()));
                    }
                }
            } else {
                String typeImg = vehicule.getType().toLowerCase() + ".png";
                java.io.InputStream is = getClass().getResourceAsStream("/images/" + typeImg);
                if (is != null)
                    bgView.setImage(new Image(is));
            }
            bgView.setFitWidth(300);
            bgView.setFitHeight(180);
            bgView.setPreserveRatio(false);

            if (!vehicule.isDisponible()) {
                javafx.scene.effect.ColorAdjust desaturate = new javafx.scene.effect.ColorAdjust();
                desaturate.setSaturation(-1.0);
                bgView.setEffect(desaturate);
            }

            Rectangle clip = new Rectangle(300, 180);
            clip.setArcWidth(28);
            clip.setArcHeight(28);
            bgView.setClip(clip);
            imageContainer.getChildren().add(bgView);
        } catch (Exception e) {
        }

        // Status Badge Overlay on Image
        Label statusBadge = new Label(vehicule.isDisponible() ? "DISPONIBLE" : "INDISPONIBLE");
        statusBadge.setStyle("-fx-background-color: " + (vehicule.isDisponible() ? "#10b981" : "#ef4444") + "; " +
                "-fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: 900; -fx-letter-spacing: 1px;");
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(statusBadge, new Insets(15));
        imageContainer.getChildren().add(statusBadge);

        // 2. Content Body (Bottom)
        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20;");

        Label compLabel = new Label(vehicule.getCompagnie());
        compLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #1e293b; -fx-font-weight: 800;");

        Label numLabel = new Label("N° " + vehicule.getNumero() + " • " + vehicule.getCapacite() + " places");
        numLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        javafx.scene.layout.HBox priceRow = new javafx.scene.layout.HBox(6);
        priceRow.setAlignment(Pos.BASELINE_LEFT);
        Label priceVal = new Label(String.format("%.2f DT", vehicule.getPrix()));
        priceVal.setStyle("-fx-font-size: 20px; -fx-text-fill: " + (vehicule.isDisponible() ? "#0ea5e9" : "#94a3b8")
                + "; -fx-font-weight: 900;");
        priceRow.getChildren().add(priceVal);

        Button reserveBtn = new Button(vehicule.isDisponible() ? "Réserver" : "Indisponible");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        if (vehicule.isDisponible()) {
            reserveBtn.setStyle(
                    "-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 8; -fx-cursor: hand;");
            reserveBtn.setOnMouseEntered(e -> reserveBtn.setStyle(
                    "-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 8; -fx-cursor: hand;"));
            reserveBtn.setOnMouseExited(e -> reserveBtn.setStyle(
                    "-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 8; -fx-cursor: hand;"));
            reserveBtn.setOnAction(e -> handleReservation(vehicule));
        } else {
            reserveBtn.setDisable(true);
            reserveBtn.setStyle(
                    "-fx-opacity: 0.6; -fx-cursor: default; -fx-background-color: #475569; -fx-text-fill: white; -fx-padding: 10 0; -fx-background-radius: 8;");
        }

        content.getChildren().addAll(compLabel, numLabel, priceRow, reserveBtn);
        cardRoot.getChildren().addAll(imageContainer, content);

        return cardRoot;
    }

    private void handleReservation(BaseVehicule vehicule) {
        if (currentUser == null)
            return;

        // Prompt for Email only
        TextInputDialog dialog = new TextInputDialog(currentUser.getEmail());
        dialog.setTitle("E-mail");
        dialog.setHeaderText("Confirmez votre e-mail");
        dialog.setContentText("Adresse :");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String email = result.get();
            if (email.isEmpty()) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setContentText("E-mail obligatoire.");
                errorAlert.show();
                return;
            }

            // Update user email if changed
            if (!email.equals(currentUser.getEmail())) {
                currentUser.setEmail(email);
                userService.updateEmail(currentUser.getIdUser(), email);
            }

            // Perform reservation
            Reservation r = new Reservation();
            r.setIdUser(currentUser.getIdUser());
            r.setTypeTransport(vehicule.getType());
            r.setIdVehicule(vehicule.getId());
            r.setDateReservation(LocalDateTime.now());
            r.setStatut("CONFIRMED");

            boolean success = reservationService.ajouter(r);

            if (success) {
                // Prepare transport entity for notification
                Transport t = new Transport();
                t.setType(vehicule.getType());
                t.setCompagnie(vehicule.getCompagnie());
                t.setNumero(vehicule.getNumero());

                // Send Notifications
                notifyService.sendConfirmationEmail(currentUser, t, r);
                if (currentUser.getTelephone() != null && !currentUser.getTelephone().isEmpty()) {
                    notifyService.sendConfirmationSMS(currentUser, t, r);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("C'est fait !");
                alert.setContentText("Réservation confirmée et e-mail envoyé.");
                alert.showAndWait();

                // 🔄 Refresh the UI instantly so the just-reserved vehicle turns grey
                loadVehicules();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setContentText("Échec de l'enregistrement de la réservation. Veuillez réessayer.");
                errorAlert.show();
            }
        }
    }

    @FXML
    private void goBack() {
        try {
            javafx.scene.Scene scene = titleLabel.getScene();
            javafx.scene.layout.Pane contentArea = (javafx.scene.layout.Pane) scene.lookup("#contentArea");
            if (contentArea == null)
                contentArea = (javafx.scene.layout.Pane) scene.lookup("#contentContainer");

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transport_type_selector.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());

                // Pass current user if controller supports it
                Object controller = loader.getController();
                if (controller instanceof TransportTypeSelectorController) {
                    ((TransportTypeSelectorController) controller).setCurrentUser(currentUser);
                }

                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/transport_type_selector.fxml");
        }
    }

}
