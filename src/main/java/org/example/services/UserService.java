package org.example.services;

import org.example.entities.User;
import org.example.utils.DatabaseConnection;
import org.example.PaiementApp.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Consolidated service for user management, authentication, and wallet operations. */
public class UserService {

    private Connection conn;

    public UserService() {
        try {
            this.conn = DatabaseConnection.getConnection();
        } catch (Exception e) {
            System.err.println("Database error in UserService: " + e.getMessage());
        }
    }

    public void ensureSchemaConsistency() {
        // Schema is now handled by the SQL dump provided by user, 
        // but we keep this for minor runtime additions if needed.
    }

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }
        return null;
    }

    public User getUserByName(String name) {
        String sql = "SELECT * FROM users WHERE username = ? OR full_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by name: " + e.getMessage());
        }
        return null;
    }

    public void updateBalance(int userId, double amount) {
        String sql = "UPDATE users SET wallet_balance = wallet_balance + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating balance: " + e.getMessage());
        }
    }

    public void addLoyaltyPoints(int userId, int points) {
        String sql = "UPDATE users SET loyalty_points = loyalty_points + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating loyalty points: " + e.getMessage());
        }
    }

    public boolean transfer(int fromId, String toName, double amount) {
        User toUser = getUserByName(toName);
        if (toUser == null) return false;

        try {
            conn.setAutoCommit(false);
            User fromUser = getUserById(fromId);
            if (fromUser != null && fromUser.getWalletBalance() >= amount) {
                updateBalance(fromId, -amount);
                updateBalance(toUser.getId(), amount);
                addLoyaltyPoints(fromId, 10);
                conn.commit();
                return true;
            } else {
                conn.rollback();
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            System.err.println("Transfer error: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
        return false;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setPassword_hash(rs.getString("password_hash"));
        u.setFull_name(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setAvatar(rs.getString("avatar"));
        u.setRole_string(rs.getString("role"));
        u.setIdProfession(rs.getInt("idProfession"));
        u.setTelephone(rs.getString("telephone"));
        u.setWalletBalance(rs.getDouble("wallet_balance"));
        u.setLoyaltyPoints(rs.getInt("loyalty_points"));
        return u;
    }

    // Keep legacy support for profession/phone/email updates
    public void updateProfession(int idUser, int idProfession) {
        String sql = "UPDATE users SET idProfession=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProfession);
            ps.setInt(2, idUser);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void updatePhone(int idUser, String telephone) {
        String sql = "UPDATE users SET telephone=?, phone=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, telephone);
            ps.setString(2, telephone);
            ps.setInt(3, idUser);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void updateEmail(int idUser, String email) {
        String sql = "UPDATE users SET email=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, idUser);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void createDefaultUsers() {
        // Logic to ensure at least one admin and one user exist for demo/testing
        try {
            if (getUserByName("Admin") == null) {
                String sql = "INSERT INTO users (username, password, role, full_name, wallet_balance) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, "Admin");
                    ps.setString(2, "admin123");
                    ps.setString(3, "ADMIN");
                    ps.setString(4, "Administrator");
                    ps.setDouble(5, 1000.0);
                    ps.executeUpdate();
                }
            }
            if (getUserByName("UserTest") == null) {
                String sql = "INSERT INTO users (username, password, role, full_name, wallet_balance) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, "UserTest");
                    ps.setString(2, "user123");
                    ps.setString(3, "USER");
                    ps.setString(4, "Test User");
                    ps.setDouble(5, 100.0);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating default users: " + e.getMessage());
        }
    }
}
