package databasePart1;
import java.sql.*;
import java.util.*;
import application.Question;  
import application.Answer;    
import application.User;
import javafx.application.Platform;
import javafx.scene.control.Alert;
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
    
    private void ensureConnected() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connectToDatabase();
        }
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Database connection could not be established.");
        }
    }

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
                + "role VARCHAR(20))";
        statement.execute(userTable);
        createQATables();
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE, "
                + "expiresAt TIMESTAMP)";
        statement.execute(invitationCodesTable);
    }

    public boolean isDatabaseEmpty() throws SQLException {
        ensureConnected();
        final String sql = "SELECT COUNT(*) AS count FROM cse360users";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("count") == 0;
        }
        return true;
    }

    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, email, middleInitial, password, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(2, user.getEmail());
            }
            if (user.getMiddleInitial() == null || user.getMiddleInitial().trim().isEmpty()) {
                pstmt.setNull(3, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(3, user.getMiddleInitial().toUpperCase());
            }
            pstmt.setString(4, user.getPassword());
            pstmt.setString(5, user.getRole());
            pstmt.executeUpdate();
        }
    }

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

    public boolean login(User user) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    try {
                        user.setEmail(rs.getString("email"));
                        user.setMiddleInitial(rs.getString("middleInitial"));
                    } catch (SQLException e) {
                        // Columns might not exist
                    }
                    return true;
                }
                return false;
            }
        }
    }

    public boolean doesAdminExist() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users WHERE role = 'admin'";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") > 0;
        }
        return false;
    }

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

    public boolean deleteUser(String username) throws SQLException {
        if (isLastAdmin(username)) {
            System.err.println("Cannot delete the last admin user!");
            return false;
        }
        String sql = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private boolean isLastAdmin(String username) throws SQLException {
        String roleQuery = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(roleQuery)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && !"admin".equals(rs.getString("role"))) {
                return false;
            }
        }
        String countQuery = "SELECT COUNT(*) AS count FROM cse360users WHERE role = 'admin'";
        try (PreparedStatement pstmt = connection.prepareStatement(countQuery)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") <= 1;
            }
        }
        return false;
    }

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

    public boolean updateUserPassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean setOtp(String username, String otp) throws SQLException {
        String sql = "UPDATE cse360users SET otp = ?, otpIsUsed = FALSE, otpExpiresAt = NULL WHERE userName = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

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

    public boolean consumeOtp(String username) throws SQLException {
        String q = "UPDATE cse360users SET otp = NULL, otpIsUsed = TRUE, otpExpiresAt = NULL WHERE userName = ?";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        }
    }

    public int purgeExpiredOtps() throws SQLException {
        String q = "UPDATE cse360users SET otp = NULL, otpIsUsed = TRUE WHERE otpExpiresAt IS NOT NULL AND otpExpiresAt <= CURRENT_TIMESTAMP";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            return ps.executeUpdate();
        }
    }

    public boolean resetUserPassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean updateUserRole(String username, String newRole) throws SQLException {
        if (isLastAdmin(username) && !"admin".equals(newRole)) {
            System.err.println("Cannot remove admin role from the last admin!");
            return false;
        }
        String sql = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public String generateInvitationCode() {
        return generateInvitationCode(0);
    }

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

    public int purgeExpiredInvitationCodes() throws SQLException {
        String q = "UPDATE InvitationCodes SET isUsed = TRUE WHERE expiresAt IS NOT NULL AND expiresAt <= CURRENT_TIMESTAMP AND isUsed = FALSE";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            return ps.executeUpdate();
        }
    }

    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    private void createQATables() throws SQLException {
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "title VARCHAR(100) NOT NULL, "
                + "content VARCHAR(500) NOT NULL, "
                + "askedBy VARCHAR(20) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "isResolved BOOLEAN DEFAULT FALSE, "
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
                + "upvotes INT DEFAULT 0, "
                + "FOREIGN KEY (questionId) REFERENCES questions(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (answeredBy) REFERENCES cse360users(userName))";
        statement.execute(answersTable);
        String privateMessagesTable = "CREATE TABLE IF NOT EXISTS private_messages ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "questionId INT NOT NULL, "
                + "sender VARCHAR(20) NOT NULL, "
                + "messageType VARCHAR(10) NOT NULL, "   // 'QUESTION' or 'ANSWER'
                + "content VARCHAR(500) NOT NULL, "
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "isRead BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (questionId) REFERENCES questions(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (sender) REFERENCES cse360users(userName))";
        statement.execute(privateMessagesTable);
    }

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
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(id));
                    return q;
                }
            }
        }
        return null;
    }

    
    
    // Get all questions restricted by user
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
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }
    
    // Filters for future implementation
    public static class QuestionFilter {
        private String askedBy;                 // optional
        private Boolean isResolved;             // optional
        private LocalDateTime createdAfter;     // optional
        private LocalDateTime createdBefore;    // optional

        // --- Getters ---
        public String getAskedBy() { return askedBy; }
        public Boolean getIsResolved() { return isResolved; }
        public LocalDateTime getCreatedAfter() { return createdAfter; }
        public LocalDateTime getCreatedBefore() { return createdBefore; }

        // --- Setters ---
        public void setAskedBy(String askedBy) { this.askedBy = askedBy; }
        public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
        public void setCreatedAfter(LocalDateTime createdAfter) { this.createdAfter = createdAfter; }
        public void setCreatedBefore(LocalDateTime createdBefore) { this.createdBefore = createdBefore; }
    }

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
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

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
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

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

    public boolean deleteQuestion(int questionId, String username) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ? AND askedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

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
                        rs.getInt("upvotes")
                    );
                    answers.add(a);
                }
            }
        }
        return answers;
    }

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

    public boolean deleteAnswer(int answerId, String username) throws SQLException {
        String sql = "DELETE FROM answers WHERE id = ? AND answeredBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean markAnswerAsRead(int answerId) throws SQLException {
        String sql = "UPDATE answers SET isRead = TRUE WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }


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
    
    public boolean closeQuestion(int questionId, String username) throws SQLException {
        String sql = "UPDATE questions SET isResolved = TRUE WHERE id = ? AND askedBy = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    public static class PrivateMessage {
        private final int id;
        private final int questionId;
        private final String sender;
        private final String messageType; // 'QUESTION' or 'ANSWER'
        private final String content;
        private final java.time.LocalDateTime createdAt;
        private final boolean isRead;

        public PrivateMessage(int id, int questionId, String sender, String messageType, String content,
                              java.time.LocalDateTime createdAt, boolean isRead) {
            this.id = id; this.questionId = questionId; this.sender = sender;
            this.messageType = messageType; this.content = content;
            this.createdAt = createdAt; this.isRead = isRead;
        }
        public int getId() { return id; }
        public int getQuestionId() { return questionId; }
        public String getSender() { return sender; }
        public String getMessageType() { return messageType; }
        public String getContent() { return content; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public boolean isRead() { return isRead; }
    }
    
    /** Top‑level questions (parentQuestionId IS NULL), newest first. */
    public List<Question> getQuestionsTopLevel() throws SQLException {
        QuestionFilter f = new QuestionFilter();
        List<Question> results = new ArrayList<>();
        String sql = "SELECT id, title, content, askedBy, createdAt, isResolved, resolvedAnswerId "
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
                    rs.getInt("resolvedAnswerId")
                );
                q.setAnswers(getAnswersForQuestion(q.getId()));
                results.add(q);
            }
        }
        return results;
    }

    /** Follow‑up questions that reference a parent question. */
    public List<Question> getFollowupQuestions(int parentQuestionId) throws SQLException {
        List<Question> results = new ArrayList<>();
        String sql = "SELECT id, title, content, askedBy, createdAt, isResolved, resolvedAnswerId "
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
                        rs.getInt("resolvedAnswerId")
                    );
                    q.setAnswers(getAnswersForQuestion(q.getId()));
                    results.add(q);
                }
            }
        }
        return results;
    }

    /** Create a revised child question under a parent question. */
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

    /** Add a private feedback message. messageType ∈ {'QUESTION','ANSWER'} */
    public int addPrivateMessage(int questionId, String sender, String messageType, String content) throws SQLException {
        String sql = "INSERT INTO private_messages (questionId, sender, messageType, content, createdAt, isRead) "
                   + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, FALSE)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, questionId);
            ps.setString(2, sender);
            ps.setString(3, messageType);
            ps.setString(4, content);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to insert private message");
    }

    /** All private messages for a question (visibility filtered in UI). */
    public List<PrivateMessage> getPrivateMessagesForQuestion(int questionId) throws SQLException {
        List<PrivateMessage> out = new ArrayList<>();
        String sql = "SELECT id, questionId, sender, messageType, content, createdAt, isRead "
                   + "FROM private_messages WHERE questionId = ? ORDER BY createdAt ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new PrivateMessage(
                        rs.getInt("id"),
                        rs.getInt("questionId"),
                        rs.getString("sender"),
                        rs.getString("messageType"),
                        rs.getString("content"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("isRead")
                    ));
                }
            }
        }
        return out;
    }

    /** Count unread private messages for the asker (messages not sent by the asker). */
    public int getUnreadPrivateCountForAsker(int questionId, String askerUserName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM private_messages WHERE questionId = ? AND isRead = FALSE AND sender <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ps.setString(2, askerUserName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /** Mark all unread private messages to the asker as read. */
    public int markPrivateMessagesReadByAsker(int questionId, String askerUserName) throws SQLException {
        String sql = "UPDATE private_messages SET isRead = TRUE WHERE questionId = ? AND isRead = FALSE AND sender <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ps.setString(2, askerUserName);
            return ps.executeUpdate();
        }
    }

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
