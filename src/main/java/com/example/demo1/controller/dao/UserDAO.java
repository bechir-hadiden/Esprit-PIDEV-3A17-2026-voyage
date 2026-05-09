package com.example.demo1.controller.dao;
import com.example.demo1.entity.User;
import com.example.demo1.Utils.Database;
import com.example.demo1.Utils.BCryptWrapper;

import java.sql.*;

/**
 * Data Access Object for User entity.
 * Handles all database operations related to users.
 */
public class UserDAO {

    private final Database dbConnection;

    public UserDAO() {
        this.dbConnection = Database.getInstance();
    }

    /**
     * Authenticate user with username and password.
     * For ADMIN users only: accepts both BCrypt hashed password AND plain password.
     * For CLIENT users: only accepts BCrypt hashed password.
     *
     * @param username User's username
     * @param password User's plain text password
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String passwordHash = rs.getString("password_hash");
                String role = rs.getString("role");

                // Special case for ADMIN: also accept plain password (check first to avoid
                // BCrypt exception)
                if ("ADMIN".equals(role) && password.equals(passwordHash)) {
                    return extractUserFromResultSet(rs);
                }

                // Verify password using BCrypt (works for properly hashed passwords)
                // Normalize $2y$ / $2b$ prefixes to $2a$ because jbcrypt 0.4 only supports $2a$
                String normalizedHash = passwordHash.replaceFirst("^\\$2[by]\\$", "\\$2a\\$");
                try {
                    if (BCryptWrapper.checkpw(password, normalizedHash)) {
                        return extractUserFromResultSet(rs);
                    }
                } catch (IllegalArgumentException e) {
                    // Password hash is not in BCrypt format, already checked plain text above
                    System.out.println("Invalid BCrypt hash format for user: " + username);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get user by ID.
     *
     * @param userId User ID
     * @return User object or null if not found
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get user by email.
     *
     * @param email User email
     * @return User object or null if not found
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get user by username.
     *
     * @param username Username
     * @return User object or null if not found
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by username: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Create a new user.
     *
     * @param user          User object with data
     * @param plainPassword Plain text password to be hashed
     * @return true if user created successfully, false otherwise
     */
    public boolean createUser(User user, String plainPassword) {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, avatar, role, wallet_balance, loyalty_points) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Hash the password
            String passwordHash = BCryptWrapper.hashpw(plainPassword, BCryptWrapper.gensalt());

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getAvatar());
            pstmt.setString(7, user.getRole());
            pstmt.setDouble(8, user.getWalletBalance());
            pstmt.setInt(9, user.getLoyaltyPoints());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Get generated ID
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(String.valueOf(generatedKeys.getInt(1)));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update existing user.
     *
     * @param user User object with updated data
     * @return true if update successful, false otherwise
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, avatar = ?, wallet_balance = ?, loyalty_points = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getAvatar());
            pstmt.setDouble(5, user.getWalletBalance());
            pstmt.setInt(6, user.getLoyaltyPoints());
            pstmt.setInt(7, Integer.parseInt(user.getId()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update user password.
     *
     * @param userId      User ID
     * @param newPassword New plain text password
     * @return true if update successful, false otherwise
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String passwordHash = BCryptWrapper.hashpw(newPassword, BCryptWrapper.gensalt());
            pstmt.setString(1, passwordHash);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Delete user by ID.
     *
     * @param userId User ID
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Extract User object from ResultSet.
     *
     * @param rs ResultSet positioned at a user row
     * @return User object
     * @throws SQLException if error reading from ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(String.valueOf(rs.getInt("id")));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAvatar(rs.getString("avatar"));
        user.setRole(rs.getString("role"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setWalletBalance(rs.getDouble("wallet_balance"));
        user.setLoyaltyPoints(rs.getInt("loyalty_points"));
        return user;
    }
}
