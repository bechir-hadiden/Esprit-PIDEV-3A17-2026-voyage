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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class EditVoyageController {

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
    private Voyage currentVoyage;
    private File selectedImageFile;
    private ImageView imagePreview;
    private boolean imageChanged = false;

    public EditVoyageController() {
        voyageService = new VoyageServices();
    }

    @FXML
    public void initialize() {
        imagePreview = new ImageView();
        imagePreview.setFitWidth(400);
        imagePreview.setFitHeight(180);
        imagePreview.setPreserveRatio(true);
        imagePreview.setVisible(false);

        System.out.println("✅ EditVoyageController initialisé");
    }

    public void setVoyageController(VoyageController controller) {
        this.voyageController = controller;
    }

    // Charger les données du voyage à modifier
    public void setVoyage(Voyage voyage) {
        this.currentVoyage = voyage;

        // Remplir les champs avec les données existantes
        txtDestination.setText(voyage.getDestination());
        dateDebut.setValue(voyage.getDateDebut());
        dateFin.setValue(voyage.getDateFin());
        txtPrix.setText(String.valueOf(voyage.getPrix()));
        txtImagePath.setText(voyage.getImagePath());
        txtDescription.setText(voyage.getDescription());

        // Afficher l'image actuelle
        loadCurrentImage(voyage.getImagePath());

        System.out.println("📝 Chargement du voyage: " + voyage.getDestination());
    }

    private void loadCurrentImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return;

        // Prendre seulement la première si plusieurs séparées par ";"
        String firstImage = imagePath.split(";")[0].trim();
        if (firstImage.isEmpty()) return;

        new Thread(() -> {
            try {
                String urlComplete;

                if (firstImage.startsWith("http://") || firstImage.startsWith("https://")) {
                    urlComplete = firstImage;

                } else if (firstImage.startsWith("/uploads/")) {
                    urlComplete = "http://localhost:8000" + firstImage;

                } else if (firstImage.startsWith("/images/") || firstImage.startsWith("/")) {
                    var res = getClass().getResource(firstImage);
                    if (res != null) {
                        urlComplete = res.toExternalForm();
                    } else {
                        String projectPath = System.getProperty("user.dir");
                        java.nio.file.Path p = java.nio.file.Paths.get(
                                projectPath, "src", "main", "resources", firstImage);
                        urlComplete = p.toUri().toString();
                    }
                } else {
                    var res = getClass().getResource("/" + firstImage);
                    urlComplete = res != null ? res.toExternalForm() : firstImage;
                }

                // ✅ HttpURLConnection + User-Agent comme DestinationController
                java.net.HttpURLConnection conn =
                        (java.net.HttpURLConnection) new java.net.URL(urlComplete).openConnection();
                conn.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                                "Chrome/120.0.0.0 Safari/537.36");
                conn.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(15000);
                conn.connect();

                if (conn.getResponseCode() != java.net.HttpURLConnection.HTTP_OK) return;

                try (java.io.InputStream is = conn.getInputStream()) {
                    Image image = new Image(is);
                    if (!image.isError()) {
                        javafx.application.Platform.runLater(() -> {
                            imagePreview.setImage(image);
                            imagePreview.setVisible(true);
                            if (lblNoImage != null) lblNoImage.setVisible(false);
                            if (!imagePreviewContainer.getChildren().contains(imagePreview)) {
                                imagePreviewContainer.getChildren().add(imagePreview);
                            }
                            System.out.println("✅ Aperçu chargé : " + firstImage);
                        });
                    }
                }

            } catch (Exception e) {
                System.err.println("⚠️ Impossible de charger l'aperçu : " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        String userHome = System.getProperty("user.home");
        File initialDirectory = new File(userHome, "Desktop");
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        Stage stage = (Stage) txtDestination.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            imageChanged = true;
            txtImagePath.setText(selectedImageFile.getName());
            System.out.println("📁 Nouvelle image sélectionnée: " + selectedImageFile.getAbsolutePath());

            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);
                lblNoImage.setVisible(false);

                if (!imagePreviewContainer.getChildren().contains(imagePreview)) {
                    imagePreviewContainer.getChildren().add(imagePreview);
                }

                System.out.println("✅ Aperçu mis à jour");
            } catch (Exception e) {
                lblMessage.setText("❌ Erreur lors du chargement de l'aperçu");
                System.err.println("❌ Erreur aperçu: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave() {
        System.out.println("💾 Tentative de modification...");

        if (!validateFields()) {
            return;
        }

        try {
            // Mettre à jour les données du voyage
            currentVoyage.setDestination(txtDestination.getText().trim());
            currentVoyage.setDateDebut(dateDebut.getValue());
            currentVoyage.setDateFin(dateFin.getValue());
            currentVoyage.setPrix(Double.parseDouble(txtPrix.getText().trim()));
            currentVoyage.setDescription(txtDescription.getText().trim());

            // Gérer l'image si elle a été changée
            if (imageChanged && selectedImageFile != null) {
                System.out.println("📸 Copie de la nouvelle image...");
                String imagePath = copyImageToResources(selectedImageFile);
                currentVoyage.setImagePath(imagePath);
                System.out.println("✅ Image mise à jour: " + imagePath);
            }

            // Enregistrer dans la base de données
            System.out.println("💾 Mise à jour dans la base de données...");
            boolean success = voyageService.updateVoyage(currentVoyage);

            if (success) {
                System.out.println("✅ Voyage modifié avec succès!");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Le voyage vers " + currentVoyage.getDestination() + " a été modifié avec succès !");
                alert.showAndWait();

                if (voyageController != null) {
                    voyageController.refreshVoyages();
                }

                closeWindow();
            } else {
                lblMessage.setText("❌ Erreur lors de la modification du voyage");
                System.err.println("❌ Échec de la modification");
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

    @FXML
    private void handleDelete() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer le voyage");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le voyage vers " +
                currentVoyage.getDestination() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("🗑️ Suppression du voyage ID: " + currentVoyage.getId());

            boolean success = voyageService.deleteVoyage(currentVoyage.getId());

            if (success) {
                System.out.println("✅ Voyage supprimé avec succès!");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Le voyage a été supprimé avec succès !");
                alert.showAndWait();

                if (voyageController != null) {
                    voyageController.refreshVoyages();
                }

                closeWindow();
            } else {
                lblMessage.setText("❌ Erreur lors de la suppression du voyage");
                System.err.println("❌ Échec de la suppression");
            }
        }
    }

    private String copyImageToResources(File imageFile) {
        try {
            String projectPath = System.getProperty("user.dir");
            Path sourceImagesDir = Paths.get(projectPath, "src", "main", "resources", "images");

            if (!Files.exists(sourceImagesDir)) {
                Files.createDirectories(sourceImagesDir);
            }

            Path targetImagesDir = Paths.get(projectPath, "target", "classes", "images");

            if (!Files.exists(targetImagesDir)) {
                Files.createDirectories(targetImagesDir);
            }

            String fileName = System.currentTimeMillis() + "_" + cleanFileName(imageFile.getName());

            Path sourceTargetPath = sourceImagesDir.resolve(fileName);
            Path compiledTargetPath = targetImagesDir.resolve(fileName);

            Files.copy(imageFile.toPath(), sourceTargetPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(imageFile.toPath(), compiledTargetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/images/" + fileName;

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la copie de l'image: " + e.getMessage());
            e.printStackTrace();
            return currentVoyage.getImagePath(); // Garder l'ancienne image en cas d'erreur
        }
    }

    private String cleanFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
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