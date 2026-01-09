# Quick Testing Guide - Contest Rating Predictor

## Test Accounts

### Admin Account
- **Username**: admin
- **Password**: admin1234
- **Role**: Administrator with full control

### Regular User Accounts
- **Username**: user001 - user030
- **Password**: password123 (example)
- **Role**: Regular users with view-only access

---

## Test Scenario 1: Admin Creates Contest
**Steps:**
1. Login as admin (admin/admin1234)
2. Go to Admin Dashboard
3. Fill in contest details:
   - Contest ID: TEST_CONTEST_001
   - Contest Name: Test Contest - January 2026
   - Select date: January 25, 2026
   - Duration: 120 minutes
   - Max Participants: 100
4. Click "Create Contest"
5. Verify success message appears

**Expected Result:**
- Contest appears in the contests list
- 30 default users are auto-registered
- Contest is saved to database

---

## Test Scenario 2: User Sees Admin Contest
**Steps:**
1. Login as user001 (user001/password123)
2. Go to "Search Contests"
3. Look for "TEST_CONTEST_001 - Test Contest - January 2026"
4. It should appear in the list with "[Upcoming]" status

**Expected Result:**
- User can see the newly created admin contest
- User can register for the contest
- Contest appears in all contest lists

---

## Test Scenario 3: User Cannot Edit Solve Counts
**Steps:**
1. Stay logged in as user001
2. Navigate to "Rating Predictor"
3. Fetch contestants (use default limit: 100)
4. Try to edit "Problems Solved" column
5. Try to edit "Penalty" column

**Expected Result:**
- Cells are NOT editable (table is read-only)
- No edit cursor appears
- If forced to edit, "Access Denied" message appears
- Status bar shows "User mode: Problem count and penalty are read-only"

---

## Test Scenario 4: Admin Can Edit Solve Counts
**Steps:**
1. Logout and login as admin (admin/admin1234)
2. Navigate to "Rating Predictor"
3. Fetch contestants (use default limit: 100)
4. Try to double-click on "Problems Solved" cell
5. Edit a value and press Enter
6. Try to edit "Penalty" column similarly

**Expected Result:**
- Cells are editable (table is enabled for editing)
- Values can be changed
- Rankings automatically recalculate
- Status bar shows "Admin mode: You can now edit problems solved and penalty"

---

## Test Scenario 5: Admin Edits Contest Standings
**Steps:**
1. Login as admin (admin/admin1234)
2. Go to "Contest Standings"
3. Select "TEST_CONTEST_001" from dropdown
4. View the standings with 30 registered users
5. Double-click on a "Solved" or "Penalty" cell
6. Modify the value
7. Click "Generate Contest" button

**Expected Result:**
- Admin can edit solve counts and penalties
- Values are updated in the table
- Clicking "Generate Contest" triggers ranking calculation
- Rating changes are calculated and displayed

---

## Test Scenario 6: User Cannot Edit Contest Standings
**Steps:**
1. Logout and login as user001
2. Go to "Contest Standings"
3. Select any contest with "[Upcoming]" status
4. Try to double-click on a "Solved" cell
5. Try to click "Generate Contest" button

**Expected Result:**
- Cells are NOT editable
- "Generate Contest" button is HIDDEN
- User can only VIEW standings
- "Register" button is visible instead

---

## Test Scenario 7: New Future Contests Available
**Steps:**
1. Login as any user
2. Go to "Search Contests"
3. Filter by "Upcoming Contests"
4. Scroll through the list

**Expected Result:**
- All contests from January 15 onwards are visible
- Contests span through February 28, 2026
- Include various contest types:
  - Codeforces (CF932-936)
  - AtCoder Beginner (ABC346-350)
  - AtCoder Regular (ARC173-176)
  - Educational Codeforces (EDU165-169)
  - LeetCode (LC428-432, LC102-104)
  - Custom contests (WC2026, ALGO_FEB, etc.)

---

## Test Scenario 8: Dynamic Registration
**Steps:**
1. Login as user002
2. Go to Contest Standings
3. Select "TEST_CONTEST_001"
4. Verify user002 is in the participants list
5. Click "Register" button (should show "Already registered" or similar)
6. Go to Search Contests
7. Find "TEST_CONTEST_001"
8. Click Register (if not already registered)

**Expected Result:**
- User can register for new contests
- Registration is saved to database
- User appears in contest standings
- User cannot register twice for same contest

---

## Verification Checklist

- [ ] Admin dashboard loads without errors
- [ ] Contest creation form validates input
- [ ] New contests appear immediately in user views
- [ ] User views are read-only for solve counts
- [ ] Admin views are editable for solve counts
- [ ] Contest standings load with participants
- [ ] Admin can generate contest results
- [ ] Ratings are calculated and updated
- [ ] All 40+ new contests load successfully
- [ ] Contest dates are in correct range (Jan 15 - Feb 28)
- [ ] Navigation between screens works smoothly
- [ ] No exceptions in console output
- [ ] Database persists all changes

---

## Troubleshooting

**Issue**: Contest doesn't appear for users
- **Solution**: Verify it's marked as future (isPast = false) in database

**Issue**: Admin edits not working
- **Solution**: Check that isAdmin flag is set to true

**Issue**: User can edit solve counts
- **Solution**: Verify table.setEditable(false) is called on initialize

**Issue**: Contests not loading
- **Solution**: Check database file exists at `contest_predictor.db` in project root

---

**Last Updated**: January 8, 2026
**System Version**: 1.0 Enhanced

