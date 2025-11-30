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

public class staffpage {
    
    private final DatabaseHelper databaseHelper;
    Label titleLabel;
    Label statusLabel = new Label();
    VBox displayReviewers = new VBox(6);
    double thresh01 = 2;
    double thresh02 = 0.25;
    double thresh03 = 0.33;
    int questionsAsked;
    int totalAnswers;
    int correctAnswers;
    int totalReviews;
    int weight;
    double currentThresh = 25;
    Label stats = new Label();
    Label myScore = new Label();
    Label Thresh01 = new Label();
    Label Thresh02 = new Label();
    Label Thresh03 = new Label();
    Label ThreshCalcLabel = new Label();
    TextField reviewerThreshold = new TextField();
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    public staffpage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
     * Displays the staff page
     *  
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

        HBox anotherHBox = new HBox (12, roleLabel, titleLabel, statusLabel); 
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
        double[] threshs = databaseHelper.getThresh();
        currentThresh = threshs[0];
        thresh01 = threshs[1];
        thresh02 = threshs[2];
        thresh03 = threshs[3];
        Thresh01 = new Label();
        Thresh02 = new Label();
        Thresh03 = new Label();
        Label statsLabel = new Label("Statistics");
        statsLabel.setStyle(colors.LABEL);
        questionsAsked = databaseHelper.questionsCount(currentUser.getUserName());
        totalAnswers = databaseHelper.answersCount(currentUser.getUserName());
        correctAnswers = databaseHelper.correctAnswersCount(currentUser.getUserName());
        totalReviews = databaseHelper.reviewsCount(currentUser.getUserName());
        weight = currentUser.getWeight();
        updateScore();
        Label reviewerThresh = new Label("Trusted Reviewer Threshold:");
        reviewerThresh.setStyle(colors.LABEL);
        reviewerThreshold.setMaxWidth(250);
        Button updateThreshold = new Button("✓");
        updateThreshold.setStyle(colors.GO);
        updateThreshold.setOnAction(e -> {
        	try {
				databaseHelper.updateThresh(currentThresh);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        });
        Slider Threshslider = new Slider();
        Threshslider.setMin(0);
        Threshslider.setMax(100);
        Threshslider.setValue(currentThresh);
        Threshslider.setMajorTickUnit(3);
        Threshslider.setMinorTickCount(4);
        Threshslider.setShowTickMarks(true);
        Threshslider.setShowTickLabels(true);
        Threshslider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Display the value rounded to 2 decimal places
        	currentThresh = newValue.doubleValue();
            updateScore();
        });
        Label threshCalculator = new Label("");
        Slider ThreshsliderO1 = new Slider();
        ThreshsliderO1.setMin(0);
        ThreshsliderO1.setMax(10);
        ThreshsliderO1.setValue(thresh01);
        ThreshsliderO1.setMajorTickUnit(2.5);
        ThreshsliderO1.setMinorTickCount(4);
        ThreshsliderO1.setShowTickMarks(true);
        ThreshsliderO1.setShowTickLabels(true);
        ThreshsliderO1.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Display the value rounded to 2 decimal places
        	thresh01 = newValue.doubleValue();
            updateScore();
        });
        Slider ThreshsliderO2 = new Slider();
        ThreshsliderO2.setMin(0);
        ThreshsliderO2.setMax(3);
        ThreshsliderO2.setValue(thresh02);
        ThreshsliderO2.setMajorTickUnit(3);
        ThreshsliderO2.setMinorTickCount(4);
        ThreshsliderO2.setShowTickMarks(true);
        ThreshsliderO2.setShowTickLabels(true);
        ThreshsliderO2.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Display the value rounded to 2 decimal places
        	thresh02 = newValue.doubleValue();
            updateScore();
        });
        Slider ThreshsliderO3 = new Slider();
        ThreshsliderO3.setMin(0);
        ThreshsliderO3.setMax(3);
        ThreshsliderO3.setValue(thresh03);
        ThreshsliderO3.setMajorTickUnit(3);
        ThreshsliderO3.setMinorTickCount(4);
        ThreshsliderO3.setShowTickMarks(true);
        ThreshsliderO3.setShowTickLabels(true);
        ThreshsliderO3.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Display the value rounded to 2 decimal places
        	thresh03 = newValue.doubleValue();
            updateScore();
        });
        Button saveCalculations = new Button("✓");
        saveCalculations.setStyle(colors.GO);
        saveCalculations.setOnAction(e->{
        	try {
				databaseHelper.updateScoreCards(thresh01, thresh02, thresh03);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        });
        HBox ThreshCalculator = new HBox(Thresh01, ThreshsliderO1, Thresh02, ThreshsliderO2, Thresh03, ThreshsliderO3, saveCalculations);
        HBox ThreshSettings = new HBox(6, reviewerThresh, reviewerThreshold, updateThreshold, Threshslider);
        HBox bottombar = new HBox(6, goBackButton, logoutButton);
        Label reviewersLabel = new Label("Reviewers:");
        reviewersLabel.setStyle(colors.LABEL);
        vbox.getChildren().addAll(anotherHBox, statsLabel, stats, myScore, ThreshCalcLabel, ThreshCalculator, ThreshSettings, reviewersLabel, displayReviewers, bottombar);
        loadReviewers(primaryStage, currentUser);
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
    /**
     * Method to update the GUI for score calculations.
     */
    private void updateScore() {
    	Thresh01.setText(String.format("%.2f",thresh01));
    	Thresh02.setText(String.format("%.2f",thresh02));
    	Thresh03.setText(String.format("%.2f",thresh03));
    	reviewerThreshold.setText(String.format("%.2f",currentThresh));
        stats.setText("Questions Asked: "+questionsAsked+" | Total Answers: "+totalAnswers+" | Answers Marked Correct: "+correctAnswers+" | Upvotes: "+weight+" | Total Reviews: "+totalReviews);
        double myReviewerScore = ((double)correctAnswers/totalAnswers)*thresh01+(thresh02*weight)+(totalReviews*thresh03);
        myScore.setText("My Reviewer Score: "+String.format("%.2f", (double) myReviewerScore));
        myScore.setStyle(colors.LABEL);
        ThreshCalcLabel.setText("((CorrectAnswers/TotalAnswers) * "+String.format("%.2f",thresh01)+") + ("+String.format("%.2f",thresh02)+" * Upvotes) + (TotalReviews * "+String.format("%.2f",thresh03)+")");
    }
    
    /**
     * Method to load and display reviewers.
     */
    private void loadReviewers(Stage primaryStage, User currentUser) {
    	displayReviewers.getChildren().clear();
    	try {
    		//admin, instructor will not show up on this list if they create reviews, because they are not "Reviewers"
			List<User> reviewers = databaseHelper.getUsers_Role("Reviewer");
			for(User r : reviewers) {
	              Label head = new Label();
	              head.setStyle("-fx-font-weight:bold; -fx-text-fill:#000;");
	              head.setText(r.getUserName());
	              
	              Button demoteReviewer = new Button("Demote");
                  demoteReviewer.setStyle("-fx-text-fill:#fff; -fx-background-color:#f00");
		          demoteReviewer.setOnAction(e -> {
		              demoteReviewer(primaryStage, currentUser, r.getUserName());
		          });
		       	  demoteReviewer.setVisible(currentUser.getPrivileges() > 2); //hide button if not instructor+
	              Button loadReviews = new Button("Load Reviews");
	              loadReviews.setOnAction(e -> {
	            	  StudentQAPage qaPage = new StudentQAPage(databaseHelper, currentUser);
	                  qaPage.show(primaryStage, 1, r.getUserName());
	            	  //loadAllReviews(r.getUserName());
	            	  });
	              Button loadAnswers = new Button("Load Answers");
	              loadAnswers.setOnAction(e -> {
	            	  StudentQAPage qaPage = new StudentQAPage(databaseHelper, currentUser);
	                  qaPage.show(primaryStage, 2, r.getUserName());
	            	  //loadAllAnswersBy(r.getUserName());
	            	  });
	              Button favoriteReviewer = new Button("Add to Favorites");
                  favoriteReviewer.setStyle("-fx-text-fill: white; -fx-font-size: 11px; " +"-fx-background-color: #ff7700; -fx-padding: 2px 5px 2px 5px;");
	              favoriteReviewer.setOnAction(e -> {
					try {
						databaseHelper.addFavorite(currentUser.getUserName(), r.getUserName());
						favoriteReviewer.setVisible(false);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
	              //hide add favorite button if already favorited
	              String[] current = databaseHelper.getFavorites(currentUser.getUserName());
	              for (String s : current) {
	            	  System.out.println(s);
	                  if (s.equalsIgnoreCase(r.getUserName())) {
	                      favoriteReviewer.setVisible(false);
	                  }
	              }
	              if(currentUser.getUserName().equals(r.getUserName())) {
	            	  favoriteReviewer.setVisible(false);
	              }
	              head.setStyle("-fx-font-weight:bold; -fx-text-fill:#000;");
	              HBox wrap = new HBox(6);
	              wrap.setPadding(new Insets(8,0,0,0));
	              wrap.getChildren().addAll(head, loadReviews, loadAnswers, demoteReviewer, favoriteReviewer);
	              displayReviewers.getChildren().add(wrap);
				//System.out.println(r.getUserName());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * Method to demote a reviewer.
     * 
     * @param reviewer Username of the reviewer to demote.
     */
    private void demoteReviewer(Stage primaryStage, User currentUser, String reviewer) {
    	try {
			databaseHelper.updateUserRole(reviewer, "Student");
			loadReviewers(primaryStage, currentUser);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}

