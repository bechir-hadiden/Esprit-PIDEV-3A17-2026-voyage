package com.example.demo1.controller.dao;

import com.example.demo1.entity.Booking;
import com.example.demo1.Utils.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists hotel bookings to the database (bookings table).
 */
public class BookingDAO {
    private final Database db = Database.getInstance();

    /**
     * Insert a new booking for the given user.
     * @return generated booking id (as string) or null on failure
     */
    public String create(Booking b, int userId) {
        String sql = "INSERT INTO bookings (user_id, hotel_id, room_type_id, check_in_date, check_out_date, guests, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, Integer.parseInt(b.getHotelId()));
            int rtId = b.getRoomTypeId();
            if (rtId > 0) {
                pstmt.setInt(3, rtId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setDate(4, Date.valueOf(b.getCheckIn()));
            pstmt.setDate(5, Date.valueOf(b.getCheckOut()));
            pstmt.setInt(6, b.getGuestCount());
            pstmt.setDouble(7, b.getTotalPrice());
            pstmt.setString(8, b.getStatus().name());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("BookingDAO create error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Load all hotel bookings for a user (with hotel and room names from joins).
     */
    public List<Booking> getByUserId(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.id, b.hotel_id, b.room_type_id, b.check_in_date, b.check_out_date, b.guests, b.total_price, b.status, b.created_at, " +
                "h.name AS hotel_name, rt.name AS room_type_name " +
                "FROM bookings b " +
                "JOIN hotels h ON h.id = b.hotel_id " +
                "LEFT JOIN room_types rt ON rt.id = b.room_type_id " +
                "WHERE b.user_id = ? ORDER BY b.created_at DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Booking b = mapRow(rs);
                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("BookingDAO getByUserId error: " + e.getMessage());
        }
        return list;
    }

    public boolean cancel(int bookingId) {
        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BookingDAO cancel error: " + e.getMessage());
        }
        return false;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(String.valueOf(rs.getInt("id")));
        b.setHotelId(String.valueOf(rs.getInt("hotel_id")));
        b.setHotelName(rs.getString("hotel_name"));
        b.setRoomType(rs.getString("room_type_name") != null ? rs.getString("room_type_name") : "Room");
        b.setRoomTypeId(rs.getInt("room_type_id"));
        if (rs.wasNull()) b.setRoomTypeId(0);
        b.setCheckIn(rs.getObject("check_in_date", LocalDate.class));
        b.setCheckOut(rs.getObject("check_out_date", LocalDate.class));
        b.setGuestCount(rs.getInt("guests"));
        b.setTotalPrice(rs.getDouble("total_price"));
        String status = rs.getString("status");
        if (status != null) {
            try {
                b.setStatus(Booking.Status.valueOf(status));
            } catch (Exception e) {
                b.setStatus(Booking.Status.CONFIRMED);
            }
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            b.setCreatedAt(created.toLocalDateTime().toLocalDate());
        }
        return b;
    }
}
