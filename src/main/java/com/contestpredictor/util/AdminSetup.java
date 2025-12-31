package com.contestpredictor.util;

import com.contestpredictor.data.AdminDatabase;

import java.util.Scanner;

/**
 * Utility class for setting up admin accounts
 * Run this class directly to create new admin accounts
 */
public class AdminSetup {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AdminDatabase adminDB = AdminDatabase.getInstance();
        
        System.out.println("========================================");
        System.out.println("   Contest Predictor - Admin Setup");
        System.out.println("========================================\n");
        
        // Display default admin info
        System.out.println("Default Admin Account:");
        System.out.println("  Username: admin");
        System.out.println("  Password: admin123");
        System.out.println();
        
        System.out.print("Do you want to create a new admin account? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        
        if (!response.equals("yes") && !response.equals("y")) {
            System.out.println("Setup cancelled.");
            scanner.close();
            return;
        }
        
        System.out.println("\nEnter new admin details:");
        
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            System.out.println("Error: Username cannot be empty!");
            scanner.close();
            return;
        }
        
        // Check if username already exists
        if (adminDB.adminExists(username)) {
            System.out.println("Error: Username '" + username + "' already exists!");
            scanner.close();
            return;
        }
        
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        if (password.isEmpty()) {
            System.out.println("Error: Password cannot be empty!");
            scanner.close();
            return;
        }
        
        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();
        
        if (fullName.isEmpty()) {
            fullName = username; // Default to username if not provided
        }
        
        // Register the admin (no email required)
        boolean success = adminDB.registerAdmin(username, password, fullName);
        
        if (success) {
            System.out.println("\n✓ Admin account created successfully!");
            System.out.println("  Username: " + username);
            System.out.println("  Full Name: " + fullName);
            System.out.println("\nYou can now login using the Admin Login button.");
        } else {
            System.out.println("\n✗ Failed to create admin account!");
        }
        
        scanner.close();
    }
}
