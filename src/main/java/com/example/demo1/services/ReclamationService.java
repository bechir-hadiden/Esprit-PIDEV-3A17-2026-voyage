package com.example.demo1.services;

import com.example.demo1.Utils.Database;
import com.example.demo1.entity.Reclamation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {

    private Connection connection;

    public ReclamationService() {
        this.connection = Database.getInstance().getConnection();
    }

    /**
     * Ajouter une nouvelle réclamation dans la base de données
     */
    public boolean ajouterReclamation(Reclamation reclamation) {
        String query = "INSERT INTO reclamations (nom, email, telephone, message, date_creation, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, reclamation.getNom());
            ps.setString(2, reclamation.getEmail());
            ps.setString(3, reclamation.getTelephone());
            ps.setString(4, reclamation.getMessage());
            ps.setTimestamp(5, Timestamp.valueOf(reclamation.getDateCreation()));
            ps.setString(6, reclamation.getStatut());

            int rowsAffected = ps.executeUpdate();

            // Récupérer l'ID généré
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    reclamation.setId(rs.getInt(1));
                }
                System.out.println("✅ Réclamation ajoutée avec succès (ID: " + reclamation.getId() + ")");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la réclamation : " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Récupérer toutes les réclamations
     */
    public List<Reclamation> getAllReclamations() {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT * FROM reclamations ORDER BY date_creation DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id"));
                reclamation.setNom(rs.getString("nom"));
                reclamation.setEmail(rs.getString("email"));
                reclamation.setTelephone(rs.getString("telephone"));
                reclamation.setMessage(rs.getString("message"));
                reclamation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                reclamation.setStatut(rs.getString("statut"));

                reclamations.add(reclamation);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des réclamations : " + e.getMessage());
            e.printStackTrace();
        }

        return reclamations;
    }

    /**
     * Récupérer une réclamation par ID
     */
    public Reclamation getReclamationById(int id) {
        String query = "SELECT * FROM reclamations WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id"));
                reclamation.setNom(rs.getString("nom"));
                reclamation.setEmail(rs.getString("email"));
                reclamation.setTelephone(rs.getString("telephone"));
                reclamation.setMessage(rs.getString("message"));
                reclamation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                reclamation.setStatut(rs.getString("statut"));

                return reclamation;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de la réclamation : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Mettre à jour le statut d'une réclamation
     */
    public boolean updateStatut(int id, String nouveauStatut) {
        String query = "UPDATE reclamations SET statut = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, nouveauStatut);
            ps.setInt(2, id);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Statut mis à jour avec succès");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut : " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Supprimer une réclamation
     */
    public boolean supprimerReclamation(int id) {
        String query = "DELETE FROM reclamations WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Réclamation supprimée avec succès");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la réclamation : " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Compter le nombre de réclamations par statut
     */
    public int countByStatut(String statut) {
        String query = "SELECT COUNT(*) FROM reclamations WHERE statut = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage : " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }
}
