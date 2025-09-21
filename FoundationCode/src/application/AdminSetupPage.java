package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

public class AdminSetupPage {
    
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Input fields for userName, email, middle initial, and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email (required)");  // Changed to required
        emailField.setMaxWidth(250);
        
        TextField middleInitialField = new TextField();
        middleInitialField.setPromptText("Middle Initial (Optional)");
        middleInitialField.setMaxWidth(50);
        middleInitialField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                middleInitialField.setText(newValue.substring(0, 1));
            }
        });

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
            String middleInitial = middleInitialField.getText();
            String password = passwordField.getText();
            
            // Validation
            if (userName.trim().isEmpty() || password.trim().isEmpty()) {
                errorLabel.setText("Username and password cannot be empty");
                return;
            }
            
            // Email is now mandatory
            if (email.trim().isEmpty()) {
                errorLabel.setText("Email is required");
                return;
            }
            
            // Email format validation
            if (!email.contains("@") || !email.contains(".")) {
                errorLabel.setText("Please enter a valid email address (e.g., admin@example.com)");
                return;
            }
            
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
        
        Label requiredNote = new Label("* Email is required for administrator account");
        requiredNote.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        
        layout.getChildren().addAll(titleLabel, infoLabel, requiredNote, userNameField, emailField, middleInitialField, passwordField, setupButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
