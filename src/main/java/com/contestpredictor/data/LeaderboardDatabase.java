package com.contestpredictor.data;

import com.contestpredictor.model.LeaderboardContest;
import com.contestpredictor.model.LeaderboardEntry;
import com.contestpredictor.model.CombinedLeaderboardEntry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Database manager for leaderboard-related operations
 * Handles creation, registration, standings, and aggregation of leaderboard contests
 */
public class LeaderboardDatabase {
    private static LeaderboardDatabase instance;
    private DatabaseManager dbManager;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LeaderboardDatabase() {
        dbManager = DatabaseManager.getInstance();
    }

    public static LeaderboardDatabase getInstance() {
        if (instance == null) {
            instance = new LeaderboardDatabase();
        }
        return instance;
    }

    // ============ LEADERBOARD CONTEST OPERATIONS ============

    /**
     * Create a new leaderboard contest
     */
    public boolean createLeaderboardContest(LeaderboardContest contest) {
        try {
            String sql = "INSERT INTO leaderboard_contests " +
                    "(contest_id, contest_name, description, start_date, end_date, max_problems, " +
                    "is_active, standings_finalized, created_by_admin, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contest.getContestId());
            pstmt.setString(2, contest.getContestName());
            pstmt.setString(3, contest.getDescription());
            pstmt.setString(4, contest.getStartDate().format(formatter));
            pstmt.setString(5, contest.getEndDate().format(formatter));
            pstmt.setInt(6, contest.getMaxProblems());
            pstmt.setInt(7, contest.isActive() ? 1 : 0);
            pstmt.setInt(8, contest.isStandings_finalized() ? 1 : 0);
            pstmt.setString(9, contest.getCreatedByAdmin());
            pstmt.setString(10, contest.getCreatedAt().format(formatter));
            
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error creating leaderboard contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all leaderboard contests
     */
    public List<LeaderboardContest> getAllLeaderboardContests() {
        List<LeaderboardContest> contests = new ArrayList<>();
        try {
            String sql = "SELECT * FROM leaderboard_contests ORDER BY created_at DESC";
            System.out.println("DEBUG LeaderboardDatabase: Executing query: " + sql);
            java.sql.Statement stmt = dbManager.getConnection().createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql);
            
            int count = 0;
            while (rs.next()) {
                count++;
                String contestId = rs.getString("contest_id");
                String contestName = rs.getString("contest_name");
                System.out.println("DEBUG LeaderboardDatabase: Found contest " + count + ": " + contestId + " - " + contestName);
                
                LeaderboardContest contest = new LeaderboardContest(
                    contestId,
                    contestName,
                    rs.getString("description"),
                    LocalDateTime.parse(rs.getString("start_date"), formatter),
                    LocalDateTime.parse(rs.getString("end_date"), formatter),
                    rs.getInt("max_problems"),
                    rs.getString("created_by_admin")
                );
                contest.setActive(rs.getInt("is_active") == 1);
                contest.setStandings_finalized(rs.getInt("standings_finalized") == 1);
                contest.setCreatedAt(LocalDateTime.parse(rs.getString("created_at"), formatter));
                contests.add(contest);
            }
            System.out.println("DEBUG LeaderboardDatabase: Total contests loaded: " + count);
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error retrieving leaderboard contests: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("DEBUG LeaderboardDatabase: Returning " + contests.size() + " contests");
        return contests;
    }

    /**
     * Get a specific leaderboard contest by ID
     */
    public LeaderboardContest getLeaderboardContestById(String contestId) {
        try {
            String sql = "SELECT * FROM leaderboard_contests WHERE contest_id = ?";
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contestId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                LeaderboardContest contest = new LeaderboardContest(
                    rs.getString("contest_id"),
                    rs.getString("contest_name"),
                    rs.getString("description"),
                    LocalDateTime.parse(rs.getString("start_date"), formatter),
                    LocalDateTime.parse(rs.getString("end_date"), formatter),
                    rs.getInt("max_problems"),
                    rs.getString("created_by_admin")
                );
                contest.setActive(rs.getInt("is_active") == 1);
                contest.setStandings_finalized(rs.getInt("standings_finalized") == 1);
                return contest;
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            System.err.println("Error retrieving leaderboard contest: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update leaderboard contest status
     */
    public boolean updateLeaderboardContestStatus(String contestId, boolean isActive) {
        try {
            String sql = "UPDATE leaderboard_contests SET is_active = ? WHERE contest_id = ?";
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, isActive ? 1 : 0);
            pstmt.setString(2, contestId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error updating leaderboard contest status: " + e.getMessage());
            return false;
        }
    }

    // ============ USER REGISTRATION OPERATIONS ============

    /**
     * Register a user for a leaderboard contest
     */
    public boolean registerUserForLeaderboardContest(String contestId, String username) {
        try {
            String sql = "INSERT INTO leaderboard_registrations (contest_id, username, registered_at) " +
                    "VALUES (?, ?, ?)";
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contestId);
            pstmt.setString(2, username);
            pstmt.setString(3, LocalDateTime.now().format(formatter));
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error registering user for leaderboard contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Unregister a user from a leaderboard contest
     */
    public boolean unregisterUserFromLeaderboardContest(String contestId, String username) {
        try {
            String sql = "DELETE FROM leaderboard_registrations WHERE contest_id = ? AND username = ?";
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contestId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error unregistering user from leaderboard contest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if user is registered for a contest
     */
    public boolean isUserRegisteredForContest(String contestId, String username) {
        try {
            String sql = "SELECT 1 FROM leaderboard_registrations WHERE contest_id = ? AND username = ?";
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contestId);
            pstmt.setString(2, username);
            java.sql.ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            pstmt.close();
            return exists;
        } catch (Exception e) {
            System.err.println("Error checking user registration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get registered users for a contest
     */
    public List<String> getRegisteredUsersForContest(String contestId) {
        List<String> users = new ArrayList<>();
        try {
            String sql = "SELECT username FROM leaderboard_registrations WHERE contest_id = ? ORDER BY registered_at";
            System.out.println("DEBUG LeaderboardDatabase: Getting registered users for contest: " + contestId);
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contestId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                String username = rs.getString("username");
                users.add(username);
                System.out.println("DEBUG LeaderboardDatabase: Found registered user " + count + ": " + username);
            }
            System.out.println("DEBUG LeaderboardDatabase: Total registered users for " + contestId + ": " + count);
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            System.err.println("Error retrieving registered users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    // ============ LEADERBOARD ENTRY OPERATIONS (Individual Contest Standings) ============

    /**
     * Add or update leaderboard entry for a user in a contest
     */
    public boolean addLeaderboardEntry(LeaderboardEntry entry) {
        try {
            String checkSql = "SELECT id FROM leaderboard_entries WHERE contest_id = ? AND username = ?";
            java.sql.PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
            checkStmt.setString(1, entry.getContestId());
            checkStmt.setString(2, entry.getUsername());
            java.sql.ResultSet rs = checkStmt.executeQuery();
            
            boolean exists = rs.next();
            rs.close();
            checkStmt.close();
            
            String sql;
            if (exists) {
                sql = "UPDATE leaderboard_entries SET rank = ?, solve_count = ?, total_penalty = ?, " +
                      "total_time = ?, status = ? WHERE contest_id = ? AND username = ?";
            } else {
                sql = "INSERT INTO leaderboard_entries (contest_id, username, rank, solve_count, " +
                      "total_penalty, total_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            }
            
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            
            if (exists) {
                pstmt.setInt(1, entry.getRank());
                pstmt.setInt(2, entry.getSolveCount());
                pstmt.setInt(3, entry.getTotalPenalty());
                pstmt.setLong(4, entry.getTotalTime());
                pstmt.setString(5, entry.getStatus());
                pstmt.setString(6, entry.getContestId());
                pstmt.setString(7, entry.getUsername());
            } else {
                pstmt.setString(1, entry.getContestId());
                pstmt.setString(2, entry.getUsername());
                pstmt.setInt(3, entry.getRank());
                pstmt.setInt(4, entry.getSolveCount());
                pstmt.setInt(5, entry.getTotalPenalty());
                pstmt.setLong(6, entry.getTotalTime());
                pstmt.setString(7, entry.getStatus());
            }
            
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error adding/updating leaderboard entry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get standings for a specific contest
     */
    public List<LeaderboardEntry> getContestStandings(String contestId) {
        List<LeaderboardEntry> standings = new ArrayList<>();
        try {
            String sql = "SELECT * FROM leaderboard_entries WHERE contest_id = ? ORDER BY rank ASC";
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contestId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LeaderboardEntry entry = new LeaderboardEntry(
                    rs.getString("username"),
                    rs.getString("contest_id"),
                    rs.getInt("rank"),
                    rs.getInt("solve_count"),
                    rs.getInt("total_penalty"),
                    rs.getLong("total_time")
                );
                entry.setStatus(rs.getString("status"));
                standings.add(entry);
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            System.err.println("Error retrieving contest standings: " + e.getMessage());
        }
        return standings;
    }

    /**
     * Delete a single leaderboard entry for a user in a contest
     */
    public boolean deleteLeaderboardEntry(String contestId, String username) {
        try {
            String sql = "DELETE FROM leaderboard_entries WHERE contest_id = ? AND username = ?";
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contestId);
            pstmt.setString(2, username);
            int affected = pstmt.executeUpdate();
            pstmt.close();
            return affected > 0;
        } catch (Exception e) {
            System.err.println("Error deleting leaderboard entry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete leaderboard entries for a contest (for re-calculation)
     */
    public boolean deleteContestStandings(String contestId) {
        try {
            String sql = "DELETE FROM leaderboard_entries WHERE contest_id = ?";
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contestId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting contest standings: " + e.getMessage());
            return false;
        }
    }

    // ============ COMBINED LEADERBOARD OPERATIONS ============

    /**
     * Calculate and update the combined leaderboard
     * This aggregates all contest standings into an overall ranking
     */
    public boolean updateCombinedLeaderboard() {
        try {
            // Get all contests with finalized standings
            String contestSql = "SELECT contest_id FROM leaderboard_contests WHERE standings_finalized = 1";
            java.sql.Statement contestStmt = dbManager.getConnection().createStatement();
            java.sql.ResultSet contestRs = contestStmt.executeQuery(contestSql);
            
            Map<String, Integer> userTotalSolves = new HashMap<>();
            Map<String, Integer> userTotalPenalty = new HashMap<>();
            Map<String, Integer> userContestCount = new HashMap<>();
            
            while (contestRs.next()) {
                String contestId = contestRs.getString("contest_id");
                List<LeaderboardEntry> standings = getContestStandings(contestId);
                
                for (LeaderboardEntry entry : standings) {
                    String username = entry.getUsername();
                    userTotalSolves.put(username, userTotalSolves.getOrDefault(username, 0) + entry.getSolveCount());
                    userTotalPenalty.put(username, userTotalPenalty.getOrDefault(username, 0) + entry.getTotalPenalty());
                    userContestCount.put(username, userContestCount.getOrDefault(username, 0) + 1);
                }
            }
            contestRs.close();
            contestStmt.close();
            
            // Clear existing combined leaderboard
            String clearSql = "DELETE FROM combined_leaderboard";
            java.sql.Statement clearStmt = dbManager.getConnection().createStatement();
            clearStmt.executeUpdate(clearSql);
            clearStmt.close();
            
            // Sort users by total solves (descending) then by total penalty (ascending)
            List<String> sortedUsers = new ArrayList<>(userTotalSolves.keySet());
            sortedUsers.sort((a, b) -> {
                int comparesolves = Integer.compare(userTotalSolves.get(b), userTotalSolves.get(a));
                if (comparesolves != 0) return comparesolves;
                return Integer.compare(userTotalPenalty.get(a), userTotalPenalty.get(b));
            });
            
            // Insert sorted users into combined leaderboard with ranks
            String insertSql = "INSERT INTO combined_leaderboard " +
                    "(username, total_solves, total_penalty, overall_rank, contests_participated, last_updated) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            
            for (int i = 0; i < sortedUsers.size(); i++) {
                String username = sortedUsers.get(i);
                java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(insertSql);
                pstmt.setString(1, username);
                pstmt.setInt(2, userTotalSolves.get(username));
                pstmt.setInt(3, userTotalPenalty.get(username));
                pstmt.setInt(4, i + 1); // 1-based ranking
                pstmt.setInt(5, userContestCount.get(username));
                pstmt.setString(6, LocalDateTime.now().format(formatter));
                pstmt.executeUpdate();
                pstmt.close();
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating combined leaderboard: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get combined leaderboard entries
     */
    public List<CombinedLeaderboardEntry> getCombinedLeaderboard() {
        List<CombinedLeaderboardEntry> leaderboard = new ArrayList<>();
        try {
            String sql = "SELECT * FROM combined_leaderboard ORDER BY overall_rank ASC";
            java.sql.Statement stmt = dbManager.getConnection().createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                CombinedLeaderboardEntry entry = new CombinedLeaderboardEntry(
                    rs.getString("username"),
                    rs.getInt("total_solves"),
                    rs.getInt("total_penalty"),
                    rs.getInt("overall_rank"),
                    rs.getInt("contests_participated")
                );
                leaderboard.add(entry);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error retrieving combined leaderboard: " + e.getMessage());
        }
        return leaderboard;
    }

    /**
     * Refresh the combined leaderboard by recalculating from all finalized contests
     */
    public void refreshCombinedLeaderboard() {
        try {
            updateCombinedLeaderboard();
            System.out.println("Combined leaderboard refreshed successfully");
        } catch (Exception e) {
            System.err.println("Error refreshing combined leaderboard: " + e.getMessage());
        }
    }

    /**
     * Finalize standings for a contest (mark standings as finalized)
     */
    public boolean finalizeContestStandings(String contestId) {
        try {
            String sql = "UPDATE leaderboard_contests SET standings_finalized = 1 WHERE contest_id = ?";
            java.sql.PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, contestId);
            pstmt.executeUpdate();
            pstmt.close();
            
            // Update combined leaderboard after finalizing standings
            updateCombinedLeaderboard();
            
            return true;
        } catch (Exception e) {
            System.err.println("Error finalizing contest standings: " + e.getMessage());
            return false;
        }
    }
}
