package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an answer to a question in the Q&amp;A system.
 */
public class Answer {
    private int id;
    private int questionId;
    private String content;
    private String answeredBy;
    private LocalDateTime createdAt;
    private boolean isRead;
    private boolean isFlagged;
    private int upvotes;
    
    // Constants for validation
    public static final int CONTENT_MIN_LENGTH = 5;
    public static final int CONTENT_MAX_LENGTH = 500;  // CHANGED FROM 2000

    /**
     * Constructor to create a new Answer for a question.
     * 
     * @param questionId ID of the question the answer will be attached to.
     * @param content String containing the contents of the answer.
     * @param answeredBy String containing the username of the person posting the answer.
     */
    public Answer(int questionId, String content, String answeredBy) {
        this.questionId = questionId;
        this.content = validateContent(content);
        this.answeredBy = answeredBy;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.isFlagged = false;
        this.upvotes = 0;
    }
    
    /**
     * Constructor for loading an Answer from the database.
     * 
     * @param id ID of the answer.
     * @param questionId ID of the question the answer is attached to.
     * @param content String containing the contents of the answer.
     * @param answeredBy String containing the username of the person posting the answer.
     * @param createdAt Time and date the answer was created.
     * @param isRead Boolean of whether the answer is read or not.
     * @param isFlagged Boolean of whether the answer is flagged or not.
     * @param upvotes Number of upvotes the answer has received.
     */
    public Answer(int id, int questionId, String content, String answeredBy, 
                  LocalDateTime createdAt, boolean isRead, boolean isFlagged, int upvotes) {
        this.id = id;
        this.questionId = questionId;
        this.content = content;
        this.answeredBy = answeredBy;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.upvotes = upvotes;
    }
    
    /**
     * Method to validate the contents of an answer.
     * 
     * @param content String containing the contents of the answer.
     * @throws IllegalArgumentException If the content is invalid.
     * @return Answer contents if they are valid.
     */
    public static String validateContent(String content) {
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
    /**
     * Gets the answers ID.
     * @return id.
     */
    public int getId() { return id; }

     /**
     * Gets the question ID.
     * @return questionId.
     */
    public int getQuestionId() { return questionId; }

    /**
     * Gets the answers content.
     * @return content.
     */
    public String getContent() { return content; }

    /**
     * Gets answeredBy.
     * @return answeredBy.
     */
    public String getAnsweredBy() { return answeredBy; }

    /**
     * Gets the date and time the answer was created at.
     * @return createdAt.
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Gets the value of isRead.
     * @return isRead.
     */
    public boolean isRead() { return isRead; }
    
    
    /**
     * Gets the value of isFlagged.
     * @return isFlagged
     */
    public boolean isFlagged() { return isFlagged; }

    /**
     * Gets the number of upvotes.
     * @return upvotes.
     */
    public int getUpvotes() { return upvotes; }
    
    // Setters
    /**
     * Sets the answer ID.
     * @param id Int to set ID to.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Sets the answers content.
     * @param content String of what to set the content to.
     */
    public void setContent(String content) {
        this.content = validateContent(content);
    }

    /**
     * Sets the value of isRead to true.
     */
    public void markAsRead() {
        this.isRead = true;
    }
    
    /**
     * Sets the value of isFlagged to true.
     */
    public void markAsFlagged() {
    	this.isFlagged = true;
    }

    /**
     * Increments the value of upvotes.
     */
    public void incrementUpvotes() {
        this.upvotes++;
    }
    
    // Utility methods
    /**
     * Gets the date and time the answer was created at but formatted.
     * @return createdAt.
     */
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return createdAt.format(formatter);
    }

    /**
     * Gets answer details in a formatted form with id, answeredBy, and upvotes.
     * @return String with id, answeredBy, and upvotes.
     */
    @Override
    public String toString() {
        return String.format("Answer #%d by %s (%d upvotes)", 
            id, answeredBy, upvotes);
    }

}

