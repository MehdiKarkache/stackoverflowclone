package com.stackclone.service;

import com.stackclone.model.Answer;
import com.stackclone.model.Post;
import com.stackclone.model.User;
import com.stackclone.model.Vote;
import com.stackclone.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VoteService {

    public record VoteResult(Long postId, int score, int myVote) {
    }

    private final VoteRepository votes;
    private final Lookups lookups;
    private final int upvoteMinReputation;
    private final int downvoteMinReputation;

    public VoteService(VoteRepository votes, Lookups lookups,
                       @Value("${app.privileges.upvote:15}") int upvoteMinReputation,
                       @Value("${app.privileges.downvote:125}") int downvoteMinReputation) {
        this.votes = votes;
        this.lookups = lookups;
        this.upvoteMinReputation = upvoteMinReputation;
        this.downvoteMinReputation = downvoteMinReputation;
    }

    /**
     * Enregistre le vote de l'utilisateur courant : +1, -1, ou 0 pour annuler.
     * Le score du post et la réputation sont ajustés de la différence avec le vote précédent.
     */
    @Transactional
    public VoteResult vote(Long postId, int value) {
        if (value < -1 || value > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le vote doit valoir -1, 0 ou 1");
        }
        User voter = lookups.currentUser();
        Post post = lookups.post(postId);
        if (post.getAuthor().getId().equals(voter.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez pas voter pour votre propre post");
        }

        Vote existing = votes.findByUserIdAndPostId(voter.getId(), postId).orElse(null);
        int previous = existing != null ? existing.getValue() : 0;
        if (previous == value) {
            return new VoteResult(postId, post.getScore(), value);
        }
        checkPrivilege(voter, value);

        post.addScore(value - previous);
        post.getAuthor().addReputation(authorReputation(value) - authorReputation(previous));
        if (post instanceof Answer) {
            voter.addReputation(voterReputation(value) - voterReputation(previous));
        }

        if (value == 0) {
            votes.delete(existing);
        } else if (existing != null) {
            existing.setValue(value);
        } else {
            votes.save(new Vote(voter, post, value));
        }
        return new VoteResult(postId, post.getScore(), value);
    }

    private void checkPrivilege(User voter, int value) {
        if (value == 1 && voter.getReputation() < upvoteMinReputation) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Il faut " + upvoteMinReputation + " points de réputation pour voter pour");
        }
        if (value == -1 && voter.getReputation() < downvoteMinReputation) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Il faut " + downvoteMinReputation + " points de réputation pour voter contre");
        }
    }

    private static int authorReputation(int vote) {
        return switch (vote) {
            case 1 -> ReputationRules.UPVOTE_RECEIVED;
            case -1 -> ReputationRules.DOWNVOTE_RECEIVED;
            default -> 0;
        };
    }

    private static int voterReputation(int vote) {
        return vote == -1 ? ReputationRules.DOWNVOTE_ANSWER_COST : 0;
    }
}
