package com.contestpredictor.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a user's final rating in a Selection Contest.
 * The final rating is the sum of all weighted sub-contest ratings.
 */
public class FinalRating {
    private int id;
    private int registrationId;
    private int selectionContestId;
    private int userId;
    private String username;                  // For display
    private String codeforcesHandle;          // For display
    private String selectionContestName;      // For display
    
    private double finalRating;               // Σ(subContestRating × weight)
    private int overallRank;
    private int contestsParticipated;         // Number of sub-contests participated
    private int totalSubContests;             // Total sub-contests in selection
    private LocalDateTime calculatedAt;
    
    private List<ContestResult> contestResults;  // Individual sub-contest results

    public FinalRating() {
        this.calculatedAt = LocalDateTime.now();
    }

    public FinalRating(int registrationId, int selectionContestId) {
        this();
        this.registrationId = registrationId;
        this.selectionContestId = selectionContestId;
    }

    public FinalRating(int id, int registrationId, int selectionContestId,
                       double finalRating, int overallRank, LocalDateTime calculatedAt) {
        this.id = id;
        this.registrationId = registrationId;
        this.selectionContestId = selectionContestId;
        this.finalRating = finalRating;
        this.overallRank = overallRank;
        this.calculatedAt = calculatedAt;
    }

    // Calculate final rating from contest results and weights
    public void calculateFromResults(List<ContestResult> results, List<SubContest> subContests) {
        this.contestResults = results;
        this.totalSubContests = subContests.size();
        this.contestsParticipated = 0;
        this.finalRating = 0.0;

        for (ContestResult result : results) {
            if (result.didParticipate()) {
                this.contestsParticipated++;
                // Find the matching sub-contest to get weight
                for (SubContest sc : subContests) {
                    if (sc.getId() == result.getSubContestId()) {
                        double weighted = result.getCalculatedRating() * sc.getWeight();
                        result.setWeightedRating(weighted);
                        this.finalRating += weighted;
                        break;
                    }
                }
            }
        }
    }

    // Get participation percentage
    public double getParticipationPercentage() {
        if (totalSubContests == 0) return 0.0;
        return (double) contestsParticipated / totalSubContests * 100.0;
    }

    // Get formatted final rating
    public String getFormattedRating() {
        return String.format("%.2f", finalRating);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getSelectionContestId() {
        return selectionContestId;
    }

    public void setSelectionContestId(int selectionContestId) {
        this.selectionContestId = selectionContestId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCodeforcesHandle() {
        return codeforcesHandle;
    }

    public void setCodeforcesHandle(String codeforcesHandle) {
        this.codeforcesHandle = codeforcesHandle;
    }

    public String getSelectionContestName() {
        return selectionContestName;
    }

    public void setSelectionContestName(String selectionContestName) {
        this.selectionContestName = selectionContestName;
    }

    public double getFinalRating() {
        return finalRating;
    }

    public void setFinalRating(double finalRating) {
        this.finalRating = finalRating;
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

    public int getTotalSubContests() {
        return totalSubContests;
    }

    public void setTotalSubContests(int totalSubContests) {
        this.totalSubContests = totalSubContests;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public List<ContestResult> getContestResults() {
        return contestResults;
    }

    public void setContestResults(List<ContestResult> contestResults) {
        this.contestResults = contestResults;
    }

    @Override
    public String toString() {
        return String.format("Rank #%d: %s (%.2f)", overallRank, username, finalRating);
    }
}
