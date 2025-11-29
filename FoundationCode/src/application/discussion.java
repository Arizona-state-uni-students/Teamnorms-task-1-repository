package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.paint.Color;

/**
 * Discussion provides the interface for students to interact with each other.
 * There are public and private channels restricted by privileges.
 */
public class discussion {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private VBox mainLayout;
    private ScrollPane scrollPane;
    private int channel = -1;
    ComboBox<String> roleDropdown;
    VBox messagesContainer;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    /**
     * Label for privilege
     */
    String[] visibilityLabels = {"Public", "Reviewer+", "Staff+", "Instructor+", "Admin Only"};
    /**
     *  Initializes dicussion page
     * @param databaseHelper database object
     * @param currentUser logged in user
     */
    public discussion(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    /**
     * Displays the discussion stage. 
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
        mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(12);
        mainLayout.setPrefSize(1100, 700);

        // Header
        Label title = new Label("Discussion");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #2c3e50");
        
        Button loadallchannels = new Button("Load All Available Channels");
        loadallchannels.setOnAction(e->{
        	channel=-1;
        	refreshMessages(channel);
        });

        HBox header = new HBox(10, title, loadallchannels);
        header.setAlignment(Pos.CENTER_LEFT);

        // Input box at the bottom
        TextField inputField = new TextField();
        inputField.setPromptText("enter text here...");
        inputField.setStyle("-fx-background-radius: 25; -fx-padding: 12;");
        inputField.setPrefWidth(680);

        inputField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 120) {
                inputField.setText(oldText);
                inputField.positionCaret(oldText.length());
                inputField.setStyle("-fx-background-radius: 25; -fx-padding: 12; -fx-background-color: #ffebee; -fx-border-color: #e74c3c; -fx-border-width: 2;");
            }else {
                inputField.setStyle("-fx-background-radius: 25; -fx-padding: 12;");
            }
        });
        
        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        sendBtn.setPrefWidth(100);
        sendBtn.setOnAction(e->{
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                try {//0 for public message
                    databaseHelper.postDiscussion(currentUser.getUid(), currentUser.getUserName(), text, 0);
                    inputField.clear();
                    refreshMessages(0);
//                    refreshMessages(messagesContainer);
                } catch (SQLException ex) {
                    showAlert("Error", "Could not send message: " + ex.getMessage());
                }
        }});
        //generates what channel your message can go to
        roleDropdown = new ComboBox<>();
        for (int i = 0; i < visibilityLabels.length; i++) {
            if (currentUser.getPrivileges() >= i) {
                roleDropdown.getItems().add(visibilityLabels[i]);
            }
        }
        roleDropdown.getSelectionModel().selectFirst();
        roleDropdown.setPromptText("All Available");
        roleDropdown.setOnAction(e -> {
        	channel = roleDropdown.getSelectionModel().getSelectedIndex();
        	if(channel==5) {channel=-1;}
        	refreshMessages(channel);
        });
        
        HBox inputBox = new HBox(10, roleDropdown, inputField, sendBtn);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(15));
        inputBox.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0;");
        
        
        inputField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                        event.consume(); // prevent the Enter from creating a new line
                        String text = inputField.getText().trim();
                        if (!text.isEmpty()) {
                            try {//0 for public message
                                databaseHelper.postDiscussion(currentUser.getUid(), currentUser.getUserName(), text, roleDropdown.getSelectionModel().getSelectedIndex());
                                inputField.clear();
                                refreshMessages(channel);
//                                refreshMessages(messagesContainer);
                            } catch (SQLException ex) {
                                showAlert("Error", "Could not send message: " + ex.getMessage());
                            }}
                    break;
                default:
                    break;
            }
        });
        // Container for messages
        messagesContainer = new VBox(10);
        messagesContainer.setPadding(new Insets(15));
        scrollPane = new ScrollPane(messagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Auto-scroll to bottom when new message arrives
        messagesContainer.heightProperty().addListener((obs, old, newVal) -> 
            scrollPane.setVvalue(1.0));

        // Load initial messages
        refreshMessages(channel);

        // Back button
        Button backBtn = new Button("Back to Home");
        backBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        backBtn.setOnAction(e -> new WelcomeLoginPage(databaseHelper).show(primaryStage, currentUser));
        
        Button sqaas = new Button("sQaaS");
        sqaas.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        sqaas.setOnAction(e -> new StudentQAPage(databaseHelper, currentUser).show(primaryStage));
        
        Button profile = new Button("Profile");
        profile.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        profile.setOnAction(e -> {
			try {
				new UserHomePage(databaseHelper).show(primaryStage, currentUser);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

        HBox bottomBar = new HBox(10, backBtn, sqaas, profile);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(new VBox(inputBox, bottomBar));
        BorderPane.setMargin(inputBox, new Insets(10));

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("sQaaS™ - Discussion");
        primaryStage.show();
    }
    /**
     * Loads the discussion posts and filters based on user input and permissions.
     * 
     * @param int type
     */
    private void refreshMessages(int type) {
        Platform.runLater(() -> {
            try {
                List<discussionPost> messages = databaseHelper.getAllDiscussionMessages();
                messagesContainer.getChildren().clear();

                for (discussionPost msg : messages) {
                	if(msg.getType()>currentUser.getPrivileges()) { //skips loading message if privleges arent there (filters private channels)
                		continue;
                	}
                	if(type==-1) {}else {
	                	if(msg.getType()!=type) {
	                		continue;
	                	}
                	}
                    boolean isMine = msg.getUsername().equals(currentUser.getUserName());

                    String role = databaseHelper.getUserRole(msg.getUsername());

                    // Username + Role
                    Label userLabel = new Label(msg.getUsername());
                    userLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

                    Label roleLabel = new Label(" • " + role.toUpperCase());
                    roleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
                    roleLabel.setTextFill(Color.GRAY);

                    HBox userRow = new HBox(userLabel, roleLabel);
                    userRow.setAlignment(Pos.CENTER_LEFT);

                    // Message text
                    Label msgLabel = new Label(msg.getMessage());
                    msgLabel.setWrapText(true);
                    msgLabel.setMaxWidth(600);
                    msgLabel.setPadding(new Insets(10, 14, 10, 14));

                    // Timestamp
                    Label timeLabel = new Label(msg.getFormattedTime());
                    timeLabel.setFont(Font.font(10));
                    timeLabel.setTextFill(Color.GRAY);
                    
                    // channel
                    Label channels = new Label(visibilityLabels[msg.getType()]);
                    channels.setFont(Font.font(10));
                    channels.setTextFill(Color.GRAY);
                    channels.setPadding(new Insets(2, 2, 2, 2));
                    
                    //delete
                    Button deleteme = new Button("x");
                    deleteme.setOnAction(e->{
                    	try {
							databaseHelper.deleteDiscussionPost(msg.getId());
							refreshMessages(channel);
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                    });
                    deleteme.setVisible( //only show button if the user has permission
                    	    currentUser.getPrivileges() >= 2 || //staff+
                    	    currentUser.getUserName().equals(msg.getUsername())
                    	);
                    
                    switch(msg.getType()) {
                    case 0 ->{channels.setVisible(false);}
                    case 1 ->{channels.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #fff; -fx-background-radius: 8;");}
                    case 2 ->{channels.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #fff; -fx-background-radius: 8;");}
                    case 3 ->{channels.setStyle("-fx-background-color: #7b1fa2; -fx-text-fill: #fff; -fx-background-radius: 8;");}
                    case 4 ->{channels.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: #fff; -fx-background-radius: 8;");}
                    
                    }

                    HBox bottom = new HBox(4, timeLabel, channels, deleteme);
                    VBox content = new VBox(4, userRow, msgLabel, bottom);

                    // Full bubble
                    VBox bubble = new VBox(content);
                    bubble.setMaxWidth(680);
                    bubble.setPadding(new Insets(10));
                    bubble.setStyle("-fx-background-radius: 18;");

                    // === LEFT (others) vs RIGHT (you) ===
                    if (isMine) {
                        // YOUR MESSAGE
                    	switch (role.toLowerCase()) {
                        case "admin" -> {
                            bubble.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-border-color: #e74c3c; -fx-border-width: 0 2 0 0;");
                            userLabel.setTextFill(Color.web("#c62828"));
                            roleLabel.setTextFill(Color.web("#e74c3c"));
                        }
                        case "instructor" -> {
                            bubble.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-border-color: #7b1fa2; -fx-border-width: 0 2 0 0;");
                            userLabel.setTextFill(Color.web("#7b1fa2"));
                            roleLabel.setTextFill(Color.web("#7b1fa2"));
                        }
                        case "staff" -> {
                            bubble.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-border-color: #ffd600; -fx-border-width: 0 2 0 0;");
                            userLabel.setTextFill(Color.web("#f57f17"));
                            roleLabel.setTextFill(Color.web("#f9a825"));
                        }
                        case "reviewer" -> {
                            bubble.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-border-color: #4caf50; -fx-border-width: 0 2 0 0;");
                            userLabel.setTextFill(Color.web("#2e7d32"));
                            roleLabel.setTextFill(Color.web("#4caf50"));
                        }
                        case "student" -> {
                        	bubble.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-border-color: #3498db; -fx-border-width: 0 2 0 0;");
                            userLabel.setTextFill(Color.web("#3498db"));
                            roleLabel.setTextFill(Color.web("#3498db"));
                        }
                        default -> {
                            bubble.setStyle("-fx-background-color: #c6c6c6; -fx-background-radius: 8;");
                            userLabel.setTextFill(Color.GRAY);
                            roleLabel.setTextFill(Color.LIGHTGRAY);
                        }}
                        msgLabel.setTextFill(Color.web("#000"));
                        timeLabel.setTextFill(Color.web("#95a5a6"));
                        userRow.setAlignment(Pos.CENTER_RIGHT);
                        content.setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        // OTHERS
                    	switch (role.toLowerCase()) {
                        case "admin" -> {
                            bubble.setStyle("-fx-background-color: #ffebee; -fx-background-radius: 8; -fx-border-color: #e74c3c; -fx-border-width: 0 0 0 2;");
                            userLabel.setTextFill(Color.web("#c62828"));
                            roleLabel.setTextFill(Color.web("#e74c3c"));
                        }
                        case "instructor" -> {
                            bubble.setStyle("-fx-background-color: #f3e5f5; -fx-background-radius: 8; -fx-border-color: #7b1fa2; -fx-border-width: 0 0 0 2;");
                            userLabel.setTextFill(Color.web("#7b1fa2"));
                            roleLabel.setTextFill(Color.web("#7b1fa2"));
                        }
                        case "staff" -> {
                            bubble.setStyle("-fx-background-color: #fffde7; -fx-background-radius: 8; -fx-border-color: #ffd600; -fx-border-width: 0 0 0 2;");
                            userLabel.setTextFill(Color.web("#f57f17"));
                            roleLabel.setTextFill(Color.web("#f9a825"));
                        }
                        case "reviewer" -> {
                            bubble.setStyle("-fx-background-color: #e8f5e8; -fx-background-radius: 8; -fx-border-color: #4caf50; -fx-border-width: 0 0 0 2;");
                            userLabel.setTextFill(Color.web("#2e7d32"));
                            roleLabel.setTextFill(Color.web("#4caf50"));
                        }
                        case "student" -> {
                        	bubble.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-border-color: #3498db; -fx-border-width: 0 0 0 2;");
                            userLabel.setTextFill(Color.web("#3498db"));
                            roleLabel.setTextFill(Color.web("#3498db"));
                        }
                        default -> {
                            bubble.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8;");
                            userLabel.setTextFill(Color.GRAY);
                            roleLabel.setTextFill(Color.LIGHTGRAY);
                        }}
                        //bubble.setStyle("-fx-background-color: #f1f3f5; -fx-background-radius: 8;");
                        //userLabel.setTextFill(Color.web("#2c3e50"));
                        //roleLabel.setTextFill(Color.web("#7f8c8d"));
                        msgLabel.setTextFill(Color.BLACK);
                        timeLabel.setTextFill(Color.web("#95a5a6"));
                    }

                    // Add spacing between messages
                    VBox messageWrapper = new VBox(bubble);
                    messageWrapper.setPadding(new Insets(4, 20, 4, 20));

                    // Align bubble left or right
                    if (isMine) {
                        HBox wrapper = new HBox(messageWrapper);
                        wrapper.setAlignment(Pos.CENTER_RIGHT);
                        messagesContainer.getChildren().add(wrapper);
                    } else {
                        HBox wrapper = new HBox(messageWrapper);
                        wrapper.setAlignment(Pos.CENTER_LEFT);
                        messagesContainer.getChildren().add(wrapper);
                    }
                }

                // scroll to top
                //scrollPane.setVvalue(0.0);

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Error", "Could not load messages.");
            }
        });
    }
    /** Shows an alert popup
     * 
     * @param title String title of alert
     * @param message String message of alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}