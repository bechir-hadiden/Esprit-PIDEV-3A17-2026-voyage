package org.example.demo10.service;

import org.example.demo10.model.Voyage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VoyageService {

    private static List<Voyage> voyages = new ArrayList<>();

    // Initialisation des données statiques
    static {
        initialiserVoyages();
    }

    private static void initialiserVoyages() {
        voyages.add(new Voyage(
                1, "Paris - Ville Lumière",
                "Découvrez la magie de Paris : Tour Eiffel, Louvre, Montmartre et croisière sur la Seine. Un séjour romantique inoubliable.",
                899.00, 5,
                "https://images.unsplash.com/photo-1502602898657-3b917ce6c1b8?w=400",
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(20),
                12
        ));

        voyages.add(new Voyage(
                2, "Rome et la Dolce Vita",
                "Visitez le Colisée, le Vatican, la Fontaine de Trévi et dégustez les meilleures pâtes de Rome. Histoire et gastronomie.",
                1099.00, 7,
                "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=400",
                LocalDate.now().plusDays(25),
                LocalDate.now().plusDays(32),
                8
        ));

        voyages.add(new Voyage(
                3, "Barcelone - Gaudí et Méditerranée",
                "Sagrada Familia, Parc Güell, Ramblas et plages. L'art et la mer pour des vacances parfaites.",
                949.00, 6,
                "https://images.unsplash.com/photo-1583422409514-88d7dd2e3e7c?w=400",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(16),
                5
        ));

        voyages.add(new Voyage(
                4, "Londres - Royal Experience",
                "Big Ben, London Eye, Buckingham Palace, West End. La capitale britannique vous attend.",
                1199.00, 6,
                "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=400",
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(26),
                3
        ));

        voyages.add(new Voyage(
                5, "Venise - Cité des Doges",
                "Gondoles, canaux, place Saint-Marc et palais vénitiens. Un voyage de rêve dans la cité romantique.",
                1299.00, 5,
                "https://images.unsplash.com/photo-1523906834658-6e24e2384f88?w=400",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(35),
                0
        ));

        voyages.add(new Voyage(
                6, "Santorini - Île des Merveilles",
                "Couchers de soleil, villages blancs et bleus, plages volcaniques. Le paradis sur terre.",
                1599.00, 8,
                "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?w=400",
                LocalDate.now().plusDays(40),
                LocalDate.now().plusDays(48),
                15
        ));

        voyages.add(new Voyage(
                7, "New York - The Big Apple",
                "Times Square, Central Park, Statue de la Liberté, Broadway. La ville qui ne dort jamais.",
                1899.00, 7,
                "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=400",
                LocalDate.now().plusDays(35),
                LocalDate.now().plusDays(42),
                4
        ));

        voyages.add(new Voyage(
                8, "Tokyo - Futur et Tradition",
                "Temples anciens, gratte-ciels futuristes, gastronomie unique. Le Japon authentique.",
                2199.00, 9,
                "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=400",
                LocalDate.now().plusDays(50),
                LocalDate.now().plusDays(59),
                6
        ));
    }

    public List<Voyage> getAllVoyages() {
        return new ArrayList<>(voyages);
    }

    public Voyage getVoyageById(int id) {
        return voyages.stream()
                .filter(v -> v.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Voyage> rechercherVoyages(String recherche) {
        if (recherche == null || recherche.trim().isEmpty()) {
            return getAllVoyages();
        }

        String rechercheLower = recherche.toLowerCase().trim();
        return voyages.stream()
                .filter(v -> v.getDestination().toLowerCase().contains(rechercheLower) ||
                        v.getDescription().toLowerCase().contains(rechercheLower))
                .toList();
    }

    public List<Voyage> filtrerParPrix(double prixMax) {
        return voyages.stream()
                .filter(v -> v.getPrix() <= prixMax)
                .toList();
    }

    public List<Voyage> filtrerParDuree(int dureeMax) {
        return voyages.stream()
                .filter(v -> v.getDuree() <= dureeMax)
                .toList();
    }

    public List<Voyage> getVoyagesDisponibles() {
        return voyages.stream()
                .filter(v -> v.getPlacesDisponibles() > 0)
                .toList();
    }

    public boolean mettreAJourPlaces(int voyageId, int nombrePersonnes) {
        Voyage voyage = getVoyageById(voyageId);
        if (voyage != null && voyage.getPlacesDisponibles() >= nombrePersonnes) {
            voyage.setPlacesDisponibles(voyage.getPlacesDisponibles() - nombrePersonnes);
            return true;
        }
        return false;
    }
}