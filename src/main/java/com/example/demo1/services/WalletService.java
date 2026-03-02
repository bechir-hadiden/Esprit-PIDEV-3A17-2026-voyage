package com.example.demo1.services;

import com.example.demo1.Utils.Database;
import com.example.demo1.entity.Offre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WalletService {

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

    public static class WalletForecastResult {
        private final boolean predictionAvailable;
        private final int windowDays;
        private final int actionCount;
        private final double totalOutflow;
        private final double averageDailyOutflow;
        private final int estimatedDaysToEmpty;
        private final LocalDate estimatedEmptyDate;
        private final String message;

        public WalletForecastResult(
                boolean predictionAvailable,
                int windowDays,
                int actionCount,
                double totalOutflow,
                double averageDailyOutflow,
                int estimatedDaysToEmpty,
                LocalDate estimatedEmptyDate,
                String message
        ) {
            this.predictionAvailable = predictionAvailable;
            this.windowDays = windowDays;
            this.actionCount = actionCount;
            this.totalOutflow = totalOutflow;
            this.averageDailyOutflow = averageDailyOutflow;
            this.estimatedDaysToEmpty = estimatedDaysToEmpty;
            this.estimatedEmptyDate = estimatedEmptyDate;
            this.message = message;
        }

        public boolean isPredictionAvailable() {
            return predictionAvailable;
        }

        public int getWindowDays() {
            return windowDays;
        }

        public int getActionCount() {
            return actionCount;
        }

        public double getTotalOutflow() {
            return totalOutflow;
        }

        public double getAverageDailyOutflow() {
            return averageDailyOutflow;
        }

        public int getEstimatedDaysToEmpty() {
            return estimatedDaysToEmpty;
        }

        public LocalDate getEstimatedEmptyDate() {
            return estimatedEmptyDate;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class OfferRecommendation {
        private final String title;
        private final String category;
        private final int discountPercent;
        private final double finalPrice;
        private final String reason;

        public OfferRecommendation(String title, String category, int discountPercent, double finalPrice, String reason) {
            this.title = title;
            this.category = category;
            this.discountPercent = discountPercent;
            this.finalPrice = finalPrice;
            this.reason = reason;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        public int getDiscountPercent() {
            return discountPercent;
        }

        public double getFinalPrice() {
            return finalPrice;
        }

        public String getReason() {
            return reason;
        }
    }

    public static class WalletRecommendationResult {
        private final String cashbackTip;
        private final List<OfferRecommendation> offers;
        private final String message;

        public WalletRecommendationResult(String cashbackTip, List<OfferRecommendation> offers, String message) {
            this.cashbackTip = cashbackTip;
            this.offers = offers;
            this.message = message;
        }

        public String getCashbackTip() {
            return cashbackTip;
        }

        public List<OfferRecommendation> getOffers() {
            return offers;
        }

        public String getMessage() {
            return message;
        }
    }

    private final Database database = Database.getInstance();
    private final OffreService offreService = new OffreService();
    private static final int FORECAST_WINDOW_DAYS = 30;
    private static final int RECOMMENDATION_WINDOW_DAYS = 90;
    private volatile boolean transactionSchemaReady = false;

    public WalletService() {
        ensureWalletTransactionSchema();
    }

    public WalletActionResult topUp(int userId, double amount) {
        if (amount <= 0) {
            return new WalletActionResult(false, "Amount must be greater than 0.", 0, 0, 0);
        }
        ensureWalletTransactionSchema();

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

            try {
                insertWalletTransaction(conn, userId, "TOP_UP", "IN", amount, "Wallet recharge");
            } catch (SQLException ignored) {
                // Forecast history is optional; recharge should still succeed.
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
        ensureWalletTransactionSchema();

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

            try {
                insertWalletTransaction(conn, senderId, "TRANSFER_OUT", "OUT", amount, "Transfer to user #" + recipientId);
                insertWalletTransaction(conn, recipientId, "TRANSFER_IN", "IN", amount, "Transfer from user #" + senderId);
            } catch (SQLException ignored) {
                // Forecast history is optional; transfer should still succeed.
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

    public WalletForecastResult predictBalanceDepletion(int userId, double currentBalance) {
        if (userId <= 0) {
            return unavailableForecast("Invalid user identifier.");
        }
        if (currentBalance <= 0) {
            LocalDate today = LocalDate.now();
            return new WalletForecastResult(
                    true,
                    FORECAST_WINDOW_DAYS,
                    0,
                    0,
                    0,
                    0,
                    today,
                    "Wallet is already empty."
            );
        }

        ensureWalletTransactionSchema();

        Connection conn = database.getConnection();
        if (conn == null) {
            return unavailableForecast("Database connection unavailable.");
        }

        LocalDate startDate = LocalDate.now().minusDays(FORECAST_WINDOW_DAYS - 1L);
        Timestamp startTs = Timestamp.valueOf(startDate.atStartOfDay());

        double transferOutflow = 0;
        int transferActions = 0;
        double transferInflows = 0;
        int inflowActions = 0;
        double walletPaymentOutflow = 0;
        int walletPaymentActions = 0;

        try (PreparedStatement transferPs = conn.prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) AS total, COUNT(*) AS cnt " +
                        "FROM wallet_transactions " +
                        "WHERE user_id = ? AND direction = 'OUT' AND created_at >= ?")) {
            transferPs.setInt(1, userId);
            transferPs.setTimestamp(2, startTs);

            try (ResultSet rs = transferPs.executeQuery()) {
                if (rs.next()) {
                    transferOutflow = rs.getDouble("total");
                    transferActions = rs.getInt("cnt");
                }
            }
        } catch (SQLException e) {
            return unavailableForecast("Cannot read wallet transfers: " + e.getMessage());
        }

        try (PreparedStatement inflowPs = conn.prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) AS total, COUNT(*) AS cnt " +
                        "FROM wallet_transactions " +
                        "WHERE user_id = ? AND direction = 'IN' AND created_at >= ?")) {
            inflowPs.setInt(1, userId);
            inflowPs.setTimestamp(2, startTs);

            try (ResultSet rs = inflowPs.executeQuery()) {
                if (rs.next()) {
                    transferInflows = rs.getDouble("total");
                    inflowActions = rs.getInt("cnt");
                }
            }
        } catch (SQLException e) {
            transferInflows = 0;
            inflowActions = 0;
        }

        try (PreparedStatement walletPayPs = conn.prepareStatement(
                "SELECT COALESCE(SUM(montant), 0) AS total, COUNT(*) AS cnt " +
                        "FROM paiements " +
                        "WHERE user_id = ? " +
                        "AND date_paiement >= ? " +
                        "AND LOWER(statut_paiement) LIKE 'effectu%' " +
                        "AND LOWER(methode_paiement) LIKE '%portefeuille%'")) {
            walletPayPs.setInt(1, userId);
            walletPayPs.setDate(2, Date.valueOf(startDate));

            try (ResultSet rs = walletPayPs.executeQuery()) {
                if (rs.next()) {
                    walletPaymentOutflow = rs.getDouble("total");
                    walletPaymentActions = rs.getInt("cnt");
                }
            }
        } catch (SQLException e) {
            // Keep forecast alive even if paiements table is not available in some environments.
            walletPaymentOutflow = 0;
            walletPaymentActions = 0;
        }

        int actionCount = transferActions + walletPaymentActions + inflowActions;
        double totalOutflow = round2(Math.max(0, transferOutflow) + Math.max(0, walletPaymentOutflow));
        double totalInflows = round2(Math.max(0, transferInflows));
        if (totalOutflow <= 0) {
            return unavailableForecast("Not enough spending history yet.");
        }

        double netOutflow = round2(totalOutflow - totalInflows);
        if (netOutflow <= 0) {
            return unavailableForecast("Balance trend is stable or growing in recent history.");
        }

        double avgDailyOutflow = round2(netOutflow / FORECAST_WINDOW_DAYS);
        if (avgDailyOutflow <= 0) {
            return unavailableForecast("Not enough net spending history yet.");
        }

        int estimatedDays = (int) Math.ceil(currentBalance / avgDailyOutflow);
        LocalDate estimatedEmptyDate = LocalDate.now().plusDays(estimatedDays);

        return new WalletForecastResult(
                true,
                FORECAST_WINDOW_DAYS,
                actionCount,
                totalOutflow,
                avgDailyOutflow,
                estimatedDays,
                estimatedEmptyDate,
                "Estimated from last " + FORECAST_WINDOW_DAYS + " days."
        );
    }

    private WalletForecastResult unavailableForecast(String message) {
        return new WalletForecastResult(
                false,
                FORECAST_WINDOW_DAYS,
                0,
                0,
                0,
                -1,
                null,
                message
        );
    }

    private void ensureWalletTransactionSchema() {
        if (transactionSchemaReady) {
            return;
        }

        synchronized (WalletService.class) {
            if (transactionSchemaReady) {
                return;
            }

            Connection conn = database.getConnection();
            if (conn == null) {
                return;
            }

            try (Statement st = conn.createStatement()) {
                st.execute(
                        "CREATE TABLE IF NOT EXISTS wallet_transactions (" +
                                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                                "user_id INT NOT NULL," +
                                "type VARCHAR(40) NOT NULL," +
                                "direction VARCHAR(8) NOT NULL," +
                                "amount DECIMAL(10,2) NOT NULL," +
                                "description VARCHAR(255)," +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );
                try {
                    st.execute("CREATE INDEX idx_wallet_transactions_user_time ON wallet_transactions(user_id, created_at)");
                } catch (SQLException ignored) {
                    // Index may already exist depending on DB/version.
                }
                transactionSchemaReady = true;
            } catch (SQLException e) {
                // Keep app functional even when schema migration is not possible.
                transactionSchemaReady = false;
            }
        }
    }

    private void insertWalletTransaction(
            Connection conn,
            int userId,
            String type,
            String direction,
            double amount,
            String description
    ) throws SQLException {
        if (conn == null || userId <= 0 || amount <= 0) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO wallet_transactions (user_id, type, direction, amount, description) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, direction);
            ps.setDouble(4, round2(amount));
            ps.setString(5, description);
            ps.executeUpdate();
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
