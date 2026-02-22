package com.example.demo1.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 🌤️ WeatherService - OpenWeatherMap API
 * Gratuit : 1000 requêtes/jour
 * Clé API : https://openweathermap.org/api
 */
public class WeatherService {

    // ✅ Remplace par ta clé API OpenWeatherMap (gratuite)
    private static final String API_KEY = "0e9a59d809515554dc2e0b321be5ac37";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    // ============================================
    // 🌡️ MODÈLE DE DONNÉES MÉTÉO
    // ============================================
    public static class WeatherData {
        public String ville;
        public String pays;
        public double temperature;
        public double temperatureMin;
        public double temperatureMax;
        public int humidite;
        public String description;
        public String icone;
        public double vitesseVent;
        public String emoji;

        @Override
        public String toString() {
            return emoji + " " + description + " | " +
                    String.format("%.0f°C", temperature) +
                    " (Min: " + String.format("%.0f°C", temperatureMin) +
                    " / Max: " + String.format("%.0f°C", temperatureMax) + ")";
        }
    }

    // ============================================
    // 🔍 OBTENIR LA MÉTÉO PAR VILLE
    // ============================================
    public WeatherData getMeteo(String ville) {
        try {
            String encodedVille = URLEncoder.encode(ville, StandardCharsets.UTF_8);
            String urlStr = BASE_URL + "?q=" + encodedVille +
                    "&appid=" + API_KEY +
                    "&units=metric" +
                    "&lang=fr";

            String response = makeRequest(urlStr);
            if (response == null) return null;

            return parseResponse(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur WeatherService: " + e.getMessage());
            return null;
        }
    }

    // ============================================
    // 🔍 OBTENIR LA MÉTÉO PAR COORDONNÉES
    // ============================================
    public WeatherData getMeteoParCoordonnees(double lat, double lon) {
        try {
            String urlStr = BASE_URL + "?lat=" + lat + "&lon=" + lon +
                    "&appid=" + API_KEY +
                    "&units=metric" +
                    "&lang=fr";

            String response = makeRequest(urlStr);
            if (response == null) return null;

            return parseResponse(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur WeatherService coordonnées: " + e.getMessage());
            return null;
        }
    }

    // ============================================
    // 📡 FAIRE LA REQUÊTE HTTP
    // ============================================
    private String makeRequest(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ HTTP Error: " + responseCode);
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
            System.err.println("❌ Requête échouée: " + e.getMessage());
            return null;
        }
    }

    // ============================================
    // 🔄 PARSER LA RÉPONSE JSON
    // ============================================
    private WeatherData parseResponse(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            WeatherData data = new WeatherData();

            // Ville et pays
            data.ville = root.get("name").getAsString();
            data.pays = root.getAsJsonObject("sys").get("country").getAsString();

            // Températures
            JsonObject main = root.getAsJsonObject("main");
            data.temperature = main.get("temp").getAsDouble();
            data.temperatureMin = main.get("temp_min").getAsDouble();
            data.temperatureMax = main.get("temp_max").getAsDouble();
            data.humidite = main.get("humidity").getAsInt();

            // Description et icône
            JsonObject weather = root.getAsJsonArray("weather").get(0).getAsJsonObject();
            data.description = capitaliser(weather.get("description").getAsString());
            data.icone = weather.get("icon").getAsString();

            // Vent
            data.vitesseVent = root.getAsJsonObject("wind").get("speed").getAsDouble();

            // Emoji selon la météo
            data.emoji = getEmoji(weather.get("main").getAsString());

            return data;

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing JSON météo: " + e.getMessage());
            return null;
        }
    }

    // ============================================
    // 😊 EMOJI SELON MÉTÉO
    // ============================================
    private String getEmoji(String condition) {
        switch (condition.toLowerCase()) {
            case "clear":       return "☀️";
            case "clouds":      return "☁️";
            case "rain":        return "🌧️";
            case "drizzle":     return "🌦️";
            case "thunderstorm":return "⛈️";
            case "snow":        return "❄️";
            case "mist":
            case "fog":         return "🌫️";
            case "haze":        return "🌁";
            default:            return "🌤️";
        }
    }

    private String capitaliser(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}