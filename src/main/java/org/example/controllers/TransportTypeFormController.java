package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.example.entities.TransportType;
import org.example.services.TransportTypeService;

public class TransportTypeFormController {

    @FXML
    private Label formTitle;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prixField;
    @FXML
    private javafx.scene.image.ImageView imagePreview;
    @FXML
    private Button saveBtn;

    private String selectedImageName;

    private TransportTypeService typeService = new TransportTypeService();
    private TransportType currentType;
    private boolean isEditMode = false;

    public void setType(TransportType t) {
        this.currentType = t;
        this.isEditMode = (t != null);

        if (isEditMode) {
            formTitle.setText("Modifier la Catégorie");
            nomField.setText(t.getNom());
            prixField.setText(String.valueOf(t.getPrixDepart()));

            if (t.getImage() != null && !t.getImage().isEmpty()) {
                selectedImageName = t.getImage();
                try {
                    String path = "/images/" + t.getImage();
                    java.net.URL res = getClass().getResource(path);
                    if (res != null) {
                        imagePreview.setImage(new javafx.scene.image.Image(res.toExternalForm()));
                    } else {
                        java.io.File file = new java.io.File(t.getImage());
                        if (file.exists()) {
                            imagePreview.setImage(new javafx.scene.image.Image(file.toURI().toString()));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Could not load type image preview: " + t.getImage());
                }
            }
        } else {
            formTitle.setText("Nouvelle Catégorie");
        }
    }

    @FXML
    private void handleChooseImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Sélectionner une photo pour cette catégorie");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        javafx.stage.Stage stage = (javafx.stage.Stage) nomField.getScene().getWindow();
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                java.io.File destDir = new java.io.File("src/main/resources/images");
                if (!destDir.exists())
                    destDir.mkdirs();

                String name = selectedFile.getName();
                String ext = name.substring(name.lastIndexOf("."));
                String newName = "type_" + System.currentTimeMillis() + ext;
                java.io.File destFile = new java.io.File(destDir, newName);

                java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                this.selectedImageName = newName;
                this.imagePreview.setImage(new javafx.scene.image.Image(selectedFile.toURI().toString()));

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'importer l'image.");
            }
        }
    }

    @FXML
    private void handleSave() {
        String nom = nomField.getText().trim();
        String prixStr = prixField.getText().trim();

        // 1. Validation du nom (non vide, lettres seulement, min 2 chars, max 50 chars)
        if (nom.isEmpty()) {
            showAlert("Erreur", "Nom requis.");
            return;
        }
        if (nom.length() > 50) {
            showAlert("Erreur", "Nom trop long (max 50).");
            return;
        }
        if (!nom.matches("^[a-zA-Z\\s]{2,50}$")) {
            showAlert("Erreur de Saisie", "Le nom doit contenir uniquement des lettres (min 2 caractères).");
            return;
        }

        // 2. Validation du prix (nombre décimal positif, max 10 chiffres)
        if (prixStr.length() > 10) {
            showAlert("Erreur de Saisie", "Le prix est trop long (max 10 chiffres).");
            return;
        }
        double prix;
        try {
            prix = Double.parseDouble(prixStr);
            if (prix < 0) {
                showAlert("Erreur de Saisie", "Le prix de départ ne peut pas être négatif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Prix invalide.");
            return;
        }

        try {
            TransportType t = isEditMode ? currentType : new TransportType();
            t.setNom(nom);
            t.setPrixDepart(prix);
            t.setImage(selectedImageName);

            if (isEditMode) {
                typeService.modifier(t);
            } else {
                typeService.ajouter(t);
            }
            handleBack();
        } catch (Exception e) {
            showAlert("Erreur", "Échec de l'enregistrement.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            StackPane contentArea = (StackPane) nomField.getScene().lookup("#contentArea");
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/type_table_view.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
            } else {
                MainShellController shell = MainShellController.getInstance();
                if (shell != null) {
                    shell.loadView("/fxml/type_table_view.fxml", shell.getBtnCategories());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
