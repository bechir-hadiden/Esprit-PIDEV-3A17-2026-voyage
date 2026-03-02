package org.example.services;

import org.example.entities.User;
import org.example.utils.DatabaseConnection;

import java.sql.*;

/** Service for user authentication and management. */
public class UserService {

    public void ensureSchemaConsistency() {
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement()) {
            st.executeUpdate("ALTER TABLE user ADD COLUMN IF NOT EXISTS idProfession INT DEFAULT 0");
            st.executeUpdate("ALTER TABLE user ADD COLUMN IF NOT EXISTS telephone VARCHAR(20) DEFAULT ''");
            st.executeUpdate("ALTER TABLE user ADD COLUMN IF NOT EXISTS email VARCHAR(100) DEFAULT ''");
        } catch (Exception e) {
            // Ignore error if column already exists
        }
    }

    public User authenticate(String username, String password) {
        ensureSchemaConsistency();
        String sql = "SELECT * FROM user WHERE username=? AND password=?";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null)
                return null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return new User(
                            rs.getInt("idUser"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getInt("idProfession"),
                            rs.getString("telephone"),
                            rs.getString("email"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createDefaultUsers() {
        ensureSchemaConsistency();
        String sql = "INSERT IGNORE INTO user (username, password, role, idProfession, telephone, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null)
                return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                // Create user
                ps.setString(1, "user");
                ps.setString(2, "user123");
                ps.setString(3, "USER");
                ps.setInt(4, 0);
                ps.setString(5, "+216 12 345 678");
                ps.setString(6, "user@example.com");
                ps.executeUpdate();

                // Create admin
                ps.setString(1, "admin");
                ps.setString(2, "admin123");
                ps.setString(3, "ADMIN");
                ps.setInt(4, 0);
                ps.setString(5, "+216 98 765 432");
                ps.setString(6, "admin@example.com");
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateProfession(int idUser, int idProfession) {
        String sql = "UPDATE user SET idProfession=? WHERE idUser=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProfession);
            ps.setInt(2, idUser);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePhone(int idUser, String telephone) {
        String sql = "UPDATE user SET telephone=? WHERE idUser=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, telephone);
            ps.setInt(2, idUser);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateEmail(int idUser, String email) {
        String sql = "UPDATE user SET email=? WHERE idUser=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, idUser);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
