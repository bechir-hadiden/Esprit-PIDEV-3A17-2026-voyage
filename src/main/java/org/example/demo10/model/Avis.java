package org.example.demo10.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Avis {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty nomClient = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();
    private IntegerProperty note = new SimpleIntegerProperty();
    private StringProperty commentaire = new SimpleStringProperty();
    private ObjectProperty<LocalDate> dateAvis = new SimpleObjectProperty<>();
    private IntegerProperty voyageId = new SimpleIntegerProperty();

    // NOUVEAUX ATTRIBUTS POUR LES VOTES
    private IntegerProperty votesUtiles = new SimpleIntegerProperty(0);
    private IntegerProperty votesPasUtiles = new SimpleIntegerProperty(0);
    private IntegerProperty scorePertinence = new SimpleIntegerProperty(0);

    // Constructeur par défaut
    public Avis() {}

    // Constructeur avec paramètres (mis à jour)
    public Avis(int id, String nomClient, String email, int note, String commentaire,
                LocalDate dateAvis, int voyageId) {
        this.id.set(id);
        this.nomClient.set(nomClient);
        this.email.set(email);
        this.note.set(note);
        this.commentaire.set(commentaire);
        this.dateAvis.set(dateAvis);
        this.voyageId.set(voyageId);
        // Les votes sont initialisés à 0 par défaut
    }

    // Getters et Setters pour id
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Getters et Setters pour nomClient
    public String getNomClient() {
        return nomClient.get();
    }

    public void setNomClient(String nomClient) {
        this.nomClient.set(nomClient);
    }

    public StringProperty nomClientProperty() {
        return nomClient;
    }

    // Getters et Setters pour email
    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    // Getters et Setters pour note
    public int getNote() {
        return note.get();
    }

    public void setNote(int note) {
        this.note.set(note);
    }

    public IntegerProperty noteProperty() {
        return note;
    }

    // Getters et Setters pour commentaire
    public String getCommentaire() {
        return commentaire.get();
    }

    public void setCommentaire(String commentaire) {
        this.commentaire.set(commentaire);
    }

    public StringProperty commentaireProperty() {
        return commentaire;
    }

    // Getters et Setters pour dateAvis
    public LocalDate getDateAvis() {
        return dateAvis.get();
    }

    public void setDateAvis(LocalDate dateAvis) {
        this.dateAvis.set(dateAvis);
    }

    public ObjectProperty<LocalDate> dateAvisProperty() {
        return dateAvis;
    }

    // Getters et Setters pour voyageId
    public int getVoyageId() {
        return voyageId.get();
    }

    public void setVoyageId(int voyageId) {
        this.voyageId.set(voyageId);
    }

    public IntegerProperty voyageIdProperty() {
        return voyageId;
    }

    // ========== NOUVELLES MÉTHODES POUR LES VOTES ==========

    // Votes utiles
    public int getVotesUtiles() {
        return votesUtiles.get();
    }

    public void setVotesUtiles(int votesUtiles) {
        this.votesUtiles.set(votesUtiles);
    }

    public IntegerProperty votesUtilesProperty() {
        return votesUtiles;
    }

    // Votes pas utiles
    public int getVotesPasUtiles() {
        return votesPasUtiles.get();
    }

    public void setVotesPasUtiles(int votesPasUtiles) {
        this.votesPasUtiles.set(votesPasUtiles);
    }

    public IntegerProperty votesPasUtilesProperty() {
        return votesPasUtiles;
    }

    // Score de pertinence
    public int getScorePertinence() {
        return scorePertinence.get();
    }

    public void setScorePertinence(int scorePertinence) {
        this.scorePertinence.set(scorePertinence);
    }

    public IntegerProperty scorePertinenceProperty() {
        return scorePertinence;
    }

    // Méthodes utilitaires pour les votes
    public int getTotalVotes() {
        return getVotesUtiles() + getVotesPasUtiles();
    }

    public double getPourcentageUtile() {
        int total = getTotalVotes();
        if (total == 0) return 0;
        return (getVotesUtiles() * 100.0) / total;
    }

    public String getRatioUtileFormate() {
        int total = getTotalVotes();
        if (total == 0) return "Aucun vote";
        return String.format("%.0f%% utile (%d/%d)",
                getPourcentageUtile(), getVotesUtiles(), total);
    }

    public String getStatistiquesVotes() {
        int total = getTotalVotes();
        if (total == 0) {
            return "Soyez le premier à voter !";
        }
        return String.format("👍 %d | 👎 %d | Score: %d",
                getVotesUtiles(), getVotesPasUtiles(), getScorePertinence());
    }

    // Méthode pour calculer le score (utile pour le tri)
    public void calculerScore() {
        setScorePertinence((getVotesUtiles() * 2) - getVotesPasUtiles());
    }

    // Méthode toString mise à jour
    @Override
    public String toString() {
        return "Avis{" +
                "id=" + id.get() +
                ", nomClient='" + nomClient.get() + '\'' +
                ", email='" + email.get() + '\'' +
                ", note=" + note.get() +
                ", commentaire='" + commentaire.get() + '\'' +
                ", dateAvis=" + dateAvis.get() +
                ", voyageId=" + voyageId.get() +
                ", votesUtiles=" + votesUtiles.get() +
                ", votesPasUtiles=" + votesPasUtiles.get() +
                ", scorePertinence=" + scorePertinence.get() +
                '}';
    }
}