# Admin Login - Quick Guide

## 🔐 How to Login as Admin

### Default Admin Credentials:
```
Username: admin
Password: admin123
```

### Login Steps:
1. Open the application
2. On the login screen, enter:
   - Username: `admin`
   - Password: `admin123`
3. Click the **"Admin Login"** button (orange button)
4. Admin Dashboard will open

## 🎯 Admin Dashboard Features

### Tab 1: 📅 Manage Contests
- **Create Contests**: Fill form and click "Create Contest"
- **View All Contests**: See list in table below
- **Delete Contests**: Click "Delete" button in Actions column

### Tab 2: 👥 Manage Participants  
- **Select Contest**: Choose from dropdown
- **Load Participants**: Click "Load Participants" button
- **Add Participant**: Enter username → Click "Add Participant"
- **Remove Participant**: Click "Remove" button in participant row

### Tab 3: 📊 Statistics
- View system statistics
- Click "Refresh Statistics" to update

## ✨ Key Differences from User Login

| Feature | User | Admin |
|---------|------|-------|
| **Login Button** | "Sign In" (blue) | "Admin Login" (orange) |
| **Email Required** | Yes | No |
| **Dashboard** | Profile with contests | Contest management |
| **Has Rating** | Yes | No |
| **Can Predict** | Yes | No |
| **Manages Contests** | No | Yes |
| **Manages Participants** | No | Yes |

## 🚀 What Admin Can Do

✅ Create new contests (past or future)  
✅ Delete contests  
✅ Add participants to contests  
✅ Remove participants from contests  
✅ View system statistics  
✅ Monitor all contests and users  

## ❌ What Admin Cannot Do

❌ Participate in contests  
❌ Have a personal rating  
❌ Predict ratings  
❌ Register as a regular user  

## 🆕 Creating Additional Admin Accounts

Run the `AdminSetup` utility:
1. Navigate to project directory
2. Run: `java com.contestpredictor.util.AdminSetup`
3. Follow prompts (no email required)

---

**The admin system is now simplified and works just like user login - no email needed!**
