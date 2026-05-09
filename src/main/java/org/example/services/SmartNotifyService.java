package org.example.services;

import org.example.entities.Reservation;
import org.example.entities.Transport;
import org.example.entities.User;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Service for sending real notifications (SMS via Textbelt, Email via SMTP).
 */
public class SmartNotifyService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // SMTP Configuration (Example for Gmail)
    private final String SMTP_HOST = "smtp.gmail.com";
    private final String SMTP_PORT = "587";
    private final String SMTP_USER = "marambousteni37@gmail.com"; // Mis à jour automatiquement
    private final String SMTP_PASS = "vqyxmdkgsoyqzweo"; // Mot de passe d'application nettoyé

    public void sendConfirmationSMS(User user, Transport transport, Reservation reservation) {
        String phone = user.getTelephone();
        if (phone == null || phone.isEmpty()) {
            System.out.println("LOG [SmartNotify]: No phone number provided, skipping SMS.");
            return;
        }

        String dateStr = reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String messageBody = String.format(
                "SmartNotify: Bonjour %s, votre reservation (%s) est confirmee pour le %s. Merci!",
                user.getUsername(),
                transport.getCompagnie(),
                dateStr);

        try {
            String form = "number=" + phone + "&message=" + messageBody + "&key=textbelt";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://textbelt.com/text"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "Java-HttpClient-17") // Adding User-Agent to avoid 403
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            System.out.println("LOG [SmartNotify]: Attempting to send REAL SMS to " + phone);
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        System.out.println("LOG [SmartNotify]: Textbelt Status: " + response.statusCode());
                        System.out.println("LOG [SmartNotify]: Textbelt Response: " + response.body());
                    });
        } catch (Exception e) {
            System.err.println("LOG [SmartNotify]: SMS Error: " + e.getMessage());
        }
    }

    public void sendConfirmationEmail(User user, Transport transport, Reservation reservation) {
        String toEmail = user.getEmail();
        if (toEmail == null || toEmail.isEmpty()) {
            System.out.println("LOG [SmartNotify]: No email address provided, skipping Email.");
            return;
        }

        // IMPORTANT: Check if user has updated the placeholders
        if (SMTP_USER.contains("VOTRE_EMAIL") || SMTP_PASS.contains("VOTRE_MOT_DE_PASSE")) {
            System.err.println("CRITICAL [SmartNotify]: EMAIL NON ENVOYÉ !");
            System.err.println(
                    ">> VOUS DEVEZ CONFIGURER VOS IDENTIFIANTS GMAIL DANS : src/main/java/org/example/services/SmartNotifyService.java");
            System.err.println(">> Tuto : https://myaccount.google.com/apppasswords");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        try {
            String dateStr = reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Confirmation de votre réservation - SmartNotify");

            String content = String.format(
                    "Bonjour %s,\n\n" +
                            "Votre réservation est confirmée :\n" +
                            "Transport : %s\n" +
                            "Date : %s\n\n" +
                            "Merci d'avoir choisi SmartNotify.",
                    user.getUsername(), transport.getCompagnie(), dateStr);

            message.setText(content);

            System.out.println("LOG [SmartNotify]: Attempting to send REAL EMAIL to " + toEmail);

            // Send in a separate thread to avoid UI freeze
            new Thread(() -> {
                try {
                    jakarta.mail.Transport.send(message);
                    System.out.println("LOG [SmartNotify]: REAL EMAIL SENT successfully to " + toEmail);
                } catch (MessagingException e) {
                    System.err.println("LOG [SmartNotify]: EMAIL ERROR: " + e.getMessage());
                    if (e.getMessage().contains("535")) {
                        System.err.println(
                                "[FIX]: Le mot de passe est rejeté par Google. Générez un 'Mot de passe d'application' ici : https://myaccount.google.com/apppasswords");
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAIConfirmation(User user, org.example.entities.BaseVehicule vehicule, Reservation reservation) {
        String toEmail = user.getEmail();
        if (toEmail == null || toEmail.isEmpty())
            return;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        try {
            String dateStr = reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Confirmation de votre réservation AI - SmartNotify");

            String content = String.format(
                    "Bonjour %s,\n\n" +
                            "Votre réservation conseillée par l'AI est confirmée :\n" +
                            "Véhicule : %s (%s)\n" +
                            "Type : %s\n" +
                            "Date : %s\n\n" +
                            "Merci d'utiliser notre assistant intelligent !",
                    user.getUsername(), vehicule.getCompagnie(), vehicule.getNumero(), vehicule.getType(), dateStr);

            message.setText(content);

            new Thread(() -> {
                try {
                    jakarta.mail.Transport.send(message);
                } catch (MessagingException e) {
                    System.err.println("LOG [SmartNotify]: AI EMAIL ERROR: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCancellationAlertToAdmin(User user, Reservation reservation) {
        new Thread(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            try {
                String dateStr = reservation.getDateReservation() != null
                        ? reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "N/A";

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SMTP_USER));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(SMTP_USER));
                message.setSubject("⚠️ ALERTE : Annulation de réservation #" + reservation.getIdReservation());

                String content = String.format(
                        "Bonjour Administrateur,\n\n" +
                                "L'utilisateur suivant vient d'annuler sa réservation :\n" +
                                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                "👤 Utilisateur    : %s (ID: %d)\n" +
                                "📧 Email          : %s\n" +
                                "🎫 Réservation ID : #%d\n" +
                                "🚌 Type Transport : %s\n" +
                                "📅 Date prévue    : %s\n" +
                                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                                "Connectez-vous à l'application pour voir les statistiques mises à jour.\n\n" +
                                "SmartNotify Admin Alert",
                        user.getUsername(), user.getIdUser(),
                        user.getEmail() != null ? user.getEmail() : "Non renseigné",
                        reservation.getIdReservation(),
                        reservation.getTypeTransport() != null ? reservation.getTypeTransport() : "N/A",
                        dateStr);

                message.setText(content);
                jakarta.mail.Transport.send(message);

                // --- ADDED: DATABASE STORAGE ---
                org.example.entities.Notification n = new org.example.entities.Notification(
                        user.getIdUser(),
                        reservation.getIdReservation(),
                        String.format("Réservation #%d (%s) annulée par %s",
                                reservation.getIdReservation(),
                                reservation.getTypeTransport(),
                                user.getUsername()),
                        "CANCELLATION");
                new org.example.services.NotificationService().ajouter(n);
                // -------------------------------

                System.out.println(
                        "LOG [SmartNotify]: Admin alert sent for reservation #" + reservation.getIdReservation());
            } catch (Exception e) {
                System.err.println("LOG [SmartNotify]: Admin alert error: " + e.getMessage());
            }
        }).start();
    }
}
