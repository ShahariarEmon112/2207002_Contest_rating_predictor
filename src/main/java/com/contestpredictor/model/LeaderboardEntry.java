package com.contestpredictor.model;

/**
 * Model for individual leaderboard entries
 * Represents a user's standing in a specific contest
 */
public class LeaderboardEntry {
    private String username;
    private String contestId;
    private int rank; // Rank in the specific contest
    private int solveCount; // Number of problems solved
    private int totalPenalty; // Total time penalty
    private long totalTime; // Total time spent (in milliseconds or minutes)
    private String status; // Status like "Solved", "Attempted", etc.

    public LeaderboardEntry(String username, String contestId, int rank, 
                           int solveCount, int totalPenalty, long totalTime) {
        this.username = username;
        this.contestId = contestId;
        this.rank = rank;
        this.solveCount = solveCount;
        this.totalPenalty = totalPenalty;
        this.totalTime = totalTime;
        this.status = "Completed";
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContestId() {
        return contestId;
    }

    public void setContestId(String contestId) {
        this.contestId = contestId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getSolveCount() {
        return solveCount;
    }

    public void setSolveCount(int solveCount) {
        this.solveCount = solveCount;
    }

    public int getTotalPenalty() {
        return totalPenalty;
    }

    public void setTotalPenalty(int totalPenalty) {
        this.totalPenalty = totalPenalty;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "LeaderboardEntry{" +
                "username='" + username + '\'' +
                ", contestId='" + contestId + '\'' +
                ", rank=" + rank +
                ", solveCount=" + solveCount +
                ", totalPenalty=" + totalPenalty +
                '}';
    }
}
