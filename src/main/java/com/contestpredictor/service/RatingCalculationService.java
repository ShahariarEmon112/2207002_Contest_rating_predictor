package com.contestpredictor.service;

import com.contestpredictor.data.DatabaseManager;
import com.contestpredictor.model.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for calculating ratings in the Selection Contest system.
 * Implements weighted rating calculation: Final Rating = Σ(SubContestRating × Weight)
 */
public class RatingCalculationService {
    
    private final DatabaseManager dbManager;
    private final CodeforcesApiService cfService;
    
    public RatingCalculationService() {
        this.dbManager = DatabaseManager.getInstance();
        this.cfService = new CodeforcesApiService();
    }
    
    /**
     * Calculate rating for a single participant in a sub-contest
     * Uses an enhanced Elo-based rating system
     */
    public double calculateSubContestRating(int rank, int totalParticipants, double points, 
                                            int problemsSolved, int penalty, int currentRating) {
        if (rank <= 0 || totalParticipants <= 0) {
            return 0.0;
        }
        
        // Base performance rating from rank
        double performance = calculatePerformanceRating(rank, totalParticipants, 1500);
        
        // Problem solving bonus (each problem adds value)
        double solvingBonus = problemsSolved * 50.0;
        
        // Points bonus (for CF-style scoring)
        double pointsBonus = points * 10.0;
        
        // Penalty reduction (lower penalty is better)
        double penaltyFactor = Math.max(0.5, 1.0 - (penalty / 10000.0));
        
        // Calculate raw rating
        double rawRating = (performance * 0.6) + solvingBonus + (pointsBonus * penaltyFactor);
        
        // Apply Elo-style adjustment based on current rating
        double expectedRank = calculateExpectedRank(currentRating, 1500, totalParticipants);
        double rankDiff = expectedRank - rank;
        
        if (rankDiff > 0) {
            // Performed better than expected - bonus
            rawRating += Math.min(rankDiff * 5.0, 200);
        } else {
            // Performed worse than expected - penalty
            rawRating += Math.max(rankDiff * 3.0, -100);
        }
        
        // Ensure minimum rating of 0
        return Math.max(0, rawRating);
    }
    
    /**
     * Calculate performance rating based on rank percentile
     */
    private double calculatePerformanceRating(int rank, int totalParticipants, double avgRating) {
        // Percentile-based performance
        double percentile = 1.0 - ((double) rank / totalParticipants);
        
        // Convert percentile to rating using logistic function
        double performance = avgRating + 400 * Math.log10(percentile / (1 - percentile + 0.01));
        
        // Clamp to reasonable bounds
        return Math.max(0, Math.min(4000, performance));
    }
    
    /**
     * Calculate expected rank based on rating
     */
    private double calculateExpectedRank(int rating, double avgRating, int totalParticipants) {
        double expectedPercentile = 1.0 / (1.0 + Math.pow(10.0, (avgRating - rating) / 400.0));
        return totalParticipants * (1.0 - expectedPercentile) + 1;
    }
    
    /**
     * Fetch standings from Codeforces and calculate ratings for all registrations in a sub-contest
     */
    public boolean fetchAndCalculateSubContestResults(SubContest subContest, List<Registration> registrations) {
        // Fetch standings from CF
        CodeforcesApiService.ApiResponse<List<CodeforcesApiService.StandingRow>> response = 
            cfService.getContestStandings(subContest.getCodeforcesContestId());
        
        if (!response.success) {
            System.err.println("Failed to fetch standings: " + response.message);
            return false;
        }
        
        List<CodeforcesApiService.StandingRow> standings = response.data;
        int totalParticipants = standings.size();
        
        // Create a map of handles to standings for quick lookup
        Map<String, CodeforcesApiService.StandingRow> standingsMap = new HashMap<>();
        for (CodeforcesApiService.StandingRow row : standings) {
            standingsMap.put(row.handle.toLowerCase(), row);
        }
        
        // Process each registration
        for (Registration reg : registrations) {
            String handle = reg.getCodeforcesHandle().toLowerCase();
            
            ContestResult result = new ContestResult(reg.getId(), subContest.getId());
            result.setCodeforcesHandle(reg.getCodeforcesHandle());
            
            if (standingsMap.containsKey(handle)) {
                CodeforcesApiService.StandingRow standing = standingsMap.get(handle);
                
                result.setCfRank(standing.rank);
                result.setCfPoints(standing.points);
                result.setCfPenalty(standing.penalty);
                result.setProblemsSolved(standing.problemsSolved);
                result.setParticipated(true);
                
                // Calculate rating
                double calculatedRating = calculateSubContestRating(
                    standing.rank,
                    totalParticipants,
                    standing.points,
                    standing.problemsSolved,
                    standing.penalty,
                    1500 // Default starting rating
                );
                
                result.setCalculatedRating(calculatedRating);
                result.calculateWeightedRating(subContest.getWeight());
                
            } else {
                // User didn't participate
                result.setParticipated(false);
                result.setCalculatedRating(0);
                result.setWeightedRating(0);
            }
            
            result.setFetchedAt(LocalDateTime.now());
            dbManager.saveContestResult(result);
        }
        
        // Mark sub-contest as fetched
        subContest.setFetched(true);
        dbManager.updateSubContest(subContest);
        
        return true;
    }
    
    /**
     * Calculate and save final ratings for all registrations in a selection contest
     */
    public boolean calculateFinalRatings(SelectionContest selectionContest) {
        List<Registration> registrations = dbManager.getRegistrationsByContestId(selectionContest.getId());
        List<SubContest> subContests = dbManager.getSubContestsBySelectionId(selectionContest.getId());
        
        if (registrations.isEmpty() || subContests.isEmpty()) {
            return false;
        }
        
        List<FinalRating> allRatings = new ArrayList<>();
        
        // Calculate final rating for each registration
        for (Registration reg : registrations) {
            if (reg.getStatus() != Registration.RegistrationStatus.ACTIVE) {
                continue;
            }
            
            List<ContestResult> results = dbManager.getResultsByRegistrationId(reg.getId());
            
            FinalRating finalRating = new FinalRating(reg.getId(), selectionContest.getId());
            finalRating.setUserId(reg.getUserId());
            finalRating.setUsername(reg.getUsername());
            finalRating.setCodeforcesHandle(reg.getCodeforcesHandle());
            finalRating.setSelectionContestName(selectionContest.getName());
            
            // Calculate weighted sum
            finalRating.calculateFromResults(results, subContests);
            
            allRatings.add(finalRating);
        }
        
        // Sort by final rating (descending) and assign ranks
        allRatings.sort((a, b) -> Double.compare(b.getFinalRating(), a.getFinalRating()));
        
        int rank = 1;
        double prevRating = -1;
        int sameRankCount = 0;
        
        for (FinalRating rating : allRatings) {
            if (rating.getFinalRating() == prevRating) {
                // Same rating = same rank
                sameRankCount++;
            } else {
                rank += sameRankCount;
                sameRankCount = 1;
                prevRating = rating.getFinalRating();
            }
            
            rating.setOverallRank(rank);
            rating.setCalculatedAt(LocalDateTime.now());
            dbManager.saveFinalRating(rating);
        }
        
        return true;
    }
    
    /**
     * Recalculate all results for a selection contest
     * This fetches fresh data from Codeforces and recalculates everything
     */
    public boolean recalculateAllResults(int selectionContestId) {
        SelectionContest contest = dbManager.getSelectionContestById(selectionContestId);
        if (contest == null) {
            return false;
        }
        
        List<Registration> registrations = dbManager.getRegistrationsByContestId(selectionContestId);
        List<SubContest> subContests = dbManager.getSubContestsBySelectionId(selectionContestId);
        
        // Fetch and calculate for each sub-contest
        for (SubContest sc : subContests) {
            // Only fetch finished contests
            if ("FINISHED".equals(sc.getPhase()) || sc.getPhase() == null) {
                fetchAndCalculateSubContestResults(sc, registrations);
            }
        }
        
        // Calculate final ratings
        return calculateFinalRatings(contest);
    }
    
    /**
     * Get standings for a selection contest
     */
    public List<FinalRating> getStandings(int selectionContestId) {
        return dbManager.getFinalRatingsByContestId(selectionContestId);
    }
    
    /**
     * Get detailed results for a specific user in a selection contest
     */
    public Map<String, Object> getUserDetailedResults(int userId, int selectionContestId) {
        Map<String, Object> details = new HashMap<>();
        
        Registration reg = dbManager.getRegistration(userId, selectionContestId);
        if (reg == null) {
            return null;
        }
        
        FinalRating finalRating = dbManager.getFinalRating(userId, selectionContestId);
        List<ContestResult> results = dbManager.getResultsByRegistrationId(reg.getId());
        
        details.put("registration", reg);
        details.put("finalRating", finalRating);
        details.put("subContestResults", results);
        
        return details;
    }
}
