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

After login, you'll see **Profile** with 3 options in navbar:

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
