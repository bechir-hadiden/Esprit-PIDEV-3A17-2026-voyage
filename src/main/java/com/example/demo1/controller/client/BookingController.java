package com.example.demo1.controller.client;
import com.example.demo1.entity.Booking;
import com.example.demo1.entity.Hotel;
import com.example.demo1.entity.RoomType;
import com.example.demo1.HelloApplication;
import com.example.demo1.services.BookingService;
import com.example.demo1.services.SessionManager;
import com.example.demo1.Utils.Formatter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class BookingController {

    @FXML
    private Label step1Label;
    @FXML
    private Label step2Label;
    @FXML
    private Label step3Label;
    @FXML
    private Button backButton;
    @FXML
    private Label hotelNameLabel;
    @FXML
    private Label roomTypeLabel;
    @FXML
    private Label checkInLabel;
    @FXML
    private Label checkOutLabel;
    @FXML
    private Label guestsLabel;
    @FXML
    private Label nightsLabel;
    @FXML
    private Label roomRateLabel;
    @FXML
    private Label taxesLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextArea requestsArea;
    @FXML
    private TextField cardNumberField;
    @FXML
    private TextField cardNameField;
    @FXML
    private TextField expiryField;
    @FXML
    private TextField cvvField;
    @FXML
    private Button continueButton;

    @FXML
    private VBox guestDetailsForm;
    @FXML
    private VBox paymentForm;
    @FXML
    private VBox confirmationView;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BookingService bookingService = BookingService.getInstance();
    private Hotel hotel;
    private RoomType room;
    private int step = 1;

    @FXML
    public void initialize() {
        hotel = sessionManager.getSelectedHotel();
        room = sessionManager.getSelectedRoom();

        if (hotel == null || room == null) {
            HelloApplication.showView(SessionManager.View.HOTELS);
            return;
        }

        loadBookingSummary();
        updateStepUI();
    }

    private void loadBookingSummary() {
        hotelNameLabel.setText(hotel.getName());
        roomTypeLabel.setText(room.getName());

        LocalDate checkIn = sessionManager.getSearchFilters().getCheckIn();
        LocalDate checkOut = sessionManager.getSearchFilters().getCheckOut();
        int guests = sessionManager.getSearchFilters().getGuestCount();

        if (checkIn == null)
            checkIn = LocalDate.now().plusDays(1);
        if (checkOut == null)
            checkOut = checkIn.plusDays(3);

        long nights = Formatter.getDaysBetween(checkIn, checkOut);
        double roomRate = room.getPricePerNight() * nights;
        double taxes = roomRate * 0.12;
        double total = roomRate + taxes;

        checkInLabel.setText(Formatter.formatDate(checkIn));
        checkOutLabel.setText(Formatter.formatDate(checkOut));
        guestsLabel.setText(String.valueOf(guests));
        nightsLabel.setText(String.valueOf(nights));
        roomRateLabel.setText(Formatter.formatCurrency(roomRate));
        taxesLabel.setText(Formatter.formatCurrency(taxes));
        totalLabel.setText(Formatter.formatCurrency(total));
    }

    private void updateStepUI() {
        // Update Step Indicators
        step1Label.getStyleClass().removeAll("step-active", "step-completed");
        step2Label.getStyleClass().removeAll("step-active", "step-completed");
        step3Label.getStyleClass().removeAll("step-active", "step-completed");

        if (step == 1) {
            step1Label.getStyleClass().add("step-active");
        } else {
            step1Label.getStyleClass().add("step-completed");
        }

        if (step == 2) {
            step2Label.getStyleClass().add("step-active");
        } else if (step > 2) {
            step2Label.getStyleClass().add("step-completed");
        }

        if (step == 3) {
            step3Label.getStyleClass().add("step-active");
        }

        // Update Form Visibility
        guestDetailsForm.setVisible(step == 1);
        guestDetailsForm.setManaged(step == 1);

        paymentForm.setVisible(step == 2);
        paymentForm.setManaged(step == 2);

        confirmationView.setVisible(step == 3);
        confirmationView.setManaged(step == 3);

        continueButton.setText(step == 3 ? "Confirm Booking" : "Continue");
    }

    @FXML
    private void handleContinue() {
        if (step == 1) {
            if (validateGuestInfo()) {
                step = 2;
                updateStepUI();
            }
        } else if (step == 2) {
            if (validatePaymentInfo()) {
                step = 3;
                updateStepUI();
            }
        } else if (step == 3) {
            confirmBooking();
        }
    }

    @FXML
    private void handleBack() {
        if (step > 1) {
            step--;
            updateStepUI();
        } else {
            HelloApplication.showView(SessionManager.View.HOTEL_DETAILS);
        }
    }

    private boolean validateGuestInfo() {
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty()) {
            showAlert("Please fill in all required fields");
            return false;
        }
        return true;
    }

    private boolean validatePaymentInfo() {
        if (cardNumberField.getText().trim().length() < 16 ||
                cardNameField.getText().trim().isEmpty() ||
                expiryField.getText().trim().length() < 5 ||
                cvvField.getText().trim().length() < 3) {
            showAlert("Please enter valid payment information");
            return false;
        }
        return true;
    }

    private void confirmBooking() {
        LocalDate checkIn = sessionManager.getSearchFilters().getCheckIn();
        LocalDate checkOut = sessionManager.getSearchFilters().getCheckOut();
        int guests = sessionManager.getSearchFilters().getGuestCount();

        if (checkIn == null)
            checkIn = LocalDate.now().plusDays(1);
        if (checkOut == null)
            checkOut = checkIn.plusDays(3);

        long nights = Formatter.getDaysBetween(checkIn, checkOut);
        double total = (room.getPricePerNight() * nights) * 1.12;

        int roomTypeId = 0;
        try {
            roomTypeId = Integer.parseInt(room.getId());
        } catch (NumberFormatException e) {
            // keep 0 if room id is not numeric
        }
        Booking booking = bookingService.addHotelBooking(
                hotel.getId(),
                hotel.getName(),
                room.getName(),
                roomTypeId,
                checkIn,
                checkOut,
                guests,
                total,
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                requestsArea.getText().trim());

        showSuccessAlert();
        HelloApplication.showView(SessionManager.View.MY_BOOKINGS);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Confirmed");
        alert.setHeaderText("Your reservation has been confirmed!");
        alert.setContentText("You can view all your bookings in the My Bookings section.");
        alert.showAndWait();
    }
}
