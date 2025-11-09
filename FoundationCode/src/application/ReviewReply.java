package application;

import java.time.LocalDateTime;

public class ReviewReply {
    
    private final int id;
    private final int reviewId;
    private final String replyText;
    private final String repliedBy;
    private final LocalDateTime createdAt;


    public ReviewReply(int id, int reviewId, String replyText, String repliedBy, LocalDateTime createdAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.replyText = replyText;
        this.repliedBy = repliedBy;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getReviewId() {
        return reviewId;
    }

    public String getReplyText() {
        return replyText;
    }

    public String getRepliedBy() {
        return repliedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    
}