package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a question in the Q&amp;A system.
 * Contains question content, metadata, and associated answers.
 */
public class Question {
    private int id;
    private String title;
    private String content;
    private String askedBy;
    private LocalDateTime createdAt;
    private boolean isResolved;
    private boolean isFlagged;
    private int resolvedAnswerId;
    private List<Answer> answers;
    
    // Constants for validation
    public static final int TITLE_MIN_LENGTH = 5;
    public static final int TITLE_MAX_LENGTH = 100;
    public static final int CONTENT_MIN_LENGTH = 10;
    public static final int CONTENT_MAX_LENGTH = 500;  // CHANGED FROM 2000
    
    /**
     * Constructor to create a new Question. (ID auto generated)
     * 
     * @param title String containing the title of the question.
     * @param content String containing the contents of the question.
     * @param askedBy String containing the username of the person asking the question.
     */
    public Question(String title, String content, String askedBy) {
        this.title = validateTitle(title);
        this.content = validateContent(content);
        this.askedBy = askedBy;
        this.createdAt = LocalDateTime.now();
        this.isResolved = false;
        this.isFlagged = false;
        this.resolvedAnswerId = -1;
        this.answers = new ArrayList<>();
    }
    
    /**
     * Constructor for loading a question from the database.
     * 
     * @param id ID of the question.
     * @param title String containing the title of the question.
     * @param content String containing the contents of the question.
     * @param askedBy String containing the username of the person asking the question.
     * @param createdAt Time and date the question was created.
     * @param isResolved Boolean of whether the question is resolved.
     * @param isFlagged Boolean of whether the question is flagged or not.
     * @param resolvedAnswerId ID of the answer responsible for resolving the question.
     */
    public Question(int id, String title, String content, String askedBy, 
                    LocalDateTime createdAt, boolean isResolved, boolean isFlagged, int resolvedAnswerId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.askedBy = askedBy;
        this.createdAt = createdAt;
        this.isResolved = isResolved;
        this.isFlagged = isFlagged;
        this.resolvedAnswerId = resolvedAnswerId;
        this.answers = new ArrayList<>();
    }
    
    /**
     * Method to validate the title of a question.
     * 
     * @param title String containing the title of the question.
     * @throws IllegalArgumentException If the title is invalid.
     * @return Question title if its valid.
     */
    public static String validateTitle(String title) {
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

    /**
     * Method to validate the contents of a question.
     * 
     * @param content String containing the contents of the question.
     * @throws IllegalArgumentException If the content is invalid.
     * @return Question contents if they are valid.
     */
    public static String validateContent(String content) {
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
    /**
     * Gets the question ID.
     * @return id.
     */
    public int getId() { return id; }

    /**
     * Gets the question title.
     * @return title.
     */
    public String getTitle() { return title; }

    /**
     * Gets the question content.
     * @return content.
     */
    public String getContent() { return content; }

    /**
     * Gets askedBy.
     * @return askedBy.
     */
    public String getAskedBy() { return askedBy; }

    /**
     * Gets the date and time the question was created at.
     * @return createdAt.
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Gets the value of isResolved.
     * @return isResolved.
     */
    public boolean isResolved() { return isResolved; }

    /**
     * Gets the value of isFlagged.
     * @return isFlagged.
     */
    public boolean isFlagged() { return isFlagged; }
    
    /**
     * Gets resolvedAnswerId.
     * @return resolvedAnswerId.
     */
    public int getResolvedAnswerId() { return resolvedAnswerId; }

    /**
     * Gets a List of Answers.
     * @return An ArrayList of Answers.
     */
    public List<Answer> getAnswers() { return new ArrayList<>(answers); }

    /**
     * Gets the number of unread answers.
     * @return Number of answers where isRead is false.
     */
    public int getUnreadAnswerCount() {
        return (int) answers.stream().filter(a -> !a.isRead()).count();
    }
    
    // Setters
    /**
     * Sets the question ID.
     * @param id Int to set ID to.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Sets the question title.
     * @param title String to set title to.
     */
    public void setTitle(String title) {
        this.title = validateTitle(title);
    }

    /**
     * Sets the question content.
     * @param content String to set content to.
     */
    public void setContent(String content) {
        this.content = validateContent(content);
    }

    /**
     * Sets the value of isResolved to true and the value of resolvedAnswerId.
     * @param answerId ID of answer resolving question.
     */
    public void markAsResolved(int answerId) {
        this.isResolved = true;
        this.resolvedAnswerId = answerId;
    }
    
    /**
     * Sets the value of isFlagged to true.
     */
    public void markAsFlagged() {
    	this.isFlagged = true;
    }

    /**
     * Sets the value of isResolved to false and resets the value of resolvedAnswerId.
     */
    public void markAsUnresolved() {
        this.isResolved = false;
        this.resolvedAnswerId = -1;
    }

    /**
     * Adds an answer to the List of Answers for this question.
     * @param answer Answer to add to this question.
     */
    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }

    /**
     * Sets the answers for this question.
     * @param answers List of Answers for this question.
     */
    public void setAnswers(List<Answer> answers) {
        this.answers = new ArrayList<>(answers);
    }
    
    // Utility methods
    /**
     * Gets the date and time the question was created at but formatted.
     * @return createdAt.
     */
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return createdAt.format(formatter);
    }

    /**
     * Gets question details in a formatted form.
     * @return String with id, title, askedBy, number of answers, and isResolved.
     */
    @Override
    public String toString() {
        return String.format("Question #%d: %s (by %s, %d answers, %s)", 
            id, title, askedBy, answers.size(), isResolved ? "Resolved" : "Unresolved");
    }

}
