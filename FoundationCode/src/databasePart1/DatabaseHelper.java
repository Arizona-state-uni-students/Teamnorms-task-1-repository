package databasePart1;
import java.sql.*;
import java.util.*;
import application.Question;  
import application.Answer;
import application.AnswerFeedback;
import application.User;
import application.Review;
import application.ReviewReply;
import java.time.LocalDateTime;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {
    // JDBC driver name and database URL 
    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  
    // Database credentials 
    static final String USER = "sa"; 
    static final String PASS = ""; 
    private Connection connection = null;
    private Statement statement = null; 
    
    /**
     * Method to connect to database
     * @throws SQLException If a database error occurs.
     */
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement  = connection.createStatement(); // keep if legacy code needs it
            createTables();
            updateDatabaseSchema();
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 JDBC Driver not found", e);
        } catch (SQLException e) {
            // surface the problem to StartCSE360 instead of continuing
            throw e;
        }
    }
    
    /**
     * Method to ensure connection to database.
     * @throws SQLException If a database error occurs.
     */
    private void ensureConnected() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connectToDatabase();
        }
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Database connection could not be established.");
        }
    }

    /**
     * Create user table and invitation code table.
     * @throws SQLException If a database error occurs.
     */
    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(20) UNIQUE, "
                + "email VARCHAR(255), "
                + "firstName VARCHAR(20), "
                + "middleInitial VARCHAR(1), "
                + "lastName VARCHAR(20), "
                + "password VARCHAR(20), "
                + "otp VARCHAR(16), "
                + "role VARCHAR(20), "
                + "hasRequest BOOLEAN DEFAULT FALSE, "
        		+ "favorites VARCHAR(500)"
        		+ ")";
        statement.execute(userTable);
        createQATables();
        createDirectMessageTables();
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE, "
                + "expiresAt TIMESTAMP)";
        statement.execute(invitationCodesTable);
    }
    
    /**
     * Updates the database schema.
     */
    public void updateDatabaseSchema() {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "EMAIL")) {
                if (!rs.next()) {
                    System.out.println("Adding email column to database...");
                    statement.execute("ALTER TABLE cse360users ADD COLUMN email VARCHAR(255)");
                    System.out.println("Email column added successfully!");
                }
            }
            try (ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "MIDDLEINITIAL")) {
                if (!rs.next()) {
                    System.out.println("Adding middleInitial column to database...");
                    statement.execute("ALTER TABLE cse360users ADD COLUMN middleInitial VARCHAR(1)");
                    System.out.println("Middle Initial column added successfully!");
                }
            }
            try (ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "HASREQUEST")) {
                if (!rs.next()) {
                    System.out.println("Adding hasRequest column to database...");
                    statement.execute("ALTER TABLE cse360users ADD COLUMN hasRequest BOOLEAN DEFAULT FALSE");
                    System.out.println("hasRequest added successfully!");
                }
            }
            try (ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "OTPISUSED")) {
                if (!rs.next()) {
                    boolean hasOld;
                    try (ResultSet rsOld = meta.getColumns(null, null, "CSE360USERS", "TEMPPASSWORD_ISUSED")) {
                        hasOld = rsOld.next();
                    }
                    System.out.println("Adding otpIsUsed column to database...");
                    statement.execute("ALTER TABLE cse360users ADD COLUMN otpIsUsed BOOLEAN DEFAULT FALSE");
                    if (hasOld) {
                        statement.execute("UPDATE cse360users SET otpIsUsed = tempPassword_IsUsed WHERE tempPassword_IsUsed IS NOT NULL");
                        System.out.println("otpIsUsed backfilled from tempPassword_IsUsed.");
                    }
                }
            }
            try (ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "OTPEXPIRESAT")) {
                if (!rs.next()) {
                    System.out.println("Adding otpExpiresAt column to database...");
                    statement.execute("ALTER TABLE cse360users ADD COLUMN otpExpiresAt TIMESTAMP");
                    System.out.println("otpExpiresAt column added successfully!");
                }
            }
            try (ResultSet rs = meta.getColumns(null, null, "INVITATIONCODES", "EXPIRESAT")) {
                if (!rs.next()) {
                    System.out.println("Adding expiresAt column to InvitationCodes...");
                    statement.execute("ALTER TABLE InvitationCodes ADD COLUMN expiresAt TIMESTAMP");
                    System.out.println("expiresAt column added successfully!");
                }
            }
            try (ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "WEIGHT")) {
                if (!rs.next()) {
                    System.out.println("Adding weight column to database...");
                    statement.execute("ALTER TABLE cse360users ADD COLUMN weight INT DEFAULT 0");
                    System.out.println("Weight column added successfully!");
                    // Set weight to 0 for existing records
                    statement.execute("UPDATE cse360users SET weight = 0 WHERE weight IS NULL");
                    System.out.println("Existing records updated with weight = 0.");
                }
            }
            try (ResultSet rs = meta.getColumns(null, null, "QUESTIONS", "PARENTQUESTIONID")) {
                if (!rs.next()) {
                    System.out.println("Adding parentQuestionId to questions…");
                    statement.execute("ALTER TABLE questions ADD COLUMN parentQuestionId INT");
                    // Optional: FK (H2 allows this)
                    try { statement.execute("ALTER TABLE questions ADD CONSTRAINT fk_parent_q FOREIGN KEY (parentQuestionId) REFERENCES questions(id)"); }
                    catch (SQLException ignored) {}
                }
            }

        } catch (SQLException e) {
            System.out.println("Note: Could not add columns — they may already exist: " + e.getMessage());
        }
    }
    
    /**
     * Creates tables in database for questions, answers, private messages, answer feedback, reviews, and review replies.
     * 
     * @throws SQLException If a database error occurs.
     */
    private void createQATables() throws SQLException {
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "title VARCHAR(100) NOT NULL, "
                + "content VARCHAR(500) NOT NULL, "
                + "askedBy VARCHAR(20) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "isResolved BOOLEAN DEFAULT FALSE, "
                + "isFlagged BOOLEAN DEFAULT FALSE, "
                + "resolvedAnswerId INT DEFAULT -1, "
                + "FOREIGN KEY (askedBy) REFERENCES cse360users(userName))";
        statement.execute(questionsTable);

        String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "questionId INT NOT NULL, "
                + "content VARCHAR(500) NOT NULL, "
                + "answeredBy VARCHAR(20) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "isRead BOOLEAN DEFAULT FALSE, "
                + "hasReview BOOLEAN DEFAULT FALSE, "
                + "upvotes INT DEFAULT 0, "
                + "isFlagged BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (questionId) REFERENCES questions(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (answeredBy) REFERENCES cse360users(userName))";
        statement.execute(answersTable);

        // FIXED: Use from_user instead of sender
        String privateMessagesTable = "CREATE TABLE IF NOT EXISTS private_messages ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "questionId INT NOT NULL, "
                + "to_user VARCHAR(20) NOT NULL, "
                + "from_user VARCHAR(20) NOT NULL, "
                + "messageType VARCHAR(10) NOT NULL, "
                + "content VARCHAR(500) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "isRead BOOLEAN DEFAULT FALSE, "
                + "isFlagged BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (questionId) REFERENCES questions(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (from_user) REFERENCES cse360users(userName))";
        statement.execute(privateMessagesTable);
        
        String answerFeedbackTable = "CREATE TABLE IF NOT EXISTS answer_feedback ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "answerId INT NOT NULL, "
                + "feedbackText VARCHAR(500) NOT NULL, "
                + "givenBy VARCHAR(20) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "isFlagged BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (answerId) REFERENCES answers(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (givenBy) REFERENCES cse360users(userName))";
        statement.execute(answerFeedbackTable);
        
        String reviewsTable = "CREATE TABLE IF NOT EXISTS reviews ("
        		+ "    id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "    answerId INT NOT NULL,"
        		+ "    reviewText VARCHAR(1000) NOT NULL,"
        		+ "    writtenBy VARCHAR(20) NOT NULL,"
        		+ "    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
        		+ "	   isFlagged BOOLEAN DEFAULT FALSE, "
        		+ "    FOREIGN KEY (answerId) REFERENCES answers(id) ON DELETE CASCADE,"
        		+ "    FOREIGN KEY (writtenBy) REFERENCES cse360users(userName)"
        		+ ");";
        statement.execute(reviewsTable);
        
        String reviewRepliesTable = "CREATE TABLE IF NOT EXISTS review_replies ("
        		+ "    id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "    reviewId INT NOT NULL,"
        		+ "    replyText VARCHAR(500) NOT NULL,"
        		+ "    repliedBy VARCHAR(20) NOT NULL,"
        		+ "    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
        		+ "	   isFlagged BOOLEAN DEFAULT FALSE, "
        		+ "    FOREIGN KEY (reviewId) REFERENCES reviews(id) ON DELETE CASCADE,"
        		+ "    FOREIGN KEY (repliedBy) REFERENCES cse360users(userName)"
        		+ ");";
        statement.execute(reviewRepliesTable);
    }
    
    /**
     * Creates tables for direct messaging system.
     * 
     * @throws SQLException If a database error occurs.
     */
    private void createDirectMessageTables() throws SQLException {
        // Direct messages table
        String directMessagesTable = "CREATE TABLE IF NOT EXISTS direct_messages ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "user1 VARCHAR(20) NOT NULL, "
                + "user2 VARCHAR(20) NOT NULL, "
                + "fromUser VARCHAR(20) NOT NULL, "
                + "content VARCHAR(500) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "isRead BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (user1) REFERENCES cse360users(userName), "
                + "FOREIGN KEY (user2) REFERENCES cse360users(userName), "
                + "FOREIGN KEY (fromUser) REFERENCES cse360users(userName), "
                + "CONSTRAINT chk_user_order CHECK (user1 < user2))";
        statement.execute(directMessagesTable);
        
        // Conversations summary table
        String conversationsTable = "CREATE TABLE IF NOT EXISTS conversations ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "user1 VARCHAR(20) NOT NULL, "
                + "user2 VARCHAR(20) NOT NULL, "
                + "lastMessageAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "lastMessageContent VARCHAR(100), "
                + "UNIQUE(user1, user2), "
                + "FOREIGN KEY (user1) REFERENCES cse360users(userName), "
                + "FOREIGN KEY (user2) REFERENCES cse360users(userName), "
                + "CONSTRAINT chk_conversation_user_order CHECK (user1 < user2))";
        statement.execute(conversationsTable);
    }
    
    /**
     * Updates the text of an existing review and overwrites the 'createdAt' 
     * column to reflect the time of the update (modification).
     * @param reviewId The ID of the review to update.
     * @param newReviewText The new content for the review.
     * @return true if the review was successfully updated, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean editReview(int reviewId, String newReviewText) throws SQLException {
        String sql = "UPDATE reviews SET reviewText = ?, createdAt = CURRENT_TIMESTAMP WHERE id = ?"; 
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newReviewText);
            statement.setInt(2, reviewId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Retrieves a single Review object from the database by its answer ID.
     * @param answerId The primary key ID of the review to retrieve.
     * @return The Review object, or null if not found.
     * @throws SQLException if a database access error occurs.
     */
    public Review getReviewById(int answerId) throws SQLException {
        String sql = "SELECT id, answerId, reviewText, writtenBy, isFlagged, createdAt FROM reviews WHERE answerId = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, answerId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Review(
                        rs.getInt("id"),
                        rs.getInt("answerId"),
                        rs.getString("reviewText"),
                        rs.getString("writtenBy"),
                        rs.getBoolean("isFlagged"),
                        rs.getTimestamp("createdAt").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Adds a reply to an existing review.
     * @param reviewId The ID of the review being replied to.
     * @param userName The username of the replier.
     * @param replyText The content of the reply.
     * @throws SQLException if a database access error occurs.
     */
    public void addReviewReply(int reviewId, String userName, String replyText) throws SQLException {
        String sql = "INSERT INTO review_replies (reviewId, replyText, repliedBy) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reviewId);
            statement.setString(2, replyText);
            statement.setString(3, userName);
            statement.executeUpdate(); 
        }
    }
    
    /**
     * Retrieves a list of all ReviewReply objects associated with a given review ID.
     * @param reviewId The ID of the review to retrieve replies for.
     * @return A List of ReviewReply objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<ReviewReply> getRepliesForReview(int reviewId) throws SQLException {
        List<ReviewReply> replies = new ArrayList<>();
        
        String sql = "SELECT id, reviewId, replyText, repliedBy, isFlagged, createdAt FROM review_replies WHERE reviewId = ? ORDER BY createdAt ASC";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reviewId);
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ReviewReply reply = new ReviewReply(
                        rs.getInt("id"),
                        rs.getInt("reviewId"),
                        rs.getString("replyText"),
                        rs.getString("repliedBy"),
                        rs.getBoolean("isFlagged"),
                        rs.getTimestamp("createdAt").toLocalDateTime()
                    );
                    replies.add(reply);
                }
            }
        }
        return replies;
    }
    
    /**
     * Retrieves a list of all Review objects, optionally filtered by username.
     * If username is null or an empty string, all reviews are returned.
     * @param username The username of the user whose reviews to retrieve (optional).
     * @return A List of Review objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<Review> getAllReviews(String username) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT id, answerId, reviewText, writtenBy, isFlagged, createdAt FROM reviews";
        boolean filterByUser = username != null && !username.trim().isEmpty();
        if (filterByUser) {
            sql += " WHERE writtenBy = ?";
        }
        sql += " ORDER BY createdAt DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (filterByUser) {
                statement.setString(1, username);
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review(
                        rs.getInt("id"),
                        rs.getInt("answerId"),
                        rs.getString("reviewText"),
                        rs.getString("writtenBy"),
                        rs.getBoolean("isFlagged"),
                        rs.getTimestamp("createdAt").toLocalDateTime()
                    );
                    reviews.add(review);
                }
            }
        }
        return reviews;
    }
    
    /**
     * Retrieves a list of all Review objects associated with a given answer ID.
     * @param answerId The ID of the answer to retrieve reviews for.
     * @return A List of Review objects. The list will be empty if no reviews are found.
     * @throws SQLException if a database access error occurs.
     */
    public List<Review> getReviewsByAnswerId(int answerId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        
        // Order by createdAt ensures older reviews appear first
        String sql = "SELECT id, answerId, reviewText, writtenBy, createdAt FROM reviews WHERE answerId = ? ORDER BY createdAt ASC";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, answerId);
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review(
                        rs.getInt("id"),
                        rs.getInt("answerId"),
                        rs.getString("reviewText"),
                        rs.getString("writtenBy"),
                        rs.getBoolean("isFlagged"),
                        rs.getTimestamp("createdAt").toLocalDateTime()
                    );
                    reviews.add(review);
                }
            }
        }
        return reviews;
    }
    /**
     * Adds a review to an answer.
     * @param id        The ID of the answer to add a review to.
     * @param userName  Username of the reviewer.
     * @param reviewText Text content of the review.
     * @throws SQLException if a database access error occurs.
     */
    public void addReview(int id, String userName, String reviewText) throws SQLException {
        String sql = "INSERT INTO reviews (answerId, reviewText, writtenBy) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setString(2, reviewText);
            statement.setString(3, userName);
            statement.executeUpdate(); // Execute the insertion
            updateAnswerHasReview(true, id);
        }
    }
    /**
     * Updates an answer to show if it has a review
     * @param tf The value to update the reviewed status to.
     * @param answerId The ID of the answer to update.
     * @throws SQLException if a database access error occurs.
     */
    public void updateAnswerHasReview(Boolean tf, int answerId) throws SQLException {
    	String sql = "UPDATE answers SET hasReview = ? WHERE id = ?"; 
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, tf); 
            statement.setInt(2, answerId);
            statement.executeUpdate();
  
        }
    }
    
    /**
     * Retrieves the status of the 'hasReview' flag for a specific answer.
     * @param answerId The ID of the answer to check.
     * @return true if the answer has been reviewed, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean getAnswerHasReview(int answerId) throws SQLException {
        String sql = "SELECT hasReview FROM answers WHERE id = ?"; 
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, answerId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("hasReview");
                } else {
                    return false; 
                }
            }
        }
    }
    /**
     * Deletes a review record from the database by its primary key ID.
     * Due to FOREIGN KEY ON DELETE CASCADE, all associated review_replies are also deleted.
     * @param reviewId The ID of the review to delete.
     * @throws SQLException if a database access error occurs.
     */
    public void deleteReviewById(int reviewId) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ?"; 
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reviewId);
            statement.executeUpdate();
            updateAnswerHasReview(false, getReviewPointsTo(reviewId));
        }
    }
    
    /**
     * Gets the answerId the review points to
     * @param reviewId The ID of the review to check.
     * @return the answer ID.
     * @throws SQLException if a database access error occurs.
     */
    public int getReviewPointsTo(int reviewId) throws SQLException {
        String sql = "SELECT answerId FROM reviews WHERE id = ?"; 
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reviewId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // Returns the answerId that the review points to
                    return rs.getInt("answerId"); 
                } else {
                    return 0; 
                }
            }
        }
    }
    /**
     * Retrieves the ID of the question associated with a specific answer ID.
     * @param answerId The ID of the answer.
     * @return The ID of the corresponding question, or -1 if not found.
     * @throws SQLException if a database access error occurs.
     */
    public Question getQuestionByAnswerId(int answerId) throws SQLException {
        String sql = "SELECT questionId FROM answers WHERE id = ?";
        int questionId = -1;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, answerId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    questionId = rs.getInt("questionId");
                    Question q = getQuestionById(questionId);
                    return q;
                }else {
                	return null;
                }
            }
        }
    }
    
    /**
     * Check if the database is empty.
     * @return True or False
     * @throws SQLException If a database error occurs.
     */
    public boolean isDatabaseEmpty() throws SQLException {
        ensureConnected();
        final String sql = "SELECT COUNT(*) AS count FROM cse360users";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("count") == 0;
        }
        return true;
    }

    /**
     * Register new user into database user table.
     * 
     * @param user User to add into database.
     * @throws SQLException If a database error occurs.
     */
    public boolean register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, email, firstName, middleInitial, lastName, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getMiddleInitial().toUpperCase());
            pstmt.setString(5, user.getLastName());
            pstmt.setString(6, user.getPassword());
            pstmt.setString(7, user.getRole());
            pstmt.executeUpdate();

            return true;
        }
    }

    /**
     * Updates a user's email in the database.
     * 
     * @param username Username of the user to change the email for.
     * @param newEmail String to set the email to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateUserEmail(String username, String newEmail) throws SQLException {
        String sql = "UPDATE cse360users SET email = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (newEmail == null || newEmail.trim().isEmpty()) {
                pstmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(1, newEmail);
            }
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    /**
     * Updates a user's name in the database.
     * 
     * @param username Username of the user to change the email for.
     * @param firstname String to set the firstName to.
     * @param mi String to set the middleInitial to.
     * @param ln String to set the lastName to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateUsersName(String username, String firstname, String mi, String ln) throws SQLException {
        String sql = "UPDATE cse360users SET firstName = ?, middleInitial = ?, lastName = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firstname);
            pstmt.setString(2, mi);
            pstmt.setString(3, ln);
            pstmt.setString(4, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    /**
     * Gets the total number of questions created by username
     * 
     * @param username of the user to get question count
     * @return int number of questions asked
     * @throws SQLException
     */
    public int questionsCount(String username) throws SQLException {
        String sql = "SELECT COUNT(id) FROM questions WHERE askedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0; 
            }
            
        }
    }
    /**
     * Gets the total number of answers created by username
     * 
     * @param username
     * @return int number of answers
     * @throws SQLException
     */
    public int answersCount(String username) throws SQLException {
        String sql = "SELECT COUNT(id) FROM answers WHERE answeredBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0; 
            }
            
        }
    }
    /**
     * Gets the total number of answers marked as correct solutions by username
     * 
     * @param username
     * @return int number of answers marked as correct
     * @throws SQLException
     */
    public int correctAnswersCount(String username) throws SQLException{
    	String sql = "SELECT COUNT(A.id) " +
                "FROM answers A " +
                "INNER JOIN questions Q ON A.id = Q.resolvedAnswerId " +
                "WHERE A.answeredBy = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1); 
                    }
                    return 0;
                }
            }
    }
    /**
     * Gets the total number of reviews by username
     * @param username
     * @return int number of reviews and review replies
     * @throws SQLException
     */
    public int reviewsCount(String username) throws SQLException {
    	int total = 0;
        String sql = "SELECT COUNT(id) FROM reviews WHERE writtenBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getInt(1);
                }
            }
        }
        String sql2 = "SELECT COUNT(id) FROM review_replies WHERE repliedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql2)) {
            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total += rs.getInt(1);
                }
            }
        }
        return total;
    }
    
    /**
     * Log user into the system.
     * 
     * @param user User logging in.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean login(User user) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    try {
                    	user.setFirstName(rs.getString("firstName"));
                        user.setLastName(rs.getString("lastName"));
                        user.setEmail(rs.getString("email"));
                        user.setMiddleInitial(rs.getString("middleInitial"));
                        user.setWeight(rs.getInt("weight"));
                    } catch (SQLException e) {
                        // Columns might not exist
                    }
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * Checks if an Admin exists in the database.
     * 
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean doesAdminExist() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users WHERE role = 'admin'";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") > 0;
        }
        return false;
    }
    
    /**
     * Checks whether a certain user exists.
     * 
     * @param userName Username to check existence of.
     * @return True or False based on function success.
     */
    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Gets a list of all users in the database.
     * 
     * @return List of Users
     * @throws SQLException If a database error occurs.
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, email, middleInitial, role, password, weight FROM cse360users ORDER BY role";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                String middleInitial = rs.getString("middleInitial");
                String role = rs.getString("role");
                String password = rs.getString("password");
                int weight = rs.getInt("weight");
                User user = new User(username, password, role);
                user.setEmail(email);
                user.setMiddleInitial(middleInitial);
                user.setWeight(weight);
                users.add(user);
               
            }
        }
        return users;
    }

    /**
     * Gets a list of Reviewers by their role (all but two)
     * @return a list of users and their information
     * @throws SQLException If a database error occurs.
     */
    public List<User> getReviewers() throws SQLException {
    	ensureConnected();
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM cse360users WHERE role != User AND Student";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User(
                        rs.getString("userName"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getInt("weight"),
                        rs.getBoolean("hasRequest"),
                        rs.getString("favorites")
                    );
                    users.add(u);}}}
        return users;}
    

    /**
     * Updates the middle initial of a user in the database.
     * 
     * @param username Username to set middleInitial for.
     * @param newMiddleInitial String to set middleInitial to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateUserMiddleInitial(String username, String newMiddleInitial) throws SQLException {
        String sql = "UPDATE cse360users SET middleInitial = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (newMiddleInitial == null || newMiddleInitial.trim().isEmpty()) {
                pstmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                String initial = newMiddleInitial.trim().toUpperCase();
                if (initial.length() > 1) {
                    initial = initial.substring(0, 1);
                }
                pstmt.setString(1, initial);
            }
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Gets the role of a user in the database.
     * 
     * @return Role value
     * @param userName Username to get the role of.
     */
    public String getUserRole(String userName) {
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Gets the weight of a user in the database.
     * 
     * @return Weight value (int)
     * @param userName Username to get the role of.
     */
    public int getUserWeight(String userName) {
        String query = "SELECT weight FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("weight");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Deletes a user from the database.
     * 
     * @param username Username of the user to delete.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    

    /**
     * Gets the number of users currently in the database.
     * 
     * @return Number of users in database.
     * @throws SQLException If a database error occurs.
     */
    public int getUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM cse360users";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    /**
     * Gets user information by username.
     * 
     * @param username Username to retrieve info for.
     * @return User information.
     * @throws SQLException If a database error occurs.
     */
    public Optional<User> getUserByUsername(String username) throws SQLException {
        String sql = "SELECT username, role, password FROM cse360users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    String passwordHash = rs.getString("password");
                    User user = new User(username, passwordHash, role);
                    try {
                        String email = rs.getString("email");
                        user.setEmail(email);
                    } catch (SQLException e) {
                        // Email column might not exist
                    }
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Gets a User object from the Database
     * 
     * @param username The User to search for
     * @return a user and their information
     * @throws SQLException If a database error occurs.
     */
    public User getUser(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM cse360users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { 
                    return new User(
                        rs.getString("userName"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getInt("weight"),
                        rs.getBoolean("hasRequest"),
                        rs.getString("favorites")
                    );
                } else {
                    return null; 
                }
            }
        }
    }   
    
    
    /**
     * Gets a list of users by their role
     * 
     * @param role The role to search for
     * @return a list of users and their information
     * @throws SQLException If a database error occurs.
     */
    public List<User> getUsers_Role(String role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM cse360users WHERE role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (role != null) {
                pstmt.setString(1, role);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User(
                        rs.getString("userName"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getInt("weight"),
                        rs.getBoolean("hasRequest"),
                        rs.getString("favorites")
                    );
                    users.add(u);
                }
            }
        }
        return users;
    }
    
    
    /**
     * Updates the password of a user in the database.
     * 
     * @param username Username of the user to update information for.
     * @param newPassword String to update the password to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateUserPassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Sets a one time password for a user without an expiration time.
     * 
     * @param username Username of the user to set the one time password for.
     * @param otp String of the one time password.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean setOtp(String username, String otp) throws SQLException {
        String sql = "UPDATE cse360users SET otp = ?, otpIsUsed = FALSE, otpExpiresAt = NULL WHERE userName = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Sets a one time password for a user with an expiration time.
     * 
     * @param username Username of the user to set the one time password for.
     * @param otp String of the one time password.
     * @param ttlMinutes Time in minutes until the otp expires.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean setOtp(String username, String otp, int ttlMinutes) throws SQLException {
        String sql = "UPDATE cse360users SET otp = ?, otpIsUsed = FALSE, otpExpiresAt = ? WHERE userName = ?";
        java.sql.Timestamp expiresAt = new java.sql.Timestamp(System.currentTimeMillis() + ttlMinutes * 60L * 1000L);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setTimestamp(2, expiresAt);
            ps.setString(3, username);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Checks whether the one time password is valid.
     * 
     * @param username Username of the user with an otp to check.
     * @param otp String of the one time password.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean isOtpValid(String username, String otp) throws SQLException {
        String q = "SELECT 1 FROM cse360users " +
                   "WHERE userName = ? AND otp = ? " +
                   "AND (otpIsUsed = FALSE OR otpIsUsed IS NULL) " +
                   "AND (otpExpiresAt IS NULL OR otpExpiresAt > CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, username);
            ps.setString(2, otp);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Marks the one time password as used.
     * 
     * @param username Username of the user with an otp to use.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean consumeOtp(String username) throws SQLException {
        String q = "UPDATE cse360users SET otp = NULL, otpIsUsed = TRUE, otpExpiresAt = NULL WHERE userName = ?";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Removes expired one time passwords from the database.
     * 
     * @return The row count
     * @throws SQLException If a database error occurs.
     */
    public int purgeExpiredOtps() throws SQLException {
        String q = "UPDATE cse360users SET otp = NULL, otpIsUsed = TRUE WHERE otpExpiresAt IS NOT NULL AND otpExpiresAt <= CURRENT_TIMESTAMP";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Resets a user's password in the database.
     * 
     * @param username Username of the user to reset the password for.
     * @param newPassword String to update the password to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean resetUserPassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Updates the role of a user in the database.
     * 
     * @param username Username of the user to update role for.
     * @param newRole String of role to update to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateUserRole(String username, String newRole) throws SQLException {
        String sql = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Generates an invitation code
     * 
     * @return Invitation code.
     */
    public String generateInvitationCode() {
        return generateInvitationCode(0);
    }
    
    /**
     * Generates an invitation code.
     * 
     * @param ttlMinutes Time in minutes until invitation code expires.
     * @return Invitation code.
     */
    public String generateInvitationCode(int ttlMinutes) {
        String code = java.util.UUID.randomUUID().toString().substring(0, 4);
        String sql = "INSERT INTO InvitationCodes (code, isUsed, expiresAt) VALUES (?, FALSE, ?)";
        java.sql.Timestamp expiresAt = (ttlMinutes > 0)
                ? new java.sql.Timestamp(System.currentTimeMillis() + ttlMinutes * 60L * 1000L)
                : null;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            if (expiresAt == null) ps.setNull(2, java.sql.Types.TIMESTAMP);
            else ps.setTimestamp(2, expiresAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * Validates an invitation code.
     * 
     * @param code String of the invitation code.
     * @return True or False
     */
    public boolean validateInvitationCode(String code) {
        String q = "SELECT 1 FROM InvitationCodes " +
                   "WHERE code = ? AND isUsed = FALSE " +
                   "AND (expiresAt IS NULL OR expiresAt > CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    markInvitationCodeAsUsed(code);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Removes expired invitation code from the database.
     * 
     * @return The row count
     * @throws SQLException If a database error occurs.
     */
    public int purgeExpiredInvitationCodes() throws SQLException {
        String q = "UPDATE InvitationCodes SET isUsed = TRUE WHERE expiresAt IS NOT NULL AND expiresAt <= CURRENT_TIMESTAMP AND isUsed = FALSE";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            return ps.executeUpdate();
        }
    }
    
    /**
     * Marks an invitation code as used.
     * 
     * @param code String of the invitation code.
     */
    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
    public boolean hasPendingRequest(String username) throws SQLException {
        ensureConnected();

        String sql = "SELECT hasRequest FROM cse360users WHERE userName = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("hasRequest");
                }
            }
        }
        return false; // User not found → no request
    }


    /**
     * Updates the value of hasRequest.
     * 
     * @param username Username of the user.
     * @param tf Boolean value to update to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateHasRequest(String username, Boolean tf) throws SQLException {
        String sql = "UPDATE cse360users SET hasRequest = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        	pstmt.setBoolean(1, tf);
        	pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Gets a list of users who have a pending request.
     * 
     * @return List of users where hasRequest is true.
     * @throws SQLException If a database error occurs.
     */
    public List<User> getUsersWithRequest() throws SQLException {
    	List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM cse360users WHERE hasRequest = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, true);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                    		rs.getString("username"),
                    		rs.getString("password"),
                    		rs.getString("role")
                    );
                    users.add(user);
                }
            }
        }
        return users;
    }
    
    /**
     * Adds feedback/comment to an answer
     * 
     * @param feedback AnswerFeedback object to add
     * @return The generated feedback ID
     * @throws SQLException If a database error occurs
     */
    public int addAnswerFeedback(AnswerFeedback feedback) throws SQLException {
        String sql = "INSERT INTO answer_feedback (answerId, feedbackText, givenBy, createdAt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, feedback.getAnswerId());
            ps.setString(2, feedback.getFeedbackText());
            ps.setString(3, feedback.getGivenBy());
            ps.setTimestamp(4, Timestamp.valueOf(feedback.getCreatedAt()));
            
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    feedback.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to add answer feedback");
    }

    /**
     * Gets all feedback for a specific answer
     * 
     * @param answerId The answer ID
     * @return List of feedback
     * @throws SQLException If a database error occurs
     */
    public List<AnswerFeedback> getFeedbackForAnswer(int answerId) throws SQLException {
        List<AnswerFeedback> feedback = new ArrayList<>();
        String sql = "SELECT * FROM answer_feedback WHERE answerId = ? ORDER BY createdAt ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, answerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    feedback.add(new AnswerFeedback(
                        rs.getInt("id"),
                        rs.getInt("answerId"),
                        rs.getString("feedbackText"),
                        rs.getString("givenBy"),
                        rs.getBoolean("isFlagged"),
                        rs.getTimestamp("createdAt").toLocalDateTime()
                    ));
                }
            }
        }
        return feedback;
    }
    
    
    /**
     * Creates a new question in the database.
     * 
     * @param question Question to add into database.
     * @return Question Id
     * @throws SQLException If a database error occurs.
     */
    public int createQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO questions (title, content, askedBy, createdAt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getContent());
            pstmt.setString(3, question.getAskedBy());
            pstmt.setTimestamp(4, Timestamp.valueOf(question.getCreatedAt()));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating question failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    question.setId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating question failed, no ID obtained.");
                }
            }
        }
    }
    /**
     * Gets a question by its ID.
     * 
     * @param id Question id of question to retrieve.
     * @return Question
     * @throws SQLException If a database error occurs.
     */
    public Question getQuestionById(int id) throws SQLException {
        String sql = "SELECT * FROM questions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Question q = new Question(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("askedBy"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isResolved"),
                        rs.getBoolean("isFlagged"),
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(id));
                    return q;
                }
            }
        }
        return null;
    }

    
    
    /**
     * Gets all questions by a specific user.
     * 
     * @param username String of username of user to retrieve questions from.
     * @return List of questions
     * @throws SQLException If a database error occurs.
     */
    public List<Question> getAllQuestions(String username) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = username == null ? 
            "SELECT * FROM questions ORDER BY createdAt DESC" :
            "SELECT * FROM questions WHERE askedBy = ? ORDER BY createdAt DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (username != null) {
                pstmt.setString(1, username);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("askedBy"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isResolved"),
                        rs.getBoolean("isFlagged"),
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }
    
    /** Filters for future implementation */
    public static class QuestionFilter {
        private String askedBy;                 // optional
        private Boolean isResolved;             // optional
        private LocalDateTime createdAfter;     // optional
        private LocalDateTime createdBefore;    // optional

        // --- Getters ---
        /**
         * Gets askedBy
         * @return askedBy
         */
        public String getAskedBy() { return askedBy; }
        /**
         * Gets isResolved
         * @return isResolved
         */
        public Boolean getIsResolved() { return isResolved; }
        /**
         * Gets createdAfter
         * @return createdAfter
         */
        public LocalDateTime getCreatedAfter() { return createdAfter; }
        /**
         * Gets createdBefore
         * @return createdBefore
         */
        public LocalDateTime getCreatedBefore() { return createdBefore; }

        // --- Setters ---
        /**
         * Sets askedBy
         * @param askedBy
         */
        public void setAskedBy(String askedBy) { this.askedBy = askedBy; }
        /**
         * Sets isResolved
         * @param isResolved
         */
        public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
        /**
         * Sets createdAfter
         * @param createdAfter
         */
        public void setCreatedAfter(LocalDateTime createdAfter) { this.createdAfter = createdAfter; }
        /**
         * Sets createdBefore
         * @param createdBefore
         */
        public void setCreatedBefore(LocalDateTime createdBefore) { this.createdBefore = createdBefore; }
    }

    /**
     * Gets a list of all unresolved questions in the database.
     * 
     * @return List of questions.
     * @throws SQLException If a database error occurs.
     */
    public List<Question> getUnresolvedQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE isResolved = FALSE ORDER BY createdAt DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("askedBy"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isResolved"),
                        rs.getBoolean("isFlagged"),
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    /**
     * Gets a list of questions based on a keyword.
     * 
     * @param keyword String to search for.
     * @return List of questions
     * @throws SQLException If a database error occurs.
     */
    public List<Question> searchQuestions(String keyword) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE LOWER(title) LIKE ? OR LOWER(content) LIKE ? ORDER BY createdAt DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("askedBy"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isResolved"),
                        rs.getBoolean("isFlagged"),
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    /**
     * Updates a questions information (Title, content, id, and askedBy).
     * 
     * @param question Question to update information for.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateQuestion(Question question) throws SQLException {
        String sql = "UPDATE questions SET title = ?, content = ? WHERE id = ? AND askedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getContent());
            pstmt.setInt(3, question.getId());
            pstmt.setString(4, question.getAskedBy());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Deletes a question from the database.
     * 
     * @param questionId ID of the question to delete.
     * @param username Username of the user who asked the question.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean deleteQuestion(int questionId, String username) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ? AND askedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Marks a question as resolved.
     * 
     * @param questionId ID of the question to mark as resolved.
     * @param answerId ID of the answer responsible for resolving the question.
     * @param username Username of the user who asked the question.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean markQuestionResolved(int questionId, int answerId, String username) throws SQLException {
        String sql = "UPDATE questions SET isResolved = TRUE, resolvedAnswerId = ? WHERE id = ? AND askedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            pstmt.setInt(2, questionId);
            pstmt.setString(3, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Marks a question as flagged.
     * 
     * @param questionId ID of the question to mark as flagged.
     * @param username Username of the user who asked the question.
     * @param tf Boolean to set isFlagged to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean markQuestionFlagged(int questionId, String username, boolean tf) throws SQLException {
        String sql = "UPDATE questions SET isFlagged = ? WHERE id = ? AND askedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        	pstmt.setBoolean(1, tf);
        	pstmt.setInt(2, questionId);
            pstmt.setString(3, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Marks a answer as flagged.
     * 
     * @param id ID of the answer to mark as flagged.
     * @param username Username of the user who posted the answer.
     * @param tf Boolean to set isFlagged to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean markAnswerFlagged(int id, String username, boolean tf) throws SQLException {
        String sql = "UPDATE answers SET isFlagged = ? WHERE id = ? AND answeredBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        	pstmt.setBoolean(1, tf);
        	pstmt.setInt(2, id);
            pstmt.setString(3, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Marks a review as flagged.
     * 
 	 * @param id ID of the review to mark as flagged.
     * @param username Username of the user who posted review.
     * @param tf Boolean to set isFlagged to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean markReviewFlagged(int id, String username, boolean tf) throws SQLException {
        String sql = "UPDATE reviews SET isFlagged = ? WHERE id = ? AND writtenBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        	pstmt.setBoolean(1, tf);
        	pstmt.setInt(2, id);
            pstmt.setString(3, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Marks a review reply as flagged.
     * 
     * @param id ID of the reply to mark as flagged.
     * @param username Username of the user who posted reply.
     * @param tf Boolean to set isFlagged to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean markReplyFlagged(int id, String username, boolean tf) throws SQLException {
        String sql = "UPDATE review_replies SET isFlagged = ? WHERE id = ? AND repliedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        	pstmt.setBoolean(1, tf);
        	pstmt.setInt(2, id);
            pstmt.setString(3, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Marks a feedback as flagged.
     * 
     * @param id ID of the feedback to mark as flagged.
     * @param username Username of the user who posted the feedback.
     * @param tf Boolean to set isFlagged to.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean markFeedbackAsFlagged(int id, String username, boolean tf) throws SQLException {
        String sql = "UPDATE answer_feedback SET isFlagged = ? WHERE id = ? AND givenBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, tf);
        	pstmt.setInt(2, id);
            pstmt.setString(3, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    /**
     * Returns a list of all objects where isFlagged is true.
     * @return List of flagged objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Object> getFlaggedObjects() throws SQLException {
        List<Object> flagged = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE isFlagged = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer(
                        rs.getInt("id"),
                        rs.getInt("questionId"),
                        rs.getString("content"),
                        rs.getString("answeredBy"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isRead"),
                        rs.getBoolean("isFlagged"),
                        rs.getInt("upvotes")
                    );
                    flagged.add(a);
                }
            }
        }
        
        sql = "SELECT * FROM question WHERE isFlagged = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                	Question q = new Question(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("askedBy"),
                            rs.getTimestamp("createdAt").toLocalDateTime(),
                            rs.getBoolean("isResolved"),
                            rs.getBoolean("isFlagged"),
                            rs.getInt("resolvedAnswerId")
                    );
                    flagged.add(q);
                }
            }
        }
        
        sql = "SELECT * FROM reviews WHERE isFlagged = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                	Review r = new Review(
                            rs.getInt("id"),
                            rs.getInt("answerId"),
                            rs.getString("reviewText"),
                            rs.getString("writtenBy"),
                            rs.getBoolean("isFlagged"),
                            rs.getTimestamp("createdAt").toLocalDateTime()
                        );
                    flagged.add(r);
                }
            }
        }
        
        sql = "SELECT * FROM review_replies WHERE isFlagged = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                	ReviewReply rp = new ReviewReply(
                            rs.getInt("id"),
                            rs.getInt("reviewId"),
                            rs.getString("replyText"),
                            rs.getString("repliedBy"),
                            rs.getBoolean("isFlagged"),
                            rs.getTimestamp("createdAt").toLocalDateTime()
                    );
                    flagged.add(rp);
                }
            }
        }
        
        sql = "SELECT * FROM private_messages WHERE isFlagged = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                	PrivateMessage pm = new PrivateMessage(
                            rs.getInt("id"),
                            rs.getInt("questionId"),
                            rs.getString("to_user"),
                            rs.getString("from_user"),
                            rs.getString("messageType"),
                            rs.getString("content"),
                            rs.getTimestamp("createdAt").toLocalDateTime(),
                            rs.getBoolean("isRead"),
                            rs.getBoolean("isFlagged")
                    );
                    flagged.add(pm);
                }
            }
        }
        
        sql = "SELECT * FROM answer_feedback WHERE isFlagged = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                	AnswerFeedback af = new AnswerFeedback(
                            rs.getInt("id"),
                            rs.getInt("answerId"),
                            rs.getString("feedbackText"),
                            rs.getString("givenBy"),
                            rs.getBoolean("isFlagged"),
                            rs.getTimestamp("createdAt").toLocalDateTime()
                    );
                    flagged.add(af);
                }
            }
        }
        return flagged;
    }
    
    /**
     * Creates a new answer in the database.
     * 
     * @param answer Answer to add into database.
     * @return Answer ID
     * @throws SQLException If a database error occurs.
     */
    public int createAnswer(Answer answer) throws SQLException {
        String sql = "INSERT INTO answers (questionId, content, answeredBy, createdAt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, answer.getQuestionId());
            pstmt.setString(2, answer.getContent());
            pstmt.setString(3, answer.getAnsweredBy());
            pstmt.setTimestamp(4, Timestamp.valueOf(answer.getCreatedAt()));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating answer failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    answer.setId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating answer failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Gets the number of unread answers for a particular question.
     * 
     * @param questionId ID of question to check the number of unread answers for.
     * @return Number of unread answers.
     * @throws SQLException If a database error occurs.
     */
    public int getUnreadAnswersCount(int questionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM answers WHERE questionId = ? AND isRead = false";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0; 
    }
    
    /**
     * Gets the list of answers for a user.
     *
     * @param user Username whose answers should be returned.
     * @return List of answers written by that user.
     * @throws SQLException If a database error occurs.
     */
    public List<Answer> getAnswersByUser(String user) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE answeredBy = ? ORDER BY upvotes DESC, createdAt ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer(
                        rs.getInt("id"),
                        rs.getInt("questionId"),
                        rs.getString("content"),
                        rs.getString("answeredBy"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isRead"),
                        rs.getBoolean("isFlagged"),
                        rs.getInt("upvotes")
                    );
                    answers.add(a);
                }
            }
        }
        return answers;
    }
    
    /**
     * Gets the list of answers for users with a certain role.
     *
     * @param role Role whose answers should be returned.
     * @return List of answers written by users with that role.
     * @throws SQLException If a database error occurs.
     */
    public List<Answer> getAnswersByRole(String role) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE questionId = ? ORDER BY upvotes DESC, createdAt ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer(
                        rs.getInt("id"),
                        rs.getInt("questionId"),
                        rs.getString("content"),
                        rs.getString("answeredBy"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isRead"),
                        rs.getBoolean("isFlagged"),
                        rs.getInt("upvotes")
                    );
                    answers.add(a);
                }
            }
        }
        return answers;
    }
    
    /**
     * Gets the list of answers for a particular question.
     * 
     * @param questionId ID of the question to retrieve the answers for.
     * @return List of answers
     * @throws SQLException If a database error occurs.
     */
    public List<Answer> getAnswersForQuestion(int questionId) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE questionId = ? ORDER BY upvotes DESC, createdAt ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer(
                        rs.getInt("id"),
                        rs.getInt("questionId"),
                        rs.getString("content"),
                        rs.getString("answeredBy"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isRead"),
                        rs.getBoolean("isFlagged"),
                        rs.getInt("upvotes")
                    );
                    answers.add(a);
                }
            }
        }
        return answers;
    }
    

    /**
     * Updates an answers information (Content, id, answeredBy).
     * 
     * @param answer Answer to update information for.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateAnswer(Answer answer) throws SQLException {
        String sql = "UPDATE answers SET content = ? WHERE id = ? AND answeredBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, answer.getContent());
            pstmt.setInt(2, answer.getId());
            pstmt.setString(3, answer.getAnsweredBy());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Deletes an answer from the database.
     * 
     * @param answerId ID of the answer to delete from the database.
     * @param username Username of the user who posted the answer.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean deleteAnswer(int answerId, String username) throws SQLException {
        String sql = "DELETE FROM answers WHERE id = ? AND answeredBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Marks an answer as read.
     * 
     * @param answerId Id of the answer to mark as read.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean markAnswerAsRead(int answerId) throws SQLException {
        String sql = "UPDATE answers SET isRead = TRUE WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Updates the upvotes for an answer.
     * 
     * @param answerId ID of the answer to update.
     * @param username Username of the user who posted the answer.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean upvoteAnswer(int answerId, String username) throws SQLException {
        try {

            // Increment the upvotes for the answer
            String upvoteSql = "UPDATE answers SET upvotes = upvotes + 1 WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(upvoteSql)) {
                pstmt.setInt(1, answerId);
                pstmt.executeUpdate();
            }
            // Increment the answerer's weight
            String updateWeightSql = "UPDATE cse360users SET weight = weight + 1 WHERE userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateWeightSql)) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error in upvoteAnswer: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Closes a question (marks as resolved).
     * 
     * @param questionId ID of the question to close.
     * @param username Username of the user who asked the question.
     * @return True or False based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean closeQuestion(int questionId, String username) throws SQLException {
        String sql = "UPDATE questions SET isResolved = TRUE WHERE id = ? AND askedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Represents a private message
     */
    public static class PrivateMessage {
        private final int id;
        private final int questionId;
        private final String to_user;
        private final String sender;
        private final String messageType; // 'QUESTION' or 'ANSWER'
        private final String content;
        private final java.time.LocalDateTime createdAt;
        private final boolean isRead;
        private boolean isFlagged;
        /**
         * Constructor to create a new PrivateMessage
         * 
         * @param id ID of the private message.
         * @param questionId ID of the question the message is meant for.
         * @param sender Username of the user sending the message.
         * @param messageType Type of message (Question or Answer).
         * @param content Content of the message.
         * @param createdAt Time the message was created.
         * @param isRead If the message is read or not.
         * @param isFlagged If the message is flagged or not.
         */
        public PrivateMessage(int id, int questionId, String to_user, String sender, String messageType, String content,
                              java.time.LocalDateTime createdAt, boolean isRead, boolean isFlagged) {
            this.id = id; this.questionId = questionId; this.to_user = to_user; this.sender = sender;
            this.messageType = messageType; this.content = content;
            this.createdAt = createdAt; this.isRead = isRead; this.isFlagged = isFlagged;
        }
        /**
         * Gets the message id
         * @return id
         */
        public int getId() { return id; }
        
        /**
         * Gets the questionId
         * @return questionId
         */
        public int getQuestionId() { return questionId; }
        /**
         * Gets the recipient
         * @return to_user
         */
        public String getTo() {return to_user;}
        /**
         * Gets the sender
         * @return sender
         */
        public String getSender() { return sender; }
        
        /**
         * Gets the message type
         * @return messageType
         */
        public String getMessageType() { return messageType; }
        
        /**
         * Gets the content
         * @return content
         */
        public String getContent() { return content; }
        
        /**
         * Gets createdAt
         * @return createdAt
         */
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        
        /**
         * Gets the value of isRead
         * @return isRead
         */
        public boolean isRead() { return isRead; }
        
        /**
         * Gets the value of isFlagged.
         * @return isFlagged.
         */
        public boolean isFlagged() { return isFlagged; }
        /**
         * Sets the value of isFlagged.
         */
        public void setIsFlagged(boolean tf) { this.isFlagged = tf; }
    }
    
    /**
     * Gets a list of questions where parentQuestionId is NULL (Top Level).
     * 
     * @return List of questions
     * @throws SQLException If a database error occurs.
     */
    public List<Question> getQuestionsTopLevel() throws SQLException {
        QuestionFilter f = new QuestionFilter();
        List<Question> results = new ArrayList<>();
        String sql = "SELECT id, title, content, askedBy, createdAt, isResolved, isFlagged, resolvedAnswerId "
                   + "FROM questions WHERE parentQuestionId IS NULL ORDER BY createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Question q = new Question(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("askedBy"),
                    rs.getTimestamp("createdAt").toLocalDateTime(),
                    rs.getBoolean("isResolved"),
                    rs.getBoolean("isFlagged"),
                    rs.getInt("resolvedAnswerId")
                );
                q.setAnswers(getAnswersForQuestion(q.getId()));
                results.add(q);
            }
        }
        return results;
    }

    /**
     * Gets a list of questions that have a parent question (follow up questions).
     * 
     * @param parentQuestionId ID of the parent question.
     * @return List of questions
     * @throws SQLException If a database error occurs.
     */
    public List<Question> getFollowupQuestions(int parentQuestionId) throws SQLException {
        List<Question> results = new ArrayList<>();
        String sql = "SELECT id, title, content, askedBy, createdAt, isResolved, isFlagged, resolvedAnswerId "
                   + "FROM questions WHERE parentQuestionId = ? ORDER BY createdAt ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, parentQuestionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("askedBy"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isResolved"),
                        rs.getBoolean("isFlagged"),
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(q.getId()));
                    results.add(q);
                }
            }
        }
        return results;
    }

    /**
     * Creates a follow up question to another question (Child question to a parent question).
     * 
     * @param parentQuestionId ID of the parent question.
     * @param title Title for the follow up question.
     * @param content Content of the follow up question.
     * @param askedBy Username of the user asking the question.
     * @return Generated keys
     * @throws SQLException If a database error occurs.
     */
    public int createFollowupQuestion(int parentQuestionId, String title, String content, String askedBy) throws SQLException {
        String sql = "INSERT INTO questions (title, content, askedBy, createdAt, isResolved, resolvedAnswerId, parentQuestionId) "
                   + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, FALSE, NULL, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, askedBy);
            ps.setInt(4, parentQuestionId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to insert follow‑up question");
    }

    /**
     * Creates a private feedback message.
     *
     * @param questionId ID of the question the message is about.
     * @param to         Username of the recipient.
     * @param from       Username of the sender.
     * @param messageType Type of private message (e.g., "QUESTION" or "ANSWER").
     * @param content    Content of the private message.
     * @return The generated ID of the private message.
     * @throws SQLException If a database error occurs.
     */
    public int addPrivateMessage(int questionId, String to, String from, String messageType, String content) throws SQLException {
        String sql = "INSERT INTO private_messages (questionId, to_user, from_user, messageType, content, createdAt, isRead) "
                   + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, FALSE)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, questionId);
            ps.setString(2, to);
            ps.setString(3, from);
            ps.setString(4, messageType);
            ps.setString(5, content);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to insert private message");
    }

    /**
     * Gets a list of all private messages for a question.
     *
     * @param questionId ID of the question to get private messages for.
     * @return List of private messages for that question.
     * @throws SQLException If a database error occurs.
     */
    public List<PrivateMessage> getPrivateMessagesForQuestion(int questionId) throws SQLException {
        List<PrivateMessage> out = new ArrayList<>();
        String sql = "SELECT id, questionId, to_user, from_user, messageType, content, createdAt, isRead, isFlagged "
                + "FROM private_messages WHERE questionId = ? ORDER BY createdAt ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new PrivateMessage(
                        rs.getInt("id"),
                        rs.getInt("questionId"),
                        rs.getString("to_user"),
                        rs.getString("from_user"),
                        rs.getString("messageType"),
                        rs.getString("content"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isRead"),
                        rs.getBoolean("isFlagged")
                    ));
                }
            }
        }
        return out;
    }

    /**
     * Gets the number of unread private messages for the asker.
     * 
     * @param questionId ID of the question to get unread count for.
     * @param askerUserName Username of the asker.
     * @return Number of unread private messages.
     * @throws SQLException If a database error occurs.
     */
    public int getUnreadPrivateCountForAsker(int questionId, String askerUserName) throws SQLException {
    	String sql = "SELECT COUNT(*) FROM private_messages WHERE questionId = ? AND isRead = FALSE AND from_user <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ps.setString(2, askerUserName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Marks all unread private messages to the asker as read.
     * 
     * @param questionId ID of the question to mark unread messages as read.
     * @param askerUserName Username of the asker.
     * @return Row count
     * @throws SQLException If a database error occurs.
     */
    public int markPrivateMessagesReadByAsker(int questionId, String askerUserName) throws SQLException {
    	String sql = "UPDATE private_messages SET isRead = TRUE WHERE questionId = ? AND isRead = FALSE AND from_user <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ps.setString(2, askerUserName);
            return ps.executeUpdate();
        }
    }
    
    /**
     * Sets favorites
     * 
     * @return false
     */
    public boolean setFavorites() {
    	
    	return false;
    }

    /**
     * Method to get a users favorited reviewers.
     * 
     * @param username Username of the user to get favorites for.
     * @return String array of the users favorites.
     * @throws SQLException If a database error occurs.
     */
    public String[] getFavorites(String username) throws SQLException {
    	ensureConnected();

        String sql = "SELECT favorites FROM cse360users WHERE userName = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String csv = rs.getString("favorites");
                    if (csv == null || csv.trim().isEmpty()) {
                        return new String[0];               // empty array
                    }
                    // split on commas (trim spaces)
                    String[] parts = csv.split("\\s*,\\s*");
                    return parts;
                }
            }
        }
        return null;   // user does not exist
    }

    /**
     * Method to add a reviewer to a users list of favorites.
     * 
     * @param username Username of the user to add the favorite for.
     * @param favUsername Username of the user to add to favorites
     * @return True or false based on function success.
     * @throws SQLException If a database error occurs.
     */
    public boolean addFavorite(String username, String favUsername) throws SQLException {
        ensureConnected();

        String[] current = getFavorites(username);
        if (current == null) {
            return false;               // user not found
        }

        // ---- check for duplicate ----
        for (String s : current) {
            if (s.equalsIgnoreCase(favUsername)) {
                return true;            // already present
            }
        }

        // ---- grow the array by one element ----
        String[] bigger = new String[current.length + 1];
        System.arraycopy(current, 0, bigger, 0, current.length);
        bigger[current.length] = favUsername;

        // ---- write the new CSV back ----
        String newCsv = String.join(",", bigger);
        String sql = "UPDATE cse360users SET favorites = ? WHERE userName = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newCsv);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Removes a username from the user's favorites list.
     *
     * @param username The user whose favorites to modify.
     * @param favToRemove The favorite username to remove.
     * @return true if the row was updated or the favorite was already absent.
     * @throws SQLException if a database error occurs
     */
    public boolean removeFavorite(String username, String favToRemove) throws SQLException {
        ensureConnected();

        // 1. Get current favorites as array
        String[] current = getFavorites(username);
        if (current == null || current.length == 0) {
            return true; // nothing to remove
        }

        // 2. Count how many entries we will keep
        int keepCount = 0;
        for (String fav : current) {
            if (!fav.equalsIgnoreCase(favToRemove)) {
                keepCount++;
            }
        }

        // 3. If nothing changed → already absent
        if (keepCount == current.length) {
            return true;
        }

        // 4. Build new smaller array
        String[] smaller = new String[keepCount];
        int idx = 0;
        for (String fav : current) {
            if (!fav.equalsIgnoreCase(favToRemove)) {
                smaller[idx++] = fav;
            }
        }

        // 5. Write back
        String newCsv = keepCount == 0 ? null : String.join(",", smaller);

        String sql = "UPDATE cse360users SET favorites = ? WHERE userName = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (newCsv == null) {
                ps.setNull(1, java.sql.Types.VARCHAR);
            } else {
                ps.setString(1, newCsv);
            }
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }
    
 // ========== DIRECT MESSAGING METHODS ==========

    /**
     * Inner class representing a direct message between two users.
     */
    public static class DirectMessage {
        private final int id;
        private final String user1;
        private final String user2;
        private final String fromUser;
        private final String content;
        private final LocalDateTime createdAt;
        private final boolean isRead;
        
        /**
         * Constructor for DirectMessage.
         * 
         * @param id Message ID
         * @param user1 First user (alphabetically)
         * @param user2 Second user (alphabetically)
         * @param fromUser Actual sender
         * @param content Message text
         * @param createdAt Timestamp
         * @param isRead Read status
         */
        public DirectMessage(int id, String user1, String user2, String fromUser, 
                            String content, LocalDateTime createdAt, boolean isRead) {
            this.id = id;
            this.user1 = user1;
            this.user2 = user2;
            this.fromUser = fromUser;
            this.content = content;
            this.createdAt = createdAt;
            this.isRead = isRead;
        }
        
        /**
         * Gets message ID.
         * @return id
         */
        public int getId() { return id; }
        
        /**
         * Gets first user.
         * @return user1
         */
        public String getUser1() { return user1; }
        
        /**
         * Gets second user.
         * @return user2
         */
        public String getUser2() { return user2; }
        
        /**
         * Gets sender.
         * @return fromUser
         */
        public String getFromUser() { return fromUser; }
        
        /**
         * Gets message content.
         * @return content
         */
        public String getContent() { return content; }
        
        /**
         * Gets creation timestamp.
         * @return createdAt
         */
        public LocalDateTime getCreatedAt() { return createdAt; }
        
        /**
         * Gets read status.
         * @return isRead
         */
        public boolean isRead() { return isRead; }
        
        /**
         * Gets the other user in conversation.
         * 
         * @param currentUser Current user's username
         * @return Other user's username
         */
        public String getOtherUser(String currentUser) {
            return currentUser.equalsIgnoreCase(user1) ? user2 : user1;
        }
        
        /**
         * Checks if message was sent by user.
         * 
         * @param username Username to check
         * @return True if sent by this user
         */
        public boolean isSentBy(String username) {
            return fromUser.equalsIgnoreCase(username);
        }
    }

    /**
     * Inner class representing a conversation summary for inbox display.
     */
    public static class ConversationSummary {
        private final String otherUser;
        private final String lastMessageContent;
        private final LocalDateTime lastMessageAt;
        private final int unreadCount;
        
        /**
         * Constructor for ConversationSummary.
         * 
         * @param otherUser Other participant
         * @param lastMessageContent Last message preview
         * @param lastMessageAt Last message time
         * @param unreadCount Unread message count
         */
        public ConversationSummary(String otherUser, String lastMessageContent, 
                                   LocalDateTime lastMessageAt, int unreadCount) {
            this.otherUser = otherUser;
            this.lastMessageContent = lastMessageContent;
            this.lastMessageAt = lastMessageAt;
            this.unreadCount = unreadCount;
        }
        
        /**
         * Gets other user.
         * @return otherUser
         */
        public String getOtherUser() { return otherUser; }
        
        /**
         * Gets last message preview.
         * @return lastMessageContent
         */
        public String getLastMessageContent() { return lastMessageContent; }
        
        /**
         * Gets last message time.
         * @return lastMessageAt
         */
        public LocalDateTime getLastMessageAt() { return lastMessageAt; }
        
        /**
         * Gets unread count.
         * @return unreadCount
         */
        public int getUnreadCount() { return unreadCount; }
    }

    // ========== CREATE (C in CRUD) ==========

    /**
     * Sends a direct message between two users.
     * 
     * @param fromUser Sender username
     * @param toUser Recipient username
     * @param content Message text
     * @return Generated message ID
     * @throws SQLException If database error occurs
     */
    public int sendDirectMessage(String fromUser, String toUser, String content) throws SQLException {
        ensureConnected();
        
        // Order users alphabetically
        String user1 = fromUser.compareTo(toUser) < 0 ? fromUser : toUser;
        String user2 = fromUser.compareTo(toUser) < 0 ? toUser : fromUser;
        
        String sql = "INSERT INTO direct_messages (user1, user2, fromUser, content, createdAt, isRead) "
                   + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, FALSE)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.setString(3, fromUser);
            ps.setString(4, content);
            ps.executeUpdate();
            
            updateConversationSummary(user1, user2, content);
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to send direct message");
    }

    // ========== READ (R in CRUD) ==========

    /**
     * Gets all messages in a conversation between two users.
     * 
     * @param currentUser Current user's username
     * @param otherUser Other user's username
     * @return List of messages (oldest first)
     * @throws SQLException If database error occurs
     */
    public List<DirectMessage> getConversation(String currentUser, String otherUser) throws SQLException {
        ensureConnected();
        
        String user1 = currentUser.compareTo(otherUser) < 0 ? currentUser : otherUser;
        String user2 = currentUser.compareTo(otherUser) < 0 ? otherUser : currentUser;
        
        List<DirectMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM direct_messages WHERE user1 = ? AND user2 = ? ORDER BY createdAt ASC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(new DirectMessage(
                        rs.getInt("id"),
                        rs.getString("user1"),
                        rs.getString("user2"),
                        rs.getString("fromUser"),
                        rs.getString("content"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isRead")
                    ));
                }
            }
        }
        return messages;
    }

    /**
     * Gets conversation list for user's inbox.
     * 
     * @param username Current user's username
     * @return List of conversation summaries (most recent first)
     * @throws SQLException If database error occurs
     */
    public List<ConversationSummary> getConversationList(String username) throws SQLException {
        ensureConnected();
        
        List<ConversationSummary> summaries = new ArrayList<>();
        String sql = "SELECT c.user1, c.user2, c.lastMessageContent, c.lastMessageAt, "
                   + "(SELECT COUNT(*) FROM direct_messages dm "
                   + " WHERE ((dm.user1 = c.user1 AND dm.user2 = c.user2) "
                   + "    OR (dm.user1 = c.user2 AND dm.user2 = c.user1)) "
                   + "   AND dm.fromUser != ? AND dm.isRead = FALSE) AS unreadCount "
                   + "FROM conversations c "
                   + "WHERE c.user1 = ? OR c.user2 = ? "
                   + "ORDER BY c.lastMessageAt DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String user1 = rs.getString("user1");
                    String user2 = rs.getString("user2");
                    String otherUser = username.equalsIgnoreCase(user1) ? user2 : user1;
                    
                    summaries.add(new ConversationSummary(
                        otherUser,
                        rs.getString("lastMessageContent"),
                        rs.getTimestamp("lastMessageAt").toLocalDateTime(),
                        rs.getInt("unreadCount")
                    ));
                }
            }
        }
        return summaries;
    }

    /**
     * Searches for users by username (for autocomplete).
     * Filters by role-based messaging permissions.
     * 
     * @param searchTerm Partial username to search
     * @param currentUser Current user's username
     * @param currentUserRole Current user's role
     * @return List of matching usernames (max 10)
     * @throws SQLException If database error occurs
     */
    public List<String> searchUsers(String searchTerm, String currentUser, String currentUserRole) throws SQLException {
        ensureConnected();
        
        List<String> users = new ArrayList<>();
        String roleFilter = buildRoleFilter(currentUserRole);
        
        String sql = "SELECT userName FROM cse360users "
                   + "WHERE userName != ? "
                   + "AND LOWER(userName) LIKE ? "
                   + roleFilter
                   + "ORDER BY userName ASC LIMIT 10";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, currentUser);
            ps.setString(2, "%" + searchTerm.toLowerCase() + "%");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(rs.getString("userName"));
                }
            }
        }
        return users;
    }

    /**
     * Gets total unread message count for a user.
     * 
     * @param username User's username
     * @return Total unread count
     * @throws SQLException If database error occurs
     */
    public int getTotalUnreadCount(String username) throws SQLException {
        ensureConnected();
        
        String sql = "SELECT COUNT(*) FROM direct_messages "
                   + "WHERE (user1 = ? OR user2 = ?) AND fromUser != ? AND isRead = FALSE";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // ========== UPDATE (U in CRUD) ==========

    /**
     * Marks all messages in a conversation as read.
     * 
     * @param currentUser Current user's username
     * @param otherUser Other user's username
     * @return Number of messages marked as read
     * @throws SQLException If database error occurs
     */
    public int markConversationAsRead(String currentUser, String otherUser) throws SQLException {
        ensureConnected();
        
        String user1 = currentUser.compareTo(otherUser) < 0 ? currentUser : otherUser;
        String user2 = currentUser.compareTo(otherUser) < 0 ? otherUser : currentUser;
        
        String sql = "UPDATE direct_messages SET isRead = TRUE "
                   + "WHERE user1 = ? AND user2 = ? AND fromUser = ? AND isRead = FALSE";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.setString(3, otherUser);
            return ps.executeUpdate();
        }
    }

    /**
     * Updates a direct message's content.
     * Only the sender can edit their own messages.
     * 
     * @param messageId Message ID to update
     * @param newContent New message content
     * @param username Username requesting edit (must be sender)
     * @return True if updated successfully
     * @throws SQLException If database error occurs
     */
    public boolean updateDirectMessage(int messageId, String newContent, String username) throws SQLException {
        ensureConnected();
        
        String sql = "UPDATE direct_messages SET content = ? WHERE id = ? AND fromUser = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newContent);
            ps.setInt(2, messageId);
            ps.setString(3, username);
            return ps.executeUpdate() > 0;
        }
    }

    // ========== DELETE (D in CRUD) ==========

    /**
     * Deletes a direct message.
     * Only the sender can delete their own messages.
     * 
     * @param messageId Message ID to delete
     * @param username Username requesting deletion (must be sender)
     * @return True if deleted successfully
     * @throws SQLException If database error occurs
     */
    public boolean deleteDirectMessage(int messageId, String username) throws SQLException {
        ensureConnected();
        
        String sql = "DELETE FROM direct_messages WHERE id = ? AND fromUser = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Builds SQL role filter for messaging permissions.
     * 
     * @param userRole User's role
     * @return SQL WHERE clause fragment
     */
    private String buildRoleFilter(String userRole) {
        if (userRole == null) return "";
        
        String role = userRole.toLowerCase();
        
        if (role.equals("admin")) {
            return "";
        } else if (role.equals("staff") || role.equals("instructor")) {
            return "AND (role IN ('Student', 'Reviewer', 'Staff', 'Instructor'))";
        } else if (role.equals("student") || role.equals("reviewer")) {
            return "AND (role IN ('Student', 'Reviewer'))";
        } else {
            return "AND (role IN ('Student', 'Reviewer'))";
        }
    }

    /**
     * Updates conversation summary after sending a message.
     * Internal helper method.
     * 
     * @param user1 First user
     * @param user2 Second user
     * @param content Message content
     * @throws SQLException If database error occurs
     */
    private void updateConversationSummary(String user1, String user2, String content) throws SQLException {
        String preview = content.length() > 100 ? content.substring(0, 97) + "..." : content;
        
        String sql = "MERGE INTO conversations (user1, user2, lastMessageAt, lastMessageContent) "
                   + "KEY (user1, user2) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.setString(3, preview);
            ps.executeUpdate();
        }
    }

    /**
     * Closes database connection
     */
    public void closeConnection() {
        try { 
            if (statement != null) statement.close(); 
        } catch (SQLException se2) { 
            se2.printStackTrace();
        } 
        try { 
            if (connection != null) connection.close(); 
        } catch (SQLException se) { 
            se.printStackTrace(); 
        } 
    }

    /**
     * Truncates the database
     */
    public void truncate() {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement(); 
            statement.execute("DROP ALL OBJECTS");
            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

}

