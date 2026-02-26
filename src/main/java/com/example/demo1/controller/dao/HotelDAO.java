package com.example.demo1.controller.dao;

import com.example.demo1.entity.Hotel;
import com.example.demo1.entity.RoomType;
import com.example.demo1.Utils.Database;
import javafx.collections.FXCollections;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Hotel entity.
 * Handles all database operations related to hotels including related data
 * (images, amenities, policies, rooms).
 */
public class HotelDAO {

    private final Database dbConnection;

    public HotelDAO() {
        this.dbConnection = Database.getInstance();
    }

    /**
     * Get all hotels with complete data.
     *
     * @return List of Hotel objects with images, amenities, policies, and rooms
     */
    public List<Hotel> getAllHotels() {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotels ORDER BY name";

        // First, extract all basic hotel data
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Hotel hotel = extractHotelFromResultSet(rs);
                hotels.add(hotel);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all hotels: " + e.getMessage());
            e.printStackTrace();
        }

        // Then, load complete data for each hotel (using separate connections)
        for (Hotel hotel : hotels) {
            try {
                loadCompleteHotelData(hotel);
            } catch (SQLException e) {
                System.err.println("Error loading complete data for hotel " + hotel.getId() + ": " + e.getMessage());
            }
        }

        return hotels;
    }

    /**
     * Get hotel by ID with complete data.
     *
     * @param hotelId Hotel ID
     * @return Hotel object or null if not found
     */
    public Hotel getHotelById(String hotelId) {
        String sql = "SELECT * FROM hotels WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(hotelId));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Hotel hotel = extractHotelFromResultSet(rs);
                loadCompleteHotelData(hotel);
                return hotel;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching hotel by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Search hotels by query (name, city, country, or location).
     *
     * @param query Search query
     * @return List of matching hotels
     */
    public List<Hotel> searchHotels(String query) {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotels WHERE name LIKE ? OR city LIKE ? OR country LIKE ? OR location LIKE ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Hotel hotel = extractHotelFromResultSet(rs);
                loadCompleteHotelData(hotel);
                hotels.add(hotel);
            }
        } catch (SQLException e) {
            System.err.println("Error searching hotels: " + e.getMessage());
            e.printStackTrace();
        }

        return hotels;
    }

    /**
     * Create a new hotel with all related data.
     *
     * @param hotel Hotel object with images, amenities, policies, and rooms
     * @return true if creation successful, false otherwise
     */
    public boolean createHotel(Hotel hotel) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert hotel
            String hotelSql = "INSERT INTO hotels (name, location, city, country, description, " +
                    "price_per_night, price_per_week, rating, review_count, " +
                    "check_in_time, check_out_time, contact_email, contact_phone) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(hotelSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, hotel.getName());
            pstmt.setString(2, hotel.getLocation());
            pstmt.setString(3, hotel.getCity());
            pstmt.setString(4, hotel.getCountry());
            pstmt.setString(5, hotel.getDescription());
            pstmt.setDouble(6, hotel.getPricePerNight());
            pstmt.setDouble(7, hotel.getPricePerWeek());
            pstmt.setDouble(8, hotel.getRating());
            pstmt.setInt(9, hotel.getReviewCount());
            pstmt.setString(10, hotel.getCheckInTime());
            pstmt.setString(11, hotel.getCheckOutTime());
            pstmt.setString(12, hotel.getContactEmail());
            pstmt.setString(13, hotel.getContactPhone());

            pstmt.executeUpdate();

            // Get generated hotel ID
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int hotelId = generatedKeys.getInt(1);
                hotel.setId(String.valueOf(hotelId));

                // Insert images
                insertHotelImages(conn, hotelId, hotel.getImages());

                // Insert amenities
                insertHotelAmenities(conn, hotelId, hotel.getAmenities());

                // Insert policies
                insertHotelPolicies(conn, hotelId, hotel.getPolicies());

                // Insert room types
                insertRoomTypes(conn, hotelId, hotel.getRoomTypes());
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            System.err.println("Error creating hotel: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Update existing hotel with all related data.
     *
     * @param hotel Hotel object with updated data
     * @return true if update successful, false otherwise
     */
    public boolean updateHotel(Hotel hotel) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            int hotelId = Integer.parseInt(hotel.getId());

            // Update hotel
            String hotelSql = "UPDATE hotels SET name = ?, location = ?, city = ?, country = ?, " +
                    "description = ?, price_per_night = ?, price_per_week = ?, rating = ?, " +
                    "review_count = ?, check_in_time = ?, check_out_time = ?, " +
                    "contact_email = ?, contact_phone = ? WHERE id = ?";

            PreparedStatement pstmt = conn.prepareStatement(hotelSql);
            pstmt.setString(1, hotel.getName());
            pstmt.setString(2, hotel.getLocation());
            pstmt.setString(3, hotel.getCity());
            pstmt.setString(4, hotel.getCountry());
            pstmt.setString(5, hotel.getDescription());
            pstmt.setDouble(6, hotel.getPricePerNight());
            pstmt.setDouble(7, hotel.getPricePerWeek());
            pstmt.setDouble(8, hotel.getRating());
            pstmt.setInt(9, hotel.getReviewCount());
            pstmt.setString(10, hotel.getCheckInTime());
            pstmt.setString(11, hotel.getCheckOutTime());
            pstmt.setString(12, hotel.getContactEmail());
            pstmt.setString(13, hotel.getContactPhone());
            pstmt.setInt(14, hotelId);

            pstmt.executeUpdate();

            // Delete and re-insert related data
            deleteHotelRelatedData(conn, hotelId);

            insertHotelImages(conn, hotelId, hotel.getImages());
            insertHotelAmenities(conn, hotelId, hotel.getAmenities());
            insertHotelPolicies(conn, hotelId, hotel.getPolicies());
            insertRoomTypes(conn, hotelId, hotel.getRoomTypes());

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating hotel: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete hotel by ID.
     * Related data will be cascade deleted automatically.
     *
     * @param hotelId Hotel ID
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteHotel(int hotelId) {
        String sql = "DELETE FROM hotels WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting hotel: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // ==================== Helper Methods ====================

    /**
     * Extract Hotel object from ResultSet (basic data only).
     */
    private Hotel extractHotelFromResultSet(ResultSet rs) throws SQLException {
        Hotel hotel = new Hotel();
        hotel.setId(String.valueOf(rs.getInt("id")));
        hotel.setName(rs.getString("name"));
        hotel.setLocation(rs.getString("location"));
        hotel.setCity(rs.getString("city"));
        hotel.setCountry(rs.getString("country"));
        hotel.setDescription(rs.getString("description"));
        hotel.setPricePerNight(rs.getDouble("price_per_night"));
        hotel.setPricePerWeek(rs.getDouble("price_per_week"));
        hotel.setRating(rs.getDouble("rating"));
        hotel.setReviewCount(rs.getInt("review_count"));
        hotel.setCheckInTime(rs.getString("check_in_time"));
        hotel.setCheckOutTime(rs.getString("check_out_time"));
        hotel.setContactEmail(rs.getString("contact_email"));
        hotel.setContactPhone(rs.getString("contact_phone"));
        return hotel;
    }

    /**
     * Load complete hotel data including images, amenities, policies, and rooms.
     */
    private void loadCompleteHotelData(Hotel hotel) throws SQLException {
        int hotelId = Integer.parseInt(hotel.getId());

        // Load images
        hotel.getImages().clear();
        hotel.getImages().addAll(loadHotelImages(hotelId));

        // Load amenities
        hotel.getAmenities().clear();
        hotel.getAmenities().addAll(loadHotelAmenities(hotelId));

        // Load policies
        hotel.getPolicies().clear();
        hotel.getPolicies().addAll(loadHotelPolicies(hotelId));

        // Load room types
        hotel.getRoomTypes().clear();
        hotel.getRoomTypes().addAll(FXCollections.observableArrayList(loadRoomTypes(hotelId)));
    }

    /**
     * Load hotel images from database.
     */
    private List<String> loadHotelImages(int hotelId) throws SQLException {
        List<String> images = new ArrayList<>();
        String sql = "SELECT image_url FROM hotel_images WHERE hotel_id = ? ORDER BY display_order";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                images.add(rs.getString("image_url"));
            }
        }

        return images;
    }

    /**
     * Load hotel amenities from database.
     */
    private List<String> loadHotelAmenities(int hotelId) throws SQLException {
        List<String> amenities = new ArrayList<>();
        String sql = "SELECT amenity_name FROM hotel_amenities WHERE hotel_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                amenities.add(rs.getString("amenity_name"));
            }
        }

        return amenities;
    }

    /**
     * Load hotel policies from database.
     */
    private List<String> loadHotelPolicies(int hotelId) throws SQLException {
        List<String> policies = new ArrayList<>();
        String sql = "SELECT policy_text FROM hotel_policies WHERE hotel_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                policies.add(rs.getString("policy_text"));
            }
        }

        return policies;
    }

    /**
     * Load room types for hotel.
     */
    private List<RoomType> loadRoomTypes(int hotelId) throws SQLException {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT * FROM room_types WHERE hotel_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);
            ResultSet rs = pstmt.executeQuery();

            // First, collect all room data from ResultSet
            while (rs.next()) {
                RoomType room = new RoomType();
                int roomId = rs.getInt("id");
                room.setId(String.valueOf(roomId));
                room.setName(rs.getString("name"));
                room.setDescription(rs.getString("description"));
                room.setMaxGuests(rs.getInt("max_occupancy"));
                room.setPricePerNight(rs.getDouble("price_per_night"));
                room.setAvailable(rs.getBoolean("is_available"));

                roomTypes.add(room);
            }

            // Close the ResultSet before loading nested data
            rs.close();
        }

        // Now load nested data for each room (after ResultSet is closed)
        for (RoomType room : roomTypes) {
            int roomId = Integer.parseInt(room.getId());
            room.getAmenities().addAll(loadRoomAmenities(roomId));
            room.getImages().addAll(loadRoomImages(roomId));
        }

        return roomTypes;
    }

    /**
     * Load room amenities from database.
     */
    private List<String> loadRoomAmenities(int roomTypeId) throws SQLException {
        List<String> amenities = new ArrayList<>();
        String sql = "SELECT amenity_name FROM room_amenities WHERE room_type_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomTypeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                amenities.add(rs.getString("amenity_name"));
            }
        }

        return amenities;
    }

    /**
     * Load room images from database.
     */
    private List<String> loadRoomImages(int roomTypeId) throws SQLException {
        List<String> images = new ArrayList<>();
        String sql = "SELECT image_url FROM room_images WHERE room_type_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomTypeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                images.add(rs.getString("image_url"));
            }
        }

        return images;
    }

    /**
     * Insert hotel images.
     */
    private void insertHotelImages(Connection conn, int hotelId, List<String> images) throws SQLException {
        String sql = "INSERT INTO hotel_images (hotel_id, image_url, display_order) VALUES (?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (int i = 0; i < images.size(); i++) {
            pstmt.setInt(1, hotelId);
            pstmt.setString(2, images.get(i));
            pstmt.setInt(3, i + 1);
            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }

    /**
     * Insert hotel amenities.
     */
    private void insertHotelAmenities(Connection conn, int hotelId, List<String> amenities) throws SQLException {
        String sql = "INSERT INTO hotel_amenities (hotel_id, amenity_name) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (String amenity : amenities) {
            pstmt.setInt(1, hotelId);
            pstmt.setString(2, amenity);
            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }

    /**
     * Insert hotel policies.
     */
    private void insertHotelPolicies(Connection conn, int hotelId, List<String> policies) throws SQLException {
        String sql = "INSERT INTO hotel_policies (hotel_id, policy_text) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (String policy : policies) {
            pstmt.setInt(1, hotelId);
            pstmt.setString(2, policy);
            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }

    /**
     * Insert room types with amenities and images.
     */
    private void insertRoomTypes(Connection conn, int hotelId, List<RoomType> roomTypes) throws SQLException {
        String sql = "INSERT INTO room_types (hotel_id, name, description, max_occupancy, price_per_night, is_available) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        for (RoomType room : roomTypes) {
            pstmt.setInt(1, hotelId);
            pstmt.setString(2, room.getName());
            pstmt.setString(3, room.getDescription());
            pstmt.setInt(4, room.getMaxGuests());
            pstmt.setDouble(5, room.getPricePerNight());
            pstmt.setBoolean(6, room.isAvailable());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int roomId = generatedKeys.getInt(1);
                insertRoomAmenities(conn, roomId, room.getAmenities());
                insertRoomImages(conn, roomId, room.getImages());
            }
        }
    }

    /**
     * Insert room amenities.
     */
    private void insertRoomAmenities(Connection conn, int roomTypeId, List<String> amenities) throws SQLException {
        String sql = "INSERT INTO room_amenities (room_type_id, amenity_name) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (String amenity : amenities) {
            pstmt.setInt(1, roomTypeId);
            pstmt.setString(2, amenity);
            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }

    /**
     * Insert room images.
     */
    private void insertRoomImages(Connection conn, int roomTypeId, List<String> images) throws SQLException {
        String sql = "INSERT INTO room_images (room_type_id, image_url) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (String image : images) {
            pstmt.setInt(1, roomTypeId);
            pstmt.setString(2, image);
            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }

    /**
     * Delete all hotel related data (images, amenities, policies, rooms).
     */
    private void deleteHotelRelatedData(Connection conn, int hotelId) throws SQLException {
        // Delete room-related data first
        conn.prepareStatement(
                        "DELETE FROM room_amenities WHERE room_type_id IN (SELECT id FROM room_types WHERE hotel_id = "
                                + hotelId + ")")
                .executeUpdate();
        conn.prepareStatement(
                        "DELETE FROM room_images WHERE room_type_id IN (SELECT id FROM room_types WHERE hotel_id = " + hotelId
                                + ")")
                .executeUpdate();
        conn.prepareStatement("DELETE FROM room_types WHERE hotel_id = " + hotelId).executeUpdate();

        // Delete hotel-related data
        conn.prepareStatement("DELETE FROM hotel_images WHERE hotel_id = " + hotelId).executeUpdate();
        conn.prepareStatement("DELETE FROM hotel_amenities WHERE hotel_id = " + hotelId).executeUpdate();
        conn.prepareStatement("DELETE FROM hotel_policies WHERE hotel_id = " + hotelId).executeUpdate();
    }
}

