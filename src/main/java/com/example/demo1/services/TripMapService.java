package com.example.demo1.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 🗺️ TripMapService - OpenTripMap API
 * Gratuit : 5000 requêtes/jour
 * Clé API : https://opentripmap.io/register
 */
public class TripMapService {

    // ✅ Remplace par ta clé API OpenTripMap (gratuite)
    private static final String API_KEY = "5ae2e3f221c38a28845f05b6e82af676f2e3d11ca80b91fd8d4bb61a";
    private static final String BASE_URL = "https://api.opentripmap.com/0.1/fr/places";

    // ============================================
    // 📍 MODÈLE POINT D'INTÉRÊT
    // ============================================
    public static class PointInteret {
        public String nom;
        public String categorie;
        public String emoji;
        public double latitude;
        public double longitude;
        public String description;
        public int rating;

        @Override
        public String toString() {
            return emoji + " " + nom + " (" + categorie + ")";
        }
    }

    // ============================================
    // 🔍 CHERCHER UNE VILLE ET OBTENIR SES COORDS
    // ============================================
    public double[] getCoordonneesVille(String ville) {
        try {
            String encoded = URLEncoder.encode(ville, StandardCharsets.UTF_8);
            String urlStr = BASE_URL + "/geoname?name=" + encoded +
                    "&apikey=" + API_KEY;

            String response = makeRequest(urlStr);
            if (response == null) return null;

            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            if (root.has("lat") && root.has("lon")) {
                return new double[]{
                        root.get("lat").getAsDouble(),
                        root.get("lon").getAsDouble()
                };
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur getCoordonneesVille: " + e.getMessage());
        }
        return null;
    }

    // ============================================
    // 🏛️ OBTENIR LES POINTS D'INTÉRÊT
    // ============================================
    public List<PointInteret> getPointsInteret(String ville, int limite) {
        List<PointInteret> points = new ArrayList<>();

        try {
            // 1. Obtenir les coordonnées
            double[] coords = getCoordonneesVille(ville);
            if (coords == null) return points;

            double lat = coords[0];
            double lon = coords[1];

            // 2. Chercher les points d'intérêt autour (rayon 5km)
            String urlStr = BASE_URL + "/radius?" +
                    "radius=5000" +
                    "&lon=" + lon +
                    "&lat=" + lat +
                    "&kinds=interesting_places" +
                    "&rate=2" +         // qualité minimale
                    "&limit=" + limite +
                    "&format=json" +
                    "&apikey=" + API_KEY;

            String response = makeRequest(urlStr);
            if (response == null) return points;

            JsonArray features = JsonParser.parseString(response).getAsJsonArray();

            for (int i = 0; i < features.size(); i++) {
                JsonObject feature = features.get(i).getAsJsonObject();
                PointInteret point = parsePoint(feature);
                if (point != null && point.nom != null && !point.nom.isEmpty()) {
                    points.add(point);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur getPointsInteret: " + e.getMessage());
        }

        return points;
    }

    // ============================================
    // 🔄 PARSER UN POINT D'INTÉRÊT
    // ============================================
    private PointInteret parsePoint(JsonObject feature) {
        try {
            PointInteret point = new PointInteret();

            // Nom
            JsonObject props = feature.getAsJsonObject("properties");
            point.nom = props.has("name") ? props.get("name").getAsString() : "";
            if (point.nom.isEmpty()) return null;

            // Catégorie
            String kinds = props.has("kinds") ? props.get("kinds").getAsString() : "";
            point.categorie = getCategorieLabel(kinds);
            point.emoji = getCategorieEmoji(kinds);

            // Coordonnées
            JsonObject geometry = feature.getAsJsonObject("geometry");
            JsonArray coordsArr = geometry.getAsJsonArray("coordinates");
            point.longitude = coordsArr.get(0).getAsDouble();
            point.latitude = coordsArr.get(1).getAsDouble();

            // Rating
            point.rating = props.has("rate") ? props.get("rate").getAsInt() : 0;

            return point;

        } catch (Exception e) {
            return null;
        }
    }

    // ============================================
    // 🏷️ LABEL CATÉGORIE
    // ============================================
    private String getCategorieLabel(String kinds) {
        if (kinds.contains("museum"))          return "Musée";
        if (kinds.contains("historic"))        return "Site historique";
        if (kinds.contains("natural"))         return "Nature";
        if (kinds.contains("religion"))        return "Lieu religieux";
        if (kinds.contains("architecture"))    return "Architecture";
        if (kinds.contains("park"))            return "Parc";
        if (kinds.contains("beach"))           return "Plage";
        if (kinds.contains("amusement"))       return "Divertissement";
        if (kinds.contains("sport"))           return "Sport";
        if (kinds.contains("food"))            return "Gastronomie";
        return "Point d'intérêt";
    }

    // ============================================
    // 😊 EMOJI CATÉGORIE
    // ============================================
    private String getCategorieEmoji(String kinds) {
        if (kinds.contains("museum"))          return "🏛️";
        if (kinds.contains("historic"))        return "🏰";
        if (kinds.contains("natural"))         return "🌿";
        if (kinds.contains("religion"))        return "⛪";
        if (kinds.contains("architecture"))    return "🏗️";
        if (kinds.contains("park"))            return "🌳";
        if (kinds.contains("beach"))           return "🏖️";
        if (kinds.contains("amusement"))       return "🎡";
        if (kinds.contains("sport"))           return "⚽";
        if (kinds.contains("food"))            return "🍽️";
        return "📍";
    }

    // ============================================
    // 📡 REQUÊTE HTTP
    // ============================================
    private String makeRequest(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(7000);

            if (conn.getResponseCode() != 200) return null;

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            return sb.toString();

        } catch (Exception e) {
            System.err.println("❌ Requête TripMap échouée: " + e.getMessage());
            return null;
        }
    }
}