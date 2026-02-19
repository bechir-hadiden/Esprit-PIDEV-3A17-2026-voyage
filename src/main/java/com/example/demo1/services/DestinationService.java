package com.example.demo1.services;

import com.example.demo1.Utils.Database;
import com.example.demo1.entity.Destination;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinationService {

    private Connection connection;

    public DestinationService() {
        this.connection = Database.getInstance().getConnection();
    }

    // ============================================
    // 💾 SAUVEGARDER UNE DESTINATION
    // ============================================
    public boolean save(Destination destination) {
        String sql = """
            INSERT INTO destination (nom, pays, code_iata, description, image_url, video_url)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, destination.getNom());
            pstmt.setString(2, destination.getPays());
            pstmt.setString(3, destination.getCodeIata());
            pstmt.setString(4, destination.getDescription());
            pstmt.setString(5, destination.getImageUrl());
            pstmt.setString(6, destination.getVideoUrl());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    destination.setId(keys.getInt(1));
                }
                System.out.println("✅ Destination sauvegardée: " + destination.getNom());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur save destination: " + e.getMessage());
        }
        return false;
    }

    // ============================================
    // 📋 RÉCUPÉRER TOUTES LES DESTINATIONS
    // ============================================
    public List<Destination> getAll() {
        List<Destination> destinations = new ArrayList<>();
        String sql = "SELECT * FROM destination ORDER BY nom";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                destinations.add(mapper(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getAll destinations: " + e.getMessage());
        }

        return destinations;
    }

    // ============================================
    // 🔍 RÉCUPÉRER PAR ID
    // ============================================
    public Destination getById(int id) {
        String sql = "SELECT * FROM destination WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapper(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur getById destination: " + e.getMessage());
        }
        return null;
    }


    public boolean update(Destination destination) {
        String sql = "UPDATE destination SET " +
                "nom = ?, " +
                "pays = ?, " +
                "code_iata = ?, " +
                "description = ?, " +
                "image_url = ?, " +
                "video_url = ? " +
                "WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, destination.getNom());
            pstmt.setString(2, destination.getPays());
            pstmt.setString(3, destination.getCodeIata());
            pstmt.setString(4, destination.getDescription());
            pstmt.setString(5, destination.getImageUrl());
            pstmt.setString(6, destination.getVideoUrl());
            pstmt.setInt(7, destination.getId());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Destination modifiée: " + destination.getNom());
                return true;
            } else {
                System.out.println("⚠️ Aucune destination modifiée (ID introuvable: " + destination.getId() + ")");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur update destination: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // ============================================
    // 🎬 METTRE À JOUR LA VIDÉO URL
    // ============================================
    public boolean updateVideoUrl(int destinationId, String videoUrl) {
        String sql = "UPDATE destination SET video_url = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, videoUrl);
            pstmt.setInt(2, destinationId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ video_url mise à jour pour destination ID: " + destinationId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur updateVideoUrl: " + e.getMessage());
        }
        return false;
    }

    // ============================================
    // 🗺️ MAPPER ResultSet → Destination
    // ============================================
    private Destination mapper(ResultSet rs) throws SQLException {
        Destination d = new Destination();
        d.setId(rs.getInt("id"));
        d.setNom(rs.getString("nom"));
        d.setPays(rs.getString("pays"));
        d.setCodeIata(rs.getString("code_iata"));
        d.setDescription(rs.getString("description"));
        d.setImageUrl(rs.getString("image_url"));
        d.setVideoUrl(rs.getString("video_url"));
        return d;
    }


    public boolean delete(int id) {
        String sql = "DELETE FROM destination WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Destination supprimée: ID " + id);
                return true;
            } else {
                System.out.println("⚠️ Aucune destination supprimée (ID introuvable: " + id + ")");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur delete destination: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}