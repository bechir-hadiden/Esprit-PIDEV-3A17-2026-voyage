package com.example.demo1.controller;

import com.example.demo1.entity.Reclamation;
import com.example.demo1.services.EmailService;
import com.example.demo1.services.ReclamationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ContactController {

    // ----- COMPOSANTS FXML -----
    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextArea txtDeclaration;
    @FXML private Label lblCaracteres;
    @FXML private Button btnEnvoyer;

    // ----- SERVICES -----
    private ReclamationService reclamationService;

    // ----- CONSTANTES -----
    private static final int MAX_CARACTERES = 1000;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // ----- INITIALISATION -----
    @FXML
    private void initialize() {
        System.out.println("✅ Initialisation du ContactController...");

        // Initialiser le service
        reclamationService = new ReclamationService();

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

        // Créer l'objet Reclamation
        Reclamation reclamation = new Reclamation(nom, email, telephone, message);

        // Construction du message complet pour l'email
        String messageComplet = construireMessage(nom, email, telephone, message);

        // Désactivation pendant l'envoi
        desactiverFormulaire(true);

        // Traitement en arrière-plan
        new Thread(() -> {
            boolean successDB = false;
            boolean successEmail = false;

            try {
                // 1. Enregistrer dans la base de données
                System.out.println("💾 Enregistrement dans la base de données...");
                successDB = reclamationService.ajouterReclamation(reclamation);

                // 2. Envoyer l'email
                System.out.println("📧 Envoi de l'email...");
                successEmail = EmailService.envoyerDeclaration(nom, email, messageComplet);

            } catch (Exception e) {
                System.err.println("❌ Erreur lors du traitement : " + e.getMessage());
                e.printStackTrace();
            }

            // Retour sur le thread JavaFX
            final boolean dbSuccess = successDB;
            final boolean emailSuccess = successEmail;

            Platform.runLater(() -> {
                desactiverFormulaire(false);

                if (dbSuccess && emailSuccess) {
                    // Succès complet
                    afficherSuccesComplet(reclamation.getId());
                    reinitialiserFormulaire();
                } else if (dbSuccess && !emailSuccess) {
                    // Base de données OK, mais email échoué
                    afficherSuccesPartiel(reclamation.getId());
                    reinitialiserFormulaire();
                } else if (!dbSuccess && emailSuccess) {
                    // Email OK, mais base de données échouée
                    afficherAvertissement();
                    reinitialiserFormulaire();
                } else {
                    // Échec complet
                    afficherErreur("Impossible d'enregistrer votre réclamation.\nVeuillez réessayer plus tard.");
                }
            });
        }).start();
    }

    /**
     * Construit le message formaté pour l'email
     */
    private String construireMessage(String nom, String email, String telephone, String message) {
        StringBuilder msg = new StringBuilder();
        msg.append("=== NOUVELLE RÉCLAMATION CLIENT ===\n\n");
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
            btnEnvoyer.setText("⏳ Traitement en cours...");
            btnEnvoyer.setDisable(true);
        } else {
            btnEnvoyer.setText("📤 Envoyer");
            btnEnvoyer.setDisable(false);
        }
    }

    /**
     * Affiche un message de succès complet
     */
    private void afficherSuccesComplet(int reclamationId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("✅ Réclamation enregistrée avec succès !");
        alert.setContentText(
                "Numéro de réclamation : #" + reclamationId + "\n\n" +
                        "Votre réclamation a été enregistrée dans notre système.\n" +
                        "Un email de confirmation vous a été envoyé.\n\n" +
                        "Nous traiterons votre demande dans les plus brefs délais.\n\n" +
                        "Merci de votre confiance !"
        );
        alert.showAndWait();
    }

    /**
     * Affiche un message de succès partiel (DB OK, Email KO)
     */
    private void afficherSuccesPartiel(int reclamationId) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Réclamation enregistrée");
        alert.setHeaderText("⚠️ Réclamation enregistrée (email non envoyé)");
        alert.setContentText(
                "Numéro de réclamation : #" + reclamationId + "\n\n" +
                        "Votre réclamation a été enregistrée avec succès.\n" +
                        "Cependant, l'email de confirmation n'a pas pu être envoyé.\n\n" +
                        "Nous vous contacterons directement pour le suivi."
        );
        alert.showAndWait();
    }

    /**
     * Affiche un avertissement (Email OK, DB KO)
     */
    private void afficherAvertissement() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Avertissement");
        alert.setHeaderText("⚠️ Email envoyé");
        alert.setContentText(
                "Votre email a été envoyé avec succès.\n" +
                        "Cependant, une erreur est survenue lors de l'enregistrement.\n\n" +
                        "Nous avons bien reçu votre message par email."
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
