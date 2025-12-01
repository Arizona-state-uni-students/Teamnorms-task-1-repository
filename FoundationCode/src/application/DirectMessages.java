package application;

import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper.ConversationSummary;
import databasePart1.DatabaseHelper.DirectMessage;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DirectMessagesPage provides a direct messaging interface between users.
 * Features split-pane layout with inbox (left) and chat thread (right).
 */
public class DirectMessages {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private Stage primaryStage;
    
    // UI Components
    private SplitPane splitPane;
    private VBox inboxPanel;
    private VBox chatPanel;
    private ScrollPane inboxScroll;
    private ScrollPane chatScroll;
    private VBox conversationListContainer;
    private VBox messageThreadContainer;
    private TextField searchField;
    private VBox searchResultsContainer;
    private String selectedUser = null;
    private Button toggleInboxButton;
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
    
    /**
     * Constructor for DirectMessagesPage.
     * 
     * @param databaseHelper Database helper
     * @param currentUser Current user
     */
    public DirectMessages(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    /**
     * Displays the direct messages page.
     * 
     * @param primaryStage Stage to display on
     */
    public void show(Stage primaryStage, String username) {
        this.primaryStage = primaryStage;
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        HBox topBar = createTopBar();
        root.setTop(topBar);
        
        splitPane = new SplitPane();
        splitPane.setDividerPositions(0.3);
        
        inboxPanel = buildInboxPanel();
        chatPanel = buildChatPanel();
        
        splitPane.getItems().addAll(inboxPanel, chatPanel);
        root.setCenter(splitPane);
        
        loadConversationList();
        
        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("sQaaS™ - Direct Messages");
        primaryStage.show();
        if(username!=null) {
        	selectConversation(username);
        	//createMessageInput(id);
        }
    }
    
    /**
     * Creates the top navigation bar.
     * 
     * @return Top bar HBox
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #9C27B0;");
        
        Label titleLabel = new Label("Direct Messages");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label userLabel = new Label("Signed in as: " + currentUser.getUserName());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button backButton = new Button("← Back");
        backButton.setStyle("-fx-background-color: #7B1FA2; -fx-text-fill: white; -fx-font-size: 12px;");
        backButton.setOnAction(e -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, currentUser);
        });
        
        Button refreshButton = new Button("⟳ Refresh");
        refreshButton.setStyle("-fx-background-color: #7B1FA2; -fx-text-fill: white; -fx-font-size: 12px;");
        refreshButton.setOnAction(e -> {
            loadConversationList();
            if (selectedUser != null) {
                loadChatThread(selectedUser);
            }
        });
        
        topBar.getChildren().addAll(titleLabel, userLabel, spacer, refreshButton, backButton);
        return topBar;
    }
    
    /**
     * Builds the inbox panel with conversation list and search.
     * 
     * @return Inbox VBox
     */
    private VBox buildInboxPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white;");
        
        Label inboxLabel = new Label("Conversations");
        inboxLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        searchField = new TextField();
        searchField.setPromptText("Search or start new conversation...");
        searchField.setStyle("-fx-pref-height: 35px;");
        
        searchResultsContainer = new VBox(5);
        searchResultsContainer.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; "
                                      + "-fx-border-width: 1; -fx-padding: 5;");
        searchResultsContainer.setVisible(false);
        searchResultsContainer.setManaged(false);
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                searchResultsContainer.setVisible(false);
                searchResultsContainer.setManaged(false);
            } else {
                performUserSearch(newVal.trim());
            }
        });
        
        conversationListContainer = new VBox(5);
        inboxScroll = new ScrollPane(conversationListContainer);
        inboxScroll.setFitToWidth(true);
        inboxScroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(inboxScroll, Priority.ALWAYS);
        
        panel.getChildren().addAll(inboxLabel, searchField, searchResultsContainer, 
                                   new Separator(), inboxScroll);
        return panel;
    }
    
    /**
     * Builds the chat panel with messages and input.
     * 
     * @return Chat VBox
     */
    private VBox buildChatPanel() {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: white;");
        
        HBox chatHeader = new HBox(10);
        chatHeader.setPadding(new Insets(15));
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
        
        Label chatHeaderLabel = new Label("Select a conversation");
        chatHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        chatHeader.getChildren().add(chatHeaderLabel);
        
        messageThreadContainer = new VBox(10);
        messageThreadContainer.setPadding(new Insets(15));
        chatScroll = new ScrollPane(messageThreadContainer);
        chatScroll.setFitToWidth(true);
        chatScroll.setStyle("-fx-background-color: white;");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);
        
        HBox inputArea = createMessageInput();
        
        panel.getChildren().addAll(chatHeader, chatScroll, inputArea);
        return panel;
    }
    
    /**
     * Creates the message input area.
     * 
     * @return Input HBox
     */
    private HBox createMessageInput() {
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(15));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");
        
        TextArea messageInput = new TextArea();
        messageInput.setPromptText("Type your message...");
        messageInput.setWrapText(true);
        messageInput.setPrefRowCount(2);
        messageInput.setMaxHeight(60);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Label charCounter = new Label("0/500");
        charCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        messageInput.textProperty().addListener((obs, oldVal, newVal) -> {
            charCounter.setText(newVal.length() + "/500");
            if (newVal.length() > 500) {
                messageInput.setText(newVal.substring(0, 500));
            }
        });
        
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; "
                          + "-fx-font-weight: bold; -fx-pref-width: 80px;");
        sendButton.setDisable(true);
        
        sendButton.setOnAction(e -> {
            String content = messageInput.getText().trim();
            if (!content.isEmpty() && selectedUser != null) {
                sendMessage(selectedUser, content);
                messageInput.clear();
            }
        });
        
        VBox inputWrapper = new VBox(5, messageInput, charCounter);
        HBox.setHgrow(inputWrapper, Priority.ALWAYS);
        
        inputArea.getChildren().addAll(inputWrapper, sendButton);
        return inputArea;
    }
    
    /**
     * Loads conversation list from database.
     */
    private void loadConversationList() {
        conversationListContainer.getChildren().clear();
        
        try {
            List<ConversationSummary> conversations = databaseHelper.getConversationList(currentUser.getUserName());
            
            if (conversations.isEmpty()) {
                Label emptyLabel = new Label("No conversations yet.\nSearch for a user to start messaging.");
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-text-alignment: center;");
                emptyLabel.setWrapText(true);
                conversationListContainer.getChildren().add(emptyLabel);
            } else {
                for (ConversationSummary conv : conversations) {
                    VBox convCard = createConversationCard(conv);
                    conversationListContainer.getChildren().add(convCard);
                }
            }
        } catch (SQLException ex) {
            showAlert("Error", "Failed to load conversations: " + ex.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Creates a conversation card for inbox display.
     * 
     * @param conv Conversation summary
     * @return Conversation card VBox
     */
    private VBox createConversationCard(ConversationSummary conv) {
        VBox card = new VBox(3);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #eee; "
                    + "-fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(conv.getOtherUser());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        
        if (conv.getUnreadCount() > 0) {
            Label unreadBadge = new Label(conv.getUnreadCount() > 9 ? "9+" : String.valueOf(conv.getUnreadCount()));
            unreadBadge.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; "
                               + "-fx-background-radius: 10; -fx-padding: 2 6; -fx-font-size: 10px;");
            header.getChildren().addAll(nameLabel, unreadBadge);
        } else {
            header.getChildren().add(nameLabel);
        }
        
        String preview = conv.getLastMessageContent();
        if (preview != null && preview.length() > 40) {
            preview = preview.substring(0, 37) + "...";
        }
        Label previewLabel = new Label(preview != null ? preview : "No messages yet");
        previewLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        
        Label timeLabel = new Label(conv.getLastMessageAt().format(TIME_FORMAT));
        timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 10px;");
        
        card.getChildren().addAll(header, previewLabel, timeLabel);
        
        if (conv.getOtherUser().equals(selectedUser)) {
            card.setStyle(card.getStyle() + "-fx-background-color: #f0e6f6;");
        }
        
        card.setOnMouseClicked(e -> {
            selectConversation(conv.getOtherUser());
        });
        
        card.setOnMouseEntered(e -> {
            if (!conv.getOtherUser().equals(selectedUser)) {
                card.setStyle(card.getStyle() + "-fx-background-color: #fafafa;");
            }
        });
        card.setOnMouseExited(e -> {
            if (!conv.getOtherUser().equals(selectedUser)) {
                card.setStyle("-fx-background-color: white; -fx-border-color: #eee; "
                            + "-fx-border-width: 0 0 1 0; -fx-cursor: hand;");
            }
        });
        
        return card;
    }
    
    /**
     * Performs user search for autocomplete.
     * 
     * @param searchTerm Search query
     */
    private void performUserSearch(String searchTerm) {
        searchResultsContainer.getChildren().clear();
        
        try {
            List<String> results = databaseHelper.searchUsers(searchTerm, 
                                                             currentUser.getUserName(), 
                                                             currentUser.getRole());
            
            if (results.isEmpty()) {
                Label noResults = new Label("No users found");
                noResults.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                searchResultsContainer.getChildren().add(noResults);
            } else {
                for (String username : results) {
                    HBox resultCard = createSearchResultCard(username);
                    searchResultsContainer.getChildren().add(resultCard);
                }
            }
            
            searchResultsContainer.setVisible(true);
            searchResultsContainer.setManaged(true);
            
        } catch (SQLException ex) {
            showAlert("Error", "Failed to search users: " + ex.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Creates a search result card.
     * 
     * @param username Username to display
     * @return Search result HBox
     */
    private HBox createSearchResultCard(String username) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(8));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-cursor: hand;");
        
        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-weight: bold;");
        
        card.getChildren().add(nameLabel);
        
        card.setOnMouseClicked(e -> {
            searchField.clear();
            searchResultsContainer.setVisible(false);
            searchResultsContainer.setManaged(false);
            selectConversation(username);
        });
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f0e6f6; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-cursor: hand;"));
        
        return card;
    }
    
    /**
     * Selects a conversation and loads chat thread.
     * 
     * @param username Other user's username
     */
    private void selectConversation(String username) {
        selectedUser = username;
        loadChatThread(username);
        loadConversationList();
        
        ((HBox) chatPanel.getChildren().get(2)).getChildren().stream()
            .filter(node -> node instanceof Button)
            .forEach(node -> ((Button) node).setDisable(false));
        
        HBox chatHeader = (HBox) chatPanel.getChildren().get(0);
        Label headerLabel = (Label) chatHeader.getChildren().get(0);
        headerLabel.setText("Chat with " + username);
    }
    
    /**
     * Loads chat thread for selected conversation.
     * 
     * @param otherUser Other user's username
     */
    private void loadChatThread(String otherUser) {
        messageThreadContainer.getChildren().clear();
        
        try {
            List<DirectMessage> messages = databaseHelper.getConversation(currentUser.getUserName(), otherUser);
            
            if (messages.isEmpty()) {
                Label emptyLabel = new Label("No messages yet. Start the conversation!");
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                messageThreadContainer.getChildren().add(emptyLabel);
            } else {
                for (DirectMessage msg : messages) {
                    HBox messageBubble = createMessageBubble(msg);
                    messageThreadContainer.getChildren().add(messageBubble);
                }
                
                databaseHelper.markConversationAsRead(currentUser.getUserName(), otherUser);
            }
            
            Platform.runLater(() -> chatScroll.setVvalue(1.0));
            
        } catch (SQLException ex) {
            showAlert("Error", "Failed to load messages: " + ex.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Creates a message bubble for display.
     * 
     * @param msg DirectMessage to display
     * @return Message bubble HBox
     */
    private HBox createMessageBubble(DirectMessage msg) {
        HBox container = new HBox();
        container.setPadding(new Insets(5, 0, 5, 0));
        
        boolean isSentByMe = msg.isSentBy(currentUser.getUserName());
        
        VBox bubble = new VBox(3);
        bubble.setPadding(new Insets(10));
        bubble.setMaxWidth(400);
        
        if (isSentByMe) {
            bubble.setStyle("-fx-background-color: #9C27B0; -fx-background-radius: 15; "
                          + "-fx-border-radius: 15;");
            container.setAlignment(Pos.CENTER_RIGHT);
            
            Label contentLabel = new Label(msg.getContent());
            contentLabel.setWrapText(true);
            contentLabel.setStyle("-fx-text-fill: white;");
            
            Label timeLabel = new Label(msg.getCreatedAt().format(TIME_FORMAT));
            timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 9px;");
            
            bubble.getChildren().addAll(contentLabel, timeLabel);
            
        } else {
            bubble.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 15; "
                          + "-fx-border-radius: 15;");
            container.setAlignment(Pos.CENTER_LEFT);
            
            Label senderLabel = new Label(msg.getFromUser());
            senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #666;");
            Label contentLabel = new Label(msg.getContent());
            contentLabel.setWrapText(true);
            contentLabel.setStyle("-fx-text-fill: black;");
            
            Label timeLabel = new Label(msg.getCreatedAt().format(TIME_FORMAT));
            timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 9px;");
            
            bubble.getChildren().addAll(senderLabel, contentLabel, timeLabel);
        }
        
        container.getChildren().add(bubble);
        return container;
    }
    
    /**
     * Sends a message to selected user.
     * 
     * @param toUser Recipient username
     * @param content Message content
     */
    private void sendMessage(String toUser, String content) {
        try {
            databaseHelper.sendDirectMessage(currentUser.getUserName(), toUser, content);
            loadChatThread(toUser);
            loadConversationList();
        } catch (SQLException ex) {
            showAlert("Error", "Failed to send message: " + ex.getMessage(), AlertType.ERROR);
        }
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param title Alert title
     * @param content Alert message
     * @param type Alert type
     */
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Method to view a review.
     * 
     * @param review Review to display.
     * @throws SQLException 
     */

}