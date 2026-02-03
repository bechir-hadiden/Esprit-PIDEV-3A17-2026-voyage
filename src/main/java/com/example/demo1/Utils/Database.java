package com.example.demo1.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/smart-trip";
        String user = "hamdi";
        String password = "hadmi123";
        return DriverManager.getConnection(url, user, password);
    }
    }
}