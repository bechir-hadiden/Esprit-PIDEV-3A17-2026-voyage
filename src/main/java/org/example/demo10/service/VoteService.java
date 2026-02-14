package org.example.demo10.service;

import org.example.demo10.dao.VoteDAO;
import org.example.demo10.model.Vote;

public class VoteService {
    private VoteDAO voteDAO;

    public VoteService() {
        this.voteDAO = new VoteDAO();
    }

    /**
     * L'utilisateur vote pour un avis
     */
    public boolean voter(int avisId, int userId, boolean estUtile) {
        Vote.TypeVote type = estUtile ? Vote.TypeVote.UTILE : Vote.TypeVote.PAS_UTILE;
        return voteDAO.voter(avisId, userId, type);
    }

    /**
     * Récupère le vote de l'utilisateur pour un avis
     */
    public Vote getVoteUtilisateur(int avisId, int userId) {
        return voteDAO.getVote(avisId, userId);
    }

    /**
     * Calcule le score de pertinence d'un avis
     */
    public int calculerScorePertinence(int avisId) {
        int[] votes = voteDAO.compterVotes(avisId);
        return (votes[0] * 2) - votes[1];
    }

    /**
     * Récupère les statistiques de votes pour un avis
     */
    public String getStatistiquesVotes(int avisId) {
        int[] votes = voteDAO.compterVotes(avisId);
        int total = votes[0] + votes[1];

        if (total == 0) {
            return "Soyez le premier à voter pour cet avis !";
        }

        double pourcentageUtile = (votes[0] * 100.0) / total;

        return String.format(
                "👍 %d personnes ont trouvé cet avis utile (%.0f%%) | 👎 %d personnes non",
                votes[0], pourcentageUtile, votes[1]
        );
    }

    /**
     * Annule le vote d'un utilisateur
     */
    public boolean annulerVote(int avisId, int userId) {
        Vote vote = voteDAO.getVote(avisId, userId);
        if (vote != null) {
            return voteDAO.supprimerVote(vote.getId());
        }
        return false;
    }
}