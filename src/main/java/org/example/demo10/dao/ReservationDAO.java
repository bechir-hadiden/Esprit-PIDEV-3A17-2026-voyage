package org.example.demo10.dao;

import org.example.demo10.DBConnection;
import org.example.demo10.model.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    // Récupérer toutes les réservations
    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String query = "SELECT * FROM reservation ORDER BY date_reservation DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                list.add(extractReservationFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Récupérer une réservation par ID
    public Reservation getReservationById(int id) {
        String query = "SELECT * FROM reservation WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractReservationFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Récupérer les réservations par client
    public List<Reservation> getReservationsByClient(String email) {
        List<Reservation> list = new ArrayList<>();
        String query = "SELECT * FROM reservation WHERE email_client = ? ORDER BY date_reservation DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractReservationFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Ajouter une réservation
    public boolean addReservation(Reservation reservation) {
        String query = "INSERT INTO reservation (nom_client, email_client, telephone, voyage_id, date_reservation, nombre_personnes, statut, commentaire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, reservation.getNomClient());
            pstmt.setString(2, reservation.getEmailClient());
            pstmt.setString(3, reservation.getTelephone());
            pstmt.setInt(4, reservation.getVoyageId());
            pstmt.setDate(5, Date.valueOf(reservation.getDateReservation()));
            pstmt.setInt(6, reservation.getNombrePersonnes());
            pstmt.setString(7, reservation.getStatut());
            pstmt.setString(8, reservation.getCommentaire());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mettre à jour une réservation
    public boolean updateReservation(Reservation reservation) {
        String query = "UPDATE reservation SET nom_client=?, email_client=?, telephone=?, voyage_id=?, " +
                "date_reservation=?, nombre_personnes=?, statut=?, commentaire=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, reservation.getNomClient());
            pstmt.setString(2, reservation.getEmailClient());
            pstmt.setString(3, reservation.getTelephone());
            pstmt.setInt(4, reservation.getVoyageId());
            pstmt.setDate(5, Date.valueOf(reservation.getDateReservation()));
            pstmt.setInt(6, reservation.getNombrePersonnes());
            pstmt.setString(7, reservation.getStatut());
            pstmt.setString(8, reservation.getCommentaire());
            pstmt.setInt(9, reservation.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Supprimer une réservation
    public boolean deleteReservation(int id) {
        String query = "DELETE FROM reservation WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Changer le statut d'une réservation
    public boolean updateStatut(int id, String nouveauStatut) {
        String query = "UPDATE reservation SET statut = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Récupérer les réservations par statut
    public List<Reservation> getReservationsByStatut(String statut) {
        List<Reservation> list = new ArrayList<>();
        String query = "SELECT * FROM reservation WHERE statut = ? ORDER BY date_reservation DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractReservationFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Récupérer les réservations par voyage
    public List<Reservation> getReservationsByVoyage(int voyageId) {
        List<Reservation> list = new ArrayList<>();
        String query = "SELECT * FROM reservation WHERE voyage_id = ? ORDER BY date_reservation DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, voyageId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractReservationFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Méthode utilitaire pour extraire une réservation du ResultSet
    private Reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setNomClient(rs.getString("nom_client"));
        r.setEmailClient(rs.getString("email_client"));
        r.setTelephone(rs.getString("telephone"));
        r.setVoyageId(rs.getInt("voyage_id"));

        Date dateSql = rs.getDate("date_reservation");
        if (dateSql != null) {
            r.setDateReservation(dateSql.toLocalDate());
        }

        r.setNombrePersonnes(rs.getInt("nombre_personnes"));
        r.setStatut(rs.getString("statut"));
        r.setCommentaire(rs.getString("commentaire"));
        return r;
    }
}