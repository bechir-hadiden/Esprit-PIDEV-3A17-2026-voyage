package org.example.services;

import org.example.entities.*;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Service for reservation management with new 2-level architecture. */
public class ReservationService {

    public boolean ajouter(Reservation r) {
        // Try with new columns first. If idTransport fails due to FK, we still have a
        // problem,
        // so we must ensure idTransport is handled as NULL if not applicable.
        String sql = "INSERT INTO reservation(idUser, idTransport, typeTransport, idVehicule, dateReservation, statut) VALUES (?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.println(
                    "DEBUG DB: Adding reservation for User=" + r.getIdUser() + " Vehicule=" + r.getIdVehicule());

            ps.setInt(1, r.getIdUser());
            // We use setNull to avoid FK constraint issues if 0 is not a valid transport ID
            if (r.getIdTransport() > 0) {
                ps.setInt(2, r.getIdTransport());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            ps.setString(3, r.getTypeTransport());
            ps.setInt(4, r.getIdVehicule());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(r.getDateReservation()));
            ps.setString(6, r.getStatut());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                // AUTOMATIC: Mark vehicle as unavailable
                new VehiculeService().updateDisponibilite(r.getIdVehicule(), false);
            }
            System.out.println("DEBUG DB: Reservation affected rows: " + affected);
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("CRITICAL DB ERROR in ajouter(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("GENERAL ERROR in ReservationService.ajouter(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Reservation> listerParUser(int idUser) {
        List<Reservation> list = new ArrayList<>();
        VehiculeService vehiculeService = new VehiculeService();

        // Query reservations for the user
        String sql = "SELECT * FROM reservation WHERE idUser = ? ORDER BY dateReservation DESC";

        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Reservation res = new Reservation(
                        rs.getInt("idReservation"),
                        rs.getInt("idUser"),
                        rs.getInt("idTransport"),
                        rs.getTimestamp("dateReservation").toLocalDateTime(),
                        rs.getString("statut"));

                // Get typeTransport and idVehicule from new columns
                String typeTransport = rs.getString("typeTransport");
                int idVehicule = rs.getInt("idVehicule");

                res.setTypeTransport(typeTransport);
                res.setIdVehicule(idVehicule);

                // Fetch vehicle details from appropriate table
                if (typeTransport != null && idVehicule > 0) {
                    BaseVehicule vehicule = vehiculeService.getById(typeTransport, idVehicule);
                    if (vehicule != null) {
                        res.setTransportType(vehicule.getType());
                        res.setTransportCompagnie(vehicule.getCompagnie());
                        res.setTransportNumero(vehicule.getNumero());
                        res.setTransportCapacite(vehicule.getCapacite());
                        res.setTransportPrix(vehicule.getPrix());
                    }
                }

                list.add(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void modifier(Reservation r) {
        String sql = "UPDATE reservation SET dateReservation = ?, statut = ? WHERE idReservation = ?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(r.getDateReservation()));
            ps.setString(2, r.getStatut());
            ps.setInt(3, r.getIdReservation());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int idReservation) {
        String selectSql = "SELECT idVehicule FROM reservation WHERE idReservation = ?";
        String deleteSql = "DELETE FROM reservation WHERE idReservation = ?";
        try (Connection con = DatabaseConnection.getConnection()) {
            int idVehicule = -1;
            try (PreparedStatement psSel = con.prepareStatement(selectSql)) {
                psSel.setInt(1, idReservation);
                ResultSet rs = psSel.executeQuery();
                if (rs.next())
                    idVehicule = rs.getInt("idVehicule");
            }
            try (PreparedStatement psDel = con.prepareStatement(deleteSql)) {
                psDel.setInt(1, idReservation);
                if (psDel.executeUpdate() > 0 && idVehicule > 0) {
                    new VehiculeService().updateDisponibilite(idVehicule, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Reservation> listerToutes() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation ORDER BY dateReservation DESC";
        try (Connection con = DatabaseConnection.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Reservation res = new Reservation(
                        rs.getInt("idReservation"),
                        rs.getInt("idUser"),
                        rs.getInt("idTransport"),
                        rs.getTimestamp("dateReservation").toLocalDateTime(),
                        rs.getString("statut"));
                res.setTypeTransport(rs.getString("typeTransport"));
                res.setIdVehicule(rs.getInt("idVehicule"));
                list.add(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
