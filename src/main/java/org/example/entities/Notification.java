package org.example.entities;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private int reservationId;
    private String message;
    private LocalDateTime dateSent;
    private boolean isRead;
    private String type;

    public Notification() {
    }

    public Notification(int userId, int reservationId, String message, String type) {
        this.userId = userId;
        this.reservationId = reservationId;
        this.message = message;
        this.type = type;
        this.dateSent = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateSent() {
        return dateSent;
    }

    public void setDateSent(LocalDateTime dateSent) {
        this.dateSent = dateSent;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
