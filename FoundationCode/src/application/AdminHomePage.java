package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
	// Reference to the DatabaseHelper for database interactions
	private final DatabaseHelper databaseHelper;
	public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    public void show(Stage primaryStage, User user) {
    	VBox layout = new VBox();
    	
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // label to display the welcome message for the admin
	    Label adminLabel = new Label("Hello, Admin!");
	    adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    
	    Button truncateButton = new Button("Truncate Database");
	    truncateButton.setOnAction(a -> {
	    	databaseHelper.truncate();
	    	databaseHelper.closeConnection();
	    	Platform.exit();
	    });
	    
	    // Button directory to traverse into the User Database Page
	    Button userDatabase = new Button("User Database");
	    userDatabase.setOnAction(a -> {
	    		try {
					new UserDatabaseUI(databaseHelper, user).show(primaryStage);
				} catch (SQLException e) {
					// TODO Auto-generated catch block. Need to adjust
					e.printStackTrace();
				}
	    });	    
	    
	    Label userDisplay = new Label("User: "+user.getUserName()+", you have level "+user.getPrivileges()+" privileges as "+user.getRole());
	    userDisplay.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
        Button inviteButton = new Button("Invite");
        inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage, user);
        });
        
	    Button logoutButton = new Button("logout");
	    logoutButton.setOnAction(a -> {
	            new UserLoginPage(databaseHelper).show(primaryStage);
	    });
	    Button goBackButton = new Button("Go back"); goBackButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
	    goBackButton.setOnAction(a -> {
	    	new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
	    });
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
	    
	    layout.getChildren().addAll(adminLabel, inviteButton, truncateButton, userDatabase, logoutButton, goBackButton, quitButton);
	    Scene adminScene = new Scene(layout, 800, 400);
	    // Set the scene to primary stage
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Admin Page");
    }
}
