package org.example.demo10.model;

import java.time.LocalDateTime;

public class Vote {
    private int id;
    private int avisId;
    private int userId;
    private TypeVote type;
    private LocalDateTime dateVote;

    public enum TypeVote {
        UTILE("👍 Utile"),
        PAS_UTILE("👎 Pas utile");

        private String label;

        TypeVote(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public Vote() {}

    public Vote(int avisId, int userId, TypeVote type) {
        this.avisId = avisId;
        this.userId = userId;
        this.type = type;
        this.dateVote = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAvisId() { return avisId; }
    public void setAvisId(int avisId) { this.avisId = avisId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public TypeVote getType() { return type; }
    public void setType(TypeVote type) { this.type = type; }

    public LocalDateTime getDateVote() { return dateVote; }
    public void setDateVote(LocalDateTime dateVote) { this.dateVote = dateVote; }
}