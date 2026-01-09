package com.contestpredictor.controller;

import com.contestpredictor.data.LeaderboardDatabase;
import com.contestpredictor.model.LeaderboardContest;
import com.contestpredictor.model.CombinedLeaderboardEntry;
import com.contestpredictor.model.LeaderboardEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class UserLeaderboardController {
    
    @FXML
    private ListView<LeaderboardContest> availableContestsListView;
    
    @FXML
    private Label myRegistrationsLabel;
    
    @FXML
    private ListView<LeaderboardContest> registeredContestsListView;
    
    @FXML
    private TableView<LeaderboardEntry> contestStandingsTable;
    
    @FXML
    private TableColumn<LeaderboardEntry, Integer> rankColumn;
    @FXML
    private TableColumn<LeaderboardEntry, String> usernameColumn;
    @FXML
    private TableColumn<LeaderboardEntry, Integer> solveCountColumn;
    @FXML
    private TableColumn<LeaderboardEntry, Integer> penaltyColumn;
    
    @FXML
    private TableView<CombinedLeaderboardEntry> combinedLeaderboardTable;
    
    @FXML
    private TableColumn<CombinedLeaderboardEntry, Integer> overallRankColumn;
    @FXML
    private TableColumn<CombinedLeaderboardEntry, String> userColumn;
    @FXML
    private TableColumn<CombinedLeaderboardEntry, Integer> totalSolvesColumn;
    @FXML
    private TableColumn<CombinedLeaderboardEntry, Integer> totalPenaltyColumn;
    @FXML
    private TableColumn<CombinedLeaderboardEntry, Integer> contestsParticipatedColumn;
    
    @FXML
    private Label selectedContestLabel;
    
    @FXML
    private Button registerButton;
    @FXML
    private Button unregisterButton;
    @FXML
    private Button viewStandingsButton;
    @FXML
    private Button backButton;
    @FXML
    private ComboBox<LeaderboardContest> contestStandingsComboBox;
    @FXML
    private TabPane tabPane;
    
    private LeaderboardDatabase leaderboardDB;
    private String currentUsername;
    private LeaderboardContest selectedContest;

    @FXML
    public void initialize() {
        leaderboardDB = LeaderboardDatabase.getInstance();
        setupTableColumns();
        setupListViewCellFactories();
        setupComboBox();
        
        // Note: loadAvailableContests, loadRegisteredContests, loadCombinedLeaderboard
        // are called in setCurrentUsername() after username is set
        
        // Set up contest selection for available contests
        availableContestsListView.setOnMouseClicked(event -> {
            selectedContest = availableContestsListView.getSelectionModel().getSelectedItem();
            if (selectedContest != null) {
                selectedContestLabel.setText("Selected: " + selectedContest.getContestName());
                updateRegisterButtonState();
            }
        });
        
        // Set up contest selection for registered contests to load standings
        registeredContestsListView.setOnMouseClicked(event -> {
            LeaderboardContest selected = registeredContestsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                loadContestStandings(selected.getContestId());
            }
        });
    }

    private void setupComboBox() {
        // Setup ComboBox to display contest names
        contestStandingsComboBox.setCellFactory(param -> new ListCell<LeaderboardContest>() {
            @Override
            protected void updateItem(LeaderboardContest contest, boolean empty) {
                super.updateItem(contest, empty);
                if (empty || contest == null) {
                    setText(null);
                } else {
                    setText(contest.getContestName());
                }
            }
        });
        
        // Setup button cell to show selected contest name
        contestStandingsComboBox.setButtonCell(new ListCell<LeaderboardContest>() {
            @Override
            protected void updateItem(LeaderboardContest contest, boolean empty) {
                super.updateItem(contest, empty);
                if (empty || contest == null) {
                    setText(null);
                } else {
                    setText(contest.getContestName());
                }
            }
        });
        
        // Load registered contests in ComboBox
        contestStandingsComboBox.setOnAction(event -> {
            LeaderboardContest selected = contestStandingsComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                loadContestStandings(selected.getContestId());
            }
        });
    }

    private void setupListViewCellFactories() {
        // Display contest names instead of toString()
        availableContestsListView.setCellFactory(param -> new ListCell<LeaderboardContest>() {
            @Override
            protected void updateItem(LeaderboardContest contest, boolean empty) {
                super.updateItem(contest, empty);
                if (empty || contest == null) {
                    setText(null);
                } else {
                    setText(contest.getContestName() + " (" + contest.getContestId() + ")");
                }
            }
        });
        
        registeredContestsListView.setCellFactory(param -> new ListCell<LeaderboardContest>() {
            @Override
            protected void updateItem(LeaderboardContest contest, boolean empty) {
                super.updateItem(contest, empty);
                if (empty || contest == null) {
                    setText(null);
                } else {
                    setText(contest.getContestName() + " (" + contest.getContestId() + ")");
                }
            }
        });
    }

    private void setupTableColumns() {
        // Contest standings columns
        rankColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRank()));
        usernameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getUsername()));
        solveCountColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSolveCount()));
        penaltyColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalPenalty()));
        
        // Combined leaderboard columns
        overallRankColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOverallRank()));
        userColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getUsername()));
        totalSolvesColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalSolves()));
        totalPenaltyColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalPenalty()));
        contestsParticipatedColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getContestsParticipated()));
    }

    private void loadAvailableContests() {
        List<LeaderboardContest> contests = leaderboardDB.getAllLeaderboardContests();
        ObservableList<LeaderboardContest> observableContests = FXCollections.observableArrayList(contests);
        availableContestsListView.setItems(observableContests);
    }

    private void loadRegisteredContests() {
        List<LeaderboardContest> allContests = leaderboardDB.getAllLeaderboardContests();
        ObservableList<LeaderboardContest> registeredContests = FXCollections.observableArrayList();
        
        for (LeaderboardContest contest : allContests) {
            if (leaderboardDB.isUserRegisteredForContest(contest.getContestId(), currentUsername)) {
                registeredContests.add(contest);
            }
        }
        
        registeredContestsListView.setItems(registeredContests);
        contestStandingsComboBox.setItems(registeredContests);
        myRegistrationsLabel.setText("My Registrations (" + registeredContests.size() + ")");
        
        // Auto-select first contest if available
        if (!registeredContests.isEmpty()) {
            contestStandingsComboBox.getSelectionModel().selectFirst();
            loadContestStandings(registeredContests.get(0).getContestId());
        }
    }

    private void loadCombinedLeaderboard() {
        // Refresh the combined leaderboard calculation before loading
        leaderboardDB.refreshCombinedLeaderboard();
        
        List<CombinedLeaderboardEntry> leaderboard = leaderboardDB.getCombinedLeaderboard();
        ObservableList<CombinedLeaderboardEntry> observableLeaderboard = FXCollections.observableArrayList(leaderboard);
        combinedLeaderboardTable.setItems(observableLeaderboard);
    }

    private void updateRegisterButtonState() {
        if (selectedContest != null) {
            boolean isRegistered = leaderboardDB.isUserRegisteredForContest(selectedContest.getContestId(), currentUsername);
            registerButton.setDisable(isRegistered);
            unregisterButton.setDisable(!isRegistered);
        }
    }

    @FXML
    private void handleRegisterForContest() {
        if (selectedContest == null) {
            showError("Please select a contest first");
            return;
        }

        if (leaderboardDB.registerUserForLeaderboardContest(selectedContest.getContestId(), currentUsername)) {
            showSuccess("Successfully registered for " + selectedContest.getContestName());
            loadRegisteredContests();
            updateRegisterButtonState();
        } else {
            showError("Failed to register for contest");
        }
    }

    @FXML
    private void handleUnregisterFromContest() {
        if (selectedContest == null) {
            showError("Please select a contest first");
            return;
        }

        if (leaderboardDB.unregisterUserFromLeaderboardContest(selectedContest.getContestId(), currentUsername)) {
            showSuccess("Successfully unregistered from " + selectedContest.getContestName());
            loadRegisteredContests();
            updateRegisterButtonState();
        } else {
            showError("Failed to unregister from contest");
        }
    }

    @FXML
    private void handleViewStandings() {
        if (selectedContest == null) {
            showError("Please select a contest first");
            return;
        }

        // Switch to Contest Standings tab (index 2)
        if (tabPane != null) {
            tabPane.getSelectionModel().select(2);
        }
        
        // Select the contest in the combo box
        if (contestStandingsComboBox != null) {
            contestStandingsComboBox.getSelectionModel().select(selectedContest);
        }
        
        // Load the standings for the selected contest
        loadContestStandings(selectedContest.getContestId());
    }

    private void loadContestStandings(String contestId) {
        List<LeaderboardEntry> standings = leaderboardDB.getContestStandings(contestId);
        ObservableList<LeaderboardEntry> observableStandings = FXCollections.observableArrayList(standings);
        contestStandingsTable.setItems(observableStandings);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Profile.fxml"));
            if (loader.getLocation() == null) {
                showError("Cannot find Profile.fxml file");
                return;
            }
            
            Parent root = loader.load();
            
            ProfileController controller = loader.getController();
            controller.setUser(new com.contestpredictor.model.User(currentUsername, "", 0, 0, currentUsername));
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            
            String stylesheet = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);
            
            stage.setTitle("Profile");
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
            showError("Error navigating back: " + e.getMessage());
        }
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        loadRegisteredContests();
        loadAvailableContests();
        loadCombinedLeaderboard();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
