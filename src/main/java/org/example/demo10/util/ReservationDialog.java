package org.example.demo10.util;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.demo10.model.User;
import org.example.demo10.model.Voyage;
import org.example.demo10.service.ReservationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReservationDialog {

    public static void showReservationDialog(Voyage voyage, User user,
                                             ReservationService reservationService,
                                             Runnable onSuccess) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle réservation");
        dialog.setHeaderText("Réserver : " + voyage.getDestination());

        dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        // Informations du voyage
        Label lblVoyageInfo = new Label(String.format(
                "📅 Du %s au %s\n" +
                        "💰 Prix: %s par personne\n" +
                        "🎫 Places disponibles: %d",
                voyage.getDateDepart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                voyage.getDateRetour().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                voyage.getPrixFormate(),
                voyage.getPlacesDisponibles()
        ));
        lblVoyageInfo.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;");

        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 10, 0));

        // Nom (pré-rempli avec l'utilisateur connecté)
        Label lblNom = new Label("Nom:");
        TextField txtNom = new TextField(user.getNom());
        txtNom.setEditable(false);
        txtNom.setStyle("-fx-background-color: #f0f0f0;");

        // Email (pré-rempli)
        Label lblEmail = new Label("Email:");
        TextField txtEmail = new TextField(user.getEmail());
        txtEmail.setEditable(false);
        txtEmail.setStyle("-fx-background-color: #f0f0f0;");

        // Téléphone
        Label lblTel = new Label("Téléphone:");
        TextField txtTel = new TextField();
        txtTel.setPromptText("06 XX XX XX XX");

        // Nombre de personnes
        Label lblPersonnes = new Label("Nombre de personnes:");
        Spinner<Integer> spinnerPersonnes = new Spinner<>(1, voyage.getPlacesDisponibles(), 1);
        spinnerPersonnes.setEditable(true);

        // Date de réservation (aujourd'hui par défaut)
        Label lblDate = new Label("Date de réservation:");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setEditable(false);

        // Commentaire
        Label lblCommentaire = new Label("Commentaire (optionnel):");
        TextArea txtCommentaire = new TextArea();
        txtCommentaire.setPrefRowCount(3);
        txtCommentaire.setPromptText("Exigences particulières, remarques...");

        // Calcul du prix total
        Label lblPrixTotal = new Label();
        lblPrixTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745; -fx-font-size: 16px;");

        spinnerPersonnes.valueProperty().addListener((obs, old, newVal) -> {
            double total = voyage.getPrix() * newVal;
            lblPrixTotal.setText("💰 Prix total: " + String.format("%,.0f €", total));
        });

        // Prix total initial
        lblPrixTotal.setText("💰 Prix total: " + String.format("%,.0f €", voyage.getPrix()));

        grid.add(lblNom, 0, 0);
        grid.add(txtNom, 1, 0);
        grid.add(lblEmail, 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(lblTel, 0, 2);
        grid.add(txtTel, 1, 2);
        grid.add(lblPersonnes, 0, 3);
        grid.add(spinnerPersonnes, 1, 3);
        grid.add(lblDate, 0, 4);
        grid.add(datePicker, 1, 4);

        content.getChildren().addAll(lblVoyageInfo, grid, lblCommentaire, txtCommentaire, lblPrixTotal);

        dialog.getDialogPane().setContent(content);

        // Validation
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Confirmer la réservation");
        okButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (txtTel.getText().trim().isEmpty()) {
                showAlert("Erreur", "Veuillez saisir votre numéro de téléphone.");
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(result -> {
            boolean success = reservationService.ajouterReservation(
                    txtNom.getText(),
                    txtEmail.getText(),
                    txtTel.getText(),
                    voyage.getId(),
                    datePicker.getValue(),
                    spinnerPersonnes.getValue(),
                    txtCommentaire.getText()
            );

            if (success) {
                // Mettre à jour les places disponibles
                voyage.setPlacesDisponibles(voyage.getPlacesDisponibles() - spinnerPersonnes.getValue());

                showAlert("Succès", "✅ Réservation confirmée !\nUn email de confirmation vous sera envoyé.");
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else {
                showAlert("Erreur", "❌ Échec de la réservation.");
            }
        });
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}