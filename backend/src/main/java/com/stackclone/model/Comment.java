package com.stackclone.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 600)
    private String body;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Comment() {
    }

    public Comment(User author, Post post, String body) {
        this.author = author;
        this.post = post;
        this.body = body;
    }

    public Long getId() { return id; }
    public String getBody() { return body; }
    public User getAuthor() { return author; }
    public Post getPost() { return post; }
    public Instant getCreatedAt() { return createdAt; }
}
