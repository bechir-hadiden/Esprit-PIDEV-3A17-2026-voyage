package org.example.demo10.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Reservation {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty nomClient = new SimpleStringProperty();
    private StringProperty emailClient = new SimpleStringProperty();
    private StringProperty telephone = new SimpleStringProperty();
    private IntegerProperty voyageId = new SimpleIntegerProperty();
    private ObjectProperty<LocalDate> dateReservation = new SimpleObjectProperty<>();
    private IntegerProperty nombrePersonnes = new SimpleIntegerProperty();
    private StringProperty statut = new SimpleStringProperty();
    private StringProperty commentaire = new SimpleStringProperty();

    // Constructeurs
    public Reservation() {}

    public Reservation(int id, String nomClient, String emailClient, String telephone,
                       int voyageId, LocalDate dateReservation, int nombrePersonnes,
                       String statut, String commentaire) {
        this.id.set(id);
        this.nomClient.set(nomClient);
        this.emailClient.set(emailClient);
        this.telephone.set(telephone);
        this.voyageId.set(voyageId);
        this.dateReservation.set(dateReservation);
        this.nombrePersonnes.set(nombrePersonnes);
        this.statut.set(statut);
        this.commentaire.set(commentaire);
    }

    // Getters et Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getNomClient() { return nomClient.get(); }
    public void setNomClient(String nomClient) { this.nomClient.set(nomClient); }
    public StringProperty nomClientProperty() { return nomClient; }

    public String getEmailClient() { return emailClient.get(); }
    public void setEmailClient(String emailClient) { this.emailClient.set(emailClient); }
    public StringProperty emailClientProperty() { return emailClient; }

    public String getTelephone() { return telephone.get(); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public StringProperty telephoneProperty() { return telephone; }

    public int getVoyageId() { return voyageId.get(); }
    public void setVoyageId(int voyageId) { this.voyageId.set(voyageId); }
    public IntegerProperty voyageIdProperty() { return voyageId; }

    public LocalDate getDateReservation() { return dateReservation.get(); }
    public void setDateReservation(LocalDate dateReservation) { this.dateReservation.set(dateReservation); }
    public ObjectProperty<LocalDate> dateReservationProperty() { return dateReservation; }

    public int getNombrePersonnes() { return nombrePersonnes.get(); }
    public void setNombrePersonnes(int nombrePersonnes) { this.nombrePersonnes.set(nombrePersonnes); }
    public IntegerProperty nombrePersonnesProperty() { return nombrePersonnes; }

    public String getStatut() { return statut.get(); }
    public void setStatut(String statut) { this.statut.set(statut); }
    public StringProperty statutProperty() { return statut; }

    public String getCommentaire() { return commentaire.get(); }
    public void setCommentaire(String commentaire) { this.commentaire.set(commentaire); }
    public StringProperty commentaireProperty() { return commentaire; }

    // Méthodes utilitaires
    public String getStatutAvecCouleur() {
        switch(statut.get()) {
            case "confirmée": return "✅ Confirmée";
            case "annulée": return "❌ Annulée";
            default: return "⏳ En attente";
        }
    }

    @Override
    public String toString() {
        return "Réservation #" + id.get() + " - " + nomClient.get() + " (" + statut.get() + ")";
    }
}