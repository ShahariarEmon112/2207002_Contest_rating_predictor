package com.contestpredictor.model;

import java.time.LocalDateTime;

/**
 * Represents a user's registration to a Selection Contest.
 * Links a user to a selection contest with their Codeforces handle.
 */
public class Registration {
    private int id;
    private int userId;
    private String username;              // For display
    private int selectionContestId;
    private String selectionContestName;  // For display
    private String codeforcesHandle;      // CF handle at registration time
    private LocalDateTime registeredAt;
    private RegistrationStatus status;

    public enum RegistrationStatus {
        ACTIVE("Active"),
        WITHDRAWN("Withdrawn"),
        DISQUALIFIED("Disqualified");

        private final String displayName;

        RegistrationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static RegistrationStatus fromString(String text) {
            for (RegistrationStatus status : RegistrationStatus.values()) {
                if (status.name().equalsIgnoreCase(text)) {
                    return status;
                }
            }
            return ACTIVE;
        }
    }

    public Registration() {
        this.registeredAt = LocalDateTime.now();
        this.status = RegistrationStatus.ACTIVE;
    }

    public Registration(int userId, int selectionContestId, String codeforcesHandle) {
        this();
        this.userId = userId;
        this.selectionContestId = selectionContestId;
        this.codeforcesHandle = codeforcesHandle;
    }

    public Registration(int id, int userId, int selectionContestId, String codeforcesHandle,
                        LocalDateTime registeredAt, RegistrationStatus status) {
        this.id = id;
        this.userId = userId;
        this.selectionContestId = selectionContestId;
        this.codeforcesHandle = codeforcesHandle;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    // Check if registration is valid for participation
    public boolean isValid() {
        return status == RegistrationStatus.ACTIVE;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getSelectionContestId() {
        return selectionContestId;
    }

    public void setSelectionContestId(int selectionContestId) {
        this.selectionContestId = selectionContestId;
    }

    public String getSelectionContestName() {
        return selectionContestName;
    }

    public void setSelectionContestName(String selectionContestName) {
        this.selectionContestName = selectionContestName;
    }

    public String getCodeforcesHandle() {
        return codeforcesHandle;
    }

    public void setCodeforcesHandle(String codeforcesHandle) {
        this.codeforcesHandle = codeforcesHandle;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return username + " - " + codeforcesHandle + " (" + status.getDisplayName() + ")";
    }
}
