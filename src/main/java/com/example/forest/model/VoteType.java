package com.example.forest.model;

/**
 * VoteType.java
 *
 * Enum representing the two possible types of votes that users can cast on posts.
 * Each vote type is associated with a numerical value indicating its direction
 * (positive or negative), which is typically used to calculate the post’s overall score.
 *
 * <p>Typical usage:</p>
 * <ul>
 *   <li><b>UPVOTE</b> — Represents a positive vote (+1).</li>
 *   <li><b>DOWNVOTE</b> — Represents a negative vote (-1).</li>
 * </ul>
 */
public enum VoteType {

    /** Represents a positive vote (+1). */
    UPVOTE(1),

    /** Represents a negative vote (-1). */
    DOWNVOTE(-1);

    /** The numeric representation of the vote direction. */
    private final int direction;

    /**
     * Constructs a {@code VoteType} enum constant with a numeric direction value.
     *
     * @param direction +1 for upvote, -1 for downvote.
     */
    VoteType(int direction) {
        this.direction = direction;
    }

    /**
     * Returns the numeric direction associated with the vote.
     *
     * @return +1 for upvote, -1 for downvote.
     */
    public int getDirection() {
        return direction;
    }
}
