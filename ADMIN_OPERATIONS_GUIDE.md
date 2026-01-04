# Admin Operations Guide

## Admin Login
**Credentials**: `admin` / `admin1234`

The admin can login from the same login page as regular users. The system automatically detects admin credentials and redirects to the Admin Dashboard.

## Admin Dashboard Features

### 1. Header Actions
- **📊 Manage Standings**: Opens the contest standings management interface
- **Logout**: Returns to login page

### 2. Manage Contests Tab 📅

#### Create New Contest
Fill in the form:
- **Contest ID**: Unique identifier (e.g., CONTEST_001)
- **Contest Name**: Display name (e.g., Weekly Contest #1)
- **Date & Time**: Select contest date
- **Duration**: Contest length in minutes (e.g., 120)
- **Max Participants**: Maximum allowed participants (default: 1000)

Click "Create Contest" to add it to the system.

#### View All Contests
The table shows:
- Contest ID
- Contest Name
- Date & Time
- Duration
- Status (Past/Future)
- Actions (Delete button)

#### Delete Contest
Click the red "Delete" button next to any contest to remove it (including all participants).

### 3. Manage Participants Tab 👥

#### View Participants
1. Select a contest from dropdown
2. Click "Load Participants"
3. View all registered participants

#### Add Participant
1. Select a contest
2. Enter username in the text field
3. Click "Add Participant"
- User must exist in the system
- Participant starts with 0 problems solved

#### Remove Participant
Click the "Remove" button next to any participant in the table.

### 4. Statistics Tab 📊

View system-wide statistics:
- **Total Contests**: All contests in database
- **Total Users**: All registered users
- **Future Contests**: Upcoming contests
- **Total Participants**: Sum across all contests

Click "Refresh Statistics" to update.

## Contest Standings Management

Access via "📊 Manage Standings" button in header.

### Interface Components

1. **Contest Selector**: Dropdown to choose which contest to manage
2. **Refresh Button**: Reload standings data
3. **Generate Contest Results**: Calculate rankings and ratings
4. **Standings Table**: Editable table with participant data

### Editing Solve Counts

The table columns are:
- **Rank**: Auto-calculated after generation
- **Username**: Participant identifier
- **Current Rating**: Their rating before contest
- **Solved**: Number of problems solved (EDITABLE)
- **Penalty**: Total penalty time in minutes (EDITABLE)
- **New Rating**: Predicted rating after contest
- **Change**: Rating change (+/-)
- **Actions**: Remove button

#### How to Edit:
1. **Double-click** on any cell in "Solved" or "Penalty" column
2. Type the new value
3. Press Enter
4. Changes are saved immediately to database

### Generating Contest Results

After editing solve counts for participants:

1. Click "Generate Contest Results" button
2. System performs:
   - Sorts participants by solved (descending), then penalty (ascending)
   - Assigns ranks (1st, 2nd, 3rd, etc.)
   - Calculates expected ranks based on ratings
   - Computes rating changes
   - Updates database

3. Table refreshes showing:
   - New rankings
   - Predicted ratings
   - Rating changes

### Auto-Population

When you first select a contest or click "Generate Contest Results":
- System automatically adds **user001 through user030** as participants
- All start with 0 problems solved and 0 penalty
- Real registered users are also added

## Workflow Example

### Complete Contest Management Flow:

1. **Login as Admin**
   ```
   Username: admin
   Password: admin1234
   ```

2. **Navigate to Standings**
   - Click "📊 Manage Standings" in header

3. **Select Contest**
   - Choose from dropdown (e.g., "CF925 - Codeforces Round 925")

4. **Verify Participants**
   - See user001-user030 automatically included
   - Any registered real users also appear

5. **Update Solve Counts**
   - Double-click "Solved" for user001 → Enter "5"
   - Double-click "Penalty" for user001 → Enter "120"
   - Double-click "Solved" for user002 → Enter "4"
   - Double-click "Penalty" for user002 → Enter "100"
   - Repeat for other participants

6. **Generate Results**
   - Click "Generate Contest Results"
   - Confirm dialog
   - View updated rankings

7. **Verify Results**
   - Rankings sorted correctly
   - Rating changes calculated
   - New ratings displayed

## Important Notes

### Database Persistence
- All changes are saved immediately to SQLite database
- Data persists across application restarts
- Located in `contest_predictor.db` file

### Participant Management
- Default users (user001-user030) are auto-added to ALL new contests
- Real users must register themselves or admin can add manually
- Removing participant doesn't delete the user account

### Rating Algorithm
The system uses an ELO-based algorithm:
```
Expected Rank = 1 + Σ(probability of losing to each opponent)
Rating Change = (Expected Rank - Actual Rank) × 20
New Rating = Current Rating + Rating Change
```
- Rating changes capped at ±150
- Higher current rating → higher expected rank → smaller gains from good performance

### Contest Status
- **Future Contests**: Registration open, can manage standings
- **Past Contests**: Historical data, standings are finalized
- Only future contests appear in standings management dropdown

## Advanced Operations

### Bulk Solve Count Updates
For efficiency when updating many participants:
1. Edit multiple cells without generating results
2. All edits save automatically
3. Generate results once at the end

### Handling Registration
- Users can self-register from their profile
- Admin can manually add users via "Manage Participants" tab
- Check "contest_registrations" table in database for full list

### Troubleshooting

**Problem**: Can't edit cells
- **Solution**: Make sure you're double-clicking (not single-click)
- Table must be in edit mode

**Problem**: Participants not showing
- **Solution**: Click "Refresh" button or re-select contest

**Problem**: Changes not saving
- **Solution**: Ensure database file has write permissions

**Problem**: Rating changes seem wrong
- **Solution**: Check that all solve counts are entered correctly
- Verify participant ratings are accurate

## Security Notes

- Admin password is stored in database (in production, should be hashed)
- Only admin can edit solve counts via UI
- Regular users have read-only access to standings
- Admin cannot compete in contests (separate from user accounts)

## Best Practices

1. **Before Contest**: Create contest, verify it appears in dropdown
2. **During Contest**: Users register themselves
3. **After Contest**: Admin updates solve counts, generates results
4. **Regular Maintenance**: Review statistics tab, clean up old contests

## Command Summary

| Action | Steps |
|--------|-------|
| Login | Enter `admin`/`admin1234` on login page |
| Create Contest | Dashboard → Manage Contests → Fill form → Create |
| Manage Standings | Dashboard → 📊 Manage Standings button |
| Edit Solve Count | Select contest → Double-click cell → Enter value |
| Generate Results | Update all scores → Click "Generate Contest Results" |
| Remove Participant | Standings view → Click Remove button |
| View Statistics | Dashboard → Statistics tab → Refresh |

---

**Remember**: Admin and user accounts are separate. Admin cannot participate in contests as a regular user. For testing both roles, use separate accounts.
