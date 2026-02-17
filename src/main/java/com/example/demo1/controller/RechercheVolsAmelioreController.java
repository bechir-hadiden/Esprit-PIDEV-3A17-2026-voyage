package com.example.demo1.controller;

import com.example.demo1.entity.Vol;
import com.example.demo1.services.AmadeusService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la recherche avancée de vols avec autocomplétion intelligente
 */
public class RechercheVolsAmelioreController {

    // ============ Composants FXML ============
    @FXML private RadioButton rbAllerRetour;
    @FXML private RadioButton rbAllerSimple;
    @FXML private TextField tfOrigine;
    @FXML private TextField tfDestination;
    @FXML private ListView<String> lvOrigine;
    @FXML private ListView<String> lvDestination;
    @FXML private DatePicker dpDepart;
    @FXML private DatePicker dpRetour;
    @FXML private VBox vboxRetour;
    @FXML private ComboBox<String> cbPassagers;
    @FXML private CheckBox cbVolDirect;
    @FXML private CheckBox cbBagages;
    @FXML private VBox vboxResultats;

    // ============ Variables d'état ============
    private String codeOrigine = "";
    private String codeDestination = "";
    private List<Map<String, String>> dernieresSuggestionsOrigine;
    private List<Map<String, String>> dernieresSuggestionsDestination;

    // ============ Constantes ============
    private static final int DELAI_RECHERCHE_MS = 300;
    private static final int CARACTERES_MIN_RECHERCHE = 2;
    private AmadeusService amadeusService;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation de la recherche intelligente de vols");
        amadeusService = new AmadeusService();

        initialiserPassagers();
        initialiserDates();
        configurerTypeVoyage();
        configurerAutocompletion();

        System.out.println("✅ Interface prête");
    }

    /**
     * Initialise la liste déroulante des passagers
     */
    private void initialiserPassagers() {
        cbPassagers.setItems(FXCollections.observableArrayList(
                "1 passager", "2 passagers", "3 passagers",
                "4 passagers", "5 passagers", "6+ passagers"
        ));
        cbPassagers.setValue("1 passager");
    }

    /**
     * Initialise les dates par défaut
     */
    private void initialiserDates() {
        dpDepart.setValue(LocalDate.now().plusDays(7));
        dpRetour.setValue(LocalDate.now().plusDays(14));
    }

    /**
     * Configure le comportement aller simple / aller-retour
     */
    private void configurerTypeVoyage() {
        rbAllerSimple.selectedProperty().addListener((obs, old, nouveau) -> {
            vboxRetour.setVisible(!nouveau);
            vboxRetour.setManaged(!nouveau);
        });
    }

    /**
     * Configure le système d'autocomplétion pour les deux champs
     */
    private void configurerAutocompletion() {
        // ✅ CORRECTION: Configuration Origine avec gestion correcte du clic
        configurerListeAutocompletion(lvOrigine, tfOrigine, this::selectionnerOrigine);

        // ✅ CORRECTION: Configuration Destination avec gestion correcte du clic
        configurerListeAutocompletion(lvDestination, tfDestination, this::selectionnerDestination);
    }

    /**
     * Configure une liste d'autocomplétion avec son comportement de sélection
     * ✅ CORRECTION: Ajout du TextField pour gérer le focus correctement
     */
    private void configurerListeAutocompletion(ListView<String> liste, TextField champ, Runnable actionSelection) {
        // Gestion du clic sur un élément
        liste.setOnMouseClicked(event -> {
            if (liste.getSelectionModel().getSelectedItem() != null) {
                actionSelection.run();
                liste.setVisible(false);
                champ.requestFocus(); // ✅ Redonner le focus au champ
            }
        });

        // ✅ CORRECTION: Gestion du focus AMÉLIORÉE
        champ.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Quand le champ perd le focus
                // ✅ Vérifier si le focus n'est pas sur la liste
                Platform.runLater(() -> {
                    // Si la liste n'a pas le focus, on la cache
                    if (!liste.isFocused()) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(300); // ✅ Délai augmenté pour permettre le clic
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            Platform.runLater(() -> liste.setVisible(false));
                        }).start();
                    }
                });
            }
        });

        // ✅ NOUVEAU: Gestion de la sélection avec les flèches + ENTRÉE
        champ.setOnKeyPressed(event -> {
            if (liste.isVisible()) {
                switch (event.getCode()) {
                    case DOWN:
                        // Naviguer vers le bas dans la liste
                        liste.requestFocus();
                        liste.getSelectionModel().selectFirst();
                        event.consume();
                        break;
                    case ESCAPE:
                        // Fermer la liste
                        liste.setVisible(false);
                        event.consume();
                        break;
                }
            }
        });

        // ✅ NOUVEAU: Navigation clavier dans la liste
        liste.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (liste.getSelectionModel().getSelectedItem() != null) {
                        actionSelection.run();
                        liste.setVisible(false);
                        champ.requestFocus();
                    }
                    event.consume();
                    break;
                case ESCAPE:
                    liste.setVisible(false);
                    champ.requestFocus();
                    event.consume();
                    break;
            }
        });
    }

    /**
     * Gère la sélection d'une origine dans la liste
     */
    private void selectionnerOrigine() {
        String selection = lvOrigine.getSelectionModel().getSelectedItem();
        int index = lvOrigine.getSelectionModel().getSelectedIndex();

        if (index >= 0 && dernieresSuggestionsOrigine != null &&
                index < dernieresSuggestionsOrigine.size()) {

            Map<String, String> aeroport = dernieresSuggestionsOrigine.get(index);
            codeOrigine = aeroport.get("code");
            tfOrigine.setText(selection);

            System.out.println("✅ Origine sélectionnée: " + aeroport.get("name") + " (" + codeOrigine + ")");
        }
    }

    /**
     * Gère la sélection d'une destination dans la liste
     */
    private void selectionnerDestination() {
        String selection = lvDestination.getSelectionModel().getSelectedItem();
        int index = lvDestination.getSelectionModel().getSelectedIndex();

        if (index >= 0 && dernieresSuggestionsDestination != null &&
                index < dernieresSuggestionsDestination.size()) {

            Map<String, String> aeroport = dernieresSuggestionsDestination.get(index);
            codeDestination = aeroport.get("code");
            tfDestination.setText(selection);

            System.out.println("✅ Destination sélectionnée: " + aeroport.get("name") + " (" + codeDestination + ")");
        }
    }

    /**
     * Déclenché à chaque frappe dans le champ Origine
     */
    @FXML
    private void onOrigineKeyReleased() {
        String keyword = tfOrigine.getText().trim();

        if (keyword.length() >= CARACTERES_MIN_RECHERCHE) {
            rechercherAeroportsAsync(keyword, lvOrigine, true);
        } else {
            lvOrigine.setVisible(false);
            codeOrigine = "";
        }
    }

    /**
     * Déclenché à chaque frappe dans le champ Destination
     */
    @FXML
    private void onDestinationKeyReleased() {
        String keyword = tfDestination.getText().trim();

        if (keyword.length() >= CARACTERES_MIN_RECHERCHE) {
            rechercherAeroportsAsync(keyword, lvDestination, false);
        } else {
            lvDestination.setVisible(false);
            codeDestination = "";
        }
    }

    /**
     * Recherche des aéroports de manière asynchrone
     * @param keyword Mot-clé de recherche
     * @param liste Liste où afficher les résultats
     * @param isOrigine true si c'est pour l'origine, false pour la destination
     */
    private void rechercherAeroportsAsync(String keyword, ListView<String> liste, boolean isOrigine) {
        System.out.println("🔍 Recherche: '" + keyword + "'");

        Task<List<Map<String, String>>> task = new Task<>() {
            @Override
            protected List<Map<String, String>> call() throws Exception {
                Thread.sleep(DELAI_RECHERCHE_MS);
                return AmadeusService.rechercherAeroports(keyword);
            }
        };

        task.setOnSucceeded(event -> {
            List<Map<String, String>> resultats = task.getValue();
            afficherSuggestions(resultats, liste, isOrigine);
        });

        task.setOnFailed(event -> {
            System.err.println("❌ Erreur recherche: " + task.getException().getMessage());
            liste.setVisible(false);
        });

        new Thread(task).start();
    }

    /**
     * Affiche les suggestions dans la liste déroulante
     */
    private void afficherSuggestions(List<Map<String, String>> resultats,
                                     ListView<String> liste, boolean isOrigine) {
        if (resultats.isEmpty()) {
            liste.setVisible(false);
            System.out.println("⚠️ Aucun résultat");
            return;
        }

        // Sauvegarder les résultats pour la sélection ultérieure
        if (isOrigine) {
            dernieresSuggestionsOrigine = resultats;
        } else {
            dernieresSuggestionsDestination = resultats;
        }

        // Créer la liste d'affichage
        var suggestions = FXCollections.observableArrayList(
                resultats.stream()
                        .map(a -> a.get("display"))
                        .toList()
        );

        liste.setItems(suggestions);
        liste.setVisible(true);

        // ✅ CORRECTION: S'assurer que la liste est au premier plan
        liste.toFront();

        System.out.println("✅ " + suggestions.size() + " suggestions affichées");
    }

    /**
     * Échange l'origine et la destination
     */
    @FXML
    private void echangerAeroports() {
        String tempTexte = tfOrigine.getText();
        String tempCode = codeOrigine;
        List<Map<String, String>> tempSuggestions = dernieresSuggestionsOrigine;

        tfOrigine.setText(tfDestination.getText());
        codeOrigine = codeDestination;
        dernieresSuggestionsOrigine = dernieresSuggestionsDestination;

        tfDestination.setText(tempTexte);
        codeDestination = tempCode;
        dernieresSuggestionsDestination = tempSuggestions;

        System.out.println("🔄 Aéroports échangés");
    }

    /**
     * Lance la recherche de vols
     */
    @FXML
    private void rechercherVols() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🛫 RECHERCHE DE VOLS");
        System.out.println("=".repeat(50));

        // Validation
        if (!validerFormulaire()) {
            return;
        }

        // Extraction des paramètres
        int nbPassagers = extraireNombrePassagers();
        String dateDepart = dpDepart.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        System.out.println("Origine: " + codeOrigine);
        System.out.println("Destination: " + codeDestination);
        System.out.println("Date: " + dateDepart);
        System.out.println("Passagers: " + nbPassagers);

        // Lancer la recherche
        afficherChargement();
        rechercherVolsAsync(codeOrigine, codeDestination, dateDepart, nbPassagers);
    }

    /**
     * Valide que tous les champs obligatoires sont remplis
     */
    private boolean validerFormulaire() {
        if (codeOrigine.isEmpty() || codeDestination.isEmpty()) {
            afficherErreur("Sélection requise",
                    "Veuillez sélectionner une origine et une destination.\n\n" +
                            "💡 Astuce: Tapez au moins 2 lettres (ex: 'tun', 'paris', 'alger') " +
                            "puis CLIQUEZ sur un résultat dans la liste.");
            return false;
        }

        if (dpDepart.getValue() == null) {
            afficherErreur("Date manquante", "Veuillez sélectionner une date de départ.");
            return false;
        }

        if (rbAllerRetour.isSelected() && dpRetour.getValue() == null) {
            afficherErreur("Date manquante", "Veuillez sélectionner une date de retour.");
            return false;
        }

        return true;
    }

    /**
     * Extrait le nombre de passagers du ComboBox
     */
    private int extraireNombrePassagers() {
        String selection = cbPassagers.getValue();
        return selection.contains("+") ? 6 :
                Integer.parseInt(selection.substring(0, 1));
    }

    /**
     * Affiche un indicateur de chargement
     */
    private void afficherChargement() {
        vboxResultats.getChildren().clear();

        ProgressIndicator loader = new ProgressIndicator();
        Label texte = new Label("🔄 Recherche des meilleurs vols...");
        texte.setStyle("-fx-font-size: 18px; -fx-padding: 20;");

        VBox loaderBox = new VBox(15, loader, texte);
        loaderBox.setAlignment(Pos.CENTER);
        loaderBox.setPadding(new Insets(100));

        vboxResultats.getChildren().add(loaderBox);
    }

    /**
     * Recherche les vols de manière asynchrone
     */
    private void rechercherVolsAsync(String origine, String destination,
                                     String date, int passagers) {
        Task<List<Vol>> task = new Task<>() {
            @Override
            protected List<Vol> call() {
                // ✅ CORRECTION: Utiliser l'instance
                return amadeusService.rechercherVols(origine, destination, date, passagers);
            }
        };

        task.setOnSucceeded(event -> afficherResultats(task.getValue()));
        task.setOnFailed(event -> afficherErreurRecherche());

        new Thread(task).start();
    }

    /**
     * Affiche les résultats de la recherche
     */
    private void afficherResultats(List<Vol> vols) {
        vboxResultats.getChildren().clear();

        if (vols.isEmpty()) {
            afficherAucunResultat();
        } else {
            System.out.println("✅ " + vols.size() + " vols trouvés");
            vols.forEach(vol -> vboxResultats.getChildren().add(creerCarteVol(vol)));
        }

        System.out.println("=".repeat(50) + "\n");
    }

    /**
     * Affiche un message quand aucun vol n'est trouvé
     */
    private void afficherAucunResultat() {
        Label message = new Label(
                "😕 Aucun vol trouvé pour cette recherche\n\n" +
                        "💡 Essayez de modifier vos dates ou votre destination"
        );
        message.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-text-fill: #666; " +
                        "-fx-padding: 80; " +
                        "-fx-text-alignment: center;"
        );
        vboxResultats.getChildren().add(message);
        System.out.println("⚠️ Aucun vol disponible");
    }

    /**
     * Affiche une erreur lors de la recherche
     */
    private void afficherErreurRecherche() {
        vboxResultats.getChildren().clear();
        Label erreur = new Label("❌ Une erreur est survenue lors de la recherche");
        erreur.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-padding: 80;");
        vboxResultats.getChildren().add(erreur);
        System.err.println("❌ Échec de la recherche");
    }

    /**
     * Crée une carte visuelle pour un vol
     */
    private VBox creerCarteVol(Vol vol) {
        VBox carte = new VBox(12);
        carte.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 25; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 2);"
        );

        carte.getChildren().addAll(
                creerEnteteCarte(vol),
                new Separator(),
                creerHorairesCarte(vol)
        );

        return carte;
    }

    /**
     * Crée l'en-tête d'une carte de vol (compagnie, escales, prix)
     */
    private HBox creerEnteteCarte(Vol vol) {
        HBox entete = new HBox(15);
        entete.setAlignment(Pos.CENTER_LEFT);

        Label compagnie = new Label("✈️ " + vol.getCompagnie());
        compagnie.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label escales = new Label(
                vol.getEscales() == 0 ? "✅ Direct" : "🔄 " + vol.getEscales() + " escale(s)"
        );
        escales.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Region espaceur = new Region();
        HBox.setHgrow(espaceur, Priority.ALWAYS);

        Label prix = new Label(String.format("%.0f %s", vol.getPrix(), vol.getDevise()));
        prix.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #667eea;");

        entete.getChildren().addAll(compagnie, escales, espaceur, prix);
        return entete;
    }

    /**
     * Crée la section horaires d'une carte de vol
     */
    private HBox creerHorairesCarte(Vol vol) {
        HBox horaires = new HBox(50);
        horaires.setAlignment(Pos.CENTER);
        horaires.setPadding(new Insets(10, 0, 0, 0));

        horaires.getChildren().addAll(
                creerInfoVille(vol.getHeureDepart(), vol.getDepart()),
                creerDuree(vol.getDuree()),
                creerInfoVille(vol.getHeureArrivee(), vol.getArrivee())
        );

        return horaires;
    }

    /**
     * Crée un bloc d'information pour une ville (heure + nom)
     */
    private VBox creerInfoVille(String heure, String ville) {
        VBox info = new VBox(5);
        info.setAlignment(Pos.CENTER);

        Label labelHeure = new Label(heure);
        labelHeure.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label labelVille = new Label(ville);
        labelVille.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        info.getChildren().addAll(labelHeure, labelVille);
        return info;
    }

    /**
     * Crée l'affichage de la durée du vol
     */
    private VBox creerDuree(String duree) {
        VBox boxDuree = new VBox(5);
        boxDuree.setAlignment(Pos.CENTER);

        Label labelDuree = new Label("⏱️ " + duree);
        labelDuree.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");

        Label ligne = new Label("—————————");
        ligne.setStyle("-fx-text-fill: #ddd;");

        boxDuree.getChildren().addAll(labelDuree, ligne);
        return boxDuree;
    }

    /**
     * Pré-remplit le formulaire avec une destination (utilisé depuis les cartes)
     */
    public void preRemplirDestination(String codeIATA) {
        codeDestination = codeIATA;
        tfDestination.setText(codeIATA);

        codeOrigine = "TUN";
        tfOrigine.setText("Tunis (TUN)");

        System.out.println("✅ Formulaire pré-rempli: " + codeOrigine + " → " + codeDestination);
    }

    /**
     * Affiche une alerte d'erreur
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}