package com.example.demo1.services;

import com.example.demo1.Utils.Database;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class YouTubeService {

    private static final String API_KEY = "AIzaSyDCkDgRUtc7QVEGkIy-Hk5OcehlEVqp9-k";
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    private Connection connection;
    private DestinationService destinationService;

    public YouTubeService() {
        this.connection = Database.getInstance().getConnection();
        this.destinationService = new DestinationService();
        System.out.println("✅ YouTubeService initialisé");
    }

    // ========================================
    // 🎬 MÉTHODE PRINCIPALE (version unique)
    // ========================================
    public String getVideoUrl(int destinationId, String nomVille) {
        System.out.println("========================================");
        System.out.println("🎬 Recherche vidéo pour: " + nomVille);
        System.out.println("========================================");

        // ✅ Validation du nom
        if (nomVille == null || nomVille.trim().isEmpty()) {
            System.err.println("❌ Nom null ou vide");
            return null;
        }
        if (nomVille.trim().length() < 3) {
            System.err.println("❌ Nom trop court: " + nomVille);
            return null;
        }
        if (nomVille.matches("^(.)\\1+$")) {
            System.err.println("❌ Nom invalide (caractères répétés): " + nomVille);
            return null;
        }
        if (!nomVille.toLowerCase().matches(".*[aeiouy].*")) {
            System.err.println("❌ Nom invalide (pas de voyelles): " + nomVille);
            return null;
        }

        // 1️⃣ Vérifier en BDD d'abord
        String urlEnBase = getVideoUrlFromBDD(destinationId);
        if (urlEnBase != null && !urlEnBase.isEmpty()) {
            System.out.println("✅ URL trouvée en BDD: " + urlEnBase);
            return urlEnBase;
        }

        // 2️⃣ Appeler YouTube API
        System.out.println("🔍 Appel YouTube API...");
        String urlYoutube = rechercherVideoYoutube(nomVille);
        if (urlYoutube != null) {
            sauvegarderVideoUrl(destinationId, urlYoutube);
            return urlYoutube;
        }

        // 3️⃣ Aucune vidéo trouvée
        System.err.println("❌ Aucune vidéo trouvée pour: " + nomVille);
        return null;
    }

    // ========================================
    // 🔍 APPEL YOUTUBE API
    // ========================================
    private String rechercherVideoYoutube(String nomVille) {
        try {
            String query = URLEncoder.encode(
                    nomVille + " travel tourism 4K",
                    StandardCharsets.UTF_8
            );

            String url = String.format(
                    "%s?part=snippet&q=%s&type=video&maxResults=1&videoDuration=medium&videoDefinition=high&key=%s",
                    YOUTUBE_SEARCH_URL, query, API_KEY
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("📊 Code réponse YouTube: " + response.statusCode());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray items = json.getAsJsonArray("items");

                if (items != null && items.size() > 0) {
                    JsonObject firstItem = items.get(0).getAsJsonObject();
                    String videoId = firstItem.getAsJsonObject("id").get("videoId").getAsString();
                    String titre = firstItem.getAsJsonObject("snippet").get("title").getAsString();
                    String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

                    System.out.println("✅ Vidéo trouvée: " + titre);
                    System.out.println("   URL: " + videoUrl);
                    return videoUrl;
                } else {
                    System.out.println("⚠️ Aucune vidéo pour: " + nomVille);
                }
            } else if (response.statusCode() == 403) {
                System.err.println("❌ QUOTA DÉPASSÉ ou CLÉ INVALIDE !");
            } else {
                System.err.println("❌ Erreur YouTube API: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Exception YouTube: " + e.getMessage());
        }
        return null;
    }

    // ========================================
    // 💾 LIRE L'URL DEPUIS LA BDD
    // ========================================
    private String getVideoUrlFromBDD(int destinationId) {
        String sql = "SELECT video_url FROM destination WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, destinationId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String url = rs.getString("video_url");
                if (url != null && !url.isEmpty()) return url;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture BDD: " + e.getMessage());
        }
        return null;
    }

    // ========================================
    // 💾 SAUVEGARDER L'URL EN BDD
    // ========================================
    private void sauvegarderVideoUrl(int destinationId, String videoUrl) {
        String sql = "UPDATE destination SET video_url = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, videoUrl);
            pstmt.setInt(2, destinationId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("💾 URL sauvegardée en BDD !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur sauvegarde BDD: " + e.getMessage());
        }
    }

    // ========================================
    // 🔄 FORCER RECHARGEMENT
    // ========================================
    public String forcerRechargerVideo(int destinationId, String nomVille) {
        String sql = "UPDATE destination SET video_url = NULL WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, destinationId);
            pstmt.executeUpdate();
            System.out.println("🗑️ Ancienne URL effacée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
        return getVideoUrl(destinationId, nomVille);
    }

    // ========================================
    // 📊 TESTER LA CLÉ API
    // ========================================
    public boolean testerCleAPI() {
        try {
            String url = YOUTUBE_SEARCH_URL + "?part=snippet&q=test&type=video&maxResults=1&key=" + API_KEY;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Clé API valide !");
                return true;
            } else {
                System.err.println("❌ Clé API invalide ! Code: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur test clé API: " + e.getMessage());
            return false;
        }
    }
}