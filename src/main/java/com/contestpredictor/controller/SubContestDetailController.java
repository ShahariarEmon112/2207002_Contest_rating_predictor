package com.contestpredictor.controller;

import com.contestpredictor.data.DatabaseManager;
import com.contestpredictor.model.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Sub-Contest Detail view
 * Shows standings for a specific Codeforces contest within a Selection Contest
 */
public class SubContestDetailController {

    // Header
    @FXML private Label headerLabel;
    @FXML private Label userNameLabel;
    
    // Contest Info
    @FXML private Label contestNameLabel;
    @FXML private Label parentContestLabel;
    @FXML private Label cfIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label durationLabel;
    @FXML private Label weightLabel;
    @FXML private Label participantsLabel;
    @FXML private Label statusLabel;
    
    // Your Result
    @FXML private VBox yourResultSection;
    @FXML private Label yourRankLabel;
    @FXML private Label yourPointsLabel;
    @FXML private Label yourRatingLabel;
    @FXML private Label yourWeightedLabel;
    
    // Standings
    @FXML private TextField searchField;
    @FXML private TableView<StandingRow> standingsTable;
    @FXML private TableColumn<StandingRow, Integer> rankCol;
    @FXML private TableColumn<StandingRow, String> handleCol;
    @FXML private TableColumn<StandingRow, String> usernameCol;
    @FXML private TableColumn<StandingRow, Double> pointsCol;
    @FXML private TableColumn<StandingRow, Integer> penaltyCol;
    @FXML private TableColumn<StandingRow, Double> ratingCol;
    @FXML private TableColumn<StandingRow, Double> weightedCol;
    
    // Status
    @FXML private Label bottomStatusLabel;
    @FXML private Label fetchStatusLabel;
    
    private User currentUser;
    private SubContest currentSubContest;
    private SelectionContest parentContest;
    private DatabaseManager dbManager;
    private ObservableList<StandingRow> allStandings = FXCollections.observableArrayList();
    
    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        setupStandingsTable();
        setupSearch();
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText("User: " + user.getFullName());
        }
    }
    
    public void setSubContest(SubContest subContest) {
        this.currentSubContest = subContest;
        loadSubContestDetails();
    }
    
    public void setSelectionContest(SelectionContest contest) {
        this.parentContest = contest;
        if (contest != null) {
            parentContestLabel.setText("Part of: " + contest.getName() + " (" + contest.getContestCode() + ")");
        }
    }
    
    private void setupStandingsTable() {
        rankCol.setCellValueFactory(data -> data.getValue().rankProperty().asObject());
        handleCol.setCellValueFactory(data -> data.getValue().handleProperty());
        usernameCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        pointsCol.setCellValueFactory(data -> data.getValue().pointsProperty().asObject());
        penaltyCol.setCellValueFactory(data -> data.getValue().penaltyProperty().asObject());
        ratingCol.setCellValueFactory(data -> data.getValue().ratingProperty().asObject());
        weightedCol.setCellValueFactory(data -> data.getValue().weightedProperty().asObject());
        
        // Color code the handle column based on rating
        handleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Highlight current user
                    if (currentUser != null && item.equals(currentUser.getCodeforcesHandle())) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                standingsTable.setItems(allStandings);
            } else {
                FilteredList<StandingRow> filtered = new FilteredList<>(allStandings, row -> {
                    String lower = newVal.toLowerCase();
                    return row.getHandle().toLowerCase().contains(lower) ||
                           row.getUsername().toLowerCase().contains(lower);
                });
                standingsTable.setItems(filtered);
            }
        });
    }
    
    private void loadSubContestDetails() {
        if (currentSubContest == null) return;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        String contestName = currentSubContest.getContestName();
        if (contestName == null || contestName.isEmpty()) {
            contestName = "Codeforces Contest " + currentSubContest.getCodeforcesContestId();
        }
        
        headerLabel.setText(contestName);
        contestNameLabel.setText(contestName);
        cfIdLabel.setText(String.valueOf(currentSubContest.getCodeforcesContestId()));
        
        if (currentSubContest.getContestDate() != null) {
            dateLabel.setText(currentSubContest.getContestDate().format(formatter));
        } else {
            dateLabel.setText("--");
        }
        
        int durationMinutes = currentSubContest.getDurationSeconds() / 60;
        if (durationMinutes > 0) {
            durationLabel.setText(durationMinutes + " min");
        } else {
            durationLabel.setText("--");
        }
        
        weightLabel.setText(String.format("%.1f", currentSubContest.getWeight()));
        
        String phase = currentSubContest.getPhase();
        statusLabel.setText(phase != null ? phase : "FINISHED");
        if ("FINISHED".equals(phase)) {
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
        
        // Load standings
        loadStandings();
        
        // Load user's result
        loadUserResult();
        
        bottomStatusLabel.setText("Loaded sub-contest: CF " + currentSubContest.getCodeforcesContestId());
    }
    
    private void loadStandings() {
        allStandings.clear();
        int participantCount = 0;
        
        try {
            // Get all contest results for this sub-contest
            List<ContestResult> results = dbManager.getContestResultsBySubContest(currentSubContest.getId());
            
            for (ContestResult result : results) {
                // Use registration to get user
                Registration reg = dbManager.getRegistrationById(result.getRegistrationId());
                String username = reg != null ? reg.getUsername() : "Unknown";
                String handle = result.getCodeforcesHandle();
                
                double weighted = result.getCalculatedRating() * currentSubContest.getWeight();
                
                allStandings.add(new StandingRow(
                    result.getCfRank(),
                    handle != null ? handle : "--",
                    username,
                    result.getCfPoints(),
                    result.getCfPenalty(),
                    result.getCalculatedRating(),
                    weighted
                ));
                participantCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            fetchStatusLabel.setText("Error loading standings");
        }
        
        standingsTable.setItems(allStandings);
        participantsLabel.setText(String.valueOf(participantCount));
        
        if (participantCount == 0) {
            fetchStatusLabel.setText("No standings data available yet");
        }
    }
    
    private void loadUserResult() {
        if (currentUser == null || currentSubContest == null) {
            yourResultSection.setVisible(false);
            yourResultSection.setManaged(false);
            return;
        }
        
        try {
            ContestResult userResult = dbManager.getContestResult(currentUser.getId(), currentSubContest.getId());
            
            if (userResult != null) {
                yourResultSection.setVisible(true);
                yourResultSection.setManaged(true);
                
                yourRankLabel.setText(String.valueOf(userResult.getCfRank()));
                yourPointsLabel.setText(String.format("%.0f", userResult.getCfPoints()));
                yourRatingLabel.setText(String.format("%.0f", userResult.getCalculatedRating()));
                
                double weighted = userResult.getCalculatedRating() * currentSubContest.getWeight();
                yourWeightedLabel.setText(String.format("%.1f", weighted));
            } else {
                yourResultSection.setVisible(false);
                yourResultSection.setManaged(false);
            }
        } catch (Exception e) {
            yourResultSection.setVisible(false);
            yourResultSection.setManaged(false);
        }
    }
    
    @FXML
    private void handleOpenCF() {
        try {
            String url = "https://codeforces.com/contest/" + currentSubContest.getCodeforcesContestId();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                showAlert("Cannot Open Browser", "Please visit: " + url);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to open Codeforces: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadStandings();
        fetchStatusLabel.setText("Standings refreshed");
    }
    
    @FXML
    private void handleBack() {
        try {
            if (parentContest != null) {
                // Go back to selection contest detail
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SelectionContestDetail.fxml"));
                Parent root = loader.load();
                
                SelectionContestDetailController controller = loader.getController();
                controller.setUser(currentUser);
                controller.setContest(parentContest);
                
                Stage stage = (Stage) headerLabel.getScene().getWindow();
                Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
                
                try {
                    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                } catch (Exception e) {
                    System.err.println("Could not load CSS: " + e.getMessage());
                }
                
                stage.setScene(scene);
                stage.setTitle(parentContest.getName() + " - Contest Details");
            } else {
                // Fallback based on user role
                String dashboardFxml;
                String title;
                
                if (currentUser != null && currentUser.isSetter()) {
                    dashboardFxml = "/fxml/SetterDashboard.fxml";
                    title = "Setter Dashboard - Contest Rating Predictor";
                } else {
                    dashboardFxml = "/fxml/UserDashboard.fxml";
                    title = "Contestant Dashboard - Contest Rating Predictor";
                }
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource(dashboardFxml));
                Parent root = loader.load();
                
                if (currentUser != null) {
                    if (currentUser.isSetter()) {
                        SetterDashboardController controller = loader.getController();
                        controller.setUser(currentUser);
                    } else {
                        UserDashboardController controller = loader.getController();
                        controller.setUser(currentUser);
                    }
                }
                
                Stage stage = (Stage) headerLabel.getScene().getWindow();
                Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
                
                try {
                    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                } catch (Exception e) {
                    System.err.println("Could not load CSS: " + e.getMessage());
                }
                
                stage.setScene(scene);
                stage.setTitle(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Row class
    public static class StandingRow {
        private final SimpleIntegerProperty rank;
        private final SimpleStringProperty handle;
        private final SimpleStringProperty username;
        private final SimpleDoubleProperty points;
        private final SimpleIntegerProperty penalty;
        private final SimpleDoubleProperty rating;
        private final SimpleDoubleProperty weighted;
        
        public StandingRow(int rank, String handle, String username, double points, int penalty, double rating, double weighted) {
            this.rank = new SimpleIntegerProperty(rank);
            this.handle = new SimpleStringProperty(handle);
            this.username = new SimpleStringProperty(username);
            this.points = new SimpleDoubleProperty(points);
            this.penalty = new SimpleIntegerProperty(penalty);
            this.rating = new SimpleDoubleProperty(rating);
            this.weighted = new SimpleDoubleProperty(weighted);
        }
        
        public String getHandle() { return handle.get(); }
        public String getUsername() { return username.get(); }
        
        public SimpleIntegerProperty rankProperty() { return rank; }
        public SimpleStringProperty handleProperty() { return handle; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleDoubleProperty pointsProperty() { return points; }
        public SimpleIntegerProperty penaltyProperty() { return penalty; }
        public SimpleDoubleProperty ratingProperty() { return rating; }
        public SimpleDoubleProperty weightedProperty() { return weighted; }
    }
}
