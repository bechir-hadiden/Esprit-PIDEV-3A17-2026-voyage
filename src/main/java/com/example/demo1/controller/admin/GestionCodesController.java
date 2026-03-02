package com.example.demo1.controller.admin;



import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.example.demo1.entity.CodePromo;
import com.example.demo1.entity.Offre;
import com.example.demo1.services.CodePromoService;

import java.io.FileOutputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class GestionCodesController {

    @FXML private Label lblTitreOffre;
    @FXML private TextField txtCode;
    @FXML private DatePicker dpExpiration;
    @FXML private TableView<CodePromo> tableCodes;
    @FXML private TableColumn<CodePromo, String> colCode;
    @FXML private TableColumn<CodePromo, Date> colExpire;

    private CodePromoService cs = new CodePromoService();
    private Offre currentOffre;

    public void setOffre(Offre o) {
        this.currentOffre = o;
        lblTitreOffre.setText("Coupons pour : " + o.getTitre());
        refreshTable();
    }

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code_texte"));
        colExpire.setCellValueFactory(new PropertyValueFactory<>("date_expiration"));
    }

    private void refreshTable() {
        try {
            // MÉTIER : On filtre les codes pour n'afficher que ceux de l'offre sélectionnée
            List<CodePromo> allCodes = cs.afficher();
            List<CodePromo> filtered = allCodes.stream()
                    .filter(c -> c.getId_offre() == currentOffre.getId_offre())
                    .collect(Collectors.toList());
            tableCodes.setItems(FXCollections.observableArrayList(filtered));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- LOGIQUE MÉTIER ---
    @FXML
    private void handleGenererMetier() {
        // Appelle la méthode métier de génération aléatoire
        String codeGenere = cs.genererCodeAutomatique();
        txtCode.setText(codeGenere);
    }

    @FXML
    private void handleAjouter() {
        if (txtCode.getText().isEmpty() || dpExpiration.getValue() == null) {
            showAlert("Erreur", "Remplissez tous les champs.");
            return;
        }
        try {
            CodePromo cp = new CodePromo(txtCode.getText(), Date.valueOf(dpExpiration.getValue()), currentOffre.getId_offre());
            cs.ajouter(cp);
            refreshTable();
            txtCode.clear();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSupprimer() {
        CodePromo selected = tableCodes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                cs.supprimer(selected.getId_code());
                refreshTable();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- LOGIQUE API (Génération PDF) ---
    @FXML
    private void handleExportPDF() {
        CodePromo selected = tableCodes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection requise", "Veuillez sélectionner un coupon dans le tableau.");
            return;
        }

        try {
            // 1. Définir le nom et le chemin du fichier
            String fileName = "Coupon_" + selected.getCode_texte() + ".pdf";
            // On enregistre à la racine du projet pour être sûr de le trouver
            java.io.File file = new java.io.File(System.getProperty("user.dir") + "/" + fileName);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // 2. Contenu du PDF (iText)
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 20, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.BLUE);
            document.add(new Paragraph("--- SMARTTRIP : BON DE RÉDUCTION ---", titleFont));
            document.add(new Paragraph("\nOffre : " + currentOffre.getTitre()));
            document.add(new Paragraph("Destination : " + (currentOffre.getDestination() != null ? currentOffre.getDestination() : "N/A")));
            document.add(new Paragraph("Remise : -" + currentOffre.getTaux_remise() + "%"));
            document.add(new Paragraph("Valable jusqu'au : " + selected.getDate_expiration()));
            document.add(new Paragraph("\n"));

            // 3. RÉ-INTÉGRATION DU QR CODE (API 2 : ZXing)
            // C'est indispensable pour valider ton mail du prof (2 APIs)
            com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(selected.getCode_texte(), com.google.zxing.BarcodeFormat.QR_CODE, 200, 200);

            java.io.ByteArrayOutputStream pngOutputStream = new java.io.ByteArrayOutputStream();
            com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            com.itextpdf.text.Image qrCodeImage = com.itextpdf.text.Image.getInstance(pngData);
            qrCodeImage.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(qrCodeImage);

            document.add(new Paragraph("\nMerci de soutenir l'économie locale (ODD 8).",
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC)));

            document.close();

            // 4. OUVERTURE AUTOMATIQUE (L'effet "Wow" pour la démo)
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }

            System.out.println("PDF généré et ouvert : " + file.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF");
            e.printStackTrace();
            showAlert("Erreur critique", "Impossible de générer le PDF : " + e.getMessage());
        }
    }
    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t); a.setContentText(c); a.show();
    }
}
