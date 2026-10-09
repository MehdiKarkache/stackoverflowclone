package com.stackclone.service;

import com.stackclone.dto.PostDtos.AnswerDto;
import com.stackclone.dto.PostDtos.AnswerRequest;
import com.stackclone.model.Answer;
import com.stackclone.model.Question;
import com.stackclone.model.QuestionStatus;
import com.stackclone.model.User;
import com.stackclone.repository.AnswerRepository;
import com.stackclone.repository.VoteRepository;
import com.stackclone.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AnswerService {

    private final AnswerRepository answers;
    private final VoteRepository votes;
    private final Lookups lookups;
    private final PostMapper mapper;

    public AnswerService(AnswerRepository answers, VoteRepository votes, Lookups lookups, PostMapper mapper) {
        this.answers = answers;
        this.votes = votes;
        this.lookups = lookups;
        this.mapper = mapper;
    }

    @Transactional
    public AnswerDto create(Long questionId, AnswerRequest req) {
        Question question = lookups.question(questionId);
        if (question.getStatus() != QuestionStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette question est fermée");
        }
        Answer answer = answers.save(new Answer(lookups.currentUser(), question, req.body()));
        question.addAnswer(answer);
        return mapper.toDto(answer);
    }

    @Transactional
    public AnswerDto update(Long id, AnswerRequest req) {
        Answer answer = lookups.answer(id);
        Permissions.requireOwnerOrModerator(answer.getAuthor());
        answer.edit(req.body());
        return mapper.toDto(answer);
    }

    @Transactional
    public void delete(Long id) {
        Answer answer = lookups.answer(id);
        Permissions.requireOwnerOrModerator(answer.getAuthor());
        if (answer.isAccepted()) {
            revokeAcceptReputation(answer);
        }
        votes.deleteByPostIds(List.of(answer.getId()));
        answer.getQuestion().removeAnswer(answer);
    }

    /**
     * Seul l'auteur de la question peut accepter une réponse.
     * Rappeler sur la réponse déjà acceptée retire l'acceptation.
     */
    @Transactional
    public AnswerDto toggleAccept(Long id) {
        Answer answer = lookups.answer(id);
        Question question = answer.getQuestion();
        if (!question.getAuthor().getId().equals(CurrentUser.requireId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l'auteur de la question peut accepter une réponse");
        }

        Answer previous = question.getAcceptedAnswer();
        if (previous != null) {
            revokeAcceptReputation(previous);
        }
        if (previous == answer) {
            question.acceptAnswer(null);
        } else {
            question.acceptAnswer(answer);
            grantAcceptReputation(answer);
        }
        return mapper.toDto(answer);
    }

    private static void grantAcceptReputation(Answer answer) {
        applyAcceptReputation(answer, 1);
    }

    private static void revokeAcceptReputation(Answer answer) {
        applyAcceptReputation(answer, -1);
    }

    private static void applyAcceptReputation(Answer answer, int sign) {
        User answerer = answer.getAuthor();
        User asker = answer.getQuestion().getAuthor();
        // Pas de réputation quand on accepte sa propre réponse
        if (!answerer.getId().equals(asker.getId())) {
            answerer.addReputation(sign * ReputationRules.ANSWER_ACCEPTED);
            asker.addReputation(sign * ReputationRules.ACCEPTING_ANSWER);
        }
    }
}
