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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.demo10.model.Avis;
import org.example.demo10.model.User;
import org.example.demo10.model.Vote;
import org.example.demo10.service.AvisService;
import org.example.demo10.service.VoteService;
import org.example.demo10.util.LoginDialog;
import org.example.demo10.dao.UserDAO;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ClientAvisController {

    @FXML private TextField txtClientNom;
    @FXML private TextField txtClientEmail;
    @FXML private ChoiceBox<Integer> choiceClientNote;
    @FXML private TextField txtClientVoyageId;
    @FXML private TextArea txtClientCommentaire;
    @FXML private Button btnSoumettre;
    @FXML private Button btnConnexion;

    @FXML private TextField txtRechercheClient;
    @FXML private ComboBox<String> comboFiltreNote;
    @FXML private Button btnRechercherClient;
    @FXML private Button btnResetClient;

    @FXML private Button btnStatistiquesClient;

    @FXML private VBox containerAvis;

    // Services
    private AvisService avisService;
    private VoteService voteService;
    private UserDAO userDAO;

    private ObservableList<Avis> avisClientList;
    private ObservableList<Avis> avisClientListFiltree;
    private Integer avisEnCoursModification;

    // Utilisateur connecté
    private User utilisateurConnecte;
    private boolean estConnecte = false;

    @FXML
    public void initialize() {
        avisService = new AvisService();
        voteService = new VoteService();
        userDAO = new UserDAO();

        avisClientList = FXCollections.observableArrayList();
        avisClientListFiltree = FXCollections.observableArrayList();
        avisEnCoursModification = null;

        // Initialiser le ChoiceBox des notes
        choiceClientNote.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));

        // Initialiser le ComboBox de filtrage
        comboFiltreNote.setItems(FXCollections.observableArrayList(
                "Toutes les notes", "5 étoiles", "4 étoiles", "3 étoiles", "2 étoiles", "1 étoile"
        ));
        comboFiltreNote.setValue("Toutes les notes");

        // Charger les avis
        chargerAvisClients();

        // Configuration des boutons
        btnSoumettre.setOnAction(e -> ajouterOuModifierAvis());
        btnRechercherClient.setOnAction(e -> rechercherAvisClient());
        btnResetClient.setOnAction(e -> resetRechercheClient());
        btnStatistiquesClient.setOnAction(e -> ouvrirStatistiquesClient());
        btnConnexion.setOnAction(e -> gererConnexion());

        // Message de bienvenue
        showAlert("Bienvenue", "Bienvenue dans l'espace client. Connectez-vous pour voter !");
    }

    private void chargerAvisClients() {
        avisClientList.clear();
        avisClientList.addAll(avisService.getAllAvis());
        avisClientListFiltree.setAll(avisClientList);
        afficherCartesAvis();
    }

    private void afficherCartesAvis() {
        containerAvis.getChildren().clear();

        if (avisClientListFiltree.isEmpty()) {
            Label lblAucunAvis = new Label("Aucun avis à afficher. Soyez le premier à donner votre avis !");
            lblAucunAvis.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
            containerAvis.getChildren().add(lblAucunAvis);
            return;
        }

        for (Avis avis : avisClientListFiltree) {
            VBox carteAvis = creerCarteAvis(avis);
            containerAvis.getChildren().add(carteAvis);
        }
    }

    private VBox creerCarteAvis(Avis avis) {
        VBox carte = new VBox();
        carte.setSpacing(10);
        carte.setPadding(new Insets(20));
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 15; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // En-tête de la carte
        HBox enTete = new HBox();
        enTete.setSpacing(15);
        enTete.setAlignment(Pos.CENTER_LEFT);

        // Avatar avec initiale
        Circle avatar = new Circle(25);
        avatar.setFill(getCouleurParNote(avis.getNote()));

        Label initiale = new Label(getInitiale(avis.getNomClient()));
        initiale.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        initiale.setTextFill(Color.WHITE);
        StackPane avatarContainer = new StackPane(avatar, initiale);

        // Informations du client
        VBox infoClient = new VBox(5);
        Label lblNom = new Label(avis.getNomClient());
        lblNom.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblEmail = new Label(avis.getEmail());
        lblEmail.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        infoClient.getChildren().addAll(lblNom, lblEmail);

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        // Date
        Label lblDate = new Label(formatDate(avis.getDateAvis()));
        lblDate.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        enTete.getChildren().addAll(avatarContainer, infoClient, espace, lblDate);

        // Note sous forme d'étoiles
        HBox noteContainer = new HBox(5);
        noteContainer.setAlignment(Pos.CENTER_LEFT);

        for (int i = 1; i <= 5; i++) {
            Label etoile = new Label("★");
            if (i <= avis.getNote()) {
                etoile.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 20px;");
            } else {
                etoile.setStyle("-fx-text-fill: #e9ecef; -fx-font-size: 20px;");
            }
            noteContainer.getChildren().add(etoile);
        }

        Label lblNote = new Label(" " + avis.getNote() + "/5");
        lblNote.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        noteContainer.getChildren().add(lblNote);

        // Commentaire
        TextArea commentaire = new TextArea(avis.getCommentaire());
        commentaire.setEditable(false);
        commentaire.setWrapText(true);
        commentaire.setPrefRowCount(3);
        commentaire.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                "-fx-font-size: 14px; -fx-text-fill: #495057;");

        // Voyage ID
        HBox footer = new HBox();
        footer.setSpacing(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label lblVoyage = new Label("Voyage #" + avis.getVoyageId());
        lblVoyage.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #495057; " +
                "-fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 12px;");

        footer.getChildren().add(lblVoyage);

        // ========== SECTION DES VOTES ==========
        HBox votesSection = new HBox(15);
        votesSection.setAlignment(Pos.CENTER_LEFT);
        votesSection.setPadding(new Insets(10, 0, 5, 0));

        // Statistiques des votes
        Label lblVotes = new Label(avis.getStatistiquesVotes());
        lblVotes.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");

        // Bouton Utile
        Button btnUtile = new Button("👍 Utile (" + avis.getVotesUtiles() + ")");
        btnUtile.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11px;");

        // Bouton Pas Utile
        Button btnPasUtile = new Button("👎 (" + avis.getVotesPasUtiles() + ")");
        btnPasUtile.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11px;");

        // Score de pertinence
        Label lblScore = new Label("Score: " + avis.getScorePertinence());
        lblScore.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Vérifier si l'utilisateur est connecté et a déjà voté
        if (estConnecte && utilisateurConnecte != null) {
            Vote voteExistant = voteService.getVoteUtilisateur(avis.getId(), utilisateurConnecte.getId());

            if (voteExistant != null) {
                if (voteExistant.getType() == Vote.TypeVote.UTILE) {
                    btnUtile.setStyle("-fx-background-color: #155724; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11px;");
                    btnUtile.setDisable(true);
                    btnPasUtile.setDisable(true);
                } else if (voteExistant.getType() == Vote.TypeVote.PAS_UTILE) {
                    btnPasUtile.setStyle("-fx-background-color: #721c24; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11px;");
                    btnUtile.setDisable(true);
                    btnPasUtile.setDisable(true);
                }
            } else {
                // Actions des boutons de vote
                btnUtile.setOnAction(e -> voter(avis, true));
                btnPasUtile.setOnAction(e -> voter(avis, false));
            }
        } else {
            // Si non connecté, les boutons proposent de se connecter
            btnUtile.setOnAction(e -> proposerConnexion());
            btnPasUtile.setOnAction(e -> proposerConnexion());
        }

        votesSection.getChildren().addAll(lblVotes, btnUtile, btnPasUtile, lblScore);

        // Boutons de modification/suppression
        HBox boutonsAction = new HBox(10);
        boutonsAction.setAlignment(Pos.CENTER_LEFT);
        boutonsAction.setPadding(new Insets(5, 0, 0, 0));

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold; " +
                "-fx-padding: 8 15; -fx-background-radius: 20; -fx-font-size: 12px;");
        btnModifier.setOnAction(e -> preparerModification(avis));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 8 15; -fx-background-radius: 20; -fx-font-size: 12px;");
        btnSupprimer.setOnAction(e -> supprimerAvis(avis));

        boutonsAction.getChildren().addAll(btnModifier, btnSupprimer);

        // Ajouter tous les éléments à la carte
        carte.getChildren().addAll(enTete, noteContainer, commentaire, footer, votesSection, boutonsAction);

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

    // ========== GESTION DE LA CONNEXION ==========

    private void gererConnexion() {
        if (estConnecte) {
            // Déconnexion
            if (LoginDialog.showLogoutDialog(utilisateurConnecte.getNom())) {
                utilisateurConnecte = null;
                estConnecte = false;
                btnConnexion.setText("👤 Connexion");
                showAlert("Déconnexion", "Vous avez été déconnecté.");
                chargerAvisClients();
            }
        } else {
            // Connexion
            Optional<User> userOpt = LoginDialog.showLoginDialog();
            if (userOpt.isPresent()) {
                utilisateurConnecte = userOpt.get();
                estConnecte = true;
                btnConnexion.setText("👤 " + utilisateurConnecte.getNom());
                showAlert("Connexion réussie", "Bienvenue " + utilisateurConnecte.getNom() + " !");
                chargerAvisClients();
            }
        }
    }

    // ========== GESTION DES VOTES ==========

    private void voter(Avis avis, boolean estUtile) {
        if (!estConnecte || utilisateurConnecte == null) {
            proposerConnexion();
            return;
        }

        boolean success = voteService.voter(avis.getId(), utilisateurConnecte.getId(), estUtile);

        if (success) {
            String message = estUtile ? "👍 Vous avez trouvé cet avis utile !" : "👎 Vous avez marqué cet avis comme pas utile.";
            showAlert("Vote enregistré", message);

            // Recharger les avis pour mettre à jour les compteurs
            chargerAvisClients();
        } else {
            showAlert("Erreur", "Impossible d'enregistrer votre vote.");
        }
    }

    private void proposerConnexion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Connexion requise");
        alert.setHeaderText("Vous devez être connecté pour voter");
        alert.setContentText("Voulez-vous vous connecter maintenant ?");

        ButtonType btnOui = new ButtonType("Oui, me connecter");
        ButtonType btnNon = new ButtonType("Non, plus tard");

        alert.getButtonTypes().setAll(btnOui, btnNon);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnOui) {
                gererConnexion();
            }
        });
    }

    // ========== GESTION DES AVIS ==========

    private void preparerModification(Avis avis) {
        avisEnCoursModification = avis.getId();

        // Pré-remplir le formulaire
        txtClientNom.setText(avis.getNomClient());
        txtClientEmail.setText(avis.getEmail());
        choiceClientNote.setValue(avis.getNote());
        txtClientVoyageId.setText(String.valueOf(avis.getVoyageId()));
        txtClientCommentaire.setText(avis.getCommentaire());

        // Changer le bouton "Publier" en "Mettre à jour"
        btnSoumettre.setText("Mettre à jour");
        btnSoumettre.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold; " +
                "-fx-padding: 12 25; -fx-background-radius: 25; -fx-font-size: 14px;");
    }

    private void supprimerAvis(Avis avis) {
        // Confirmation de suppression
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'avis");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet avis ?\n" +
                "Nom: " + avis.getNomClient() + "\n" +
                "Email: " + avis.getEmail() + "\n" +
                "Note: " + avis.getNote() + "/5\n" +
                "Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = avisService.supprimerAvis(avis.getId());

                if (success) {
                    showAlert("Succès", "Avis supprimé avec succès!");
                    resetForm();
                    chargerAvisClients();
                } else {
                    showAlert("Erreur", "Erreur lors de la suppression de l'avis.");
                }
            }
        });
    }

    private void ajouterOuModifierAvis() {
        try {
            String nom = txtClientNom.getText().trim();
            String email = txtClientEmail.getText().trim();
            Integer note = choiceClientNote.getValue();
            String commentaire = txtClientCommentaire.getText().trim();

            if (txtClientVoyageId.getText().trim().isEmpty()) {
                showAlert("Erreur", "Veuillez entrer un ID de voyage.");
                return;
            }

            int voyageId = Integer.parseInt(txtClientVoyageId.getText().trim());

            if (nom.isEmpty() || email.isEmpty() || note == null || commentaire.isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs.");
                return;
            }

            if (note < 1 || note > 5) {
                showAlert("Erreur", "La note doit être entre 1 et 5.");
                return;
            }

            // Vérifier si on est en mode modification
            if (avisEnCoursModification != null) {
                // Mode modification
                Avis avis = avisService.getAvisById(avisEnCoursModification);
                if (avis != null) {
                    avis.setNomClient(nom);
                    avis.setEmail(email);
                    avis.setNote(note);
                    avis.setCommentaire(commentaire);
                    avis.setDateAvis(LocalDate.now());
                    avis.setVoyageId(voyageId);

                    boolean success = avisService.modifierAvis(avis);

                    if (success) {
                        showAlert("Succès", "✅ Avis modifié avec succès!");
                        resetForm();
                        chargerAvisClients();
                    } else {
                        showAlert("Erreur", "❌ Erreur lors de la modification de l'avis.");
                    }
                }
            } else {
                // Mode ajout - L'EMAIL SERA ENVOYÉ AUTOMATIQUEMENT PAR LE SERVICE
                boolean success = avisService.ajouterAvisClient(nom, email, note, commentaire, voyageId);

                if (success) {
                    showAlert("Succès", "✅ Votre avis a été publié avec succès!\n📧 Un email de notification a été envoyé à l'administrateur.");
                    resetForm();
                    chargerAvisClients();
                } else {
                    showAlert("Erreur", "❌ Vous avez déjà donné un avis pour ce voyage.");
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un ID de voyage valide.");
        }
    }

    // ========== RECHERCHE ==========

    private void rechercherAvisClient() {
        String recherche = txtRechercheClient.getText().trim();
        String filtreNote = comboFiltreNote.getValue();

        if (recherche.isEmpty() && filtreNote.equals("Toutes les notes")) {
            avisClientListFiltree.setAll(avisClientList);
            afficherCartesAvis();
            return;
        }

        List<Avis> resultats = new ArrayList<>(avisClientList);

        // Filtrer par NOM
        if (!recherche.isEmpty()) {
            String motCle = recherche.toLowerCase();
            List<Avis> avisFiltres = new ArrayList<>();

            for (Avis avis : resultats) {
                if (avis.getNomClient().toLowerCase().contains(motCle)) {
                    avisFiltres.add(avis);
                }
            }
            resultats = avisFiltres;
        }

        // Filtrer par note
        if (!filtreNote.equals("Toutes les notes")) {
            Integer noteFiltre = null;
            switch (filtreNote) {
                case "5 étoiles": noteFiltre = 5; break;
                case "4 étoiles": noteFiltre = 4; break;
                case "3 étoiles": noteFiltre = 3; break;
                case "2 étoiles": noteFiltre = 2; break;
                case "1 étoile": noteFiltre = 1; break;
            }

            if (noteFiltre != null) {
                List<Avis> avisFiltres = new ArrayList<>();
                for (Avis avis : resultats) {
                    if (avis.getNote() == noteFiltre) {
                        avisFiltres.add(avis);
                    }
                }
                resultats = avisFiltres;
            }
        }

        avisClientListFiltree.setAll(FXCollections.observableArrayList(resultats));
        afficherCartesAvis();

        if (avisClientListFiltree.isEmpty()) {
            showAlert("Information", "Aucun avis trouvé pour votre recherche.");
        }
    }

    private void resetRechercheClient() {
        txtRechercheClient.clear();
        comboFiltreNote.setValue("Toutes les notes");
        avisClientListFiltree.setAll(avisClientList);
        afficherCartesAvis();
    }

    // ========== STATISTIQUES ==========

    private void ouvrirStatistiquesClient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo10/client-statistiques.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Statistiques des Avis");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les statistiques : " + e.getMessage());
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void clearForm() {
        txtClientNom.clear();
        txtClientEmail.clear();
        choiceClientNote.setValue(null);
        txtClientVoyageId.clear();
        txtClientCommentaire.clear();
    }

    private void resetForm() {
        clearForm();
        avisEnCoursModification = null;
        btnSoumettre.setText("Publier");
        btnSoumettre.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12 25; -fx-background-radius: 25; -fx-font-size: 14px;");
    }

    private String getInitiale(String nom) {
        if (nom == null || nom.isEmpty()) {
            return "?";
        }
        return nom.substring(0, 1).toUpperCase();
    }

    private Color getCouleurParNote(int note) {
        switch (note) {
            case 5: return Color.web("#28a745");
            case 4: return Color.web("#17a2b8");
            case 3: return Color.web("#ffc107");
            case 2: return Color.web("#fd7e14");
            case 1: return Color.web("#dc3545");
            default: return Color.web("#6c757d");
        }
    }

    private String formatDate(LocalDate date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    .withLocale(Locale.FRENCH);
            return date.format(formatter);
        } catch (Exception e) {
            return date.toString();
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