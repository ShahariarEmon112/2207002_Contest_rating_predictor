package com.contestpredictor.controller;

import com.contestpredictor.data.DatabaseManager;
import com.contestpredictor.data.UserDatabase;
import com.contestpredictor.model.SelectionContest;
import com.contestpredictor.model.User;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SetterDashboardController {
    // Header
    @FXML private Label userNameLabel;
    @FXML private Label statusLabel;
    
    // My Contests Tab
    @FXML private TableView<ContestRow> myContestsTable;
    @FXML private TableColumn<ContestRow, String> contestCodeCol;
    @FXML private TableColumn<ContestRow, String> contestNameCol;
    @FXML private TableColumn<ContestRow, Integer> subContestsCol;
    @FXML private TableColumn<ContestRow, Integer> participantsCol;
    @FXML private TableColumn<ContestRow, String> startDateCol;
    @FXML private TableColumn<ContestRow, String> endDateCol;
    @FXML private TableColumn<ContestRow, String> statusCol;
    @FXML private TableColumn<ContestRow, Void> actionsCol;
    
    // Add Sub-Contests Tab
    @FXML private ComboBox<String> selectionContestCombo;
    @FXML private TextField cfContestIdField;
    @FXML private TextField cfContestNameField;
    @FXML private TextField weightField;
    @FXML private Label addSubContestMessage;
    @FXML private TableView<SubContestRow> subContestsTable;
    @FXML private TableColumn<SubContestRow, Integer> subCfIdCol;
    @FXML private TableColumn<SubContestRow, String> subNameCol;
    @FXML private TableColumn<SubContestRow, String> subWeightCol;
    @FXML private TableColumn<SubContestRow, String> subDateCol;
    @FXML private TableColumn<SubContestRow, Void> subActionsCol;
    
    // Standings Tab
    @FXML private ComboBox<String> standingsContestCombo;
    @FXML private Label selectedContestNameLabel;
    @FXML private Label infoSubContestsLabel;
    @FXML private Label infoParticipantsLabel;
    @FXML private Label infoAvgRatingLabel;
    @FXML private TableView<StandingRow> standingsTable;
    @FXML private TableColumn<StandingRow, Integer> standRankCol;
    @FXML private TableColumn<StandingRow, String> standUserCol;
    @FXML private TableColumn<StandingRow, String> standCfHandleCol;
    @FXML private TableColumn<StandingRow, Integer> standFinalRatingCol;
    @FXML private TableColumn<StandingRow, String> standRatingChangeCol;
    
    // Profile Tab
    @FXML private TextField profileFullNameField;
    @FXML private TextField profileUsernameField;
    @FXML private TextField profileEmailField;
    @FXML private Label profileRoleLabel;
    @FXML private Label profileContestsCreatedLabel;
    @FXML private Label profileCreatedAtLabel;
    @FXML private Label profileMessage;
    
    private User currentUser;
    private DatabaseManager dbManager;
    private List<Integer> myContestIds = new ArrayList<>();
    
    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        setupMyContestsTable();
        setupSubContestsTable();
        setupStandingsTable();
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        userNameLabel.setText("Welcome, " + user.getFullName() + " (Setter)");
        loadProfile();
        loadMyContests();
        loadContestCombos();
    }
    
    private void setupMyContestsTable() {
        contestCodeCol.setCellValueFactory(data -> data.getValue().codeProperty());
        contestNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        subContestsCol.setCellValueFactory(data -> data.getValue().subContestsProperty().asObject());
        participantsCol.setCellValueFactory(data -> data.getValue().participantsProperty().asObject());
        startDateCol.setCellValueFactory(data -> data.getValue().startDateProperty());
        endDateCol.setCellValueFactory(data -> data.getValue().endDateProperty());
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());
        
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
                viewBtn.setOnAction(e -> {
                    ContestRow row = getTableView().getItems().get(getIndex());
                    handleViewContest(row.getId());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });
    }
    
    private void setupSubContestsTable() {
        subCfIdCol.setCellValueFactory(data -> data.getValue().cfIdProperty().asObject());
        subNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        subWeightCol.setCellValueFactory(data -> data.getValue().weightProperty());
        subDateCol.setCellValueFactory(data -> data.getValue().dateProperty());
        
        subActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                removeBtn.setOnAction(e -> {
                    SubContestRow row = getTableView().getItems().get(getIndex());
                    handleRemoveSubContest(row.getId());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
    }
    
    private void setupStandingsTable() {
        standRankCol.setCellValueFactory(data -> data.getValue().rankProperty().asObject());
        standUserCol.setCellValueFactory(data -> data.getValue().userProperty());
        standCfHandleCol.setCellValueFactory(data -> data.getValue().cfHandleProperty());
        standFinalRatingCol.setCellValueFactory(data -> data.getValue().ratingProperty().asObject());
        standRatingChangeCol.setCellValueFactory(data -> data.getValue().changeProperty());
    }
    
    private void loadMyContests() {
        ObservableList<ContestRow> contests = FXCollections.observableArrayList();
        myContestIds.clear();
        
        try {
            // For now, load all contests (setter can see all)
            // In future, filter by created_by_setter_id
            var allContests = dbManager.getAllSelectionContests();
            
            for (var contest : allContests) {
                int subCount = dbManager.getSubContestCount(contest.getId());
                int participantCount = dbManager.getParticipantCount(contest.getId());
                
                contests.add(new ContestRow(
                    contest.getId(),
                    contest.getContestCode(),
                    contest.getName(),
                    subCount,
                    participantCount,
                    contest.getStartDate() != null ? contest.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "--",
                    contest.getEndDate() != null ? contest.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "--",
                    contest.isActive() ? "Active" : "Ended"
                ));
                myContestIds.add(contest.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        myContestsTable.setItems(contests);
        statusLabel.setText("Loaded " + contests.size() + " contests");
    }
    
    private void loadContestCombos() {
        ObservableList<String> contestOptions = FXCollections.observableArrayList();
        try {
            var allContests = dbManager.getAllSelectionContests();
            for (var contest : allContests) {
                contestOptions.add(contest.getContestCode() + " - " + contest.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        selectionContestCombo.setItems(contestOptions);
        standingsContestCombo.setItems(contestOptions);
        
        selectionContestCombo.setOnAction(e -> loadSubContestsForSelected());
    }
    
    private void loadSubContestsForSelected() {
        String selected = selectionContestCombo.getValue();
        if (selected == null) return;
        
        String contestCode = selected.split(" - ")[0];
        var contest = dbManager.getSelectionContestByCode(contestCode);
        
        if (contest != null) {
            loadSubContests(contest.getId());
        }
    }
    
    private void loadSubContests(int selectionContestId) {
        ObservableList<SubContestRow> subContests = FXCollections.observableArrayList();
        
        try {
            var subs = dbManager.getSubContestsForSelection(selectionContestId);
            for (var sub : subs) {
                subContests.add(new SubContestRow(
                    sub.getId(),
                    sub.getCodeforcesContestId(),
                    sub.getContestName() != null ? sub.getContestName() : "CF Contest " + sub.getCodeforcesContestId(),
                    String.format("%.1f", sub.getWeight()),
                    sub.getContestDate() != null ? sub.getContestDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "--"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        subContestsTable.setItems(subContests);
    }
    
    private void loadProfile() {
        if (currentUser != null) {
            profileFullNameField.setText(currentUser.getFullName());
            profileUsernameField.setText(currentUser.getUsername());
            profileEmailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            profileRoleLabel.setText("Setter");
            profileContestsCreatedLabel.setText(String.valueOf(myContestIds.size()));
            profileCreatedAtLabel.setText(currentUser.getCreatedAt() != null ? 
                currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "--");
        }
    }
    
    @FXML
    private void handleCreateContest() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Selection Contest");
        dialog.setHeaderText("Create a new Selection Contest");
        dialog.setContentText("Contest Code:");
        
        dialog.showAndWait().ifPresent(code -> {
            if (code.trim().isEmpty()) {
                showMessage(addSubContestMessage, "Contest code cannot be empty", false);
                return;
            }
            
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Contest Name");
            nameDialog.setContentText("Contest Name:");
            
            nameDialog.showAndWait().ifPresent(name -> {
                // Create SelectionContest object
                SelectionContest contest = new SelectionContest();
                contest.setContestCode(code.trim());
                contest.setName(name.trim());
                contest.setDescription("");
                contest.setCreatedByAdminId(currentUser.getId());
                contest.setActive(true);
                
                boolean created = dbManager.createSelectionContest(contest);
                if (created) {
                    showMessage(addSubContestMessage, "Contest '" + code + "' created successfully!", true);
                    loadMyContests();
                    loadContestCombos();
                } else {
                    showMessage(addSubContestMessage, "Failed to create contest. Code may already exist.", false);
                }
            });
        });
    }
    
    @FXML
    private void handleAddSubContest() {
        String selected = selectionContestCombo.getValue();
        String cfIdStr = cfContestIdField.getText().trim();
        String cfName = cfContestNameField.getText().trim();
        String weightStr = weightField.getText().trim();
        
        if (selected == null || cfIdStr.isEmpty()) {
            showMessage(addSubContestMessage, "Please select a contest and enter CF Contest ID", false);
            return;
        }
        
        try {
            int cfId = Integer.parseInt(cfIdStr);
            double weight = weightStr.isEmpty() ? 1.0 : Double.parseDouble(weightStr);
            
            if (weight < 0.1 || weight > 2.0) {
                showMessage(addSubContestMessage, "Weight must be between 0.1 and 2.0", false);
                return;
            }
            
            String contestCode = selected.split(" - ")[0];
            var contest = dbManager.getSelectionContestByCode(contestCode);
            
            if (contest != null) {
                boolean added = dbManager.addSubContest(contest.getId(), cfId, cfName.isEmpty() ? "CF Contest " + cfId : cfName, weight);
                if (added) {
                    showMessage(addSubContestMessage, "Sub-contest added successfully!", true);
                    cfContestIdField.clear();
                    cfContestNameField.clear();
                    weightField.setText("1.0");
                    loadSubContests(contest.getId());
                    loadMyContests();
                } else {
                    showMessage(addSubContestMessage, "Failed to add sub-contest", false);
                }
            }
        } catch (NumberFormatException e) {
            showMessage(addSubContestMessage, "Invalid number format", false);
        }
    }
    
    private void handleRemoveSubContest(int subContestId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Sub-Contest");
        alert.setContentText("Are you sure you want to remove this sub-contest?");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                dbManager.deleteSubContest(subContestId);
                loadSubContestsForSelected();
                loadMyContests();
            }
        });
    }
    
    @FXML
    private void handleLoadStandings() {
        String selected = standingsContestCombo.getValue();
        if (selected == null) return;
        
        String contestCode = selected.split(" - ")[0];
        var contest = dbManager.getSelectionContestByCode(contestCode);
        
        if (contest != null) {
            selectedContestNameLabel.setText(contest.getName());
            infoSubContestsLabel.setText(String.valueOf(dbManager.getSubContestCount(contest.getId())));
            int participants = dbManager.getParticipantCount(contest.getId());
            infoParticipantsLabel.setText(String.valueOf(participants));
            infoAvgRatingLabel.setText("--"); // Calculate if needed
            
            loadStandingsData(contest.getId());
        }
    }
    
    private void loadStandingsData(int contestId) {
        ObservableList<StandingRow> standings = FXCollections.observableArrayList();
        
        try {
            var finalRatings = dbManager.getFinalRatingsForContest(contestId);
            int rank = 1;
            for (var rating : finalRatings) {
                User user = dbManager.loadUserById(rating.getUserId());
                if (user != null) {
                    // Rating change is not available, show participation info instead
                    String changeInfo = rating.getContestsParticipated() + "/" + rating.getTotalSubContests();
                    standings.add(new StandingRow(
                        rank++,
                        user.getFullName(),
                        user.getCodeforcesHandle() != null ? user.getCodeforcesHandle() : "--",
                        (int) rating.getFinalRating(),
                        changeInfo
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        standingsTable.setItems(standings);
    }
    
    @FXML
    private void handleSaveProfile() {
        if (currentUser != null) {
            currentUser.setFullName(profileFullNameField.getText().trim());
            currentUser.setEmail(profileEmailField.getText().trim());
            
            dbManager.saveUser(currentUser);
            UserDatabase.getInstance().setCurrentUser(currentUser);
            
            showMessage(profileMessage, "Profile saved successfully!", true);
            userNameLabel.setText("Welcome, " + currentUser.getFullName() + " (Setter)");
        }
    }
    
    private void handleViewContest(int contestId) {
        // Find the contest and navigate to detail view
        SelectionContest contest = dbManager.getSelectionContestById(contestId);
        if (contest != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SelectionContestDetail.fxml"));
                Parent root = loader.load();
                
                SelectionContestDetailController controller = loader.getController();
                controller.setUser(currentUser);
                controller.setContest(contest);
                
                Stage stage = (Stage) userNameLabel.getScene().getWindow();
                Scene scene = new Scene(root, 1000, 700);
                
                try {
                    String css = getClass().getResource("/css/styles.css").toExternalForm();
                    scene.getStylesheets().add(css);
                } catch (Exception e) {
                    System.err.println("Could not load CSS: " + e.getMessage());
                }
                
                stage.setScene(scene);
                stage.setTitle("Contest Details - " + contest.getName());
                stage.centerOnScreen();
                
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error loading contest detail: " + e.getMessage());
            }
        } else {
            statusLabel.setText("Contest not found: " + contestId);
        }
    }
    
    @FXML
    private void handleRefreshContests() {
        loadMyContests();
        loadContestCombos();
    }
    
    @FXML
    private void handleProfile() {
        // Switch to profile tab
    }
    
    @FXML
    private void handleBack() {
        navigateToLogin();
    }
    
    @FXML
    private void handleLogout() {
        UserDatabase.getInstance().logout();
        navigateToLogin();
    }
    
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Login - Contest Rating Predictor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showMessage(Label label, String message, boolean success) {
        label.setText(message);
        label.setStyle(success ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
    }
    
    // Row classes
    public static class ContestRow {
        private final int id;
        private final SimpleStringProperty code;
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty subContests;
        private final SimpleIntegerProperty participants;
        private final SimpleStringProperty startDate;
        private final SimpleStringProperty endDate;
        private final SimpleStringProperty status;
        
        public ContestRow(int id, String code, String name, int subContests, int participants, String startDate, String endDate, String status) {
            this.id = id;
            this.code = new SimpleStringProperty(code);
            this.name = new SimpleStringProperty(name);
            this.subContests = new SimpleIntegerProperty(subContests);
            this.participants = new SimpleIntegerProperty(participants);
            this.startDate = new SimpleStringProperty(startDate);
            this.endDate = new SimpleStringProperty(endDate);
            this.status = new SimpleStringProperty(status);
        }
        
        public int getId() { return id; }
        public SimpleStringProperty codeProperty() { return code; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleIntegerProperty subContestsProperty() { return subContests; }
        public SimpleIntegerProperty participantsProperty() { return participants; }
        public SimpleStringProperty startDateProperty() { return startDate; }
        public SimpleStringProperty endDateProperty() { return endDate; }
        public SimpleStringProperty statusProperty() { return status; }
    }
    
    public static class SubContestRow {
        private final int id;
        private final SimpleIntegerProperty cfId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty weight;
        private final SimpleStringProperty date;
        
        public SubContestRow(int id, int cfId, String name, String weight, String date) {
            this.id = id;
            this.cfId = new SimpleIntegerProperty(cfId);
            this.name = new SimpleStringProperty(name);
            this.weight = new SimpleStringProperty(weight);
            this.date = new SimpleStringProperty(date);
        }
        
        public int getId() { return id; }
        public SimpleIntegerProperty cfIdProperty() { return cfId; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty weightProperty() { return weight; }
        public SimpleStringProperty dateProperty() { return date; }
    }
    
    public static class StandingRow {
        private final SimpleIntegerProperty rank;
        private final SimpleStringProperty user;
        private final SimpleStringProperty cfHandle;
        private final SimpleIntegerProperty rating;
        private final SimpleStringProperty change;
        
        public StandingRow(int rank, String user, String cfHandle, int rating, String change) {
            this.rank = new SimpleIntegerProperty(rank);
            this.user = new SimpleStringProperty(user);
            this.cfHandle = new SimpleStringProperty(cfHandle);
            this.rating = new SimpleIntegerProperty(rating);
            this.change = new SimpleStringProperty(change);
        }
        
        public SimpleIntegerProperty rankProperty() { return rank; }
        public SimpleStringProperty userProperty() { return user; }
        public SimpleStringProperty cfHandleProperty() { return cfHandle; }
        public SimpleIntegerProperty ratingProperty() { return rating; }
        public SimpleStringProperty changeProperty() { return change; }
    }
}
