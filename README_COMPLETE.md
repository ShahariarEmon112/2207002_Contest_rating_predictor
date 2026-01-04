# 🎯 Contest Rating Predictor - Complete System

## 📋 Overview

A comprehensive JavaFX application for managing competitive programming contest standings, ratings, and predictions. Features include admin controls, automatic participant generation, real-time solve count updates, and ELO-based rating calculations.

## ✨ New Features

### 🔐 Admin System
- **Admin Login**: Use `admin`/`admin1234` to access admin dashboard
- **Manage Standings**: Update solve counts, generate rankings, and calculate ratings
- **Full CRUD**: Create/delete contests, add/remove participants
- **Statistics Dashboard**: Real-time system metrics

### 📊 Contest Standings
- **Auto-Population**: Each contest includes user001-user030 by default
- **User Registration**: Real users can register and appear in standings
- **Editable Scores**: Double-click to edit solve counts and penalties
- **Rating Calculation**: ELO-based algorithm with ±150 cap
- **Database Persistence**: All changes saved to SQLite

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- JavaFX SDK
- Maven (optional, for command-line build)
- IntelliJ IDEA (recommended)

### Running the Application

#### Option 1: IntelliJ IDEA (Recommended)
1. Open project in IntelliJ IDEA
2. Right-click `src/main/java/com/contestpredictor/Main.java`
3. Select "Run 'Main.main()'"

#### Option 2: Maven
```bash
mvn clean javafx:run
```

## 🔑 Login Credentials

### Admin Access
- **Username**: `admin`
- **Password**: `admin1234`

### Test Users (30 available)
- **Usernames**: `user001` through `user030`
- **Passwords**: `pass001` through `pass030`
- **Example**: Login as `user015` with password `pass015`

## 📖 User Guide

### For Regular Users

1. **Login/Register**
   - Use existing test account (user001-user030)
   - Or create new account via "Register" button

2. **View Profile**
   - See your current rating and statistics
   - View rating color (Gray/Green/Cyan/Blue/Yellow/Orange/Red)
   - Track contests participated

3. **View Standings**
   - Click "📊 View Standings" from profile
   - Select any contest from dropdown
   - See all participants and their scores

4. **Register for Contest**
   - In standings view, click "Register Me"
   - Your username added to participant list
   - Starts with 0 problems solved

5. **Predict Rating**
   - Click "🎯 Predict Rating" button
   - Enter number of problems to solve
   - See predicted rating change

### For Admins

1. **Access Admin Dashboard**
   - Login with `admin`/`admin1234`
   - Automatically redirected to dashboard

2. **Manage Contests**
   - Tab: "📅 Manage Contests"
   - Create new contests with ID, name, date, duration
   - View all contests in table
   - Delete contests with red button

3. **Manage Participants**
   - Tab: "👥 Manage Participants"
   - Select contest from dropdown
   - Add users manually
   - Remove participants from contest

4. **Update Contest Standings**
   - Click "📊 Manage Standings" in header
   - Select contest to manage
   - Double-click "Solved" or "Penalty" cells to edit
   - Changes save automatically

5. **Generate Results**
   - After updating solve counts
   - Click "Generate Contest Results"
   - System calculates:
     - Rankings (by solved, then penalty)
     - Expected ranks based on ratings
     - Rating changes (performance-based)
     - New predicted ratings

## 🎮 Complete Workflow Example

### Scenario: Admin Managing a Contest

```
1. Login as admin (admin/admin1234)
2. Click "📊 Manage Standings" button
3. Select "CF925 - Codeforces Round 925 (Div. 2)"
4. Notice user001-user030 auto-populated with 0 solves
5. Edit scores:
   - Double-click "Solved" for user001 → Enter 5
   - Double-click "Penalty" for user001 → Enter 120
   - Double-click "Solved" for user002 → Enter 4
   - Double-click "Penalty" for user002 → Enter 100
   - Double-click "Solved" for user003 → Enter 5
   - Double-click "Penalty" for user003 → Enter 150
6. Click "Generate Contest Results"
7. View updated standings:
   - Rank 1: user001 (5 solved, 120 penalty) +25 rating
   - Rank 2: user003 (5 solved, 150 penalty) +18 rating
   - Rank 3: user002 (4 solved, 100 penalty) +12 rating
8. All changes saved to database!
```

### Scenario: User Joining Contest

```
1. Create new account: username "john_doe", password "password123"
2. Login with new credentials
3. Click "📊 View Standings" from profile
4. Select "CF926 - Codeforces Round 926"
5. Click "Register Me" button
6. See "john_doe" added to participants list!
7. Admin can now update your solve count
8. After admin generates results, refresh to see ranking
```

## 📂 Project Structure

```
2207002_Contest_rating_predictor/
├── src/
│   ├── main/
│   │   ├── java/com/contestpredictor/
│   │   │   ├── Main.java                          # Application entry point
│   │   │   ├── controller/
│   │   │   │   ├── AdminDashboardController.java  # Admin UI logic
│   │   │   │   ├── ContestStandingsController.java # NEW: Standings management
│   │   │   │   ├── LoginController.java           # Login + admin auth
│   │   │   │   ├── ProfileController.java         # User profile
│   │   │   │   └── ...
│   │   │   ├── model/
│   │   │   │   ├── Contest.java                   # Contest entity
│   │   │   │   ├── Participant.java               # Participant entity
│   │   │   │   ├── User.java                      # User entity
│   │   │   │   └── Admin.java                     # Admin entity
│   │   │   ├── data/
│   │   │   │   ├── DatabaseManager.java           # SQLite operations
│   │   │   │   ├── ContestDatabase.java           # Contest management
│   │   │   │   ├── UserDatabase.java              # User management
│   │   │   │   └── AdminDatabase.java             # Admin auth
│   │   │   └── util/
│   │   │       └── RatingPredictor.java           # Rating calculations
│   │   └── resources/
│   │       ├── fxml/
│   │       │   ├── ContestStandings.fxml          # NEW: Standings UI
│   │       │   ├── AdminDashboard.fxml
│   │       │   ├── Login.fxml
│   │       │   └── ...
│   │       └── css/
│   │           └── styles.css
├── contest_predictor.db                            # SQLite database
├── pom.xml                                         # Maven configuration
├── QUICK_START_STANDINGS.md                        # Quick start guide
├── ADMIN_OPERATIONS_GUIDE.md                       # Admin guide
└── CONTEST_STANDINGS_IMPLEMENTATION.md             # Technical docs
```

## 🗄️ Database Schema

### Tables

1. **users**
   - username (PK)
   - password
   - full_name
   - current_rating
   - contests_participated
   - rating_history

2. **admins**
   - admin_id (PK)
   - username (UNIQUE)
   - password
   - email
   - full_name
   - created_at
   - is_active

3. **contests**
   - contest_id (PK)
   - contest_name
   - date_time
   - duration
   - is_past
   - created_by_admin
   - max_participants
   - registration_open

4. **contest_registrations** *(NEW)*
   - id (PK)
   - contest_id (FK)
   - username
   - registered_at
   - UNIQUE(contest_id, username)

5. **participants** *(ENHANCED)*
   - id (PK)
   - contest_id (FK)
   - username
   - current_rating
   - problems_solved
   - total_penalty
   - rank
   - predicted_rating
   - rating_change

## 🧮 Rating Algorithm

### ELO-Based System

```java
// For each participant:
1. Calculate expected rank based on ratings of all participants
   Expected Rank = 1 + Σ(probability of finishing behind each opponent)
   
2. Compare with actual rank
   Performance = Expected Rank - Actual Rank
   
3. Calculate rating change
   Rating Change = Performance × 20
   Rating Change = max(-150, min(150, Rating Change))  // Cap at ±150
   
4. Update rating
   New Rating = Current Rating + Rating Change
```

### Ranking Rules
1. **Primary**: Problems Solved (higher is better)
2. **Secondary**: Total Penalty Time (lower is better)

## 🛠️ Features List

### ✅ Implemented

- [x] User authentication and registration
- [x] Admin authentication (admin/admin1234)
- [x] Admin dashboard with statistics
- [x] Contest creation and management
- [x] Participant management
- [x] **Contest standings view**
- [x] **Auto-population with user001-user030**
- [x] **User registration for contests**
- [x] **Editable solve counts (admin)**
- [x] **Ranking generation**
- [x] **Rating calculation**
- [x] **Database persistence (SQLite)**
- [x] User profile with statistics
- [x] Rating prediction tool
- [x] Contest search and filtering
- [x] Multiple contest types (CF, AtCoder, LeetCode, etc.)

### 🎨 UI Features

- Modern, responsive JavaFX interface
- Color-coded ratings (Gray/Brown/Green/Cyan/Blue/Yellow/Orange/Red)
- Tabbed admin dashboard
- Editable table cells
- Real-time statistics
- Confirmation dialogs
- Error handling and validation

## 📚 Documentation

- [QUICK_START_STANDINGS.md](QUICK_START_STANDINGS.md) - Quick start guide
- [ADMIN_OPERATIONS_GUIDE.md](ADMIN_OPERATIONS_GUIDE.md) - Admin user manual
- [CONTEST_STANDINGS_IMPLEMENTATION.md](CONTEST_STANDINGS_IMPLEMENTATION.md) - Technical details
- [ADMIN_LOGIN_GUIDE.md](ADMIN_LOGIN_GUIDE.md) - Admin login instructions
- [NEW_FEATURES.md](NEW_FEATURES.md) - Feature changelog

## 🐛 Troubleshooting

### Common Issues

**Problem**: Application won't start
- **Solution**: Ensure Java 17+ and JavaFX are installed
- Check IDE project configuration

**Problem**: Can't login as admin
- **Solution**: Verify credentials: `admin` (lowercase) / `admin1234`
- Check console for error messages

**Problem**: Solve counts not saving
- **Solution**: Double-click cells (not single-click)
- Ensure you're logged in as admin
- Check database file permissions

**Problem**: Participants not showing
- **Solution**: Click "Refresh" button
- Try re-selecting the contest from dropdown

**Problem**: Database errors
- **Solution**: Delete `contest_predictor.db` file and restart
- This will reset all data

### Getting Help

1. Check documentation files
2. Review console output for error messages
3. Verify Java and JavaFX versions
4. Check database file exists and has write permissions

## 🔮 Future Enhancements

- Import contest results from CSV
- Export standings to PDF/Excel
- Rating history graphs
- Email notifications for contest reminders
- Multi-language support
- Dark mode theme
- Contest analytics and insights
- Leaderboards and achievements

## 👥 Default Test Users

The system includes 30 pre-created users for testing:

| Username | Password | Rating | Full Name |
|----------|----------|--------|-----------|
| user001 | pass001 | 1200 | Alex Johnson |
| user002 | pass002 | 1450 | Sarah Williams |
| user003 | pass003 | 980 | Michael Chen |
| ... | ... | ... | ... |
| user030 | pass030 | 1950 | Samantha King |

All users have realistic ratings between 750-2050 and varying contest participation history.

## 📜 License

This project is for educational purposes.

## 🙏 Acknowledgments

- JavaFX community for UI framework
- SQLite for database engine
- Competitive programming platforms for inspiration (Codeforces, AtCoder, LeetCode)

---

## 🎯 Quick Commands

| Task | Command |
|------|---------|
| Run (IntelliJ) | Right-click Main.java → Run |
| Run (Maven) | `mvn clean javafx:run` |
| Login Admin | `admin` / `admin1234` |
| Login Test User | `user001` / `pass001` |
| Reset Database | Delete `contest_predictor.db` |

---

**Version**: 2.0 with Contest Standings System  
**Last Updated**: December 31, 2025

**Ready to use!** Login with admin credentials to start managing contest standings.
