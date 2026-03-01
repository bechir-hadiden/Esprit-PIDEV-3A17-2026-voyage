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
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void setupCitySuggestions() {
        if (searchField == null)
            return;

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty() || newVal.length() < 1) {
                suggestionsMenu.hide();
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
    }

    @FXML
    private void handleSearch() {
        loadVehicules();
    }

    @FXML
    private void handleDetectLocation() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://ip-api.com/json/"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    // Try exact city first
                    Pattern cityPattern = Pattern.compile("\"city\":\"([^\"]+)\"");
                    Matcher cityMatcher = cityPattern.matcher(body);

                    String city = null;
                    if (cityMatcher.find()) {
                        city = cityMatcher.group(1);
                    }

                    if (city != null && !city.isEmpty()) {
                        final String finalCity = city;
                        javafx.application.Platform.runLater(() -> {
                            searchField.setText(finalCity);
                            loadVehicules();
                        });
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Location detection failed: " + ex.getMessage());
                    return null;
                });
    }

    @FXML
    private void rafraichir() {
        loadVehicules();
    }

    private void displayVehiculesAsCards(List<BaseVehicule> vehicules) {
        transportCardsPane.getChildren().clear();
        for (BaseVehicule v : vehicules) {
            if (v != null) {
                transportCardsPane.getChildren().add(createSimpleVehiculeCard(v));
            }
        }
    }

    private StackPane createSimpleVehiculeCard(BaseVehicule vehicule) {
        StackPane cardRoot = new StackPane();
        cardRoot.getStyleClass().add("transport-card");
        cardRoot.setPrefSize(300, 320);
        cardRoot.setMaxSize(300, 320);

        // 1. Background Image (Specific to the vehicle)
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
                // Fallback to type-specific image
                String typeImg = vehicule.getType().toLowerCase() + ".png";
                bgView.setImage(new Image(getClass().getResourceAsStream("/images/" + typeImg)));
            }
            bgView.setFitWidth(300);
            bgView.setFitHeight(320);
            bgView.setPreserveRatio(false);
        } catch (Exception e) {
            System.err.println("Could not load image for vehicle: " + vehicule.getNumero());
        }

        // 2. Clip for rounded corners
        Rectangle clip = new Rectangle(300, 320);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        cardRoot.setClip(clip);

        // 3. Dark Overlay
        Region overlay = new Region();
        overlay.getStyleClass().add("card-overlay");

        // 4. Content
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

        Label compLabel = new Label(vehicule.getCompagnie());
        compLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: 800;");

        Label numLabel = new Label("N° " + vehicule.getNumero() + " • " + vehicule.getCapacite() + " places");
        numLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e2e8f0;");

        Label priceLabel = new Label(String.format("%.2f DT", vehicule.getPrix()));
        priceLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #10b981; -fx-font-weight: 900;");

        // Status Badge
        Label statusBadge = new Label(vehicule.isDisponible() ? "DISPONIBLE" : "INDISPONIBLE");
        statusBadge.setStyle("-fx-background-color: " + (vehicule.isDisponible() ? "#10b981" : "#ef4444") + "; " +
                "-fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: 900; -fx-letter-spacing: 1px;");

        Button reserveBtn = new Button(vehicule.isDisponible() ? "Réserver maintenant" : "Non disponible");
        reserveBtn.getStyleClass().add("reserve-button");
        reserveBtn.setMaxWidth(220);
        reserveBtn.setPrefHeight(45);
        if (!vehicule.isDisponible()) {
            reserveBtn.setDisable(true);
            reserveBtn.setStyle("-fx-opacity: 0.6; -fx-cursor: default;");
        }

        Region btnIcon = new Region();
        btnIcon.getStyleClass().addAll("icon", "icon-reserve");
        btnIcon.setPrefSize(16, 16);
        reserveBtn.setGraphic(btnIcon);
        reserveBtn.setOnAction(e -> handleReservation(vehicule));

        content.getChildren().addAll(compLabel, numLabel, priceLabel, statusBadge, reserveBtn);

        cardRoot.getChildren().addAll(bgView, overlay, content);
        cardRoot.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

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
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/user_menu.fxml", shell.getBtnDashboard());
        }
    }

}
