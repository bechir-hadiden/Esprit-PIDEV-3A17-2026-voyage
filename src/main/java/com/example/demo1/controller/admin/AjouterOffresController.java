package com.example.demo1.controller.admin;

import com.example.demo1.entity.Offre;
import com.example.demo1.services.OffreService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.example.demo1.Utils.Database;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AjouterOffresController {

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtRemise;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;

    // Les deux ComboBox dynamiques
    @FXML private ComboBox<String> cbCategorie;
    @FXML private ComboBox<String> cbItem;

    private OffreService os = new OffreService();

    @FXML
    public void initialize() {
        // 1. Remplir la liste des catégories
        cbCategorie.getItems().addAll("VOYAGE", "HOTEL", "VOL", "TRANSPORT");

        // 2. Écouteur : Charger les éléments dès qu'on change de catégorie
        cbCategorie.setOnAction(event -> {
            chargerItems(cbCategorie.getValue());
        });
    }

    /**
     * Charge les données depuis la table correspondante (Hôtels, Vols, etc.)
     */
    private void chargerItems(String categorie) {
        cbItem.getItems().clear();
        String query = "";

        switch (categorie) {
            case "HOTEL":
                query = "SELECT id, name FROM hotels";
                break;
            case "VOL":
                query = "SELECT id, arrivee FROM vols";
                break;
            case "TRANSPORT":
                query = "SELECT idVehicule, type, ville FROM vehicule";
                break;
            case "VOYAGE":
                query = "SELECT id_voyage, destination FROM voyage";
                break;
        }

        try (Connection conn = Database.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                if ("HOTEL".equals(categorie))
                    cbItem.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
                else if ("VOL".equals(categorie))
                    cbItem.getItems().add(rs.getLong("id") + " - Vers " + rs.getString("arrivee"));
                else if ("TRANSPORT".equals(categorie))
                    cbItem.getItems().add(rs.getInt("idVehicule") + " - " + rs.getString("type") + " (" + rs.getString("ville") + ")");
                else
                    cbItem.getItems().add(rs.getInt("id_voyage") + " - " + rs.getString("destination"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement items : " + e.getMessage());
        }
    }

    private boolean isSaisieValide() {
        String messageErreur = "";

        if (txtTitre.getText() == null || txtTitre.getText().trim().isEmpty()) {
            messageErreur += "- Le titre est obligatoire.\n";
        }
        try {
            int remise = Integer.parseInt(txtRemise.getText());
            if (remise < 0 || remise > 100) messageErreur += "- Remise entre 0 et 100%.\n";
        } catch (NumberFormatException e) {
            messageErreur += "- Remise doit être un nombre.\n";
        }
        if (dpDebut.getValue() == null || dpFin.getValue() == null) {
            messageErreur += "- Les dates sont obligatoires.\n";
        } else if (dpFin.getValue().isBefore(dpDebut.getValue())) {
            messageErreur += "- La date de fin doit être après le début.\n";
        }
        if (cbCategorie.getValue() == null) {
            messageErreur += "- Veuillez sélectionner une catégorie.\n";
        }
        if (cbItem.getValue() == null) {
            messageErreur += "- Veuillez sélectionner l'élément concerné.\n";
        }

        if (messageErreur.length() > 0) {
            showAlert("Erreur de saisie", messageErreur);
            return false;
        }
        return true;
    }

    @FXML
    private void handleEnregistrer() {
        if (isSaisieValide()) {
            try {
                // Extraction de l'ID depuis la chaîne "ID - Nom"
                String selectedItem = cbItem.getValue();
                String idPart = selectedItem.split(" - ")[0];
                String category = cbCategorie.getValue();

                // Initialisation des IDs à null
                Integer idVoyage = 1; // Valeur par défaut si NOT NULL dans ta DB
                Integer idHotel = null;
                Long idVol = null;
                Integer idVehicule = null;

                // Affectation selon la catégorie choisie
                if ("HOTEL".equals(category)) {
                    idHotel = Integer.parseInt(idPart);
                } else if ("VOL".equals(category)) {
                    idVol = Long.parseLong(idPart);
                } else if ("TRANSPORT".equals(category)) {
                    idVehicule = Integer.parseInt(idPart);
                } else {
                    idVoyage = Integer.parseInt(idPart);
                }

                Offre nouvelleOffre = new Offre(
                        txtTitre.getText(),
                        txtDescription.getText(),
                        Integer.parseInt(txtRemise.getText()),
                        Date.valueOf(dpDebut.getValue()),
                        Date.valueOf(dpFin.getValue()),
                        "ACTIVE",
                        idVoyage,
                        idHotel,
                        idVol,
                        idVehicule,
                        category,
                        false, // is_local_support par défaut
                        "default.jpg"
                );

                os.ajouter(nouvelleOffre);
                handleAnnuler();

            } catch (SQLException e) {
                showAlert("Erreur BDD", "Erreur lors de l'enregistrement : " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler() {
        txtTitre.getScene().getWindow().hide();
    }
}