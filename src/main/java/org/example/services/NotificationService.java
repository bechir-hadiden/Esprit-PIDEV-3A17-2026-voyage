package org.example.services;

import org.example.entities.Notification;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    public boolean ajouter(Notification n) {
        String sql = "INSERT INTO notification (user_id, reservation_id, message, type, date_sent) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, n.getUserId());
            pstmt.setInt(2, n.getReservationId());
            pstmt.setString(3, n.getMessage());
            pstmt.setString(4, n.getType());
            pstmt.setTimestamp(5, Timestamp.valueOf(n.getDateSent()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Notification> lister() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification ORDER BY date_sent DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setUserId(rs.getInt("user_id"));
                n.setReservationId(rs.getInt("reservation_id"));
                n.setMessage(rs.getString("message"));
                n.setRead(rs.getBoolean("is_read"));
                n.setType(rs.getString("type"));
                n.setDateSent(rs.getTimestamp("date_sent").toLocalDateTime());
                list.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM notification WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
