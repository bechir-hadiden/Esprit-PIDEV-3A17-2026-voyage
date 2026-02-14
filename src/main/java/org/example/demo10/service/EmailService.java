package org.example.demo10.service;

import org.example.demo10.model.Avis;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_EXPEDITEUR = "wathekzidi68@gmail.com";
    private static final String MOT_DE_PASSE = "wmsn loac ztdg vzdp";
    private static final String EMAIL_ADMIN = "wathekzidi68@gmail.com";

    public static void envoyerNotificationNouvelAvis(Avis avis) {
        System.out.println("📧 Début de l'envoi d'email...");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_EXPEDITEUR, MOT_DE_PASSE);
            }
        });
        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_EXPEDITEUR));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_ADMIN));
            message.setSubject("🔔 Nouvel avis - Voyage #" + avis.getVoyageId());

            String contenu = String.format(
                    "NOUVEL AVIS CLIENT\n\n" +
                            "Client: %s\n" +
                            "Email: %s\n" +
                            "Voyage #: %d\n" +
                            "Date: %s\n" +
                            "Note: %d/5\n\n" +
                            "Commentaire:\n\"%s\"\n\n" +
                            "--\n" +
                            "Cet email a été envoyé automatiquement.",
                    avis.getNomClient(),
                    avis.getEmail(),
                    avis.getVoyageId(),
                    avis.getDateAvis() != null ? avis.getDateAvis().toString() : "Non spécifiée",
                    avis.getNote(),
                    avis.getCommentaire()
            );

            // CORRECTION : Utilisation de setContent() avec encodage UTF-8
            message.setContent(contenu, "text/plain; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès !");

        } catch (MessagingException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}