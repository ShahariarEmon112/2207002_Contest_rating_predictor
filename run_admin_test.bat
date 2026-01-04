@echo off
echo =========================================
echo   Admin Dashboard - Quick Test
echo =========================================
echo.
echo This script will help you test the admin dashboard
echo.

REM Check if database exists
if exist "contest_predictor.db" (
    echo [OK] Database file found
) else (
    echo [INFO] Database will be created on first run
)

echo.
echo =========================================
echo   Starting Application...
echo =========================================
echo.
echo Default Admin Credentials:
echo   Username: admin
echo   Password: admin1234
echo.
echo The application will open in a moment...
echo.

REM Try to run with Maven
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Maven found - Starting application...
    call mvn clean javafx:run
) else (
    echo [ERROR] Maven not found!
    echo.
    echo Please use one of these methods:
    echo   1. Install Maven and add to PATH
    echo   2. Open project in IntelliJ IDEA
    echo   3. Right-click Main.java and select 'Run'
    echo.
    pause
)

pause
