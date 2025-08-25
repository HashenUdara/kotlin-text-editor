@echo off
REM Kotlin Text Editor - Desktop Compiler Bridge Startup Script
REM This script starts the bridge service on Windows

echo.
echo ===============================================
echo   Kotlin Text Editor - Desktop Bridge Service
echo ===============================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo [!] Python not found! Please install Python 3.7+ and add it to PATH
    echo    Download from: https://www.python.org/downloads/
    pause
    exit /b 1
)

REM Check if ADB is available
adb version >nul 2>&1
if errorlevel 1 (
    echo [!] ADB not found! Please install Android SDK Platform Tools
    echo    Make sure ADB is in your PATH
    pause
    exit /b 1
)

REM Check if kotlinc is available
kotlinc -version >nul 2>&1
if errorlevel 1 (
    echo [!] Kotlin compiler not found! Please install kotlinc
    echo     Make sure kotlinc is in your PATH
    echo     Current PATH: %PATH%
    echo     Try: where kotlinc
    pause
    exit /b 1
)

echo [+] All dependencies found!
echo.

REM Set up ADB port forwarding
echo [*] Setting up ADB port forwarding...
adb forward tcp:8765 tcp:8766
if errorlevel 1 (
    echo [!] Warning: ADB port forwarding failed
    echo     Make sure your Android device is connected
) else (
    echo [+] ADB port forwarding set up successfully
)
echo.

REM Set workspace directory
set WORKSPACE=%USERPROFILE%\kotlin-editor-bridge
if not exist "%WORKSPACE%" (
    echo [*] Creating workspace directory: %WORKSPACE%
    mkdir "%WORKSPACE%"
)

echo [*] Starting bridge service...
echo     Workspace: %WORKSPACE%
echo     Port: 8765
echo.
echo [*] Ready for Android app connections!
echo [!] Press Ctrl+C to stop the service
echo.

REM Start the bridge service
python desktop-compiler-bridge.py --workspace "%WORKSPACE%" --port 8766

echo.
echo [*] Bridge service stopped.
pause

