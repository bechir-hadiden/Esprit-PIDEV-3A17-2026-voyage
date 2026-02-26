package com.example.demo1.controller.admin;
import com.example.demo1.controller.dao.HotelDAO;
import com.example.demo1.entity.Hotel;
import com.example.demo1.entity.RoomType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javafx.stage.FileChooser;

/**
 * Controller for the Hotel Form Dialog (Add/Edit Hotel).
 */
public class HotelFormDialogController {

    @FXML
    private Label dialogTitle;
    @FXML
    private TabPane tabPane;

    // Basic Information Fields
    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField countryField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField ratingField;
    @FXML
    private TextArea descriptionArea;

    // Contact Information Fields
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;

    // Policies Fields
    @FXML
    private TextField checkinField;
    @FXML
    private TextField checkoutField;

    // Rooms Tab
    @FXML
    private TableView<RoomType> roomsTable;
    @FXML
    private TableColumn<RoomType, String> roomNameColumn;
    @FXML
    private TableColumn<RoomType, String> roomDescColumn;
    @FXML
    private TableColumn<RoomType, Integer> roomGuestsColumn;
    @FXML
    private TableColumn<RoomType, Double> roomPriceColumn;
    @FXML
    private TableColumn<RoomType, Void> roomActionsColumn;

    // Amenities checkboxes
    @FXML
    private CheckBox wifiCheckbox;
    @FXML
    private CheckBox parkingCheckbox;
    @FXML
    private CheckBox poolCheckbox;
    @FXML
    private CheckBox gymCheckbox;
    @FXML
    private CheckBox restaurantCheckbox;
    @FXML
    private CheckBox roomServiceCheckbox;
    @FXML
    private CheckBox spaCheckbox;
    @FXML
    private CheckBox barCheckbox;
    @FXML
    private CheckBox laundryCheckbox;
    @FXML
    private CheckBox petFriendlyCheckbox;
    @FXML
    private CheckBox airConditioningCheckbox;
    @FXML
    private CheckBox frontDeskCheckbox;
    @FXML
    private CheckBox airportShuttleCheckbox;
    @FXML
    private CheckBox businessCenterCheckbox;
    @FXML
    private CheckBox conferenceRoomsCheckbox;

    // Images Tab
    @FXML
    private ListView<String> imagesListView;

    @FXML
    private Button saveButton;

    private final HotelDAO hotelDAO = new HotelDAO();
    private Hotel editingHotel;
    private boolean saveClicked = false;
    private ObservableList<RoomType> rooms = FXCollections.observableArrayList();
    private ObservableList<String> images = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupRoomsTable();
        imagesListView.setItems(images);
    }

    /**
     * Setup rooms table with columns and actions.
     */
    private void setupRoomsTable() {
        roomNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        roomDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        roomGuestsColumn.setCellValueFactory(new PropertyValueFactory<>("maxGuests"));

        // Format price column
        roomPriceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        roomPriceColumn.setCellFactory(col -> new TableCell<RoomType, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.0f", price));
                }
            }
        });

        // Actions column
        roomActionsColumn.setCellFactory(col -> new TableCell<RoomType, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle(
                        "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8;");
                deleteBtn.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8;");

                editBtn.setOnAction(e -> {
                    RoomType room = getTableView().getItems().get(getIndex());
                    handleEditRoom(room);
                });

                deleteBtn.setOnAction(e -> {
                    RoomType room = getTableView().getItems().get(getIndex());
                    handleDeleteRoom(room);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        roomsTable.setItems(rooms);
    }

    /**
     * Initialize the dialog for adding a new hotel.
     */
    public void initializeForAdd() {
        dialogTitle.setText("Add New Hotel");
        editingHotel = null;
    }

    /**
     * Initialize the dialog for editing an existing hotel.
     */
    public void initializeForEdit(Hotel hotel) {
        dialogTitle.setText("Edit Hotel");
        editingHotel = hotel;
        populateFields(hotel);
    }

    /**
     * Populate form fields with hotel data.
     */
    private void populateFields(Hotel hotel) {
        // Basic info
        nameField.setText(hotel.getName());
        locationField.setText(hotel.getLocation());
        cityField.setText(hotel.getCity());
        countryField.setText(hotel.getCountry());
        priceField.setText(String.valueOf(hotel.getPricePerNight()));
        ratingField.setText(String.valueOf(hotel.getRating()));
        descriptionArea.setText(hotel.getDescription());
        phoneField.setText(hotel.getContactPhone() != null ? hotel.getContactPhone() : "");
        emailField.setText(hotel.getContactEmail() != null ? hotel.getContactEmail() : "");
        checkinField.setText(hotel.getCheckInTime() != null ? hotel.getCheckInTime() : "");
        checkoutField.setText(hotel.getCheckOutTime() != null ? hotel.getCheckOutTime() : "");

        // Rooms
        rooms.clear();
        rooms.addAll(hotel.getRoomTypes());

        // Amenities
        populateAmenities(hotel.getAmenities());

        // Images
        images.clear();
        images.addAll(hotel.getImages());
    }

    /**
     * Populate amenity checkboxes based on hotel amenities.
     */
    private void populateAmenities(List<String> amenities) {
        for (String amenity : amenities) {
            switch (amenity.toLowerCase()) {
                case "wifi":
                    wifiCheckbox.setSelected(true);
                    break;
                case "parking":
                    parkingCheckbox.setSelected(true);
                    break;
                case "swimming pool":
                    poolCheckbox.setSelected(true);
                    break;
                case "fitness center":
                    gymCheckbox.setSelected(true);
                    break;
                case "restaurant":
                    restaurantCheckbox.setSelected(true);
                    break;
                case "room service":
                    roomServiceCheckbox.setSelected(true);
                    break;
                case "spa & wellness":
                    spaCheckbox.setSelected(true);
                    break;
                case "bar/lounge":
                    barCheckbox.setSelected(true);
                    break;
                case "laundry service":
                    laundryCheckbox.setSelected(true);
                    break;
                case "pet friendly":
                    petFriendlyCheckbox.setSelected(true);
                    break;
                case "air conditioning":
                    airConditioningCheckbox.setSelected(true);
                    break;
                case "24-hour front desk":
                    frontDeskCheckbox.setSelected(true);
                    break;
                case "airport shuttle":
                    airportShuttleCheckbox.setSelected(true);
                    break;
                case "business center":
                    businessCenterCheckbox.setSelected(true);
                    break;
                case "conference rooms":
                    conferenceRoomsCheckbox.setSelected(true);
                    break;
            }
        }
    }

    /**
     * Handle add room button click.
     */
    @FXML
    private void handleAddRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/RoomFormDialog.fxml"));
            VBox dialogRoot = loader.load();
            RoomFormDialogController controller = loader.getController();
            controller.initializeForAdd();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Room Type");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                rooms.add(controller.getResultRoom());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to open room dialog: " + e.getMessage());
        }
    }

    /**
     * Handle edit room.
     */
    private void handleEditRoom(RoomType room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/RoomFormDialog.fxml"));
            VBox dialogRoot = loader.load();
            RoomFormDialogController controller = loader.getController();
            controller.initializeForEdit(room);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Room Type");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                RoomType updatedRoom = controller.getResultRoom();
                int index = rooms.indexOf(room);
                if (index >= 0) {
                    rooms.set(index, updatedRoom);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to open room dialog: " + e.getMessage());
        }
    }

    /**
     * Handle delete room.
     */
    private void handleDeleteRoom(RoomType room) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Room");
        confirmDialog.setHeaderText("Delete " + room.getName() + "?");
        confirmDialog.setContentText("This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                rooms.remove(room);
            }
        });
    }

    /**
     * Handle add image button click - opens file chooser to select images.
     */
    @FXML
    private void handleAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Hotel Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        Stage stage = (Stage) imagesListView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Copy file to project's resources folder
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
     * Copy selected image to project's resources folder.
     * @return the resource path to store in database
     */
    private String copyImageToResources(File sourceFile) {
        try {
            // Create images directory in resources if it doesn't exist
            Path targetDir = Paths.get("src", "main", "resources", "com", "smarttrip", "images", "hotels");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // Generate unique filename
            String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
            Path targetPath = targetDir.resolve(fileName);

            // Copy file
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return the resource path that can be used by ImageView
            return "/com/smarttrip/images/hotels/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
     * Get selected amenities from checkboxes.
     */
    private List<String> getSelectedAmenities() {
        List<String> amenities = new ArrayList<>();

        if (wifiCheckbox.isSelected())
            amenities.add("WiFi");
        if (parkingCheckbox.isSelected())
            amenities.add("Parking");
        if (poolCheckbox.isSelected())
            amenities.add("Swimming Pool");
        if (gymCheckbox.isSelected())
            amenities.add("Fitness Center");
        if (restaurantCheckbox.isSelected())
            amenities.add("Restaurant");
        if (roomServiceCheckbox.isSelected())
            amenities.add("Room Service");
        if (spaCheckbox.isSelected())
            amenities.add("Spa & Wellness");
        if (barCheckbox.isSelected())
            amenities.add("Bar/Lounge");
        if (laundryCheckbox.isSelected())
            amenities.add("Laundry Service");
        if (petFriendlyCheckbox.isSelected())
            amenities.add("Pet Friendly");
        if (airConditioningCheckbox.isSelected())
            amenities.add("Air Conditioning");
        if (frontDeskCheckbox.isSelected())
            amenities.add("24-Hour Front Desk");
        if (airportShuttleCheckbox.isSelected())
            amenities.add("Airport Shuttle");
        if (businessCenterCheckbox.isSelected())
            amenities.add("Business Center");
        if (conferenceRoomsCheckbox.isSelected())
            amenities.add("Conference Rooms");

        return amenities;
    }

    /**
     * Handle save button click.
     */
    @FXML
    private void handleSave() {
        if (validateInput()) {
            try {
                Hotel hotel = createHotelFromInput();
                boolean success;

                if (editingHotel == null) {
                    // Creating new hotel
                    success = hotelDAO.createHotel(hotel);
                } else {
                    // Updating existing hotel
                    hotel.setId(editingHotel.getId());
                    success = hotelDAO.updateHotel(hotel);
                }

                if (success) {
                    saveClicked = true;
                    closeDialog();
                } else {
                    showError("Failed to save hotel. Please try again.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error saving hotel: " + e.getMessage());
            }
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
            errors.append("- Hotel name is required\n");
        }
        if (locationField.getText() == null || locationField.getText().trim().isEmpty()) {
            errors.append("- Location is required\n");
        }
        if (cityField.getText() == null || cityField.getText().trim().isEmpty()) {
            errors.append("- City is required\n");
        }
        if (countryField.getText() == null || countryField.getText().trim().isEmpty()) {
            errors.append("- Country is required\n");
        }
        if (priceField.getText() == null || priceField.getText().trim().isEmpty()) {
            errors.append("- Price is required\n");
        }
        if (ratingField.getText() == null || ratingField.getText().trim().isEmpty()) {
            errors.append("- Rating is required\n");
        } else {
            try {
                double rating = Double.parseDouble(ratingField.getText().trim());
                if (rating < 0 || rating > 5) {
                    errors.append("- Rating must be between 0 and 5\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Rating must be a valid number\n");
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
     * Create Hotel object from form input.
     */
    private Hotel createHotelFromInput() {
        Hotel hotel = new Hotel();
        hotel.setName(nameField.getText().trim());
        hotel.setLocation(locationField.getText().trim());
        hotel.setCity(cityField.getText().trim());
        hotel.setCountry(countryField.getText().trim());
        hotel.setPricePerNight(Double.parseDouble(priceField.getText().trim()));
        hotel.setRating(Double.parseDouble(ratingField.getText().trim()));
        hotel.setDescription(descriptionArea.getText().trim());
        hotel.setContactPhone(phoneField.getText() != null ? phoneField.getText().trim() : null);
        hotel.setContactEmail(emailField.getText() != null ? emailField.getText().trim() : null);
        hotel.setCheckInTime(checkinField.getText() != null ? checkinField.getText().trim() : null);
        hotel.setCheckOutTime(checkoutField.getText() != null ? checkoutField.getText().trim() : null);

        // Add rooms
        hotel.getRoomTypes().addAll(rooms);

        // Add amenities
        hotel.getAmenities().addAll(getSelectedAmenities());

        // Add images
        hotel.getImages().addAll(images);

        return hotel;
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
}

