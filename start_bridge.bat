@echo off
echo ============================================================
echo 🚀 Kotlin Text Editor - Compiler Bridge
echo ============================================================

REM Add ADB to PATH for this session
set PATH=C:\Users\oketh\KotlinTextEditor\platform-tools;%PATH%

REM Check ADB
echo 🔍 Checking ADB connection...
adb devices
if %errorlevel% neq 0 (
    echo ❌ ADB not working properly
    pause
    exit /b 1
)

echo ✅ ADB is working!
echo.

REM Check for Kotlin (optional - bridge will handle missing Kotlin gracefully)
kotlinc -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  Kotlin compiler not found - some compilation features may be limited
    echo    You can install Kotlin later or use online Kotlin playground integration
) else (
    echo ✅ Kotlin compiler found
)

echo.
echo 🚀 Starting Kotlin Compiler Bridge...
echo 📱 Now you can use the Android app to compile code!
echo 🛑 Press Ctrl+C to stop the bridge
echo ============================================================

REM Run the bridge with proper PATH
python desktop_compiler_script.py

echo.
echo 👋 Bridge stopped
pause

