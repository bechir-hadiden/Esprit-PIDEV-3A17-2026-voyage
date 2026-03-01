package org.example.services;

import org.example.entities.TransportType;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Service for TransportType (simplified). */
public class TransportTypeService {

    public List<TransportType> lister() {
        List<TransportType> types = new ArrayList<>();
        String sql = "SELECT * FROM transport_type";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String nom = rs.getString("nom");
                if (nom != null) {
                    types.add(new TransportType(
                            rs.getInt("idType"),
                            nom,
                            rs.getDouble("prix_depart"),
                            rs.getString("image")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return types;
    }

    public void ajouter(TransportType type) {
        String sql = "INSERT INTO transport_type (nom, prix_depart, image) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.getNom());
            pstmt.setDouble(2, type.getPrixDepart());
            pstmt.setString(3, type.getImage());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifier(TransportType type) {
        String sql = "UPDATE transport_type SET nom = ?, prix_depart = ?, image = ? WHERE idType = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.getNom());
            pstmt.setDouble(2, type.getPrixDepart());
            pstmt.setString(3, type.getImage());
            pstmt.setInt(4, type.getIdType());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int idType) {
        String sql = "DELETE FROM transport_type WHERE idType = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idType);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> listerNoms() {
        List<String> noms = new ArrayList<>();
        for (TransportType t : lister()) {
            noms.add(t.getNom());
        }
        return noms;
    }
}
