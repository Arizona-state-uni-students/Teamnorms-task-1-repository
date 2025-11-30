package application;

import databasePart1.DatabaseHelper;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Test cases for the Direct Message System CRUD operations.
 * Tests core messaging functionality including send, read, update, delete,
 * conversation management, and role-based access controls.
 */
public class DirectMessageSystemTest {
    
    /**
     * Define reference variables to be used in testing
     */
    private static DatabaseHelper db;
    private static User testStudent1;
    private static User testStudent2;
    private static User testInstructor;
    private static User testAdmin;

    /**
     * Sets up the database and creates test users with different roles.
     * 
     * @throws SQLException If database error occurs
     */
    @BeforeAll
    static void setupDatabase() throws SQLException {
        db = new DatabaseHelper();
        db.connectToDatabase();
        
        // Create test users with different roles
        testStudent1 = new User("dmStudent1", "Pass123!", "Student");
        testStudent1.setEmail("dmstudent1@asu.edu");
        
        testStudent2 = new User("dmStudent2", "Pass456!", "Student");
        testStudent2.setEmail("dmstudent2@asu.edu");
        
        testInstructor = new User("dmInstructor", "Pass789!", "Instructor");
        testInstructor.setEmail("dminstructor@asu.edu");
        
        testAdmin = new User("dmAdmin", "Pass000!", "Admin");
        testAdmin.setEmail("dmadmin@asu.edu");
        
        // Register users if they don't exist
        if (!db.doesUserExist("dmStudent1")) {
            db.register(testStudent1);
        }
        if (!db.doesUserExist("dmStudent2")) {
            db.register(testStudent2);
        }
        if (!db.doesUserExist("dmInstructor")) {
            db.register(testInstructor);
        }
        if (!db.doesUserExist("dmAdmin")) {
            db.register(testAdmin);
        }
    }

    /**
     * Closes the database connection after all tests.
     */
    @AfterAll
    static void cleanup() {
        db.closeConnection();
    }

    // ========== CREATE (Send Message) Tests ==========

    /**
     * Tests successful creation of a direct message between two users.
     * Verifies that a positive message ID is returned.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 1: Send a valid direct message")
    void testSendValidDirectMessage() throws SQLException {
        int messageId = db.sendDirectMessage(
            testStudent1.getUserName(),
            testStudent2.getUserName(),
            "Hello, this is a test message!"
        );
        
        assertTrue(messageId > 0, "Message ID should be positive");
    }

    /**
     * Tests that users are stored alphabetically regardless of send order.
     * Verifies bidirectional conversation storage.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 2: Messages stored with alphabetical user ordering")
    void testAlphabeticalUserOrdering() throws SQLException {
        // Send from student1 to student2
        db.sendDirectMessage(testStudent1.getUserName(), testStudent2.getUserName(), 
                           "Message from student1");
        
        // Send from student2 to student1
        db.sendDirectMessage(testStudent2.getUserName(), testStudent1.getUserName(), 
                           "Reply from student2");
        
        // Both should appear in the same conversation
        List<DatabaseHelper.DirectMessage> conversation = 
            db.getConversation(testStudent1.getUserName(), testStudent2.getUserName());
        
        assertTrue(conversation.size() >= 2, "Conversation should contain both messages");
        assertEquals(conversation.get(0).getUser1(), conversation.get(1).getUser1(),
                    "user1 should be consistent across messages");
    }

    // ========== READ (Retrieve Messages) Tests ==========

    /**
     * Tests retrieving a conversation between two users in chronological order.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 3: Retrieve conversation between users")
    void testGetConversation() throws SQLException {
        // Send multiple messages
        db.sendDirectMessage(testStudent1.getUserName(), testStudent2.getUserName(), "First message");
        db.sendDirectMessage(testStudent2.getUserName(), testStudent1.getUserName(), "Second message");
        db.sendDirectMessage(testStudent1.getUserName(), testStudent2.getUserName(), "Third message");
        
        List<DatabaseHelper.DirectMessage> conversation = 
            db.getConversation(testStudent1.getUserName(), testStudent2.getUserName());
        
        assertFalse(conversation.isEmpty(), "Conversation should not be empty");
        assertTrue(conversation.size() >= 3, "Should have at least 3 messages");
        
        // Verify chronological order (oldest first)
        for (int i = 0; i < conversation.size() - 1; i++) {
            assertTrue(
                conversation.get(i).getCreatedAt().isBefore(conversation.get(i + 1).getCreatedAt()) ||
                conversation.get(i).getCreatedAt().isEqual(conversation.get(i + 1).getCreatedAt()),
                "Messages should be in chronological order"
            );
        }
    }

    /**
     * Tests retrieving the conversation list for a user's inbox.
     * Verifies summaries include unread counts and last message preview.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 4: Get conversation list with summaries")
    void testGetConversationList() throws SQLException {
        // Send messages to create conversations
        db.sendDirectMessage(testInstructor.getUserName(), testStudent1.getUserName(), 
                           "Message from instructor");
        
        List<DatabaseHelper.ConversationSummary> summaries = 
            db.getConversationList(testStudent1.getUserName());
        
        assertNotNull(summaries, "Conversation list should not be null");
        assertTrue(summaries.size() > 0, "Should have at least one conversation");
        
        // Verify summary contains required fields
        DatabaseHelper.ConversationSummary summary = summaries.get(0);
        assertNotNull(summary.getOtherUser(), "Other user should not be null");
        assertNotNull(summary.getLastMessageContent(), "Last message content should not be null");
        assertNotNull(summary.getLastMessageAt(), "Last message timestamp should not be null");
        assertTrue(summary.getUnreadCount() >= 0, "Unread count should be non-negative");
    }

    /**
     * Tests user search functionality with role-based filtering.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 5: Search users with role-based filtering")
    void testSearchUsersWithRoleFilter() throws SQLException {
        // Student searching for other students
        List<String> studentResults = db.searchUsers("dmStudent", 
                                                     testStudent1.getUserName(), 
                                                     "Student");
        
        assertNotNull(studentResults, "Search results should not be null");
        assertFalse(studentResults.contains(testStudent1.getUserName()), 
                   "Should not include current user in results");
        
        // Instructor can search for students
        List<String> instructorResults = db.searchUsers("dmStudent", 
                                                        testInstructor.getUserName(), 
                                                        "Instructor");
        
        assertNotNull(instructorResults, "Instructor search should not be null");
    }

    /**
     * Tests getting total unread message count for a user.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 6: Get total unread message count")
    void testGetTotalUnreadCount() throws SQLException {
        // Send unread messages
        db.sendDirectMessage(testStudent2.getUserName(), testStudent1.getUserName(), 
                           "Unread message 1");
        db.sendDirectMessage(testInstructor.getUserName(), testStudent1.getUserName(), 
                           "Unread message 2");
        
        int unreadCount = db.getTotalUnreadCount(testStudent1.getUserName());
        
        assertTrue(unreadCount >= 2, "Should have at least 2 unread messages");
    }

    // ========== UPDATE Tests ==========

    /**
     * Tests marking messages in a conversation as read.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 7: Mark conversation as read")
    void testMarkConversationAsRead() throws SQLException {
        // Send messages
        db.sendDirectMessage(testStudent2.getUserName(), testStudent1.getUserName(), 
                           "Message to mark as read");
        
        int initialUnread = db.getTotalUnreadCount(testStudent1.getUserName());
        
        // Mark conversation as read
        int markedCount = db.markConversationAsRead(testStudent1.getUserName(), 
                                                   testStudent2.getUserName());
        
        assertTrue(markedCount >= 0, "Should mark zero or more messages as read");
        
        int finalUnread = db.getTotalUnreadCount(testStudent1.getUserName());
        assertTrue(finalUnread <= initialUnread, "Unread count should not increase");
    }

    /**
     * Tests updating message content by the sender.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 8: Update message content by sender")
    void testUpdateDirectMessage() throws SQLException {
        // Send a message
        int messageId = db.sendDirectMessage(testStudent1.getUserName(), 
                                            testStudent2.getUserName(), 
                                            "Original message content");
        
        // Update the message
        boolean updated = db.updateDirectMessage(messageId, 
                                                "Updated message content", 
                                                testStudent1.getUserName());
        
        assertTrue(updated, "Message should be updated successfully");
        
        // Verify update
        List<DatabaseHelper.DirectMessage> conversation = 
            db.getConversation(testStudent1.getUserName(), testStudent2.getUserName());
        
        boolean foundUpdated = conversation.stream()
            .anyMatch(m -> m.getId() == messageId && 
                          m.getContent().equals("Updated message content"));
        
        assertTrue(foundUpdated, "Updated content should be in conversation");
    }

    /**
     * Tests that only the sender can update their own message.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 9: Non-sender cannot update message")
    void testNonSenderCannotUpdateMessage() throws SQLException {
        int messageId = db.sendDirectMessage(testStudent1.getUserName(), 
                                            testStudent2.getUserName(), 
                                            "Message from student1");
        
        // Try to update as different user
        boolean updated = db.updateDirectMessage(messageId, 
                                                "Malicious edit", 
                                                testStudent2.getUserName());
        
        assertFalse(updated, "Non-sender should not be able to update message");
    }

    // ========== DELETE Tests ==========

    /**
     * Tests deleting a message by the sender.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 10: Delete message by sender")
    void testDeleteDirectMessage() throws SQLException {
        int messageId = db.sendDirectMessage(testStudent1.getUserName(), 
                                            testStudent2.getUserName(), 
                                            "Message to be deleted");
        
        boolean deleted = db.deleteDirectMessage(messageId, testStudent1.getUserName());
        
        assertTrue(deleted, "Message should be deleted successfully");
        
        // Verify deletion
        List<DatabaseHelper.DirectMessage> conversation = 
            db.getConversation(testStudent1.getUserName(), testStudent2.getUserName());
        
        boolean foundDeleted = conversation.stream()
            .anyMatch(m -> m.getId() == messageId);
        
        assertFalse(foundDeleted, "Deleted message should not appear in conversation");
    }

    /**
     * Tests that only the sender can delete their own message.
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 11: Non-sender cannot delete message")
    void testNonSenderCannotDeleteMessage() throws SQLException {
        int messageId = db.sendDirectMessage(testStudent1.getUserName(), 
                                            testStudent2.getUserName(), 
                                            "Protected message");
        
        // Try to delete as different user
        boolean deleted = db.deleteDirectMessage(messageId, testStudent2.getUserName());
        
        assertFalse(deleted, "Non-sender should not be able to delete message");
    }

    // ========== Helper Method Tests ==========

    /**
     * Tests DirectMessage helper method getOtherUser().
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 12: DirectMessage getOtherUser helper")
    void testGetOtherUserHelper() throws SQLException {
        db.sendDirectMessage(testStudent1.getUserName(), testStudent2.getUserName(), 
                           "Test helper methods");
        
        List<DatabaseHelper.DirectMessage> conversation = 
            db.getConversation(testStudent1.getUserName(), testStudent2.getUserName());
        
        DatabaseHelper.DirectMessage message = conversation.get(conversation.size() - 1);
        
        String otherUser = message.getOtherUser(testStudent1.getUserName());
        assertEquals(testStudent2.getUserName(), otherUser, 
                    "getOtherUser should return the other participant");
        
        otherUser = message.getOtherUser(testStudent2.getUserName());
        assertEquals(testStudent1.getUserName(), otherUser, 
                    "getOtherUser should work from either perspective");
    }

    /**
     * Tests DirectMessage helper method isSentBy().
     * 
     * @throws SQLException If database error occurs
     */
    @Test
    @DisplayName("Test 13: DirectMessage isSentBy helper")
    void testIsSentByHelper() throws SQLException {
        db.sendDirectMessage(testStudent1.getUserName(), testStudent2.getUserName(), 
                           "Testing isSentBy");
        
        List<DatabaseHelper.DirectMessage> conversation = 
            db.getConversation(testStudent1.getUserName(), testStudent2.getUserName());
        
        DatabaseHelper.DirectMessage message = conversation.get(conversation.size() - 1);
        
        assertTrue(message.isSentBy(testStudent1.getUserName()), 
                  "isSentBy should return true for sender");
        assertFalse(message.isSentBy(testStudent2.getUserName()), 
                   "isSentBy should return false for recipient");
    }
}