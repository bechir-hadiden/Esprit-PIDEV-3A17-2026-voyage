package org.example.services;

import org.example.PaiementApp.Role;
import org.example.entities.User;
import org.example.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Service for user authentication and wallet/profile management. */
public class UserService {

    private static final String USERS_TABLE = "users";

    public void ensureSchemaConsistency() {
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement()) {
            st.executeUpdate("ALTER TABLE " + USERS_TABLE + " ADD COLUMN IF NOT EXISTS idProfession INT DEFAULT 0");
            st.executeUpdate("ALTER TABLE " + USERS_TABLE + " ADD COLUMN IF NOT EXISTS telephone VARCHAR(20) DEFAULT ''");
            st.executeUpdate("ALTER TABLE " + USERS_TABLE + " ADD COLUMN IF NOT EXISTS email VARCHAR(100) DEFAULT ''");
            st.executeUpdate("ALTER TABLE " + USERS_TABLE + " ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER'");
            st.executeUpdate("ALTER TABLE " + USERS_TABLE + " ADD COLUMN IF NOT EXISTS password VARCHAR(255) DEFAULT ''");
            st.executeUpdate("ALTER TABLE " + USERS_TABLE + " ADD COLUMN IF NOT EXISTS wallet_balance DECIMAL(10,2) DEFAULT 0.00");
            st.executeUpdate("ALTER TABLE " + USERS_TABLE + " ADD COLUMN IF NOT EXISTS loyalty_points INT DEFAULT 0");
        } catch (Exception e) {
            // Keep app running on older schemas.
        }
    }

    public User authenticate(String username, String password) {
        ensureSchemaConsistency();
        String sql = "SELECT * FROM " + USERS_TABLE + " WHERE LOWER(username)=LOWER(?) LIMIT 1";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                return null;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    String dbPassword = readString(rs, "password", "");
                    String dbPasswordHash = readString(rs, "password_hash", "");
                    if (password.equals(dbPassword) || password.equals(dbPasswordHash)) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByName(String name) {
        ensureSchemaConsistency();
        String sql = "SELECT * FROM " + USERS_TABLE + " WHERE LOWER(username)=LOWER(?) LIMIT 1";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                return null;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.List<User> getAllUsers() {
        ensureSchemaConsistency();
        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT * FROM " + USERS_TABLE;
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public User getUserById(int idUser) {
        ensureSchemaConsistency();
        String sql = "SELECT * FROM " + USERS_TABLE + " WHERE id=? LIMIT 1";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                return null;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUser);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createDefaultUsers() {
        ensureSchemaConsistency();
        String sql = "INSERT INTO " + USERS_TABLE
                + " (username, password, role, idProfession, telephone, email, wallet_balance, loyalty_points) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE username = username";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                return;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                // USER
                ps.setString(1, "user");
                ps.setString(2, "user123");
                ps.setString(3, "USER");
                ps.setInt(4, 0);
                ps.setString(5, "+216 12 345 678");
                ps.setString(6, "user@example.com");
                ps.setDouble(7, 100.0);
                ps.setInt(8, 0);
                ps.executeUpdate();

                // ADMIN
                ps.setString(1, "admin");
                ps.setString(2, "admin123");
                ps.setString(3, "ADMIN");
                ps.setInt(4, 0);
                ps.setString(5, "+216 98 765 432");
                ps.setString(6, "admin@example.com");
                ps.setDouble(7, 0.0);
                ps.setInt(8, 0);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Adds delta to wallet balance (negative for deduction). */
    public boolean updateBalance(int idUser, double delta) {
        ensureSchemaConsistency();
        String sql = "UPDATE " + USERS_TABLE + " SET wallet_balance = COALESCE(wallet_balance, 0) + ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                return false;
            }
            ps.setDouble(1, delta);
            ps.setInt(2, idUser);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Transfers amount from sender to recipient username. */
    public boolean transfer(int senderId, String recipientUsername, double amount) {
        if (amount <= 0 || recipientUsername == null || recipientUsername.trim().isEmpty()) {
            return false;
        }
        ensureSchemaConsistency();

        String lockSender = "SELECT wallet_balance FROM " + USERS_TABLE + " WHERE id = ? FOR UPDATE";
        String lockRecipient = "SELECT id FROM " + USERS_TABLE + " WHERE LOWER(username)=LOWER(?) LIMIT 1 FOR UPDATE";
        String updateWallet = "UPDATE " + USERS_TABLE + " SET wallet_balance = COALESCE(wallet_balance, 0) + ? WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                return false;
            }

            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                double senderBalance;
                int recipientId;

                try (PreparedStatement ps = con.prepareStatement(lockSender)) {
                    ps.setInt(1, senderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        senderBalance = rs.getDouble(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(lockRecipient)) {
                    ps.setString(1, recipientUsername.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        recipientId = rs.getInt(1);
                    }
                }

                if (recipientId == senderId || senderBalance < amount) {
                    con.rollback();
                    return false;
                }

                try (PreparedStatement ps = con.prepareStatement(updateWallet)) {
                    ps.setDouble(1, -amount);
                    ps.setInt(2, senderId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(updateWallet)) {
                    ps.setDouble(1, amount);
                    ps.setInt(2, recipientId);
                    ps.executeUpdate();
                }

                con.commit();
                con.setAutoCommit(oldAutoCommit);
                return true;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateProfession(int idUser, int idProfession) {
        String sql = "UPDATE " + USERS_TABLE + " SET idProfession=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                return;
            }
            ps.setInt(1, idProfession);
            ps.setInt(2, idUser);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePhone(int idUser, String telephone) {
        String sql = "UPDATE " + USERS_TABLE + " SET telephone=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                return;
            }
            ps.setString(1, telephone);
            ps.setInt(2, idUser);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean updateEmail(int idUser, String email) {
        String checkSql = "SELECT id FROM " + USERS_TABLE + " WHERE email=? AND id <> ?";
        String sql = "UPDATE " + USERS_TABLE + " SET email=? WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                checkPs.setString(1, email);
                checkPs.setInt(2, idUser);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setInt(2, idUser);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int id = readInt(rs, "id", readInt(rs, "idUser", 0));
        String username = readString(rs, "username", "");
        String password = readString(rs, "password", readString(rs, "password_hash", ""));
        String roleStr = readString(rs, "role", "USER");
        int idProfession = readInt(rs, "idProfession", 0);
        String telephone = readString(rs, "telephone", readString(rs, "phone", ""));
        String email = readString(rs, "email", "");

        User user = new User(id, username, password, roleStr, idProfession, telephone, email);
        user.setFull_name(readString(rs, "full_name", username));
        user.setWalletBalance(readDouble(rs, "wallet_balance", 0.0));
        user.setLoyaltyPoints(readInt(rs, "loyalty_points", 0));
        user.setRole(parseRole(roleStr));
        return user;
    }

    private Role parseRole(String role) {
        if (role == null) {
            return Role.USER;
        }
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (Exception e) {
            return Role.USER;
        }
    }

    private int readInt(ResultSet rs, String column, int fallback) {
        try {
            return rs.getInt(column);
        } catch (SQLException e) {
            return fallback;
        }
    }

    private double readDouble(ResultSet rs, String column, double fallback) {
        try {
            return rs.getDouble(column);
        } catch (SQLException e) {
            return fallback;
        }
    }

    private String readString(ResultSet rs, String column, String fallback) {
        try {
            String value = rs.getString(column);
            return value != null ? value : fallback;
        } catch (SQLException e) {
            return fallback;
        }
    }
}
