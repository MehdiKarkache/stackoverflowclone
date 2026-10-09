package com.stackclone.service;

/** Barème de réputation, inspiré de celui de Stack Overflow. */
public final class ReputationRules {

    public static final int UPVOTE_RECEIVED = 10;
    public static final int DOWNVOTE_RECEIVED = -2;
    /** Voter contre une réponse coûte 1 point au votant. */
    public static final int DOWNVOTE_ANSWER_COST = -1;
    public static final int ANSWER_ACCEPTED = 15;
    public static final int ACCEPTING_ANSWER = 2;

    private ReputationRules() {
    }
}
