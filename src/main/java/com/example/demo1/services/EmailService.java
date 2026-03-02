package com.example.demo1.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    // Configuration SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String ADMIN_EMAIL = "bechirhadidan8@gmail.com";
    private static final String ADMIN_PASSWORD = "pdul stke pohv xege";
    
    // Instance fields for dynamic configuration
    private String user = ADMIN_EMAIL;
    private String password = ADMIN_PASSWORD;
    private String host = SMTP_HOST;
    private int port = Integer.parseInt(SMTP_PORT);

    /**
     * Envoie une nouvelle déclaration/réclamation
     */
    public static boolean envoyerDeclaration(String nomClient, String emailClient, String message) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD);
                }
            });

            // Email 1 : Notification à l'admin
            Message messageAdmin = new MimeMessage(session);
            messageAdmin.setFrom(new InternetAddress(ADMIN_EMAIL, "Système de Réclamations"));
            messageAdmin.setRecipients(Message.RecipientType.TO, InternetAddress.parse(ADMIN_EMAIL));
            messageAdmin.setReplyTo(InternetAddress.parse(emailClient));
            messageAdmin.setSubject("Nouvelle réclamation de " + nomClient);
            messageAdmin.setText(message);
            Transport.send(messageAdmin);

            System.out.println("✅ Notification envoyée à l'admin");

            // Email 2 : Confirmation au client
            Message messageClient = new MimeMessage(session);
            messageClient.setFrom(new InternetAddress(ADMIN_EMAIL, "Service Client"));
            messageClient.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailClient));
            messageClient.setSubject("Confirmation de réception de votre réclamation");

            String texteConfirmation = "Bonjour " + nomClient + ",\n\n" +
                    "Nous avons bien reçu votre réclamation.\n" +
                    "Notre équipe va l'examiner et vous répondra dans les plus brefs délais.\n\n" +
                    "Merci de votre confiance.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe Support";

            messageClient.setText(texteConfirmation);
            Transport.send(messageClient);

            System.out.println("✅ Confirmation envoyée au client : " + emailClient);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoie une réponse à une réclamation
     */
    public static boolean envoyerReponse(String emailClient, String nomClient, String sujet, String reponse) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ADMIN_EMAIL, "Service Client"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailClient));
            message.setSubject(sujet);
            message.setText(reponse);

            Transport.send(message);
            System.out.println("✅ Réponse envoyée au client : " + emailClient);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de la réponse : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoie un email simple
     */
    public static boolean envoyerEmail(String emailDestinataire, String sujet, String contenu) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ADMIN_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinataire));
            message.setSubject(sujet);
            message.setText(contenu);

            Transport.send(message);
            System.out.println("✅ Email envoyé à : " + emailDestinataire);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Teste la configuration email
     */
    public static boolean testerConfiguration() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD);
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect(SMTP_HOST, ADMIN_EMAIL, ADMIN_PASSWORD);
            transport.close();

            System.out.println("✅ Configuration email valide");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Configuration invalide : " + e.getMessage());
            return false;
        }
    }


    public boolean isConfigured() {
        return user != null && !user.isEmpty() && password != null && !password.isEmpty();
    }

    /**
     * Send a 6-digit code to the given email address.
     */
    public boolean sendResetCode(String toEmail, String code) {
        if (!isConfigured()) {
            System.err.println("SMTP not configured. Add smtp.properties with smtp.user and smtp.password.");
            return false;
        }
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", String.valueOf(port));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(user, "SmartTrip"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject("SmartTrip - Password Reset Code");
            msg.setText("Your password reset code is: " + code + "\n\nThis code expires in 10 minutes.\n\nIf you didn't request this, please ignore this email.", "UTF-8");

            Transport.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send reset email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoie le coupon de réduction avec le PDF en pièce jointe
     */
    public static boolean envoyerCouponEmail(String emailDestinataire, String nomOffre, String cheminFichierPDF) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // On utilise les constantes déjà définies dans la classe
                    return new PasswordAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ADMIN_EMAIL, "SmartTrip Promotions"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinataire));
            message.setSubject("Votre Coupon SmartTrip : " + nomOffre + " ✈️");

            // --- Création du corps du mail (MultiPart pour PJ) ---
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Félicitations !\n\nVous trouverez en pièce jointe votre coupon de réduction pour l'offre : " + nomOffre + ".\n" +
                    "Scannez le QR Code présent dans le PDF lors de votre passage.\n\n" +
                    "L'équipe SmartTrip.");

            // --- Création de la pièce jointe ---
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new java.io.File(cheminFichierPDF));

            // Assemblage du mail
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("✅ Coupon envoyé avec succès à : " + emailDestinataire);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du coupon : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}