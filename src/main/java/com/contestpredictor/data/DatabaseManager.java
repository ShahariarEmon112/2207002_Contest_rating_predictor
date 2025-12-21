package com.contestpredictor.data;

import com.contestpredictor.model.Contest;
import com.contestpredictor.model.Participant;
import com.contestpredictor.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Manager for persisting contest, user, and participant data
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:contest_predictor.db";
    private Connection connection;

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

    /**
     * Initialize database tables
     */
    private void initializeTables() {
        try {
            Statement stmt = connection.createStatement();

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY," +
                    "password TEXT NOT NULL," +
                    "full_name TEXT NOT NULL," +
                    "current_rating INTEGER NOT NULL," +
                    "contests_participated INTEGER NOT NULL," +
                    "rating_history TEXT" + // Stored as comma-separated values
                    ")");

            // Contests table
            stmt.execute("CREATE TABLE IF NOT EXISTS contests (" +
                    "contest_id TEXT PRIMARY KEY," +
                    "contest_name TEXT NOT NULL," +
                    "date_time TEXT NOT NULL," +
                    "duration INTEGER NOT NULL," +
                    "is_past INTEGER NOT NULL" + // 0 = false, 1 = true
                    ")");

            // Participants table
            stmt.execute("CREATE TABLE IF NOT EXISTS participants (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "contest_id TEXT NOT NULL," +
                    "username TEXT NOT NULL," +
                    "current_rating INTEGER NOT NULL," +
                    "problems_solved INTEGER NOT NULL," +
                    "total_penalty INTEGER NOT NULL," +
                    "rank INTEGER NOT NULL," +
                    "predicted_rating INTEGER NOT NULL," +
                    "rating_change INTEGER NOT NULL," +
                    "FOREIGN KEY (contest_id) REFERENCES contests(contest_id)" +
                    ")");

            stmt.close();
            System.out.println("Database tables initialized successfully");
        } catch (SQLException e) {
            System.err.println("Failed to initialize tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save a user to the database
     */
    public void saveUser(User user) {
        String sql = "INSERT OR REPLACE INTO users (username, password, full_name, current_rating, contests_participated, rating_history) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setInt(4, user.getCurrentRating());
            pstmt.setInt(5, user.getContestsParticipated());
            
            // Convert rating history to comma-separated string
            StringBuilder history = new StringBuilder();
            for (int i = 0; i < user.getRatingHistory().size(); i++) {
                history.append(user.getRatingHistory().get(i));
                if (i < user.getRatingHistory().size() - 1) {
                    history.append(",");
                }
            }
            pstmt.setString(6, history.toString());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save user: " + e.getMessage());
        }
    }

    /**
     * Load a user from the database
     */
    public User loadUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getInt("current_rating"),
                    rs.getInt("contests_participated"),
                    rs.getString("full_name")
                );
                
                // Parse rating history
                String historyStr = rs.getString("rating_history");
                if (historyStr != null && !historyStr.isEmpty()) {
                    user.getRatingHistory().clear();
                    String[] ratings = historyStr.split(",");
                    for (String rating : ratings) {
                        user.getRatingHistory().add(Integer.parseInt(rating.trim()));
                    }
                }
                
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Failed to load user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Save a contest to the database
     */
    public void saveContest(Contest contest) {
        String sql = "INSERT OR REPLACE INTO contests (contest_id, contest_name, date_time, duration, is_past) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contest.getContestId());
            pstmt.setString(2, contest.getContestName());
            pstmt.setString(3, contest.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setInt(4, contest.getDuration());
            pstmt.setInt(5, contest.isPast() ? 1 : 0);
            
            pstmt.executeUpdate();
            
            // Save participants
            saveParticipants(contest);
        } catch (SQLException e) {
            System.err.println("Failed to save contest: " + e.getMessage());
        }
    }

    /**
     * Save participants for a contest
     */
    private void saveParticipants(Contest contest) {
        // First, delete existing participants for this contest
        String deleteSql = "DELETE FROM participants WHERE contest_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setString(1, contest.getContestId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete old participants: " + e.getMessage());
        }
        
        // Insert new participants
        String insertSql = "INSERT INTO participants (contest_id, username, current_rating, problems_solved, total_penalty, rank, predicted_rating, rating_change) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            for (Participant p : contest.getParticipants()) {
                pstmt.setString(1, contest.getContestId());
                pstmt.setString(2, p.getUsername());
                pstmt.setInt(3, p.getCurrentRating());
                pstmt.setInt(4, p.getProblemsSolved());
                pstmt.setInt(5, p.getTotalPenalty());
                pstmt.setInt(6, p.getRank());
                pstmt.setInt(7, p.getPredictedRating());
                pstmt.setInt(8, p.getRatingChange());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save participants: " + e.getMessage());
        }
    }

    /**
     * Load a contest from the database
     */
    public Contest loadContest(String contestId) {
        String sql = "SELECT * FROM contests WHERE contest_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Contest contest = new Contest(
                    rs.getString("contest_id"),
                    rs.getString("contest_name"),
                    LocalDateTime.parse(rs.getString("date_time"), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    rs.getInt("duration"),
                    rs.getInt("is_past") == 1
                );
                
                // Load participants
                loadParticipantsForContest(contest);
                
                return contest;
            }
        } catch (SQLException e) {
            System.err.println("Failed to load contest: " + e.getMessage());
        }
        return null;
    }

    /**
     * Load participants for a contest
     */
    private void loadParticipantsForContest(Contest contest) {
        String sql = "SELECT * FROM participants WHERE contest_id = ? ORDER BY rank ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, contest.getContestId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Participant p = new Participant(
                    rs.getString("username"),
                    rs.getInt("current_rating"),
                    rs.getInt("problems_solved"),
                    rs.getInt("total_penalty")
                );
                p.setRank(rs.getInt("rank"));
                p.setPredictedRating(rs.getInt("predicted_rating"));
                p.setRatingChange(rs.getInt("rating_change"));
                
                contest.addParticipant(p);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load participants: " + e.getMessage());
        }
    }

    /**
     * Load all contests from the database
     */
    public List<Contest> loadAllContests() {
        List<Contest> contests = new ArrayList<>();
        String sql = "SELECT contest_id FROM contests";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Contest contest = loadContest(rs.getString("contest_id"));
                if (contest != null) {
                    contests.add(contest);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load contests: " + e.getMessage());
        }
        return contests;
    }

    /**
     * Check if database has contests
     */
    public boolean hasContests() {
        String sql = "SELECT COUNT(*) FROM contests";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to check contests: " + e.getMessage());
        }
        return false;
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
