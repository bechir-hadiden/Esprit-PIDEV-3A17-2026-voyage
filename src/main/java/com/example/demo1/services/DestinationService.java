package com.example.demo1.services;

import com.example.demo1.Utils.Database;
import com.example.demo1.entity.Destination;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinationService {

    private Connection connection;

    // ✅ SÉPARATEUR pour stocker plusieurs images dans image_url
    // Exemple: "/images/a.jpg;/images/b.jpg;/images/c.jpg"
    private static final String SEPARATOR = ";";

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

            // ✅ Stocker toutes les images en une seule cellule séparées par ";"
            pstmt.setString(5, imagesToString(destination.getImages(), destination.getImageUrl()));
            pstmt.setString(6, destination.getVideoUrl());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    destination.setId(keys.getInt(1));
                    System.out.println("✅ Destination sauvegardée id=" + destination.getId()
                            + " avec " + destination.getImages().size() + " image(s)");
                }
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
                Destination d = mapper(rs);
                destinations.add(d);
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
            if (rs.next()) return mapper(rs);

        } catch (SQLException e) {
            System.err.println("❌ Erreur getById destination: " + e.getMessage());
        }
        return null;
    }

    // ============================================
    // ✏️ MODIFIER
    // ============================================
    public boolean update(Destination destination) {
        String sql = "UPDATE destination SET nom=?, pays=?, code_iata=?, " +
                "description=?, image_url=?, video_url=? WHERE id=?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, destination.getNom());
            pstmt.setString(2, destination.getPays());
            pstmt.setString(3, destination.getCodeIata());
            pstmt.setString(4, destination.getDescription());

            // ✅ Réécrire toutes les images dans image_url
            pstmt.setString(5, imagesToString(destination.getImages(), destination.getImageUrl()));
            pstmt.setString(6, destination.getVideoUrl());
            pstmt.setInt(7, destination.getId());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Destination mise à jour id=" + destination.getId()
                        + " avec " + destination.getImages().size() + " image(s)");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur update destination: " + e.getMessage());
        }
        return false;
    }

    // ============================================
    // 🗑️ SUPPRIMER
    // ============================================
    public boolean delete(int id) {
        String sql = "DELETE FROM destination WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete destination: " + e.getMessage());
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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur updateVideoUrl: " + e.getMessage());
        }
        return false;
    }

    // ============================================
    // ✅ Ces méthodes sont gardées pour compatibilité
    //    mais ne font plus rien (pas de table séparée)
    // ============================================
    public boolean saveImages(int destinationId, List<String> images) {
        // Les images sont maintenant dans image_url directement
        return true;
    }

    public boolean deleteImages(int destinationId) {
        // Les images sont maintenant dans image_url directement
        return true;
    }

    public List<String> getImagesByDestinationId(int destinationId) {
        Destination d = getById(destinationId);
        return d != null ? d.getImages() : new ArrayList<>();
    }

    // ============================================
    // 🗺️ MAPPER ResultSet → Destination
    // ✅ Parse image_url avec ";" pour remplir la liste images
    // ============================================
    private Destination mapper(ResultSet rs) throws SQLException {
        Destination d = new Destination();
        d.setId(rs.getInt("id"));
        d.setNom(rs.getString("nom"));
        d.setPays(rs.getString("pays"));
        d.setCodeIata(rs.getString("code_iata"));
        d.setDescription(rs.getString("description"));
        d.setVideoUrl(rs.getString("video_url"));

        // ✅ Parser image_url → liste d'images
        String imageUrl = rs.getString("image_url");
        d.setImageUrl(imageUrl); // garder la valeur brute

        if (imageUrl != null && !imageUrl.isEmpty()) {
            List<String> images = new ArrayList<>();
            for (String url : imageUrl.split(SEPARATOR)) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) images.add(trimmed);
            }
            d.setImages(images);
            System.out.println("🖼️ " + d.getNom() + " → " + images.size() + " image(s)");
        }

        return d;
    }

    // ============================================
    // 🔧 HELPERS : convertir liste ↔ string
    // ============================================

    /**
     * Convertit une liste d'images en String séparé par ";"
     * Ex: ["/images/a.jpg", "/images/b.jpg"] → "/images/a.jpg;/images/b.jpg"
     */
    private String imagesToString(List<String> images, String fallbackUrl) {
        if (images != null && !images.isEmpty()) {
            return String.join(SEPARATOR, images);
        }
        // fallback : utiliser imageUrl simple
        return fallbackUrl != null ? fallbackUrl : "";
    }
}