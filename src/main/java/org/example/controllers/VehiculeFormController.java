package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.entities.*;
import org.example.services.TransportTypeService;
import org.example.services.VehiculeService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.stage.Modality;

import java.util.stream.Collectors;

public class VehiculeFormController {

    @FXML
    private Label formTitle;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private TextField compagnieField;
    @FXML
    private TextField numeroField;
    @FXML
    private TextField capaciteField;
    @FXML
    private TextField prixField;
    @FXML
    private ImageView imagePreview;
    @FXML
    private CheckBox disponibleCheckBox;
    @FXML
    private TextField villeField;

    @FXML
    private Button saveBtn;

    private String selectedImageName;

    private VehiculeService vehiculeService = new VehiculeService();
    private TransportTypeService typeService = new TransportTypeService();
    private BaseVehicule currentVehicule;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                typeService.lister().stream().map(TransportType::getNom).collect(Collectors.toList())));
    }

    public void setVehicule(BaseVehicule v) {
        this.currentVehicule = v;
        this.isEditMode = (v != null);

        if (isEditMode) {
            formTitle.setText("Modifier le Véhicule");
            typeComboBox.setValue(v.getType());
            compagnieField.setText(v.getCompagnie());
            numeroField.setText(v.getNumero());
            capaciteField.setText(String.valueOf(v.getCapacite()));
            prixField.setText(String.valueOf(v.getPrix()));
            disponibleCheckBox.setSelected(v.isDisponible());
            villeField.setText(v.getVille());

            if (v.getImage() != null && !v.getImage().isEmpty()) {
                selectedImageName = v.getImage();
                try {
                    // Try to load from resources if it's just a filename
                    String path = "/images/" + v.getImage();
                    java.net.URL res = getClass().getResource(path);
                    if (res != null) {
                        imagePreview.setImage(new Image(res.toExternalForm()));
                    } else {
                        // Fallback to absolute file path if it exists
                        File file = new File(v.getImage());
                        if (file.exists()) {
                            imagePreview.setImage(new Image(file.toURI().toString()));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Could not load vehicle image preview: " + v.getImage());
                }
            }
        } else {
            formTitle.setText("Ajouter un Véhicule");
        }
    }

    @FXML
    private void handleSave() {
        String type = typeComboBox.getValue();
        String compagnie = compagnieField.getText().trim();
        String numero = numeroField.getText().trim();
        String capaciteStr = capaciteField.getText().trim();
        String prixStr = prixField.getText().trim();
        String ville = villeField.getText().trim();

        // 1. Validation du Type
        if (type == null || type.isEmpty()) {
            showAlert("Erreur de Saisie", "Veuillez sélectionner un type de transport.");
            return;
        }

        // 2. Validation de la Compagnie
        if (compagnie.isEmpty()) {
            showAlert("Erreur de Saisie", "Le nom de la compagnie est obligatoire.");
            return;
        }
        if (compagnie.length() < 2 || compagnie.length() > 100) {
            showAlert("Erreur de Saisie", "La compagnie doit avoir entre 2 et 100 caractères.");
            return;
        }

        // 3. Validation du Numéro
        if (numero.isEmpty()) {
            showAlert("Erreur de Saisie", "Le numéro du véhicule est obligatoire.");
            return;
        }
        if (numero.length() > 20) {
            showAlert("Erreur de Saisie", "Le numéro ne doit pas dépasser 20 caractères.");
            return;
        }

        // 4. Validation de la Capacité
        int capacite;
        try {
            capacite = Integer.parseInt(capaciteStr);
            if (capacite <= 0) {
                showAlert("Erreur de Saisie", "La capacité doit être positive.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur de Saisie", "Capacité invalide.");
            return;
        }

        // 5. Validation du Prix
        double prix;
        try {
            prix = Double.parseDouble(prixStr);
            if (prix <= 0) {
                showAlert("Erreur de Saisie", "Le prix doit être positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur de Saisie", "Prix invalide.");
            return;
        }

        try {
            BaseVehicule v = isEditMode ? currentVehicule : createInstance(type);
            v.setType(type);
            v.setCompagnie(compagnie);
            v.setNumero(numero);
            v.setCapacite(capacite);
            v.setPrix(prix);
            v.setDisponible(disponibleCheckBox.isSelected());
            v.setVille(ville);

            // Set Image
            if (selectedImageName != null) {
                v.setImage(selectedImageName);
            }

            if (isEditMode) {
                vehiculeService.modifier(v);
            } else {
                vehiculeService.ajouter(v);
            }
            handleBack();
        } catch (Exception e) {
            showAlert("Erreur", "Échec de l'enregistrement.");
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo du véhicule");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(saveBtn.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Ensure target directory exists
                File destDir = new File("src/main/resources/images");
                if (!destDir.exists())
                    destDir.mkdirs();

                // Generate a unique filename to avoid collisions
                String ext = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String newName = "veh_" + System.currentTimeMillis() + ext;
                File destFile = new File(destDir, newName);

                // Copy file to project resources
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                this.selectedImageName = newName;
                this.imagePreview.setImage(new Image(selectedFile.toURI().toString()));

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'importer l'image.");
            }
        }
    }

    @FXML
    private void handleBack() {
        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/vehicule_table_view.fxml", shell.getBtnFleet());
        }
    }

    private BaseVehicule createInstance(String type) {
        switch (type) {
            case "Bus":
                return new Bus();
            case "Taxi":
                return new Taxi();
            case "Voiture":
                return new Voiture();
            case "Scooter":
                return new Scooter();
            default:
                return new GenericVehicule(type);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
