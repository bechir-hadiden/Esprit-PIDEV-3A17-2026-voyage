package com.example.demo1.controller.admin;

import com.example.demo1.controller.dao.HotelDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.services.TransportTypeService;

public class AdminDashboardController {

    @FXML
    private Label hotelCountLabel;

    @FXML
    private Label transportCountLabel;

    private final HotelDAO hotelDAO = new HotelDAO();
    private final TransportTypeService transportTypeService = new TransportTypeService();

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {
        try {
            int hotelCount = hotelDAO.getAllHotels().size();
            hotelCountLabel.setText(String.valueOf(hotelCount));
        } catch (Exception e) {
            System.err.println("Error loading hotel stats: " + e.getMessage());
            hotelCountLabel.setText("Error");
        }

        try {
            int transportCount = transportTypeService.lister().size();
            transportCountLabel.setText(String.valueOf(transportCount));
        } catch (Exception e) {
            System.err.println("Error loading transport stats: " + e.getMessage());
            if (transportCountLabel != null) {
                transportCountLabel.setText("Error");
            }
        }
    }

    @FXML
    private void handleAddHotel() {
        System.out.println("Add hotel - Full CRUD coming in next iteration");
        // TODO: Navigate to add hotel form
    }

    @FXML
    private void handleViewHotels() {
        System.out.println("View hotels - Navigating to hotels list");
        // This will trigger navigation to hotels view via parent controller
    }
}
