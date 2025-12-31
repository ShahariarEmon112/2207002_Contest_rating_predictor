package com.contestpredictor.model;

/**
 * Contestant model for dynamic rating prediction
 * Represents a participant from Codeforces API with rating calculation fields
 */
public class Contestant {
    private String handle;
    private int oldRating;
    private int rank;
    private int problemsSolved;
    private int penalty;
    private int delta;
    private int newRating;
    
    public Contestant(String handle, int oldRating, int rank, int problemsSolved, int penalty) {
        this.handle = handle;
        this.oldRating = oldRating;
        this.rank = rank;
        this.problemsSolved = problemsSolved;
        this.penalty = penalty;
        this.delta = 0;
        this.newRating = oldRating;
    }
    
    // Constructor for API fetching (rank and contest data will be assigned later)
    public Contestant(String handle, int oldRating) {
        this(handle, oldRating, 0, 0, 0);
    }
    
    // Getters and Setters
    public String getHandle() {
        return handle;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    public int getOldRating() {
        return oldRating;
    }
    
    public void setOldRating(int oldRating) {
        this.oldRating = oldRating;
        this.newRating = oldRating + delta;
    }
    
    public int getRank() {
        return rank;
    }
    
    public void setRank(int rank) {
        this.rank = rank;
    }
    
    public int getProblemsSolved() {
        return problemsSolved;
    }
    
    public void setProblemsSolved(int problemsSolved) {
        this.problemsSolved = problemsSolved;
    }
    
    public int getPenalty() {
        return penalty;
    }
    
    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }
    
    public int getDelta() {
        return delta;
    }
    
    public void setDelta(int delta) {
        this.delta = delta;
        this.newRating = oldRating + delta;
    }
    
    public int getNewRating() {
        return newRating;
    }
    
    public void setNewRating(int newRating) {
        this.newRating = newRating;
        this.delta = newRating - oldRating;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %d → %d (Δ%+d)", 
                             handle, oldRating, newRating, delta);
    }
}
