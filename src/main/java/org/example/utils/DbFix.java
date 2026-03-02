package org.example.utils;

import org.example.services.UserService;
import org.example.entities.User;
import org.example.PaiementApp.Role;
import org.example.utils.DatabaseConnection;

public class DbFix {
    public static void main(String[] args) {
        UserService userService = new UserService();
        User user2 = userService.getUserByName("UserTest2");
        if (user2 == null) {
            System.out.println("Adding UserTest2...");
            try {
                var conn = DatabaseConnection.getConnection();
                var st = conn.createStatement();
                st.execute("INSERT INTO users (username, role, wallet_balance) VALUES ('UserTest2', 'USER', 50.0)");
                System.out.println("✅ UserTest2 added successfully!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("UserTest2 already exists.");
        }
    }
}
