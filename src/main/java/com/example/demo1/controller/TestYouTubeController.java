package com.example.demo1.controller;

import com.example.demo1.entity.Destination;
import com.example.demo1.services.DestinationService;
import com.example.demo1.services.QRCodeService;
import com.example.demo1.services.YouTubeService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.util.List;

public class TestYouTubeController {

    // ===== FXML =====
    @FXML private Button btnTesterCle;
    @FXML private Label lblStatutCle;
    @FXML private TextField tfVille;
    @FXML private Label lblResultatRecherche;
    @FXML private ComboBox<String> cbDestinations;
    @FXML private ImageView imgQRCode;
    @FXML private VBox vboxQR;
    @FXML private HBox hboxLoading;
    @FXML private Label lblUrlVideo;
    @FXML private TextArea taConsole;

    // ===== SERVICES =====
    private YouTubeService youtubeService;
    private DestinationService destinationService;
    private QRCodeService qrCodeService;

    // ===== DESTINATIONS CHARGÉES =====
    private List<Destination> destinations;

    // ===== INITIALISATION =====
    @FXML
    public void initialize() {
        youtubeService = new YouTubeService();
        destinationService = new DestinationService();
        qrCodeService = new QRCodeService();

        // Charger les destinations depuis la BDD
        chargerDestinations();

        log("✅ Interface de test initialisée");
        log("📋 Destinations chargées: " + (destinations != null ? destinations.size() : 0));
    }

    // ===== CHARGER LES DESTINATIONS =====
    private void chargerDestinations() {
        destinations = destinationService.getAll();

        if (destinations.isEmpty()) {
            cbDestinations.setItems(FXCollections.observableArrayList(
                    "⚠️ Aucune destination en BDD"
            ));
            log("⚠️ Aucune destination trouvée en BDD");
            log("   Ajoutez des destinations d'abord !");
        } else {
            cbDestinations.setItems(FXCollections.observableArrayList(
                    destinations.stream()
                            .map(d -> d.getNom() + " (ID:" + d.getId() + ")")
                            .toList()
            ));
            log("📋 Destinations disponibles:");
            destinations.forEach(d -> log("   - " + d.getNom() + " (ID:" + d.getId() + ")"));
        }
    }

    // ===== TEST CLÉ API =====
    @FXML
    private void handleTesterCle() {
        log("\n🧪 Test de la clé API YouTube...");
        btnTesterCle.setDisable(true);

        new Thread(() -> {
            boolean valide = youtubeService.testerCleAPI();

            Platform.runLater(() -> {
                if (valide) {
                    lblStatutCle.setText("✅ Clé API valide !");
                    lblStatutCle.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    log("✅ Clé API YouTube valide !");
                } else {
                    lblStatutCle.setText("❌ Clé API invalide !");
                    lblStatutCle.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    log("❌ Clé API invalide !");
                    log("   Vérifiez API_KEY dans YouTubeService.java");
                }
                btnTesterCle.setDisable(false);
            });
        }).start();
    }

    // ===== RECHERCHER UNE VIDÉO =====
    @FXML
    private void handleRechercherVideo() {
        String ville = tfVille.getText().trim();

        if (ville.isEmpty()) {
            lblResultatRecherche.setText("⚠️ Entrez le nom d'une ville !");
            return;
        }

        log("\n🔍 Recherche vidéo pour: " + ville);
        lblResultatRecherche.setText("⏳ Recherche en cours...");

        new Thread(() -> {
            // Test avec destination ID = -1 (sans BDD)
            String url = youtubeService.getVideoUrl(-1, ville);

            Platform.runLater(() -> {
                if (url != null) {
                    lblResultatRecherche.setText("✅ URL trouvée: " + url);
                    lblResultatRecherche.setStyle("-fx-text-fill: green;");
                    log("✅ URL: " + url);
                } else {
                    lblResultatRecherche.setText("❌ Aucune vidéo trouvée");
                    lblResultatRecherche.setStyle("-fx-text-fill: red;");
                    log("❌ Aucune vidéo trouvée pour: " + ville);
                }
            });
        }).start();
    }

    // ===== GÉNÉRER QR CODE =====
    @FXML
    private void handleGenererQRCode() {
        int selectedIndex = cbDestinations.getSelectionModel().getSelectedIndex();

        if (selectedIndex < 0 || destinations.isEmpty()) {
            log("⚠️ Sélectionnez une destination !");
            return;
        }

        Destination destination = destinations.get(selectedIndex);
        log("\n📱 Génération QR Code pour: " + destination.getNom());

        // Afficher loading
        vboxQR.setVisible(false);
        hboxLoading.setVisible(true);

        new Thread(() -> {
            // Obtenir l'URL YouTube
            String videoUrl = youtubeService.getVideoUrl(
                    destination.getId(),
                    destination.getNom()
            );

            // Générer le QR Code
            Image qrImage = qrCodeService.genererQRCode(videoUrl, 250);

            Platform.runLater(() -> {
                hboxLoading.setVisible(false);

                if (qrImage != null) {
                    imgQRCode.setImage(qrImage);
                    lblUrlVideo.setText("🔗 " + videoUrl);
                    vboxQR.setVisible(true);

                    log("✅ QR Code généré !");
                    log("   URL: " + videoUrl);
                    log("   📱 Scannez avec votre téléphone !");
                } else {
                    log("❌ Erreur génération QR Code");
                }
            });
        }).start();
    }

    // ===== VIDER CONSOLE =====
    @FXML
    private void handleViderConsole() {
        taConsole.clear();
    }

    // ===== LOG =====
    private void log(String message) {
        Platform.runLater(() -> {
            taConsole.appendText(message + "\n");
            System.out.println(message);
        });
    }
}