package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.SQLException;

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
    
    /**
     * Displays the welcome login page in the primary stage. 
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param user User currently using the system.
     */
    public void show(Stage primaryStage, User user) {
        Label welcomeLabel = new Label("Hello, "+user.getFullName()+"!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        VBox vbox = new VBox(6);
        
        Button userButton = new Button("My User Page");
        userButton.setStyle(colors.BASIC + colors.STUDENT_PRIMARY);
        userButton.setOnAction(a -> {
                try {
					new UserHomePage(databaseHelper).show(primaryStage, user);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        });
        
        Button qaButton = new Button("Student Q&A System");
        qaButton.setStyle(colors.BASIC);
        qaButton.setOnAction(a -> {
            new StudentQAPage(databaseHelper, user).show(primaryStage);
        });
        
        
        Button messagesButton = new Button("Direct Messages");
        messagesButton.setStyle(colors.BASIC);
        messagesButton.setOnAction(a -> {
            new DirectMessages(databaseHelper, user).show(primaryStage);
        });
        
        Button logout = new Button("logout");
        logout.setStyle(colors.BASIC);
        logout.setOnAction(a -> {
                new UserLoginPage(databaseHelper).show(primaryStage);
        });
        
        Button reviewerButton = new Button("Reviewer Page");
        reviewerButton.setStyle(colors.BASIC + colors.REVIEWER_PRIMARY);
        reviewerButton.setOnAction(a -> {
//        		new AdminHomePage(databaseHelper).show(primaryStage, user);
        });
        reviewerButton.setVisible(user.getPrivileges()>=2);
        Button adminButton = new Button("Admin Page");
        adminButton.setStyle(colors.BASIC + colors.ADMIN_PRIMARY);
        adminButton.setOnAction(a -> {
                new AdminHomePage(databaseHelper).show(primaryStage, user);
        });
        adminButton.setVisible(user.getPrivileges()>=99);
        Button staffButton = new Button("Staff/Instructor Page");
        staffButton.setStyle(colors.BASIC + colors.STAFF_PRIMARY);
        staffButton.setOnAction(a -> {
                //new AdminHomePage(databaseHelper).show(primaryStage, user);
        });
        
        
        
        adminButton.setManaged(user.getPrivileges()>=5);
        staffButton.setManaged(user.getPrivileges()>=3);
        reviewerButton.setManaged(user.getPrivileges()>=2);
        
        // Set background image based on user privileges
        Image backgroundImage = new Image(getClass().getResource("/blankuser.png").toExternalForm());
        if(user.getPrivileges()==1) {backgroundImage = new Image(getClass().getResource("/blankstudent.png").toExternalForm());}
        if(user.getPrivileges()==2) {backgroundImage = new Image(getClass().getResource("/blankreviewer.png").toExternalForm());}
        if(user.getPrivileges()==3) {backgroundImage = new Image(getClass().getResource("/blankstaff.png").toExternalForm());}
        if(user.getPrivileges()==4) {backgroundImage = new Image(getClass().getResource("/blankinstructor.png").toExternalForm());}
        if(user.getPrivileges()==99) {backgroundImage = new Image(getClass().getResource("/blankadmin.png").toExternalForm());}
        
        BackgroundImage backgroundImg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
        );
        
        vbox.setBackground(new Background(backgroundImg));
        
        // Build VBox layout
        HBox hbox = new HBox(10, userButton, reviewerButton, staffButton, adminButton);
        hbox.setAlignment(Pos.TOP_CENTER);
        welcomeLabel.setStyle("-fx-padding: 60 0;");
        
        // Add base elements to VBox in order
        vbox.getChildren().addAll(hbox, welcomeLabel, qaButton, messagesButton);
        
        // Add Tickets button for Staff (3), Instructor (4), and Admin (99)
        if(user.getPrivileges() == 3 || user.getPrivileges() == 4 || user.getPrivileges() == 99) {
            Button ticketsButton = new Button("Tickets");
            ticketsButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #FF5722; -fx-text-fill: white;");
            ticketsButton.setOnAction(a -> {
                new TicketsPage(databaseHelper, user).show(primaryStage, user);
            });
            vbox.getChildren().add(ticketsButton);
        }
        
        // Add logout button at the end
        vbox.getChildren().add(logout);
        
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setPadding(new Insets(20));
        Scene welcomeScene = new Scene(vbox, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");
    }
}
