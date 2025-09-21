package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import databasePart1.*;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
    
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    private boolean usernameSet = false;
    private boolean passwordSet = false;
    private boolean emailSet = false;
    private boolean nameSet = false;
    public void show(Stage primaryStage) {
    	// Establish GUI Grid
    	GridPane grid = new GridPane();
    	//grid.setGridLinesVisible(true); // for testing
    	grid.setPadding(new Insets(10));
    	grid.setHgap(10); // Horizontal gap between columns
    	grid.setVgap(10); // Vertical gap between rows
    	grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    	
    	// set background image
    	Image backgroundImage = new Image(getClass().getResource("/admincreate.png").toExternalForm());
    	BackgroundImage backgroundImg = new BackgroundImage(
    			backgroundImage,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundPosition.CENTER,
    			new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
    			);
    	grid.setBackground(new Background(backgroundImg));
    	
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
    	// username field
        Label userNameLabel = new Label("Username");
        userNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);
        userNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 20) { // username max length 20
            	userNameField.setText(newValue.substring(0, 20));
            }
            	// username validating fsm
                validateUsername(newValue, errorLabel);
        });
        // email field
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 20) { // email max length 20
            	emailField.setText(newValue.substring(0, 20));
            }
        	String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            Pattern pattern = Pattern.compile(emailRegex);
            Matcher matcher = pattern.matcher(newValue);

            if (matcher.find()) {
                //System.out.println("Found email: " + matcher.group());
                errorLabel.setText("Valid Email");
                errorLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                emailSet=true;
            }else {
	            errorLabel.setText("Invalid Email");
	            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
	            emailSet=false;
            }
        });
        // password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 20) { // password max length 20
            	passwordField.setText(newValue.substring(0, 20));
            }
            	// password validating fsm
                validatePassword(newValue, errorLabel);
        });
        // name fields
        Label firstNameLabel = new Label("First Name");
        firstNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Enter First Name");
        firstNameField.setMaxWidth(200);
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 20) { // first name max length 20
            	firstNameField.setText(newValue.substring(0, 20));
            }
            if (newValue.length() >= 1) { // first name min length 1
            	nameSet=true;
            }else {
            	nameSet=false;
            }
        });
        Label middleNameLabel = new Label("Middle Initial");
        middleNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField middleNameField = new TextField();
        middleNameField.setPromptText("M.I.");
        middleNameField.setMaxWidth(40);
        middleNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                middleNameField.setText(newValue.substring(0, 1));
            }
        });

        Label lastNameLabel = new Label("Last Name");
        lastNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Enter Last Name");
        lastNameField.setMaxWidth(200);
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 20) { // last name max length 20
            	lastNameField.setText(newValue.substring(0, 20));
            }
            if (newValue.length() >= 1) { // last name min length 1
            	nameSet=true;
            }else {
            	nameSet=false;
            }
        });        


        Button setupButton = new Button("Continue as Admin");
        setupButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
        
        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String email = emailField.getText();
            String firstName = firstNameField.getText();
            String middleInitial = middleNameField.getText();
            String password = passwordField.getText();
       
            if(usernameSet && passwordSet && emailSet && nameSet) {
            try {
                // Create a new User object with admin role and register in the database
            	User user = new User(userName, password, "admin");
                user.setEmail(email);
                user.setMiddleInitial(middleInitial);
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");
                
                // Navigate to the Welcome Login Page
                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
            } catch (SQLException e) {
                errorLabel.setText("Database error: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
            }else {
         	   errorLabel.setText("Fields cannot be left blank: Username, Password, Email, Name");
               errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            }
        });
	    
        Label titleLabel = new Label("Administrator Setup");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ff9900;");
        
        Label infoLabel = new Label("Create the first administrator account for the system.");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        grid.add(titleLabel, 1, 0);    // Column 1, Row 0
        grid.add(infoLabel, 1, 1);     // Column 1, Row 1
	    grid.add(userNameLabel, 0, 2); // Column 0, Row 2
        grid.add(userNameField, 1, 2); // Column 1, Row 2
        grid.add(passwordLabel, 0, 3); // Column 0, Row 3
        grid.add(passwordField, 1, 3); // Column 1, Row 3
        grid.add(emailLabel, 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(firstNameLabel, 0, 5);
        grid.add(firstNameField, 1, 5);
        grid.add(middleNameLabel, 0, 6);
        grid.add(middleNameField, 1, 6);
        grid.add(lastNameLabel, 0, 7);
        grid.add(lastNameField, 1, 7);
        grid.add(errorLabel, 1, 8);
        grid.add(setupButton, 2, 9);
        GridPane.setHalignment(userNameLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(passwordLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(emailLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(firstNameLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(middleNameLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(lastNameLabel, javafx.geometry.HPos.RIGHT);
	    Scene scene1 = new Scene(grid, 800, 500);
        primaryStage.setScene(scene1);
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
    // FSM for username validation
	private void validateUsername(String input, Label label) {
		//TODO: 
		//		GUI elements to let user know password requirements/restrictions
		// Current Requirements: 4+ length, No Special Characters
		// (Is that what we want?)
		String inputText = input;
		if (input.isEmpty()) {}
		else
		{
			String errMessage = PasswordEvaluator.evaluateUsername(inputText);
			//updateFlags();
			if (errMessage != "") {
				System.out.println(errMessage);
				label.setText("The username must be at least 4 characters long.\nCan only include: upper case and lower case letters and numbers.");
				label.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
				usernameSet=false;
			}
			else if (PasswordEvaluator.UfoundLongEnough) {
				label.setText("Success! The username satisfies the requirements.");
				label.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
				usernameSet=true;
			} else {
				label.setText("This username does not yet satisfy requirements.");
				label.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
				usernameSet=false;
			}
		}
	}
    // FSM for password validation
	private void validatePassword(String input, Label label) {
		//TODO: 
		//		GUI elements to let user know password requirements/restrictions
		// Current Requirements: 6+ length, OneCapital, OneLowerCase, OneDigit, OneSpecialChar
		// (Is that what we want?)
		String inputText = input;
		if (input.isEmpty()) {}
		else
		{
			String errMessage = PasswordEvaluator.evaluatePassword(inputText);
			//updateFlags();
			if (errMessage != "") {
				//label.setText("Failure! The password is not valid.");
				label.setText("Password must be at least 6 characters long and include:\nUpper case letter, lower case letter, a digit and a special symbol.");
				label.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
				passwordSet=false;
			}
			else if (PasswordEvaluator.PfoundUpperCase && PasswordEvaluator.PfoundLowerCase &&
					PasswordEvaluator.PfoundNumericDigit && PasswordEvaluator.PfoundSpecialChar &&
					PasswordEvaluator.PfoundLongEnough) {
				label.setText("Success! The password satisfies the requirements.");
				label.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
				passwordSet=true;
			} else {
				label.setText("The password as currently entered is not yet valid.");
				label.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
				passwordSet=false;
			}
		}
	}
}

