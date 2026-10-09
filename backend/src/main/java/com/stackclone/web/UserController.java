package com.stackclone.web;

import com.stackclone.dto.PageResponse;
import com.stackclone.dto.UserDtos.UpdateProfileRequest;
import com.stackclone.dto.UserDtos.UserProfile;
import com.stackclone.dto.UserDtos.UserSummary;
import com.stackclone.model.User;
import com.stackclone.repository.AnswerRepository;
import com.stackclone.repository.QuestionRepository;
import com.stackclone.repository.UserRepository;
import com.stackclone.service.Lookups;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;
    private final QuestionRepository questions;
    private final AnswerRepository answers;
    private final Lookups lookups;

    public UserController(UserRepository users, QuestionRepository questions, AnswerRepository answers,
                          Lookups lookups) {
        this.users = users;
        this.questions = questions;
        this.answers = answers;
        this.lookups = lookups;
    }

    /** Utilisateurs triés par réputation. */
    @GetMapping
    public PageResponse<UserSummary> list(@RequestParam(defaultValue = "") String q,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "36") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100),
                Sort.by(Sort.Order.desc("reputation"), Sort.Order.asc("username")));
        return PageResponse.of(users.search(q.trim(), pageable), UserSummary::from);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public UserProfile profile(@PathVariable Long id) {
        return toProfile(lookups.user(id));
    }

    @PutMapping("/me")
    @Transactional
    public UserProfile updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        User me = lookups.currentUser();
        me.setBio(req.bio());
        return toProfile(me);
    }

    private UserProfile toProfile(User u) {
        return new UserProfile(u.getId(), u.getUsername(), u.getBio(), u.getReputation(), u.getRole().name(),
                u.getCreatedAt(), questions.countByAuthorId(u.getId()), answers.countByAuthorId(u.getId()));
    }
}
