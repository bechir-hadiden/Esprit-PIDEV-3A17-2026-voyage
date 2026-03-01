package org.example.services;

import org.example.entities.*;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Generic service for all vehicle types using a single table. */
public class VehiculeService {

    public BaseVehicule getById(String type, int id) {
        String sql = "SELECT * FROM vehicule WHERE idVehicule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return mapResultSetToVehicule(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BaseVehicule listerParId(int id) {
        String sql = "SELECT * FROM vehicule WHERE idVehicule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return mapResultSetToVehicule(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<BaseVehicule> listerTous() {
        List<BaseVehicule> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicule";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToVehicule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BaseVehicule> listerParType(String type) {
        return listerParVille(type, "");
    }

    public List<BaseVehicule> listerParVille(String type, String ville) {
        List<BaseVehicule> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE (type = ? OR ? = '') AND (LOWER(ville) LIKE ? OR ? = '')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setString(2, type);
            pstmt.setString(3, "%" + ville.toLowerCase() + "%");
            pstmt.setString(4, ville);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVehicule(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void ajouter(BaseVehicule v) {
        String sql = "INSERT INTO vehicule (type, compagnie, numero, capacite, prix, disponible, image, latitude, longitude, ville) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, v.getType());
            pstmt.setString(2, v.getCompagnie());
            pstmt.setString(3, v.getNumero());
            pstmt.setInt(4, v.getCapacite());
            pstmt.setDouble(5, v.getPrix());
            pstmt.setBoolean(6, v.isDisponible());
            pstmt.setString(7, v.getImage());
            pstmt.setDouble(8, v.getLatitude());
            pstmt.setDouble(9, v.getLongitude());
            pstmt.setString(10, v.getVille());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifier(BaseVehicule v) {
        String sql = "UPDATE vehicule SET compagnie = ?, numero = ?, capacite = ?, prix = ?, disponible = ?, image = ?, latitude = ?, longitude = ?, ville = ? WHERE idVehicule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, v.getCompagnie());
            pstmt.setString(2, v.getNumero());
            pstmt.setInt(3, v.getCapacite());
            pstmt.setDouble(4, v.getPrix());
            pstmt.setBoolean(5, v.isDisponible());
            pstmt.setString(6, v.getImage());
            pstmt.setDouble(7, v.getLatitude());
            pstmt.setDouble(8, v.getLongitude());
            pstmt.setString(9, v.getVille());
            pstmt.setInt(10, v.getId());
            int rows = pstmt.executeUpdate();
            System.out.println("Modified vehicle " + v.getId() + " - rows affected: " + rows);
        } catch (Exception e) {
            System.err.println("Error modifying vehicle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void supprimer(String type, int id) {
        String sql = "DELETE FROM vehicule WHERE idVehicule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Met à jour la disponibilité d'un véhicule par son ID. */
    public void updateDisponibilite(int vehiculeId, boolean disponible) {
        String sql = "UPDATE vehicule SET disponible = ? WHERE idVehicule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, disponible);
            pstmt.setInt(2, vehiculeId);
            pstmt.executeUpdate();
            System.out.println("LOG [VehiculeService]: Vehicle " + vehiculeId + " disponibilite = " + disponible);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private BaseVehicule mapResultSetToVehicule(ResultSet rs) {
        try {
            String type = rs.getString("type");
            if (type == null)
                type = "Inconnu";
            BaseVehicule v = createInstance(type);
            v.setId(rs.getInt("idVehicule"));
            v.setType(type);
            v.setCompagnie(rs.getString("compagnie"));
            v.setNumero(rs.getString("numero"));
            v.setCapacite(rs.getInt("capacite"));
            v.setPrix(rs.getDouble("prix"));
            v.setDisponible(rs.getBoolean("disponible"));

            // Null-safe for extra fields
            try {
                v.setImage(rs.getString("image"));
            } catch (SQLException e) {
            }
            try {
                v.setVille(rs.getString("ville"));
            } catch (SQLException e) {
            }

            // Null-safe for coordinates
            try {
                v.setLatitude(rs.getDouble("latitude"));
                v.setLongitude(rs.getDouble("longitude"));
            } catch (SQLException e) {
            }

            return v;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private BaseVehicule createInstance(String type) {
        if (type == null)
            return new GenericVehicule("Inconnu");
        // We can use the concrete classes for known types,
        // and a generic one for others if we want,
        // but for now let's use a Generic class later.
        // For now, if it's unknown, we'll use a new class "GenericVehicule" or just a
        // concrete BaseVehicule.
        switch (type) {
            case "Bus":
                return new Bus();
            case "Taxi":
                return new Taxi();
            case "Voiture":
                return new Voiture();
            case "Scooter":
                return new Scooter();
            default:
                return new GenericVehicule(type);
        }
    }
}
