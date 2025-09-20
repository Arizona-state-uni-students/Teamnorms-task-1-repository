package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;
	
    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    // variable privileges 
	    int privilege = user.getPrivileges();
	    
	    Label welcomeLabel = new Label("Welcome "+user.getUserName()+", you have level "+user.getPrivileges()+" privileges as "+user.getRole());
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    
	    
	    Button userButton = new Button("Continue to User Page");
	    userButton.setOnAction(a -> {
	    		new UserHomePage(databaseHelper).show(primaryStage, user);
	    });
	    Button adminButton = new Button("Continue to Admin Page");
	    adminButton.setOnAction(a -> {
	    		new AdminHomePage(databaseHelper).show(primaryStage, user);
	    });
	    
	    // Button to quit the application
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
	    
	    Button logout = new Button("logout");
	    logout.setOnAction(a -> {
	            new UserLoginPage(databaseHelper).show(primaryStage);
	    });
	    
	    // "Invite" button for admin to generate invitation codes
	    if ("admin".equals(user.getRole())) {
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage, user);
            });
            layout.getChildren().add(inviteButton);
        }
	    if("admin".equals(user.getRole())) {
		layout.getChildren().addAll(welcomeLabel, userButton, adminButton, quitButton, logout);
		}else {
	    layout.getChildren().addAll(welcomeLabel, userButton, quitButton, logout);
		}
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}
