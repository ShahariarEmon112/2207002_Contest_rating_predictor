package com.contestpredictor.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with the Codeforces API.
 * Provides methods to fetch contest info, standings, and user info.
 */
public class CodeforcesApiService {
    private static final String BASE_URL = "https://codeforces.com/api/";
    private static final int RATE_LIMIT_MS = 2000; // 2 seconds between requests
    private static final int TIMEOUT_MS = 30000; // 30 seconds timeout
    private static final Gson gson = new Gson();
    
    private static long lastRequestTime = 0;
    
    // Cache for contest info to reduce API calls
    private static final Map<Integer, ContestInfo> contestCache = new HashMap<>();
    
    /**
     * DTO for contest information
     */
    public static class ContestInfo {
        public int id;
        public String name;
        public String phase;
        public int durationSeconds;
        public long startTimeSeconds;
        public LocalDateTime startTime;
        
        @Override
        public String toString() {
            return name + " (ID: " + id + ", Phase: " + phase + ")";
        }
    }
    
    /**
     * DTO for user standing in a contest
     */
    public static class StandingRow {
        public String handle;
        public int rank;
        public double points;
        public int penalty;
        public int problemsSolved;
        public int oldRating;
        public int newRating;
        public int ratingChange;
        
        @Override
        public String toString() {
            return String.format("Rank %d: %s (%.0f points)", rank, handle, points);
        }
    }
    
    /**
     * DTO for user info
     */
    public static class UserInfo {
        public String handle;
        public int rating;
        public int maxRating;
        public String rank;
        public String maxRank;
        public String firstName;
        public String lastName;
        public String country;
        public String organization;
        
        @Override
        public String toString() {
            return handle + " (Rating: " + rating + ", Rank: " + rank + ")";
        }
    }
    
    /**
     * API Response wrapper
     */
    public static class ApiResponse<T> {
        public boolean success;
        public String message;
        public T data;
        
        public static <T> ApiResponse<T> success(T data) {
            ApiResponse<T> response = new ApiResponse<>();
            response.success = true;
            response.data = data;
            return response;
        }
        
        public static <T> ApiResponse<T> error(String message) {
            ApiResponse<T> response = new ApiResponse<>();
            response.success = false;
            response.message = message;
            return response;
        }
    }
    
    /**
     * Fetch contest information by ID
     */
    public ApiResponse<ContestInfo> getContestInfo(int contestId) {
        // Check cache first
        if (contestCache.containsKey(contestId)) {
            return ApiResponse.success(contestCache.get(contestId));
        }
        
        try {
            String url = BASE_URL + "contest.list?gym=false";
            String response = makeApiRequest(url);
            
            JsonObject json = gson.fromJson(response, JsonObject.class);
            
            if (!"OK".equals(json.get("status").getAsString())) {
                return ApiResponse.error("API returned error status");
            }
            
            JsonArray contests = json.getAsJsonArray("result");
            
            for (JsonElement element : contests) {
                JsonObject contestJson = element.getAsJsonObject();
                int id = contestJson.get("id").getAsInt();
                
                if (id == contestId) {
                    ContestInfo info = parseContestInfo(contestJson);
                    contestCache.put(contestId, info);
                    return ApiResponse.success(info);
                }
            }
            
            return ApiResponse.error("Contest with ID " + contestId + " not found");
            
        } catch (Exception e) {
            return ApiResponse.error("Failed to fetch contest info: " + e.getMessage());
        }
    }
    
    /**
     * Fetch contest standings
     */
    public ApiResponse<List<StandingRow>> getContestStandings(int contestId) {
        return getContestStandings(contestId, null, 0);
    }
    
    /**
     * Fetch contest standings with optional filters
     */
    public ApiResponse<List<StandingRow>> getContestStandings(int contestId, List<String> handles, int count) {
        try {
            StringBuilder url = new StringBuilder(BASE_URL + "contest.standings?contestId=" + contestId);
            
            if (handles != null && !handles.isEmpty()) {
                url.append("&handles=").append(String.join(";", handles));
            }
            
            if (count > 0) {
                url.append("&from=1&count=").append(count);
            }
            
            url.append("&showUnofficial=false");
            
            String response = makeApiRequest(url.toString());
            
            JsonObject json = gson.fromJson(response, JsonObject.class);
            
            if (!"OK".equals(json.get("status").getAsString())) {
                String comment = json.has("comment") ? json.get("comment").getAsString() : "Unknown error";
                return ApiResponse.error("API error: " + comment);
            }
            
            JsonObject result = json.getAsJsonObject("result");
            JsonArray rows = result.getAsJsonArray("rows");
            
            List<StandingRow> standings = new ArrayList<>();
            
            for (JsonElement element : rows) {
                JsonObject row = element.getAsJsonObject();
                StandingRow standing = new StandingRow();
                
                standing.rank = row.get("rank").getAsInt();
                standing.points = row.get("points").getAsDouble();
                standing.penalty = row.get("penalty").getAsInt();
                
                // Get handle from party.members
                JsonObject party = row.getAsJsonObject("party");
                JsonArray members = party.getAsJsonArray("members");
                if (members.size() > 0) {
                    standing.handle = members.get(0).getAsJsonObject().get("handle").getAsString();
                }
                
                // Count problems solved
                JsonArray problemResults = row.getAsJsonArray("problemResults");
                int solved = 0;
                for (JsonElement pr : problemResults) {
                    if (pr.getAsJsonObject().get("points").getAsDouble() > 0) {
                        solved++;
                    }
                }
                standing.problemsSolved = solved;
                
                standings.add(standing);
            }
            
            return ApiResponse.success(standings);
            
        } catch (Exception e) {
            return ApiResponse.error("Failed to fetch standings: " + e.getMessage());
        }
    }
    
    /**
     * Fetch user info by handle
     */
    public ApiResponse<UserInfo> getUserInfo(String handle) {
        try {
            String url = BASE_URL + "user.info?handles=" + handle;
            String response = makeApiRequest(url);
            
            JsonObject json = gson.fromJson(response, JsonObject.class);
            
            if (!"OK".equals(json.get("status").getAsString())) {
                String comment = json.has("comment") ? json.get("comment").getAsString() : "Unknown error";
                return ApiResponse.error("API error: " + comment);
            }
            
            JsonArray users = json.getAsJsonArray("result");
            
            if (users.size() == 0) {
                return ApiResponse.error("User not found: " + handle);
            }
            
            JsonObject userJson = users.get(0).getAsJsonObject();
            UserInfo user = new UserInfo();
            
            user.handle = userJson.get("handle").getAsString();
            user.rating = userJson.has("rating") ? userJson.get("rating").getAsInt() : 0;
            user.maxRating = userJson.has("maxRating") ? userJson.get("maxRating").getAsInt() : 0;
            user.rank = userJson.has("rank") ? userJson.get("rank").getAsString() : "unrated";
            user.maxRank = userJson.has("maxRank") ? userJson.get("maxRank").getAsString() : "unrated";
            user.firstName = userJson.has("firstName") ? userJson.get("firstName").getAsString() : "";
            user.lastName = userJson.has("lastName") ? userJson.get("lastName").getAsString() : "";
            user.country = userJson.has("country") ? userJson.get("country").getAsString() : "";
            user.organization = userJson.has("organization") ? userJson.get("organization").getAsString() : "";
            
            return ApiResponse.success(user);
            
        } catch (Exception e) {
            return ApiResponse.error("Failed to fetch user info: " + e.getMessage());
        }
    }
    
    /**
     * Validate if a Codeforces handle exists
     */
    public ApiResponse<Boolean> validateHandle(String handle) {
        ApiResponse<UserInfo> response = getUserInfo(handle);
        if (response.success) {
            return ApiResponse.success(true);
        } else {
            return ApiResponse.error(response.message);
        }
    }
    
    /**
     * Get user's rating changes (contest history)
     */
    public ApiResponse<List<Map<String, Object>>> getUserRatingChanges(String handle) {
        try {
            String url = BASE_URL + "user.rating?handle=" + handle;
            String response = makeApiRequest(url);
            
            JsonObject json = gson.fromJson(response, JsonObject.class);
            
            if (!"OK".equals(json.get("status").getAsString())) {
                String comment = json.has("comment") ? json.get("comment").getAsString() : "Unknown error";
                return ApiResponse.error("API error: " + comment);
            }
            
            JsonArray changes = json.getAsJsonArray("result");
            List<Map<String, Object>> ratingChanges = new ArrayList<>();
            
            for (JsonElement element : changes) {
                JsonObject change = element.getAsJsonObject();
                Map<String, Object> ratingChange = new HashMap<>();
                
                ratingChange.put("contestId", change.get("contestId").getAsInt());
                ratingChange.put("contestName", change.get("contestName").getAsString());
                ratingChange.put("rank", change.get("rank").getAsInt());
                ratingChange.put("oldRating", change.get("oldRating").getAsInt());
                ratingChange.put("newRating", change.get("newRating").getAsInt());
                
                ratingChanges.add(ratingChange);
            }
            
            return ApiResponse.success(ratingChanges);
            
        } catch (Exception e) {
            return ApiResponse.error("Failed to fetch rating changes: " + e.getMessage());
        }
    }
    
    /**
     * Get recent contests
     */
    public ApiResponse<List<ContestInfo>> getRecentContests(int count) {
        try {
            String url = BASE_URL + "contest.list?gym=false";
            String response = makeApiRequest(url);
            
            JsonObject json = gson.fromJson(response, JsonObject.class);
            
            if (!"OK".equals(json.get("status").getAsString())) {
                return ApiResponse.error("API returned error status");
            }
            
            JsonArray contests = json.getAsJsonArray("result");
            List<ContestInfo> recentContests = new ArrayList<>();
            
            int added = 0;
            for (JsonElement element : contests) {
                JsonObject contestJson = element.getAsJsonObject();
                String phase = contestJson.get("phase").getAsString();
                
                // Only include finished contests
                if ("FINISHED".equals(phase)) {
                    ContestInfo info = parseContestInfo(contestJson);
                    recentContests.add(info);
                    contestCache.put(info.id, info);
                    
                    added++;
                    if (added >= count) break;
                }
            }
            
            return ApiResponse.success(recentContests);
            
        } catch (Exception e) {
            return ApiResponse.error("Failed to fetch recent contests: " + e.getMessage());
        }
    }
    
    /**
     * Find a user's standing in a specific contest
     */
    public ApiResponse<StandingRow> getUserStandingInContest(int contestId, String handle) {
        List<String> handles = new ArrayList<>();
        handles.add(handle);
        
        ApiResponse<List<StandingRow>> response = getContestStandings(contestId, handles, 0);
        
        if (!response.success) {
            return ApiResponse.error(response.message);
        }
        
        if (response.data.isEmpty()) {
            return ApiResponse.error("User " + handle + " did not participate in contest " + contestId);
        }
        
        return ApiResponse.success(response.data.get(0));
    }
    
    // ==================== Private Helper Methods ====================
    
    private ContestInfo parseContestInfo(JsonObject contestJson) {
        ContestInfo info = new ContestInfo();
        info.id = contestJson.get("id").getAsInt();
        info.name = contestJson.get("name").getAsString();
        info.phase = contestJson.get("phase").getAsString();
        info.durationSeconds = contestJson.get("durationSeconds").getAsInt();
        
        if (contestJson.has("startTimeSeconds")) {
            info.startTimeSeconds = contestJson.get("startTimeSeconds").getAsLong();
            info.startTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(info.startTimeSeconds),
                ZoneId.systemDefault()
            );
        }
        
        return info;
    }
    
    private String makeApiRequest(String urlString) throws Exception {
        // Rate limiting
        synchronized (CodeforcesApiService.class) {
            long timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime;
            if (timeSinceLastRequest < RATE_LIMIT_MS) {
                Thread.sleep(RATE_LIMIT_MS - timeSinceLastRequest);
            }
            lastRequestTime = System.currentTimeMillis();
        }
        
        @SuppressWarnings("deprecation")
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "Contest-Rating-Predictor/1.0");
        
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            throw new Exception("HTTP error code: " + responseCode);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return response.toString();
    }
    
    /**
     * Clear the contest cache
     */
    public void clearCache() {
        contestCache.clear();
    }
}
