package com.contestpredictor.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an audit log entry for tracking system actions.
 * Logs registration, login, contest creation, and other important actions.
 */
public class AuditLog {
    private int id;
    private ActionType actionType;
    private Integer userId;               // Null for admin actions
    private Integer adminId;              // Null for user actions
    private String actorName;             // Username or admin name
    private EntityType entityType;
    private Integer entityId;
    private String entityName;            // For display
    private String details;               // JSON or text details
    private String ipAddress;
    private LocalDateTime createdAt;

    public enum ActionType {
        USER_REGISTER("User Registration"),
        USER_LOGIN("User Login"),
        USER_LOGOUT("User Logout"),
        ADMIN_LOGIN("Admin Login"),
        ADMIN_LOGOUT("Admin Logout"),
        CONTEST_CREATE("Contest Created"),
        CONTEST_UPDATE("Contest Updated"),
        CONTEST_DELETE("Contest Deleted"),
        SUBCONTEST_ADD("Sub-Contest Added"),
        SUBCONTEST_REMOVE("Sub-Contest Removed"),
        SUBCONTEST_WEIGHT_UPDATE("Weight Updated"),
        REGISTRATION_CREATE("Registration Created"),
        REGISTRATION_WITHDRAW("Registration Withdrawn"),
        RATING_CALCULATE("Rating Calculated"),
        STANDINGS_FETCH("Standings Fetched");

        private final String displayName;

        ActionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EntityType {
        USER,
        ADMIN,
        SELECTION_CONTEST,
        SUB_CONTEST,
        REGISTRATION,
        RATING
    }

    public AuditLog() {
        this.createdAt = LocalDateTime.now();
    }

    public AuditLog(ActionType actionType, Integer userId, Integer adminId, 
                    EntityType entityType, Integer entityId, String details) {
        this();
        this.actionType = actionType;
        this.userId = userId;
        this.adminId = adminId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    // Builder pattern for easier creation
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private AuditLog log = new AuditLog();

        public AuditLogBuilder action(ActionType actionType) {
            log.actionType = actionType;
            return this;
        }

        public AuditLogBuilder user(Integer userId, String username) {
            log.userId = userId;
            log.actorName = username;
            return this;
        }

        public AuditLogBuilder admin(Integer adminId, String adminName) {
            log.adminId = adminId;
            log.actorName = adminName;
            return this;
        }

        public AuditLogBuilder entity(EntityType type, Integer id, String name) {
            log.entityType = type;
            log.entityId = id;
            log.entityName = name;
            return this;
        }

        public AuditLogBuilder details(String details) {
            log.details = details;
            return this;
        }

        public AuditLogBuilder ip(String ipAddress) {
            log.ipAddress = ipAddress;
            return this;
        }

        public AuditLog build() {
            return log;
        }
    }

    // Get formatted timestamp
    public String getFormattedTimestamp() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Get actor display name (user or admin)
    public String getActorDisplay() {
        if (actorName != null) return actorName;
        if (userId != null) return "User #" + userId;
        if (adminId != null) return "Admin #" + adminId;
        return "System";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s - %s", 
            getFormattedTimestamp(), 
            actionType.getDisplayName(),
            getActorDisplay(),
            details != null ? details : "");
    }
}
