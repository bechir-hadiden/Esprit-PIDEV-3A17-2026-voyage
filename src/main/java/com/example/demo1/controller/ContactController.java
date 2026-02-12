package com.example.demo1.controller;

import com.example.demo1.services.EmailService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ContactController {

    // ----- COMPOSANTS FXML -----
    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    // ❌ SUPPRIMÉ : @FXML private ComboBox<String> cmbType;
    @FXML private TextArea txtDeclaration;
    @FXML private Label lblCaracteres;
    @FXML private Button btnEnvoyer;
    // ❌ SUPPRIMÉ : @FXML private Button btnReinitialiser;

    // ----- CONSTANTES -----
    private static final int MAX_CARACTERES = 1000;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // ----- INITIALISATION -----
    @FXML
    private void initialize() {
        System.out.println("✅ Initialisation du ContactController...");
        configurerCompteurCaracteres();
        System.out.println("✅ Initialisation terminée");
    }

    /**
     * Configure le compteur de caractères pour le TextArea
     */
    private void configurerCompteurCaracteres() {
        if (txtDeclaration != null && lblCaracteres != null) {
            txtDeclaration.textProperty().addListener((obs, oldVal, newVal) -> {
                int longueur = newVal.length();
                lblCaracteres.setText(longueur + " / " + MAX_CARACTERES + " caractères");

                // Limite de caractères
                if (longueur > MAX_CARACTERES) {
                    txtDeclaration.setText(oldVal);
                }

                // Changement de couleur si limite proche
                if (longueur > MAX_CARACTERES * 0.9) {
                    lblCaracteres.setStyle("-fx-text-fill: #d32f2f;");
                } else {
                    lblCaracteres.setStyle("-fx-text-fill: #666;");
                }
            });
        }
    }

    /**
     * Gère l'envoi du message
     */
    @FXML
    private void handleEnvoyer() {
        System.out.println("📤 Bouton Envoyer cliqué");

        // Validation des champs
        if (!validerFormulaire()) {
            return;
        }

        // Récupération des données
        String nom = txtNom.getText().trim();
        String email = txtEmail.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String message = txtDeclaration.getText().trim();

        // Construction du message complet
        String messageComplet = construireMessage(nom, email, telephone, message);

        // Désactivation pendant l'envoi
        desactiverFormulaire(true);

        // Envoi en arrière-plan
        new Thread(() -> {
            boolean succes = EmailService.envoyerDeclaration(nom, email, messageComplet);

            // Retour sur le thread JavaFX
            Platform.runLater(() -> {
                desactiverFormulaire(false);

                if (succes) {
                    afficherSucces();
                    reinitialiserFormulaire();
                } else {
                    afficherErreur("Impossible d'envoyer le message.\nVeuillez réessayer plus tard.");
                }
            });
        }).start();
    }

    /**
     * Construit le message formaté pour l'email
     */
    private String construireMessage(String nom, String email, String telephone, String message) {
        StringBuilder msg = new StringBuilder();
        msg.append("=== INFORMATIONS DU CLIENT ===\n\n");
        msg.append("Nom: ").append(nom).append("\n");
        msg.append("Email: ").append(email).append("\n");

        if (!telephone.isEmpty()) {
            msg.append("Téléphone: ").append(telephone).append("\n");
        }

        msg.append("\n=== MESSAGE ===\n\n");
        msg.append(message);

        return msg.toString();
    }

    /**
     * Valide tous les champs du formulaire
     */
    private boolean validerFormulaire() {
        // Vérification nom
        if (txtNom.getText().trim().isEmpty()) {
            afficherErreur("Veuillez entrer votre nom complet.");
            txtNom.requestFocus();
            return false;
        }

        // Vérification email
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            afficherErreur("Veuillez entrer votre adresse email.");
            txtEmail.requestFocus();
            return false;
        }

        if (!email.matches(EMAIL_REGEX)) {
            afficherErreur("L'adresse email n'est pas valide.\nExemple: exemple@email.com");
            txtEmail.requestFocus();
            return false;
        }

        // Vérification message
        String message = txtDeclaration.getText().trim();
        if (message.isEmpty()) {
            afficherErreur("Veuillez entrer votre message.");
            txtDeclaration.requestFocus();
            return false;
        }

        if (message.length() < 10) {
            afficherErreur("Le message doit contenir au moins 10 caractères.");
            txtDeclaration.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Réinitialise tous les champs
     */
    private void reinitialiserFormulaire() {
        txtNom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtDeclaration.clear();
        txtNom.requestFocus();
    }

    /**
     * Active/désactive le formulaire
     */
    private void desactiverFormulaire(boolean desactiver) {
        txtNom.setDisable(desactiver);
        txtEmail.setDisable(desactiver);
        txtTelephone.setDisable(desactiver);
        txtDeclaration.setDisable(desactiver);

        if (desactiver) {
            btnEnvoyer.setText("⏳ Envoi en cours...");
            btnEnvoyer.setDisable(true);
        } else {
            btnEnvoyer.setText("📤 Envoyer");
            btnEnvoyer.setDisable(false);
        }
    }

    /**
     * Affiche un message de succès
     */
    private void afficherSucces() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("✅ Message envoyé !");
        alert.setContentText(
                "Votre message a été envoyé avec succès.\n\n" +
                        "Nous vous répondrons dans les plus brefs délais à l'adresse email que vous avez fournie.\n\n" +
                        "Merci de votre confiance !"
        );
        alert.showAndWait();
    }

    /**
     * Affiche un message d'erreur
     */
    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("❌ Une erreur est survenue");
        alert.setContentText(message);
        alert.showAndWait();
    }
}