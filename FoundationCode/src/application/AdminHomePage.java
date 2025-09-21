package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
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
    	// Establish GUI Grid
    	GridPane grid = new GridPane();
    	//grid.setGridLinesVisible(true); // for testing
    	grid.setPadding(new Insets(10));
    	grid.setHgap(10); // Horizontal gap between columns
    	grid.setVgap(10); // Vertical gap between rows
    	grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    	
    	// set background image
    	Image backgroundImage = new Image(getClass().getResource("/blankadmin.png").toExternalForm());
    	BackgroundImage backgroundImg = new BackgroundImage(
    			backgroundImage,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundPosition.CENTER,
    			new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
    			);
    	grid.setBackground(new Background(backgroundImg));
    	
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
	    userDatabase.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #ff9900; -fx-text-fill: black;");
	    userDatabase.setOnAction(a -> {
	    		try {
					new UserDatabaseUI(databaseHelper, user).show(primaryStage);
				} catch (SQLException e) {
					// TODO Auto-generated catch block. Need to adjust
					e.printStackTrace();
				}
	    });	    
	    
        Button inviteButton = new Button("Generate Invitations");
        inviteButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #ff9900; -fx-text-fill: black;");
        inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage, user);
        });
        
	    Button logoutButton = new Button("Logout");
	    logoutButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
	    logoutButton.setOnAction(a -> {
	            new UserLoginPage(databaseHelper).show(primaryStage);
	    });
	    Button switchRoleButton = new Button("Pick Role"); 
	    switchRoleButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
	    switchRoleButton.setOnAction(a -> {
	    	new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
	    });

	    grid.add(adminLabel, 0, 0);    // Column 1, Row 0
        grid.add(userDatabase, 0, 1);     // Column 1, Row 1
	    grid.add(inviteButton, 0, 2); // Column 0, Row 2
        grid.add(switchRoleButton, 0, 3); // Column 0, Row 3
        grid.add(logoutButton, 0, 4);
	    
	    
	    Scene adminScene = new Scene(grid, 800, 400);
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Admin Page");
    }
}

