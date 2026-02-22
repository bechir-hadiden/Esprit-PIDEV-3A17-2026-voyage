package com.example.demo1.controller;

import com.example.demo1.entity.Destination;
import com.example.demo1.entity.Voyage;
import com.example.demo1.services.DestinationService;
import com.example.demo1.services.VoyageServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.util.List;

public class AddVoyageController {

    // ===== FXML =====
    @FXML private ComboBox<Destination> cbDestination;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private TextField txtPrix;
    @FXML private TextArea txtDescription;
    @FXML private Label lblMessage;
    @FXML private VBox imagePreviewContainer;
    @FXML private Label lblNoImage;
    @FXML private TextField tfPaysDepart;

    // ===== SERVICES =====
    private VoyageServices voyageService;
    private DestinationService destinationService;
    private VoyageController voyageController;
    private ImageView imagePreview;

    // ✅ VARIABLE POUR DISTINGUER AJOUT / MODIFICATION
    private Voyage voyageAModifier = null;

    public AddVoyageController() {
        voyageService = new VoyageServices();
        destinationService = new DestinationService();
    }

    // ============================================
    // 🚀 INITIALISATION
    // ============================================
    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation AddVoyageController...");

        imagePreview = new ImageView();
        imagePreview.setFitWidth(400);
        imagePreview.setFitHeight(180);
        imagePreview.setPreserveRatio(true);
        imagePreview.setVisible(false);

        chargerDestinations();

        // ✅ Valeur par défaut
        tfPaysDepart.setText("Tunisie");

        cbDestination.setOnAction(e -> {
            Destination dest = cbDestination.getValue();
            if (dest != null) afficherApercu(dest);
        });

        System.out.println("✅ AddVoyageController initialisé");
    }

    // ============================================
    // 📋 CHARGER LES DESTINATIONS
    // ============================================
    private void chargerDestinations() {
        List<Destination> destinations = destinationService.getAll();
        cbDestination.setItems(FXCollections.observableArrayList(destinations));

        cbDestination.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Destination item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("✈️ " + item.getNom() +
                            (item.getPays() != null && !item.getPays().isEmpty()
                                    ? " — " + item.getPays() : "") +
                            (item.getCodeIata() != null && !item.getCodeIata().isEmpty()
                                    ? " (" + item.getCodeIata() + ")" : ""));
                }
            }
        });

        cbDestination.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Destination item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choisissez une destination...");
                } else {
                    setText("✈️ " + item.getNom() +
                            (item.getPays() != null && !item.getPays().isEmpty()
                                    ? " — " + item.getPays() : ""));
                }
            }
        });
    }

    // ============================================
    // 🖼️ APERCU IMAGE
    // ============================================
    private void afficherApercu(Destination dest) {
        imagePreviewContainer.getChildren().clear();

        if (dest.getImageUrl() != null && !dest.getImageUrl().isEmpty()) {
            try {
                Image image;
                String imageUrl = dest.getImageUrl();

                if (imageUrl.startsWith("/images/")) {
                    File fichier = new File("src/main/resources" + imageUrl);
                    image = fichier.exists()
                            ? new Image(fichier.toURI().toString())
                            : new Image(imageUrl, true);
                } else {
                    image = new Image(imageUrl, true);
                }

                imagePreview.setImage(image);
                imagePreview.setVisible(true);
                imagePreviewContainer.getChildren().add(imagePreview);

            } catch (Exception e) {
                afficherPlaceholder(dest.getNom());
            }
        } else {
            afficherPlaceholder(dest.getNom());
        }
    }

    private void afficherPlaceholder(String nom) {
        Label placeholder = new Label("📷 " + nom);
        placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaa; -fx-padding: 50;");
        imagePreviewContainer.getChildren().add(placeholder);
    }

    // ============================================
    // 💾 ENREGISTRER (AJOUT OU MODIFICATION)
    // ============================================
    @FXML
    private void handleSave() {
        if (!validateFields()) return;

        try {
            Destination destChoisie = cbDestination.getValue();

            if (voyageAModifier != null) {
                // ✅ MODE MODIFICATION
                voyageAModifier.setDestination(destChoisie.getNom());
                voyageAModifier.setDestinationId(destChoisie.getId());
                voyageAModifier.setDestinationObj(destChoisie);
                voyageAModifier.setDateDebut(dateDebut.getValue());
                voyageAModifier.setDateFin(dateFin.getValue());
                voyageAModifier.setPrix(Double.parseDouble(txtPrix.getText().trim()));
                voyageAModifier.setDescription(txtDescription.getText().trim());
                voyageAModifier.setImagePath(destChoisie.getImageUrl() != null
                        ? destChoisie.getImageUrl() : "/images/default.jpg");
                voyageAModifier.setPaysDepart(tfPaysDepart.getText().trim());

                boolean success = voyageService.updateVoyage(voyageAModifier);
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION,
                            "✅ Voyage modifié avec succès !").showAndWait();
                    closeWindow();
                } else {
                    afficherErreur("❌ Erreur lors de la modification");
                }

            } else {
                // ✅ MODE AJOUT
                Voyage voyage = new Voyage();
                voyage.setDestination(destChoisie.getNom());
                voyage.setDestinationId(destChoisie.getId());
                voyage.setDestinationObj(destChoisie);
                voyage.setDateDebut(dateDebut.getValue());
                voyage.setDateFin(dateFin.getValue());
                voyage.setPrix(Double.parseDouble(txtPrix.getText().trim()));
                voyage.setDescription(txtDescription.getText().trim());
                voyage.setImagePath(destChoisie.getImageUrl() != null
                        ? destChoisie.getImageUrl() : "/images/default.jpg");
                voyage.setPaysDepart(tfPaysDepart.getText().trim());

                boolean success = voyageService.addVoyage(voyage);
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION,
                            "✅ Voyage ajouté avec succès !").showAndWait();
                    closeWindow();
                } else {
                    afficherErreur("❌ Erreur lors de l'ajout");
                }
            }

        } catch (NumberFormatException e) {
            afficherErreur("❌ Le prix doit être un nombre valide");
        } catch (Exception e) {
            afficherErreur("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================
    // ✅ VALIDATION
    // ============================================
    private boolean validateFields() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);

        if (cbDestination.getValue() == null) {
            afficherErreur("❌ Veuillez choisir une destination !");
            return false;
        }
        if (dateDebut.getValue() == null) {
            afficherErreur("❌ La date de début est obligatoire");
            return false;
        }
        if (dateFin.getValue() == null) {
            afficherErreur("❌ La date de fin est obligatoire");
            return false;
        }
        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            afficherErreur("❌ La date de fin doit être après la date de début");
            return false;
        }
        if (txtPrix.getText() == null || txtPrix.getText().trim().isEmpty()) {
            afficherErreur("❌ Le prix est obligatoire");
            return false;
        }
        try {
            double prix = Double.parseDouble(txtPrix.getText().trim());
            if (prix <= 0) {
                afficherErreur("❌ Le prix doit être supérieur à 0");
                return false;
            }
        } catch (NumberFormatException e) {
            afficherErreur("❌ Le prix doit être un nombre valide");
            return false;
        }
        if (tfPaysDepart.getText() == null || tfPaysDepart.getText().trim().isEmpty()) {
            afficherErreur("❌ Veuillez saisir un pays de départ !");
            return false;
        }
        return true;
    }

    private void afficherErreur(String message) {
        lblMessage.setText(message);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    // ============================================
    // ❌ ANNULER
    // ============================================
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    public void setVoyageController(VoyageController controller) {
        this.voyageController = controller;
    }

    private void closeWindow() {
        Stage stage = (Stage) cbDestination.getScene().getWindow();
        stage.close();
    }

    // ============================================
    // 📝 PRÉ-REMPLIR (modification)
    // ============================================
    public void preRemplir(Voyage voyage) {
        if (voyage == null) return;

        // ✅ MÉMORISER LE VOYAGE À MODIFIER
        this.voyageAModifier = voyage;

        if (voyage.getPaysDepart() != null)
            tfPaysDepart.setText(voyage.getPaysDepart());

        if (voyage.getDestinationId() > 0) {
            cbDestination.getItems().stream()
                    .filter(d -> d.getId() == voyage.getDestinationId())
                    .findFirst()
                    .ifPresent(d -> {
                        cbDestination.setValue(d);
                        afficherApercu(d);
                    });
        }

        if (voyage.getDateDebut() != null) dateDebut.setValue(voyage.getDateDebut());
        if (voyage.getDateFin() != null) dateFin.setValue(voyage.getDateFin());
        txtPrix.setText(String.valueOf(voyage.getPrix()));
        if (voyage.getDescription() != null) txtDescription.setText(voyage.getDescription());

        System.out.println("✅ Formulaire pré-rempli pour: " + voyage.getDestination());
    }
}