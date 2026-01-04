package com.contestpredictor.controller;

import com.contestpredictor.data.DatabaseManager;
import com.contestpredictor.model.SelectionContest;
import com.contestpredictor.model.SubContest;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for Add Sub-Contest dialog
 */
public class AddSubContestController {

    @FXML private TextField cfLinkField;
    @FXML private TextField weightField;
    @FXML private TextArea descriptionField;
    @FXML private Label messageLabel;
    
    private SelectionContest selectionContest;
    private DatabaseManager dbManager;
    private Runnable onSubContestAdded;
    
    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
    }
    
    public void setSelectionContest(SelectionContest contest) {
        this.selectionContest = contest;
    }
    
    public void setOnSubContestAdded(Runnable callback) {
        this.onSubContestAdded = callback;
    }
    
    @FXML
    private void handleAdd() {
        String linkOrId = cfLinkField.getText().trim();
        String weightStr = weightField.getText().trim();
        String description = descriptionField.getText().trim();
        
        if (linkOrId.isEmpty()) {
            showMessage("Please enter a Codeforces contest link or ID", false);
            return;
        }
        
        // Extract contest ID
        int cfContestId = extractCodeforcesContestId(linkOrId);
        if (cfContestId <= 0) {
            showMessage("Invalid Codeforces contest link/ID. Examples:\n• https://codeforces.com/contest/1234\n• 1234", false);
            return;
        }
        
        // Parse weight
        double weight = 1.0;
        try {
            weight = Double.parseDouble(weightStr);
            if (weight <= 0) {
                showMessage("Weight must be greater than 0", false);
                return;
            }
        } catch (NumberFormatException e) {
            showMessage("Invalid weight. Please enter a number (e.g., 1.0, 1.5, 2.0)", false);
            return;
        }
        
        // Check if already exists
        if (dbManager.subContestExists(selectionContest.getId(), cfContestId)) {
            showMessage("This Codeforces contest is already added to this selection contest", false);
            return;
        }
        
        // Create sub-contest
        SubContest subContest = new SubContest();
        subContest.setSelectionContestId(selectionContest.getId());
        subContest.setCodeforcesContestId(cfContestId);
        subContest.setContestName("CF Contest " + cfContestId); // Will be updated when fetched
        subContest.setWeight(weight);
        subContest.setPhase("PENDING");
        
        // Note: Description could be stored if we add a column for it
        
        boolean success = dbManager.addSubContest(subContest);
        
        if (success) {
            showMessage("✅ Sub-contest added successfully!", true);
            
            // Callback to refresh parent
            if (onSubContestAdded != null) {
                onSubContestAdded.run();
            }
            
            // Close dialog after short delay
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    javafx.application.Platform.runLater(this::closeDialog);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showMessage("❌ Failed to add sub-contest. It may already exist.", false);
        }
    }
    
    private int extractCodeforcesContestId(String input) {
        // Try to extract from URL patterns:
        // https://codeforces.com/contest/1234
        // https://codeforces.com/contest/1234/standings
        // codeforces.com/contest/1234
        Pattern urlPattern = Pattern.compile("codeforces\\.com/contest/(\\d+)");
        Matcher matcher = urlPattern.matcher(input);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        // Try direct number
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) cfLinkField.getScene().getWindow();
        stage.close();
    }
    
    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + (success ? "#27ae60" : "#e74c3c") + ";");
    }
}
