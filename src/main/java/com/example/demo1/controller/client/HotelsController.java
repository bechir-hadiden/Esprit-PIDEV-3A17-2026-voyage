package com.example.demo1.controller.client;
import com.example.demo1.HelloApplication;
import com.example.demo1.controller.dao.HotelDAO;
import com.example.demo1.entity.AmadeusHotelInfo;
import com.example.demo1.entity.Hotel;
import com.example.demo1.entity.SearchFilters;
import com.example.demo1.services.AmadeusHotelService;
import com.example.demo1.services.SessionManager;
import com.example.demo1.Utils.Formatter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class HotelsController {

    @FXML
    private TextField searchField;
    @FXML
    private DatePicker checkInPicker;
    @FXML
    private DatePicker checkOutPicker;
    @FXML
    private Button guestMinusBtn;
    @FXML
    private Button guestPlusBtn;
    @FXML
    private Label guestCountLabel;
    @FXML
    private VBox advancedFiltersBox;
    @FXML
    private ComboBox<String> roomTypeCombo;
    @FXML
    private ComboBox<String> guestTypeCombo;
    @FXML
    private Slider priceSlider;
    @FXML
    private ComboBox<String> ratingCombo;
    @FXML
    private Button searchButton;
    @FXML
    private HBox activeFiltersBox;
    @FXML
    private Label resultsLabel;
    @FXML
    private FlowPane hotelGrid;
    @FXML
    private VBox noResultsBox;
    @FXML
    private Button filterButton;
    @FXML
    private Label filterBadge;
    @FXML
    private Label maxPriceLabel;
    @FXML
    private TextField amadeusCityField;
    @FXML
    private Button searchAmadeusButton;
    @FXML
    private Button showDbHotelsButton;

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final HotelDAO hotelDAO = new HotelDAO();
    private final AmadeusHotelService amadeusHotelService = new AmadeusHotelService();
    private ObservableList<Hotel> hotelList;
    private FilteredList<Hotel> filteredHotels;
    private SearchFilters filters;

    @FXML
    public void initialize() {
        // Initialize filters from session or create new
        filters = sessionManager.getSearchFilters();

        // Sync search field with session filter
        String query = filters.getQuery();
        if (searchField != null && query != null) {
            searchField.setText(query);
        }

        // Load hotels from database (observable so we can replace with Amadeus results)
        hotelList = FXCollections.observableArrayList(hotelDAO.getAllHotels());
        filteredHotels = new FilteredList<>(hotelList, p -> true);

        // Setup combo boxes
        roomTypeCombo.setItems(FXCollections.observableArrayList(
                "All Room Types", "Standard Room", "Deluxe Room", "Suite", "Villa", "Penthouse"));
        roomTypeCombo.getSelectionModel().selectFirst();

        guestTypeCombo.setItems(FXCollections.observableArrayList(
                "All Guests", "Adults (18+)", "Teens (13-17)", "Children (0-12)"));
        guestTypeCombo.getSelectionModel().selectFirst();

        ratingCombo.setItems(FXCollections.observableArrayList(
                "Any rating", "3+ Stars", "4+ Stars", "4.5+ Stars"));
        ratingCombo.getSelectionModel().selectFirst();

        // Setup date pickers
        checkInPicker.setValue(filters.getCheckIn());
        checkOutPicker.setValue(filters.getCheckOut());

        // Setup guest count
        guestCountLabel.setText(String.valueOf(filters.getGuestCount()));

        // Setup price slider and sync label
        priceSlider.setValue(filters.getMaxPrice());
        updateMaxPriceLabel(filters.getMaxPrice());

        // Add listeners for real-time filtering
        setupListeners();

        // Initial display
        updateHotelDisplay();
    }

    private void updateMaxPriceLabel(double maxPrice) {
        if (maxPriceLabel != null) {
            maxPriceLabel.setText(String.format("Max Price: %s", Formatter.formatCurrencyNoCents(maxPrice)));
        }
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, old, val) -> {
            filters.setQuery(val);
            applyFilters();
        });

        checkInPicker.valueProperty().addListener((obs, old, val) -> {
            filters.setCheckIn(val);
            applyFilters();
        });

        checkOutPicker.valueProperty().addListener((obs, old, val) -> {
            filters.setCheckOut(val);
            applyFilters();
        });

        priceSlider.valueProperty().addListener((obs, old, val) -> {
            double v = val.doubleValue();
            filters.setMaxPrice(v);
            updateMaxPriceLabel(v);
            applyFilters();
        });

        guestTypeCombo.valueProperty().addListener((obs, old, val) -> {
            if (val == null || val.equals("All Guests")) {
                filters.setGuestType(SearchFilters.GuestType.ALL);
            } else if (val.equals("Adults (18+)")) {
                filters.setGuestType(SearchFilters.GuestType.ADULT);
            } else if (val.equals("Teens (13-17)")) {
                filters.setGuestType(SearchFilters.GuestType.TEEN);
            } else if (val.equals("Children (0-12)")) {
                filters.setGuestType(SearchFilters.GuestType.CHILD);
            }
            applyFilters();
        });

        roomTypeCombo.valueProperty().addListener((obs, old, val) -> {
            filters.setRoomType(
                    val != null && !val.equals("All Room Types") ? val.toLowerCase().replace(" room", "") : "all");
            applyFilters();
        });

        ratingCombo.valueProperty().addListener((obs, old, val) -> {
            if (val == null || val.equals("Any rating")) {
                filters.setMinRating(0);
            } else if (val.equals("3+ Stars")) {
                filters.setMinRating(3);
            } else if (val.equals("4+ Stars")) {
                filters.setMinRating(4);
            } else if (val.equals("4.5+ Stars")) {
                filters.setMinRating(4.5);
            }
            applyFilters();
        });
    }

    private void applyFilters() {
        filteredHotels.setPredicate(hotel -> {
            // Search query (name, city, country, location)
            String q = filters.getQuery();
            if (q != null && !q.trim().isEmpty()) {
                if (!hotel.matchesSearch(q.trim())) {
                    return false;
                }
            }

            // Max price filter (hotel's min price per night must be within budget)
            if (hotel.getPricePerNight() > filters.getMaxPrice()) {
                return false;
            }

            // Min rating filter
            if (hotel.getRating() < filters.getMinRating()) {
                return false;
            }

            // Room type filter: hotel must have at least one room matching (skip if hotel has no room data, e.g. Amadeus)
            if (!hotel.getRoomTypes().isEmpty() && !filters.getRoomType().equals("all")) {
                String filterType = filters.getRoomType().toLowerCase();
                boolean hasRoomType = hotel.getRoomTypes().stream().anyMatch(room -> {
                    String roomName = room.getName().toLowerCase();
                    return roomName.contains(filterType);
                });
                if (!hasRoomType) {
                    return false;
                }
            }

            // Guest count filter: hotel must have at least one room that fits (skip if no room data, e.g. Amadeus)
            if (!hotel.getRoomTypes().isEmpty()) {
                int guests = filters.getGuestCount();
                if (guests > 0) {
                    boolean hasRoomForGuests = hotel.getRoomTypes().stream()
                            .anyMatch(room -> room.getMaxGuests() >= guests);
                    if (!hasRoomForGuests) {
                        return false;
                    }
                }
            }

            return true;
        });

        updateHotelDisplay();
        updateFilterBadge();
    }

    private void updateHotelDisplay() {
        hotelGrid.getChildren().clear();

        int total = filteredHotels.getSource().size();
        int showing = filteredHotels.size();
        resultsLabel.setText(String.format("Showing %d of %d properties", showing, total));

        if (showing == 0) {
            hotelGrid.setVisible(false);
            hotelGrid.setManaged(false);
            noResultsBox.setVisible(true);
            noResultsBox.setManaged(true);
        } else {
            hotelGrid.setVisible(true);
            hotelGrid.setManaged(true);
            noResultsBox.setVisible(false);
            noResultsBox.setManaged(false);

            for (Hotel hotel : filteredHotels) {
                VBox card = createHotelCard(hotel);
                hotelGrid.getChildren().add(card);
            }
        }
    }

    private VBox createHotelCard(Hotel hotel) {
        VBox card = new VBox();
        card.getStyleClass().add("hotel-card");
        card.setPrefWidth(320);
        card.setOnMouseClicked(e -> {
            sessionManager.setSelectedHotel(hotel);
            HelloApplication.showView(SessionManager.View.HOTEL_DETAILS);
        });

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(320);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(false);

        if (!hotel.getImages().isEmpty()) {
            try {
                Image image = new Image(hotel.getImages().get(0), true);
                imageView.setImage(image);
            } catch (Exception e) {
                // Use placeholder
            }
        }

        Rectangle clip = new Rectangle(320, 200);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageView.setClip(clip);

        StackPane imagePane = new StackPane(imageView);

        // Rating badge
        HBox ratingBadge = new HBox(4);
        ratingBadge.getStyleClass().add("rating-badge");
        ratingBadge.setAlignment(Pos.CENTER);
        ratingBadge.setPadding(new Insets(6, 12, 6, 12));

        Label starsLabel = new Label(getStarString(hotel.getRating()));
        starsLabel.getStyleClass().add("rating-stars");

        Label ratingValue = new Label(Formatter.formatRating(hotel.getRating()));
        ratingValue.getStyleClass().add("rating-value");

        ratingBadge.getChildren().addAll(starsLabel, ratingValue);
        StackPane.setAlignment(ratingBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(ratingBadge, new Insets(12));

        // Price badge
        Label priceBadge = new Label(Formatter.formatCurrencyNoCents(hotel.getPricePerNight()) + "/night");
        priceBadge.getStyleClass().add("price-badge");
        StackPane.setAlignment(priceBadge, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(priceBadge, new Insets(12));

        imagePane.getChildren().addAll(ratingBadge, priceBadge);

        // Content
        VBox content = new VBox(10);
        content.setPadding(new Insets(16));

        Label nameLabel = new Label(hotel.getName());
        nameLabel.getStyleClass().add("hotel-name");
        nameLabel.setWrapText(true);

        HBox locationBox = new HBox(6);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        locationIcon.setStyle("-fx-text-fill: #64748B;");
        Label locationLabel = new Label(hotel.getLocation() + ", " + hotel.getCity());
        locationLabel.getStyleClass().add("hotel-location");
        locationBox.getChildren().addAll(locationIcon, locationLabel);

        HBox metaBox = new HBox(16);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        HBox reviewsBox = new HBox(4);
        reviewsBox.setAlignment(Pos.CENTER_LEFT);
        Label reviewsIcon = new Label("👥");
        reviewsIcon.setStyle("-fx-text-fill: #64748B;");
        Label reviewsLabel = new Label(hotel.getReviewCount() + " reviews");
        reviewsLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        reviewsBox.getChildren().addAll(reviewsIcon, reviewsLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label viewLabel = new Label("View Details ➝");
        viewLabel.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 13; -fx-font-weight: 500;");

        metaBox.getChildren().addAll(reviewsBox, spacer, viewLabel);

        // Amenities
        HBox amenitiesBox = new HBox(8);
        amenitiesBox.setPadding(new Insets(8, 0, 0, 0));
        int count = 0;
        for (String amenity : hotel.getAmenities()) {
            if (count++ >= 3)
                break;
            Label tag = new Label(amenity);
            tag.getStyleClass().add("tag");
            amenitiesBox.getChildren().add(tag);
        }
        if (hotel.getAmenities().size() > 3) {
            Label moreTag = new Label("+" + (hotel.getAmenities().size() - 3) + " more");
            moreTag.getStyleClass().add("tag");
            amenitiesBox.getChildren().add(moreTag);
        }

        content.getChildren().addAll(nameLabel, locationBox, metaBox, amenitiesBox);

        card.getChildren().addAll(imagePane, content);

        return card;
    }

    private String getStarString(double rating) {
        int fullStars = (int) rating;
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < fullStars) {
                stars.append("★"); // Full star
            } else if (i < rating) {
                stars.append("✫"); // Half star (if supported)
            } else {
                stars.append("☆"); // Empty star
            }
        }
        return stars.toString();
    }

    private void updateFilterBadge() {
        int count = filters.getActiveFilterCount();
        filterBadge.setText(String.valueOf(count));
        filterBadge.setVisible(count > 0);
    }

    @FXML
    private void toggleFilters() {
        boolean visible = advancedFiltersBox.isVisible();
        advancedFiltersBox.setVisible(!visible);
        advancedFiltersBox.setManaged(!visible);
    }

    @FXML
    private void decreaseGuests() {
        int count = Integer.parseInt(guestCountLabel.getText());
        if (count > 1) {
            count--;
            guestCountLabel.setText(String.valueOf(count));
            filters.setGuestCount(count);
        }
    }

    @FXML
    private void increaseGuests() {
        int count = Integer.parseInt(guestCountLabel.getText());
        if (count < 10) {
            count++;
            guestCountLabel.setText(String.valueOf(count));
            filters.setGuestCount(count);
        }
    }

    @FXML
    private void searchHotels() {
        applyFilters();
    }

    @FXML
    private void clearFilters() {
        filters.clear();
        searchField.clear();
        checkInPicker.setValue(null);
        checkOutPicker.setValue(null);
        guestCountLabel.setText("1");
        filters.setGuestCount(1);
        priceSlider.setValue(5000);
        filters.setMaxPrice(5000);
        updateMaxPriceLabel(5000);
        roomTypeCombo.getSelectionModel().selectFirst();
        guestTypeCombo.getSelectionModel().selectFirst();
        ratingCombo.getSelectionModel().selectFirst();
        applyFilters();
    }

    @FXML
    private void searchAmadeus() {
        if (!amadeusHotelService.isConfigured()) {
            showAlert("Amadeus API not configured. Add your API key and secret in src/main/resources/amadeus.properties");
            return;
        }
        String city = amadeusCityField != null ? amadeusCityField.getText().trim() : "";
        if (city.isEmpty()) {
            showAlert("Enter an IATA city code (e.g. PAR, NYC, LON)");
            return;
        }
        if (searchAmadeusButton != null) searchAmadeusButton.setDisable(true);
        Task<List<Hotel>> task = new Task<>() {
            @Override
            protected List<Hotel> call() throws Exception {
                var list = amadeusHotelService.listHotelsByCity(city, null);
                return list.stream().map(HotelsController::amadeusHotelToHotel).toList();
            }
        };
        task.setOnSucceeded(e -> {
            if (searchAmadeusButton != null) searchAmadeusButton.setDisable(false);
            List<Hotel> results = task.getValue();
            Platform.runLater(() -> {
                hotelList.clear();
                if (results != null && !results.isEmpty()) {
                    hotelList.addAll(results);
                    // Clear main search filter so Amadeus results are not hidden by query
                    if (searchField != null) searchField.clear();
                    filters.setQuery("");
                    applyFilters();
                } else {
                    applyFilters();
                    showAlert("No hotels found for this city code. Try PAR, LON, or NYC (test API has limited cities).");
                }
            });
        });
        task.setOnFailed(e -> {
            if (searchAmadeusButton != null) searchAmadeusButton.setDisable(false);
            Throwable ex = task.getException();
            Throwable cause = ex != null ? ex.getCause() : null;
            if (cause == null) cause = ex;
            String msg = cause != null ? cause.getMessage() : "Unknown error";
            if (cause instanceof com.example.demo1.services.AmadeusApiException amadeusEx) {
                if (amadeusEx.getStatusCode() == 400 && msg != null && msg.toLowerCase().contains("city code")) {
                    msg = "Invalid city code. Use a 3-letter IATA code (e.g. PAR, LON, NYC). The test API may only support certain cities.";
                } else {
                    msg = amadeusEx.getMessage();
                }
            } else if (cause instanceof IllegalArgumentException) {
                msg = cause.getMessage();
            }
            showAlert("Amadeus search failed: " + msg);
        });
        new Thread(task).start();
    }

    @FXML
    private void showDatabaseHotels() {
        hotelList.clear();
        hotelList.addAll(hotelDAO.getAllHotels());
        if (amadeusCityField != null) amadeusCityField.clear();
        applyFilters();
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Hotel Search");
            a.setHeaderText(null);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    private static Hotel amadeusHotelToHotel(AmadeusHotelInfo a) {
        Hotel h = new Hotel();
        h.setId("AM-" + (a.getHotelId() != null ? a.getHotelId() : ""));
        h.setName(a.getName() != null ? a.getName() : "Hotel");
        h.setLocation(a.getAddress() != null ? a.getAddress() : "");
        h.setCity(a.getCityName() != null ? a.getCityName() : a.getIataCode() != null ? a.getIataCode() : "");
        h.setCountry(a.getCountryCode() != null ? a.getCountryCode() : "");
        h.setRating(a.getRating() != null ? a.getRating() : 0);
        h.setReviewCount(0);
        h.setPricePerNight(0); // Amadeus list doesn't include price; use 0 so filter doesn't exclude
        h.setPricePerWeek(0);
        return h;
    }
}

