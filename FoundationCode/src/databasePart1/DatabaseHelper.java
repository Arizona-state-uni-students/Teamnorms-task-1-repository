package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import application.User;


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
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			 createTables();  // Create the necessary tables if they don't exist
		        updateDatabaseSchema();  // Update schema for existing databases
			
			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
	    String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "userName VARCHAR(255) UNIQUE, "
	            + "email VARCHAR(255), "  // Add email field
	            + "password VARCHAR(255), "
	            + "role VARCHAR(20))";
	    statement.execute(userTable);
	    
	    // Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
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
	    String insertUser = "INSERT INTO cse360users (userName, email, password, role) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
	        pstmt.setString(1, user.getUserName());
	        // Handle null or empty email
	        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
	            pstmt.setNull(2, java.sql.Types.VARCHAR);
	        } else {
	            pstmt.setString(2, user.getEmail());
	        }
	        pstmt.setString(3, user.getPassword());
	        pstmt.setString(4, user.getRole());
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
	                // Set the email from database
	                user.setEmail(rs.getString("email"));
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
	    String sql = "SELECT username, email, role, password FROM cse360users ORDER BY role";
	    
	    try (PreparedStatement ps = connection.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        
	        while (rs.next()) {
	            String username = rs.getString("username");
	            String email = rs.getString("email");
	            String role = rs.getString("role");
	            String passwordHash = rs.getString("password");

	            User user = new User(username, passwordHash, role);
	            user.setEmail(email);
	            users.add(user);
	        }
	    }
	    return users;
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
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
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
	        // Check if email column exists
	        DatabaseMetaData meta = connection.getMetaData();
	        ResultSet rs = meta.getColumns(null, null, "CSE360USERS", "EMAIL");
	        
	        if (!rs.next()) {
	            // Email column doesn't exist, add it
	            System.out.println("Adding email column to database...");
	            String alterTable = "ALTER TABLE cse360users ADD COLUMN email VARCHAR(255)";
	            statement.execute(alterTable);
	            System.out.println("Email column added successfully!");
	        }
	        rs.close();
	    } catch (SQLException e) {
	        System.out.println("Note: Could not add email column - it may already exist");
	        // Not critical if it fails - might already exist
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
