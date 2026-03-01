package com.example.demo1.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class WhatsAppController {

    // ===== FXML =====
    @FXML private ComboBox<String> cbTypeMessage;
    @FXML private TextField txtNumero;
    @FXML private TextField txtNomClient;
    @FXML private TextField txtDestination;
    @FXML private TextField txtPays;
    @FXML private TextField txtDateDepart;
    @FXML private TextField txtDateFin;
    @FXML private TextField txtHotel;
    @FXML private TextField txtPrix;
    @FXML private TextField txtReference;
    @FXML private Label lblStatut;
    @FXML private Label lblTestStatut;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private ProgressIndicator progressTest;
    @FXML private HBox panelTestResult;
    @FXML private VBox panelTestBox;

    // ===== TWILIO CONFIG =====
    // ⚠️ Remplace par tes vraies credentials Twilio
    private static final String ACCOUNT_SID = "AC4d6f990053c1a86ba9a524215025200d";
    private static final String AUTH_TOKEN   = "FHTQTZ33NFY3MJCD9C5BSBJD";
    private static final String FROM_NUMBER  = "whatsapp:+17854294619" ; // Sandbox Twilio
    // Pour production : "whatsapp:+TONVRAINUM"

    // ============================================
    // 🚀 INITIALISATION
    // ============================================
    @FXML
    public void initialize() {
        // Types de messages disponibles
        cbTypeMessage.setItems(FXCollections.observableArrayList(
                "Confirmation de réservation",
                "Rappel de voyage",
                "Offre spéciale",
                "Message personnalisé"
        ));
        cbTypeMessage.setValue("Confirmation de réservation");

        // Cacher les indicateurs au départ
        if (progressIndicator != null) progressIndicator.setVisible(false);
        if (progressTest != null) progressTest.setVisible(false);
        if (panelTestResult != null) panelTestResult.setVisible(false);
    }

    // ============================================
    // 🔌 TESTER LA CONNEXION TWILIO
    // ============================================
    @FXML
    private void testerConnexion() {
        setTestUI(true, "⏳ Test en cours...", "#0A6CF1");

        new Thread(() -> {
            try {
                String credentials = ACCOUNT_SID + ":" + AUTH_TOKEN;
                String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + ACCOUNT_SID + ".json"))
                        .header("Authorization", "Basic " + encoded)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                int status = response.statusCode();
                Platform.runLater(() -> {
                    if (status == 200) {
                        setTestUI(false, "✅ Connexion Twilio réussie ! Credentials valides.", "#4caf50");
                    } else if (status == 401) {
                        setTestUI(false, "❌ Credentials invalides (401). Vérifiez SID et Token.", "#f44336");
                    } else {
                        setTestUI(false, "⚠️ Réponse inattendue: " + status, "#FF9800");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        setTestUI(false, "❌ Erreur réseau: " + e.getMessage(), "#f44336"));
            }
        }).start();
    }

    // ============================================
    // 📤 ENVOYER MESSAGE WHATSAPP
    // ============================================
    @FXML
    private void envoyer() {
        String numero = txtNumero.getText().trim();

        if (numero.isEmpty()) {
            setStatut("⚠️ Veuillez entrer un numéro WhatsApp", "#FF9800");
            return;
        }

        // Construire le message selon le type
        String message = construireMessage();
        String toNumber = "whatsapp:+" + numero;

        setEnvoiUI(true, "⏳ Envoi en cours...", "#0A6CF1");

        new Thread(() -> {
            try {
                String credentials = ACCOUNT_SID + ":" + AUTH_TOKEN;
                String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

                String body = "To=" + java.net.URLEncoder.encode(toNumber, "UTF-8")
                        + "&From=" + java.net.URLEncoder.encode(FROM_NUMBER, "UTF-8")
                        + "&Body=" + java.net.URLEncoder.encode(message, "UTF-8");

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/"
                                + ACCOUNT_SID + "/Messages.json"))
                        .header("Authorization", "Basic " + encoded)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                int status = response.statusCode();
                Platform.runLater(() -> {
                    if (status == 201) {
                        setEnvoiUI(false,
                                "✅ Message WhatsApp envoyé avec succès !", "#4caf50");
                        viderFormulaire();
                    } else if (status == 401) {
                        setEnvoiUI(false,
                                "❌ Credentials invalides. Vérifiez SID et Token.", "#f44336");
                    } else if (status == 400) {
                        setEnvoiUI(false,
                                "❌ Erreur 400 : Le client n'a pas rejoint le sandbox Twilio.", "#f44336");
                    } else {
                        setEnvoiUI(false,
                                "⚠️ Erreur " + status + ": " + response.body(), "#f44336");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        setEnvoiUI(false, "❌ Erreur: " + e.getMessage(), "#f44336"));
            }
        }).start();
    }

    // ============================================
    // 📝 CONSTRUIRE LE MESSAGE
    // ============================================
    private String construireMessage() {
        String nom     = txtNomClient.getText().trim();
        String dest    = txtDestination.getText().trim();
        String pays    = txtPays.getText().trim();
        String depart  = txtDateDepart.getText().trim();
        String fin     = txtDateFin.getText().trim();
        String hotel   = txtHotel.getText().trim();
        String prix    = txtPrix.getText().trim();
        String ref     = txtReference.getText().trim();
        String type    = cbTypeMessage.getValue();

        String nomAffiche = nom.isEmpty() ? "Cher client" : "Cher(e) " + nom;

        switch (type) {
            case "Confirmation de réservation":
                return "✈️ *SmartTrip - Confirmation de Réservation*\n\n"
                        + nomAffiche + ",\n\n"
                        + "Votre réservation est confirmée !\n\n"
                        + (dest.isEmpty()  ? "" : "🌍 *Destination :* " + dest + (pays.isEmpty() ? "" : ", " + pays) + "\n")
                        + (depart.isEmpty() ? "" : "📅 *Départ :* " + depart + "\n")
                        + (fin.isEmpty()    ? "" : "📅 *Retour :* " + fin + "\n")
                        + (hotel.isEmpty()  ? "" : "🏨 *Hôtel :* " + hotel + "\n")
                        + (prix.isEmpty()   ? "" : "💰 *Prix total :* " + prix + " TND\n")
                        + (ref.isEmpty()    ? "" : "📋 *Référence :* " + ref + "\n")
                        + "\nMerci de votre confiance !\n"
                        + "📞 SmartTrip - +216 XX XXX XXX";

            case "Rappel de voyage":
                return "⏰ *SmartTrip - Rappel de Voyage*\n\n"
                        + nomAffiche + ",\n\n"
                        + "Votre voyage approche ! 🎉\n\n"
                        + (dest.isEmpty()  ? "" : "🌍 *Destination :* " + dest + "\n")
                        + (depart.isEmpty() ? "" : "📅 *Date de départ :* " + depart + "\n")
                        + (hotel.isEmpty()  ? "" : "🏨 *Hôtel :* " + hotel + "\n")
                        + "\nPensez à préparer vos documents de voyage.\n"
                        + "Bon voyage ! ✈️\n"
                        + "📞 SmartTrip - +216 XX XXX XXX";

            case "Offre spéciale":
                return "🎁 *SmartTrip - Offre Spéciale*\n\n"
                        + nomAffiche + ",\n\n"
                        + "Nous avons une offre exclusive pour vous !\n\n"
                        + (dest.isEmpty()  ? "" : "🌍 *Destination :* " + dest + "\n")
                        + (prix.isEmpty()   ? "" : "💰 *À partir de :* " + prix + " TND\n")
                        + (depart.isEmpty() ? "" : "📅 *Disponible dès le :* " + depart + "\n")
                        + "\nContactez-nous pour réserver !\n"
                        + "📞 SmartTrip - +216 XX XXX XXX";

            default: // Message personnalisé
                return "✈️ *SmartTrip*\n\n"
                        + nomAffiche + ",\n\n"
                        + (dest.isEmpty()  ? "" : "🌍 Destination : " + dest + "\n")
                        + (depart.isEmpty() ? "" : "📅 Départ : " + depart + "\n")
                        + (prix.isEmpty()   ? "" : "💰 Prix : " + prix + " TND\n")
                        + "\nPour plus d'informations, contactez-nous.\n"
                        + "📞 SmartTrip - +216 XX XXX XXX";
        }
    }

    // ============================================
    // 🔧 HELPERS UI
    // ============================================
    private void setTestUI(boolean loading, String message, String color) {
        if (progressTest != null) progressTest.setVisible(loading);
        if (panelTestResult != null) panelTestResult.setVisible(true);
        if (lblTestStatut != null) {
            lblTestStatut.setText(message);
            lblTestStatut.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";");
        }
    }

    private void setEnvoiUI(boolean loading, String message, String color) {
        if (progressIndicator != null) progressIndicator.setVisible(loading);
        setStatut(message, color);
    }

    private void setStatut(String message, String color) {
        if (lblStatut != null) {
            lblStatut.setText(message);
            lblStatut.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 12px;" +
                    "-fx-text-fill: " + color + ";");
        }
    }

    private void viderFormulaire() {
        txtNumero.clear();
        txtNomClient.clear();
        txtDestination.clear();
        txtPays.clear();
        txtDateDepart.clear();
        txtDateFin.clear();
        txtHotel.clear();
        txtPrix.clear();
        txtReference.clear();
    }
}