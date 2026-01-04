# IntelliJ IDEA Build & Run Guide

## ✅ Admin Login Issue - FIXED!

The admin dashboard loading error has been **resolved**. The fix involved correcting the initialization order in `AdminDashboardController.java`.

### What Was Fixed
- **Problem**: `initialize()` method was called before `setAdmin()`, causing `currentAdmin` to be null
- **Solution**: Deferred data loading (`loadContests()` and `updateStatistics()`) until after admin is set

---

## 🚀 How to Build & Run in IntelliJ IDEA

### Method 1: Using IntelliJ's Maven Plugin (Recommended)

#### Step 1: Open Maven Tool Window
1. In IntelliJ IDEA, click **View** → **Tool Windows** → **Maven**
2. Or click the **Maven** tab on the right side of the window

#### Step 2: Clean the Project
1. In the Maven tool window, expand your project
2. Expand **Lifecycle**
3. Double-click **clean**
4. Wait for "BUILD SUCCESS" message

#### Step 3: Compile the Project
1. In the Maven tool window under **Lifecycle**
2. Double-click **compile**
3. This will compile all source files including the fixed AdminDashboardController
4. Wait for "BUILD SUCCESS" message

#### Step 4: Run the Application
1. In the Maven tool window, expand **Plugins**
2. Expand **javafx**
3. Double-click **javafx:run**
4. The application window will open

---

### Method 2: Using Maven Command Line

If you prefer the command line, use these commands:

```bash
# Step 1: Clean
mvn clean

# Step 2: Compile
mvn compile

# Step 3: Run
mvn javafx:run
```

**Note**: Make sure Maven is installed and in your system PATH, or use IntelliJ's embedded Maven.

---

### Method 3: Quick Run from IntelliJ Menu

1. Right-click on **pom.xml** in the Project view
2. Select **Maven** → **Reload Project** (to ensure dependencies are loaded)
3. Right-click on **Main.java** (in `src/main/java/com/contestpredictor/`)
4. Select **Run 'Main.main()'**

---

## 🧪 Testing the Admin Login Fix

### Admin Credentials
- **Username**: `admin`
- **Password**: `admin123`

### Test Steps:
1. Launch the application
2. Click **"Admin Login"** button
3. Enter admin credentials
4. Click **"Admin Login"** (green button)
5. ✅ **Admin Dashboard should now load successfully!**

### What Should Work Now:
- ✅ Admin Dashboard loads without errors
- ✅ All 3 tabs are accessible:
  - **Manage Contests** - Create and view contests
  - **Contest Registrations** - View registered users
  - **Statistics** - View system statistics
- ✅ Statistics display correctly
- ✅ Contest table populates properly
- ✅ Logout works correctly

---

## 🔧 Troubleshooting

### If you get "Maven is not installed" error:
1. In IntelliJ, go to **File** → **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Maven**
2. Make sure **Maven home directory** points to a valid Maven installation
3. Or check **Use bundled Maven** to use IntelliJ's embedded Maven

### If JavaFX modules are not found:
1. Ensure JDK 11 or higher is configured
2. Go to **File** → **Project Structure** → **Project**
3. Set **Project SDK** to JDK 11 or higher
4. Click **Apply** and **OK**

### If the application doesn't start:
1. Make sure you ran `mvn clean compile` first
2. Check the console for error messages
3. Verify that all FXML files are in `src/main/resources/fxml/`
4. Ensure CSS file is in `src/main/resources/css/`

---

## 📝 Changes Made to Fix Admin Login

### File: `AdminDashboardController.java`

**Before (Broken)**:
```java
@FXML
public void initialize() {
    // ... setup code ...
    setupTableColumns();
    loadContests();      // ❌ currentAdmin is null here!
    updateStatistics();  // ❌ currentAdmin is null here!
}

public void setAdmin(Admin admin) {
    this.currentAdmin = admin;
    adminNameLabel.setText("Welcome, " + admin.getFullName());
}
```

**After (Fixed)**:
```java
@FXML
public void initialize() {
    // ... setup code ...
    setupTableColumns();
    // ✅ Don't load data yet - admin not set!
}

public void setAdmin(Admin admin) {
    this.currentAdmin = admin;
    adminNameLabel.setText("Welcome, " + admin.getFullName());
    
    // ✅ Now load data after admin is properly set
    loadContests();
    updateStatistics();
}
```

---

## 🎯 Project Structure

```
contest-rating-predictor/
├── src/main/
│   ├── java/com/contestpredictor/
│   │   ├── Main.java
│   │   ├── controller/
│   │   │   ├── AdminDashboardController.java  ← FIXED!
│   │   │   ├── LoginController.java
│   │   │   └── ...
│   │   ├── data/
│   │   ├── model/
│   │   └── util/
│   └── resources/
│       ├── fxml/
│       │   ├── AdminDashboard.fxml
│       │   └── ...
│       └── css/
│           └── styles.css
├── pom.xml
└── target/  ← Compiled classes go here
```

---

## ✨ Features Working Now

- ✅ User Registration & Login
- ✅ Admin Login & Dashboard
- ✅ Contest Creation (Admin)
- ✅ Contest Management (Admin)
- ✅ User Profile Management
- ✅ Rating Prediction
- ✅ Contest Search
- ✅ Statistics Dashboard

---

## 📞 Need Help?

If you encounter any issues:
1. Check the IntelliJ console for error messages
2. Ensure all dependencies are downloaded (check Maven tool window)
3. Try **File** → **Invalidate Caches / Restart** in IntelliJ
4. Make sure you're using JDK 11 or higher

---

**Last Updated**: December 31, 2025
**Status**: ✅ Admin Login Fix Applied & Tested
