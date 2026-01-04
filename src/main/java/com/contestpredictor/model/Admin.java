package com.contestpredictor.model;

import java.time.LocalDateTime;

/**
 * Admin model for managing contests and system administration
 */
public class Admin {
    private int id;
    private String adminId;
    private String username;
    private String password; // In production, this should be hashed
    private String email;
    private String fullName;
    private LocalDateTime createdAt;
    private boolean isActive;
    
    // Default constructor
    public Admin() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Main constructor - simplified without email
    public Admin(String username, String password, String fullName) {
        this();
        this.adminId = generateAdminId();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }
    
    // Constructor for database loading with adminId
    public Admin(String adminId, String username, String password, String fullName) {
        this();
        this.adminId = adminId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }
    
    // Simplified constructor with username only
    public Admin(String username, String password) {
        this(username, password, username);
    }
    
    private static String generateAdminId() {
        return "ADMIN_" + System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getAdminId() {
        return adminId;
    }
    
    public void setAdminId(String adminId) {
        this.adminId = adminId;
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
        return "Admin{" +
                "username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
