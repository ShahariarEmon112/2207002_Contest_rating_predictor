# 🎯 Admin Dashboard - Quick Visual Guide

## 🚀 How to Access Admin Dashboard

### Step 1: Start the Application
```bash
# Method 1: Using IntelliJ IDEA (Easiest)
1. Open project in IntelliJ
2. Find src/main/java/com/contestpredictor/Main.java
3. Right-click → Run 'Main.main()'

# Method 2: Using Maven
mvn clean javafx:run

# Method 3: Using Quick Test Script
run_admin_test.bat
```

### Step 2: Login as Admin
```
┌─────────────────────────────────────┐
│      Contest Rating Predictor       │
│                                     │
│  Username: [admin             ]     │
│  Password: [admin1234         ]     │
│                                     │
│         [ Sign In ]                 │
└─────────────────────────────────────┘
```

**Credentials:**
- Username: `admin`
- Password: `admin1234`

---

## 📊 Admin Dashboard Layout

```
╔═══════════════════════════════════════════════════════════════╗
║  🛡️ ADMIN DASHBOARD              📊 Manage    Admin    Logout ║
║  Contest Manager & Controller     Standings                   ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  [📅 Manage Contests] [👥 Manage Participants] [📊 Statistics]║
║  ═══════════════════════════════════════════════════════════  ║
║                                                               ║
║  CREATE NEW CONTEST                                           ║
║  ┌──────────────────────────────────────────────────────┐    ║
║  │ Contest ID:     [CONTEST_001              ]          │    ║
║  │ Contest Name:   [Weekly Contest #1        ]          │    ║
║  │ Date & Time:    [2026-01-01              ]          │    ║
║  │ Duration (min): [120                      ]          │    ║
║  │ Max Parts:      [1000                     ]          │    ║
║  │ [Create Contest] [Clear Form]                        │    ║
║  └──────────────────────────────────────────────────────┘    ║
║                                                               ║
║  ALL CONTESTS                                                 ║
║  ┌──────────────────────────────────────────────────────┐    ║
║  │ ID      │ Name    │ Date/Time    │ Duration │ Status │    ║
║  ├──────────────────────────────────────────────────────┤    ║
║  │ CON_001 │ Week #1 │ 2026-01-15   │ 120      │ Future │    ║
║  │ CON_002 │ Week #2 │ 2025-12-20   │ 90       │ Past   │    ║
║  └──────────────────────────────────────────────────────┘    ║
╚═══════════════════════════════════════════════════════════════╝
```

---

## 🎮 Tab 1: Manage Contests

### ✨ Features
1. **Create New Contest**
   - Enter unique Contest ID
   - Set contest name
   - Select date
   - Set duration in minutes
   - Set max participants

2. **View All Contests**
   - See past and future contests
   - Delete contests
   - Monitor contest status

### 📝 Example Usage
```
1. Fill in Contest Details:
   ├── Contest ID: "CONTEST_001"
   ├── Name: "Weekly Contest #1"
   ├── Date: Select from calendar
   ├── Duration: "120" minutes
   └── Max Participants: "1000"

2. Click "Create Contest"

3. ✅ Success! Contest appears in table below
```

---

## 👥 Tab 2: Manage Participants

```
╔═══════════════════════════════════════════════════════════════╗
║  Select Contest: [CONTEST_001 - Weekly Contest #1  ▼]        ║
║  [Load Participants]                                          ║
║                                                               ║
║  ADD PARTICIPANT TO CONTEST                                   ║
║  ┌──────────────────────────────────────────────────────┐    ║
║  │ Username: [user001        ] [Add Participant]        │    ║
║  └──────────────────────────────────────────────────────┘    ║
║                                                               ║
║  CONTEST PARTICIPANTS (15 participants)                       ║
║  ┌──────────────────────────────────────────────────────┐    ║
║  │ Username │ Rating │ Solved │ Penalty │ Rank │ Action │    ║
║  ├──────────────────────────────────────────────────────┤    ║
║  │ user001  │ 1500   │ 3      │ 120     │ 1    │ Remove │    ║
║  │ user002  │ 1450   │ 2      │ 150     │ 2    │ Remove │    ║
║  │ user003  │ 1400   │ 2      │ 180     │ 3    │ Remove │    ║
║  └──────────────────────────────────────────────────────┘    ║
╚═══════════════════════════════════════════════════════════════╝
```

### ✨ Features
1. **Select Contest**: Choose contest from dropdown
2. **Add Participants**: Add users by username
3. **View Participants**: See all registered participants
4. **Remove Participants**: Remove users from contest

### 📝 Example Usage
```
1. Select Contest: "CONTEST_001 - Weekly Contest #1"
2. Click "Load Participants"
3. Enter Username: "user001"
4. Click "Add Participant"
5. ✅ Participant added! Appears in table
```

---

## 📊 Tab 3: Statistics

```
╔═══════════════════════════════════════════════════════════════╗
║                    SYSTEM STATISTICS                          ║
║                                                               ║
║  ┌─────────────────┐  ┌─────────────────┐                   ║
║  │  Total Contests │  │   Total Users   │                   ║
║  │                 │  │                 │                   ║
║  │       25        │  │      150        │                   ║
║  └─────────────────┘  └─────────────────┘                   ║
║                                                               ║
║  ┌─────────────────┐  ┌─────────────────┐                   ║
║  │ Future Contests │  │Total Participants│                   ║
║  │                 │  │                 │                   ║
║  │       12        │  │      2,450      │                   ║
║  └─────────────────┘  └─────────────────┘                   ║
║                                                               ║
║             [Refresh Statistics]                              ║
╚═══════════════════════════════════════════════════════════════╝
```

### ✨ Features
1. **Total Contests**: Count of all contests
2. **Total Users**: Count of registered users
3. **Future Contests**: Upcoming contests
4. **Total Participants**: All contest registrations
5. **Refresh**: Update statistics in real-time

---

## 🔄 Common Workflows

### Workflow 1: Create a New Contest
```
Step 1: Login as admin (admin/admin1234)
   ↓
Step 2: Go to "Manage Contests" tab
   ↓
Step 3: Fill in contest details
   ↓
Step 4: Click "Create Contest"
   ↓
Step 5: ✅ Contest created!
```

### Workflow 2: Add Participants to Contest
```
Step 1: Go to "Manage Participants" tab
   ↓
Step 2: Select contest from dropdown
   ↓
Step 3: Click "Load Participants"
   ↓
Step 4: Enter username in field
   ↓
Step 5: Click "Add Participant"
   ↓
Step 6: ✅ Participant added!
```

### Workflow 3: Monitor System Statistics
```
Step 1: Go to "Statistics" tab
   ↓
Step 2: View current statistics
   ↓
Step 3: Click "Refresh Statistics" for updates
   ↓
Step 4: ✅ Statistics updated!
```

### Workflow 4: Manage Contest Standings
```
Step 1: Click "📊 Manage Standings" button (top-right)
   ↓
Step 2: View/edit contest standings
   ↓
Step 3: Return to dashboard
```

---

## ⚡ Quick Actions

### From Admin Dashboard
| Action | Location | Button |
|--------|----------|--------|
| Create Contest | Manage Contests tab | Create Contest |
| Add Participant | Manage Participants tab | Add Participant |
| View Statistics | Statistics tab | (auto-display) |
| Manage Standings | Top header | 📊 Manage Standings |
| Logout | Top-right corner | Logout |

---

## 🎨 Color Coding

The dashboard uses color-coded elements:

- 🟦 **Blue (Primary)**: Main actions, manage standings
- 🟩 **Green**: Success messages, total users
- 🟧 **Orange**: Admin theme, future contests
- 🟪 **Purple**: Total participants
- 🟥 **Red**: Delete/Remove actions
- ⬜ **White/Gray**: Forms and tables

---

## ✅ Verification Steps

After logging in, verify these items work:

### ✓ Basic Functionality
- [ ] Admin dashboard loads
- [ ] Three tabs are visible
- [ ] Admin name shows in header
- [ ] Logout button works

### ✓ Manage Contests
- [ ] Can create new contest
- [ ] Contest appears in table
- [ ] Can view contest details
- [ ] Can delete contest
- [ ] Status shows correctly (Past/Future)

### ✓ Manage Participants
- [ ] Can select contest from dropdown
- [ ] Can load participants
- [ ] Can add new participant
- [ ] Participant appears in table
- [ ] Can remove participant

### ✓ Statistics
- [ ] Statistics display (even if 0)
- [ ] Numbers are accurate
- [ ] Refresh button updates counts
- [ ] All 4 stat cards show

---

## 🆘 Troubleshooting

### Problem: Dashboard won't load
**Solution:**
```
1. Check console for errors
2. Verify FXML file exists
3. Ensure database is created
4. Try restarting application
```

### Problem: Can't login as admin
**Solution:**
```
1. Use correct credentials: admin/admin1234
2. Check if database file exists
3. Run AdminSetup to create new admin
4. Check console for authentication errors
```

### Problem: Statistics show 0
**Solution:**
```
1. This is normal for new installations
2. Create contests to see numbers update
3. Add participants to contests
4. Click "Refresh Statistics"
```

### Problem: Can't add participant
**Solution:**
```
1. Ensure user exists in system
2. Check if already added to contest
3. Verify contest is selected
4. Click "Load Participants" first
```

---

## 🎯 Success Checklist

When everything works, you should see:

✅ Login successful with admin credentials
✅ Dashboard opens with 3 tabs
✅ Admin name appears in header
✅ Can create contests
✅ Can add participants
✅ Statistics display correctly
✅ No errors in console
✅ All buttons respond
✅ Tables populate with data
✅ Can navigate between screens

---

## 📞 Next Steps

1. **Create Your First Contest**
   - Use the guide above
   - Test with sample data

2. **Add Participants**
   - Use existing users (user001-user030)
   - Or create new users

3. **Explore Features**
   - Try all tabs
   - Test all buttons
   - Check statistics

4. **Customize**
   - Create your own contests
   - Add your own participants
   - Monitor system growth

---

## 🎉 Ready to Use!

Your admin dashboard is fully functional and ready for:
- ✅ Contest management
- ✅ Participant management
- ✅ System monitoring
- ✅ Statistics tracking

**Login now with: `admin` / `admin1234`**

---

## 📚 Additional Resources

- [ADMIN_SYSTEM_COMPLETE.md](ADMIN_SYSTEM_COMPLETE.md) - Complete technical guide
- [ADMIN_OPERATIONS_GUIDE.md](ADMIN_OPERATIONS_GUIDE.md) - Operations manual
- [README.md](README.md) - Project overview

---

**Happy Managing! 🎊**
