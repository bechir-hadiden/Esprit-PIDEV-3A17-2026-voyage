package org.example.services;

import org.example.entities.Paiement;
import org.example.entities.User;
import org.example.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PaiementService {

    private Connection conn;

    public PaiementService() {
        try {
            this.conn = DatabaseConnection.getConnection();
            if (this.conn == null) {
                throw new SQLException("Database connection failed.");
            }
            ensureSchema();
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public boolean ajouter(Paiement p) {
        if (conn == null) {
            System.err.println("Payment insert skipped: DB connection is null.");
            return false;
        }

        String req = "INSERT INTO paiements (montant, date_paiement, statut_paiement, methode_paiement, stripe_session_id, user_id, booking_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setDouble(1, p.getMontant());
            ps.setDate(2, p.getDatePaiement());
            ps.setString(3, p.getStatut_paiement() != null ? p.getStatut_paiement() : "En attente");
            ps.setString(4, p.getMethodePaiement());
            ps.setString(5, p.getStripeSessionId());
            ps.setInt(6, p.getUserId());
            if (p.getBookingId() != null) {
                ps.setInt(7, p.getBookingId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.executeUpdate();
            System.out.println("Payment inserted successfully.");
            return true;
        } catch (SQLException e) {
            System.err.println("Payment insert error: " + e.getMessage());
            return false;
        }
    }

    public void supprimer(int id) {
        String req = "DELETE FROM paiements WHERE id_paiement = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Payment deleted successfully.");
        } catch (SQLException e) {
            System.err.println("Payment delete error: " + e.getMessage());
        }
    }

    public void modifier(Paiement p) {
        String req = "UPDATE paiements SET montant = ?, date_paiement = ?, statut_paiement = ?, methode_paiement = ?, stripe_session_id = ?, user_id = ?, booking_id = ? WHERE id_paiement = ?";
        try {
            String oldStatus = "";
            String checkStatusReq = "SELECT statut_paiement FROM paiements WHERE id_paiement = ?";
            try (PreparedStatement checkSt = conn.prepareStatement(checkStatusReq)) {
                checkSt.setInt(1, p.getIdPaiement());
                ResultSet rsStatus = checkSt.executeQuery();
                if (rsStatus.next()) {
                    oldStatus = rsStatus.getString(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setDouble(1, p.getMontant());
                ps.setDate(2, p.getDatePaiement());
                ps.setString(3, p.getStatut_paiement());
                ps.setString(4, p.getMethodePaiement());
                ps.setString(5, p.getStripeSessionId());
                ps.setInt(6, p.getUserId());
                if (p.getBookingId() != null) {
                    ps.setInt(7, p.getBookingId());
                } else {
                    ps.setNull(7, Types.INTEGER);
                }
                ps.setInt(8, p.getIdPaiement());
                ps.executeUpdate();
            }

            if ("Effectu\u00E9".equalsIgnoreCase(p.getStatut_paiement()) && !"Effectu\u00E9".equalsIgnoreCase(oldStatus)) {
                double cashback = p.getMontant() * 0.05;
                new UserService().updateBalance(p.getUserId(), cashback);

                User user = new UserService().getUserById(p.getUserId());
                if (user != null && user.getEmail() != null) {
                    EmailService.sendPaymentConfirmation(user.getEmail(), user.getName(), p.getMontant(), "DT");
                }
            }

            System.out.println("Payment updated successfully.");
        } catch (SQLException e) {
            System.err.println("Payment update error: " + e.getMessage());
        }
    }

    public List<Paiement> afficher() {
        List<Paiement> paiements = new ArrayList<>();
        if (conn == null) {
            System.err.println("Payment fetch skipped: DB connection is null.");
            return paiements;
        }
        String req = "SELECT * FROM paiements";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Paiement p = new Paiement();
                p.setIdPaiement(rs.getInt("id_paiement"));
                p.setMontant(rs.getDouble("montant"));
                p.setDatePaiement(rs.getDate("date_paiement"));
                p.setStatut_paiement(rs.getString("statut_paiement"));
                p.setMethodePaiement(rs.getString("methode_paiement"));
                p.setStripeSessionId(rs.getString("stripe_session_id"));
                p.setUserId(rs.getInt("user_id"));
                int bookingId = rs.getInt("booking_id");
                if (!rs.wasNull()) {
                    p.setBookingId(bookingId);
                }
                paiements.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Payment fetch error: " + e.getMessage());
        }
        return paiements;
    }

    private void ensureSchema() {
        if (conn == null) {
            return;
        }

        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS paiements (" +
                    "id_paiement INT PRIMARY KEY AUTO_INCREMENT," +
                    "montant DECIMAL(10,2) NOT NULL," +
                    "date_paiement DATE," +
                    "statut_paiement VARCHAR(50) DEFAULT 'En attente'," +
                    "methode_paiement VARCHAR(100)," +
                    "stripe_session_id VARCHAR(255)," +
                    "user_id INT," +
                    "booking_id INT" +
                    ")");
        } catch (SQLException e) {
            System.err.println("Payment table creation error: " + e.getMessage());
        }

        addColumnIfMissing("methode_paiement", "ALTER TABLE paiements ADD COLUMN methode_paiement VARCHAR(100)");
        addColumnIfMissing("stripe_session_id", "ALTER TABLE paiements ADD COLUMN stripe_session_id VARCHAR(255)");
        addColumnIfMissing("user_id", "ALTER TABLE paiements ADD COLUMN user_id INT");
        addColumnIfMissing("booking_id", "ALTER TABLE paiements ADD COLUMN booking_id INT");
    }

    private void addColumnIfMissing(String columnName, String alterSql) {
        if (conn == null) {
            return;
        }
        try {
            if (!hasColumn(columnName)) {
                try (Statement st = conn.createStatement()) {
                    st.execute(alterSql);
                }
            }
        } catch (SQLException e) {
            System.err.println("Payment schema warning: " + e.getMessage());
        }
    }

    private boolean hasColumn(String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, "paiements", columnName)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, "PAIEMENTS", columnName.toUpperCase())) {
            return rs.next();
        }
    }
}
