package application;

import java.time.LocalDateTime;

/**
 * Represents a review of an answer in the application.
 * Encapsulates the details of a review, including its unique identifier,
 * the ID of the answer being reviewed, the text content of the review, the author of the review,
 * and the timestamp when the review was created.

 * Review objects are used to store and retrieve review data from the database
 *  or to pass review information between application layers.
 *
 */
public class Review {
    
    /** The unique identifier for this review. */
    private final int id;
    
    /** The identifier of the answer to which this review applies. */
    private final int answerId;
    
    /** The textual content of the review. */
    private final String reviewText;
    
    /** The name or identifier of the user who wrote the review. */
    private final String writtenBy;
    
    /** The date and time when the review was created. */
    private final LocalDateTime createdAt;

    /**
     * Constructs a new {@code Review} with the specified details.
     *
     * @param id         the unique identifier for the review
     * @param answerId   the identifier of the answer being reviewed
     * @param reviewText the text content of the review
     * @param writtenBy  the user who authored the review
     * @param createdAt  the timestamp when the review was created
     * @throws NullPointerException 
     */
    public Review(int id, int answerId, String reviewText, String writtenBy, LocalDateTime createdAt) {
        this.id = id;
        this.answerId = answerId;
        this.reviewText = reviewText;
        this.writtenBy = writtenBy;
        this.createdAt = createdAt;
    }

    /**
     * Returns the unique identifier of this review.
     * @return the review ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the identifier of the answer to which this review belongs.
     * @return the answer ID
     */
    public int getAnswerId() {
        return answerId;
    }

    /**
     * Returns the textual content of the review.
     * @return the review text
     */
    public String getReviewText() {
        return reviewText;
    }

    /**
     * Returns the name or identifier of the user who wrote the review.
     * @return the author of the review
     */
    public String getWrittenBy() {
        return writtenBy;
    }

    /**
     * Returns the date and time when the review was created.
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}