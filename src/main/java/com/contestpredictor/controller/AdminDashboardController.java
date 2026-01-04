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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for Admin Dashboard - Selection Contest Management
 */
public class AdminDashboardController {
    
    // Header
    @FXML private Label adminNameLabel;
    
    // Navigation
    @FXML private Button dashboardBtn;
    @FXML private Button contestsBtn;
    @FXML private Button subContestsBtn;
    @FXML private Button registrationsBtn;
    @FXML private Button standingsBtn;
    @FXML private Button auditLogsBtn;
    
    // Content Area
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private VBox contestsView;
    @FXML private VBox subContestsView;
    @FXML private VBox registrationsView;
    @FXML private VBox standingsView;
    @FXML private VBox auditLogsView;
    
    // Dashboard Stats
    @FXML private Label totalContestsLabel;
    @FXML private Label activeContestsLabel;
    @FXML private Label totalRegistrationsLabel;
    @FXML private Label totalUsersLabel;
    
    // Recent Contests Table
    @FXML private TableView<SelectionContest> recentContestsTable;
    @FXML private TableColumn<SelectionContest, String> contestCodeCol;
    @FXML private TableColumn<SelectionContest, String> contestNameCol;
    @FXML private TableColumn<SelectionContest, Integer> subContestCountCol;
    @FXML private TableColumn<SelectionContest, Integer> registrationCountCol;
    @FXML private TableColumn<SelectionContest, String> statusCol;
    @FXML private TableColumn<SelectionContest, Void> actionsCol;
    
    // All Contests Table
    @FXML private TableView<SelectionContest> allContestsTable;
    @FXML private TableColumn<SelectionContest, String> allContestCodeCol;
    @FXML private TableColumn<SelectionContest, String> allContestNameCol;
    @FXML private TableColumn<SelectionContest, String> allContestDescCol;
    @FXML private TableColumn<SelectionContest, Integer> allSubContestCountCol;
    @FXML private TableColumn<SelectionContest, Integer> allRegistrationCountCol;
    @FXML private TableColumn<SelectionContest, String> allCreatedDateCol;
    @FXML private TableColumn<SelectionContest, String> allStatusCol;
    @FXML private TableColumn<SelectionContest, Void> allActionsCol;
    
    // Create Contest Form
    @FXML private VBox createContestForm;
    @FXML private TextField contestCodeField;
    @FXML private TextField contestNameField;
    @FXML private TextArea contestDescField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    // Sub-Contests
    @FXML private ComboBox<SelectionContest> selectContestCombo;
    @FXML private VBox addSubContestForm;
    @FXML private TextField cfContestIdField;
    @FXML private TextField weightField;
    @FXML private VBox cfContestInfoBox;
    @FXML private Label cfContestNameLabel;
    @FXML private Label cfContestPhaseLabel;
    @FXML private Label cfContestDurationLabel;
    @FXML private TableView<SubContest> subContestsTable;
    @FXML private TableColumn<SubContest, Integer> cfIdCol;
    @FXML private TableColumn<SubContest, String> cfNameCol;
    @FXML private TableColumn<SubContest, String> cfDateCol;
    @FXML private TableColumn<SubContest, Double> weightCol;
    @FXML private TableColumn<SubContest, String> phaseCol;
    @FXML private TableColumn<SubContest, Void> subActionsCol;
    
    // Registrations
    @FXML private ComboBox<SelectionContest> regContestCombo;
    @FXML private TableView<Registration> registrationsTable;
    @FXML private TableColumn<Registration, String> regUsernameCol;
    @FXML private TableColumn<Registration, String> regCfHandleCol;
    @FXML private TableColumn<Registration, String> regContestCol;
    @FXML private TableColumn<Registration, String> regDateCol;
    @FXML private TableColumn<Registration, String> regStatusCol;
    @FXML private TableColumn<Registration, Void> regActionsCol;
    
    // Standings
    @FXML private ComboBox<SelectionContest> standingsContestCombo;
    @FXML private TableView<FinalRating> standingsTable;
    @FXML private TableColumn<FinalRating, Integer> rankCol;
    @FXML private TableColumn<FinalRating, String> standUsernameCol;
    @FXML private TableColumn<FinalRating, String> standCfHandleCol;
    @FXML private TableColumn<FinalRating, Double> finalRatingCol;
    @FXML private TableColumn<FinalRating, Integer> participatedCol;
    @FXML private TableColumn<FinalRating, Void> standActionsCol;
    
    // Audit Logs
    @FXML private ComboBox<String> logTypeCombo;
    @FXML private TableView<AuditLog> auditLogsTable;
    @FXML private TableColumn<AuditLog, String> logTimeCol;
    @FXML private TableColumn<AuditLog, String> logActionCol;
    @FXML private TableColumn<AuditLog, String> logActorCol;
    @FXML private TableColumn<AuditLog, String> logEntityCol;
    @FXML private TableColumn<AuditLog, String> logDetailsCol;
    
    // Status Bar
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;
    
    private Admin currentAdmin;
    private DatabaseManager dbManager;
    private CodeforcesApiService cfApiService;
    private RatingCalculationService ratingService;
    private CodeforcesApiService.ContestInfo fetchedContestInfo;
    
    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        cfApiService = new CodeforcesApiService();
        ratingService = new RatingCalculationService();
        
        setupTableColumns();
        setupComboBoxes();
        
        // Set default dates
        if (startDatePicker != null) startDatePicker.setValue(LocalDate.now());
        if (endDatePicker != null) endDatePicker.setValue(LocalDate.now().plusMonths(1));
    }
    
    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        if (adminNameLabel != null && admin != null) {
            adminNameLabel.setText("Admin: " + admin.getFullName());
        }
        loadDashboardData();
    }
    
    // ==================== Navigation Methods ====================
    
    @FXML
    public void showDashboard() {
        switchView(dashboardView);
        loadDashboardData();
    }
    
    @FXML
    public void showContests() {
        switchView(contestsView);
        loadAllContests();
    }
    
    @FXML
    public void showSubContests() {
        switchView(subContestsView);
        loadContestComboBox(selectContestCombo);
    }
    
    @FXML
    public void showRegistrations() {
        switchView(registrationsView);
        loadContestComboBox(regContestCombo);
        loadAllRegistrations();
    }
    
    @FXML
    public void showStandings() {
        switchView(standingsView);
        loadContestComboBox(standingsContestCombo);
    }
    
    @FXML
    public void showAuditLogs() {
        switchView(auditLogsView);
        loadAuditLogs();
    }
    
    private void switchView(VBox targetView) {
        if (dashboardView != null) { dashboardView.setVisible(false); dashboardView.setManaged(false); }
        if (contestsView != null) { contestsView.setVisible(false); contestsView.setManaged(false); }
        if (subContestsView != null) { subContestsView.setVisible(false); subContestsView.setManaged(false); }
        if (registrationsView != null) { registrationsView.setVisible(false); registrationsView.setManaged(false); }
        if (standingsView != null) { standingsView.setVisible(false); standingsView.setManaged(false); }
        if (auditLogsView != null) { auditLogsView.setVisible(false); auditLogsView.setManaged(false); }
        
        if (targetView != null) {
            targetView.setVisible(true);
            targetView.setManaged(true);
        }
    }
    
    // ==================== Contest Management ====================
    
    @FXML
    public void handleCreateContest() {
        if (createContestForm != null) {
            createContestForm.setVisible(true);
            createContestForm.setManaged(true);
        }
    }
    
    @FXML
    public void handleCancelCreateContest() {
        if (createContestForm != null) {
            createContestForm.setVisible(false);
            createContestForm.setManaged(false);
            clearContestForm();
        }
    }
    
    @FXML
    public void handleSaveContest() {
        try {
            String code = contestCodeField.getText().trim();
            String name = contestNameField.getText().trim();
            String description = contestDescField != null ? contestDescField.getText().trim() : "";
            LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : LocalDate.now();
            LocalDate endDate = endDatePicker != null ? endDatePicker.getValue() : LocalDate.now().plusMonths(1);
            
            if (code.isEmpty() || name.isEmpty()) {
                showAlert("Validation Error", "Please fill in contest code and name");
                return;
            }
            
            SelectionContest contest = new SelectionContest(code, name, description);
            contest.setCreatedByAdminId(currentAdmin != null ? currentAdmin.getId() : 0);
            contest.setStartDate(startDate.atStartOfDay());
            contest.setEndDate(endDate.atTime(23, 59));
            contest.setActive(true);
            
            boolean success = dbManager.createSelectionContest(contest);
            if (success) {
                // Log the action
                logAction(AuditLog.ActionType.CONTEST_CREATE, AuditLog.EntityType.SELECTION_CONTEST, 
                         contest.getId(), "Created selection contest: " + name);
                
                showInfo("Success", "Selection contest created successfully!");
                handleCancelCreateContest();
                loadAllContests();
                loadDashboardData();
            } else {
                showAlert("Error", "Failed to create contest");
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to create contest: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearContestForm() {
        if (contestCodeField != null) contestCodeField.clear();
        if (contestNameField != null) contestNameField.clear();
        if (contestDescField != null) contestDescField.clear();
        if (startDatePicker != null) startDatePicker.setValue(LocalDate.now());
        if (endDatePicker != null) endDatePicker.setValue(LocalDate.now().plusMonths(1));
    }
    
    // ==================== Sub-Contest Management ====================
    
    @FXML
    public void handleAddSubContest() {
        if (selectContestCombo == null || selectContestCombo.getValue() == null) {
            showAlert("Selection Required", "Please select a contest first");
            return;
        }
        if (addSubContestForm != null) {
            addSubContestForm.setVisible(true);
            addSubContestForm.setManaged(true);
        }
    }
    
    @FXML
    public void handleCancelAddSubContest() {
        if (addSubContestForm != null) {
            addSubContestForm.setVisible(false);
            addSubContestForm.setManaged(false);
            clearSubContestForm();
        }
    }
    
    @FXML
    public void handleFetchContestInfo() {
        try {
            int cfContestId = Integer.parseInt(cfContestIdField.getText().trim());
            setStatus("Fetching contest info from Codeforces...");
            
            CompletableFuture.supplyAsync(() -> cfApiService.getContestInfo(cfContestId))
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response != null && response.success && response.data != null) {
                        fetchedContestInfo = response.data;
                        cfContestNameLabel.setText("Contest: " + response.data.name);
                        cfContestPhaseLabel.setText("Phase: " + response.data.phase);
                        cfContestDurationLabel.setText("Duration: " + (response.data.durationSeconds / 3600) + " hours");
                        cfContestInfoBox.setVisible(true);
                        cfContestInfoBox.setManaged(true);
                        setStatus("Contest info fetched successfully");
                    } else {
                        showAlert("Error", "Contest not found or API error");
                        setStatus("Failed to fetch contest info");
                    }
                }));
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid contest ID");
        }
    }
    
    @FXML
    public void handleSaveSubContest() {
        try {
            SelectionContest selectedContest = selectContestCombo.getValue();
            if (selectedContest == null) {
                showAlert("Error", "Please select a contest");
                return;
            }
            
            int cfContestId = Integer.parseInt(cfContestIdField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());
            
            if (weight < 0 || weight > 1) {
                showAlert("Validation Error", "Weight must be between 0.0 and 1.0");
                return;
            }
            
            SubContest subContest = new SubContest(cfContestId);
            subContest.setSelectionContestId(selectedContest.getId());
            subContest.setWeight(weight);
            
            if (fetchedContestInfo != null) {
                subContest.setContestName(fetchedContestInfo.name);
                subContest.setPhase(fetchedContestInfo.phase);
                subContest.setDurationSeconds(fetchedContestInfo.durationSeconds);
            }
            
            boolean success = dbManager.addSubContest(subContest);
            if (success) {
                logAction(AuditLog.ActionType.SUBCONTEST_ADD, AuditLog.EntityType.SUB_CONTEST,
                         subContest.getId(), "Added sub-contest CF#" + cfContestId + " to " + selectedContest.getContestCode());
                
                showInfo("Success", "Sub-contest added successfully!");
                handleCancelAddSubContest();
                loadSubContests(selectedContest.getId());
            } else {
                showAlert("Error", "Failed to add sub-contest");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for contest ID and weight");
        } catch (Exception e) {
            showAlert("Error", "Failed to add sub-contest: " + e.getMessage());
        }
    }
    
    private void clearSubContestForm() {
        if (cfContestIdField != null) cfContestIdField.clear();
        if (weightField != null) weightField.clear();
        if (cfContestInfoBox != null) {
            cfContestInfoBox.setVisible(false);
            cfContestInfoBox.setManaged(false);
        }
        fetchedContestInfo = null;
    }
    
    // ==================== Standings Management ====================
    
    @FXML
    public void handleRecalculateStandings() {
        SelectionContest selected = standingsContestCombo != null ? standingsContestCombo.getValue() : null;
        if (selected == null) {
            showAlert("Selection Required", "Please select a contest first");
            return;
        }
        
        setStatus("Recalculating standings...");
        
        CompletableFuture.runAsync(() -> {
            ratingService.calculateFinalRatings(selected);
        }).thenRun(() -> Platform.runLater(() -> {
            loadStandings(selected.getId());
            setStatus("Standings recalculated successfully");
            showInfo("Success", "Standings have been recalculated");
        }));
    }
    
    @FXML
    public void handleFetchCFData() {
        SelectionContest selected = standingsContestCombo != null ? standingsContestCombo.getValue() : null;
        if (selected == null) {
            showAlert("Selection Required", "Please select a contest first");
            return;
        }
        
        setStatus("Fetching data from Codeforces...");
        
        CompletableFuture.runAsync(() -> {
            List<SubContest> subContests = dbManager.getSubContestsBySelectionId(selected.getId());
            List<Registration> registrations = dbManager.getRegistrationsByContestId(selected.getId());
            
            for (SubContest sub : subContests) {
                ratingService.fetchAndCalculateSubContestResults(sub, registrations);
            }
            
            // Calculate final ratings
            ratingService.calculateFinalRatings(selected);
        }).thenRun(() -> Platform.runLater(() -> {
            loadStandings(selected.getId());
            setStatus("Codeforces data fetched and processed");
            showInfo("Success", "Data fetched from Codeforces successfully");
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                showAlert("Error", "Failed to fetch CF data: " + ex.getMessage());
                setStatus("Error fetching CF data");
            });
            return null;
        });
    }
    
    // ==================== Registrations Management ====================
    
    @FXML
    public void handleExportRegistrations() {
        SelectionContest selected = regContestCombo != null ? regContestCombo.getValue() : null;
        List<Registration> registrations;
        
        if (selected != null) {
            registrations = dbManager.getRegistrationsByContestId(selected.getId());
        } else {
            registrations = dbManager.getAllRegistrations();
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Registrations");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("registrations.csv");
        
        File file = fileChooser.showSaveDialog(contentArea.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Username,CF Handle,Contest,Status,Registered At");
                for (Registration reg : registrations) {
                    writer.printf("%s,%s,%d,%s,%s%n",
                        reg.getUsername(), reg.getCodeforcesHandle(), 
                        reg.getSelectionContestId(), reg.getStatus(), reg.getRegisteredAt());
                }
                showInfo("Export Complete", "Registrations exported to " + file.getName());
            } catch (Exception e) {
                showAlert("Error", "Failed to export: " + e.getMessage());
            }
        }
    }
    
    // ==================== Audit Logs ====================
    
    @FXML
    public void handleRefreshLogs() {
        loadAuditLogs();
    }
    
    @FXML
    public void handleRefreshData() {
        loadDashboardData();
        setStatus("Data refreshed");
        updateLastUpdate();
    }
    
    // ==================== Logout ====================
    
    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
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
        handleLogout();
    }
    
    // ==================== Data Loading ====================
    
    private void loadDashboardData() {
        try {
            List<SelectionContest> contests = dbManager.getAllSelectionContests();
            
            // Update stats
            if (totalContestsLabel != null) totalContestsLabel.setText(String.valueOf(contests.size()));
            
            long activeCount = contests.stream().filter(SelectionContest::isActive).count();
            if (activeContestsLabel != null) activeContestsLabel.setText(String.valueOf(activeCount));
            
            int totalRegs = 0;
            for (SelectionContest c : contests) {
                totalRegs += dbManager.getRegistrationsByContestId(c.getId()).size();
            }
            if (totalRegistrationsLabel != null) totalRegistrationsLabel.setText(String.valueOf(totalRegs));
            
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(dbManager.getAllUsers().size()));
            
            // Load recent contests table
            if (recentContestsTable != null) {
                ObservableList<SelectionContest> contestList = FXCollections.observableArrayList(
                    contests.size() > 5 ? contests.subList(0, 5) : contests
                );
                recentContestsTable.setItems(contestList);
            }
            
            updateLastUpdate();
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
        }
    }
    
    private void loadAllContests() {
        try {
            List<SelectionContest> contests = dbManager.getAllSelectionContests();
            if (allContestsTable != null) {
                allContestsTable.setItems(FXCollections.observableArrayList(contests));
            }
        } catch (Exception e) {
            System.err.println("Error loading contests: " + e.getMessage());
        }
    }
    
    private void loadSubContests(int selectionContestId) {
        try {
            List<SubContest> subContests = dbManager.getSubContestsBySelectionId(selectionContestId);
            if (subContestsTable != null) {
                subContestsTable.setItems(FXCollections.observableArrayList(subContests));
            }
        } catch (Exception e) {
            System.err.println("Error loading sub-contests: " + e.getMessage());
        }
    }
    
    private void loadAllRegistrations() {
        try {
            List<Registration> registrations = dbManager.getAllRegistrations();
            if (registrationsTable != null) {
                registrationsTable.setItems(FXCollections.observableArrayList(registrations));
            }
        } catch (Exception e) {
            System.err.println("Error loading registrations: " + e.getMessage());
        }
    }
    
    private void loadStandings(int contestId) {
        try {
            List<FinalRating> ratings = dbManager.getFinalRatingsByContestId(contestId);
            // Sort by final rating descending
            ratings.sort((a, b) -> Double.compare(b.getFinalRating(), a.getFinalRating()));
            
            // Assign ranks
            int rank = 1;
            for (FinalRating r : ratings) {
                r.setOverallRank(rank++);
            }
            
            if (standingsTable != null) {
                standingsTable.setItems(FXCollections.observableArrayList(ratings));
            }
        } catch (Exception e) {
            System.err.println("Error loading standings: " + e.getMessage());
        }
    }
    
    private void loadAuditLogs() {
        try {
            List<AuditLog> logs = dbManager.getAuditLogs(100);
            if (auditLogsTable != null) {
                auditLogsTable.setItems(FXCollections.observableArrayList(logs));
            }
        } catch (Exception e) {
            System.err.println("Error loading audit logs: " + e.getMessage());
        }
    }
    
    private void loadContestComboBox(ComboBox<SelectionContest> comboBox) {
        if (comboBox != null) {
            List<SelectionContest> contests = dbManager.getAllSelectionContests();
            comboBox.setItems(FXCollections.observableArrayList(contests));
            comboBox.setConverter(new javafx.util.StringConverter<SelectionContest>() {
                @Override
                public String toString(SelectionContest c) {
                    return c != null ? c.getContestCode() + " - " + c.getName() : "";
                }
                @Override
                public SelectionContest fromString(String s) { return null; }
            });
            
            comboBox.setOnAction(e -> {
                SelectionContest selected = comboBox.getValue();
                if (selected != null) {
                    if (comboBox == selectContestCombo) {
                        loadSubContests(selected.getId());
                    } else if (comboBox == regContestCombo) {
                        loadRegistrationsByContest(selected.getId());
                    } else if (comboBox == standingsContestCombo) {
                        loadStandings(selected.getId());
                    }
                }
            });
        }
    }
    
    private void loadRegistrationsByContest(int contestId) {
        try {
            List<Registration> registrations = dbManager.getRegistrationsByContestId(contestId);
            if (registrationsTable != null) {
                registrationsTable.setItems(FXCollections.observableArrayList(registrations));
            }
        } catch (Exception e) {
            System.err.println("Error loading registrations: " + e.getMessage());
        }
    }
    
    // ==================== Table Setup ====================
    
    private void setupTableColumns() {
        // Recent/All Contests Table
        setupContestTableColumns(contestCodeCol, contestNameCol, subContestCountCol, 
                                  registrationCountCol, statusCol, actionsCol);
        setupContestTableColumns(allContestCodeCol, allContestNameCol, allSubContestCountCol,
                                  allRegistrationCountCol, allStatusCol, allActionsCol);
        
        if (allContestDescCol != null) {
            allContestDescCol.setCellValueFactory(data -> 
                new SimpleStringProperty(truncate(data.getValue().getDescription(), 50)));
        }
        if (allCreatedDateCol != null) {
            allCreatedDateCol.setCellValueFactory(data -> {
                LocalDateTime created = data.getValue().getCreatedAt();
                return new SimpleStringProperty(created != null ? 
                    created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "--");
            });
        }
        
        // Sub-Contests Table
        if (cfIdCol != null) cfIdCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getCodeforcesContestId()).asObject());
        if (cfNameCol != null) cfNameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getContestName()));
        if (cfDateCol != null) cfDateCol.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getContestDate();
            return new SimpleStringProperty(date != null ? 
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "--");
        });
        if (weightCol != null) weightCol.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getWeight()).asObject());
        if (phaseCol != null) phaseCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getPhase()));
        
        // Registrations Table
        if (regUsernameCol != null) regUsernameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getUsername()));
        if (regCfHandleCol != null) regCfHandleCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCodeforcesHandle()));
        if (regContestCol != null) regContestCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getSelectionContestId())));
        if (regDateCol != null) regDateCol.setCellValueFactory(data -> {
            LocalDateTime regAt = data.getValue().getRegisteredAt();
            return new SimpleStringProperty(regAt != null ? 
                regAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "--");
        });
        if (regStatusCol != null) regStatusCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStatus().name()));
        
        // Standings Table
        if (rankCol != null) rankCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getOverallRank()).asObject());
        if (standUsernameCol != null) standUsernameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getUsername()));
        if (standCfHandleCol != null) standCfHandleCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCodeforcesHandle()));
        if (finalRatingCol != null) finalRatingCol.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getFinalRating()).asObject());
        if (participatedCol != null) participatedCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getContestsParticipated()).asObject());
        
        // Audit Logs Table
        if (logTimeCol != null) logTimeCol.setCellValueFactory(data -> {
            LocalDateTime ts = data.getValue().getCreatedAt();
            return new SimpleStringProperty(ts != null ? 
                ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "--");
        });
        if (logActionCol != null) logActionCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getActionType() != null ? 
                data.getValue().getActionType().getDisplayName() : "--"));
        if (logActorCol != null) logActorCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getActorDisplay()));
        if (logEntityCol != null) logEntityCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEntityType() + "#" + data.getValue().getEntityId()));
        if (logDetailsCol != null) logDetailsCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDetails()));
    }
    
    private void setupContestTableColumns(
            TableColumn<SelectionContest, String> codeCol,
            TableColumn<SelectionContest, String> nameCol,
            TableColumn<SelectionContest, Integer> subCountCol,
            TableColumn<SelectionContest, Integer> regCountCol,
            TableColumn<SelectionContest, String> statusCol,
            TableColumn<SelectionContest, Void> actionsCol) {
        
        if (codeCol != null) codeCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getContestCode()));
        if (nameCol != null) nameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getName()));
        if (subCountCol != null) subCountCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getSubContestCount()).asObject());
        if (regCountCol != null) regCountCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(dbManager.getRegistrationsByContestId(
                data.getValue().getId()).size()).asObject());
        if (statusCol != null) statusCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));
        
        if (actionsCol != null) {
            actionsCol.setCellFactory(col -> new TableCell<>() {
                private final Button viewBtn = new Button("View");
                private final Button deleteBtn = new Button("Delete");
                private final HBox buttons = new HBox(5, viewBtn, deleteBtn);
                
                {
                    viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
                    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
                    
                    viewBtn.setOnAction(e -> {
                        SelectionContest contest = getTableView().getItems().get(getIndex());
                        showContestDetails(contest);
                    });
                    deleteBtn.setOnAction(e -> {
                        SelectionContest contest = getTableView().getItems().get(getIndex());
                        handleDeleteContest(contest);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            });
        }
    }
    
    private void setupComboBoxes() {
        if (logTypeCombo != null) {
            logTypeCombo.setItems(FXCollections.observableArrayList(
                "All", "USER_REGISTER", "CONTEST_CREATE", "REGISTRATION_CREATE", "RATING_CALCULATE"
            ));
            logTypeCombo.setValue("All");
        }
    }
    
    // ==================== Helper Methods ====================
    
    private void showContestDetails(SelectionContest contest) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contest Details");
        alert.setHeaderText(contest.getName());
        alert.setContentText(
            "Code: " + contest.getContestCode() + "\n" +
            "Description: " + contest.getDescription() + "\n" +
            "Start: " + contest.getStartDate() + "\n" +
            "End: " + contest.getEndDate() + "\n" +
            "Active: " + contest.isActive()
        );
        alert.showAndWait();
    }
    
    private void handleDeleteContest(SelectionContest contest) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Contest");
        confirm.setContentText("Are you sure you want to delete: " + contest.getName() + "?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = dbManager.deleteSelectionContest(contest.getId());
            if (success) {
                logAction(AuditLog.ActionType.CONTEST_DELETE, AuditLog.EntityType.SELECTION_CONTEST,
                         contest.getId(), "Deleted contest: " + contest.getContestCode());
                showInfo("Success", "Contest deleted");
                loadAllContests();
                loadDashboardData();
            } else {
                showAlert("Error", "Failed to delete contest");
            }
        }
    }
    
    private void logAction(AuditLog.ActionType action, AuditLog.EntityType entity, 
                           int entityId, String details) {
        AuditLog log = AuditLog.builder()
            .action(action)
            .admin(currentAdmin != null ? currentAdmin.getId() : 0, 
                   currentAdmin != null ? currentAdmin.getFullName() : "System")
            .entity(entity, entityId, null)
            .details(details)
            .build();
        dbManager.saveAuditLog(log);
    }
    
    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    private void updateLastUpdate() {
        if (lastUpdateLabel != null) {
            lastUpdateLabel.setText("Last Update: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
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
