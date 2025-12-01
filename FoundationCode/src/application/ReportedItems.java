package application;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;



/**
 * ReportedItems class represents the user interface for the reported item page.
 */
public class ReportedItems {
	private final DatabaseHelper databaseHelper;
	private GridPane grid;
	private GridPane topGrid;
	private GridPane msgGrid;
	private Label staffLabel;
	private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
	
	public ReportedItems(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}
	
	
	/**
	 * Displays the ReportedItems page in the primary stage.
	 * @param primaryStage the primary stage where the scene will be displayed.
	 * @param user User currently using system
	 * @throws SQLException if database error occurs
	 */
	public void show(Stage primaryStage, User user) throws SQLException {
		// Establish GUI Grid
    	grid = new GridPane();
    	//grid.setGridLinesVisible(true); // for testing
    	grid.setPadding(new Insets(10));
    	grid.setHgap(10); // Horizontal gap between columns
    	grid.setVgap(10); // Vertical gap between rows
    	grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    	
    	// Grid for top buttons
    	topGrid = new GridPane();
    	topGrid.setPadding(new Insets(10));
    	topGrid.setHgap(10); // Horizontal gap between columns
    	topGrid.setVgap(10); // Vertical gap between rows
    	topGrid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    	
    	// Grid to hold messages
    	msgGrid = new GridPane();
    	msgGrid.setPadding(new Insets(10));
    	msgGrid.setHgap(10); // Horizontal gap between columns
    	msgGrid.setVgap(10); // Vertical gap between rows
    	msgGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    	
    	
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
    	

    	// Top label
	    staffLabel = new Label("Hello, Staff!");
	    staffLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    
	    // Back button
	    Button backButton = new Button("Go back");
	    backButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
	    backButton.setOnAction(a -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
	    });
	    

	    // Clear grids
	    topGrid.getChildren().clear();
	    grid.getChildren().clear();

	    // Load all messages by default
	    try {
			loadAllObjects(user, databaseHelper.getFlaggedObjects());
		} catch (SQLException e) {
			showAlert("Error", "Failed to load messages", AlertType.ERROR);
			e.printStackTrace();
		}
	    
	    
	    // Add all buttons to topGrid
	    topGrid.add(backButton, 0, 0);
	    
	    // Add label, topGrid and msgGrid to the main grid
	    grid.add(staffLabel, 0, 0);  
        grid.add(topGrid, 0, 1);
        grid.add(msgGrid, 0, 2);
        
	    
		// Make page scrollable
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
	    
	    Scene staffScene = new Scene(scrollPane, 800, 400);
	    primaryStage.setScene(staffScene);
	    primaryStage.setTitle("Staff Page");
    }
	
	/**
	 * Loads all objects in database onto reported items page.
	 * 
	 * @param user User currently using system.
	 * @param messaages List of messages to load.
	 * @throws SQLException if database error occurs.
	 */
	public void loadAllObjects(User user, List<Object> ReportedItems) throws SQLException {
		// Clear msgGrid
		msgGrid.getChildren().clear();

		int curIndex = 1;
		// Loop through messages
		for(Object m : ReportedItems) {
			if(m instanceof Question) {
				Question targetMessage = (Question) m;
				
				// Info label
				String postedBy = targetMessage.getAskedBy();
				String timestamp = targetMessage.getCreatedAt().format(TS);
				Label infoLabel = new Label("Message Type: Question" + " • Posted By: " + postedBy + " • On: " + timestamp);
	            infoLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#0099ff;");
				
	            // Grid for message
				GridPane messageGrid = new GridPane();
				messageGrid.setPadding(new Insets(10));
				messageGrid.setHgap(10); // Horizontal gap between columns
				messageGrid.setVgap(10); // Vertical gap between rows
				messageGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				messageGrid.setStyle("-fx-background-color: #b5b5b5;");
				messageGrid.setMinWidth(700);
				
				// Grid for message buttons
				GridPane messageBtnGrid = new GridPane();
				messageBtnGrid.setPadding(new Insets(10));
				messageBtnGrid.setHgap(10); // Horizontal gap between columns
				messageBtnGrid.setVgap(10); // Vertical gap between rows
				messageBtnGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				
				// Label containing message contents
				Label message = new Label(targetMessage.getContent());
				message.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

	            
	            // Delete message button
				Button deleteMsgBtn = new Button("Delete Question");
				deleteMsgBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #FF0000; -fx-text-fill: white;");
				deleteMsgBtn.setOnAction(a -> {
					try {
						databaseHelper.deleteQuestion(targetMessage.getId(), user.getUserName());
						System.out.println("Question deleted");
						loadAllObjects(user, databaseHelper.getFlaggedObjects());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    });
                
                
				// Mark as Unflagged button
				Button markAsUnflaggedBtn = new Button("Keep Question");
				markAsUnflaggedBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #388E3C; -fx-text-fill: white;");
				markAsUnflaggedBtn.setOnAction(a -> {
						try {
							databaseHelper.markQuestionFlagged(targetMessage.getId(), false);
							targetMessage.setIsFlagged(false);
							loadAllObjects(user, databaseHelper.getFlaggedObjects());
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			    );

				messageBtnGrid.add(deleteMsgBtn, 0, 0);
				messageBtnGrid.add(markAsUnflaggedBtn, 1, 0);
				
				// Add stuff to grids
				messageGrid.add(infoLabel, 0, 0);
				messageGrid.add(message, 0, 1);
				messageGrid.add(messageBtnGrid, 0, 2);
				msgGrid.add(messageGrid, 0, curIndex + 1);
				curIndex++;
			}
			if(m instanceof Answer) {
				Answer targetMessage = (Answer) m;
				
				// Info label
				String postedBy = targetMessage.getAnsweredBy();
				String timestamp = targetMessage.getCreatedAt().format(TS);
				Label infoLabel = new Label("Message Type: Answer" + " • Posted By: " + postedBy + " • On: " + timestamp);
	            infoLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#0099ff;");
				
	            // Grid for message
				GridPane messageGrid = new GridPane();
				messageGrid.setPadding(new Insets(10));
				messageGrid.setHgap(10); // Horizontal gap between columns
				messageGrid.setVgap(10); // Vertical gap between rows
				messageGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				messageGrid.setStyle("-fx-background-color: #b5b5b5;");
				messageGrid.setMinWidth(700);
				
				// Grid for message buttons
				GridPane messageBtnGrid = new GridPane();
				messageBtnGrid.setPadding(new Insets(10));
				messageBtnGrid.setHgap(10); // Horizontal gap between columns
				messageBtnGrid.setVgap(10); // Vertical gap between rows
				messageBtnGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				
				// Label containing message contents
				Label message = new Label(targetMessage.getContent());
				message.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

	            
	            // Delete message button
				Button deleteMsgBtn = new Button("Delete Answer");
				deleteMsgBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #FF0000; -fx-text-fill: white;");
				deleteMsgBtn.setOnAction(a -> {
					try {
						databaseHelper.deleteAnswer(targetMessage.getId(), user.getUserName());
						System.out.println("Answer deleted");
						loadAllObjects(user, databaseHelper.getFlaggedObjects());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    });
	            
	            
				// Mark as Unflagged button
				Button markAsUnflaggedBtn = new Button("Keep Answer");
				markAsUnflaggedBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #388E3C; -fx-text-fill: white;");
				markAsUnflaggedBtn.setOnAction(a -> {
						try {
							databaseHelper.markAnswerFlagged(targetMessage.getId(), false);
							targetMessage.setIsFlagged(false);
							loadAllObjects(user, databaseHelper.getFlaggedObjects());
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			    );

				messageBtnGrid.add(deleteMsgBtn, 0, 0);
				messageBtnGrid.add(markAsUnflaggedBtn, 1, 0);
				
				// Add stuff to grids
				messageGrid.add(infoLabel, 0, 0);
				messageGrid.add(message, 0, 1);
				messageGrid.add(messageBtnGrid, 0, 2);
				msgGrid.add(messageGrid, 0, curIndex + 1);
				curIndex++;
			}			
			if(m instanceof Review) {
				Review targetMessage = (Review) m;
				
				// Info label
				String postedBy = targetMessage.getWrittenBy();
				String timestamp = targetMessage.getCreatedAt().format(TS);
				Label infoLabel = new Label("Message Type: Review" + " • Posted By: " + postedBy + " • On: " + timestamp);
	            infoLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#0099ff;");
				
	            // Grid for message
				GridPane messageGrid = new GridPane();
				messageGrid.setPadding(new Insets(10));
				messageGrid.setHgap(10); // Horizontal gap between columns
				messageGrid.setVgap(10); // Vertical gap between rows
				messageGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				messageGrid.setStyle("-fx-background-color: #b5b5b5;");
				messageGrid.setMinWidth(700);
				
				// Grid for message buttons
				GridPane messageBtnGrid = new GridPane();
				messageBtnGrid.setPadding(new Insets(10));
				messageBtnGrid.setHgap(10); // Horizontal gap between columns
				messageBtnGrid.setVgap(10); // Vertical gap between rows
				messageBtnGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				
				// Label containing message contents
				Label message = new Label(targetMessage.getReviewText());
				message.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

	            
	            // Delete message button
				Button deleteMsgBtn = new Button("Delete Review");
				deleteMsgBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #FF0000; -fx-text-fill: white;");
				deleteMsgBtn.setOnAction(a -> {
					try {
						databaseHelper.deleteReviewById(targetMessage.getId());
						System.out.println("Review deleted");
						loadAllObjects(user, databaseHelper.getFlaggedObjects());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    });
	            
	            
				// Mark as Unflagged button
				Button markAsUnflaggedBtn = new Button("Keep Review");
				markAsUnflaggedBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #388E3C; -fx-text-fill: white;");
				markAsUnflaggedBtn.setOnAction(a -> {
						try {
							databaseHelper.markReviewFlagged(targetMessage.getId(), false);
							targetMessage.setIsFlagged(false);
							loadAllObjects(user, databaseHelper.getFlaggedObjects());
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			    );

				messageBtnGrid.add(deleteMsgBtn, 0, 0);
				messageBtnGrid.add(markAsUnflaggedBtn, 1, 0);
				
				// Add stuff to grids
				messageGrid.add(infoLabel, 0, 0);
				messageGrid.add(message, 0, 1);
				messageGrid.add(messageBtnGrid, 0, 2);
				msgGrid.add(messageGrid, 0, curIndex + 1);
				curIndex++;
			}
			if(m instanceof ReviewReply) {
				ReviewReply targetMessage = (ReviewReply) m;
				
				// Info label
				String postedBy = targetMessage.getRepliedBy();
				String timestamp = targetMessage.getCreatedAt().format(TS);
				Label infoLabel = new Label("Message Type: Review Reply" + " • Posted By: " + postedBy + " • On: " + timestamp);
	            infoLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#0099ff;");
				
	            // Grid for message
				GridPane messageGrid = new GridPane();
				messageGrid.setPadding(new Insets(10));
				messageGrid.setHgap(10); // Horizontal gap between columns
				messageGrid.setVgap(10); // Vertical gap between rows
				messageGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				messageGrid.setStyle("-fx-background-color: #b5b5b5;");
				messageGrid.setMinWidth(700);
				
				// Grid for message buttons
				GridPane messageBtnGrid = new GridPane();
				messageBtnGrid.setPadding(new Insets(10));
				messageBtnGrid.setHgap(10); // Horizontal gap between columns
				messageBtnGrid.setVgap(10); // Vertical gap between rows
				messageBtnGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				
				// Label containing message contents
				Label message = new Label(targetMessage.getReplyText());
				message.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

	            
	            // Delete message button
				Button deleteMsgBtn = new Button("Delete Review Reply");
				deleteMsgBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #FF0000; -fx-text-fill: white;");
				deleteMsgBtn.setOnAction(a -> {
					try {
						databaseHelper.deleteReviewReplyById(targetMessage.getId());
						System.out.println("Review Reply deleted");
						loadAllObjects(user, databaseHelper.getFlaggedObjects());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    });
	            
	            
				// Mark as Unflagged button
				Button markAsUnflaggedBtn = new Button("Keep Review Reply");
				markAsUnflaggedBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #388E3C; -fx-text-fill: white;");
				markAsUnflaggedBtn.setOnAction(a -> {
						try {
							databaseHelper.markReviewReplyAsFlagged(targetMessage.getId(), false);
							targetMessage.setIsFlagged(false);
							loadAllObjects(user, databaseHelper.getFlaggedObjects());
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			    );

				messageBtnGrid.add(deleteMsgBtn, 0, 0);
				messageBtnGrid.add(markAsUnflaggedBtn, 1, 0);
				
				// Add stuff to grids
				messageGrid.add(infoLabel, 0, 0);
				messageGrid.add(message, 0, 1);
				messageGrid.add(messageBtnGrid, 0, 2);
				msgGrid.add(messageGrid, 0, curIndex + 1);
				curIndex++;
			}
			if(m instanceof DatabaseHelper.PrivateMessage) {
				DatabaseHelper.PrivateMessage targetMessage = (DatabaseHelper.PrivateMessage) m;
				
				// Info label
				String postedBy = targetMessage.getSender();
				String timestamp = targetMessage.getCreatedAt().format(TS);
				Label infoLabel = new Label("Message Type: Private Message" + " • Posted By: " + postedBy + " • On: " + timestamp);
	            infoLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#0099ff;");
				
	            // Grid for message
				GridPane messageGrid = new GridPane();
				messageGrid.setPadding(new Insets(10));
				messageGrid.setHgap(10); // Horizontal gap between columns
				messageGrid.setVgap(10); // Vertical gap between rows
				messageGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				messageGrid.setStyle("-fx-background-color: #b5b5b5;");
				messageGrid.setMinWidth(700);
				
				// Grid for message buttons
				GridPane messageBtnGrid = new GridPane();
				messageBtnGrid.setPadding(new Insets(10));
				messageBtnGrid.setHgap(10); // Horizontal gap between columns
				messageBtnGrid.setVgap(10); // Vertical gap between rows
				messageBtnGrid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				
				// Label containing message contents
				Label message = new Label(targetMessage.getContent());
				message.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

	            
	            // Delete message button
				Button deleteMsgBtn = new Button("Delete Private Answer");
				deleteMsgBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #FF0000; -fx-text-fill: white;");
				deleteMsgBtn.setOnAction(a -> {
					try {
						databaseHelper.deletePrivateAnswerById(targetMessage.getId());
						System.out.println("Private Answer deleted");
						loadAllObjects(user, databaseHelper.getFlaggedObjects());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    });
	            
	            
				// Mark as Unflagged button
				Button markAsUnflaggedBtn = new Button("Keep Private Answer");
				markAsUnflaggedBtn.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #388E3C; -fx-text-fill: white;");
				markAsUnflaggedBtn.setOnAction(a -> {
						try {
							databaseHelper.markFeedbackAsFlagged(targetMessage.getId(), false);
							targetMessage.setIsFlagged(false);
							loadAllObjects(user, databaseHelper.getFlaggedObjects());
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			    );

				messageBtnGrid.add(deleteMsgBtn, 0, 0);
				messageBtnGrid.add(markAsUnflaggedBtn, 1, 0);
				
				// Add stuff to grids
				messageGrid.add(infoLabel, 0, 0);
				messageGrid.add(message, 0, 1);
				messageGrid.add(messageBtnGrid, 0, 2);
				msgGrid.add(messageGrid, 0, curIndex + 1);
				curIndex++;
			}
		}
		

	}

	/**
     * Method to show an alert to the user.
     * 
     * @param title Title of the alert.
     * @param content Content of the alert.
     * @param type AlertType
     */
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
