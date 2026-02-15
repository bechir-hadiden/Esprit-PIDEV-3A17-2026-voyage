package org.example.demo10.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.demo10.model.User;
import org.example.demo10.model.Voyage;
import org.example.demo10.service.ReservationService;
import org.example.demo10.service.VoyageService;
import org.example.demo10.util.LoginDialog;
import org.example.demo10.util.ReservationDialog;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ClientVoyagesController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private Button btnRechercher;
    @FXML private Button btnReset;
    @FXML private Button btnConnexion;
    @FXML private Button btnMesReservations;
    @FXML private VBox containerVoyages;

    private VoyageService voyageService;
    private ReservationService reservationService;
    private ObservableList<Voyage> voyagesList;
    private ObservableList<Voyage> voyagesListFiltree;

    // Utilisateur connecté
    private User utilisateurConnecte;
    private boolean estConnecte = false;

    @FXML
    public void initialize() {
        voyageService = new VoyageService();
        reservationService = new ReservationService();

        voyagesList = FXCollections.observableArrayList();
        voyagesListFiltree = FXCollections.observableArrayList();

        // Initialiser le filtre
        comboFiltre.setItems(FXCollections.observableArrayList(
                "Tous", "Disponibles", "Prix < 1000€", "Prix 1000-1500€", "Prix > 1500€", "Moins de 7 jours"
        ));
        comboFiltre.setValue("Tous");

        // Charger les voyages
        chargerVoyages();

        // Configuration des boutons
        btnRechercher.setOnAction(e -> rechercherVoyages());
        btnReset.setOnAction(e -> resetRecherche());
        btnConnexion.setOnAction(e -> gererConnexion());
        btnMesReservations.setOnAction(e -> ouvrirMesReservations());
    }

    private void chargerVoyages() {
        voyagesList.clear();
        voyagesList.addAll(voyageService.getAllVoyages());
        voyagesListFiltree.setAll(voyagesList);
        afficherCartesVoyages();
    }

    private void afficherCartesVoyages() {
        containerVoyages.getChildren().clear();

        if (voyagesListFiltree.isEmpty()) {
            Label lblAucun = new Label("Aucun voyage trouvé.");
            lblAucun.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
            containerVoyages.getChildren().add(lblAucun);
            return;
        }

        for (Voyage voyage : voyagesListFiltree) {
            VBox carteVoyage = creerCarteVoyage(voyage);
            containerVoyages.getChildren().add(carteVoyage);
        }
    }

    private VBox creerCarteVoyage(Voyage voyage) {
        VBox carte = new VBox();
        carte.setSpacing(15);
        carte.setPadding(new Insets(20));
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 15; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // En-tête avec destination
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        // Image (placeholder ou image locale)
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        try {
            // Essayer de charger une image depuis le web
            Image image = new Image(voyage.getImageUrl(), true);
            imageView.setImage(image);
        } catch (Exception e) {
            // Image par défaut si le chargement échoue
            imageView.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 10;");
        }

        // Informations principales
        VBox infoPrincipale = new VBox(5);

        Label lblDestination = new Label(voyage.getDestination());
        lblDestination.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lblPrix = new Label(voyage.getPrixFormate());
        lblPrix.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        Label lblDuree = new Label("⏱️ " + voyage.getDureeFormate());
        lblDuree.setStyle("-fx-text-fill: #6c757d;");

        infoPrincipale.getChildren().addAll(lblDestination, lblPrix, lblDuree);

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        // Statut des places
        Label lblStatut = new Label(voyage.getStatutPlaces());
        lblStatut.setStyle("-fx-background-color: " + voyage.getCouleurStatut() + "; " +
                "-fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 20; -fx-font-weight: bold;");

        header.getChildren().addAll(imageView, infoPrincipale, espace, lblStatut);

        // Description
        Label lblDescription = new Label(voyage.getDescription());
        lblDescription.setWrapText(true);
        lblDescription.setStyle("-fx-text-fill: #495057; -fx-font-size: 14px;");

        // Dates
        HBox datesBox = new HBox(20);
        datesBox.setAlignment(Pos.CENTER_LEFT);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Label lblDateDepart = new Label("📅 Départ: " + voyage.getDateDepart().format(formatter));
        lblDateDepart.setStyle("-fx-text-fill: #17a2b8;");

        Label lblDateRetour = new Label("🏁 Retour: " + voyage.getDateRetour().format(formatter));
        lblDateRetour.setStyle("-fx-text-fill: #17a2b8;");

        datesBox.getChildren().addAll(lblDateDepart, lblDateRetour);

        // Bouton Réserver
        Button btnReserver = new Button("📝 Réserver");
        btnReserver.setPrefWidth(200);

        if (voyage.getPlacesDisponibles() > 0) {
            btnReserver.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-padding: 12 20; -fx-background-radius: 25; -fx-font-size: 14px;");
            btnReserver.setOnAction(e -> reserverVoyage(voyage));
        } else {
            btnReserver.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-padding: 12 20; -fx-background-radius: 25; -fx-font-size: 14px;");
            btnReserver.setDisable(true);
            btnReserver.setText("❌ Complet");
        }

        // Pied de carte
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getChildren().add(btnReserver);

        carte.getChildren().addAll(header, lblDescription, datesBox, footer);

        // Effet hover
        carte.setOnMouseEntered(e -> {
            carte.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; " +
                    "-fx-border-color: #007bff; -fx-border-radius: 15; -fx-border-width: 2; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,123,255,0.2), 15, 0, 0, 5);");
        });

        carte.setOnMouseExited(e -> {
            carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                    "-fx-border-color: #e9ecef; -fx-border-radius: 15; -fx-border-width: 1; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        });

        return carte;
    }

    private void rechercherVoyages() {
        String recherche = txtRecherche.getText().trim();
        String filtre = comboFiltre.getValue();

        List<Voyage> resultats = voyageService.getAllVoyages();

        // Filtrer par recherche texte
        if (!recherche.isEmpty()) {
            resultats = voyageService.rechercherVoyages(recherche);
        }

        // Filtrer par critère
        switch (filtre) {
            case "Disponibles":
                resultats = resultats.stream()
                        .filter(v -> v.getPlacesDisponibles() > 0)
                        .toList();
                break;
            case "Prix < 1000€":
                resultats = resultats.stream()
                        .filter(v -> v.getPrix() < 1000)
                        .toList();
                break;
            case "Prix 1000-1500€":
                resultats = resultats.stream()
                        .filter(v -> v.getPrix() >= 1000 && v.getPrix() <= 1500)
                        .toList();
                break;
            case "Prix > 1500€":
                resultats = resultats.stream()
                        .filter(v -> v.getPrix() > 1500)
                        .toList();
                break;
            case "Moins de 7 jours":
                resultats = resultats.stream()
                        .filter(v -> v.getDuree() < 7)
                        .toList();
                break;
        }

        voyagesListFiltree.setAll(resultats);
        afficherCartesVoyages();

        if (voyagesListFiltree.isEmpty()) {
            showAlert("Information", "Aucun voyage trouvé.");
        }
    }

    private void resetRecherche() {
        txtRecherche.clear();
        comboFiltre.setValue("Tous");
        voyagesListFiltree.setAll(voyagesList);
        afficherCartesVoyages();
    }

    private void reserverVoyage(Voyage voyage) {
        if (!estConnecte || utilisateurConnecte == null) {
            // Demander de se connecter
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Connexion requise");
            alert.setHeaderText("Vous devez être connecté pour réserver");
            alert.setContentText("Voulez-vous vous connecter maintenant ?");

            ButtonType btnOui = new ButtonType("Oui, me connecter");
            ButtonType btnNon = new ButtonType("Non, plus tard");

            alert.getButtonTypes().setAll(btnOui, btnNon);

            if (alert.showAndWait().orElse(btnNon) == btnOui) {
                gererConnexion();
            }
            return;
        }

        // Ouvrir la boîte de dialogue de réservation
        ReservationDialog.showReservationDialog(voyage, utilisateurConnecte, reservationService, this::chargerVoyages);
    }

    private void gererConnexion() {
        if (estConnecte) {
            // Déconnexion
            if (LoginDialog.showLogoutDialog(utilisateurConnecte.getNom())) {
                utilisateurConnecte = null;
                estConnecte = false;
                btnConnexion.setText("👤 Connexion");
                showAlert("Déconnexion", "Vous avez été déconnecté.");
            }
        } else {
            // Connexion
            Optional<User> userOpt = LoginDialog.showLoginDialog();
            if (userOpt.isPresent()) {
                utilisateurConnecte = userOpt.get();
                estConnecte = true;
                btnConnexion.setText("👤 " + utilisateurConnecte.getNom());
                showAlert("Connexion réussie", "Bienvenue " + utilisateurConnecte.getNom() + " !");
            }
        }
    }

    private void ouvrirMesReservations() {
        if (!estConnecte || utilisateurConnecte == null) {
            showAlert("Information", "Connectez-vous pour voir vos réservations.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo10/client-mes-reservations.fxml"));
            Parent root = loader.load();

            ClientMesReservationsController controller = loader.getController();
            controller.setUtilisateurConnecte(utilisateurConnecte);

            Stage stage = new Stage();
            stage.setTitle("Mes Réservations");
            stage.setScene(new Scene(root, 1000, 600));
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir vos réservations.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}