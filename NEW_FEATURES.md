# Contest Rating Predictor - New Features Documentation

## 🎯 Overview
This update adds a comprehensive dynamic rating prediction system using Codeforces-style Elo algorithms, admin control panel, and contest registration functionality.

---

## ✨ New Features

### 1. **Dynamic Rating Predictor** 🔮
A fully dynamic rating prediction system that fetches real contestants from Codeforces API.

**Location**: Rating Predictor button in the navigation menu

**Features**:
- Fetch up to 1000 active Codeforces users via API
- Automatic rank assignment based on current ratings
- Expected rank calculation using Elo win probability formula
- Dynamic delta (rating change) computation
- Real-time statistics (avg delta, max gain, max loss)
- Editable rank column for manual adjustments
- Color-coded delta display (green for positive, red for negative)

**How it works**:
1. Enter number of contestants (default: 100)
2. Click "Fetch from API" to load real Codeforces users
3. Ranks are auto-assigned (higher rating = better rank)
4. Click "Predict Rating Changes" to calculate deltas
5. View predicted ratings for all contestants

**Algorithm**:
```
Win Probability: P = 1 / (1 + 10^((Rb - Ra) / 400))
Expected Rank = 1 + Σ(win probability vs each opponent)
Delta = K * log(Expected Rank / Actual Rank)
New Rating = Old Rating + Delta
```

---

### 2. **Admin Login System** 🛡️

**Default Admin Credentials**:
- Username: `admin`
- Password: `admin123`

**Admin Features**:
- Separate admin authentication
- Contest creation and management
- User and registration statistics
- Contest status monitoring

**How to Access**:
1. Go to Login page
2. Enter admin credentials
3. Click "Admin Login" button (orange)
4. Access Admin Dashboard

---

### 3. **Admin Dashboard** 📊

**Tab 1: Manage Contests**
- Create new contests with custom parameters
- Set contest ID, name, date/time, duration
- Configure max participants
- View all contests in table format
- Monitor registration status

**Tab 2: Contest Registrations**
- View registered users per contest
- Monitor registration counts
- Manage user registrations

**Tab 3: Statistics**
- Total contests created
- Total users in system
- Active contests count
- Total registrations across all contests

---

### 4. **Enhanced Contest Model** 🏆

**New Contest Properties**:
- `registeredUsers` - List of registered usernames
- `createdByAdmin` - Admin who created the contest
- `maxParticipants` - Maximum allowed participants
- `registrationOpen` - Toggle registration status

**Contest Methods**:
- `registerUser(username)` - Register user for contest
- `unregisterUser(username)` - Remove registration
- `isUserRegistered(username)` - Check registration status
- `getRegisteredCount()` - Get registration count

---

### 5. **Enhanced Database Schema** 💾

**New Tables**:

**admins**:
- admin_id (PRIMARY KEY)
- username (UNIQUE)
- password
- email
- full_name
- created_at
- is_active

**contest_registrations**:
- id (PRIMARY KEY)
- contest_id (FOREIGN KEY)
- username
- registered_at
- UNIQUE(contest_id, username)

**rating_history**:
- id (PRIMARY KEY)
- contest_id (FOREIGN KEY)
- username
- old_rating
- new_rating
- delta
- contest_date

**Enhanced contests table**:
- created_by_admin
- max_participants
- registration_open

---

## 🔧 Technical Implementation

### Key Classes Added:

**1. Contestant.java**
- Model for dynamic rating prediction
- Properties: handle, oldRating, rank, expectedRank, delta, newRating

**2. ContestantRatingPredictor.java**
- Core rating prediction algorithms
- Codeforces API integration
- Elo-based calculations

**3. Admin.java**
- Admin user model
- Authentication and management

**4. AdminDatabase.java**
- Admin authentication
- Admin CRUD operations

**5. RatingPredictorController.java**
- UI controller for rating predictor
- API fetch handling
- Statistics computation

**6. AdminDashboardController.java**
- Admin panel controller
- Contest creation
- Statistics management

---

## 📦 Dependencies Added

**Gson** (version 2.10.1):
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

Required for JSON parsing from Codeforces API.

---

## 🚀 How to Use

### For Users:

1. **Login** with existing credentials
2. Click **"Rating Predictor"** in the navigation menu
3. **Fetch contestants** from Codeforces API
4. **View predicted ratings** for all contestants
5. **Adjust ranks** manually if needed (double-click rank cell)
6. **Recalculate** predictions after rank changes

### For Admins:

1. **Login** with admin credentials
2. Navigate to **Admin Dashboard**
3. **Create contests** in "Manage Contests" tab
4. **Monitor registrations** in "Contest Registrations" tab
5. **View statistics** in "Statistics" tab
6. **Logout** when done

---

## 🎨 UI Enhancements

- New **Rating Predictor** button in navigation
- New **Admin Login** button on login page (orange)
- Color-coded rating changes (green/red)
- Real-time statistics display
- Responsive table views
- Professional admin dashboard

---

## 🔐 Security Notes

⚠️ **For Production**:
- Hash admin passwords (currently plain text)
- Add password strength requirements
- Implement session management
- Add HTTPS for API calls
- Validate all user inputs
- Add rate limiting for API calls

---

## 📝 API Usage

**Codeforces API Endpoint**:
```
https://codeforces.com/api/user.ratedList?activeOnly=true
```

**Rate Limits**: Please respect Codeforces API rate limits (1 call per 2 seconds recommended)

**Response Format**: JSON with user array containing:
- handle (username)
- rating (current rating)
- Other metadata

---

## 🐛 Troubleshooting

**Issue**: Rating Predictor shows "Fetch Error"
- **Solution**: Check internet connection and Codeforces API status

**Issue**: Admin Login fails
- **Solution**: Ensure database is initialized (default admin created automatically)

**Issue**: Contest creation fails
- **Solution**: Check for duplicate contest IDs

---

## 🔄 Backward Compatibility

✅ All existing features remain unchanged:
- User authentication
- Profile management
- Contest search
- Rating calculation (original method)
- User registration

The new features are **additive only** and don't break existing functionality.

---

## 📚 Future Enhancements (Suggested)

1. Contest registration UI for users
2. Email notifications for contest updates
3. Rating history graphs
4. Leaderboards
5. Contest standings integration
6. Multi-platform API support (AtCoder, LeetCode)
7. Export data to CSV/Excel
8. Advanced analytics dashboard

---

## 👨‍💻 Development Notes

**Build Command**:
```bash
mvn clean compile
mvn javafx:run
```

**Required Java Version**: 11 or higher
**Required Maven**: 3.6 or higher

---

## 📄 License

This project is for educational purposes. Please respect Codeforces API terms of service.

---

**Last Updated**: December 30, 2025
**Version**: 2.0.0
**Author**: Contest Rating Predictor Team
