package com.example.demo1.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ✅ Service de conversion de devises
 * API : Frankfurter (https://api.frankfurter.dev)
 * - 100% GRATUIT
 * - SANS clé API
 * - SANS limite de requêtes
 * - Source : Banque Centrale Européenne
 */
public class CurrencyService {

    private static final String BASE_URL = "https://api.frankfurter.dev/v1/latest";

    // Devises supportées avec leurs symboles et noms complets
    public static final Map<String, String[]> DEVISES = new LinkedHashMap<>() {{
        put("TND", new String[]{"د.ت", "Dinar Tunisien"});
        put("EUR", new String[]{"€",   "Euro"});
        put("USD", new String[]{"$",   "Dollar Américain"});
        put("GBP", new String[]{"£",   "Livre Sterling"});
        put("JPY", new String[]{"¥",   "Yen Japonais"});
        put("AED", new String[]{"د.إ", "Dirham Émirats"});
        put("SAR", new String[]{"﷼",   "Riyal Saoudien"});
        put("MAD", new String[]{"د.م.", "Dirham Marocain"});
        put("CAD", new String[]{"CA$", "Dollar Canadien"});
        put("AUD", new String[]{"A$",  "Dollar Australien"});
        put("CHF", new String[]{"Fr",  "Franc Suisse"});
        put("CNY", new String[]{"¥",   "Yuan Chinois"});
        put("THB", new String[]{"฿",   "Baht Thaïlandais"});
        put("TRY", new String[]{"₺",   "Livre Turque"});
    }};

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Cache simple pour éviter trop de requêtes
    private Map<String, Double> cachedRates = null;
    private String cachedBaseCurrency = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_DURATION_MS = 60 * 60 * 1000; // 1 heure

    public CurrencyService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Récupère les taux de change depuis une devise de base
     * Exemple : getExchangeRates("EUR") → { "USD": 1.08, "TND": 3.35, ... }
     *
     * Pour TND (non supporté directement par Frankfurter),
     * on passe par EUR comme intermédiaire.
     */
    public Map<String, Double> getExchangeRates(String baseCurrency) throws Exception {
        // Vérifier le cache
        long now = System.currentTimeMillis();
        if (cachedRates != null
                && baseCurrency.equals(cachedBaseCurrency)
                && (now - cacheTimestamp) < CACHE_DURATION_MS) {
            System.out.println("✅ Taux depuis le cache pour " + baseCurrency);
            return cachedRates;
        }

        // TND n'est pas dans Frankfurter → on utilise EUR comme base
        // et on ajoute TND manuellement avec un taux fixe approximatif
        String apiBase = baseCurrency.equals("TND") ? "EUR" : baseCurrency;

        String url = BASE_URL + "?base=" + apiBase;
        System.out.println("🌐 Appel API: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erreur API: " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode rates = root.get("rates");

        Map<String, Double> result = new LinkedHashMap<>();

        // Ajouter la devise de base elle-même (taux = 1.0)
        result.put(apiBase, 1.0);

        // Ajouter TND approximatif (1 EUR ≈ 3.35 TND)
        double tndRate = 3.35;
        result.put("TND", tndRate);

        // Ajouter toutes les devises retournées par l'API
        rates.fields().forEachRemaining(entry -> {
            result.put(entry.getKey(), entry.getValue().asDouble());
        });

        // Si la base était TND, on recalcule tout relativement à TND
        if (baseCurrency.equals("TND")) {
            Map<String, Double> tndBased = new LinkedHashMap<>();
            double eurToTnd = tndRate; // 1 EUR = 3.35 TND
            tndBased.put("TND", 1.0);
            result.forEach((currency, rateFromEur) -> {
                if (!currency.equals("TND") && !currency.equals("EUR")) {
                    // taux depuis TND = taux depuis EUR / EUR→TND
                    tndBased.put(currency, rateFromEur / eurToTnd);
                }
            });
            tndBased.put("EUR", 1.0 / eurToTnd);
            cachedRates = tndBased;
            cachedBaseCurrency = "TND";
            cacheTimestamp = now;
            return tndBased;
        }

        cachedRates = result;
        cachedBaseCurrency = baseCurrency;
        cacheTimestamp = now;

        System.out.println("✅ " + result.size() + " devises chargées pour base=" + baseCurrency);
        return result;
    }

    /**
     * Convertit un montant d'une devise vers une autre
     *
     * @param amount   montant à convertir
     * @param from     devise source  (ex: "EUR")
     * @param to       devise cible   (ex: "USD")
     * @return         montant converti
     */
    public double convert(double amount, String from, String to) throws Exception {
        if (from.equals(to)) return amount;

        Map<String, Double> rates = getExchangeRates(from);
        Double rate = rates.get(to);

        if (rate == null) {
            throw new Exception("Devise non trouvée: " + to);
        }
        return amount * rate;
    }

    /**
     * Retourne le symbole d'une devise (ex: "EUR" → "€")
     */
    public static String getSymbole(String code) {
        String[] info = DEVISES.get(code);
        return info != null ? info[0] : code;
    }

    /**
     * Retourne le nom complet d'une devise (ex: "EUR" → "Euro")
     */
    public static String getNom(String code) {
        String[] info = DEVISES.get(code);
        return info != null ? info[1] : code;
    }
}