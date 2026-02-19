package com.example.demo1.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class QRCodeService {

    private YouTubeService youtubeService;

    // ============================================
    // 🏗️ CONSTRUCTEUR
    // ============================================
    public QRCodeService() {
        this.youtubeService = new YouTubeService();
        System.out.println("✅ QRCodeService initialisé");
    }

    // ============================================
    // 📱 GÉNÉRER QR CODE POUR UNE DESTINATION (avec ID)
    // ============================================
    public Image genererQRCodeDestination(int destinationId, String nomVille, int taille) {
        System.out.println("📱 Génération QR Code pour: " + nomVille);
        System.out.println("   Destination ID: " + destinationId);

        // 1️⃣ Obtenir l'URL YouTube (depuis BDD ou API)
        String videoUrl = youtubeService.getVideoUrl(destinationId, nomVille);

        if (videoUrl == null || videoUrl.isEmpty()) {
            System.err.println("❌ Aucune URL vidéo trouvée");
            return null;
        }

        // 2️⃣ Générer le QR Code avec l'URL
        return genererQRCode(videoUrl, taille);
    }

    // ============================================
    // 🔲 GÉNÉRER QR CODE DEPUIS UNE URL
    // ============================================
    public Image genererQRCode(String url, int taille) {
        if (url == null || url.isEmpty()) {
            System.err.println("❌ URL vide, impossible de générer le QR Code");
            return null;
        }

        try {
            System.out.println("🔲 Génération QR Code...");
            System.out.println("   URL: " + url);
            System.out.println("   Taille: " + taille + "x" + taille);

            // Configuration du QR Code
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Générer la matrice QR
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                    url,
                    BarcodeFormat.QR_CODE,
                    taille,
                    taille,
                    hints
            );

            // Convertir en image JavaFX
            WritableImage image = new WritableImage(taille, taille);
            PixelWriter pixelWriter = image.getPixelWriter();

            for (int x = 0; x < taille; x++) {
                for (int y = 0; y < taille; y++) {
                    // Noir pour les modules QR, blanc pour le fond
                    pixelWriter.setColor(
                            x, y,
                            bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE
                    );
                }
            }

            System.out.println("✅ QR Code généré avec succès !");
            return image;

        } catch (WriterException e) {
            System.err.println("❌ Erreur génération QR Code: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}