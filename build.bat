@echo off
echo ========================================
echo   Contest Predictor - Build Script
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH!
    echo.
    echo Please install Maven from: https://maven.apache.org/download.cgi
    echo Or add Maven to your system PATH.
    pause
    exit /b 1
)

echo Cleaning previous build...
call mvn clean

echo.
echo Compiling project...
call mvn compile

echo.
echo ========================================
echo Build complete!
echo ========================================
echo.
echo To run the application, use:
echo   mvn javafx:run
echo.
echo To create admin accounts, use:
echo   mvn exec:java -Dexec.mainClass="com.contestpredictor.util.AdminSetup"
echo.
pause
