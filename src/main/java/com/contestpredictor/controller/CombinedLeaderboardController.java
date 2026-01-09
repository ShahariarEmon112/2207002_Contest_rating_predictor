package com.contestpredictor.controller;

import com.contestpredictor.data.LeaderboardDatabase;
import com.contestpredictor.model.CombinedLeaderboardEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CombinedLeaderboardController {
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private TableView<CombinedLeaderboardEntry> combinedTable;
    
    @FXML
    private TableColumn<CombinedLeaderboardEntry, Integer> overallRankColumn;
    @FXML
    private TableColumn<CombinedLeaderboardEntry, String> usernameColumn;
    @FXML
    private TableColumn<CombinedLeaderboardEntry, Integer> totalSolvesColumn;
    @FXML
    private TableColumn<CombinedLeaderboardEntry, Integer> totalPenaltyColumn;
    @FXML
    private TableColumn<CombinedLeaderboardEntry, Integer> contestsParticipatedColumn;
    
    private LeaderboardDatabase leaderboardDB;
    
    @FXML
    public void initialize() {
        leaderboardDB = LeaderboardDatabase.getInstance();
        setupTableColumns();
    }
    
    private void setupTableColumns() {
        overallRankColumn.setCellValueFactory(new PropertyValueFactory<>("overallRank"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        totalSolvesColumn.setCellValueFactory(new PropertyValueFactory<>("totalSolves"));
        totalPenaltyColumn.setCellValueFactory(new PropertyValueFactory<>("totalPenalty"));
        contestsParticipatedColumn.setCellValueFactory(new PropertyValueFactory<>("contestsParticipated"));
    }
    
    public void loadCombinedLeaderboard() {
        // Refresh the combined leaderboard calculation before loading
        leaderboardDB.refreshCombinedLeaderboard();
        
        List<CombinedLeaderboardEntry> combined = leaderboardDB.getCombinedLeaderboard();
        
        if (combined.isEmpty()) {
            titleLabel.setText("📊 Overall Leaderboard (No Data - Finalize contest standings to update)");
        } else {
            titleLabel.setText("🏆 Overall Leaderboard - Top Performers Across All Contests (" + combined.size() + " participants)");
        }
        
        ObservableList<CombinedLeaderboardEntry> observableCombined = FXCollections.observableArrayList(combined);
        combinedTable.setItems(observableCombined);
    }
}
