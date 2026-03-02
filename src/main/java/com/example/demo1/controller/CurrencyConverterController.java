package com.example.demo1.controller;

import com.example.demo1.services.CurrencyService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class CurrencyConverterController {

    @FXML private TextField txtMontant;
    @FXML private ComboBox<String> cbDeviseSrc;
    @FXML private ComboBox<String> cbDeviseDst;
    @FXML private Label lblResultat;
    @FXML private Label lblTaux;
    @FXML private Label lblStatut;

    // Boutons raccourcis
    @FXML private Button btnUSD;
    @FXML private Button btnGBP;
    @FXML private Button btnJPY;
    @FXML private Button btnAED;
    @FXML private Button btnSAR;
    @FXML private Button btnEUR;

    private final CurrencyService currencyService = new CurrencyService();

    @FXML
    public void initialize() {
        // Remplir les ComboBox avec toutes les devises
        CurrencyService.DEVISES.forEach((code, info) -> {
            String item = code + "  -  " + info[1];
            cbDeviseSrc.getItems().add(item);
            cbDeviseDst.getItems().add(item);
        });

        // Valeurs par défaut
        cbDeviseSrc.setValue("EUR  -  Euro");
        cbDeviseDst.setValue("TND  -  Dinar Tunisien");
        txtMontant.setText("1000");

        // Convertir automatiquement à l'ouverture
        convertir();

        // Listener sur le champ montant (conversion en temps réel au changement)
        txtMontant.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && newVal.matches("[0-9]*\\.?[0-9]*")) {
                convertir();
            }
        });
    }

    // ============================================
    // Extraire le code devise depuis "EUR  -  Euro"
    // ============================================
    private String getCode(String item) {
        if (item == null) return "EUR";
        return item.split("  -  ")[0].trim();
    }

    // ============================================
    // Trouver l'item complet depuis un code
    // ============================================
    private String getItemParCode(String code) {
        for (String item : cbDeviseDst.getItems()) {
            if (item.startsWith(code + "  -  ")) return item;
        }
        return null;
    }

    // ============================================
    // Clic sur un bouton raccourci (USD, GBP, etc.)
    // Sélectionne la devise comme cible
    // ============================================
    @FXML
    public void selectionnerDevise(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String code = btn.getText().trim(); // "USD", "GBP", etc.

        String item = getItemParCode(code);
        if (item != null) {
            cbDeviseDst.setValue(item);
            convertir();
        }

        // Mettre en évidence le bouton sélectionné
        resetBoutons();
        btn.setStyle(btn.getStyle()
                .replace("-fx-background-color: white;", "-fx-background-color: #083D77;")
                .replace("-fx-text-fill: #083D77;",      "-fx-text-fill: white;")
                .replace("-fx-border-color: #d0dff0;",   "-fx-border-color: #083D77;"));
    }

    private void resetBoutons() {
        String base = "-fx-background-color: white; -fx-text-fill: #083D77;" +
                "-fx-font-family: 'Georgia'; -fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 7 14; -fx-background-radius: 20;" +
                "-fx-border-color: #d0dff0; -fx-border-width: 1;" +
                "-fx-border-radius: 20; -fx-cursor: hand;";
        if (btnUSD != null) btnUSD.setStyle(base);
        if (btnGBP != null) btnGBP.setStyle(base);
        if (btnJPY != null) btnJPY.setStyle(base);
        if (btnAED != null) btnAED.setStyle(base);
        if (btnSAR != null) btnSAR.setStyle(base);
        if (btnEUR != null) btnEUR.setStyle(base);
    }

    // ============================================
    // Convertir
    // ============================================
    @FXML
    public void convertir() {
        String fromStr    = cbDeviseSrc.getValue();
        String toStr      = cbDeviseDst.getValue();
        String montantStr = txtMontant.getText().trim();

        if (fromStr == null || toStr == null || montantStr.isEmpty()) return;

        double montant;
        try {
            montant = Double.parseDouble(montantStr.replace(",", "."));
        } catch (NumberFormatException e) {
            lblResultat.setText("Montant invalide");
            return;
        }

        String from = getCode(fromStr);
        String to   = getCode(toStr);

        // UI en attente
        lblStatut.setText("Chargement...");
        lblStatut.setStyle("-fx-text-fill: #aab8cc; -fx-font-size: 10px;");
        lblResultat.setText("...");

        // Appel API dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                double resultat = currencyService.convert(montant, from, to);
                Map<String, Double> rates = currencyService.getExchangeRates(from);
                double taux = rates.getOrDefault(to, 1.0);

                String symSrc = CurrencyService.getSymbole(from);
                String symDst = CurrencyService.getSymbole(to);

                String txtResultat = String.format("%s %.2f  =  %s %.2f",
                        symSrc, montant, symDst, resultat);
                String txtTaux = String.format("1 %s = %s %.4f  |  Taux BCE en direct",
                        from, symDst, taux);

                Platform.runLater(() -> {
                    lblResultat.setText(txtResultat);
                    lblTaux.setText(txtTaux);
                    lblStatut.setText("OK - Mis a jour");
                    lblStatut.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 10px;");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblResultat.setText("Erreur de connexion");
                    lblTaux.setText("Verifiez votre connexion internet");
                    lblStatut.setText("Hors ligne");
                    lblStatut.setStyle("-fx-text-fill: #e53935; -fx-font-size: 10px;");
                });
                System.err.println("Erreur conversion: " + e.getMessage());
            }
        }).start();
    }

    // ============================================
    // Inverser les deux devises
    // ============================================
    @FXML
    public void inverser() {
        String src = cbDeviseSrc.getValue();
        String dst = cbDeviseDst.getValue();
        cbDeviseSrc.setValue(dst);
        cbDeviseDst.setValue(src);
        convertir();
    }
}