package org.example.services;

import org.example.entities.Profession;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessionService {

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS profession (" +
                "idProfession INT AUTO_INCREMENT PRIMARY KEY," +
                "titre VARCHAR(255) NOT NULL," +
                "description TEXT" +
                ")";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void seedProfessions() {
        createTable();
        String checkSql = "SELECT COUNT(*) FROM profession";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO profession (titre, description) VALUES (?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    // Smart Mobility Manager
                    ps.setString(1, "Responsable mobilité intelligente (Smart Mobility Manager)");
                    ps.setString(2, "➤ Gestion des systèmes de transport\n" +
                            "➤ Optimisation des trajets\n" +
                            "➤ Intégration GPS, trafic en temps réel, IA\n" +
                            "➤ Coordination des différents moyens de transport (bus, train, avion, VTC, location)");
                    ps.executeUpdate();

                    // Digital Transport Project Manager
                    ps.setString(1, "Chef de projet transport digital");
                    ps.setString(2, "➤ Pilotage du module transport de la plateforme\n" +
                            "➤ Organisation des fonctionnalités transport\n" +
                            "➤ Coordination technique et partenaires\n" +
                            "➤ Développement de solutions de transport intelligentes et connectées");
                    ps.executeUpdate();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Profession> lister() {
        List<Profession> list = new ArrayList<>();
        String sql = "SELECT * FROM profession";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Profession(
                        rs.getInt("idProfession"),
                        rs.getString("titre"),
                        rs.getString("description")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
