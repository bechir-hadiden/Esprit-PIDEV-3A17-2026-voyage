package org.example.demo10;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/agence_voyage?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {

        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ MySQL connecté !");
            return conn;

        } catch (Exception e) {
            System.out.println("❌ Erreur MySQL");
            e.printStackTrace();
            return null;
        }
    }
}
