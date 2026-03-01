package org.example.services;

public class MapService {

    // No API key required for Leaflet/OpenStreetMap.

    /**
     * Mock method to calculate distance.
     * In a real implementation, this would call Google Distance Matrix API.
     */
    public double calculateDistance(String origin, String destination) {
        // Mocking: return a random distance between 5 and 50 km for demo
        return Math.random() * 45 + 5;
    }

    /**
     * Mock method to calculate estimated time.
     */
    public String calculateEstimatedTime(String origin, String destination) {
        double dist = calculateDistance(origin, destination);
        int mins = (int) (dist * 2); // Roughly 2 mins per km
        return mins + " mins";
    }
}
