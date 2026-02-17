package com.example.demo1.services;

import com.example.demo1.entity.Destination;
import com.example.demo1.entity.Vol;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AmadeusService {

    private VolService volService;  // ⬅️ SERVICE POUR BD

    public AmadeusService() {
        this.volService = new VolService();  // ⬅️ INITIALISER VolService
    }

    // ========================================
    // 🔑 CLÉS API AMADEUS
    // ========================================
    private static final String API_KEY = "RdEtkKg789RkB19nGAXyVZRD9RKA2fus";
    private static final String API_SECRET = "Yg3vGjGZBNZ7QZ8Z";

    // ========================================
    // 🌐 URLS DE L'API AMADEUS (DYNAMIQUE)
    // ========================================
    private static final String AUTH_URL = "https://test.api.amadeus.com/v1/security/oauth2/token";
    private static final String SEARCH_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";
    private static final String INSPIRATION_URL = "https://test.api.amadeus.com/v1/shopping/flight-destinations";

    private static String accessToken = null;

    // ========================================
    // ✅ AUTHENTIFICATION API (DYNAMIQUE)
    // ========================================
    private static String getAccessToken() {
        if (accessToken != null) {
            return accessToken;
        }

        try {
            System.out.println("🔐 Authentification API Amadeus...");

            HttpClient client = HttpClient.newHttpClient();

            Map<String, String> params = new HashMap<>();
            params.put("grant_type", "client_credentials");
            params.put("client_id", API_KEY);
            params.put("client_secret", API_SECRET);

            String formData = buildFormData(params);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                accessToken = json.get("access_token").getAsString();
                System.out.println("✅ Token d'authentification obtenu depuis l'API");
                return accessToken;
            } else {
                System.err.println("❌ Erreur auth API: " + response.statusCode());
                System.err.println(response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur d'authentification API:");
            e.printStackTrace();
        }

        return null;
    }

    // ========================================
    // ✅ DESTINATIONS DYNAMIQUES DEPUIS L'API
    // ========================================
    public static List<Destination> getDestinationsInspirations(String origine) {
        System.out.println("========================================");
        System.out.println("🌍 Récupération des destinations depuis l'API Amadeus");
        System.out.println("📍 Origine: " + origine);
        System.out.println("========================================");

        List<Destination> destinations = new ArrayList<>();

        try {
            String token = getAccessToken();
            if (token == null) {
                System.err.println("❌ Impossible d'obtenir le token API");
                return destinations;
            }

            HttpClient client = HttpClient.newHttpClient();

            // URL simplifiée pour éviter les erreurs 500
            String url = String.format(
                    "%s?origin=%s&maxPrice=2000",
                    INSPIRATION_URL, origine
            );

            System.out.println("📡 Appel API Flight Inspiration...");
            System.out.println("🔗 URL: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("📊 Code réponse API: " + response.statusCode());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

                if (jsonObject.has("data")) {
                    JsonArray data = jsonObject.getAsJsonArray("data");
                    System.out.println("✅ L'API a retourné " + data.size() + " destinations");

                    int count = 0;
                    for (JsonElement element : data) {
                        if (count >= 12) break;

                        try {
                            JsonObject destData = element.getAsJsonObject();

                            String codeIATA = destData.get("destination").getAsString();
                            double prix = destData.getAsJsonObject("price").get("total").getAsDouble();

                            Map<String, String> infos = getInfosDestination(codeIATA);

                            Destination destination = creerDestination(
                                    infos.get("ville"),
                                    infos.get("pays"),
                                    codeIATA,
                                    prix,
                                    count
                            );

                            destinations.add(destination);
                            count++;

                            System.out.println("   ✅ [" + count + "] " + infos.get("ville") +
                                    " (" + codeIATA + ") : " +
                                    String.format("%.2f EUR", prix));

                        } catch (Exception e) {
                            System.err.println("   ⚠️ Erreur parsing destination: " + e.getMessage());
                            continue;
                        }
                    }
                } else {
                    System.out.println("⚠️ L'API n'a retourné aucune donnée");
                }

            } else if (response.statusCode() == 400) {
                System.err.println("❌ Erreur 400 - Paramètres invalides");
                System.err.println("📋 Réponse: " + response.body());
                System.out.println("💡 Suggestion: Vérifiez le code IATA: " + origine);

            } else if (response.statusCode() == 500) {
                System.err.println("❌ Erreur 500 - Erreur serveur API");
                System.err.println("📋 Réponse: " + response.body());
                System.out.println("💡 L'API Flight Inspiration ne supporte peut-être pas cet aéroport");
                System.out.println("🔄 Tentative avec la méthode alternative...");

                // Fallback vers recherche directe
                destinations = getDestinationsParRecherche(origine);

            } else {
                System.err.println("❌ Erreur API " + response.statusCode());
                System.err.println(response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'appel API: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("========================================");
        System.out.println("✅ Total: " + destinations.size() + " destinations chargées");
        System.out.println("========================================");

        return destinations;
    }

    // ========================================
    // ✅ MÉTHODE ALTERNATIVE : Recherche directe
    // ========================================
    /**
     * Recherche directe de vols vers des destinations populaires
     * Utilisée quand l'API Flight Inspiration retourne une erreur 500
     */
    private static List<Destination> getDestinationsParRecherche(String origine) {
        System.out.println("🔄 Utilisation de la méthode de recherche directe...");

        List<Destination> destinations = new ArrayList<>();

        // Liste des destinations populaires à tester
        String[][] destinationsTest = {
                {"CDG", "Paris", "France"},
                {"FCO", "Rome", "Italie"},
                {"BCN", "Barcelone", "Espagne"},
                {"IST", "Istanbul", "Turquie"},
                {"DXB", "Dubaï", "Émirats Arabes Unis"},
                {"MAD", "Madrid", "Espagne"},
                {"LHR", "Londres", "Royaume-Uni"},
                {"CAI", "Le Caire", "Égypte"},
                {"AMS", "Amsterdam", "Pays-Bas"},
                {"LIS", "Lisbonne", "Portugal"},
                {"ATH", "Athènes", "Grèce"},
                {"PRG", "Prague", "République Tchèque"}
        };

        LocalDate dateDansUnMois = LocalDate.now().plusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = dateDansUnMois.format(formatter);

        int maxDestinations = 12;
        int index = 0;

        // ⬅️ CRÉER UNE INSTANCE POUR LA RECHERCHE
        AmadeusService service = new AmadeusService();

        while (destinations.size() < maxDestinations && index < destinationsTest.length) {
            String[] destInfo = destinationsTest[index];
            index++;

            String codeIATA = destInfo[0];
            String nom = destInfo[1];
            String pays = destInfo[2];

            try {
                System.out.println("🔍 [" + (destinations.size() + 1) + "/" + maxDestinations +
                        "] Test de " + nom + " (" + codeIATA + ")...");

                // ⬅️ UTILISER L'INSTANCE NON-STATIQUE
                List<Vol> vols = service.rechercherVols(origine, codeIATA, date, 1);

                if (!vols.isEmpty()) {
                    double prixMin = vols.stream()
                            .mapToDouble(Vol::getPrix)
                            .min()
                            .orElse(0.0);

                    Destination destination = creerDestination(
                            nom, pays, codeIATA, prixMin, destinations.size()
                    );
                    destinations.add(destination);

                    System.out.println("   ✅ " + nom + " ajouté : " +
                            String.format("%.2f EUR", prixMin));
                } else {
                    System.out.println("   ⚠️ Aucun vol disponible");
                }

                Thread.sleep(500);

            } catch (Exception e) {
                System.err.println("   ❌ Erreur: " + e.getMessage());
            }
        }

        return destinations;
    }

    // ========================================
    // ✅ RECHERCHE DE VOLS DYNAMIQUE - AVEC SAUVEGARDE EN BD
    // ========================================
    public List<Vol> rechercherVols(String origine, String destination,
                                    String dateDepart, int adultes) {
        List<Vol> vols = new ArrayList<>();

        try {
            System.out.println("🔍 Recherche de vols dans l'API Amadeus...");
            System.out.println("   De: " + origine + " → Vers: " + destination);
            System.out.println("   Date: " + dateDepart + " | Adultes: " + adultes);

            String token = getAccessToken();
            if (token == null) {
                System.err.println("❌ Token API non disponible");
                return vols;
            }

            HttpClient client = HttpClient.newHttpClient();

            String url = String.format(
                    "%s?originLocationCode=%s&destinationLocationCode=%s&departureDate=%s&adults=%d&max=10&currencyCode=EUR",
                    SEARCH_URL, origine, destination, dateDepart, adultes
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

                if (jsonObject.has("data")) {
                    JsonArray data = jsonObject.getAsJsonArray("data");
                    System.out.println("✅ L'API a trouvé " + data.size() + " vols");

                    for (int i = 0; i < data.size(); i++) {
                        try {
                            JsonObject offer = data.get(i).getAsJsonObject();

                            double prix = offer.getAsJsonObject("price").get("total").getAsDouble();
                            String devise = offer.getAsJsonObject("price").get("currency").getAsString();

                            JsonArray itineraries = offer.getAsJsonArray("itineraries");
                            JsonObject firstItinerary = itineraries.get(0).getAsJsonObject();
                            JsonArray segments = firstItinerary.getAsJsonArray("segments");

                            JsonObject firstSegment = segments.get(0).getAsJsonObject();
                            JsonObject lastSegment = segments.get(segments.size() - 1).getAsJsonObject();

                            String compagnie = firstSegment.get("carrierCode").getAsString();
                            int escales = segments.size() - 1;

                            String departureDateTime = firstSegment.getAsJsonObject("departure").get("at").getAsString();
                            String arrivalDateTime = lastSegment.getAsJsonObject("arrival").get("at").getAsString();

                            String dateDepart2 = departureDateTime.substring(0, 10);
                            String heureDepart = departureDateTime.substring(11, 16);
                            String dateArrivee = arrivalDateTime.substring(0, 10);
                            String heureArrivee = arrivalDateTime.substring(11, 16);

                            String duree = firstItinerary.get("duration").getAsString()
                                    .replace("PT", "").replace("H", "h").replace("M", "m");

                            Vol vol = new Vol(
                                    compagnie, origine, destination,
                                    dateDepart2, dateArrivee,
                                    heureDepart, heureArrivee,
                                    prix, devise, escales, duree
                            );

                            // ✅ ✅ ✅ SAUVEGARDER LE VOL DANS LA BASE DE DONNÉES ✅ ✅ ✅
//                            try {
//                                volService.create(vol);
//                                System.out.println("   💾 Vol sauvegardé en BD avec ID: " + vol.getId());
//                            } catch (Exception e) {
//                                System.err.println("   ⚠️ Erreur sauvegarde BD: " + e.getMessage());
//                            }

                            vols.add(vol);

                            System.out.println("   ✅ Vol " + (i+1) + ": " + compagnie +
                                    " - " + prix + " " + devise +
                                    " - " + escales + " escale(s)");

                        } catch (Exception e) {
                            System.err.println("   ⚠️ Erreur parsing vol: " + e.getMessage());
                            continue;
                        }
                    }
                }
            } else {
                System.err.println("❌ Erreur API: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur recherche vols: " + e.getMessage());
        }

        System.out.println("📊 Résultat: " + vols.size() + " vols trouvés et sauvegardés");
        return vols;
    }

    // ========================================
    // 🛠️ MÉTHODES UTILITAIRES
    // ========================================

    private static Destination creerDestination(String nom, String pays, String codeIATA,
                                                double prix, int index) {
        Destination destination = new Destination(
                nom,
                pays,
                codeIATA,
                getDescription(nom),
                getImageUrl(codeIATA),
                determinerCategorie(index, prix),
                prix < 300,
                prix < 300 ? calculerReduction(prix) : 0
        );

        destination.setPrixMin(prix);
        destination.setDevise("EUR");

        return destination;
    }

    private static Map<String, String> getInfosDestination(String iata) {
        Map<String, String> infos = new HashMap<>();

        Map<String, String[]> mapping = new HashMap<>();
        mapping.put("CDG", new String[]{"Paris", "France"});
        mapping.put("ORY", new String[]{"Paris", "France"});
        mapping.put("FCO", new String[]{"Rome", "Italie"});
        mapping.put("BCN", new String[]{"Barcelone", "Espagne"});
        mapping.put("MAD", new String[]{"Madrid", "Espagne"});
        mapping.put("LHR", new String[]{"Londres", "Royaume-Uni"});
        mapping.put("IST", new String[]{"Istanbul", "Turquie"});
        mapping.put("DXB", new String[]{"Dubaï", "Émirats Arabes Unis"});
        mapping.put("CAI", new String[]{"Le Caire", "Égypte"});
        mapping.put("AMS", new String[]{"Amsterdam", "Pays-Bas"});
        mapping.put("MXP", new String[]{"Milan", "Italie"});
        mapping.put("ATH", new String[]{"Athènes", "Grèce"});
        mapping.put("LIS", new String[]{"Lisbonne", "Portugal"});
        mapping.put("PRG", new String[]{"Prague", "République Tchèque"});
        mapping.put("VIE", new String[]{"Vienne", "Autriche"});
        mapping.put("BRU", new String[]{"Bruxelles", "Belgique"});
        mapping.put("ZRH", new String[]{"Zurich", "Suisse"});
        mapping.put("MUC", new String[]{"Munich", "Allemagne"});
        mapping.put("FRA", new String[]{"Francfort", "Allemagne"});

        if (mapping.containsKey(iata)) {
            infos.put("ville", mapping.get(iata)[0]);
            infos.put("pays", mapping.get(iata)[1]);
        } else {
            infos.put("ville", iata);
            infos.put("pays", "Destination");
        }

        return infos;
    }


    public static List<Map<String, String>> rechercherAeroports(String keyword) {
        List<Map<String, String>> aeroports = new ArrayList<>();

        try {
            String token = getAccessToken();
            if (token == null) {
                System.err.println("❌ Token d'authentification non disponible");
                return aeroports;
            }

            HttpClient client = HttpClient.newHttpClient();

            // API Amadeus Airport & City Search
            // Cherche dans les noms de villes ET de pays
            String url = String.format(
                    "https://test.api.amadeus.com/v1/reference-data/locations?subType=CITY,AIRPORT&keyword=%s&page[limit]=15&sort=analytics.travelers.score&view=FULL",
                    URLEncoder.encode(keyword, StandardCharsets.UTF_8)
            );

            System.out.println("🔍 Recherche API pour: " + keyword);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

                if (jsonObject.has("data")) {
                    JsonArray data = jsonObject.getAsJsonArray("data");

                    System.out.println("✅ " + data.size() + " résultats trouvés");

                    for (JsonElement element : data) {
                        JsonObject location = element.getAsJsonObject();

                        Map<String, String> aeroport = new HashMap<>();

                        // Code IATA
                        String iataCode = location.get("iataCode").getAsString();
                        aeroport.put("code", iataCode);

                        // Nom de l'aéroport/ville
                        String name = location.get("name").getAsString();
                        aeroport.put("name", name);

                        // Type (CITY ou AIRPORT)
                        String subType = location.get("subType").getAsString();
                        aeroport.put("type", subType);

                        // Détails géographiques
                        if (location.has("address")) {
                            JsonObject address = location.getAsJsonObject("address");

                            String cityName = address.has("cityName") ?
                                    address.get("cityName").getAsString() : "";
                            String countryName = address.has("countryName") ?
                                    address.get("countryName").getAsString() : "";
                            String countryCode = address.has("countryCode") ?
                                    address.get("countryCode").getAsString() : "";

                            aeroport.put("city", cityName);
                            aeroport.put("country", countryName);
                            aeroport.put("countryCode", countryCode);

                            // Texte d'affichage complet
                            String displayText = construireTexteAffichage(
                                    name, iataCode, cityName, countryName, subType
                            );
                            aeroport.put("display", displayText);

                        } else {
                            aeroport.put("display", String.format("%s (%s)", name, iataCode));
                        }

                        aeroports.add(aeroport);
                    }
                }
            } else if (response.statusCode() == 401) {
                System.err.println("❌ Token expiré, renouvellement...");
                accessToken = null; // Forcer le renouvellement
            } else {
                System.err.println("❌ Erreur API: " + response.statusCode());
                System.err.println("Réponse: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche d'aéroports:");
            e.printStackTrace();
        }

        return aeroports;
    }


    private static String construireTexteAffichage(String name, String iataCode,
                                                   String city, String country, String type) {
        StringBuilder display = new StringBuilder();

        // Icône selon le type
        String icone = type.equals("CITY") ? "🏙️" : "✈️";
        display.append(icone).append(" ");

        // Nom principal
        display.append(name);

        // Code IATA
        display.append(" (").append(iataCode).append(")");

        // Localisation
        if (!city.isEmpty() && !city.equals(name)) {
            display.append(" - ").append(city);
        }

        if (!country.isEmpty()) {
            display.append(", ").append(country);
        }

        return display.toString();
    }

    private static String buildFormData(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }

    private static String getDescription(String ville) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("Paris", "La ville lumière vous attend avec ses monuments iconiques.");
        descriptions.put("Rome", "L'éternelle cité avec le Colisée et le Vatican.");
        descriptions.put("Istanbul", "Entre Orient et Occident, une ville fascinante.");
        descriptions.put("Barcelone", "Architecture Gaudí et plages méditerranéennes.");
        descriptions.put("Londres", "Royauté britannique et musées mondiaux.");
        descriptions.put("Dubaï", "Luxe, gratte-ciels et désert.");
        descriptions.put("Madrid", "Capitale vibrante d'Espagne.");
        descriptions.put("Le Caire", "Pyramides de Gizeh et trésors égyptiens.");
        descriptions.put("Amsterdam", "Canaux romantiques et musées.");
        descriptions.put("Lisbonne", "Tramways et pastéis de nata.");
        descriptions.put("Milan", "Capitale de la mode italienne.");
        descriptions.put("Athènes", "Berceau de la civilisation.");
        descriptions.put("Prague", "Ville aux cent clochers.");
        descriptions.put("Vienne", "Capitale de la musique classique.");
        descriptions.put("Bruxelles", "Chocolat et Grand-Place.");

        return descriptions.getOrDefault(ville, "Découvrez cette destination magnifique.");
    }

    private static String getImageUrl(String codeIATA) {
        Map<String, String> images = new HashMap<>();
        images.put("CDG", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800");
        images.put("FCO", "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=800");
        images.put("BCN", "https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800");
        images.put("IST", "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?w=800");
        images.put("DXB", "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800");
        images.put("MAD", "https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=800");
        images.put("LHR", "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800");
        images.put("CAI", "https://images.unsplash.com/photo-1572252009286-268acec5ca0a?w=800");
        images.put("AMS", "https://images.unsplash.com/photo-1534351590666-13e3e96b5017?w=800");
        images.put("LIS", "https://images.unsplash.com/photo-1555881400-74d7acaacd8b?w=800");

        return images.getOrDefault(codeIATA, "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800");
    }

    private static String determinerCategorie(int index, double prix) {
        if (prix > 0 && prix < 300) return "promo";
        if (index < 4) return "populaire";
        return "nouveau";
    }

    private static int calculerReduction(double prix) {
        if (prix < 150) return 30;
        if (prix < 250) return 20;
        if (prix < 300) return 15;
        return 10;
    }
}