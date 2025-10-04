package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import application.Question;  
import application.Answer;    

import application.User;
import javafx.application.Platform;
import javafx.scene.control.Alert;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			try {
				connection = DriverManager.getConnection(DB_URL, USER, PASS);
				statement = connection.createStatement(); 
			} catch (SQLException e) {
			    Platform.runLater(() ->
			        new Alert(Alert.AlertType.ERROR,
			            "Couldn’t open the database.\nClose any other running copy and try again.\n\n" + e.getMessage()
			        ).showAndWait()
			    );
			    return; // bail before building the rest of the UI
			}
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			 createTables();  // Create the necessary tables if they don't exist
		        updateDatabaseSchema();  // Update schema for existing databases
			
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
	    String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "userName VARCHAR(20) UNIQUE, "
	            + "email VARCHAR(255), "
	            + "firstName VARCHAR(20), "
	            + "middleInitial VARCHAR(1), "  // Add middle initial field (1 character max)
	            + "lastName VARCHAR(20), "
	            + "password VARCHAR(20), "
	            + "otp VARCHAR(16), "
	            + "role VARCHAR(20))";
	    statement.execute(userTable);
	    
	    createQATables();  // Create Q&A system tables
	    // Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE, "
	            + "expiresAt TIMESTAMP)";
	    statement.execute(invitationCodesTable);
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
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
	
	// Also add a method to update user email
	public boolean updateUserEmail(String username, String newEmail) throws SQLException {
	    String sql = "UPDATE cse360users SET email = ? WHERE userName = ?";  // Should already be correct
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

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
	    String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getRole());
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                // Set email and middle initial from database
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
	
	// Check if any admin exists in the database
	public boolean doesAdminExist() throws SQLException {
	    String query = "SELECT COUNT(*) AS count FROM cse360users WHERE role = 'admin'";
	    ResultSet resultSet = statement.executeQuery(query);
	    if (resultSet.next()) {
	        return resultSet.getInt("count") > 0;
	    }
	    return false;
	}
	
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Lists the users in the database
	public List<User> getAllUsers() throws SQLException {
	    List<User> users = new ArrayList<>();
	    String sql = "SELECT username, email, middleInitial, role, password FROM cse360users ORDER BY role";
	    
	    try (PreparedStatement ps = connection.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        
	        while (rs.next()) {
	            String username = rs.getString("username");
	            String email = rs.getString("email");
	            String middleInitial = rs.getString("middleInitial");
	            String role = rs.getString("role");
	            String passwordHash = rs.getString("password");

	            User user = new User(username, passwordHash, role);
	            user.setEmail(email);
	            user.setMiddleInitial(middleInitial);
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
	            // Ensure it's only 1 character
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

	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	public boolean deleteUser(String username) throws SQLException {
	    // Check if this is the last admin
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
	
	// Count for total users
	public int getUserCount() throws SQLException {
	    String sql = "SELECT COUNT(*) AS count FROM cse360users";  // Changed from "users" to "cse360users"
	    
	    try (PreparedStatement ps = connection.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        
	        if (rs.next()) {
	            return rs.getInt("count");
	        }
	    }
	    return 0;
	}
	
	// Single user lookup via userName
	public Optional<User> getUserByUsername(String username) throws SQLException {
	    String sql = "SELECT username, role, password FROM cse360users WHERE username = ?";  // Changed from "users" to "cse360users"
	    
	    try (PreparedStatement ps = connection.prepareStatement(sql)) {
	        ps.setString(1, username);
	        
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                String role = rs.getString("role");
	                String passwordHash = rs.getString("password");
	                User user = new User(username, passwordHash, role);
	                // Try to get email if it exists
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
	
	// Update user's password
	public boolean updateUserPassword(String username, String newPassword) throws SQLException {
	    String sql = "UPDATE cse360users SET password = ? WHERE userName = ?";  // Changed from "users" to "cse360users"
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setString(1, newPassword);
	        pstmt.setString(2, username);
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	    }
	}
	
	// --- Admin issued One-Time Password  ---

	// Existing behavior (no expiration)
	public boolean setOtp(String username, String otp) throws SQLException {
	    String sql = "UPDATE cse360users SET otp = ?, otpIsUsed = FALSE, otpExpiresAt = NULL WHERE userName = ?";
	    try (PreparedStatement ps = connection.prepareStatement(sql)) {
	        ps.setString(1, otp);
	        ps.setString(2, username);
	        return ps.executeUpdate() > 0;
	    }
	}

	// Set OTP with TTL (minutes). We compute the expiry in Java for portability.
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
	// Validate OTP: must match value, not used, and not expired
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

	// Consume OTP after successful password change
	public boolean consumeOtp(String username) throws SQLException {
	    String q = "UPDATE cse360users SET otp = NULL, otpIsUsed = TRUE, otpExpiresAt = NULL WHERE userName = ?";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, username);
	        return ps.executeUpdate() > 0;
	    }
	}

	// Optional: clear any expired OTPs (housekeeping, not required for flow)
	public int purgeExpiredOtps() throws SQLException {
	    String q = "UPDATE cse360users SET otp = NULL, otpIsUsed = TRUE WHERE otpExpiresAt IS NOT NULL AND otpExpiresAt <= CURRENT_TIMESTAMP";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        return ps.executeUpdate();
	    }
	}
	
	public boolean resetUserPassword(String username, String newPassword) throws SQLException {
	    String sql = "UPDATE cse360users SET password = ? WHERE userName = ?";  // Changed from "users" to "cse360users"
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setString(1, newPassword);
	        pstmt.setString(2, username);
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;
	    }
	}
	
	// Retrieves the role of a user from the database using their UserName.
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
	
	// --- Invitation codes with expiration ---

	// Backwards-compatible (no expiry)
	public String generateInvitationCode() {
	    return generateInvitationCode(0); // 0 = no expiry (NULL)
	}

	// Generate with TTL (minutes); returns the code
	public String generateInvitationCode(int ttlMinutes) {
	    String code = java.util.UUID.randomUUID().toString().substring(0, 4); // 4-char style
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

	// Validate: must be unused and not expired
	public boolean validateInvitationCode(String code) {
	    String q = "SELECT 1 FROM InvitationCodes " +
	               "WHERE code = ? AND isUsed = FALSE " +
	               "AND (expiresAt IS NULL OR expiresAt > CURRENT_TIMESTAMP)";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, code);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                markInvitationCodeAsUsed(code); // keep your existing behavior
	                return true;
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	// Optional: housekeeping
	public int purgeExpiredInvitationCodes() throws SQLException {
	    String q = "UPDATE InvitationCodes SET isUsed = TRUE WHERE expiresAt IS NOT NULL AND expiresAt <= CURRENT_TIMESTAMP AND isUsed = FALSE";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        return ps.executeUpdate();
	    }
	}

	
	// Marks the invitation code as used in the database.
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
	        // Use metadata to detect columns one-by-one
	        DatabaseMetaData meta = connection.getMetaData();

	        // EMAIL
	        try (ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "EMAIL")) {
	            if (!rs.next()) {
	                System.out.println("Adding email column to database...");
	                statement.execute("ALTER TABLE cse360users ADD COLUMN email VARCHAR(255)");
	                System.out.println("Email column added successfully!");
	            }
	        }

	        // MIDDLEINITIAL
	        try (ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "MIDDLEINITIAL")) {
	            if (!rs.next()) {
	                System.out.println("Adding middleInitial column to database...");
	                statement.execute("ALTER TABLE cse360users ADD COLUMN middleInitial VARCHAR(1)");
	                System.out.println("Middle Initial column added successfully!");
	            }
	        }

	        // otpIsUsed (boolean flag)
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

	        // otpExpiresAt (timestamp)
	        try (ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "OTPEXPIRESAT")) {
	            if (!rs.next()) {
	                System.out.println("Adding otpExpiresAt column to database...");
	                statement.execute("ALTER TABLE cse360users ADD COLUMN otpExpiresAt TIMESTAMP");
	                System.out.println("otpExpiresAt column added successfully!");
	            }
	        }

	        // InvitationCodes.expiresAt
	        try (ResultSet rs = meta.getColumns(null, null, "INVITATIONCODES", "EXPIRESAT")) {
	            if (!rs.next()) {
	                System.out.println("Adding expiresAt column to InvitationCodes...");
	                statement.execute("ALTER TABLE InvitationCodes ADD COLUMN expiresAt TIMESTAMP");
	                System.out.println("expiresAt column added successfully!");
	            }
	        }


	    } catch (SQLException e) {
	        System.out.println("Note: Could not add columns — they may already exist: " + e.getMessage());
	    }
	}
	
	// ============ Q&A System Database Methods ============


	/**

	 * Creates tables for the Q&A system if they don't exist

	 */

	private void createQATables() throws SQLException {

	    // Questions table

	    String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "title VARCHAR(100) NOT NULL, "  // CHANGED FROM 200
	            + "content VARCHAR(500) NOT NULL, "  // CHANGED FROM 2000
	            + "askedBy VARCHAR(20) NOT NULL, "
	            + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "isResolved BOOLEAN DEFAULT FALSE, "
	            + "resolvedAnswerId INT DEFAULT -1, "
	            + "FOREIGN KEY (askedBy) REFERENCES cse360users(userName))";
	    statement.execute(questionsTable);

	    

	    // Answers table

	    String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "questionId INT NOT NULL, "
	            + "content VARCHAR(500) NOT NULL, "  // CHANGED FROM 2000
	            + "answeredBy VARCHAR(20) NOT NULL, "
	            + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "isRead BOOLEAN DEFAULT FALSE, "
	            + "upvotes INT DEFAULT 0, "
	            + "FOREIGN KEY (questionId) REFERENCES questions(id) ON DELETE CASCADE, "
	            + "FOREIGN KEY (answeredBy) REFERENCES cse360users(userName))";
	    statement.execute(answersTable);

	}


	// Update the existing createTables() method to call createQATables()

	// Add this line inside createTables():

//	     createQATables();


	// ============ CRUD Operations for Questions ============


	/**

	 * Creates a new question in the database

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

	 * Retrieves a single question by ID

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

	                    rs.getInt("resolvedAnswerId")

	                );

	                // Load answers for this question

	                q.setAnswers(getAnswersForQuestion(id));

	                return q;

	            }

	        }

	    }

	    return null;

	}


	/**

	 * Retrieves all questions (optionally filtered by user)

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

	                    rs.getInt("resolvedAnswerId")

	                );

	                // Load answers for each question

	                q.setAnswers(getAnswersForQuestion(q.getId()));

	                questions.add(q);

	            }

	        }

	    }

	    return questions;

	}


	/**

	 * Retrieves only unresolved questions

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

	 * Searches for questions containing keywords

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

	 * Updates an existing question

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

	 * Deletes a question (and its answers via CASCADE)

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

	 * Marks a question as resolved with a specific answer

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


	// ============ CRUD Operations for Answers ============


	/**

	 * Creates a new answer for a question

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

	 * Retrieves all answers for a specific question

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

	                    rs.getInt("upvotes")

	                );

	                answers.add(a);

	            }

	        }

	    }

	    return answers;

	}


	/**

	 * Updates an existing answer

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

	 * Deletes an answer

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

	 * Marks an answer as read

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

	 * Increments upvotes for an answer

	 */

	public boolean upvoteAnswer(int answerId) throws SQLException {

	    String sql = "UPDATE answers SET upvotes = upvotes + 1 WHERE id = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

	        pstmt.setInt(1, answerId);

	        

	        int rowsAffected = pstmt.executeUpdate();

	        return rowsAffected > 0;

	    }

	}

	/**
	 * Closes a question (marks as resolved without specifying an answer)
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

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	public void truncate() {
		// Clears and rebuilds databases. Fresh start. 
		try {
		Class.forName(JDBC_DRIVER);
		System.out.println("Connecting to database...");
		connection = DriverManager.getConnection(DB_URL, USER, PASS);
		statement = connection.createStatement(); 
		statement.execute("DROP ALL OBJECTS");
		createTables();
		}catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
		}
	}

}
