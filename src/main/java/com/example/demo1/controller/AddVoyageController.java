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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
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

    // ===== CARROUSEL =====
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private HBox hboxIndicateurs;
    @FXML private Label lblCompteur;

    // ===== ÉTAT CARROUSEL =====
    private List<String> listeImages = new ArrayList<>();
    private int indexImageCourant = 0;
    private ImageView imagePreview;

    // ===== SERVICES =====
    private VoyageServices voyageService;
    private DestinationService destinationService;
    private VoyageController voyageController;
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

        // Préparer l'ImageView du carrousel
        imagePreview = new ImageView();
        imagePreview.setFitWidth(480);
        imagePreview.setFitHeight(200);
        imagePreview.setPreserveRatio(false);
        imagePreview.setStyle("-fx-background-radius: 12;");

        // Cacher les boutons au départ
        btnPrev.setVisible(false);
        btnPrev.setManaged(false);
        btnNext.setVisible(false);
        btnNext.setManaged(false);

        chargerDestinations();
        tfPaysDepart.setText("Tunisie");

        // Quand on choisit une destination → afficher son carrousel
        cbDestination.setOnAction(e -> {
            Destination dest = cbDestination.getValue();
            if (dest != null) afficherCarrousel(dest);
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
    // 🎠 AFFICHER LE CARROUSEL
    // ============================================
    private void afficherCarrousel(Destination dest) {
        // ✅ Récupérer la liste complète des images
        listeImages = new ArrayList<>(dest.getImages());
        indexImageCourant = 0;

        System.out.println("🖼️ Nombre d'images pour " + dest.getNom() + " : " + listeImages.size());

        if (listeImages.isEmpty()) {
            afficherPlaceholder(dest.getNom());
            cacherBoutons();
            return;
        }

        // Afficher la première image
        afficherImageIndex(0);

        // ✅ Afficher les boutons seulement si plusieurs images
        boolean multipleImages = listeImages.size() > 1;
        btnPrev.setVisible(multipleImages);
        btnPrev.setManaged(multipleImages);
        btnNext.setVisible(multipleImages);
        btnNext.setManaged(multipleImages);

        // Mettre à jour les indicateurs et le compteur
        mettreAJourIndicateurs();
        mettreAJourCompteur();
    }

    // ============================================
    // 🖼️ AFFICHER L'IMAGE À L'INDEX DONNÉ
    // ============================================
    private void afficherImageIndex(int index) {
        imagePreviewContainer.getChildren().clear();

        if (index < 0 || index >= listeImages.size()) return;

        String imageUrl = listeImages.get(index);
        System.out.println("📷 Affichage image [" + index + "] : " + imageUrl);

        try {
            Image image;

            if (imageUrl.startsWith("/images/")) {
                // Image locale dans les ressources
                File fichier = new File("src/main/resources" + imageUrl);
                if (fichier.exists()) {
                    image = new Image(fichier.toURI().toString());
                } else {
                    // Essayer depuis le classpath
                    var stream = getClass().getResourceAsStream(imageUrl);
                    if (stream != null) {
                        image = new Image(stream);
                    } else {
                        afficherPlaceholder("Image introuvable");
                        return;
                    }
                }
            } else {
                // URL externe (http/https)
                image = new Image(imageUrl, true);
            }

            imagePreview.setImage(image);
            imagePreviewContainer.getChildren().add(imagePreview);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement image: " + e.getMessage());
            afficherPlaceholder("Erreur image");
        }
    }

    // ============================================
    // ◀ PRÉCÉDENT
    // ============================================
    @FXML
    private void handlePrev() {
        if (listeImages.isEmpty()) return;
        indexImageCourant = (indexImageCourant - 1 + listeImages.size()) % listeImages.size();
        afficherImageIndex(indexImageCourant);
        mettreAJourIndicateurs();
        mettreAJourCompteur();
    }

    // ============================================
    // ▶ SUIVANT
    // ============================================
    @FXML
    private void handleNext() {
        if (listeImages.isEmpty()) return;
        indexImageCourant = (indexImageCourant + 1) % listeImages.size();
        afficherImageIndex(indexImageCourant);
        mettreAJourIndicateurs();
        mettreAJourCompteur();
    }

    // ============================================
    // 🔵 INDICATEURS (points cliquables en bas)
    // ============================================
    private void mettreAJourIndicateurs() {
        hboxIndicateurs.getChildren().clear();

        for (int i = 0; i < listeImages.size(); i++) {
            Circle point = new Circle(5);
            point.setFill(i == indexImageCourant
                    ? Color.web("#667eea")  // actif  → violet
                    : Color.web("#cccccc")); // inactif → gris
            point.setStyle("-fx-cursor: hand;");

            final int idx = i;
            point.setOnMouseClicked(e -> {
                indexImageCourant = idx;
                afficherImageIndex(indexImageCourant);
                mettreAJourIndicateurs();
                mettreAJourCompteur();
            });

            hboxIndicateurs.getChildren().add(point);
        }
    }

    // ============================================
    // 🔢 COMPTEUR  ex: 2 / 5
    // ============================================
    private void mettreAJourCompteur() {
        if (listeImages.size() > 1) {
            lblCompteur.setText((indexImageCourant + 1) + " / " + listeImages.size());
        } else {
            lblCompteur.setText("");
        }
    }

    // ============================================
    // 🙈 CACHER LES BOUTONS
    // ============================================
    private void cacherBoutons() {
        btnPrev.setVisible(false);
        btnPrev.setManaged(false);
        btnNext.setVisible(false);
        btnNext.setManaged(false);
        hboxIndicateurs.getChildren().clear();
        lblCompteur.setText("");
    }

    // ============================================
    // 📷 PLACEHOLDER (aucune image)
    // ============================================
    private void afficherPlaceholder(String nom) {
        imagePreviewContainer.getChildren().clear();
        Label placeholder = new Label("📷 " + nom);
        placeholder.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaa; -fx-padding: 50;");
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
                // MODE MODIFICATION
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
                // MODE AJOUT
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
        this.voyageAModifier = voyage;

        if (voyage.getPaysDepart() != null)
            tfPaysDepart.setText(voyage.getPaysDepart());

        if (voyage.getDestinationId() > 0) {
            cbDestination.getItems().stream()
                    .filter(d -> d.getId() == voyage.getDestinationId())
                    .findFirst()
                    .ifPresent(d -> {
                        cbDestination.setValue(d);
                        afficherCarrousel(d);
                    });
        }

        if (voyage.getDateDebut() != null) dateDebut.setValue(voyage.getDateDebut());
        if (voyage.getDateFin() != null)   dateFin.setValue(voyage.getDateFin());
        txtPrix.setText(String.valueOf(voyage.getPrix()));
        if (voyage.getDescription() != null) txtDescription.setText(voyage.getDescription());

        System.out.println("✅ Formulaire pré-rempli pour: " + voyage.getDestination());
    }
}