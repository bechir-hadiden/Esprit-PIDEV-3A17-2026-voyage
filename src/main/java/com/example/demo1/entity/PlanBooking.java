package com.example.demo1.entity;

import javafx.beans.property.*;
import java.time.LocalDate;

public class PlanBooking {
    public enum Status {
        ACTIVE, CANCELLED, EXPIRED
    }

    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty planId = new SimpleStringProperty();
    private final StringProperty planName = new SimpleStringProperty();
    private final StringProperty userEmail = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final StringProperty duration = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.ACTIVE);
    private final ObjectProperty<LocalDate> createdAt = new SimpleObjectProperty<>(LocalDate.now());

    public PlanBooking() {}

    public PlanBooking(String id, String planId, String planName, String userEmail,
                       double price, String duration, LocalDate startDate) {
        this.id.set(id);
        this.planId.set(planId);
        this.planName.set(planName);
        this.userEmail.set(userEmail);
        this.price.set(price);
        this.duration.set(duration);
        this.startDate.set(startDate);
    }

    // Getters and Setters
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    public String getPlanId() { return planId.get(); }
    public void setPlanId(String value) { planId.set(value); }
    public StringProperty planIdProperty() { return planId; }

    public String getPlanName() { return planName.get(); }
    public void setPlanName(String value) { planName.set(value); }
    public StringProperty planNameProperty() { return planName; }

    public String getUserEmail() { return userEmail.get(); }
    public void setUserEmail(String value) { userEmail.set(value); }
    public StringProperty userEmailProperty() { return userEmail; }

    public double getPrice() { return price.get(); }
    public void setPrice(double value) { price.set(value); }
    public DoubleProperty priceProperty() { return price; }

    public String getDuration() { return duration.get(); }
    public void setDuration(String value) { duration.set(value); }
    public StringProperty durationProperty() { return duration; }

    public LocalDate getStartDate() { return startDate.get(); }
    public void setStartDate(LocalDate value) { startDate.set(value); }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }

    public Status getStatus() { return status.get(); }
    public void setStatus(Status value) { status.set(value); }
    public ObjectProperty<Status> statusProperty() { return status; }

    public LocalDate getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDate value) { createdAt.set(value); }
    public ObjectProperty<LocalDate> createdAtProperty() { return createdAt; }

    public String getFormattedPrice() {
        return String.format("$%.2f", getPrice());
    }

    public boolean isActive() {
        return getStatus() == Status.ACTIVE;
    }
}
