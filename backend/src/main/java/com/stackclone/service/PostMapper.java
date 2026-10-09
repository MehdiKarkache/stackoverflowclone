package com.stackclone.service;

import com.stackclone.dto.PostDtos.AnswerDto;
import com.stackclone.dto.PostDtos.CommentDto;
import com.stackclone.dto.PostDtos.QuestionDetail;
import com.stackclone.dto.PostDtos.QuestionSummary;
import com.stackclone.dto.UserDtos.UserSummary;
import com.stackclone.model.*;
import com.stackclone.repository.VoteRepository;
import com.stackclone.security.CurrentUser;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PostMapper {

    private static final int EXCERPT_LENGTH = 200;

    private final VoteRepository votes;

    public PostMapper(VoteRepository votes) {
        this.votes = votes;
    }

    public QuestionSummary toSummary(Question q) {
        return new QuestionSummary(q.getId(), q.getTitle(), excerpt(q.getBody()), q.getScore(), q.getAnswerCount(),
                q.getViewCount(), q.getAcceptedAnswer() != null, tagNames(q),
                UserSummary.from(q.getAuthor()), q.getCreatedAt());
    }

    public QuestionDetail toDetail(Question q) {
        List<Answer> answers = new ArrayList<>(q.getAnswers());
        // Réponse acceptée en premier, puis par score, puis la plus ancienne
        answers.sort(Comparator.comparing(Answer::isAccepted).reversed()
                .thenComparing(Comparator.comparingInt(Answer::getScore).reversed())
                .thenComparing(Answer::getCreatedAt));

        List<Long> postIds = new ArrayList<>();
        postIds.add(q.getId());
        answers.forEach(a -> postIds.add(a.getId()));
        Map<Long, Integer> myVotes = myVotes(postIds);

        return new QuestionDetail(q.getId(), q.getTitle(), q.getBody(), q.getScore(), q.getViewCount(),
                q.getStatus().name(), tagNames(q), UserSummary.from(q.getAuthor()), q.getCreatedAt(),
                q.getUpdatedAt(), myVotes.getOrDefault(q.getId(), 0),
                q.getAcceptedAnswer() != null ? q.getAcceptedAnswer().getId() : null,
                comments(q), answers.stream().map(a -> toDto(a, myVotes.getOrDefault(a.getId(), 0))).toList());
    }

    public AnswerDto toDto(Answer a) {
        return toDto(a, myVotes(List.of(a.getId())).getOrDefault(a.getId(), 0));
    }

    public CommentDto toDto(Comment c) {
        return new CommentDto(c.getId(), c.getPost().getId(), c.getBody(), UserSummary.from(c.getAuthor()),
                c.getCreatedAt());
    }

    private AnswerDto toDto(Answer a, int myVote) {
        return new AnswerDto(a.getId(), a.getQuestion().getId(), a.getBody(), a.getScore(), a.isAccepted(),
                UserSummary.from(a.getAuthor()), a.getCreatedAt(), a.getUpdatedAt(), myVote, comments(a));
    }

    private List<CommentDto> comments(Post p) {
        return p.getComments().stream().map(this::toDto).toList();
    }

    private Map<Long, Integer> myVotes(Collection<Long> postIds) {
        Optional<Long> userId = CurrentUser.id();
        if (userId.isEmpty() || postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> result = new HashMap<>();
        votes.findByUserIdAndPostIdIn(userId.get(), postIds)
                .forEach(v -> result.put(v.getPost().getId(), v.getValue()));
        return result;
    }

    private static List<String> tagNames(Question q) {
        return q.getTags().stream().map(Tag::getName).toList();
    }

    /** Extrait en texte brut : on retire la syntaxe Markdown la plus courante. */
    static String excerpt(String body) {
        String text = body.replaceAll("```[\\s\\S]*?```", " ")
                .replaceAll("[#*_`>\\[\\]()!]", "")
                .replaceAll("\\s+", " ")
                .trim();
        return text.length() <= EXCERPT_LENGTH ? text : text.substring(0, EXCERPT_LENGTH).trim() + "…";
    }
}
