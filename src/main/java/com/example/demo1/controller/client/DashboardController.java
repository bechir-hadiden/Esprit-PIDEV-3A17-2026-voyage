package com.example.demo1.controller.client;
import com.example.demo1.HelloApplication;
import com.example.demo1.controller.dao.HotelDAO;
import com.example.demo1.entity.Hotel;
import com.example.demo1.services.BookingService;
import com.example.demo1.services.SessionManager;
import com.example.demo1.Utils.Formatter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    @FXML
    private Label totalTripsLabel;
    @FXML
    private Label hotelBookingsLabel;
    @FXML
    private Label upcomingLabel;
    @FXML
    private Label savedLabel;
    @FXML
    private HBox featuredHotelsContainer;
    @FXML
    private VBox recentSearchesContainer;

    private final BookingService bookingService = BookingService.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final HotelDAO hotelDAO = new HotelDAO();

    @FXML
    public void initialize() {
        updateStats();
        loadFeaturedHotels();
        loadRecentSearches();
    }

    private void updateStats() {
        int totalTrips = bookingService.getHotelBookings().size() + bookingService.getPlanBookings().size();
        int hotelBookings = (int) bookingService.getHotelBookings().stream()
                .filter(b -> b.getStatus() == com.example.demo1.entity.Booking.Status.CONFIRMED)
                .count();
        int upcoming = (int) bookingService.getHotelBookings().stream()
                .filter(b -> b.getStatus() == com.example.demo1.entity.Booking.Status.CONFIRMED
                        && b.getCheckIn().isAfter(LocalDate.now()))
                .count();

        totalTripsLabel.setText(String.valueOf(totalTrips));
        hotelBookingsLabel.setText(String.valueOf(hotelBookings));
        upcomingLabel.setText(String.valueOf(upcoming));
        savedLabel.setText("0"); // Would be implemented with favorites feature
    }

    private void loadFeaturedHotels() {
        List<Hotel> allHotels = hotelDAO.getAllHotels();
        List<Hotel> featuredHotels = allHotels.subList(0, Math.min(3, allHotels.size()));

        for (Hotel hotel : featuredHotels) {
            VBox card = createHotelCard(hotel);
            featuredHotelsContainer.getChildren().add(card);
        }
    }

    private VBox createHotelCard(Hotel hotel) {
        VBox card = new VBox();
        card.getStyleClass().add("hotel-card");
        card.setPrefWidth(280);
        card.setOnMouseClicked(e -> {
            sessionManager.setSelectedHotel(hotel);
            HelloApplication.showView(SessionManager.View.HOTEL_DETAILS);
        });

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        if (!hotel.getImages().isEmpty()) {
            try {
                Image image = new Image(hotel.getImages().get(0), true);
                imageView.setImage(image);
            } catch (Exception e) {
                // Use placeholder if image fails to load
            }
        }

        Rectangle clip = new Rectangle(280, 160);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageView.setClip(clip);

        // Rating badge overlay
        StackPane imagePane = new StackPane(imageView);

        HBox ratingBadge = new HBox(4);
        ratingBadge.getStyleClass().add("rating-badge");
        ratingBadge.setAlignment(Pos.CENTER);
        ratingBadge.setPadding(new Insets(6, 12, 6, 12));

        Label starsLabel = new Label("\uf005\uf005\uf005\uf005\uf005");
        starsLabel.getStyleClass().add("rating-stars");

        Label ratingValue = new Label(Formatter.formatRating(hotel.getRating()));
        ratingValue.getStyleClass().add("rating-value");

        ratingBadge.getChildren().addAll(starsLabel, ratingValue);
        StackPane.setAlignment(ratingBadge, Pos.TOP_LEFT);
        StackPane.setMargin(ratingBadge, new Insets(12));

        imagePane.getChildren().add(ratingBadge);

        // Content
        VBox content = new VBox(8);
        content.setPadding(new Insets(16));

        Label nameLabel = new Label(hotel.getName());
        nameLabel.getStyleClass().add("hotel-name");
        nameLabel.setWrapText(true);

        HBox locationBox = new HBox(6);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍"); // Map marker
        locationIcon.setStyle("-fx-text-fill: #64748B;");
        Label locationLabel = new Label(hotel.getCity());
        locationLabel.getStyleClass().add("hotel-location");
        locationBox.getChildren().addAll(locationIcon, locationLabel);

        HBox priceBox = new HBox(4);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label(Formatter.formatCurrencyNoCents(hotel.getPricePerNight()));
        priceLabel.getStyleClass().add("hotel-price");
        Label unitLabel = new Label("/night");
        unitLabel.getStyleClass().add("hotel-price-unit");
        priceBox.getChildren().addAll(priceLabel, unitLabel);

        content.getChildren().addAll(nameLabel, locationBox, priceBox);

        card.getChildren().addAll(imagePane, content);

        return card;
    }

    private void loadRecentSearches() {
        // Sample recent searches
        String[][] searches = {
                { "New York, USA", "Mar 15 - Mar 20", "Hotel" },
                { "Paris, France", "Apr 10 - Apr 15", "Flight + Hotel" },
                { "Tokyo, Japan", "May 1 - May 10", "Package" }
        };

        for (String[] search : searches) {
            HBox item = new HBox(12);
            item.setAlignment(Pos.CENTER_LEFT);
            item.getStyleClass().add("recent-search-item");
            item.setPadding(new Insets(12));
            item.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-cursor: hand;");

            StackPane iconBg = new StackPane();
            iconBg.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
            iconBg.setPrefSize(40, 40);

            Label icon = new Label("🕒"); // Clock
            icon.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 14;");
            iconBg.getChildren().add(icon);

            VBox info = new VBox(4);
            info.setAlignment(Pos.CENTER_LEFT);

            Label location = new Label(search[0]);
            location.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 14; -fx-font-weight: 500;");

            Label dates = new Label(search[1]);
            dates.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12;");

            info.getChildren().addAll(location, dates);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label typeLabel = new Label(search[2]);
            typeLabel.setStyle(
                    "-fx-background-color: #E2E8F0; -fx-text-fill: #64748B; -fx-background-radius: 10; -fx-padding: 4 12; -fx-font-size: 11;");

            item.getChildren().addAll(iconBg, info, spacer, typeLabel);

            recentSearchesContainer.getChildren().add(item);
        }
    }

    @FXML
    private void exploreHotels() {
        HelloApplication.showView(SessionManager.View.HOTELS);
    }

    @FXML
    private void bookTransport() {
        HelloApplication.showView(SessionManager.View.TRANSPORT);
    }

    @FXML
    private void viewPlans() {
        HelloApplication.showView(SessionManager.View.TICKET_PLANS);
    }

    @FXML
    private void viewAllHotels() {
        HelloApplication.showView(SessionManager.View.HOTELS);
    }
}

