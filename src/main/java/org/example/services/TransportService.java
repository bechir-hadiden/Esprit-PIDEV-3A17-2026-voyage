package org.example.services;

import org.example.entities.Transport;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransportService {

    public void ajouter(Transport t) {
        String sql = "INSERT INTO transport_catalog(type, compagnie, numero, capacite, imageUrl, description, prix) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getType());
            ps.setString(2, t.getCompagnie());
            ps.setString(3, t.getNumero());
            ps.setInt(4, t.getCapacite());
            ps.setString(5, t.getImageUrl());
            ps.setString(6, t.getDescription());
            ps.setDouble(7, t.getPrix());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void modifier(Transport t) {
        String sql = "UPDATE transport_catalog SET type=?, compagnie=?, numero=?, capacite=?, imageUrl=?, description=?, prix=? WHERE idTransport=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getType());
            ps.setString(2, t.getCompagnie());
            ps.setString(3, t.getNumero());
            ps.setInt(4, t.getCapacite());
            ps.setString(5, t.getImageUrl());
            ps.setString(6, t.getDescription());
            ps.setDouble(7, t.getPrix());
            ps.setInt(8, t.getIdTransport());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM transport_catalog WHERE idTransport=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Transport> lister() {
        List<Transport> list = new ArrayList<>();
        String sql = "SELECT * FROM transport_catalog";

        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Transport t = new Transport(
                        rs.getInt("idTransport"),
                        rs.getString("type"),
                        rs.getString("compagnie"),
                        rs.getString("numero"),
                        rs.getInt("capacite"));

                // Set optional fields if they exist
                try {
                    t.setImageUrl(rs.getString("imageUrl"));
                    t.setDescription(rs.getString("description"));
                    t.setPrix(rs.getDouble("prix"));
                } catch (SQLException e) {
                    // Columns don't exist, skip
                }

                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Transport> listerParType(String type) {
        List<Transport> list = new ArrayList<>();
        String sql = "SELECT * FROM transport_catalog WHERE type=?";

        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transport t = new Transport(
                        rs.getInt("idTransport"),
                        rs.getString("type"),
                        rs.getString("compagnie"),
                        rs.getString("numero"),
                        rs.getInt("capacite"));

                // Set optional fields if they exist
                try {
                    t.setImageUrl(rs.getString("imageUrl"));
                    t.setDescription(rs.getString("description"));
                    t.setPrix(rs.getDouble("prix"));
                } catch (SQLException e) {
                    // Columns don't exist, skip
                }

                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getSurgePrice(int idTransport, double basePrice) {
        // Simple Demand-based surge pricing logic
        int reservationsCount = 0;
        String sql = "SELECT COUNT(*) FROM transport_reservation WHERE idVehicule = ? AND DATE(dateReservation) = CURDATE()";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTransport);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                reservationsCount = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        double multiplier = 1.0;
        if (reservationsCount > 10)
            multiplier = 1.25;
        else if (reservationsCount > 5)
            multiplier = 1.15;

        // Weather multiplier
        WeatherService weatherService = new WeatherService();
        WeatherService.WeatherData weather = weatherService.getWeather("Tunis");
        if (weather != null && weather.isRainy)
            multiplier += 0.10;

        return basePrice * multiplier;
    }
}
