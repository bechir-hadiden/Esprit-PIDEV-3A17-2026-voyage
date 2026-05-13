package com.example.demo1.controller.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.CheckBox;
import org.example.entities.Paiement;
import org.example.entities.User;
import org.example.services.PaiementService;
import org.example.services.UserService;

import org.example.utils.PDFService;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.sql.Date;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.*;
import org.example.utils.DatabaseConnection;

public class AdminPaymentsController {
    private static final String FILTER_ALL = "All Statuses";
    private static final String STATUS_PENDING = "En attente";
    private static final String STATUS_COMPLETED = "Effectu\u00E9";
    private static final String STATUS_CANCELED = "Annul\u00E9";

    @FXML
    private TableView<Paiement> paymentsTable;
    @FXML
    private TableColumn<Paiement, Boolean> selectColumn;
    @FXML
    private TableColumn<Paiement, String> dateColumn;
    @FXML
    private TableColumn<Paiement, String> userColumn;
    @FXML
    private TableColumn<Paiement, String> bookingColumn;
    @FXML
    private TableColumn<Paiement, Double> amountColumn;
    @FXML
    private TableColumn<Paiement, String> methodColumn;
    @FXML
    private TableColumn<Paiement, String> statusColumn;
    @FXML
    private TableColumn<Paiement, Void> aiColumn;
    @FXML
    private TableColumn<Paiement, Void> actionsColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;

    private final PaiementService paiementService = new PaiementService();
    private final UserService userService = new UserService();
    private final ObservableList<Paiement> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupFilters();
        applyFilters();
    }

    private void setupTable() {
        // Checkbox column
        selectColumn.setCellFactory(column -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : cb);
                setAlignment(Pos.CENTER);
            }
        });

        // DATE Column (with time)
        dateColumn.setCellValueFactory(data -> {
            Date paymentDate = data.getValue().getDatePaiement();
            return new javafx.beans.property.SimpleStringProperty(paymentDate != null ? paymentDate.toString() + " 00:00" : "-");
        });

        // CLIENT Column (Name + Email)
        userColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Paiement p = getTableRow().getItem();
                User u = userService.getUserById(p.getUserId());
                VBox box = new VBox(2);
                Label name = new Label(u != null ? u.getName() : "User #" + p.getUserId());
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
                Label email = new Label(u != null ? u.getEmail() : "");
                email.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");
                box.getChildren().addAll(name, email);
                setGraphic(box);
            }
        });

        // RÉSERVATION # Column (Badge style)
        bookingColumn.setCellValueFactory(data -> {
            Integer bookingId = data.getValue().getBookingId();
            return new javafx.beans.property.SimpleStringProperty(bookingId != null ? "#" + bookingId : "-");
        });
        bookingColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.equals("-")) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // MONTANT Column (DT formatting)
        amountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getMontant()).asObject());
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", item).replace('.', ','));
                    setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold;");
                }
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        // MÉTHODE Column (Icon + Text)
        methodColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMethodePaiement()));
        methodColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label label = new Label();
                label.setStyle("-fx-text-fill: #1e293b;");
                if (item.toLowerCase().contains("stripe")) {
                    label.setText("💳 Stripe");
                } else if (item.toLowerCase().contains("portefeuille") || item.toLowerCase().contains("wallet")) {
                    label.setText("💰 Portefeuille Interne");
                } else if (item.toLowerCase().contains("carte")) {
                    label.setText("💳 Carte Bancaire");
                } else {
                    label.setText("💵 " + item);
                }
                setGraphic(label);
            }
        });

        // STATUT Column (Badge style "Validé")
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut_paiement()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                String text = isCompletedStatus(item) ? "Validé" : item;
                Label badge = new Label(text);
                if (isCompletedStatus(item)) {
                    badge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 3 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 11px;");
                } else if (isPendingStatus(item)) {
                    badge.setStyle("-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-padding: 3 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 11px;");
                } else {
                    badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 3 12; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 11px;");
                }
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // ANALYSE IA Column (SÉCURISÉ + Progress Bar)
        aiColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                VBox box = new VBox(2);
                HBox top = new HBox(5);
                Label secure = new Label("SÉCURISÉ");
                secure.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold; -fx-font-size: 10px;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label percent = new Label("0%");
                percent.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold; -fx-font-size: 10px;");
                top.getChildren().addAll(secure, spacer, percent);
                
                ProgressBar pb = new ProgressBar(0);
                pb.setPrefWidth(100);
                pb.setPrefHeight(6);
                pb.setStyle("-fx-accent: #10b981; -fx-control-inner-background: #f3f4f6;");
                
                box.getChildren().addAll(top, pb);
                setGraphic(box);
            }
        });

        // ACTIONS Column (Small icons)
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button pdfBtn = new Button("📄");
            private final Button editBtn = new Button("📝");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox container = new HBox(5, pdfBtn, editBtn, deleteBtn);
            {
                container.setAlignment(Pos.CENTER);
                pdfBtn.setStyle("-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-padding: 4 8; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; -fx-padding: 4 8; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 4 8; -fx-cursor: hand;");
                
                pdfBtn.setTooltip(new Tooltip("PDF Receipt"));
                editBtn.setTooltip(new Tooltip("Edit Payment"));
                deleteBtn.setTooltip(new Tooltip("Delete Payment"));

                pdfBtn.setOnAction(e -> {
                    Paiement p = getTableRow().getItem();
                    if (p != null) handleDownloadPDF(p);
                });
                editBtn.setOnAction(e -> {
                    Paiement p = getTableRow().getItem();
                    if (p != null) handleEdit(p);
                });
                deleteBtn.setOnAction(e -> {
                    Paiement p = getTableRow().getItem();
                    if (p != null) handleDelete(p);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void loadData() {
        masterData.setAll(paiementService.afficher());
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                FILTER_ALL,
                STATUS_PENDING,
                STATUS_COMPLETED,
                STATUS_CANCELED));
        statusFilter.setValue(FILTER_ALL);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilter.getValue() == null ? FILTER_ALL : statusFilter.getValue();

        List<Paiement> filtered = masterData.stream()
                .filter(paiement -> {
                    String username = getUserName(paiement.getUserId()).toLowerCase();
                    boolean matchesSearch = search.isEmpty()
                            || String.valueOf(paiement.getIdPaiement()).contains(search)
                            || username.contains(search);

                    boolean matchesStatus = matchesStatusFilter(paiement.getStatut_paiement(), selectedStatus);
                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        paymentsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private boolean matchesStatusFilter(String paymentStatus, String selectedStatus) {
        if (FILTER_ALL.equalsIgnoreCase(selectedStatus)) {
            return true;
        }
        if (STATUS_PENDING.equalsIgnoreCase(selectedStatus)) {
            return isPendingStatus(paymentStatus);
        }
        if (STATUS_COMPLETED.equalsIgnoreCase(selectedStatus)) {
            return isCompletedStatus(paymentStatus);
        }
        if (STATUS_CANCELED.equalsIgnoreCase(selectedStatus)) {
            return isCanceledStatus(paymentStatus);
        }
        return normalizeToken(paymentStatus).equals(normalizeToken(selectedStatus));
    }

    private String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .trim();
        // Handle existing mojibake variants that may already exist in DB rows.
        return normalized
                .replace("ã©", "e")
                .replace("ã¨", "e")
                .replace("ã", "a");
    }

    private boolean isPendingStatus(String status) {
        return normalizeToken(status).startsWith("en attente");
    }

    private boolean isCompletedStatus(String status) {
        return normalizeToken(status).startsWith("effectu");
    }

    private boolean isCanceledStatus(String status) {
        return normalizeToken(status).startsWith("annul");
    }

    private String getUserName(int userId) {
        User user = userService.getUserById(userId);
        if (user != null && user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        return "User #" + userId;
    }

    private void handleEdit(Paiement paiement) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modification Financière");
        dialog.setHeaderText("Mise à jour du paiement #" + paiement.getIdPaiement());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- Fields Setup ---
        ComboBox<User> userBox = new ComboBox<>(FXCollections.observableArrayList(userService.getAllUsers()));
        userBox.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getEmail());
            }
        });
        userBox.setButtonCell(userBox.getCellFactory().call(null));
        userBox.setMaxWidth(Double.MAX_VALUE);
        // Find and select current user
        userBox.getItems().stream().filter(u -> u.getId() == paiement.getUserId()).findFirst().ifPresent(userBox::setValue);

        ComboBox<Plan> planBox = new ComboBox<>(FXCollections.observableArrayList(getAllPlans()));
        planBox.getItems().add(0, new Plan(0, "Aucun abonnement"));
        planBox.setMaxWidth(Double.MAX_VALUE);
        if (paiement.getPlanId() != null) {
            planBox.getItems().stream().filter(p -> p.id == paiement.getPlanId()).findFirst().ifPresent(planBox::setValue);
        } else {
            planBox.getSelectionModel().selectFirst();
        }

        TextField amountField = new TextField(String.valueOf(paiement.getMontant()));
        TextField methodField = new TextField(paiement.getMethodePaiement() != null ? paiement.getMethodePaiement() : "");
        
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
                STATUS_PENDING,
                STATUS_COMPLETED,
                STATUS_CANCELED));
        statusBox.setMaxWidth(Double.MAX_VALUE);
        String currentStatus = paiement.getStatut_paiement();
        if (isCompletedStatus(currentStatus)) statusBox.setValue(STATUS_COMPLETED);
        else if (isCanceledStatus(currentStatus)) statusBox.setValue(STATUS_CANCELED);
        else statusBox.setValue(STATUS_PENDING);

        // Billing Details
        TextField nomField = new TextField(paiement.getNomFacturation() != null ? paiement.getNomFacturation() : "");
        TextField prenomField = new TextField(paiement.getPrenomFacturation() != null ? paiement.getPrenomFacturation() : "");
        TextField emailField = new TextField(paiement.getEmailFacturation() != null ? paiement.getEmailFacturation() : "");
        TextField phoneField = new TextField(paiement.getTelephoneFacturation() != null ? paiement.getTelephoneFacturation() : "");

        // --- Layout ---
        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(10);
        form.setPadding(new javafx.geometry.Insets(20));

        // Row 0
        form.add(new Label("UTILISATEUR BÉNÉFICIAIRE"), 0, 0);
        form.add(new Label("ABONNEMENT LIÉ (OPTIONNEL)"), 1, 0);
        // Row 1
        form.add(userBox, 0, 1);
        form.add(planBox, 1, 1);

        // Row 2
        form.add(new Label("MONTANT TRANSACTION (DT)"), 0, 2);
        form.add(new Label("MODE DE RÈGLEMENT"), 1, 2);
        // Row 3
        form.add(amountField, 0, 3);
        form.add(methodField, 1, 3);

        // Row 4
        form.add(new Label("STATUT DE LA TRANSACTION"), 0, 4, 2, 1);
        // Row 5
        form.add(statusBox, 0, 5, 2, 1);

        // Section header
        Label billingTitle = new Label("DÉTAILS DE FACTURATION");
        billingTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #6b7280; -fx-padding: 20 0 5 0;");
        form.add(billingTitle, 0, 6, 2, 1);

        // Row 7
        form.add(new Label("NOM"), 0, 7);
        form.add(new Label("PRÉNOM"), 1, 7);
        // Row 8
        form.add(nomField, 0, 8);
        form.add(prenomField, 1, 8);

        // Row 9
        form.add(new Label("EMAIL FACTURATION"), 0, 9);
        form.add(new Label("TÉLÉPHONE"), 1, 9);
        // Row 10
        form.add(emailField, 0, 10);
        form.add(phoneField, 1, 10);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(600);

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;

            try {
                double amount = Double.parseDouble(amountField.getText().trim().replace(',', '.'));
                if (amount <= 0) throw new IllegalArgumentException("Le montant doit etre positif.");

                User selectedUser = userBox.getValue();
                if (selectedUser == null) throw new IllegalArgumentException("L'utilisateur est obligatoire.");

                paiement.setUserId(selectedUser.getId());
                paiement.setMontant(amount);
                paiement.setMethodePaiement(methodField.getText().trim());
                paiement.setStatut_paiement(statusBox.getValue());
                
                Plan selectedPlan = planBox.getValue();
                paiement.setPlanId(selectedPlan != null && selectedPlan.id > 0 ? selectedPlan.id : null);

                paiement.setNomFacturation(nomField.getText().trim());
                paiement.setPrenomFacturation(prenomField.getText().trim());
                paiement.setEmailFacturation(emailField.getText().trim());
                paiement.setTelephoneFacturation(phoneField.getText().trim());

                paiementService.modifier(paiement);
                loadData();
                applyFilters();
                showInfo("Success", "Paiement mis à jour avec succès.");
            } catch (Exception ex) {
                showError("Erreur", "Echec de modification: " + ex.getMessage());
            }
        });
    }

    private List<Plan> getAllPlans() {
        List<Plan> plans = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM plans")) {
            while (rs.next()) {
                plans.add(new Plan(rs.getInt("id"), rs.getString("name")));
            }
        } catch (Exception e) {
            System.err.println("Error fetching plans: " + e.getMessage());
        }
        return plans;
    }

    private static class Plan {
        int id;
        String name;
        Plan(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private void handleDownloadPDF(Paiement paiement) {
        try {
            User user = userService.getUserById(paiement.getUserId());
            String fileName = "Confirmation_Paiement_" + paiement.getIdPaiement() + ".pdf";
            String filePath = System.getProperty("user.home") + File.separator + fileName;

            PDFService.generatePaymentReceipt(paiement, user, filePath);

            File file = new File(filePath);
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                showInfo("PDF Généré", "Le reçu a été téléchargé dans : " + filePath);
            }
        } catch (Exception e) {
            showError("Erreur PDF", "Échec de génération du PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDelete(Paiement paiement) {
        Alert alert = new Alert(
                Alert.AlertType.WARNING,
                "Supprimer le paiement #" + paiement.getIdPaiement() + " ?",
                ButtonType.YES,
                ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                paiementService.supprimer(paiement.getIdPaiement());
                loadData();
                applyFilters();
            }
        });
    }

    @FXML
    private void handleOpenStripe() {
        try {
            Desktop.getDesktop().browse(new URI("https://dashboard.stripe.com/test/payments"));
        } catch (Exception e) {
            showError("Error", "Could not open browser: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
        applyFilters();
    }

    @FXML
    private void handleShowRealizedPayments() {
        loadData();
        statusFilter.setValue(STATUS_COMPLETED);
        applyFilters();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
