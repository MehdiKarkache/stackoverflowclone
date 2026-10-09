package com.stackclone.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Un seul vote par utilisateur et par post (contrainte unique).
 * value vaut +1 (upvote) ou -1 (downvote).
 */
@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "vote_value", nullable = false)
    private int value;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Vote() {
    }

    public Vote(User user, Post post, int value) {
        this.user = user;
        this.post = post;
        this.value = value;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Post getPost() { return post; }
    public int getValue() { return value; }

    public void setValue(int value) {
        this.value = value;
        this.createdAt = Instant.now();
    }
}
