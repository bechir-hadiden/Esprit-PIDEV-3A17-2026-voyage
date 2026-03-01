package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.entities.Reservation;
import org.example.services.AIService;
import org.example.services.ReservationService;
import org.example.services.TransportTypeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AdminStatsController {

    @FXML
    private PieChart typePieChart;
    @FXML
    private BarChart<String, Number> activityBarChart;
    @FXML
    private Label aiAdviceLabel;

    private ReservationService reservationService = new ReservationService();
    private AIService aiService = new AIService();
    private TransportTypeService typeService = new TransportTypeService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        List<Reservation> allReservations = reservationService.listerToutes();

        // Get valid type names from the database (only types that actually exist)
        Set<String> validTypes = typeService.lister().stream()
                .map(t -> t.getNom().trim().toLowerCase())
                .collect(Collectors.toSet());

        // 1. PieChart: Group by type, FILTERED to only existing types
        // (case-insensitive)
        Map<String, Long> byType = allReservations.stream()
                .filter(r -> r.getTypeTransport() != null)
                .filter(r -> validTypes.contains(r.getTypeTransport().trim().toLowerCase()))
                .collect(Collectors.groupingBy(Reservation::getTypeTransport, Collectors.counting()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        byType.forEach((type, count) -> pieData.add(new PieChart.Data(type + " (" + count + ")", count)));
        typePieChart.setData(pieData);
        if (pieData.isEmpty()) {
            typePieChart.setTitle("Aucune réservation trouvée");
        }

        // 2. BarChart: Group by date (last 7 days)
        Map<LocalDate, Long> byDate = allReservations.stream()
                .filter(r -> r.getDateReservation() != null)
                .map(r -> r.getDateReservation().toLocalDate())
                .filter(d -> d.isAfter(LocalDate.now().minusDays(8)))
                .collect(Collectors.groupingBy(d -> d, TreeMap::new, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        // Ensure all last 7 days are present even with 0 bookings
        for (int i = 7; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            long count = byDate.getOrDefault(d, 0L);
            series.getData().add(new XYChart.Data<>(d.format(formatter), count));
        }
        activityBarChart.getData().clear();
        activityBarChart.getData().add(series);

        // 3. AI Analysis
        requestAIAnalysis(byType, byDate);
    }

    private void requestAIAnalysis(Map<String, Long> byType, Map<LocalDate, Long> byDate) {
        // Ajout d'infos sur l'existant réel
        int totalVehicules = new org.example.services.VehiculeService().listerTous().size();

        StringBuilder statsSummary = new StringBuilder();
        statsSummary.append("Répartition par type : ").append(byType.toString()).append("\n");
        statsSummary.append("Total véhicules en BDD : ").append(totalVehicules).append("\n");
        statsSummary.append("Activité récente (7j) : ").append(byDate.toString());

        aiAdviceLabel.setText("L'IA analyse vos données...");

        // Call AI service in a background thread to avoid UI freeze
        new Thread(() -> {
            String advice = aiService.getAdminInsights(statsSummary.toString());
            javafx.application.Platform.runLater(() -> aiAdviceLabel.setText(advice));
        }).start();
    }

    @FXML
    private void refreshAI() {
        loadData();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            Stage stage = (Stage) aiAdviceLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
