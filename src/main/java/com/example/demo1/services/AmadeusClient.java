package com.example.demo1.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo1.config.AmadeusConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * HTTP client for Amadeus APIs: OAuth token and API calls.
 */
public class AmadeusClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final AmadeusConfig config;
    private final HttpClient httpClient;
    private final AtomicReference<String> accessToken = new AtomicReference<>();
    private volatile long tokenExpiresAt;

    public AmadeusClient() {
        this(new AmadeusConfig());
    }

    public AmadeusClient(AmadeusConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
    }

    public boolean isConfigured() {
        return config.isConfigured();
    }

    /**
     * Get a valid OAuth2 access token (cached until near expiry).
     */
    public String getAccessToken() throws Exception {
        if (!config.isConfigured()) {
            throw new IllegalStateException("Amadeus API key and secret are not configured. Add amadeus.properties or set AMADEUS_API_KEY and AMADEUS_API_SECRET.");
        }
        if (accessToken.get() != null && System.currentTimeMillis() < tokenExpiresAt - 60_000) {
            return accessToken.get();
        }
        String form = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(config.getApiKey(), StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(config.getApiSecret(), StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(config.getHost() + "/v1/security/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("Amadeus OAuth failed: " + res.statusCode() + " " + res.body());
        }
        JsonNode root = MAPPER.readTree(res.body());
        String token = root.path("access_token").asText(null);
        int expiresIn = root.path("expires_in").asInt(1799);
        if (token == null) {
            throw new RuntimeException("Amadeus response missing access_token");
        }
        accessToken.set(token);
        tokenExpiresAt = System.currentTimeMillis() + expiresIn * 1000L;
        return token;
    }

    /**
     * GET request to an Amadeus API path (e.g. /v1/reference-data/locations/hotels/by-city).
     * Query string can be null or e.g. "cityCode=PAR".
     */
    public JsonNode get(String path, String queryString) throws Exception {
        String token = getAccessToken();
        String url = config.getHost() + path + (queryString != null && !queryString.isEmpty() ? "?" + queryString : "");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("Amadeus API Response Status: " + res.statusCode());
        System.out.println("Amadeus API Response Body: " + res.body());
        if (res.statusCode() != 200) {
            String detail = parseErrorDetail(res.body());
            throw new AmadeusApiException(res.statusCode(), detail != null ? detail : res.body());
        }
        return MAPPER.readTree(res.body());
    }

    /** Parse Amadeus error JSON to get first error detail (e.g. "Invalid city code"). */
    private static String parseErrorDetail(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            JsonNode root = MAPPER.readTree(body);
            JsonNode errors = root.path("errors");
            if (errors.isArray() && errors.size() > 0) {
                JsonNode first = errors.get(0);
                if (first.has("detail")) return first.get("detail").asText(null);
                if (first.has("title")) return first.get("title").asText(null);
            }
        } catch (Exception ignored) { }
        return null;
    }

    public JsonNode get(String path) throws Exception {
        return get(path, null);
    }
}

