package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

public class SetupAccountPage {
    
    private final DatabaseHelper databaseHelper;
    
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        
        // Input fields for userName, email, middle initial, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email (required)");  // Changed to required
        emailField.setMaxWidth(250);
        
        TextField middleInitialField = new TextField();
        middleInitialField.setPromptText("Middle Initial (Optional)");
        middleInitialField.setMaxWidth(50);
        // Limit to 1 character
        middleInitialField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                middleInitialField.setText(newValue.substring(0, 1));
            }
        });

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        Button setupButton = new Button("Create Account");
        Button goBack = new Button("goBack");
        
        // Navigate back to UserLoginPage
        goBack.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage);
        });
        
        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String email = emailField.getText();
            String middleInitial = middleInitialField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();
            
            // Validation
            if (userName.trim().isEmpty() || password.trim().isEmpty()) {
                errorLabel.setText("Username and password cannot be empty");
                return;
            }
            
            // Email is now mandatory for new users
            if (email.trim().isEmpty()) {
                errorLabel.setText("Email is required");
                return;
            }
            
            // Email format validation
            if (!email.contains("@") || !email.contains(".")) {
                errorLabel.setText("Please enter a valid email address (e.g., user@example.com)");
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

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Label titleLabel = new Label("New User Registration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label infoLabel = new Label("All fields marked with * are required");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        // Add asterisks to required fields labels
        Label requiredNote = new Label("* Required fields");
        requiredNote.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        
        // Check if database is empty to show appropriate message
        try {
            if (databaseHelper.isDatabaseEmpty()) {
                // First user setup
                infoLabel.setText("First user will become administrator - no invitation code needed");
                infoLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px; -fx-font-weight: bold;");
                layout.getChildren().addAll(titleLabel, infoLabel, requiredNote, userNameField, emailField, middleInitialField, passwordField, setupButton, errorLabel, goBack);
            } else {
                // Normal user setup
                infoLabel.setText("Email and invitation code are required");
                infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                layout.getChildren().addAll(titleLabel, infoLabel, requiredNote, userNameField, emailField, middleInitialField, passwordField, inviteCodeField, setupButton, errorLabel, goBack);
            }
        } catch (SQLException e) {
            // If error, show all fields as a fallback
            layout.getChildren().addAll(titleLabel, infoLabel, requiredNote, userNameField, emailField, middleInitialField, passwordField, inviteCodeField, setupButton, errorLabel, goBack);
        }

        primaryStage.setScene(new Scene(layout, 800, 450));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
