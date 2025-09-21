package application;

import databasePart1.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

/**
 * FirstPage class represents the initial screen for the first user.
 * It prompts the user to set up administrator access and navigate to the setup screen.
 */
public class FirstPage {
	
	// Reference to the DatabaseHelper for database interactions
	private final DatabaseHelper databaseHelper;
	public FirstPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

	/**
     * Displays the first page in the provided primary stage. 
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	// Establish GUI Grid
    	GridPane grid = new GridPane();
    	//grid.setGridLinesVisible(true); // for testing
    	grid.setPadding(new Insets(10));
    	grid.setHgap(10); // Horizontal gap between columns
    	grid.setVgap(40); // Vertical gap between rows
    	grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    	
    	// set background image
    	Image backgroundImage = new Image(getClass().getResource("/sqaas.png").toExternalForm());
    	BackgroundImage backgroundImg = new BackgroundImage(
    			backgroundImage,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundRepeat.NO_REPEAT,
    			BackgroundPosition.CENTER,
    			new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
    			);
    	grid.setBackground(new Background(backgroundImg));
    	
    	// Label to display the welcome message for the first user
	    Label headerLabel = new Label("Welcome to Student's Question and Answer System (sQaaS)");
	    headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    String admin="The first run of sQaaS will establish an administrator account. Continue to be an administrator.";
	    Label adminLabel = new Label(admin);
	    adminLabel.setStyle("-fx-font-size: 16px;");
	    adminLabel.setWrapText(true);
        adminLabel.setMaxWidth(300); // Restrict width to prevent overflow
	    
	    Button continueButton = new Button("Continue");
	    continueButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
	    continueButton.setOnAction(a -> {
	        new AdminSetupPage(databaseHelper).show(primaryStage);
	        
	    });
	    Button quitButton = new Button("Quit");
	    quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: transparent; -fx-text-fill: black;");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
	    HBox buttonBox = new HBox(10, quitButton, continueButton); // 10px spacing between buttons
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER); // Center align buttons

	    grid.add(headerLabel, 0, 0); // Column 0, Row 0
        grid.add(adminLabel, 0, 1); // Column 0, Row 1
	    grid.add(buttonBox, 0, 2); // Column 0, Row 2
	    GridPane.setHalignment(adminLabel, javafx.geometry.HPos.RIGHT);
	    Scene firstPageScene = new Scene(grid, 800, 400);


	    // Set the scene to primary stage
	    primaryStage.setScene(firstPageScene);
	    primaryStage.setTitle("sQaaS First Run");
	    primaryStage.setResizable(false);
    	primaryStage.show();
    }
}