package com.example.demo1.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * ✅ Service WhatsApp via Twilio
 * Documentation : https://www.twilio.com/docs/whatsapp
 *
 * ⚠️  AVANT D'UTILISER :
 * 1. Créer un compte sur twilio.com/try-twilio
 * 2. Remplacer ACCOUNT_SID, AUTH_TOKEN, TWILIO_WHATSAPP_NUMBER
 * 3. Activer le Sandbox WhatsApp dans le dashboard Twilio
 * 4. Le client doit d'abord envoyer "join <mot>" au +14155238886
 */
public class WhatsAppService {

    // ============================================
    // ⚙️  CONFIGURATION — À remplir avec vos credentials
    // ============================================
    private static final String ACCOUNT_SID         = "AC4d6f990053c1a86ba9a524215025200d";
    private static final String AUTH_TOKEN          = "FHTQTZ33NFY3MJCD9C5BSBJD";
    private static final String TWILIO_WHATSAPP_NUM = "whatsapp:+14155238886"; // Sandbox Twilio

    private static boolean initialise = false;

    // ============================================
    // 🔌 INITIALISATION TWILIO (une seule fois)
    // ============================================
    private static void init() {
        if (!initialise) {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            initialise = true;
            System.out.println("✅ Twilio initialisé");
        }
    }

    // ============================================
    // 📤 MÉTHODE GÉNÉRIQUE — Envoyer un message WhatsApp
    // ============================================
    public static boolean envoyerMessage(String numeroClient, String contenu) {
        try {
            init();

            // Formater le numéro (ex: "21698765432" → "whatsapp:+21698765432")
            String numFormate = numeroClient.startsWith("+")
                    ? "whatsapp:" + numeroClient
                    : "whatsapp:+" + numeroClient;

            Message message = Message.creator(
                    new PhoneNumber(numFormate),        // destinataire
                    new PhoneNumber(TWILIO_WHATSAPP_NUM), // expéditeur (sandbox)
                    contenu                             // message
            ).create();

            System.out.println("✅ WhatsApp envoyé | SID: " + message.getSid()
                    + " | Status: " + message.getStatus());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur WhatsApp: " + e.getMessage());
            return false;
        }
    }

    // ============================================
    // ✈️  1. CONFIRMATION DE RÉSERVATION
    // ============================================
    public static boolean envoyerConfirmationReservation(
            String numeroClient,
            String nomClient,
            String destination,
            String dateDepart,
            String dateFin,
            String reference,
            double prix) {

        String message = String.format(
                "*SmartTrip* - Confirmation de Voyage\n\n" +
                        "Bonjour *%s* !\n\n" +
                        "Votre voyage est confirme :\n" +
                        "- Destination : *%s*\n" +
                        "- Depart      : *%s*\n" +
                        "- Retour      : *%s*\n" +
                        "- Prix total  : *%.0f TND*\n" +
                        "- Reference   : *#%s*\n\n" +
                        "Merci de votre confiance !\n" +
                        "Contact : +216 XX XXX XXX",
                nomClient, destination, dateDepart, dateFin, prix, reference
        );

        return envoyerMessage(numeroClient, message);
    }

    // ============================================
    // 🔔 2. RAPPEL J-1 AVANT DÉPART
    // ============================================
    public static boolean envoyerRappelVeille(
            String numeroClient,
            String nomClient,
            String destination,
            String heureDepart,
            String aeroport) {

        String message = String.format(
                "*SmartTrip* - Rappel Voyage\n\n" +
                        "Bonjour *%s* !\n\n" +
                        "Votre voyage vers *%s* est *demain* !\n\n" +
                        "- Heure de depart : *%s*\n" +
                        "- Aeroport        : *%s*\n\n" +
                        "Checklist :\n" +
                        "- Passeport / Visa\n" +
                        "- Billet imprime\n" +
                        "- Arriver 3h avant\n\n" +
                        "Bon voyage ! SmartTrip",
                nomClient, destination, heureDepart, aeroport
        );

        return envoyerMessage(numeroClient, message);
    }

    // ============================================
    // ⚠️  3. ALERTE MODIFICATION / ANNULATION
    // ============================================
    public static boolean envoyerAlerteModification(
            String numeroClient,
            String nomClient,
            String destination,
            String typeAlerte,  // "modifié" ou "annulé"
            String details) {

        String message = String.format(
                "*SmartTrip* - ALERTE IMPORTANTE\n\n" +
                        "Bonjour *%s*,\n\n" +
                        "Votre voyage vers *%s* a ete *%s*.\n\n" +
                        "Details : %s\n\n" +
                        "Contactez-nous immediatement :\n" +
                        "Tel : +216 XX XXX XXX\n" +
                        "Email : contact@smarttrip.tn",
                nomClient, destination, typeAlerte, details
        );

        return envoyerMessage(numeroClient, message);
    }

    // ============================================
    // 🔑 4. CODE OTP — Vérification de compte
    // ============================================
    public static boolean envoyerOTP(String numeroClient, String codeOTP) {
        String message = String.format(
                "*SmartTrip* - Verification\n\n" +
                        "Votre code de verification est :\n\n" +
                        "*%s*\n\n" +
                        "Ce code expire dans *10 minutes*.\n" +
                        "Ne le partagez avec personne.",
                codeOTP
        );

        return envoyerMessage(numeroClient, message);
    }

    // ============================================
    // 📋 5. ENVOI ITINÉRAIRE COMPLET
    // ============================================
    public static boolean envoyerItineraire(
            String numeroClient,
            String nomClient,
            String destination,
            String pays,
            String dateDepart,
            String dateFin,
            String hotel,
            String reference) {

        String message = String.format(
                "*SmartTrip* - Votre Itineraire\n\n" +
                        "Bonjour *%s* !\n\n" +
                        "Voici votre itineraire complet :\n\n" +
                        "*DESTINATION*\n" +
                        "%s, %s\n\n" +
                        "*DATES*\n" +
                        "Depart : %s\n" +
                        "Retour : %s\n\n" +
                        "*HEBERGEMENT*\n" +
                        "%s\n\n" +
                        "*REFERENCE DOSSIER*\n" +
                        "#%s\n\n" +
                        "Bonne preparation ! SmartTrip",
                nomClient, destination, pays,
                dateDepart, dateFin, hotel, reference
        );

        return envoyerMessage(numeroClient, message);
    }
}