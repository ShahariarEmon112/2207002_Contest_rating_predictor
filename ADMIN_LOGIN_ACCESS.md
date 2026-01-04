# Admin Login Access Guide

## 🔐 Admin Login Interface Created

### How to Access Admin Login

1. **From Main Application Launch:**
   - Run the application normally
   - You'll see the regular user login screen
   - Look for the **"Admin Login →"** link at the bottom (in orange/yellow color)
   - Click on it to access the dedicated Admin Login interface

2. **Admin Login Screen Features:**
   - 🔐 Dedicated admin portal with red/dark theme
   - Security notice and warning banner
   - Specialized admin credentials fields
   - "Back to User Login" option to return to regular login

### Admin Credentials

The system already has admin accounts set up. Use these credentials:

- **Username:** `admin`
- **Password:** `admin123`

### Visual Differences

**User Login:**
- Purple/Blue gradient theme
- Icon: 📊
- Title: "Contest Rating Predictor"
- Friendly, welcoming design

**Admin Login:**
- Red/Dark gradient theme  
- Icon: 🔐
- Title: "Admin Portal"
- Professional, secure design with warning indicators

### Navigation Flow

```
Main Login Screen
    ↓
Click "Admin Login →"
    ↓
Admin Login Interface
    ↓
Enter Admin Credentials
    ↓
Admin Dashboard
```

### Files Created/Modified

1. **New Files:**
   - `src/main/resources/fxml/AdminLogin.fxml` - Admin login interface
   - `src/main/java/com/contestpredictor/controller/AdminLoginController.java` - Admin login controller

2. **Modified Files:**
   - `src/main/resources/fxml/Login.fxml` - Added admin login link
   - `src/main/java/com/contestpredictor/controller/LoginController.java` - Added navigation to admin login
   - `src/main/resources/css/styles.css` - Added admin-specific styling

### Security Features

✓ Separate admin login interface  
✓ Distinct visual theme to prevent confusion  
✓ Password clearing on failed login attempts  
✓ Security notices displayed  
✓ Direct validation against AdminDatabase

### Testing the Admin Login

1. Build the project:
   ```bash
   mvn clean install
   ```

2. Run the application:
   ```bash
   mvn javafx:run
   ```

3. On the login screen, click "Admin Login →"
4. Enter admin credentials (admin/admin123)
5. You should be redirected to the Admin Dashboard

---

**Note:** The admin login is completely separate from user login but shares the same database infrastructure through the AdminDatabase class.
