package application;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
    private String userName;
    private String password;
    private String role;
    private String email;

    // Constructor to initialize a new User object with userName, password, and role.
    public User(String userName, String password, String role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.email = ""; // Default empty email
    }
    
    public User(String userName, String email, String password, String role) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public int getPrivileges() {
    	int privilege=0;
    	switch(getRole()) {
    		case "user":
    			privilege = 0;
    			break;
	    	case "student":
	    		privilege = 1;
	    		break;
	    	case "reviewer":
	    		privilege = 2;
	    		break;
	    	case "instructor":
	    		privilege = 3;
	    		break;
	    	case "staff":
	    		privilege = 4;
	    		break;
	    	case "admin":
	    		privilege = 99;
	    		break;
	    	default:
	    		privilege = 0;
    	}
    	return privilege;
    }

}
