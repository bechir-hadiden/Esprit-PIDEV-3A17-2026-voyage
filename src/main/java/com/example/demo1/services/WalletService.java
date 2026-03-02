package com.example.demo1.services;

import com.example.demo1.Utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public classWalletService {

    public static class WalletActionResult {
        private final boolean success;
        private final String message;
        private final double newBalance;
        private final int newPoints;
        private final int earnedPoints;

        public WalletActionResult(boolean success, String message, double newBalance, int newPoints, int earnedPoints) {
            this.success = success;
            this.message = message;
            this.newBalance = newBalance;
            this.newPoints = newPoints;
            this.earnedPoints = earnedPoints;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public double getNewBalance() {
            return newBalance;
        }

        public int getNewPoints() {
            return newPoints;
        }

        public int getEarnedPoints() {
            return earnedPoints;
        }
    }

    private final Database database = Database.getInstance();

    public WalletActionResult topUp(int userId, double amount) {
        if (amount <= 0) {
            return new WalletActionResult(false, "Amount must be greater than 0.", 0, 0, 0);
        }

        Connection conn = database.getConnection();
        if (conn == null) {
            return new WalletActionResult(false, "Database connection unavailable.", 0, 0, 0);
        }

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            double currentBalance;
            int currentPoints;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT wallet_balance, loyalty_points FROM users WHERE id = ? FOR UPDATE")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return new WalletActionResult(false, "User not found.", 0, 0, 0);
                    }
                    currentBalance = rs.getDouble("wallet_balance");
                    currentPoints = rs.getInt("loyalty_points");
                }
            }

            int earnedPoints = Math.max(1, (int) Math.floor(amount / 10.0));
            double newBalance = round2(currentBalance + amount);
            int newPoints = currentPoints + earnedPoints;

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET wallet_balance = ?, loyalty_points = ? WHERE id = ?")) {
                ps.setDouble(1, newBalance);
                ps.setInt(2, newPoints);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }

            conn.commit();
            return new WalletActionResult(true, "Wallet recharged successfully.", newBalance, newPoints, earnedPoints);
        } catch (Exception e) {
            rollbackQuietly(conn);
            return new WalletActionResult(false, "Top-up failed: " + e.getMessage(), 0, 0, 0);
        } finally {
            restoreAutoCommit(conn, originalAutoCommit);
        }
    }

    public WalletActionResult transfer(int senderId, String recipientIdentifier, double amount) {
        if (recipientIdentifier == null || recipientIdentifier.trim().isEmpty()) {
            return new WalletActionResult(false, "Recipient email or username is required.", 0, 0, 0);
        }
        if (amount <= 0) {
            return new WalletActionResult(false, "Amount must be greater than 0.", 0, 0, 0);
        }

        Connection conn = database.getConnection();
        if (conn == null) {
            return new WalletActionResult(false, "Database connection unavailable.", 0, 0, 0);
        }

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            double senderBalance;
            int senderPoints;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, wallet_balance, loyalty_points FROM users WHERE id = ? FOR UPDATE")) {
                ps.setInt(1, senderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return new WalletActionResult(false, "Sender not found.", 0, 0, 0);
                    }
                    senderBalance = rs.getDouble("wallet_balance");
                    senderPoints = rs.getInt("loyalty_points");
                }
            }

            int recipientId;
            double recipientBalance;
            int recipientPoints;
            String recipientName;
            String lookup = recipientIdentifier.trim();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, full_name, wallet_balance, loyalty_points FROM users "
                            + "WHERE LOWER(email) = LOWER(?) OR LOWER(username) = LOWER(?) "
                            + "LIMIT 1 FOR UPDATE")) {
                ps.setString(1, lookup);
                ps.setString(2, lookup);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return new WalletActionResult(false, "Recipient not found.", 0, 0, 0);
                    }
                    recipientId = rs.getInt("id");
                    recipientName = rs.getString("full_name");
                    recipientBalance = rs.getDouble("wallet_balance");
                    recipientPoints = rs.getInt("loyalty_points");
                }
            }

            if (recipientId == senderId) {
                conn.rollback();
                return new WalletActionResult(false, "You cannot transfer to your own account.", 0, 0, 0);
            }
            if (senderBalance < amount) {
                conn.rollback();
                return new WalletActionResult(false, "Insufficient wallet balance.", 0, 0, 0);
            }

            int senderEarnedPoints = Math.max(1, (int) Math.floor(amount / 20.0));
            int recipientEarnedPoints = Math.max(0, (int) Math.floor(amount / 50.0));

            double senderNewBalance = round2(senderBalance - amount);
            int senderNewPoints = senderPoints + senderEarnedPoints;

            double recipientNewBalance = round2(recipientBalance + amount);
            int recipientNewPoints = recipientPoints + recipientEarnedPoints;

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET wallet_balance = ?, loyalty_points = ? WHERE id = ?")) {
                ps.setDouble(1, senderNewBalance);
                ps.setInt(2, senderNewPoints);
                ps.setInt(3, senderId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET wallet_balance = ?, loyalty_points = ? WHERE id = ?")) {
                ps.setDouble(1, recipientNewBalance);
                ps.setInt(2, recipientNewPoints);
                ps.setInt(3, recipientId);
                ps.executeUpdate();
            }

            conn.commit();
            String msg = "Transfer sent to " + (recipientName != null && !recipientName.isBlank() ? recipientName : lookup) + ".";
            return new WalletActionResult(true, msg, senderNewBalance, senderNewPoints, senderEarnedPoints);
        } catch (Exception e) {
            rollbackQuietly(conn);
            return new WalletActionResult(false, "Transfer failed: " + e.getMessage(), 0, 0, 0);
        } finally {
            restoreAutoCommit(conn, originalAutoCommit);
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static void rollbackQuietly(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException ignored) {
        }
    }

    private static void restoreAutoCommit(Connection conn, boolean autoCommit) {
        try {
            if (conn != null) {
                conn.setAutoCommit(autoCommit);
            }
        } catch (SQLException ignored) {
        }
    }
}
