package com.example.demo1.services;

import com.example.demo1.entity.CodePromo;
import com.example.demo1.Utils.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CodePromoService implements IService<CodePromo> {
    private Connection connection;

    public CodePromoService() {
        connection = Database.getInstance().getConnection();
    }

    @Override
    public void ajouter(CodePromo cp) throws SQLException {
        String req = "INSERT INTO code_promo (code_texte, date_expiration, id_offre) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, cp.getCode_texte());
        ps.setDate(2, cp.getDate_expiration());
        ps.setInt(3, cp.getId_offre());
        ps.executeUpdate();
    }

    @Override
    public List<CodePromo> afficher() throws SQLException {
        List<CodePromo> codes = new ArrayList<>();
        String req = "SELECT * FROM code_promo";
        ResultSet rs = connection.createStatement().executeQuery(req);
        while (rs.next()) {
            codes.add(new CodePromo(
                    rs.getInt("id_code"),
                    rs.getString("code_texte"),
                    rs.getDate("date_expiration"),
                    rs.getInt("id_offre")
            ));
        }
        return codes;
    }
    @Override
    public void modifier(CodePromo cp) throws SQLException {
        String req = "UPDATE code_promo SET code_texte = ?, date_expiration = ?, id_offre = ? WHERE id_code = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, cp.getCode_texte());
        ps.setDate(2, cp.getDate_expiration());
        ps.setInt(3, cp.getId_offre());
        ps.setInt(4, cp.getId_code());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM code_promo WHERE id_code = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // --- LOGIQUE MÉTIER (Indispensable pour demain) ---
    public String genererCodeAutomatique() {
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int index = (int)(alphaNumericString.length() * Math.random());
            sb.append(alphaNumericString.charAt(index));
        }
        return "SMART-" + sb.toString(); // Ex: SMART-A5Z8R2Q1
    }
}