package application;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, role, and other attributes.
 */
public class User {
    private String userName;
    private String email;
    private String middleInitial;
    private String password;
    private String role;
    private int privileges = 99; // Default privileges (for admin)
    private String firstName;
    private String lastName;
    private int weight;

    // Constructor for minimal user creation
    public User(String userName, String password, String role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.email = "";
        this.middleInitial = "";
        this.firstName = "";
        this.lastName = "";
        this.weight = 0;
    }

    // Constructor for full user creation
    public User(String userName, String email, String password, String role, 
                String firstName, String lastName, String middleInitial, int weight) {
        this.userName = userName;
        this.email = email != null ? email : "";
        this.password = password;
        this.role = role;
        this.firstName = firstName != null ? firstName : "";
        this.lastName = lastName != null ? lastName : "";
        this.middleInitial = middleInitial != null ? middleInitial : "";
        this.weight = weight;
    }

    // Getters and setters
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
        this.email = email != null ? email : "";
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial != null ? middleInitial : "";
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

    public void setPrivileges(int privileges) {
        this.privileges = privileges;
    }

    public int getPrivileges() {
        switch (role) {
            case "user":
                return 0;
            case "student":
                return 1;
            case "reviewer":
                return 2;
            case "instructor":
                return 3;
            case "staff":
                return 4;
            case "admin":
                return 99;
            default:
                return 0;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName : "";
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName : "";
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}