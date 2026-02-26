package com.example.demo1.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {
    public enum Status {
        PENDING, CONFIRMED, CANCELLED
    }

    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty hotelId = new SimpleStringProperty();
    private final StringProperty hotelName = new SimpleStringProperty();
    private final StringProperty roomType = new SimpleStringProperty();
    private final IntegerProperty roomTypeId = new SimpleIntegerProperty(0); // DB room_types.id, 0 = not set
    private final ObjectProperty<LocalDate> checkIn = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> checkOut = new SimpleObjectProperty<>();
    private final IntegerProperty guestCount = new SimpleIntegerProperty(1);
    private final DoubleProperty totalPrice = new SimpleDoubleProperty();
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.CONFIRMED);
    private final ObjectProperty<LocalDate> createdAt = new SimpleObjectProperty<>(LocalDate.now());

    // Guest details
    private final StringProperty guestFirstName = new SimpleStringProperty();
    private final StringProperty guestLastName = new SimpleStringProperty();
    private final StringProperty guestEmail = new SimpleStringProperty();
    private final StringProperty guestPhone = new SimpleStringProperty();
    private final StringProperty specialRequests = new SimpleStringProperty();

    public Booking() {}

    public Booking(String id, String hotelId, String hotelName, String roomType,
                   LocalDate checkIn, LocalDate checkOut, int guestCount, double totalPrice) {
        this.id.set(id);
        this.hotelId.set(hotelId);
        this.hotelName.set(hotelName);
        this.roomType.set(roomType);
        this.checkIn.set(checkIn);
        this.checkOut.set(checkOut);
        this.guestCount.set(guestCount);
        this.totalPrice.set(totalPrice);
    }

    // Getters and Setters
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    public String getHotelId() { return hotelId.get(); }
    public void setHotelId(String value) { hotelId.set(value); }
    public StringProperty hotelIdProperty() { return hotelId; }

    public String getHotelName() { return hotelName.get(); }
    public void setHotelName(String value) { hotelName.set(value); }
    public StringProperty hotelNameProperty() { return hotelName; }

    public String getRoomType() { return roomType.get(); }
    public void setRoomType(String value) { roomType.set(value); }
    public StringProperty roomTypeProperty() { return roomType; }

    public int getRoomTypeId() { return roomTypeId.get(); }
    public void setRoomTypeId(int value) { roomTypeId.set(value); }
    public IntegerProperty roomTypeIdProperty() { return roomTypeId; }

    public LocalDate getCheckIn() { return checkIn.get(); }
    public void setCheckIn(LocalDate value) { checkIn.set(value); }
    public ObjectProperty<LocalDate> checkInProperty() { return checkIn; }

    public LocalDate getCheckOut() { return checkOut.get(); }
    public void setCheckOut(LocalDate value) { checkOut.set(value); }
    public ObjectProperty<LocalDate> checkOutProperty() { return checkOut; }

    public int getGuestCount() { return guestCount.get(); }
    public void setGuestCount(int value) { guestCount.set(value); }
    public IntegerProperty guestCountProperty() { return guestCount; }

    public double getTotalPrice() { return totalPrice.get(); }
    public void setTotalPrice(double value) { totalPrice.set(value); }
    public DoubleProperty totalPriceProperty() { return totalPrice; }

    public Status getStatus() { return status.get(); }
    public void setStatus(Status value) { status.set(value); }
    public ObjectProperty<Status> statusProperty() { return status; }

    public LocalDate getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDate value) { createdAt.set(value); }
    public ObjectProperty<LocalDate> createdAtProperty() { return createdAt; }

    public String getGuestFirstName() { return guestFirstName.get(); }
    public void setGuestFirstName(String value) { guestFirstName.set(value); }
    public StringProperty guestFirstNameProperty() { return guestFirstName; }

    public String getGuestLastName() { return guestLastName.get(); }
    public void setGuestLastName(String value) { guestLastName.set(value); }
    public StringProperty guestLastNameProperty() { return guestLastName; }

    public String getGuestEmail() { return guestEmail.get(); }
    public void setGuestEmail(String value) { guestEmail.set(value); }
    public StringProperty guestEmailProperty() { return guestEmail; }

    public String getGuestPhone() { return guestPhone.get(); }
    public void setGuestPhone(String value) { guestPhone.set(value); }
    public StringProperty guestPhoneProperty() { return guestPhone; }

    public String getSpecialRequests() { return specialRequests.get(); }
    public void setSpecialRequests(String value) { specialRequests.set(value); }
    public StringProperty specialRequestsProperty() { return specialRequests; }

    public long getNumberOfNights() {
        if (getCheckIn() == null || getCheckOut() == null) return 0;
        return ChronoUnit.DAYS.between(getCheckIn(), getCheckOut());
    }

    public String getFormattedTotalPrice() {
        return String.format("$%.2f", getTotalPrice());
    }

    public boolean isActive() {
        return getStatus() == Status.CONFIRMED && getCheckOut().isAfter(LocalDate.now());
    }
}

