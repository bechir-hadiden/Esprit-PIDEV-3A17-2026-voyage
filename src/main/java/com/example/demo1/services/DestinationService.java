package com.example.demo1.services;

import com.example.demo1.entity.Destination;
import java.util.List;

public class DestinationService {

    private static final String ORIGINE_PAR_DEFAUT = "TUN"; // Depuis Tunis

    // Récupérer les destinations dynamiquement depuis l'API
    public static List<Destination> getDestinationsPopulaires() {
        System.out.println("🌍 Chargement des destinations depuis l'API Amadeus...");
        return AmadeusService.getDestinationsInspirations(ORIGINE_PAR_DEFAUT);
    }

    // Changer l'origine
    public static List<Destination> getDestinationsDepuis(String origine) {
        System.out.println("🌍 Chargement des destinations depuis " + origine + "...");
        return AmadeusService.getDestinationsInspirations(origine);
    }
}