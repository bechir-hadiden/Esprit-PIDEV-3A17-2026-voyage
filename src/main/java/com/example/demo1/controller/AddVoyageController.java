package com.example.demo1.controller;

import com.example.demo1.entity.Voyage;
import com.example.demo1.services.VoyageServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AddVoyageController {

    @FXML
    private TextField txtDestination;

    @FXML
    private DatePicker dateDebut;

    @FXML
    private DatePicker dateFin;

    @FXML
    private TextField txtPrix;

    @FXML
    private TextField txtImagePath;

    @FXML
    private TextArea txtDescription;

    @FXML
    private Label lblMessage;

    @FXML
    private VBox imagePreviewContainer;

    @FXML
    private Label lblNoImage;

    private VoyageServices voyageService;
    private VoyageController voyageController;
    private File selectedImageFile;
    private ImageView imagePreview;

    public AddVoyageController() {
        voyageService = new VoyageServices();
    }

    @FXML
    public void initialize() {
        // Créer l'ImageView pour l'aperçu
        imagePreview = new ImageView();
        imagePreview.setFitWidth(400);
        imagePreview.setFitHeight(180);
        imagePreview.setPreserveRatio(true);
        imagePreview.setVisible(false);

        System.out.println("✅ AddVoyageController initialisé");
    }

    public void setVoyageController(VoyageController controller) {
        this.voyageController = controller;
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Filtres pour les images
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        // Définir le répertoire initial (Bureau de l'utilisateur)
        String userHome = System.getProperty("user.home");
        File initialDirectory = new File(userHome, "Desktop");
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        // Ouvrir le dialogue
        Stage stage = (Stage) txtDestination.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            txtImagePath.setText(selectedImageFile.getName());
            System.out.println("📁 Image sélectionnée: " + selectedImageFile.getAbsolutePath());

            // Afficher l'aperçu
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);
                lblNoImage.setVisible(false);

                // Ajouter l'aperçu s'il n'est pas déjà dans le conteneur
                if (!imagePreviewContainer.getChildren().contains(imagePreview)) {
                    imagePreviewContainer.getChildren().add(imagePreview);
                }

                System.out.println("✅ Aperçu de l'image affiché");
            } catch (Exception e) {
                lblMessage.setText("❌ Erreur lors du chargement de l'aperçu");
                System.err.println("❌ Erreur aperçu: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave() {
        System.out.println("💾 Tentative d'enregistrement...");

        if (!validateFields()) {
            return;
        }

        try {
            // Créer un nouveau voyage
            Voyage voyage = new Voyage();
            voyage.setDestination(txtDestination.getText().trim());
            voyage.setDateDebut(dateDebut.getValue());
            voyage.setDateFin(dateFin.getValue());
            voyage.setPrix(Double.parseDouble(txtPrix.getText().trim()));
            voyage.setDescription(txtDescription.getText().trim());

            // Gérer l'image
            String imagePath = "/images/default.jpg"; // Image par défaut

            if (selectedImageFile != null) {
                System.out.println("📸 Copie de l'image...");
                imagePath = copyImageToResources(selectedImageFile);
                System.out.println("✅ Image copiée vers: " + imagePath);
            }

            voyage.setImagePath(imagePath);

            // Enregistrer dans la base de données
            System.out.println("💾 Enregistrement dans la base de données...");
            boolean success = voyageService.addVoyage(voyage);

            if (success) {
                System.out.println("✅ Voyage enregistré avec succès!");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Le voyage vers " + voyage.getDestination() + " a été ajouté avec succès !");
                alert.showAndWait();

                if (voyageController != null) {
                    voyageController.refreshVoyages();
                }

                closeWindow();
            } else {
                lblMessage.setText("❌ Erreur lors de l'ajout du voyage");
                System.err.println("❌ Échec de l'enregistrement");
            }

        } catch (NumberFormatException e) {
            lblMessage.setText("❌ Le prix doit être un nombre valide");
            System.err.println("❌ Erreur format prix: " + e.getMessage());
        } catch (Exception e) {
            lblMessage.setText("❌ Erreur: " + e.getMessage());
            System.err.println("❌ Erreur générale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String copyImageToResources(File imageFile) {
        try {
            // Créer le dossier images s'il n'existe pas
            String projectPath = System.getProperty("user.dir");
            Path imagesDir = Paths.get(projectPath, "src", "main", "resources", "images");

            System.out.println("📂 Dossier images: " + imagesDir.toString());

            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
                System.out.println("✅ Dossier images créé");
            }

            // Nom du fichier avec timestamp pour éviter les doublons
            String extension = getFileExtension(imageFile.getName());
            String fileName = System.currentTimeMillis() + "_" + cleanFileName(imageFile.getName());
            Path targetPath = imagesDir.resolve(fileName);

            // Copier le fichier
            Files.copy(imageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Fichier copié: " + targetPath.toString());

            // Retourner le chemin relatif pour les ressources
            return "/images/" + fileName;

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la copie de l'image: " + e.getMessage());
            e.printStackTrace();
            return "/images/default.jpg";
        }
    }

    // Méthode pour nettoyer le nom de fichier (enlever les espaces, etc.)
    private String cleanFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    // Méthode pour obtenir l'extension du fichier
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return "";
    }

    @FXML
    private void handleCancel() {
        System.out.println("❌ Annulation");
        closeWindow();
    }

    private boolean validateFields() {
        lblMessage.setText("");

        if (txtDestination.getText() == null || txtDestination.getText().trim().isEmpty()) {
            lblMessage.setText("❌ La destination est obligatoire");
            txtDestination.requestFocus();
            return false;
        }

        if (dateDebut.getValue() == null) {
            lblMessage.setText("❌ La date de début est obligatoire");
            dateDebut.requestFocus();
            return false;
        }

        if (dateFin.getValue() == null) {
            lblMessage.setText("❌ La date de fin est obligatoire");
            dateFin.requestFocus();
            return false;
        }

        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            lblMessage.setText("❌ La date de fin doit être après la date de début");
            dateFin.requestFocus();
            return false;
        }

        if (txtPrix.getText() == null || txtPrix.getText().trim().isEmpty()) {
            lblMessage.setText("❌ Le prix est obligatoire");
            txtPrix.requestFocus();
            return false;
        }

        try {
            double prix = Double.parseDouble(txtPrix.getText().trim());
            if (prix <= 0) {
                lblMessage.setText("❌ Le prix doit être supérieur à 0");
                txtPrix.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            lblMessage.setText("❌ Le prix doit être un nombre valide");
            txtPrix.requestFocus();
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) txtDestination.getScene().getWindow();
        stage.close();
    }
}