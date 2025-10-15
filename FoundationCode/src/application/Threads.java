package application;

import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelper.PrivateMessage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Threads page (Ed-like)
 *  - Left pane: list of top-level questions (parentQuestionId IS NULL), newest first
 *  - Each item shows: title, author, and createdAt
 *  - Right pane: thread view = Original Question + public Answers + Private Feedback (visible to asker, sender, staff)
 *  - Composer: post a Public Answer OR a Private Feedback (Question/Answer)
 *  - Askers can create a "Revised Question" (child) that is only shown inside the thread
 */
public class Threads {

    private final DatabaseHelper databaseHelper;
    private final User user;

    private Stage primaryStage;
    private VBox displayQuestions;
    private VBox displayQuestionThread;
    private ComboBox<String> viewFilter; // Public / Private / All

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public Threads(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
    }

    public void show(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // --- Top bar (Back + who is signed in + view filter) ---
        Button back = new Button("\u2190 Back");
        back.setOnAction(e -> new WelcomeLoginPage(databaseHelper).show(primaryStage, user));

        Label signedIn = new Label("Signed in as: " + user.getUserName() + " (" + user.getRole() + ")");
        signedIn.setStyle("-fx-text-fill:#444; -fx-font-size:12px;");

        viewFilter = new ComboBox<>();
        viewFilter.getItems().addAll("Public", "Private", "All");
        viewFilter.setValue("Public"); // default
        viewFilter.setTooltip(new Tooltip("Choose which posts to show in the thread"));

        HBox topBar = new HBox(12, back, new Separator(), signedIn, new Label("View:"), viewFilter);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 10, 8, 10));
        topBar.setStyle("-fx-background-color:#f3f3f3;");

        // --- Left panel (questions list + New button) ---
        displayQuestions = new VBox(8);
        displayQuestions.setPadding(new Insets(10));

        Button newQuestionBtn = new Button("New Question");
        newQuestionBtn.setOnAction(e -> openCreateQuestionDialog());

        VBox leftSide = new VBox(10, newQuestionBtn, displayQuestions);
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

        primaryStage.setScene(new Scene(root, 1100, 640));
        primaryStage.setTitle("Threads (FAQ)");
        primaryStage.show();

        // initial load
        refreshQuestionListAndMaybeSelect();
    }

    // --- helpers ---

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
        none.setStyle("-fx-text-fill:#666;");
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
        Label title = new Label(q.getTitle());
        title.setStyle("-fx-font-weight:bold;");
        Label meta = new Label("by " + q.getAskedBy() + " • " + q.getCreatedAt().format(TS));
        meta.setStyle("-fx-text-fill:#666; -fx-font-size:11px;");
        VBox col = new VBox(2, title, meta);
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

        boolean isAsker = user.getUserName() != null &&
                user.getUserName().equalsIgnoreCase(q.getAskedBy());
        boolean isStaff = isStaff(user.getRole());

        if (isAsker || isStaff) {
            Button followup = new Button("Create revised question");
            followup.setOnAction(e -> openCreateFollowupDialog(q));
            headerActions.getChildren().add(followup);
        }

        // header (question)
        Label title = new Label(q.getTitle());
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");
        Label meta = new Label("by " + q.getAskedBy() + " • " + q.getCreatedAt().format(TS));
        meta.setStyle("-fx-text-fill:#666; -fx-font-size:12px;");
        Label body = new Label(q.getContent());
        body.setWrapText(true);
        body.setStyle("-fx-padding:8; -fx-background-color:#fff; -fx-border-color:#ddd;");

        VBox header = new VBox(6, headerActions, title, meta, body);
        header.setPadding(new Insets(6,0,12,0));
        displayQuestionThread.getChildren().add(header);

        // If asker opened the thread, auto-mark unread private messages
        if (isAsker) {
            try { databaseHelper.markPrivateMessagesReadByAsker(q.getId(), user.getUserName()); }
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
            Label ansTitle = new Label("Answers (" + answers.size() + ")");
            ansTitle.setStyle("-fx-font-weight:bold;");
            VBox answersBox = new VBox(8, ansTitle);

            if (answers.isEmpty()) {
                Label none = new Label("No answers yet. Be the first to help!");
                none.setStyle("-fx-text-fill:#666;");
                answersBox.getChildren().add(none);
            } else {
                for (Answer a : answers) {
                    VBox card = new VBox(3);
                    Label aMeta = new Label(a.getAnsweredBy() + " • " + a.getCreatedAt().format(TS));
                    aMeta.setStyle("-fx-text-fill:#666; -fx-font-size:11px;");
                    Label aText = new Label(a.getContent());
                    aText.setWrapText(true);
                    card.getChildren().addAll(aMeta, aText);
                    card.setStyle("-fx-background-color:#f9f9f9; -fx-border-color:#eee; -fx-padding:8;");
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
        Label title = new Label("Private feedback");
        title.setStyle("-fx-font-weight:bold;");
        box.getChildren().add(title);

        try {
            List<PrivateMessage> all = databaseHelper.getPrivateMessagesForQuestion(q.getId());
            boolean any = false;
            for (PrivateMessage pm : all) {
                if (!canSeePrivate(q, pm)) continue;
                any = true;
                VBox card = new VBox(3);
                Label meta = new Label(pm.getSender() + " • " + pm.getCreatedAt().format(TS) + " • " + pm.getMessageType());
                meta.setStyle("-fx-text-fill:#666; -fx-font-size:11px;");
                Label text = new Label(pm.getContent());
                text.setWrapText(true);
                card.getChildren().addAll(meta, text);
                card.setStyle("-fx-background-color:#fffbea; -fx-border-color:#ffe08a; -fx-padding:8;");
                box.getChildren().add(card);
            }
            if (!any) {
                Label none = new Label("No private feedback yet.");
                none.setStyle("-fx-text-fill:#666;");
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
        lbl.setStyle("-fx-font-weight:bold;");
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
                Answer a = new Answer(q.getId(), content, user.getUserName());
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
        lbl.setStyle("-fx-font-weight:bold;");
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
                databaseHelper.addPrivateMessage(q.getId(), user.getUserName(), kind.getValue().toUpperCase(), content);
                loadThread(q);
            } catch (SQLException ex) {
                showError("Send failed", ex.getMessage());
            }
        });
        VBox box = new VBox(6, lbl, new HBox(6, new Label("Type:"), kind), text, send);
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
                    Question q = new Question(t, c, user.getUserName());
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
                    databaseHelper.createFollowupQuestion(parent.getId(), t, c, user.getUserName());
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
        boolean isAsker = user.getUserName() != null &&
                user.getUserName().equalsIgnoreCase(q.getAskedBy());
        boolean isSender = user.getUserName() != null &&
                user.getUserName().equalsIgnoreCase(pm.getSender());
        boolean isStaff = isStaff(user.getRole());
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

}
