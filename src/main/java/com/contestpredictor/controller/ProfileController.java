package com.contestpredictor.controller;

import com.contestpredictor.data.UserDatabase;
import com.contestpredictor.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ProfileController {

    @FXML
    private Button searchContestButton;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label ratingLabel;

    @FXML
    private Label ratingColorLabel;

    @FXML
    private Label contestsLabel;

    @FXML
    private Label maxRatingLabel;

    @FXML
    private void initialize() {
        System.out.println("=== ProfileController initialized ===");
        loadUserProfile();
    }

    private void loadUserProfile() {
        UserDatabase userDB = UserDatabase.getInstance();
        User currentUser = userDB.getCurrentUser();

        if (currentUser != null) {
            System.out.println("Loading profile for user: " + currentUser.getUsername());
            fullNameLabel.setText(currentUser.getFullName());
            usernameLabel.setText("@" + currentUser.getUsername());
            
            int rating = currentUser.getCurrentRating();
            ratingLabel.setText(String.valueOf(rating));
            String colorName = getRatingColor(rating);
            ratingColorLabel.setText(colorName);
            ratingColorLabel.setStyle("-fx-text-fill: " + getRatingColorHex(rating) + "; -fx-font-weight: bold;");
            
            contestsLabel.setText(String.valueOf(currentUser.getContestsParticipated()));
            
            // Calculate max rating from history
            int maxRating = currentUser.getRatingHistory().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(rating);
            maxRatingLabel.setText(String.valueOf(maxRating));
        }
    }

    private String getRatingColor(int rating) {
        if (rating < 400) return "Gray";
        else if (rating < 800) return "Brown";
        else if (rating < 1200) return "Green";
        else if (rating < 1600) return "Cyan";
        else if (rating < 2000) return "Blue";
        else if (rating < 2400) return "Yellow";
        else if (rating < 2800) return "Orange";
        else return "Red";
    }

    private String getRatingColorHex(int rating) {
        if (rating < 400) return "#9CA3AF"; // Gray
        else if (rating < 800) return "#92400E"; // Brown
        else if (rating < 1200) return "#10B981"; // Green
        else if (rating < 1600) return "#06B6D4"; // Cyan
        else if (rating < 2000) return "#3B82F6"; // Blue
        else if (rating < 2400) return "#FBBF24"; // Yellow
        else if (rating < 2800) return "#F97316"; // Orange
        else return "#EF4444"; // Red
    }

    @FXML
    private void handlePredictor() {
        System.out.println("=== ProfileController: Predictor button clicked ===");
        try {
            navigateTo("/fxml/Predictor.fxml", "Rating Predictor");
        } catch (Exception e) {
            System.err.println("ERROR in handlePredictor:");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearchContest() {
        System.out.println("=== ProfileController: Search Contests button clicked ===");
        System.out.println("Button handler invoked!");
        System.out.println("searchContestButton is: " + (searchContestButton != null ? "NOT NULL" : "NULL"));
        
        try {
            System.out.println("About to navigate to SearchContest.fxml");
            navigateTo("/fxml/SearchContest.fxml", "Search Contests");
            System.out.println("Navigation call completed");
        } catch (Exception e) {
            System.err.println("ERROR in handleSearchContest:");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        UserDatabase.getInstance().logout();
        navigateTo("/fxml/Login.fxml", "Login");
    }
    
    @FXML
    private void handleViewStandings() {
        System.out.println("=== ProfileController: View Standings button clicked ===");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ContestStandings.fxml"));
            Parent root = loader.load();
            
            ContestStandingsController controller = loader.getController();
            UserDatabase userDB = UserDatabase.getInstance();
            User currentUser = userDB.getCurrentUser();
            if (currentUser != null) {
                controller.setCurrentUser(currentUser.getUsername(), false);
            }
            
            Stage stage = (Stage) fullNameLabel.getScene().getWindow();
            
            // Preserve window state
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            Scene scene = new Scene(root, currentWidth, currentHeight);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Contest Standings - Contest Rating Predictor");
            
            // Restore window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
        } catch (Exception e) {
            System.err.println("ERROR in handleViewStandings:");
            e.printStackTrace();
        }
    }
    
    private void navigateTo(String fxmlPath, String title) {
        try {
            System.out.println("ProfileController: Navigating to " + fxmlPath);
            
            // Check if resource exists
            java.net.URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                System.err.println("ERROR: FXML file not found: " + fxmlPath);
                return;
            }
            System.out.println("Found resource: " + resourceUrl);
            
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");
            
            Stage stage = (Stage) fullNameLabel.getScene().getWindow();
            
            // Preserve window state
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            Scene scene = new Scene(root, currentWidth, currentHeight);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle(title + " - Contest Rating Predictor");
            
            // Restore window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
            
            System.out.println("Navigation completed successfully");
        } catch (Exception e) {
            System.err.println("ERROR in navigateTo:");
            e.printStackTrace();
        }
    }
}
