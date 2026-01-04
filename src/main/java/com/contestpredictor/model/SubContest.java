package com.contestpredictor.model;

import java.time.LocalDateTime;

/**
 * Represents a Sub-Contest within a Selection Contest.
 * Each sub-contest corresponds to a Codeforces contest with an assigned weight.
 */
public class SubContest {
    private int id;
    private int selectionContestId;
    private int codeforcesContestId;      // Codeforces contest ID
    private String contestName;            // Fetched from CF API
    private LocalDateTime contestDate;     // Fetched from CF API
    private int durationSeconds;           // Duration in seconds
    private double weight;                 // Weight for rating calculation
    private boolean isFetched;             // Whether data has been fetched from CF
    private LocalDateTime addedAt;
    private String phase;                  // BEFORE, CODING, PENDING_SYSTEM_TEST, SYSTEM_TEST, FINISHED

    public SubContest() {
        this.weight = 1.0;
        this.isFetched = false;
        this.addedAt = LocalDateTime.now();
    }

    public SubContest(int codeforcesContestId) {
        this();
        this.codeforcesContestId = codeforcesContestId;
    }

    public SubContest(int codeforcesContestId, String contestName, double weight) {
        this();
        this.codeforcesContestId = codeforcesContestId;
        this.contestName = contestName;
        this.weight = weight;
    }

    public SubContest(int id, int selectionContestId, int codeforcesContestId,
                      String contestName, LocalDateTime contestDate, double weight,
                      boolean isFetched, LocalDateTime addedAt) {
        this.id = id;
        this.selectionContestId = selectionContestId;
        this.codeforcesContestId = codeforcesContestId;
        this.contestName = contestName;
        this.contestDate = contestDate;
        this.weight = weight;
        this.isFetched = isFetched;
        this.addedAt = addedAt;
    }

    // Check if contest is finished (can fetch standings)
    public boolean isFinished() {
        return "FINISHED".equals(phase);
    }

    // Get formatted duration
    public String getFormattedDuration() {
        if (durationSeconds <= 0) return "N/A";
        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        return String.format("%d:%02d", hours, minutes);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSelectionContestId() {
        return selectionContestId;
    }

    public void setSelectionContestId(int selectionContestId) {
        this.selectionContestId = selectionContestId;
    }

    public int getCodeforcesContestId() {
        return codeforcesContestId;
    }

    public void setCodeforcesContestId(int codeforcesContestId) {
        this.codeforcesContestId = codeforcesContestId;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public LocalDateTime getContestDate() {
        return contestDate;
    }

    public void setContestDate(LocalDateTime contestDate) {
        this.contestDate = contestDate;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isFetched() {
        return isFetched;
    }

    public void setFetched(boolean fetched) {
        isFetched = fetched;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    @Override
    public String toString() {
        return contestName != null ? contestName : "CF Contest #" + codeforcesContestId;
    }
}
