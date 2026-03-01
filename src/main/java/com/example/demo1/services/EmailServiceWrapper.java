package com.example.demo1.services;

import java.lang.reflect.Method;

/**
 * Wrapper for EmailService using reflection to bypass JPMS restrictions.
 * javax.mail is a non-modular library that cannot be accessed directly from the module.
 */
public class EmailServiceWrapper {

    private static final String EMAIL_SERVICE_CLASS = "com.example.demo1.services.EmailService";

    /**
     * Send password reset email using reflection.
     * @param email User's email address
     * @param resetCode The reset code to send
     * @param username User's username
     * @return true if email sent successfully, false otherwise
     */
    public static boolean sendPasswordResetEmail(String email, String resetCode, String username) {
        try {
            Class<?> emailServiceClass = Class.forName(EMAIL_SERVICE_CLASS);
            Method method = emailServiceClass.getMethod("sendPasswordResetEmail", String.class, String.class, String.class);
            Object result = method.invoke(null, email, resetCode, username);
            return (boolean) result;
        } catch (Exception e) {
            System.err.println("Error sending password reset email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send declaration/complaint email using reflection.
     * @param nomClient Client name
     * @param emailClient Client email
     * @param message Message content
     * @return true if email sent successfully, false otherwise
     */
    public static boolean envoyerDeclaration(String nomClient, String emailClient, String message) {
        try {
            Class<?> emailServiceClass = Class.forName(EMAIL_SERVICE_CLASS);
            Method method = emailServiceClass.getMethod("envoyerDeclaration", String.class, String.class, String.class);
            Object result = method.invoke(null, nomClient, emailClient, message);
            return (boolean) result;
        } catch (Exception e) {
            System.err.println("Error sending declaration email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
