package application;

import java.time.LocalDateTime;

public class Review {
    
    // Fields corresponding to the 'reviews' table columns
    private final int id;
    private final int answerId;
    private final String reviewText;
    private final String writtenBy;
    private final LocalDateTime createdAt;


    public Review(int id, int answerId, String reviewText, String writtenBy, LocalDateTime createdAt) {
        this.id = id;
        this.answerId = answerId;
        this.reviewText = reviewText;
        this.writtenBy = writtenBy;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getAnswerId() {
        return answerId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getWrittenBy() {
        return writtenBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}