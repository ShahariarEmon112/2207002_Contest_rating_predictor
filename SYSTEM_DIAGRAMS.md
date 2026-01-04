# System Architecture and Flow Diagrams

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Contest Rating Predictor                      │
│                         Main Application                         │
└────────────────┬────────────────────────────┬───────────────────┘
                 │                            │
        ┌────────▼─────────┐        ┌────────▼──────────┐
        │   User Interface │        │  Admin Interface  │
        │   (Profile, etc) │        │    (Dashboard)    │
        └────────┬─────────┘        └────────┬──────────┘
                 │                            │
        ┌────────▼────────────────────────────▼───────────┐
        │            Contest Standings System              │
        │  - View Standings                                │
        │  - Register for Contests                         │
        │  - Edit Solve Counts (Admin)                     │
        │  - Generate Rankings & Ratings                   │
        └────────┬─────────────────────────────────────────┘
                 │
        ┌────────▼─────────────────────────────────────────┐
        │              Database Layer (SQLite)              │
        │  - Users, Admins, Contests                        │
        │  - Participants, Contest Registrations            │
        └───────────────────────────────────────────────────┘
```

## User Login Flow

```
┌───────────┐
│   Start   │
└─────┬─────┘
      │
      ▼
┌─────────────────┐
│  Login Screen   │
│  Enter username │
│  Enter password │
└─────┬───────────┘
      │
      ▼
┌──────────────────────┐
│  Check Credentials   │
└──────┬───────┬───────┘
       │       │
   Admin?    User?
       │       │
       ▼       ▼
┌────────┐  ┌────────┐
│ Admin  │  │  User  │
│Dashboard│ │Profile │
└────────┘  └────────┘
```

## Contest Standings Workflow

### User Perspective

```
┌──────────────┐
│ User Profile │
└──────┬───────┘
       │ Click "View Standings"
       ▼
┌────────────────────┐
│ Contest Standings  │
│ Select Contest     │
└──────┬─────────────┘
       │
       ├─────────────────────┐
       │                     │
       ▼                     ▼
┌─────────────┐      ┌──────────────┐
│ View Current│      │  Register Me │
│  Standings  │      │  for Contest │
└─────────────┘      └──────┬───────┘
                            │
                            ▼
                    ┌──────────────┐
                    │ Added to     │
                    │ Participants │
                    └──────────────┘
```

### Admin Perspective

```
┌──────────────┐
│    Admin     │
│  Dashboard   │
└──────┬───────┘
       │ Click "Manage Standings"
       ▼
┌──────────────────────┐
│ Contest Standings    │
│ (Admin Mode)         │
└──────┬───────────────┘
       │
       ├─────────────────────┬──────────────────┐
       │                     │                  │
       ▼                     ▼                  ▼
┌─────────────┐      ┌─────────────┐   ┌──────────────┐
│ Edit Solve  │      │   Remove    │   │   Generate   │
│   Counts    │      │ Participant │   │   Results    │
└──────┬──────┘      └─────────────┘   └──────┬───────┘
       │                                       │
       │ Double-click cells                    │
       ▼                                       ▼
┌─────────────┐                       ┌──────────────┐
│ Enter value │                       │  Calculate:  │
│ Auto-saved  │                       │  - Rankings  │
└─────────────┘                       │  - Ratings   │
                                      │  - Changes   │
                                      └──────┬───────┘
                                             │
                                             ▼
                                      ┌──────────────┐
                                      │ Save to DB & │
                                      │ Show Results │
                                      └──────────────┘
```

## Contest Generation Flow

```
┌───────────────────────┐
│  Admin selects        │
│  contest to manage    │
└──────────┬────────────┘
           │
           ▼
┌───────────────────────┐
│  System checks if     │
│  participants exist   │
└──────┬────────┬───────┘
       │        │
      No       Yes
       │        │
       ▼        │
┌──────────────┐│
│ Auto-generate││
│ participants:││
│ user001-030  ││
│ + registered ││
│    users     ││
└──────┬───────┘│
       │        │
       └────┬───┘
            │
            ▼
┌────────────────────────┐
│  Display standings     │
│  All start with 0      │
│  problems solved       │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  Admin edits solve     │
│  counts and penalties  │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  Admin clicks          │
│  "Generate Results"    │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  System processes:     │
│  1. Sort by solved     │
│  2. Assign ranks       │
│  3. Calculate ratings  │
│  4. Save to database   │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  Show updated          │
│  standings with        │
│  rankings & ratings    │
└────────────────────────┘
```

## Rating Calculation Flow

```
┌──────────────────────┐
│  For each participant│
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Get current rating  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Calculate expected  │
│  rank based on all   │
│  participant ratings │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Get actual rank     │
│  (from solve counts) │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Rating Change =     │
│  (Expected - Actual) │
│  × 20                │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Cap change at       │
│  -150 to +150        │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  New Rating =        │
│  Current + Change    │
└──────────────────────┘
```

## Database Entity Relationships

```
┌─────────────┐          ┌──────────────┐
│    Admin    │          │     User     │
└─────────────┘          └──────┬───────┘
                                │
                                │ participates
                                │
                                ▼
┌─────────────┐          ┌──────────────┐          ┌─────────────┐
│   Contest   │◄─────────┤ Registration ├─────────►│ Participant │
│             │  has     │              │  becomes │             │
│  - ID       │          │  - User      │          │  - Username │
│  - Name     │          │  - Contest   │          │  - Rating   │
│  - Date     │          │  - Timestamp │          │  - Solved   │
│  - Duration │          │              │          │  - Penalty  │
└─────┬───────┘          └──────────────┘          └─────────────┘
      │
      │ created_by
      │
      ▼
┌─────────────┐
│    Admin    │
└─────────────┘
```

## Component Interaction Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                        Controllers                            │
├──────────┬──────────┬──────────────┬─────────────────────────┤
│  Login   │  Admin   │  Profile     │  ContestStandings       │
│Controller│Dashboard │Controller    │  Controller             │
└────┬─────┴────┬─────┴──────┬───────┴───────────┬─────────────┘
     │          │            │                   │
     │          │            │                   │
     ▼          ▼            ▼                   ▼
┌────────────────────────────────────────────────────────────────┐
│                        Data Layer                              │
├──────────────┬──────────────┬──────────────┬──────────────────┤
│  UserDB      │  AdminDB     │  ContestDB   │  DatabaseManager │
└──────┬───────┴──────┬───────┴──────┬───────┴──────┬───────────┘
       │              │              │              │
       └──────────────┴──────────────┴──────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │   SQLite Database    │
              │  contest_predictor.db│
              └──────────────────────┘
```

## Data Flow for "Generate Contest Results"

```
User Action: Click "Generate Contest Results"
         │
         ▼
┌─────────────────────────────────────────┐
│ ContestStandingsController              │
│  .handleGenerateContest()               │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Get all participants from table         │
│ (ObservableList<Participant>)           │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Sort participants:                      │
│  1. By problems solved (descending)     │
│  2. By penalty (ascending)              │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Assign ranks (1, 2, 3, ...)             │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ For each participant:                   │
│  - Calculate expected rank              │
│  - Compare with actual rank             │
│  - Compute rating change                │
│  - Set predicted rating                 │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ DatabaseManager                         │
│  .updateParticipantSolveCount()         │
│  Save all participants                  │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Refresh TableView with updated data    │
│ Show success message                    │
└─────────────────────────────────────────┘
```

## File Organization

```
src/main/
├── java/com/contestpredictor/
│   ├── Main.java                        ← Entry point
│   │
│   ├── controller/                      ← UI Controllers
│   │   ├── LoginController.java             (User + Admin login)
│   │   ├── AdminDashboardController.java    (Admin features)
│   │   ├── ContestStandingsController.java  (★ Standings management)
│   │   ├── ProfileController.java           (User profile)
│   │   ├── PredictorController.java         (Rating prediction)
│   │   └── ...
│   │
│   ├── model/                           ← Data Models
│   │   ├── Contest.java                     (Contest entity)
│   │   ├── Participant.java                 (★ Participant with solve count)
│   │   ├── User.java                        (User entity)
│   │   └── Admin.java                       (Admin entity)
│   │
│   ├── data/                            ← Database Operations
│   │   ├── DatabaseManager.java             (★ Core DB operations)
│   │   ├── ContestDatabase.java             (Contest management)
│   │   ├── UserDatabase.java                (User management)
│   │   └── AdminDatabase.java               (Admin auth)
│   │
│   └── util/                            ← Utilities
│       └── RatingPredictor.java             (Rating calculations)
│
└── resources/
    ├── fxml/                            ← UI Layouts
    │   ├── ContestStandings.fxml            (★ Standings UI)
    │   ├── AdminDashboard.fxml              (Admin UI)
    │   ├── Login.fxml                       (Login UI)
    │   └── ...
    │
    └── css/
        └── styles.css                       (Styling)

★ = New or significantly modified for standings system
```

## Key Features Map

```
┌────────────────────────────────────────────────────────────────┐
│                     Feature Overview                            │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Authentication                                                 │
│  ├─ User Login        → UserDatabase.authenticate()            │
│  └─ Admin Login       → AdminDatabase.authenticate()           │
│                                                                 │
│  Contest Management                                             │
│  ├─ Create Contest    → AdminDashboardController               │
│  ├─ Delete Contest    → ContestDatabase.deleteContest()        │
│  └─ View Contests     → ContestDatabase.getAllContests()       │
│                                                                 │
│  Participant Management                                         │
│  ├─ Auto-generate     → ContestStandingsController             │
│  ├─ Manual Add        → AdminDashboardController               │
│  └─ Remove            → DatabaseManager.removeParticipant()    │
│                                                                 │
│  Standings System                                               │
│  ├─ View Standings    → ContestStandingsController (all users) │
│  ├─ Edit Solve Counts → Double-click table cells (admin only)  │
│  ├─ Register User     → DatabaseManager.registerUser...()      │
│  └─ Generate Results  → Calculate ranks & ratings              │
│                                                                 │
│  Database Persistence                                           │
│  ├─ Users             → users table                            │
│  ├─ Admins            → admins table                           │
│  ├─ Contests          → contests table                         │
│  ├─ Participants      → participants table                     │
│  └─ Registrations     → contest_registrations table            │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

## System States

```
Application States:

┌─────────────┐
│   Startup   │  → Initialize DB, load data
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    Login    │  → Authenticate user/admin
└──────┬──────┘
       │
       ├─────────────┬─────────────┐
       │             │             │
       ▼             ▼             ▼
┌────────────┐ ┌───────────┐ ┌──────────┐
│   Admin    │ │   User    │ │  Guest   │
│  Dashboard │ │  Profile  │ │ (none)   │
└────────────┘ └───────────┘ └──────────┘
       │             │
       ▼             ▼
┌────────────────────────┐
│   Contest Standings    │
│   - View mode (user)   │
│   - Edit mode (admin)  │
└────────────────────────┘
```

---

These diagrams provide a comprehensive visual representation of the system architecture, data flow, and component interactions.
