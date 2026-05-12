package com.example.demo1.services;

import com.example.demo1.entity.Offre;
import com.example.demo1.Utils.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffreService implements IService<Offre> {

    public OffreService() {
        // Constructeur vide, on ne stocke plus la connexion ici
    }

    @Override
    public void ajouter(Offre o) throws SQLException {
        // On récupère la connexion active du Singleton
        Connection conn = Database.getInstance().getConnection();

        String req = "INSERT INTO offre (titre, description, taux_remise, date_debut, date_fin, statut, " +
                "id_voyage, id_hotel, id_vol, id_vehicule, category, is_local_support, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, o.getTitre());
            ps.setString(2, o.getDescription());
            ps.setInt(3, o.getTaux_remise());
            ps.setDate(4, o.getDate_debut());
            ps.setDate(5, o.getDate_fin());
            ps.setString(6, o.getStatut());
            ps.setObject(7, o.getId_voyage(), Types.INTEGER);
            ps.setObject(8, o.getId_hotel(), Types.INTEGER);
            ps.setObject(9, o.getId_vol(), Types.BIGINT);
            ps.setObject(10, o.getId_vehicule(), Types.INTEGER);
            ps.setString(11, o.getCategory());
            ps.setBoolean(12, o.isIs_local_support());
            ps.setString(13, o.getImage_url());

            ps.executeUpdate();
            System.out.println("✅ Offre ajoutée avec succès !");
        }
    }

    @Override
    public List<Offre> afficher() throws SQLException {
        List<Offre> offres = new ArrayList<>();
        Connection conn = com.example.demo1.Utils.Database.getInstance().getConnection();

        // VÉRIFIE BIEN CHAQUE LIGNE DE CETTE REQUÊTE
        String req = "SELECT o.*, " +
                "v.destination as dest_v, " +
                "h.name as name_h, h.price_per_night as prix_h, hi.image_url as hotel_web_img, " +
                "vol.arrivee as dest_vol, vol.prix as prix_vol, vol.image_url as vol_img, " + // <-- vol_img ajouté ici
                "veh.type as type_veh, veh.ville as ville_veh, veh.prix as prix_veh, veh.image as veh_img " + // <-- veh_img ajouté ici
                "FROM offre o " +
                "LEFT JOIN voyages v ON o.id_voyage = v.id " +
                "LEFT JOIN hotels h ON o.id_hotel = h.id " +
                "LEFT JOIN hotel_images hi ON h.id = hi.hotel_id AND hi.display_order = 1 " +
                "LEFT JOIN vols vol ON o.id_vol = vol.id " +
                "LEFT JOIN vehicule veh ON o.id_vehicule = veh.idVehicule";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Offre o = new Offre(
                        rs.getInt("id_offre"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getInt("taux_remise"),
                        rs.getDate("date_debut"),
                        rs.getDate("date_fin"),
                        rs.getString("statut"),
                        rs.getInt("id_voyage"),
                        (Integer) rs.getObject("id_hotel"),
                        (Long) rs.getObject("id_vol"),
                        (Integer) rs.getObject("id_vehicule"),
                        rs.getString("category"),
                        rs.getBoolean("is_local_support"),
                        rs.getString("image_url")
                );

                String cat = o.getCategory();
                if ("HOTEL".equals(cat)) {
                    o.setDestination(rs.getString("name_h"));
                    o.setPrix_initial(rs.getDouble("prix_h"));
                    if (rs.getString("hotel_web_img") != null) o.setImage_url(rs.getString("hotel_web_img"));
                }
                else if ("VOL".equals(cat)) {
                    o.setDestination("Vers " + rs.getString("dest_vol"));
                    o.setPrix_initial(rs.getDouble("prix_vol"));
                    // RÉCUPÉRATION DE L'IMAGE DU VOL
                    o.setImage_url(rs.getString("vol_img"));
                }
                else if ("TRANSPORT".equals(cat)) {
                    o.setDestination(rs.getString("type_veh") + " (" + rs.getString("ville_veh") + ")");
                    o.setPrix_initial(rs.getDouble("prix_veh"));
                    // RÉCUPÉRATION DE L'IMAGE DU VÉHICULE
                    o.setImage_url(rs.getString("veh_img"));
                }
                else {
                    o.setDestination(rs.getString("dest_v"));
                    o.setPrix_initial(100.0);
                }

                offres.add(o);
            }
        }
        return offres;
    }


    @Override
    public void modifier(Offre o) throws SQLException {
        Connection conn = Database.getInstance().getConnection();

        String req = "UPDATE offre SET titre=?, description=?, taux_remise=?, date_debut=?, date_fin=?, " +
                "statut=?, id_voyage=?, id_hotel=?, id_vol=?, id_vehicule=?, category=?, " +
                "is_local_support=?, image_url=? WHERE id_offre=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, o.getTitre());
            ps.setString(2, o.getDescription());
            ps.setInt(3, o.getTaux_remise());
            ps.setDate(4, o.getDate_debut());
            ps.setDate(5, o.getDate_fin());
            ps.setString(6, o.getStatut());
            ps.setInt(7, o.getId_voyage());
            ps.setObject(8, o.getId_hotel(), Types.INTEGER);
            ps.setObject(9, o.getId_vol(), Types.BIGINT);
            ps.setObject(10, o.getId_vehicule(), Types.INTEGER);
            ps.setString(11, o.getCategory());
            ps.setBoolean(12, o.isIs_local_support());
            ps.setString(13, o.getImage_url());
            ps.setInt(14, o.getId_offre());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Offre ID " + o.getId_offre() + " modifiée avec succès !");
            }
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        Connection conn = Database.getInstance().getConnection();
        String req = "DELETE FROM offre WHERE id_offre=?";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Offre ID " + id + " supprimée.");
        }
    }
}