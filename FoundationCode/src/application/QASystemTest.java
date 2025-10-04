package application;


import databasePart1.DatabaseHelper;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import java.util.List;


/**

 * Test cases for the Q&A System CRUD operations

 */

public class QASystemTest {

    

    private static DatabaseHelper db;

    private static User testUser1;

    private static User testUser2;

    

    @BeforeAll

    static void setupDatabase() throws SQLException {

        db = new DatabaseHelper();

        db.connectToDatabase();

        

        // Create test users

        testUser1 = new User("testStudent1", "Pass123!", "user");

        testUser1.setEmail("test1@asu.edu");

        

        testUser2 = new User("testStudent2", "Pass456!", "user");

        testUser2.setEmail("test2@asu.edu");

        

        if (!db.doesUserExist("testStudent1")) {

            db.register(testUser1);

        }

        if (!db.doesUserExist("testStudent2")) {

            db.register(testUser2);

        }

    }

    

    @AfterAll

    static void cleanup() {

        db.closeConnection();

    }

    

    // ========== Question CRUD Tests ==========

    

    @Test

    @DisplayName("Test 1: Create valid question")

    void testCreateValidQuestion() throws SQLException {

        Question q = new Question(

            "How do I implement a linked list?",

            "I'm trying to create a linked list in Java but getting confused about pointers.",

            testUser1.getUserName()

        );

        

        int id = db.createQuestion(q);

        assertTrue(id > 0, "Question ID should be positive");

        assertEquals(id, q.getId(), "Question object should have correct ID");

    }

    

    @Test

    @DisplayName("Test 2: Create question with invalid title (too short)")

    void testCreateQuestionInvalidTitleTooShort() {

        assertThrows(IllegalArgumentException.class, () -> {

            new Question("Help", "I need help with my code", testUser1.getUserName());

        }, "Should throw exception for title less than 5 characters");

    }

    

    @Test

    @DisplayName("Test 3: Create question with invalid title (too long)")

    void testCreateQuestionInvalidTitleTooLong() {

        String longTitle = "A".repeat(201);

        assertThrows(IllegalArgumentException.class, () -> {

            new Question(longTitle, "Content here", testUser1.getUserName());

        }, "Should throw exception for title over 200 characters");

    }

    

    @Test

    @DisplayName("Test 4: Create question with empty title")

    void testCreateQuestionEmptyTitle() {

        assertThrows(IllegalArgumentException.class, () -> {

            new Question("", "Some content", testUser1.getUserName());

        }, "Should throw exception for empty title");

    }

    

    @Test

    @DisplayName("Test 5: Create question with invalid content (too short)")

    void testCreateQuestionInvalidContentTooShort() {

        assertThrows(IllegalArgumentException.class, () -> {

            new Question("Valid Title Here", "Short", testUser1.getUserName());

        }, "Should throw exception for content less than 10 characters");

    }

    

    @Test

    @DisplayName("Test 6: Create question with invalid content (too long)")

    void testCreateQuestionInvalidContentTooLong() {

        String longContent = "A".repeat(2001);

        assertThrows(IllegalArgumentException.class, () -> {

            new Question("Valid Title", longContent, testUser1.getUserName());

        }, "Should throw exception for content over 2000 characters");

    }

    

    @Test

    @DisplayName("Test 7: Create question with empty content")

    void testCreateQuestionEmptyContent() {

        assertThrows(IllegalArgumentException.class, () -> {

            new Question("Valid Title", "", testUser1.getUserName());

        }, "Should throw exception for empty content");

    }

    

    @Test

    @DisplayName("Test 8: Read question by ID")

    void testReadQuestionById() throws SQLException {

        Question q = new Question(

            "What is polymorphism?",

            "Can someone explain polymorphism with examples?",

            testUser1.getUserName()

        );

        int id = db.createQuestion(q);

        

        Question retrieved = db.getQuestionById(id);

        assertNotNull(retrieved, "Should retrieve the question");

        assertEquals(q.getTitle(), retrieved.getTitle(), "Titles should match");

        assertEquals(q.getContent(), retrieved.getContent(), "Content should match");

    }

    

    @Test

    @DisplayName("Test 9: Read non-existent question")

    void testReadNonExistentQuestion() throws SQLException {

        Question retrieved = db.getQuestionById(99999);

        assertNull(retrieved, "Should return null for non-existent question");

    }

    

    @Test

    @DisplayName("Test 10: Update question title and content")

    void testUpdateQuestion() throws SQLException {

        Question q = new Question(

            "Original Title",

            "Original content here",

            testUser1.getUserName()

        );

        int id = db.createQuestion(q);

        

        q.setTitle("Updated Title");

        q.setContent("Updated content with more details");

        boolean updated = db.updateQuestion(q);

        

        assertTrue(updated, "Update should succeed");

        

        Question retrieved = db.getQuestionById(id);

        assertEquals("Updated Title", retrieved.getTitle(), "Title should be updated");

        assertEquals("Updated content with more details", retrieved.getContent(), "Content should be updated");

    }

    

    @Test

    @DisplayName("Test 11: Update question with invalid title")

    void testUpdateQuestionInvalidTitle() throws SQLException {

        Question q = new Question(

            "Valid Original Title",

            "Valid content here",

            testUser1.getUserName()

        );

        db.createQuestion(q);

        

        assertThrows(IllegalArgumentException.class, () -> {

            q.setTitle("Bad");

        }, "Should throw exception when updating to invalid title");

    }

    

    @Test

    @DisplayName("Test 12: Delete question")

    void testDeleteQuestion() throws SQLException {

        Question q = new Question(

            "Question to Delete",

            "This question will be deleted",

            testUser1.getUserName()

        );

        int id = db.createQuestion(q);

        

        boolean deleted = db.deleteQuestion(id, testUser1.getUserName());

        assertTrue(deleted, "Deletion should succeed");

        

        Question retrieved = db.getQuestionById(id);

        assertNull(retrieved, "Deleted question should not exist");

    }

    

    @Test

    @DisplayName("Test 13: Delete question by wrong user")

    void testDeleteQuestionByWrongUser() throws SQLException {

        Question q = new Question(

            "Question by User1",

            "This question belongs to user1",

            testUser1.getUserName()

        );

        int id = db.createQuestion(q);

        

        boolean deleted = db.deleteQuestion(id, testUser2.getUserName());

        assertFalse(deleted, "User2 should not be able to delete User1's question");

        

        Question retrieved = db.getQuestionById(id);

        assertNotNull(retrieved, "Question should still exist");

    }

    

    // ========== Answer CRUD Tests ==========

    

    @Test

    @DisplayName("Test 14: Create valid answer")

    void testCreateValidAnswer() throws SQLException {

        Question q = new Question(

            "Test Question for Answer",

            "Question content here",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        Answer a = new Answer(qId, "This is my answer to your question", testUser2.getUserName());

        int aId = db.createAnswer(a);

        

        assertTrue(aId > 0, "Answer ID should be positive");

        assertEquals(aId, a.getId(), "Answer should have correct ID");

    }

    

    @Test

    @DisplayName("Test 15: Create answer with invalid content (too short)")

    void testCreateAnswerInvalidContentTooShort() {

        assertThrows(IllegalArgumentException.class, () -> {

            new Answer(1, "Hi", testUser1.getUserName());

        }, "Should throw exception for answer less than 5 characters");

    }

    

    @Test

    @DisplayName("Test 16: Create answer with invalid content (too long)")

    void testCreateAnswerInvalidContentTooLong() {

        String longContent = "A".repeat(2001);

        assertThrows(IllegalArgumentException.class, () -> {

            new Answer(1, longContent, testUser1.getUserName());

        }, "Should throw exception for answer over 2000 characters");

    }

    

    @Test

    @DisplayName("Test 17: Create answer with empty content")

    void testCreateAnswerEmptyContent() {

        assertThrows(IllegalArgumentException.class, () -> {

            new Answer(1, "", testUser1.getUserName());

        }, "Should throw exception for empty answer");

    }

    

    @Test

    @DisplayName("Test 18: Read answers for a question")

    void testReadAnswersForQuestion() throws SQLException {

        Question q = new Question(

            "Question with Multiple Answers",

            "This will have several answers",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        Answer a1 = new Answer(qId, "First answer here", testUser1.getUserName());

        Answer a2 = new Answer(qId, "Second answer here", testUser2.getUserName());

        

        db.createAnswer(a1);

        db.createAnswer(a2);

        

        List<Answer> answers = db.getAnswersForQuestion(qId);

        assertEquals(2, answers.size(), "Should have 2 answers");

    }

    

    @Test

    @DisplayName("Test 19: Update answer content")

    void testUpdateAnswer() throws SQLException {

        Question q = new Question(

            "Question for Update Test",

            "Testing answer updates",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        Answer a = new Answer(qId, "Original answer content", testUser2.getUserName());

        int aId = db.createAnswer(a);

        

        a.setContent("Updated answer content with more details");

        boolean updated = db.updateAnswer(a);

        

        assertTrue(updated, "Update should succeed");

        

        List<Answer> answers = db.getAnswersForQuestion(qId);

        Answer retrieved = answers.stream()

            .filter(ans -> ans.getId() == aId)

            .findFirst()

            .orElse(null);

        

        assertNotNull(retrieved, "Answer should exist");

        assertEquals("Updated answer content with more details", retrieved.getContent(), "Content should be updated");

    }

    

    @Test

    @DisplayName("Test 20: Delete answer")

    void testDeleteAnswer() throws SQLException {

        Question q = new Question(

            "Question for Delete Test",

            "Testing answer deletion",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        Answer a = new Answer(qId, "Answer to be deleted", testUser2.getUserName());

        int aId = db.createAnswer(a);

        

        boolean deleted = db.deleteAnswer(aId, testUser2.getUserName());

        assertTrue(deleted, "Deletion should succeed");

        

        List<Answer> answers = db.getAnswersForQuestion(qId);

        boolean exists = answers.stream().anyMatch(ans -> ans.getId() == aId);

        assertFalse(exists, "Deleted answer should not exist");

    }

    

    @Test

    @DisplayName("Test 21: Mark answer as read")

    void testMarkAnswerAsRead() throws SQLException {

        Question q = new Question(

            "Question for Read Test",

            "Testing mark as read",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        Answer a = new Answer(qId, "Unread answer", testUser2.getUserName());

        int aId = db.createAnswer(a);

        

        assertFalse(a.isRead(), "Answer should initially be unread");

        

        boolean marked = db.markAnswerAsRead(aId);

        assertTrue(marked, "Mark as read should succeed");

        

        List<Answer> answers = db.getAnswersForQuestion(qId);

        Answer retrieved = answers.stream()

            .filter(ans -> ans.getId() == aId)

            .findFirst()

            .orElse(null);

        

        assertNotNull(retrieved, "Answer should exist");

        assertTrue(retrieved.isRead(), "Answer should be marked as read");

    }

    

    @Test

    @DisplayName("Test 22: Upvote answer")

    void testUpvoteAnswer() throws SQLException {

        Question q = new Question(

            "Question for Upvote Test",

            "Testing upvotes",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        Answer a = new Answer(qId, "Answer to upvote", testUser2.getUserName());

        int aId = db.createAnswer(a);

        

        assertEquals(0, a.getUpvotes(), "Answer should start with 0 upvotes");

        

        boolean upvoted = db.upvoteAnswer(aId);

        assertTrue(upvoted, "Upvote should succeed");

        

        List<Answer> answers = db.getAnswersForQuestion(qId);

        Answer retrieved = answers.stream()

            .filter(ans -> ans.getId() == aId)

            .findFirst()

            .orElse(null);

        

        assertNotNull(retrieved, "Answer should exist");

        assertEquals(1, retrieved.getUpvotes(), "Answer should have 1 upvote");

    }

    

    // ========== Question List Tests ==========

    

    @Test

    @DisplayName("Test 23: Get all questions for user")

    void testGetAllQuestionsForUser() throws SQLException {

        // Create multiple questions for user1

        Question q1 = new Question("User1 Q1", "Content 1 here", testUser1.getUserName());

        Question q2 = new Question("User1 Q2", "Content 2 here", testUser1.getUserName());

        

        db.createQuestion(q1);

        db.createQuestion(q2);

        

        List<Question> questions = db.getAllQuestions(testUser1.getUserName());

        assertTrue(questions.size() >= 2, "Should have at least 2 questions for user1");

        

        boolean allMatchUser = questions.stream()

            .allMatch(q -> q.getAskedBy().equals(testUser1.getUserName()));

        assertTrue(allMatchUser, "All questions should belong to user1");

    }

    

    @Test

    @DisplayName("Test 24: Get unresolved questions only")

    void testGetUnresolvedQuestions() throws SQLException {

        Question q = new Question(

            "Unresolved Question",

            "This is unresolved",

            testUser1.getUserName()

        );

        db.createQuestion(q);

        

        List<Question> unresolved = db.getUnresolvedQuestions();

        assertTrue(unresolved.stream().noneMatch(Question::isResolved), 

                   "All questions should be unresolved");

    }

    

    @Test

    @DisplayName("Test 25: Search questions by keyword")

    void testSearchQuestions() throws SQLException {

        Question q = new Question(

            "How to use recursion in Java?",

            "I need help understanding recursive functions",

            testUser1.getUserName()

        );

        db.createQuestion(q);

        

        List<Question> results = db.searchQuestions("recursion");

        assertTrue(results.size() > 0, "Should find questions with 'recursion'");

        assertTrue(results.stream()

            .anyMatch(qu -> qu.getTitle().toLowerCase().contains("recursion") || 

                           qu.getContent().toLowerCase().contains("recursion")),

            "Results should contain keyword");

    }

    

    @Test

    @DisplayName("Test 26: Mark question as resolved with specific answer")

    void testMarkQuestionResolved() throws SQLException {

        Question q = new Question(

            "Question to Resolve",

            "This will be marked resolved",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        Answer a = new Answer(qId, "This is the solution", testUser2.getUserName());

        int aId = db.createAnswer(a);

        

        boolean marked = db.markQuestionResolved(qId, aId, testUser1.getUserName());

        assertTrue(marked, "Should successfully mark as resolved");

        

        Question retrieved = db.getQuestionById(qId);

        assertTrue(retrieved.isResolved(), "Question should be resolved");

        assertEquals(aId, retrieved.getResolvedAnswerId(), "Should have correct resolved answer ID");

    }

    

    @Test

    @DisplayName("Test 27: Count unread answers for question owner")

    void testUnreadAnswerCount() throws SQLException {

        Question q = new Question(

            "Question with Unread Answers",

            "Testing unread count",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        Answer a1 = new Answer(qId, "Unread answer 1", testUser2.getUserName());

        Answer a2 = new Answer(qId, "Unread answer 2", testUser2.getUserName());

        

        db.createAnswer(a1);

        db.createAnswer(a2);

        

        Question retrieved = db.getQuestionById(qId);

        assertEquals(2, retrieved.getUnreadAnswerCount(), "Should have 2 unread answers");

        

        // Mark one as read

        db.markAnswerAsRead(a1.getId());

        

        retrieved = db.getQuestionById(qId);

        assertEquals(1, retrieved.getUnreadAnswerCount(), "Should have 1 unread answer");

    }

    

    @Test

    @DisplayName("Test 28: Question deletion cascades to answers")

    void testQuestionDeletionCascade() throws SQLException {

        Question q = new Question(

            "Question to Delete with Answers",

            "This and its answers will be deleted",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        Answer a = new Answer(qId, "This answer will be deleted too", testUser2.getUserName());

        int aId = db.createAnswer(a);

        

        // Delete the question

        db.deleteQuestion(qId, testUser1.getUserName());

        

        // Check answers are also deleted

        List<Answer> answers = db.getAnswersForQuestion(qId);

        assertEquals(0, answers.size(), "Answers should be deleted with question");

    }

    

    @Test

    @DisplayName("Test 29: Question validation - null title")

    void testQuestionValidationNullTitle() {

        assertThrows(IllegalArgumentException.class, () -> {

            new Question(null, "Valid content here", testUser1.getUserName());

        }, "Should throw exception for null title");

    }

    

    @Test

    @DisplayName("Test 30: Answer validation - null content")

    void testAnswerValidationNullContent() {

        assertThrows(IllegalArgumentException.class, () -> {

            new Answer(1, null, testUser1.getUserName());

        }, "Should throw exception for null content");

    }

    

    @Test

    @DisplayName("Test 31: Close question without specifying answer")

    void testCloseQuestion() throws SQLException {

        Question q = new Question(

            "Question to Close",

            "This will be closed without specific answer",

            testUser1.getUserName()

        );

        int qId = db.createQuestion(q);

        

        // Add some answers

        Answer a1 = new Answer(qId, "First answer", testUser2.getUserName());

        Answer a2 = new Answer(qId, "Second answer", testUser2.getUserName());

        db.createAnswer(a1);

        db.createAnswer(a2);

        

        // Close the question

        boolean closed = db.closeQuestion(qId, testUser1.getUserName());

        assertTrue(closed, "Should successfully close question");

        

        Question retrieved = db.getQuestionById(qId);

        assertTrue(retrieved.isResolved(), "Question should be marked as resolved");

        assertEquals(-1, retrieved.getResolvedAnswerId(), "Should not have specific resolved answer");

    }

}