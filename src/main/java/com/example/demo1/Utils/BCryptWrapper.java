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
     */
    public static boolean checkpw(String password, String hashed) {
        try {
            Class<?> bcryptClass = Class.forName(BCRYPT_CLASS);
            return (boolean) bcryptClass.getMethod("checkpw", String.class, String.class)
                    .invoke(null, password, hashed);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification BCrypt", e);
        }
    }
}
