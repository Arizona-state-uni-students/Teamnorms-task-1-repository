package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
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
        
        // Input fields for userName, email, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        emailField.setMaxWidth(250);

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
            String password = passwordField.getText();
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
                        databaseHelper.register(user);
                        System.out.println("First admin created successfully!");
                        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                        
                    } else if(databaseHelper.validateInvitationCode(code)) {
                        // Regular user with valid invitation code
                        User user = new User(userName, password, "user");
                        user.setEmail(email);
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
        
        Label infoLabel = new Label("Invitation code required. Contact an administrator to get one.");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        // Check if database is empty to show appropriate message
        try {
            if (databaseHelper.isDatabaseEmpty()) {
                // First user setup
                infoLabel.setText("First user will become administrator - no invitation code needed");
                infoLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px; -fx-font-weight: bold;");
                layout.getChildren().addAll(titleLabel, infoLabel, userNameField, emailField, passwordField, setupButton, errorLabel, goBack);
            } else {
                // Normal user setup
                layout.getChildren().addAll(titleLabel, infoLabel, userNameField, emailField, passwordField, inviteCodeField, setupButton, errorLabel, goBack);
            }
        } catch (SQLException e) {
            // If error, show all fields as a fallback
            layout.getChildren().addAll(titleLabel, infoLabel, userNameField, emailField, passwordField, inviteCodeField, setupButton, errorLabel, goBack);
        }

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
