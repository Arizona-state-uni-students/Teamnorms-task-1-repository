package application;

import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;
import java.sql.SQLException;


/**
 * UserDatabasePage class represents the user interface for the user database.
 * This page displays user database integrated into a GUI for admin usage.
 * 
 * Thoughts and next steps:
 * 1. 
 * 2.
 * 3. Confirm layout and UI layout/display
 */

public class UserDatabaseUI {
	
	private final DatabaseHelper databaseHelper;
	private final User user;
	public UserDatabaseUI(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
    }
	
	public void show(Stage primaryStage) throws SQLException {
		VBox layout = new VBox();
		
		layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    GridPane databaseTable = new GridPane(); // Create grid for placing UI elements
    	databaseTable.setPadding(new Insets(10)); // 10px padding on top, right, bottom, left of all elements in grid
    	databaseTable.setHgap(10); // 10px Horizontal gap between columns
    	databaseTable.setVgap(10); // 10px Vertical gap between rows
    	
    	Label Header = new Label("Welcome to the User Database."); Header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        
    	// Username display
    	Label userNameLabel = new Label("Username");userNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        // Displays the username of users
        // Must find out how to display all of the users in the database
        Label userName = new Label(user.getUserName());userName.setMaxWidth(250);
        
        /*
         *  Role display
         *  
         *  Placeholder for future role change option
         *  Idea for implementation:
         *  Make the role label into a text field that can be overwritten
         */
        Label roleLabel = new Label("Role");roleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        // Displays the role of users
        // Must find out how to display all of the users in the database
        Label role = new Label(user.getRole());role.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        Button goBackButton = new Button("Go back"); goBackButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
        
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 0 20; -fx-background-color: transparent; -fx-text-fill: #2c2c2c;");
        
        HBox buttonBox = new HBox(10, goBackButton, quitButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        layout.getChildren().addAll(Header, databaseTable, buttonBox);
                
        /*
         *  For loop to go through the user database.
         *  Adds user's name, role, provides a passwordResetButton, and a Delete button for each user
         */
        
        // Database table headers
        databaseTable.add(userNameLabel, 0, 0); // Column 0, Row 0
        databaseTable.add(roleLabel, 1, 0);
        
        // Fetch users and render rows
        List<User> users = databaseHelper.getAllUsers();
        
        int rowIndex = 1; // start below header row
        for (User u : users) {
        	String targetUsername = u.getUserName();
            Label userNameCell = new Label(targetUsername);
            userNameCell.setMaxWidth(250);
            
            Label roleCell = new Label(u.getRole());
            roleCell.setMaxWidth(250);
            
            HBox passwordControls = new HBox(10);
            passwordControls.setAlignment(Pos.CENTER_LEFT);
            
            Button resetButton = new Button("Reset Password");
            passwordControls.getChildren().setAll(resetButton);
            
            resetButton.setOnAction(ev -> {
                PasswordField passwordField = new PasswordField();passwordField.setPromptText("Type your new password");passwordField.setMaxWidth(250);
                Button saveButton = new Button("Save Changes");saveButton.setPrefWidth(150);saveButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
                
                passwordControls.getChildren().setAll(passwordField, saveButton);
                
                saveButton.setOnAction(ev2 -> {
                    String newPassword = passwordField.getText();
                    if (newPassword == null || newPassword.isBlank()) {
                        // optionally show an error label here
                        return;
                    }

                    boolean success = false;
                    try {
                        success = databaseHelper.updateUserPassword(targetUsername, newPassword);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    if (success) {
                        // Reset controls back to the single button
                        passwordControls.getChildren().setAll(resetButton);
                    } else {
                        System.out.println("Password update failed for user " + targetUsername);
                    }
                });
            });

            // Optional per-row Delete button (placeholder)
            Button deleteUserBtn = new Button("Delete");

            // --- Add row to the grid ---
            databaseTable.add(userNameCell,      0, rowIndex);
            databaseTable.add(roleCell,          1, rowIndex);
            databaseTable.add(passwordControls,  2, rowIndex);
            databaseTable.add(deleteUserBtn,     3, rowIndex);

            rowIndex++;
        }

        
        GridPane.setHalignment(errorLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(quitButton, javafx.geometry.HPos.CENTER);
        
        goBackButton.setOnAction(a -> {
	    	new AdminHomePage(databaseHelper).show(primaryStage, user); // Must get admin's user to traverse back to AdminHomePage
	    });
        
        quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
	    
	    Scene userDBScene = new Scene(layout, 800, 400);
	    
	    primaryStage.setScene(userDBScene);
	    primaryStage.setTitle("User Database Page");
	}
}