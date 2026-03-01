package com.example.demo1.controller.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane; // CHANGÉ
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.example.demo1.entity.Offre;
import com.example.demo1.services.OffreService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConsultationOffresController {

    // Doit correspondre à l'ID du FXML (TilePane offersContainer)
    @FXML private TilePane offersContainer;
    @FXML private TextField txtSearch;

    @FXML private Button btnAll, btnHotels, btnVols, btnTransp;

    private OffreService os = new OffreService();
    private List<Offre> toutesLesOffres = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            toutesLesOffres = os.afficher();
            applyFilters(); // Affiche tout au début
        } catch (SQLException e) {
            e.printStackTrace();
        }

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    @FXML private void filterAll() {
        updateActiveButton(btnAll);
        applyFilters();
    }

    @FXML private void filterHotels() {
        updateActiveButton(btnHotels);
        applyFilters();
    }

    @FXML private void filterVols() {
        updateActiveButton(btnVols);
        List<Offre> list = toutesLesOffres.stream()
                // Utilise equalsIgnoreCase pour éviter les erreurs de majuscules/minuscules
                .filter(o -> "VOL".equalsIgnoreCase(o.getCategory()))
                .collect(Collectors.toList());
        displayFiltered(list);
    }

    @FXML private void filterTransp() {
        updateActiveButton(btnTransp);
        applyFilters();
    }

    private void applyFilters() {
        String searchText = txtSearch.getText().toLowerCase();
        String activeCategory = getSelectedCategory();

        List<Offre> filtered = toutesLesOffres.stream()
                .filter(o -> o.getTitre().toLowerCase().contains(searchText))
                .filter(o -> activeCategory.equals("ALL") || o.getCategory().equals(activeCategory))
                .collect(Collectors.toList());

        displayFiltered(filtered);
    }

    private void displayFiltered(List<Offre> list) {
        offersContainer.getChildren().clear(); // Utilise le nouvel ID
        for (Offre o : list) {
            offersContainer.getChildren().add(createCard(o));
        }
    }

    private void updateActiveButton(Button activeBtn) {
        List<Button> buttons = List.of(btnAll, btnHotels, btnVols, btnTransp);
        buttons.forEach(b -> b.getStyleClass().remove("filter-active")); // Classe du nouveau CSS
        activeBtn.getStyleClass().add("filter-active");
    }

    private String getSelectedCategory() {
        if (btnHotels.getStyleClass().contains("filter-active")) return "HOTEL";
        if (btnVols.getStyleClass().contains("filter-active")) return "VOL";
        if (btnTransp.getStyleClass().contains("filter-active")) return "TRANSPORT";
        return "ALL";
    }

    private VBox createCard(Offre o) {
        VBox card = new VBox(15);
        card.getStyleClass().add("offer-card");
        card.setPrefWidth(240);

        // 1. IMAGE (Rounded top)
        // 1. IMAGE
        ImageView imageView = new ImageView();
        try {
            String path = o.getImage_url();
            if (path == null || path.isEmpty()) {
                path = "default.jpg";
            }

            if (path.startsWith("http")) {
                // C'est une image Web (Unsplash pour les hôtels)
                imageView.setImage(new Image(path, true)); // 'true' pour charger en arrière-plan
            } else {
                // C'est une image locale (pour tes voyages/transports)
                var imageUrl = getClass().getResource("/images/" + path);
                if (imageUrl != null) {
                    imageView.setImage(new Image(imageUrl.toExternalForm()));
                } else {
                    imageView.setImage(new Image(getClass().getResource("/images/default.jpg").toExternalForm()));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image : " + o.getTitre());
        }

// Style et dimensions (Gardé tel quel car c'était top)
        imageView.setFitWidth(240);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(false);
        Rectangle clip = new Rectangle(240, 140);
        clip.setArcWidth(32); clip.setArcHeight(32);
        imageView.setClip(clip);

        // 2. TEXT CONTENT
        VBox textContent = new VBox(8);
        textContent.setPadding(new Insets(0, 15, 0, 15));
        textContent.setAlignment(Pos.CENTER_LEFT);

        Label loc = new Label("📍 " + (o.getDestination() != null ? o.getDestination() : "Destination"));
        loc.getStyleClass().add("offer-location");

        Label title = new Label(o.getTitre());
        title.getStyleClass().add("offer-title");
        title.setWrapText(true);

        double pInitial = o.getPrix_initial();
        double pFinal = pInitial - (pInitial * o.getTaux_remise() / 100.0);

        Label priceMain = new Label(String.format("%.0f DT", pFinal));
        priceMain.getStyleClass().add("price-main");

        Label priceOld = new Label(String.format("%.0f DT", pInitial));
        priceOld.getStyleClass().add("price-old");

        HBox priceBox = new HBox(12);
        priceBox.setAlignment(Pos.BASELINE_LEFT);
        priceBox.getChildren().addAll(priceMain, priceOld);

        // BADGE ODD 8 (Soutien Local)
        if (o.isIs_local_support()) {
            Label badge = new Label("🤝 Soutien Local");
            badge.getStyleClass().add("badge-green"); // Utilise la nouvelle classe du CSS
            textContent.getChildren().add(badge);
        }

        Button btn = new Button("Voir l’offre →");
        btn.getStyleClass().add("btn-offer");

        // ACTION DU BOUTON
        btn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client/DetailOffre.fxml"));
                Parent root = loader.load();
                DetailOffreController controller = loader.getController();
                controller.setOffreData(o);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        textContent.getChildren().addAll(loc, title, priceBox, btn);
        card.getChildren().addAll(imageView, textContent);

        return card;
    }
}