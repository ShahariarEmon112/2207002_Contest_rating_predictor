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
            ratingColorLabel.setText(getRatingColor(rating));
            
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
            
            int width = fxmlPath.contains("Login") ? 1000 : 1200;
            int height = fxmlPath.contains("Login") ? 650 : 800;
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            Stage stage = (Stage) fullNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title + " - Contest Rating Predictor");
            System.out.println("Navigation completed successfully");
        } catch (Exception e) {
            System.err.println("ERROR in navigateTo:");
            e.printStackTrace();
        }
    }
}
