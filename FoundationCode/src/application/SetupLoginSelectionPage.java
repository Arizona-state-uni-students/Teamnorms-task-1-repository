package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * The SetupLoginSelectionPage class allows users to choose between setting up a new account
 * or logging into an existing account. It provides two buttons for navigation to the respective pages.
 */
public class SetupLoginSelectionPage {
    
    private final DatabaseHelper databaseHelper;

    public SetupLoginSelectionPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        
        // Buttons to select Login / Setup options that redirect to respective pages
        Button setupButton = new Button("SetUp");
        Button loginButton = new Button("Login");
        
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        try {
            // Check if database is empty - if so, show admin setup option
            if (databaseHelper.isDatabaseEmpty()) {
                Button adminSetupButton = new Button("First Time Setup (Admin)");
                adminSetupButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                
                adminSetupButton.setOnAction(a -> {
                    new AdminSetupPage(databaseHelper).show(primaryStage);
                });
                
                Label infoLabel = new Label("No users found. Please set up the first admin account.");
                infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                
                layout.getChildren().addAll(infoLabel, adminSetupButton);
                
            } else {
                // Normal flow - show login and setup buttons
                setupButton.setOnAction(a -> {
                    new SetupAccountPage(databaseHelper).show(primaryStage);
                });
                
                loginButton.setOnAction(a -> {
                    new UserLoginPage(databaseHelper).show(primaryStage);
                });
                
                layout.getChildren().addAll(setupButton, loginButton);
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message
            Label errorLabel = new Label("Error connecting to database: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            layout.getChildren().add(errorLabel);
        }

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
