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
import javafx.scene.paint.Color;
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
    	// Establish GUI Grid
    	GridPane grid = new GridPane();
    	grid.setPadding(new Insets(10));
    	grid.setHgap(10); // Horizontal gap between columns
    	grid.setVgap(10); // Vertical gap between rows
    	
    	// set background image
    	Image backgroundImage = new Image(getClass().getResource("/titlescreen.png").toExternalForm());
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
        
        // Small (?) button to explain FSM + dual-use password field
        Button helpBtn = new Button("?");
        helpBtn.setStyle("-fx-font-size: 10px; -fx-background-color: transparent; -fx-text-fill: #0099ff; -fx-padding: 0 4;");
        helpBtn.setFocusTraversable(false);
        
        // FSM explanation text shown when (?) is clicked
        final String fsmHelp =
                "Login Password Field (FSM):\n" +
                "• Input may be your PERMANENT password or a one-time password (OTP) issued by an admin.\n\n";
        
        helpBtn.setOnAction(ev -> {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("How sign-in works");
            info.setHeaderText("Password field accepts Permanent or One-Time Password (OTP)");
            info.setContentText(fsmHelp);
            info.showAndWait();
        });
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Input field for password
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Type your password");
        passwordField.setMaxWidth(250);
        passwordField.setTooltip(new Tooltip("Enter your permanent password or a one-time password (OTP) from an admin"));
        
        // Put label and (?) on one line
        HBox passwordHeader = new HBox(6, passwordLabel, helpBtn);
        passwordHeader.setAlignment(javafx.geometry.Pos.BASELINE_LEFT);

     
        // Create Buttons
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(150); // Set button width
        loginButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");
        Button setupButton = new Button("Setup");
        setupButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: transparent; -fx-text-fill: #0099ff;");
        // Wrap buttons in an HorizontalBox for side-by-side placement
        HBox buttonBox = new HBox(10, setupButton, loginButton); // 10px spacing between buttons
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER); // Center align buttons
        
        // Button for "fresh reset." Deletes and rebuilds database
        Button truncateButton = new Button("Reset DB");
        truncateButton.setOnAction(a -> {
            databaseHelper.truncate();
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
        });
        
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 0 20; -fx-background-color: transparent; -fx-text-fill: #2c2c2c;");
        
        grid.add(Header, 0, 0); // Column 0, Row 0
        grid.add(userNameLabel, 0, 1); // Column 0, Row 1
        grid.add(userNameField, 0, 2);
        grid.add(passwordHeader, 0, 3);
        grid.add(passwordField, 0, 4); 
        grid.add(buttonBox, 0, 5);
        grid.add(errorLabel, 0, 6);
        grid.add(truncateButton, 0, 7);
        grid.add(quitButton, 0, 17);
        GridPane.setHalignment(errorLabel, javafx.geometry.HPos.RIGHT);
        GridPane.setHalignment(quitButton, javafx.geometry.HPos.CENTER);
        
        loginButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String secret   = passwordField.getText(); // could be permanent password OR OTP
            try {
                // 1) normal login attempt with permanent password
                User user = new User(userName, secret, "");
                String role = databaseHelper.getUserRole(userName);

                if (role != null) {
                    user.setRole(role);
                    if (databaseHelper.login(user)) {
                        if (!role.equals("user")) {
                            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                        } else {
                            new UserHomePage(databaseHelper).show(primaryStage, user);
                        }
                        return;
                    }

                    // 2) fallback: treat the entered value as OTP
                    if (databaseHelper.isOtpValid(userName, secret)) {
                        showForceResetDialog(primaryStage, userName); // will consume OTP after setting new password
                    } else {
                        errorLabel.setText("Error logging in. Invalid password?");
                    }
                } else {
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
    
    // Forces the user to set a new permanent password, then returns them to Login.
    private void showForceResetDialog(Stage owner, String username) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Change Password");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField p1 = new PasswordField(); p1.setPromptText("New password"); p1.setMaxWidth(250);
        PasswordField p2 = new PasswordField(); p2.setPromptText("Confirm new password"); p2.setMaxWidth(250);

        Label hint = new Label("You must set a new password to continue.");
        hint.setStyle("-fx-font-size: 12px;");

        VBox box = new VBox(10, hint, p1, p2);
        box.setPadding(new Insets(12));
        dlg.getDialogPane().setContent(box);

        Button ok = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String a = p1.getText(), b = p2.getText();
            if (a == null || a.isBlank() || !a.equals(b)) {
                e.consume();
                new Alert(Alert.AlertType.ERROR, "Passwords must match and not be empty.").showAndWait();
                return;
            }
            try {
                // 1) update permanent password
                if (databaseHelper.updateUserPassword(username, a)) {
                    // 2) clear the one-time password so it cannot be reused
                    databaseHelper.consumeOtp(username);
                    // 3) notify and return to login
                    new Alert(Alert.AlertType.INFORMATION, "Password updated. Please log in.").showAndWait();
                    dlg.close();
                    new UserLoginPage(databaseHelper).show(owner);  // GUI Container: redirect to Login
                } else {
                    e.consume();
                    new Alert(Alert.AlertType.ERROR, "Failed to update password.").showAndWait();
                }
            } catch (SQLException ex) {
                e.consume();
                new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
            }
        });

        dlg.initOwner(owner);
        dlg.showAndWait();
    }

}
