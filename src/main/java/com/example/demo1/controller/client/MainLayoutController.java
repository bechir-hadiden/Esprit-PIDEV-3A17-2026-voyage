package com.example.demo1.controller.client;

import com.example.demo1.HelloApplication;
import com.example.demo1.services.AuthService;
import com.example.demo1.services.BookingService;
import com.example.demo1.services.SessionManager;
import com.example.demo1.services.SessionManager.View;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import java.io.IOException;

public class MainLayoutController {

    @FXML
    private BorderPane rootPane;
    @FXML
    private VBox sidebar;
    @FXML
    private Label logoLabel;
    @FXML
    private VBox userProfileSection;
    @FXML
    private VBox userInfoSection;
    @FXML
    private Circle avatarCircle;
    @FXML
    private Label avatarInitials;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userEmailLabel;
    @FXML
    private VBox navContainer;
    @FXML
    private Label dashboardLabel;
    @FXML
    private Label walletLabel;
    @FXML
    private Label hotelsLabel;
    @FXML
    private Label transportLabel;
    @FXML
    private Label plansLabel;
    @FXML
    private Label bookingsLabel;
    @FXML
    private Label bookingCountBadge;
    @FXML
    private Label logoutLabel;
    @FXML
    private Label collapseIcon;
    @FXML
    private Button collapseBtn;
    @FXML
    private Label pageTitle;
    @FXML
    private Label topBarBookingCount;
    @FXML
    private VBox contentContainer;

    @FXML
    private Button dashboardBtn;
    @FXML
    private Button walletBtn;
    @FXML
    private Button hotelsBtn;
    @FXML
    private Button transportBtn;
    @FXML
    private Button plansBtn;
    @FXML
    private Button bookingsBtn;

    @FXML
    private Button DecartionBtn;


    private final SessionManager sessionManager = SessionManager.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private final BookingService bookingService = BookingService.getInstance();

    private boolean sidebarCollapsed = false;

    @FXML
    public void initialize() {
        // Set up user info
        var user = authService.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getFullName());
            userEmailLabel.setText(user.getEmail());
            avatarInitials.setText(user.getInitials());
        }

        // Update booking counts
        updateBookingCounts();

        // Listen for booking changes
        bookingService.getHotelBookings()
                .addListener((javafx.collections.ListChangeListener<com.example.demo1.entity.Booking>) c -> {
                    Platform.runLater(this::updateBookingCounts);
                });
        bookingService.getPlanBookings()
                .addListener((javafx.collections.ListChangeListener<com.example.demo1.entity.PlanBooking>) c -> {
                    Platform.runLater(this::updateBookingCounts);
                });

        // Load initial view
        loadView(sessionManager.getCurrentView());

        // Apply sidebar collapsed state
        if (sessionManager.isSidebarCollapsed()) {
            toggleSidebar();
        }
    }

    private void updateBookingCounts() {
        int count = bookingService.getTotalActiveBookings();
        bookingCountBadge.setText(String.valueOf(count));
        bookingCountBadge.setVisible(count > 0);
        topBarBookingCount.setText(String.valueOf(count));
        topBarBookingCount.setVisible(count > 0);
    }

    private void loadView(View view) {
        try {
            sessionManager.setCurrentView(view);

            // Update page title
            pageTitle.setText(getPageTitle(view));

            // Update active nav button
            updateActiveNavButton(view);

            // Load content
            String fxmlFile = getContentFxml(view);
            if (fxmlFile != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Node content = loader.load();
                contentContainer.getChildren().setAll(content);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPageTitle(View view) {
        switch (view) {
            case DASHBOARD:
                return "Dashboard";
            case MY_WALLET:
                return "My Wallet";
            case HOTELS:
                return "Find Hotels";
            case HOTEL_DETAILS:
                return "Hotel Details";
            case BOOKING:
                return "Complete Booking";
            case TRANSPORT:
                return "Book Transport";
            case OFFERS:
                return "Exclusive Offers";
            case TICKET_PLANS:
                return "Ticket Plans";
            case MY_BOOKINGS:
                return "My Bookings";
            case Decartion:
                return " Decartion";
            case SETTINGS:
                return "Settings";
            default:
                return "Dashboard";
        }
    }

    private String getContentFxml(View view) {
        switch (view) {
            case DASHBOARD:
                return "/fxml/client/Dashboard.fxml";
            case MY_WALLET:
                return "/fxml/client/MyWallet.fxml";
            case HOTELS:
                return "/fxml/client/Hotels.fxml";
            case HOTEL_DETAILS:
                return "/fxml/client/HotelDetails.fxml";
            case BOOKING:
                return "/fxml/client/Booking.fxml";
            case TRANSPORT:
                return "/fxml/user_menu.fxml";
            case OFFERS:
                return "/fxml/client/ConsultationOffres.fxml";
            case TICKET_PLANS:
                return "/fxml/client/TicketPlans.fxml";
            case MY_BOOKINGS:
                return "/fxml/client/MyBookings.fxml";
            case  Decartion:
                return "/fxml/gestion-reclamation.fxml";
            case SETTINGS:
                return "/fxml/client/Settings.fxml";
            default:
                return "/fxml/client/Dashboard.fxml";
        }
    }

    private void updateActiveNavButton(View view) {
        // Reset all buttons
        dashboardBtn.getStyleClass().remove("nav-button-active");
        walletBtn.getStyleClass().remove("nav-button-active");
        hotelsBtn.getStyleClass().remove("nav-button-active");
        transportBtn.getStyleClass().remove("nav-button-active");
        plansBtn.getStyleClass().remove("nav-button-active");
        bookingsBtn.getStyleClass().remove("nav-button-active");

        // Set active button
        switch (view) {
            case DASHBOARD:
                dashboardBtn.getStyleClass().add("nav-button-active");
                break;
            case MY_WALLET:
                walletBtn.getStyleClass().add("nav-button-active");
                break;
            case HOTELS:
            case HOTEL_DETAILS:
            case BOOKING:
                hotelsBtn.getStyleClass().add("nav-button-active");
                break;
            case TRANSPORT:
                transportBtn.getStyleClass().add("nav-button-active");
                break;
            case TICKET_PLANS:
                plansBtn.getStyleClass().add("nav-button-active");
                break;
            case MY_BOOKINGS:
                bookingsBtn.getStyleClass().add("nav-button-active");
                break;
            case SETTINGS:
                // Optionally highlight a settings button if it exists in nav
                break;
            case FORGOT_PASSWORD:
                // Not in sidebar
                break;
        }
    }

    @FXML
    private void showDashboard() {
        loadView(View.DASHBOARD);
    }

    @FXML
    private void showHotels() {
        sessionManager.clearBookingSelection();
        loadView(View.HOTELS);
    }

    @FXML
    private void showWallet() {
        loadView(View.MY_WALLET);
    }

    @FXML
    private void showTransport() {
        loadView(View.TRANSPORT);
    }

    @FXML
    private void showOffers() {
        System.out.println("Bouton cliqué, chargement de OFFERS...");
        loadView(View.OFFERS);
    }

    @FXML
    private void showPlans() {
        loadView(View.TICKET_PLANS);
    }

    @FXML
    private void showBookings() {
        loadView(View.MY_BOOKINGS);
    }

    @FXML
    private void showDecartion() {
        loadView(View.Decartion);
    }

    @FXML
    private void showProfile() {
        loadView(View.SETTINGS);
        pageTitle.setText("Profile Settings");
    }

    @FXML
    private void showNotifications() {
        loadView(View.SETTINGS);
        pageTitle.setText("Notification Preferences");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be returned to the sign in page.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                authService.logout();
                HelloApplication.showView(View.SIGN_IN);
            }
        });
    }

    @FXML
    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        sessionManager.setSidebarCollapsed(sidebarCollapsed);

        if (sidebarCollapsed) {
            sidebar.setPrefWidth(80);
            sidebar.setMaxWidth(80);
            logoLabel.setVisible(false);
            logoLabel.setManaged(false);
            userInfoSection.setVisible(false);
            userInfoSection.setManaged(false);
            dashboardLabel.setVisible(false);
            walletLabel.setVisible(false);
            hotelsLabel.setVisible(false);
            transportLabel.setVisible(false);
            plansLabel.setVisible(false);
            bookingsLabel.setVisible(false);
            logoutLabel.setVisible(false);
            collapseIcon.setText("\uf054"); // Chevron Right
        } else {
            sidebar.setPrefWidth(280);
            sidebar.setMaxWidth(280);
            logoLabel.setVisible(true);
            logoLabel.setManaged(true);
            userInfoSection.setVisible(true);
            userInfoSection.setManaged(true);
            dashboardLabel.setVisible(true);
            walletLabel.setVisible(true);
            hotelsLabel.setVisible(true);
            transportLabel.setVisible(true);
            plansLabel.setVisible(true);
            bookingsLabel.setVisible(true);
            logoutLabel.setVisible(true);
            collapseIcon.setText("\uf053"); // Chevron Left
        }
    }
}
