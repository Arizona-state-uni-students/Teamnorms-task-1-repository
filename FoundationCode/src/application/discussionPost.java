package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single message in the discussion
 */
public class discussionPost {
    
    private int id;
    private int userId;
    private String username;
    private String message;
    private int type;           // 1=public, 2=reviewer+, 3=stafF+, etc.
    private String q;           // optional tag/field (can be null)
    private LocalDateTime createdAt;

    // Formatter for pretty timestamps (e.g., "Nov 16, 2025 09:31 AM")
    private static final DateTimeFormatter DISPLAY_FORMATTER = 
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    // Constructor
    public discussionPost(int id, int userId, String username, String message, 
                           int type, String q, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.type = type;
        this.q = q;
        this.createdAt = createdAt;
    }

    // --- Getters ---
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getMessage() { return message; }
    public int getType() { return type; }
    public String getQ() { return q; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Optional: Human-readable type ---
    public String getTypeAsString() {
        return switch (type) {
            case 1 -> "Public";
            case 2 -> "Reviewer+";
            case 3 -> "Staff+";
            case 4 -> "Admin Only";
            default -> "0";
        };
    }

    public String getFormattedTime() {
        return createdAt.format(DISPLAY_FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getFormattedTime(), username, message);
    }
}