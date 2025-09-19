package application;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	
    	GridPane grid = new GridPane();
    	grid.setPadding(new Insets(10));
    	grid.setHgap(10); // Horizontal gap between columns
    	grid.setVgap(10); // Vertical gap between rows
    	
    	// set background image
    	Image backgroundImage = new Image(getClass().getResource("/solutions.png").toExternalForm());
    	BackgroundImage backgroundImg = new BackgroundImage(
    			backgroundImage,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundPosition.CENTER,
    			new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
    			);
    	grid.setBackground(new Background(backgroundImg));
        
        // Set program header
        Label Header = new Label("Login to sQaaS™");
        Header.setStyle("-fx-font-size: 22px; ");
    	// Label for Username field
        Label userNameLabel = new Label("Username");
        userNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    	// Input field for Username
        TextField userNameField = new TextField();
        userNameField.setPromptText("Type your username");
        userNameField.setMaxWidth(250);
        // Label for password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        // Input field for password
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Type your password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Create Buttons
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(150); // Set button width
        loginButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
        Button setupButton = new Button("Setup");
        setupButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: transparent; -fx-text-fill: #0099ff;");
        // Wrap buttons in an HorizontalBox for side-by-side placement
        HBox buttonBox = new HBox(10, setupButton, loginButton); // 10px spacing between buttons
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER); // Center align buttons
        
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 0 20; -fx-background-color: transparent; -fx-text-fill: #2c2c2c;");
        
        grid.add(Header, 0, 0); // Column 0, Row 0
        grid.add(userNameLabel, 0, 1); // Column 0, Row 1
        grid.add(userNameField, 0, 2);
        grid.add(passwordLabel, 0, 3);
        grid.add(passwordField, 0, 4); 
        grid.add(buttonBox, 0, 5);
        grid.add(errorLabel, 0, 6);
        grid.add(quitButton, 0, 20);
        GridPane.setHalignment(errorLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(quitButton, javafx.geometry.HPos.CENTER);
        
        loginButton.setOnAction(a -> {
        	// Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();
            try {
            	User user=new User(userName, password, "");
            	
            	// Retrieve the user's role from the database using userName
            	String role = databaseHelper.getUserRole(userName);
            	
            	if(role!=null) {
            		user.setRole(role);
            		if(databaseHelper.login(user)) {
            			new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
            			//errorLabel.setText(user.getRole());
            		}
            		else {
            			// Display an error if the login fails
                        errorLabel.setText("Error logging in. Invalid password?");
            		}
            	}
            	else {
            		// Display an error if the account does not exist
                    errorLabel.setText("Username does not exist...!");
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            } 
        });
        setupButton.setOnAction(a -> {
            new SetupAccountPage(databaseHelper).show(primaryStage);
        });
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
        Scene scene = new Scene(grid, 800, 400);    // GUI Container
        primaryStage.setScene(scene);               // GUI Container
        primaryStage.setTitle("sQaaS™");            // GUI Container
        primaryStage.setResizable(false);           // GUI Container
        primaryStage.show();                        // GUI Container
    }
}
