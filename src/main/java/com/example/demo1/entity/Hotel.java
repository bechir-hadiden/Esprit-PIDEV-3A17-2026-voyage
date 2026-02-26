package com.example.demo1.entity;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Hotel {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty location = new SimpleStringProperty();
    private final StringProperty city = new SimpleStringProperty();
    private final StringProperty country = new SimpleStringProperty();
    private final DoubleProperty pricePerNight = new SimpleDoubleProperty();
    private final DoubleProperty pricePerWeek = new SimpleDoubleProperty();
    private final DoubleProperty rating = new SimpleDoubleProperty();
    private final IntegerProperty reviewCount = new SimpleIntegerProperty();
    private final ObservableList<String> images = FXCollections.observableArrayList();
    private final StringProperty description = new SimpleStringProperty();
    private final ObservableList<String> amenities = FXCollections.observableArrayList();
    private final ObservableList<RoomType> roomTypes = FXCollections.observableArrayList();
    private final StringProperty checkInTime = new SimpleStringProperty();
    private final StringProperty checkOutTime = new SimpleStringProperty();
    private final ObservableList<String> policies = FXCollections.observableArrayList();
    private final StringProperty contactEmail = new SimpleStringProperty();
    private final StringProperty contactPhone = new SimpleStringProperty();

    public Hotel() {}

    public Hotel(String id, String name, String location, String city, String country,
                 double pricePerNight, double rating, int reviewCount) {
        this.id.set(id);
        this.name.set(name);
        this.location.set(location);
        this.city.set(city);
        this.country.set(country);
        this.pricePerNight.set(pricePerNight);
        this.rating.set(rating);
        this.reviewCount.set(reviewCount);
    }

    // Getters and Setters
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public String getLocation() { return location.get(); }
    public void setLocation(String value) { location.set(value); }
    public StringProperty locationProperty() { return location; }

    public String getCity() { return city.get(); }
    public void setCity(String value) { city.set(value); }
    public StringProperty cityProperty() { return city; }

    public String getCountry() { return country.get(); }
    public void setCountry(String value) { country.set(value); }
    public StringProperty countryProperty() { return country; }

    public double getPricePerNight() { return pricePerNight.get(); }
    public void setPricePerNight(double value) { pricePerNight.set(value); }
    public DoubleProperty pricePerNightProperty() { return pricePerNight; }

    public double getPricePerWeek() { return pricePerWeek.get(); }
    public void setPricePerWeek(double value) { pricePerWeek.set(value); }
    public DoubleProperty pricePerWeekProperty() { return pricePerWeek; }

    public double getRating() { return rating.get(); }
    public void setRating(double value) { rating.set(value); }
    public DoubleProperty ratingProperty() { return rating; }

    public int getReviewCount() { return reviewCount.get(); }
    public void setReviewCount(int value) { reviewCount.set(value); }
    public IntegerProperty reviewCountProperty() { return reviewCount; }

    public ObservableList<String> getImages() { return images; }

    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    public ObservableList<String> getAmenities() { return amenities; }

    public ObservableList<RoomType> getRoomTypes() { return roomTypes; }

    public String getCheckInTime() { return checkInTime.get(); }
    public void setCheckInTime(String value) { checkInTime.set(value); }
    public StringProperty checkInTimeProperty() { return checkInTime; }

    public String getCheckOutTime() { return checkOutTime.get(); }
    public void setCheckOutTime(String value) { checkOutTime.set(value); }
    public StringProperty checkOutTimeProperty() { return checkOutTime; }

    public ObservableList<String> getPolicies() { return policies; }

    public String getContactEmail() { return contactEmail.get(); }
    public void setContactEmail(String value) { contactEmail.set(value); }
    public StringProperty contactEmailProperty() { return contactEmail; }

    public String getContactPhone() { return contactPhone.get(); }
    public void setContactPhone(String value) { contactPhone.set(value); }
    public StringProperty contactPhoneProperty() { return contactPhone; }

    public String getFormattedPrice() {
        return String.format("$%.0f", getPricePerNight());
    }

    public String getFormattedRating() {
        return String.format("%.1f", getRating());
    }

    public String getFullAddress() {
        return String.format("%s, %s, %s", getLocation(), getCity(), getCountry());
    }

    public boolean matchesSearch(String query) {
        if (query == null || query.isEmpty()) return true;
        String lowerQuery = query.toLowerCase();
        return getName().toLowerCase().contains(lowerQuery) ||
                getCity().toLowerCase().contains(lowerQuery) ||
                getCountry().toLowerCase().contains(lowerQuery) ||
                getLocation().toLowerCase().contains(lowerQuery);
    }
}
