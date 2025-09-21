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

/**
 * User Page for managing user profile - allows users to update their password and email
 */
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
        if (currentEmail != null && !currentEmail.isEmpty()) {
            emailField.setText(currentEmail);
        } else {
            emailField.setPromptText("Add your email address");
        }
        emailField.setMaxWidth(250);
        
        Button updateEmailButton = new Button("Update Email");
        updateEmailButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        HBox emailBox = new HBox(10, emailField, updateEmailButton);
        
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
        Button goBackButton = new Button("Pick Role");
        goBackButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
        
        // Add components to grid
        grid.add(titleLabel, 0, 0, 2, 1);
        
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 0, 2);
        
        grid.add(emailLabel, 0, 3);
        grid.add(emailBox, 0, 4);
        
        grid.add(passwordLabel, 0, 5);
        grid.add(passwordField, 0, 6);
        grid.add(passwordBox, 0, 7);
        
        grid.add(statusLabel, 0, 8, 2, 1);
        grid.add(goBackButton, 0, 9);
        
        // Email update action
        updateEmailButton.setOnAction(e -> {
            String newEmail = emailField.getText().trim();
            
            // Basic email validation
            if (!newEmail.isEmpty() && !newEmail.contains("@")) {
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
        
        Scene scene = new Scene(grid, 500, 400);
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
