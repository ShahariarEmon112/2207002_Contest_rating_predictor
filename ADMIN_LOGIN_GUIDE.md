# Admin Login Guide

## Default Admin Account

A default admin account is automatically created when you first run the application:

- **Username:** `admin`
- **Password:** `admin123`

## How to Login as Admin

1. Launch the application
2. On the login screen, enter the admin credentials
3. Click the **"Admin Login"** button (orange button)
4. You will be redirected to the Admin Dashboard

## Admin Dashboard Features

Once logged in as admin, you can:

### 1. Manage Contests
- Create new contests
- View all contests
- Edit contest details
- Delete contests

### 2. Manage Registrations
- View contest registrations
- See which users registered for each contest
- Monitor participant counts

### 3. View Statistics
- Total number of contests
- Total number of users
- Active contests
- Total registrations

## Creating Additional Admin Accounts

### Option 1: Using AdminSetup Utility

Run the `AdminSetup` utility class to create new admin accounts interactively:

```bash
# From your project root
mvn compile
mvn exec:java -Dexec.mainClass="com.contestpredictor.util.AdminSetup"
```

### Option 2: Programmatically

You can also create admin accounts programmatically by using the `AdminDatabase` class:

```java
import com.contestpredictor.data.AdminDatabase;

AdminDatabase adminDB = AdminDatabase.getInstance();
adminDB.registerAdmin("newadmin", "password123", "John Doe", "john@example.com");
```

## Security Notes

⚠️ **Important:** The default password `admin123` is meant for testing purposes only. In a production environment:

1. Change the default admin password immediately
2. Implement password hashing (currently passwords are stored in plain text)
3. Add password strength requirements
4. Implement two-factor authentication
5. Add admin account lockout after failed login attempts

## Troubleshooting

### Admin Login Not Working

1. **Check Database:** Make sure the SQLite database file `contest_predictor.db` exists in your project root
2. **Verify Default Admin:** The default admin is created automatically on first run
3. **Check Console:** Look for database initialization messages in the console output
4. **Reset Database:** Delete `contest_predictor.db` and restart the application to recreate tables

### Admin Dashboard Errors

If you encounter errors when accessing the admin dashboard:

1. Ensure all FXML files are in the correct location (`src/main/resources/fxml/`)
2. Check that CSS files are loaded properly
3. Verify that the Admin object is being passed correctly to the controller
4. Look for exceptions in the console output

## Database Location

The SQLite database file is created at:
```
<project-root>/contest_predictor.db
```

You can view and edit this database using any SQLite browser/client.

## Admin Table Structure

```sql
CREATE TABLE admins (
    admin_id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    email TEXT,
    full_name TEXT NOT NULL,
    created_at TEXT NOT NULL,
    is_active INTEGER DEFAULT 1
);
```

## Support

If you encounter any issues with admin login or dashboard functionality, please check:
1. Console output for error messages
2. Database file exists and is readable
3. All dependencies are properly installed (`mvn clean install`)
4. JavaFX runtime is correctly configured
