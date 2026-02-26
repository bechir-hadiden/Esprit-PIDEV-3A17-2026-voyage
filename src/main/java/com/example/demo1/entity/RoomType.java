package com.example.demo1.entity;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RoomType {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty maxGuests = new SimpleIntegerProperty();
    private final DoubleProperty pricePerNight = new SimpleDoubleProperty();
    private final ObservableList<String> amenities = FXCollections.observableArrayList();
    private final ObservableList<String> images = FXCollections.observableArrayList();
    private final BooleanProperty available = new SimpleBooleanProperty(true);

    public RoomType() {}

    public RoomType(String id, String name, String description, int maxGuests,
                    double pricePerNight, boolean available) {
        this.id.set(id);
        this.name.set(name);
        this.description.set(description);
        this.maxGuests.set(maxGuests);
        this.pricePerNight.set(pricePerNight);
        this.available.set(available);
    }

    // Getters and Setters
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    public int getMaxGuests() { return maxGuests.get(); }
    public void setMaxGuests(int value) { maxGuests.set(value); }
    public IntegerProperty maxGuestsProperty() { return maxGuests; }

    public double getPricePerNight() { return pricePerNight.get(); }
    public void setPricePerNight(double value) { pricePerNight.set(value); }
    public DoubleProperty pricePerNightProperty() { return pricePerNight; }

    public ObservableList<String> getAmenities() { return amenities; }

    public ObservableList<String> getImages() { return images; }

    public boolean isAvailable() { return available.get(); }
    public void setAvailable(boolean value) { available.set(value); }
    public BooleanProperty availableProperty() { return available; }

    public String getFormattedPrice() {
        return String.format("$%.0f", getPricePerNight());
    }
}
