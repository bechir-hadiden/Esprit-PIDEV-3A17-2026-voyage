package com.example.demo1.controller.admin;

import com.example.demo1.entity.RoomType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Controller for the Room Form Dialog (Add/Edit Room).
 */
public class RoomFormDialogController {

    @FXML
    private Label dialogTitle;
    @FXML
    private TextField nameField;
    @FXML
    private Spinner<Integer> maxGuestsSpinner;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private CheckBox availableCheckbox;
    @FXML
    private Button saveButton;

    // Images
    @FXML
    private ListView<String> imagesListView;

    private RoomType editingRoom;
    private boolean saveClicked = false;
    private RoomType resultRoom;
    private ObservableList<String> images = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2);
        maxGuestsSpinner.setValueFactory(valueFactory);

        // Setup images list
        imagesListView.setItems(images);
    }

    /**
     * Initialize the dialog for adding a new room.
     */
    public void initializeForAdd() {
        dialogTitle.setText("Add Room Type");
        editingRoom = null;
    }

    /**
     * Initialize the dialog for editing an existing room.
     */
    public void initializeForEdit(RoomType room) {
        dialogTitle.setText("Edit Room Type");
        editingRoom = room;
        populateFields(room);
    }

    /**
     * Populate form fields with room data.
     */
    private void populateFields(RoomType room) {
        nameField.setText(room.getName());
        maxGuestsSpinner.getValueFactory().setValue(room.getMaxGuests());
        priceField.setText(String.valueOf(room.getPricePerNight()));
        descriptionArea.setText(room.getDescription());
        availableCheckbox.setSelected(room.isAvailable());

        // Images
        images.clear();
        images.addAll(room.getImages());
    }

    /**
     * Handle save button click.
     */
    @FXML
    private void handleSave() {
        if (validateInput()) {
            resultRoom = createRoomFromInput();

            // If editing, preserve the ID
            if (editingRoom != null) {
                resultRoom.setId(editingRoom.getId());
            }

            saveClicked = true;
            closeDialog();
        }
    }

    /**
     * Handle cancel button click.
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }

    /**
     * Validate user input.
     */
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errors.append("- Room name is required\n");
        }

        if (priceField.getText() == null || priceField.getText().trim().isEmpty()) {
            errors.append("- Price is required\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) {
                    errors.append("- Price must be greater than 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Price must be a valid number\n");
            }
        }

        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            errors.append("- Description is required\n");
        }

        if (errors.length() > 0) {
            showError("Please fix the following errors:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Create RoomType object from form input.
     */
    private RoomType createRoomFromInput() {
        RoomType room = new RoomType();
        room.setName(nameField.getText().trim());
        room.setDescription(descriptionArea.getText().trim());
        room.setMaxGuests(maxGuestsSpinner.getValue());
        room.setPricePerNight(Double.parseDouble(priceField.getText().trim()));
        room.setAvailable(availableCheckbox.isSelected());
        room.getImages().addAll(images);
        return room;
    }

    /**
     * Handle add image button click - opens file chooser to select room images.
     */
    @FXML
    private void handleAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Room Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        Stage stage = (Stage) imagesListView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                String imagePath = copyImageToResources(selectedFile);
                if (imagePath != null) {
                    images.add(imagePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to copy image: " + e.getMessage());
            }
        }
    }

    /**
     * Handle remove selected image button click.
     */
    @FXML
    private void handleRemoveImage() {
        String selected = imagesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            images.remove(selected);
        }
    }

    /**
     * Copy selected image to project's resources folder.
     * @return the resource path to store in database
     */
    private String copyImageToResources(File sourceFile) {
        try {
            // Create images directory in resources if it doesn't exist
            Path targetDir = Paths.get("src", "main", "resources", "com", "smarttrip", "images", "rooms");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // Generate unique filename
            String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
            Path targetPath = targetDir.resolve(fileName);

            // Copy file
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative resource path
            return "/com/smarttrip/images/rooms/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to copy image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Show error message dialog.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Close the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Check if save was clicked.
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    /**
     * Get the result room.
     */
    public RoomType getResultRoom() {
        return resultRoom;
    }
}
