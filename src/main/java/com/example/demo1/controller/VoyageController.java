package com.example.demo1.controller;

import com.example.demo1.entity.Voyage;
import com.example.demo1.services.VoyageServices;
import com.example.demo1.services.WeatherService;
import com.example.demo1.services.TripMapService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VoyageController {

    @FXML private VBox vboxVoyages;
    @FXML private ComboBox<String> cbFiltre;
    @FXML private TextField tfRecherche;
    @FXML private Label lblCount;

    private VoyageServices voyageService;
    private final WeatherService weatherService = new WeatherService();
    private final TripMapService tripMapService  = new TripMapService();
    private List<Voyage> tousLesVoyages;

    // ============================================
    // 🚀 INITIALISATION
    // ============================================
    @FXML
    public void initialize() {
        voyageService = new VoyageServices();
        if (cbFiltre != null) {
            cbFiltre.setItems(FXCollections.observableArrayList(
                    "Tous", "Prix croissant", "Prix décroissant"));
            cbFiltre.setOnAction(e -> appliquerFiltre());
        }
        chargerVoyages();
    }

    // ============================================
    // 📋 CHARGER LES VOYAGES
    // ============================================
    private void chargerVoyages() {
        if (vboxVoyages != null) {
            vboxVoyages.getChildren().clear();
            Label chargement = new Label("⏳ Chargement des voyages...");
            chargement.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 40;");
            vboxVoyages.getChildren().add(chargement);
        }

        Task<List<Voyage>> task = new Task<>() {
            @Override protected List<Voyage> call() {
                return voyageService.getAllVoyages();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            tousLesVoyages = task.getValue();
            afficherVoyages(tousLesVoyages);
            if (lblCount != null)
                lblCount.setText(tousLesVoyages.size() + " voyage(s)");
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            vboxVoyages.getChildren().clear();
            Label erreur = new Label("❌ Erreur lors du chargement");
            erreur.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-padding: 40;");
            vboxVoyages.getChildren().add(erreur);
        }));

        new Thread(task).start();
    }

    // ============================================
    // 🗺️ AFFICHER 3 VOYAGES PAR LIGNE
    // ============================================
    private void afficherVoyages(List<Voyage> voyages) {
        vboxVoyages.getChildren().clear();
        if (voyages.isEmpty()) {
            Label aucun = new Label("😕 Aucun voyage trouvé");
            aucun.setStyle("-fx-font-size: 18px; -fx-text-fill: #999; -fx-padding: 50;");
            vboxVoyages.getChildren().add(aucun);
            return;
        }

        HBox ligne = null;
        for (int i = 0; i < voyages.size(); i++) {
            if (i % 3 == 0) {
                ligne = new HBox(12);
                ligne.setPadding(new Insets(0, 0, 12, 0));
                ligne.setFillHeight(true);
                vboxVoyages.getChildren().add(ligne);
            }
            VBox carte = creerCarteVoyage(voyages.get(i));
            HBox.setHgrow(carte, Priority.ALWAYS);
            carte.setMaxWidth(Double.MAX_VALUE);
            carte.setMinWidth(0);
            ligne.getChildren().add(carte);
        }

        if (ligne != null && ligne.getChildren().size() < 3) {
            int manquants = 3 - ligne.getChildren().size();
            for (int i = 0; i < manquants; i++) {
                VBox vide = new VBox();
                HBox.setHgrow(vide, Priority.ALWAYS);
                ligne.getChildren().add(vide);
            }
        }
    }

    // ============================================
    // 🃏 CRÉER CARTE VOYAGE
    // ============================================
    private VBox creerCarteVoyage(Voyage voyage) {
        VBox carte = new VBox(0);
        carte.getStyleClass().add("carte-voyage");
        carte.setMinWidth(0);
        carte.setMaxWidth(Double.MAX_VALUE);

        carte.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            Rectangle clip = new Rectangle(newVal.getWidth(), newVal.getHeight());
            clip.setArcWidth(28);
            clip.setArcHeight(28);
            carte.setClip(clip);
        });

        // ✅ RÉCUPÉRER LES IMAGES CORRECTEMENT
        List<String> images = recupererImages(voyage);

        System.out.println("🖼️ Carte voyage '" + voyage.getDestination()
                + "' → " + images.size() + " image(s)");

        final int[] indexImage = {0};
        final List<String> imagesFinal = images;

        // ---- IMAGE PANE ----
        StackPane imagePane = new StackPane();
        imagePane.setPrefHeight(200);
        imagePane.setMinHeight(200);
        imagePane.setMaxHeight(200);
        imagePane.setStyle("-fx-background-color: #eeeeee;");

        ImageView imgView = new ImageView();
        imgView.setFitHeight(200);
        imgView.setPreserveRatio(false);
        imgView.fitWidthProperty().bind(carte.widthProperty());

        // Charger la 1ère image
        if (!imagesFinal.isEmpty()) {
            chargerImage(imgView, imagesFinal.get(0), imagePane, voyage);
        } else {
            ajouterPlaceholder(imagePane, voyage);
        }
        imagePane.getChildren().add(imgView);

        // ---- BOUTONS NAVIGATION si plusieurs images ----
        if (imagesFinal.size() > 1) {
            Button btnPrev = creerBoutonNav("❮");
            Button btnNext = creerBoutonNav("❯");

            // Points indicateurs
            HBox dots = new HBox(5);
            dots.setAlignment(Pos.CENTER);
            for (int i = 0; i < imagesFinal.size(); i++) {
                Label dot = new Label("●");
                dot.setStyle(i == 0
                        ? "-fx-text-fill: white; -fx-font-size: 8px;"
                        : "-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 8px;");
                dots.getChildren().add(dot);
            }

            Label lblCompteur = new Label("1 / " + imagesFinal.size());
            lblCompteur.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white;" +
                            "-fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 8;");

            btnPrev.setOnAction(e -> {
                indexImage[0] = (indexImage[0] - 1 + imagesFinal.size()) % imagesFinal.size();
                chargerImage(imgView, imagesFinal.get(indexImage[0]), imagePane, voyage);
                mettreAJourDots(dots, indexImage[0]);
                lblCompteur.setText((indexImage[0] + 1) + " / " + imagesFinal.size());
            });

            btnNext.setOnAction(e -> {
                indexImage[0] = (indexImage[0] + 1) % imagesFinal.size();
                chargerImage(imgView, imagesFinal.get(indexImage[0]), imagePane, voyage);
                mettreAJourDots(dots, indexImage[0]);
                lblCompteur.setText((indexImage[0] + 1) + " / " + imagesFinal.size());
            });

            StackPane.setAlignment(btnPrev, Pos.CENTER_LEFT);
            StackPane.setAlignment(btnNext, Pos.CENTER_RIGHT);
            StackPane.setAlignment(dots, Pos.BOTTOM_CENTER);
            StackPane.setAlignment(lblCompteur, Pos.TOP_RIGHT);
            StackPane.setMargin(btnPrev, new Insets(0, 0, 0, 6));
            StackPane.setMargin(btnNext, new Insets(0, 6, 0, 0));
            StackPane.setMargin(dots, new Insets(0, 0, 6, 0));
            StackPane.setMargin(lblCompteur, new Insets(6, 6, 0, 0));

            imagePane.getChildren().addAll(btnPrev, btnNext, dots, lblCompteur);
        }

        // ---- CONTENU ----
        VBox contenu = new VBox(6);
        contenu.setPadding(new Insets(15, 15, 10, 15));
        VBox.setVgrow(contenu, Priority.ALWAYS);

        String nomDest = voyage.getDestinationObj() != null
                ? voyage.getDestinationObj().getNom() : voyage.getDestination();

        Label lblDest = new Label("✈️ " + nomDest);
        lblDest.getStyleClass().add("lbl-destination");

        String pays = voyage.getDestinationObj() != null
                && voyage.getDestinationObj().getPays() != null
                ? "🌍 " + voyage.getDestinationObj().getPays() : "";
        Label lblPays = new Label(pays);
        lblPays.getStyleClass().add("lbl-pays");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dates = voyage.getDateDebut() != null && voyage.getDateFin() != null
                ? "📅 " + voyage.getDateDebut().format(fmt) + "  →  " + voyage.getDateFin().format(fmt) : "";
        Label lblDates = new Label(dates);
        lblDates.getStyleClass().add("lbl-dates");

        contenu.getChildren().addAll(lblDest, lblPays, lblDates);

        if (voyage.getPaysDepart() != null && !voyage.getPaysDepart().isEmpty()) {
            Label lblDepart = new Label("🛫 Depuis: " + voyage.getPaysDepart());
            lblDepart.getStyleClass().add("lbl-depart");
            contenu.getChildren().add(lblDepart);
        }

        if (voyage.getDescription() != null && !voyage.getDescription().isEmpty()) {
            Label lblDesc = new Label(voyage.getDescription());
            lblDesc.getStyleClass().add("lbl-description");
            lblDesc.setWrapText(true);
            contenu.getChildren().add(lblDesc);
        }

        Separator sep = new Separator();
        sep.getStyleClass().add("separator");

        // ---- FOOTER ----
        HBox footer = new HBox(8);
        footer.setPadding(new Insets(10, 12, 12, 12));
        footer.setAlignment(Pos.CENTER_LEFT);

        Label lblPrix = new Label(String.format("%.0f TND", voyage.getPrix()));
        lblPrix.getStyleClass().add("lbl-prix");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.getStyleClass().add("btn-modifier");
        btnModifier.setOnAction(e -> handleModifier(voyage));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.getStyleClass().add("btn-supprimer");
        btnSupprimer.setOnAction(e -> handleSupprimer(voyage));

        Button btnDetails = new Button("🔍 Détails");
        btnDetails.getStyleClass().add("btn-details");
        btnDetails.setOnAction(e -> ouvrirPopupInfo(voyage));

        footer.getChildren().addAll(lblPrix, spacer, btnModifier, btnSupprimer, btnDetails);
        carte.getChildren().addAll(imagePane, contenu, sep, footer);
        return carte;
    }

    // ============================================
    // ✅ RÉCUPÉRER LES IMAGES D'UN VOYAGE
    // Parse image_url avec ";" pour avoir la liste complète
    // ============================================
    private List<String> recupererImages(Voyage voyage) {
        List<String> images = new ArrayList<>();

        // ✅ Priorité 1 : images depuis la destination (déjà parsées par DestinationService)
        if (voyage.getDestinationObj() != null) {
            List<String> destImages = voyage.getDestinationObj().getImages();
            if (destImages != null && !destImages.isEmpty()) {
                images.addAll(destImages);
                return images;
            }

            // ✅ Priorité 2 : parser imageUrl de la destination
            String destImageUrl = voyage.getDestinationObj().getImageUrl();
            if (destImageUrl != null && !destImageUrl.isEmpty()) {
                // Prendre seulement la première image
                String premiere = destImageUrl.split(";")[0].trim();
                if (!premiere.isEmpty()) {
                    images.add(premiere);
                }
                return images;
            }
        }

        // ✅ Priorité 3 : imagePath du voyage — prendre seulement la 1ère
        String imagePath = voyage.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            // ✅ Split par ";" et prendre uniquement la première
            String premiere = imagePath.split(";")[0].trim();
            if (!premiere.isEmpty()) {
                images.add(premiere);
            }
        }

        return images;
    }

    // ============================================
    // ➕ AJOUTER
    // ============================================
    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddVoyage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un voyage");
            stage.setScene(new Scene(root, 550, 700));
            stage.setOnHidden(e -> chargerVoyages());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Impossible d'ouvrir le formulaire:\n" + e.getMessage()).showAndWait();
        }
    }

    // ============================================
    // ✏️ MODIFIER
    // ============================================
    private void handleModifier(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddVoyage.fxml"));
            Parent root = loader.load();
            AddVoyageController controller = loader.getController();
            controller.preRemplir(voyage);
            Stage stage = new Stage();
            stage.setTitle("Modifier le voyage");
            stage.setScene(new Scene(root, 550, 700));
            stage.setOnHidden(e -> chargerVoyages());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le formulaire").showAndWait();
        }
    }

    // ============================================
    // 🗑️ SUPPRIMER
    // ============================================
    private void handleSupprimer(Voyage voyage) {
        String nom = voyage.getDestinationObj() != null
                ? voyage.getDestinationObj().getNom() : voyage.getDestination();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce voyage ?");
        confirm.setContentText("Voulez-vous supprimer le voyage vers " + nom + " ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean ok = voyageService.deleteVoyage(voyage.getId());
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "✅ Voyage supprimé !").showAndWait();
                chargerVoyages();
            } else {
                new Alert(Alert.AlertType.ERROR, "❌ Erreur lors de la suppression").showAndWait();
            }
        }
    }

    // ============================================
    // 🔍 RECHERCHE
    // ============================================
    @FXML
    private void handleRecherche() {
        if (tousLesVoyages == null || tfRecherche == null) return;
        String kw = tfRecherche.getText().toLowerCase().trim();
        if (kw.isEmpty()) {
            afficherVoyages(tousLesVoyages);
            if (lblCount != null) lblCount.setText(tousLesVoyages.size() + " voyage(s)");
            return;
        }
        List<Voyage> filtres = tousLesVoyages.stream().filter(v -> {
            String dest = v.getDestinationObj() != null
                    ? v.getDestinationObj().getNom() : v.getDestination();
            return dest != null && dest.toLowerCase().contains(kw);
        }).collect(Collectors.toList());
        afficherVoyages(filtres);
        if (lblCount != null) lblCount.setText(filtres.size() + " résultat(s)");
    }

    // ============================================
    // 🔄 FILTRER
    // ============================================
    @FXML
    private void appliquerFiltre() {
        if (tousLesVoyages == null || cbFiltre == null || cbFiltre.getValue() == null) return;
        String val = cbFiltre.getValue();
        List<Voyage> filtres;
        if (val.equals("Prix croissant"))
            filtres = tousLesVoyages.stream()
                    .sorted((a, b) -> Double.compare(a.getPrix(), b.getPrix()))
                    .collect(Collectors.toList());
        else if (val.equals("Prix décroissant"))
            filtres = tousLesVoyages.stream()
                    .sorted((a, b) -> Double.compare(b.getPrix(), a.getPrix()))
                    .collect(Collectors.toList());
        else filtres = tousLesVoyages;
        afficherVoyages(filtres);
    }

    // ============================================
    // 🔄 ACTUALISER
    // ============================================
    @FXML
    private void handleActualiser() {
        if (tfRecherche != null) tfRecherche.clear();
        chargerVoyages();
    }

    // ============================================
    // 🏠 RETOUR ACCUEIL
    // ============================================
    @FXML
    private void retourAccueil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 750));
            stage.setTitle("Accueil");
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ============================================
    // 🔍 RECHERCHE VOLS
    // ============================================
    @FXML
    private void ouvrirRecherchePersonnalisee(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RechercheVols.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Recherche de vols");
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ============================================
    // 🪟 POPUP INFO VOYAGE
    // ============================================
    private void ouvrirPopupInfo(Voyage voyage) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);

        String nomDest = voyage.getDestinationObj() != null
                ? voyage.getDestinationObj().getNom() : voyage.getDestination();

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 5);");
        root.setPrefWidth(580);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 18, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #5C6BC0, #3949AB);" +
                "-fx-background-radius: 16 16 0 0;");
        Label lblTitre = new Label("🌍 " + nomDest);
        lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button btnFermer = new Button("✕");
        btnFermer.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-weight: bold;");
        btnFermer.setOnAction(e -> popup.close());
        header.getChildren().addAll(lblTitre, hSpacer, btnFermer);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f4f6fb;");

        VBox sectionInfos = creerSection("✈️ Informations du voyage", "#FFF3E0", "#E65100");
        VBox infosBody = new VBox(8);
        infosBody.setPadding(new Insets(12, 15, 15, 15));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (voyage.getDateDebut() != null && voyage.getDateFin() != null)
            infosBody.getChildren().add(creerLigne("📅", "Période",
                    voyage.getDateDebut().format(fmt) + " → " + voyage.getDateFin().format(fmt)));
        infosBody.getChildren().add(creerLigne("💰", "Prix",
                String.format("%.0f TND par personne", voyage.getPrix())));
        if (voyage.getPaysDepart() != null && !voyage.getPaysDepart().isEmpty())
            infosBody.getChildren().add(creerLigne("🛫", "Départ depuis", voyage.getPaysDepart()));
        if (voyage.getDestinationObj() != null && voyage.getDestinationObj().getPays() != null)
            infosBody.getChildren().add(creerLigne("🌍", "Pays", voyage.getDestinationObj().getPays()));
        if (voyage.getDescription() != null && !voyage.getDescription().isEmpty()) {
            Label d = new Label("📝 " + voyage.getDescription());
            d.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            d.setWrapText(true);
            infosBody.getChildren().add(d);
        }
        sectionInfos.getChildren().add(infosBody);

        VBox sectionMeteo = creerSection("🌤️ Météo actuelle", "#E3F2FD", "#1565C0");
        Label meteoLoad = new Label("⏳ Chargement de la météo...");
        meteoLoad.setStyle("-fx-font-size: 13px; -fx-text-fill: #999; -fx-padding: 12 15 15 15;");
        sectionMeteo.getChildren().add(meteoLoad);

        VBox sectionPDI = creerSection("🏛️ À visiter", "#E8F5E9", "#2E7D32");
        Label pdiLoad = new Label("⏳ Chargement des points d'intérêt...");
        pdiLoad.setStyle("-fx-font-size: 13px; -fx-text-fill: #999; -fx-padding: 12 15 15 15;");
        sectionPDI.getChildren().add(pdiLoad);

        content.getChildren().addAll(sectionInfos, sectionMeteo, sectionPDI);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(500);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.getChildren().addAll(header, scroll);
        popup.setScene(new Scene(root));
        popup.show();

        new Thread(() -> {
            WeatherService.WeatherData meteo = weatherService.getMeteo(nomDest);
            Platform.runLater(() -> {
                sectionMeteo.getChildren().remove(meteoLoad);
                if (meteo != null) sectionMeteo.getChildren().add(creerBlocMeteo(meteo));
                else {
                    Label err = new Label("❌ Météo indisponible");
                    err.setStyle("-fx-font-size: 13px; -fx-text-fill: #EF5350; -fx-padding: 12 15 15 15;");
                    sectionMeteo.getChildren().add(err);
                }
            });
        }).start();

        new Thread(() -> {
            List<TripMapService.PointInteret> points = tripMapService.getPointsInteret(nomDest, 6);
            Platform.runLater(() -> {
                sectionPDI.getChildren().remove(pdiLoad);
                if (!points.isEmpty()) {
                    VBox liste = new VBox(6);
                    liste.setPadding(new Insets(8, 12, 12, 12));
                    points.forEach(p -> liste.getChildren().add(creerItemPDI(p)));
                    sectionPDI.getChildren().add(liste);
                } else {
                    Label aucun = new Label("😕 Aucun point d'intérêt trouvé");
                    aucun.setStyle("-fx-font-size: 13px; -fx-text-fill: #999; -fx-padding: 12 15 15 15;");
                    sectionPDI.getChildren().add(aucun);
                }
            });
        }).start();
    }

    // ============================================
    // 🧱 HELPERS UI
    // ============================================
    private VBox creerSection(String titre, String bg, String textColor) {
        VBox section = new VBox(0);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);");
        HBox h = new HBox();
        h.setPadding(new Insets(11, 15, 11, 15));
        h.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 12 12 0 0;");
        Label lbl = new Label(titre);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        h.getChildren().add(lbl);
        section.getChildren().add(h);
        return section;
    }

    private HBox creerLigne(String emoji, String label, String valeur) {
        HBox ligne = new HBox(10);
        ligne.setAlignment(Pos.CENTER_LEFT);
        Label e = new Label(emoji); e.setStyle("-fx-font-size: 14px;"); e.setMinWidth(22);
        Label l = new Label(label + " :"); l.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-min-width: 110;");
        Label v = new Label(valeur); v.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        v.setWrapText(true);
        ligne.getChildren().addAll(e, l, v);
        return ligne;
    }

    private HBox creerBlocMeteo(WeatherService.WeatherData m) {
        HBox box = new HBox(20);
        box.setPadding(new Insets(12, 15, 15, 15));
        box.setAlignment(Pos.CENTER_LEFT);
        VBox left = new VBox(4); left.setAlignment(Pos.CENTER);
        Label em = new Label(m.emoji); em.setStyle("-fx-font-size: 44px;");
        Label desc = new Label(m.description); desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");
        left.getChildren().addAll(em, desc);
        VBox right = new VBox(6);
        Label temp = new Label(String.format("%.0f°C", m.temperature));
        temp.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        Label mm = new Label("🌡️ Min " + String.format("%.0f°C", m.temperatureMin)
                + "  /  Max " + String.format("%.0f°C", m.temperatureMax));
        mm.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label hum = new Label("💧 Humidité : " + m.humidite + "%");
        hum.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label vent = new Label("💨 Vent : " + String.format("%.0f", m.vitesseVent) + " km/h");
        vent.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        right.getChildren().addAll(temp, mm, hum, vent);
        box.getChildren().addAll(left, right);
        return box;
    }

    private HBox creerItemPDI(TripMapService.PointInteret p) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(7, 10, 7, 10));
        item.setStyle("-fx-background-color: #f8f9ff; -fx-background-radius: 8;" +
                "-fx-border-color: #e8ecf4; -fx-border-radius: 8; -fx-border-width: 1;");
        Label em = new Label(p.emoji); em.setStyle("-fx-font-size: 20px;");
        VBox infos = new VBox(2);
        Label nom = new Label(p.nom); nom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label cat = new Label(p.categorie); cat.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        infos.getChildren().addAll(nom, cat);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label stars = new Label(getEtoiles(p.rating));
        stars.setStyle("-fx-text-fill: #FFA726; -fx-font-size: 12px;");
        item.getChildren().addAll(em, infos, sp, stars);
        return item;
    }

    private Button creerBoutonNav(String texte) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: rgba(0,0,0,0.45); -fx-text-fill: white;" +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20;" +
                "-fx-padding: 4 10; -fx-cursor: hand;");
        return btn;
    }

    private void chargerImage(ImageView imgView, String imageUrl,
                              StackPane imagePane, Voyage voyage) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            ajouterPlaceholder(imagePane, voyage);
            return;
        }

        // ✅ FIX : si imageUrl contient ";" → prendre uniquement la première
        if (imageUrl.contains(";")) {
            imageUrl = imageUrl.split(";")[0].trim();
        }

        if (imageUrl.isEmpty()) {
            ajouterPlaceholder(imagePane, voyage);
            return;
        }

        try {
            Image img;
            if (imageUrl.startsWith("/images/")) {
                File f = new File("src/main/resources" + imageUrl);
                if (f.exists()) {
                    img = new Image(f.toURI().toString());
                } else {
                    var res = getClass().getResource(imageUrl);
                    if (res != null) img = new Image(res.toExternalForm());
                    else { ajouterPlaceholder(imagePane, voyage); return; }
                }
            } else {
                img = new Image(imageUrl, true);
            }
            imgView.setImage(img);
        } catch (Exception e) {
            System.err.println("❌ Image non trouvée: " + imageUrl);
            ajouterPlaceholder(imagePane, voyage);
        }
    }

    private void mettreAJourDots(HBox dots, int indexActif) {
        for (int i = 0; i < dots.getChildren().size(); i++) {
            Label dot = (Label) dots.getChildren().get(i);
            dot.setStyle(i == indexActif
                    ? "-fx-text-fill: white; -fx-font-size: 8px;"
                    : "-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 8px;");
        }
    }

    private void ajouterPlaceholder(StackPane pane, Voyage voyage) {
        pane.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);");
        String nom = voyage.getDestinationObj() != null
                ? voyage.getDestinationObj().getNom() : voyage.getDestination();
        String initiale = (nom != null && !nom.isEmpty())
                ? String.valueOf(nom.charAt(0)).toUpperCase() : "✈";
        Label li = new Label(initiale);
        li.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label ln = new Label(nom != null ? nom : "Voyage");
        ln.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-weight: bold;");
        ln.setWrapText(true);
        ln.setMaxWidth(160);
        ln.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        VBox vb = new VBox(5, li, ln);
        vb.setAlignment(Pos.CENTER);
        pane.getChildren().clear();
        pane.getChildren().add(vb);
    }

    private String getEtoiles(int r) {
        if (r >= 3) return "⭐⭐⭐";
        if (r >= 2) return "⭐⭐";
        if (r >= 1) return "⭐";
        return "";
    }

    public void refreshVoyages() { chargerVoyages(); }
}
