package com.contestpredictor.controller;

import com.contestpredictor.data.DatabaseManager;
import com.contestpredictor.model.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Selection Contest Detail view
 * Shows sub-contests and allows registration
 */
public class SelectionContestDetailController {

    // Header
    @FXML private Label headerLabel;
    @FXML private Label userNameLabel;
    
    // Contest Info
    @FXML private Label contestNameLabel;
    @FXML private Label contestCodeLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label subContestCountLabel;
    @FXML private Label participantCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button registerBtn;
    @FXML private Label registerStatusLabel;
    
    // Sub-Contests
    @FXML private Label totalWeightLabel;
    @FXML private VBox subContestsContainer;
    @FXML private TableView<SubContestRow> subContestsTable;
    @FXML private TableColumn<SubContestRow, Integer> cfIdCol;
    @FXML private TableColumn<SubContestRow, String> nameCol;
    @FXML private TableColumn<SubContestRow, String> dateCol;
    @FXML private TableColumn<SubContestRow, String> weightCol;
    @FXML private TableColumn<SubContestRow, String> phaseCol;
    @FXML private TableColumn<SubContestRow, Void> actionsCol;
    
    // Your Results
    @FXML private VBox yourResultsSection;
    @FXML private Label yourRankLabel;
    @FXML private Label yourRatingLabel;
    @FXML private Label yourChangeLabel;
    @FXML private TableView<ResultRow> yourResultsTable;
    @FXML private TableColumn<ResultRow, String> resSubNameCol;
    @FXML private TableColumn<ResultRow, Integer> resRankCol;
    @FXML private TableColumn<ResultRow, Double> resPointsCol;
    @FXML private TableColumn<ResultRow, Double> resWeightCol;
    @FXML private TableColumn<ResultRow, Double> resWeightedCol;
    
    // Status
    @FXML private Label bottomStatusLabel;
    
    private User currentUser;
    private SelectionContest currentContest;
    private DatabaseManager dbManager;
    private boolean isRegistered = false;
    
    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        setupSubContestsTable();
        setupResultsTable();
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText("User: " + user.getFullName());
        }
    }
    
    public void setContest(SelectionContest contest) {
        this.currentContest = contest;
        loadContestDetails();
    }
    
    private void setupSubContestsTable() {
        cfIdCol.setCellValueFactory(data -> data.getValue().cfIdProperty().asObject());
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        dateCol.setCellValueFactory(data -> data.getValue().dateProperty());
        weightCol.setCellValueFactory(data -> data.getValue().weightProperty());
        phaseCol.setCellValueFactory(data -> data.getValue().phaseProperty());
        
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View Standings");
            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                viewBtn.setOnAction(e -> {
                    SubContestRow row = getTableView().getItems().get(getIndex());
                    handleViewSubContestStandings(row.getId(), row.getCfId());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });
        
        // Double-click to view standings
        subContestsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SubContestRow selected = subContestsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleViewSubContestStandings(selected.getId(), selected.getCfId());
                }
            }
        });
    }
    
    private void setupResultsTable() {
        resSubNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        resRankCol.setCellValueFactory(data -> data.getValue().rankProperty().asObject());
        resPointsCol.setCellValueFactory(data -> data.getValue().pointsProperty().asObject());
        resWeightCol.setCellValueFactory(data -> data.getValue().weightProperty().asObject());
        resWeightedCol.setCellValueFactory(data -> data.getValue().weightedProperty().asObject());
    }
    
    private void loadContestDetails() {
        if (currentContest == null) return;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        headerLabel.setText(currentContest.getName());
        contestNameLabel.setText(currentContest.getName());
        contestCodeLabel.setText("Code: " + currentContest.getContestCode());
        
        startDateLabel.setText(currentContest.getStartDate() != null ? 
            currentContest.getStartDate().format(formatter) : "--");
        endDateLabel.setText(currentContest.getEndDate() != null ? 
            currentContest.getEndDate().format(formatter) : "--");
        
        int subCount = dbManager.getSubContestCount(currentContest.getId());
        int participantCount = dbManager.getParticipantCount(currentContest.getId());
        
        subContestCountLabel.setText(String.valueOf(subCount));
        participantCountLabel.setText(String.valueOf(participantCount));
        statusLabel.setText(currentContest.isActive() ? "Active" : "Ended");
        statusLabel.setStyle(currentContest.isActive() ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        if (currentContest.getDescription() != null) {
            descriptionLabel.setText(currentContest.getDescription());
        }
        
        // Check registration status
        checkRegistrationStatus();
        
        // Load sub-contests
        loadSubContests();
        
        // Load user results if registered
        loadUserResults();
        
        bottomStatusLabel.setText("Loaded contest: " + currentContest.getContestCode());
    }
    
    private void checkRegistrationStatus() {
        if (currentUser == null || currentContest == null) return;
        
        Registration reg = dbManager.getRegistration(currentUser.getId(), currentContest.getId());
        isRegistered = (reg != null);
        
        if (isRegistered) {
            registerBtn.setText("Already Registered ✓");
            registerBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
            registerBtn.setDisable(true);
            registerStatusLabel.setText("You are registered for this contest");
            registerStatusLabel.setStyle("-fx-text-fill: #27ae60;");
        } else {
            registerBtn.setText("Register for Contest");
            registerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
            registerBtn.setDisable(false);
            registerStatusLabel.setText("");
        }
    }
    
    private void loadSubContests() {
        ObservableList<SubContestRow> subContests = FXCollections.observableArrayList();
        double totalWeight = 0;
        
        try {
            List<SubContest> subs = dbManager.getSubContestsForSelection(currentContest.getId());
            
            for (SubContest sub : subs) {
                subContests.add(new SubContestRow(
                    sub.getId(),
                    sub.getCodeforcesContestId(),
                    sub.getContestName() != null ? sub.getContestName() : "CF Contest " + sub.getCodeforcesContestId(),
                    sub.getContestDate() != null ? sub.getContestDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "--",
                    String.format("%.1f", sub.getWeight()),
                    sub.getPhase() != null ? sub.getPhase() : "FINISHED"
                ));
                totalWeight += sub.getWeight();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        subContestsTable.setItems(subContests);
        totalWeightLabel.setText(String.format("Total Weight: %.1f", totalWeight));
    }
    
    private void loadUserResults() {
        if (!isRegistered || currentUser == null) {
            yourResultsSection.setVisible(false);
            yourResultsSection.setManaged(false);
            return;
        }
        
        // Get final rating for this user in this contest
        FinalRating finalRating = dbManager.getFinalRatingForUser(currentUser.getId(), currentContest.getId());
        
        if (finalRating != null) {
            yourResultsSection.setVisible(true);
            yourResultsSection.setManaged(true);
            
            yourRankLabel.setText(String.valueOf(finalRating.getOverallRank()));
            yourRatingLabel.setText(String.valueOf((int) finalRating.getFinalRating()));
            
            // Show participation count instead of rating change (since it's not available)
            int participated = finalRating.getContestsParticipated();
            int total = finalRating.getTotalSubContests();
            yourChangeLabel.setText(participated + "/" + total + " contests");
            yourChangeLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            
            // Load detailed results
            loadDetailedResults();
        }
    }
    
    private void loadDetailedResults() {
        ObservableList<ResultRow> results = FXCollections.observableArrayList();
        
        try {
            List<ContestResult> contestResults = dbManager.getContestResultsByUser(currentContest.getId(), currentUser.getId());
            
            for (ContestResult result : contestResults) {
                SubContest sub = dbManager.getSubContestById(result.getSubContestId());
                String subName = sub != null ? sub.getContestName() : "Unknown";
                double weight = sub != null ? sub.getWeight() : 1.0;
                
                results.add(new ResultRow(
                    subName,
                    result.getCfRank(),
                    result.getCfPoints(),
                    weight,
                    result.getCalculatedRating() * weight
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        yourResultsTable.setItems(results);
    }
    
    @FXML
    private void handleRegister() {
        if (currentUser == null) {
            showAlert("Error", "You must be logged in to register");
            return;
        }
        
        if (currentContest == null) {
            showAlert("Error", "No contest selected");
            return;
        }
        
        // Check if CF handle is set
        if (currentUser.getCodeforcesHandle() == null || currentUser.getCodeforcesHandle().isEmpty()) {
            showAlert("CF Handle Required", "Please set your Codeforces handle in your profile before registering for a contest.");
            return;
        }
        
        boolean registered = dbManager.registerUserForContest(currentUser.getId(), currentContest.getId(), currentUser.getCodeforcesHandle());
        
        if (registered) {
            isRegistered = true;
            checkRegistrationStatus();
            showAlert("Success", "You have successfully registered for " + currentContest.getName());
            loadContestDetails();
        } else {
            showAlert("Error", "Failed to register. You may already be registered.");
        }
    }
    
    @FXML
    private void handleViewStandings() {
        // Navigate to user dashboard standings tab for this contest
        try {
            if (currentUser.isSetter()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SetterDashboard.fxml"));
                Parent root = loader.load();
                
                SetterDashboardController controller = loader.getController();
                controller.setUser(currentUser);
                // TODO: Auto-select this contest in standings tab
                
                Stage stage = (Stage) headerLabel.getScene().getWindow();
                Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                
                stage.setScene(scene);
                stage.setTitle("Setter Dashboard - Contest Rating Predictor");
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDashboard.fxml"));
                Parent root = loader.load();
                
                UserDashboardController controller = loader.getController();
                controller.setUser(currentUser);
                // TODO: Auto-select this contest in standings tab
                
                Stage stage = (Stage) headerLabel.getScene().getWindow();
                Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                
                stage.setScene(scene);
                stage.setTitle("Contestant Dashboard - Contest Rating Predictor");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load standings: " + e.getMessage());
        }
    }
    
    private void handleViewSubContestStandings(int subContestId, int cfContestId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubContestDetail.fxml"));
            Parent root = loader.load();
            
            SubContestDetailController controller = loader.getController();
            controller.setUser(currentUser);
            controller.setSubContest(dbManager.getSubContestById(subContestId));
            controller.setSelectionContest(currentContest);
            
            Stage stage = (Stage) headerLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Sub-Contest Standings - CF " + cfContestId);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load sub-contest details: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBack() {
        try {
            // Navigate based on user role
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
    
    // Row classes
    public static class SubContestRow {
        private final int id;
        private final SimpleIntegerProperty cfId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty date;
        private final SimpleStringProperty weight;
        private final SimpleStringProperty phase;
        
        public SubContestRow(int id, int cfId, String name, String date, String weight, String phase) {
            this.id = id;
            this.cfId = new SimpleIntegerProperty(cfId);
            this.name = new SimpleStringProperty(name);
            this.date = new SimpleStringProperty(date);
            this.weight = new SimpleStringProperty(weight);
            this.phase = new SimpleStringProperty(phase);
        }
        
        public int getId() { return id; }
        public int getCfId() { return cfId.get(); }
        public SimpleIntegerProperty cfIdProperty() { return cfId; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty weightProperty() { return weight; }
        public SimpleStringProperty phaseProperty() { return phase; }
    }
    
    public static class ResultRow {
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty rank;
        private final SimpleDoubleProperty points;
        private final SimpleDoubleProperty weight;
        private final SimpleDoubleProperty weighted;
        
        public ResultRow(String name, int rank, double points, double weight, double weighted) {
            this.name = new SimpleStringProperty(name);
            this.rank = new SimpleIntegerProperty(rank);
            this.points = new SimpleDoubleProperty(points);
            this.weight = new SimpleDoubleProperty(weight);
            this.weighted = new SimpleDoubleProperty(weighted);
        }
        
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleIntegerProperty rankProperty() { return rank; }
        public SimpleDoubleProperty pointsProperty() { return points; }
        public SimpleDoubleProperty weightProperty() { return weight; }
        public SimpleDoubleProperty weightedProperty() { return weighted; }
    }
}
