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
 * 🗺️ TripMapService
 * ✅ Utilise Nominatim (OpenStreetMap) + Overpass API
 * ✅ 100% GRATUIT — aucune clé API requise
 */
public class TripMapService {

    // ✅ APIs gratuites sans clé
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String OVERPASS_URL  = "https://overpass-api.de/api/interpreter";

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
    // 🔍 ÉTAPE 1 : Coordonnées via Nominatim
    // ============================================
    public double[] getCoordonneesVille(String ville) {
        try {
            String encoded = URLEncoder.encode(ville, StandardCharsets.UTF_8);
            String urlStr = NOMINATIM_URL + "?q=" + encoded +
                    "&format=json&limit=1&addressdetails=0";

            String response = makeRequest(urlStr, "nominatim.openstreetmap.org");
            if (response == null || response.equals("[]")) {
                System.err.println("❌ Ville non trouvée: " + ville);
                return null;
            }

            JsonArray results = JsonParser.parseString(response).getAsJsonArray();
            if (results.size() == 0) return null;

            JsonObject first = results.get(0).getAsJsonObject();
            double lat = first.get("lat").getAsDouble();
            double lon = first.get("lon").getAsDouble();

            System.out.println("📍 Coordonnées de " + ville + ": " + lat + ", " + lon);
            return new double[]{lat, lon};

        } catch (Exception e) {
            System.err.println("❌ Erreur getCoordonneesVille: " + e.getMessage());
        }
        return null;
    }

    // ============================================
    // 🏛️ ÉTAPE 2 : Points d'intérêt via Overpass
    // ============================================
    public List<PointInteret> getPointsInteret(String ville, int limite) {
        List<PointInteret> points = new ArrayList<>();

        try {
            // 1. Obtenir les coordonnées
            double[] coords = getCoordonneesVille(ville);
            if (coords == null) {
                System.err.println("❌ Impossible de trouver: " + ville);
                return points;
            }

            double lat = coords[0];
            double lon = coords[1];
            int rayon = 5000; // 5 km

            // 2. Requête Overpass — uniquement lieux touristiques populaires
            String query = "[out:json][timeout:15];\n" +
                    "(\n" +
                    "  node[\"tourism\"~\"museum|attraction|viewpoint|gallery|zoo|theme_park\"]" +
                    "(around:" + rayon + "," + lat + "," + lon + ");\n" +
                    "  node[\"historic\"~\"monument|memorial|castle|ruins|fort|archaeological_site\"]" +
                    "(around:" + rayon + "," + lat + "," + lon + ");\n" +
                    "  node[\"leisure\"~\"park|garden|nature_reserve\"]" +
                    "(around:" + rayon + "," + lat + "," + lon + ");\n" +
                    "  node[\"natural\"~\"peak|waterfall|beach|cave_entrance\"]" +
                    "(around:" + rayon + "," + lat + "," + lon + ");\n" +
                    ");\n" +
                    "out " + (limite * 3) + ";"; // demander plus pour filtrer les sans-nom

            // Encoder la requête pour POST
            String encoded = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            String response = makePostRequest(OVERPASS_URL, encoded);

            if (response == null) return points;

            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            JsonArray elements = root.getAsJsonArray("elements");

            System.out.println("🔍 Overpass: " + elements.size() + " éléments trouvés pour " + ville);

            int count = 0;
            for (int i = 0; i < elements.size() && count < limite; i++) {
                JsonObject el = elements.get(i).getAsJsonObject();
                PointInteret point = parseElement(el);
                if (point != null) {
                    points.add(point);
                    count++;
                }
            }

            System.out.println("✅ " + points.size() + " points d'intérêt pour " + ville);

        } catch (Exception e) {
            System.err.println("❌ Erreur getPointsInteret: " + e.getMessage());
        }

        return points;
    }

    // ============================================
    // 🔄 PARSER UN ÉLÉMENT OVERPASS
    // ============================================
    private PointInteret parseElement(JsonObject el) {
        try {
            if (!el.has("tags")) return null;

            JsonObject tags = el.getAsJsonObject("tags");

            // Nom obligatoire
            String nom = tags.has("name") ? tags.get("name").getAsString() : "";
            if (nom.isEmpty() || nom.length() < 2) return null;

            PointInteret point = new PointInteret();
            point.nom = nom;

            // Coordonnées
            if (el.has("lat") && el.has("lon")) {
                point.latitude  = el.get("lat").getAsDouble();
                point.longitude = el.get("lon").getAsDouble();
            }

            // Catégorie et emoji depuis les tags OSM
            String tourism  = tags.has("tourism")  ? tags.get("tourism").getAsString()  : "";
            String historic = tags.has("historic") ? tags.get("historic").getAsString() : "";
            String amenity  = tags.has("natural")  ? tags.get("natural").getAsString()  : "";
            String leisure  = tags.has("leisure")  ? tags.get("leisure").getAsString()  : "";

            point.categorie = determinerCategorie(tourism, historic, amenity, leisure);
            point.emoji     = determinerEmoji(tourism, historic, amenity, leisure);

            // Rating basique
            point.rating = tags.has("stars") ? tags.get("stars").getAsInt() : 2;

            return point;

        } catch (Exception e) {
            return null;
        }
    }

    // ============================================
    // 🏷️ CATÉGORIE
    // ============================================
    private String determinerCategorie(String tourism, String historic,
                                       String amenity, String leisure) {
        if (!tourism.isEmpty()) {
            return switch (tourism) {
                case "museum"       -> "Musée";
                case "attraction"   -> "Attraction touristique";
                case "viewpoint"    -> "Point de vue panoramique";
                case "gallery"      -> "Galerie d'art";
                case "zoo"          -> "Zoo";
                case "theme_park"   -> "Parc d'attractions";
                default             -> "Site touristique";
            };
        }
        if (!historic.isEmpty()) {
            return switch (historic) {
                case "monument"            -> "Monument";
                case "memorial"            -> "Mémorial";
                case "castle"              -> "Château";
                case "ruins"               -> "Ruines historiques";
                case "fort"                -> "Forteresse";
                case "archaeological_site" -> "Site archéologique";
                default                    -> "Site historique";
            };
        }
        if (!leisure.isEmpty()) {
            return switch (leisure) {
                case "park"           -> "Parc";
                case "garden"         -> "Jardin public";
                case "nature_reserve" -> "Réserve naturelle";
                default               -> "Espace de loisir";
            };
        }
        if (!amenity.isEmpty()) return "Lieu populaire";
        return "Point d'intérêt";
    }

    // ============================================
    // 😊 EMOJI
    // ============================================
    private String determinerEmoji(String tourism, String historic,
                                   String amenity, String leisure) {
        if (!tourism.isEmpty()) {
            return switch (tourism) {
                case "museum"      -> "🏛️";
                case "attraction"  -> "🎯";
                case "viewpoint"   -> "🔭";
                case "gallery"     -> "🖼️";
                case "zoo"         -> "🦁";
                case "theme_park"  -> "🎢";
                default            -> "✈️";
            };
        }
        if (!historic.isEmpty()) {
            return switch (historic) {
                case "monument"            -> "🗿";
                case "memorial"            -> "🕊️";
                case "castle"              -> "🏰";
                case "ruins"               -> "🏚️";
                case "fort"                -> "🛡️";
                case "archaeological_site" -> "⛏️";
                default                    -> "🏛️";
            };
        }
        if (!leisure.isEmpty()) {
            return switch (leisure) {
                case "park"           -> "🌳";
                case "garden"         -> "🌸";
                case "nature_reserve" -> "🌿";
                default               -> "🏞️";
            };
        }
        // natural tags
        if (!amenity.isEmpty()) {
            return switch (amenity) {
                case "peak"         -> "⛰️";
                case "waterfall"    -> "💧";
                case "beach"        -> "🏖️";
                case "cave_entrance"-> "🕳️";
                default             -> "🌍";
            };
        }
        return "📍";
    }

    // ============================================
    // 📡 REQUÊTE HTTP GET
    // ============================================
    private String makeRequest(String urlStr, String userAgentHost) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            // ✅ Nominatim exige un User-Agent
            conn.setRequestProperty("User-Agent", "AmeduseApp/1.0 (" + userAgentHost + ")");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("❌ HTTP " + code + " pour: " + urlStr);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            return sb.toString();

        } catch (Exception e) {
            System.err.println("❌ Requête GET échouée: " + e.getMessage());
            return null;
        }
    }

    // ============================================
    // 📡 REQUÊTE HTTP POST (pour Overpass)
    // ============================================
    private String makePostRequest(String urlStr, String body) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", "AmeduseApp/1.0");

            // Envoyer le body
            conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("❌ Overpass HTTP " + code);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            return sb.toString();

        } catch (Exception e) {
            System.err.println("❌ Requête POST Overpass échouée: " + e.getMessage());
            return null;
        }
    }
}