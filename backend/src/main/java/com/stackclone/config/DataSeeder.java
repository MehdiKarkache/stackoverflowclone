package com.stackclone.config;

import com.stackclone.model.*;
import com.stackclone.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Remplit une base vide avec quelques données de démo.
 * Désactivable avec app.seed.enabled=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    static final String DEMO_PASSWORD = "password123";

    private final UserRepository users;
    private final QuestionRepository questions;
    private final AnswerRepository answers;
    private final CommentRepository comments;
    private final TagRepository tags;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository users, QuestionRepository questions, AnswerRepository answers,
                      CommentRepository comments, TagRepository tags, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.questions = questions;
        this.answers = answers;
        this.comments = comments;
        this.tags = tags;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        User admin = user("admin", Role.ADMIN, 2000, "Administrateur du site.");
        User alice = user("alice", Role.USER, 320, "Développeuse Java / Spring.");
        User bob = user("bob", Role.USER, 150, "Fan de React et de TypeScript.");

        Question q1 = question(bob, "How do I share state between two React components?",
                """
                I have two sibling components and I need both of them to read and update the same value.
                Right now each one has its own `useState` and they get out of sync.

                ```jsx
                function A() { const [count, setCount] = useState(0); ... }
                function B() { const [count, setCount] = useState(0); ... }
                ```

                What is the recommended approach?""",
                "react", "javascript", "react-hooks");
        Answer a1 = answer(alice, q1, """
                **Lift the state up** to their closest common parent and pass it down as props:

                ```jsx
                function Parent() {
                  const [count, setCount] = useState(0);
                  return <><A count={count} onChange={setCount} /><B count={count} onChange={setCount} /></>;
                }
                ```

                If many components deep in the tree need it, use a Context instead.""");
        q1.acceptAnswer(a1);
        comment(bob, a1, "Lifting the state worked perfectly, thanks!");

        Question q2 = question(alice, "Spring Boot: LazyInitializationException when returning an entity",
                """
                My controller returns a JPA entity directly and I get
                `LazyInitializationException: could not initialize proxy - no Session`.

                I have `spring.jpa.open-in-view=false`. How should I fix this properly?""",
                "java", "spring-boot", "jpa");
        answer(admin, q2, """
                Don't return entities from controllers. Map them to DTOs **inside a `@Transactional` service method**,
                while the session is still open. Keep `open-in-view=false`: it hides N+1 problems.

                For collections, use a fetch join or `hibernate.default_batch_fetch_size`.""");
        comment(bob, q2, "Good question, I hit the same issue last week.");

        question(bob, "What is the difference between docker compose up and docker compose start?",
                "Both seem to start my containers. When should I use one or the other?",
                "docker", "docker-compose");

        log.info("Données de démo créées : utilisateurs admin, alice, bob (mot de passe : {})", DEMO_PASSWORD);
    }

    private User user(String name, Role role, int reputation, String bio) {
        User u = new User(name, name + "@stackclone.dev", passwordEncoder.encode(DEMO_PASSWORD));
        u.setRole(role);
        u.setBio(bio);
        u.addReputation(reputation - 1);
        return users.save(u);
    }

    private Question question(User author, String title, String body, String... tagNames) {
        Question q = new Question(author, title, body);
        Set<Tag> set = new LinkedHashSet<>();
        for (String name : tagNames) {
            set.add(tags.findByName(name).orElseGet(() -> tags.save(new Tag(name))));
        }
        q.setTags(set);
        return questions.save(q);
    }

    private Answer answer(User author, Question q, String body) {
        Answer a = answers.save(new Answer(author, q, body));
        q.addAnswer(a);
        return a;
    }

    private void comment(User author, Post post, String body) {
        post.getComments().add(comments.save(new Comment(author, post, body)));
    }
}
