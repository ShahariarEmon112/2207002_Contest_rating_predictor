package com.contestpredictor.controller;

import com.contestpredictor.data.AdminDatabase;
import com.contestpredictor.data.UserDatabase;
import com.contestpredictor.model.Admin;
import com.contestpredictor.model.User;
import com.contestpredictor.model.User.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Check user login (both Setter and Contestant)
        UserDatabase userDB = UserDatabase.getInstance();
        User user = userDB.authenticate(username, password);

        if (user != null) {
            // Route based on user role
            if (user.getRole() == UserRole.SETTER) {
                navigateToSetterDashboard(user);
            } else {
                navigateToContestantDashboard(user);
            }
            return;
        }
        
        // Check admin login (fallback for existing admins)
        AdminDatabase adminDB = AdminDatabase.getInstance();
        Admin admin = adminDB.authenticate(username, password);
        
        if (admin != null) {
            navigateToAdminDashboard(admin);
            return;
        }

        showError("Invalid username or password");
    }
    
    private void navigateToContestantDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDashboard.fxml"));
            Parent root = loader.load();
            
            UserDashboardController controller = loader.getController();
            controller.setUser(user);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            navigateToScene(stage, root, "Contestant Dashboard - Contest Rating Predictor");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading contestant dashboard: " + e.getMessage());
        }
    }
    
    private void navigateToSetterDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SetterDashboard.fxml"));
            Parent root = loader.load();
            
            SetterDashboardController controller = loader.getController();
            controller.setUser(user);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            navigateToScene(stage, root, "Setter Dashboard - Contest Rating Predictor");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading setter dashboard: " + e.getMessage());
        }
    }
    
    private void navigateToAdminDashboard(Admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            
            AdminDashboardController controller = loader.getController();
            controller.setAdmin(admin);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            navigateToScene(stage, root, "Admin Dashboard - Contest Rating Predictor");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading admin dashboard: " + e.getMessage());
        }
    }
    
    private void navigateToScene(Stage stage, Parent root, String title) {
        boolean wasFullScreen = stage.isFullScreen();
        boolean wasMaximized = stage.isMaximized();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        
        Scene scene = new Scene(root, currentWidth, currentHeight);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle(title);
        
        if (wasMaximized) {
            stage.setMaximized(true);
        }
        if (wasFullScreen) {
            stage.setFullScreen(true);
        }
    }

    @FXML
    private void initialize() {
        // Add enter key handler for password field
        passwordField.setOnAction(event -> handleLogin());
        usernameField.setOnAction(event -> passwordField.requestFocus());
    }

    @FXML
    private void handleGoToAdminLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminLogin.fxml"));
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
            stage.setTitle("Admin Login - Contest Rating Predictor");
            
            // Restore window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading admin login: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
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
            stage.setTitle("Register - Contest Rating Predictor");
            
            // Restore window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading registration page: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
