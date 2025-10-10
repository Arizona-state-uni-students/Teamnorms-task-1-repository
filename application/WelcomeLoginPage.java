package application;

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
import javafx.application.Platform;
import javafx.geometry.Insets;
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
    public void show(Stage primaryStage, User user) {
    	   
        // Establish GUI Grid
        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true); // for testing
        grid.setPadding(new Insets(10));
        grid.setHgap(10); // Horizontal gap between columns
        grid.setVgap(10); // Vertical gap between rows
        grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        
        // set background image
        Image backgroundImage = new Image(getClass().getResource("/blank.png").toExternalForm());
        BackgroundImage backgroundImg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
                );
        grid.setBackground(new Background(backgroundImg));
        
        Label welcomeLabel = new Label("Hello, "+user.getUserName()+"!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button userButton = new Button("Continue to User Page");
        userButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
        userButton.setOnAction(a -> {
                new UserHomePage(databaseHelper).show(primaryStage, user);
        });
        
        // ADD THIS Q&A BUTTON FOR ALL USERS
        Button qaButton = new Button("Student Q&A System");
        qaButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        qaButton.setOnAction(a -> {
            new StudentQAPage(databaseHelper, user).show(primaryStage);
        });
        
        if ("user".equals(user.getRole())) {
            Button reviewerButton = new Button("You are not a star reviewer... Yet!");
            reviewerButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #ffdd00; -fx-text-fill: black;");
            reviewerButton.setOnAction(a -> {
                    
            });
            grid.add(reviewerButton, 0, 1); 
        }
        
        if ("admin".equals(user.getRole())) {
            Button adminButton = new Button("Continue to Admin Page");
            adminButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #ff9900; -fx-text-fill: black;");
            adminButton.setOnAction(a -> {
                    new AdminHomePage(databaseHelper).show(primaryStage, user);
            });
            grid.add(adminButton, 0, 1); 
        }
        
        Button logout = new Button("logout");
        logout.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
        logout.setOnAction(a -> {
                new UserLoginPage(databaseHelper).show(primaryStage);
        });
        
        grid.add(welcomeLabel, 0, 0);
        grid.add(userButton, 0, 2);
        grid.add(qaButton, 0, 3);     // ADD THIS LINE - Q&A button
        grid.add(logout, 0, 4);        // CHANGE from row 3 to row 4
        
        Scene welcomeScene = new Scene(grid, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");
    }
}
