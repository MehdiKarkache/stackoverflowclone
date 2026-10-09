package com.stackclone.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@DiscriminatorValue("QUESTION")
public class Question extends Post {

    @Column(length = 150)
    private String title;

    private int viewCount;

    private int answerCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QuestionStatus status = QuestionStatus.OPEN;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_answer_id")
    private Answer acceptedAnswer;

    @ManyToMany
    @JoinTable(name = "question_tags",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @OrderBy("name ASC")
    private Set<Tag> tags = new LinkedHashSet<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    protected Question() {
    }

    public Question(User author, String title, String body) {
        super(author, body);
        this.title = title;
    }

    public void edit(String title, String body) {
        this.title = title;
        edit(body);
    }

    public void incrementViews() {
        this.viewCount++;
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
        answerCount = answers.size();
    }

    public void removeAnswer(Answer answer) {
        if (acceptedAnswer == answer) {
            acceptedAnswer = null;
        }
        answers.remove(answer);
        answerCount = answers.size();
    }

    /** Accepte une réponse (ou retire l'acceptation si answer est null). */
    public void acceptAnswer(Answer answer) {
        if (acceptedAnswer != null) {
            acceptedAnswer.setAccepted(false);
        }
        acceptedAnswer = answer;
        if (answer != null) {
            answer.setAccepted(true);
        }
    }

    public void setTags(Set<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public String getTitle() { return title; }
    public int getViewCount() { return viewCount; }
    public int getAnswerCount() { return answerCount; }
    public QuestionStatus getStatus() { return status; }
    public void setStatus(QuestionStatus status) { this.status = status; }
    public Answer getAcceptedAnswer() { return acceptedAnswer; }
    public Set<Tag> getTags() { return tags; }
    public List<Answer> getAnswers() { return answers; }
}
