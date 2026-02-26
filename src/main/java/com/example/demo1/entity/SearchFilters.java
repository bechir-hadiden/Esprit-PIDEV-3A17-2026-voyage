package com.example.demo1.entity;

import javafx.beans.property.*;
import java.time.LocalDate;

public class SearchFilters {
    public enum GuestType {
        ALL, ADULT, TEEN, CHILD
    }

    private final StringProperty query = new SimpleStringProperty("");
    private final StringProperty city = new SimpleStringProperty("");
    private final StringProperty country = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> checkIn = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> checkOut = new SimpleObjectProperty<>();
    private final StringProperty roomType = new SimpleStringProperty("all");
    private final ObjectProperty<GuestType> guestType = new SimpleObjectProperty<>(GuestType.ALL);
    private final IntegerProperty guestCount = new SimpleIntegerProperty(1);
    private final DoubleProperty minPrice = new SimpleDoubleProperty(0);
    private final DoubleProperty maxPrice = new SimpleDoubleProperty(5000);
    private final DoubleProperty minRating = new SimpleDoubleProperty(0);

    public SearchFilters() {}

    // Getters and Setters
    public String getQuery() { return query.get(); }
    public void setQuery(String value) { query.set(value); }
    public StringProperty queryProperty() { return query; }

    public String getCity() { return city.get(); }
    public void setCity(String value) { city.set(value); }
    public StringProperty cityProperty() { return city; }

    public String getCountry() { return country.get(); }
    public void setCountry(String value) { country.set(value); }
    public StringProperty countryProperty() { return country; }

    public LocalDate getCheckIn() { return checkIn.get(); }
    public void setCheckIn(LocalDate value) { checkIn.set(value); }
    public ObjectProperty<LocalDate> checkInProperty() { return checkIn; }

    public LocalDate getCheckOut() { return checkOut.get(); }
    public void setCheckOut(LocalDate value) { checkOut.set(value); }
    public ObjectProperty<LocalDate> checkOutProperty() { return checkOut; }

    public String getRoomType() { return roomType.get(); }
    public void setRoomType(String value) { roomType.set(value); }
    public StringProperty roomTypeProperty() { return roomType; }

    public GuestType getGuestType() { return guestType.get(); }
    public void setGuestType(GuestType value) { guestType.set(value); }
    public ObjectProperty<GuestType> guestTypeProperty() { return guestType; }

    public int getGuestCount() { return guestCount.get(); }
    public void setGuestCount(int value) { guestCount.set(value); }
    public IntegerProperty guestCountProperty() { return guestCount; }

    public double getMinPrice() { return minPrice.get(); }
    public void setMinPrice(double value) { minPrice.set(value); }
    public DoubleProperty minPriceProperty() { return minPrice; }

    public double getMaxPrice() { return maxPrice.get(); }
    public void setMaxPrice(double value) { maxPrice.set(value); }
    public DoubleProperty maxPriceProperty() { return maxPrice; }

    public double getMinRating() { return minRating.get(); }
    public void setMinRating(double value) { minRating.set(value); }
    public DoubleProperty minRatingProperty() { return minRating; }

    public void clear() {
        setQuery("");
        setCity("");
        setCountry("");
        setCheckIn(null);
        setCheckOut(null);
        setRoomType("all");
        setGuestType(GuestType.ALL);
        setGuestCount(1);
        setMinPrice(0);
        setMaxPrice(5000);
        setMinRating(0);
    }

    public boolean hasActiveFilters() {
        return !getQuery().isEmpty() ||
                !getCity().isEmpty() ||
                !getCountry().isEmpty() ||
                getCheckIn() != null ||
                getCheckOut() != null ||
                !"all".equals(getRoomType()) ||
                getGuestType() != GuestType.ALL ||
                getGuestCount() > 1 ||
                getMaxPrice() < 5000 ||
                getMinRating() > 0;
    }

    public int getActiveFilterCount() {
        int count = 0;
        if (!getQuery().isEmpty()) count++;
        if (!getCity().isEmpty()) count++;
        if (!getCountry().isEmpty()) count++;
        if (getCheckIn() != null) count++;
        if (getCheckOut() != null) count++;
        if (!"all".equals(getRoomType())) count++;
        if (getGuestType() != GuestType.ALL) count++;
        if (getGuestCount() > 1) count++;
        if (getMaxPrice() < 5000) count++;
        if (getMinRating() > 0) count++;
        return count;
    }
}
