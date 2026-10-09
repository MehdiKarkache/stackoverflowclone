package com.stackclone.service;

import com.stackclone.model.Answer;
import com.stackclone.model.Post;
import com.stackclone.model.Question;
import com.stackclone.model.User;
import com.stackclone.repository.AnswerRepository;
import com.stackclone.repository.PostRepository;
import com.stackclone.repository.QuestionRepository;
import com.stackclone.repository.UserRepository;
import com.stackclone.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Chargement des entités avec une 404 propre quand elles n'existent pas. */
@Component
public class Lookups {

    private final UserRepository users;
    private final PostRepository posts;
    private final QuestionRepository questions;
    private final AnswerRepository answers;

    public Lookups(UserRepository users, PostRepository posts, QuestionRepository questions, AnswerRepository answers) {
        this.users = users;
        this.posts = posts;
        this.questions = questions;
        this.answers = answers;
    }

    public User currentUser() {
        return users.findById(CurrentUser.requireId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
    }

    public User user(Long id) {
        return users.findById(id).orElseThrow(() -> notFound("Utilisateur"));
    }

    public Post post(Long id) {
        return posts.findById(id).orElseThrow(() -> notFound("Post"));
    }

    public Question question(Long id) {
        return questions.findById(id).orElseThrow(() -> notFound("Question"));
    }

    public Answer answer(Long id) {
        return answers.findById(id).orElseThrow(() -> notFound("Réponse"));
    }

    private static ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " introuvable");
    }
}
