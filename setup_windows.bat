@echo off
echo ============================================================
echo 🚀 Kotlin Text Editor - Windows Setup
echo ============================================================

REM Check if Python is installed
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Python not found! Please install Python 3 from https://python.org
    pause
    exit /b 1
)

echo ✅ Python found

REM Check if we're in the right directory
if not exist "desktop_compiler_script.py" (
    echo ❌ Error: desktop_compiler_script.py not found!
    echo    Please run this script from the project root directory.
    pause
    exit /b 1
)

echo ✅ Script found

REM Run the setup
echo.
echo 🔄 Starting setup process...
echo.
python setup_integration.py

echo.
echo ============================================================
echo 📋 Setup completed! 
echo.
echo 💡 To start the compiler bridge manually:
echo    python desktop_compiler_script.py
echo.
echo 📱 Then use your Android app to compile Kotlin code!
echo ============================================================
pause
