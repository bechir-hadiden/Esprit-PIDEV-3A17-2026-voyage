package com.example.demo1.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmailService {

    // ⚙️ CONFIGURATION SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    // 📧 VOTRE EMAIL (administration)
    private static final String ADMIN_EMAIL = "bechirhadidan8@gmail.com";
    private static final String ADMIN_PASSWORD = "pdul stke pohv xege"; // Mot de passe d'application Google

    /**
     * Envoie la déclaration du client vers l'email d'administration
     * @param nomClient Nom du client
     * @param emailClient Email du client
     * @param declaration Texte de la déclaration
     * @return true si envoi réussi, false sinon
     */
    public static boolean envoyerDeclaration(String nomClient, String emailClient, String declaration) {
        try {
            System.out.println("========================================");
            System.out.println("📧 ENVOI DE DÉCLARATION EN COURS...");
            System.out.println("👤 Client: " + nomClient);
            System.out.println("📧 Email client: " + emailClient);
            System.out.println("========================================");

            // Configuration SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            // Authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD);
                }
            });

            // Création du message
            Message message = new MimeMessage(session);

            // Expéditeur : votre email admin
            message.setFrom(new InternetAddress(ADMIN_EMAIL, "Smart Trip - Système"));

            // Destinataire : votre email admin (vous recevez la déclaration)
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(ADMIN_EMAIL));

            // Reply-To : email du client (pour répondre directement au client)
            message.setReplyTo(new Address[] {
                    new InternetAddress(emailClient, nomClient)
            });

            // Sujet
            message.setSubject("📋 Nouvelle déclaration - " + nomClient);

            // Contenu HTML
            String contenuHTML = creerEmailHTML(nomClient, emailClient, declaration);
            message.setContent(contenuHTML, "text/html; charset=utf-8");

            // Envoi
            Transport.send(message);

            System.out.println("✅ Déclaration envoyée avec succès !");
            System.out.println("   → Vers: " + ADMIN_EMAIL);
            System.out.println("   → Répondre à: " + emailClient);
            System.out.println("========================================");

            return true;

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de l'envoi :");
            e.printStackTrace();
            System.out.println("========================================");
            return false;
        }
    }

    /**
     * Crée le contenu HTML de l'email
     */
    private static String creerEmailHTML(String nomClient, String emailClient, String declaration) {
        String dateHeure = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));

        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                        ".header { background: linear-gradient(135deg, #4472C4 0%%, #2E5C9A 100%%); " +
                        "          color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }" +
                        ".content { background: #f9f9f9; padding: 30px; border: 1px solid #ddd; }" +
                        ".info-box { background: white; padding: 15px; margin: 10px 0; " +
                        "           border-left: 4px solid #4472C4; border-radius: 4px; }" +
                        ".label { font-weight: bold; color: #4472C4; display: inline-block; " +
                        "        min-width: 100px; }" +
                        ".declaration { background: white; padding: 20px; margin-top: 20px; " +
                        "              border: 2px solid #4472C4; border-radius: 8px; " +
                        "              white-space: pre-wrap; line-height: 1.8; }" +
                        ".footer { background: #333; color: white; padding: 15px; " +
                        "         text-align: center; font-size: 12px; border-radius: 0 0 8px 8px; }" +
                        ".btn-reply { display: inline-block; background: #4472C4; color: white; " +
                        "            padding: 12px 30px; text-decoration: none; border-radius: 5px; " +
                        "            margin-top: 15px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "  <div class='header'>" +
                        "    <h1>📋 Nouvelle Déclaration Client</h1>" +
                        "    <p>Smart Trip - Système de gestion</p>" +
                        "  </div>" +
                        "  <div class='content'>" +
                        "    <h2 style='color: #4472C4; border-bottom: 2px solid #4472C4; padding-bottom: 10px;'>" +
                        "      Informations du Client" +
                        "    </h2>" +
                        "    <div class='info-box'>" +
                        "      <p><span class='label'>👤 Nom :</span> %s</p>" +
                        "      <p><span class='label'>📧 Email :</span> " +
                        "         <a href='mailto:%s' style='color: #4472C4;'>%s</a></p>" +
                        "      <p><span class='label'>📅 Date :</span> %s</p>" +
                        "    </div>" +
                        "    <h2 style='color: #4472C4; border-bottom: 2px solid #4472C4; " +
                        "       padding-bottom: 10px; margin-top: 30px;'>" +
                        "      📝 Déclaration" +
                        "    </h2>" +
                        "    <div class='declaration'>%s</div>" +
                        "    <div style='text-align: center;'>" +
                        "      <a href='mailto:%s' class='btn-reply'>✉️ Répondre au client</a>" +
                        "    </div>" +
                        "  </div>" +
                        "  <div class='footer'>" +
                        "    <p>© 2024 Smart Trip - Système automatisé de réception de déclarations</p>" +
                        "    <p style='font-size: 11px; margin-top: 10px;'>" +
                        "       Cliquez sur 'Répondre' dans votre client email pour répondre directement au client" +
                        "    </p>" +
                        "  </div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                nomClient,
                emailClient,
                emailClient,
                dateHeure,
                declaration.replace("\n", "<br>"),
                emailClient
        );
    }
}