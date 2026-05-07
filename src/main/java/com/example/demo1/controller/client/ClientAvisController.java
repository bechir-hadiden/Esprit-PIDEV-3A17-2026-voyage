package com.example.demo1.controller.client;

import com.example.demo1.entity.Avis;
import com.example.demo1.services.AvisService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

public class ClientAvisController {

    @FXML private TextField txtNom;
    @FXML private TextField txtEmail;
    @FXML private ChoiceBox<Integer> choiceNote;
    @FXML private TextField txtVoyageId;
    @FXML private TextArea txtCommentaire;
    @FXML private Button btnSoumettre;

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private Button btnRechercher;
    @FXML private Button btnReset;
    @FXML private Label lblNombreAvis;

    @FXML private VBox containerAvis;

    private AvisService avisService;
    private ObservableList<Avis> avisList;
    private ObservableList<Avis> avisListFiltre;
    private Integer avisEnModification;

    @FXML
    public void initialize() {
        avisService = new AvisService();
        avisList = FXCollections.observableArrayList();
        avisListFiltre = FXCollections.observableArrayList();
        avisEnModification = null;

        choiceNote.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        choiceNote.setValue(5);

        comboFiltre.setItems(FXCollections.observableArrayList("Toutes", "5★", "4★", "3★", "2★", "1★"));
        comboFiltre.setValue("Toutes");

        chargerAvis();

        btnSoumettre.setOnAction(e -> ajouterOuModifier());
        btnRechercher.setOnAction(e -> rechercher());
        btnReset.setOnAction(e -> resetRecherche());
    }

    private void chargerAvis() {
        avisList.clear();
        avisList.addAll(avisService.getAllAvis());
        avisListFiltre.setAll(avisList);
        afficherCartes();
        lblNombreAvis.setText(avisListFiltre.size() + " avis");
    }

    private void afficherCartes() {
        containerAvis.getChildren().clear();

        if (avisListFiltre.isEmpty()) {
            Label label = new Label("Aucun avis à afficher");
            label.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d;");
            containerAvis.getChildren().add(label);
            return;
        }

        for (Avis avis : avisListFiltre) {
            containerAvis.getChildren().add(creerCarte(avis));
        }
    }

    private VBox creerCarte(Avis avis) {
        VBox carte = new VBox(10);
        carte.setPadding(new Insets(20));
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-border-color: #e9ecef; -fx-border-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(25, getCouleurNote(avis.getNote()));
        Label initiale = new Label(String.valueOf(avis.getNomClient().charAt(0)));
        initiale.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        initiale.setTextFill(Color.WHITE);
        StackPane avatarPane = new StackPane(avatar, initiale);

        VBox infos = new VBox(3);
        Label nom = new Label(avis.getNomClient());
        nom.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label email = new Label(avis.getEmail());
        email.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
        infos.getChildren().addAll(nom, email);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label date = new Label(formatDate(avis.getDateAvis()));
        date.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        header.getChildren().addAll(avatarPane, infos, spacer, date);

        HBox etoilesBox = new HBox(5);
        for (int i = 1; i <= 5; i++) {
            Label etoile = new Label("★");
            etoile.setStyle(i <= avis.getNote() ? "-fx-text-fill: #ffc107; -fx-font-size: 18px;" : "-fx-text-fill: #e9ecef; -fx-font-size: 18px;");
            etoilesBox.getChildren().add(etoile);
        }
        Label noteLabel = new Label(" " + avis.getNote() + "/5");
        noteLabel.setStyle("-fx-font-weight: bold;");
        etoilesBox.getChildren().add(noteLabel);

        TextArea commentaire = new TextArea(avis.getCommentaire());
        commentaire.setEditable(false);
        commentaire.setWrapText(true);
        commentaire.setPrefRowCount(2);
        commentaire.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label voyageLabel = new Label("Voyage #" + avis.getVoyageId());
        voyageLabel.setStyle("-fx-background-color: #e9ecef; -fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 12px;");
        footer.getChildren().add(voyageLabel);

        // Vérifier si c'est l'avis de l'utilisateur courant (optionnel)
        String emailSaisi = txtEmail.getText().trim();
        boolean estMonAvis = !emailSaisi.isEmpty() && emailSaisi.equalsIgnoreCase(avis.getEmail());

        if (estMonAvis) {
            Label monAvisLabel = new Label("✓ Mon avis");
            monAvisLabel.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 11px;");
            footer.getChildren().add(monAvisLabel);
        }

        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15; -fx-font-size: 11px;");

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15; -fx-font-size: 11px;");

        // Si c'est l'avis de l'utilisateur, activer les boutons, sinon les désactiver
        if (estMonAvis) {
            btnModifier.setOnAction(e -> preparerModification(avis));
            btnSupprimer.setOnAction(e -> supprimerAvis(avis));
        } else {
            btnModifier.setDisable(true);
            btnSupprimer.setDisable(true);
            btnModifier.setOpacity(0.5);
            btnSupprimer.setOpacity(0.5);
        }

        buttons.getChildren().addAll(btnModifier, btnSupprimer);
        carte.getChildren().addAll(header, etoilesBox, commentaire, footer, buttons);
        return carte;
    }

    private void preparerModification(Avis avis) {
        avisEnModification = avis.getId();
        txtNom.setText(avis.getNomClient());
        txtEmail.setText(avis.getEmail());
        choiceNote.setValue(avis.getNote());
        txtVoyageId.setText(String.valueOf(avis.getVoyageId()));
        txtCommentaire.setText(avis.getCommentaire());
        btnSoumettre.setText("Mettre à jour");
        btnSoumettre.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;");
    }

    private void supprimerAvis(Avis avis) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'avis");
        confirm.setContentText("Supprimer l'avis de " + avis.getNomClient() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = avisService.supprimerAvis(avis.getId());
                if (success) {
                    showAlert("Succès", "Avis supprimé !");
                    resetFormulaire();
                    chargerAvis();
                } else {
                    showAlert("Erreur", "Échec de la suppression");
                }
            }
        });
    }

    private void ajouterOuModifier() {
        try {
            String nom = txtNom.getText().trim();
            String email = txtEmail.getText().trim();
            Integer note = choiceNote.getValue();
            String commentaire = txtCommentaire.getText().trim();

            if (txtVoyageId.getText().trim().isEmpty()) {
                showAlert("Erreur", "Veuillez entrer un ID de voyage.");
                return;
            }

            int voyageId = Integer.parseInt(txtVoyageId.getText().trim());

            if (nom.isEmpty() || email.isEmpty() || commentaire.isEmpty()) {
                showAlert("Erreur", "Tous les champs sont obligatoires");
                return;
            }

            boolean success;
            if (avisEnModification != null) {
                // Mode modification
                success = avisService.modifierAvis(avisEnModification, nom, email, note, commentaire, voyageId);
                if (success) {
                    showAlert("Succès", "Avis modifié !");
                } else {
                    showAlert("Erreur", "Erreur lors de la modification");
                }
            } else {
                // Mode ajout
                success = avisService.ajouterAvisClient(nom, email, note, commentaire, voyageId);
                if (success) {
                    showAlert("Succès", "Avis ajouté !");
                } else {
                    showAlert("Erreur", "Vous avez déjà donné un avis pour ce voyage");
                }
            }

            if (success) {
                resetFormulaire();
                chargerAvis();
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "ID de voyage invalide");
        }
    }

    private void rechercher() {
        String recherche = txtRecherche.getText().trim().toLowerCase();
        String filtre = comboFiltre.getValue();

        List<Avis> resultats;

        if (filtre.equals("Toutes")) {
            resultats = avisList.stream()
                    .filter(avis -> recherche.isEmpty() || avis.getNomClient().toLowerCase().contains(recherche))
                    .toList();
        } else {
            int noteFiltre = Integer.parseInt(filtre.substring(0, 1));
            resultats = avisList.stream()
                    .filter(avis -> avis.getNote() == noteFiltre)
                    .filter(avis -> recherche.isEmpty() || avis.getNomClient().toLowerCase().contains(recherche))
                    .toList();
        }

        avisListFiltre.setAll(resultats);
        afficherCartes();
        lblNombreAvis.setText(avisListFiltre.size() + " avis");

        if (resultats.isEmpty() && !recherche.isEmpty()) {
            showAlert("Information", "Aucun avis trouvé pour \"" + recherche + "\"");
        }
    }

    private void resetRecherche() {
        txtRecherche.clear();
        comboFiltre.setValue("Toutes");
        avisListFiltre.setAll(avisList);
        afficherCartes();
        lblNombreAvis.setText(avisListFiltre.size() + " avis");
    }

    private void resetFormulaire() {
        txtNom.clear();
        txtEmail.clear();
        choiceNote.setValue(5);
        txtVoyageId.clear();
        txtCommentaire.clear();
        avisEnModification = null;
        btnSoumettre.setText("Publier");
        btnSoumettre.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
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

    private String formatDate(LocalDate date) {
        try {
            return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.FRENCH));
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