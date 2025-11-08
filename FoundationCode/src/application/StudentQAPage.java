package application;

import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper.PrivateMessage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**

 * StudentQAPage provides the interface for students to interact with the Q&A system.

 * Students can ask questions, view questions, provide answers, and mark questions as resolved.

 */

public class StudentQAPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private Stage primaryStage;
    private VBox mainLayout;
    private TabPane tabPane;
    private VBox displayQuestions;
    private VBox displayQuestionThread;
    private ComboBox<String> viewFilter; // Public / Private / All
    private ComboBox<String> resolutionFilter;   // Resolved / Unresolved / All
    private Label allQuestionsError;             // Inline error banner for the All Questions tab
	
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    // Tabs
    private Tab myQuestionsTab;
    private Tab allQuestionsTab;
    private Tab askQuestionTab;
    
    public StudentQAPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    public void show(Stage primaryStage) {
        this.primaryStage = primaryStage;
        mainLayout = new VBox(-2);
        mainLayout.setPadding(new Insets(21));
        mainLayout.setPrefSize(900, 600);

		// Page background image
       Image backgroundImage = new Image(getClass().getResource("/QAbg.png").toExternalForm());
       BackgroundImage backgroundImg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(900, 600, false, false, true, false)
            );
        mainLayout.setBackground(new Background(backgroundImg));

        // Header label
        Label titleLabel = new Label("Student Q&A System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #000");
        Label welcomeLabel = new Label("\tWelcome, " + currentUser.getUserName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #fff;");

        // Create tab pane
        tabPane = new TabPane();
        tabPane.setMinHeight(400);

        // Create tabs
        askQuestionTab = createAskQuestionTab();
        myQuestionsTab = createMyQuestionsTab();
        allQuestionsTab = createAllQuestionsTab();
        tabPane.getTabs().addAll(askQuestionTab, myQuestionsTab, allQuestionsTab);
        tabPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-tab-header-background-color: transparent;");

        // ======= Bottom Buttons =======
		// Back button
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-padding: 8 16;");
        backButton.setOnAction(e -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, currentUser);
        });

		// Button to refresh page
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #0099ff; -fx-text-fill: white; -fx-padding: 8 16;");
        refreshButton.setOnAction(e -> refreshAllTabs());

        HBox buttonBox = new HBox(10, backButton, refreshButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
		
        mainLayout.getChildren().addAll(titleLabel, welcomeLabel, tabPane, buttonBox);

		// Make page scrollable
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Scene scene = new Scene(scrollPane, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("sQaaS™ - Q&A System");
        primaryStage.show();
    }

 
    // ========== ASK QUESTION TAB ==========
    private Tab createAskQuestionTab() {
        Tab tab = new Tab("Ask Question");
        tab.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        tab.setClosable(false);
		
        VBox content = new VBox(5);
        content.setPadding(new Insets(8));
        content.setStyle("-fx-background-color: white;");

		// Info label
        Label infoLabel = new Label("Ask a new question to get help from other students");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        // Search field for questions
        Label searchLabel = new Label("Search existing questions first:");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c2c2c;");

        TextField searchField = new TextField();
        searchField.setPromptText("Enter keywords to search...");
        searchField.setMaxWidth(400);

        VBox searchResultsBox = new VBox(5);
        searchResultsBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 10;");
        searchResultsBox.setVisible(false);
		
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #0099ff; -fx-text-fill: white;");
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                try {
                    List<Question> results = databaseHelper.searchQuestions(keyword);
                    displaySearchResults(searchResultsBox, results);
                    searchResultsBox.setVisible(true);
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to search questions: " + ex.getMessage(), AlertType.ERROR);
                }
            }
        });
        
        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        Separator sep1 = new Separator();
        
        // Question input form
        Label titleLabel = new Label("Question Title:*");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c2c2c;");
        
        TextField titleField = new TextField();
        titleField.setPromptText("Enter a clear, concise title (5-100 characters)");
        titleField.setMaxWidth(600);
        
        Label titleCounter = new Label("0/" + Question.TITLE_MAX_LENGTH);
        titleCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        // Validation feedback for title
        Label titleValidation = new Label();
        titleValidation.setWrapText(true);
        titleValidation.setMaxWidth(600);
        
        titleField.textProperty().addListener((obs, old, newVal) -> {
            // Update counter
            titleCounter.setText(newVal.length() + "/" + Question.TITLE_MAX_LENGTH);
            titleCounter.setStyle(newVal.length() > Question.TITLE_MAX_LENGTH ? 
                "-fx-font-size: 10px; -fx-text-fill: red;" : 
                "-fx-font-size: 10px; -fx-text-fill: #666;");
            
            // Enforce max length
            if (newVal.length() > Question.TITLE_MAX_LENGTH) {
                titleField.setText(newVal.substring(0, Question.TITLE_MAX_LENGTH));
            }
            
            // Real-time validation
            if (!newVal.trim().isEmpty()) {
                InputValidator.ValidationReport report = InputValidator.validateQuestionTitle(newVal);
                if (report.hasIssues()) {
                    titleValidation.setText(report.getFullReport());
                    titleValidation.setStyle(report.canSubmit() ? 
                        "-fx-text-fill: orange; -fx-font-size: 11px;" : 
                        "-fx-text-fill: red; -fx-font-size: 11px;");
                } else {
                    titleValidation.setText("✓ Title looks good!");
                    titleValidation.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
                }
            } else {
                titleValidation.setText("");
            }
        });
        
        Label contentLabel = new Label("Question Details:*");
        contentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c2c2c;");
        
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Describe your question in detail (10-500 characters)");
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(8);
        contentArea.setMaxWidth(600);
        
        Label contentCounter = new Label("0/" + Question.CONTENT_MAX_LENGTH);
        contentCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        // Validation feedback for content
        Label contentValidation = new Label();
        contentValidation.setWrapText(true);
        contentValidation.setMaxWidth(600);
        
        contentArea.textProperty().addListener((obs, old, newVal) -> {
            // Update counter
            contentCounter.setText(newVal.length() + "/" + Question.CONTENT_MAX_LENGTH);
            contentCounter.setStyle(newVal.length() > Question.CONTENT_MAX_LENGTH ? 
                "-fx-font-size: 10px; -fx-text-fill: red;" : 
                "-fx-font-size: 10px; -fx-text-fill: #666;");
            
            // Enforce max length
            if (newVal.length() > Question.CONTENT_MAX_LENGTH) {
                contentArea.setText(newVal.substring(0, Question.CONTENT_MAX_LENGTH));
            }
            
            // Real-time validation
            if (!newVal.trim().isEmpty()) {
                InputValidator.ValidationReport report = InputValidator.validateQuestionContent(newVal);
                if (report.hasIssues()) {
                    contentValidation.setText(report.getFullReport());
                    contentValidation.setStyle(report.canSubmit() ? 
                        "-fx-text-fill: orange; -fx-font-size: 11px;" : 
                        "-fx-text-fill: red; -fx-font-size: 11px;");
                } else {
                    contentValidation.setText("✓ Content looks good!");
                    contentValidation.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
                }
            } else {
                contentValidation.setText("");
            }
        });
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(600);
        
        Button submitButton = new Button("Submit Question");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        
        Button clearButton = new Button("Clear");
        clearButton.setStyle("-fx-background-color: #999; -fx-text-fill: white;");
        clearButton.setOnAction(e -> {
            titleField.clear();
            contentArea.clear();
            errorLabel.setText("");
            titleValidation.setText("");
            contentValidation.setText("");
        });
        
        HBox buttonBox = new HBox(10, clearButton, submitButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        submitButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String contentText = contentArea.getText().trim();
            
            // Validate title
            InputValidator.ValidationReport titleReport = InputValidator.validateQuestionTitle(title);
            if (!titleReport.canSubmit()) {
                errorLabel.setText("Title validation failed:\n" + titleReport.getFullReport());
                return;
            }
            
            // Validate content
            InputValidator.ValidationReport contentReport = InputValidator.validateQuestionContent(contentText);
            if (!contentReport.canSubmit()) {
                errorLabel.setText("Content validation failed:\n" + contentReport.getFullReport());
                return;
            }
            
            // Check for warnings
            boolean hasWarnings = titleReport.hasIssues() || contentReport.hasIssues();
            String finalTitle = title;
            String finalContent = contentText;
            
            // If there are warnings, show them and offer auto-correction
            if (hasWarnings) {
                Alert warningAlert = new Alert(AlertType.CONFIRMATION);
                warningAlert.setTitle("Validation Warnings");
                warningAlert.setHeaderText("Your question has some suggestions for improvement:");
                
                StringBuilder warningText = new StringBuilder();
                if (titleReport.hasIssues()) {
                    warningText.append("TITLE:\n").append(titleReport.getFullReport()).append("\n");
                }
                if (contentReport.hasIssues()) {
                    warningText.append("\nCONTENT:\n").append(contentReport.getFullReport());
                }
                
                warningAlert.setContentText(warningText.toString() + 
                    "\n\nWould you like to apply auto-corrections?\n" +
                    "• OK = Apply corrections and submit\n" +
                    "• Cancel = Submit as-is or edit manually");
                
                // Add a third button for "Edit More"
                ButtonType submitAsIsButton = new ButtonType("Submit As-Is");
                ButtonType applyCorrectionsButton = new ButtonType("Apply Corrections", ButtonBar.ButtonData.OK_DONE);
                ButtonType editMoreButton = new ButtonType("Edit More", ButtonBar.ButtonData.CANCEL_CLOSE);
                
                warningAlert.getButtonTypes().setAll(applyCorrectionsButton, submitAsIsButton, editMoreButton);
                
                Optional<ButtonType> result = warningAlert.showAndWait();
                
                if (result.isPresent()) {
                    if (result.get() == applyCorrectionsButton) {
                        // Apply corrections
                        finalTitle = titleReport.getCorrectedText().isEmpty() ? title : titleReport.getCorrectedText();
                        finalContent = contentReport.getCorrectedText().isEmpty() ? contentText : contentReport.getCorrectedText();
                    } else if (result.get() == editMoreButton) {
                        // User wants to edit more, just return
                        return;
                    }
                    // If submitAsIsButton, use original text (finalTitle and finalContent already set)
                } else {
                    // Dialog was closed, don't submit
                    return;
                }
            }
            
            // Create and submit question
            try {
                Question question = new Question(finalTitle, finalContent, currentUser.getUserName());
                databaseHelper.createQuestion(question);
				
                // Show success message
                Alert successAlert = new Alert(AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Question Posted!");
                successAlert.setContentText("Your question has been posted successfully.");
                successAlert.showAndWait();
				
                // Clear fields
                titleField.clear();
                contentArea.clear();
                errorLabel.setText("");
                titleValidation.setText("");
                contentValidation.setText("");
				
                // Refresh tabs to show the new question
                refreshAllTabs();
				
                // Switch to "My Questions" tab to show the posted question
                tabPane.getSelectionModel().select(1); // Index 1 is My Questions
                
            } catch (IllegalArgumentException ex) {
                errorLabel.setText("Validation error: " + ex.getMessage());
            } catch (SQLException ex) {
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to Post Question");
                errorAlert.setContentText("Database error: " + ex.getMessage());
                errorAlert.showAndWait();
            }
        });
        
        content.getChildren().addAll(
            infoLabel,
            new Label(""),
            searchLabel, searchBox, searchResultsBox,
            sep1,
            titleLabel, titleField, titleCounter, titleValidation,
            contentLabel, contentArea, contentCounter, contentValidation,
            errorLabel,
            buttonBox
        );

		// Make scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

	
	// Method to display searched question
    private void displaySearchResults(VBox container, List<Question> results) {
        container.getChildren().clear();
        if (results.isEmpty()) {
			// Label if no results are found
            Label noResults = new Label("No similar questions found. You can proceed to ask your question.");
            noResults.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            container.getChildren().add(noResults);
			
        } else {
			// Label for if results are found
            Label header = new Label("Similar questions found (" + results.size() + "):");
            header.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff6600;");
            container.getChildren().add(header);
			
			// Loop over all questions to display them
            for (Question q : results) {
                VBox questionBox = new VBox(5);
                questionBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 8; -fx-border-color: #ddd; -fx-border-width: 1;");
                Label titleLbl = new Label(q.getTitle());
                titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
                Label metaLbl = new Label(q.getAnswers().size() + " answers • " + 
                                         (q.isResolved() ? "✓ Resolved" : "Unresolved") + 
                                         " • by " + q.getAskedBy());
                metaLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #666; ");
				
                Button viewBtn = new Button("View");
                viewBtn.setStyle("-fx-font-size: 10px;");
                viewBtn.setOnAction(e -> {
                    // Switch to all questions tab and show this question
                    tabPane.getSelectionModel().select(allQuestionsTab);

                });
                questionBox.getChildren().addAll(titleLbl, metaLbl, viewBtn);
                container.getChildren().add(questionBox);
            }
        }
    }

	
    // ========== MY QUESTIONS TAB ==========
    private Tab createMyQuestionsTab() {
		// Create tab to view your user questions
        Tab tab = new Tab("My Questions");
        tab.setClosable(false);
        VBox questionsContainer = new VBox(10);
		
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        Label infoLabel = new Label("Your questions and their answers");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        try {
            List<Question> myQuestions = databaseHelper.getAllQuestions(currentUser.getUserName());
            displayMyQuestions(questionsContainer, myQuestions);
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading questions: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            questionsContainer.getChildren().add(errorLabel);
            e.printStackTrace();
        }
        content.getChildren().addAll(infoLabel, questionsContainer);

		// Make scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        
        return tab;
    }


	// Display only questions asked by current user
    private void displayMyQuestions(VBox container, List<Question> questions) {
        container.getChildren().clear();
        if (questions.isEmpty()) {
            Label emptyLabel = new Label("You haven't asked any questions yet.");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            container.getChildren().add(emptyLabel);
            return;
        }

        for (Question q : questions) {
            VBox questionCard = createMyQuestionCard(q);
            container.getChildren().add(questionCard);
        }
    }

	
    private VBox createMyQuestionCard(Question question) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: #fafafa;");
		
        // Title and status
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(question.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c;");
        Label statusLabel = new Label(question.isResolved() ? "✓ RESOLVED" : "UNRESOLVED");
        statusLabel.setStyle(question.isResolved() ? 
            "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px;" :
            "-fx-background-color: #ff9800; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px;");
        headerBox.getChildren().addAll(titleLabel, statusLabel);
		
        // Content preview
        String contentPreview = question.getContent().length() > 150 ? 
            question.getContent().substring(0, 150) + "..." : 
            question.getContent();
        Label contentLabel = new Label(contentPreview);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #333;");
		
        // Metadata
        int unreadCount = 0 ;
		try {
			unreadCount = databaseHelper.getUnreadAnswersCount(question.getId());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Label metaLabel = new Label(
            question.getAnswers().size() + " answers" + 
            (unreadCount > 0 ? " (" + unreadCount + " unread)" : "") +
            " • Posted: " + question.getFormattedDate()
        );
        metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

		
        // Buttons
        Button viewAnswersBtn = new Button("View Answers (" + question.getAnswers().size() + ")");
        viewAnswersBtn.setStyle("-fx-background-color: #0099ff; -fx-text-fill: white;");
        viewAnswersBtn.setOnAction(e -> showAnswersDialog(question));

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        editBtn.setOnAction(e -> editQuestion(question));
        editBtn.setDisable(question.isResolved()); // Can't edit resolved questions

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteQuestion(question));

        // ADD CLOSE QUESTION BUTTON
        Button closeBtn = new Button("Close Question");
        closeBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        closeBtn.setOnAction(e -> closeQuestion(question));
        closeBtn.setVisible(!question.isResolved() && question.getAnswers().size() > 0); // Only show if unresolved and has answers

        HBox buttonBox = new HBox(10, viewAnswersBtn, editBtn, closeBtn, deleteBtn);
        card.getChildren().addAll(headerBox, contentLabel, metaLabel, buttonBox);
        return card;
    }
    

	// Mark question as resolved (close it)
    private void closeQuestion(Question question) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Close Question");
        confirm.setHeaderText("Close this question?");
        confirm.setContentText("This will mark your question as resolved. You've received " + 
                              question.getAnswers().size() + " answer(s). Are you satisfied with the responses?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = databaseHelper.closeQuestion(question.getId(), currentUser.getUserName());
                if (success) {
                    showAlert("Success", "Question closed successfully!", AlertType.INFORMATION);
                    refreshAllTabs();
                } else {
                    showAlert("Error", "Failed to close question.", AlertType.ERROR);
                }
            } catch (SQLException ex) {
                showAlert("Error", "Database error: " + ex.getMessage(), AlertType.ERROR);
            }
        }
    }

	
    // ========== ALL QUESTIONS TAB ==========
    private Tab createAllQuestionsTab() {
        Tab tab = new Tab("All Questions");
        tab.setClosable(false);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label signedIn = new Label("Signed in as: " + currentUser.getUserName() + " (" + currentUser.getRole() + ")");
        signedIn.setStyle("-fx-text-fill:#444; -fx-font-size:12px;");

        viewFilter = new ComboBox<>();
        viewFilter.getItems().addAll("Public", "Private", "All");
        viewFilter.setValue("All"); // default
        viewFilter.setTooltip(new Tooltip("Choose which posts to show in the thread"));

        HBox topBar = new HBox(12, new Separator(), signedIn, new Label("View:") {{ setStyle("-fx-text-fill: black;"); }}, viewFilter);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 10, 8, 10));
        topBar.setStyle("-fx-background-color:#f3f3f3;");

        // --- Left panel (questions list + New button) ---
        displayQuestions = new VBox(8);
        displayQuestions.setPadding(new Insets(10));

        Button newQuestionBtn = new Button("New Question");
        newQuestionBtn.setOnAction(e -> openCreateQuestionDialog());

        // Resolution filter (matches Threads-style simple controls)
        resolutionFilter = new ComboBox<>();
        resolutionFilter.getItems().addAll("All Questions", "Unresolved Only", "Resolved Only");
        resolutionFilter.setValue("All Questions"); // default
        resolutionFilter.setTooltip(new Tooltip("Filter by resolution status"));

        Label resolutionLbl = new Label("Resolution:");
        resolutionLbl.setStyle("-fx-text-fill: black; -fx-font-size:12px;");

        HBox filterBox = new HBox(8, resolutionLbl, resolutionFilter);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        // Inline error banner (kept subtle; only shown on failures)
        allQuestionsError = new Label();
        allQuestionsError.setStyle("-fx-text-fill: black; -fx-font-size:11px;");
        allQuestionsError.setVisible(false);

        // Rebuild the left side to include controls + error + list
        VBox leftSide = new VBox(10, newQuestionBtn, filterBox, allQuestionsError, displayQuestions);
        leftSide.setPadding(new Insets(10));

        ScrollPane scrollLeft = new ScrollPane(leftSide);
        scrollLeft.setFitToWidth(true);

        // --- Right panel (thread content) ---
        displayQuestionThread = new VBox(10);
        displayQuestionThread.setPadding(new Insets(12));

        ScrollPane scrollRight = new ScrollPane(displayQuestionThread);
        scrollRight.setFitToWidth(true);

        SplitPane split = new SplitPane(scrollLeft, scrollRight);
        split.setDividerPositions(0.35);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(split);
        
        // Change handler for the resolution filter
        resolutionFilter.setOnAction(e -> {
            allQuestionsError.setVisible(false);
            try {
                String f = resolutionFilter.getValue();
                List<Question> questions;

                if ("Unresolved Only".equals(f)) {
                    questions = databaseHelper.getUnresolvedQuestions();
                } else if ("Resolved Only".equals(f)) {
                    questions = databaseHelper.getAllQuestions(null);
                    questions.removeIf(q -> !q.isResolved());
                } else {
                    // default to top-level thread list ordering
                    questions = databaseHelper.getQuestionsTopLevel();
                }

                // Re-render the left-panel question list (Threads-style rows)
                displayQuestions.getChildren().clear();
                if (questions.isEmpty()) {
                    showEmptyLeftState();
                    showEmptyRightState();
                } else {
                    for (Question q : questions) {
                        displayQuestions.getChildren().add(questionRow(q));
                    }
                    // Keep UX parity: select the first row automatically
                    loadThread(questions.get(0));
                }
            } catch (SQLException ex) {
                displayQuestions.getChildren().clear();
                allQuestionsError.setText("Error loading questions: " + ex.getMessage());
                allQuestionsError.setVisible(true);
            }
        });

        // Initial load honoring the default filter
        resolutionFilter.getOnAction().handle(null);

        content.getChildren().addAll(root, new Separator(), split);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        
        root.setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");
        
        return tab;
    }
    
    private void refreshQuestionListAndMaybeSelect() {
        displayQuestions.getChildren().clear();
        try {
            List<Question> questions = databaseHelper.getQuestionsTopLevel();
            if (questions.isEmpty()) {
                showEmptyLeftState();
                showEmptyRightState();
                return;
            }
            for (Question q : questions) {
                displayQuestions.getChildren().add(questionRow(q));
            }
            // Select the first question by default
            loadThread(questions.get(0));
        } catch (SQLException ex) {
            showError("Load failed", ex.getMessage());
        }
    }

    private void showEmptyLeftState() {
        Label none = new Label("No questions yet.");
        none.setStyle("-fx-text-fill: black;");
        VBox box = new VBox(6, none);
        box.setPadding(new Insets(10));
        displayQuestions.getChildren().add(box);
    }

    private void showEmptyRightState() {
        displayQuestionThread.getChildren().clear();
        Label title = new Label("Start the discussion");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");
        Label text = new Label("There are no questions yet. Create one to begin.");
        Button create = new Button("Create your first question");
        create.setOnAction(e -> openCreateQuestionDialog());
        VBox inner = new VBox(10, title, text, create);
        inner.setAlignment(Pos.CENTER);
        inner.setPadding(new Insets(30));
        displayQuestionThread.getChildren().add(inner);
    }

    private HBox questionRow(Question q) {
        // Title
        Label title = new Label(q.getTitle());
        title.setStyle("-fx-font-weight:bold; -fx-text-fill: black;");
        
        // First metadata line: author and date
        Label meta1 = new Label("by " + q.getAskedBy() + " • " + q.getCreatedAt().format(TS));
        meta1.setStyle("-fx-text-fill: black; -fx-font-size:11px;");
        
        // Second metadata line: answer counts and resolution status
        StringBuilder meta2Text = new StringBuilder();
        
        try {
            int publicAnswerCount = databaseHelper.getAnswersForQuestion(q.getId()).size();
            List<PrivateMessage> privateMessages = databaseHelper.getPrivateMessagesForQuestion(q.getId());
            
            // Count visible private messages
            int visiblePrivateCount = 0;
            for (PrivateMessage pm : privateMessages) {
                if (canSeePrivate(q, pm)) {
                    visiblePrivateCount++;
                }
            }
            
            // Display answer counts
            if (visiblePrivateCount > 0) {
                meta2Text.append(publicAnswerCount).append(" public + ")
                         .append(visiblePrivateCount).append(" private");
            } else {
                meta2Text.append(publicAnswerCount).append(" answer");
                if (publicAnswerCount != 1) meta2Text.append("s");
            }
        } catch (SQLException ex) {
            meta2Text.append("answers unavailable");
        }
        
        // Add resolution status
        if (q.isResolved()) {
            meta2Text.append(" • ✓ Resolved");
        }
        
        Label meta2 = new Label(meta2Text.toString());
        meta2.setStyle("-fx-text-fill: #666; -fx-font-size:10px;");
        
        // Stack all labels vertically
        VBox col = new VBox(2, title, meta1, meta2);
        col.setPadding(new Insets(8));

        // Make whole row clickable
        HBox row = new HBox(col);
        row.setStyle("-fx-background-color:#ffffff; -fx-border-color:#e6e6e6;");
        row.setOnMouseClicked(e -> loadThread(q));
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f7faff; -fx-border-color:#cfe3ff;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color:#ffffff; -fx-border-color:#e6e6e6;"));
        return row;
    }

    private void loadThread(Question q) {
        displayQuestionThread.getChildren().clear();

        // header row with action buttons
        HBox headerActions = new HBox(10);
        Button logBtn = new Button("View Activity Log");
        logBtn.setOnAction(e -> openLogDialog(q));
        headerActions.getChildren().add(logBtn);

        boolean isAsker = currentUser.getUserName() != null &&
                currentUser.getUserName().equalsIgnoreCase(q.getAskedBy());
        boolean isStaff = isStaff(currentUser.getRole());

        if (isAsker || isStaff) {
            Button followup = new Button("Create revised question");
            followup.setOnAction(e -> openCreateFollowupDialog(q));
            headerActions.getChildren().add(followup);
        }
        
        // Add edit/delete buttons for question owner
        if (isAsker && !q.isResolved()) {
            Button editBtn = new Button("Edit Question");
            editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
            editBtn.setOnAction(e -> {
                editQuestion(q);
                refreshQuestionListAndMaybeSelect();
            });
            headerActions.getChildren().add(editBtn);
        }
        
        if (isAsker) {
            Button deleteBtn = new Button("Delete Question");
            deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> {
                deleteQuestion(q);
                refreshQuestionListAndMaybeSelect();
            });
            headerActions.getChildren().add(deleteBtn);
        }

        // header (question)
        Label title = new Label(q.getTitle());
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill: black;");
        Label meta = new Label("by " + q.getAskedBy() + " • " + q.getCreatedAt().format(TS));
        meta.setStyle("-fx-text-fill: black; -fx-font-size:12px;");
        Label body = new Label(q.getContent());
        body.setWrapText(true);
        body.setStyle("-fx-padding:8; -fx-background-color:#fff; -fx-border-color:#ddd; -fx-text-fill: black;");

        VBox header = new VBox(6, headerActions, title, meta, body);
        header.setPadding(new Insets(6,0,12,0));
        displayQuestionThread.getChildren().add(header);

        // If asker opened the thread, auto-mark unread private messages
        if (isAsker) {
            try { databaseHelper.markPrivateMessagesReadByAsker(q.getId(), currentUser.getUserName()); }
            catch (SQLException ignored) {}
        }

        // Thread body based on filter
        String mode = viewFilter.getValue();
        if ("Private".equals(mode)) {
            renderPrivateSection(q);
        } else if ("All".equals(mode)) {
            renderPublicAnswers(q);
            renderPrivateSection(q);
        } else { // Public
            renderPublicAnswers(q);
        }

        // revised children list
        renderFollowups(q);

        // composer(s)
        renderComposers(q, mode);

        // react to filter changes
        viewFilter.setOnAction(ev -> loadThread(q));
    }

    private void renderPublicAnswers(Question q) {
        try {
            List<Answer> answers = databaseHelper.getAnswersForQuestion(q.getId());
            
            // Header with "View All Answers" button
            HBox ansHeader = new HBox(10);
            ansHeader.setAlignment(Pos.CENTER_LEFT);
            
            Label ansTitle = new Label("Answers (" + answers.size() + ")");
            ansTitle.setStyle("-fx-font-weight:bold; -fx-text-fill: black;");
            
            Button viewAllBtn = new Button("View All Answers Dialog");
            viewAllBtn.setStyle("-fx-background-color: #0099ff; -fx-text-fill: white; -fx-font-size: 11px;");
            viewAllBtn.setOnAction(e -> {
                // Reload question with fresh answer data
                try {
                    Question freshQ = databaseHelper.getQuestionById(q.getId());
                    if (freshQ != null) {
                        showAnswersDialog(freshQ);
                    }
                } catch (SQLException ex) {
                    showError("Load failed", ex.getMessage());
                }
            });
            
            ansHeader.getChildren().addAll(ansTitle, viewAllBtn);
            VBox answersBox = new VBox(8, ansHeader);

            boolean isAsker = currentUser.getUserName() != null &&
                    currentUser.getUserName().equalsIgnoreCase(q.getAskedBy());

            if (answers.isEmpty()) {
                Label none = new Label("No answers yet. Be the first to help!");
                none.setStyle("-fx-text-fill: black;");
                answersBox.getChildren().add(none);
            } else {
                for (Answer a : answers) {
                    VBox card = new VBox(5);
                    
                    // Answer metadata and content
                    Label aMeta = new Label(a.getAnsweredBy() + " • " + a.getCreatedAt().format(TS) + " • " + a.getUpvotes() + " upvotes");
                    aMeta.setStyle("-fx-text-fill: black; -fx-font-size:11px;");
                    Label aText = new Label(a.getContent());
                    aText.setWrapText(true);
                    aText.setStyle("-fx-text-fill: black;");
                    
                    // Action buttons
                    HBox actionBox = new HBox(5);
                    
                    // Upvote button (for everyone except the answer author)
                    if (!a.getAnsweredBy().equals(currentUser.getUserName())) {
                        Button upvoteBtn = new Button("👍 Upvote");
                        upvoteBtn.setStyle("-fx-font-size: 10px;");
                        upvoteBtn.setOnAction(e -> {
                            try {
                                databaseHelper.upvoteAnswer(a.getId(), a.getAnsweredBy());
                                showAlert("Success", "Upvoted!", AlertType.INFORMATION);
                                loadThread(q); // Refresh
                            } catch (SQLException ex) {
                                showAlert("Error", "Failed to upvote: " + ex.getMessage(), AlertType.ERROR);
                            }
                        });
                        actionBox.getChildren().add(upvoteBtn);
                    }
                    
                    // Mark as solution button (only for question owner on unresolved questions)
                    if (isAsker && !q.isResolved()) {
                        Button markBtn = new Button("✓ Mark as Solution");
                        markBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");
                        markBtn.setOnAction(e -> {
                            try {
                                databaseHelper.markQuestionResolved(q.getId(), a.getId(), currentUser.getUserName());
                                showAlert("Success", "Question marked as resolved!", AlertType.INFORMATION);
                                refreshQuestionListAndMaybeSelect();
                            } catch (SQLException ex) {
                                showAlert("Error", "Failed to mark as resolved: " + ex.getMessage(), AlertType.ERROR);
                            }
                        });
                        actionBox.getChildren().add(markBtn);
                    }
                    
                    // Edit/Delete buttons (only for answer owner)
                    if (a.getAnsweredBy().equals(currentUser.getUserName())) {
                        Button editBtn = new Button("Edit");
                        editBtn.setStyle("-fx-font-size: 10px;");
                        editBtn.setOnAction(e -> {
                            editAnswer(a);
                            loadThread(q); // Refresh
                        });
                        
                        Button deleteBtn = new Button("Delete");
                        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");
                        deleteBtn.setOnAction(e -> {
                            try {
                                databaseHelper.deleteAnswer(a.getId(), currentUser.getUserName());
                                showAlert("Success", "Answer deleted!", AlertType.INFORMATION);
                                loadThread(q); // Refresh
                            } catch (SQLException ex) {
                                showAlert("Error", "Failed to delete answer: " + ex.getMessage(), AlertType.ERROR);
                            }
                        });
                        
                        actionBox.getChildren().addAll(editBtn, deleteBtn);
                    }
                    
                    card.getChildren().addAll(aMeta, aText, actionBox);
                    
                    // Highlight if this is the accepted answer
                    if (q.isResolved() && a.getId() == q.getResolvedAnswerId()) {
                        card.setStyle("-fx-background-color:#e8f5e9; -fx-border-color:#4CAF50; -fx-border-width:2; -fx-padding:8;");
                        Label acceptedLabel = new Label("✓ Accepted Answer");
                        acceptedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-font-size:10px;");
                        card.getChildren().add(0, acceptedLabel);
                    } else {
                        card.setStyle("-fx-background-color:#f9f9f9; -fx-border-color:#eee; -fx-padding:8;");
                    }
                    
                    answersBox.getChildren().add(card);
                }
            }
            displayQuestionThread.getChildren().add(answersBox);
        } catch (SQLException ex) {
            showError("Answers load failed", ex.getMessage());
        }
    }

    private void renderPrivateSection(Question q) {
        VBox box = new VBox(8);
        
        try {
            List<PrivateMessage> all = databaseHelper.getPrivateMessagesForQuestion(q.getId());
            
            // Count visible messages first
            int visibleCount = 0;
            for (PrivateMessage pm : all) {
                if (canSeePrivate(q, pm)) {
                    visibleCount++;
                }
            }
            
            // Create the title with the count
            Label title = new Label("Private feedback (" + visibleCount + ")");
            title.setStyle("-fx-font-weight:bold; -fx-text-fill: black;");
            box.getChildren().add(title);
            
            // Render the messages
            boolean any = false;
            for (PrivateMessage pm : all) {
                if (!canSeePrivate(q, pm)) continue;
                any = true;
                VBox card = new VBox(3);
                Label meta = new Label(pm.getSender() + " • " + pm.getCreatedAt().format(TS) + " • " + pm.getMessageType());
                meta.setStyle("-fx-text-fill: black; -fx-font-size:11px;");
                Label text = new Label(pm.getContent());
                text.setWrapText(true);
                text.setStyle("-fx-text-fill: black;");
                card.getChildren().addAll(meta, text);
                card.setStyle("-fx-background-color:#fffbea; -fx-border-color:#ffe08a; -fx-padding:8;");
                box.getChildren().add(card);
            }
            
            if (!any) {
                Label none = new Label("No private feedback yet.");
                none.setStyle("-fx-text-fill: black;");
                box.getChildren().add(none);
            }
        } catch (SQLException ex) {
            showError("Private messages load failed", ex.getMessage());
        }

        displayQuestionThread.getChildren().add(box);
    }

    private void renderFollowups(Question q) {
        try {
            List<Question> kids = databaseHelper.getFollowupQuestions(q.getId());
            if (kids == null || kids.isEmpty()) return;
            Label head = new Label("Revised questions");
            head.setStyle("-fx-font-weight:bold;");
            VBox list = new VBox(6);
            for (Question k : kids) {
                Hyperlink link = new Hyperlink(k.getTitle() + " — " + k.getCreatedAt().format(TS));
                link.setOnAction(e -> loadThread(k));
                list.getChildren().add(link);
            }
            VBox wrap = new VBox(6, head, list);
            wrap.setPadding(new Insets(8,0,0,0));
            displayQuestionThread.getChildren().add(wrap);
        } catch (SQLException ex) {
            showError("Followups load failed", ex.getMessage());
        }
    }

    private void renderComposers(Question q, String mode) {
        // public composer
        if (!"Private".equals(mode)) {
            displayQuestionThread.getChildren().add(answerComposer(q));
        }
        // private composer
        if (!"Public".equals(mode)) {
            displayQuestionThread.getChildren().add(privateComposer(q));
        }
    }

    private VBox answerComposer(Question q) {
        Label lbl = new Label("Add a public answer");
        lbl.setStyle("-fx-font-weight:bold; -fx-text-fill: black;");
        TextArea text = new TextArea();
        text.setPromptText("Write your answer...");
        text.setWrapText(true);
        text.setPrefRowCount(3);
        Button post = new Button("Post Public Answer");
        post.setOnAction(e -> {
            String content = trimOrEmpty(text.getText());
            if (content.length() < 5) {
                showError("Too short", "Please write a longer answer.");
                return;
            }
            try {
                Answer a = new Answer(q.getId(), content, currentUser.getUserName());
                databaseHelper.createAnswer(a);
                loadThread(q); // refresh thread
            } catch (SQLException ex) {
                showError("Post failed", ex.getMessage());
            }
        });
        VBox box = new VBox(6, lbl, text, post);
        box.setPadding(new Insets(10,0,0,0));
        return box;
    }

    private VBox privateComposer(Question q) {
        Label lbl = new Label("Send private feedback to the asker");
        lbl.setStyle("-fx-font-weight:bold; -fx-text-fill: black;");
        ComboBox<String> kind = new ComboBox<>();
        kind.getItems().addAll("Question", "Answer");
        kind.setValue("Question");
        TextArea text = new TextArea();
        text.setPromptText("Write your private message...");
        text.setWrapText(true);
        text.setPrefRowCount(3);
        Button send = new Button("Send Private");
        send.setOnAction(e -> {
            String content = trimOrEmpty(text.getText());
            if (content.length() < 5) {
                showError("Too short", "Please write a longer message.");
                return;
            }
            try {
                databaseHelper.addPrivateMessage(q.getId(), currentUser.getUserName(), kind.getValue().toUpperCase(), content);
                loadThread(q);
            } catch (SQLException ex) {
                showError("Send failed", ex.getMessage());
            }
        });
        VBox box = new VBox(6, lbl, new HBox(6, new Label("Type:") {{ setStyle("-fx-text-fill: black;"); }}, kind), text, send);
        box.setPadding(new Insets(10,0,0,0));
        return box;
    }

    private void openCreateQuestionDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Question");

        Label l1 = new Label("Title:");
        TextField tfTitle = new TextField();
        tfTitle.setPromptText("Short summary of your problem");

        Label l2 = new Label("Content:");
        TextArea taContent = new TextArea();
        taContent.setPromptText("Describe the problem in more detail");
        taContent.setWrapText(true);
        taContent.setPrefRowCount(4);

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.add(l1, 0, 0); gp.add(tfTitle, 1, 0);
        gp.add(l2, 0, 1); gp.add(taContent, 1, 1);

        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(btn -> btn);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String t = trimOrEmpty(tfTitle.getText());
                String c = trimOrEmpty(taContent.getText());
                if (t.length() < 5 || c.length() < 10) {
                    showError("Invalid input", "Please provide a longer title and content.");
                    return;
                }
                try {
                    Question q = new Question(t, c, currentUser.getUserName());
                    databaseHelper.createQuestion(q);
                    refreshQuestionListAndMaybeSelect();
                } catch (SQLException ex) {
                    showError("Create failed", ex.getMessage());
                }
            }
        });
    }

    private void openCreateFollowupDialog(Question parent) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create revised question");

        Label l1 = new Label("Title:");
        TextField tfTitle = new TextField();
        tfTitle.setPromptText("Updated title based on feedback");

        Label l2 = new Label("Content:");
        TextArea taContent = new TextArea();
        taContent.setPromptText("Write the revised question content");
        taContent.setWrapText(true);
        taContent.setPrefRowCount(4);

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.add(l1, 0, 0); gp.add(tfTitle, 1, 0);
        gp.add(l2, 0, 1); gp.add(taContent, 1, 1);

        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String t = trimOrEmpty(tfTitle.getText());
                String c = trimOrEmpty(taContent.getText());
                if (t.length() < 5 || c.length() < 10) {
                    showError("Invalid input", "Please provide a longer title and content.");
                    return;
                }
                try {
                    databaseHelper.createFollowupQuestion(parent.getId(), t, c, currentUser.getUserName());
                    loadThread(parent); // reload; children list will refresh
                } catch (SQLException ex) {
                    showError("Create failed", ex.getMessage());
                }
            }
        });
    }

    private void openLogDialog(Question q) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Activity Log");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox content = new VBox(8);
        content.setPadding(new Insets(10));

        List<EventRecord> events = new ArrayList<>();
        // question created
        events.add(new EventRecord(q.getCreatedAt(), "Question created by " + q.getAskedBy() + ": " + q.getTitle()));

        try {
            // answers
            for (Answer a : databaseHelper.getAnswersForQuestion(q.getId())) {
                events.add(new EventRecord(a.getCreatedAt(), "Answer by " + a.getAnsweredBy() + ": " + a.getContent()));
            }
            // private (filter by visibility)
            for (PrivateMessage pm : databaseHelper.getPrivateMessagesForQuestion(q.getId())) {
                if (canSeePrivate(q, pm)) {
                    events.add(new EventRecord(pm.getCreatedAt(), "Private " + pm.getMessageType() + " by " + pm.getSender() + ": " + pm.getContent()));
                }
            }
            // followups
            for (Question child : databaseHelper.getFollowupQuestions(q.getId())) {
                events.add(new EventRecord(child.getCreatedAt(), "Revised question created by " + child.getAskedBy() + ": " + child.getTitle()));
            }
        } catch (SQLException ex) {
            content.getChildren().add(new Label("Failed to load log: " + ex.getMessage()));
        }

        events.sort(Comparator.comparing(e -> e.t));
        for (EventRecord e : events) {
            Label line = new Label("• " + TS.format(e.t) + " — " + e.text);
            line.setWrapText(true);
            content.getChildren().add(line);
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        dialog.getDialogPane().setContent(sp);
        dialog.showAndWait();
    }

    // --- utils ---

    private boolean canSeePrivate(Question q, PrivateMessage pm) {
        boolean isAsker = currentUser.getUserName() != null &&
                currentUser.getUserName().equalsIgnoreCase(q.getAskedBy());
        boolean isSender = currentUser.getUserName() != null &&
                currentUser.getUserName().equalsIgnoreCase(pm.getSender());
        boolean isStaff = isStaff(currentUser.getRole());
        return isAsker || isSender || isStaff;
    }

    private boolean isStaff(String role) {
        if (role == null) return false;
        String r = role.toLowerCase();
        return r.contains("admin") || r.contains("instructor") || r.contains("staff");
    }

    private String trimOrEmpty(String s) {
        return (s == null) ? "" : s.trim();
    }
    
    // Simple error alert helper used by posting/loading actions.
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null); // keep it clean
        alert.showAndWait();
    }  

    // simple record for the log
    private static class EventRecord {
        LocalDateTime t;
        String text;
        EventRecord(LocalDateTime t, String text) { this.t = t; this.text = text; }
    }

    // Display all questions
    private void displayAllQuestions(VBox container, List<Question> questions) {
        container.getChildren().clear();
        if (questions.isEmpty()) {
            Label emptyLabel = new Label("No questions found.");
            emptyLabel.setStyle("-fx-text-fill: #2c2c2c; -fx-font-style: italic;");
            container.getChildren().add(emptyLabel);
            return;
        }

        for (Question q : questions) {
            VBox questionCard = createAllQuestionCard(q);
            container.getChildren().add(questionCard);
        }
    }

	
    private VBox createAllQuestionCard(Question question) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: white;");

		
        // Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(question.getTitle());
        titleLabel.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#2c2c2c;");

        Label statusLabel = new Label(question.isResolved() ? "✓ RESOLVED" : "");
        statusLabel.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white; -fx-padding:3 8; -fx-font-size:10px;");
        statusLabel.setVisible(question.isResolved());

        headerBox.getChildren().addAll(titleLabel, statusLabel);

		
        // Content
        Label contentLabel = new Label(question.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-padding:8; -fx-background-color:#fff; -fx-border-color:#ddd;");

		
        // Metadata
        int unreadCount = 0 ;
		try {
			unreadCount = databaseHelper.getUnreadAnswersCount(question.getId());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Label metaLabel = new Label(
            "Asked by: " + question.getAskedBy() + 
            " • " + question.getAnswers().size() + " answers" +
            (unreadCount > 0 ? " (" + unreadCount + " unread)" : "") +
            " • " + question.getFormattedDate()
        );
        metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

		
        // Show resolved answer if exists
        if (question.isResolved()) {
            Optional<Answer> resolvedAnswer = question.getAnswers().stream()
                .filter(a -> a.getId() == question.getResolvedAnswerId())
                .findFirst();
            if (resolvedAnswer.isPresent()) {
                VBox resolvedBox = new VBox(5);
                resolvedBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-border-color: #4CAF50; -fx-border-width: 1;");
                Label resolvedLabel = new Label("✓ Accepted Answer:");
                resolvedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                Label answerContent = new Label(resolvedAnswer.get().getContent());
                answerContent.setWrapText(true);
                answerContent.setStyle(" -fx-text-fill: #2c2c2c;");
                Label answerMeta = new Label("by " + resolvedAnswer.get().getAnsweredBy() + 
                                            " • " + resolvedAnswer.get().getUpvotes() + " upvotes");
                answerMeta.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                resolvedBox.getChildren().addAll(resolvedLabel, answerContent, answerMeta);
                card.getChildren().add(resolvedBox);
            }
        }
        // Buttons
        Button viewBtn = new Button("View All Answers");
        viewBtn.setStyle("-fx-background-color: #0099ff; -fx-text-fill: white;");
        viewBtn.setOnAction(e -> showAnswersDialog(question));
        Button answerBtn = new Button("Provide Answer");
        answerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        answerBtn.setOnAction(e -> provideAnswer(question));
        HBox buttonBox = new HBox(10, viewBtn, answerBtn);
        card.getChildren().addAll(headerBox, contentLabel, metaLabel, buttonBox);
        return card;
    }

	
    // ========== DIALOG METHODS ==========
    private void showAnswersDialog(Question question) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Answers for: " + question.getTitle());
        dialog.setHeaderText(question.getAnswers().size() + " answer(s)");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setMaxWidth(600);

		
        if (question.getAnswers().isEmpty()) {
            Label noAnswers = new Label("No answers yet. Be the first to answer!");
            noAnswers.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            content.getChildren().add(noAnswers);
        } else {
            for (Answer answer : question.getAnswers()) {
                VBox answerBox = new VBox(5);
                answerBox.setPadding(new Insets(10));
                boolean isResolved = question.isResolved() && answer.getId() == question.getResolvedAnswerId();
                answerBox.setStyle(isResolved ? 
                    "-fx-border-color: #4CAF50; -fx-border-width: 2; -fx-background-color: #e8f5e9;" :
                    "-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: #fafafa;");

                if (isResolved) {
                    Label acceptedLabel = new Label("✓ Accepted Answer");
                    acceptedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                    answerBox.getChildren().add(acceptedLabel);
                }

                Label contentLabel = new Label(answer.getContent());
                contentLabel.setWrapText(true);
                contentLabel.setStyle("-fx-text-fill: #2c2c2c;");
                Label metaLabel = new Label(
                    "by " + answer.getAnsweredBy() + 
                    " • " + answer.getUpvotes() + " upvotes" +
                    " • " + answer.getFormattedDate()
                );
                metaLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                HBox actionBox = new HBox(5);

                
                // Upvote button
                Button upvoteBtn = new Button("👍 Upvote");
                upvoteBtn.setStyle("-fx-font-size: 10px;");
                upvoteBtn.setOnAction(e -> {
                    try {
                        databaseHelper.upvoteAnswer(answer.getId(), answer.getAnsweredBy());
                        showAlert("Success", "Upvoted!", AlertType.INFORMATION);
                        dialog.close();
                        refreshAllTabs();
                    } catch (SQLException ex) {
                        showAlert("Error", "Failed to upvote: " + ex.getMessage(), AlertType.ERROR);
                    }
                });

                actionBox.getChildren().add(upvoteBtn);

				
                // If this is the question owner and question is not resolved
                if (question.getAskedBy().equals(currentUser.getUserName()) && !question.isResolved()) {
                    Button markResolvedBtn = new Button("✓ Mark as Solution");
                    markResolvedBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px;");
                    markResolvedBtn.setOnAction(e -> {
                        try {
                            databaseHelper.markQuestionResolved(question.getId(), answer.getId(), currentUser.getUserName());
                            showAlert("Success", "Question marked as resolved!", AlertType.INFORMATION);
                            dialog.close();
                            refreshAllTabs();
                        } catch (SQLException ex) {
                            showAlert("Error", "Failed to mark as resolved: " + ex.getMessage(), AlertType.ERROR);
                        }
                    });
                    actionBox.getChildren().add(markResolvedBtn);
                }

				
                // If this is the answer owner
                if (answer.getAnsweredBy().equals(currentUser.getUserName())) {
                    Button editBtn = new Button("Edit");
                    editBtn.setStyle("-fx-font-size: 10px;");
                    editBtn.setOnAction(e -> {
                        editAnswer(answer);
						dialog.close();
						refreshAllTabs();
                    });
                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");
                    deleteBtn.setOnAction(e -> {
                        try {
                            databaseHelper.deleteAnswer(answer.getId(), currentUser.getUserName());
                            showAlert("Success", "Answer deleted!", AlertType.INFORMATION);
                            dialog.close();
                            refreshAllTabs();
                        } catch (SQLException ex) {
                            showAlert("Error", "Failed to delete answer: " + ex.getMessage(), AlertType.ERROR);
                        }
                    });

                    actionBox.getChildren().addAll(editBtn, deleteBtn);
                }
                answerBox.getChildren().addAll(contentLabel, metaLabel, actionBox);
                content.getChildren().add(answerBox);
            }
        }

		
        // Mark answers as read if this is the question owner
        if (question.getAskedBy().equals(currentUser.getUserName())) {
            for (Answer answer : question.getAnswers()) {
                if (!answer.isRead()) {
                    try {
                        databaseHelper.markAnswerAsRead(answer.getId());
                    } catch (SQLException e) {
                        // Silently fail
                    }
                }
            }
        }

		
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
        refreshAllTabs();
    }


	// Method to provide an answer to a question
    private void provideAnswer(Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Provide Answer");
        dialog.setHeaderText("Answer to: " + question.getTitle());
        dialog.initOwner(primaryStage); // ADD THIS LINE - ensures proper parent
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        
        Label infoLabel = new Label("Share your knowledge to help others!");
        infoLabel.setStyle("-fx-text-fill: #666;");
        
        TextArea answerArea = new TextArea();
        answerArea.setPromptText("Enter your answer (5-500 characters)");
        answerArea.setWrapText(true);
        answerArea.setPrefRowCount(8);
        answerArea.setMaxWidth(500);
        
        Label counter = new Label("0/" + Answer.CONTENT_MAX_LENGTH);
        counter.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        Label validation = new Label();
        validation.setWrapText(true);
        validation.setMaxWidth(500);

		// Listener to keep answer under max length and check input
        answerArea.textProperty().addListener((obs, old, newVal) -> {
            counter.setText(newVal.length() + "/" + Answer.CONTENT_MAX_LENGTH);
            counter.setStyle(newVal.length() > Answer.CONTENT_MAX_LENGTH ? 
                "-fx-font-size: 10px; -fx-text-fill: red;" : 
                "-fx-font-size: 10px; -fx-text-fill: #666;");
            
            if (newVal.length() > Answer.CONTENT_MAX_LENGTH) {
                answerArea.setText(newVal.substring(0, Answer.CONTENT_MAX_LENGTH));
            }
            
            if (!newVal.trim().isEmpty()) {
                InputValidator.ValidationReport report = InputValidator.validateAnswerContent(newVal);
                if (report.hasIssues()) {
                    validation.setText(report.getFullReport());
                    validation.setStyle(report.canSubmit() ? 
                        "-fx-text-fill: orange; -fx-font-size: 11px;" : 
                        "-fx-text-fill: red; -fx-font-size: 11px;");
                } else {
                    validation.setText("✓ Answer looks good!");
                    validation.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
                }
            } else {
                validation.setText("");
            }
        });
        
        content.getChildren().addAll(infoLabel, answerArea, counter, validation);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		
        // Disable OK button when text is empty or too short
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        answerArea.textProperty().addListener((obs, old, newVal) -> {
            okButton.setDisable(newVal.trim().length() < Answer.CONTENT_MIN_LENGTH);
        });

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return answerArea.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(answerText -> {
            if (!answerText.isEmpty()) {
                // Validate
                InputValidator.ValidationReport report = InputValidator.validateAnswerContent(answerText);
                
                if (!report.canSubmit()) {
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.initOwner(primaryStage); // ADD THIS
                    errorAlert.setTitle("Validation Error");
                    errorAlert.setHeaderText("Answer Validation Failed");
                    errorAlert.setContentText(report.getFullReport());
                    errorAlert.showAndWait();
                    return;
                }
                
                String finalAnswer = answerText;
				
                // Show warnings and offer corrections
                if (report.hasIssues()) {
                    Alert warningAlert = new Alert(AlertType.CONFIRMATION);
                    warningAlert.initOwner(primaryStage); // ADD THIS
                    warningAlert.setTitle("Validation Warnings");
                    warningAlert.setHeaderText("Your answer has some suggestions:");
                    
                    ButtonType applyButton = new ButtonType("Apply Corrections", ButtonBar.ButtonData.OK_DONE);
                    ButtonType submitAsIsButton = new ButtonType("Submit As-Is", ButtonBar.ButtonData.NO);
                    ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    
                    warningAlert.getButtonTypes().setAll(applyButton, submitAsIsButton, cancelButton);
                    warningAlert.setContentText(report.getFullReport());
                    
                    Optional<ButtonType> warningResult = warningAlert.showAndWait();
                    if (warningResult.isPresent()) {
                        if (warningResult.get() == applyButton) {
                            finalAnswer = report.getCorrectedText().isEmpty() ? answerText : report.getCorrectedText();
                        } else if (warningResult.get() == cancelButton) {
                            return; // Don't submit
                        }
                        // If submitAsIsButton, use original
                    } else {
                        return; // Dialog closed, don't submit
                    }
                }
                
                try {
                    Answer answer = new Answer(question.getId(), finalAnswer, currentUser.getUserName());
                    databaseHelper.createAnswer(answer);
                    
                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.initOwner(primaryStage); // ADD THIS
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Answer Posted!");
                    successAlert.setContentText("Your answer has been posted successfully.");
                    successAlert.showAndWait();
                    
                    refreshAllTabs();
                } catch (IllegalArgumentException ex) {
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.initOwner(primaryStage); // ADD THIS
                    errorAlert.setTitle("Error");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                } catch (SQLException ex) {
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.initOwner(primaryStage); // ADD THIS
                    errorAlert.setTitle("Database Error");
                    errorAlert.setContentText("Failed to post answer: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }

    
	// Method to edit existing question
    private void editQuestion(Question question) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Edit Question");
        dialog.setHeaderText("Update your question");
		
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
		
        Label titleLabel = new Label("Title:");
		
        TextField titleField = new TextField(question.getTitle());
        titleField.setMaxWidth(500);
		
        Label contentLabel = new Label("Content:");
		
        TextArea contentArea = new TextArea(question.getContent());
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(8);
        contentArea.setMaxWidth(500);
        content.getChildren().addAll(titleLabel, titleField, contentLabel, contentArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
  

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    question.setTitle(titleField.getText());
                    question.setContent(contentArea.getText());
                    return question;
                } catch (IllegalArgumentException e) {
                    showAlert("Error", e.getMessage(), AlertType.ERROR);
                }
            }
            return null;
        });

        
        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(updatedQuestion -> {
            try {
                databaseHelper.updateQuestion(updatedQuestion);
                showAlert("Success", "Question updated!", AlertType.INFORMATION);
                refreshAllTabs();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update question: " + ex.getMessage(), AlertType.ERROR);
            }
        });
    }

    
	// Method to delete question
    private void deleteQuestion(Question question) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Delete Question");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will permanently delete your question and all its answers.");
		
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                databaseHelper.deleteQuestion(question.getId(), currentUser.getUserName());
                showAlert("Success", "Question deleted!", AlertType.INFORMATION);
                refreshAllTabs();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to delete question: " + ex.getMessage(), AlertType.ERROR);
            }
        }
    }


	// Method to edit existing answer
    private void editAnswer(Answer answer) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Answer");
        dialog.setHeaderText("Update your answer");
		
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
		
        TextArea answerArea = new TextArea(answer.getContent());
        answerArea.setWrapText(true);
        answerArea.setPrefRowCount(8);
        answerArea.setMaxWidth(500);
        content.getChildren().add(answerArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        
        dialog.setResultConverter(button -> {

            if (button == ButtonType.OK) {
                return answerArea.getText().trim();
            }
            return null;
        });

		
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            try {
                answer.setContent(newContent);
                databaseHelper.updateAnswer(answer);
                showAlert("Success", "Answer updated!", AlertType.INFORMATION);
                refreshAllTabs();
            } catch (IllegalArgumentException ex) {
                showAlert("Error", ex.getMessage(), AlertType.ERROR);
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update answer: " + ex.getMessage(), AlertType.ERROR);
            }
        });

    }


    // ========== UTILITY METHODS ==========
    private void refreshAllTabs() {
        // Store current selection
        int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
        // Recreate the tabs
        Tab newAskTab = createAskQuestionTab();
        Tab newMyQuestionsTab = createMyQuestionsTab();
        Tab newAllQuestionsTab = createAllQuestionsTab();
        // Replace tabs
        tabPane.getTabs().clear();
        tabPane.getTabs().addAll(newAskTab, newMyQuestionsTab, newAllQuestionsTab);
        // Restore selection
        if (selectedIndex >= 0 && selectedIndex < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(selectedIndex);
        }
        // Update references
        askQuestionTab = newAskTab;
        myQuestionsTab = newMyQuestionsTab;
        allQuestionsTab = newAllQuestionsTab;
    }

    
	// Method to show alert
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}