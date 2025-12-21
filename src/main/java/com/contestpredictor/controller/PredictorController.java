package com.contestpredictor.controller;

import com.contestpredictor.data.UserDatabase;
import com.contestpredictor.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PredictorController {

    @FXML private Button searchContestButton;
    @FXML private Label currentRatingLabel;
    @FXML private Label contestsCountLabel;
    @FXML private TextField totalParticipantsField;
    @FXML private TextField yourRankField;
    @FXML private javafx.scene.layout.VBox resultsSection;
    @FXML private Label ratingChangeLabel;
    @FXML private Label newRatingLabel;
    @FXML private Label performanceLabel;
    @FXML private Label errorLabel;

    private int calculatedNewRating;
    private User currentUser;

    @FXML
    private void initialize() {
        System.out.println("=== PredictorController initialized ===");
        
        currentUser = UserDatabase.getInstance().getCurrentUser();
        if (currentUser != null) {
            System.out.println("Current user: " + currentUser.getUsername());
            currentRatingLabel.setText(String.valueOf(currentUser.getCurrentRating()));
            contestsCountLabel.setText(String.valueOf(currentUser.getContestsParticipated()));
        } else {
            System.err.println("ERROR: No current user found!");
        }
    }

    @FXML
    private void handleCalculate() {
        // Hide previous results and errors
        errorLabel.setVisible(false);
        resultsSection.setVisible(false);
        resultsSection.setManaged(false);

        try {
            // Validate input fields are not empty
            String totalParticipantsText = totalParticipantsField.getText().trim();
            String yourRankText = yourRankField.getText().trim();
            
            if (totalParticipantsText.isEmpty() || yourRankText.isEmpty()) {
                showError("Please fill in all fields");
                return;
            }

            // Parse input values
            int totalParticipants = Integer.parseInt(totalParticipantsText);
            int yourRank = Integer.parseInt(yourRankText);

            // Validate input ranges
            if (totalParticipants <= 0) {
                showError("Total participants must be greater than 0");
                return;
            }
            
            if (yourRank <= 0) {
                showError("Rank must be greater than 0");
                return;
            }
            
            if (yourRank > totalParticipants) {
                showError("Rank cannot be greater than total participants");
                return;
            }

            // Calculate rating prediction
            int currentRating = currentUser.getCurrentRating();
            double performance = calculatePerformance(yourRank, totalParticipants);
            int ratingChange = calculateRatingChange(currentRating, performance, currentUser.getContestsParticipated());
            calculatedNewRating = Math.max(0, currentRating + ratingChange);

            // Display results
            displayResults(ratingChange, calculatedNewRating, (int) performance);
            
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers only");
        } catch (Exception e) {
            showError("An error occurred during calculation");
            e.printStackTrace();
        }
    }

    /**
     * Calculate performance rating based on rank
     * Uses percentile-based formula inspired by competitive programming platforms
     * 
     * @param rank Your rank in the contest
     * @param totalParticipants Total number of participants
     * @return Performance rating
     */
    private double calculatePerformance(int rank, int totalParticipants) {
        double avgRating = 1400.0;
        double percentile = (double) (totalParticipants - rank + 1) / totalParticipants;
        percentile = Math.max(0.005, Math.min(0.995, percentile));
        return avgRating + 400.0 * Math.log10(percentile / (1.0 - percentile));
    }

    /**
     * Calculate rating change using Elo-style algorithm
     * Considers current rating, performance, and participation history
     * 
     * @param currentRating Current user rating
     * @param performance Calculated performance rating
     * @param contestsParticipated Number of contests participated
     * @return Rating change value
     */
    private int calculateRatingChange(int currentRating, double performance, int contestsParticipated) {
        double delta = (performance - currentRating) / 2.0;
        
        // Volatility factor (higher for newer participants)
        double volatility = contestsParticipated < 5 ? 1.5 : contestsParticipated < 10 ? 1.25 : contestsParticipated < 20 ? 1.1 : 1.0;
        delta *= volatility;
        
        // K-factor based on rating tier
        double kFactor = currentRating >= 2400 ? 16.0 : currentRating >= 2000 ? 20.0 : currentRating >= 1600 ? 24.0 : currentRating >= 1200 ? 28.0 : 32.0;
        delta *= (kFactor / 32.0);
        
        // Cap maximum change based on rating tier
        int maxChange = currentRating >= 2400 ? 150 : currentRating >= 2000 ? 180 : currentRating >= 1600 ? 220 : currentRating >= 1200 ? 250 : 300;
        int finalChange = (int) Math.round(delta);
        
        return Math.abs(finalChange) > maxChange ? (finalChange > 0 ? maxChange : -maxChange) : finalChange;
    }

    /**
     * Display calculated rating prediction results
     */
    private void displayResults(int ratingChange, int newRating, int performance) {
        // Format and display rating change with color
        ratingChangeLabel.setText((ratingChange >= 0 ? "+" : "") + ratingChange);
        ratingChangeLabel.setStyle("-fx-text-fill: " + (ratingChange >= 0 ? "#4CAF50" : "#f44336") + ";");
        
        // Display new rating and performance
        newRatingLabel.setText(String.valueOf(newRating));
        performanceLabel.setText(String.valueOf(performance));
        
        // Show results section
        resultsSection.setVisible(true);
        resultsSection.setManaged(true);
    }

    /**
     * Apply calculated rating to user profile
     */
    @FXML
    private void handleApplyRating() {
        if (currentUser != null) {
            // Update user rating history and contests count
            currentUser.addRatingToHistory(calculatedNewRating);
            currentUser.setContestsParticipated(currentUser.getContestsParticipated() + 1);
            UserDatabase.getInstance().setCurrentUser(currentUser);
            
            // Refresh displayed stats
            currentRatingLabel.setText(String.valueOf(currentUser.getCurrentRating()));
            contestsCountLabel.setText(String.valueOf(currentUser.getContestsParticipated()));
            
            totalParticipantsField.clear();
            yourRankField.clear();
            resultsSection.setVisible(false);
            
            errorLabel.setText("✓ Rating updated successfully!");
            errorLabel.setStyle("-fx-text-fill: #4CAF50;");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleProfile() {
        System.out.println("=== PredictorController: Profile button clicked ===");
        try {
            navigateTo("/fxml/Profile.fxml", "Profile");
        } catch (Exception e) {
            System.err.println("ERROR in handleProfile:");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearchContest() {
        System.out.println("=== PredictorController: Search Contests button clicked ===");
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
        System.out.println("=== PredictorController: Logout button clicked ===");
        UserDatabase.getInstance().logout();
        navigateTo("/fxml/Login.fxml", "Login");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            System.out.println("PredictorController: Navigating to " + fxmlPath);
            
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
            
            Stage stage = (Stage) currentRatingLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title + " - Contest Rating Predictor");
            System.out.println("Navigation completed successfully");
        } catch (Exception e) {
            System.err.println("ERROR in navigateTo:");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #f44336;");
        errorLabel.setVisible(true);
    }
}
