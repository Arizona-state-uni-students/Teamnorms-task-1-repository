package application;


import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import java.util.*;
import java.sql.SQLException;


/**

 * UserDatabasePage class represents the user interface for the user database.

 * This page displays user database integrated into a GUI for admin usage.

 */

public class UserDatabaseUI {

    

    private final DatabaseHelper databaseHelper;
    private final User user;
    private Stage primaryStage;
    private VBox layout;
    private GridPane databaseTable;

    

    public UserDatabaseUI(DatabaseHelper databaseHelper, User user) {

        this.databaseHelper = databaseHelper;
        this.user = user;

    }

    

    public void show(Stage primaryStage) throws SQLException {

        this.primaryStage = primaryStage;

        layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        

        databaseTable = new GridPane();
        databaseTable.setPadding(new Insets(10));
        databaseTable.setHgap(10);
        databaseTable.setVgap(10);

        

        Label Header = new Label("Welcome to the User Database.");
        Header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        

        Button goBackButton = new Button("Go back");
        goBackButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;");

        

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #0099ff; -fx-text-fill: white;");

        

        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 14px; -fx-padding: 0 20; -fx-background-color: transparent; -fx-text-fill: #2c2c2c;");

        

        HBox buttonBox = new HBox(10, goBackButton, refreshButton, quitButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        

        layout.getChildren().addAll(Header, databaseTable, buttonBox);

        

        // Load the user data

        loadUserData();

        

        goBackButton.setOnAction(a -> {

            new AdminHomePage(databaseHelper).show(primaryStage, user);

        });

        

        refreshButton.setOnAction(a -> {

            try {

                loadUserData();

            } catch (SQLException e) {

                showAlert("Error", "Failed to refresh data: " + e.getMessage(), AlertType.ERROR);

            }

        });

        

        quitButton.setOnAction(a -> {

            databaseHelper.closeConnection();

            Platform.exit();

        });

        

        ScrollPane scrollPane = new ScrollPane(layout);

        scrollPane.setFitToWidth(true);
        Scene userDBScene = new Scene(scrollPane, 900, 500);

        

        primaryStage.setScene(userDBScene);
        primaryStage.setTitle("User Database Page");

    }

    

    private void loadUserData() throws SQLException {

        databaseTable.getChildren().clear();

        

        // Database table headers

        Label userNameLabel = new Label("Username");
        userNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        

        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        

        Label roleLabel = new Label("Role");
        roleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        

        Label actionsLabel = new Label("Actions");
        actionsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        

        databaseTable.add(userNameLabel, 0, 0);
        databaseTable.add(emailLabel, 1, 0);
        databaseTable.add(roleLabel, 2, 0);
        databaseTable.add(actionsLabel, 3, 0);

        

        // Fetch users and render rows

        List<User> users = databaseHelper.getAllUsers();

        

        int rowIndex = 1;

        for (User u : users) {

            String targetUsername = u.getUserName();

            

            Label userNameCell = new Label(targetUsername);
            userNameCell.setMaxWidth(150);

            

            Label emailCell = new Label(u.getEmail() != null ? u.getEmail() : "Not set");

            emailCell.setMaxWidth(200);
            emailCell.setStyle("-fx-text-fill: #666;");
            Button editEmailBtn = new Button("Edit");
            editEmailBtn.setStyle("-fx-font-size: 10px;");
            editEmailBtn.setOnAction(e -> editUserEmail(targetUsername, u.getEmail()));

            
            HBox emailBox = new HBox(5);
            emailBox.getChildren().addAll(emailCell, editEmailBtn);
            

            // Role with change option

            HBox roleBox = new HBox(5);
            Label roleCell = new Label(u.getRole());

            roleCell.setMaxWidth(80);
            roleCell.setStyle("-fx-font-weight: bold; -fx-text-fill: " + 

                (u.getRole().equals("admin") ? "#FF6600;" : "#0066CC;"));

            

            Button changeRoleBtn = new Button("Change");

            changeRoleBtn.setStyle("-fx-font-size: 10px;");
            changeRoleBtn.setOnAction(e -> changeUserRole(targetUsername, u.getRole()));

            

            roleBox.getChildren().addAll(roleCell, changeRoleBtn);

            

            // Password reset controls

            HBox passwordControls = new HBox(5);
            passwordControls.setAlignment(Pos.CENTER_LEFT);

            

            Button resetButton = new Button("Reset Password");

            resetButton.setStyle("-fx-font-size: 11px;");
            passwordControls.getChildren().add(resetButton);

            

            resetButton.setOnAction(ev -> {

                PasswordField passwordField = new PasswordField();

                passwordField.setPromptText("New password");
                passwordField.setMaxWidth(120);

                

                Button saveButton = new Button("Save");
                saveButton.setStyle("-fx-font-size: 11px; -fx-background-color: #0099ff; -fx-text-fill: white;");

                

                Button cancelButton = new Button("Cancel");

                cancelButton.setStyle("-fx-font-size: 11px;");
                passwordControls.getChildren().setAll(passwordField, saveButton, cancelButton);

                

                saveButton.setOnAction(ev2 -> {

                    String newPassword = passwordField.getText();

                    if (newPassword == null || newPassword.isBlank()) {
                        showAlert("Error", "Password cannot be empty!", AlertType.ERROR);

                        return;

                    }

                    

                    try {

                        if (databaseHelper.updateUserPassword(targetUsername, newPassword)) {

                            showAlert("Success", "Password updated for " + targetUsername, AlertType.INFORMATION);
                            passwordControls.getChildren().setAll(resetButton);

                        } else {

                            showAlert("Error", "Failed to update password", AlertType.ERROR);

                        }

                    } catch (SQLException ex) {

                        showAlert("Error", "Database error: " + ex.getMessage(), AlertType.ERROR);

                    }

                });

                

                cancelButton.setOnAction(ev2 -> {
                	
                    passwordControls.getChildren().setAll(resetButton);

                });

            });

            

            // Delete button with confirmation

            Button deleteUserBtn = new Button("Delete");
            deleteUserBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #ff4444; -fx-text-fill: white;");
            deleteUserBtn.setOnAction(e -> deleteUser(targetUsername));

            

            // Add row to the grid

            databaseTable.add(userNameCell, 0, rowIndex);
            databaseTable.add(roleBox, 2, rowIndex);
            databaseTable.add(passwordControls, 3, rowIndex);
            databaseTable.add(deleteUserBtn, 4, rowIndex);
            databaseTable.add(emailBox, 1, rowIndex);
            rowIndex++;

        }

    }

    private void editUserEmail(String username, String currentEmail) {
        TextInputDialog dialog = new TextInputDialog(currentEmail != null ? currentEmail : "");
        dialog.setTitle("Edit Email");
        dialog.setHeaderText("Edit email for user: " + username);
        dialog.setContentText("Enter new email address:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newEmail -> {
            // Basic email validation
            if (!newEmail.trim().isEmpty() && !newEmail.contains("@")) {
                showAlert("Invalid Email", "Please enter a valid email address", AlertType.ERROR);
                return;
            }
            
            try {
                if (databaseHelper.updateUserEmail(username, newEmail.trim())) {
                    showAlert("Success", "Email updated successfully for " + username, AlertType.INFORMATION);
                    loadUserData(); // Refresh the display
                } else {
                    showAlert("Error", "Failed to update email", AlertType.ERROR);
                }
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to update email: " + ex.getMessage(), AlertType.ERROR);
                ex.printStackTrace();
            }
        });
    }
    

    private void changeUserRole(String username, String currentRole) {

        ChoiceDialog<String> dialog = new ChoiceDialog<>(currentRole, "admin", "user");

        dialog.setTitle("Change User Role");
        dialog.setHeaderText("Change role for: " + username);
        dialog.setContentText("Select new role:");

        

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newRole -> {

            if (!newRole.equals(currentRole)) {

                try {

                    if (databaseHelper.updateUserRole(username, newRole)) {

                        showAlert("Success", "Role updated for " + username, AlertType.INFORMATION);

                        loadUserData();

                    } else {

                        showAlert("Error", "Cannot change role. This might be the last admin.", AlertType.ERROR);

                    }

                } catch (SQLException e) {

                    showAlert("Error", "Database error: " + e.getMessage(), AlertType.ERROR);

                }

            }

        });

    }

    

    private void deleteUser(String username) {

        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);

        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete User");
        confirmDialog.setContentText("Are you sure you want to delete user: " + username + "?");

        

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            try {

                if (databaseHelper.deleteUser(username)) {

                    showAlert("Success", "User deleted: " + username, AlertType.INFORMATION);

                    loadUserData();
                } else {

                    showAlert("Error", "Cannot delete user. This might be the last admin.", AlertType.ERROR);
                }

            } catch (SQLException e) {
            	
                showAlert("Error", "Database error: " + e.getMessage(), AlertType.ERROR);

            }

        }

    }

    private void showAlert(String title, String content, AlertType type) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();

    }

}
