@echo off
REM Kotlin Text Editor - Desktop Compiler Bridge Startup Script
REM ===========================================================
REM This script starts the desktop compiler bridge service and checks dependencies.

echo.
echo ===================================================
echo Kotlin Text Editor - Desktop Compiler Bridge
echo ===================================================
echo.

REM Check if Python is available
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] ERROR: Python is not installed or not in PATH
    echo     Please install Python 3.7+ from https://python.org
    echo.
    pause
    exit /b 1
)

echo [+] Python is available

REM Check if ADB is available
adb version >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] ERROR: ADB is not installed or not in PATH
    echo     Please install Android SDK Platform Tools
    echo     Download from: https://developer.android.com/studio/releases/platform-tools
    echo.
    pause
    exit /b 1
)

echo [+] ADB is available

REM Check if Kotlin compiler is available
kotlinc -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] WARNING: kotlinc is not installed or not in PATH
    echo     Kotlin compilation will not work
    echo     Download from: https://kotlinlang.org/docs/command-line.html
    echo.
) else (
    echo [+] Kotlin compiler is available
)

REM Check if Java compiler is available
javac -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] WARNING: javac is not installed or not in PATH
    echo     Java compilation will not work
    echo     Please install JDK from: https://adoptium.net/
    echo.
) else (
    echo [+] Java compiler is available
)

REM Check for connected Android devices
echo [*] Checking for connected Android devices...
adb devices | findstr "device$" >nul
if %errorlevel% neq 0 (
    echo [!] WARNING: No Android devices found
    echo     Please connect your Android device via USB and enable USB debugging
    echo     You can still start the bridge service and connect devices later
    echo.
) else (
    echo [+] Android device(s) connected
)

echo.
echo [*] Starting Desktop Compiler Bridge...
echo [*] Workspace: %cd%\kotlin-editor-bridge
echo [*] Press Ctrl+C to stop the service
echo.
echo ===================================================

REM Start the bridge service
python desktop-compiler-bridge.py --workspace "kotlin-editor-bridge"

echo.
echo [*] Bridge service stopped
pause
