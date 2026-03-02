package org.example.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherService {

    // Using a public API key for demonstration (OpenWeatherMap)
    // In a real production app, this should be moved to env variables
    private static final String API_KEY = "bd5e378503939dda5b56f11962383651"; // Demo key
    private static final String API_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static class WeatherData {
        public double temp;
        public String condition;
        public boolean isRainy;
        public double windSpeed;

        public WeatherData(double temp, String condition, boolean isRainy, double windSpeed) {
            this.temp = temp;
            this.condition = condition;
            this.isRainy = isRainy;
            this.windSpeed = windSpeed;
        }

        @Override
        public String toString() {
            return String.format("%.1f°C, %s %s", temp, condition, isRainy ? "🌧️" : "☀️");
        }
    }

    private static final Map<String, WeatherData> cache = new HashMap<>();

    public WeatherData getWeather(String city) {
        if (city == null || city.isEmpty())
            return new WeatherData(20, "Clear", false, 5);

        // Return cached data if available (simple TTL not implemented for brevity)
        if (cache.containsKey(city))
            return cache.get(city);

        try {
            String url = String.format("%s?q=%s,TN&units=metric&appid=%s", API_BASE_URL, city.replace(" ", "%20"),
                    API_KEY);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();

                // Simple regex parsing to avoid adding external JSON dependencies if not
                // present
                double temp = parseDouble(body, "\"temp\":", ",");
                String condition = parseString(body, "\"main\":\"", "\"");
                boolean isRainy = body.toLowerCase().contains("rain") || body.toLowerCase().contains("drizzle");
                double wind = parseDouble(body, "\"speed\":", "}");

                WeatherData data = new WeatherData(temp, condition, isRainy, wind);
                cache.put(city, data);
                return data;
            }
        } catch (Exception e) {
            System.err.println("Weather API Error: " + e.getMessage());
        }

        // Fallback data for Tunisia if API fails
        return new WeatherData(22, "Sunny", false, 10);
    }

    private double parseDouble(String json, String key, String separator) {
        try {
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf(separator, start);
            return Double.parseDouble(json.substring(start, end));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String parseString(String json, String key, String endDelimiter) {
        try {
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf(endDelimiter, start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
