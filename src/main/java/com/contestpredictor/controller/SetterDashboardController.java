package com.contestpredictor.controller;

import com.contestpredictor.data.DatabaseManager;
import com.contestpredictor.data.UserDatabase;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for Setter Dashboard - Create and manage Selection Contests
 */
public class SetterDashboardController {

    // Header
    @FXML private Label userNameLabel;
    @FXML private Label statusLabel;
    @FXML private TabPane mainTabPane;
    
    // My Contests Tab
    @FXML private TextField searchMyContestsField;
    @FXML private TableView<ContestRow> myContestsTable;
    @FXML private TableColumn<ContestRow, String> contestCodeCol;
    @FXML private TableColumn<ContestRow, String> contestNameCol;
    @FXML private TableColumn<ContestRow, String> shareKeyCol;
    @FXML private TableColumn<ContestRow, Integer> subContestsCol;
    @FXML private TableColumn<ContestRow, Integer> participantsCol;
    @FXML private TableColumn<ContestRow, String> statusCol;
    @FXML private TableColumn<ContestRow, String> createdAtCol;
    @FXML private TableColumn<ContestRow, Void> actionsCol;
    
    // Create Contest Tab
    @FXML private TextField newContestNameField;
    @FXML private TextArea newContestDescField;
    @FXML private Label createContestMessage;
    
    // Manage Sub-Contests Tab
    @FXML private ComboBox<SelectionContest> subContestSelectionCombo;
    @FXML private Label selectedContestKeyLabel;
    @FXML private VBox addSubContestSection;
    @FXML private TextField cfContestLinkField;
    @FXML private TextField subContestWeightField;
    @FXML private Label addSubContestMessage;
    @FXML private TableView<SubContestRow> subContestsTable;
    @FXML private TableColumn<SubContestRow, Integer> subCfIdCol;
    @FXML private TableColumn<SubContestRow, String> subNameCol;
    @FXML private TableColumn<SubContestRow, String> subDateCol;
    @FXML private TableColumn<SubContestRow, String> subWeightCol;
    @FXML private TableColumn<SubContestRow, String> subPhaseCol;
    @FXML private TableColumn<SubContestRow, Void> subActionsCol;
    
    // Standings Tab
    @FXML private ComboBox<SelectionContest> standingsContestCombo;
    @FXML private Label standingsInfoLabel;
    @FXML private HBox standingsInfoSection;
    @FXML private Label standingsContestNameLabel;
    @FXML private Label standingsSubCountLabel;
    @FXML private Label standingsParticipantCountLabel;
    @FXML private Label standingsShareKeyLabel;
    @FXML private TableView<StandingRow> standingsTable;
    @FXML private TableColumn<StandingRow, Integer> standRankCol;
    @FXML private TableColumn<StandingRow, String> standUserCol;
    @FXML private TableColumn<StandingRow, String> standCfHandleCol;
    @FXML private TableColumn<StandingRow, Double> standFinalRatingCol;
    @FXML private TableColumn<StandingRow, String> standParticipatedCol;
    
    // Profile Tab
    @FXML private TextField profileFullNameField;
    @FXML private TextField profileUsernameField;
    @FXML private TextField profileEmailField;
    @FXML private Label profileRoleLabel;
    @FXML private Label profileContestsCreatedLabel;
    @FXML private Label profileMessage;
    
    private User currentUser;
    private DatabaseManager dbManager;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
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
    
    // ==================== MY CONTESTS TAB ====================
    
    private void setupMyContestsTable() {
        contestCodeCol.setCellValueFactory(data -> data.getValue().codeProperty());
        contestNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        shareKeyCol.setCellValueFactory(data -> data.getValue().shareKeyProperty());
        subContestsCol.setCellValueFactory(data -> data.getValue().subContestsProperty().asObject());
        participantsCol.setCellValueFactory(data -> data.getValue().participantsProperty().asObject());
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());
        createdAtCol.setCellValueFactory(data -> data.getValue().createdAtProperty());
        
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button manageBtn = new Button("Manage");
            private final HBox buttons = new HBox(5, viewBtn, manageBtn);
            
            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
                manageBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 11;");
                
                viewBtn.setOnAction(e -> {
                    ContestRow row = getTableView().getItems().get(getIndex());
                    navigateToContestDetail(row.getId());
                });
                
                manageBtn.setOnAction(e -> {
                    ContestRow row = getTableView().getItems().get(getIndex());
                    switchToSubContestTab(row.getId());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }
    
    private void loadMyContests() {
        ObservableList<ContestRow> contests = FXCollections.observableArrayList();
        
        List<SelectionContest> myContests = dbManager.getContestsByCreator(currentUser.getId());
        
        for (SelectionContest contest : myContests) {
            int subCount = dbManager.getSubContestCount(contest.getId());
            int participants = dbManager.getParticipantCount(contest.getId());
            String status = contest.isActive() ? "Active" : "Ended";
            String createdAt = contest.getCreatedAt() != null ? contest.getCreatedAt().format(DATE_FORMAT) : "--";
            String shareKey = contest.getShareKey() != null ? contest.getShareKey() : "N/A";
            
            contests.add(new ContestRow(
                contest.getId(),
                contest.getContestCode(),
                contest.getName(),
                shareKey,
                subCount,
                participants,
                status,
                createdAt
            ));
        }
        
        myContestsTable.setItems(contests);
    }
    
    private void navigateToContestDetail(int contestId) {
        SelectionContest contest = dbManager.getSelectionContestById(contestId);
        if (contest != null) {
            navigateToContestManagement(contest);
        }
    }
    
    private void switchToSubContestTab(int contestId) {
        SelectionContest contest = dbManager.getSelectionContestById(contestId);
        if (contest != null) {
            navigateToContestManagement(contest);
        }
    }
    
    @FXML
    private void handleCreateContest() {
        // Switch to Create Contest tab
        mainTabPane.getSelectionModel().select(1);
    }
    
    @FXML
    private void handleRefreshContests() {
        loadMyContests();
        loadContestCombos();
        showStatus("Contests refreshed", true);
    }
    
    // ==================== CREATE CONTEST TAB ====================
    
    @FXML
    private void handleCreateNewContest() {
        String name = newContestNameField.getText().trim();
        String desc = newContestDescField.getText().trim();
        
        if (name.isEmpty()) {
            showMessage(createContestMessage, "Please enter a contest name", false);
            return;
        }
        
        // Auto-generate contest code from name
        String code = generateContestCode(name);
        
        // Create the contest
        SelectionContest contest = new SelectionContest();
        contest.setContestCode(code);
        contest.setName(name);
        contest.setDescription(desc);
        contest.setCreatedByAdminId(currentUser.getId());
        contest.setActive(true);
        
        boolean success = dbManager.createSelectionContest(contest);
        
        if (success) {
            showMessage(createContestMessage, "✅ Contest created! Navigating to management...", true);
            
            // Clear fields
            newContestNameField.clear();
            newContestDescField.clear();
            
            // Navigate to the management page for this contest
            navigateToContestManagement(contest);
        } else {
            showMessage(createContestMessage, "❌ Failed to create contest. Please try again.", false);
        }
    }
    
    private String generateContestCode(String name) {
        // Generate code from name: uppercase, no spaces, with timestamp for uniqueness
        String base = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (base.length() > 10) {
            base = base.substring(0, 10);
        }
        return base + "-" + System.currentTimeMillis() % 100000;
    }
    
    private void navigateToContestManagement(SelectionContest contest) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SelectionContestManage.fxml"));
            Parent root = loader.load();
            
            SelectionContestManageController controller = loader.getController();
            controller.setUser(currentUser);
            controller.setContest(contest);
            
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 750);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception e) { }
            
            stage.setScene(scene);
            stage.setTitle("Manage Contest - " + contest.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error loading contest management: " + e.getMessage(), false);
        }
    }
    
    // ==================== MANAGE SUB-CONTESTS TAB ====================
    
    private void setupSubContestsTable() {
        subCfIdCol.setCellValueFactory(data -> data.getValue().cfIdProperty().asObject());
        subNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        subDateCol.setCellValueFactory(data -> data.getValue().dateProperty());
        subWeightCol.setCellValueFactory(data -> data.getValue().weightProperty());
        subPhaseCol.setCellValueFactory(data -> data.getValue().phaseProperty());
        
        subActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editWeightBtn = new Button("Edit Weight");
            private final Button removeBtn = new Button("Remove");
            private final HBox buttons = new HBox(5, editWeightBtn, removeBtn);
            
            {
                editWeightBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10;");
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10;");
                
                editWeightBtn.setOnAction(e -> {
                    SubContestRow row = getTableView().getItems().get(getIndex());
                    handleEditWeight(row);
                });
                
                removeBtn.setOnAction(e -> {
                    SubContestRow row = getTableView().getItems().get(getIndex());
                    handleRemoveSubContest(row);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }
    
    private void loadContestCombos() {
        List<SelectionContest> myContests = dbManager.getContestsByCreator(currentUser.getId());
        
        subContestSelectionCombo.setItems(FXCollections.observableArrayList(myContests));
        standingsContestCombo.setItems(FXCollections.observableArrayList(myContests));
    }
    
    @FXML
    private void handleContestSelected() {
        SelectionContest selected = subContestSelectionCombo.getValue();
        
        if (selected != null) {
            addSubContestSection.setVisible(true);
            addSubContestSection.setManaged(true);
            selectedContestKeyLabel.setText("Share Key: " + (selected.getShareKey() != null ? selected.getShareKey() : "N/A"));
            
            loadSubContests(selected.getId());
        } else {
            addSubContestSection.setVisible(false);
            addSubContestSection.setManaged(false);
            selectedContestKeyLabel.setText("");
            subContestsTable.getItems().clear();
        }
    }
    
    private void loadSubContests(int selectionContestId) {
        ObservableList<SubContestRow> subContests = FXCollections.observableArrayList();
        
        List<SubContest> subs = dbManager.getSubContestsForSelection(selectionContestId);
        
        for (SubContest sub : subs) {
            String date = sub.getContestDate() != null ? sub.getContestDate().format(DATE_FORMAT) : "--";
            
            subContests.add(new SubContestRow(
                sub.getId(),
                sub.getCodeforcesContestId(),
                sub.getContestName() != null ? sub.getContestName() : "CF Contest " + sub.getCodeforcesContestId(),
                date,
                String.format("%.2f", sub.getWeight()),
                sub.getPhase() != null ? sub.getPhase() : "Unknown"
            ));
        }
        
        subContestsTable.setItems(subContests);
    }
    
    @FXML
    private void handleAddSubContest() {
        SelectionContest selected = subContestSelectionCombo.getValue();
        if (selected == null) {
            showMessage(addSubContestMessage, "Please select a contest first", false);
            return;
        }
        
        String linkOrId = cfContestLinkField.getText().trim();
        String weightStr = subContestWeightField.getText().trim();
        
        if (linkOrId.isEmpty()) {
            showMessage(addSubContestMessage, "Please enter CF contest link or ID", false);
            return;
        }
        
        // Extract contest ID from link or use directly
        int cfContestId = extractCodeforcesContestId(linkOrId);
        if (cfContestId <= 0) {
            showMessage(addSubContestMessage, "Invalid Codeforces contest link/ID", false);
            return;
        }
        
        double weight = 1.0;
        try {
            weight = Double.parseDouble(weightStr);
            if (weight <= 0) weight = 1.0;
        } catch (NumberFormatException e) {
            weight = 1.0;
        }
        
        // Create sub-contest
        SubContest subContest = new SubContest();
        subContest.setSelectionContestId(selected.getId());
        subContest.setCodeforcesContestId(cfContestId);
        subContest.setContestName("CF Contest " + cfContestId);
        subContest.setWeight(weight);
        subContest.setPhase("PENDING");
        
        boolean success = dbManager.addSubContest(subContest);
        
        if (success) {
            showMessage(addSubContestMessage, "✅ Sub-contest added!", true);
            cfContestLinkField.clear();
            subContestWeightField.setText("1.0");
            loadSubContests(selected.getId());
            loadMyContests(); // Refresh sub-contest count
        } else {
            showMessage(addSubContestMessage, "❌ Failed to add. Contest may already exist.", false);
        }
    }
    
    private int extractCodeforcesContestId(String input) {
        // Try to extract from URL like https://codeforces.com/contest/1234
        Pattern urlPattern = Pattern.compile("codeforces\\.com/contest/(\\d+)");
        Matcher matcher = urlPattern.matcher(input);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        // Try direct number
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private void handleEditWeight(SubContestRow row) {
        TextInputDialog dialog = new TextInputDialog(row.getWeight());
        dialog.setTitle("Edit Weight");
        dialog.setHeaderText("Edit weight for: " + row.getName());
        dialog.setContentText("New weight:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(weightStr -> {
            try {
                double newWeight = Double.parseDouble(weightStr);
                if (newWeight > 0) {
                    boolean success = dbManager.updateSubContestWeight(row.getId(), newWeight);
                    if (success) {
                        SelectionContest selected = subContestSelectionCombo.getValue();
                        if (selected != null) {
                            loadSubContests(selected.getId());
                        }
                        showMessage(addSubContestMessage, "Weight updated!", true);
                    }
                }
            } catch (NumberFormatException e) {
                showMessage(addSubContestMessage, "Invalid weight value", false);
            }
        });
    }
    
    private void handleRemoveSubContest(SubContestRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Sub-Contest");
        confirm.setHeaderText("Remove " + row.getName() + "?");
        confirm.setContentText("This will remove this sub-contest from the selection contest.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = dbManager.deleteSubContest(row.getId());
            if (success) {
                SelectionContest selected = subContestSelectionCombo.getValue();
                if (selected != null) {
                    loadSubContests(selected.getId());
                }
                loadMyContests();
                showMessage(addSubContestMessage, "Sub-contest removed", true);
            }
        }
    }
    
    // ==================== STANDINGS TAB ====================
    
    private void setupStandingsTable() {
        standRankCol.setCellValueFactory(data -> data.getValue().rankProperty().asObject());
        standUserCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        standCfHandleCol.setCellValueFactory(data -> data.getValue().cfHandleProperty());
        standFinalRatingCol.setCellValueFactory(data -> data.getValue().finalRatingProperty().asObject());
        standParticipatedCol.setCellValueFactory(data -> data.getValue().participatedProperty());
    }
    
    @FXML
    private void handleLoadStandings() {
        SelectionContest selected = standingsContestCombo.getValue();
        
        if (selected == null) {
            standingsInfoSection.setVisible(false);
            standingsInfoSection.setManaged(false);
            standingsTable.getItems().clear();
            return;
        }
        
        // Show info section
        standingsInfoSection.setVisible(true);
        standingsInfoSection.setManaged(true);
        
        standingsContestNameLabel.setText(selected.getName());
        standingsSubCountLabel.setText(String.valueOf(dbManager.getSubContestCount(selected.getId())));
        standingsParticipantCountLabel.setText(String.valueOf(dbManager.getParticipantCount(selected.getId())));
        standingsShareKeyLabel.setText(selected.getShareKey() != null ? selected.getShareKey() : "N/A");
        
        loadStandingsData(selected.getId());
    }
    
    @FXML
    private void handleRefreshStandings() {
        handleLoadStandings();
        showStatus("Standings refreshed", true);
    }
    
    private void loadStandingsData(int contestId) {
        ObservableList<StandingRow> standings = FXCollections.observableArrayList();
        
        try {
            List<FinalRating> finalRatings = dbManager.getFinalRatingsForContest(contestId);
            int rank = 1;
            
            for (FinalRating rating : finalRatings) {
                User user = dbManager.loadUserById(rating.getUserId());
                if (user != null) {
                    String participated = rating.getContestsParticipated() + "/" + rating.getTotalSubContests();
                    standings.add(new StandingRow(
                        rank++,
                        user.getFullName(),
                        user.getCodeforcesHandle() != null ? user.getCodeforcesHandle() : "--",
                        rating.getFinalRating(),
                        participated
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        standingsTable.setItems(standings);
    }
    
    // ==================== PROFILE TAB ====================
    
    private void loadProfile() {
        if (currentUser != null) {
            profileFullNameField.setText(currentUser.getFullName());
            profileUsernameField.setText(currentUser.getUsername());
            profileEmailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            profileRoleLabel.setText(currentUser.getRole().toString());
            
            int contestsCreated = dbManager.getContestsByCreator(currentUser.getId()).size();
            profileContestsCreatedLabel.setText(String.valueOf(contestsCreated));
        }
    }
    
    @FXML
    private void handleSaveProfile() {
        if (currentUser != null) {
            currentUser.setFullName(profileFullNameField.getText().trim());
            currentUser.setEmail(profileEmailField.getText().trim());
            
            dbManager.saveUser(currentUser);
            UserDatabase.getInstance().setCurrentUser(currentUser);
            userNameLabel.setText("Welcome, " + currentUser.getFullName() + " (Setter)");
            
            showMessage(profileMessage, "Profile saved!", true);
        }
    }
    
    // ==================== NAVIGATION ====================
    
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
            Scene scene = new Scene(root, 900, 600);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception e) { }
            
            stage.setScene(scene);
            stage.setTitle("Login - Contest Rating Predictor");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void showStatus(String message, boolean success) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: " + (success ? "#2ecc71" : "#e74c3c") + ";");
        }
    }
    
    private void showMessage(Label label, String message, boolean success) {
        if (label != null) {
            label.setText(message);
            label.setStyle("-fx-text-fill: " + (success ? "#27ae60" : "#e74c3c") + ";");
        }
    }
    
    // ==================== ROW CLASSES ====================
    
    public static class ContestRow {
        private final int id;
        private final SimpleStringProperty code;
        private final SimpleStringProperty name;
        private final SimpleStringProperty shareKey;
        private final SimpleIntegerProperty subContests;
        private final SimpleIntegerProperty participants;
        private final SimpleStringProperty status;
        private final SimpleStringProperty createdAt;
        
        public ContestRow(int id, String code, String name, String shareKey, int subContests, int participants, String status, String createdAt) {
            this.id = id;
            this.code = new SimpleStringProperty(code);
            this.name = new SimpleStringProperty(name);
            this.shareKey = new SimpleStringProperty(shareKey);
            this.subContests = new SimpleIntegerProperty(subContests);
            this.participants = new SimpleIntegerProperty(participants);
            this.status = new SimpleStringProperty(status);
            this.createdAt = new SimpleStringProperty(createdAt);
        }
        
        public int getId() { return id; }
        public SimpleStringProperty codeProperty() { return code; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty shareKeyProperty() { return shareKey; }
        public SimpleIntegerProperty subContestsProperty() { return subContests; }
        public SimpleIntegerProperty participantsProperty() { return participants; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty createdAtProperty() { return createdAt; }
    }
    
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
        public String getName() { return name.get(); }
        public String getWeight() { return weight.get(); }
        public SimpleIntegerProperty cfIdProperty() { return cfId; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty weightProperty() { return weight; }
        public SimpleStringProperty phaseProperty() { return phase; }
    }
    
    public static class StandingRow {
        private final SimpleIntegerProperty rank;
        private final SimpleStringProperty username;
        private final SimpleStringProperty cfHandle;
        private final SimpleDoubleProperty finalRating;
        private final SimpleStringProperty participated;
        
        public StandingRow(int rank, String username, String cfHandle, double finalRating, String participated) {
            this.rank = new SimpleIntegerProperty(rank);
            this.username = new SimpleStringProperty(username);
            this.cfHandle = new SimpleStringProperty(cfHandle);
            this.finalRating = new SimpleDoubleProperty(finalRating);
            this.participated = new SimpleStringProperty(participated);
        }
        
        public SimpleIntegerProperty rankProperty() { return rank; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty cfHandleProperty() { return cfHandle; }
        public SimpleDoubleProperty finalRatingProperty() { return finalRating; }
        public SimpleStringProperty participatedProperty() { return participated; }
    }
}
