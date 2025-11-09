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
import application.AnswerFeedback;
import java.util.Map;
import java.util.HashMap;

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
    private VBox displayReviewers;
    private VBox displayQuestionThread;
    private ComboBox<String> viewFilter; // Public / Private / All
    private ComboBox<String> resolutionFilter;   // Resolved / Unresolved / All
    private ComboBox<String> reviewerFilter;	// All / Favorite / Pending
    private Label allQuestionsError;             // Inline error banner for the All Questions tab
	
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    // Tabs
    private Tab myQuestionsTab;
    private Tab allQuestionsTab;
    private Tab askQuestionTab;
    private Tab reviewersTab;
    
    public StudentQAPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    public void show(Stage primaryStage) {
        this.primaryStage = primaryStage;
        mainLayout = new VBox(-2);
        mainLayout.setPadding(new Insets(21));
        mainLayout.setPrefSize(1100, 600);

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
        reviewersTab = createReviewersTab();
        tabPane.getTabs().addAll(askQuestionTab, myQuestionsTab, allQuestionsTab, reviewersTab);
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

    // ========== REVIEWER REQUEST TAB ==========
    private Tab createReviewersTab() {
        Tab tab = new Tab("Reviewers");
        tab.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        tab.setClosable(false);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label signedIn = new Label("Signed in as: " + currentUser.getUserName() + " (" + currentUser.getRole() + ")");
        signedIn.setStyle("-fx-text-fill:#444; -fx-font-size:12px;");

        ComboBox<String> reviewerFilterCombo = new ComboBox<>();
        reviewerFilterCombo.getItems().addAll("All Reviewers", "Favorite Reviewers", "Pending Reviewers");
        reviewerFilterCombo.setValue("All Reviewers");
        reviewerFilterCombo.setTooltip(new Tooltip("Choose which reviewers to show"));

        HBox topBar = new HBox(12, new Separator(), signedIn, new Label("View:") {{ setStyle("-fx-text-fill: black;"); }}, reviewerFilterCombo);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 10, 8, 10));
        topBar.setStyle("-fx-background-color:#f3f3f3;");

        displayReviewers = new VBox(8);

        content.getChildren().addAll(topBar, displayReviewers);

		// Make scrollable
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }
    // ========== END REVIEWER REQUEST TAB ==========
    
    
    
    // ========== CREATE QUESTION TAB ============
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
        
        // Spell check button for title
        Button spellCheckTitleBtn = new Button("✓ Check Spelling");
        spellCheckTitleBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        
        Button applyTitleCorrectionsBtn = new Button("Apply Corrections");
        applyTitleCorrectionsBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        applyTitleCorrectionsBtn.setVisible(false);
        
        spellCheckTitleBtn.setOnAction(e -> {
            String titleText = titleField.getText();
            if (titleText != null && !titleText.trim().isEmpty()) {
                SpellChecker.ValidationResult spellResult = SpellChecker.validateText(titleText);
                if (spellResult.hasIssues()) {
                    titleValidation.setText("Suggestions found. Click 'Apply Corrections' to fix.\n" + spellResult.getSummary());
                    titleValidation.setStyle("-fx-text-fill: orange; -fx-font-size: 11px;");
                    applyTitleCorrectionsBtn.setVisible(true);
                } else {
                    titleValidation.setText("✓ No spelling errors found!");
                    titleValidation.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
                    applyTitleCorrectionsBtn.setVisible(false);
                }
            }
        });
        
        applyTitleCorrectionsBtn.setOnAction(e -> {
            String titleText = titleField.getText();
            if (titleText != null && !titleText.trim().isEmpty()) {
                SpellChecker.ValidationResult spellResult = SpellChecker.validateText(titleText);
                titleField.setText(spellResult.getCorrectedText());
                titleValidation.setText("✓ Corrections applied!");
                titleValidation.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
                applyTitleCorrectionsBtn.setVisible(false);
            }
        });
        
        HBox titleButtonBox = new HBox(10, titleCounter, spellCheckTitleBtn, applyTitleCorrectionsBtn);
        titleButtonBox.setAlignment(Pos.CENTER_LEFT);
        
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
            
            // Real-time validation (length only, not spelling)
            if (!newVal.trim().isEmpty()) {
                InputValidator.ValidationReport report = InputValidator.validateQuestionTitle(newVal);
                if (report.hasIssues() && !report.canSubmit()) {
                    // Only show blocking errors in real-time
                    titleValidation.setText(report.getFullReport());
                    titleValidation.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                } else if (newVal.length() >= Question.TITLE_MIN_LENGTH) {
                    titleValidation.setText("✓ Title length is good!");
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
        
        // Spell check button for content
        Button spellCheckContentBtn = new Button("✓ Check Spelling");
        spellCheckContentBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        
        Button applyContentCorrectionsBtn = new Button("Apply Corrections");
        applyContentCorrectionsBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        applyContentCorrectionsBtn.setVisible(false);
        
        spellCheckContentBtn.setOnAction(e -> {
            String contentText = contentArea.getText();
            if (contentText != null && !contentText.trim().isEmpty()) {
                SpellChecker.ValidationResult spellResult = SpellChecker.validateText(contentText);
                if (spellResult.hasIssues()) {
                    contentValidation.setText("Suggestions found. Click 'Apply Corrections' to fix.\n" + spellResult.getSummary());
                    contentValidation.setStyle("-fx-text-fill: orange; -fx-font-size: 11px;");
                    applyContentCorrectionsBtn.setVisible(true);
                } else {
                    contentValidation.setText("✓ No spelling errors found!");
                    contentValidation.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
                    applyContentCorrectionsBtn.setVisible(false);
                }
            }
        });
        
        applyContentCorrectionsBtn.setOnAction(e -> {
            String contentText = contentArea.getText();
            if (contentText != null && !contentText.trim().isEmpty()) {
                SpellChecker.ValidationResult spellResult = SpellChecker.validateText(contentText);
                contentArea.setText(spellResult.getCorrectedText());
                contentValidation.setText("✓ Corrections applied!");
                contentValidation.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
                applyContentCorrectionsBtn.setVisible(false);
            }
        });
        
        HBox contentButtonBox = new HBox(10, contentCounter, spellCheckContentBtn, applyContentCorrectionsBtn);
        contentButtonBox.setAlignment(Pos.CENTER_LEFT);
        
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
            
            // Real-time validation (length only, not spelling)
            if (!newVal.trim().isEmpty()) {
                InputValidator.ValidationReport report = InputValidator.validateQuestionContent(newVal);
                if (report.hasIssues() && !report.canSubmit()) {
                    // Only show blocking errors in real-time
                    contentValidation.setText(report.getFullReport());
                    contentValidation.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                } else if (newVal.length() >= Question.CONTENT_MIN_LENGTH) {
                    contentValidation.setText("✓ Content length is good!");
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
            applyTitleCorrectionsBtn.setVisible(false);
            applyContentCorrectionsBtn.setVisible(false);
        });
        
        HBox buttonBox = new HBox(10, clearButton, submitButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        submitButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String contentText = contentArea.getText().trim();
            
            // Don't auto-correct - validate as-is
            
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
            
            // Check for spelling warnings (not blocking)
            SpellChecker.ValidationResult titleSpellCheck = SpellChecker.validateText(title);
            SpellChecker.ValidationResult contentSpellCheck = SpellChecker.validateText(contentText);
            
            boolean hasSpellingIssues = titleSpellCheck.hasIssues() || contentSpellCheck.hasIssues();
            
            String finalTitle = title;
            String finalContent = contentText;
            
            // If there are spelling issues, offer to apply corrections
            if (hasSpellingIssues) {
                Alert warningAlert = new Alert(AlertType.CONFIRMATION);
                warningAlert.setTitle("Spelling Suggestions");
                warningAlert.setHeaderText("We found some spelling suggestions:");
                
                StringBuilder warningText = new StringBuilder();
                if (titleSpellCheck.hasIssues()) {
                    warningText.append("TITLE:\n").append(titleSpellCheck.getSummary()).append("\n");
                }
                if (contentSpellCheck.hasIssues()) {
                    warningText.append("\nCONTENT:\n").append(contentSpellCheck.getSummary());
                }
                
                warningAlert.setContentText(warningText.toString() + 
                    "\n\nWould you like to apply corrections?\n" +
                    "• Apply Corrections = Use spell-checked version\n" +
                    "• Submit As-Is = Post without changes\n" +
                    "• Cancel = Go back to edit");
                
                ButtonType submitAsIsButton = new ButtonType("Submit As-Is", ButtonBar.ButtonData.OK_DONE);
                ButtonType applyCorrectionsButton = new ButtonType("Apply Corrections");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                
                warningAlert.getButtonTypes().setAll(applyCorrectionsButton, submitAsIsButton, cancelButton);
                
                Optional<ButtonType> result = warningAlert.showAndWait();
                
                if (result.isPresent()) {
                    if (result.get() == applyCorrectionsButton) {
                        // Apply corrections
                        finalTitle = titleSpellCheck.getCorrectedText().isEmpty() ? title : titleSpellCheck.getCorrectedText();
                        finalContent = contentSpellCheck.getCorrectedText().isEmpty() ? contentText : contentSpellCheck.getCorrectedText();
                    } else if (result.get() == cancelButton) {
                        // User wants to go back and edit
                        return;
                    }
                    // If submitAsIsButton, use original text (already set)
                } else {
                    // Dialog closed, don't submit
                    return;
                }
            }
            
            // Create and submit question
            try {
                Question question = new Question(finalTitle, finalContent, currentUser.getUserName());
                databaseHelper.createQuestion(question);
                
                Alert successAlert = new Alert(AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Question Posted!");
                successAlert.setContentText("Your question has been posted successfully.");
                successAlert.showAndWait();
                
                titleField.clear();
                contentArea.clear();
                errorLabel.setText("");
                titleValidation.setText("");
                contentValidation.setText("");
                applyTitleCorrectionsBtn.setVisible(false);
                applyContentCorrectionsBtn.setVisible(false);
                
                refreshAllTabs();
                tabPane.getSelectionModel().select(1); // Switch to My Questions
                
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
            titleLabel, titleField, titleButtonBox, titleValidation,
            contentLabel, contentArea, contentButtonBox, contentValidation,
            errorLabel,
            buttonBox
        );

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
    // ==========END CREATE QUESTION TAB ============
    
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
    //========== END MY QUESTIONS TAB ==========
	
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
        //displayQuestions.setPadding(new Insets(6));

        Button newQuestionBtn = new Button("New Question");
        newQuestionBtn.setOnAction(e -> openCreateQuestionDialog());

        // Resolution filter (matches Threads-style simple controls)
        resolutionFilter = new ComboBox<>();
        resolutionFilter.getItems().addAll("All Questions", "Unresolved Only", "Resolved Only", "Answers with Reviewer", "Answers without Reviewer");
        resolutionFilter.setValue("All Questions"); // default
        resolutionFilter.setTooltip(new Tooltip("Filter by resolution status"));

        Label resolutionLbl = new Label("Filter:");
        resolutionLbl.setStyle("-fx-text-fill: black; -fx-font-size:12px;");

        HBox filterBox = new HBox(4, resolutionLbl, resolutionFilter);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        // Inline error banner (kept subtle; only shown on failures)
        allQuestionsError = new Label();
        allQuestionsError.setStyle("-fx-text-fill: black; -fx-font-size:11px;");
        allQuestionsError.setVisible(false);

        // Rebuild the left side to include controls + error + list
        VBox leftSide = new VBox(4, newQuestionBtn, filterBox, allQuestionsError, displayQuestions);
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
        
        //handler for the filter
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
        
        Label meta22 = new Label("Reviewed");
        meta22.setStyle(
                "-fx-text-fill: white; " +
                "-fx-font-size: 11px; " +
                "-fx-background-color: #ff7700; " + 
                "-fx-padding: 2px 5px 2px 5px; "
            );
        	meta22.setVisible(false);
        	
        if(q.isResolved()) {
        	meta22.setText("Resolved");
        	meta22.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2px 5px 2px 5px; ; -fx-font-size: 11px;" );
        	meta22.setVisible(true);
        }
        
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
        VBox col = new VBox(2, title, meta1, meta2, meta22);
        col.setPadding(new Insets(8));

        // Make whole row clickable
        HBox row = new HBox(col);
        row.setStyle("-fx-background-color:#ffffff; -fx-border-color:#e6e6e6; -fx-border-width:1;");
        if(q.isResolved()) {
        row.setStyle("-fx-background-color:#e8f5e9; -fx-border-color:#4CAF50; -fx-border-width:2;");
        }
        row.setOnMouseClicked(e -> loadThread(q));
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
            Button closeBtn = new Button("Close Question");
            closeBtn.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white;");
            closeBtn.setVisible(!q.isResolved() && q.getAnswers().size() > 0); // Only show if unresolved and has answers
            closeBtn.setOnAction(e -> {
            	closeQuestion(q);
                refreshQuestionListAndMaybeSelect();
            });
            headerActions.getChildren().addAll(deleteBtn, closeBtn);
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
        // react to filter changes
        viewFilter.setOnAction(ev -> loadThread(q));
    }

    /**
     * Renders public answers for a question with feedback functionality
     */
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
                    Label status = new Label("STATUS");
                    String role = databaseHelper.getUserRole(a.getAnsweredBy());
                    status.setText(role);
                    if("Admin".equals(role)) {
                        status.setStyle("-fx-text-fill: white; -fx-font-size: 11px; " +
                                       "-fx-background-color: #C00000; -fx-padding: 2px 5px 2px 5px;");
                    }
                    if("Reviewer".equals(role)) {
                        status.setStyle("-fx-text-fill: white; -fx-font-size: 11px; " +
                                       "-fx-background-color: #ff7700; -fx-padding: 2px 5px 2px 5px;");
                    }
                    if("Staff".equals(role)) {
                        status.setStyle("-fx-text-fill: white; -fx-font-size: 11px; " +
                                       "-fx-background-color: #900FE0; -fx-padding: 2px 5px 2px 5px;");
                    }
                    if("User".equals(role)) {
                        status.setStyle("-fx-text-fill: white; -fx-font-size: 11px; " +
                                       "-fx-background-color: #0099ff; -fx-padding: 2px 5px 2px 5px;");
                    }
                    
                    Label aMeta = new Label(a.getAnsweredBy() + " • " + a.getCreatedAt().format(TS) + 
                                           " • " + a.getUpvotes() + " upvotes");
                    aMeta.setStyle("-fx-text-fill: black; -fx-font-size: 11px; -fx-padding: 2px 5px 2px 5px;");
                    
                    Label aText = new Label(a.getContent());
                    aText.setWrapText(true);
                    aText.setStyle("-fx-text-fill: black;");
                    
                    // Action buttons
                    HBox actionBox = new HBox(5);
                    
                    // Check if this is the current user's answer
                    boolean isOwnAnswer = a.getAnsweredBy().equals(currentUser.getUserName());
                    
                    // Upvote button (for everyone except the answer author)
                    if (!isOwnAnswer) {
                        Button upvoteBtn = new Button("👍 Upvote");
                        upvoteBtn.setStyle("-fx-font-size: 10px;");
                        upvoteBtn.setOnAction(e -> {
                            try {
                                databaseHelper.upvoteAnswer(a.getId(), a.getAnsweredBy());
                                showAlert("Success", "Upvoted!", AlertType.INFORMATION);
                                loadThread(q);
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
                    if (isOwnAnswer) {
                        Button editBtn = new Button("Edit");
                        editBtn.setStyle("-fx-font-size: 10px;");
                        editBtn.setOnAction(e -> {
                            editAnswer(a);
                            loadThread(q);
                        });
                        
                        Button deleteBtn = new Button("Delete");
                        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");
                        deleteBtn.setOnAction(e -> {
                            try {
                                databaseHelper.deleteAnswer(a.getId(), currentUser.getUserName());
                                showAlert("Success", "Answer deleted!", AlertType.INFORMATION);
                                loadThread(q);
                            } catch (SQLException ex) {
                                showAlert("Error", "Failed to delete answer: " + ex.getMessage(), AlertType.ERROR);
                            }
                        });
                        
                        actionBox.getChildren().addAll(editBtn, deleteBtn);
                    }
                    
                    HBox box = new HBox(6, status, aMeta);
                    card.getChildren().addAll(box, aText, actionBox);
                    
                    // Show existing feedback
                    try {
                        List<AnswerFeedback> feedbackList = databaseHelper.getFeedbackForAnswer(a.getId());
                        if (!feedbackList.isEmpty()) {
                            VBox feedbackBox = new VBox(3);
                            feedbackBox.setStyle("-fx-background-color: #F0F0F0; -fx-padding: 5; -fx-border-color: #CCC; -fx-border-width: 1;");
                            Label feedbackTitle = new Label("💬 Feedback (" + feedbackList.size() + "):");
                            feedbackTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
                            feedbackBox.getChildren().add(feedbackTitle);
                            
                            for (AnswerFeedback fb : feedbackList) {
                                Label fbLabel = new Label(fb.getGivenBy() + ": " + fb.getFeedbackText());
                                fbLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #333;");
                                fbLabel.setWrapText(true);
                                feedbackBox.getChildren().add(fbLabel);
                            }
                            card.getChildren().add(feedbackBox);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    
                    // Highlight if this is the accepted answer
                    if (q.isResolved() && a.getId() == q.getResolvedAnswerId()) {
                        card.setStyle("-fx-background-color:#e8f5e9; -fx-border-color:#4CAF50; " +
                                     "-fx-border-width:2; -fx-padding:8;");
                        Label acceptedLabel = new Label("✓ Accepted Answer");
                        acceptedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-font-size:10px;");
                        card.getChildren().add(0, acceptedLabel);
                    } else {
                        card.setStyle("-fx-background-color:#f9f9f9; -fx-border-color:#eee; -fx-padding:8;");
                        if("Reviewer".equals(role)) {
                            card.setStyle("-fx-background-color:#ffffaa; -fx-border-color:#eee; -fx-padding:8; " +
                                         "-fx-border-color: #ff7700; -fx-border-width: 1px; -fx-border-style: solid;");
                        }
                    }
                    
                    answersBox.getChildren().add(card);
                }
            }
            displayQuestionThread.getChildren().add(answersBox);
        } catch (SQLException ex) {
            showError("Answers load failed", ex.getMessage());
        }
        if(!q.isResolved()) {
            displayQuestionThread.getChildren().add(answerComposer(q));
        }
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

	String to="";
	/**
	 * Creates a composer for private messages
	 */
	private VBox privateComposer(Question q) {
	    VBox box = new VBox(6);
	    
	    Label lbl = new Label("Send private feedback");
	    lbl.setStyle("-fx-font-weight:bold; -fx-text-fill: white;");
	    
	    // Dropdown to select who to send feedback to
	    ComboBox<String> recipientSelector = new ComboBox<>();
	    recipientSelector.setPromptText("Select recipient...");
	    Label recipientLabel = new Label("To:");
	    recipientLabel.setStyle("-fx-text-fill: white;");
	    
	    // Question/Answer type selector
	    ComboBox<String> kind = new ComboBox<>();
	    kind.getItems().addAll("Question", "Answer");
	    kind.setValue("Question");
	    Label typeLabel = new Label("Type:");
	    typeLabel.setStyle("-fx-text-fill: white;");
	    
	    TextArea text = new TextArea();
	    text.setPromptText("Write your private message...");
	    text.setWrapText(true);
	    text.setPrefRowCount(3);
	    
	    Label charCounter = new Label("0/500");
	    charCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
	    
	    Label spellValidation = new Label();
	    spellValidation.setWrapText(true);
	    spellValidation.setMaxWidth(600);
	    spellValidation.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
	    
	    // Character counter
	    text.textProperty().addListener((obs, old, newVal) -> {
	        charCounter.setText(newVal.length() + "/500");
	        charCounter.setStyle(newVal.length() > 500 ? 
	            "-fx-font-size: 10px; -fx-text-fill: red;" : 
	            "-fx-font-size: 10px; -fx-text-fill: white;");
	        
	        if (newVal.length() > 500) {
	            text.setText(newVal.substring(0, 500));
	        }
	    });
	    
	    Button spellCheckBtn = new Button("✓ Check Spelling");
	    spellCheckBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
	    spellCheckBtn.setOnAction(e -> {
	        String msgText = text.getText();
	        if (msgText != null && !msgText.trim().isEmpty()) {
	            SpellChecker.ValidationResult spellResult = SpellChecker.validateText(msgText);
	            if (spellResult.hasIssues()) {
	                // Show suggestion, don't auto-apply
	                spellValidation.setText("Suggestions: " + spellResult.getSummary());
	                spellValidation.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 10px;");
	            } else {
	                spellValidation.setText("✓ No spelling errors found!");
	                spellValidation.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 10px;");
	            }
	        }
	    });
	    
	    Button applyCorrectionsBtn = new Button("Apply Corrections");
	    applyCorrectionsBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
	    applyCorrectionsBtn.setVisible(false);
	    applyCorrectionsBtn.setOnAction(e -> {
	        String msgText = text.getText();
	        if (msgText != null && !msgText.trim().isEmpty()) {
	            SpellChecker.ValidationResult spellResult = SpellChecker.validateText(msgText);
	            text.setText(spellResult.getCorrectedText());
	            spellValidation.setText("✓ Corrections applied!");
	            spellValidation.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 10px;");
	            applyCorrectionsBtn.setVisible(false);
	        }
	    });
	    
	    // Update spell check button to show apply button
	    spellCheckBtn.setOnAction(e -> {
	        String msgText = text.getText();
	        if (msgText != null && !msgText.trim().isEmpty()) {
	            SpellChecker.ValidationResult spellResult = SpellChecker.validateText(msgText);
	            if (spellResult.hasIssues()) {
	                spellValidation.setText("Suggestions found. Click 'Apply Corrections' to fix them.\n" + spellResult.getSummary());
	                spellValidation.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 10px;");
	                applyCorrectionsBtn.setVisible(true);
	            } else {
	                spellValidation.setText("✓ No spelling errors found!");
	                spellValidation.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 10px;");
	                applyCorrectionsBtn.setVisible(false);
	            }
	        }
	    });
	    
	    HBox toolBar = new HBox(10, charCounter, spellCheckBtn, applyCorrectionsBtn);
	    toolBar.setAlignment(Pos.CENTER_LEFT);
	    
	    Button send = new Button("Send Private");
	    
	    // Populate dropdown with available recipients for THIS specific question
	    try {
	        List<PrivateMessage> existingMessages = databaseHelper.getPrivateMessagesForQuestion(q.getId());
	        Map<String, String> userMessageTypes = new HashMap<>();
	        
	        // Add the question asker as an option (if not current user)
	        if (!currentUser.getUserName().equals(q.getAskedBy())) {
	            recipientSelector.getItems().add(q.getAskedBy() + " (Question Asker)");
	        }
	        
	        // Add all users who have posted answers to this question
	        List<Answer> answers = databaseHelper.getAnswersForQuestion(q.getId());
	        for (Answer a : answers) {
	            if (!currentUser.getUserName().equals(a.getAnsweredBy()) && 
	                !recipientSelector.getItems().contains(a.getAnsweredBy())) {
	                recipientSelector.getItems().add(a.getAnsweredBy());
	            }
	        }
	        
	        // Track existing conversation types
	        for (PrivateMessage pm : existingMessages) {
	            String otherUser = pm.getSender().equals(currentUser.getUserName()) ? pm.getTo() : pm.getSender();
	            if (!currentUser.getUserName().equals(otherUser)) {
	                userMessageTypes.put(otherUser, pm.getMessageType());
	                if (!recipientSelector.getItems().contains(otherUser)) {
	                    recipientSelector.getItems().add(otherUser);
	                }
	            }
	        }
	        
	        // If no recipients available
	        if (recipientSelector.getItems().isEmpty()) {
	            Label noRecipients = new Label("No users available to send feedback to yet. Wait for someone to answer or provide feedback.");
	            noRecipients.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
	            noRecipients.setWrapText(true);
	            box.getChildren().add(noRecipients);
	            box.setPadding(new Insets(10,0,0,0));
	            return box;
	        }
	        
	        // When user selects a recipient, determine if it's a new or existing conversation
	        recipientSelector.setOnAction(ev -> {
	            String selected = recipientSelector.getValue();
	            if (selected != null) {
	                String actualUsername = selected.replace(" (Question Asker)", "");
	                
	                // Check if there's an existing conversation
	                String existingType = userMessageTypes.get(actualUsername);
	                if (existingType != null) {
	                    // Existing conversation - set type and hide selector
	                    kind.setValue(existingType);
	                    kind.setVisible(false);
	                    typeLabel.setVisible(false);
	                } else {
	                    // New conversation - show type selector
	                    kind.setVisible(true);
	                    typeLabel.setVisible(true);
	                }
	            }
	        });
	        
	        // Initially hide type selector
	        kind.setVisible(false);
	        typeLabel.setVisible(false);
	        
	    } catch (SQLException ex) {
	        showError("load failed", ex.getMessage());
	    }
	    
	    send.setOnAction(e -> {
	        String content = trimOrEmpty(text.getText());
	        if (content.length() < 5) {
	            showError("Too short", "Please write a longer message.");
	            return;
	        }
	        String recipient = recipientSelector.getValue();
	        if (recipient == null || recipient.isEmpty()) {
	            showError("No recipient", "Please select a user to send feedback to.");
	            return;
	        }
	        
	        // Extract actual username (remove label if present)
	        String actualRecipient = recipient.replace(" (Question Asker)", "");
	        
	        // Don't auto-correct, send as-is
	        try {
	            databaseHelper.addPrivateMessage(q.getId(), actualRecipient, currentUser.getUserName(), kind.getValue().toUpperCase(), content);
	            text.clear();
	            spellValidation.setText("");
	            applyCorrectionsBtn.setVisible(false);
	            loadThread(q); // Refresh to show new message
	        } catch (SQLException ex) {
	            showError("Send failed", ex.getMessage());
	        }
	    });
	    
	    HBox recipientRow = new HBox(6, recipientLabel, recipientSelector);
	    recipientRow.setAlignment(Pos.CENTER_LEFT);
	    
	    HBox typeRow = new HBox(6, typeLabel, kind);
	    typeRow.setAlignment(Pos.CENTER_LEFT);
	    
	    box.getChildren().addAll(lbl, recipientRow, typeRow, text, toolBar, spellValidation, send);
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
        boolean isTo = currentUser.getUserName().equals(pm.getTo());
        boolean isStaff = isStaff(currentUser.getRole());
        return isAsker || isSender || isStaff || isTo;
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
        
        Label titleCounter = new Label(question.getTitle().length() + "/" + Question.TITLE_MAX_LENGTH);
        titleCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        Label titleSpellValidation = new Label();
        titleSpellValidation.setWrapText(true);
        titleSpellValidation.setMaxWidth(500);
        
        Button titleSpellCheckBtn = new Button("✓ Check Spelling");
        titleSpellCheckBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        
        Button applyTitleCorrectionsBtn = new Button("Apply Corrections");
        applyTitleCorrectionsBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        applyTitleCorrectionsBtn.setVisible(false);
        
        // Store spell check results
        final SpellChecker.ValidationResult[] titleSpellResult = {null};
        
        titleSpellCheckBtn.setOnAction(e -> {
            String titleText = titleField.getText();
            if (titleText != null && !titleText.trim().isEmpty()) {
                SpellChecker.ValidationResult spellResult = SpellChecker.validateText(titleText);
                titleSpellResult[0] = spellResult; // Store result
                
                if (spellResult.hasIssues()) {
                    titleSpellValidation.setText("Suggestions found. Click 'Apply Corrections' to fix.\n" + spellResult.getSummary());
                    titleSpellValidation.setStyle("-fx-text-fill: orange; -fx-font-size: 10px;");
                    applyTitleCorrectionsBtn.setVisible(true);
                } else {
                    titleSpellValidation.setText("✓ No spelling errors found!");
                    titleSpellValidation.setStyle("-fx-text-fill: green; -fx-font-size: 10px;");
                    applyTitleCorrectionsBtn.setVisible(false);
                }
            }
        });
        
        applyTitleCorrectionsBtn.setOnAction(e -> {
            if (titleSpellResult[0] != null) {
                titleField.setText(titleSpellResult[0].getCorrectedText());
                titleSpellValidation.setText("✓ Corrections applied!");
                titleSpellValidation.setStyle("-fx-text-fill: green; -fx-font-size: 10px;");
                applyTitleCorrectionsBtn.setVisible(false);
                titleSpellResult[0] = null;
            }
        });
        
        HBox titleToolBar = new HBox(10, titleCounter, titleSpellCheckBtn, applyTitleCorrectionsBtn);
        titleToolBar.setAlignment(Pos.CENTER_LEFT);
        
        titleField.textProperty().addListener((obs, old, newVal) -> {
            titleCounter.setText(newVal.length() + "/" + Question.TITLE_MAX_LENGTH);
            titleCounter.setStyle(newVal.length() > Question.TITLE_MAX_LENGTH ? 
                "-fx-font-size: 10px; -fx-text-fill: red;" : 
                "-fx-font-size: 10px; -fx-text-fill: #666;");
            
            if (newVal.length() > Question.TITLE_MAX_LENGTH) {
                titleField.setText(newVal.substring(0, Question.TITLE_MAX_LENGTH));
            }
        });
        
        Label contentLabel = new Label("Content:");
        TextArea contentArea = new TextArea(question.getContent());
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(8);
        contentArea.setMaxWidth(500);
        
        Label contentCounter = new Label(question.getContent().length() + "/" + Question.CONTENT_MAX_LENGTH);
        contentCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        Label contentSpellValidation = new Label();
        contentSpellValidation.setWrapText(true);
        contentSpellValidation.setMaxWidth(500);
        
        Button contentSpellCheckBtn = new Button("✓ Check Spelling");
        contentSpellCheckBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        
        Button applyContentCorrectionsBtn = new Button("Apply Corrections");
        applyContentCorrectionsBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        applyContentCorrectionsBtn.setVisible(false);
        
        // Store spell check results
        final SpellChecker.ValidationResult[] contentSpellResult = {null};
        
        contentSpellCheckBtn.setOnAction(e -> {
            String contentText = contentArea.getText();
            if (contentText != null && !contentText.trim().isEmpty()) {
                SpellChecker.ValidationResult spellResult = SpellChecker.validateText(contentText);
                contentSpellResult[0] = spellResult; // Store result
                
                if (spellResult.hasIssues()) {
                    contentSpellValidation.setText("Suggestions found. Click 'Apply Corrections' to fix.\n" + spellResult.getSummary());
                    contentSpellValidation.setStyle("-fx-text-fill: orange; -fx-font-size: 10px;");
                    applyContentCorrectionsBtn.setVisible(true);
                } else {
                    contentSpellValidation.setText("✓ No spelling errors found!");
                    contentSpellValidation.setStyle("-fx-text-fill: green; -fx-font-size: 10px;");
                    applyContentCorrectionsBtn.setVisible(false);
                }
            }
        });
        
        applyContentCorrectionsBtn.setOnAction(e -> {
            if (contentSpellResult[0] != null) {
                contentArea.setText(contentSpellResult[0].getCorrectedText());
                contentSpellValidation.setText("✓ Corrections applied!");
                contentSpellValidation.setStyle("-fx-text-fill: green; -fx-font-size: 10px;");
                applyContentCorrectionsBtn.setVisible(false);
                contentSpellResult[0] = null;
            }
        });
        
        HBox contentToolBar = new HBox(10, contentCounter, contentSpellCheckBtn, applyContentCorrectionsBtn);
        contentToolBar.setAlignment(Pos.CENTER_LEFT);
        
        contentArea.textProperty().addListener((obs, old, newVal) -> {
            contentCounter.setText(newVal.length() + "/" + Question.CONTENT_MAX_LENGTH);
            contentCounter.setStyle(newVal.length() > Question.CONTENT_MAX_LENGTH ? 
                "-fx-font-size: 10px; -fx-text-fill: red;" : 
                "-fx-font-size: 10px; -fx-text-fill: #666;");
            
            if (newVal.length() > Question.CONTENT_MAX_LENGTH) {
                contentArea.setText(newVal.substring(0, Question.CONTENT_MAX_LENGTH));
            }
        });
        
        content.getChildren().addAll(
            titleLabel, titleField, titleToolBar, titleSpellValidation,
            contentLabel, contentArea, contentToolBar, contentSpellValidation
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    // Don't auto-correct - use text as-is
                    question.setTitle(titleField.getText().trim());
                    question.setContent(contentArea.getText().trim());
                    return question;
                } catch (IllegalArgumentException ex) {
                    showAlert("Error", ex.getMessage(), AlertType.ERROR);
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
        
        Label charCounter = new Label(answer.getContent().length() + "/" + Answer.CONTENT_MAX_LENGTH);
        charCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        Label spellValidation = new Label();
        spellValidation.setWrapText(true);
        spellValidation.setMaxWidth(500);
        spellValidation.setStyle("-fx-font-size: 10px;");
        
        // Character counter
        answerArea.textProperty().addListener((obs, old, newVal) -> {
            charCounter.setText(newVal.length() + "/" + Answer.CONTENT_MAX_LENGTH);
            charCounter.setStyle(newVal.length() > Answer.CONTENT_MAX_LENGTH ? 
                "-fx-font-size: 10px; -fx-text-fill: red;" : 
                "-fx-font-size: 10px; -fx-text-fill: #666;");
            
            if (newVal.length() > Answer.CONTENT_MAX_LENGTH) {
                answerArea.setText(newVal.substring(0, Answer.CONTENT_MAX_LENGTH));
            }
        });
        
        Button spellCheckBtn = new Button("✓ Check Spelling");
        spellCheckBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        
        Button applyCorrectionsBtn = new Button("Apply Corrections");
        applyCorrectionsBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        applyCorrectionsBtn.setVisible(false);
        
        // Store the spell check result for the apply button
        final SpellChecker.ValidationResult[] currentSpellResult = {null};
        
        spellCheckBtn.setOnAction(e -> {
            String answerText = answerArea.getText();
            if (answerText != null && !answerText.trim().isEmpty()) {
                SpellChecker.ValidationResult spellResult = SpellChecker.validateText(answerText);
                currentSpellResult[0] = spellResult; // Store result
                
                if (spellResult.hasIssues()) {
                    spellValidation.setText("Suggestions found. Click 'Apply Corrections' to fix.\n" + spellResult.getSummary());
                    spellValidation.setStyle("-fx-text-fill: orange; -fx-font-size: 10px;");
                    applyCorrectionsBtn.setVisible(true);
                } else {
                    spellValidation.setText("✓ No spelling errors found!");
                    spellValidation.setStyle("-fx-text-fill: green; -fx-font-size: 10px;");
                    applyCorrectionsBtn.setVisible(false);
                }
            }
        });
        
        applyCorrectionsBtn.setOnAction(e -> {
            if (currentSpellResult[0] != null) {
                answerArea.setText(currentSpellResult[0].getCorrectedText());
                spellValidation.setText("✓ Corrections applied!");
                spellValidation.setStyle("-fx-text-fill: green; -fx-font-size: 10px;");
                applyCorrectionsBtn.setVisible(false);
                currentSpellResult[0] = null; // Clear the result
            }
        });
        
        HBox toolBar = new HBox(10, charCounter, spellCheckBtn, applyCorrectionsBtn);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        
        content.getChildren().addAll(answerArea, toolBar, spellValidation);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return answerArea.getText().trim(); // Return as-is, no auto-correct
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            try {
                // Don't auto-correct - save as-is
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
        Tab newReviewersTab = createReviewersTab();
        
        // Replace tabs
        tabPane.getTabs().clear();
        tabPane.getTabs().addAll(newAskTab, newMyQuestionsTab, newAllQuestionsTab, newReviewersTab);
        
        // Restore selection
        if (selectedIndex >= 0 && selectedIndex < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(selectedIndex);
        }
        
        // Update references
        askQuestionTab = newAskTab;
        myQuestionsTab = newMyQuestionsTab;
        allQuestionsTab = newAllQuestionsTab;
        reviewersTab = newReviewersTab;
    }

	// Method to show alert
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Creates a composer for public answers
     */
    private VBox answerComposer(Question q) {
        Label lbl = new Label("Add a public answer");
        lbl.setStyle("-fx-font-weight:bold; -fx-text-fill: black;");
        
        TextArea text = new TextArea();
        text.setPromptText("Write your answer...");
        text.setWrapText(true);
        text.setPrefRowCount(3);
        
        Label charCounter = new Label("0/" + Answer.CONTENT_MAX_LENGTH);
        charCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        Label spellValidation = new Label();
        spellValidation.setWrapText(true);
        spellValidation.setMaxWidth(600);
        spellValidation.setStyle("-fx-font-size: 10px;");
        
        text.textProperty().addListener((obs, old, newVal) -> {
            charCounter.setText(newVal.length() + "/" + Answer.CONTENT_MAX_LENGTH);
            charCounter.setStyle(newVal.length() > Answer.CONTENT_MAX_LENGTH ? 
                "-fx-font-size: 10px; -fx-text-fill: red;" : 
                "-fx-font-size: 10px; -fx-text-fill: #666;");
            
            if (newVal.length() > Answer.CONTENT_MAX_LENGTH) {
                text.setText(newVal.substring(0, Answer.CONTENT_MAX_LENGTH));
            }
        });
        
        Button spellCheckBtn = new Button("✓ Check Spelling");
        spellCheckBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        
        Button applyCorrectionsBtn = new Button("Apply Corrections");
        applyCorrectionsBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        applyCorrectionsBtn.setVisible(false);
        
        spellCheckBtn.setOnAction(e -> {
            String answerText = text.getText();
            if (answerText != null && !answerText.trim().isEmpty()) {
                SpellChecker.ValidationResult spellResult = SpellChecker.validateText(answerText);
                if (spellResult.hasIssues()) {
                    spellValidation.setText("Suggestions found. Click 'Apply Corrections' to fix.\n" + spellResult.getSummary());
                    spellValidation.setStyle("-fx-text-fill: orange; -fx-font-size: 10px;");
                    applyCorrectionsBtn.setVisible(true);
                } else {
                    spellValidation.setText("✓ No spelling errors found!");
                    spellValidation.setStyle("-fx-text-fill: green; -fx-font-size: 10px;");
                    applyCorrectionsBtn.setVisible(false);
                }
            }
        });
        
        applyCorrectionsBtn.setOnAction(e -> {
            String answerText = text.getText();
            if (answerText != null && !answerText.trim().isEmpty()) {
                SpellChecker.ValidationResult spellResult = SpellChecker.validateText(answerText);
                text.setText(spellResult.getCorrectedText());
                spellValidation.setText("✓ Corrections applied!");
                spellValidation.setStyle("-fx-text-fill: green; -fx-font-size: 10px;");
                applyCorrectionsBtn.setVisible(false);
            }
        });
        
        HBox toolBar = new HBox(10, charCounter, spellCheckBtn, applyCorrectionsBtn);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        
        Button post = new Button("Post Public Answer");
        post.setOnAction(e -> {
            String content = trimOrEmpty(text.getText());
            if (content.length() < 5) {
                showError("Too short", "Please write a longer answer.");
                return;
            }
            
            // Don't auto-correct - post as-is
            try {
                Answer a = new Answer(q.getId(), content, currentUser.getUserName());
                databaseHelper.createAnswer(a);
                loadThread(q);
            } catch (SQLException ex) {
                showError("Post failed", ex.getMessage());
            }
        });
        
        VBox box = new VBox(6, lbl, text, toolBar, spellValidation, post);
        box.setPadding(new Insets(10,0,0,0));
        return box;
    }

    /**
     * Renders the private message section
     */
    private void renderPrivateSection(Question q) {
        VBox box = new VBox(8);
        int visibleCount = 0;
        try {
            List<PrivateMessage> allPM = databaseHelper.getPrivateMessagesForQuestion(q.getId());
            // Count visible messages first
            for (PrivateMessage pm : allPM) {
                if (canSeePrivate(q, pm)) {
                    visibleCount++;
                }
            }
            // Create the title with the count
            Label title = new Label("Private feedback (" + visibleCount + ")");
            title.setStyle("-fx-font-weight:bold; -fx-text-fill: white;");
            box.getChildren().add(title);
            
            // Render the messages
            boolean any = false;
            for (PrivateMessage pm : allPM) {
                if (!canSeePrivate(q, pm)) continue;
                any = true;
                VBox card = new VBox(3);
                Label meta = new Label(pm.getSender() + " • " + pm.getCreatedAt().format(TS) + " • " + pm.getMessageType() + " --> " + pm.getTo());
                meta.setStyle("-fx-text-fill: white; -fx-font-size:11px;");
                Label text = new Label(pm.getContent());
                text.setWrapText(true);
                text.setStyle("-fx-text-fill: black;");
                card.getChildren().addAll(meta, text);
                card.setStyle("-fx-background-color:#999; -fx-border-color:#ccc; -fx-padding:8;");
                box.getChildren().add(card);
            }
            
            if (!any) {
                Label none = new Label("No private feedback yet.");
                none.setStyle("-fx-text-fill: white;");
                box.getChildren().add(none);
            }
            box.setStyle("-fx-background-color:#4c4c4c; -fx-padding:8px;");
        } catch (SQLException ex) {
            showError("Private messages load failed", ex.getMessage());
        }
        
        // Always show composer for unresolved questions (resets per question)
        if(!q.isResolved()) {
            if (!"Public".equals(viewFilter.getValue())) {
                box.getChildren().add(privateComposer(q));
            }
        }
        displayQuestionThread.getChildren().add(box);
    }
}