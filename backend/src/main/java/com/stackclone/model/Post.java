package com.stackclone.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Base commune des questions et des réponses : auteur, contenu, score, commentaires.
 * Stockées dans une seule table "posts", comme la base de Stack Overflow.
 */
@Entity
@Table(name = "posts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "post_type", length = 10)
public abstract class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30000)
    private String body;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    private int score;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    protected Post() {
    }

    protected Post(User author, String body) {
        this.author = author;
        this.body = body;
    }

    public void edit(String body) {
        this.body = body;
        this.updatedAt = Instant.now();
    }

    public void addScore(int delta) {
        this.score += delta;
    }

    public Long getId() { return id; }
    public String getBody() { return body; }
    public User getAuthor() { return author; }
    public int getScore() { return score; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Comment> getComments() { return comments; }
}
