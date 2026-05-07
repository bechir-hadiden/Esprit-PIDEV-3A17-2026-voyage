package com.example.demo1.controller.admin;

import com.example.demo1.entity.Avis;
import com.example.demo1.services.AvisService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminAvisController {

    // ==================== COMPOSANTS FXML ====================

    // Barre de recherche
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltreNote;
    @FXML private TextField txtFiltreVoyageId;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnRechercher;
    @FXML private Button btnReset;
    @FXML private Button btnExporterPDF;
    @FXML private Button btnAjouter;

    // Statistiques
    @FXML private Label lblTotalAvis;
    @FXML private Label lblNoteMoyenne;
    @FXML private Label lblAvisMois;
    @FXML private Label lblVoyagesNotes;
    @FXML private Label lblResultatRecherche;

    // TableView
    @FXML private TableView<Avis> tableViewAvis;
    @FXML private TableColumn<Avis, Integer> colId;
    @FXML private TableColumn<Avis, String> colNom;
    @FXML private TableColumn<Avis, String> colEmail;
    @FXML private TableColumn<Avis, Integer> colNote;
    @FXML private TableColumn<Avis, String> colCommentaire;
    @FXML private TableColumn<Avis, String> colDate;
    @FXML private TableColumn<Avis, Integer> colVoyageId;
    @FXML private TableColumn<Avis, Void> colActions;

    // Dialogues
    private Dialog<Avis> avisDialog;

    private AvisService avisService;
    private ObservableList<Avis> avisList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        avisService = new AvisService();
        avisList = FXCollections.observableArrayList();

        // Configuration du filtre note
        comboFiltreNote.setItems(FXCollections.observableArrayList("Toutes", "5★", "4★", "3★", "2★", "1★"));
        comboFiltreNote.setValue("Toutes");

        // Configuration des colonnes du tableau
        configurerColonnes();

        // Ajouter les boutons d'action
        ajouterBoutonsAction();

        // Charger les données
        chargerAvis();
        chargerStatistiques();

        // Écouteurs des boutons
        btnRechercher.setOnAction(e -> rechercherAvis());
        btnReset.setOnAction(e -> resetRecherche());
        btnExporterPDF.setOnAction(e -> exporterPDF());
        btnAjouter.setOnAction(e -> ajouterAvis());
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colDate.setCellValueFactory(cellData -> {
            Avis avis = cellData.getValue();
            return new SimpleStringProperty(avis.getDateAvis().format(dateFormatter));
        });
        colVoyageId.setCellValueFactory(new PropertyValueFactory<>("voyageId"));

        // Style pour la colonne note (avec étoiles)
        colNote.setCellFactory(column -> new TableCell<Avis, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox stars = new HBox(2);
                    for (int i = 1; i <= 5; i++) {
                        Label star = new Label("★");
                        if (i <= item) {
                            star.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 14px;");
                        } else {
                            star.setStyle("-fx-text-fill: #e9ecef; -fx-font-size: 14px;");
                        }
                        stars.getChildren().add(star);
                    }
                    Label noteLabel = new Label(" (" + item + "/5)");
                    noteLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
                    stars.getChildren().add(noteLabel);
                    setGraphic(stars);
                    setText(null);
                }
            }
        });
    }

    private void ajouterBoutonsAction() {
        colActions.setCellFactory(col -> new TableCell<Avis, Void>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final Button btnDetails = new Button("Détails");
            private final HBox actions = new HBox(8, btnDetails, btnModifier, btnSupprimer);

            {
                btnDetails.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 15; -fx-cursor: hand;");
                btnModifier.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 15; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 15; -fx-cursor: hand;");
                actions.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Avis avis = getTableView().getItems().get(getIndex());
                    btnDetails.setOnAction(e -> voirDetails(avis));
                    btnModifier.setOnAction(e -> modifierAvis(avis));
                    btnSupprimer.setOnAction(e -> supprimerAvis(avis));
                    setGraphic(actions);
                }
            }
        });
    }

    // ==================== MÉTHODES CRUD ====================

    private void chargerAvis() {
        List<Avis> avis = avisService.getAllAvis();
        avisList.setAll(avis);
        tableViewAvis.setItems(avisList);
        int count = avisList.size();
        lblResultatRecherche.setText(count + " avis trouvé" + (count > 1 ? "s" : ""));
    }

    private void chargerStatistiques() {
        List<Avis> tousAvis = avisService.getAllAvis();

        int total = tousAvis.size();
        lblTotalAvis.setText(String.valueOf(total));

        double moyenne = tousAvis.stream()
                .mapToInt(Avis::getNote)
                .average()
                .orElse(0);
        lblNoteMoyenne.setText(String.format("%.1f / 5", moyenne));

        long avisMois = tousAvis.stream()
                .filter(a -> a.getDateAvis().getMonth() == LocalDate.now().getMonth() &&
                        a.getDateAvis().getYear() == LocalDate.now().getYear())
                .count();
        lblAvisMois.setText(String.valueOf(avisMois));

        long voyagesNotes = tousAvis.stream()
                .mapToInt(Avis::getVoyageId)
                .distinct()
                .count();
        lblVoyagesNotes.setText(String.valueOf(voyagesNotes));
    }

    // ==================== AJOUTER UN AVIS ====================

    private void ajouterAvis() {
        creerDialogueAvis(null);
        Optional<Avis> result = avisDialog.showAndWait();

        result.ifPresent(avis -> {
            boolean success = avisService.ajouterAvisClient(
                    avis.getNomClient(),
                    avis.getEmail(),
                    avis.getNote(),
                    avis.getCommentaire(),
                    avis.getVoyageId()
            );

            if (success) {
                showAlert("Succès", "Avis ajouté avec succès !", Alert.AlertType.INFORMATION);
                chargerAvis();
                chargerStatistiques();
            } else {
                showAlert("Erreur", "Erreur lors de l'ajout de l'avis.", Alert.AlertType.ERROR);
            }
        });
    }

    // ==================== MODIFIER UN AVIS ====================

    private void modifierAvis(Avis avis) {
        creerDialogueAvis(avis);
        Optional<Avis> result = avisDialog.showAndWait();

        result.ifPresent(avisModifie -> {
            boolean success = avisService.modifierAvis(avisModifie);
            if (success) {
                showAlert("Succès", "Avis modifié avec succès !", Alert.AlertType.INFORMATION);
                chargerAvis();
                chargerStatistiques();
            } else {
                showAlert("Erreur", "Erreur lors de la modification.", Alert.AlertType.ERROR);
            }
        });
    }

    // ==================== SUPPRIMER UN AVIS ====================

    private void supprimerAvis(Avis avis) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'avis");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'avis de \"" + avis.getNomClient() + "\" ?\n\nCette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = avisService.supprimerAvis(avis.getId());
                if (success) {
                    showAlert("Succès", "Avis supprimé avec succès !", Alert.AlertType.INFORMATION);
                    chargerAvis();
                    chargerStatistiques();
                } else {
                    showAlert("Erreur", "Erreur lors de la suppression.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ==================== VOIR DÉTAILS D'UN AVIS ====================

    private void voirDetails(Avis avis) {
        Dialog<Void> detailsDialog = new Dialog<>();
        detailsDialog.setTitle("Détails de l'avis");
        detailsDialog.setHeaderText("Avis de " + avis.getNomClient());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f8f9fa;");

        Circle avatar = new Circle(40);
        avatar.setFill(getCouleurNote(avis.getNote()));
        Label initiale = new Label(String.valueOf(avis.getNomClient().charAt(0)));
        initiale.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        initiale.setTextFill(Color.WHITE);
        StackPane avatarPane = new StackPane(avatar, initiale);
        StackPane.setAlignment(avatarPane, Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(new Label(avis.getNomClient()), 1, 0);

        grid.add(new Label("Email:"), 0, 1);
        grid.add(new Label(avis.getEmail()), 1, 1);

        grid.add(new Label("Note:"), 0, 2);
        HBox stars = new HBox(3);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setStyle(i <= avis.getNote() ? "-fx-text-fill: #ffc107; -fx-font-size: 18px;" : "-fx-text-fill: #e9ecef; -fx-font-size: 18px;");
            stars.getChildren().add(star);
        }
        grid.add(stars, 1, 2);

        grid.add(new Label("Voyage ID:"), 0, 3);
        grid.add(new Label(String.valueOf(avis.getVoyageId())), 1, 3);

        grid.add(new Label("Date:"), 0, 4);
        grid.add(new Label(avis.getDateAvis().format(dateFormatter)), 1, 4);

        grid.add(new Label("Commentaire:"), 0, 5);
        TextArea commentArea = new TextArea(avis.getCommentaire());
        commentArea.setEditable(false);
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(5);
        commentArea.setStyle("-fx-background-color: white;");
        grid.add(commentArea, 1, 5);

        content.getChildren().addAll(avatarPane, grid);
        detailsDialog.getDialogPane().setContent(content);
        detailsDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        detailsDialog.showAndWait();
    }

    // ==================== DIALOGUE DE SAISIE AVIS ====================

    private void creerDialogueAvis(Avis avis) {
        avisDialog = new Dialog<>();
        avisDialog.setTitle(avis == null ? "Ajouter un avis" : "Modifier l'avis");
        avisDialog.setHeaderText(avis == null ? "Nouvel avis" : "Modifier l'avis de " + avis.getNomClient());

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        TextField nomField = new TextField(avis != null ? avis.getNomClient() : "");
        nomField.setPromptText("Nom complet");
        nomField.setStyle("-fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");

        TextField emailField = new TextField(avis != null ? avis.getEmail() : "");
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");

        ChoiceBox<Integer> noteChoice = new ChoiceBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        noteChoice.setValue(avis != null ? avis.getNote() : 5);
        noteChoice.setStyle("-fx-padding: 8;");

        TextField voyageField = new TextField(avis != null ? String.valueOf(avis.getVoyageId()) : "");
        voyageField.setPromptText("ID du voyage");
        voyageField.setStyle("-fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");

        TextArea commentArea = new TextArea(avis != null ? avis.getCommentaire() : "");
        commentArea.setPromptText("Commentaire...");
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");

        DatePicker datePicker = new DatePicker(avis != null ? avis.getDateAvis() : LocalDate.now());
        datePicker.setStyle("-fx-padding: 8;");

        // Création des champs labellisés
        VBox nomBox = new VBox(5);
        nomBox.getChildren().addAll(new Label("Nom complet:"), nomField);

        VBox emailBox = new VBox(5);
        emailBox.getChildren().addAll(new Label("Email:"), emailField);

        VBox noteBox = new VBox(5);
        noteBox.getChildren().addAll(new Label("Note:"), noteChoice);

        VBox voyageBox = new VBox(5);
        voyageBox.getChildren().addAll(new Label("ID du voyage:"), voyageField);

        VBox dateBox = new VBox(5);
        dateBox.getChildren().addAll(new Label("Date:"), datePicker);

        VBox commentBox = new VBox(5);
        commentBox.getChildren().addAll(new Label("Commentaire:"), commentArea);

        content.getChildren().addAll(nomBox, emailBox, noteBox, voyageBox, dateBox, commentBox);

        avisDialog.getDialogPane().setContent(content);
        avisDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        avisDialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    Avis nouvelAvis = new Avis();
                    if (avis != null) {
                        nouvelAvis.setId(avis.getId());
                    }
                    nouvelAvis.setNomClient(nomField.getText());
                    nouvelAvis.setEmail(emailField.getText());
                    nouvelAvis.setNote(noteChoice.getValue());
                    nouvelAvis.setVoyageId(Integer.parseInt(voyageField.getText()));
                    nouvelAvis.setCommentaire(commentArea.getText());
                    nouvelAvis.setDateAvis(datePicker.getValue());
                    return nouvelAvis;
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "ID de voyage invalide", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });
    }

    // ==================== RECHERCHE AVANCÉE ====================

    private void rechercherAvis() {
        String recherche = txtRecherche.getText().trim().toLowerCase();
        String filtreNote = comboFiltreNote.getValue();
        String voyageIdText = txtFiltreVoyageId.getText().trim();
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        List<Avis> resultats = avisService.getAllAvis();

        if (!recherche.isEmpty()) {
            resultats = resultats.stream()
                    .filter(a -> a.getNomClient().toLowerCase().contains(recherche) ||
                            a.getEmail().toLowerCase().contains(recherche) ||
                            a.getCommentaire().toLowerCase().contains(recherche))
                    .toList();
        }

        if (!filtreNote.equals("Toutes")) {
            int note = Integer.parseInt(filtreNote.substring(0, 1));
            resultats = resultats.stream()
                    .filter(a -> a.getNote() == note)
                    .toList();
        }

        if (!voyageIdText.isEmpty()) {
            try {
                int voyageId = Integer.parseInt(voyageIdText);
                resultats = resultats.stream()
                        .filter(a -> a.getVoyageId() == voyageId)
                        .toList();
            } catch (NumberFormatException ignored) {}
        }

        if (debut != null) {
            resultats = resultats.stream()
                    .filter(a -> !a.getDateAvis().isBefore(debut))
                    .toList();
        }
        if (fin != null) {
            resultats = resultats.stream()
                    .filter(a -> !a.getDateAvis().isAfter(fin))
                    .toList();
        }

        avisList.setAll(resultats);
        int count = avisList.size();
        lblResultatRecherche.setText(count + " avis trouvé" + (count > 1 ? "s" : ""));
    }

    private void resetRecherche() {
        txtRecherche.clear();
        comboFiltreNote.setValue("Toutes");
        txtFiltreVoyageId.clear();
        dateDebut.setValue(null);
        dateFin.setValue(null);
        chargerAvis();
    }

    private void exporterPDF() {
        showAlert("Export PDF", "Fonctionnalité d'export PDF en cours de développement...", Alert.AlertType.INFORMATION);
    }

    private Color getCouleurNote(int note) {
        switch (note) {
            case 5: return Color.web("#28a745");
            case 4: return Color.web("#17a2b8");
            case 3: return Color.web("#ffc107");
            case 2: return Color.web("#fd7e14");
            case 1: return Color.web("#dc3545");
            default: return Color.web("#6c757d");
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}