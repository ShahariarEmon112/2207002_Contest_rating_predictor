package com.contestpredictor.controller;

import com.contestpredictor.data.UserDatabase;
import com.contestpredictor.model.User.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class RegistrationController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;
    
    @FXML
    private RadioButton contestantRadio;
    
    @FXML
    private RadioButton setterRadio;
    
    private ToggleGroup accountTypeGroup;

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        boolean isSetter = setterRadio.isSelected();

        // Validate input fields
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        // Validate full name (at least 2 characters)
        if (fullName.length() < 2) {
            showError("Full name must be at least 2 characters");
            return;
        }

        // Validate username (at least 4 characters, alphanumeric)
        if (username.length() < 4) {
            showError("Username must be at least 4 characters");
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showError("Username can only contain letters, numbers, and underscores");
            return;
        }

        // Validate password (at least 6 characters)
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Create user with appropriate role
        UserRole role = isSetter ? UserRole.SETTER : UserRole.CONTESTANT;
        UserDatabase userDB = UserDatabase.getInstance();
        boolean success = userDB.registerUserWithRole(username, password, fullName, email, role);
        
        if (success) {
            String roleText = isSetter ? "Setter" : "Contestant";
            showSuccess(roleText + " account created successfully! Redirecting to login...");
            
            // Navigate to login after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> handleBackToLogin());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Username already exists. Please choose another one.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            // Preserve window state
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            Scene scene = new Scene(root, currentWidth, currentHeight);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Login - Contest Rating Predictor");
            
            // Restore window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading login page: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        // Setup account type toggle group
        accountTypeGroup = new ToggleGroup();
        contestantRadio.setToggleGroup(accountTypeGroup);
        setterRadio.setToggleGroup(accountTypeGroup);
        contestantRadio.setSelected(true); // Default to contestant
        
        // Add enter key handlers for smooth navigation
        fullNameField.setOnAction(event -> usernameField.requestFocus());
        usernameField.setOnAction(event -> emailField.requestFocus());
        emailField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> confirmPasswordField.requestFocus());
        confirmPasswordField.setOnAction(event -> handleRegister());
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-background-color: #ffebee;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-background-color: #e8f5e9;");
        messageLabel.setVisible(true);
    }
}
