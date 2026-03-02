package org.example.utils;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/** Basic configuration utility for loading API keys. */
public class Config {
    private static final Properties props = new Properties();

    static {
        // 1) Load external file from project working directory if present.
        Path externalConfig = Paths.get("config.properties");
        if (Files.exists(externalConfig)) {
            try (Reader reader = Files.newBufferedReader(externalConfig, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (Exception e) {
                System.err.println("Could not load external config.properties: " + e.getMessage());
            }
        }

        // 2) Load classpath resource as fallback.
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            System.err.println("Could not load classpath config.properties, using env/defaults.");
        }
    }

    private static String getEnv(String... keys) {
        for (String key : keys) {
            String val = System.getenv(key);
            if (val != null && !val.trim().isEmpty()) {
                return val;
            }
        }
        return null;
    }

    public static String get(String key) {
        // Stripe aliases (common naming patterns in local setups / CI).
        if ("stripe.api.key".equals(key)) {
            String stripeVal = getEnv("STRIPE_API_KEY", "STRIPE_SECRET_KEY", "STRIPE_KEY");
            if (stripeVal != null) {
                return stripeVal;
            }
        }

        // Generic env mapping fallback.
        String envKey = key.replace(".", "_").toUpperCase();
        String envVal = getEnv(envKey);
        if (envVal != null) {
            return envVal;
        }

        // Finally check properties files.
        String propVal = props.getProperty(key);
        return propVal != null ? propVal : "YOUR_API_KEY_HERE";
    }
}
