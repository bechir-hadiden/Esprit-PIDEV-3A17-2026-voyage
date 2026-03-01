package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.example.entities.Reservation;
import org.example.services.AIService;
import org.example.services.ReservationService;
import org.example.services.TransportTypeService;
import org.example.services.VehiculeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminStatsController {

    @FXML
    private PieChart typePieChart;
    @FXML
    private BarChart<String, Number> activityBarChart;
    @FXML
    private Label aiAdviceLabel;
    @FXML
    private HBox kpiRow;

    private final ReservationService reservationService = new ReservationService();
    private final AIService aiService = new AIService();
    private final TransportTypeService typeService = new TransportTypeService();
    private final VehiculeService vehiculeService = new VehiculeService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        List<Reservation> allReservations = reservationService.listerToutes();

        // Valid type names from DB
        Set<String> validTypes = typeService.lister().stream()
                .map(t -> t.getNom().trim().toLowerCase())
                .collect(Collectors.toSet());

        // ── 1. PieChart ──────────────────────────────────────────────
        Map<String, Long> byType = allReservations.stream()
                .filter(r -> r.getTypeTransport() != null)
                .filter(r -> validTypes.contains(r.getTypeTransport().trim().toLowerCase()))
                .collect(Collectors.groupingBy(Reservation::getTypeTransport, Collectors.counting()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        byType.forEach((type, count) -> pieData.add(new PieChart.Data(type + " (" + count + ")", count)));
        typePieChart.setData(pieData);
        typePieChart.setLegendVisible(true);
        if (pieData.isEmpty())
            typePieChart.setTitle("Aucune réservation trouvée");

        // ── 2. BarChart (last 7 days) ────────────────────────────────
        Map<LocalDate, Long> byDate = allReservations.stream()
                .filter(r -> r.getDateReservation() != null)
                .map(r -> r.getDateReservation().toLocalDate())
                .filter(d -> d.isAfter(LocalDate.now().minusDays(8)))
                .collect(Collectors.groupingBy(d -> d, TreeMap::new, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 7; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            series.getData().add(new XYChart.Data<>(d.format(fmt), byDate.getOrDefault(d, 0L)));
        }
        activityBarChart.getData().clear();
        activityBarChart.getData().add(series);

        // ── Axes labels ─────────────────────────────────────────────
        // Rotate X-axis (dates) vertically as requested
        CategoryAxis xAxis = (CategoryAxis) activityBarChart.getXAxis();
        xAxis.setTickLabelRotation(-90); // dates vertical
        xAxis.setTickMarkVisible(true);
        xAxis.setTickLabelsVisible(true);
        xAxis.setLabel("Date");

        NumberAxis yAxis = (NumberAxis) activityBarChart.getYAxis();
        yAxis.setTickLabelRotation(0); // numbers horizontal
        yAxis.setTickMarkVisible(true);
        yAxis.setTickLabelsVisible(true);
        yAxis.setMinorTickVisible(false);
        yAxis.setLabel("Réservations");
        yAxis.setAutoRanging(true);

        // ── 3. KPI Cards ─────────────────────────────────────────────
        long totalReservations = allReservations.size();
        long totalVehicules = vehiculeService.listerTous().size();
        long totalTypes = typeService.lister().size();
        long today = byDate.getOrDefault(LocalDate.now(), 0L);
        long yesterday = byDate.getOrDefault(LocalDate.now().minusDays(1), 0L);

        // Trend vs yesterday
        String resTrend = computeTrend(today, yesterday);
        String resTrendClass = resTrend.startsWith("▲") ? "kpi-trend-up"
                : resTrend.startsWith("▼") ? "kpi-trend-down" : "kpi-trend-neutral";

        // Disponibility rate
        long disponibles = vehiculeService.listerTous().stream().filter(v -> v.isDisponible()).count();
        int dispoPercent = totalVehicules > 0 ? (int) (disponibles * 100 / totalVehicules) : 0;
        String dispoTrend = dispoPercent >= 70 ? "▲ " + dispoPercent + "% dispo" : "▼ " + dispoPercent + "% dispo";
        String dispoClass = dispoPercent >= 70 ? "kpi-trend-up" : "kpi-trend-down";

        if (kpiRow != null) {
            kpiRow.getChildren().clear();
            kpiRow.getChildren().addAll(
                    makeKpiCard("📋  Réservations Totales", String.valueOf(totalReservations),
                            "Toutes périodes confondues", resTrend, resTrendClass),
                    makeKpiCard("📆  Aujourd'hui", String.valueOf(today),
                            "Réservations créées ce jour", compareDays(today, yesterday), resTrendClass),
                    makeKpiCard("🚗  Flotte", String.valueOf(totalVehicules),
                            "Véhicules dans la base", dispoTrend, dispoClass),
                    makeKpiCard("🏷️  Catégories", String.valueOf(totalTypes),
                            "Types de transport actifs", "→ Stable", "kpi-trend-neutral"));
        }

        // ── 4. AI Analysis ──────────────────────────────────────────
        requestAIAnalysis(byType, byDate, totalVehicules);
    }

    // ── Build one KPI card ────────────────────────────────────────────
    private VBox makeKpiCard(String title, String value, String subtitle,
            String trendText, String trendStyleClass) {
        VBox card = new VBox(10);
        card.getStyleClass().add("kpi-card");
        card.setAlignment(Pos.TOP_LEFT);

        Label lTitle = new Label(title);
        lTitle.getStyleClass().add("kpi-label");

        Label lValue = new Label(value);
        lValue.getStyleClass().add("kpi-number");

        Label lSub = new Label(subtitle);
        lSub.getStyleClass().add("stats-section-sub");

        Label lTrend = new Label(trendText);
        lTrend.getStyleClass().addAll("kpi-trend", trendStyleClass);

        card.getChildren().addAll(lTitle, lValue, lSub, lTrend);
        HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);
        return card;
    }

    // ── Trend helpers ─────────────────────────────────────────────────
    private String computeTrend(long current, long previous) {
        if (previous == 0 && current == 0)
            return "→ Aucun changement";
        if (previous == 0)
            return "▲ Nouveau: +" + current;
        long diff = current - previous;
        int pct = (int) (diff * 100 / previous);
        if (diff > 0)
            return "▲ +" + pct + "% vs hier";
        if (diff < 0)
            return "▼ " + pct + "% vs hier";
        return "→ Stable vs hier";
    }

    private String compareDays(long today, long yesterday) {
        if (today > yesterday)
            return "▲ Plus qu'hier (" + yesterday + ")";
        if (today < yesterday)
            return "▼ Moins qu'hier (" + yesterday + ")";
        return "→ Identique à hier";
    }

    private void requestAIAnalysis(Map<String, Long> byType, Map<LocalDate, Long> byDate, long totalVehicules) {
        StringBuilder summary = new StringBuilder();
        summary.append("Répartition par type : ").append(byType).append("\n");
        summary.append("Total véhicules : ").append(totalVehicules).append("\n");
        summary.append("Activité récente (7j) : ").append(byDate);

        if (aiAdviceLabel != null)
            aiAdviceLabel.setText("⏳  L'IA analyse vos données…");

        new Thread(() -> {
            String advice = aiService.getAdminInsights(summary.toString());
            javafx.application.Platform.runLater(() -> {
                if (aiAdviceLabel != null)
                    aiAdviceLabel.setText(advice);
            });
        }).start();
    }

    @FXML
    private void refreshAI() {
        loadData();
    }

    @FXML
    private void handleBack() {
        try {
            Pane contentArea = (Pane) aiAdviceLabel.getScene().lookup("#contentContainer");
            if (contentArea == null)
                contentArea = (Pane) aiAdviceLabel.getScene().lookup("#contentArea");

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminTransport.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
            } else {
                MainShellController shell = MainShellController.getInstance();
                if (shell != null)
                    shell.loadView("/fxml/admin/AdminTransport.fxml");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
