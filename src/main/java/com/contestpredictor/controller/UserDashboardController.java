package com.contestpredictor.controller;

import com.contestpredictor.data.DatabaseManager;
import com.contestpredictor.model.*;
import com.contestpredictor.service.CodeforcesApiService;
import com.contestpredictor.service.RatingCalculationService;
import javafx.application.Platform;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for User Dashboard - Join contests, view standings
 */
public class UserDashboardController {
    
    // Header
    @FXML private Label userNameLabel;
    @FXML private Label cfHandleLabel;
    
    // Available Contests Tab
    @FXML private TextField searchContestField;
    @FXML private TableView<SelectionContest> availableContestsTable;
    @FXML private TableColumn<SelectionContest, String> avContestCodeCol;
    @FXML private TableColumn<SelectionContest, String> avContestNameCol;
    @FXML private TableColumn<SelectionContest, Integer> avSubContestsCol;
    @FXML private TableColumn<SelectionContest, Integer> avParticipantsCol;
    @FXML private TableColumn<SelectionContest, String> avStartDateCol;
    @FXML private TableColumn<SelectionContest, String> avEndDateCol;
    @FXML private TableColumn<SelectionContest, String> avStatusCol;
    @FXML private TableColumn<SelectionContest, Void> avActionsCol;
    
    // My Contests Tab
    @FXML private Label myContestsCountLabel;
    @FXML private TableView<RegistrationWithContest> myContestsTable;
    @FXML private TableColumn<RegistrationWithContest, String> myContestCodeCol;
    @FXML private TableColumn<RegistrationWithContest, String> myContestNameCol;
    @FXML private TableColumn<RegistrationWithContest, Integer> mySubContestsCol;
    @FXML private TableColumn<RegistrationWithContest, String> myRegDateCol;
    @FXML private TableColumn<RegistrationWithContest, String> myStatusCol;
    @FXML private TableColumn<RegistrationWithContest, Double> myFinalRatingCol;
    @FXML private TableColumn<RegistrationWithContest, Integer> myRankCol;
    @FXML private TableColumn<RegistrationWithContest, Void> myActionsCol;
    
    // Standings Tab
    @FXML private ComboBox<SelectionContest> standingsContestCombo;
    @FXML private VBox contestInfoCard;
    @FXML private Label selectedContestNameLabel;
    @FXML private Label infoSubContestsLabel;
    @FXML private Label infoParticipantsLabel;
    @FXML private Label infoYourRankLabel;
    @FXML private Label infoYourRatingLabel;
    @FXML private VBox subContestBreakdown;
    @FXML private TableView<ContestResult> subContestResultsTable;
    @FXML private TableColumn<ContestResult, String> scNameCol;
    @FXML private TableColumn<ContestResult, Double> scWeightCol;
    @FXML private TableColumn<ContestResult, Integer> scRankCol;
    @FXML private TableColumn<ContestResult, Double> scPointsCol;
    @FXML private TableColumn<ContestResult, Double> scRatingCol;
    @FXML private TableColumn<ContestResult, Double> scWeightedCol;
    @FXML private TableView<FinalRating> standingsTable;
    @FXML private TableColumn<FinalRating, Integer> rankCol;
    @FXML private TableColumn<FinalRating, String> usernameCol;
    @FXML private TableColumn<FinalRating, String> cfHandleCol;
    @FXML private TableColumn<FinalRating, Double> finalRatingCol;
    @FXML private TableColumn<FinalRating, Integer> participatedCol;
    
    // Profile Tab
    @FXML private Label profileUsernameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private TextField cfHandleField;
    @FXML private Label cfRatingLabel;
    @FXML private Label cfRankLabel;
    @FXML private Label memberSinceLabel;
    @FXML private Label profileStatusLabel;
    @FXML private VBox cfValidationBox;
    @FXML private Label valHandleLabel;
    @FXML private Label valRatingLabel;
    @FXML private Label valRankLabel;
    @FXML private Label valMaxRatingLabel;
    
    // Status Bar
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;
    
    private User currentUser;
    private DatabaseManager dbManager;
    private CodeforcesApiService cfApiService;
    @SuppressWarnings("unused")
    private RatingCalculationService ratingService;
    
    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        cfApiService = new CodeforcesApiService();
        ratingService = new RatingCalculationService();
        
        setupTableColumns();
        setupSearchFilter();
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            if (userNameLabel != null) {
                userNameLabel.setText("Welcome, " + user.getUsername());
            }
            if (cfHandleLabel != null) {
                cfHandleLabel.setText("CF: " + (user.getCodeforcesHandle() != null ? 
                    user.getCodeforcesHandle() : "Not set"));
            }
            loadProfileData();
            loadAvailableContests();
            loadMyContests();
            loadStandingsComboBox();
        }
    }
    
    // ==================== Contest Actions ====================
    
    @FXML
    public void handleRefreshContests() {
        loadAvailableContests();
        setStatus("Contests refreshed");
    }
    
    @FXML
    public void handleLoadStandings() {
        SelectionContest selected = standingsContestCombo.getValue();
        if (selected != null) {
            loadStandingsForContest(selected);
        }
    }
    
    private void joinContest(SelectionContest contest) {
        if (currentUser.getCodeforcesHandle() == null || currentUser.getCodeforcesHandle().isEmpty()) {
            showAlert("CF Handle Required", "Please set your Codeforces handle in the Profile tab before joining a contest.");
            return;
        }
        
        // Check if already registered
        List<Registration> existingRegs = dbManager.getRegistrationsByContestId(contest.getId());
        boolean alreadyRegistered = existingRegs.stream()
            .anyMatch(r -> r.getUserId() == currentUser.getId());
        
        if (alreadyRegistered) {
            showAlert("Already Registered", "You are already registered for this contest.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Join Contest");
        confirm.setHeaderText("Join " + contest.getName() + "?");
        confirm.setContentText("You will be registered with CF handle: " + currentUser.getCodeforcesHandle());
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Registration registration = new Registration();
            registration.setUserId(currentUser.getId());
            registration.setSelectionContestId(contest.getId());
            registration.setCodeforcesHandle(currentUser.getCodeforcesHandle());
            registration.setUsername(currentUser.getUsername());
            registration.setStatus(Registration.RegistrationStatus.ACTIVE);
            registration.setRegisteredAt(LocalDateTime.now());
            
            boolean success = dbManager.registerForContest(registration);
            if (success) {
                showInfo("Success", "You have successfully joined the contest!");
                loadAvailableContests();
                loadMyContests();
            } else {
                showAlert("Error", "Failed to join contest");
            }
        }
    }
    
    private void navigateToContestDetail(SelectionContest contest) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SelectionContestDetail.fxml"));
            Parent root = loader.load();
            
            SelectionContestDetailController controller = loader.getController();
            controller.setUser(currentUser);
            controller.setContest(contest);
            
            Stage stage = (Stage) availableContestsTable.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            
            // Try to load CSS
            try {
                String css = getClass().getResource("/css/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.err.println("Could not load CSS: " + e.getMessage());
            }
            
            stage.setScene(scene);
            stage.setTitle("Contest Details - " + contest.getName());
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load contest detail view: " + e.getMessage());
        }
    }
    
    private void withdrawFromContest(RegistrationWithContest regWithContest) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Withdraw from Contest");
        confirm.setHeaderText("Withdraw from " + regWithContest.getContest().getName() + "?");
        confirm.setContentText("Are you sure you want to withdraw?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Registration reg = regWithContest.getRegistration();
            reg.setStatus(Registration.RegistrationStatus.WITHDRAWN);
            
            boolean success = dbManager.updateRegistrationStatus(reg.getId(), Registration.RegistrationStatus.WITHDRAWN);
            if (success) {
                showInfo("Success", "You have withdrawn from the contest");
                loadMyContests();
            } else {
                showAlert("Error", "Failed to withdraw");
            }
        }
    }
    
    // ==================== Profile Actions ====================
    
    @FXML
    public void handleProfile() {
        // Switch to profile tab - already visible
    }
    
    @FXML
    public void handleValidateCFHandle() {
        String handle = cfHandleField.getText().trim();
        if (handle.isEmpty()) {
            showAlert("Input Required", "Please enter a Codeforces handle");
            return;
        }
        
        setStatus("Validating CF handle...");
        
        CompletableFuture.supplyAsync(() -> cfApiService.getUserInfo(handle))
            .thenAccept(response -> Platform.runLater(() -> {
                if (response != null && response.success && response.data != null) {
                    CodeforcesApiService.UserInfo info = response.data;
                    valHandleLabel.setText(info.handle);
                    valRatingLabel.setText(String.valueOf(info.rating));
                    valRankLabel.setText(info.rank != null ? info.rank : "Unrated");
                    valMaxRatingLabel.setText(String.valueOf(info.maxRating));
                    cfValidationBox.setVisible(true);
                    cfValidationBox.setManaged(true);
                    profileStatusLabel.setText("✓ Handle validated successfully");
                    profileStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                    setStatus("CF handle validated");
                } else {
                    showAlert("Validation Failed", "Could not find Codeforces user: " + handle);
                    setStatus("CF handle validation failed");
                }
            }));
    }
    
    @FXML
    public void handleUpdateCFHandle() {
        String handle = cfHandleField.getText().trim();
        if (handle.isEmpty()) {
            showAlert("Input Required", "Please enter a Codeforces handle");
            return;
        }
        
        // Validate first
        CodeforcesApiService.ApiResponse<CodeforcesApiService.UserInfo> response = cfApiService.getUserInfo(handle);
        if (response == null || !response.success || response.data == null) {
            showAlert("Validation Failed", "Please validate your CF handle first");
            return;
        }
        
        currentUser.setCodeforcesHandle(handle);
        boolean success = dbManager.updateUserCodeforcesHandle(currentUser.getId(), handle);
        
        if (success) {
            cfHandleLabel.setText("CF: " + handle);
            profileStatusLabel.setText("✓ CF handle updated successfully");
            profileStatusLabel.setStyle("-fx-text-fill: #27ae60;");
            showInfo("Success", "Codeforces handle updated!");
        } else {
            showAlert("Error", "Failed to update CF handle");
        }
    }
    
    @FXML
    public void handleChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter new password");
        dialog.setContentText("New password:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            if (newPassword.length() < 4) {
                showAlert("Validation Error", "Password must be at least 4 characters");
                return;
            }
            
            boolean success = dbManager.updateUserPassword(currentUser.getId(), newPassword);
            if (success) {
                showInfo("Success", "Password changed successfully");
            } else {
                showAlert("Error", "Failed to change password");
            }
        });
    }
    
    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login - Contest Rating Predictor");
        } catch (Exception e) {
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleBack() {
        // Back button - same as logout
        handleLogout();
    }
    
    // ==================== Data Loading ====================
    
    private void loadAvailableContests() {
        try {
            List<SelectionContest> contests = dbManager.getAllSelectionContests();
            // Filter active contests
            contests.removeIf(c -> !c.isActive());
            
            if (availableContestsTable != null) {
                availableContestsTable.setItems(FXCollections.observableArrayList(contests));
            }
        } catch (Exception e) {
            System.err.println("Error loading contests: " + e.getMessage());
        }
    }
    
    private void loadMyContests() {
        try {
            List<Registration> myRegistrations = dbManager.getRegistrationsByUser(currentUser.getId());
            ObservableList<RegistrationWithContest> regList = FXCollections.observableArrayList();
            
            for (Registration reg : myRegistrations) {
                SelectionContest contest = dbManager.getSelectionContestById(reg.getSelectionContestId());
                if (contest != null) {
                    FinalRating rating = dbManager.getFinalRating(reg.getSelectionContestId(), currentUser.getId());
                    regList.add(new RegistrationWithContest(reg, contest, rating));
                }
            }
            
            if (myContestsTable != null) {
                myContestsTable.setItems(regList);
            }
            if (myContestsCountLabel != null) {
                myContestsCountLabel.setText(regList.size() + " contests");
            }
        } catch (Exception e) {
            System.err.println("Error loading my contests: " + e.getMessage());
        }
    }
    
    private void loadStandingsComboBox() {
        if (standingsContestCombo != null) {
            List<SelectionContest> contests = dbManager.getAllSelectionContests();
            standingsContestCombo.setItems(FXCollections.observableArrayList(contests));
            standingsContestCombo.setConverter(new javafx.util.StringConverter<SelectionContest>() {
                @Override
                public String toString(SelectionContest c) {
                    return c != null ? c.getContestCode() + " - " + c.getName() : "";
                }
                @Override
                public SelectionContest fromString(String s) { return null; }
            });
        }
    }
    
    private void loadStandingsForContest(SelectionContest contest) {
        // Update contest info card
        if (selectedContestNameLabel != null) selectedContestNameLabel.setText(contest.getName());
        
        List<SubContest> subContests = dbManager.getSubContestsBySelectionId(contest.getId());
        if (infoSubContestsLabel != null) infoSubContestsLabel.setText(String.valueOf(subContests.size()));
        
        List<Registration> registrations = dbManager.getRegistrationsByContestId(contest.getId());
        if (infoParticipantsLabel != null) infoParticipantsLabel.setText(String.valueOf(registrations.size()));
        
        // Load standings
        List<FinalRating> ratings = dbManager.getFinalRatingsByContestId(contest.getId());
        ratings.sort((a, b) -> Double.compare(b.getFinalRating(), a.getFinalRating()));
        
        int rank = 1;
        int userRank = -1;
        double userRating = 0;
        
        for (FinalRating r : ratings) {
            r.setOverallRank(rank);
            if (r.getUserId() == currentUser.getId()) {
                userRank = rank;
                userRating = r.getFinalRating();
            }
            rank++;
        }
        
        if (infoYourRankLabel != null) infoYourRankLabel.setText(userRank > 0 ? "#" + userRank : "--");
        if (infoYourRatingLabel != null) infoYourRatingLabel.setText(userRank > 0 ? String.format("%.2f", userRating) : "--");
        
        if (standingsTable != null) {
            standingsTable.setItems(FXCollections.observableArrayList(ratings));
        }
        
        // Load user's sub-contest breakdown if registered
        if (userRank > 0) {
            List<ContestResult> myResults = dbManager.getContestResultsByUser(contest.getId(), currentUser.getId());
            if (subContestResultsTable != null && !myResults.isEmpty()) {
                subContestResultsTable.setItems(FXCollections.observableArrayList(myResults));
                if (subContestBreakdown != null) {
                    subContestBreakdown.setVisible(true);
                    subContestBreakdown.setManaged(true);
                }
            }
        } else {
            if (subContestBreakdown != null) {
                subContestBreakdown.setVisible(false);
                subContestBreakdown.setManaged(false);
            }
        }
    }
    
    private void loadProfileData() {
        if (currentUser != null) {
            if (profileUsernameLabel != null) 
                profileUsernameLabel.setText(currentUser.getUsername());
            if (profileEmailLabel != null) 
                profileEmailLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "--");
            if (cfHandleField != null && currentUser.getCodeforcesHandle() != null) 
                cfHandleField.setText(currentUser.getCodeforcesHandle());
            if (memberSinceLabel != null && currentUser.getCreatedAt() != null)
                memberSinceLabel.setText(currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            // Fetch CF rating if handle is set
            if (currentUser.getCodeforcesHandle() != null && !currentUser.getCodeforcesHandle().isEmpty()) {
                CompletableFuture.supplyAsync(() -> cfApiService.getUserInfo(currentUser.getCodeforcesHandle()))
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response != null && response.success && response.data != null) {
                            CodeforcesApiService.UserInfo info = response.data;
                            if (cfRatingLabel != null) cfRatingLabel.setText(String.valueOf(info.rating));
                            if (cfRankLabel != null) cfRankLabel.setText(info.rank != null ? info.rank : "Unrated");
                        }
                    }));
            }
        }
    }
    
    // ==================== Table Setup ====================
    
    private void setupTableColumns() {
        // Available Contests
        if (avContestCodeCol != null) avContestCodeCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getContestCode()));
        if (avContestNameCol != null) avContestNameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getName()));
        if (avSubContestsCol != null) avSubContestsCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(dbManager.getSubContestsBySelectionId(
                data.getValue().getId()).size()).asObject());
        if (avParticipantsCol != null) avParticipantsCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(dbManager.getRegistrationsByContestId(
                data.getValue().getId()).size()).asObject());
        if (avStartDateCol != null) avStartDateCol.setCellValueFactory(data -> {
            LocalDateTime start = data.getValue().getStartDate();
            return new SimpleStringProperty(start != null ? 
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "--");
        });
        if (avEndDateCol != null) avEndDateCol.setCellValueFactory(data -> {
            LocalDateTime end = data.getValue().getEndDate();
            return new SimpleStringProperty(end != null ? 
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "--");
        });
        if (avStatusCol != null) avStatusCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Ended"));
        
        if (avActionsCol != null) {
            avActionsCol.setCellFactory(col -> new TableCell<>() {
                private final Button viewBtn = new Button("View");
                private final Button joinBtn = new Button("Join");
                private final HBox buttons = new HBox(5, viewBtn, joinBtn);
                
                {
                    viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    joinBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                    
                    viewBtn.setOnAction(e -> {
                        SelectionContest contest = getTableView().getItems().get(getIndex());
                        navigateToContestDetail(contest);
                    });
                    
                    joinBtn.setOnAction(e -> {
                        SelectionContest contest = getTableView().getItems().get(getIndex());
                        joinContest(contest);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        SelectionContest contest = getTableView().getItems().get(getIndex());
                        boolean alreadyJoined = dbManager.getRegistrationsByContestId(contest.getId())
                            .stream().anyMatch(r -> r.getUserId() == currentUser.getId());
                        
                        if (alreadyJoined) {
                            Label joined = new Label("Joined ✓");
                            joined.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            HBox box = new HBox(5, viewBtn, joined);
                            setGraphic(box);
                        } else {
                            setGraphic(buttons);
                        }
                    }
                }
            });
        }
        
        // Double-click to view contest details
        if (availableContestsTable != null) {
            availableContestsTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    SelectionContest selected = availableContestsTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        navigateToContestDetail(selected);
                    }
                }
            });
        }
        
        // My Contests
        if (myContestCodeCol != null) myContestCodeCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getContest().getContestCode()));
        if (myContestNameCol != null) myContestNameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getContest().getName()));
        if (mySubContestsCol != null) mySubContestsCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(dbManager.getSubContestsBySelectionId(
                data.getValue().getContest().getId()).size()).asObject());
        if (myRegDateCol != null) myRegDateCol.setCellValueFactory(data -> {
            LocalDateTime regAt = data.getValue().getRegistration().getRegisteredAt();
            return new SimpleStringProperty(regAt != null ? 
                regAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "--");
        });
        if (myStatusCol != null) myStatusCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getRegistration().getStatus().name()));
        if (myFinalRatingCol != null) myFinalRatingCol.setCellValueFactory(data -> {
            FinalRating rating = data.getValue().getFinalRating();
            return new SimpleDoubleProperty(rating != null ? rating.getFinalRating() : 0).asObject();
        });
        if (myRankCol != null) myRankCol.setCellValueFactory(data -> {
            FinalRating rating = data.getValue().getFinalRating();
            return new SimpleIntegerProperty(rating != null ? rating.getOverallRank() : 0).asObject();
        });
        
        if (myActionsCol != null) {
            myActionsCol.setCellFactory(col -> new TableCell<>() {
                private final Button viewBtn = new Button("View");
                private final Button withdrawBtn = new Button("Withdraw");
                private final HBox buttons = new HBox(5, viewBtn, withdrawBtn);
                
                {
                    viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
                    withdrawBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
                    
                    viewBtn.setOnAction(e -> {
                        RegistrationWithContest reg = getTableView().getItems().get(getIndex());
                        navigateToContestDetail(reg.getContest());
                    });
                    withdrawBtn.setOnAction(e -> {
                        RegistrationWithContest reg = getTableView().getItems().get(getIndex());
                        withdrawFromContest(reg);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        RegistrationWithContest reg = getTableView().getItems().get(getIndex());
                        if (reg.getRegistration().getStatus() == Registration.RegistrationStatus.WITHDRAWN) {
                            setGraphic(viewBtn);
                        } else {
                            setGraphic(buttons);
                        }
                    }
                }
            });
        }
        
        // Double-click on My Contests to view details
        if (myContestsTable != null) {
            myContestsTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    RegistrationWithContest selected = myContestsTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        navigateToContestDetail(selected.getContest());
                    }
                }
            });
        }
        
        // Standings Table
        if (rankCol != null) rankCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getOverallRank()).asObject());
        if (usernameCol != null) usernameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getUsername()));
        if (cfHandleCol != null) cfHandleCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCodeforcesHandle()));
        if (finalRatingCol != null) finalRatingCol.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getFinalRating()).asObject());
        if (participatedCol != null) participatedCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getContestsParticipated()).asObject());
        
        // Sub-Contest Results Table
        if (scNameCol != null) scNameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getSubContestName()));
        if (scWeightCol != null) scWeightCol.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getWeightedRating()).asObject());
        if (scRankCol != null) scRankCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getCfRank()).asObject());
        if (scPointsCol != null) scPointsCol.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getCfPoints()).asObject());
        if (scRatingCol != null) scRatingCol.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getCalculatedRating()).asObject());
        if (scWeightedCol != null) scWeightedCol.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getWeightedRating()).asObject());
    }
    
    private void setupSearchFilter() {
        if (searchContestField != null) {
            searchContestField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.isEmpty()) {
                    loadAvailableContests();
                } else {
                    String search = newVal.toLowerCase();
                    List<SelectionContest> all = dbManager.getAllSelectionContests();
                    all.removeIf(c -> !c.isActive());
                    all.removeIf(c -> !c.getName().toLowerCase().contains(search) && 
                                     !c.getContestCode().toLowerCase().contains(search));
                    availableContestsTable.setItems(FXCollections.observableArrayList(all));
                }
            });
        }
    }
    
    // ==================== Helper Methods ====================
    
    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
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
    
    // ==================== Helper Class ====================
    
    public static class RegistrationWithContest {
        private final Registration registration;
        private final SelectionContest contest;
        private final FinalRating finalRating;
        
        public RegistrationWithContest(Registration registration, SelectionContest contest, FinalRating finalRating) {
            this.registration = registration;
            this.contest = contest;
            this.finalRating = finalRating;
        }
        
        public Registration getRegistration() { return registration; }
        public SelectionContest getContest() { return contest; }
        public FinalRating getFinalRating() { return finalRating; }
    }
}
