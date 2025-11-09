package application;

import java.time.LocalDateTime;

/**
 * Represents a reply to a review.
 * Encapsulates the details of a reply, including its unique identifier,
 * the ID of the review it responds to, the text content of the reply, the author of the reply,
 * and the timestamp when the reply was created.
 * 
 * Instances of this class are used to store and retrieve reply information from reviews
 * in the database or to transfer reply data between layers of the application.
 *
 * 
 */
public class ReviewReply {
    
    /** The unique identifier for this reply. */
    private final int id;
    
    /** The identifier of the review to which this reply belongs. */
    private final int reviewId;
    
    /** The textual content of the reply. */
    private final String replyText;
    
    /** The name or identifier of the user who created the reply. */
    private final String repliedBy;
    
    /** The date and time when the reply was created. */
    private final LocalDateTime createdAt;

    /**
     * Constructs for ReviewReply object
     *
     * @param id        the unique identifier for the reply
     * @param reviewId  the identifier of the review being replied to
     * @param replyText the text content of the reply
     * @param repliedBy the user who authored the reply
     * @param createdAt the timestamp when the reply was created
     * @throws NullPointerException
     */
    public ReviewReply(int id, int reviewId, String replyText, String repliedBy, LocalDateTime createdAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.replyText = replyText;
        this.repliedBy = repliedBy;
        this.createdAt = createdAt;
    }

    /**
     * Returns the unique identifier of this reply.
     * @return the reply ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the identifier of the review to which this reply belongs.
     * @return the review ID
     */
    public int getReviewId() {
        return reviewId;
    }

    /**
     * Returns the textual content of the reply.
     * @return the reply text, never {@code null}
     */
    public String getReplyText() {
        return replyText;
    }

    /**
     * Returns the name or identifier of the user who created the reply.
     * @return the author of the reply, never {@code null}
     */
    public String getRepliedBy() {
        return repliedBy;
    }

    /**
     * Returns the date and time when the reply was created.
     * @return the creation timestamp, never {@code null}
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}