package com.example.demo1.controller.admin;

import com.example.demo1.entity.Offre;
import com.example.demo1.services.OffreService;
import com.example.demo1.Utils.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class ModifierOffreController {
    @FXML private TextField txtTitre, txtRemise;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dpDebut, dpFin;
    @FXML private ComboBox<String> cbCategorie, cbItem;

    private OffreService os = new OffreService();
    private int idOffreActuelle;
    private String currentImageUrl;
    private boolean currentLocalSupport;

    @FXML
    public void initialize() {
        cbCategorie.getItems().addAll("VOYAGE", "HOTEL", "VOL", "TRANSPORT");
        cbCategorie.setOnAction(event -> chargerItems(cbCategorie.getValue(), null));
    }

    private void chargerItems(String categorie, Integer idToSelect) {
        cbItem.getItems().clear();
        String query = "";
        switch (categorie) {
            case "HOTEL": query = "SELECT id, name FROM hotels"; break;
            case "VOL": query = "SELECT id, arrivee FROM vols"; break;
            case "TRANSPORT": query = "SELECT idVehicule, type, ville FROM vehicule"; break;
            default: query = "SELECT id_voyage, destination FROM voyage"; break;
        }

        try (Connection conn = Database.getInstance().getConnection();
             ResultSet rs = conn.createStatement().executeQuery(query)) {
            while (rs.next()) {
                String itemText = "";
                int id = 0;
                if ("HOTEL".equals(categorie)) {
                    id = rs.getInt("id");
                    itemText = id + " - " + rs.getString("name");
                } else if ("TRANSPORT".equals(categorie)) {
                    id = rs.getInt("idVehicule");
                    itemText = id + " - " + rs.getString("type") + " (" + rs.getString("ville") + ")";
                } else if ("VOL".equals(categorie)) {
                    id = rs.getInt("id");
                    itemText = id + " - Vers " + rs.getString("arrivee");
                } else {
                    id = rs.getInt("id_voyage");
                    itemText = id + " - " + rs.getString("destination");
                }
                cbItem.getItems().add(itemText);
                if (idToSelect != null && id == idToSelect) cbItem.setValue(itemText);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void setData(Offre o) {
        this.idOffreActuelle = o.getId_offre();
        txtTitre.setText(o.getTitre());
        txtDescription.setText(o.getDescription());
        txtRemise.setText(String.valueOf(o.getTaux_remise()));
        if (o.getDate_debut() != null) {
            dpDebut.setValue(o.getDate_debut().toLocalDate());
        } else {
            dpDebut.setValue(null); // Ou une date par défaut
        }

        if (o.getDate_fin() != null) {
            dpFin.setValue(o.getDate_fin().toLocalDate());
        } else {
            dpFin.setValue(null);
        }
        this.currentImageUrl = o.getImage_url();
        this.currentLocalSupport = o.isIs_local_support();
        cbCategorie.setValue(o.getCategory());

        Integer idTarget = o.getId_voyage();
        if ("HOTEL".equals(o.getCategory())) idTarget = o.getId_hotel();
        if ("TRANSPORT".equals(o.getCategory())) idTarget = o.getId_vehicule();
        if ("VOL".equals(o.getCategory())) idTarget = (o.getId_vol() != null) ? o.getId_vol().intValue() : null;

        chargerItems(o.getCategory(), idTarget);
    }

    @FXML
    private void handleEnregistrer() {
        try {
            if (cbItem.getValue() == null) {
                System.err.println("Erreur : Aucun élément sélectionné.");
                return;
            }

            int idPart = Integer.parseInt(cbItem.getValue().split(" - ")[0]);
            String category = cbCategorie.getValue();
            Integer idV = 1, idH = null, idVeh = null;
            Long idVol = null;

            if ("HOTEL".equals(category)) idH = idPart;
            else if ("VOL".equals(category)) idVol = (long) idPart;
            else if ("TRANSPORT".equals(category)) idVeh = idPart;
            else idV = idPart;

            // ON APPELLE LE CONSTRUCTEUR À 14 PARAMÈTRES
            Offre o = new Offre(
                    idOffreActuelle, txtTitre.getText(), txtDescription.getText(),
                    Integer.parseInt(txtRemise.getText()),
                    Date.valueOf(dpDebut.getValue()), Date.valueOf(dpFin.getValue()),
                    "ACTIVE", idV, idH, idVol, idVeh, category,
                    currentLocalSupport, currentImageUrl
            );

            os.modifier(o);
            handleAnnuler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleAnnuler() { txtTitre.getScene().getWindow().hide(); }
}