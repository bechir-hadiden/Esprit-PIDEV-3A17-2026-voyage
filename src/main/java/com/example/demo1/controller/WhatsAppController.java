package com.example.demo1.controller;

import com.example.demo1.services.WhatsAppService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Random;

public class WhatsAppController {

    @FXML private TextField txtNumero;
    @FXML private TextField txtNomClient;
    @FXML private TextField txtDestination;
    @FXML private TextField txtDateDepart;
    @FXML private TextField txtDateFin;
    @FXML private TextField txtReference;
    @FXML private TextField txtPrix;
    @FXML private TextField txtHotel;
    @FXML private TextField txtPays;

    @FXML private ComboBox<String> cbTypeMessage;
    @FXML private Label lblStatut;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private VBox panelChamps;

    @FXML
    public void initialize() {
        cbTypeMessage.getItems().addAll(
                "Confirmation de Reservation",
                "Rappel Veille de Depart",
                "Alerte Modification",
                "Code OTP",
                "Itineraire Complet"
        );
        cbTypeMessage.setValue("Confirmation de Reservation");
        progressIndicator.setVisible(false);
    }

    // ============================================
    // ENVOYER selon le type sélectionné
    // ============================================
    @FXML
    public void envoyer() {
        String type = cbTypeMessage.getValue();
        if (type == null) return;

        String numero = txtNumero.getText().trim();
        if (numero.isEmpty()) {
            afficherStatut("Numero de telephone requis !", false);
            return;
        }

        progressIndicator.setVisible(true);
        lblStatut.setText("Envoi en cours...");

        new Thread(() -> {
            boolean succes = false;

            switch (type) {
                case "Confirmation de Reservation" -> {
                    succes = WhatsAppService.envoyerConfirmationReservation(
                            numero,
                            txtNomClient.getText().trim(),
                            txtDestination.getText().trim(),
                            txtDateDepart.getText().trim(),
                            txtDateFin.getText().trim(),
                            txtReference.getText().trim(),
                            parseDouble(txtPrix.getText())
                    );
                }
                case "Rappel Veille de Depart" -> {
                    succes = WhatsAppService.envoyerRappelVeille(
                            numero,
                            txtNomClient.getText().trim(),
                            txtDestination.getText().trim(),
                            txtDateDepart.getText().trim(),
                            txtHotel.getText().trim()  // utilisé comme aéroport ici
                    );
                }
                case "Alerte Modification" -> {
                    succes = WhatsAppService.envoyerAlerteModification(
                            numero,
                            txtNomClient.getText().trim(),
                            txtDestination.getText().trim(),
                            "modifie",
                            "Veuillez contacter notre service client."
                    );
                }
                case "Code OTP" -> {
                    String otp = genererOTP();
                    succes = WhatsAppService.envoyerOTP(numero, otp);
                }
                case "Itineraire Complet" -> {
                    succes = WhatsAppService.envoyerItineraire(
                            numero,
                            txtNomClient.getText().trim(),
                            txtDestination.getText().trim(),
                            txtPays.getText().trim(),
                            txtDateDepart.getText().trim(),
                            txtDateFin.getText().trim(),
                            txtHotel.getText().trim(),
                            txtReference.getText().trim()
                    );
                }
            }

            final boolean resultat = succes;
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                if (resultat) {
                    afficherStatut("WhatsApp envoye avec succes !", true);
                } else {
                    afficherStatut("Echec de l envoi. Verifiez vos credentials Twilio.", false);
                }
            });
        }).start();
    }

    // ============================================
    // HELPERS
    // ============================================
    private void afficherStatut(String message, boolean succes) {
        lblStatut.setText(message);
        lblStatut.setStyle(succes
                ? "-fx-text-fill: #4caf50; -fx-font-weight: bold;"
                : "-fx-text-fill: #e53935; -fx-font-weight: bold;");
    }

    private double parseDouble(String val) {
        try { return Double.parseDouble(val.replace(",", ".")); }
        catch (Exception e) { return 0; }
    }

    private String genererOTP() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}