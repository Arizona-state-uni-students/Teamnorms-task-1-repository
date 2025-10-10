package application;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

import java.sql.SQLException;

import databasePart1.*;

public class UserHomePage {
    
    private final DatabaseHelper databaseHelper;
    
    public UserHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void show(Stage primaryStage, User currentUser) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("User Page");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Username section (read-only)
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextField usernameField = new TextField(currentUser.getUserName());
        usernameField.setEditable(false);
        usernameField.setMaxWidth(250);
        usernameField.setStyle("-fx-background-color: #e0e0e0;");
        
        // Email section
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextField emailField = new TextField();
        String currentEmail = currentUser.getEmail();
        
        // Create the email update button first
        Button updateEmailButton = new Button("Update Email");
        updateEmailButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        // Create the emailBox
        HBox emailBox = new HBox(10, emailField, updateEmailButton);
        
        // Now handle the email field setup and warning
        if (currentEmail == null || currentEmail.isEmpty()) {
            emailField.setPromptText("Please add your email address");
            emailField.setStyle("-fx-border-color: orange; -fx-border-width: 1px;");
            emailField.setMaxWidth(250);
            
            Label emailWarning = new Label("⚠ Email is required for all users");
            emailWarning.setStyle("-fx-text-fill: orange; -fx-font-size: 10px;");
            
            VBox emailContainer = new VBox(5);
            emailContainer.getChildren().addAll(emailBox, emailWarning);
            
            grid.add(emailLabel, 0, 3);
            grid.add(emailContainer, 0, 4);
        } else {
            emailField.setText(currentEmail);
            emailField.setMaxWidth(250);
            grid.add(emailLabel, 0, 3);
            grid.add(emailBox, 0, 4);
        }
        
        // Middle Initial section
        Label middleInitialLabel = new Label("Middle Initial");
        middleInitialLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField middleInitialField = new TextField();
        String currentMiddleInitial = currentUser.getMiddleInitial();
        if (currentMiddleInitial != null && !currentMiddleInitial.isEmpty()) {
            middleInitialField.setText(currentMiddleInitial);
        } else {
            middleInitialField.setPromptText("Add middle initial");
        }
        middleInitialField.setMaxWidth(50);
        middleInitialField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                middleInitialField.setText(newValue.substring(0, 1));
            }
        });

        Button updateMiddleInitialButton = new Button("Update");
        updateMiddleInitialButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        HBox middleInitialBox = new HBox(10, middleInitialField, updateMiddleInitialButton);
        
        // Password section
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter new password");
        passwordField.setMaxWidth(250);
        
        HBox passwordBox = new HBox(10);
        Button revertButton = new Button("Revert");
        Button savePasswordButton = new Button("Save Changes");
        savePasswordButton.setStyle("-fx-background-color: #0099ff; -fx-text-fill: white;");
        
        passwordBox.getChildren().addAll(revertButton, savePasswordButton);
        
        // Status/Error label
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");
        
        // Go back button
        Button goBackButton = new Button("Go back");
        goBackButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
        logoutButton.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage);
        });
        // Add components to grid
        grid.add(titleLabel, 0, 0, 2, 1);
        
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 0, 2);
        
        // Email section is already added in the conditional block above
        
        grid.add(middleInitialLabel, 0, 5);
        grid.add(middleInitialBox, 0, 6);
        
        grid.add(passwordLabel, 0, 7);
        grid.add(passwordField, 0, 8);
        grid.add(passwordBox, 0, 9);
        
        grid.add(statusLabel, 0, 10, 2, 1);
        grid.add(goBackButton, 0, 11);
        grid.add(logoutButton, 1, 11);
        
        // Email update action
        updateEmailButton.setOnAction(e -> {
            String newEmail = emailField.getText().trim();
            
            // Basic email validation
            if (!newEmail.isEmpty() && (!newEmail.contains("@") || !newEmail.contains("."))) {
                showAlert("Invalid Email", "Please enter a valid email address", AlertType.ERROR);
                return;
            }
            
            try {
                if (databaseHelper.updateUserEmail(currentUser.getUserName(), newEmail)) {
                    currentUser.setEmail(newEmail);
                    statusLabel.setText("Email updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                    
                    // Show success alert
                    showAlert("Success", "Email updated successfully!", AlertType.INFORMATION);
                } else {
                    statusLabel.setText("Failed to update email");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                ex.printStackTrace();
            }
        });
        
        // Middle Initial update action
        updateMiddleInitialButton.setOnAction(e -> {
            String newMiddleInitial = middleInitialField.getText().trim();
            
            try {
                if (databaseHelper.updateUserMiddleInitial(currentUser.getUserName(), newMiddleInitial)) {
                    currentUser.setMiddleInitial(newMiddleInitial);
                    statusLabel.setText("Middle initial updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                    
                    showAlert("Success", "Middle initial updated successfully!", AlertType.INFORMATION);
                } else {
                    statusLabel.setText("Failed to update middle initial");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                ex.printStackTrace();
            }
        });
        
        // Password save action
        savePasswordButton.setOnAction(e -> {
            String newPassword = passwordField.getText();
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                statusLabel.setText("Password cannot be empty!");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                return;
            }
            
            try {
                if (databaseHelper.updateUserPassword(currentUser.getUserName(), newPassword)) {
                    currentUser.setPassword(newPassword);
                    passwordField.clear();
                    statusLabel.setText("Password updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                    
                    // Show success alert
                    showAlert("Success", "Password updated successfully!", AlertType.INFORMATION);
                } else {
                    statusLabel.setText("Failed to update password");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                ex.printStackTrace();
            }
        });
        
        // Revert password field
        revertButton.setOnAction(e -> {
            passwordField.clear();
            statusLabel.setText("");
        });
        
        // Go back action
        goBackButton.setOnAction(e -> {
            // Navigate back to welcome page
            new WelcomeLoginPage(databaseHelper).show(primaryStage, currentUser);
        });
        
        Scene scene = new Scene(grid, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("sQaaS™ - User Profile");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
