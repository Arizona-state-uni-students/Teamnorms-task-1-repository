package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an answer to a question in the Q&A system.
 */
public class Answer {
    private int id;
    private int questionId;
    private String content;
    private String answeredBy;
    private LocalDateTime createdAt;
    private boolean isRead;
    private int upvotes;
    
    // Constants for validation
    public static final int CONTENT_MIN_LENGTH = 5;
    public static final int CONTENT_MAX_LENGTH = 500;  // CHANGED FROM 2000
    
    // Constructor for new answers
    public Answer(int questionId, String content, String answeredBy) {
        this.questionId = questionId;
        this.content = validateContent(content);
        this.answeredBy = answeredBy;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.upvotes = 0;
    }
    
    // Constructor for loading from database
    public Answer(int id, int questionId, String content, String answeredBy, 
                  LocalDateTime createdAt, boolean isRead, int upvotes) {
        this.id = id;
        this.questionId = questionId;
        this.content = content;
        this.answeredBy = answeredBy;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.upvotes = upvotes;
    }
    
    // Validation
    private String validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer content cannot be empty");
        }
        if (content.length() < CONTENT_MIN_LENGTH) {
            throw new IllegalArgumentException("Answer must be at least " + CONTENT_MIN_LENGTH + " characters");
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("Answer cannot exceed " + CONTENT_MAX_LENGTH + " characters");
        }
        return content.trim();
    }
    
    // Getters
    public int getId() { return id; }
    public int getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getAnsweredBy() { return answeredBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
    public int getUpvotes() { return upvotes; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    
    public void setContent(String content) {
        this.content = validateContent(content);
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
    
    public void incrementUpvotes() {
        this.upvotes++;
    }
    
    // Utility methods
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return createdAt.format(formatter);
    }
    
    @Override
    public String toString() {
        return String.format("Answer #%d by %s (%d upvotes)", 
            id, answeredBy, upvotes);
    }
}