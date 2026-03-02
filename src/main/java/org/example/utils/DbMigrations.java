package org.example.utils;

import java.sql.Connection;
import java.sql.Statement;

public class DbMigrations {
    public static void main(String[] args) {
        System.out.println("Starting Database Migrations...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 1. Create messages table
            System.out.println("Creating messages table...");
            stmt.execute("CREATE TABLE IF NOT EXISTS `messages` (" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                    "  `sender_id` int(11) NOT NULL," +
                    "  `receiver_id` int(11) NOT NULL," +
                    "  `content` text NOT NULL," +
                    "  `sent_at` timestamp NOT NULL DEFAULT current_timestamp()," +
                    "  PRIMARY KEY (`id`)," +
                    "  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE," +
                    "  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            
            // 2. Add booking_id to paiements if not exists
            System.out.println("Updating schema for payments...");
            try {
                stmt.execute("ALTER TABLE `paiements` ADD COLUMN `booking_id` int(11) DEFAULT NULL;");
                stmt.execute("ALTER TABLE `paiements` ADD CONSTRAINT `fk_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE;");
            } catch (Exception e) {}

            try {
                stmt.execute("ALTER TABLE `users` ADD COLUMN `wallet_balance` decimal(10,2) DEFAULT 0.00;");
                stmt.execute("ALTER TABLE `users` ADD COLUMN `loyalty_points` int(11) DEFAULT 0;");
                System.out.println("✅ Database updated successfully!");
            } catch (Exception e) {
                if (e.getMessage().contains("Duplicate column")) {
                    System.out.println("ℹ️ User columns already exist.");
                } else {
                    System.err.println("⚠️ Warning during ALTER TABLE: " + e.getMessage());
                }
            }
            
            System.out.println("🚀 All migrations completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Migration failed!");
            e.printStackTrace();
        }
    }
}
