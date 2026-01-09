package com.contestpredictor.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for leaderboard contests managed by admins
 * Part of the "KUET Team Formation Contest Leaderboard" system
 */
public class LeaderboardContest {
    private String contestId;
    private String contestName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int maxProblems; // Number of problems in the contest
    private boolean isActive; // Whether this contest is currently accepting registrations
    private boolean standings_finalized; // Whether final standings have been determined
    private String createdByAdmin;
    private LocalDateTime createdAt;
    private List<String> registeredUsers; // Usernames of registered participants
    private int totalParticipants;

    public LeaderboardContest(String contestId, String contestName, String description, 
                             LocalDateTime startDate, LocalDateTime endDate, 
                             int maxProblems, String createdByAdmin) {
        this.contestId = contestId;
        this.contestName = contestName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxProblems = maxProblems;
        this.createdByAdmin = createdByAdmin;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.standings_finalized = false;
        this.registeredUsers = new ArrayList<>();
        this.totalParticipants = 0;
    }

    // Getters and Setters
    public String getContestId() {
        return contestId;
    }

    public void setContestId(String contestId) {
        this.contestId = contestId;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getMaxProblems() {
        return maxProblems;
    }

    public void setMaxProblems(int maxProblems) {
        this.maxProblems = maxProblems;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isStandings_finalized() {
        return standings_finalized;
    }

    public void setStandings_finalized(boolean standings_finalized) {
        this.standings_finalized = standings_finalized;
    }

    public String getCreatedByAdmin() {
        return createdByAdmin;
    }

    public void setCreatedByAdmin(String createdByAdmin) {
        this.createdByAdmin = createdByAdmin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getRegisteredUsers() {
        return registeredUsers;
    }

    public void setRegisteredUsers(List<String> registeredUsers) {
        this.registeredUsers = registeredUsers;
    }

    public boolean registerUser(String username) {
        if (!isActive || registeredUsers.contains(username)) {
            return false;
        }
        registeredUsers.add(username);
        totalParticipants++;
        return true;
    }

    public boolean unregisterUser(String username) {
        if (registeredUsers.remove(username)) {
            totalParticipants--;
            return true;
        }
        return false;
    }

    public boolean isUserRegistered(String username) {
        return registeredUsers.contains(username);
    }

    public int getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(int totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    @Override
    public String toString() {
        return "LeaderboardContest{" +
                "contestId='" + contestId + '\'' +
                ", contestName='" + contestName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isActive=" + isActive +
                ", standings_finalized=" + standings_finalized +
                '}';
    }
}
