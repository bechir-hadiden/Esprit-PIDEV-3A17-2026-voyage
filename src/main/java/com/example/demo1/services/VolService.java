package com.example.demo1.services;

import com.example.demo1.Utils.Database;
import com.example.demo1.entity.Vol;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VolService {

    private Connection connection;

    public VolService() {
        this.connection = Database.getInstance().getConnection();
        createTableIfNotExists();
    }

    /**
     * Créer la table vols si elle n'existe pas
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS vols (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                compagnie VARCHAR(50) NOT NULL,
                depart VARCHAR(10) NOT NULL,
                arrivee VARCHAR(10) NOT NULL,
                date_depart VARCHAR(20) NOT NULL,
                date_arrivee VARCHAR(20),
                heure_depart VARCHAR(10),
                heure_arrivee VARCHAR(10),
                prix DOUBLE NOT NULL,
                devise VARCHAR(10),
                escales INT DEFAULT 0,
                duree VARCHAR(50),
                date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_route (depart, arrivee),
                INDEX idx_date (date_depart)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'vols' vérifiée/créée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur création table: " + e.getMessage());
        }
    }

    /**
     * Sauvegarder un vol dans la base de données
     */
    public boolean save(Vol vol) {
        // Vérifier si le vol existe déjà
        if (volExiste(vol)) {
            System.out.println("⚠️ Vol déjà existant (ignoré): " +
                    vol.getCompagnie() + " " + vol.getDepart() + "→" + vol.getArrivee());
            return false;
        }

        String sql = """
            INSERT INTO vols (compagnie, depart, arrivee, date_depart, 
                            date_arrivee, heure_depart, heure_arrivee, 
                            prix, devise, escales, duree)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, vol.getCompagnie());
            pstmt.setString(2, vol.getDepart());
            pstmt.setString(3, vol.getArrivee());
            pstmt.setString(4, vol.getDateDepart());
            pstmt.setString(5, vol.getDateArrivee());
            pstmt.setString(6, vol.getHeureDepart());
            pstmt.setString(7, vol.getHeureArrivee());
            pstmt.setDouble(8, vol.getPrix());
            pstmt.setString(9, vol.getDevise());
            pstmt.setInt(10, vol.getEscales());
            pstmt.setString(11, vol.getDuree());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        vol.setId(generatedKeys.getLong(1));
                    }
                }
                System.out.println("💾 Vol sauvegardé: " + vol.getCompagnie() +
                        " - " + vol.getPrix() + " " + vol.getDevise());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur sauvegarde vol: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Sauvegarder plusieurs vols
     */
    public int saveAll(List<Vol> vols) {
        int count = 0;
        for (Vol vol : vols) {
            if (save(vol)) {
                count++;
            }
        }
        System.out.println("💾 Total: " + count + " vols sauvegardés sur " + vols.size());
        return count;
    }

    /**
     * Vérifier si un vol existe déjà
     */
    private boolean volExiste(Vol vol) {
        String sql = """
            SELECT COUNT(*) FROM vols 
            WHERE compagnie = ? AND depart = ? AND arrivee = ? 
            AND date_depart = ? AND heure_depart = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vol.getCompagnie());
            pstmt.setString(2, vol.getDepart());
            pstmt.setString(3, vol.getArrivee());
            pstmt.setString(4, vol.getDateDepart());
            pstmt.setString(5, vol.getHeureDepart());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification vol: " + e.getMessage());
        }

        return false;
    }

    /**
     * Récupérer tous les vols
     */
    public List<Vol> getAllVols() {
        List<Vol> vols = new ArrayList<>();
        String sql = "SELECT * FROM vols ORDER BY date_depart DESC, heure_depart ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vol vol = mapResultSetToVol(rs);
                vols.add(vol);
            }

            System.out.println("📊 " + vols.size() + " vols récupérés de la base");

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération vols: " + e.getMessage());
        }

        return vols;
    }

    /**
     * Rechercher les vols par depart et arrivee
     */
    public List<Vol> getVolsByRoute(String depart, String arrivee) {
        List<Vol> vols = new ArrayList<>();
        String sql = """
            SELECT * FROM vols 
            WHERE depart = ? AND arrivee = ?
            ORDER BY date_depart DESC, prix ASC
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, depart);
            pstmt.setString(2, arrivee);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vols.add(mapResultSetToVol(rs));
            }

            System.out.println("📊 " + vols.size() + " vols trouvés pour " + depart + " → " + arrivee);

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche vols: " + e.getMessage());
        }

        return vols;
    }

    /**
     * Rechercher les vols par date
     */
    public List<Vol> getVolsByDate(String date) {
        List<Vol> vols = new ArrayList<>();
        String sql = "SELECT * FROM vols WHERE date_depart = ? ORDER BY heure_depart ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vols.add(mapResultSetToVol(rs));
            }

            System.out.println("📊 " + vols.size() + " vols trouvés pour le " + date);

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche vols: " + e.getMessage());
        }

        return vols;
    }

    /**
     * Rechercher les vols par compagnie
     */
    public List<Vol> getVolsByCompagnie(String compagnie) {
        List<Vol> vols = new ArrayList<>();
        String sql = "SELECT * FROM vols WHERE compagnie = ? ORDER BY date_depart DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, compagnie);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vols.add(mapResultSetToVol(rs));
            }

            System.out.println("📊 " + vols.size() + " vols trouvés pour " + compagnie);

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche vols: " + e.getMessage());
        }

        return vols;
    }

    /**
     * Récupérer un vol par ID
     */
    public Vol getVolById(Long id) {
        String sql = "SELECT * FROM vols WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToVol(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération vol: " + e.getMessage());
        }

        return null;
    }

    /**
     * Mettre à jour un vol
     */
    public boolean update(Vol vol) {
        String sql = """
            UPDATE vols SET 
                compagnie = ?, depart = ?, arrivee = ?,
                date_depart = ?, date_arrivee = ?,
                heure_depart = ?, heure_arrivee = ?,
                prix = ?, devise = ?, escales = ?, duree = ?
            WHERE id = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vol.getCompagnie());
            pstmt.setString(2, vol.getDepart());
            pstmt.setString(3, vol.getArrivee());
            pstmt.setString(4, vol.getDateDepart());
            pstmt.setString(5, vol.getDateArrivee());
            pstmt.setString(6, vol.getHeureDepart());
            pstmt.setString(7, vol.getHeureArrivee());
            pstmt.setDouble(8, vol.getPrix());
            pstmt.setString(9, vol.getDevise());
            pstmt.setInt(10, vol.getEscales());
            pstmt.setString(11, vol.getDuree());
            pstmt.setLong(12, vol.getId());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ Vol mis à jour: ID " + vol.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour vol: " + e.getMessage());
        }

        return false;
    }

    /**
     * Supprimer un vol par ID
     */
    public boolean delete(Long id) {
        String sql = "DELETE FROM vols WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            int deleted = pstmt.executeUpdate();
            if (deleted > 0) {
                System.out.println("🗑️ Vol supprimé: ID " + id);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression vol: " + e.getMessage());
        }

        return false;
    }

    /**
     * Supprimer les anciens vols
     */
    public int deleteOldFlights(int joursAvant) {
        LocalDate dateLimite = LocalDate.now().minusDays(joursAvant);
        String dateStr = dateLimite.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String sql = "DELETE FROM vols WHERE date_depart < ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, dateStr);
            int deleted = pstmt.executeUpdate();
            System.out.println("🧹 " + deleted + " anciens vols supprimés (avant " + dateStr + ")");
            return deleted;
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression vols: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Supprimer tous les vols
     */
    public int deleteAll() {
        String sql = "DELETE FROM vols";

        try (Statement stmt = connection.createStatement()) {
            int deleted = stmt.executeUpdate(sql);
            System.out.println("🧹 " + deleted + " vols supprimés (TOUT)");
            return deleted;
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression tous vols: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Compter le nombre total de vols
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM vols";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage vols: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Compter les vols par destination
     */
    public int countByDestination(String arrivee) {
        String sql = "SELECT COUNT(*) FROM vols WHERE arrivee = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, arrivee);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage vols: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Obtenir les statistiques des vols
     */
    public void printStatistics() {
        System.out.println("\n========================================");
        System.out.println("📊 STATISTIQUES DES VOLS");
        System.out.println("========================================");
        System.out.println("Total vols en base: " + count());

        // Prix moyen
        String sqlAvg = "SELECT AVG(prix) as prix_moyen FROM vols";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sqlAvg)) {
            if (rs.next()) {
                System.out.printf("Prix moyen: %.2f EUR%n", rs.getDouble("prix_moyen"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Destinations les plus fréquentes
        String sqlDest = "SELECT arrivee, COUNT(*) as nb FROM vols GROUP BY arrivee ORDER BY nb DESC LIMIT 5";
        System.out.println("\nTop 5 destinations:");
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sqlDest)) {
            int rank = 1;
            while (rs.next()) {
                System.out.println("  " + rank + ". " + rs.getString("arrivee") +
                        " (" + rs.getInt("nb") + " vols)");
                rank++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("========================================\n");
    }

    /**
     * Mapper un ResultSet vers un objet Vol
     */
    private Vol mapResultSetToVol(ResultSet rs) throws SQLException {
        Vol vol = new Vol();
        vol.setId(rs.getLong("id"));
        vol.setCompagnie(rs.getString("compagnie"));
        vol.setDepart(rs.getString("depart"));
        vol.setArrivee(rs.getString("arrivee"));
        vol.setDateDepart(rs.getString("date_depart"));
        vol.setDateArrivee(rs.getString("date_arrivee"));
        vol.setHeureDepart(rs.getString("heure_depart"));
        vol.setHeureArrivee(rs.getString("heure_arrivee"));
        vol.setPrix(rs.getDouble("prix"));
        vol.setDevise(rs.getString("devise"));
        vol.setEscales(rs.getInt("escales"));
        vol.setDuree(rs.getString("duree"));

        return vol;
    }
}