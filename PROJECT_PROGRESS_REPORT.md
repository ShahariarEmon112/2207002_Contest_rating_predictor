# Project Progress Report

## 1. Project Title
**Contest Rating Predictor - A JavaFX Desktop Application**

---

## 2. Short Description of Your Project

Contest Rating Predictor is a comprehensive desktop application built with JavaFX that helps competitive programmers predict and track their contest ratings. The system integrates with the Codeforces API to fetch real contestant data and implements an AtCoder-style rating calculation algorithm. The application features user authentication, contest management, dynamic rating prediction, admin dashboard, and personalized user profiles with rating history tracking.

**Key Technologies Used:**
- JavaFX 17.0.2 for GUI development
- Java 11 for backend logic
- SQLite database for data persistence
- Codeforces API for real-time contestant data
- Maven for dependency management
- JSON (Gson) for API response parsing

---

## 3. Brief Summary of Work Completed So Far

### 3.1 Core Application Features

**User Authentication System:**
- Implemented secure user registration and login functionality
- Database-backed user management with profile persistence
- Session management for logged-in users

**Rating Prediction Engine:**
- Developed two rating prediction mechanisms:
  1. **Basic Predictor:** Manual entry of total participants and rank to calculate rating changes
  2. **Dynamic Predictor:** Integration with Codeforces API to fetch real contestants with live ratings
- Implemented AtCoder-style rating formula using Elo-based calculations
- Expected rank calculation: `P = 1 / (1 + 10^((Rb - Ra) / 400))`
- Rating delta computation: `Delta = K * log(Expected Rank / Actual Rank)`
- Real-time statistics display (average delta, max gain, max loss)
- Color-coded delta visualization (green for positive, red for negative changes)

**Contest Management System:**
- Contest search functionality with filters (All/Past/Upcoming contests)
- Contest details viewing with standings
- User registration for upcoming contests
- Contest participation tracking

**User Profile Dashboard:**
- Current rating display with color-tier system
- Contest history with participation count
- Maximum rating achieved tracking
- Rating change history

### 3.2 API Integration Implementation

Successfully integrated Codeforces API to enhance prediction accuracy:
- **API Endpoint:** `https://codeforces.com/api/user.ratedList?activeOnly=true`
- **Implementation:** Created `ContestantRatingPredictor.java` utility class
- **Data Fetching:** HTTP GET requests using `HttpURLConnection`
- **JSON Parsing:** Implemented Gson library for parsing API responses
- **Dynamic Loading:** Support for 1-1000 contestants with real ratings
- **Error Handling:** Comprehensive exception handling for API failures and network issues

**API Integration Code Structure:**
```
ContestantRatingPredictor.java
├── fetchContestants(limit) - Fetches real contestant data from API
├── parseContestants(JsonArray) - Parses JSON response into Contestant objects
├── assignRanks(List<Contestant>) - Assigns simulated contest ranks
├── calculateExpectedRank() - Elo-based expected rank calculation
├── calculateRatingChange() - AtCoder-style rating delta computation
└── getStatistics() - Real-time analytics for all contestants
```

### 3.3 Admin Module

**Admin Authentication:**
- Separate admin login interface
- Secure admin credentials (default: admin/admin123)
- Admin-specific database table with role management

**Admin Dashboard Features:**
- **Contest Management Tab:** Create new contests with full details (ID, name, date, duration, max participants)
- **Registration Management Tab:** View and manage user registrations for each contest
- **Statistics Tab:** Real-time system statistics (total contests, users, active contests, registrations)
- **TableView Integration:** Professional data display with sortable columns

### 3.4 Database Architecture

Implemented SQLite database with three main tables:
- **Users Table:** Stores user credentials, ratings, and contest history
- **Contests Table:** Manages contest information, registrations, and status
- **Admins Table:** Handles admin authentication and permissions

**Key Database Features:**
- CRUD operations for all entities
- Relationship management between users and contests
- Transaction support for data integrity
- Automatic schema creation on first run

### 3.5 User Interface Design

**FXML Layouts Created:**
- Login.fxml - User/Admin authentication interface
- Register.fxml - New user registration form
- Predictor.fxml - Main navigation hub
- RatingPredictor.fxml - Dynamic API-based rating prediction
- Profile.fxml - User profile and rating history
- SearchContest.fxml - Contest browsing and filtering
- ContestStandings.fxml - Contest details and participant standings
- AdminDashboard.fxml - Admin control panel with tabs

**CSS Styling:**
- Custom color scheme with gradient backgrounds
- Professional button designs with hover effects
- Responsive TableView styling
- Color-coded rating tiers (gray, green, cyan, blue, violet, orange, red)

### 3.6 Controllers Implementation

Developed 8 controller classes following MVC pattern:
- `LoginController` - Handles authentication logic
- `RegistrationController` - Manages user registration
- `PredictorController` - Main navigation controller
- `RatingPredictorController` - API integration and rating calculations
- `ProfileController` - User profile management
- `ContestSearchController` - Contest filtering and search
- `ContestStandingsController` - Contest details display
- `AdminDashboardController` - Admin operations management

### 3.7 Build Configuration

- Maven POM configuration with all dependencies
- JavaFX Maven plugin for application execution
- Build scripts for Windows (build.bat)
- IntelliJ IDEA project configuration

**[Screenshots will be inserted here]**

---

## 4. Screenshot from GitHub Repository

**GitHub Insights → Contributors → Commits Over Time**

**[Screenshot will be inserted here]**

---

## 5. Suggestions Given by Teacher That Have Been Addressed

### Suggestion: Collect APIs and Connect Them for the Prediction Part

**How I Addressed It:**

Initially, the rating predictor relied solely on manual input where users would enter the total number of participants and their rank to calculate potential rating changes. While functional, this approach lacked real-world data accuracy and didn't reflect actual competitive programming scenarios.

**Implementation Steps:**

1. **API Research and Selection:**
   - Researched competitive programming platforms (Codeforces, AtCoder, CodeChef)
   - Selected Codeforces API due to comprehensive documentation and active user base
   - Chose the `user.ratedList` endpoint for fetching real contestant ratings

2. **API Integration Development:**
   - Created `ContestantRatingPredictor.java` utility class for API communication
   - Implemented HTTP request handling using `HttpURLConnection`
   - Added Gson dependency (version 2.10.1) in pom.xml for JSON parsing
   - Developed error handling for network failures and API rate limits

3. **Data Processing Pipeline:**
   - Fetched real contestant data with username, rating, rank, and max rating
   - Created `Contestant.java` model class to represent API data structure
   - Implemented data transformation from JSON to Java objects
   - Added support for dynamic contestant count (1-1000 users)

4. **Rating Calculation Enhancement:**
   - Integrated fetched real ratings into Elo formula
   - Calculated expected ranks based on actual rating distributions
   - Implemented AtCoder-style delta computation: `K * log(Expected/Actual)`
   - Added statistical analysis (average change, max gain/loss)

5. **User Interface Development:**
   - Created `RatingPredictor.fxml` with TableView for contestant display
   - Added `RatingPredictorController` for UI-API integration
   - Implemented real-time statistics panel
   - Added editable rank column for what-if scenarios
   - Color-coded rating changes (green: positive, red: negative)

6. **Testing and Validation:**
   - Tested API connectivity with various contestant counts
   - Validated rating calculations against known contest results
   - Implemented error messages for API failures
   - Added loading indicators during API calls

**Technical Achievement:**
- Successfully merged external API data with internal rating prediction algorithms
- Created a seamless user experience with live data
- Enhanced prediction accuracy by using real competitive programmer ratings
- Maintained application responsiveness during API operations

**Result:** The dynamic rating predictor now provides realistic rating change predictions based on actual competitive programmer ratings, significantly improving the application's value and accuracy compared to the manual entry system.

---

## 6. Suggestions Given by Teacher That I Plan to Address

### Future Enhancement: Develop a Dynamic Android Application

**Current Status:** Desktop application (JavaFX) is complete and fully functional.

**Planned Implementation:**
- Convert the JavaFX desktop application into a native Android application
- Implement responsive mobile UI using Android XML layouts
- Utilize Android Room database for local data persistence
- Integrate Retrofit library for API communication
- Apply Material Design principles for modern Android UI/UX
- Implement Android authentication using SharedPreferences
- Optimize for various Android screen sizes and orientations
- Add push notifications for upcoming contests
- Implement offline mode with local caching

**Timeline:** Starting development from today onwards, with planned completion within the next sprint cycle.

---

## 7. Next Plan of Action

### Immediate Goals (Current Sprint):

1. **Android Application Development:**
   - Set up Android Studio project with Material Design 3
   - Convert all FXML layouts to Android XML layouts
   - Implement Activity classes corresponding to each controller
   - Migrate SQLite database logic to Android Room
   - Integrate Retrofit for Codeforces API communication

2. **Mobile-Specific Features:**
   - Implement biometric authentication (fingerprint/face unlock)
   - Add dark mode support for better mobile experience
   - Create home screen widgets for quick rating checks
   - Implement swipe gestures for navigation
   - Add pull-to-refresh for contest lists

3. **Cross-Platform Synchronization:**
   - Design cloud synchronization strategy
   - Consider Firebase integration for user data backup
   - Implement account syncing between desktop and mobile

4. **Testing and Deployment:**
   - Unit testing for all Android components
   - UI testing using Espresso framework
   - Beta testing with select users
   - Prepare for Google Play Store deployment

### Long-Term Goals:

- Implement social features (friend comparisons, leaderboards)
- Add support for multiple competitive programming platforms (AtCoder, CodeChef)
- Develop web dashboard using Spring Boot backend
- Integrate machine learning for personalized training recommendations
- Create community features (discussion forums, contest reminders)

---

## Conclusion

The desktop version of the Contest Rating Predictor is now complete with full API integration, admin management, and comprehensive user features. The successful implementation of Codeforces API integration has significantly enhanced the application's predictive accuracy. Moving forward, the focus shifts to developing a dynamic Android application to provide mobile accessibility while maintaining all core features of the desktop version.
