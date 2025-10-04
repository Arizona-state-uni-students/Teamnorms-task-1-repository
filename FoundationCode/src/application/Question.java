package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a question in the Q&A system.
 * Contains question content, metadata, and associated answers.
 */
public class Question {
    private int id;
    private String title;
    private String content;
    private String askedBy;
    private LocalDateTime createdAt;
    private boolean isResolved;
    private int resolvedAnswerId;
    private List<Answer> answers;
    
    // Constants for validation
    public static final int TITLE_MIN_LENGTH = 5;
    public static final int TITLE_MAX_LENGTH = 100;
    public static final int CONTENT_MIN_LENGTH = 10;
    public static final int CONTENT_MAX_LENGTH = 500;  // CHANGED FROM 2000
    
    // Constructor for new questions (ID auto-generated)
    public Question(String title, String content, String askedBy) {
        this.title = validateTitle(title);
        this.content = validateContent(content);
        this.askedBy = askedBy;
        this.createdAt = LocalDateTime.now();
        this.isResolved = false;
        this.resolvedAnswerId = -1;
        this.answers = new ArrayList<>();
    }
    
    // Constructor for loading from database
    public Question(int id, String title, String content, String askedBy, 
                    LocalDateTime createdAt, boolean isResolved, int resolvedAnswerId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.askedBy = askedBy;
        this.createdAt = createdAt;
        this.isResolved = isResolved;
        this.resolvedAnswerId = resolvedAnswerId;
        this.answers = new ArrayList<>();
    }
    
    // Validation methods
    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Question title cannot be empty");
        }
        if (title.length() < TITLE_MIN_LENGTH) {
            throw new IllegalArgumentException("Question title must be at least " + TITLE_MIN_LENGTH + " characters");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Question title cannot exceed " + TITLE_MAX_LENGTH + " characters");
        }
        return title.trim();
    }
    
    private String validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Question content cannot be empty");
        }
        if (content.length() < CONTENT_MIN_LENGTH) {
            throw new IllegalArgumentException("Question content must be at least " + CONTENT_MIN_LENGTH + " characters");
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("Question content cannot exceed " + CONTENT_MAX_LENGTH + " characters");
        }
        return content.trim();
    }
    
    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAskedBy() { return askedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isResolved() { return isResolved; }
    public int getResolvedAnswerId() { return resolvedAnswerId; }
    public List<Answer> getAnswers() { return new ArrayList<>(answers); }
    public int getUnreadAnswerCount() {
        return (int) answers.stream().filter(a -> !a.isRead()).count();
    }
    
    // Setters
    public void setId(int id) { this.id = id; }
    
    public void setTitle(String title) {
        this.title = validateTitle(title);
    }
    
    public void setContent(String content) {
        this.content = validateContent(content);
    }
    
    public void markAsResolved(int answerId) {
        this.isResolved = true;
        this.resolvedAnswerId = answerId;
    }
    
    public void markAsUnresolved() {
        this.isResolved = false;
        this.resolvedAnswerId = -1;
    }
    
    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }
    
    public void setAnswers(List<Answer> answers) {
        this.answers = new ArrayList<>(answers);
    }
    
    // Utility methods
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return createdAt.format(formatter);
    }
    
    @Override
    public String toString() {
        return String.format("Question #%d: %s (by %s, %d answers, %s)", 
            id, title, askedBy, answers.size(), isResolved ? "Resolved" : "Unresolved");
    }
}