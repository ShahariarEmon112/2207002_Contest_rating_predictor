package com.contestpredictor.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a User in the Selection Contest system.
 * Users can be either CONTESTANT (participate in contests) or SETTER (create contests).
 */
public class User {
    
    /**
     * User roles in the system
     */
    public enum UserRole {
        CONTESTANT("Contestant"),  // Participates in contests
        SETTER("Setter");          // Creates and manages contests
        
        private final String displayName;
        
        UserRole(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private UserRole role;                    // CONTESTANT or SETTER
    private String codeforcesHandle;          // Codeforces username
    private int currentRating;
    private int contestsParticipated;
    private List<Integer> ratingHistory;
    private LocalDateTime createdAt;
    private boolean isActive;

    public User() {
        this.ratingHistory = new ArrayList<>();
        this.currentRating = 1500;
        this.contestsParticipated = 0;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.role = UserRole.CONTESTANT; // Default role
    }

    public User(String username, String password, String fullName) {
        this();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.ratingHistory.add(this.currentRating);
    }
    
    public User(String username, String password, String fullName, UserRole role) {
        this(username, password, fullName);
        this.role = role;
    }

    public User(String username, String password, int currentRating, int contestsParticipated, String fullName) {
        this();
        this.username = username;
        this.password = password;
        this.currentRating = currentRating;
        this.contestsParticipated = contestsParticipated;
        this.fullName = fullName;
        this.ratingHistory.add(currentRating);
    }

    // Add a new rating to history
    public void addRatingToHistory(int rating) {
        this.ratingHistory.add(rating);
        this.currentRating = rating;
    }

    // Increment contest participation
    public void incrementContestsParticipated() {
        this.contestsParticipated++;
    }

    // Get rating change since last contest
    public int getLastRatingChange() {
        if (ratingHistory.size() < 2) {
            return 0;
        }
        int size = ratingHistory.size();
        return ratingHistory.get(size - 1) - ratingHistory.get(size - 2);
    }

    // Get rank title based on rating (CF-style)
    public String getRankTitle() {
        if (currentRating >= 2400) return "Grandmaster";
        if (currentRating >= 2100) return "Master";
        if (currentRating >= 1900) return "Candidate Master";
        if (currentRating >= 1600) return "Expert";
        if (currentRating >= 1400) return "Specialist";
        if (currentRating >= 1200) return "Pupil";
        return "Newbie";
    }

    // Get rank color based on rating (CF-style hex colors)
    public String getRankColor() {
        if (currentRating >= 2400) return "#FF0000"; // Red
        if (currentRating >= 2100) return "#FF8C00"; // Orange
        if (currentRating >= 1900) return "#AA00AA"; // Violet
        if (currentRating >= 1600) return "#0000FF"; // Blue
        if (currentRating >= 1400) return "#03A89E"; // Cyan
        if (currentRating >= 1200) return "#008000"; // Green
        return "#808080"; // Gray
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public boolean isContestant() {
        return role == UserRole.CONTESTANT;
    }
    
    public boolean isSetter() {
        return role == UserRole.SETTER;
    }

    public String getCodeforcesHandle() {
        return codeforcesHandle;
    }

    public void setCodeforcesHandle(String codeforcesHandle) {
        this.codeforcesHandle = codeforcesHandle;
    }

    public int getCurrentRating() {
        return currentRating;
    }

    public void setCurrentRating(int currentRating) {
        this.currentRating = currentRating;
    }

    public int getContestsParticipated() {
        return contestsParticipated;
    }

    public void setContestsParticipated(int contestsParticipated) {
        this.contestsParticipated = contestsParticipated;
    }

    public List<Integer> getRatingHistory() {
        return ratingHistory;
    }

    public void setRatingHistory(List<Integer> ratingHistory) {
        this.ratingHistory = ratingHistory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return username + " (" + fullName + ") - " + role.getDisplayName();
    }
}
