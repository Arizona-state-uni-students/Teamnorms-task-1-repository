package application;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
    private String userName;
    private String password;
    private String role;

    // Constructor to initialize a new User object with userName, password, and role.
    public User( String userName, String password, String role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
    }
    
    // Sets the role of the user.
    public void setRole(String role) {
    	this.role=role;
    }
    
    // Sets the password of the user
    public void setPassword(String password) {
    	this.password=password;
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

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}
