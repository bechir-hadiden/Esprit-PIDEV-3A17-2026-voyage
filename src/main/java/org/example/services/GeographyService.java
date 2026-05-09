package org.example.services;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GeographyService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double FALLBACK_DISTANCE_KM = 50.0;
    private static final Map<String, Coordinates> cityCoords = new HashMap<>();

    static {
        cityCoords.put("TUNIS", new Coordinates(36.8065, 10.1815));
        cityCoords.put("ARIANA", new Coordinates(36.8665, 10.1647));
        cityCoords.put("BEN AROUS", new Coordinates(36.7531, 10.2189));
        cityCoords.put("MANOUBA", new Coordinates(36.8091, 10.0963));
        cityCoords.put("SOUSSE", new Coordinates(35.8256, 10.6369));
        cityCoords.put("SFAX", new Coordinates(34.7406, 10.7603));
        cityCoords.put("BIZERTE", new Coordinates(37.2744, 9.8739));
        cityCoords.put("GABES", new Coordinates(33.8815, 10.0982));
        cityCoords.put("MONASTIR", new Coordinates(35.7780, 10.8262));
        cityCoords.put("HAMMAMET", new Coordinates(36.4000, 10.6167));
        cityCoords.put("DJERBA", new Coordinates(33.8075, 10.8451));
        cityCoords.put("TOZEUR", new Coordinates(33.9197, 8.1335));
        cityCoords.put("KAIROUAN", new Coordinates(35.6781, 10.0963));
        cityCoords.put("NABEUL", new Coordinates(36.4561, 10.7376));
        cityCoords.put("BORJ CEDRIA", new Coordinates(36.7280, 10.4150));
        cityCoords.put("MAHDIA", new Coordinates(35.5047, 11.0622));
        cityCoords.put("KASSERINE", new Coordinates(35.1681, 8.8365));
        cityCoords.put("SIDI BOUZID", new Coordinates(35.0382, 9.4858));
        cityCoords.put("GAFSA", new Coordinates(34.4250, 8.7842));
        cityCoords.put("KEBILI", new Coordinates(33.7044, 8.9690));
        cityCoords.put("MEDENINE", new Coordinates(33.3398, 10.4910));
        cityCoords.put("TATAOUINE", new Coordinates(32.9297, 10.4518));
        cityCoords.put("BEJA", new Coordinates(36.7256, 9.1817));
        cityCoords.put("JENDOUBA", new Coordinates(36.5011, 8.7802));
        cityCoords.put("LE KEF", new Coordinates(36.1747, 8.7049));
        cityCoords.put("SILIANA", new Coordinates(36.0849, 9.3708));
        cityCoords.put("ZAGHOUAN", new Coordinates(36.4029, 10.1429));
    }

    public static class Coordinates {
        public double lat, lon;

        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    public Coordinates getCoordinates(String city) {
        Coordinates c = cityCoords.get(normalizeCityKey(city));
        if (c == null) {
            return null;
        }
        return new Coordinates(c.lat, c.lon);
    }

    public boolean isValidCoordinate(double lat, double lon) {
        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            return false;
        }
        if (Math.abs(lat) < 0.000001 && Math.abs(lon) < 0.000001) {
            return false;
        }
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    /** Returns distance in KM using Haversine formula. */
    public double calculateDistance(String cityA, String cityB) {
        Coordinates c1 = cityCoords.get(normalizeCityKey(cityA));
        Coordinates c2 = cityCoords.get(normalizeCityKey(cityB));

        if (c1 == null || c2 == null)
            return FALLBACK_DISTANCE_KM; // Fallback if one city is unknown

        return calculateDistance(c1.lat, c1.lon, c2.lat, c2.lon);
    }

    public double calculateDistance(double latA, double lonA, double latB, double lonB) {
        double dLat = Math.toRadians(latB - latA);
        double dLon = Math.toRadians(lonB - lonA);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latA)) * Math.cos(Math.toRadians(latB)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private String normalizeCityKey(String city) {
        if (city == null) {
            return "";
        }
        String normalized = Normalizer.normalize(city, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.trim().toUpperCase(Locale.ROOT);
    }

    /** Returns estimated time in minutes. */
    public int estimateTravelTime(double distanceKm, String vehicleType) {
        double avgSpeed = 80.0; // Default km/h
        String normalizedType = vehicleType == null ? "" : vehicleType.trim().toLowerCase(Locale.ROOT);
        switch (normalizedType) {
            case "bus":
                avgSpeed = 65.0;
                break;
            case "taxi":
                avgSpeed = 95.0;
                break;
            case "voiture":
                avgSpeed = 90.0;
                break;
            case "scooter":
                avgSpeed = 40.0;
                break;
        }

        double hours = distanceKm / avgSpeed;
        return (int) (hours * 60);
    }

    public String formatTime(int minutes) {
        if (minutes < 60)
            return minutes + " min";
        int h = minutes / 60;
        int m = minutes % 60;
        return h + "h " + m + "min";
    }
}
