package com.stackclone.service;

import com.stackclone.dto.PageResponse;
import com.stackclone.dto.PostDtos.QuestionDetail;
import com.stackclone.dto.PostDtos.QuestionRequest;
import com.stackclone.dto.PostDtos.QuestionSummary;
import com.stackclone.model.Question;
import com.stackclone.model.Tag;
import com.stackclone.model.User;
import com.stackclone.repository.QuestionRepository;
import com.stackclone.repository.TagRepository;
import com.stackclone.repository.VoteRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class QuestionService {

    public enum SortOrder { NEWEST, VOTES, UNANSWERED, VIEWS }

    private final QuestionRepository questions;
    private final TagRepository tags;
    private final VoteRepository votes;
    private final Lookups lookups;
    private final PostMapper mapper;

    public QuestionService(QuestionRepository questions, TagRepository tags, VoteRepository votes,
                           Lookups lookups, PostMapper mapper) {
        this.questions = questions;
        this.tags = tags;
        this.votes = votes;
        this.lookups = lookups;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionSummary> list(SortOrder sort, String tag, String search, Long authorId,
                                              int page, int size) {
        Sort order = switch (sort) {
            case VOTES -> Sort.by(Sort.Order.desc("score"), Sort.Order.desc("createdAt"));
            case VIEWS -> Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("createdAt"));
            case NEWEST, UNANSWERED -> Sort.by(Sort.Order.desc("createdAt"));
        };
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 50), order);
        Specification<Question> spec = filters(sort == SortOrder.UNANSWERED, tag, search, authorId);
        return PageResponse.of(questions.findAll(spec, pageable), mapper::toSummary);
    }

    /** Affiche une question et incrémente son compteur de vues. */
    @Transactional
    public QuestionDetail view(Long id) {
        Question q = lookups.question(id);
        q.incrementViews();
        return mapper.toDetail(q);
    }

    @Transactional
    public QuestionDetail create(QuestionRequest req) {
        User author = lookups.currentUser();
        Question q = new Question(author, req.title().trim(), req.body());
        q.setTags(resolveTags(req.tags()));
        return mapper.toDetail(questions.save(q));
    }

    @Transactional
    public QuestionDetail update(Long id, QuestionRequest req) {
        Question q = lookups.question(id);
        Permissions.requireOwnerOrModerator(q.getAuthor());
        q.edit(req.title().trim(), req.body());
        q.setTags(resolveTags(req.tags()));
        return mapper.toDetail(q);
    }

    @Transactional
    public void delete(Long id) {
        Question q = lookups.question(id);
        Permissions.requireOwnerOrModerator(q.getAuthor());
        List<Long> postIds = new ArrayList<>();
        postIds.add(q.getId());
        q.getAnswers().forEach(a -> postIds.add(a.getId()));
        votes.deleteByPostIds(postIds);
        q.acceptAnswer(null);
        questions.delete(q);
    }

    private Set<Tag> resolveTags(List<String> names) {
        Set<String> normalized = new LinkedHashSet<>();
        names.forEach(n -> normalized.add(n.trim().toLowerCase(Locale.ROOT)));
        Map<String, Tag> existing = new HashMap<>();
        tags.findByNameIn(normalized).forEach(t -> existing.put(t.getName(), t));

        Set<Tag> result = new LinkedHashSet<>();
        for (String name : normalized) {
            result.add(existing.computeIfAbsent(name, n -> tags.save(new Tag(n))));
        }
        return result;
    }

    private static Specification<Question> filters(boolean unanswered, String tag, String search, Long authorId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (unanswered) {
                predicates.add(cb.equal(root.get("answerCount"), 0));
            }
            if (tag != null && !tag.isBlank()) {
                predicates.add(cb.equal(root.join("tags").get("name"), tag.trim().toLowerCase(Locale.ROOT)));
            }
            if (authorId != null) {
                predicates.add(cb.equal(root.get("author").get("id"), authorId));
            }
            if (search != null && !search.isBlank()) {
                // Recherche simple : chaque mot doit apparaître dans le titre ou le corps
                for (String word : search.trim().toLowerCase(Locale.ROOT).split("\\s+")) {
                    String pattern = "%" + word.replace("%", "\\%").replace("_", "\\_") + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("title")), pattern, '\\'),
                            cb.like(cb.lower(root.get("body")), pattern, '\\')));
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
