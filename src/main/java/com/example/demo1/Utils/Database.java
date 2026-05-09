package com.example.demo1.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/smarttrip_db3";
    private static final String USER = "root"; // Changez selon votre config
    private static final String PASSWORD = ""; // Changez selon votre config

    private static Database instance;
    private Connection connection;

    private Database() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données réussie!");
            ensureSchema(); // Auto-repair schema if columns are missing
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données!");
            e.printStackTrace();
        }
    }

    private void ensureSchema() {
        if (connection == null)
            return;
        try (java.sql.Statement st = connection.createStatement()) {
            // Ensure users table has wallet_balance and loyalty_points
            addColumnIfMissing(st, "users", "wallet_balance", "DOUBLE DEFAULT 0");
            addColumnIfMissing(st, "users", "loyalty_points", "INT DEFAULT 0");
            addColumnIfMissing(st, "users", "password_hash", "VARCHAR(255) DEFAULT ''");
            addColumnIfMissing(st, "users", "phone", "VARCHAR(20) DEFAULT ''");

            // Ensure hotels table has all needed fields from dump
            addColumnIfMissing(st, "hotels", "price_per_week", "DECIMAL(10,2) DEFAULT NULL");
            addColumnIfMissing(st, "hotels", "contact_email", "VARCHAR(100) DEFAULT NULL");
            addColumnIfMissing(st, "hotels", "contact_phone", "VARCHAR(20) DEFAULT NULL");

            System.out.println("✅ Vérification du schéma terminée.");
        } catch (java.sql.SQLException e) {
            System.err.println("⚠️ Erreur lors de la vérification du schéma: " + e.getMessage());
        }
    }

    private void addColumnIfMissing(java.sql.Statement st, String tableName, String columnName, String columnType)
            throws java.sql.SQLException {
        try {
            // Check if column exists
            java.sql.DatabaseMetaData meta = connection.getMetaData();
            try (java.sql.ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
                if (!rs.next()) {
                    System.out.println("🛠 Ajout de la colonne '" + columnName + "' à la table '" + tableName + "'...");
                    st.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
                }
            }
        } catch (java.sql.SQLException e) {
            // Fallback for some DB versions or names
            System.err.println("   Note: Tentative d'ajout de " + columnName + " a échoué ou déjà présent.");
        }
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("❌ Impossible de se connecter à la base de données. Vérifiez que MySQL est démarré.");
            System.err.println("   URL: " + URL);
            System.err.println("   Pour continuer sans base de données, utilisez le mode démo.");
            return null; // Retourne null si la connexion échoue
        }
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
