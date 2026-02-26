package com.example.demo1.controller.client;
import com.example.demo1.entity.Booking;
import com.example.demo1.entity.PlanBooking;
import com.example.demo1.services.AuthService;
import com.example.demo1.services.BookingService;
import com.example.demo1.Utils.Formatter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MyBookingsController {

    @FXML private Label totalBookingsLabel;
    @FXML private Label hotelBookingsLabel;
    @FXML private Label planBookingsLabel;
    @FXML private Label totalSpentLabel;
    @FXML private TabPane bookingsTabPane;
    @FXML private VBox hotelBookingsContainer;
    @FXML private VBox planBookingsContainer;

    private final BookingService bookingService = BookingService.getInstance();

    @FXML
    public void initialize() {
        if (AuthService.getInstance().isLoggedIn()) {
            bookingService.loadHotelBookingsFromDb();
        }
        updateStats();
        loadHotelBookings();
        loadPlanBookings();
    }

    private void updateStats() {
        int totalBookings = bookingService.getTotalActiveBookings();
        int hotelCount = (int) bookingService.getHotelBookings().stream()
                .filter(b -> b.getStatus() == Booking.Status.CONFIRMED)
                .count();
        int planCount = (int) bookingService.getPlanBookings().stream()
                .filter(b -> b.getStatus() == PlanBooking.Status.ACTIVE)
                .count();
        double totalSpent = bookingService.getTotalSpent();

        totalBookingsLabel.setText(String.valueOf(totalBookings));
        hotelBookingsLabel.setText(String.valueOf(hotelCount));
        planBookingsLabel.setText(String.valueOf(planCount));
        totalSpentLabel.setText(Formatter.formatCurrencyNoCents(totalSpent));
    }

    private void loadHotelBookings() {
        hotelBookingsContainer.getChildren().clear();

        var activeBookings = bookingService.getHotelBookings().filtered(
                b -> b.getStatus() == Booking.Status.CONFIRMED
        );

        if (activeBookings.isEmpty()) {
            VBox emptyState = createEmptyState("No hotel bookings yet", "Start exploring and book your first stay");
            hotelBookingsContainer.getChildren().add(emptyState);
            return;
        }

        for (Booking booking : activeBookings) {
            HBox card = createHotelBookingCard(booking);
            hotelBookingsContainer.getChildren().add(card);
        }
    }

    private HBox createHotelBookingCard(Booking booking) {
        HBox card = new HBox(24);
        card.getStyleClass().add("booking-card");
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER_LEFT);

        // Hotel info
        VBox info = new VBox(8);
        info.setPrefWidth(200);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBg = new StackPane();
        iconBg.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 12;");
        iconBg.setPrefSize(48, 48);

        Label icon = new Label("\uf236");
        icon.setStyle("-fx-font-family: 'FontAwesome'; -fx-text-fill: #2563EB; -fx-font-size: 20;");
        iconBg.getChildren().add(icon);

        VBox nameBox = new VBox(4);
        Label nameLabel = new Label(booking.getHotelName());
        nameLabel.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 16; -fx-font-weight: bold;");
        Label roomLabel = new Label(booking.getRoomType());
        roomLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        nameBox.getChildren().addAll(nameLabel, roomLabel);

        header.getChildren().addAll(iconBg, nameBox);

        Label statusLabel = new Label("Confirmed");
        statusLabel.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #166534; -fx-background-radius: 10; -fx-padding: 4 12; -fx-font-size: 12;");

        info.getChildren().addAll(header, statusLabel);

        // Details
        GridPane details = new GridPane();
        details.setHgap(24);
        details.setVgap(12);
        HBox.setHgrow(details, Priority.ALWAYS);

        addDetail(details, 0, 0, "Check-in", Formatter.formatDate(booking.getCheckIn()));
        addDetail(details, 1, 0, "Check-out", Formatter.formatDate(booking.getCheckOut()));
        addDetail(details, 2, 0, "Guests", String.valueOf(booking.getGuestCount()));
        addDetail(details, 3, 0, "Total", booking.getFormattedTotalPrice());

        // Cancel button
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("outline-button");
        cancelBtn.setStyle("-fx-border-color: #FECACA; -fx-text-fill: #EF4444;");
        cancelBtn.setOnAction(e -> cancelBooking(booking.getId()));

        card.getChildren().addAll(info, details, cancelBtn);

        return card;
    }

    private void addDetail(GridPane grid, int col, int row, String label, String value) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 14; -fx-font-weight: 500;");
        box.getChildren().addAll(lbl, val);
        grid.add(box, col, row);
    }

    private void loadPlanBookings() {
        planBookingsContainer.getChildren().clear();

        var activePlans = bookingService.getPlanBookings().filtered(
                b -> b.getStatus() == PlanBooking.Status.ACTIVE
        );

        if (activePlans.isEmpty()) {
            VBox emptyState = createEmptyState("No active plans", "Subscribe to a plan and unlock exclusive benefits");
            planBookingsContainer.getChildren().add(emptyState);
            return;
        }

        for (PlanBooking plan : activePlans) {
            HBox card = createPlanBookingCard(plan);
            planBookingsContainer.getChildren().add(card);
        }
    }

    private HBox createPlanBookingCard(PlanBooking plan) {
        HBox card = new HBox(24);
        card.getStyleClass().add("booking-card");
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER_LEFT);

        // Plan info
        VBox info = new VBox(8);
        info.setPrefWidth(200);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBg = new StackPane();
        iconBg.setStyle("-fx-background-color: #F3E8FF; -fx-background-radius: 12;");
        iconBg.setPrefSize(48, 48);

        Label icon = new Label("\uf145");
        icon.setStyle("-fx-font-family: 'FontAwesome'; -fx-text-fill: #9333EA; -fx-font-size: 20;");
        iconBg.getChildren().add(icon);

        VBox nameBox = new VBox(4);
        Label nameLabel = new Label(plan.getPlanName());
        nameLabel.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 16; -fx-font-weight: bold;");
        Label durationLabel = new Label(plan.getDuration());
        durationLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        nameBox.getChildren().addAll(nameLabel, durationLabel);

        header.getChildren().addAll(iconBg, nameBox);

        Label statusLabel = new Label("Active");
        statusLabel.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #166534; -fx-background-radius: 10; -fx-padding: 4 12; -fx-font-size: 12;");

        info.getChildren().addAll(header, statusLabel);

        // Details
        GridPane details = new GridPane();
        details.setHgap(48);
        details.setVgap(12);
        HBox.setHgrow(details, Priority.ALWAYS);

        addDetail(details, 0, 0, "Start Date", Formatter.formatDate(plan.getStartDate()));
        addDetail(details, 1, 0, "Price", plan.getFormattedPrice());

        // Cancel button
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("outline-button");
        cancelBtn.setStyle("-fx-border-color: #FECACA; -fx-text-fill: #EF4444;");
        cancelBtn.setOnAction(e -> cancelPlan(plan.getId()));

        card.getChildren().addAll(info, details, cancelBtn);

        return card;
    }

    private VBox createEmptyState(String title, String subtitle) {
        VBox empty = new VBox(16);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(64));

        StackPane iconBg = new StackPane();
        iconBg.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 50%;");
        iconBg.setPrefSize(80, 80);

        Label icon = new Label("\uf236");
        icon.setStyle("-fx-font-family: 'FontAwesome'; -fx-text-fill: #94A3B8; -fx-font-size: 32;");
        iconBg.getChildren().add(icon);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 18; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14;");

        empty.getChildren().addAll(iconBg, titleLabel, subtitleLabel);

        return empty;
    }

    private void cancelBooking(String bookingId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Booking");
        confirm.setHeaderText("Are you sure you want to cancel this reservation?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                bookingService.cancelHotelBooking(bookingId);
                loadHotelBookings();
                updateStats();
            }
        });
    }

    private void cancelPlan(String planId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Subscription");
        confirm.setHeaderText("Are you sure you want to cancel this subscription?");
        confirm.setContentText("Your benefits will continue until the end of the billing period.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                bookingService.cancelPlanBooking(planId);
                loadPlanBookings();
                updateStats();
            }
        });
    }
}

