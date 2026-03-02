package com.example.demo1.controller.client;

import com.example.demo1.entity.Booking;
import com.example.demo1.entity.Hotel;
import com.example.demo1.entity.RoomType;
import com.example.demo1.HelloApplication;
import com.example.demo1.services.BookingService;
import com.example.demo1.services.SessionManager;
import com.example.demo1.services.AuthService;
import com.example.demo1.Utils.Formatter;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.time.LocalDate;
import java.sql.Date;

import org.example.services.StripeService;
import org.example.services.PaiementService;
import org.example.entities.Paiement;
import org.example.utils.PDFService;
import java.io.File;
import java.awt.Desktop;

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

    // Payment Selection Fields
    @FXML
    private Label walletBalanceLabel;
    @FXML
    private Label loyaltyPointsLabel;
    @FXML
    private ToggleGroup paymentGroup;
    @FXML
    private ToggleButton stripeToggle;
    @FXML
    private ToggleButton walletToggle;
    @FXML
    private ToggleButton paypalToggle;
    @FXML
    private ToggleButton d17Toggle;
    @FXML
    private VBox stripeDetails;
    @FXML
    private VBox walletDetails;
    @FXML
    private VBox paypalDetails;
    @FXML
    private VBox d17Details;
    @FXML
    private Label walletStatusLabel;

    @FXML
    private VBox guestDetailsForm;
    @FXML
    private VBox paymentForm;
    @FXML
    private VBox confirmationView;
    @FXML
    private Label confirmIconLabel;
    @FXML
    private Label confirmTitleLabel;
    @FXML
    private Label confirmSubLabel;
    @FXML
    private Button downloadPdfButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BookingService bookingService = BookingService.getInstance();
    private final PaiementService paiementService = new PaiementService();

    private Hotel hotel;
    private RoomType room;
    private int step = 1;
    private String currentStripeSessionId;
    private boolean isPaymentVerified = false;
    private double bookingTotalAmount = 0.0;
    private Paiement lastPaiement;

    @FXML
    public void initialize() {
        hotel = sessionManager.getSelectedHotel();
        room = sessionManager.getSelectedRoom();

        if (hotel == null || room == null) {
            HelloApplication.showView(SessionManager.View.HOTELS);
            return;
        }

        prefillGuestInfoFromUser();
        loadBookingSummary();
        step = 2; // Open directly on payment after selecting a room
        updateStepUI();
        setupPaymentToggles();
    }

    private void prefillGuestInfoFromUser() {
        var user = AuthService.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String fullName = user.getFullName() != null ? user.getFullName().trim() : "";
        if (!fullName.isEmpty()) {
            String[] parts = fullName.split("\\s+", 2);
            firstNameField.setText(parts[0]);
            lastNameField.setText(parts.length > 1 ? parts[1] : "");
        }

        emailField.setText(user.getEmail() != null ? user.getEmail() : "");

        String phone = user.getPhone();
        if (phone == null || phone.trim().isEmpty()) {
            phone = user.getTelephone();
        }
        phoneField.setText(phone != null ? phone : "");
    }

    private void setupPaymentToggles() {
        if (paymentGroup == null)
            return;

        paymentGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                stripeToggle.setSelected(true); // Default to Stripe
                return;
            }

            stripeDetails.setVisible(newT == stripeToggle);
            walletDetails.setVisible(newT == walletToggle);
            paypalDetails.setVisible(newT == paypalToggle);
            d17Details.setVisible(newT == d17Toggle);

            // Refresh button text
            if (step == 2) {
                if (newT == stripeToggle) {
                    continueButton.setText(currentStripeSessionId == null ? "Pay with Stripe" : "Verify Payment");
                } else if (newT == walletToggle) {
                    continueButton.setText("Pay with Wallet");
                } else {
                    continueButton.setText("Continue to " + ((ToggleButton) newT).getText());
                }
            }
        });

        stripeToggle.setSelected(true);
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
        bookingTotalAmount = total;

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

        if (step == 2) {
            updatePaymentDetails();
        } else if (step == 3) {
            continueButton.setText("Confirm Booking");
        } else {
            continueButton.setText("Continue");
        }
    }

    private void updatePaymentDetails() {
        var user = AuthService.getInstance().getCurrentUser();
        if (user != null) {
            walletBalanceLabel.setText(String.format("%.2f DT", user.getWalletBalance()));
            loyaltyPointsLabel.setText(user.getLoyaltyPoints() + " pts");

            if (user.getWalletBalance() < bookingTotalAmount) {
                walletStatusLabel.setText(
                        "Insufficient balance (" + String.format("%.2f", bookingTotalAmount) + " DT required)");
                walletStatusLabel.setTextFill(Color.RED);
            } else {
                walletStatusLabel.setText("Pay instantly using your wallet balance.");
                walletStatusLabel.setTextFill(Color.valueOf("#64748B"));
            }
        }

        ToggleButton sel = (ToggleButton) paymentGroup.getSelectedToggle();
        if (sel == stripeToggle) {
            continueButton.setText(currentStripeSessionId == null ? "Pay with Stripe" : "Verify Payment");
        } else if (sel == walletToggle) {
            continueButton.setText("Pay with Wallet");
        } else {
            continueButton.setText("Proceed to Payment");
        }
    }

    @FXML
    private void handleContinue() {
        if (step == 1) {
            if (validateGuestInfo()) {
                step = 2;
                updateStepUI();
            }
        } else if (step == 2) {
            if (!validateGuestInfo()) {
                step = 1;
                updateStepUI();
                return;
            }
            ToggleButton sel = (ToggleButton) paymentGroup.getSelectedToggle();
            if (sel == stripeToggle) {
                if (currentStripeSessionId == null)
                    processStripePayment();
                else
                    verifyStripePayment();
            } else if (sel == walletToggle) {
                processWalletPayment();
            } else {
                processExternalPayment(sel.getText());
            }
        } else if (step == 3) {
            if (lastPaiement != null) {
                HelloApplication.showView(SessionManager.View.MY_BOOKINGS);
            } else {
                confirmBooking();
            }
        }
    }

    private void processStripePayment() {
        System.out.println("[Stripe] Pay action triggered. total=" + bookingTotalAmount);
        if (!StripeService.isConfigured()) {
            showAlert(
                    "Stripe is not configured. Set stripe.api.key in ./config.properties (project root) or STRIPE_API_KEY env var.");
            return;
        }
        if (bookingTotalAmount <= 0) {
            showAlert("Invalid booking total. Please go back and select room dates again.");
            return;
        }

        continueButton.setDisable(true);
        continueButton.setText("Connecting to Stripe...");

        Task<StripeService.StripeCheckoutResult> checkoutTask = new Task<>() {
            @Override
            protected StripeService.StripeCheckoutResult call() throws Exception {
                return StripeService.createCheckoutSession(bookingTotalAmount);
            }
        };

        checkoutTask.setOnSucceeded(event -> {
            StripeService.StripeCheckoutResult result = checkoutTask.getValue();
            System.out.println("[Stripe] Checkout session created. sessionId="
                    + (result != null ? result.sessionId : "null"));
            if (result == null || result.url == null || result.url.isBlank()) {
                continueButton.setDisable(false);
                updateStepUI();
                showAlert("Stripe checkout link could not be created.");
                return;
            }

            currentStripeSessionId = result.sessionId;
            boolean opened = HelloApplication.openExternalUrl(result.url);
            continueButton.setDisable(false);
            updateStepUI();

            if (opened) {
                showInfo("Stripe checkout opened in your browser. After payment, click 'Verify Payment'.");
            } else {
                copyToClipboard(result.url);
                showLinkDialog(result.url);
            }
        });

        checkoutTask.setOnFailed(event -> {
            Throwable ex = checkoutTask.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
            continueButton.setDisable(false);
            updateStepUI();
            showAlert("Error initiating payment: " + extractRootMessage(ex));
        });

        Thread stripeThread = new Thread(checkoutTask, "stripe-checkout-task");
        stripeThread.setDaemon(true);
        stripeThread.start();
    }

    private void verifyStripePayment() {
        if (StripeService.isSessionPaid(currentStripeSessionId)) {
            isPaymentVerified = true;
            step = 3;
            updateStepUI();
            showAlert("Payment verified successfully!");
        } else {
            showAlert("Payment not completed yet. Please finish the payment in your browser.");
        }
    }

    private void processWalletPayment() {
        var user = AuthService.getInstance().getCurrentUser();
        if (user == null) {
            showAlert("No active user session. Please sign in again.");
            return;
        }
        double total = bookingTotalAmount;

        if (user.getWalletBalance() >= total) {
            double previousBalance = user.getWalletBalance();
            int previousPoints = user.getLoyaltyPoints();

            // Deduct immediately (in memory first)
            user.setWalletBalance(user.getWalletBalance() - total);
            // Award loyalty points (5% of base price)
            user.setLoyaltyPoints(user.getLoyaltyPoints() + (int) (total * 0.05));

            // Sync with DB
            boolean updated = new com.example.demo1.controller.dao.UserDAO().updateUser(user);
            if (!updated) {
                user.setWalletBalance(previousBalance);
                user.setLoyaltyPoints(previousPoints);
                showAlert("Wallet payment failed: could not update wallet in database.");
                return;
            }

            isPaymentVerified = true;
            step = 3;
            updateStepUI();
            showAlert("Success! Amount deducted from your wallet.");
        } else {
            showAlert("Insufficient wallet balance. Recharge from My Wallet then retry.");
        }
    }

    private void processExternalPayment(String method) {
        // Mock redirect for PayPal/D17
        showAlert("Redirecting to " + method + " secure gateway...");
        // In a real app, this would open a browser similar to Stripe
        isPaymentVerified = true; // For demo, we auto-verify
        step = 3;
        updateStepUI();
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

        if (booking != null && isPaymentVerified) {
            // Record payment in database
            Paiement p = new Paiement();
            p.setMontant(total);
            p.setDatePaiement(Date.valueOf(LocalDate.now()));
            p.setStatut_paiement("Effectu\u00E9");

            ToggleButton sel = (ToggleButton) paymentGroup.getSelectedToggle();
            if (sel == stripeToggle) {
                p.setMethodePaiement("Carte Bancaire (Stripe)");
                p.setStripeSessionId(currentStripeSessionId);
            } else if (sel == walletToggle) {
                p.setMethodePaiement("Portefeuille Interne");
            } else {
                p.setMethodePaiement(sel.getText());
            }

            try {
                p.setBookingId(Integer.parseInt(booking.getId()));
            } catch (Exception e) {
                // Ignore if ID is not numeric
            }

            // Link to user if available
            var currentUser = AuthService.getInstance().getCurrentUser();
            if (currentUser != null) {
                try {
                    p.setUserId(Integer.parseInt(currentUser.getId()));
                } catch (NumberFormatException e) {
                    showAlert("Payment record warning: invalid user id format.");
                }
            }

            boolean paymentSaved = paiementService.ajouter(p);
            if (!paymentSaved) {
                showAlert("Payment was completed but could not be saved to database.");
            } else {
                this.lastPaiement = p;

                // Auto-open PDF
                handleDownloadPDF();

                // Update UI state
                confirmIconLabel.setText("\u2728"); // Sparkles
                confirmTitleLabel.setText("R\u00E9servation Confirm\u00E9e !");
                confirmSubLabel.setText(
                        "Votre voyage est pr\u00EAt. Vous pouvez t\u00E9l\u00E9charger votre re\u00E7u ci-dessous.");
                downloadPdfButton.setVisible(true);
                downloadPdfButton.setManaged(true);
                continueButton.setText("Aller \u00E0 Mes R\u00E9servations");
            }
        }
    }

    @FXML
    private void handleDownloadPDF() {
        if (lastPaiement == null) {
            showAlert("Aucun paiement trouv\u00E9 pour cette session.");
            return;
        }

        try {
            var user = AuthService.getInstance().getCurrentUser();
            String fileName = "Confirmation_Paiement_" + lastPaiement.getIdPaiement() + ".pdf";
            String filePath = System.getProperty("user.home") + File.separator + fileName;

            org.example.entities.User entUser = new org.example.entities.User();
            if (user != null) {
                entUser.setFull_name(user.getFullName());
                entUser.setEmail(user.getEmail());
            }

            PDFService.generatePaymentReceipt(lastPaiement, entUser, filePath);

            File file = new File(filePath);
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            }
        } catch (Exception e) {
            showAlert("\u00C9chec de g\u00E9n\u00E9ration du PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void copyToClipboard(String url) {
        ClipboardContent content = new ClipboardContent();
        content.putString(url);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void showLinkDialog(String url) {
        TextInputDialog dialog = new TextInputDialog(url);
        dialog.setTitle("Open Stripe Checkout");
        dialog.setHeaderText("Browser did not open automatically");
        dialog.setContentText("Checkout URL (already copied):");
        dialog.showAndWait();
    }

    private String extractRootMessage(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error";
        }

        Throwable cursor = throwable;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }

        String message = cursor.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.getMessage();
        }
        return (message == null || message.isBlank()) ? cursor.getClass().getSimpleName() : message;
    }
}
