# Quick Start Guide - Contest Standings System

## 🚀 Quick Setup

### Option 1: Using IntelliJ IDEA (Recommended)
1. Open the project in IntelliJ IDEA
2. Wait for dependencies to download
3. Right-click on `Main.java` → Run 'Main.main()'

### Option 2: Using Maven Command Line
```bash
mvn clean javafx:run
```

## 🔐 Login Credentials

### Admin Access:
- **Username**: `admin`
- **Password**: `admin1234`

### Test Users (30 available):
- **Username**: `user001` to `user030`
- **Password**: `pass001` to `pass030`
- Example: Login as `user015` with password `pass015`

## 📊 Features Overview

### 1. Admin Features
After logging in as admin:

1. **View Dashboard**
   - See statistics: Total contests, users, participants
   - Manage contests and participants

2. **Manage Contest Standings**
   - Click "📊 Manage Standings" button in header
   - Select a contest from dropdown
   - Double-click on "Solved" or "Penalty" cells to edit
   - Click "Generate Contest Results" to calculate rankings

3. **Add/Remove Participants**
   - Navigate to "👥 Manage Participants" tab
   - Select contest and add users manually

### 2. User Features
After logging in as regular user:

1. **View Profile**
   - See your rating, contests participated, and max rating
   
2. **View Contest Standings**
   - Click "📊 View Standings" button
   - Select any contest to view participants
   - See current rankings and scores

3. **Register for Contests**
   - In standings view, select a contest
   - Click "Register Me" button
   - You'll be added to the participant list

## 🎯 Typical Workflow

### Admin Creating and Managing Contest:

1. **Login as admin** (`admin` / `admin1234`)
2. Go to "📅 Manage Contests" tab
3. Create a new contest (optional - future contests already exist)
4. Click "📊 Manage Standings" button
5. Select your contest
6. The system automatically adds user001-user030
7. Edit solve counts:
   - Double-click on "Solved" cell for any participant
   - Enter number (e.g., 3 for 3 problems solved)
   - Double-click on "Penalty" cell
   - Enter penalty time in minutes
8. Click "Generate Contest Results"
9. View updated rankings and rating changes!

### User Joining and Viewing Contest:

1. **Register a new user** or login as existing user
2. Click "📊 View Standings" from profile
3. Select a contest (e.g., "CF925 - Codeforces Round 925")
4. Click "Register Me" button
5. Your username now appears in the standings!
6. After admin generates results, refresh to see your ranking

## 🔄 Contest Results Generation

When admin clicks "Generate Contest Results":

1. **Ranking Calculation**:
   - Primary: Problems solved (higher is better)
   - Secondary: Penalty time (lower is better)

2. **Rating Calculation**:
   - Expected rank calculated based on all ratings
   - Rating change = (expected - actual) × 20
   - Capped between -150 and +150
   - New rating = current rating + change

3. **Database Update**:
   - All changes saved to SQLite database
   - Persists across sessions

## 📁 Database Location

All data is stored in: `contest_predictor.db`
- Located in project root directory
- Contains users, contests, participants, and registrations
- Can be deleted to reset all data

## 🐛 Troubleshooting

### "Contest list is empty"
- Database is initializing on first run
- Wait a moment and refresh

### "Cannot edit solve count"
- Make sure you're logged in as **admin**
- Double-click the cell (not single click)
- Table must be in edit mode

### "User not registered"
- Click "Register Me" button first
- Refresh the standings view

### "Admin login not working"
- Verify username is exactly `admin` (lowercase)
- Verify password is exactly `admin1234`
- Try logging out and in again

## 💡 Tips

1. **Default Participants**: Every contest automatically includes user001-user030
2. **Edit Multiple Cells**: You can edit multiple participants before generating results
3. **Real-time Updates**: All admin changes are saved immediately to database
4. **Test Accounts**: Use user001-user030 for testing without creating new accounts
5. **Rating Colors**: Ratings follow standard competitive programming colors:
   - Gray (< 400)
   - Brown (400-800)
   - Green (800-1200)
   - Cyan (1200-1600)
   - Blue (1600-2000)
   - Yellow (2000-2400)
   - Orange (2400-2800)
   - Red (2800+)

## 📚 Available Contests

The system includes multiple future contests ready for use:
- Codeforces Rounds (CF925, CF926, CF927, etc.)
- AtCoder Contests (ABC340, ABC341, ARC171, etc.)
- Educational Codeforces Rounds
- LeetCode Weekly Contests

Select any to start managing!

## 🎉 Example Usage

**Scenario**: Admin wants to simulate a contest

1. Login as `admin`/`admin1234`
2. Click "📊 Manage Standings"
3. Select "CF925 - Codeforces Round 925 (Div. 2)"
4. See user001-user030 with 0 solves
5. Edit some scores:
   - user001: 5 solved, 120 penalty
   - user002: 4 solved, 100 penalty
   - user003: 5 solved, 150 penalty
6. Click "Generate Contest Results"
7. View rankings:
   - user001 (rank 1 - 5 solved, 120 penalty)
   - user003 (rank 2 - 5 solved, 150 penalty)
   - user002 (rank 3 - 4 solved, 100 penalty)
8. See rating changes calculated!

**Scenario**: User wants to join contest

1. Login as `user001`/`pass001`
2. Click "📊 View Standings"
3. Select a contest
4. See yourself already in standings (user001 is auto-included)
5. OR create new user account:
   - Register as "john_doe"
   - Login
   - Click "📊 View Standings"
   - Click "Register Me"
   - Now "john_doe" appears in standings!

---

## Need Help?

Check `CONTEST_STANDINGS_IMPLEMENTATION.md` for detailed technical documentation.
