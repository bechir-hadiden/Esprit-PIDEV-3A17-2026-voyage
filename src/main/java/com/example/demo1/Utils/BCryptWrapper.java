package com.example.demo1.Utils;

/**
 * Wrapper pour BCrypt qui évite les problèmes de module JPMS.
 * Cette classe charge BCrypt via reflection depuis le unnamed module.
 */
public class BCryptWrapper {

    private static final String BCRYPT_CLASS = "org.mindrot.jbcrypt.BCrypt";

    /**
     * Hache un mot de passe avec BCrypt
     */
    public static String hashpw(String password, String salt) {
        try {
            Class<?> bcryptClass = Class.forName(BCRYPT_CLASS);
            return (String) bcryptClass.getMethod("hashpw", String.class, String.class)
                    .invoke(null, password, salt);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du hachage BCrypt", e);
        }
    }

    /**
     * Génère un salt BCrypt
     */
    public static String gensalt() {
        try {
            Class<?> bcryptClass = Class.forName(BCRYPT_CLASS);
            return (String) bcryptClass.getMethod("gensalt").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du salt BCrypt", e);
        }
    }

    /**
     * Vérifie un mot de passe contre un hash BCrypt
     * Gère les différentes versions (2a, 2b, 2y, 2x)
     */
    public static boolean checkpw(String password, String hashed) {
        try {
            // Convertir les différentes versions BCrypt vers $2a$ (supporté par jBCrypt)
            String normalizedHash = normalizeBcryptHash(hashed);
            
            Class<?> bcryptClass = Class.forName(BCRYPT_CLASS);
            return (boolean) bcryptClass.getMethod("checkpw", String.class, String.class)
                    .invoke(null, password, normalizedHash);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification BCrypt", e);
        }
    }
    
    /**
     * Normalise le hash BCrypt pour la compatibilité avec jBCrypt 0.4
     * Convertit $2y$, $2b$, $2x$ vers $2a$
     */
    private static String normalizeBcryptHash(String hash) {
        if (hash == null || hash.length() < 4) {
            return hash;
        }
        
        // Détecter la version du hash
        if (hash.startsWith("$2y$")) {
            // Format PHP moderne -> convertir en $2a$
            return "$2a$" + hash.substring(4);
        } else if (hash.startsWith("$2b$")) {
            // Format OpenBSD -> convertir en $2a$
            return "$2a$" + hash.substring(4);
        } else if (hash.startsWith("$2x$")) {
            // Ancien format bugué -> convertir en $2a$
            return "$2a$" + hash.substring(4);
        }
        
        // Déjà au format $2a$ ou autre format inconnu, retourner tel quel
        return hash;
    }
}
