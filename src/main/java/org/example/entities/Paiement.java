package org.example.entities;

import java.sql.Date;

public class Paiement {
    private int idPaiement;
    private double montant;
    private Date datePaiement;
    private String statut_paiement;
    private String methodePaiement;

    private String stripeSessionId;
    private int userId;
    private Integer bookingId; // Link to hotel booking
    private Integer planId;    // Link to subscription plan

    // Billing Details
    private String nomFacturation;
    private String prenomFacturation;
    private String emailFacturation;
    private String telephoneFacturation;
    private String description;

    public Paiement() {
    }

    public Paiement(int idPaiement, double montant, Date datePaiement, String statut_paiement, String methodePaiement, String stripeSessionId) {
        this.idPaiement = idPaiement;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statut_paiement = statut_paiement;
        this.methodePaiement = methodePaiement;
        this.stripeSessionId = stripeSessionId;
    }

    public Paiement(double montant, Date datePaiement, String statut_paiement, String methodePaiement, String stripeSessionId) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statut_paiement = statut_paiement;
        this.methodePaiement = methodePaiement;
        this.stripeSessionId = stripeSessionId;
    }

    public Paiement(double montant, Date datePaiement, String statut_paiement, String methodePaiement) {
        this(montant, datePaiement, statut_paiement, methodePaiement, null);
    }

    public Paiement(double montant, Date datePaiement, String statut_paiement) {
        this(montant, datePaiement, statut_paiement, "Carte Bancaire", null);
    }

    public int getIdPaiement() {
        return idPaiement;
    }

    public void setIdPaiement(int idPaiement) {
        this.idPaiement = idPaiement;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Date getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(Date datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getStatut_paiement() {
        return statut_paiement;
    }

    public void setStatut_paiement(String statut_paiement) {
        this.statut_paiement = statut_paiement;
    }

    // Compatibility aliases for UI and other controllers
    public String getStatutPaiement() {
        return statut_paiement;
    }

    public void setStatutPaiement(String statutPaiement) {
        this.statut_paiement = statutPaiement;
    }

    public String getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(String methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public String getFormattedAmount(boolean isEuro) {
        if (isEuro) {
            double amountInEuro = montant / 3.4;
            return String.format("%.2f €", amountInEuro);
        }
        return String.format("%.2f DT", montant);
    }

    public String getStripeSessionId() {
        return stripeSessionId;
    }

    public void setStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public String getNomFacturation() { return nomFacturation; }
    public void setNomFacturation(String nomFacturation) { this.nomFacturation = nomFacturation; }

    public String getPrenomFacturation() { return prenomFacturation; }
    public void setPrenomFacturation(String prenomFacturation) { this.prenomFacturation = prenomFacturation; }

    public String getEmailFacturation() { return emailFacturation; }
    public void setEmailFacturation(String emailFacturation) { this.emailFacturation = emailFacturation; }

    public String getTelephoneFacturation() { return telephoneFacturation; }
    public void setTelephoneFacturation(String telephoneFacturation) { this.telephoneFacturation = telephoneFacturation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Compatibility getters/setters
    @Override
    public String toString() {
        return "Paiement{" +
                "idPaiement=" + idPaiement +
                ", montant=" + montant +
                ", datePaiement=" + datePaiement +
                ", statut_paiement='" + statut_paiement + '\'' +
                ", methodePaiement='" + methodePaiement + '\'' +
                ", stripeSessionId='" + stripeSessionId + '\'' +
                '}';
    }
}
