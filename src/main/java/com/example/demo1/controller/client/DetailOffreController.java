package com.example.demo1.controller.client;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import com.example.demo1.entity.CodePromo;
import com.example.demo1.entity.Offre;
import com.example.demo1.services.CodePromoService;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.sql.Date;
import java.time.LocalDate;

public class DetailOffreController {

    @FXML private ImageView imgDetail;
    @FXML private Label lblDestination, lblTitre, lblRemise, lblDescription;
    @FXML private TextField txtCodeGenere;
    @FXML private Button btnPDF;
    @FXML private Label lblDates;
    @FXML private Label lblRemiseBadge;

    private Offre currentOffre;
    private CodePromoService cs = new CodePromoService();

    public void setOffreData(Offre o) {
        this.currentOffre = o;

        // Remplissage des textes
        lblTitre.setText(o.getTitre());
        lblDestination.setText("📍 " + (o.getDestination() != null ? o.getDestination() : "Destination"));
        lblDescription.setText(o.getDescription());
        lblRemiseBadge.setText("-" + o.getTaux_remise() + "%");

        if (o.getDate_debut() != null && o.getDate_fin() != null) {
            lblDates.setText("Du " + o.getDate_debut() + " au " + o.getDate_fin());
        }

        // --- LOGIQUE PHOTO INTELLIGENTE (WEB vs LOCAL) ---
        try {
            String path = o.getImage_url();
            if (path == null || path.isEmpty()) {
                path = "default.jpg";
            }

            if (path.startsWith("http")) {
                // CAS 1 : C'est un lien Web (Unsplash pour les Hôtels)
                // On charge l'image directement via l'URL
                Image img = new Image(path, true); // 'true' permet de charger en arrière-plan sans freezer l'appli
                imgDetail.setImage(img);
            } else {
                // CAS 2 : C'est une image locale (Vols, Transports, Voyages)
                java.net.URL imageUrl = getClass().getResource("/images/" + path);
                if (imageUrl != null) {
                    imgDetail.setImage(new Image(imageUrl.toExternalForm()));
                } else {
                    // Image par défaut si le fichier n'existe pas dans le dossier
                    java.net.URL defaultUrl = getClass().getResource("/images/default.jpg");
                    if (defaultUrl != null) imgDetail.setImage(new Image(defaultUrl.toExternalForm()));
                }
            }

            // --- APPLIQUER LES COINS ARRONDIS SUR L'IMAGE ---
            Rectangle clip = new Rectangle(500, 250);
            clip.setArcWidth(40);
            clip.setArcHeight(40);
            imgDetail.setClip(clip);

        } catch (Exception e) {
            System.err.println("Erreur chargement image détails : " + e.getMessage());
        }
    }

    @FXML
    private void handleGenererPromo() {
        String nouveauCode = cs.genererCodeAutomatique();
        txtCodeGenere.setText(nouveauCode);

        try {
            CodePromo cp = new CodePromo(nouveauCode, Date.valueOf(LocalDate.now().plusMonths(1)), currentOffre.getId_offre());
            cs.ajouter(cp);
            btnPDF.setDisable(false);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- LOGIQUE API : EXPORT PDF + GÉNÉRATION QR CODE ---
    @FXML
    private void handleExportPDF() {
        try {
            String codePromoText = txtCodeGenere.getText();
            String fileName = "Coupon_SmartTrip_" + codePromoText + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // 1. En-tête du PDF
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLUE);
            Paragraph title = new Paragraph("SMARTTRIP - BON DE RÉDUCTION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\nDestination : " + currentOffre.getDestination()));
            document.add(new Paragraph("Offre : " + currentOffre.getTitre()));
            document.add(new Paragraph("Remise : -" + currentOffre.getTaux_remise() + "%"));
            document.add(new Paragraph("------------------------------------------------------------------------------------------"));

            // 2. GÉNÉRATION DU QR CODE (API 2 : ZXING)
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // On encode le texte du code promo dans un QR Code de 200x200
            BitMatrix bitMatrix = qrCodeWriter.encode(codePromoText, BarcodeFormat.QR_CODE, 200, 200);

            // Conversion en flux d'image (PNG)
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            // Création de l'image iText
            com.itextpdf.text.Image qrCodeImg = com.itextpdf.text.Image.getInstance(pngData);
            qrCodeImg.setAlignment(Element.ALIGN_CENTER);

            // 3. Ajout du QR Code au document
            document.add(new Paragraph("\nScannez ce code pour utiliser votre remise :"));
            document.add(qrCodeImg);

            document.add(new Paragraph("\nCODE TEXTE : " + codePromoText, new Font(Font.FontFamily.COURIER, 14, Font.BOLD)));

            // 4. Mention ODD 8
            if(currentOffre.isIs_local_support()) {
                Font greenFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, new BaseColor(39, 174, 96));
                Paragraph oddMsg = new Paragraph("\nCe voyage soutient l'économie locale et favorise le travail décent (ODD 8).", greenFont);
                oddMsg.setAlignment(Element.ALIGN_CENTER);
                document.add(oddMsg);
            }

            document.close();

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Export réussi");
            a.setHeaderText("Votre coupon est prêt !");
            a.setContentText("Le fichier PDF avec QR Code a été généré : " + fileName);
            a.show();

        } catch (Exception e) {
            System.err.println("Erreur génération PDF/QR : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleFermer() {
        lblTitre.getScene().getWindow().hide();
    }
}
