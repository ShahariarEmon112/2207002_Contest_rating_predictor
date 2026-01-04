package com.contestpredictor.data;

import com.contestpredictor.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Manager for the Selection Contest Rating System.
 * Handles all database operations for users, admins, selection contests,
 * sub-contests, registrations, results, and audit logs.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:contest_predictor.db";
    private Connection connection;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeTables();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Initialize all database tables
     */
    private void initializeTables() {
        try {
            Statement stmt = connection.createStatement();

            // ==================== MIGRATE OLD USERS TABLE ====================
            // Check if users table has old schema (password instead of password_hash)
            try {
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)");
                boolean hasPasswordHash = false;
                boolean hasPassword = false;
                while (rs.next()) {
                    String colName = rs.getString("name");
                    if ("password_hash".equals(colName)) hasPasswordHash = true;
                    if ("password".equals(colName)) hasPassword = true;
                }
                rs.close();
                
                // If old schema exists, migrate it
                if (hasPassword && !hasPasswordHash) {
                    System.out.println("Migrating users table to new schema...");
                    stmt.execute("ALTER TABLE users RENAME TO users_old");
                    stmt.execute("CREATE TABLE users (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "username TEXT UNIQUE NOT NULL," +
                            "password_hash TEXT NOT NULL," +
                            "email TEXT," +
                            "full_name TEXT NOT NULL," +
                            "role TEXT DEFAULT 'CONTESTANT'," +
                            "codeforces_handle TEXT," +
                            "current_rating INTEGER DEFAULT 1500," +
                            "contests_participated INTEGER DEFAULT 0," +
                            "rating_history TEXT," +
                            "created_at TEXT NOT NULL," +
                            "is_active INTEGER DEFAULT 1" +
                            ")");
                    // Copy old data
                    stmt.execute("INSERT INTO users (username, password_hash, full_name, current_rating, contests_participated, created_at, is_active) " +
                            "SELECT username, password, COALESCE(full_name, username), COALESCE(current_rating, 1500), COALESCE(contests_participated, 0), " +
                            "COALESCE(created_at, datetime('now')), 1 FROM users_old");
                    stmt.execute("DROP TABLE users_old");
                    System.out.println("Users table migration complete!");
                }
            } catch (SQLException e) {
                // Table doesn't exist yet, will be created below
            }

            // ==================== USERS TABLE ====================
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL," +
                    "email TEXT," +
                    "full_name TEXT NOT NULL," +
                    "role TEXT DEFAULT 'CONTESTANT'," +
                    "codeforces_handle TEXT," +
                    "current_rating INTEGER DEFAULT 1500," +
                    "contests_participated INTEGER DEFAULT 0," +
                    "rating_history TEXT," +
                    "created_at TEXT NOT NULL," +
                    "is_active INTEGER DEFAULT 1" +
                    ")");
            
            // Add role column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN role TEXT DEFAULT 'CONTESTANT'");
            } catch (Exception e) {
                // Column already exists, ignore
            }

            // ==================== ADMINS TABLE ====================
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL," +
                    "email TEXT," +
                    "full_name TEXT NOT NULL," +
                    "created_at TEXT NOT NULL," +
                    "is_active INTEGER DEFAULT 1" +
                    ")");

            // ==================== SELECTION CONTESTS TABLE ====================
            stmt.execute("CREATE TABLE IF NOT EXISTS selection_contests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "contest_code TEXT UNIQUE NOT NULL," +
                    "share_key TEXT UNIQUE," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "created_by_admin_id INTEGER," +
                    "created_at TEXT NOT NULL," +
                    "start_date TEXT," +
                    "end_date TEXT," +
                    "is_active INTEGER DEFAULT 1," +
                    "FOREIGN KEY (created_by_admin_id) REFERENCES users(id)" +
                    ")");
            
            // Add share_key column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE selection_contests ADD COLUMN share_key TEXT UNIQUE");
            } catch (SQLException e) {
                // Column already exists
            };

            // ==================== SUB CONTESTS TABLE ====================
            stmt.execute("CREATE TABLE IF NOT EXISTS sub_contests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "selection_contest_id INTEGER NOT NULL," +
                    "codeforces_contest_id INTEGER NOT NULL," +
                    "contest_name TEXT," +
                    "contest_date TEXT," +
                    "duration_seconds INTEGER DEFAULT 0," +
                    "weight REAL DEFAULT 1.0," +
                    "phase TEXT," +
                    "is_fetched INTEGER DEFAULT 0," +
                    "added_at TEXT NOT NULL," +
                    "FOREIGN KEY (selection_contest_id) REFERENCES selection_contests(id) ON DELETE CASCADE," +
                    "UNIQUE(selection_contest_id, codeforces_contest_id)" +
                    ")");

            // ==================== REGISTRATIONS TABLE ====================
            stmt.execute("CREATE TABLE IF NOT EXISTS registrations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "selection_contest_id INTEGER NOT NULL," +
                    "codeforces_handle TEXT NOT NULL," +
                    "registered_at TEXT NOT NULL," +
                    "status TEXT DEFAULT 'ACTIVE'," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (selection_contest_id) REFERENCES selection_contests(id) ON DELETE CASCADE," +
                    "UNIQUE(user_id, selection_contest_id)" +
                    ")");

            // ==================== CONTEST RESULTS TABLE ====================
            stmt.execute("CREATE TABLE IF NOT EXISTS contest_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "registration_id INTEGER NOT NULL," +
                    "sub_contest_id INTEGER NOT NULL," +
                    "cf_rank INTEGER DEFAULT 0," +
                    "cf_points REAL DEFAULT 0," +
                    "cf_penalty INTEGER DEFAULT 0," +
                    "problems_solved INTEGER DEFAULT 0," +
                    "cf_old_rating INTEGER DEFAULT 0," +
                    "cf_new_rating INTEGER DEFAULT 0," +
                    "cf_rating_change INTEGER DEFAULT 0," +
                    "calculated_rating REAL DEFAULT 0," +
                    "weighted_rating REAL DEFAULT 0," +
                    "is_participated INTEGER DEFAULT 0," +
                    "fetched_at TEXT," +
                    "FOREIGN KEY (registration_id) REFERENCES registrations(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (sub_contest_id) REFERENCES sub_contests(id) ON DELETE CASCADE," +
                    "UNIQUE(registration_id, sub_contest_id)" +
                    ")");

            // ==================== FINAL RATINGS TABLE ====================
            stmt.execute("CREATE TABLE IF NOT EXISTS final_ratings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "registration_id INTEGER NOT NULL," +
                    "selection_contest_id INTEGER NOT NULL," +
                    "final_rating REAL NOT NULL," +
                    "overall_rank INTEGER," +
                    "contests_participated INTEGER DEFAULT 0," +
                    "total_sub_contests INTEGER DEFAULT 0," +
                    "calculated_at TEXT NOT NULL," +
                    "FOREIGN KEY (registration_id) REFERENCES registrations(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (selection_contest_id) REFERENCES selection_contests(id) ON DELETE CASCADE," +
                    "UNIQUE(registration_id, selection_contest_id)" +
                    ")");

            // ==================== AUDIT LOGS TABLE ====================
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "action_type TEXT NOT NULL," +
                    "user_id INTEGER," +
                    "admin_id INTEGER," +
                    "actor_name TEXT," +
                    "entity_type TEXT," +
                    "entity_id INTEGER," +
                    "entity_name TEXT," +
                    "details TEXT," +
                    "ip_address TEXT," +
                    "created_at TEXT NOT NULL" +
                    ")");

            // ==================== CREATE INDEXES ====================
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_registrations_user ON registrations(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_registrations_contest ON registrations(selection_contest_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sub_contests_selection ON sub_contests(selection_contest_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contest_results_registration ON contest_results(registration_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_final_ratings_contest ON final_ratings(selection_contest_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_date ON audit_logs(created_at)");

            // Create default admin if not exists
            createDefaultAdmin();

            stmt.close();
            System.out.println("Database tables initialized successfully");
        } catch (SQLException e) {
            System.err.println("Failed to initialize tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create default admin account
     */
    private void createDefaultAdmin() {
        String checkSql = "SELECT COUNT(*) FROM admins WHERE username = 'admin'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO admins (username, password_hash, email, full_name, created_at, is_active) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin1234"); // TODO: Hash in production
                    pstmt.setString(3, "admin@contestpredictor.com");
                    pstmt.setString(4, "System Administrator");
                    pstmt.setString(5, LocalDateTime.now().format(DATE_FORMAT));
                    pstmt.setInt(6, 1);
                    pstmt.executeUpdate();
                    System.out.println("Default admin account created (username: admin, password: admin1234)");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to create default admin: " + e.getMessage());
        }
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Save or update a user
     */
    public boolean saveUser(User user) {
        String sql;
        boolean isNew = user.getId() == 0;
        
        if (isNew) {
            sql = "INSERT INTO users (username, password_hash, email, full_name, role, codeforces_handle, current_rating, contests_participated, rating_history, created_at, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE users SET username=?, password_hash=?, email=?, full_name=?, role=?, codeforces_handle=?, current_rating=?, contests_participated=?, rating_history=?, created_at=?, is_active=? WHERE id=?";
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole() != null ? user.getRole().name() : "CONTESTANT");
            pstmt.setString(6, user.getCodeforcesHandle());
            pstmt.setInt(7, user.getCurrentRating());
            pstmt.setInt(8, user.getContestsParticipated());
            pstmt.setString(9, serializeRatingHistory(user.getRatingHistory()));
            pstmt.setString(10, user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMAT) : LocalDateTime.now().format(DATE_FORMAT));
            pstmt.setInt(11, user.isActive() ? 1 : 0);
            
            if (!isNew) {
                pstmt.setInt(12, user.getId());
            }

            pstmt.executeUpdate();
            
            if (isNew) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to save user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load user by username
     */
    public User loadUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Load user by ID
     */
    public User loadUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load users: " + e.getMessage());
        }
        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        
        // Load role with fallback to CONTESTANT for existing users
        String roleStr = rs.getString("role");
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                user.setRole(User.UserRole.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                user.setRole(User.UserRole.CONTESTANT);
            }
        } else {
            user.setRole(User.UserRole.CONTESTANT);
        }
        
        user.setCodeforcesHandle(rs.getString("codeforces_handle"));
        user.setCurrentRating(rs.getInt("current_rating"));
        user.setContestsParticipated(rs.getInt("contests_participated"));
        user.setRatingHistory(deserializeRatingHistory(rs.getString("rating_history")));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            user.setCreatedAt(LocalDateTime.parse(createdAt, DATE_FORMAT));
        }
        user.setActive(rs.getInt("is_active") == 1);
        return user;
    }

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Validate admin credentials
     */
    public Admin validateAdmin(String username, String password) {
        String sql = "SELECT * FROM admins WHERE username = ? AND password_hash = ? AND is_active = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setUsername(rs.getString("username"));
                admin.setEmail(rs.getString("email"));
                admin.setFullName(rs.getString("full_name"));
                return admin;
            }
        } catch (SQLException e) {
            System.err.println("Failed to validate admin: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get admin by ID
     */
    public Admin getAdminById(int adminId) {
        String sql = "SELECT * FROM admins WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, adminId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setUsername(rs.getString("username"));
                admin.setEmail(rs.getString("email"));
                admin.setFullName(rs.getString("full_name"));
                return admin;
            }
        } catch (SQLException e) {
            System.err.println("Failed to get admin: " + e.getMessage());
        }
        return null;
    }

    // ==================== SELECTION CONTEST OPERATIONS ====================

    /**
     * Create a new selection contest
     */
    public boolean createSelectionContest(SelectionContest contest) {
        String sql = "INSERT INTO selection_contests (contest_code, share_key, name, description, created_by_admin_id, created_at, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, contest.getContestCode());
            pstmt.setString(2, contest.getShareKey());
            pstmt.setString(3, contest.getName());
            pstmt.setString(4, contest.getDescription());
            pstmt.setInt(5, contest.getCreatedByAdminId());
            pstmt.setString(6, contest.getCreatedAt().format(DATE_FORMAT));
            pstmt.setString(7, contest.getStartDate() != null ? contest.getStartDate().format(DATE_FORMAT) : null);
            pstmt.setString(8, contest.getEndDate() != null ? contest.getEndDate().format(DATE_FORMAT) : null);
            pstmt.setInt(9, contest.isActive() ? 1 : 0);

            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                contest.setId(generatedKeys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to create selection contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update a selection contest
     */
    public boolean updateSelectionContest(SelectionContest contest) {
        String sql = "UPDATE selection_contests SET name=?, description=?, start_date=?, end_date=?, is_active=? WHERE id=?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contest.getName());
            pstmt.setString(2, contest.getDescription());
            pstmt.setString(3, contest.getStartDate() != null ? contest.getStartDate().format(DATE_FORMAT) : null);
            pstmt.setString(4, contest.getEndDate() != null ? contest.getEndDate().format(DATE_FORMAT) : null);
            pstmt.setInt(5, contest.isActive() ? 1 : 0);
            pstmt.setInt(6, contest.getId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to update selection contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a selection contest
     */
    public boolean deleteSelectionContest(int contestId) {
        String sql = "DELETE FROM selection_contests WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, contestId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to delete selection contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get selection contest by ID
     */
    public SelectionContest getSelectionContestById(int contestId) {
        String sql = "SELECT sc.*, a.full_name as admin_name FROM selection_contests sc " +
                     "LEFT JOIN admins a ON sc.created_by_admin_id = a.id WHERE sc.id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, contestId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SelectionContest contest = mapResultSetToSelectionContest(rs);
                contest.setSubContests(getSubContestsBySelectionId(contestId));
                contest.setRegistrationCount(getRegistrationCount(contestId));
                return contest;
            }
        } catch (SQLException e) {
            System.err.println("Failed to get selection contest: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get selection contest by code
     */
    public SelectionContest getSelectionContestByCode(String contestCode) {
        String sql = "SELECT sc.*, a.full_name as admin_name FROM selection_contests sc " +
                     "LEFT JOIN admins a ON sc.created_by_admin_id = a.id WHERE sc.contest_code = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SelectionContest contest = mapResultSetToSelectionContest(rs);
                contest.setSubContests(getSubContestsBySelectionId(contest.getId()));
                contest.setRegistrationCount(getRegistrationCount(contest.getId()));
                return contest;
            }
        } catch (SQLException e) {
            System.err.println("Failed to get selection contest by code: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all selection contests
     */
    public List<SelectionContest> getAllSelectionContests() {
        List<SelectionContest> contests = new ArrayList<>();
        String sql = "SELECT sc.*, a.full_name as admin_name FROM selection_contests sc " +
                     "LEFT JOIN admins a ON sc.created_by_admin_id = a.id ORDER BY sc.created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SelectionContest contest = mapResultSetToSelectionContest(rs);
                contest.setRegistrationCount(getRegistrationCount(contest.getId()));
                contests.add(contest);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get selection contests: " + e.getMessage());
        }
        return contests;
    }

    /**
     * Get active selection contests
     */
    public List<SelectionContest> getActiveSelectionContests() {
        List<SelectionContest> contests = new ArrayList<>();
        String sql = "SELECT sc.*, a.full_name as admin_name FROM selection_contests sc " +
                     "LEFT JOIN admins a ON sc.created_by_admin_id = a.id " +
                     "WHERE sc.is_active = 1 ORDER BY sc.created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SelectionContest contest = mapResultSetToSelectionContest(rs);
                contest.setRegistrationCount(getRegistrationCount(contest.getId()));
                contests.add(contest);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get active selection contests: " + e.getMessage());
        }
        return contests;
    }

    private SelectionContest mapResultSetToSelectionContest(ResultSet rs) throws SQLException {
        SelectionContest contest = new SelectionContest();
        contest.setId(rs.getInt("id"));
        contest.setContestCode(rs.getString("contest_code"));
        
        // Handle share_key - might not exist in older databases
        try {
            contest.setShareKey(rs.getString("share_key"));
        } catch (SQLException e) {
            // Column doesn't exist, keep generated key
        }
        
        contest.setName(rs.getString("name"));
        contest.setDescription(rs.getString("description"));
        contest.setCreatedByAdminId(rs.getInt("created_by_admin_id"));
        
        // Handle admin_name - might not exist in all queries
        try {
            contest.setCreatedByAdminName(rs.getString("admin_name"));
        } catch (SQLException e) {
            // Column doesn't exist in this query
        }
        
        contest.setCreatedAt(parseDateTime(rs.getString("created_at")));
        contest.setStartDate(parseDateTime(rs.getString("start_date")));
        contest.setEndDate(parseDateTime(rs.getString("end_date")));
        contest.setActive(rs.getInt("is_active") == 1);
        return contest;
    }
    
    /**
     * Find selection contest by share key
     */
    public SelectionContest getSelectionContestByShareKey(String shareKey) {
        String sql = "SELECT sc.*, u.full_name as admin_name FROM selection_contests sc " +
                     "LEFT JOIN users u ON sc.created_by_admin_id = u.id " +
                     "WHERE sc.share_key = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, shareKey.toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSelectionContest(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get contest by share key: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get contests created by a specific user (setter)
     */
    public List<SelectionContest> getContestsByCreator(int userId) {
        List<SelectionContest> contests = new ArrayList<>();
        String sql = "SELECT sc.*, u.full_name as admin_name FROM selection_contests sc " +
                     "LEFT JOIN users u ON sc.created_by_admin_id = u.id " +
                     "WHERE sc.created_by_admin_id = ? ORDER BY sc.created_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                contests.add(mapResultSetToSelectionContest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get contests by creator: " + e.getMessage());
        }
        return contests;
    }
    
    /**
     * Update sub-contest weight
     */
    public boolean updateSubContestWeight(int subContestId, double newWeight) {
        String sql = "UPDATE sub_contests SET weight = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, newWeight);
            pstmt.setInt(2, subContestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update sub-contest weight: " + e.getMessage());
            return false;
        }
    }

    // ==================== SUB CONTEST OPERATIONS ====================

    /**
     * Add a sub-contest to a selection contest
     */
    public boolean addSubContest(SubContest subContest) {
        String sql = "INSERT INTO sub_contests (selection_contest_id, codeforces_contest_id, contest_name, contest_date, duration_seconds, weight, phase, is_fetched, added_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, subContest.getSelectionContestId());
            pstmt.setInt(2, subContest.getCodeforcesContestId());
            pstmt.setString(3, subContest.getContestName());
            pstmt.setString(4, subContest.getContestDate() != null ? subContest.getContestDate().format(DATE_FORMAT) : null);
            pstmt.setInt(5, subContest.getDurationSeconds());
            pstmt.setDouble(6, subContest.getWeight());
            pstmt.setString(7, subContest.getPhase());
            pstmt.setInt(8, subContest.isFetched() ? 1 : 0);
            pstmt.setString(9, subContest.getAddedAt().format(DATE_FORMAT));

            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                subContest.setId(generatedKeys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to add sub-contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update a sub-contest
     */
    public boolean updateSubContest(SubContest subContest) {
        String sql = "UPDATE sub_contests SET contest_name=?, contest_date=?, duration_seconds=?, weight=?, phase=?, is_fetched=? WHERE id=?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, subContest.getContestName());
            pstmt.setString(2, subContest.getContestDate() != null ? subContest.getContestDate().format(DATE_FORMAT) : null);
            pstmt.setInt(3, subContest.getDurationSeconds());
            pstmt.setDouble(4, subContest.getWeight());
            pstmt.setString(5, subContest.getPhase());
            pstmt.setInt(6, subContest.isFetched() ? 1 : 0);
            pstmt.setInt(7, subContest.getId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to update sub-contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a sub-contest
     */
    public boolean deleteSubContest(int subContestId) {
        String sql = "DELETE FROM sub_contests WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subContestId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to delete sub-contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get sub-contests by selection contest ID
     */
    public List<SubContest> getSubContestsBySelectionId(int selectionContestId) {
        List<SubContest> subContests = new ArrayList<>();
        String sql = "SELECT * FROM sub_contests WHERE selection_contest_id = ? ORDER BY contest_date ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                subContests.add(mapResultSetToSubContest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get sub-contests: " + e.getMessage());
        }
        return subContests;
    }

    /**
     * Get sub-contest by ID
     */
    public SubContest getSubContestById(int subContestId) {
        String sql = "SELECT * FROM sub_contests WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subContestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSubContest(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get sub-contest: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Check if a sub-contest already exists for a selection contest
     */
    public boolean subContestExists(int selectionContestId, int codeforcesContestId) {
        String sql = "SELECT COUNT(*) FROM sub_contests WHERE selection_contest_id = ? AND codeforces_contest_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectionContestId);
            pstmt.setInt(2, codeforcesContestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to check sub-contest existence: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Get participant count for a specific sub-contest
     */
    public int getSubContestParticipantCount(int subContestId) {
        String sql = "SELECT COUNT(DISTINCT cr.registration_id) FROM contest_results cr WHERE cr.sub_contest_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subContestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get sub-contest participant count: " + e.getMessage());
        }
        return 0;
    }

    private SubContest mapResultSetToSubContest(ResultSet rs) throws SQLException {
        SubContest subContest = new SubContest();
        subContest.setId(rs.getInt("id"));
        subContest.setSelectionContestId(rs.getInt("selection_contest_id"));
        subContest.setCodeforcesContestId(rs.getInt("codeforces_contest_id"));
        subContest.setContestName(rs.getString("contest_name"));
        subContest.setContestDate(parseDateTime(rs.getString("contest_date")));
        subContest.setDurationSeconds(rs.getInt("duration_seconds"));
        subContest.setWeight(rs.getDouble("weight"));
        subContest.setPhase(rs.getString("phase"));
        subContest.setFetched(rs.getInt("is_fetched") == 1);
        subContest.setAddedAt(parseDateTime(rs.getString("added_at")));
        return subContest;
    }

    // ==================== REGISTRATION OPERATIONS ====================

    /**
     * Register a user for a selection contest
     */
    public boolean registerForContest(Registration registration) {
        String sql = "INSERT INTO registrations (user_id, selection_contest_id, codeforces_handle, registered_at, status) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, registration.getUserId());
            pstmt.setInt(2, registration.getSelectionContestId());
            pstmt.setString(3, registration.getCodeforcesHandle());
            pstmt.setString(4, registration.getRegisteredAt().format(DATE_FORMAT));
            pstmt.setString(5, registration.getStatus().name());

            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                registration.setId(generatedKeys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to register for contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update registration status
     */
    public boolean updateRegistrationStatus(int registrationId, Registration.RegistrationStatus status) {
        String sql = "UPDATE registrations SET status = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, registrationId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to update registration status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get registrations by selection contest ID
     */
    public List<Registration> getRegistrationsByContestId(int selectionContestId) {
        List<Registration> registrations = new ArrayList<>();
        String sql = "SELECT r.*, u.username, sc.name as contest_name FROM registrations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN selection_contests sc ON r.selection_contest_id = sc.id " +
                     "WHERE r.selection_contest_id = ? ORDER BY r.registered_at ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get registrations: " + e.getMessage());
        }
        return registrations;
    }

    /**
     * Get registrations by user ID
     */
    public List<Registration> getRegistrationsByUserId(int userId) {
        List<Registration> registrations = new ArrayList<>();
        String sql = "SELECT r.*, u.username, sc.name as contest_name FROM registrations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN selection_contests sc ON r.selection_contest_id = sc.id " +
                     "WHERE r.user_id = ? ORDER BY r.registered_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get user registrations: " + e.getMessage());
        }
        return registrations;
    }

    /**
     * Get registration by ID
     */
    public Registration getRegistrationById(int registrationId) {
        String sql = "SELECT r.*, u.username, sc.name as contest_name FROM registrations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN selection_contests sc ON r.selection_contest_id = sc.id " +
                     "WHERE r.id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, registrationId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRegistration(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get registration by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Check if user is registered for a contest
     */
    public boolean isUserRegistered(int userId, int selectionContestId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE user_id = ? AND selection_contest_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to check user registration: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get registration count for a contest
     */
    public int getRegistrationCount(int selectionContestId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE selection_contest_id = ? AND status = 'ACTIVE'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get registration count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get registration by user and contest
     */
    public Registration getRegistration(int userId, int selectionContestId) {
        String sql = "SELECT r.*, u.username, sc.name as contest_name FROM registrations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN selection_contests sc ON r.selection_contest_id = sc.id " +
                     "WHERE r.user_id = ? AND r.selection_contest_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRegistration(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get registration: " + e.getMessage());
        }
        return null;
    }

    private Registration mapResultSetToRegistration(ResultSet rs) throws SQLException {
        Registration registration = new Registration();
        registration.setId(rs.getInt("id"));
        registration.setUserId(rs.getInt("user_id"));
        registration.setUsername(rs.getString("username"));
        registration.setSelectionContestId(rs.getInt("selection_contest_id"));
        registration.setSelectionContestName(rs.getString("contest_name"));
        registration.setCodeforcesHandle(rs.getString("codeforces_handle"));
        registration.setRegisteredAt(parseDateTime(rs.getString("registered_at")));
        registration.setStatus(Registration.RegistrationStatus.fromString(rs.getString("status")));
        return registration;
    }

    // ==================== CONTEST RESULT OPERATIONS ====================

    /**
     * Save contest result
     */
    public boolean saveContestResult(ContestResult result) {
        String sql = "INSERT OR REPLACE INTO contest_results (registration_id, sub_contest_id, cf_rank, cf_points, cf_penalty, problems_solved, cf_old_rating, cf_new_rating, cf_rating_change, calculated_rating, weighted_rating, is_participated, fetched_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, result.getRegistrationId());
            pstmt.setInt(2, result.getSubContestId());
            pstmt.setInt(3, result.getCfRank());
            pstmt.setDouble(4, result.getCfPoints());
            pstmt.setInt(5, result.getCfPenalty());
            pstmt.setInt(6, result.getProblemsSolved());
            pstmt.setInt(7, result.getCfOldRating());
            pstmt.setInt(8, result.getCfNewRating());
            pstmt.setInt(9, result.getCfRatingChange());
            pstmt.setDouble(10, result.getCalculatedRating());
            pstmt.setDouble(11, result.getWeightedRating());
            pstmt.setInt(12, result.isParticipated() ? 1 : 0);
            pstmt.setString(13, result.getFetchedAt().format(DATE_FORMAT));

            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                result.setId(generatedKeys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to save contest result: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get results for a registration
     */
    public List<ContestResult> getResultsByRegistrationId(int registrationId) {
        List<ContestResult> results = new ArrayList<>();
        String sql = "SELECT cr.*, sc.contest_name as sub_contest_name FROM contest_results cr " +
                     "JOIN sub_contests sc ON cr.sub_contest_id = sc.id " +
                     "WHERE cr.registration_id = ? ORDER BY sc.contest_date ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, registrationId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                results.add(mapResultSetToContestResult(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get contest results: " + e.getMessage());
        }
        return results;
    }

    /**
     * Get results for a sub-contest
     */
    public List<ContestResult> getResultsBySubContestId(int subContestId) {
        List<ContestResult> results = new ArrayList<>();
        String sql = "SELECT cr.*, sc.contest_name as sub_contest_name, r.codeforces_handle " +
                     "FROM contest_results cr " +
                     "JOIN sub_contests sc ON cr.sub_contest_id = sc.id " +
                     "JOIN registrations r ON cr.registration_id = r.id " +
                     "WHERE cr.sub_contest_id = ? ORDER BY cr.cf_rank ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subContestId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ContestResult result = mapResultSetToContestResult(rs);
                result.setCodeforcesHandle(rs.getString("codeforces_handle"));
                results.add(result);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get sub-contest results: " + e.getMessage());
        }
        return results;
    }

    private ContestResult mapResultSetToContestResult(ResultSet rs) throws SQLException {
        ContestResult result = new ContestResult();
        result.setId(rs.getInt("id"));
        result.setRegistrationId(rs.getInt("registration_id"));
        result.setSubContestId(rs.getInt("sub_contest_id"));
        result.setSubContestName(rs.getString("sub_contest_name"));
        result.setCfRank(rs.getInt("cf_rank"));
        result.setCfPoints(rs.getDouble("cf_points"));
        result.setCfPenalty(rs.getInt("cf_penalty"));
        result.setProblemsSolved(rs.getInt("problems_solved"));
        result.setCfOldRating(rs.getInt("cf_old_rating"));
        result.setCfNewRating(rs.getInt("cf_new_rating"));
        result.setCfRatingChange(rs.getInt("cf_rating_change"));
        result.setCalculatedRating(rs.getDouble("calculated_rating"));
        result.setWeightedRating(rs.getDouble("weighted_rating"));
        result.setParticipated(rs.getInt("is_participated") == 1);
        result.setFetchedAt(parseDateTime(rs.getString("fetched_at")));
        return result;
    }

    // ==================== FINAL RATING OPERATIONS ====================

    /**
     * Save or update final rating
     */
    public boolean saveFinalRating(FinalRating rating) {
        String sql = "INSERT OR REPLACE INTO final_ratings (registration_id, selection_contest_id, final_rating, overall_rank, contests_participated, total_sub_contests, calculated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, rating.getRegistrationId());
            pstmt.setInt(2, rating.getSelectionContestId());
            pstmt.setDouble(3, rating.getFinalRating());
            pstmt.setInt(4, rating.getOverallRank());
            pstmt.setInt(5, rating.getContestsParticipated());
            pstmt.setInt(6, rating.getTotalSubContests());
            pstmt.setString(7, rating.getCalculatedAt().format(DATE_FORMAT));

            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                rating.setId(generatedKeys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to save final rating: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get final ratings for a selection contest (standings)
     */
    public List<FinalRating> getFinalRatingsByContestId(int selectionContestId) {
        List<FinalRating> ratings = new ArrayList<>();
        String sql = "SELECT fr.*, u.username, r.codeforces_handle, sc.name as contest_name " +
                     "FROM final_ratings fr " +
                     "JOIN registrations r ON fr.registration_id = r.id " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN selection_contests sc ON fr.selection_contest_id = sc.id " +
                     "WHERE fr.selection_contest_id = ? ORDER BY fr.overall_rank ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ratings.add(mapResultSetToFinalRating(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get final ratings: " + e.getMessage());
        }
        return ratings;
    }

    /**
     * Get final rating for a user in a contest
     */
    public FinalRating getFinalRating(int userId, int selectionContestId) {
        String sql = "SELECT fr.*, u.username, r.codeforces_handle, sc.name as contest_name " +
                     "FROM final_ratings fr " +
                     "JOIN registrations r ON fr.registration_id = r.id " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN selection_contests sc ON fr.selection_contest_id = sc.id " +
                     "WHERE r.user_id = ? AND fr.selection_contest_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToFinalRating(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get final rating: " + e.getMessage());
        }
        return null;
    }

    private FinalRating mapResultSetToFinalRating(ResultSet rs) throws SQLException {
        FinalRating rating = new FinalRating();
        rating.setId(rs.getInt("id"));
        rating.setRegistrationId(rs.getInt("registration_id"));
        rating.setSelectionContestId(rs.getInt("selection_contest_id"));
        rating.setUsername(rs.getString("username"));
        rating.setCodeforcesHandle(rs.getString("codeforces_handle"));
        rating.setSelectionContestName(rs.getString("contest_name"));
        rating.setFinalRating(rs.getDouble("final_rating"));
        rating.setOverallRank(rs.getInt("overall_rank"));
        rating.setContestsParticipated(rs.getInt("contests_participated"));
        rating.setTotalSubContests(rs.getInt("total_sub_contests"));
        rating.setCalculatedAt(parseDateTime(rs.getString("calculated_at")));
        return rating;
    }

    // ==================== AUDIT LOG OPERATIONS ====================

    /**
     * Save audit log
     */
    public boolean saveAuditLog(AuditLog log) {
        String sql = "INSERT INTO audit_logs (action_type, user_id, admin_id, actor_name, entity_type, entity_id, entity_name, details, ip_address, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, log.getActionType().name());
            pstmt.setObject(2, log.getUserId());
            pstmt.setObject(3, log.getAdminId());
            pstmt.setString(4, log.getActorName());
            pstmt.setString(5, log.getEntityType() != null ? log.getEntityType().name() : null);
            pstmt.setObject(6, log.getEntityId());
            pstmt.setString(7, log.getEntityName());
            pstmt.setString(8, log.getDetails());
            pstmt.setString(9, log.getIpAddress());
            pstmt.setString(10, log.getCreatedAt().format(DATE_FORMAT));

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to save audit log: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get recent audit logs
     */
    public List<AuditLog> getRecentAuditLogs(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get audit logs: " + e.getMessage());
        }
        return logs;
    }

    /**
     * Get audit logs by action type
     */
    public List<AuditLog> getAuditLogsByAction(AuditLog.ActionType actionType, int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE action_type = ? ORDER BY created_at DESC LIMIT ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, actionType.name());
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get audit logs by action: " + e.getMessage());
        }
        return logs;
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        log.setActionType(AuditLog.ActionType.valueOf(rs.getString("action_type")));
        log.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        log.setAdminId(rs.getObject("admin_id") != null ? rs.getInt("admin_id") : null);
        log.setActorName(rs.getString("actor_name"));
        String entityType = rs.getString("entity_type");
        if (entityType != null) {
            log.setEntityType(AuditLog.EntityType.valueOf(entityType));
        }
        log.setEntityId(rs.getObject("entity_id") != null ? rs.getInt("entity_id") : null);
        log.setEntityName(rs.getString("entity_name"));
        log.setDetails(rs.getString("details"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setCreatedAt(parseDateTime(rs.getString("created_at")));
        return log;
    }

    // ==================== UTILITY METHODS ====================

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private String serializeRatingHistory(List<Integer> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(history.get(i));
            if (i < history.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private List<Integer> deserializeRatingHistory(String historyStr) {
        List<Integer> history = new ArrayList<>();
        if (historyStr == null || historyStr.isEmpty()) {
            return history;
        }
        String[] parts = historyStr.split(",");
        for (String part : parts) {
            try {
                history.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }
        return history;
    }

    /**
     * Generate unique contest code
     */
    public String generateContestCode() {
        String prefix = "SC" + LocalDateTime.now().getYear() + "-";
        int count = 1;
        
        String sql = "SELECT COUNT(*) FROM selection_contests WHERE contest_code LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Failed to generate contest code: " + e.getMessage());
        }
        
        return String.format("%s%03d", prefix, count);
    }

    // ==================== ADDITIONAL METHODS ====================

    /**
     * Alias for getRegistrationsByUserId
     */
    public List<Registration> getRegistrationsByUser(int userId) {
        return getRegistrationsByUserId(userId);
    }

    /**
     * Get all registrations
     */
    public List<Registration> getAllRegistrations() {
        List<Registration> registrations = new ArrayList<>();
        String sql = "SELECT r.*, u.username FROM registrations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "ORDER BY r.registered_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get all registrations: " + e.getMessage());
        }
        return registrations;
    }

    /**
     * Update user's Codeforces handle
     */
    public boolean updateUserCodeforcesHandle(int userId, String cfHandle) {
        String sql = "UPDATE users SET codeforces_handle = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cfHandle);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update CF handle: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user's password
     */
    public boolean updateUserPassword(int userId, String newPassword) {
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get contest results by user for a specific contest
     */
    public List<ContestResult> getContestResultsByUser(int selectionContestId, int userId) {
        List<ContestResult> results = new ArrayList<>();
        String sql = "SELECT * FROM contest_results WHERE selection_contest_id = ? AND user_id = ? ORDER BY cf_contest_id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectionContestId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                results.add(mapResultSetToContestResult(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get contest results: " + e.getMessage());
        }
        return results;
    }

    /**
     * Get audit logs with limit
     */
    public List<AuditLog> getAuditLogs(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get audit logs: " + e.getMessage());
        }
        return logs;
    }
    
    // ==================== HELPER METHODS FOR DASHBOARD ====================
    
    /**
     * Get count of sub-contests for a selection contest
     */
    public int getSubContestCount(int selectionContestId) {
        String sql = "SELECT COUNT(*) FROM sub_contests WHERE selection_contest_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get sub-contest count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get count of participants for a selection contest
     */
    public int getParticipantCount(int selectionContestId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE selection_contest_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get participant count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get sub-contests for a selection contest (alias for getSubContestsBySelectionId)
     */
    public List<SubContest> getSubContestsForSelection(int selectionContestId) {
        return getSubContestsBySelectionId(selectionContestId);
    }
    
    /**
     * Add a sub-contest with simple parameters
     */
    public boolean addSubContest(int selectionContestId, int codeforcesContestId, String contestName, double weight) {
        SubContest subContest = new SubContest();
        subContest.setSelectionContestId(selectionContestId);
        subContest.setCodeforcesContestId(codeforcesContestId);
        subContest.setContestName(contestName);
        subContest.setWeight(weight);
        subContest.setAddedAt(LocalDateTime.now());
        return addSubContest(subContest);
    }
    
    /**
     * Get final ratings for a contest
     */
    public List<FinalRating> getFinalRatingsForContest(int selectionContestId) {
        return getFinalRatingsByContestId(selectionContestId);
    }
    
    /**
     * Register a user for a contest
     */
    public boolean registerUserForContest(int userId, int selectionContestId, String cfHandle) {
        String sql = "INSERT INTO registrations (selection_contest_id, user_id, codeforces_handle, registration_date, status) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, selectionContestId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, cfHandle);
            pstmt.setString(4, LocalDateTime.now().format(DATE_FORMAT));
            pstmt.setString(5, "REGISTERED");
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to register user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get final rating for a specific user in a contest
     */
    public FinalRating getFinalRatingForUser(int userId, int selectionContestId) {
        String sql = "SELECT * FROM final_ratings WHERE user_id = ? AND selection_contest_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, selectionContestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToFinalRating(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get final rating for user: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get contest results for a specific sub-contest
     */
    public List<ContestResult> getContestResultsBySubContest(int subContestId) {
        List<ContestResult> results = new ArrayList<>();
        String sql = "SELECT * FROM contest_results WHERE sub_contest_id = ? ORDER BY rank ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subContestId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                results.add(mapResultSetToContestResult(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get contest results by sub-contest: " + e.getMessage());
        }
        return results;
    }
    
    /**
     * Get a specific contest result for a user in a sub-contest
     */
    public ContestResult getContestResult(int userId, int subContestId) {
        String sql = "SELECT * FROM contest_results WHERE user_id = ? AND sub_contest_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, subContestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToContestResult(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get contest result: " + e.getMessage());
        }
        return null;
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database: " + e.getMessage());
        }
    }
}
