package com.contestpredictor.model;

import java.time.LocalDateTime;

/**
 * Represents a user's result in a specific sub-contest.
 * Stores performance data fetched from Codeforces and calculated rating.
 */
public class ContestResult {
    private int id;
    private int registrationId;
    private int subContestId;
    private String codeforcesHandle;      // For reference
    private String subContestName;        // For display
    
    // Data from Codeforces
    private int cfRank;                   // Rank in the contest
    private double cfPoints;              // Total points scored
    private int cfPenalty;                // Penalty time
    private int problemsSolved;           // Number of problems solved
    private int cfOldRating;              // CF rating before contest
    private int cfNewRating;              // CF rating after contest
    private int cfRatingChange;           // CF rating delta
    
    // Calculated data
    private double calculatedRating;      // Rating from our algorithm
    private double weightedRating;        // calculatedRating * weight
    private LocalDateTime fetchedAt;
    private boolean isParticipated;       // Did the user actually participate?

    public ContestResult() {
        this.fetchedAt = LocalDateTime.now();
        this.isParticipated = false;
    }

    public ContestResult(int registrationId, int subContestId) {
        this();
        this.registrationId = registrationId;
        this.subContestId = subContestId;
    }

    public ContestResult(int id, int registrationId, int subContestId,
                         int cfRank, double cfPoints, int cfPenalty, int problemsSolved,
                         double calculatedRating, LocalDateTime fetchedAt) {
        this.id = id;
        this.registrationId = registrationId;
        this.subContestId = subContestId;
        this.cfRank = cfRank;
        this.cfPoints = cfPoints;
        this.cfPenalty = cfPenalty;
        this.problemsSolved = problemsSolved;
        this.calculatedRating = calculatedRating;
        this.fetchedAt = fetchedAt;
        this.isParticipated = cfRank > 0;
    }

    // Calculate weighted rating given the sub-contest weight
    public void calculateWeightedRating(double weight) {
        this.weightedRating = this.calculatedRating * weight;
    }

    // Check if user participated (non-zero rank or problems solved)
    public boolean didParticipate() {
        return isParticipated || cfRank > 0 || problemsSolved > 0;
    }

    // Get formatted penalty (HH:MM:SS)
    public String getFormattedPenalty() {
        int hours = cfPenalty / 3600;
        int minutes = (cfPenalty % 3600) / 60;
        int seconds = cfPenalty % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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

    public int getSubContestId() {
        return subContestId;
    }

    public void setSubContestId(int subContestId) {
        this.subContestId = subContestId;
    }

    public String getCodeforcesHandle() {
        return codeforcesHandle;
    }

    public void setCodeforcesHandle(String codeforcesHandle) {
        this.codeforcesHandle = codeforcesHandle;
    }

    public String getSubContestName() {
        return subContestName;
    }

    public void setSubContestName(String subContestName) {
        this.subContestName = subContestName;
    }

    public int getCfRank() {
        return cfRank;
    }

    public void setCfRank(int cfRank) {
        this.cfRank = cfRank;
        this.isParticipated = cfRank > 0;
    }

    public double getCfPoints() {
        return cfPoints;
    }

    public void setCfPoints(double cfPoints) {
        this.cfPoints = cfPoints;
    }

    public int getCfPenalty() {
        return cfPenalty;
    }

    public void setCfPenalty(int cfPenalty) {
        this.cfPenalty = cfPenalty;
    }

    public int getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(int problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public int getCfOldRating() {
        return cfOldRating;
    }

    public void setCfOldRating(int cfOldRating) {
        this.cfOldRating = cfOldRating;
    }

    public int getCfNewRating() {
        return cfNewRating;
    }

    public void setCfNewRating(int cfNewRating) {
        this.cfNewRating = cfNewRating;
    }

    public int getCfRatingChange() {
        return cfRatingChange;
    }

    public void setCfRatingChange(int cfRatingChange) {
        this.cfRatingChange = cfRatingChange;
    }

    public double getCalculatedRating() {
        return calculatedRating;
    }

    public void setCalculatedRating(double calculatedRating) {
        this.calculatedRating = calculatedRating;
    }

    public double getWeightedRating() {
        return weightedRating;
    }

    public void setWeightedRating(double weightedRating) {
        this.weightedRating = weightedRating;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public boolean isParticipated() {
        return isParticipated;
    }

    public void setParticipated(boolean participated) {
        isParticipated = participated;
    }

    @Override
    public String toString() {
        return String.format("Rank: %d, Solved: %d, Rating: %.2f", cfRank, problemsSolved, calculatedRating);
    }
}
