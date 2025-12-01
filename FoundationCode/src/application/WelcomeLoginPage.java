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
import java.util.List;

import databasePart1.*;
import databasePart1.DatabaseHelper.ConversationSummary;

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
        qaButton.setStyle(colors.BASIC + colors.STUDENT_PRIMARY);
        qaButton.setOnAction(a -> {
            new StudentQAPage(databaseHelper, user).show(primaryStage, 0, null);
        });
    	int unreadMessages = 0;
        try {
			List<ConversationSummary> conversations = databaseHelper.getConversationList(user.getUserName());
			for (ConversationSummary conv : conversations) {
				unreadMessages += conv.getUnreadCount();
			}
			//conv.getUnreadCount() > 9 ? "9+" : String.valueOf(conv.getUnreadCount())
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Button messagesButton = new Button("("+unreadMessages+") Direct Messages");
        if(unreadMessages==0) {messagesButton.setText("Direct Messages");}
        messagesButton.setStyle(colors.BASIC);
        messagesButton.setOnAction(a -> {
            new DirectMessages(databaseHelper, user).show(primaryStage, null);
        });
        
        Button logout = new Button("logout");
        logout.setStyle(colors.BASIC);
        logout.setOnAction(a -> {
                new UserLoginPage(databaseHelper).show(primaryStage);
        });
        
        Button reviewerButton = new Button("Reviewers");
        reviewerButton.setStyle(colors.BASIC + colors.REVIEWER_PRIMARY);
        reviewerButton.setOnAction(a -> {
        	StudentQAPage qaPage = new StudentQAPage(databaseHelper, user);
            qaPage.show(primaryStage, 3, null);
        });
        
        Button adminButton = new Button("Admin Page");
        adminButton.setStyle(colors.BASIC + colors.ADMIN_PRIMARY);
        adminButton.setOnAction(a -> {
                new AdminHomePage(databaseHelper).show(primaryStage, user);
        });

        Button staffButton = new Button("Staff/Instructor Page");
        staffButton.setStyle(colors.BASIC + colors.STAFF_PRIMARY);
        staffButton.setOnAction(a -> {
                try {
					new staffpage(databaseHelper).show(primaryStage, user);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        });
        int total = 0;
        try {
			total = databaseHelper.getFlaggedObjects().size();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Button Reported = new Button("("+total+") Reported Items");
        if(total==0) {Reported.setText("Reported Items");}
        Reported.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #FF5722; -fx-text-fill: white;");
        Reported.setOnAction(a -> {
            try {
				new ReportedItems(databaseHelper).show(primaryStage, user);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });
        int totaltickets = 0;
        try {
			List<Ticket> tickets = databaseHelper.getOpenTickets();
	        totaltickets = tickets.size();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // Tickets button for Staff, Instructor, and Admin
        Button ticketsButton = new Button("("+totaltickets+") Tickets");
        if(totaltickets==0) {ticketsButton.setText("Tickets");}
        ticketsButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #FF5722; -fx-text-fill: white;");
        ticketsButton.setOnAction(a -> {
            new TicketsPage(databaseHelper, user).show(primaryStage, user);
            
        });
        
        adminButton.setManaged(user.getPrivileges()>=5);
        staffButton.setManaged(user.getPrivileges()>=3);
        ticketsButton.setManaged(user.getPrivileges()>=3);
        Reported.setManaged(user.getPrivileges()>=3);
        //reviewerButton.setManaged(user.getPrivileges()>=2);
        
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
        
        VBox vbox = new VBox(6);
        vbox.setBackground(new Background(backgroundImg));
        
        HBox hbox = new HBox(10, userButton, reviewerButton, staffButton, adminButton);
        hbox.setAlignment(Pos.TOP_CENTER);
        welcomeLabel.setStyle("-fx-padding: 30 0;");
        
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-padding: 10 0;");
        vbox.getChildren().addAll(hbox, sep1, messagesButton, qaButton, welcomeLabel, Reported, ticketsButton, logout);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setPadding(new Insets(20));
        Scene welcomeScene = new Scene(vbox, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");
    }
}
