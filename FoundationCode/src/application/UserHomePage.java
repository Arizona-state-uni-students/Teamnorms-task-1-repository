package application;

import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {

	private final DatabaseHelper databaseHelper;

    public UserHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage, User user) {
    	
    	GridPane grid = new GridPane(); // Create grid for placing UI elements
    	grid.setPadding(new Insets(10)); // 10px padding on top, right, bottom, left of all elements in grid
    	grid.setHgap(10); // 10px Horizontal gap between columns
    	grid.setVgap(10); // 10px Vertical gap between rows
    	
        
        Label Header = new Label("User Page"); Header.setStyle("-fx-font-size: 22px; ");
        Label userNameLabel = new Label("Username");userNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField userNameField = new TextField();userNameField.setPromptText("Type your new username");userNameField.setMaxWidth(250);
        Label passwordLabel = new Label("Password");passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();passwordField.setPromptText("Type your new password");passwordField.setMaxWidth(250);
        
        // Populate user items
        userNameField.setText(user.getUserName());
        passwordField.setText(user.getPassword());
        
        // Label to display error messages
        Label errorLabel = new Label();errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Create Buttons
        Button saveButton = new Button("Save Changes");
        saveButton.setPrefWidth(150); // Set button width
        saveButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
        Button revertButton = new Button("Revert");
        revertButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: transparent; -fx-text-fill: #0099ff;");
        HBox buttonBox = new HBox(10, revertButton, saveButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        Button goBackButton = new Button("Go back"); goBackButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
        
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 0 20; -fx-background-color: transparent; -fx-text-fill: #2c2c2c;");
        
        grid.add(Header, 0, 0); // Column 0, Row 0
        grid.add(userNameLabel, 0, 1); // Column 0, Row 1
        grid.add(userNameField, 0, 2);
        grid.add(passwordLabel, 0, 3);
        grid.add(passwordField, 0, 4); 
        grid.add(buttonBox, 0, 5);
        grid.add(errorLabel, 0, 6);
        grid.add(goBackButton, 2, 7);
        grid.add(quitButton, 0, 20);
        GridPane.setHalignment(errorLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(quitButton, javafx.geometry.HPos.CENTER);
        
	    goBackButton.setOnAction(a -> {
	    	new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
	    });
        revertButton.setOnAction(a -> {
            userNameField.setText(user.getUserName());
            passwordField.setText(user.getPassword());
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
