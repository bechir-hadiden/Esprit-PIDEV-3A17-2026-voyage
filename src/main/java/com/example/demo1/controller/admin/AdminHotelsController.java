package com.example.demo1.controller.admin;
import com.example.demo1.controller.dao.HotelDAO;
import com.example.demo1.entity.Hotel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AdminHotelsController {

    @FXML
    private TableView<Hotel> hotelsTable;
    @FXML
    private TableColumn<Hotel, String> idColumn;
    @FXML
    private TableColumn<Hotel, String> nameColumn;
    @FXML
    private TableColumn<Hotel, String> cityColumn;
    @FXML
    private TableColumn<Hotel, String> countryColumn;
    @FXML
    private TableColumn<Hotel, Double> priceColumn;
    @FXML
    private TableColumn<Hotel, Double> ratingColumn;
    @FXML
    private TableColumn<Hotel, Void> actionsColumn;

    private final HotelDAO hotelDAO = new HotelDAO();

    @FXML
    public void initialize() {
        setupTable();
        loadHotels();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Format price column
        priceColumn.setCellFactory(col -> new TableCell<Hotel, Double>() {
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

        // Format rating column
        ratingColumn.setCellFactory(col -> new TableCell<Hotel, Double>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f ⭐", rating));
                }
            }
        });

        // Actions column - Edit and Delete buttons
        actionsColumn.setCellFactory(col -> new TableCell<Hotel, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle(
                        "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                deleteBtn.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");

                editBtn.setOnAction(e -> {
                    Hotel hotel = getTableView().getItems().get(getIndex());
                    handleEditHotel(hotel);
                });

                deleteBtn.setOnAction(e -> {
                    Hotel hotel = getTableView().getItems().get(getIndex());
                    handleDeleteHotel(hotel);
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
    }

    private void loadHotels() {
        try {
            List<Hotel> hotels = hotelDAO.getAllHotels();
            hotelsTable.setItems(FXCollections.observableArrayList(hotels));
            System.out.println("Loaded " + hotels.size() + " hotels from database");
        } catch (Exception e) {
            System.err.println("Error loading hotels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Add Hotel button click.
     */
    @FXML
    private void handleAddHotel() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/HotelFormDialog.fxml"));
            VBox dialogRoot = loader.load();
            HotelFormDialogController controller = loader.getController();
            controller.initializeForAdd();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Hotel");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                loadHotels(); // Refresh table
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to open add hotel dialog: " + e.getMessage());
        }
    }

    /**
     * Handle Edit Hotel.
     */
    private void handleEditHotel(Hotel hotel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/HotelFormDialog.fxml"));
            VBox dialogRoot = loader.load();
            HotelFormDialogController controller = loader.getController();
            controller.initializeForEdit(hotel);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Hotel");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                loadHotels(); // Refresh table
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to open edit hotel dialog: " + e.getMessage());
        }
    }

    /**
     * Handle Delete Hotel.
     */
    private void handleDeleteHotel(Hotel hotel) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Hotel");
        confirmDialog.setHeaderText("Delete " + hotel.getName() + "?");
        confirmDialog.setContentText(
                "This action cannot be undone. All related data (rooms, amenities, policies) will also be deleted.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = hotelDAO.deleteHotel(Integer.parseInt(hotel.getId()));
                if (success) {
                    loadHotels(); // Refresh table
                    showInfo("Hotel deleted successfully");
                } else {
                    showError("Failed to delete hotel. Please try again.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error deleting hotel: " + e.getMessage());
            }
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
     * Show info message dialog.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

