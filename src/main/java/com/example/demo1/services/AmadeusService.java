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

    private VolService volService;

    // ========================================
    // 🔑 CLÉS API (STATIC OK)
    // ========================================
    private static final String API_KEY = "RdEtkKg789RkB19nGAXyVZRD9RKA2fus";
    private static final String API_SECRET = "Yg3vGjGZBNZ7QZ8Z";
    private static final String AUTH_URL = "https://test.api.amadeus.com/v1/security/oauth2/token";
    private static final String SEARCH_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";
    private static final String INSPIRATION_URL = "https://test.api.amadeus.com/v1/shopping/flight-destinations";

    private String accessToken = null;  // ⬅️ NON STATIC

    // ========================================
    // 🏗️ CONSTRUCTEUR
    // ========================================
    public AmadeusService() {
        this.volService = new VolService();
        System.out.println("✅ AmadeusService initialisé");
    }

    // ========================================
    // 🔐 AUTHENTIFICATION (NON STATIC)
    // ========================================
    private String getAccessToken() {  // ⬅️ RETIRER static
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
                System.out.println("✅ Token obtenu");
                return accessToken;
            } else {
                System.err.println("❌ Erreur auth: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur authentification: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ========================================
    // 🌍 DESTINATIONS INSPIRATIONS (NON STATIC)
    // ========================================
    public List<Destination> getDestinationsInspirations(String origine) {  // ⬅️ RETIRER static
        System.out.println("========================================");
        System.out.println("🌍 Récupération destinations depuis API Amadeus");
        System.out.println("📍 Origine: " + origine);
        System.out.println("========================================");

        List<Destination> destinations = new ArrayList<>();

        try {
            String token = getAccessToken();
            if (token == null) {
                System.err.println("❌ Token non disponible");
                return destinations;
            }

            HttpClient client = HttpClient.newHttpClient();

            String url = String.format(
                    "%s?origin=%s&maxPrice=2000",
                    INSPIRATION_URL, origine
            );

            System.out.println("📡 Appel API Flight Inspiration...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("📊 Code réponse: " + response.statusCode());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

                if (jsonObject.has("data")) {
                    JsonArray data = jsonObject.getAsJsonArray("data");
                    System.out.println("✅ " + data.size() + " destinations trouvées");

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
                                    " (" + codeIATA + ") : " + String.format("%.2f EUR", prix));

                        } catch (Exception e) {
                            System.err.println("   ⚠️ Erreur: " + e.getMessage());
                            continue;
                        }
                    }
                }

            } else if (response.statusCode() == 500) {
                System.err.println("❌ Erreur 500 - Fallback...");
                destinations = getDestinationsParRecherche(origine);

            } else {
                System.err.println("❌ Erreur API: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("========================================");
        System.out.println("✅ Total: " + destinations.size() + " destinations");
        System.out.println("========================================");

        return destinations;
    }

    // ========================================
    // 🔄 MÉTHODE ALTERNATIVE (NON STATIC)
    // ========================================
    private List<Destination> getDestinationsParRecherche(String origine) {  // ⬅️ RETIRER static
        System.out.println("🔄 Méthode alternative...");

        List<Destination> destinations = new ArrayList<>();

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
                {"LIS", "Lisbonne", "Portugal"}
        };

        LocalDate dateDansUnMois = LocalDate.now().plusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = dateDansUnMois.format(formatter);

        int maxDestinations = 12;
        int index = 0;

        while (destinations.size() < maxDestinations && index < destinationsTest.length) {
            String[] destInfo = destinationsTest[index];
            index++;

            String codeIATA = destInfo[0];
            String nom = destInfo[1];
            String pays = destInfo[2];

            try {
                System.out.println("🔍 Test " + nom + "...");

                // ⬅️ PLUS BESOIN DE CRÉER UNE INSTANCE, ON EST DÉJÀ DANS UNE INSTANCE
                List<Vol> vols = rechercherVols(origine, codeIATA, date, 1);

                if (!vols.isEmpty()) {
                    double prixMin = vols.stream()
                            .mapToDouble(Vol::getPrix)
                            .min()
                            .orElse(0.0);

                    Destination destination = creerDestination(
                            nom, pays, codeIATA, prixMin, destinations.size()
                    );
                    destinations.add(destination);

                    System.out.println("   ✅ " + nom + " : " + String.format("%.2f EUR", prixMin));
                }

                Thread.sleep(500);

            } catch (Exception e) {
                System.err.println("   ❌ Erreur: " + e.getMessage());
            }
        }

        return destinations;
    }

    // ========================================
    // ✈️ RECHERCHE VOLS (NON STATIC)
    // ========================================
    public List<Vol> rechercherVols(String origine, String destination,
                                    String dateDepart, int adultes) {  // ⬅️ Déjà NON static ✅
        List<Vol> vols = new ArrayList<>();

        try {
            System.out.println("🔍 Recherche vols...");

            String token = getAccessToken();
            if (token == null) {
                System.err.println("❌ Token non disponible");
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
                    System.out.println("✅ " + data.size() + " vols trouvés");

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

                            vols.add(vol);

                        } catch (Exception e) {
                            System.err.println("   ⚠️ Erreur parsing: " + e.getMessage());
                            continue;
                        }
                    }
                }
            } else {
                System.err.println("❌ Erreur API: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }

        System.out.println("📊 Résultat: " + vols.size() + " vols");
        return vols;
    }

    // ========================================
    // 🔍 RECHERCHE AÉROPORTS (NON STATIC)
    // ========================================
    public List<Map<String, String>> rechercherAeroports(String keyword) {  // ⬅️ RETIRER static
        List<Map<String, String>> aeroports = new ArrayList<>();

        try {
            String token = getAccessToken();
            if (token == null) {
                System.err.println("❌ Token non disponible");
                return aeroports;
            }

            HttpClient client = HttpClient.newHttpClient();

            String url = String.format(
                    "https://test.api.amadeus.com/v1/reference-data/locations?subType=CITY,AIRPORT&keyword=%s&page[limit]=15&sort=analytics.travelers.score&view=FULL",
                    URLEncoder.encode(keyword, StandardCharsets.UTF_8)
            );

            System.out.println("🔍 Recherche: " + keyword);

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
                    System.out.println("✅ " + data.size() + " résultats");

                    for (JsonElement element : data) {
                        JsonObject location = element.getAsJsonObject();

                        Map<String, String> aeroport = new HashMap<>();

                        String iataCode = location.get("iataCode").getAsString();
                        aeroport.put("code", iataCode);

                        String name = location.get("name").getAsString();
                        aeroport.put("name", name);

                        String subType = location.get("subType").getAsString();
                        aeroport.put("type", subType);

                        if (location.has("address")) {
                            JsonObject address = location.getAsJsonObject("address");

                            String cityName = address.has("cityName") ?
                                    address.get("cityName").getAsString() : "";
                            String countryName = address.has("countryName") ?
                                    address.get("countryName").getAsString() : "";

                            aeroport.put("city", cityName);
                            aeroport.put("country", countryName);

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
                System.err.println("❌ Token expiré");
                accessToken = null;
            } else {
                System.err.println("❌ Erreur: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }

        return aeroports;
    }

    // ========================================
    // 🛠️ MÉTHODES UTILITAIRES (NON STATIC)
    // ========================================

    private Destination creerDestination(String nom, String pays, String codeIATA,
                                         double prix, int index) {  // ⬅️ Déjà NON static ✅
        Destination destination = new Destination();

        destination.setNom(nom);
        destination.setPays(pays);
        destination.setCodeIata(codeIATA);
        destination.setDescription(getDescription(nom));
        destination.setImageUrl(getImageUrl(codeIATA));
        destination.setPrixMin(prix);
        destination.setDevise("EUR");

        String categorie = determinerCategorie(index, prix);
        destination.setCategorie(categorie);

        if (prix < 300) {
            destination.setPromo(true);
            destination.setReduction(calculerReduction(prix));
        } else {
            destination.setPromo(false);
            destination.setReduction(0);
        }

        destination.setAutoLabel();

        return destination;
    }

    private Map<String, String> getInfosDestination(String iata) {  // ⬅️ RETIRER static
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
        mapping.put("LIS", new String[]{"Lisbonne", "Portugal"});

        if (mapping.containsKey(iata)) {
            infos.put("ville", mapping.get(iata)[0]);
            infos.put("pays", mapping.get(iata)[1]);
        } else {
            infos.put("ville", iata);
            infos.put("pays", "Destination");
        }

        return infos;
    }

    private String construireTexteAffichage(String name, String iataCode,
                                            String city, String country, String type) {  // ⬅️ RETIRER static
        StringBuilder display = new StringBuilder();

        String icone = type.equals("CITY") ? "🏙️" : "✈️";
        display.append(icone).append(" ");
        display.append(name);
        display.append(" (").append(iataCode).append(")");

        if (!city.isEmpty() && !city.equals(name)) {
            display.append(" - ").append(city);
        }

        if (!country.isEmpty()) {
            display.append(", ").append(country);
        }

        return display.toString();
    }

    private String buildFormData(Map<String, String> params) {  // ⬅️ RETIRER static
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

    private String getDescription(String ville) {  // ⬅️ RETIRER static
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("Paris", "La ville lumière vous attend avec ses monuments iconiques.");
        descriptions.put("Rome", "L'éternelle cité avec le Colisée et le Vatican.");
        descriptions.put("Istanbul", "Entre Orient et Occident, une ville fascinante.");
        descriptions.put("Barcelone", "Architecture Gaudí et plages méditerranéennes.");
        descriptions.put("Londres", "Royauté britannique et musées mondiaux.");
        descriptions.put("Dubaï", "Luxe, gratte-ciels et désert.");

        return descriptions.getOrDefault(ville, "Découvrez cette destination magnifique.");
    }

    private String getImageUrl(String codeIATA) {  // ⬅️ RETIRER static
        Map<String, String> images = new HashMap<>();
        images.put("CDG", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800");
        images.put("FCO", "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=800");
        images.put("BCN", "https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800");
        images.put("IST", "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?w=800");
        images.put("DXB", "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800");

        return images.getOrDefault(codeIATA, "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800");
    }

    private String determinerCategorie(int index, double prix) {  // ⬅️ RETIRER static
        if (prix > 0 && prix < 300) return "promo";
        if (index < 4) return "populaire";
        return "nouveau";
    }

    private int calculerReduction(double prix) {  // ⬅️ RETIRER static
        if (prix < 150) return 30;
        if (prix < 250) return 20;
        if (prix < 300) return 15;
        return 10;
    }
}