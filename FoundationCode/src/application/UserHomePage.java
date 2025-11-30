package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import databasePart1.*;

public class UserHomePage {
    
    private final DatabaseHelper databaseHelper;
    Label titleLabel;
    Label statusLabel = new Label();
    VBox displayReviewers = new VBox(6);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    public UserHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the user home page in the primary stage. 
     * @param primaryStage The primary stage where the scene will be displayed.
     * @param currentUser User currently using the system.
	 * @throws SQLException If a database error occurs.
     */
    public void show(Stage primaryStage, User currentUser) throws SQLException {
    	VBox vbox = new VBox(8);
    	vbox.setPadding(new Insets(15));
        statusLabel.setStyle("-fx-font-size: 12px;");
        titleLabel = new Label(currentUser.getFullName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.TOP_RIGHT);
        Label roleLabel = new Label(currentUser.getRole());
        roleLabel.setStyle(colors.BASIC + colors.USER_PRIMARY);
        if(currentUser.getPrivileges()==1) {roleLabel.setStyle(colors.BASIC + colors.STUDENT_PRIMARY);}
        if(currentUser.getPrivileges()==2) {roleLabel.setStyle(colors.BASIC + colors.REVIEWER_PRIMARY);}
        if(currentUser.getPrivileges()==3) {roleLabel.setStyle(colors.BASIC + colors.STAFF_PRIMARY);}
        if(currentUser.getPrivileges()==4) {roleLabel.setStyle(colors.BASIC + colors.INSTRUCTOR_PRIMARY);}
        if(currentUser.getPrivileges()==99) {roleLabel.setStyle(colors.BASIC + colors.ADMIN_PRIMARY);}
        //if(currentUser.getPrivileges()>=2) {loadAllReviews(currentUser.getUserName());}

        
        Label usernameLabel = new Label("User");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField usernameField = new TextField(currentUser.getUserName());
        usernameField.setEditable(false);
        usernameField.setMaxWidth(120);
        usernameField.setStyle("-fx-background-color: #e0e0e0;");

        Label nameLabel = new Label("Name");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextField firstField = new TextField(currentUser.getFirstName());
        firstField.setMaxWidth(150);
        TextField middleField = new TextField();
        middleField.setText(currentUser.getMiddleInitial());
        middleField.setMaxWidth(25);
        middleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                middleField.setText(newValue.substring(0, 1));
            }
        });
        TextField lastField = new TextField(currentUser.getLastName());
        lastField.setMaxWidth(150);
        
        Button updateName = new Button("✓");
        updateName.setStyle(colors.GO);
        updateName.setOnAction(e -> {
            if(firstField.getText().length()>0 && middleField.getText().length()==1 && lastField.getText().length()>0) {
	            try {
	                if (databaseHelper.updateUsersName(currentUser.getUserName(), firstField.getText(), middleField.getText(), lastField.getText())) {
	                	currentUser.setFirstName(firstField.getText());
	                    currentUser.setMiddleInitial(middleField.getText());
	                    currentUser.setLastName(lastField.getText());
	                    statusLabel.setText("successful!");
	                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
	                   
	                    showAlert("Success", "successful update!", AlertType.INFORMATION);
	                    titleLabel.setText(currentUser.getFullName());
	                }
	           }catch (SQLException ex) {
	               ex.printStackTrace();
	           }
            }else {
            	showAlert("Failure", "name failed to update!", AlertType.INFORMATION);
                statusLabel.setText("Failed");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
             }
        });
        Button revertName = new Button("x");
        revertName.setStyle(colors.BASIC);
        revertName.setOnAction(e->{
        	firstField.setText(currentUser.getFirstName());
        	middleField.setText(currentUser.getMiddleInitial());
        	lastField.setText(currentUser.getLastName());
        });
        
        HBox namebox = new HBox(4, firstField, middleField, lastField, updateName, revertName);
        HBox roleBox = new HBox(4, roleLabel, usernameLabel, usernameField, nameLabel, namebox);
        
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField emailField = new TextField();
        emailField.setText(currentUser.getEmail());
        emailField.setMaxWidth(250);
        Button updateEmailButton = new Button("✓");
        updateEmailButton.setStyle(colors.GO);
        updateEmailButton.setOnAction(e -> {
            String newEmail = emailField.getText().trim();
            if (!newEmail.isEmpty() && (!newEmail.contains("@") || !newEmail.contains("."))) {
                showAlert("Invalid Email", "Please enter a valid email address", AlertType.ERROR);
                return;
            }
            if(newEmail.isEmpty()) {
                emailField.setPromptText("email address");
                emailField.setStyle("-fx-border-color: orange; -fx-border-width: 1px;");
                emailField.setMaxWidth(200);
                Label emailWarning = new Label("⚠ Email is required for all users");
                emailWarning.setStyle("-fx-text-fill: orange; -fx-font-size: 10px;");
                return;
            }
            
            try {
                if (databaseHelper.updateUserEmail(currentUser.getUserName(), newEmail)) {
                    currentUser.setEmail(newEmail);
                    statusLabel.setText("Email updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                    showAlert("Success", "Email updated successfully!", AlertType.INFORMATION);
                } else {
                    statusLabel.setText("Failed to update email");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                ex.printStackTrace();
            }
        });
        Button revertEmailButton = new Button("x");
        revertEmailButton.setStyle(colors.BASIC);
        revertEmailButton.setOnAction(e->{
        	emailField.setText(currentUser.getEmail());
        });
        HBox emailBox = new HBox(10, emailLabel, emailField, updateEmailButton, revertEmailButton);
        
        HBox anotherHBox = new HBox (12, titleLabel, statusLabel);
        vbox.getChildren().addAll(roleBox, anotherHBox, emailBox);
        
        
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter new password");
        passwordField.setMaxWidth(200);
        Button revertPasswordButton = new Button("x");
        revertPasswordButton.setStyle(colors.BASIC);
        revertPasswordButton.setOnAction(e -> {
            passwordField.clear();
            statusLabel.setText("");
        });
        Button savePasswordButton = new Button("✓");
        savePasswordButton.setStyle(colors.GO);
        savePasswordButton.setOnAction(e -> {
            String newPassword = passwordField.getText();
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                statusLabel.setText("Password cannot be empty!");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                return;
            }
            
            try {
                if (databaseHelper.updateUserPassword(currentUser.getUserName(), newPassword)) {
                    currentUser.setPassword(newPassword);
                    passwordField.clear();
                    statusLabel.setText("Password updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                    showAlert("Success", "Password updated successfully!", AlertType.INFORMATION);
                } else {
                    statusLabel.setText("Failed to update password");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                ex.printStackTrace();
            }
        });
        HBox passwordBox = new HBox(10);
        passwordBox.getChildren().addAll(passwordLabel, passwordField, savePasswordButton, revertPasswordButton);
 
        
        Label reviewerLabel = new Label("Reviewer Status");
        reviewerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextField reviewerField = new TextField();
        reviewerField.setText("You are not a Reviewer");
        if (databaseHelper.hasPendingRequest(currentUser.getUserName())) {reviewerField.setText("Application Sent!");}
        reviewerField.setEditable(false);
        reviewerField.setMaxWidth(200);
        reviewerField.setStyle("-fx-background-color: #e0e0e0;");        
        
        Button reviewerRequest = new Button("Request to be a reviewer");
		reviewerRequest.setStyle(colors.GO);
        reviewerRequest.setOnAction(e -> {
        	try {
        		if (databaseHelper.updateHasRequest(currentUser.getUserName(), true)) {
        			currentUser.setReviewerApplicant(true);
        			reviewerRequest.setText("Reviewer Request Pending");
        			reviewerRequest.setDisable(true);
        		}
        		else {
                    statusLabel.setText("Failed to request a reviewer role");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                }
        		}
        	catch (SQLException ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                ex.printStackTrace();
            }
        });
		reviewerRequest.setDisable(true);
			if (databaseHelper.hasPendingRequest(currentUser.getUserName())) {
				reviewerRequest.setText("Reviewer Request Pending...");        	
			}
			else if (currentUser.getPrivileges() > 1) {
				reviewerRequest.setText("Request to be a reviewer");
		        reviewerField.setText("You are a Reviewer");
			}
			else {
				reviewerRequest.setDisable(false);
			}
        HBox reviewThings = new HBox (6, reviewerField, reviewerRequest);
        
        Label statsLabel = new Label("Statistics");
        statsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        int questionsAsked = databaseHelper.questionsCount(currentUser.getUserName());
        int totalAnswers = databaseHelper.answersCount(currentUser.getUserName());
        int correctAnswers = databaseHelper.correctAnswersCount(currentUser.getUserName());
        int totalReviews = databaseHelper.reviewsCount(currentUser.getUserName());
        Label stats = new Label("Questions Asked: "+questionsAsked+" | Questions Answered: "+totalAnswers+" | Answers Marked Correct: "+correctAnswers+" | Weight: "+currentUser.getWeight()+" | Total Reviews: "+totalReviews);
        double score = databaseHelper.getReviewerScore(currentUser.getUserName());
        double[] thresh = databaseHelper.getThresh();
        Label scorecard = new Label("Reviewer score: "+String.format("%.2f", score)+ " || Trust Threshold: "+String.format("%.2f", thresh[0]));
        Label scorestatus = new Label("Status: ✓ Trusted Reviewer");
        scorecard.setManaged(currentUser.getPrivileges()>=2);
        scorestatus.setManaged(currentUser.getPrivileges()>=2);
        if(score<thresh[0]) {scorestatus.setText("Status: Currently not trusted entirely as a reviewer.");}
        Button goBackButton = new Button("Main Page");
        goBackButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
        goBackButton.setOnAction(e -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, currentUser);
        });
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");
        logoutButton.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage);
        });
        Button loadReviews = new Button("Load Reviews");
        loadReviews.setStyle(colors.BASIC + colors.REVIEWER_PRIMARY);
        loadReviews.setOnAction(e->{
        	StudentQAPage qaPage = new StudentQAPage(databaseHelper, currentUser);
            qaPage.show(primaryStage, 1, currentUser.getUserName());
        });
        loadReviews.setManaged(currentUser.getPrivileges()>=2);
        Button loadAnswers = new Button("Load Answers");
        loadAnswers.setStyle(colors.BASIC + colors.STUDENT_PRIMARY);
        loadAnswers.setOnAction(e->{
      	  StudentQAPage qaPage = new StudentQAPage(databaseHelper, currentUser);
          qaPage.show(primaryStage, 2, currentUser.getUserName());
        });
        HBox reviewButtons = new HBox(6, loadReviews, loadAnswers);
        HBox bottombar = new HBox(6, goBackButton, logoutButton);
        vbox.getChildren().addAll(passwordBox, reviewerLabel, reviewThings, statsLabel, stats, scorecard, scorestatus, reviewButtons, bottombar);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);
        Scene scene = new Scene(scrollPane, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("sQaaS™ - User Profile");
        primaryStage.setResizable(false);
        primaryStage.show();
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

