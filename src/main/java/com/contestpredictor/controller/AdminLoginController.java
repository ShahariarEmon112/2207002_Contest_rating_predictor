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
        try {
            AdminDatabase adminDB = AdminDatabase.getInstance();
            Admin admin = adminDB.authenticate(username, password);
            
            if (admin != null) {
                // Admin login successful - navigate to admin dashboard
                System.out.println("Admin authenticated successfully: " + admin.getUsername());
                loadAdminDashboard(admin);
            } else {
                showError("Invalid admin credentials");
                passwordField.clear();
            }
        } catch (Exception e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
            showError("Authentication error. Please try again.");
        }
    }

    private void loadAdminDashboard(Admin admin) {
        try {
            System.out.println("Loading AdminDashboard.fxml...");
            
            // Verify FXML file exists
            java.net.URL fxmlUrl = getClass().getResource("/fxml/AdminDashboard.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: AdminDashboard.fxml not found!");
                showError("Admin dashboard file not found. Please check installation.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");
            
            AdminDashboardController controller = loader.getController();
            if (controller == null) {
                System.err.println("ERROR: AdminDashboardController is null!");
                showError("Failed to initialize admin dashboard controller.");
                return;
            }
            
            System.out.println("Setting admin in controller...");
            controller.setAdmin(admin);
            System.out.println("Admin set successfully");
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            // Preserve window state
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            Scene scene = new Scene(root, currentWidth, currentHeight);
            
            // Load CSS if available
            java.net.URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS loaded successfully");
            } else {
                System.err.println("WARNING: styles.css not found");
            }
            
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard - Contest Rating Predictor");
            
            // Restore window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
            
            System.out.println("Admin dashboard loaded successfully!");
            
        } catch (java.io.IOException e) {
            System.err.println("IO Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load admin dashboard. File may be corrupted.");
        } catch (IllegalStateException e) {
            System.err.println("State error in controller: " + e.getMessage());
            e.printStackTrace();
            showError("Dashboard initialization failed: " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("Null pointer in dashboard loading: " + e.getMessage());
            e.printStackTrace();
            showError("Missing required component in dashboard.");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading dashboard. Check console for details.");
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
        } catch (java.io.IOException e) {
            System.err.println("Failed to load user login FXML: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load user login. Please check if Login.fxml exists.");
        } catch (IllegalStateException e) {
            System.err.println("Controller initialization error: " + e.getMessage());
            e.printStackTrace();
            showError("User login initialization failed. Please contact support.");
        } catch (Exception e) {
            System.err.println("Unexpected error loading user login: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            showError("An unexpected error occurred. Please try again or contact support.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
