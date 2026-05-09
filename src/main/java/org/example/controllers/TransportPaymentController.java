package org.example.controllers;

import com.example.demo1.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.entities.Paiement;
import org.example.entities.Reservation;
import org.example.entities.User;
import org.example.services.PaiementService;
import org.example.utils.PDFService;

import java.io.File;
import java.awt.Desktop;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Controller for the Transport Payment screen.
 * Handles Stripe, Internal Wallet, PayPal, and D17 payments for transport
 * reservations.
 */
public class TransportPaymentController {

    @FXML
    private Label reservationInfoLabel;
    @FXML
    private Label amountLabel;
    @FXML
    private Label walletBalanceLabel;
    @FXML
    private Label loyaltyPointsLabel;
    @FXML
    private Label walletStatusLabel;
    @FXML
    private ToggleGroup paymentGroup;
    @FXML
    private ToggleButton stripeToggle;
    @FXML
    private ToggleButton walletToggle;
    @FXML
    private ToggleButton paypalToggle;
    @FXML
    private ToggleButton d17Toggle;
    @FXML
    private VBox stripeDetails;
    @FXML
    private VBox walletDetails;
    @FXML
    private VBox paypalDetails;
    @FXML
    private VBox d17Details;
    @FXML
    private Button payButton;

    private Reservation reservation;
    private User currentUser;
    private final PaiementService paiementService = new PaiementService();

    @FXML
    public void initialize() {
        setupPaymentToggles();
    }

    private void setupPaymentToggles() {
        if (paymentGroup == null)
            return;

        paymentGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                stripeToggle.setSelected(true);
                return;
            }
            stripeDetails.setVisible(newT == stripeToggle);
            walletDetails.setVisible(newT == walletToggle);
            paypalDetails.setVisible(newT == paypalToggle);
            d17Details.setVisible(newT == d17Toggle);

            if (newT == stripeToggle) {
                payButton.setText("Pay with Stripe");
                payButton.setStyle(
                        "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 16 0; -fx-background-radius: 12; -fx-cursor: hand;");
            } else if (newT == walletToggle) {
                payButton.setText("💰 Payer avec Portefeuille");
                payButton.setStyle(
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 16 0; -fx-background-radius: 12; -fx-cursor: hand;");
            } else if (newT == paypalToggle) {
                payButton.setText("🅿️ Payer avec PayPal");
                payButton.setStyle(
                        "-fx-background-color: #0070ba; -fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 16 0; -fx-background-radius: 12; -fx-cursor: hand;");
            } else if (newT == d17Toggle) {
                payButton.setText("📲 Payer avec D17");
                payButton.setStyle(
                        "-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 16 0; -fx-background-radius: 12; -fx-cursor: hand;");
            }
        });

        stripeToggle.setSelected(true);
    }

    public void setReservation(Reservation r, User user) {
        this.reservation = r;
        this.currentUser = user;

        reservationInfoLabel.setText(
                "Réservation #" + r.getIdReservation() + " · " + r.getTransportCompagnie() + " · "
                        + r.getTransportType());
        amountLabel.setText(String.format("%.2f DT", r.getTransportPrix()));

        // Refresh wallet from auth session if available
        try {
            com.example.demo1.entity.User authUser = AuthService.getInstance().getCurrentUser();
            if (authUser != null) {
                walletBalanceLabel.setText(String.format("%.2f DT", authUser.getWalletBalance()));
                loyaltyPointsLabel.setText(authUser.getLoyaltyPoints() + " pts");

                if (authUser.getWalletBalance() < r.getTransportPrix()) {
                    walletStatusLabel.setText(
                            "Solde insuffisant (" + String.format("%.2f", r.getTransportPrix()) + " DT requis)");
                    walletStatusLabel.setTextFill(Color.RED);
                } else {
                    walletStatusLabel.setText("Payez instantanément avec votre solde.");
                    walletStatusLabel.setTextFill(Color.valueOf("#64748B"));
                }
            }
        } catch (Exception e) {
            // Fallback
        }
    }

    @FXML
    private void handlePay() {
        if (reservation == null)
            return;

        ToggleButton selected = (ToggleButton) paymentGroup.getSelectedToggle();
        String method;
        if (selected == stripeToggle)
            method = "Carte Bancaire (Stripe)";
        else if (selected == walletToggle)
            method = "Portefeuille Interne";
        else if (selected == paypalToggle)
            method = "PayPal";
        else
            method = "D17";

        double price = reservation.getTransportPrix();

        // Get user ID - from currentUser or auth service
        int userId = 0;
        if (currentUser != null) {
            userId = currentUser.getIdUser();
        }
        // Fallback: try auth service
        if (userId == 0) {
            try {
                com.example.demo1.entity.User authUser = AuthService.getInstance().getCurrentUser();
                if (authUser != null && authUser.getId() != null) {
                    userId = Integer.parseInt(authUser.getId());
                }
            } catch (Exception ignored) {
            }
        }

        // Insert directly using the reliable Database singleton connection
        boolean saved = false;
        String sql = "INSERT INTO paiements (montant, date_paiement, statut_paiement, methode_paiement, stripe_session_id, user_id, booking_id) VALUES (?, CURDATE(), ?, ?, NULL, ?, NULL)";
        try (java.sql.Connection conn = com.example.demo1.Utils.Database.getInstance().getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, price);
            ps.setString(2, "Effectué");
            ps.setString(3, method);
            ps.setInt(4, userId);
            int rows = ps.executeUpdate();
            saved = rows > 0;
            System.out
                    .println("Transport payment inserted: " + rows + " row(s), userId=" + userId + ", amount=" + price);
        } catch (Exception e) {
            System.err.println("Transport payment insert error: " + e.getMessage());
            e.printStackTrace();
        }

        if (saved) {
            // Generate PDF Receipt
            try {
                Paiement p = new Paiement();
                p.setMontant(price);
                p.setDatePaiement(java.sql.Date.valueOf(java.time.LocalDate.now()));
                p.setStatut_paiement("Effectué");
                p.setMethodePaiement(method);
                p.setUserId(userId);

                String fileName = "Recu_Transport_" + reservation.getIdReservation() + "_" + System.currentTimeMillis()
                        + ".pdf";
                String filePath = System.getProperty("user.home") + File.separator + fileName;

                org.example.entities.User entUser = new org.example.entities.User();
                if (currentUser != null) {
                    entUser.setFull_name(currentUser.getUsername());
                    entUser.setEmail(currentUser.getEmail());
                }

                PDFService.generatePaymentReceipt(p, entUser, filePath);

                File pdfFile = new File(filePath);
                if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Paiement Réussi ✅");
            success.setHeaderText("Paiement confirmé !");
            success.setContentText(String.format(
                    "Montant payé : %.2f DT\nMéthode : %s\nVéhicule : %s - %s\nVotre reçu PDF a été généré.",
                    price, method, reservation.getTransportCompagnie(), reservation.getTransportType()));
            success.showAndWait();

            goBack();
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Erreur de paiement");
            error.setContentText("Le paiement n'a pas pu être enregistré. Détails dans la console.");
            error.showAndWait();
        }
    }

    @FXML
    private void goBack() {
        try {
            javafx.scene.Scene scene = payButton.getScene();

            javafx.scene.layout.Pane contentArea = (javafx.scene.layout.Pane) scene.lookup("#contentArea");
            if (contentArea == null)
                contentArea = (javafx.scene.layout.Pane) scene.lookup("#contentContainer");

            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_reservations.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
                MyReservationsController ctrl = loader.getController();
                ctrl.setCurrentUser(currentUser);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MainShellController shell = MainShellController.getInstance();
        if (shell != null) {
            shell.loadView("/fxml/my_reservations.fxml");
        }
    }
}
