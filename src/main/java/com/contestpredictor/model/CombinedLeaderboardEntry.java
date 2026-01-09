package com.contestpredictor.model;

/**
 * Model for combined leaderboard entries across all contests
 * Represents a user's aggregate standing across all leaderboard contests
 */
public class CombinedLeaderboardEntry {
    private String username;
    private int totalSolves; // Total problems solved across all contests
    private int totalPenalty; // Total penalty across all contests
    private int overallRank; // Overall rank in the combined leaderboard
    private int contestsParticipated; // Number of contests participated in
    private String status; // Status in combined leaderboard

    public CombinedLeaderboardEntry(String username, int totalSolves, int totalPenalty, 
                                   int overallRank, int contestsParticipated) {
        this.username = username;
        this.totalSolves = totalSolves;
        this.totalPenalty = totalPenalty;
        this.overallRank = overallRank;
        this.contestsParticipated = contestsParticipated;
        this.status = "Active";
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalSolves() {
        return totalSolves;
    }

    public void setTotalSolves(int totalSolves) {
        this.totalSolves = totalSolves;
    }

    public int getTotalPenalty() {
        return totalPenalty;
    }

    public void setTotalPenalty(int totalPenalty) {
        this.totalPenalty = totalPenalty;
    }

    public int getOverallRank() {
        return overallRank;
    }

    public void setOverallRank(int overallRank) {
        this.overallRank = overallRank;
    }

    public int getContestsParticipated() {
        return contestsParticipated;
    }

    public void setContestsParticipated(int contestsParticipated) {
        this.contestsParticipated = contestsParticipated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CombinedLeaderboardEntry{" +
                "username='" + username + '\'' +
                ", totalSolves=" + totalSolves +
                ", totalPenalty=" + totalPenalty +
                ", overallRank=" + overallRank +
                ", contestsParticipated=" + contestsParticipated +
                '}';
    }
}
