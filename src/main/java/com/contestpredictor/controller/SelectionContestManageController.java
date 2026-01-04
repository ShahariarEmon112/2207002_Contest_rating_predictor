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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing a single Selection Contest
 * Shows sub-contests, allows adding new ones, and displays standings
 */
public class SelectionContestManageController {

    // Header
    @FXML private Label headerLabel;
    @FXML private Label userNameLabel;
    
    // Contest Info
    @FXML private Label contestNameLabel;
    @FXML private Label shareKeyLabel;
    @FXML private Label subContestCountLabel;
    @FXML private Label participantCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;
    
    // Sub-Contests Table
    @FXML private TableView<SubContestRow> subContestsTable;
    @FXML private TableColumn<SubContestRow, Integer> cfIdCol;
    @FXML private TableColumn<SubContestRow, String> contestNameCol;
    @FXML private TableColumn<SubContestRow, String> dateCol;
    @FXML private TableColumn<SubContestRow, String> weightCol;
    @FXML private TableColumn<SubContestRow, Integer> participantsCol;
    @FXML private TableColumn<SubContestRow, String> phaseCol;
    @FXML private TableColumn<SubContestRow, Void> actionsCol;
    
    // Standings Table
    @FXML private TableView<StandingRow> standingsTable;
    @FXML private TableColumn<StandingRow, Integer> rankCol;
    @FXML private TableColumn<StandingRow, String> userCol;
    @FXML private TableColumn<StandingRow, String> cfHandleCol;
    @FXML private TableColumn<StandingRow, Double> finalRatingCol;
    @FXML private TableColumn<StandingRow, String> participatedCol;
    
    // Status
    @FXML private Label bottomStatusLabel;
    
    private User currentUser;
    private SelectionContest currentContest;
    private DatabaseManager dbManager;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    @FXML
    private void initialize() {
        dbManager = DatabaseManager.getInstance();
        setupSubContestsTable();
        setupStandingsTable();
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText("Setter: " + user.getFullName());
        }
    }
    
    public void setContest(SelectionContest contest) {
        this.currentContest = contest;
        loadContestDetails();
        loadSubContests();
        loadStandings();
    }
    
    private void setupSubContestsTable() {
        cfIdCol.setCellValueFactory(data -> data.getValue().cfIdProperty().asObject());
        contestNameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        dateCol.setCellValueFactory(data -> data.getValue().dateProperty());
        weightCol.setCellValueFactory(data -> data.getValue().weightProperty());
        participantsCol.setCellValueFactory(data -> data.getValue().participantsProperty().asObject());
        phaseCol.setCellValueFactory(data -> data.getValue().phaseProperty());
        
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit Weight");
            private final Button removeBtn = new Button("Remove");
            private final HBox buttons = new HBox(5, viewBtn, editBtn, removeBtn);
            
            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10;");
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10;");
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10;");
                
                viewBtn.setOnAction(e -> {
                    SubContestRow row = getTableView().getItems().get(getIndex());
                    handleViewSubContest(row);
                });
                
                editBtn.setOnAction(e -> {
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
    
    private void setupStandingsTable() {
        rankCol.setCellValueFactory(data -> data.getValue().rankProperty().asObject());
        userCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        cfHandleCol.setCellValueFactory(data -> data.getValue().cfHandleProperty());
        finalRatingCol.setCellValueFactory(data -> data.getValue().finalRatingProperty().asObject());
        participatedCol.setCellValueFactory(data -> data.getValue().participatedProperty());
    }
    
    private void loadContestDetails() {
        if (currentContest == null) return;
        
        headerLabel.setText("Manage: " + currentContest.getName());
        contestNameLabel.setText(currentContest.getName());
        shareKeyLabel.setText(currentContest.getShareKey() != null ? currentContest.getShareKey() : "N/A");
        
        int subCount = dbManager.getSubContestCount(currentContest.getId());
        int participants = dbManager.getParticipantCount(currentContest.getId());
        
        subContestCountLabel.setText(String.valueOf(subCount));
        participantCountLabel.setText(String.valueOf(participants));
        
        statusLabel.setText(currentContest.isActive() ? "Active" : "Ended");
        statusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + 
            (currentContest.isActive() ? "#27ae60" : "#e74c3c") + ";");
        
        descriptionLabel.setText(currentContest.getDescription() != null && !currentContest.getDescription().isEmpty() 
            ? currentContest.getDescription() : "No description");
    }
    
    private void loadSubContests() {
        ObservableList<SubContestRow> subContests = FXCollections.observableArrayList();
        
        List<SubContest> subs = dbManager.getSubContestsForSelection(currentContest.getId());
        
        for (SubContest sub : subs) {
            String date = sub.getContestDate() != null ? sub.getContestDate().format(DATE_FORMAT) : "--";
            int participants = dbManager.getSubContestParticipantCount(sub.getId());
            
            subContests.add(new SubContestRow(
                sub.getId(),
                sub.getCodeforcesContestId(),
                sub.getContestName() != null ? sub.getContestName() : "CF Contest " + sub.getCodeforcesContestId(),
                date,
                String.format("%.2f", sub.getWeight()),
                participants,
                sub.getPhase() != null ? sub.getPhase() : "Unknown"
            ));
        }
        
        subContestsTable.setItems(subContests);
        
        // Update count
        subContestCountLabel.setText(String.valueOf(subContests.size()));
    }
    
    private void loadStandings() {
        ObservableList<StandingRow> standings = FXCollections.observableArrayList();
        
        try {
            List<FinalRating> finalRatings = dbManager.getFinalRatingsForContest(currentContest.getId());
            int rank = 1;
            int limit = 5; // Show top 5 in preview
            
            for (FinalRating rating : finalRatings) {
                if (rank > limit) break;
                
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
    
    @FXML
    private void handleCopyShareKey() {
        if (currentContest != null && currentContest.getShareKey() != null) {
            ClipboardContent content = new ClipboardContent();
            content.putString(currentContest.getShareKey());
            Clipboard.getSystemClipboard().setContent(content);
            showStatus("Share key copied to clipboard!", true);
        }
    }
    
    @FXML
    private void handleOpenAddSubContest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddSubContest.fxml"));
            Parent root = loader.load();
            
            AddSubContestController controller = loader.getController();
            controller.setSelectionContest(currentContest);
            controller.setOnSubContestAdded(() -> {
                loadSubContests();
                loadContestDetails();
                showStatus("Sub-contest added successfully!", true);
            });
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(subContestsTable.getScene().getWindow());
            dialog.setTitle("Add Sub-Contest");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error opening add dialog: " + e.getMessage(), false);
        }
    }
    
    private void handleViewSubContest(SubContestRow row) {
        try {
            SubContest subContest = dbManager.getSubContestById(row.getId());
            if (subContest == null) return;
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubContestDetail.fxml"));
            Parent root = loader.load();
            
            SubContestDetailController controller = loader.getController();
            controller.setUser(currentUser);
            controller.setSubContest(subContest);
            controller.setSelectionContest(currentContest);
            
            Stage stage = (Stage) subContestsTable.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception e) { }
            
            stage.setScene(scene);
            stage.setTitle("Sub-Contest: " + row.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error viewing sub-contest: " + e.getMessage(), false);
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
                        loadSubContests();
                        showStatus("Weight updated!", true);
                    }
                }
            } catch (NumberFormatException e) {
                showStatus("Invalid weight value", false);
            }
        });
    }
    
    private void handleRemoveSubContest(SubContestRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Sub-Contest");
        confirm.setHeaderText("Remove " + row.getName() + "?");
        confirm.setContentText("This will remove this sub-contest and all associated results.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = dbManager.deleteSubContest(row.getId());
            if (success) {
                loadSubContests();
                loadContestDetails();
                showStatus("Sub-contest removed", true);
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadSubContests();
        loadStandings();
        loadContestDetails();
        showStatus("Refreshed!", true);
    }
    
    @FXML
    private void handleViewStandings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SelectionContestDetail.fxml"));
            Parent root = loader.load();
            
            SelectionContestDetailController controller = loader.getController();
            controller.setUser(currentUser);
            controller.setContest(currentContest);
            
            Stage stage = (Stage) subContestsTable.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception e) { }
            
            stage.setScene(scene);
            stage.setTitle("Contest Details - " + currentContest.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SetterDashboard.fxml"));
            Parent root = loader.load();
            
            SetterDashboardController controller = loader.getController();
            controller.setUser(currentUser);
            
            Stage stage = (Stage) subContestsTable.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 750);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception e) { }
            
            stage.setScene(scene);
            stage.setTitle("Setter Dashboard");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showStatus(String message, boolean success) {
        bottomStatusLabel.setText(message);
        bottomStatusLabel.setStyle("-fx-text-fill: " + (success ? "#27ae60" : "#e74c3c") + ";");
    }
    
    // ==================== ROW CLASSES ====================
    
    public static class SubContestRow {
        private final int id;
        private final SimpleIntegerProperty cfId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty date;
        private final SimpleStringProperty weight;
        private final SimpleIntegerProperty participants;
        private final SimpleStringProperty phase;
        
        public SubContestRow(int id, int cfId, String name, String date, String weight, int participants, String phase) {
            this.id = id;
            this.cfId = new SimpleIntegerProperty(cfId);
            this.name = new SimpleStringProperty(name);
            this.date = new SimpleStringProperty(date);
            this.weight = new SimpleStringProperty(weight);
            this.participants = new SimpleIntegerProperty(participants);
            this.phase = new SimpleStringProperty(phase);
        }
        
        public int getId() { return id; }
        public String getName() { return name.get(); }
        public String getWeight() { return weight.get(); }
        public SimpleIntegerProperty cfIdProperty() { return cfId; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty weightProperty() { return weight; }
        public SimpleIntegerProperty participantsProperty() { return participants; }
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
