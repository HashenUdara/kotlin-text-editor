#!/bin/bash

echo "============================================================"
echo "🚀 Kotlin Text Editor - macOS/Linux Setup"
echo "============================================================"

# Check if Python 3 is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 not found! Please install Python 3"
    echo "   macOS: brew install python3"
    echo "   Ubuntu: sudo apt install python3"
    echo "   Other: https://python.org"
    exit 1
fi

echo "✅ Python 3 found"

# Check if we're in the right directory
if [ ! -f "desktop_compiler_script.py" ]; then
    echo "❌ Error: desktop_compiler_script.py not found!"
    echo "   Please run this script from the project root directory."
    exit 1
fi

echo "✅ Script found"

# Make the script executable
chmod +x desktop_compiler_script.py
chmod +x setup_integration.py
chmod +x quick_setup.py

echo "✅ Scripts made executable"

# Run the setup
echo ""
echo "🔄 Starting setup process..."
echo ""
python3 setup_integration.py

echo ""
echo "============================================================"
echo "📋 Setup completed!"
echo ""
echo "💡 To start the compiler bridge manually:"
echo "   ./desktop_compiler_script.py"
echo "   OR"
echo "   python3 desktop_compiler_script.py"
echo ""
echo "📱 Then use your Android app to compile Kotlin code!"
echo "============================================================"
