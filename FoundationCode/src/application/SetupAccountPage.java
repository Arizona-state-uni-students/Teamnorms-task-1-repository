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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, email, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
    
    private final DatabaseHelper databaseHelper;
    
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Establish GUI Grid
    	GridPane grid = new GridPane();
    	//grid.setGridLinesVisible(true); // for testing
    	grid.setPadding(new Insets(10));
    	grid.setHgap(10); // Horizontal gap between columns
    	grid.setVgap(10); // Vertical gap between rows
    	grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    	
    	// set background image
    	Image backgroundImage = new Image(getClass().getResource("/createaccount.png").toExternalForm());
    	BackgroundImage backgroundImg = new BackgroundImage(
    			backgroundImage,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundPosition.CENTER,
    			new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
    			);
    	grid.setBackground(new Background(backgroundImg));
    	
    	// username field
        Label userNameLabel = new Label("Username");
        userNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);
        
        // email field
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);
        
        // password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // name fields
        Label firstNameLabel = new Label("First Name");
        firstNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Enter First Name");
        firstNameField.setMaxWidth(200);
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
        Label LastNameLabel = new Label("Last Name");
        LastNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField LastNameField = new TextField();
        LastNameField.setPromptText("Enter Last Name");
        LastNameField.setMaxWidth(200);
        
        // invite code fields
        Label inviteCodeLabel = new Label("Invitation Code");
        inviteCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        Button setupButton = new Button("Create Account");
        setupButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
        Button goBack = new Button("Cancel");
        goBack.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
        // Navigate back to UserLoginPage
        goBack.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage);
        });
        
        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String middleInitial = middleNameField.getText();
            String code = inviteCodeField.getText();
            
            // Validation
            if (userName.trim().isEmpty() || password.trim().isEmpty()) {
                errorLabel.setText("Username and password cannot be empty");
                return;
            }
            
            // Basic email validation
            if (!email.trim().isEmpty() && !email.contains("@")) {
                errorLabel.setText("Please enter a valid email address");
                return;
            }
            
            try {
                // Check if user already exists
                if(!databaseHelper.doesUserExist(userName)) {
                    
                    // Check if database is empty (first user becomes admin)
                    if(databaseHelper.isDatabaseEmpty()) {
                        // First user becomes admin automatically
                    	User user = new User(userName, password, "admin");
                        user.setEmail(email);
                        user.setMiddleInitial(middleInitial);
                        databaseHelper.register(user);
                        System.out.println("First admin created successfully!");
                        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                        
                    } else if(databaseHelper.validateInvitationCode(code)) {
                        // Regular user with valid invitation code
                    	 User user = new User(userName, password, "user");
                         user.setEmail(email);
                         user.setMiddleInitial(middleInitial);
                         databaseHelper.register(user);
                        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                        
                    } else {
                        errorLabel.setText("Please enter a valid invitation code. Contact an admin if you don't have one.");
                    }
                } else {
                    errorLabel.setText("This username is taken! Please use another to setup an account");
                }
                
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
                errorLabel.setText("Database error occurred");
            }
        });
        
        Label titleLabel = new Label("User Registration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label infoLabel = new Label("Invitation code required. Contact an administrator to get one.");
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
        grid.add(LastNameLabel, 0, 7);
        grid.add(LastNameField, 1, 7);
        grid.add(inviteCodeLabel, 0, 8);
        grid.add(inviteCodeField, 1, 8);
        grid.add(errorLabel, 1, 9);
        grid.add(goBack, 0, 10);
        grid.add(setupButton, 1, 10);
        GridPane.setHalignment(userNameLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(passwordLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(emailLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(firstNameLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(middleNameLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(LastNameLabel, javafx.geometry.HPos.RIGHT);
	    Scene scene1 = new Scene(grid, 800, 400);
        primaryStage.setScene(scene1);
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
