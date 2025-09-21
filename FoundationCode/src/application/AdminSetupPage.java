package application;

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
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
    
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Establish GUI Grid
    	GridPane grid = new GridPane();
    	//grid.setGridLinesVisible(true); // for testing
    	grid.setPadding(new Insets(10));
    	grid.setHgap(10); // Horizontal gap between columns
    	grid.setVgap(10); // Vertical gap between rows
    	grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    	
    	// set background image
    	Image backgroundImage = new Image(getClass().getResource("/admincreate.png").toExternalForm());
    	BackgroundImage backgroundImg = new BackgroundImage(
    			backgroundImage,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundPosition.CENTER,
    			new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
    			);
    	grid.setBackground(new Background(backgroundImg));
    	
    	
    	// username field
        Label userNameLabel = new Label("Username");
        userNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);
        
        // email field
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);
        
        // password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // name fields
        Label firstNameLabel = new Label("First Name");
        firstNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Enter First Name");
        firstNameField.setMaxWidth(200);
        Label middleNameLabel = new Label("Middle Initial");
        middleNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField middleNameField = new TextField();
        middleNameField.setPromptText("M.I.");
        middleNameField.setMaxWidth(40);
        middleNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                middleNameField.setText(newValue.substring(0, 1));
            }
        });
        Label LastNameLabel = new Label("Last Name");
        LastNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField LastNameField = new TextField();
        LastNameField.setPromptText("Enter Last Name");
        LastNameField.setMaxWidth(200);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Continue as Admin");
        setupButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
        
        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String email = emailField.getText();
            String middleInitial = middleNameField.getText();
            String password = passwordField.getText();
            
            // Validation
            if (userName.trim().isEmpty() || password.trim().isEmpty() || email.trim().isEmpty()) {
                errorLabel.setText("Fields cannot be empty.");
                return;
            }
            
            // Basic email validation if provided
            if (!email.trim().isEmpty() && !email.contains("@")) {
                errorLabel.setText("Please enter a valid email address");
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
	    
        Label titleLabel = new Label("Administrator Setup");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ff9900;");
        
        Label infoLabel = new Label("Create the first administrator account for the system.");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        grid.add(titleLabel, 1, 0);    // Column 1, Row 0
        grid.add(infoLabel, 1, 1);     // Column 1, Row 1
	    grid.add(userNameLabel, 0, 2); // Column 0, Row 2
        grid.add(userNameField, 1, 2); // Column 1, Row 2
        grid.add(passwordLabel, 0, 3); // Column 0, Row 3
        grid.add(passwordField, 1, 3); // Column 1, Row 3
        grid.add(emailLabel, 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(firstNameLabel, 0, 5);
        grid.add(firstNameField, 1, 5);
        grid.add(middleNameLabel, 0, 6);
        grid.add(middleNameField, 1, 6);
        grid.add(LastNameLabel, 0, 7);
        grid.add(LastNameField, 1, 7);
        grid.add(errorLabel, 1, 8);
        grid.add(setupButton, 2, 9);
        GridPane.setHalignment(userNameLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(passwordLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(emailLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(firstNameLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(middleNameLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(LastNameLabel, javafx.geometry.HPos.RIGHT);
	    Scene scene1 = new Scene(grid, 800, 400);
        primaryStage.setScene(scene1);
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
