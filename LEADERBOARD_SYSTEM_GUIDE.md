# KUET Team Formation Contest Leaderboard System - Implementation Guide

## Overview
A new comprehensive leaderboard system has been successfully integrated into the Contest Rating Predictor application. This system allows admins to create and manage team formation contests, users to register and participate, and generate both individual contest standings and a combined overall leaderboard.

---

## System Architecture

### 1. **Data Models**

#### LeaderboardContest.java
- Represents a single leaderboard contest that can be created by admins
- **Key Properties:**
  - `contestId`: Unique identifier (format: LBC_<timestamp>)
  - `contestName`: Name of the contest
  - `description`: Contest description
  - `startDate` & `endDate`: Contest duration
  - `maxProblems`: Number of problems in the contest
  - `isActive`: Whether registrations are open
  - `standings_finalized`: Whether final standings have been calculated
  - `createdByAdmin`: Username of the admin who created it
  - `registeredUsers`: List of registered participants

#### LeaderboardEntry.java
- Represents an individual user's performance in a specific contest
- **Key Properties:**
  - `username`: Participant username
  - `contestId`: Associated contest
  - `rank`: Participant's rank in this contest
  - `solveCount`: Number of problems solved
  - `totalPenalty`: Total penalty (time + wrong submissions)
  - `totalTime`: Total time spent
  - `status`: Completion status

#### CombinedLeaderboardEntry.java
- Represents a user's aggregate performance across ALL leaderboard contests
- **Key Properties:**
  - `username`: Participant username
  - `totalSolves`: Combined solves from all contests
  - `totalPenalty`: Combined penalty from all contests
  - `overallRank`: Overall ranking position
  - `contestsParticipated`: Number of contests participated in

---

### 2. **Database Layer**

#### LeaderboardDatabase.java
Complete database manager for all leaderboard operations:

**Leaderboard Contest Operations:**
- `createLeaderboardContest()` - Create a new contest
- `getAllLeaderboardContests()` - Retrieve all contests
- `getLeaderboardContestById()` - Get specific contest
- `updateLeaderboardContestStatus()` - Enable/disable contest

**User Registration Operations:**
- `registerUserForLeaderboardContest()` - Register user for contest
- `unregisterUserFromLeaderboardContest()` - Unregister user
- `isUserRegisteredForContest()` - Check registration status
- `getRegisteredUsersForContest()` - Get list of registered users

**Leaderboard Entry Operations (Individual Contest):**
- `addLeaderboardEntry()` - Add/update standing entry
- `getContestStandings()` - Get all standings for a contest
- `deleteContestStandings()` - Clear standings (for recalculation)

**Combined Leaderboard Operations:**
- `updateCombinedLeaderboard()` - Calculate and update overall standings
- `getCombinedLeaderboard()` - Get overall leaderboard
- `finalizeContestStandings()` - Mark contest as finalized and update combined leaderboard

#### Database Tables Created:
1. **leaderboard_contests** - Contest metadata
2. **leaderboard_registrations** - User registrations for contests
3. **leaderboard_entries** - Individual contest standings
4. **combined_leaderboard** - Overall rankings across all contests

---

### 3. **Controllers**

#### AdminManageLeaderboardController.java
Manages leaderboard administration:
- **Functionality:**
  - View all leaderboard contests
  - Add/update/delete standings entries
  - Manage solve counts and penalties
  - Finalize contest standings
  - Trigger combined leaderboard update
- **UI Components:**
  - Contest selection ListView
  - Standings TableView
  - Entry management form (username, solve count, penalty, time)
  - Action buttons for CRUD operations

#### CreateLeaderboardContestController.java
Dialog controller for creating new contests:
- **Inputs:**
  - Contest name
  - Description
  - Start and end dates
  - Maximum number of problems
- **Validation:**
  - Ensures all required fields are filled
  - Validates date sequence (start before end)
  - Auto-generates unique contest ID

#### UserLeaderboardController.java
User-facing leaderboard interface:
- **Tab 1: Available Contests**
  - View all active leaderboard contests
  - Register/unregister for contests
  - View contest standings
- **Tab 2: My Registrations**
  - See contests user is registered for
  - Track participation status
- **Tab 3: Contest Standings**
  - View detailed standings for selected contest
  - See participant rankings, solves, and penalties
- **Tab 4: Overall Leaderboard**
  - View the combined leaderboard
  - See overall rankings based on total solves and penalty

---

### 4. **User Interface (FXML Files)**

#### AdminManageLeaderboard.fxml
Admin dashboard for leaderboard management:
- Contest list with selection
- Standings table with sorting
- Entry management form
- Create, update, delete, and finalize buttons

#### CreateLeaderboardContest.fxml
Modal dialog for contest creation:
- Form fields for contest details
- Date pickers for contest duration
- Spinner for problem count
- Success/error feedback

#### UserLeaderboard.fxml
Multi-tab user interface:
- Tab-based navigation
- Contest discovery and registration
- Standings viewing
- Overall leaderboard display

---

## Workflow

### Admin Workflow:
1. **Access Leaderboard Management**
   - From Admin Dashboard → Click "🎯 Leaderboard" button or Tab 4
   - Navigates to AdminManageLeaderboard page

2. **Create Contest**
   - Click "➕ New Contest" button
   - Fill in contest details (name, description, dates, max problems)
   - Click "✅ Create Contest"

3. **Add Standings**
   - Select contest from list
   - Enter participant details (username, solve count, penalty, time)
   - Click "➕ Add Entry"

4. **Update Standings**
   - Select contestant from table
   - Modify solve count, penalty, or time
   - Click "✏️ Update Entry"

5. **Finalize Standings**
   - After all entries are added/updated
   - Click "✅ Finalize Standings"
   - System automatically updates combined leaderboard

### User Workflow:
1. **Access Leaderboard**
   - From Profile page → Click "🏆 Team Leaderboard" button
   - Navigates to UserLeaderboard page

2. **View Available Contests**
   - Tab 1 shows all active contests
   - Click on contest to select
   - View basic contest information

3. **Register for Contest**
   - Select desired contest
   - Click "✅ Register for Contest"
   - User appears in contest participant list

4. **View My Registrations**
   - Tab 2 shows registered contests
   - Track which contests user is participating in

5. **View Contest Standings**
   - Tab 3 displays standings after contest is finalized
   - See your rank, solves, and penalty

6. **View Overall Leaderboard**
   - Tab 4 shows combined rankings
   - See total solves, total penalty, and overall rank
   - Compare performance across all contests

---

## Key Features

### 1. **Contest Management**
- Admins can create unlimited contests
- Each contest has independent standings
- Contests can be activated/deactivated
- Date-based contest scheduling

### 2. **User Registration**
- Users can freely register/unregister
- Registration tracking per contest
- No duplicate registrations
- Registration status verification

### 3. **Standings Management**
- Flexible solve count tracking
- Penalty calculation (time + wrong submissions)
- Automatic ranking based on solve count and penalty
- Support for multiple problem configurations

### 4. **Combined Leaderboard**
- Automatically aggregates all finalized contests
- Combines scores: Total Solves + Total Penalty
- Primary sort: Total solves (descending)
- Secondary sort: Total penalty (ascending)
- Tracks contest participation count

### 5. **Data Persistence**
- All data stored in SQLite database
- Automatic schema initialization
- Foreign key relationships maintained
- UNIQUE constraints prevent duplicates

---

## Navigation Integration

### Updated Navigation Paths:

**Admin Dashboard:**
```
Admin Dashboard → Tab 4: "🎯 KUET Team Formation Leaderboard"
                    ↓
              AdminManageLeaderboard
```

**Profile Page:**
```
Profile → "🏆 Team Leaderboard" button (navbar or quick actions)
            ↓
         UserLeaderboard
```

**Back Navigation:**
- AdminManageLeaderboard → Back → AdminDashboard
- UserLeaderboard → Back → Profile

---

## Database Schema

### leaderboard_contests
```sql
CREATE TABLE leaderboard_contests (
    contest_id TEXT PRIMARY KEY,
    contest_name TEXT NOT NULL,
    description TEXT,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    max_problems INTEGER NOT NULL,
    is_active INTEGER DEFAULT 1,
    standings_finalized INTEGER DEFAULT 0,
    created_by_admin TEXT NOT NULL,
    created_at TEXT NOT NULL
);
```

### leaderboard_registrations
```sql
CREATE TABLE leaderboard_registrations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contest_id TEXT NOT NULL,
    username TEXT NOT NULL,
    registered_at TEXT NOT NULL,
    UNIQUE(contest_id, username),
    FOREIGN KEY (contest_id) REFERENCES leaderboard_contests(contest_id)
);
```

### leaderboard_entries
```sql
CREATE TABLE leaderboard_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contest_id TEXT NOT NULL,
    username TEXT NOT NULL,
    rank INTEGER NOT NULL,
    solve_count INTEGER NOT NULL,
    total_penalty INTEGER NOT NULL,
    total_time INTEGER NOT NULL,
    status TEXT DEFAULT 'Completed',
    UNIQUE(contest_id, username),
    FOREIGN KEY (contest_id) REFERENCES leaderboard_contests(contest_id)
);
```

### combined_leaderboard
```sql
CREATE TABLE combined_leaderboard (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    total_solves INTEGER NOT NULL,
    total_penalty INTEGER NOT NULL,
    overall_rank INTEGER NOT NULL,
    contests_participated INTEGER NOT NULL,
    status TEXT DEFAULT 'Active',
    last_updated TEXT
);
```

---

## CSS Styling

Added new CSS classes for leaderboard UI:

- `.section-box` - Container styling
- `.section-title` - Title styling
- `.button-success` - Green success buttons
- `.button-warning` - Orange warning buttons
- `.button-danger` - Red danger buttons
- `.button-primary` - Blue primary buttons
- `.navbar-button-success` - Navbar button variants
- `.tab-pane` - Tab styling
- `.leaderboard-tab-content` - Tab content background

---

## Error Handling

All controllers implement robust error handling:
- Dialog-based error messages
- Input validation
- Database operation verification
- User feedback via alerts

---

## Future Enhancements

Potential improvements for future versions:
1. **Team-based leaderboards** - Group users into teams
2. **Filters and sorting** - Advanced leaderboard filtering
3. **Export functionality** - Export standings to CSV/PDF
4. **Real-time updates** - Live leaderboard updates
5. **Achievement badges** - Recognition system
6. **Statistics** - Detailed performance analytics
7. **Leaderboard history** - Archive past leaderboards
8. **Custom scoring** - Configurable scoring algorithms

---

## Files Modified/Created

### New Files Created:
1. Model Classes:
   - `LeaderboardContest.java`
   - `LeaderboardEntry.java`
   - `CombinedLeaderboardEntry.java`

2. Data Layer:
   - `LeaderboardDatabase.java`

3. Controllers:
   - `AdminManageLeaderboardController.java`
   - `CreateLeaderboardContestController.java`
   - `UserLeaderboardController.java`

4. FXML Files:
   - `AdminManageLeaderboard.fxml`
   - `CreateLeaderboardContest.fxml`
   - `UserLeaderboard.fxml`

### Files Modified:
1. `DatabaseManager.java` - Added leaderboard tables + getConnection() method
2. `AdminDashboardController.java` - Added leaderboard navigation
3. `AdminDashboard.fxml` - Added leaderboard tab and button
4. `ProfileController.java` - Added leaderboard navigation
5. `Profile.fxml` - Added leaderboard buttons
6. `styles.css` - Added leaderboard styling

---

## Testing Checklist

- [ ] Create leaderboard contest as admin
- [ ] Register user for contest
- [ ] Add standings entries with solve counts
- [ ] Update existing standings
- [ ] Finalize contest standings
- [ ] Verify combined leaderboard updates
- [ ] Check overall rankings calculation
- [ ] Unregister from contest
- [ ] View contest standings as user
- [ ] Check pagination and scrolling
- [ ] Verify error messages
- [ ] Test back navigation
- [ ] Validate data persistence

---

## Conclusion

The KUET Team Formation Contest Leaderboard system is now fully integrated and ready for use. It provides a comprehensive platform for managing team contests, tracking participant performance, and maintaining both individual and aggregate rankings.

For questions or issues, refer to the controller implementations and database schema documentation above.
