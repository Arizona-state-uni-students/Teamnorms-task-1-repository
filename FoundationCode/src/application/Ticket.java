package application;

import java.sql.Timestamp;

/**
 * Ticket class represents an admin request ticket in the system.
 * Used by instructors to request admin actions, tracked and resolved by admins.
 */
public class Ticket {
    private int id;
    private String title;
    private String content;
    private String askedBy;
    private Timestamp createdAt;
    private boolean isResolved;
    private Timestamp resolvedAt;
    private String resolvedBy;
    private String resolutionComments;
    private int parentTicketId; // Reference to original ticket if this is a reopened ticket
    
    // Constructors
    public Ticket() {
        this.parentTicketId = -1; // -1 means no parent (not a reopened ticket)
    }
    
    public Ticket(String title, String content, String askedBy) {
        this.title = title;
        this.content = content;
        this.askedBy = askedBy;
        this.isResolved = false;
        this.parentTicketId = -1;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getAskedBy() {
        return askedBy;
    }
    
    public void setAskedBy(String askedBy) {
        this.askedBy = askedBy;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isResolved() {
        return isResolved;
    }
    
    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }
    
    public Timestamp getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public String getResolvedBy() {
        return resolvedBy;
    }
    
    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }
    
    public String getResolutionComments() {
        return resolutionComments;
    }
    
    public void setResolutionComments(String resolutionComments) {
        this.resolutionComments = resolutionComments;
    }
    
    public int getParentTicketId() {
        return parentTicketId;
    }
    
    public void setParentTicketId(int parentTicketId) {
        this.parentTicketId = parentTicketId;
    }
    
    /**
     * Checks if this ticket is a reopened ticket.
     * 
     * @return true if this ticket references a parent ticket
     */
    public boolean isReopenedTicket() {
        return parentTicketId > 0;
    }
    
    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", askedBy='" + askedBy + '\'' +
                ", isResolved=" + isResolved +
                '}';
    }
}
