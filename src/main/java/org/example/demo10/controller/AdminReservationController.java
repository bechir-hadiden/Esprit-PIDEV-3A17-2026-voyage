package org.example.demo10.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.example.demo10.model.Reservation;
import org.example.demo10.model.Voyage;
import org.example.demo10.service.ReservationService;
import org.example.demo10.service.VoyageService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminReservationController {

    @FXML private TableView<Reservation> tableViewReservations;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, String> colNom;
    @FXML private TableColumn<Reservation, String> colEmail;
    @FXML private TableColumn<Reservation, String> colTel;
    @FXML private TableColumn<Reservation, String> colDestination;
    @FXML private TableColumn<Reservation, LocalDate> colDate;
    @FXML private TableColumn<Reservation, Integer> colPersonnes;
    @FXML private TableColumn<Reservation, String> colStatut;
    @FXML private TableColumn<Reservation, String> colCommentaire;

    @FXML private ComboBox<String> comboFiltreStatut;
    @FXML private TextField txtRecherche;
    @FXML private Button btnRechercher;
    @FXML private Button btnReset;

    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnActualiser;
    @FXML private Button btnStats;
    @FXML private Button btnExporter;

    @FXML private Label lblTotalReservations;
    @FXML private Label lblEnAttente;
    @FXML private Label lblConfirmees;
    @FXML private Label lblAnnulees;
    @FXML private Label lblTotalPersonnes;

    private ReservationService reservationService;
    private VoyageService voyageService;
    private ObservableList<Reservation> reservationList;
    private ObservableList<Reservation> reservationListFiltree;

    @FXML
    public void initialize() {
        reservationService = new ReservationService();
        voyageService = new VoyageService();
        reservationList = FXCollections.observableArrayList();
        reservationListFiltree = FXCollections.observableArrayList();

        // Configuration des colonnes
        configurerColonnes();

        // Configuration des filtres
        comboFiltreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "en_attente", "confirmée", "annulée"
        ));
        comboFiltreStatut.setValue("Tous");

        tableViewReservations.setItems(reservationListFiltree);

        // Charger les données
        chargerReservations();

        // Configuration des boutons
        btnRechercher.setOnAction(e -> rechercherReservations());
        btnReset.setOnAction(e -> resetRecherche());
        btnModifier.setOnAction(e -> modifierReservation());
        btnSupprimer.setOnAction(e -> supprimerReservation());
        btnActualiser.setOnAction(e -> chargerReservations());
        btnStats.setOnAction(e -> afficherStatistiques());
        btnExporter.setOnAction(e -> exporterReservations());
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailClient"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDestination.setCellValueFactory(cellData -> {
            int voyageId = cellData.getValue().getVoyageId();
            Voyage voyage = voyageService.getVoyageById(voyageId);
            String destination = (voyage != null) ? voyage.getDestination() : "Voyage #" + voyageId;
            return new SimpleStringProperty(destination);
        });
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));
        colPersonnes.setCellValueFactory(new PropertyValueFactory<>("nombrePersonnes"));
        colStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatutAvecCouleur()));
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

        // Formatage de la colonne date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colDate.setCellFactory(column -> new TableCell<Reservation, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Coloration de la colonne statut
        colStatut.setCellFactory(column -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    if (statut.contains("Confirmée")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (statut.contains("Annulée")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void chargerReservations() {
        reservationList.clear();
        reservationList.addAll(reservationService.getAllReservations());
        reservationListFiltree.setAll(reservationList);
        mettreAJourStatistiques();
    }

    private void mettreAJourStatistiques() {
        int total = reservationList.size();
        long enAttente = reservationList.stream().filter(r -> "en_attente".equals(r.getStatut())).count();
        long confirmees = reservationList.stream().filter(r -> "confirmée".equals(r.getStatut())).count();
        long annulees = reservationList.stream().filter(r -> "annulée".equals(r.getStatut())).count();
        int totalPersonnes = reservationList.stream()
                .filter(r -> !"annulée".equals(r.getStatut()))
                .mapToInt(Reservation::getNombrePersonnes)
                .sum();

        lblTotalReservations.setText(String.valueOf(total));
        lblEnAttente.setText(String.valueOf(enAttente));
        lblConfirmees.setText(String.valueOf(confirmees));
        lblAnnulees.setText(String.valueOf(annulees));
        lblTotalPersonnes.setText(String.valueOf(totalPersonnes));
    }

    private void rechercherReservations() {
        String recherche = txtRecherche.getText().trim().toLowerCase();
        String statut = comboFiltreStatut.getValue();

        List<Reservation> resultats = reservationList;

        // Filtrer par recherche (nom, email, téléphone)
        if (!recherche.isEmpty()) {
            resultats = resultats.stream()
                    .filter(r -> r.getNomClient().toLowerCase().contains(recherche) ||
                            r.getEmailClient().toLowerCase().contains(recherche) ||
                            r.getTelephone().contains(recherche))
                    .toList();
        }

        // Filtrer par statut
        if (!"Tous".equals(statut)) {
            resultats = resultats.stream()
                    .filter(r -> r.getStatut().equals(statut))
                    .toList();
        }

        reservationListFiltree.setAll(resultats);

        if (reservationListFiltree.isEmpty()) {
            showAlert("Information", "Aucune réservation trouvée.");
        }
    }

    private void resetRecherche() {
        txtRecherche.clear();
        comboFiltreStatut.setValue("Tous");
        reservationListFiltree.setAll(reservationList);
    }

    private void modifierReservation() {
        Reservation selected = tableViewReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une réservation.");
            return;
        }

        // Créer la boîte de dialogue de modification
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Modifier réservation #" + selected.getId());
        dialog.setHeaderText("Client: " + selected.getNomClient());

        ButtonType modifierButton = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(modifierButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        // Champs du formulaire
        TextField nomField = new TextField(selected.getNomClient());
        TextField emailField = new TextField(selected.getEmailClient());
        TextField telField = new TextField(selected.getTelephone());

        ComboBox<Integer> voyageCombo = new ComboBox<>();
        voyageCombo.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8); // IDs des voyages
        voyageCombo.setValue(selected.getVoyageId());

        DatePicker datePicker = new DatePicker(selected.getDateReservation());
        Spinner<Integer> spinnerPersonnes = new Spinner<>(1, 20, selected.getNombrePersonnes());

        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("en_attente", "confirmée", "annulée");
        statutCombo.setValue(selected.getStatut());

        TextArea commentaireArea = new TextArea(selected.getCommentaire());
        commentaireArea.setPrefRowCount(3);

        // Ajout au grid
        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Téléphone:"), 0, 2);
        grid.add(telField, 1, 2);
        grid.add(new Label("Voyage ID:"), 0, 3);
        grid.add(voyageCombo, 1, 3);
        grid.add(new Label("Date:"), 0, 4);
        grid.add(datePicker, 1, 4);
        grid.add(new Label("Personnes:"), 0, 5);
        grid.add(spinnerPersonnes, 1, 5);
        grid.add(new Label("Statut:"), 0, 6);
        grid.add(statutCombo, 1, 6);
        grid.add(new Label("Commentaire:"), 0, 7);
        grid.add(commentaireArea, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == modifierButton) {
                try {
                    selected.setNomClient(nomField.getText());
                    selected.setEmailClient(emailField.getText());
                    selected.setTelephone(telField.getText());
                    selected.setVoyageId(voyageCombo.getValue());
                    selected.setDateReservation(datePicker.getValue());
                    selected.setNombrePersonnes(spinnerPersonnes.getValue());
                    selected.setStatut(statutCombo.getValue());
                    selected.setCommentaire(commentaireArea.getText());
                    return selected;
                } catch (Exception e) {
                    showAlert("Erreur", "Valeurs invalides.");
                    return null;
                }
            }
            return null;
        });

        Optional<Reservation> result = dialog.showAndWait();
        result.ifPresent(reservation -> {
            boolean success = reservationService.modifierReservation(
                    reservation.getId(), reservation.getNomClient(), reservation.getEmailClient(),
                    reservation.getTelephone(), reservation.getVoyageId(), reservation.getDateReservation(),
                    reservation.getNombrePersonnes(), reservation.getStatut(), reservation.getCommentaire()
            );

            if (success) {
                showAlert("Succès", "Réservation modifiée !");
                chargerReservations();
            } else {
                showAlert("Erreur", "Échec de la modification.");
            }
        });
    }

    private void supprimerReservation() {
        Reservation selected = tableViewReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une réservation.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la réservation #" + selected.getId());
        confirm.setContentText("Client: " + selected.getNomClient() + "\nCette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = reservationService.supprimerReservation(selected.getId());
            if (success) {
                showAlert("Succès", "Réservation supprimée !");
                chargerReservations();
            } else {
                showAlert("Erreur", "Échec de la suppression.");
            }
        }
    }

    private void afficherStatistiques() {
        int total = reservationList.size();
        long enAttente = reservationList.stream().filter(r -> "en_attente".equals(r.getStatut())).count();
        long confirmees = reservationList.stream().filter(r -> "confirmée".equals(r.getStatut())).count();
        long annulees = reservationList.stream().filter(r -> "annulée".equals(r.getStatut())).count();
        int totalPersonnes = reservationList.stream()
                .filter(r -> !"annulée".equals(r.getStatut()))
                .mapToInt(Reservation::getNombrePersonnes)
                .sum();

        double chiffreAffaires = reservationList.stream()
                .filter(r -> "confirmée".equals(r.getStatut()))
                .mapToDouble(r -> {
                    Voyage v = voyageService.getVoyageById(r.getVoyageId());
                    return (v != null) ? v.getPrix() * r.getNombrePersonnes() : 0;
                })
                .sum();

        String stats = String.format("""
            📊 STATISTIQUES DES RÉSERVATIONS
            ═══════════════════════════════
            
            📈 TOTAL: %d réservations
            👥 Total voyageurs: %d personnes
            💰 Chiffre d'affaires: %.0f €
            
            ⏳ En attente: %d (%.1f%%)
            ✅ Confirmées: %d (%.1f%%)
            ❌ Annulées: %d (%.1f%%)
            """,
                total,
                totalPersonnes,
                chiffreAffaires,
                enAttente, (total > 0 ? enAttente * 100.0 / total : 0),
                confirmees, (total > 0 ? confirmees * 100.0 / total : 0),
                annulees, (total > 0 ? annulees * 100.0 / total : 0)
        );

        TextArea textArea = new TextArea(stats);
        textArea.setEditable(false);
        textArea.setPrefRowCount(12);
        textArea.setPrefWidth(400);
        textArea.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 14px;");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques détaillées");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void exporterReservations() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Nom;Email;Téléphone;Destination;Date;Personnes;Statut;Commentaire\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Reservation r : reservationListFiltree) {
            Voyage v = voyageService.getVoyageById(r.getVoyageId());
            String destination = (v != null) ? v.getDestination() : "Voyage #" + r.getVoyageId();

            sb.append(r.getId()).append(";")
                    .append(r.getNomClient()).append(";")
                    .append(r.getEmailClient()).append(";")
                    .append(r.getTelephone()).append(";")
                    .append(destination).append(";")
                    .append(r.getDateReservation().format(formatter)).append(";")
                    .append(r.getNombrePersonnes()).append(";")
                    .append(r.getStatut()).append(";")
                    .append(r.getCommentaire() != null ? r.getCommentaire() : "").append("\n");
        }

        // Sauvegarde dans un fichier
        try {
            java.io.FileWriter writer = new java.io.FileWriter("reservations_export.csv");
            writer.write(sb.toString());
            writer.close();
            showAlert("Succès", "Export réussi dans 'reservations_export.csv'");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'export: " + e.getMessage());
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