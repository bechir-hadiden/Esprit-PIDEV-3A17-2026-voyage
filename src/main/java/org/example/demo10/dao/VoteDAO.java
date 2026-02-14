package org.example.demo10.dao;

import org.example.demo10.DBConnection;
import org.example.demo10.model.Vote;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoteDAO {

    // Ajouter ou modifier un vote
    public boolean voter(int avisId, int userId, Vote.TypeVote type) {
        // Vérifier si l'utilisateur a déjà voté
        Vote voteExistant = getVote(avisId, userId);

        if (voteExistant != null) {
            // Mettre à jour le vote existant
            return updateVote(voteExistant.getId(), type);
        } else {
            // Créer un nouveau vote
            return insertVote(avisId, userId, type);
        }
    }

    private boolean insertVote(int avisId, int userId, Vote.TypeVote type) {
        String query = "INSERT INTO avis_votes (avis_id, user_id, vote_type) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, avisId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, type.name());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateVote(int voteId, Vote.TypeVote type) {
        String query = "UPDATE avis_votes SET vote_type = ?, date_vote = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, type.name());
            pstmt.setInt(2, voteId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Récupérer le vote d'un utilisateur pour un avis
    public Vote getVote(int avisId, int userId) {
        String query = "SELECT * FROM avis_votes WHERE avis_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, avisId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractVoteFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Compter les votes pour un avis
    public int[] compterVotes(int avisId) {
        String query = "SELECT vote_type, COUNT(*) as count FROM avis_votes WHERE avis_id = ? GROUP BY vote_type";
        int[] resultats = new int[2]; // [0] = utile, [1] = pas utile

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, avisId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("vote_type");
                int count = rs.getInt("count");

                if ("UTILE".equals(type)) {
                    resultats[0] = count;
                } else if ("PAS_UTILE".equals(type)) {
                    resultats[1] = count;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultats;
    }

    // Récupérer tous les votes pour un avis
    public List<Vote> getVotesByAvis(int avisId) {
        List<Vote> votes = new ArrayList<>();
        String query = "SELECT * FROM avis_votes WHERE avis_id = ? ORDER BY date_vote DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, avisId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                votes.add(extractVoteFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return votes;
    }

    // Supprimer un vote
    public boolean supprimerVote(int voteId) {
        String query = "DELETE FROM avis_votes WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, voteId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Vote extractVoteFromResultSet(ResultSet rs) throws SQLException {
        Vote vote = new Vote();
        vote.setId(rs.getInt("id"));
        vote.setAvisId(rs.getInt("avis_id"));
        vote.setUserId(rs.getInt("user_id"));
        vote.setType(Vote.TypeVote.valueOf(rs.getString("vote_type")));
        if (rs.getTimestamp("date_vote") != null) {
            vote.setDateVote(rs.getTimestamp("date_vote").toLocalDateTime());
        }
        return vote;
    }
}