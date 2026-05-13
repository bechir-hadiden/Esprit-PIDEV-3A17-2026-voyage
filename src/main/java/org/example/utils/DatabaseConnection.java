package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/subaru";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.err.println(
                    "ERROR: Could not connect to the database. Please ensure MySQL is running on localhost:3306 and the 'transport' database exists.");
            e.printStackTrace();
            return null;
        }
    }
}
