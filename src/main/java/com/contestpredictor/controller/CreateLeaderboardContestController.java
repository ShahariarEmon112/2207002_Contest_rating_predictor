package com.contestpredictor.controller;

import com.contestpredictor.data.LeaderboardDatabase;
import com.contestpredictor.model.LeaderboardContest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class CreateLeaderboardContestController {
    
    @FXML
    private TextField contestNameField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private DatePicker startDatePicker;
    
    @FXML
    private DatePicker endDatePicker;
    
    @FXML
    private Spinner<Integer> maxProblemsSpinner;
    
    @FXML
    private Button createButton;
    
    @FXML
    private Button cancelButton;
    
    private LeaderboardDatabase leaderboardDB;
    private String adminUsername;
    private AdminManageLeaderboardController parentController;

    @FXML
    public void initialize() {
        leaderboardDB = LeaderboardDatabase.getInstance();
        
        // Initialize spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 5);
        maxProblemsSpinner.setValueFactory(valueFactory);
    }

    @FXML
    private void handleCreate() {
        String contestName = contestNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        
        if (contestName.isEmpty()) {
            showError("Please enter contest name");
            return;
        }

        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showError("Please select start and end dates");
            return;
        }

        LocalDateTime startDateTime = startDatePicker.getValue().atStartOfDay();
        LocalDateTime endDateTime = endDatePicker.getValue().atTime(23, 59, 59);
        
        if (startDateTime.isAfter(endDateTime)) {
            showError("Start date must be before end date");
            return;
        }

        int maxProblems = maxProblemsSpinner.getValue();
        String contestId = "LBC_" + System.currentTimeMillis();

        LeaderboardContest contest = new LeaderboardContest(
            contestId, contestName, description, startDateTime, endDateTime, maxProblems, adminUsername
        );

        if (leaderboardDB.createLeaderboardContest(contest)) {
            showSuccess("Leaderboard contest created successfully!");
            if (parentController != null) {
                parentController.refreshContests();
            }
            closeWindow();
        } else {
            showError("Failed to create leaderboard contest");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void setAdminUsername(String username) {
        this.adminUsername = username;
    }

    public void setParentController(AdminManageLeaderboardController controller) {
        this.parentController = controller;
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
