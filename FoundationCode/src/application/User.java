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
    private boolean isReviewerApplicant;
    private String userFavorites;

    /**
     * Constructor for minimal user creation
     * 
     * @param userName String to set userName to.
     * @param password String to set password to.
     * @param role String of the user role.
     */
    public User(String userName, String password, String role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.email = "";
        this.middleInitial = "";
        this.firstName = "";
        this.lastName = "";
        this.weight = 0;
        this.userFavorites = "";
    }
    
    /**
     * Constructor for user list creation.
     * 
     * @param userName String to set userName to.
     * @param role String of the user role.
     * @param email String to set email to.
     * @param firstName String to set firstName to.
     * @param lastName String to set lastName to.
     * @param weight int representing user privilege weight.
     * @param reviewerApplicant boolean representing if the user has a pending reviewer application.
     */
    public User(String userName, String role, String email, String firstname, String lastname, int weight, boolean reviewerApplicant, String userFavorites) {
        this.userName = userName;
        this.role = role;
        this.email = email;
        this.firstName = firstname;
        this.lastName = lastname;
        this.weight = weight;
        this.isReviewerApplicant = reviewerApplicant;
        this.userFavorites = userFavorites;
    }

    /**
     * Constructor for full user creation.
     * 
     * @param userName String to set userName to.
     * @param email String to set email to.
     * @param password String to set password to.
     * @param role String of the user role.
     * @param firstName String to set firstName to.
     * @param lastName String to set lastName to.
     * @param middleInitial String to set middleInitial to.
     * @param weight int representing user privilege weight.
     */
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
    /**
     * Gets the user's userName.
     * 
     * @return userName
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Sets the user's userName.
     * 
     * @param userName String of what to set userName to.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the user's email.
     * 
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email.
     * 
     * @param email String of what to set email to.
     */
    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    /**
     * Gets the user's middle initial.
     * 
     * @return middleInitial
     */
    public String getMiddleInitial() {
        return middleInitial.toUpperCase();
    }

    /**
     * Sets the user's middle initial.
     * 
     * @param middleInitial String of what to set middleInitial to.
     */
    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial != null ? middleInitial : "";
    }

    /**
     * Gets the user's password.
     * 
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     * 
     * @param password String of what to set password to.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's role.
     * 
     * @return role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     * 
     * @param role String of what to set role to.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Sets the user's privileges.
     * 
     * @param privileges int representing user privilege weight.
     */
    public void setPrivileges(int privileges) {
        this.privileges = privileges;
    }
    
    /**
     * Get the weight of the users permissions from the 'role' description.
     * 
     * @return int representing user privilege weight.
     */
    public int getPrivileges() {
        switch (role) {
            case "User":
                return 0;
            case "Student":
                return 1;
            case "Reviewer":
                return 2;
            case "Staff":
                return 3;
            case "Instructor":
                return 4;
            case "Admin":
                return 99;
            default:
                return 0;
        }
    }

    /**
     * Gets the user's first name.
     * 
     * @return firstName
     */
    public String getFirstName() {
    	if(firstName.length()>1) {firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
    	}else {firstName = firstName.toUpperCase();}
     return firstName;
    }

    /**
     * Sets the user's first name.
     * 
     * @param firstName String of what to set firstName to.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName : "";
    }

    /**
     * Gets the user's last name.
     * 
     * @return lastName
     */
    public String getLastName() {
    	if(lastName.length()>1) {lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
    	}else {lastName = lastName.toUpperCase();}
        return lastName;
    }

    /**
     * Sets the user's last name.
     * 
     * @param lastName String of what to set lastName to.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName : "";
    }

    /**
     * Gets the user's privilege weight.
     * 
     * @return weight
     */
    public int getWeight() {
        return weight;
    }
    
    

    /**
     * Sets the user's privilege weight.
     * 
     * @param weight int to set weight to.
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }
    /**
     * Gets if the applicant has a pending reviewer application.
     * 
     * @return boolean
     */
    public boolean isReviewerApplicant() {
    	return isReviewerApplicant;
    }
    /**
     * Sets if the user has a reviewer application pending.
     * 
     * @param applicant boolean to determine if pending application.
     */
    public void setReviewerApplicant(boolean applicant) {
    	this.isReviewerApplicant = applicant;
    }
    
    /**
     * Gets the user's favorite items as an array.
     * Favorites are stored as a comma-separated string in userFavorites.
     *
     * @return String array of favorites; empty array if none.
     */
    public String[] getFavorites() {
        if (userFavorites == null || userFavorites.trim().isEmpty()) {
            return new String[0];
        }
        return userFavorites.split(",");
    }

    /**
     * Adds a favorite item to the user's favorites list.
     * Prevents duplicates and trims whitespace.
     *
     * @param favorite The favorite item to add.
     */
    public void addFavorite(String favorite) {
        if (favorite == null || favorite.trim().isEmpty()) {
            return;
        }
        favorite = favorite.trim();

        String[] currentFavorites = getFavorites();
        for (String f : currentFavorites) {
            if (f.equals(favorite)) {
                return; // Already exists, no action needed
            }
        }

        if (userFavorites == null || userFavorites.isEmpty()) {
            userFavorites = favorite;
        } else {
            userFavorites += "," + favorite;
        }
    }

    /**
     * Removes a favorite item from the user's favorites list.
     *
     * @param favorite The favorite item to remove.
     */
    public void removeFavorite(String favorite) {
        if (favorite == null || favorite.trim().isEmpty() || userFavorites == null) {
            return;
        }
        favorite = favorite.trim();

        String[] favorites = getFavorites();
        StringBuilder newFavorites = new StringBuilder();

        for (String f : favorites) {
            if (!f.equals(favorite)) {
                if (newFavorites.length() > 0) {
                    newFavorites.append(",");
                }
                newFavorites.append(f);
            }
        }

        userFavorites = newFavorites.toString();
        if (userFavorites.isEmpty()) {
            userFavorites = null; // Optional: clean up empty state
        }
    }
    public String getFullName() {
    	if(getPrivileges()==4) {return "Instructor "+getLastName();}
    	else if(getPrivileges()==99) {return "Administrator "+getLastName();}
    	else {
        return getFirstName() +" "+ getMiddleInitial() +". "+ getLastName();
    	}
    }
    /**
     * Returns a string representation of the User object, 
     * including key details and excluding the password.
     * * @return A formatted string containing the user's details.
     */
    @Override
    public String toString() {
        // Construct the full name, handling middle initial gracefully
        String fullName = firstName;
        if (middleInitial != null && !middleInitial.isEmpty()) {
            fullName += " " + middleInitial + ".";
        }
        if (lastName != null && !lastName.isEmpty()) {
            fullName += " " + lastName;
        }

        // Display up to 3 favorites for a concise output
        String[] favoritesArray = getFavorites();
        String favoritesSummary;
        if (favoritesArray.length == 0) {
            favoritesSummary = "None";
        } else if (favoritesArray.length <= 3) {
            favoritesSummary = String.join(", ", favoritesArray);
        } else {
            // Display first three and indicate the count of the rest
            favoritesSummary = String.join(", ", favoritesArray[0], favoritesArray[1], favoritesArray[2]) 
                               + " (+ " + (favoritesArray.length - 3) + " more)";
        }
        
        return "User [" +
                "userName='" + userName + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName.trim() + '\'' +
                ", email='" + email + '\'' +
                ", weight=" + weight + 
                ", isReviewerApplicant=" + isReviewerApplicant +
                ", favorites=" + favoritesSummary +
                ']';
    }

}
