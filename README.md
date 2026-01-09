# Contest Rating Predictor

JavaFX application for predicting contest ratings.

## Run (IntelliJ IDEA)

1. Open project in IntelliJ IDEA
2. Right-click `Main.java` → Run 'Main.main()'

## Run (Maven Command Line)

```bash
mvn clean javafx:run
```

## Login

- Username: `shahariar`
- Password: `123`

## Features

After login, you'll see **Profile** with options in navbar:

1. **Rating Predictor** - Calculate rating changes
   - Enter total participants & your rank
   - Click "Calculate Rating Change"
   - View predicted rating change
   - Click "Apply This Rating" to save

2. **Search Contests** - Browse contests
   - Search by name or ID
   - Filter: All/Past/Upcoming
   - Click any contest to view details

3. **Profile** - View your stats
   - Current rating & color tier
   - Contests participated
   - Max rating achieved

## 🎯 NEW: KUET Team Formation Contest Leaderboard System

A comprehensive leaderboard system for managing team-based contests:

### User Features:
- **View Available Contests** - Browse all leaderboard contests
- **Register/Unregister** - Join contests you're interested in
- **View Standings** - See contestant rankings, solves, and penalties
- **Overall Leaderboard** - Check your rank across all finalized contests

### Admin Features:
- **Create Contests** - Set up new team formation contests
- **Manage Standings** - Add and update participant scores
- **Finalize Contests** - Lock standings and trigger leaderboard calculation
- **Combined Leaderboard** - Automatic aggregation of all contest results

### Access Leaderboard:
- **Users:** Profile → "🏆 Team Leaderboard" button
- **Admins:** Admin Dashboard → Tab 4 or "🎯 Leaderboard" button

### Documentation:
- See `LEADERBOARD_SYSTEM_GUIDE.md` for detailed documentation
- See `LEADERBOARD_QUICK_REFERENCE.md` for quick reference
