package com.example.demo1.controller.client;
import com.example.demo1.HelloApplication;
import com.example.demo1.controller.dao.HotelDAO;
import com.example.demo1.entity.Hotel;
import com.example.demo1.entity.RoomType;
import com.example.demo1.services.AmadeusHotelService;
import com.example.demo1.services.BookingService;
import com.example.demo1.services.SessionManager;
import com.example.demo1.Utils.Formatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class HotelDetailsController {

    @FXML
    private Label hotelNameLabel;
    @FXML
    private Label hotelLocationLabel;
    @FXML
    private StackPane mainImagePane;
    @FXML
    private VBox thumbnailsBox;
    @FXML
    private Label ratingStarsLabel;
    @FXML
    private Label ratingValueLabel;
    @FXML
    private Label reviewCountLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label checkInLabel;
    @FXML
    private Label checkOutLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label summaryRatingLabel;
    @FXML
    private Label summaryPriceLabel;
    @FXML
    private Label summaryDescriptionLabel;
    @FXML
    private VBox roomsContainer;
    @FXML
    private FlowPane amenitiesContainer;
    @FXML
    private VBox policiesContainer;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final HotelDAO hotelDAO = new HotelDAO();
    private final AmadeusHotelService amadeusHotelService = new AmadeusHotelService();
    private Hotel hotel;
    private int selectedImageIndex = 0;
    private boolean isAmadeusHotel;

    @FXML
    public void initialize() {
        Hotel selected = sessionManager.getSelectedHotel();
        if (selected == null) {
            HelloApplication.showView(SessionManager.View.HOTELS);
            return;
        }
        // Amadeus hotels have id like "AM-XXX" and are not in our DB; use session data only
        isAmadeusHotel = selected.getId() != null && selected.getId().startsWith("AM-");
        if (isAmadeusHotel) {
            hotel = selected;
        } else {
            hotel = hotelDAO.getHotelById(selected.getId());
            if (hotel == null) {
                HelloApplication.showView(SessionManager.View.HOTELS);
                return;
            }
            sessionManager.setSelectedHotel(hotel);
        }

        loadHotelData();
        if (isAmadeusHotel && amadeusHotelService.isConfigured()) {
            fetchAmadeusOffers();
        }
    }

    private void loadHotelData() {
        // Header
        hotelNameLabel.setText(nullToPlaceholder(hotel.getName(), "Hotel"));
        hotelLocationLabel.setText(nullToPlaceholder(hotel.getFullAddress(), "—"));

        // Images (with placeholder when empty)
        loadImages();

        // Rating
        ratingStarsLabel.setText(getStarString(hotel.getRating()));
        ratingValueLabel.setText(Formatter.formatRating(hotel.getRating()));
        reviewCountLabel.setText("(" + hotel.getReviewCount() + " reviews)");

        // Description
        String desc = hotel.getDescription();
        if (isAmadeusHotel && (desc == null || desc.isEmpty())) {
            desc = "This hotel is listed in Amadeus. " + hotel.getName() + " is located in " + hotel.getCity() + ", " + hotel.getCountry() + ". "
                    + "Set check-in and check-out dates on the Hotels page, then return here to load room rates. "
                    + "You can also contact the hotel directly for availability and pricing.";
        }
        descriptionLabel.setText(nullToPlaceholder(desc, "No description available."));

        // Summary section (always visible)
        if (summaryRatingLabel != null) summaryRatingLabel.setText(getStarString(hotel.getRating()) + "  " + Formatter.formatRating(hotel.getRating()));
        if (summaryDescriptionLabel != null) summaryDescriptionLabel.setText(descriptionLabel.getText());
        updateSummaryPrice();

        // Info
        String checkInVal = nullToPlaceholder(hotel.getCheckInTime(), "—");
        String checkOutVal = nullToPlaceholder(hotel.getCheckOutTime(), "—");
        String phoneVal = nullToPlaceholder(hotel.getContactPhone(), "—");
        String emailVal = nullToPlaceholder(hotel.getContactEmail(), "—");
        if (isAmadeusHotel && "—".equals(checkInVal)) checkInVal = "Set on Hotels page";
        if (isAmadeusHotel && "—".equals(checkOutVal)) checkOutVal = "Set on Hotels page";
        if (isAmadeusHotel && "—".equals(phoneVal)) phoneVal = "Contact hotel";
        if (isAmadeusHotel && "—".equals(emailVal)) emailVal = "Contact hotel";
        checkInLabel.setText(checkInVal);
        checkOutLabel.setText(checkOutVal);
        phoneLabel.setText(phoneVal);
        emailLabel.setText(emailVal);

        // Rooms (for Amadeus, may be filled later by fetchAmadeusOffers)
        loadRooms();

        // Amenities
        loadAmenities();

        // Policies
        loadPolicies();
    }

    private static String nullToPlaceholder(String value, String placeholder) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : placeholder;
    }

    private void updateSummaryPrice() {
        if (summaryPriceLabel == null) return;
        double minPrice = 0;
        for (RoomType r : hotel.getRoomTypes()) {
            if (r.getPricePerNight() > 0 && (minPrice == 0 || r.getPricePerNight() < minPrice)) {
                minPrice = r.getPricePerNight();
            }
        }
        if (minPrice > 0) {
            summaryPriceLabel.setText("From " + Formatter.formatCurrencyNoCents(minPrice) + " / night");
        } else if (isAmadeusHotel) {
            summaryPriceLabel.setText("See Rooms tab for rates (set dates on Hotels page first)");
        } else {
            summaryPriceLabel.setText(hotel.getPricePerNight() > 0 ? Formatter.formatCurrencyNoCents(hotel.getPricePerNight()) + " / night" : "—");
        }
    }

    private void fetchAmadeusOffers() {
        String amadeusId = hotel.getId().startsWith("AM-") ? hotel.getId().substring(3) : hotel.getId();
        var filters = sessionManager.getSearchFilters();
        java.time.LocalDate checkIn = filters.getCheckIn() != null ? filters.getCheckIn() : java.time.LocalDate.now().plusDays(1);
        java.time.LocalDate checkOut = filters.getCheckOut() != null ? filters.getCheckOut() : checkIn.plusDays(1);
        int adults = Math.max(1, filters.getGuestCount());
        Task<java.util.List<RoomType>> task = new Task<>() {
            @Override
            protected java.util.List<RoomType> call() throws Exception {
                return amadeusHotelService.getHotelOffers(amadeusId, checkIn, checkOut, adults);
            }
        };
        task.setOnSucceeded(e -> {
            java.util.List<RoomType> offers = task.getValue();
            if (offers != null && !offers.isEmpty()) {
                hotel.getRoomTypes().clear();
                hotel.getRoomTypes().addAll(offers);
                Platform.runLater(() -> {
                    loadRooms();
                    updateSummaryPrice();
                });
            }
        });
        task.setOnFailed(e -> {
            Platform.runLater(() -> loadRooms()); // ensure "No rooms" or loading message is replaced
        });
        new Thread(task).start();
    }

    private void loadImages() {
        if (hotel.getImages().isEmpty()) {
            VBox box = new VBox(12);
            box.setAlignment(Pos.CENTER);
            box.setMinHeight(200);
            Label icon = new Label("🖼");
            icon.setStyle("-fx-font-size: 48; -fx-text-fill: #94A3B8;");
            Label placeholder = new Label("No photos available");
            placeholder.setStyle("-fx-font-size: 16; -fx-text-fill: #64748B;");
            box.getChildren().addAll(icon, placeholder);
            mainImagePane.getChildren().setAll(box);
            StackPane.setAlignment(box, Pos.CENTER);
            return;
        }

        // Main image
        updateMainImage();

        // Thumbnails
        thumbnailsBox.getChildren().clear();
        for (int i = 0; i < hotel.getImages().size(); i++) {
            final int index = i;
            ImageView thumb = new ImageView();
            thumb.setFitWidth(150);
            thumb.setFitHeight(100);
            thumb.setPreserveRatio(false);

            try {
                Image image = new Image(hotel.getImages().get(i), true);
                thumb.setImage(image);
            } catch (Exception e) {
                // Use placeholder
            }

            Rectangle clip = new Rectangle(150, 100);
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            thumb.setClip(clip);

            StackPane thumbPane = new StackPane(thumb);
            thumbPane.getStyleClass().add("gallery-thumb");
            if (i == selectedImageIndex) {
                thumbPane.getStyleClass().add("selected");
            }

            thumbPane.setOnMouseClicked(e -> {
                selectedImageIndex = index;
                updateMainImage();
                updateThumbnailSelection();
            });

            thumbnailsBox.getChildren().add(thumbPane);
        }
    }

    private void updateMainImage() {
        if (hotel.getImages().isEmpty())
            return;

        ImageView mainImage = new ImageView();
        mainImage.setPreserveRatio(true);
        mainImage.fitWidthProperty().bind(mainImagePane.widthProperty());
        mainImage.fitHeightProperty().bind(mainImagePane.heightProperty());

        try {
            Image image = new Image(hotel.getImages().get(selectedImageIndex), true);
            mainImage.setImage(image);
        } catch (Exception e) {
            // Use placeholder
        }

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(mainImagePane.widthProperty());
        clip.heightProperty().bind(mainImagePane.heightProperty());
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        mainImage.setClip(clip);

        mainImagePane.getChildren().setAll(mainImage);
    }

    private void updateThumbnailSelection() {
        for (int i = 0; i < thumbnailsBox.getChildren().size(); i++) {
            StackPane thumb = (StackPane) thumbnailsBox.getChildren().get(i);
            if (i == selectedImageIndex) {
                thumb.getStyleClass().add("selected");
            } else {
                thumb.getStyleClass().remove("selected");
            }
        }
    }

    private void loadRooms() {
        roomsContainer.getChildren().clear();

        List<RoomType> roomTypes = hotel.getRoomTypes();
        if (roomTypes == null || roomTypes.isEmpty()) {
            VBox emptyBox = new VBox(16);
            emptyBox.setAlignment(Pos.CENTER_LEFT);
            Label msg = new Label(isAmadeusHotel
                    ? "Room rates from Amadeus will appear here when you set check-in and check-out dates on the Hotels page, then open this hotel again."
                    : "No rooms available.");
            msg.setWrapText(true);
            msg.setMaxWidth(450);
            msg.setStyle("-fx-font-size: 14; -fx-text-fill: #475569;");
            // Add a placeholder room card for Amadeus so there's something to see
            if (isAmadeusHotel) {
                HBox card = new HBox(24);
                card.getStyleClass().add("room-card");
                card.setPadding(new Insets(24));
                card.setAlignment(Pos.CENTER_LEFT);
                VBox info = new VBox(8);
                Label name = new Label("Standard Room");
                name.getStyleClass().add("room-name");
                Label roomDesc = new Label("Contact the hotel for availability and rates. Use the phone or email above if available.");
                roomDesc.setWrapText(true);
                roomDesc.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
                info.getChildren().addAll(name, roomDesc);
                HBox.setHgrow(info, Priority.ALWAYS);
                Label priceHint = new Label("Contact for price");
                priceHint.getStyleClass().add("room-price");
                card.getChildren().addAll(info, priceHint);
                emptyBox.getChildren().addAll(msg, card);
            } else {
                emptyBox.getChildren().add(msg);
            }
            roomsContainer.getChildren().add(emptyBox);
            return;
        }

        for (RoomType room : roomTypes) {
            HBox roomCard = new HBox(24);
            roomCard.getStyleClass().add("room-card");
            roomCard.setPadding(new Insets(24));

            // Room image
            ImageView roomImage = new ImageView();
            roomImage.setFitWidth(200);
            roomImage.setFitHeight(150);
            roomImage.setPreserveRatio(false);

            if (!room.getImages().isEmpty()) {
                try {
                    Image image = new Image(room.getImages().get(0), true);
                    roomImage.setImage(image);
                } catch (Exception e) {
                    // Use placeholder
                }
            }

            Rectangle clip = new Rectangle(200, 150);
            clip.setArcWidth(16);
            clip.setArcHeight(16);
            roomImage.setClip(clip);

            // Room info
            VBox info = new VBox(8);
            HBox.setHgrow(info, Priority.ALWAYS);

            HBox header = new HBox(12);
            header.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(room.getName());
            nameLabel.getStyleClass().add("room-name");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label statusLabel = new Label(room.isAvailable() ? "Available" : "Booked");
            statusLabel.getStyleClass().add(room.isAvailable() ? "tag-success" : "tag-error");

            header.getChildren().addAll(nameLabel, spacer, statusLabel);

            Label descLabel = new Label(room.getDescription());
            descLabel.getStyleClass().add("text-muted");
            descLabel.setWrapText(true);

            HBox guestsBox = new HBox(6);
            guestsBox.setAlignment(Pos.CENTER_LEFT);
            Label guestsIcon = new Label("👥");
            guestsIcon.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
            Label guestsLabel = new Label("Max " + room.getMaxGuests() + " guests");
            guestsLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
            guestsBox.getChildren().addAll(guestsIcon, guestsLabel);

            // Amenities
            HBox amenities = new HBox(8);
            for (String amenity : room.getAmenities()) {
                Label tag = new Label(amenity);
                tag.getStyleClass().add("tag");
                amenities.getChildren().add(tag);
            }

            info.getChildren().addAll(header, descLabel, guestsBox, amenities);

            // Price and button
            VBox action = new VBox(12);
            action.setAlignment(Pos.CENTER_RIGHT);

            Label priceLabel = new Label(Formatter.formatCurrencyNoCents(room.getPricePerNight()));
            priceLabel.getStyleClass().add("room-price");

            Label unitLabel = new Label("/night");
            unitLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");

            Button selectBtn = new Button(isAmadeusHotel ? "Info only" : "Select Room");
            selectBtn.getStyleClass().add("primary-button");
            selectBtn.setDisable(isAmadeusHotel || !room.isAvailable());
            if (!isAmadeusHotel) {
                selectBtn.setOnAction(e -> selectRoom(room));
            }

            action.getChildren().addAll(priceLabel, unitLabel, selectBtn);

            roomCard.getChildren().addAll(roomImage, info, action);
            roomsContainer.getChildren().add(roomCard);
        }
    }

    private void loadAmenities() {
        amenitiesContainer.getChildren().clear();

        List<String> amenities = hotel.getAmenities();
        if (amenities == null || amenities.isEmpty()) {
            if (isAmadeusHotel) {
                for (String a : new String[]{"Free WiFi", "Parking", "24h Reception", "Air Conditioning"}) {
                    HBox item = new HBox(12);
                    item.getStyleClass().add("amenity-item");
                    item.setAlignment(Pos.CENTER_LEFT);
                    item.setPadding(new Insets(12));
                    Label icon = new Label("✓");
                    icon.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 14;");
                    Label text = new Label(a);
                    text.getStyleClass().add("amenity-text");
                    item.getChildren().addAll(icon, text);
                    amenitiesContainer.getChildren().add(item);
                }
                Label note = new Label("(Typical amenities – contact hotel for full list)");
                note.setStyle("-fx-font-size: 12; -fx-text-fill: #94A3B8;");
                amenitiesContainer.getChildren().add(note);
            } else {
                Label msg = new Label("No amenities listed.");
                msg.getStyleClass().add("text-muted");
                amenitiesContainer.getChildren().add(msg);
            }
            return;
        }
        for (String amenity : amenities) {
            HBox item = new HBox(12);
            item.getStyleClass().add("amenity-item");
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(12));

            StackPane iconBg = new StackPane();
            iconBg.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
            iconBg.setPrefSize(32, 32);

            Label icon = new Label(getAmenityIcon(amenity));
            icon.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 14;");
            iconBg.getChildren().add(icon);

            Label text = new Label(amenity);
            text.getStyleClass().add("amenity-text");

            item.getChildren().addAll(iconBg, text);
            amenitiesContainer.getChildren().add(item);
        }
    }

    private void loadPolicies() {
        policiesContainer.getChildren().clear();

        if (hotel.getPolicies().isEmpty()) {
            if (isAmadeusHotel) {
                for (String p : new String[]{"Check-in and check-out times vary – contact hotel", "Cancellation policy – contact hotel for details", "Pets – contact hotel for policy"}) {
                    HBox item = new HBox(12);
                    item.setAlignment(Pos.CENTER_LEFT);
                    item.setPadding(new Insets(16));
                    item.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12;");
                    Label icon = new Label("✓");
                    icon.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 14;");
                    Label text = new Label(p);
                    text.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 14;");
                    item.getChildren().addAll(icon, text);
                    policiesContainer.getChildren().add(item);
                }
            } else {
                Label msg = new Label("No policies listed.");
                msg.getStyleClass().add("text-muted");
                policiesContainer.getChildren().add(msg);
            }
            return;
        }
        for (String policy : hotel.getPolicies()) {
            HBox item = new HBox(12);
            item.getStyleClass().add("policy-item");
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(16));
            item.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12;");

            StackPane iconBg = new StackPane();
            iconBg.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
            iconBg.setPrefSize(32, 32);

            Label icon = new Label("✓");
            icon.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 14;");
            iconBg.getChildren().add(icon);

            Label text = new Label(policy);
            text.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 14;");

            item.getChildren().addAll(iconBg, text);
            policiesContainer.getChildren().add(item);
        }
    }

    private void selectRoom(RoomType room) {
        sessionManager.setSelectedRoom(room);
        HelloApplication.showView(SessionManager.View.BOOKING);
    }

    private String getStarString(double rating) {
        int fullStars = (int) rating;
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < fullStars) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    private String getAmenityIcon(String amenity) {
        String lower = amenity.toLowerCase();
        if (lower.contains("wifi"))
            return "📶";
        if (lower.contains("pool"))
            return "🏊";
        if (lower.contains("spa"))
            return "🧖";
        if (lower.contains("fitness") || lower.contains("gym"))
            return "💪";
        if (lower.contains("restaurant"))
            return "🍽";
        if (lower.contains("bar"))
            return "🍸";
        if (lower.contains("parking"))
            return "🅿";
        if (lower.contains("concierge"))
            return "🛎";
        if (lower.contains("beach"))
            return "🏖";
        return "✓";
    }

    @FXML
    private void goBack() {
        HelloApplication.showView(SessionManager.View.HOTELS);
    }
}

