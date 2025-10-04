package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import java.sql.SQLException;
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
        
        mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        Label titleLabel = new Label("Student Q&A System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label welcomeLabel = new Label("Welcome, " + currentUser.getUserName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        // Create tab pane
        tabPane = new TabPane();
        tabPane.setMinHeight(400); // ADD THIS LINE - ensures content is visible
        
        // Create tabs
        askQuestionTab = createAskQuestionTab();
        myQuestionsTab = createMyQuestionsTab();
        allQuestionsTab = createAllQuestionsTab();
        
        tabPane.getTabs().addAll(askQuestionTab, myQuestionsTab, allQuestionsTab);
        
        // Bottom buttons
        Button backButton = new Button("Back to Home");
        backButton.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-padding: 8 16;");
        backButton.setOnAction(e -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, currentUser);
        });
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #0099ff; -fx-text-fill: white; -fx-padding: 8 16;");
        refreshButton.setOnAction(e -> refreshAllTabs());
        
        HBox buttonBox = new HBox(10, backButton, refreshButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        mainLayout.getChildren().addAll(titleLabel, welcomeLabel, tabPane, buttonBox);
        
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true); // ADD THIS LINE
        
        Scene scene = new Scene(scrollPane, 900, 600);
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("sQaaS™ - Q&A System");
        primaryStage.show();
    }

    

    // ========== ASK QUESTION TAB ==========

    

    private Tab createAskQuestionTab() {
        Tab tab = new Tab("Ask Question");
        tab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        Label infoLabel = new Label("Ask a new question to get help from other students");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        // Search for similar questions
        Label searchLabel = new Label("Search existing questions first:");
        searchLabel.setStyle("-fx-font-weight: bold;");
        
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
        titleLabel.setStyle("-fx-font-weight: bold;");
        
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
        contentLabel.setStyle("-fx-font-weight: bold;");
        
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
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        
        return tab;
    }
    

    private void displaySearchResults(VBox container, List<Question> results) {

        container.getChildren().clear();

        

        if (results.isEmpty()) {

            Label noResults = new Label("No similar questions found. You can proceed to ask your question.");

            noResults.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

            container.getChildren().add(noResults);

        } else {

            Label header = new Label("Similar questions found (" + results.size() + "):");

            header.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff6600;");

            container.getChildren().add(header);

            

            for (Question q : results) {

                VBox questionBox = new VBox(5);

                questionBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 8; -fx-border-color: #ddd; -fx-border-width: 1;");

                

                Label titleLbl = new Label(q.getTitle());

                titleLbl.setStyle("-fx-font-weight: bold;");

                

                Label metaLbl = new Label(q.getAnswers().size() + " answers • " + 

                                         (q.isResolved() ? "✓ Resolved" : "Unresolved") + 

                                         " • by " + q.getAskedBy());

                metaLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

                

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
        Tab tab = new Tab("My Questions");
        tab.setClosable(false);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        Label infoLabel = new Label("Your questions and their answers");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        VBox questionsContainer = new VBox(10);
        
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
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        
        return tab;
    }

    

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
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
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
        int unreadCount = question.getUnreadAnswerCount();
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
        
        // Filter controls
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Show:");
        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Questions", "Unresolved Only", "Resolved Only");
        filterCombo.setValue("Unresolved Only");
        
        filterBox.getChildren().addAll(filterLabel, filterCombo);
        
        VBox questionsContainer = new VBox(10);
        
        // Load questions based on filter
        filterCombo.setOnAction(e -> {
            try {
                String filter = filterCombo.getValue();
                List<Question> questions;
                
                if ("Unresolved Only".equals(filter)) {
                    questions = databaseHelper.getUnresolvedQuestions();
                } else if ("Resolved Only".equals(filter)) {
                    questions = databaseHelper.getAllQuestions(null);
                    questions.removeIf(q -> !q.isResolved());
                } else {
                    questions = databaseHelper.getAllQuestions(null);
                }
                
                displayAllQuestions(questionsContainer, questions);
            } catch (SQLException ex) {
                questionsContainer.getChildren().clear();
                Label errorLabel = new Label("Error loading questions: " + ex.getMessage());
                errorLabel.setStyle("-fx-text-fill: red;");
                questionsContainer.getChildren().add(errorLabel);
                ex.printStackTrace();
            }
        });
        
        // Initial load
        try {
            List<Question> questions = databaseHelper.getUnresolvedQuestions();
            displayAllQuestions(questionsContainer, questions);
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading questions: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            questionsContainer.getChildren().add(errorLabel);
            e.printStackTrace();
        }
        
        content.getChildren().addAll(filterBox, new Separator(), questionsContainer);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        
        return tab;
    }

    

    private void displayAllQuestions(VBox container, List<Question> questions) {

        container.getChildren().clear();

        

        if (questions.isEmpty()) {

            Label emptyLabel = new Label("No questions found.");

            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");

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

        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        

        Label statusLabel = new Label(question.isResolved() ? "✓ RESOLVED" : "");

        statusLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px;");

        statusLabel.setVisible(question.isResolved());

        

        headerBox.getChildren().addAll(titleLabel, statusLabel);

        

        // Content

        Label contentLabel = new Label(question.getContent());

        contentLabel.setWrapText(true);

        contentLabel.setStyle("-fx-text-fill: #333;");

        

        // Metadata

        Label metaLabel = new Label(

            "Asked by: " + question.getAskedBy() + 

            " • " + question.getAnswers().size() + " answers" +

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

                        databaseHelper.upvoteAnswer(answer.getId());

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

                        dialog.close();

                        editAnswer(answer);

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

    

    private void showAlert(String title, String content, AlertType type) {

        Alert alert = new Alert(type);

        alert.setTitle(title);

        alert.setContentText(content);

        alert.showAndWait();

    }

}