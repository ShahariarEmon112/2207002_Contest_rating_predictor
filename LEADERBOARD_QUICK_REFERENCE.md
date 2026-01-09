# KUET Team Formation Leaderboard - Quick Reference

## System Components Overview

### 📊 Models (3 new)
- **LeaderboardContest** - Contest definition
- **LeaderboardEntry** - Individual contest standings
- **CombinedLeaderboardEntry** - Overall rankings

### 💾 Database (1 new)
- **LeaderboardDatabase** - Handles all leaderboard operations

### 🎮 Controllers (3 new)
- **AdminManageLeaderboardController** - Admin interface
- **CreateLeaderboardContestController** - Contest creation dialog
- **UserLeaderboardController** - User interface with 4 tabs

### 🎨 UI Files (3 new)
- **AdminManageLeaderboard.fxml** - Admin dashboard
- **CreateLeaderboardContest.fxml** - Contest creation form
- **UserLeaderboard.fxml** - User leaderboard viewer

### 🔧 Database Tables (4 new)
- `leaderboard_contests` - Contest data
- `leaderboard_registrations` - User registrations
- `leaderboard_entries` - Individual standings
- `combined_leaderboard` - Overall rankings

---

## How to Use

### For Admins:
```
1. Open Admin Dashboard
2. Click "🎯 Leaderboard" button (top navbar or Tab 4)
3. Click "➕ New Contest" to create contest
4. Select contest from list
5. Add participants and standings:
   - Username
   - Solve Count
   - Penalty
   - Time (minutes)
6. Click "✅ Finalize Standings" to lock and update combined leaderboard
```

### For Users:
```
1. Open Profile page
2. Click "🏆 Team Leaderboard" button
3. Choose tab:
   - Available Contests → Register for contests
   - My Registrations → See your contests
   - Contest Standings → View specific contest standings
   - Overall Leaderboard → See your overall rank
```

---

## Key Workflows

### Creating a Contest
1. Admin → Dashboard → Leaderboard Tab/Button
2. Click "New Contest"
3. Fill form:
   - Name
   - Description
   - Start Date
   - End Date
   - Max Problems
4. Click "Create Contest"

### Managing Standings
1. Select contest from list
2. Enter participant username
3. Enter solve count, penalty, time
4. Click "Add Entry" OR "Update Entry"
5. Click "Finalize Standings" when done

### Registering for Contest
1. User → Profile → Team Leaderboard
2. Tab 1: Available Contests
3. Select contest
4. Click "Register for Contest"
5. Check Tab 2: My Registrations to confirm

### Viewing Overall Standings
1. User → Profile → Team Leaderboard
2. Tab 4: Overall Leaderboard
3. See rankings, total solves, total penalty

---

## Database Operations

### Common Queries (via LeaderboardDatabase):

**Create Contest:**
```java
LeaderboardContest contest = new LeaderboardContest(...);
leaderboardDB.createLeaderboardContest(contest);
```

**Register User:**
```java
leaderboardDB.registerUserForLeaderboardContest(contestId, username);
```

**Add Standings:**
```java
LeaderboardEntry entry = new LeaderboardEntry(...);
leaderboardDB.addLeaderboardEntry(entry);
```

**Finalize & Update:**
```java
leaderboardDB.finalizeContestStandings(contestId);
// This automatically updates combined leaderboard
```

**Get Combined Leaderboard:**
```java
List<CombinedLeaderboardEntry> rankings = leaderboardDB.getCombinedLeaderboard();
```

---

## Button Reference

### Admin Buttons
| Button | Action | Location |
|--------|--------|----------|
| 🎯 Leaderboard | Navigate to leaderboard | Admin Dashboard navbar |
| ➕ New Contest | Open contest creation dialog | AdminManageLeaderboard |
| ➕ Add Entry | Add standings entry | AdminManageLeaderboard |
| ✏️ Update Entry | Modify existing entry | AdminManageLeaderboard |
| 🗑️ Delete Entry | Remove standings entry | AdminManageLeaderboard |
| ✅ Finalize | Lock standings, update combined | AdminManageLeaderboard |
| ← Back | Return to dashboard | AdminManageLeaderboard |

### User Buttons
| Button | Action | Location |
|--------|--------|----------|
| 🏆 Team Leaderboard | Open leaderboard | Profile |
| ✅ Register | Join contest | UserLeaderboard Tab 1 |
| ❌ Unregister | Leave contest | UserLeaderboard Tab 1 |
| 📊 View Standings | See contest rankings | UserLeaderboard Tab 1 |
| ← Back | Return to profile | UserLeaderboard |

---

## Data Flow

```
Admin Creates Contest
    ↓
Admin Adds Participants
    ↓
Users Register via UI
    ↓
Admin Enters Standings (solves, penalty, time)
    ↓
Admin Finalizes Contest
    ↓
System Updates Combined Leaderboard
    ↓
Users View Overall Rankings
```

---

## File Locations

```
src/main/java/com/contestpredictor/
├── model/
│   ├── LeaderboardContest.java
│   ├── LeaderboardEntry.java
│   └── CombinedLeaderboardEntry.java
├── data/
│   └── LeaderboardDatabase.java
└── controller/
    ├── AdminManageLeaderboardController.java
    ├── CreateLeaderboardContestController.java
    └── UserLeaderboardController.java

src/main/resources/
├── fxml/
│   ├── AdminManageLeaderboard.fxml
│   ├── CreateLeaderboardContest.fxml
│   └── UserLeaderboard.fxml
└── css/
    └── styles.css (updated with leaderboard styles)
```

---

## Integration Points

### AdminDashboardController
```java
@FXML
public void handleManageLeaderboard() {
    // Opens AdminManageLeaderboard.fxml
}
```

### ProfileController
```java
@FXML
private void handleViewLeaderboard() {
    // Opens UserLeaderboard.fxml
}
```

---

## Error Handling

All operations include:
- ✓ Input validation
- ✓ Database error checking
- ✓ User feedback via alerts
- ✓ Exception logging
- ✓ Graceful failure

---

## Ranking Algorithm

### Combined Leaderboard Calculation:
1. Collect all entries from finalized contests
2. Aggregate by username:
   - Sum all solve counts
   - Sum all penalties
   - Count contests participated
3. Sort by:
   - Primary: Total solves (descending - more is better)
   - Secondary: Total penalty (ascending - less is better)
4. Assign ranks 1, 2, 3, ...

### Example:
```
User A: 10 solves, 100 penalty → Rank 1
User B: 10 solves, 150 penalty → Rank 2 (more penalty)
User C: 8 solves, 50 penalty  → Rank 3 (fewer solves)
```

---

## CSS Classes

```css
.section-box              /* Contest container */
.section-title           /* Section heading */
.info-label             /* Information text */
.button-success         /* Green buttons */
.button-warning         /* Orange buttons */
.button-danger          /* Red buttons */
.button-primary         /* Blue buttons */
.navbar-button-success  /* Navbar green buttons */
.tab-pane               /* Tab container */
.leaderboard-tab-content /* Tab content styling */
```

---

## Testing Scenarios

### Admin Test:
1. Create contest with 5 problems
2. Add 10 participants with varying solves
3. Finalize standings
4. Verify combined leaderboard updated

### User Test:
1. Register for 2 contests
2. View my registrations (should show 2)
3. Check overall leaderboard
4. Verify own rank calculation

### Edge Cases:
- Unregister after contest finalized
- Update standings multiple times
- Finalize contest with no entries
- Register duplicate user (should fail)

---

## Performance Notes

- All database operations use prepared statements
- UNIQUE constraints prevent data duplication
- Leaderboard calculation is O(n log n) due to sorting
- No N+1 queries - batch loading implemented

---

## Future Considerations

- Add team-based contests
- Implement real-time leaderboard updates
- Add contest categories/difficulty levels
- Support for concurrent contests
- Leaderboard snapshots/history
- Custom scoring systems
- Export functionality

---

## Support & Troubleshooting

### Issue: Contest not appearing in list
- Solution: Refresh page, check database connection

### Issue: Standings not finalizing
- Solution: Ensure all required fields filled, check logs

### Issue: Combined leaderboard empty
- Solution: Finalize at least one contest first

### Issue: Navigation not working
- Solution: Check FXML file exists in resources folder

---

**Version:** 1.0  
**Last Updated:** 2026-01-08  
**Status:** Production Ready ✓
