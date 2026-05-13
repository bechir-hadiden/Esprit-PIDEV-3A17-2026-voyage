package com.example.demo1.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChatbotService {

    // 🔑 VOTRE CLÉ API GROQ (obtenez-la sur https://console.groq.com)

    // 🌐 URL de l'API Groq
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    /**
     * Envoie un message au chatbot Groq
     * @param messageUtilisateur Question du client
     * @return Réponse du chatbot
     */
    public static String envoyerMessage(String messageUtilisateur) {
        try {
            System.out.println("========================================");
            System.out.println("🤖 Groq AI - Envoi du message...");
            System.out.println("📝 Message: " + messageUtilisateur);

            // Construction du contexte système
            String systemPrompt =
                    "Tu es l'assistant virtuel de Smart Trip, une agence de voyage tunisienne.\n\n" +
                            "🎯 TON RÔLE :\n" +
                            "- Aide les clients à trouver des destinations de voyage\n" +
                            "- Réponds aux questions sur les vols, prix et réservations\n" +
                            "- Fournis des conseils de voyage personnalisés\n\n" +

                            "📍 NOS DESTINATIONS :\n" +
                            "Paris (350€), Rome (400€), Barcelone (280€), Istanbul (320€), " +
                            "Dubaï (650€), Londres (420€), Madrid (380€), Le Caire (450€)\n\n" +

                            "💰 PRIX MOYENS :\n" +
                            "Europe : 200-500€ | Moyen-Orient : 500-800€\n\n" +

                            "🎨 STYLE :\n" +
                            "- Réponds en français\n" +
                            "- Sois chaleureux et professionnel\n" +
                            "- Reste concis (2-4 phrases)\n" +
                            "- Utilise des emojis avec modération (🌍 ✈️ 💰 🏨)";

            // Construction du JSON pour Groq
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "llama-3.3-70b-versatile"); // Modèle gratuit et puissant

            JsonArray messages = new JsonArray();

            // Message système
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", systemPrompt);
            messages.add(systemMessage);

            // Message utilisateur
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", messageUtilisateur);
            messages.add(userMessage);

            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 300);
            requestBody.addProperty("top_p", 0.9);

            System.out.println("📡 Envoi à Groq API...");

            // Appel API
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_API_URL))
                    .header("Authorization", "Bearer " + GROQ_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("📊 Code réponse: " + response.statusCode());

            if (response.statusCode() == 200) {
                // Parser la réponse
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

                if (jsonResponse.has("choices")) {
                    JsonArray choices = jsonResponse.getAsJsonArray("choices");

                    if (choices.size() > 0) {
                        String reponse = choices.get(0).getAsJsonObject()
                                .getAsJsonObject("message")
                                .get("content").getAsString();

                        System.out.println("✅ Réponse reçue de Groq");
                        System.out.println("💬 Aperçu: " + reponse.substring(0, Math.min(100, reponse.length())) + "...");
                        System.out.println("========================================");
                        return reponse.trim();
                    }
                }
            } else if (response.statusCode() == 401) {
                System.err.println("❌ Erreur 401 - Clé API invalide");
                System.err.println("📋 Vérifiez votre clé sur https://console.groq.com");
                return "❌ Erreur d'authentification. Veuillez vérifier la configuration.";

            } else if (response.statusCode() == 429) {
                System.err.println("❌ Erreur 429 - Quota dépassé");
                return "⏳ Service temporairement surchargé. Réessayez dans quelques instants.";

            } else {
                System.err.println("❌ Erreur " + response.statusCode());
                System.err.println("📋 Réponse: " + response.body());
                return "❌ Une erreur s'est produite. Veuillez réessayer.";
            }

        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
            return "❌ Erreur technique. Veuillez réessayer dans un instant.";
        }

        return "Désolé, je n'ai pas pu traiter votre demande. 🔧";
    }

    /**
     * Test du chatbot
     */
    public static void main(String[] args) {
        System.out.println("🧪 TEST DU CHATBOT GROQ");
        System.out.println("========================================\n");

        String[] tests = {
                "Bonjour !",
                "Quelles destinations proposez-vous ?",
                "Combien coûte un vol pour Paris ?",
                "Je veux partir à Dubaï, des conseils ?",
                "Comment réserver un vol ?",
                "Merci !"
        };

        for (String test : tests) {
            System.out.println("\n👤 CLIENT: " + test);
            String reponse = envoyerMessage(test);
            System.out.println("\n🤖 ASSISTANT: " + reponse);
            System.out.println("\n" + "=".repeat(70) + "\n");

            try {
                Thread.sleep(1000); // Pause entre requêtes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("✅ Test terminé !");
    }
}