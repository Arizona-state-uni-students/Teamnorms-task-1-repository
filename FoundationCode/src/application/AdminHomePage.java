package application;

import java.sql.SQLException;
import java.util.Optional;

import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */
public class AdminHomePage {
	// Reference to the DatabaseHelper for database interactions
	private final DatabaseHelper databaseHelper;
	
	public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
	
	/**
     * Displays the admin home page in the primary stage. 
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param user The current admin user.
     */
    public void show(Stage primaryStage, User user) {
    	// Establish GUI Grid
    	GridPane grid = new GridPane();
    	//grid.setGridLinesVisible(true); // for testing
    	grid.setPadding(new Insets(10));
    	grid.setHgap(10); // Horizontal gap between columns
    	grid.setVgap(10); // Vertical gap between rows
    	grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    	
    	// Set background image
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
	    	new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
	    });
	    
	    // ==================== TEST USER GENERATION ====================
	    
	    // Create button container for user generation
	    HBox generateButtonBox = new HBox(10);
	    generateButtonBox.setAlignment(Pos.CENTER);
	    
	    // Single user generation button
	    Button generateOneButton = new Button("Generate One Test Admin");
	    generateOneButton.setStyle("-fx-font-size: 12px; -fx-padding: 8 15; -fx-background-color: #4CAF50; -fx-text-fill: white;");
	    generateOneButton.setOnAction(e -> {
	        int nextNum = getNextAdminNumber();
	        System.out.println("Generating single test admin: admin" + nextNum);
	        generateTestAdmins(1);
	    });
	    
	    // Multiple user generation button
	    Button generateMultipleButton = new Button("Generate Multiple Test Admins");
	    generateMultipleButton.setStyle("-fx-font-size: 12px; -fx-padding: 8 15; -fx-background-color: #2196F3; -fx-text-fill: white;");
	    generateMultipleButton.setOnAction(e -> showBatchGenerationDialog());
	    
	    generateButtonBox.getChildren().addAll(generateOneButton, generateMultipleButton);
	    
	    // Add all components to grid
	    grid.add(adminLabel, 0, 0);
        grid.add(userDatabase, 0, 1);
	    grid.add(inviteButton, 0, 2);
        grid.add(switchRoleButton, 0, 3);
        grid.add(logoutButton, 0, 4);
        grid.add(generateButtonBox, 0, 6);
	    
	    Scene adminScene = new Scene(grid, 800, 400);
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Admin Page");
    }
    
    // ==================== HELPER METHODS FOR USER GENERATION ====================
    
    /**
     * Finds the next available admin username number using existing database helper.
     * Checks from admin2 onwards and finds the first available number.
     * 
     * @return The next available admin number (e.g., if admin2, admin3 exist, returns 4)
     */
    private int getNextAdminNumber() {
        int nextNum = 2; // Start from admin2 (admin or admin1 is primary)
        
        // Use existing doesUserExist method
        while (databaseHelper.doesUserExist("admin" + nextNum)) {
            nextNum++;
            // Safety check to prevent infinite loop
            if (nextNum > 1000) {
                System.err.println("Warning: Reached admin1000, stopping search");
                break;
            }
        }
        
        return nextNum;
    }
    
    /**
     * Generates the specified number of test admin users.
     * Uses existing database helper methods to check for conflicts.
     * 
     * @param count Number of test admin users to generate
     */
    private void generateTestAdmins(int count) {
        int startNum = getNextAdminNumber();
        int successCount = 0;
        int failCount = 0;
        StringBuilder createdUsers = new StringBuilder();
        StringBuilder failedUsers = new StringBuilder();
        
        for (int i = 0; i < count; i++) {
            int adminNum = startNum + i;
            String username = "admin" + adminNum;
            
            // Double-check user doesn't exist (shouldn't happen with our logic, but safety first)
            if (databaseHelper.doesUserExist(username)) {
                failCount++;
                failedUsers.append("  • ").append(username).append(" (already exists)\n");
                System.err.println("Skipping " + username + " - already exists");
                continue;
            }
            
            try {
                User newUser = new User(
                    username,
                    "test" + adminNum + "@sqaas.test",
                    "Password1!",
                    "Student",
                    "Test",
                    "Admin",
                    "T",
                    1
                );
                
                boolean success = databaseHelper.register(newUser);
                
                if (success) {
                    successCount++;
                    createdUsers.append("  • ").append(username).append(" (test").append(adminNum).append("@sqaas.test)\n");
                    System.out.println("✓ Generated: " + username);
                } else {
                    failCount++;
                    failedUsers.append("  • ").append(username).append(" (registration failed)\n");
                    System.err.println("✗ Failed to generate: " + username);
                }
                
            } catch (SQLException ex) {
                failCount++;
                failedUsers.append("  • ").append(username).append(" (error: ").append(ex.getMessage()).append(")\n");
                System.err.println("Error generating " + username + ": " + ex.getMessage());
            }
        }
        
        // Show detailed summary
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test User Generation Complete");
        alert.setHeaderText(String.format("Created %d of %d requested user(s)", successCount, count));
        
        StringBuilder content = new StringBuilder();
        if (successCount > 0) {
            content.append("✓ Successfully created:\n").append(createdUsers.toString()).append("\n");
        }
        if (failCount > 0) {
            content.append("✗ Failed:\n").append(failedUsers.toString()).append("\n");
        }
        content.append("Default password for all: Password1!");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    /**
     * Shows dialog for batch admin generation with count selection.
     * Allows user to select between 1-10 users to generate using a Spinner control.
     */
    private void showBatchGenerationDialog() {
        // Create custom dialog
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Generate Multiple Test Admins");
        dialog.setHeaderText("Select how many test admin users to create");
        
        // Set button types
        ButtonType generateButtonType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);
        
        // Create content
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        
        // Label
        Label instructionLabel = new Label("Number of test admins to generate:");
        instructionLabel.setStyle("-fx-font-size: 14px;");
        
        // Spinner for count selection (1-10)
        Spinner<Integer> countSpinner = new Spinner<>(1, 10, 5); // min=1, max=10, initial=5
        countSpinner.setEditable(true);
        countSpinner.setPrefWidth(100);
        countSpinner.setStyle("-fx-font-size: 14px;");
        
        // Info label showing next username
        int nextNum = getNextAdminNumber();
        Label infoLabel = new Label(String.format(
            "Will create: admin%d through admin%d", 
            nextNum, 
            nextNum + countSpinner.getValue() - 1
        ));
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        // Update info label when spinner value changes
        countSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            int next = getNextAdminNumber();
            infoLabel.setText(String.format(
                "Will create: admin%d through admin%d", 
                next, 
                next + newVal - 1
            ));
        });
        
        // Additional info
        Label passwordLabel = new Label("All users will have password: Password1!");
        passwordLabel.setStyle("-fx-font-size: 11px; -fx-font-style: italic; -fx-text-fill: #888;");
        
        content.getChildren().addAll(instructionLabel, countSpinner, infoLabel, passwordLabel);
        dialog.getDialogPane().setContent(content);
        
        // Convert result to integer when Generate is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                return countSpinner.getValue();
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(count -> {
            if (count > 0) {
                generateTestAdmins(count);
            }
        });
    }
    
    /**
     * Displays error alert.
     * 
     * @param message Error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Generation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
