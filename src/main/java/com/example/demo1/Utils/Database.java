package com.example.demo1.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/smarttrip_db";
    private static final String USER = "root";  // Changez selon votre config
    private static final String PASSWORD = "";  // Changez selon votre config

    private static Database instance;
    private Connection connection;

    private Database() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données réussie!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données!");
            e.printStackTrace();
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
