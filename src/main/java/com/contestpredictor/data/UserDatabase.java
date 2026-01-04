package com.contestpredictor.data;

import com.contestpredictor.model.User;
import com.contestpredictor.model.User.UserRole;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class UserDatabase {
    private static UserDatabase instance;
    private Map<String, User> users;
    private User currentUser;
    private DatabaseManager dbManager;

    private UserDatabase() {
        users = new HashMap<>();
        dbManager = DatabaseManager.getInstance();
        initializeUsers();
    }

    public static UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    private void initializeUsers() {
        // Creating 30 predefined user accounts
        // Format: username, password, currentRating, contestsParticipated, fullName
        
        // Try to load from database first, if not exist create new
        createOrLoadUser("user001", "pass001", 1200, 15, "Alex Johnson");
        createOrLoadUser("user002", "pass002", 1450, 22, "Sarah Williams");
        createOrLoadUser("user003", "pass003", 980, 8, "Michael Chen");
        createOrLoadUser("user004", "pass004", 1650, 35, "Emily Davis");
        createOrLoadUser("user005", "pass005", 1100, 12, "David Martinez");
        
        createOrLoadUser("user006", "pass006", 1820, 48, "Jessica Brown");
        createOrLoadUser("user007", "pass007", 750, 5, "Christopher Lee");
        createOrLoadUser("user008", "pass008", 1550, 28, "Amanda Taylor");
        createOrLoadUser("user009", "pass009", 1380, 19, "Daniel Garcia");
        createOrLoadUser("user010", "pass010", 1920, 55, "Jennifer Wilson");
        
        createOrLoadUser("user011", "pass011", 1050, 10, "Matthew Anderson");
        createOrLoadUser("user012", "pass012", 1700, 40, "Lisa Thomas");
        createOrLoadUser("user013", "pass013", 890, 7, "Ryan Moore");
        createOrLoadUser("user014", "pass014", 1490, 25, "Nicole Jackson");
        createOrLoadUser("user015", "pass015", 1250, 17, "Kevin White");
        
        createOrLoadUser("user016", "pass016", 1600, 32, "Rachel Harris");
        createOrLoadUser("user017", "pass017", 1150, 14, "Brandon Martin");
        createOrLoadUser("user018", "pass018", 1780, 45, "Michelle Thompson");
        createOrLoadUser("user019", "pass019", 950, 9, "Jason Garcia");
        createOrLoadUser("user020", "pass020", 2050, 60, "Lauren Martinez");
        
        createOrLoadUser("user021", "pass021", 1320, 20, "Andrew Robinson");
        createOrLoadUser("user022", "pass022", 1470, 26, "Stephanie Clark");
        createOrLoadUser("user023", "pass023", 820, 6, "Justin Rodriguez");
        createOrLoadUser("user024", "pass024", 1590, 30, "Ashley Lewis");
        createOrLoadUser("user025", "pass025", 1210, 16, "Tyler Lee");
        
        createOrLoadUser("user026", "pass026", 1850, 50, "Megan Walker");
        createOrLoadUser("user027", "pass027", 1080, 11, "Eric Hall");
        createOrLoadUser("user028", "pass028", 1680, 38, "Brittany Allen");
        createOrLoadUser("user029", "pass029", 1410, 23, "Jonathan Young");
        createOrLoadUser("user030", "pass030", 1950, 58, "Samantha King");
    }
    
    private void createOrLoadUser(String username, String password, int rating, int contests, String fullName) {
        User user = dbManager.loadUser(username);
        if (user == null) {
            user = new User(username, password, rating, contests, fullName);
            dbManager.saveUser(user);
            // Reload to get the ID
            user = dbManager.loadUser(username);
        }
        if (user != null) {
            users.put(username, user);
        }
    }

    public User authenticate(String username, String password) {
        // First try to load from database for fresh data
        User user = dbManager.loadUser(username);
        if (user == null) {
            user = users.get(username);
        }
        
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            users.put(username, user); // Update cache
            return user;
        }
        return null;
    }

    public boolean registerUser(String username, String password, String fullName) {
        return registerUserWithRole(username, password, fullName, null, UserRole.CONTESTANT);
    }
    
    public boolean registerUserWithRole(String username, String password, String fullName, String email, UserRole role) {
        // Check if username already exists
        if (users.containsKey(username) || dbManager.loadUser(username) != null) {
            return false;
        }
        
        // Create new user with appropriate role
        User newUser = new User(username, password, fullName, role);
        newUser.setEmail(email);
        newUser.setCurrentRating(1000);
        newUser.setContestsParticipated(0);
        
        // Save to database
        boolean saved = dbManager.saveUser(newUser);
        if (saved) {
            users.put(username, newUser);
            currentUser = newUser;
        }
        
        return saved;
    }
    
    public List<User> getSetters() {
        List<User> setters = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getRole() == UserRole.SETTER) {
                setters.add(user);
            }
        }
        return setters;
    }
    
    public List<User> getContestants() {
        List<User> contestants = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getRole() == UserRole.CONTESTANT) {
                contestants.add(user);
            }
        }
        return contestants;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Save updated user to database
        if (user != null) {
            dbManager.saveUser(user);
        }
    }

    public Map<String, User> getAllUsers() {
        return users;
    }
    
    public User getUser(String username) {
        return users.get(username);
    }
    
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    public void logout() {
        currentUser = null;
    }
}
