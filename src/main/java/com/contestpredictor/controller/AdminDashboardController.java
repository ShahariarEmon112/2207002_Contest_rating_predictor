package com.contestpredictor.controller;

import com.contestpredictor.data.ContestDatabase;
import com.contestpredictor.model.Admin;
import com.contestpredictor.model.Contest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {
    
    @FXML private Label adminNameLabel;
    @FXML private Button logoutButton;
    
    // Create Contest Section
    @FXML private TextField contestIdField;
    @FXML private TextField contestNameField;
    @FXML private DatePicker contestDatePicker;
    @FXML private TextField durationField;
    @FXML private TextField maxParticipantsField;
    @FXML private Button createContestButton;
    @FXML private Label contestStatusLabel;
    
    // Contests Table
    @FXML private TableView<Contest> contestsTable;
    @FXML private TableColumn<Contest, String> contestIdColumn;
    @FXML private TableColumn<Contest, String> contestNameColumn;
    @FXML private TableColumn<Contest, String> dateTimeColumn;
    @FXML private TableColumn<Contest, Integer> durationColumn;
    @FXML private TableColumn<Contest, Integer> registrationsColumn;
    @FXML private TableColumn<Contest, String> statusColumn;
    @FXML private TableColumn<Contest, Void> actionsColumn;
    
    // Registrations Section
    @FXML private ComboBox<String> contestSelector;
    @FXML private Label registrationCountLabel;
    @FXML private TableView<?> registrationsTable;
    @FXML private TableColumn<?, String> usernameColumn;
    @FXML private TableColumn<?, String> registeredAtColumn;
    @FXML private TableColumn<?, Void> userActionsColumn;
    
    // Statistics
    @FXML private Label totalContestsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeContestsLabel;
    @FXML private Label totalRegistrationsLabel;
    
    private Admin currentAdmin;
    
    @FXML
    public void initialize() {
        // Set default values
        maxParticipantsField.setText("1000");
        contestDatePicker.setValue(LocalDate.now());
        
        // Initialize table columns
        setupTableColumns();
        
        loadContests();
        updateStatistics();
        
        // Set admin name if already set
        if (currentAdmin != null && adminNameLabel != null) {
            adminNameLabel.setText("Welcome, " + currentAdmin.getFullName());
        }
    }
    
    private void setupTableColumns() {
        // Setup contests table columns
        if (contestIdColumn != null) {
            contestIdColumn.setCellValueFactory(new PropertyValueFactory<>("contestId"));
        }
        if (contestNameColumn != null) {
            contestNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if (dateTimeColumn != null) {
            dateTimeColumn.setCellValueFactory(cellData -> {
                Contest contest = cellData.getValue();
                String formatted = contest.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            });
        }
        if (durationColumn != null) {
            durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        }
        if (registrationsColumn != null) {
            registrationsColumn.setCellValueFactory(cellData -> {
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            });
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(cellData -> {
                Contest contest = cellData.getValue();
                String status = contest.isPast() ? "Past" : "Upcoming";
                return new javafx.beans.property.SimpleStringProperty(status);
            });
        }
    }
    
    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        if (adminNameLabel != null) {
            adminNameLabel.setText("Welcome, " + admin.getFullName());
        }
    }
    
    @FXML
    private void handleCreateContest() {
        try {
            String contestId = contestIdField.getText().trim();
            String contestName = contestNameField.getText().trim();
            LocalDate date = contestDatePicker.getValue();
            int duration = Integer.parseInt(durationField.getText().trim());
            int maxParticipants = Integer.parseInt(maxParticipantsField.getText().trim());
            
            // Validation
            if (contestId.isEmpty() || contestName.isEmpty()) {
                showAlert("Validation Error", "Please fill in all required fields");
                return;
            }
            
            if (date == null) {
                showAlert("Validation Error", "Please select a date");
                return;
            }
            
            // Create contest with current time
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.now());
            boolean isPast = dateTime.isBefore(LocalDateTime.now());
            
            Contest contest = new Contest(
                contestId,
                contestName,
                dateTime,
                duration,
                isPast,
                currentAdmin.getUsername(),
                maxParticipants
            );
            
            // Save to database
            ContestDatabase contestDB = ContestDatabase.getInstance();
            boolean success = contestDB.saveContestWithAdmin(contest);
            
            if (success) {
                contestStatusLabel.setText("✓ Contest created successfully!");
                contestStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
                handleClearForm();
                loadContests();
                updateStatistics();
            } else {
                showAlert("Error", "Failed to create contest. Contest ID may already exist.");
            }
            
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Duration and Max Participants must be valid numbers");
        } catch (Exception e) {
            showAlert("Error", "Failed to create contest: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearForm() {
        contestIdField.clear();
        contestNameField.clear();
        contestDatePicker.setValue(LocalDate.now());
        durationField.clear();
        maxParticipantsField.setText("1000");
        contestStatusLabel.setText("");
    }
    
    @FXML
    private void handleViewRegistrations() {
        // Placeholder - will be implemented with contest registration
        showInfo("Coming Soon", "Contest registration viewing will be available soon");
    }
    
    @FXML
    private void handleRefreshStats() {
        updateStatistics();
        showInfo("Refreshed", "Statistics updated successfully");
    }
    
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Preserve window state
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            Scene scene = new Scene(root, currentWidth, currentHeight);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login - Contest Rating Predictor");
            
            // Restore window state
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }
    
    private void loadContests() {
        try {
            ContestDatabase contestDB = ContestDatabase.getInstance();
            var contests = contestDB.getAllContests();
            
            // Populate the contests table
            if (contestsTable != null) {
                ObservableList<Contest> contestList = FXCollections.observableArrayList(contests);
                contestsTable.setItems(contestList);
            }
            
            // Update total contests label
            if (totalContestsLabel != null) {
                totalContestsLabel.setText(String.valueOf(contests.size()));
            }
            
        } catch (Exception e) {
            System.err.println("Error loading contests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStatistics() {
        try {
            ContestDatabase contestDB = ContestDatabase.getInstance();
            var allContests = contestDB.getAllContests();
            
            totalContestsLabel.setText(String.valueOf(allContests.size()));
            
            long activeCount = allContests.stream()
                .filter(c -> !c.isPast())
                .count();
            activeContestsLabel.setText(String.valueOf(activeCount));
            
            // These would need proper database queries
            totalUsersLabel.setText("0"); // Placeholder
            totalRegistrationsLabel.setText("0"); // Placeholder
            
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
