package com.example.demo1.services;

import com.example.demo1.Utils.Database;
import com.example.demo1.entity.Destination;
import com.example.demo1.entity.Voyage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoyageServices {

    private Connection connection;

    public VoyageServices() {
        connection = Database.getInstance().getConnection();
    }

    // ============================================
    // 📋 RÉCUPÉRER TOUS LES VOYAGES
    // ============================================
    public List<Voyage> getAllVoyages() {
        List<Voyage> voyages = new ArrayList<>();
        String query = "SELECT v.*, d.nom as dest_nom, d.pays as dest_pays, " +
                "d.image_url as dest_image, d.code_iata as dest_iata " +
                "FROM voyages v " +
                "LEFT JOIN destination d ON v.destination_id = d.id " +
                "ORDER BY v.dateDebut";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Voyage voyage = new Voyage();
                voyage.setId(rs.getInt("id"));
                voyage.setDestination(rs.getString("destination"));
                voyage.setDateDebut(rs.getDate("dateDebut").toLocalDate());
                voyage.setDateFin(rs.getDate("dateFin").toLocalDate());
                voyage.setPrix(rs.getDouble("prix"));
                voyage.setImagePath(rs.getString("imagePath"));
                voyage.setDescription(rs.getString("description"));
                voyage.setDestinationId(rs.getInt("destination_id"));

                // ✅ Pays de départ
                voyage.setPaysDepart(rs.getString("pays_depart"));

                // ✅ Charger l'objet Destination lié
                int destId = rs.getInt("destination_id");
                if (destId > 0) {
                    Destination dest = new Destination();
                    dest.setId(destId);
                    dest.setNom(rs.getString("dest_nom"));
                    dest.setPays(rs.getString("dest_pays"));
                    dest.setImageUrl(rs.getString("dest_image"));
                    dest.setCodeIata(rs.getString("dest_iata"));
                    voyage.setDestinationObj(dest);
                }

                voyages.add(voyage);
            }
            System.out.println("✅ Total voyages: " + voyages.size());

        } catch (SQLException e) {
            System.err.println("❌ Erreur getAllVoyages: " + e.getMessage());
        }
        return voyages;
    }

    // ============================================
    // ➕ AJOUTER UN VOYAGE
    // ============================================
    public boolean addVoyage(Voyage voyage) {
        String query = "INSERT INTO voyages (destination, dateDebut, dateFin, " +
                "prix, imagePath, description, destination_id, pays_depart) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, voyage.getDestination());
            pstmt.setDate(2, Date.valueOf(voyage.getDateDebut()));
            pstmt.setDate(3, Date.valueOf(voyage.getDateFin()));
            pstmt.setDouble(4, voyage.getPrix());
            pstmt.setString(5, voyage.getImagePath());
            pstmt.setString(6, voyage.getDescription());

            if (voyage.getDestinationId() > 0) {
                pstmt.setInt(7, voyage.getDestinationId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            // ✅ Pays de départ
            pstmt.setString(8, voyage.getPaysDepart());

            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("✅ Voyage ajouté: " + voyage.getDestination());
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur addVoyage: " + e.getMessage());
            return false;
        }
    }

    // ============================================
    // ✏️ MODIFIER UN VOYAGE
    // ============================================
    public boolean updateVoyage(Voyage voyage) {
        String query = "UPDATE voyages SET destination=?, dateDebut=?, dateFin=?, " +
                "prix=?, imagePath=?, description=?, destination_id=?, " +
                "pays_depart=? WHERE id=?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, voyage.getDestination());
            pstmt.setDate(2, Date.valueOf(voyage.getDateDebut()));
            pstmt.setDate(3, Date.valueOf(voyage.getDateFin()));
            pstmt.setDouble(4, voyage.getPrix());
            pstmt.setString(5, voyage.getImagePath());
            pstmt.setString(6, voyage.getDescription());

            if (voyage.getDestinationId() > 0) {
                pstmt.setInt(7, voyage.getDestinationId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            // ✅ Pays de départ
            pstmt.setString(8, voyage.getPaysDepart());
            pstmt.setInt(9, voyage.getId());

            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("✅ Voyage modifié: " + voyage.getDestination());
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur updateVoyage: " + e.getMessage());
            return false;
        }
    }

    // ============================================
    // 🗑️ SUPPRIMER UN VOYAGE
    // ============================================
    public boolean deleteVoyage(int id) {
        String query = "DELETE FROM voyages WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("✅ Voyage supprimé: ID " + id);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur deleteVoyage: " + e.getMessage());
            return false;
        }
    }
}