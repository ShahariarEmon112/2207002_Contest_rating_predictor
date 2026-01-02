package com.contestpredictor.controller;

import com.contestpredictor.data.AdminDatabase;
import com.contestpredictor.model.Admin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        // Add enter key handler for password field
        passwordField.setOnAction(event -> handleAdminLogin());
        usernameField.setOnAction(event -> passwordField.requestFocus());
    }

    @FXML
    private void handleAdminLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Check admin authentication
        AdminDatabase adminDB = AdminDatabase.getInstance();
        Admin admin = adminDB.authenticate(username, password);
        
        if (admin != null) {
            // Admin login successful - navigate to admin dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
                Parent root = loader.load();
                
                AdminDashboardController controller = loader.getController();
                controller.setAdmin(admin);
                
                Stage stage = (Stage) usernameField.getScene().getWindow();
                
                // Preserve window state
                boolean wasFullScreen = stage.isFullScreen();
                boolean wasMaximized = stage.isMaximized();
                double currentWidth = stage.getWidth();
                double currentHeight = stage.getHeight();
                
                Scene scene = new Scene(root, currentWidth, currentHeight);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                
                stage.setScene(scene);
                stage.setTitle("Admin Dashboard - Contest Rating Predictor");
                
                // Restore window state
                if (wasMaximized) {
                    stage.setMaximized(true);
                }
                if (wasFullScreen) {
                    stage.setFullScreen(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error loading admin dashboard: " + e.getMessage());
            }
        } else {
            showError("Invalid admin credentials");
            // Clear password field for security
            passwordField.clear();
        }
    }

    @FXML
    private void handleGoToUserLogin() {
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
            showError("Error loading user login: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
