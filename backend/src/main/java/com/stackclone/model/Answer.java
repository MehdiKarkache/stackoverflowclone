package com.stackclone.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ANSWER")
public class Answer extends Post {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    private boolean accepted;

    protected Answer() {
    }

    public Answer(User author, Question question, String body) {
        super(author, body);
        this.question = question;
    }

    public Question getQuestion() { return question; }
    public boolean isAccepted() { return accepted; }
    void setAccepted(boolean accepted) { this.accepted = accepted; }
}
