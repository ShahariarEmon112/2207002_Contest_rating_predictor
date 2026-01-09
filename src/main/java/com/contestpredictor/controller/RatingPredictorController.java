package com.contestpredictor.controller;

import com.contestpredictor.model.Contestant;
import com.contestpredictor.util.ContestantRatingPredictor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;

public class RatingPredictorController {
    
    @FXML private TextField limitField;
    @FXML private Button fetchButton;
    @FXML private Button predictButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;
    
    @FXML private TableView<Contestant> contestantsTable;
    @FXML private TableColumn<Contestant, String> handleColumn;
    @FXML private TableColumn<Contestant, Integer> oldRatingColumn;
    @FXML private TableColumn<Contestant, Integer> rankColumn;
    @FXML private TableColumn<Contestant, Integer> problemsSolvedColumn;
    @FXML private TableColumn<Contestant, Integer> penaltyColumn;
    @FXML private TableColumn<Contestant, Integer> deltaColumn;
    @FXML private TableColumn<Contestant, Integer> newRatingColumn;
    
    @FXML private Label totalContestantsLabel;
    @FXML private Label avgDeltaLabel;
    @FXML private Label maxGainLabel;
    @FXML private Label maxLossLabel;
    
    private ObservableList<Contestant> contestants;
    private boolean isAdmin = false; // Flag to track if current user is admin
    
    @FXML
    public void initialize() {
        contestants = FXCollections.observableArrayList();
        
        // Configure table columns
        handleColumn.setCellValueFactory(new PropertyValueFactory<>("handle"));
        oldRatingColumn.setCellValueFactory(new PropertyValueFactory<>("oldRating"));
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        problemsSolvedColumn.setCellValueFactory(new PropertyValueFactory<>("problemsSolved"));
        penaltyColumn.setCellValueFactory(new PropertyValueFactory<>("penalty"));
        deltaColumn.setCellValueFactory(new PropertyValueFactory<>("delta"));
        newRatingColumn.setCellValueFactory(new PropertyValueFactory<>("newRating"));
        
        // Make problems solved column editable only for admins
        problemsSolvedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        problemsSolvedColumn.setOnEditCommit(event -> {
            if (!isAdmin) {
                showAlert("Access Denied", "Only admins can update problems solved. This is a read-only field for users.");
                event.consume();
                return;
            }
            Contestant contestant = event.getRowValue();
            contestant.setProblemsSolved(event.getNewValue());
            recalculateRankingsAndRatings();
            updateStatus("Problems solved updated for " + contestant.getHandle() + " - Rankings recalculated");
        });
        
        // Make penalty column editable only for admins
        penaltyColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        penaltyColumn.setOnEditCommit(event -> {
            if (!isAdmin) {
                showAlert("Access Denied", "Only admins can update penalty. This is a read-only field for users.");
                event.consume();
                return;
            }
            Contestant contestant = event.getRowValue();
            contestant.setPenalty(event.getNewValue());
            recalculateRankingsAndRatings();
            updateStatus("Penalty updated for " + contestant.getHandle() + " - Rankings recalculated");
        });
        
        // Make rank column read-only but visible (auto-calculated)
        rankColumn.setEditable(false);
        
        // Color code delta column
        deltaColumn.setCellFactory(column -> new TableCell<Contestant, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item > 0 ? "+" + item : item.toString());
                    if (item > 0) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else if (item < 0) {
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #757575;");
                    }
                }
            }
        });
        
        contestantsTable.setItems(contestants);
        contestantsTable.setEditable(false); // Start as read-only, will be enabled for admins
        
        // Set default limit
        limitField.setText("100");
    }
    
    /**
     * Set admin status - call this to enable/disable editing
     */
    public void setAdminStatus(boolean admin) {
        this.isAdmin = admin;
        
        // Update table editable state
        if (isAdmin) {
            contestantsTable.setEditable(true);
            updateStatus("Admin mode: You can now edit problems solved and penalty");
        } else {
            contestantsTable.setEditable(false);
            updateStatus("User mode: Problem count and penalty are read-only");
        }
    }
    
    
    @FXML
    private void handleFetchContestants() {
        try {
            int limit = Integer.parseInt(limitField.getText());
            
            if (limit <= 0 || limit > 1000) {
                showAlert("Invalid Input", "Please enter a number between 1 and 1000");
                return;
            }
            
            updateStatus("Fetching contestants from Codeforces API...");
            fetchButton.setDisable(true);
            
            // Run API call in background thread
            new Thread(() -> {
                try {
                    var fetchedContestants = ContestantRatingPredictor.fetchContestants(limit);
                    
                    // Initialize with random contest data for demonstration
                    for (Contestant c : fetchedContestants) {
                        c.setProblemsSolved((int)(Math.random() * 8) + 1); // 1-8 problems
                        c.setPenalty((int)(Math.random() * 300) + 10); // 10-310 minutes penalty
                    }
                    
                    Platform.runLater(() -> {
                        contestants.clear();
                        contestants.addAll(fetchedContestants);
                        recalculateRankingsAndRatings();
                        updateStatus("Successfully fetched " + fetchedContestants.size() + " contestants with random contest data");
                        updateStatistics();
                        fetchButton.setDisable(false);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("Fetch Error", "Failed to fetch contestants: " + e.getMessage());
                        updateStatus("Error: " + e.getMessage());
                        fetchButton.setDisable(false);
                    });
                }
            }).start();
            
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number");
        }
    }
    
    @FXML
    private void handlePredictRatings() {
        if (contestants.isEmpty()) {
            showAlert("No Data", "Please fetch contestants first");
            return;
        }
        
        updateStatus("Recalculating rating predictions...");
        recalculateRankingsAndRatings();
        updateStatus("Rating predictions recalculated successfully");
    }
    
    /**
     * Dynamically recalculates ranks and ratings when data changes
     */
    private void recalculateRankingsAndRatings() {
        if (contestants.isEmpty()) return;
        
        // Recalculate ranks based on problems solved and penalty
        ContestantRatingPredictor.assignRanks(contestants);
        
        // Recalculate rating changes using AtCoder formula
        ContestantRatingPredictor.computeRatingChanges(contestants);
        ContestantRatingPredictor.updateRatings(contestants);
        
        // Refresh table display
        contestantsTable.refresh();
        updateStatistics();
    }
    
    @FXML
    private void handleClear() {
        contestants.clear();
        updateStatistics();
        updateStatus("All data cleared");
    }
    
    @FXML
    private void handleBack() {
        try {
            // Navigate back to main menu or previous page
            Stage stage = (Stage) backButton.getScene().getWindow();
            
            // Preserve window state
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Predictor.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root, currentWidth, currentHeight));
            
            // Restore window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to return to previous page: " + e.getMessage());
        }
    }
    
    private void updateStatistics() {
        if (contestants.isEmpty()) {
            totalContestantsLabel.setText("0");
            avgDeltaLabel.setText("0");
            maxGainLabel.setText("0");
            maxLossLabel.setText("0");
            return;
        }
        
        totalContestantsLabel.setText(String.valueOf(contestants.size()));
        
        double avgDelta = contestants.stream()
                .mapToInt(Contestant::getDelta)
                .average()
                .orElse(0.0);
        avgDeltaLabel.setText(String.format("%+.1f", avgDelta));
        
        int maxGain = contestants.stream()
                .mapToInt(Contestant::getDelta)
                .max()
                .orElse(0);
        maxGainLabel.setText("+" + maxGain);
        
        int maxLoss = contestants.stream()
                .mapToInt(Contestant::getDelta)
                .min()
                .orElse(0);
        maxLossLabel.setText(String.valueOf(maxLoss));
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
