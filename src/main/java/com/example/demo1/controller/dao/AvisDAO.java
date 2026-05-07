package com.example.demo1.controller.dao;

import com.example.demo1.entity.Avis;
import com.example.demo1.Utils.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvisDAO {

    private final Database database;

    public AvisDAO() {
        this.database = Database.getInstance();
    }

    private Connection getConnection() {
        return database.getConnection();
    }

    public List<Avis> getAllAvis() {
        List<Avis> avisList = new ArrayList<>();
        String sql = "SELECT * FROM avis ORDER BY date_avis DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                avisList.add(mapResultSetToAvis(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAllAvis: " + e.getMessage());
        }
        return avisList;
    }

    public Avis getAvisById(int id) {
        String sql = "SELECT * FROM avis WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToAvis(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAvisById: " + e.getMessage());
        }
        return null;
    }

    public List<Avis> getAvisByVoyage(int voyageId) {
        List<Avis> avisList = new ArrayList<>();
        String sql = "SELECT * FROM avis WHERE voyage_id = ? ORDER BY date_avis DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, voyageId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                avisList.add(mapResultSetToAvis(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAvisByVoyage: " + e.getMessage());
        }
        return avisList;
    }

    public boolean addAvis(Avis avis) {
        String sql = "INSERT INTO avis (nom_client, email, note, commentaire, date_avis, voyage_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, avis.getNomClient());
            ps.setString(2, avis.getEmail());
            ps.setInt(3, avis.getNote());
            ps.setString(4, avis.getCommentaire());
            ps.setDate(5, Date.valueOf(avis.getDateAvis()));
            ps.setInt(6, avis.getVoyageId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur addAvis: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAvis(Avis avis) {
        String sql = "UPDATE avis SET nom_client = ?, email = ?, note = ?, commentaire = ?, date_avis = ?, voyage_id = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, avis.getNomClient());
            ps.setString(2, avis.getEmail());
            ps.setInt(3, avis.getNote());
            ps.setString(4, avis.getCommentaire());
            ps.setDate(5, Date.valueOf(avis.getDateAvis()));
            ps.setInt(6, avis.getVoyageId());
            ps.setInt(7, avis.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur updateAvis: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAvis(int id) {
        String sql = "DELETE FROM avis WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur deleteAvis: " + e.getMessage());
            return false;
        }
    }

    public boolean hasUserReviewed(int voyageId, String email) {
        String sql = "SELECT COUNT(*) FROM avis WHERE voyage_id = ? AND email = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, voyageId);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur hasUserReviewed: " + e.getMessage());
        }
        return false;
    }

    public double getNoteMoyenne(int voyageId) {
        String sql = "SELECT AVG(note) as moyenne FROM avis WHERE voyage_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, voyageId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("moyenne");
            }
        } catch (SQLException e) {
            System.err.println("Erreur getNoteMoyenne: " + e.getMessage());
        }
        return 0;
    }

    private Avis mapResultSetToAvis(ResultSet rs) throws SQLException {
        Avis avis = new Avis();
        avis.setId(rs.getInt("id"));
        avis.setNomClient(rs.getString("nom_client"));
        avis.setEmail(rs.getString("email"));
        avis.setNote(rs.getInt("note"));
        avis.setCommentaire(rs.getString("commentaire"));
        avis.setDateAvis(rs.getDate("date_avis").toLocalDate());
        avis.setVoyageId(rs.getInt("voyage_id"));
        return avis;
    }
}