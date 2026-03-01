package org.example.services;

import org.example.entities.BaseVehicule;
import org.example.entities.Bus;
import org.example.entities.Taxi;
import org.example.entities.Voiture;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AIService {

    private VehiculeService vehiculeService = new VehiculeService();
    private Random random = new Random();

    private String escapeJson(String input) {
        if (input == null)
            return "\"\"";
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    // IMPORTANT: Remplacez par votre clé API OpenAI réelle
    private static final String API_KEY = "votre_cle_openai_ici";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public static class AIRecommendation {
        public BaseVehicule vehicule;
        public String reason;

        public AIRecommendation(BaseVehicule v, String r) {
            this.vehicule = v;
            this.reason = r;
        }
    }

    public AIRecommendation getDetailedRecommendation(String start, String end) {
        List<BaseVehicule> all = vehiculeService.listerTous();
        if (all.isEmpty())
            return null;

        // Si la clé n'est pas configurée, on utilise le fallback
        if (API_KEY.equals("votre_cle_openai_ici")) {
            return getHeuristicRecommendation(all, start, end);
        }

        try {
            String vehiclesList = all.stream()
                    .filter(BaseVehicule::isDisponible)
                    .map(v -> "ID:" + v.getId() + " - " + v.getType() + " (" + v.getCompagnie() + ", Capacité:"
                            + v.getCapacite() + ", Tarif/Base:" + v.getPrix() + " DT)")
                    .collect(Collectors.joining("\n"));

            String prompt = "Tu es un assistant de transport expert en Tunisie. " +
                    "Voici la liste des véhicules disponibles :\n" + vehiclesList + "\n\n" +
                    "L'utilisateur veut aller de '" + start + "' à '" + end + "'.\n" +
                    "Recommande le MEILLEUR véhicule de la liste et donne une raison courte et convaincante basée sur la distance, le confort ou le type de trajet.\n"
                    +
                    "Réponds STRICTEMENT au format JSON suivant : {\"vehicule_id\": X, \"reason\": \"...\"}";

            String jsonPayload = "{" +
                    "\"model\": \"gpt-3.5-turbo\"," +
                    "\"messages\": [{\"role\": \"user\", \"content\": " + escapeJson(prompt) + "}]," +
                    "\"temperature\": 0.7" +
                    "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                // Extraction simple du JSON imbriqué dans la réponse OpenAI
                // Car OpenAI renvoie un gros JSON, on cherche le contenu du message
                Pattern contentPattern = Pattern.compile("\"content\":\\s*\"(.*?)\"", Pattern.DOTALL);
                Matcher contentMatcher = contentPattern.matcher(body);
                if (contentMatcher.find()) {
                    String aiText = contentMatcher.group(1).replace("\\\"", "\"").replace("\\n", "\n");

                    // Maintenant on cherche notre petit JSON final dans le texte d'OpenAI
                    Pattern idPattern = Pattern.compile("\"vehicule_id\":\\s*(\\d+)");
                    Pattern reasonPattern = Pattern.compile("\"reason\":\\s*\"(.*?)\"");

                    Matcher idM = idPattern.matcher(aiText);
                    Matcher reasonM = reasonPattern.matcher(aiText);

                    if (idM.find() && reasonM.find()) {
                        int id = Integer.parseInt(idM.group(1));
                        String reason = reasonM.group(1);

                        return all.stream()
                                .filter(v -> v.getId() == id)
                                .findFirst()
                                .map(v -> new AIRecommendation(v, reason))
                                .orElse(getHeuristicRecommendation(all, start, end));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback en cas d'erreur API
        return getHeuristicRecommendation(all, start, end);
    }

    private AIRecommendation getHeuristicRecommendation(List<BaseVehicule> all, String start, String end) {
        String s = start.toLowerCase();
        String e = end.toLowerCase();

        boolean isTunis = s.contains("tunis") || s.contains("cedria") || s.contains("ariana")
                || s.contains("ben arous");
        boolean destTunis = e.contains("tunis") || e.contains("cedria") || e.contains("ariana")
                || e.contains("ben arous");

        boolean differentRegion = false;
        String reasonStr = "";

        if (isTunis && !destTunis) {
            differentRegion = true;
            reasonStr = "Ce trajet sort de la zone du Grand Tunis. Un Bus est recommandé pour sa fiabilité sur de longues distances inter-régionales.";
        } else if (!isTunis && destTunis) {
            differentRegion = true;
            reasonStr = "Vous entrez dans la capitale. Le Bus est l'option la plus économique et sécurisée pour ce type de voyage.";
        } else if (isTunis && destTunis) {
            reasonStr = "Trajet local détecté dans le Grand Tunis. Un Taxi ou une Voiture offre la meilleure flexibilité pour vos déplacements urbains.";
        } else {
            differentRegion = true;
            reasonStr = "Distance inter-urbaine importante détectée. L'AI suggère un transport collectif (Bus) pour optimiser votre confort.";
        }

        List<BaseVehicule> candidates;
        if (differentRegion) {
            candidates = all.stream()
                    .filter(v -> v.isDisponible() && v instanceof Bus)
                    .collect(Collectors.toList());
            if (candidates.isEmpty())
                candidates = all.stream()
                        .filter(v -> v.isDisponible() && v instanceof Voiture)
                        .collect(Collectors.toList());
        } else {
            candidates = all.stream()
                    .filter(v -> v.isDisponible() && (v instanceof Taxi || v instanceof Voiture))
                    .collect(Collectors.toList());
        }

        BaseVehicule selected = candidates.isEmpty() ? all.get(random.nextInt(all.size()))
                : candidates.get(random.nextInt(candidates.size()));
        return new AIRecommendation(selected, reasonStr);
    }

    public String getAdminInsights(String statsSummary) {
        if (API_KEY.equals("votre_cle_openai_ici")) {
            // Simulation pour la démonstration si pas de clé
            return "💡 [MODE DÉMO - IA SIMULÉE]\n\n" +
                    "1. **Optimisation Weekend** : Vos données montrent une forte demande le samedi. Envisagez de mettre 2 bus supplémentaires en service pour éviter les surcharges.\n"
                    +
                    "2. **Promotion Mi-Semaine** : Les mardis sont calmes. Une réduction de 15% sur les trajets en 'Voiture' pourrait booster l'occupation.\n"
                    +
                    "3. **Maintenance Préventive** : Votre flotte de Taxis a été très sollicitée. Une révision technique est conseillée pour les véhicules ID: 12 et 14.";
        }

        try {
            String prompt = "Tu es un expert en logistique de transport. Voici un résumé des statistiques de réservations récentes :\n"
                    + statsSummary + "\n\n" +
                    "Analyse ces données et donne 3 conseils stratégiques courts pour l'administrateur (ex: augmenter le nombre de bus le weekend, baisser les prix en semaine, etc.). Réponds de manière concise et professionnelle en français.";

            String jsonPayload = "{" +
                    "\"model\": \"gpt-3.5-turbo\"," +
                    "\"messages\": [{\"role\": \"user\", \"content\": " + escapeJson(prompt) + "}]," +
                    "\"temperature\": 0.7" +
                    "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                Pattern contentPattern = Pattern.compile("\"content\":\\s*\"(.*?)\"", Pattern.DOTALL);
                Matcher contentMatcher = contentPattern.matcher(body);
                if (contentMatcher.find()) {
                    return contentMatcher.group(1).replace("\\\"", "\"").replace("\\n", "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Analyse indisponible pour le moment. Vérifiez votre connexion.";
    }
}
