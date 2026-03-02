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
import java.util.List;
import java.util.stream.Collectors;

public class AdminPaymentsController {
    private static final String FILTER_ALL = "All Statuses";
    private static final String STATUS_PENDING = "En attente";
    private static final String STATUS_COMPLETED = "Effectu\u00E9";
    private static final String STATUS_CANCELED = "Annul\u00E9";

    @FXML
    private TableView<Paiement> paymentsTable;
    @FXML
    private TableColumn<Paiement, Integer> idColumn;
    @FXML
    private TableColumn<Paiement, String> userColumn;
    @FXML
    private TableColumn<Paiement, Double> amountColumn;
    @FXML
    private TableColumn<Paiement, String> dateColumn;
    @FXML
    private TableColumn<Paiement, String> methodColumn;
    @FXML
    private TableColumn<Paiement, String> statusColumn;
    @FXML
    private TableColumn<Paiement, String> bookingColumn;
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
        idColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdPaiement()).asObject());
        userColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(getUserName(data.getValue().getUserId())));
        amountColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getMontant()).asObject());
        dateColumn.setCellValueFactory(data -> {
            Date paymentDate = data.getValue().getDatePaiement();
            return new javafx.beans.property.SimpleStringProperty(paymentDate != null ? paymentDate.toString() : "-");
        });
        methodColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMethodePaiement()));

        statusColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut_paiement()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                Label badge = new Label(item);
                badge.getStyleClass().add("badge");
                if (isCompletedStatus(item)) {
                    badge.getStyleClass().add("badge-success");
                } else if (isPendingStatus(item)) {
                    badge.getStyleClass().add("badge-warning");
                } else {
                    badge.getStyleClass().add("badge-danger");
                }
                setGraphic(badge);
            }
        });

        bookingColumn.setCellValueFactory(data -> {
            Integer bookingId = data.getValue().getBookingId();
            return new javafx.beans.property.SimpleStringProperty(bookingId != null ? "#" + bookingId : "-");
        });

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button pdfBtn = new Button("PDF");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox container = new HBox(8, editBtn, pdfBtn, deleteBtn);

            {
                container.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("admin-button-secondary");
                pdfBtn.getStyleClass().add("admin-button-info");
                deleteBtn.getStyleClass().add("admin-button-danger");
                editBtn.setTooltip(new Tooltip("Modifier le paiement"));
                pdfBtn.setTooltip(new Tooltip("Télécharger la confirmation PDF"));
                deleteBtn.setTooltip(new Tooltip("Supprimer le paiement"));

                // Style
                editBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12px;");
                pdfBtn.setStyle(
                        "-fx-padding: 6 12; -fx-font-size: 12px; -fx-background-color: #0ea5e9; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12px;");

                editBtn.setOnAction(e -> {
                    Paiement paiement = getCurrentRowPayment();
                    if (paiement != null) {
                        handleEdit(paiement);
                    }
                });

                pdfBtn.setOnAction(e -> {
                    Paiement paiement = getCurrentRowPayment();
                    if (paiement != null) {
                        handleDownloadPDF(paiement);
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Paiement paiement = getCurrentRowPayment();
                    if (paiement != null) {
                        handleDelete(paiement);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getCurrentRowPayment() == null ? null : container);
            }

            private Paiement getCurrentRowPayment() {
                int rowIndex = getIndex();
                if (rowIndex < 0 || rowIndex >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(rowIndex);
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
        dialog.setTitle("Modifier paiement");
        dialog.setHeaderText("Paiement #" + paiement.getIdPaiement());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField amountField = new TextField(String.valueOf(paiement.getMontant()));
        DatePicker datePicker = new DatePicker(
                paiement.getDatePaiement() != null ? paiement.getDatePaiement().toLocalDate() : LocalDate.now());
        TextField methodField = new TextField(
                paiement.getMethodePaiement() != null ? paiement.getMethodePaiement() : "");
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
                STATUS_PENDING,
                STATUS_COMPLETED,
                STATUS_CANCELED));
        statusBox.setMaxWidth(Double.MAX_VALUE);

        String currentStatus = paiement.getStatut_paiement();
        if (isCompletedStatus(currentStatus)) {
            statusBox.setValue(STATUS_COMPLETED);
        } else if (isCanceledStatus(currentStatus)) {
            statusBox.setValue(STATUS_CANCELED);
        } else {
            statusBox.setValue(STATUS_PENDING);
        }

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Montant"), 0, 0);
        form.add(amountField, 1, 0);
        form.add(new Label("Date"), 0, 1);
        form.add(datePicker, 1, 1);
        form.add(new Label("Methode"), 0, 2);
        form.add(methodField, 1, 2);
        form.add(new Label("Statut"), 0, 3);
        form.add(statusBox, 1, 3);

        dialog.getDialogPane().setContent(form);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(false);
        }

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) {
                return;
            }

            try {
                String amountInput = amountField.getText() == null ? ""
                        : amountField.getText().trim().replace(',', '.');
                double amount = Double.parseDouble(amountInput);
                if (amount <= 0) {
                    throw new IllegalArgumentException("Le montant doit etre positif.");
                }

                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate == null) {
                    throw new IllegalArgumentException("La date est obligatoire.");
                }

                String method = methodField.getText() == null ? "" : methodField.getText().trim();
                if (method.isEmpty()) {
                    throw new IllegalArgumentException("La methode est obligatoire.");
                }

                String newStatus = statusBox.getValue();
                if (newStatus == null || newStatus.isBlank()) {
                    throw new IllegalArgumentException("Le statut est obligatoire.");
                }

                paiement.setMontant(amount);
                paiement.setDatePaiement(Date.valueOf(selectedDate));
                paiement.setMethodePaiement(method);
                paiement.setStatut_paiement(newStatus);

                paiementService.modifier(paiement);
                loadData();
                applyFilters();
                showInfo("Success", "Paiement modifie avec succes.");
            } catch (NumberFormatException ex) {
                showError("Erreur", "Le montant doit etre un nombre valide.");
            } catch (IllegalArgumentException ex) {
                showError("Erreur", ex.getMessage());
            } catch (Exception ex) {
                showError("Erreur", "Echec de modification: " + ex.getMessage());
            }
        });
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
