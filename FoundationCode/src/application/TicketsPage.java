package application;

import application.Ticket;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * TicketsPage provides ticket management interface for instructors, staff, and admins.
 * Instructors can create and reopen tickets. Admins can close tickets with resolution comments.
 * All roles can view open and closed tickets.
 */
public class TicketsPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private Stage primaryStage;
    
    // UI Components
    private TabPane tabPane;
    private VBox openTicketsContainer;
    private VBox closedTicketsContainer;
    private ScrollPane openTicketsScroll;
    private ScrollPane closedTicketsScroll;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    public TicketsPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.currentUser = null;
    }
    
    public TicketsPage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.currentUser = user;
    }
    
    /**
     * Displays the tickets page in the primary stage.
     * 
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param user The current user accessing the page.
     */
    public void show(Stage primaryStage, User user) {
        this.primaryStage = primaryStage;
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Top bar
        HBox topBar = createTopBar(user);
        root.setTop(topBar);
        
        // Main content - tabs for Open and Closed tickets
        tabPane = new TabPane();
        
        Tab openTab = new Tab("Open Tickets");
        openTab.setClosable(false);
        openTab.setContent(buildOpenTicketsPanel(user));
        
        Tab closedTab = new Tab("Closed Tickets");
        closedTab.setClosable(false);
        closedTab.setContent(buildClosedTicketsPanel(user));
        
        tabPane.getTabs().addAll(openTab, closedTab);
        root.setCenter(tabPane);
        
        // Load initial data
        refreshTickets();
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("sQaaS™ - Ticket Management");
        primaryStage.show();
    }
    
    /**
     * Creates the top navigation bar.
     */
    private HBox createTopBar(User user) {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #9C27B0;");
        
        Label titleLabel = new Label("🎫 Ticket Management");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Create new ticket button (only for instructors)
        Button createButton = new Button("+ Create Ticket");
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        createButton.setOnMouseClicked(e -> showCreateTicketDialog());
        
        // Only show create button for instructors
        if (user.getRole().equals("Instructor") || user.getRole().equals("Staff")) {
            topBar.getChildren().addAll(titleLabel, spacer, createButton);
        } else {
            topBar.getChildren().addAll(titleLabel, spacer);
        }
        
        Button backButton = new Button("← Back");
        backButton.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
        backButton.setOnAction(e -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
        });
        
        topBar.getChildren().add(backButton);
        
        return topBar;
    }
    
    /**
     * Builds the open tickets panel.
     */
    private ScrollPane buildOpenTicketsPanel(User user) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        
        openTicketsContainer = new VBox(10);
        container.getChildren().add(openTicketsContainer);
        
        openTicketsScroll = new ScrollPane(container);
        openTicketsScroll.setFitToWidth(true);
        openTicketsScroll.setStyle("-fx-background: #f5f5f5;");
        
        return openTicketsScroll;
    }
    
    /**
     * Builds the closed tickets panel.
     */
    private ScrollPane buildClosedTicketsPanel(User user) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        
        closedTicketsContainer = new VBox(10);
        container.getChildren().add(closedTicketsContainer);
        
        closedTicketsScroll = new ScrollPane(container);
        closedTicketsScroll.setFitToWidth(true);
        closedTicketsScroll.setStyle("-fx-background: #f5f5f5;");
        
        return closedTicketsScroll;
    }
    
    /**
     * Refreshes both open and closed ticket lists.
     */
    private void refreshTickets() {
        loadOpenTickets();
        loadClosedTickets();
    }
    
    /**
     * Loads and displays open tickets.
     */
    private void loadOpenTickets() {
        openTicketsContainer.getChildren().clear();
        
        try {
            List<Ticket> tickets = databaseHelper.getOpenTickets();
            
            if (tickets.isEmpty()) {
                Label emptyLabel = new Label("No open tickets");
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                openTicketsContainer.getChildren().add(emptyLabel);
            } else {
                for (Ticket ticket : tickets) {
                    VBox card = createTicketCard(ticket, false);
                    openTicketsContainer.getChildren().add(card);
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load open tickets: " + ex.getMessage());
        }
    }
    
    /**
     * Loads and displays closed tickets.
     */
    private void loadClosedTickets() {
        closedTicketsContainer.getChildren().clear();
        
        try {
            List<Ticket> tickets = databaseHelper.getClosedTickets();
            
            if (tickets.isEmpty()) {
                Label emptyLabel = new Label("No closed tickets");
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                closedTicketsContainer.getChildren().add(emptyLabel);
            } else {
                for (Ticket ticket : tickets) {
                    VBox card = createTicketCard(ticket, true);
                    closedTicketsContainer.getChildren().add(card);
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load closed tickets: " + ex.getMessage());
        }
    }
    
    /**
     * Creates a visual card for a ticket.
     */
    private VBox createTicketCard(Ticket ticket, boolean isClosed) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; "
                    + "-fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header with title and status
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(ticket.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Status badge
        Label statusBadge = new Label(isClosed ? "CLOSED" : "OPEN");
        statusBadge.setStyle(isClosed 
            ? "-fx-background-color: #666; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3;"
            : "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3;");
        
        // Reopened badge if applicable
        if (ticket.isReopenedTicket()) {
            Label reopenedBadge = new Label("REOPENED");
            reopenedBadge.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; "
                                 + "-fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px;");
            header.getChildren().addAll(titleLabel, statusBadge, reopenedBadge);
        } else {
            header.getChildren().addAll(titleLabel, statusBadge);
        }
        
        // Metadata
        Label metaLabel = new Label(String.format("Created by %s on %s", 
            ticket.getAskedBy(), 
            ticket.getCreatedAt().toLocalDateTime().format(DATE_FORMAT)));
        metaLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        
        // Content
        Label contentLabel = new Label(ticket.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13px;");
        
        card.getChildren().addAll(header, metaLabel, new Separator(), contentLabel);
        
        // Resolution details if closed
        if (isClosed && ticket.getResolutionComments() != null) {
            VBox resolutionBox = new VBox(5);
            resolutionBox.setPadding(new Insets(10));
            resolutionBox.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 3;");
            
            Label resolutionTitle = new Label("Resolution:");
            resolutionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            
            Label resolutionText = new Label(ticket.getResolutionComments());
            resolutionText.setWrapText(true);
            resolutionText.setStyle("-fx-font-size: 12px;");
            
            Label resolvedByLabel = new Label(String.format("Resolved by %s on %s",
                ticket.getResolvedBy(),
                ticket.getResolvedAt().toLocalDateTime().format(DATE_FORMAT)));
            resolvedByLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px; -fx-font-style: italic;");
            
            resolutionBox.getChildren().addAll(resolutionTitle, resolutionText, resolvedByLabel);
            card.getChildren().addAll(new Separator(), resolutionBox);
        }
        
        // Link to parent ticket if reopened
        if (ticket.isReopenedTicket()) {
            Button viewParentButton = new Button("View Original Ticket #" + ticket.getParentTicketId());
            viewParentButton.setStyle("-fx-font-size: 11px; -fx-text-fill: #9C27B0; -fx-cursor: hand;");
            viewParentButton.setOnAction(e -> showTicketDetails(ticket.getParentTicketId()));
            card.getChildren().add(viewParentButton);
        }
        
        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        if (!isClosed && currentUser.getRole().equals("Admin")) {
            // Admin can close open tickets
            Button closeButton = new Button("Close Ticket");
            closeButton.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
            closeButton.setOnAction(e -> showCloseTicketDialog(ticket));
            actions.getChildren().add(closeButton);
        }
        
        if (isClosed && currentUser.getRole().equals("Instructor")) {
            // Instructors can reopen closed tickets
            Button reopenButton = new Button("Reopen Ticket");
            reopenButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
            reopenButton.setOnAction(e -> showReopenTicketDialog(ticket));
            actions.getChildren().add(reopenButton);
        }
        
        if (actions.getChildren().size() > 0) {
            card.getChildren().add(actions);
        }
        
        return card;
    }
    
    /**
     * Shows dialog to create a new ticket.
     */
    private void showCreateTicketDialog() {
        Dialog<Ticket> dialog = new Dialog<>();
        dialog.setTitle("Create New Ticket");
        dialog.setHeaderText("Request admin action");
        
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Ticket title (max 100 characters)");
        titleField.setStyle("-fx-font-size: 13px;");
        
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Describe the action you need admins to perform...");
        contentArea.setPrefRowCount(5);
        contentArea.setWrapText(true);
        
        Label charCount = new Label("0/500");
        charCount.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        contentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.length();
            charCount.setText(length + "/500");
            if (length > 500) {
                contentArea.setText(oldVal);
            }
        });
        
        content.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), contentArea, charCount
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Ticket(titleField.getText(), contentArea.getText(), currentUser.getUserName());
            }
            return null;
        });
        
        Optional<Ticket> result = dialog.showAndWait();
        result.ifPresent(ticket -> {
            try {
                int ticketId = databaseHelper.createTicket(
                    ticket.getTitle(),
                    ticket.getContent(),
                    currentUser.getUserName()
                );
                
                if (ticketId > 0) {
                    showInfo("Ticket created successfully with ID: " + ticketId);
                    refreshTickets();
                } else {
                    showError("Failed to create ticket");
                }
            } catch (SQLException ex) {
                showError("Error creating ticket: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Shows dialog to close a ticket with resolution comments.
     */
    private void showCloseTicketDialog(Ticket ticket) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Close Ticket #" + ticket.getId());
        dialog.setHeaderText("Document actions taken");
        
        ButtonType closeButtonType = new ButtonType("Close Ticket", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label ticketInfo = new Label("Ticket: " + ticket.getTitle());
        ticketInfo.setStyle("-fx-font-weight: bold;");
        
        TextArea resolutionArea = new TextArea();
        resolutionArea.setPromptText("Describe the actions you took to resolve this ticket...");
        resolutionArea.setPrefRowCount(5);
        resolutionArea.setWrapText(true);
        
        Label charCount = new Label("0/500");
        charCount.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        resolutionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.length();
            charCount.setText(length + "/500");
            if (length > 500) {
                resolutionArea.setText(oldVal);
            }
        });
        
        content.getChildren().addAll(
            ticketInfo,
            new Label("Resolution Comments:"),
            resolutionArea,
            charCount
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == closeButtonType) {
                return resolutionArea.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(resolutionComments -> {
            try {
                databaseHelper.closeTicket(ticket.getId(), currentUser.getUserName(), resolutionComments);
                showInfo("Ticket #" + ticket.getId() + " closed successfully");
                refreshTickets();
            } catch (SQLException ex) {
                showError("Error closing ticket: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Shows dialog to reopen a closed ticket.
     */
    private void showReopenTicketDialog(Ticket ticket) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reopen Ticket #" + ticket.getId());
        dialog.setHeaderText("Update ticket description");
        
        ButtonType reopenButtonType = new ButtonType("Reopen", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reopenButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label ticketInfo = new Label("Original Ticket: " + ticket.getTitle());
        ticketInfo.setStyle("-fx-font-weight: bold;");
        
        TextArea updatedContent = new TextArea();
        updatedContent.setPromptText("Describe why you're reopening this ticket and what additional action is needed...");
        updatedContent.setPrefRowCount(5);
        updatedContent.setWrapText(true);
        
        Label charCount = new Label("0/500");
        charCount.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        updatedContent.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.length();
            charCount.setText(length + "/500");
            if (length > 500) {
                updatedContent.setText(oldVal);
            }
        });
        
        content.getChildren().addAll(
            ticketInfo,
            new Label("Updated Description:"),
            updatedContent,
            charCount
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reopenButtonType) {
                return updatedContent.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            try {
                int newTicketId = databaseHelper.reopenTicket(
                    ticket.getId(),
                    newContent,
                    currentUser.getUserName()
                );
                
                if (newTicketId > 0) {
                    showInfo("Ticket reopened successfully as Ticket #" + newTicketId);
                    refreshTickets();
                    // Switch to open tickets tab
                    tabPane.getSelectionModel().select(0);
                } else {
                    showError("Failed to reopen ticket");
                }
            } catch (SQLException ex) {
                showError("Error reopening ticket: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Shows details of a specific ticket (used for viewing parent tickets).
     */
    private void showTicketDetails(int ticketId) {
        try {
            Ticket ticket = databaseHelper.getTicketById(ticketId);
            if (ticket != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ticket #" + ticketId);
                alert.setHeaderText(ticket.getTitle());
                
                StringBuilder content = new StringBuilder();
                content.append("Created by: ").append(ticket.getAskedBy()).append("\n");
                content.append("Created: ").append(ticket.getCreatedAt().toLocalDateTime().format(DATE_FORMAT)).append("\n\n");
                content.append("Description:\n").append(ticket.getContent()).append("\n\n");
                
                if (ticket.isResolved()) {
                    content.append("Status: CLOSED\n");
                    content.append("Resolved by: ").append(ticket.getResolvedBy()).append("\n");
                    content.append("Resolved: ").append(ticket.getResolvedAt().toLocalDateTime().format(DATE_FORMAT)).append("\n\n");
                    content.append("Resolution:\n").append(ticket.getResolutionComments());
                }
                
                alert.setContentText(content.toString());
                alert.showAndWait();
            }
        } catch (SQLException ex) {
            showError("Error loading ticket: " + ex.getMessage());
        }
    }
    
    /**
     * Shows an informational alert.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an error alert.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
