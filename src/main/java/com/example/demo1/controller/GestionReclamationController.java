package com.example.demo1.controller;

import com.example.demo1.entity.Reclamation;
import com.example.demo1.services.EmailService;
import com.example.demo1.services.ReclamationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GestionReclamationController {

    // ----- COMPOSANTS FXML -----
    // Header
    @FXML private Label lblStatistiques;
    @FXML private Button btnActualiser;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private TextField txtRecherche;

    // Table
    @FXML private TableView<Reclamation> tableReclamations;
    @FXML private TableColumn<Reclamation, String> colId;
    @FXML private TableColumn<Reclamation, String> colNom;
    @FXML private TableColumn<Reclamation, String> colEmail;
    @FXML private TableColumn<Reclamation, String> colDate;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private Label lblNombreReclamations;

    // Détails
    @FXML private VBox emptyState;
    @FXML private VBox detailsBox;
    @FXML private Label lblDetailNom;
    @FXML private Label lblDetailEmail;
    @FXML private Label lblDetailTelephone;
    @FXML private Label lblDetailDate;
    @FXML private TextArea txtDetailMessage;
    @FXML private ComboBox<String> cmbDetailStatut;
    @FXML private TextArea txtReponse;
    @FXML private Button btnEnvoyerReponse;
    @FXML private Button btnSupprimer;
    @FXML private Label lblReclamationId;

    // Footer
    @FXML private Label lblStatus;
    @FXML private Label lblLastUpdate;

    // ----- VARIABLES -----
    private ReclamationService reclamationService;
    private ObservableList<Reclamation> reclamationsList;
    private ObservableList<Reclamation> filteredList;
    private Reclamation selectedReclamation;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ----- INITIALISATION -----
    @FXML
    private void initialize() {
        System.out.println("✅ Initialisation du GestionReclamationController...");

        // Initialiser le service
        reclamationService = new ReclamationService();

        // Initialiser les listes
        reclamationsList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        // Configurer la table
        configurerTable();

        // Configurer les ComboBox
        configurerComboBox();

        // Charger les données
        chargerReclamations();

        // Configurer la sélection
        configurerSelection();

        System.out.println("✅ Initialisation terminée");
    }

    /**
     * Configure les colonnes de la table
     */
    private void configurerTable() {
        // Colonne ID
        colId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));

        // Colonne Nom
        colNom.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom()));

        // Colonne Email
        colEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));

        // Colonne Date
        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateCreation().format(dateFormatter)));

        // Colonne Statut avec style
        colStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut()));

        colStatut.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Appliquer des styles selon le statut
                    switch (item) {
                        case "NOUVEAU":
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
                            break;
                        case "EN_COURS":
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #f57c00; -fx-font-weight: bold;");
                            break;
                        case "TRAITE":
                            setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #388e3c; -fx-font-weight: bold;");
                            break;
                        case "FERME":
                            setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #757575;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Lier la table à la liste filtrée
        tableReclamations.setItems(filteredList);
    }

    /**
     * Configure les ComboBox
     */
    private void configurerComboBox() {
        // ComboBox de filtre
        cmbStatut.setItems(FXCollections.observableArrayList(
                "Tous", "NOUVEAU", "EN_COURS", "TRAITE", "FERME"
        ));
        cmbStatut.setValue("Tous");

        // ComboBox de statut détail
        cmbDetailStatut.setItems(FXCollections.observableArrayList(
                "NOUVEAU", "EN_COURS", "TRAITE", "FERME"
        ));
    }

    /**
     * Configure la sélection de la table
     */
    private void configurerSelection() {
        tableReclamations.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        afficherDetails(newSelection);
                    }
                }
        );
    }

    /**
     * Charge toutes les réclamations
     */
    private void chargerReclamations() {
        updateStatus("Chargement des réclamations...");

        new Thread(() -> {
            List<Reclamation> reclamations = reclamationService.getAllReclamations();

            Platform.runLater(() -> {
                reclamationsList.clear();
                reclamationsList.addAll(reclamations);

                // Appliquer les filtres
                appliquerFiltres();

                // Mettre à jour les statistiques
                updateStatistiques();

                // Mettre à jour l'heure
                updateLastUpdate();

                updateStatus("Prêt - " + reclamations.size() + " réclamation(s) chargée(s)");
            });
        }).start();
    }

    /**
     * Applique les filtres sur la liste
     */
    private void appliquerFiltres() {
        String statutFiltre = cmbStatut.getValue();
        String recherche = txtRecherche.getText().toLowerCase().trim();

        List<Reclamation> filtered = reclamationsList.stream()
                .filter(r -> {
                    // Filtre par statut
                    boolean matchStatut = statutFiltre.equals("Tous") ||
                            r.getStatut().equals(statutFiltre);

                    // Filtre par recherche
                    boolean matchRecherche = recherche.isEmpty() ||
                            r.getNom().toLowerCase().contains(recherche) ||
                            r.getEmail().toLowerCase().contains(recherche);

                    return matchStatut && matchRecherche;
                })
                .collect(Collectors.toList());

        filteredList.clear();
        filteredList.addAll(filtered);

        lblNombreReclamations.setText(filtered.size() + " réclamation(s)");
    }

    /**
     * Affiche les détails d'une réclamation
     */
    private void afficherDetails(Reclamation reclamation) {
        selectedReclamation = reclamation;

        // Masquer l'état vide et afficher les détails
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        detailsBox.setVisible(true);
        detailsBox.setManaged(true);

        // Remplir les informations
        lblDetailNom.setText(reclamation.getNom());
        lblDetailEmail.setText(reclamation.getEmail());
        lblDetailTelephone.setText(reclamation.getTelephone() != null && !reclamation.getTelephone().isEmpty()
                ? reclamation.getTelephone()
                : "Non renseigné");
        lblDetailDate.setText(reclamation.getDateCreation().format(dateFormatter));
        txtDetailMessage.setText(reclamation.getMessage());
        cmbDetailStatut.setValue(reclamation.getStatut());
        lblReclamationId.setText("Réclamation #" + reclamation.getId());

        // Vider la zone de réponse
        txtReponse.clear();
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistiques() {
        int total = reclamationsList.size();
        int nouveau = (int) reclamationsList.stream().filter(r -> r.getStatut().equals("NOUVEAU")).count();
        int enCours = (int) reclamationsList.stream().filter(r -> r.getStatut().equals("EN_COURS")).count();
        int traite = (int) reclamationsList.stream().filter(r -> r.getStatut().equals("TRAITE")).count();

        lblStatistiques.setText(String.format(
                "Total: %d | Nouveau: %d | En cours: %d | Traité: %d",
                total, nouveau, enCours, traite
        ));
    }

    /**
     * Met à jour le statut dans la barre
     */
    private void updateStatus(String message) {
        Platform.runLater(() -> lblStatus.setText(message));
    }

    /**
     * Met à jour l'heure de dernière actualisation
     */
    private void updateLastUpdate() {
        String now = java.time.LocalDateTime.now().format(dateFormatter);
        lblLastUpdate.setText(now);
    }

    // ----- HANDLERS -----

    @FXML
    private void handleActualiser() {
        chargerReclamations();
    }

    @FXML
    private void handleFiltrer() {
        appliquerFiltres();
    }

    @FXML
    private void handleRechercher() {
        appliquerFiltres();
    }

    @FXML
    private void handleEffacerFiltres() {
        cmbStatut.setValue("Tous");
        txtRecherche.clear();
        appliquerFiltres();
    }

    @FXML
    private void handleChangerStatut() {
        if (selectedReclamation == null) {
            showError("Aucune réclamation sélectionnée");
            return;
        }

        String nouveauStatut = cmbDetailStatut.getValue();
        if (nouveauStatut == null || nouveauStatut.isEmpty()) {
            showError("Veuillez sélectionner un statut");
            return;
        }

        updateStatus("Mise à jour du statut...");

        new Thread(() -> {
            boolean success = reclamationService.updateStatut(selectedReclamation.getId(), nouveauStatut);

            Platform.runLater(() -> {
                if (success) {
                    selectedReclamation.setStatut(nouveauStatut);
                    tableReclamations.refresh();
                    updateStatistiques();
                    showSuccess("Statut mis à jour avec succès");
                    updateStatus("Prêt");
                } else {
                    showError("Impossible de mettre à jour le statut");
                    updateStatus("Erreur");
                }
            });
        }).start();
    }

    @FXML
    private void handleEnvoyerReponse() {
        if (selectedReclamation == null) {
            showError("Aucune réclamation sélectionnée");
            return;
        }

        String reponse = txtReponse.getText().trim();
        if (reponse.isEmpty()) {
            showError("Veuillez entrer une réponse");
            txtReponse.requestFocus();
            return;
        }

        // Confirmer l'envoi
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer l'envoi");
        confirmation.setHeaderText("Envoyer la réponse ?");
        confirmation.setContentText("La réponse sera envoyée à : " + selectedReclamation.getEmail());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            envoyerReponse(reponse);
        }
    }

    /**
     * Envoie la réponse par email
     */
    /**
     * Envoie la réponse par email
     */
    private void envoyerReponse(String reponse) {
        updateStatus("Envoi de la réponse...");
        btnEnvoyerReponse.setDisable(true);

        new Thread(() -> {
            // Construire le sujet et le message
            String sujet = "Réponse à votre réclamation #" + selectedReclamation.getId();
            String messageComplet = construireMessageReponse(reponse);

            // Envoyer l'email avec la bonne méthode
            boolean success = EmailService.envoyerReponse(
                    selectedReclamation.getEmail(),  // Email du client
                    selectedReclamation.getNom(),     // Nom du client
                    sujet,                            // Sujet
                    messageComplet                    // Message complet
            );

            Platform.runLater(() -> {
                btnEnvoyerReponse.setDisable(false);

                if (success) {
                    showSuccess("Réponse envoyée avec succès !");
                    txtReponse.clear();

                    // Mettre à jour le statut si c'était "NOUVEAU"
                    if (selectedReclamation.getStatut().equals("NOUVEAU")) {
                        cmbDetailStatut.setValue("EN_COURS");
                        handleChangerStatut();
                    }

                    updateStatus("Prêt");
                } else {
                    showError("Impossible d'envoyer la réponse.\nVérifiez votre configuration email.");
                    updateStatus("Erreur");
                }
            });
        }).start();
    }

    /**
     * Construit le message de réponse
     */
    /**
     * Construit le message de réponse
     */
    private String construireMessageReponse(String reponse) {
        StringBuilder msg = new StringBuilder();
        msg.append("Bonjour ").append(selectedReclamation.getNom()).append(",\n\n");
        msg.append("Nous avons bien reçu votre réclamation (#").append(selectedReclamation.getId()).append(").\n\n");
        msg.append("Voici notre réponse :\n\n");
        msg.append(reponse);
        msg.append("\n\n");
        msg.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        msg.append("Pour rappel, votre message était :\n");
        msg.append(selectedReclamation.getMessage());
        msg.append("\n\n");
        msg.append("Cordialement,\n");
        msg.append("L'équipe Support\n");
        msg.append("bechirhadidan8@gmail.com");
        return msg.toString();
    }

    @FXML
    private void handleSupprimer() {
        if (selectedReclamation == null) {
            showError("Aucune réclamation sélectionnée");
            return;
        }

        // Confirmer la suppression
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer cette réclamation ?");
        confirmation.setContentText(
                "Réclamation #" + selectedReclamation.getId() + "\n" +
                        "De : " + selectedReclamation.getNom() + "\n\n" +
                        "Cette action est irréversible !"
        );

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            supprimerReclamation();
        }
    }

    /**
     * Supprime la réclamation
     */
    private void supprimerReclamation() {
        updateStatus("Suppression en cours...");

        int id = selectedReclamation.getId();

        new Thread(() -> {
            boolean success = reclamationService.supprimerReclamation(id);

            Platform.runLater(() -> {
                if (success) {
                    reclamationsList.remove(selectedReclamation);
                    filteredList.remove(selectedReclamation);

                    // Réinitialiser l'affichage
                    emptyState.setVisible(true);
                    emptyState.setManaged(true);
                    detailsBox.setVisible(false);
                    detailsBox.setManaged(false);

                    selectedReclamation = null;

                    updateStatistiques();
                    showSuccess("Réclamation supprimée avec succès");
                    updateStatus("Prêt");
                } else {
                    showError("Impossible de supprimer la réclamation");
                    updateStatus("Erreur");
                }
            });
        }).start();
    }

    // ----- UTILITAIRES -----

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("✅ Opération réussie");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("❌ Une erreur est survenue");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
