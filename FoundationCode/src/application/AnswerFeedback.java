package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents feedback/comment on an answer
 */
public class AnswerFeedback {
    private int id;
    private int answerId;
    private String feedbackText;
    private String givenBy;
    private LocalDateTime createdAt;
    
    /**
     * Constructor for creating new feedback
     */
    public AnswerFeedback(int answerId, String feedbackText, String givenBy) {
        this.answerId = answerId;
        this.feedbackText = feedbackText;
        this.givenBy = givenBy;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Constructor for loading from database
     */
    public AnswerFeedback(int id, int answerId, String feedbackText, String givenBy, LocalDateTime createdAt) {
        this.id = id;
        this.answerId = answerId;
        this.feedbackText = feedbackText;
        this.givenBy = givenBy;
        this.createdAt = createdAt;
    }
    
    // Getters
    public int getId() { return id; }
    public int getAnswerId() { return answerId; }
    public String getFeedbackText() { return feedbackText; }
    public String getGivenBy() { return givenBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setId(int id) { this.id = id; }
    
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return createdAt.format(formatter);
    }
}