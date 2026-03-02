package org.example.entities;

import org.example.PaiementApp.Role;

/** Entity class for User data, consolidated from SQL schema and duplicate entities. */
public class User {
    private int id;
    private String username;
    private String password;
    private String password_hash;
    private String full_name;
    private String email;
    private String phone;
    private String avatar;
    private String role_string; // Original role field as string
    private Role role; // Enum role for payment logic
    private int idProfession;
    private String telephone;
    private double walletBalance;
    private int loyaltyPoints;

    public User() {
    }

    // Constructor for Auth/Transport logic
    public User(int id, String username, String password, String role_string, int idProfession, String telephone, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role_string = role_string;
        this.idProfession = idProfession;
        this.telephone = telephone;
        this.email = email;
    }

    // Constructor for Payment logic
    public User(int id, String name, String email, Role role, double walletBalance, int loyaltyPoints) {
        this.id = id;
        this.full_name = name;
        this.email = email;
        this.role = role;
        this.walletBalance = walletBalance;
        this.loyaltyPoints = loyaltyPoints;
        this.role_string = role != null ? role.name() : null;
    }

    // Constructor for UI Demo (PaiementApp)
    public User(String username, Role role) {
        this.username = username;
        this.role = role;
        this.role_string = role != null ? role.name() : null;
    }

    // All Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPassword_hash() { return password_hash; }
    public void setPassword_hash(String password_hash) { this.password_hash = password_hash; }

    public String getFull_name() { return full_name; }
    public void setFull_name(String full_name) { this.full_name = full_name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getRole_string() { return role_string; }
    public void setRole_string(String role_string) { 
        this.role_string = role_string;
        try {
            this.role = Role.valueOf(role_string);
        } catch (Exception e) {
            // Role enum might not match all string roles
        }
    }

    public Role getRole() { return role; }
    public void setRole(Role role) { 
        this.role = role;
        this.role_string = role != null ? role.name() : null;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role_string) || (role != null && role == Role.ADMIN);
    }

    public int getIdProfession() { return idProfession; }
    public void setIdProfession(int idProfession) { this.idProfession = idProfession; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    // Compatibility getters/setters
    public int getIdUser() { return id; }
    public void setIdUser(int id) { this.id = id; }
    public String getName() { return full_name != null ? full_name : username; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role_string +
                ", walletBalance=" + walletBalance +
                '}';
    }
}
