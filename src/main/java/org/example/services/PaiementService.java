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

    public PaiementService() {
        // Run schema migration on first use
        try (Connection c = getConn()) {
            if (c != null)
                ensureSchema(c);
        } catch (Exception e) {
            System.err.println("PaiementService init error: " + e.getMessage());
        }
    }

    /**
     * Always returns a fresh connection to avoid stale/closed connection issues.
     */
    private Connection getConn() {
        Connection c = DatabaseConnection.getConnection();
        if (c == null) {
            System.err.println("PaiementService: DB connection is null.");
        }
        return c;
    }

    public boolean ajouter(Paiement p) {
        String req = "INSERT INTO paiements (montant, date_paiement, statut_paiement, methode_paiement, stripe_session_id, user_id, booking_id, plan_id, nom_facturation, prenom_facturation, email_facturation, telephone_facturation, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn()) {
            if (conn == null)
                return false;
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setDouble(1, p.getMontant());
                ps.setDate(2, p.getDatePaiement());
                ps.setString(3, p.getStatut_paiement() != null ? p.getStatut_paiement() : "En attente");
                ps.setString(4, p.getMethodePaiement());
                ps.setString(5, p.getStripeSessionId());
                ps.setInt(6, p.getUserId());
                
                if (p.getBookingId() != null) ps.setInt(7, p.getBookingId());
                else ps.setNull(7, Types.INTEGER);
                
                if (p.getPlanId() != null) ps.setInt(8, p.getPlanId());
                else ps.setNull(8, Types.INTEGER);

                ps.setString(9, p.getNomFacturation());
                ps.setString(10, p.getPrenomFacturation());
                ps.setString(11, p.getEmailFacturation());
                ps.setString(12, p.getTelephoneFacturation());
                ps.setString(13, p.getDescription());

                int rows = ps.executeUpdate();
                System.out.println("Payment inserted: " + rows + " row(s).");
                return rows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Payment insert error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void supprimer(int id) {
        String req = "DELETE FROM paiements WHERE id_paiement = ?";
        try (Connection conn = getConn()) {
            if (conn == null)
                return;
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                System.out.println("Payment deleted successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Payment delete error: " + e.getMessage());
        }
    }

    public void modifier(Paiement p) {
        String req = "UPDATE paiements SET montant = ?, date_paiement = ?, statut_paiement = ?, methode_paiement = ?, stripe_session_id = ?, user_id = ?, booking_id = ?, plan_id = ?, nom_facturation = ?, prenom_facturation = ?, email_facturation = ?, telephone_facturation = ?, description = ? WHERE id_paiement = ?";
        try (Connection conn = getConn()) {
            if (conn == null)
                return;
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
                
                if (p.getBookingId() != null) ps.setInt(7, p.getBookingId());
                else ps.setNull(7, Types.INTEGER);

                if (p.getPlanId() != null) ps.setInt(8, p.getPlanId());
                else ps.setNull(8, Types.INTEGER);

                ps.setString(9, p.getNomFacturation());
                ps.setString(10, p.getPrenomFacturation());
                ps.setString(11, p.getEmailFacturation());
                ps.setString(12, p.getTelephoneFacturation());
                ps.setString(13, p.getDescription());
                
                ps.setInt(14, p.getIdPaiement());
                ps.executeUpdate();
            }

            if ("Effectué".equalsIgnoreCase(p.getStatut_paiement())
                    && !"Effectué".equalsIgnoreCase(oldStatus)) {
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

    public Paiement getPaiementByBookingId(int bookingId) {
        String req = "SELECT * FROM paiements WHERE booking_id = ? LIMIT 1";
        try (Connection conn = getConn()) {
            if (conn == null)
                return null;
            try (PreparedStatement ps = conn.prepareStatement(req)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Paiement p = new Paiement();
                        p.setIdPaiement(rs.getInt("id_paiement"));
                        p.setMontant(rs.getDouble("montant"));
                        p.setDatePaiement(rs.getDate("date_paiement"));
                        p.setStatut_paiement(rs.getString("statut_paiement"));
                        p.setMethodePaiement(rs.getString("methode_paiement"));
                        p.setUserId(rs.getInt("user_id"));
                        p.setBookingId(rs.getInt("booking_id"));
                        return p;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payment for booking: " + e.getMessage());
        }
        return null;
    }

    public List<Paiement> afficher() {
        List<Paiement> paiements = new ArrayList<>();
        String req = "SELECT * FROM paiements";
        try (Connection conn = getConn()) {
            if (conn == null)
                return paiements;
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
                    if (!rs.wasNull()) p.setBookingId(bookingId);
                    
                    int planId = rs.getInt("plan_id");
                    if (!rs.wasNull()) p.setPlanId(planId);

                    p.setNomFacturation(rs.getString("nom_facturation"));
                    p.setPrenomFacturation(rs.getString("prenom_facturation"));
                    p.setEmailFacturation(rs.getString("email_facturation"));
                    p.setTelephoneFacturation(rs.getString("telephone_facturation"));
                    p.setDescription(rs.getString("description"));

                    paiements.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Payment fetch error: " + e.getMessage());
        }
        return paiements;
    }

    private void ensureSchema(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS paiements (" +
                    "id_paiement INT PRIMARY KEY AUTO_INCREMENT," +
                    "montant DECIMAL(10,2) NOT NULL," +
                    "date_paiement DATE," +
                    "statut_paiement VARCHAR(50) DEFAULT 'En attente'," +
                    "methode_paiement VARCHAR(100)," +
                    "stripe_session_id VARCHAR(255)," +
                    "user_id INT," +
                    "booking_id INT," +
                    "plan_id INT," +
                    "nom_facturation VARCHAR(100)," +
                    "prenom_facturation VARCHAR(100)," +
                    "email_facturation VARCHAR(150)," +
                    "telephone_facturation VARCHAR(20)," +
                    "description TEXT" +
                    ")");
        } catch (SQLException e) {
            System.err.println("Payment table creation error: " + e.getMessage());
        }

        addColumnIfMissing(conn, "methode_paiement", "ALTER TABLE paiements ADD COLUMN methode_paiement VARCHAR(100)");
        addColumnIfMissing(conn, "stripe_session_id",
                "ALTER TABLE paiements ADD COLUMN stripe_session_id VARCHAR(255)");
        addColumnIfMissing(conn, "user_id", "ALTER TABLE paiements ADD COLUMN user_id INT");
        addColumnIfMissing(conn, "booking_id", "ALTER TABLE paiements ADD COLUMN booking_id INT");
        addColumnIfMissing(conn, "plan_id", "ALTER TABLE paiements ADD COLUMN plan_id INT");
        addColumnIfMissing(conn, "nom_facturation", "ALTER TABLE paiements ADD COLUMN nom_facturation VARCHAR(100)");
        addColumnIfMissing(conn, "prenom_facturation", "ALTER TABLE paiements ADD COLUMN prenom_facturation VARCHAR(100)");
        addColumnIfMissing(conn, "email_facturation", "ALTER TABLE paiements ADD COLUMN email_facturation VARCHAR(150)");
        addColumnIfMissing(conn, "telephone_facturation", "ALTER TABLE paiements ADD COLUMN telephone_facturation VARCHAR(20)");
        addColumnIfMissing(conn, "description", "ALTER TABLE paiements ADD COLUMN description TEXT");
    }

    private void addColumnIfMissing(Connection conn, String columnName, String alterSql) {
        try {
            if (!hasColumn(conn, columnName)) {
                try (Statement st = conn.createStatement()) {
                    st.execute(alterSql);
                }
            }
        } catch (SQLException e) {
            System.err.println("Payment schema warning: " + e.getMessage());
        }
    }

    private boolean hasColumn(Connection conn, String columnName) throws SQLException {
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
