package com.contestpredictor.data;

import com.contestpredictor.model.Contest;
import com.contestpredictor.model.Participant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Legacy ContestDatabase - now uses in-memory storage only.
 * For the new Selection Contest system, use DatabaseManager directly.
 * This class is kept for backward compatibility with existing controllers.
 */
public class ContestDatabase {
    private static ContestDatabase instance;
    private List<Contest> contests;

    private ContestDatabase() {
        contests = new ArrayList<>();
        initializeContests();
    }

    public static ContestDatabase getInstance() {
        if (instance == null) {
            instance = new ContestDatabase();
        }
        return instance;
    }

    private void initializeContests() {
        // Past Codeforces Contests
        Contest cf1 = new Contest("CF918", "Codeforces Round 918 (Div. 2)", 
            LocalDateTime.of(2024, 11, 15, 17, 35), 120, true);
        Contest cf2 = new Contest("CF920", "Codeforces Round 920 (Div. 2)", 
            LocalDateTime.of(2024, 11, 28, 17, 35), 120, true);
        Contest cf3 = new Contest("CF922", "Codeforces Round 922 (Div. 1 + Div. 2)", 
            LocalDateTime.of(2024, 12, 5, 17, 35), 150, true);
        
        // Past AtCoder Contests
        Contest abc1 = new Contest("ABC335", "AtCoder Beginner Contest 335", 
            LocalDateTime.of(2024, 11, 10, 21, 0), 100, true);
        Contest abc2 = new Contest("ABC336", "AtCoder Beginner Contest 336", 
            LocalDateTime.of(2024, 11, 24, 21, 0), 100, true);

        // Add participants to past contests
        addRealisticParticipantsToPastContest(cf1);
        addRealisticParticipantsToPastContest(cf2);
        addRealisticParticipantsToPastContest(cf3);
        addRealisticParticipantsToPastContest(abc1);
        addRealisticParticipantsToPastContest(abc2);

        contests.add(cf1);
        contests.add(cf2);
        contests.add(cf3);
        contests.add(abc1);
        contests.add(abc2);

        // Future contests
        Contest cf_future1 = new Contest("CF925", "Codeforces Round 925 (Div. 2)", 
            LocalDateTime.of(2025, 12, 24, 17, 35), 120, false);
        Contest cf_future2 = new Contest("CF926", "Codeforces Round 926 (Div. 1 + Div. 2)", 
            LocalDateTime.of(2025, 12, 28, 17, 35), 150, false);
        
        contests.add(cf_future1);
        contests.add(cf_future2);
    }

    private void addRealisticParticipantsToPastContest(Contest contest) {
        String[] usernames = {
            "tourist", "jiangly", "Benq", "ecnerwala", "Um_nik",
            "maroonrk", "Radewoosh", "Petr", "mnbvmar", "ksun48",
            "scott_wu", "ainta", "Swistakk", "Enchom", "aid",
            "ko_osaga", "Shayan", "sunset", "orzdevinwang", "gamegame"
        };

        Random random = new Random(contest.getContestId().hashCode());
        
        for (int i = 0; i < 20; i++) {
            String username = usernames[i % usernames.length];
            int rating = 1000 + random.nextInt(1500);
            int solved = random.nextInt(7);
            int predicted = rating + (random.nextInt(200) - 100);
            
            Participant p = new Participant(username, rating, solved, predicted);
            contest.addParticipant(p);
        }
    }

    public List<Contest> getAllContests() {
        return new ArrayList<>(contests);
    }

    public Contest getContestById(String contestId) {
        return contests.stream()
            .filter(c -> c.getContestId().equals(contestId))
            .findFirst()
            .orElse(null);
    }

    public List<Contest> getPastContests() {
        List<Contest> past = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Contest c : contests) {
            if (c.getDateTime().isBefore(now) || c.isPast()) {
                past.add(c);
            }
        }
        return past;
    }

    public List<Contest> getUpcomingContests() {
        List<Contest> upcoming = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Contest c : contests) {
            if (c.getDateTime().isAfter(now) && !c.isPast()) {
                upcoming.add(c);
            }
        }
        return upcoming;
    }
    
    public List<Contest> getFutureContests() {
        return getUpcomingContests();
    }
    
    public List<Contest> searchContests(String query) {
        List<Contest> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Contest c : contests) {
            if (c.getContestId().toLowerCase().contains(lowerQuery) || 
                c.getContestName().toLowerCase().contains(lowerQuery)) {
                results.add(c);
            }
        }
        return results;
    }

    public void addContest(Contest contest) {
        if (getContestById(contest.getContestId()) == null) {
            contests.add(contest);
        }
    }

    public void updateContest(Contest contest) {
        for (int i = 0; i < contests.size(); i++) {
            if (contests.get(i).getContestId().equals(contest.getContestId())) {
                contests.set(i, contest);
                break;
            }
        }
    }
}
