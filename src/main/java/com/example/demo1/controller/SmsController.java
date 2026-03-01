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

public class SmsController {

    // ===== FXML =====
    @FXML
    private ComboBox<String> cbTypeMessage;
    @FXML
    private TextField txtNumero;
    @FXML
    private TextField txtNomClient;
    @FXML
    private TextField txtDestination;
    @FXML
    private TextField txtPays;
    @FXML
    private TextField txtDateDepart;
    @FXML
    private TextField txtDateFin;
    @FXML
    private TextField txtHotel;
    @FXML
    private TextField txtPrix;
    @FXML
    private TextField txtReference;
    @FXML
    private Label lblStatut;
    @FXML
    private Label lblTestStatut;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private ProgressIndicator progressTest;
    @FXML
    private HBox panelTestResult;
    @FXML
    private VBox panelTestBox;

    // ===== TWILIO CONFIG =====
    // ⚠️ Remplace par tes vraies credentials Twilio
    private static final String ACCOUNT_SID = "AC4d6f990053c1a86ba9a524215025200d";
    private static final String AUTH_TOKEN = "ea03096dc60a8e59a0f045408f710b46";
    private static final String FROM_NUMBER = "+17854294619";
    // Pour production : "whatsapp:+TONVRAINUM"

    // ===== TWILIO CONFIG SMS =====

    // ============================================
    // 🚀 INITIALISATION
    // ============================================
    @FXML
    public void initialize() {
        cbTypeMessage.setItems(FXCollections.observableArrayList(
                "Confirmation de réservation",
                "Rappel de voyage",
                "Offre spéciale",
                "Message personnalisé"
        ));
        cbTypeMessage.setValue("Confirmation de réservation");

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
                        .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/"
                                + ACCOUNT_SID + ".json"))
                        .header("Authorization", "Basic " + encoded)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                int status = response.statusCode();
                Platform.runLater(() -> {
                    if (status == 200) {
                        setTestUI(false, "✅ Connexion Twilio réussie ! Prêt à envoyer des SMS.", "#4caf50");
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
    // 📤 ENVOYER SMS
    // ============================================
    @FXML
    private void envoyer() {
        String numero = txtNumero.getText().trim();

        if (numero.isEmpty()) {
            setStatut("⚠️ Veuillez entrer un numéro de téléphone", "#FF9800");
            return;
        }

        // S'assurer que le numéro commence par +
        if (!numero.startsWith("+")) {
            numero = "+" + numero;
        }

        String message = construireMessage();
        String toNumber = numero;

        setEnvoiUI(true, "⏳ Envoi du SMS en cours...", "#0A6CF1");

        final String finalNumero = toNumber;
        new Thread(() -> {
            try {
                String credentials = ACCOUNT_SID + ":" + AUTH_TOKEN;
                String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

                String body = "To=" + java.net.URLEncoder.encode(finalNumero, "UTF-8")
                        + "&From=" + java.net.URLEncoder.encode(FROM_NUMBER, "UTF-8")
                        + "&Body=" + java.net.URLEncoder.encode(message, "UTF-8");

//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create("https://textbelt.com/text"))
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .POST(HttpRequest.BodyPublishers.ofString(body))
//                        .build();

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
                System.out.println("Twilio response: " + response.body());

                Platform.runLater(() -> {
                    if (status == 201) {
                        setEnvoiUI(false, "✅ SMS envoyé avec succès à " + finalNumero + " !", "#4caf50");
                        viderFormulaire();
                    } else if (status == 401) {
                        setEnvoiUI(false, "❌ Credentials invalides. Vérifiez SID et Token.", "#f44336");
                    } else if (status == 400) {
                        setEnvoiUI(false, "❌ Erreur 400 : Numéro invalide ou non vérifié.", "#f44336");
                    } else if (status == 21608) {
                        setEnvoiUI(false, "❌ Le numéro n'est pas vérifié (compte trial).", "#f44336");
                    } else {
                        setEnvoiUI(false, "⚠️ Erreur " + status, "#f44336");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        setEnvoiUI(false, "❌ Erreur: " + e.getMessage(), "#f44336"));
            }
        }).start();
    }

    // ============================================
    // 📝 CONSTRUIRE LE MESSAGE SMS
    // ============================================
    private String construireMessage() {
        String nom = txtNomClient.getText().trim();
        String dest = txtDestination.getText().trim();
        String pays = txtPays.getText().trim();
        String depart = txtDateDepart.getText().trim();
        String fin = txtDateFin.getText().trim();
        String hotel = txtHotel.getText().trim();
        String prix = txtPrix.getText().trim();
        String ref = txtReference.getText().trim();
        String type = cbTypeMessage.getValue();

        String nomAffiche = nom.isEmpty() ? "Cher client" : "Bonjour " + nom;

        switch (type) {
            case "Confirmation de réservation":
                return "SmartTrip - Confirmation\n"
                        + nomAffiche + ",\n"
                        + "Reservation confirmee !\n"
                        + (dest.isEmpty() ? "" : "Dest: " + dest + (pays.isEmpty() ? "" : ", " + pays) + "\n")
                        + (depart.isEmpty() ? "" : "Depart: " + depart + "\n")
                        + (fin.isEmpty() ? "" : "Retour: " + fin + "\n")
                        + (hotel.isEmpty() ? "" : "Hotel: " + hotel + "\n")
                        + (prix.isEmpty() ? "" : "Prix: " + prix + " TND\n")
                        + (ref.isEmpty() ? "" : "Ref: " + ref + "\n")
                        + "Merci ! SmartTrip +216XXXXXXXX";

            case "Rappel de voyage":
                return "SmartTrip - Rappel\n"
                        + nomAffiche + ",\n"
                        + "Votre voyage approche !\n"
                        + (dest.isEmpty() ? "" : "Dest: " + dest + "\n")
                        + (depart.isEmpty() ? "" : "Depart: " + depart + "\n")
                        + (hotel.isEmpty() ? "" : "Hotel: " + hotel + "\n")
                        + "Bon voyage ! SmartTrip";

            case "Offre spéciale":
                return "SmartTrip - Offre Speciale\n"
                        + nomAffiche + ",\n"
                        + "Offre exclusive !\n"
                        + (dest.isEmpty() ? "" : "Dest: " + dest + "\n")
                        + (prix.isEmpty() ? "" : "A partir de: " + prix + " TND\n")
                        + (depart.isEmpty() ? "" : "Dispo: " + depart + "\n")
                        + "Reservez: +216XXXXXXXX";

            default:
                return "SmartTrip\n"
                        + nomAffiche + ",\n"
                        + (dest.isEmpty() ? "" : "Dest: " + dest + "\n")
                        + (depart.isEmpty() ? "" : "Depart: " + depart + "\n")
                        + (prix.isEmpty() ? "" : "Prix: " + prix + " TND\n")
                        + "Contact: +216XXXXXXXX";
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