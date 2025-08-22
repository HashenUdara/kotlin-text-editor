#!/usr/bin/env python3
"""
Quick Setup for Kotlin Text Editor Compilation
This script provides a fast setup for users who already have prerequisites.
"""

import os
import sys
import subprocess
import platform
from pathlib import Path

def run_command(command):
    """Run a command and return success status"""
    try:
        result = subprocess.run(command, shell=True, capture_output=True, text=True)
        return result.returncode == 0, result.stdout, result.stderr
    except Exception as e:
        return False, "", str(e)

def main():
    print("🚀 Kotlin Text Editor - Quick Setup")
    print("=" * 50)
    
    # Check if we're in the right directory
    if not Path("desktop_compiler_script.py").exists():
        print("❌ Error: desktop_compiler_script.py not found!")
        print("   Please run this script from the project root directory.")
        sys.exit(1)
    
    # Quick checks
    print("🔍 Checking prerequisites...")
    
    # Check ADB
    success, stdout, stderr = run_command("adb version")
    if success:
        print("✅ ADB found")
    else:
        print("❌ ADB not found - please install Android SDK")
        print("   Download: https://developer.android.com/studio/command-line")
    
    # Check Kotlin
    success, stdout, stderr = run_command("kotlinc -version")
    if success:
        print("✅ Kotlin compiler found")
    else:
        print("❌ Kotlin compiler not found")
        print("   Install: https://kotlinlang.org/docs/command-line.html")
    
    # Check device
    success, stdout, stderr = run_command("adb devices")
    if success and "device" in stdout:
        devices = [line for line in stdout.split('\n') if line.strip() and 'device' in line and 'List' not in line]
        if devices:
            print(f"✅ Android device connected: {devices[0].split()[0]}")
        else:
            print("⚠️  No Android devices found")
            print("   Please connect your device and enable USB debugging")
    else:
        print("❌ Cannot check devices")
    
    print("\n" + "=" * 50)
    print("🎯 Ready to start!")
    print("\n📋 Instructions:")
    print("1. Run the compiler bridge:")
    print(f"   python3 desktop_compiler_script.py")
    print("\n2. On your Android device:")
    print("   - Open Kotlin Text Editor")
    print("   - Write some Kotlin code")
    print("   - Tap Menu ☰ → 'Compile Code'")
    print("\n3. Watch both screens for compilation progress!")
    
    # Ask if user wants to start the bridge now
    print("\n" + "-" * 50)
    choice = input("🚀 Start the compiler bridge now? (y/n): ").lower().strip()
    
    if choice in ['y', 'yes']:
        print("\n🔄 Starting Kotlin Compiler Bridge...")
        print("🛑 Press Ctrl+C to stop")
        print("=" * 50)
        try:
            # Run the desktop compiler script
            os.system("python3 desktop_compiler_script.py")
        except KeyboardInterrupt:
            print("\n👋 Bridge stopped by user")
    else:
        print("\n💡 When ready, run:")
        print("   python3 desktop_compiler_script.py")
    
    print("\n🎉 Happy coding!")

if __name__ == "__main__":
    main()
