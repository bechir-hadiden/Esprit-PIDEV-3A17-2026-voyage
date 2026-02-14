package org.example.demo10.dao;

import org.example.demo10.DBConnection;
import org.example.demo10.model.Avis;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvisDAO {

    // ========== MÉTHODES PRINCIPALES ==========

    // Récupérer tous les avis
    public List<Avis> getAllAvis() {
        List<Avis> avisList = new ArrayList<>();
        String query = "SELECT * FROM avis ORDER BY date_avis DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Avis avis = extractAvisFromResultSet(rs);
                avisList.add(avis);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return avisList;
    }

    // Récupérer un avis par ID
    public Avis getAvisById(int id) {
        String query = "SELECT * FROM avis WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAvisFromResultSet(rs);
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Récupérer les avis par voyage
    public List<Avis> getAvisByVoyage(int voyageId) {
        List<Avis> avisList = new ArrayList<>();
        String query = "SELECT * FROM avis WHERE voyage_id = ? ORDER BY date_avis DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, voyageId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Avis avis = extractAvisFromResultSet(rs);
                avisList.add(avis);
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return avisList;
    }

    // ========== NOUVELLE MÉTHODE POUR EMAIL ==========

    /**
     * Récupère un avis par ses détails (nom, email, voyageId)
     * Utilisé pour envoyer un email après l'ajout d'un avis
     */
    public Avis getAvisByDetails(String nomClient, String email, int voyageId) {
        String query = "SELECT * FROM avis WHERE nom_client = ? AND email = ? AND voyage_id = ? ORDER BY id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nomClient);
            pstmt.setString(2, email);
            pstmt.setInt(3, voyageId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAvisFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========== MÉTHODES CRUD ==========

    // Ajouter un avis
    public boolean addAvis(Avis avis) {
        String query = "INSERT INTO avis (nom_client, email, note, commentaire, date_avis, voyage_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, avis.getNomClient());
            pstmt.setString(2, avis.getEmail());
            pstmt.setInt(3, avis.getNote());
            pstmt.setString(4, avis.getCommentaire());
            pstmt.setDate(5, Date.valueOf(avis.getDateAvis()));
            pstmt.setInt(6, avis.getVoyageId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mettre à jour un avis
    public boolean updateAvis(Avis avis) {
        String query = "UPDATE avis SET nom_client = ?, email = ?, note = ?, commentaire = ?, date_avis = ?, voyage_id = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, avis.getNomClient());
            pstmt.setString(2, avis.getEmail());
            pstmt.setInt(3, avis.getNote());
            pstmt.setString(4, avis.getCommentaire());
            pstmt.setDate(5, Date.valueOf(avis.getDateAvis()));
            pstmt.setInt(6, avis.getVoyageId());
            pstmt.setInt(7, avis.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Supprimer un avis
    public boolean deleteAvis(int id) {
        String query = "DELETE FROM avis WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== MÉTHODES STATISTIQUES ==========

    // Récupérer la note moyenne d'un voyage
    public double getNoteMoyenne(int voyageId) {
        String query = "SELECT AVG(note) as moyenne FROM avis WHERE voyage_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, voyageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("moyenne");
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Compter le nombre d'avis pour un voyage
    public int countAvisByVoyage(int voyageId) {
        String query = "SELECT COUNT(*) as total FROM avis WHERE voyage_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, voyageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ========== MÉTHODES DE RECHERCHE AVANCÉE ==========

    // Rechercher des avis par mot-clé dans le commentaire
    public List<Avis> searchAvisByKeyword(String keyword) {
        List<Avis> avisList = new ArrayList<>();
        String query = "SELECT * FROM avis WHERE commentaire LIKE ? ORDER BY date_avis DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Avis avis = extractAvisFromResultSet(rs);
                avisList.add(avis);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return avisList;
    }

    // Rechercher des avis par plage de dates
    public List<Avis> getAvisByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Avis> avisList = new ArrayList<>();
        String query = "SELECT * FROM avis WHERE date_avis BETWEEN ? AND ? ORDER BY date_avis DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Avis avis = extractAvisFromResultSet(rs);
                avisList.add(avis);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return avisList;
    }

    // ========== MÉTHODE UTILITAIRE ==========

    /**
     * Extrait un objet Avis depuis un ResultSet
     */
    private Avis extractAvisFromResultSet(ResultSet rs) throws SQLException {
        Avis avis = new Avis();
        avis.setId(rs.getInt("id"));
        avis.setNomClient(rs.getString("nom_client"));
        avis.setEmail(rs.getString("email"));
        avis.setNote(rs.getInt("note"));
        avis.setCommentaire(rs.getString("commentaire"));

        Date dateSql = rs.getDate("date_avis");
        if (dateSql != null) {
            avis.setDateAvis(dateSql.toLocalDate());
        }

        avis.setVoyageId(rs.getInt("voyage_id"));

        return avis;
    }
}