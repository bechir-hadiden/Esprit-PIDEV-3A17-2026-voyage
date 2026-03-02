package org.example;

import org.example.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class TestDBConnection {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                System.out.println("SUCCESS: Connected to database 'transport'!");
                try (Statement stmt = con.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    if (rs.next()) {
                        System.out.println("SUCCESS: Executed test query (SELECT 1).");
                    }
                }
            } else {
                System.out.println("FAILURE: Connection is null (check console for stack trace).");
            }
        } catch (Exception e) {
            System.out.println("FAILURE: Exception occurred.");
            e.printStackTrace();
        }
    }
}
