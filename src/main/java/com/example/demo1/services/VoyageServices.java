package com.example.demo1.services;

import com.example.demo1.Utils.Database;
import com.example.demo1.entity.Voyage;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class VoyageServices {

    private Connection connection;

    public VoyageServices() {
        connection = Database.getInstance().getConnection();
    }

    // Récupérer tous les voyages
    public List<Voyage> getAllVoyages() {
        List<Voyage> voyages = new ArrayList<>();
        String query = "SELECT * FROM voyages ORDER BY dateDebut";

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

                voyages.add(voyage);
                System.out.println("✅ Voyage chargé: " + voyage.getDestination());
            }

            System.out.println("✅ Total voyages chargés: " + voyages.size());

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des voyages");
            System.err.println("Message SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return voyages;
    }

    // Ajouter un voyage
    public boolean addVoyage(Voyage voyage) {
        String query = "INSERT INTO voyages (destination, dateDebut, dateFin, prix, imagePath, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, voyage.getDestination());
            pstmt.setDate(2, Date.valueOf(voyage.getDateDebut()));
            pstmt.setDate(3, Date.valueOf(voyage.getDateFin()));
            pstmt.setDouble(4, voyage.getPrix());
            pstmt.setString(5, voyage.getImagePath());
            pstmt.setString(6, voyage.getDescription());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Voyage ajouté avec succès: " + voyage.getDestination());
            }

            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du voyage");
            System.err.println("Message SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Supprimer un voyage
    public boolean deleteVoyage(int id) {
        String query = "DELETE FROM voyages WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Voyage supprimé: ID " + id);
            }

            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du voyage");
            System.err.println("Message SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Modifier un voyage
    public boolean updateVoyage(Voyage voyage) {
        String query = "UPDATE voyages SET destination=?, dateDebut=?, dateFin=?, " +
                "prix=?, imagePath=?, description=? WHERE id=?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, voyage.getDestination());
            pstmt.setDate(2, Date.valueOf(voyage.getDateDebut()));
            pstmt.setDate(3, Date.valueOf(voyage.getDateFin()));
            pstmt.setDouble(4, voyage.getPrix());
            pstmt.setString(5, voyage.getImagePath());
            pstmt.setString(6, voyage.getDescription());
            pstmt.setInt(7, voyage.getId());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Voyage modifié: " + voyage.getDestination());
            }

            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification du voyage");
            System.err.println("Message SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}