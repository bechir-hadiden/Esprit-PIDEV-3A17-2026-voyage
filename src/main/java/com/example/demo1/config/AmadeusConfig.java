package com.example.demo1.config;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads Amadeus API credentials from amadeus.properties (or environment variables).
 * Never commit amadeus.properties with real keys; use amadeus.properties.example as template.
 */
public class AmadeusConfig {
    private static final String PROP_FILE = "amadeus.properties";
    private static final String ENV_KEY = "AMADEUS_API_KEY";
    private static final String ENV_SECRET = "AMADEUS_API_SECRET";
    private static final String DEFAULT_HOST = "https://test.api.amadeus.com";

    private final String apiKey;
    private final String apiSecret;
    private final String host;

    public AmadeusConfig() {
        String key = System.getenv(ENV_KEY);
        String secret = System.getenv(ENV_SECRET);
        if (key == null || secret == null) {
            Properties p = loadProperties();
            key = key != null ? key : p.getProperty("amadeus.api.key", "");
            secret = secret != null ? secret : p.getProperty("amadeus.api.secret", "");
        }
        this.apiKey = key != null ? key.trim() : "";
        this.apiSecret = secret != null ? secret.trim() : "";
        Properties p = loadProperties();
        this.host = p.getProperty("amadeus.api.host", DEFAULT_HOST).trim();
    }

    private Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(PROP_FILE)) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            // optional file
        }
        return p;
    }

    public String getApiKey() { return apiKey; }
    public String getApiSecret() { return apiSecret; }
    public String getHost() { return host; }
    public boolean isConfigured() { return !apiKey.isEmpty() && !apiSecret.isEmpty(); }
}

