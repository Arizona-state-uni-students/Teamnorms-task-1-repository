package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

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

    public void show(Stage primaryStage) {
        // Input fields for userName, email, and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email (optional)");
        emailField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Continue as Admin");
        setupButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            
            // Validation
            if (userName.trim().isEmpty() || password.trim().isEmpty()) {
                errorLabel.setText("Username and password cannot be empty");
                return;
            }
            
            // Basic email validation if provided
            if (!email.trim().isEmpty() && !email.contains("@")) {
                errorLabel.setText("Please enter a valid email address");
                return;
            }
            
            try {
                // Create a new User object with admin role and register in the database
                User user = new User(userName, password, "admin");
                user.setEmail(email);
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");
                
                // Navigate to the Welcome Login Page
                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
            } catch (SQLException e) {
                errorLabel.setText("Database error: " + e.getMessage());
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Label titleLabel = new Label("First Administrator Setup");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        Label infoLabel = new Label("Create the first administrator account for the system");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        layout.getChildren().addAll(titleLabel, infoLabel, userNameField, emailField, passwordField, setupButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
