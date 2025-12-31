package com.contestpredictor.data;

import com.contestpredictor.model.Admin;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Database operations for Admin authentication and management
 */
public class AdminDatabase {
    private static AdminDatabase instance;
    private Connection connection;
    
    private AdminDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:contest_predictor.db");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }
    
    public static AdminDatabase getInstance() {
        if (instance == null) {
            instance = new AdminDatabase();
        }
        return instance;
    }
    
    /**
     * Authenticate admin user
     * @param username Admin username
     * @param password Admin password
     * @return Admin object if authenticated, null otherwise
     */
    public Admin authenticate(String username, String password) {
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ? AND is_active = 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Admin admin = new Admin(
                    rs.getString("admin_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name")
                );
                
                String createdAtStr = rs.getString("created_at");
                if (createdAtStr != null) {
                    admin.setCreatedAt(LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                
                admin.setActive(rs.getInt("is_active") == 1);
                
                return admin;
            }
        } catch (SQLException e) {
            System.err.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Save or update admin (simplified without email)
     */
    public boolean saveAdmin(Admin admin) {
        String sql = "INSERT OR REPLACE INTO admins (admin_id, username, password, email, full_name, created_at, is_active) " +
                     "VALUES (?, ?, ?, '', ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, admin.getAdminId());
            pstmt.setString(2, admin.getUsername());
            pstmt.setString(3, admin.getPassword());
            pstmt.setString(4, admin.getFullName());
            pstmt.setString(5, admin.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setInt(6, admin.isActive() ? 1 : 0);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to save admin: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if admin username exists
     */
    public boolean adminExists(String username) {
        String sql = "SELECT COUNT(*) FROM admins WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking admin existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get admin by username
     */
    public Admin getAdminByUsername(String username) {
        String sql = "SELECT * FROM admins WHERE username = ? AND is_active = 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Admin admin = new Admin(
                    rs.getString("admin_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name")
                );
                
                String createdAtStr = rs.getString("created_at");
                if (createdAtStr != null) {
                    admin.setCreatedAt(LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                
                admin.setActive(rs.getInt("is_active") == 1);
                
                return admin;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching admin: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Register a new admin (simplified without email)
     * @param username Admin username
     * @param password Admin password
     * @param fullName Admin full name
     * @return true if registration successful, false if username exists
     */
    public boolean registerAdmin(String username, String password, String fullName) {
        // Check if username already exists
        if (adminExists(username)) {
            return false;
        }
        
        Admin admin = new Admin(username, password, fullName);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setActive(true);
        
        return saveAdmin(admin);
    }
    
    /**
     * Deactivate admin account
     */
    public boolean deactivateAdmin(String username) {
        String sql = "UPDATE admins SET is_active = 0 WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deactivating admin: " + e.getMessage());
            return false;
        }
    }
}
