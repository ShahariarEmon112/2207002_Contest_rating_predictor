package com.contestpredictor.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Selection Contest - a parent contest containing multiple sub-contests.
 * Each sub-contest is a Codeforces contest with an assigned weight.
 */
public class SelectionContest {
    private int id;
    private String contestCode;          // Unique code for joining (e.g., "SC2026-001")
    private String shareKey;             // Auto-generated key for sharing/searching (e.g., "ABC123")
    private String name;
    private String description;
    private int createdByAdminId;        // Can be setter or admin ID
    private String createdByAdminName;   // For display purposes
    private LocalDateTime createdAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
    private List<SubContest> subContests;
    private int registrationCount;

    public SelectionContest() {
        this.subContests = new ArrayList<>();
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.shareKey = generateShareKey();
    }
    
    // Generate a random 6-character alphanumeric key
    private static String generateShareKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder key = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            key.append(chars.charAt(random.nextInt(chars.length())));
        }
        return key.toString();
    }

    public SelectionContest(String contestCode, String name, String description) {
        this();
        this.contestCode = contestCode;
        this.name = name;
        this.description = description;
    }

    public SelectionContest(int id, String contestCode, String name, String description,
                           int createdByAdminId, LocalDateTime createdAt,
                           LocalDateTime startDate, LocalDateTime endDate, boolean isActive) {
        this.id = id;
        this.contestCode = contestCode;
        this.name = name;
        this.description = description;
        this.createdByAdminId = createdByAdminId;
        this.createdAt = createdAt;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.subContests = new ArrayList<>();
    }

    // Calculate total weight of all sub-contests
    public double getTotalWeight() {
        return subContests.stream().mapToDouble(SubContest::getWeight).sum();
    }

    // Get number of sub-contests
    public int getSubContestCount() {
        return subContests.size();
    }

    // Check if contest is currently running
    public boolean isRunning() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               (startDate == null || now.isAfter(startDate)) && 
               (endDate == null || now.isBefore(endDate));
    }

    // Check if registration is open
    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && (endDate == null || now.isBefore(endDate));
    }

    // Add a sub-contest
    public void addSubContest(SubContest subContest) {
        subContest.setSelectionContestId(this.id);
        this.subContests.add(subContest);
    }

    // Remove a sub-contest
    public boolean removeSubContest(int subContestId) {
        return subContests.removeIf(sc -> sc.getId() == subContestId);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContestCode() {
        return contestCode;
    }

    public void setContestCode(String contestCode) {
        this.contestCode = contestCode;
    }

    public String getShareKey() {
        return shareKey;
    }

    public void setShareKey(String shareKey) {
        this.shareKey = shareKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreatedByAdminId() {
        return createdByAdminId;
    }

    public void setCreatedByAdminId(int createdByAdminId) {
        this.createdByAdminId = createdByAdminId;
    }

    public String getCreatedByAdminName() {
        return createdByAdminName;
    }

    public void setCreatedByAdminName(String createdByAdminName) {
        this.createdByAdminName = createdByAdminName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<SubContest> getSubContests() {
        return subContests;
    }

    public void setSubContests(List<SubContest> subContests) {
        this.subContests = subContests;
    }

    public int getRegistrationCount() {
        return registrationCount;
    }

    public void setRegistrationCount(int registrationCount) {
        this.registrationCount = registrationCount;
    }

    @Override
    public String toString() {
        return name + " (" + contestCode + ")";
    }
}
