package com.example.demo1.entity;

import javafx.beans.property.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class User {
    private StringProperty id = new SimpleStringProperty();
    private StringProperty username = new SimpleStringProperty();
    private StringProperty fullName = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();
    private StringProperty avatar = new SimpleStringProperty();
    private StringProperty phone = new SimpleStringProperty();
    private StringProperty passwordHash = new SimpleStringProperty();
    private StringProperty role = new SimpleStringProperty();
    private IntegerProperty idProfession = new SimpleIntegerProperty();
    private StringProperty telephone = new SimpleStringProperty();
    private DoubleProperty walletBalance = new SimpleDoubleProperty(0.0);
    private IntegerProperty loyaltyPoints = new SimpleIntegerProperty(0);

    public User() {
    }

    public User(String id, String fullName, String email, String avatar) {
        setId(id);
        setFullName(fullName);
        setEmail(email);
        setAvatar(avatar);
        setRole("CLIENT"); // Default role
    }

    // Getters and Setters
    public String getId() {
        return id.get();
    }

    public void setId(String value) {
        this.id.set(value);
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getFullName() {
        return fullName.get();
    }

    public void setFullName(String value) {
        this.fullName.set(value);
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String value) {
        this.email.set(value);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getAvatar() {
        return avatar.get();
    }

    public void setAvatar(String value) {
        this.avatar.set(value);
    }

    public StringProperty avatarProperty() {
        return avatar;
    }

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String value) {
        this.phone.set(value);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash.get();
    }

    public void setPasswordHash(String value) {
        this.passwordHash.set(value);
    }

    public StringProperty passwordHashProperty() {
        return passwordHash;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String value) {
        this.username.set(value);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public String getRole() {
        return role.get();
    }

    public void setRole(String value) {
        this.role.set(value);
    }

    public StringProperty roleProperty() {
        return role;
    }

    public int getIdProfession() {
        return idProfession.get();
    }

    public void setIdProfession(int value) {
        this.idProfession.set(value);
    }

    public IntegerProperty idProfessionProperty() {
        return idProfession;
    }

    public String getTelephone() {
        return telephone.get();
    }

    public void setTelephone(String value) {
        this.telephone.set(value);
    }

    public StringProperty telephoneProperty() {
        return telephone;
    }

    public double getWalletBalance() {
        return walletBalance.get();
    }

    public void setWalletBalance(double value) {
        this.walletBalance.set(value);
    }

    public DoubleProperty walletBalanceProperty() {
        return walletBalance;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints.get();
    }

    public void setLoyaltyPoints(int value) {
        this.loyaltyPoints.set(value);
    }

    public IntegerProperty loyaltyPointsProperty() {
        return loyaltyPoints;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    public String getInitials() {
        String name = getFullName();
        if (name == null || name.isEmpty())
            return "";
        String[] parts = name.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }

}
