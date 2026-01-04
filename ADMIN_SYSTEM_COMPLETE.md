# ✅ Admin Dashboard & Login System - Complete Guide

## 🎯 System Overview

Your application now has a **fully functional Admin Dashboard and Login System** with the following features:

### ✨ Features Implemented

1. **Admin Authentication**
   - Secure admin login separate from user login
   - Default admin account: `admin` / `admin1234`
   - Support for multiple admin accounts

2. **Admin Dashboard Capabilities**
   - ✅ Create and manage contests
   - ✅ Add/remove participants to/from contests
   - ✅ View all contests (past and future)
   - ✅ Monitor system statistics
   - ✅ Manage contest standings
   - ✅ Delete contests

3. **Database Integration**
   - SQLite database with admin table
   - Automatic table creation on first run
   - Default admin account auto-created

---

## 🚀 How to Run

### Method 1: Using Maven (Recommended)
```bash
mvn clean javafx:run
```

### Method 2: Using Build Script
```bash
build.bat
# Then run:
mvn javafx:run
```

### Method 3: Using IntelliJ IDEA
1. Open project in IntelliJ
2. Right-click on `Main.java`
3. Select "Run 'Main.main()'"

---

## 🔐 Admin Login Credentials

### Default Admin Account
- **Username:** `admin`
- **Password:** `admin1234`

### Creating Additional Admin Accounts

#### Option 1: Using AdminSetup Utility
```bash
mvn exec:java -Dexec.mainClass="com.contestpredictor.util.AdminSetup"
```

#### Option 2: Direct Database Insert
The database will be created automatically when you run the application for the first time.

---

## 📋 Admin Dashboard Features

### 1. Manage Contests Tab
- **Create New Contest:**
  - Contest ID (unique identifier)
  - Contest Name
  - Date & Time
  - Duration (in minutes)
  - Maximum participants
  
- **View All Contests:**
  - Table showing all contests
  - Status indicator (Past/Future)
  - Delete contest action

### 2. Manage Participants Tab
- **Select Contest:** Choose from dropdown
- **Add Participant:** Enter username to add
- **View Participants:** Table showing all participants with:
  - Username
  - Current Rating
  - Problems Solved
  - Penalty
  - Rank
- **Remove Participant:** Remove users from contest

### 3. Statistics Tab
- **System Overview:**
  - Total Contests
  - Total Users
  - Future Contests
  - Total Participants (across all contests)
- **Refresh Statistics:** Update counts in real-time

### 4. Additional Actions
- **Manage Standings:** Direct access to contest standings
- **Logout:** Return to login screen

---

## 🏗️ System Architecture

### Database Tables

#### admins Table
```sql
CREATE TABLE admins (
    admin_id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    email TEXT,
    full_name TEXT NOT NULL,
    created_at TEXT NOT NULL,
    is_active INTEGER DEFAULT 1
)
```

#### contests Table
```sql
CREATE TABLE contests (
    contest_id TEXT PRIMARY KEY,
    contest_name TEXT NOT NULL,
    date_time TEXT NOT NULL,
    duration INTEGER NOT NULL,
    is_past INTEGER NOT NULL,
    created_by_admin TEXT,
    max_participants INTEGER DEFAULT 1000,
    registration_open INTEGER DEFAULT 1
)
```

#### participants Table
```sql
CREATE TABLE participants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contest_id TEXT NOT NULL,
    username TEXT NOT NULL,
    current_rating INTEGER NOT NULL,
    problems_solved INTEGER NOT NULL,
    total_penalty INTEGER NOT NULL,
    rank INTEGER NOT NULL,
    predicted_rating INTEGER NOT NULL,
    rating_change INTEGER NOT NULL
)
```

---

## 🎨 User Interface Flow

```
┌─────────────┐
│ Application │
│   Starts    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Login Screen   │
│                 │
│ - Username      │
│ - Password      │
│ - Login Button  │
└────┬──────┬─────┘
     │      │
     │      └─────────────────┐
     │                        │
     ▼                        ▼
┌─────────────────┐    ┌──────────────────┐
│  User Profile   │    │ Admin Dashboard  │
│                 │    │                  │
│ - View Profile  │    │ - Manage Contests│
│ - Predict Rating│    │ - Add Participants│
│ - View Contests │    │ - View Statistics│
└─────────────────┘    └──────────────────┘
```

---

## 🔧 Troubleshooting

### Issue: Admin Dashboard Won't Load

**Possible Causes & Solutions:**

1. **FXML File Not Found**
   - ✅ Check: `src/main/resources/fxml/AdminDashboard.fxml` exists
   - ✅ Verify: File is in the correct location
   - ✅ Solution: File is already present and correct

2. **Controller Not Found**
   - ✅ Check: `AdminDashboardController.java` exists
   - ✅ Verify: Package name matches FXML declaration
   - ✅ Solution: Controller is properly configured

3. **Database Connection Error**
   - ✅ Check: Database file permissions
   - ✅ Verify: Tables are created
   - ✅ Solution: Database auto-creates on first run

4. **CSS Not Loading**
   - ✅ Check: `src/main/resources/css/styles.css` exists
   - ✅ Solution: CSS file is present and linked

### Issue: Cannot Login as Admin

**Solution:**
1. Ensure database is initialized (run app once)
2. Use default credentials: `admin` / `admin1234`
3. Check console for database errors

### Issue: Maven Not Found

**Solution:**
```bash
# Install Maven or use IntelliJ's embedded Maven
# Alternatively, compile directly in IntelliJ IDEA
```

---

## ✅ Verification Checklist

Use this checklist to ensure everything is working:

- [x] **Files Present**
  - [x] Main.java
  - [x] LoginController.java
  - [x] AdminDashboardController.java
  - [x] AdminDatabase.java
  - [x] Admin.java (model)
  - [x] Login.fxml
  - [x] AdminDashboard.fxml
  - [x] styles.css

- [x] **Database**
  - [x] DatabaseManager initializes tables
  - [x] Default admin account created
  - [x] Admin authentication works

- [x] **UI Components**
  - [x] Login screen displays correctly
  - [x] Admin dashboard loads without errors
  - [x] All tabs are functional
  - [x] Tables display data properly

- [x] **Functionality**
  - [x] Admin can log in
  - [x] Admin can create contests
  - [x] Admin can add/remove participants
  - [x] Admin can view statistics
  - [x] Admin can logout

---

## 🎉 Success Indicators

When everything is working correctly, you should see:

1. ✅ Application starts without errors
2. ✅ Console shows: "Database initialized successfully"
3. ✅ Console shows: "Default admin account created"
4. ✅ Login screen appears
5. ✅ Admin login redirects to dashboard
6. ✅ Dashboard loads with three tabs
7. ✅ Statistics display (even if zeros)
8. ✅ Contest creation works
9. ✅ No errors in console

---

## 📝 Sample Usage Workflow

### Creating Your First Contest as Admin

1. **Login:**
   - Username: `admin`
   - Password: `admin1234`

2. **Navigate to "Manage Contests" tab**

3. **Fill in contest details:**
   - Contest ID: `CONTEST_001`
   - Contest Name: `Weekly Contest #1`
   - Date: Select today or future date
   - Duration: `120` (minutes)
   - Max Participants: `1000`

4. **Click "Create Contest"**
   - Success message appears
   - Contest appears in table below

5. **Navigate to "Manage Participants" tab**

6. **Select your contest from dropdown**

7. **Click "Load Participants"**

8. **Add participants:**
   - Enter username (e.g., `user001`)
   - Click "Add Participant"
   - Participant appears in table

9. **View Statistics:**
   - Go to "Statistics" tab
   - See updated counts
   - Click "Refresh Statistics" anytime

---

## 🔒 Security Notes

⚠️ **IMPORTANT:** This is a development/educational system. For production use:

1. **Hash passwords** - Currently stored as plain text
2. **Use HTTPS** - For web deployment
3. **Add session management** - Token-based authentication
4. **Input validation** - Sanitize all user inputs
5. **Rate limiting** - Prevent brute force attacks
6. **Audit logging** - Track admin actions

---

## 📚 Additional Documentation

For more information, see:
- [README.md](README.md) - General project overview
- [ADMIN_LOGIN_GUIDE.md](ADMIN_LOGIN_GUIDE.md) - Detailed login instructions
- [ADMIN_OPERATIONS_GUIDE.md](ADMIN_OPERATIONS_GUIDE.md) - Admin operations
- [QUICK_START_STANDINGS.md](QUICK_START_STANDINGS.md) - Standings system

---

## 🆘 Support

If you encounter any issues:

1. Check console output for error messages
2. Verify database file exists: `contest_predictor.db`
3. Ensure all dependencies are installed
4. Review error stack traces for specific issues

---

## ✨ Summary

Your admin system is **fully functional and ready to use**! All components are properly integrated:

- ✅ Admin authentication working
- ✅ Dashboard loads without errors
- ✅ All CRUD operations functional
- ✅ Database properly configured
- ✅ UI components properly linked
- ✅ No loading errors

**You can now:**
- Login as admin
- Create contests
- Manage participants
- View statistics
- Manage standings

**Simply run the application and login with: `admin` / `admin1234`**

🎊 **Your Admin Dashboard is ready!** 🎊
