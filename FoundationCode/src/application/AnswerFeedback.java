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
    private boolean isFlagged;
    private LocalDateTime createdAt;
    
    /**
     * Constructor for creating new feedback
     * 
     * @param answerId ID of the answer the feedback is for.
     * @param feedbackText Text content of the feedback.
     * @param givenBy Username of the user giving feedback.
     */
    public AnswerFeedback(int answerId, String feedbackText, String givenBy) {
        this.answerId = answerId;
        this.feedbackText = feedbackText;
        this.givenBy = givenBy;
        this.isFlagged = false;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Constructor for loading from database
     * 
     * @param id ID of feedback.
     * @param answerId ID of the answer the feedback is for.
     * @param feedbackText Text content of the feedback.
     * @param givenBy Username of the user giving feedback.
     * @param isFlagged whether the feedback is flagged or not.
     * @param createdAt Date and Time the feedback was created.
     */
    public AnswerFeedback(int id, int answerId, String feedbackText, String givenBy, boolean isFlagged, LocalDateTime createdAt) {
        this.id = id;
        this.answerId = answerId;
        this.feedbackText = feedbackText;
        this.givenBy = givenBy;
        this.isFlagged = isFlagged;
        this.createdAt = createdAt;
    }
    
    // Getters
    /**
     * Gets the id.
     * 
     * @return id
     */
    public int getId() { return id; }
    
    /**
     * Gets the answer id.
     * 
     * @return answerId
     */
    public int getAnswerId() { return answerId; }
    
    /**
     * Gets the feedback text
     * 
     * @return feedbackText
     */
    public String getFeedbackText() { return feedbackText; }
    
    /**
     * Gets the username of the user giving feedback.
     * 
     * @return givenBy
     */
    public String getGivenBy() { return givenBy; }
    
    /**
     * Gets the value of isFlagged.
     * @return the value of isFlagged
     */
    public boolean isFlagged() { return this.isFlagged; }
    
    /**
     * Sets the value of isFlagged.
     */
    public void setIsFlagged(boolean tf) {
    	this.isFlagged = tf;
    }
    
    /**
     * Gets createdAt
     * 
     * @return createdAt
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    /**
     * Sets the id.
     * 
     * @param id id to set.
     */
    public void setId(int id) { this.id = id; }
    
    /**
     * Gets the data and time but formatted.
     * 
     * @return createdAt in a format.
     */
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return createdAt.format(formatter);
    }
}