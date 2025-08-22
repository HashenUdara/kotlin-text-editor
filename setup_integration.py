#!/usr/bin/env python3
"""
Complete Integration Setup Script for Kotlin Text Editor
This script automates the setup process for the ADB compiler integration.
"""

import os
import sys
import subprocess
import platform
import urllib.request
import zipfile
import shutil
from pathlib import Path

class IntegrationSetup:
    def __init__(self):
        self.system = platform.system().lower()
        self.setup_dir = Path.home() / "KotlinTextEditor"
        self.success_count = 0
        self.total_steps = 8
        
    def print_header(self):
        print("=" * 70)
        print("🚀 Kotlin Text Editor - Complete Integration Setup")
        print("=" * 70)
        print(f"System: {platform.system()} {platform.release()}")
        print(f"Python: {sys.version.split()[0]}")
        print(f"Setup Directory: {self.setup_dir}")
        print("-" * 70)
        
    def print_step(self, step_num, title, success=None):
        if success is None:
            print(f"\n📋 Step {step_num}/{self.total_steps}: {title}")
        elif success:
            print(f"✅ Step {step_num}/{self.total_steps}: {title} - SUCCESS")
            self.success_count += 1
        else:
            print(f"❌ Step {step_num}/{self.total_steps}: {title} - FAILED")
    
    def run_command(self, command, capture_output=True, check=True):
        """Run a system command safely"""
        try:
            if isinstance(command, str):
                command = command.split()
            result = subprocess.run(command, capture_output=capture_output, 
                                  text=True, check=check)
            return result.returncode == 0, result.stdout, result.stderr
        except (subprocess.CalledProcessError, FileNotFoundError) as e:
            return False, "", str(e)
    
    def check_adb(self):
        """Check if ADB is installed and accessible"""
        self.print_step(1, "Checking ADB Installation")
        
        success, stdout, stderr = self.run_command("adb version", check=False)
        
        if success:
            version = stdout.split('\n')[0] if stdout else "Unknown version"
            print(f"   Found: {version}")
            self.print_step(1, "ADB Found", True)
            return True
        else:
            print("   ADB not found in PATH")
            print("   📥 Installing ADB...")
            return self.install_adb()
    
    def install_adb(self):
        """Install ADB based on the operating system"""
        try:
            if self.system == "windows":
                return self.install_adb_windows()
            elif self.system == "darwin":  # macOS
                return self.install_adb_macos()
            elif self.system == "linux":
                return self.install_adb_linux()
            else:
                print(f"   ❌ Unsupported system: {self.system}")
                return False
        except Exception as e:
            print(f"   ❌ Installation failed: {e}")
            return False
    
    def install_adb_windows(self):
        """Install ADB on Windows"""
        print("   📥 Downloading Android SDK Platform Tools for Windows...")
        
        url = "https://dl.google.com/android/repository/platform-tools-latest-windows.zip"
        zip_path = self.setup_dir / "platform-tools.zip"
        extract_path = self.setup_dir / "platform-tools"
        
        # Create setup directory
        self.setup_dir.mkdir(exist_ok=True)
        
        # Download platform tools
        urllib.request.urlretrieve(url, zip_path)
        
        # Extract
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            zip_ref.extractall(self.setup_dir)
        
        # Add to PATH (temporarily)
        adb_path = extract_path / "adb.exe"
        if adb_path.exists():
            os.environ["PATH"] = f"{extract_path}{os.pathsep}{os.environ['PATH']}"
            print(f"   ✅ ADB installed to: {extract_path}")
            print(f"   🔧 Add this to your permanent PATH: {extract_path}")
            return True
        
        return False
    
    def install_adb_macos(self):
        """Install ADB on macOS using Homebrew"""
        print("   📥 Installing ADB via Homebrew...")
        
        # Check if Homebrew is installed
        success, _, _ = self.run_command("brew --version", check=False)
        if not success:
            print("   ❌ Homebrew not found. Please install Homebrew first:")
            print("   ▶️  /bin/bash -c \"$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\"")
            return False
        
        # Install Android platform tools
        success, _, _ = self.run_command("brew install android-platform-tools")
        return success
    
    def install_adb_linux(self):
        """Install ADB on Linux"""
        print("   📥 Installing ADB via package manager...")
        
        # Try different package managers
        commands = [
            ["sudo", "apt", "install", "-y", "adb"],  # Ubuntu/Debian
            ["sudo", "yum", "install", "-y", "android-tools"],  # RHEL/CentOS
            ["sudo", "pacman", "-S", "--noconfirm", "android-tools"],  # Arch
            ["sudo", "dnf", "install", "-y", "android-tools"]  # Fedora
        ]
        
        for cmd in commands:
            success, _, _ = self.run_command(cmd, check=False)
            if success:
                return True
        
        return False
    
    def check_kotlin(self):
        """Check if Kotlin compiler is installed"""
        self.print_step(2, "Checking Kotlin Compiler")
        
        success, stdout, stderr = self.run_command("kotlinc -version", check=False)
        
        if success:
            version = stdout.strip() if stdout else stderr.strip()
            print(f"   Found: {version}")
            self.print_step(2, "Kotlin Compiler Found", True)
            return True
        else:
            print("   Kotlin compiler not found")
            print("   📥 Installing Kotlin...")
            return self.install_kotlin()
    
    def install_kotlin(self):
        """Install Kotlin compiler"""
        if self.system == "darwin":  # macOS
            print("   📥 Installing Kotlin via Homebrew...")
            success, _, _ = self.run_command("brew install kotlin")
            return success
        elif self.system == "linux":
            print("   📥 Installing Kotlin via SDKMAN...")
            return self.install_kotlin_sdkman()
        elif self.system == "windows":
            print("   📥 Installing Kotlin via Chocolatey...")
            success, _, _ = self.run_command("choco install kotlin", check=False)
            if success:
                return True
            else:
                print("   📥 Installing Kotlin via SDKMAN...")
                return self.install_kotlin_sdkman()
        
        return False
    
    def install_kotlin_sdkman(self):
        """Install Kotlin using SDKMAN"""
        try:
            # Install SDKMAN if not present
            sdkman_dir = Path.home() / ".sdkman"
            if not sdkman_dir.exists():
                print("   📥 Installing SDKMAN...")
                install_cmd = 'curl -s "https://get.sdkman.io" | bash'
                os.system(install_cmd)
            
            # Source SDKMAN and install Kotlin
            sdk_script = sdkman_dir / "bin" / "sdkman-init.sh"
            if sdk_script.exists():
                install_cmd = f'source {sdk_script} && sdk install kotlin'
                result = os.system(install_cmd)
                return result == 0
            
            return False
        except Exception as e:
            print(f"   ❌ SDKMAN installation failed: {e}")
            return False
    
    def check_device_connection(self):
        """Check if Android device is connected"""
        self.print_step(3, "Checking Device Connection")
        
        success, stdout, stderr = self.run_command("adb devices")
        
        if success and stdout:
            lines = stdout.strip().split('\n')[1:]  # Skip header
            devices = [line for line in lines if line.strip() and 'device' in line]
            
            if devices:
                device_id = devices[0].split()[0]
                print(f"   Found device: {device_id}")
                self.print_step(3, "Device Connected", True)
                return True
            else:
                print("   No devices found")
                print("   📱 Please connect your Android device and enable USB debugging")
                print("   ⚙️  Settings > Developer Options > USB Debugging")
                self.print_step(3, "No Device Connected", False)
                return False
        else:
            self.print_step(3, "ADB Connection Failed", False)
            return False
    
    def setup_directories(self):
        """Create necessary directories"""
        self.print_step(4, "Setting up Directories")
        
        try:
            # Create main setup directory
            self.setup_dir.mkdir(exist_ok=True)
            
            # Create subdirectories
            (self.setup_dir / "scripts").mkdir(exist_ok=True)
            (self.setup_dir / "logs").mkdir(exist_ok=True)
            (self.setup_dir / "temp").mkdir(exist_ok=True)
            
            print(f"   Created: {self.setup_dir}")
            print(f"   Created: {self.setup_dir / 'scripts'}")
            print(f"   Created: {self.setup_dir / 'logs'}")
            print(f"   Created: {self.setup_dir / 'temp'}")
            
            self.print_step(4, "Directories Created", True)
            return True
        except Exception as e:
            print(f"   ❌ Failed to create directories: {e}")
            self.print_step(4, "Directory Creation Failed", False)
            return False
    
    def copy_compiler_script(self):
        """Copy the desktop compiler script to setup directory"""
        self.print_step(5, "Setting up Compiler Script")
        
        try:
            source_script = Path("desktop_compiler_script.py")
            target_script = self.setup_dir / "scripts" / "kotlin_compiler_bridge.py"
            
            if source_script.exists():
                shutil.copy2(source_script, target_script)
                # Make executable on Unix-like systems
                if self.system != "windows":
                    os.chmod(target_script, 0o755)
                
                print(f"   Copied to: {target_script}")
                self.print_step(5, "Compiler Script Ready", True)
                return True
            else:
                print(f"   ❌ Source script not found: {source_script}")
                self.print_step(5, "Script Copy Failed", False)
                return False
        except Exception as e:
            print(f"   ❌ Failed to copy script: {e}")
            self.print_step(5, "Script Copy Failed", False)
            return False
    
    def create_launcher_scripts(self):
        """Create launcher scripts for easy execution"""
        self.print_step(6, "Creating Launcher Scripts")
        
        try:
            script_path = self.setup_dir / "scripts" / "kotlin_compiler_bridge.py"
            
            if self.system == "windows":
                # Create Windows batch file
                launcher = self.setup_dir / "start_compiler_bridge.bat"
                launcher.write_text(f"""@echo off
echo Starting Kotlin Compiler Bridge...
cd /d "{self.setup_dir / 'scripts'}"
python kotlin_compiler_bridge.py
pause
""")
            else:
                # Create Unix shell script
                launcher = self.setup_dir / "start_compiler_bridge.sh"
                launcher.write_text(f"""#!/bin/bash
echo "Starting Kotlin Compiler Bridge..."
cd "{self.setup_dir / 'scripts'}"
python3 kotlin_compiler_bridge.py
""")
                os.chmod(launcher, 0o755)
            
            print(f"   Created launcher: {launcher}")
            self.print_step(6, "Launcher Scripts Created", True)
            return True
            
        except Exception as e:
            print(f"   ❌ Failed to create launcher: {e}")
            self.print_step(6, "Launcher Creation Failed", False)
            return False
    
    def test_compilation_setup(self):
        """Test the compilation setup with a simple Kotlin program"""
        self.print_step(7, "Testing Compilation Setup")
        
        try:
            # Create a simple test Kotlin file
            test_file = self.setup_dir / "temp" / "test.kt"
            test_file.write_text("""
fun main() {
    println("Hello from Kotlin Text Editor!")
    val message = "Setup test successful!"
    println(message)
}
""")
            
            # Try to compile it
            success, stdout, stderr = self.run_command([
                "kotlinc", str(test_file), "-d", str(self.setup_dir / "temp")
            ])
            
            if success:
                print("   ✅ Kotlin compilation test successful")
                # Clean up
                test_file.unlink()
                for class_file in (self.setup_dir / "temp").glob("*.class"):
                    class_file.unlink()
                self.print_step(7, "Compilation Test Passed", True)
                return True
            else:
                print(f"   ❌ Compilation test failed: {stderr}")
                self.print_step(7, "Compilation Test Failed", False)
                return False
                
        except Exception as e:
            print(f"   ❌ Test setup failed: {e}")
            self.print_step(7, "Test Setup Failed", False)
            return False
    
    def generate_usage_guide(self):
        """Generate a usage guide"""
        self.print_step(8, "Generating Usage Guide")
        
        try:
            guide_path = self.setup_dir / "USAGE_GUIDE.md"
            
            guide_content = f"""# Kotlin Text Editor - Usage Guide

## 🚀 Quick Start

### 1. Start the Compiler Bridge
**Windows:**
```
Double-click: {self.setup_dir / "start_compiler_bridge.bat"}
```

**macOS/Linux:**
```bash
{self.setup_dir / "start_compiler_bridge.sh"}
```

### 2. Connect Your Android Device
- Enable USB Debugging in Developer Options
- Connect via USB cable
- Allow debugging when prompted

### 3. Use the Android App
- Open Kotlin Text Editor
- Write Kotlin code
- Tap Menu ☰ → "Compile Code"
- Watch the compilation progress!

## 📁 Directory Structure
```
{self.setup_dir}/
├── scripts/
│   └── kotlin_compiler_bridge.py    # Main compiler bridge
├── logs/                             # Compilation logs
├── temp/                            # Temporary files
├── start_compiler_bridge.{("bat" if self.system == "windows" else "sh")}    # Quick launcher
└── USAGE_GUIDE.md                  # This guide
```

## 🔧 Troubleshooting

### Device Not Found
- Check USB cable (use data cable, not charge-only)
- Try different USB ports
- Restart ADB: `adb kill-server && adb start-server`

### Compilation Fails
- Ensure bridge script is running
- Check Kotlin syntax in your code
- Look at bridge terminal for error details

### Performance Tips
- Keep USB connection stable
- Don't move device during compilation
- Use quality USB cables for best performance

## 🎯 Example Code

Try this in your Kotlin Text Editor:

```kotlin
fun main() {{
    println("Hello from Android!")
    
    val numbers = listOf(1, 2, 3, 4, 5)
    val doubled = numbers.map {{ it * 2 }}
    
    println("Original: $numbers")
    println("Doubled: $doubled")
}}
```

## 📞 Support
- Check the bridge terminal for detailed error messages
- Ensure all prerequisites are properly installed
- Test with simple code examples first

**Enjoy coding Kotlin on your Android device! 🎉**
"""
            
            guide_path.write_text(guide_content)
            print(f"   Created: {guide_path}")
            self.print_step(8, "Usage Guide Generated", True)
            return True
            
        except Exception as e:
            print(f"   ❌ Failed to create guide: {e}")
            self.print_step(8, "Guide Generation Failed", False)
            return False
    
    def run_setup(self):
        """Run the complete setup process"""
        self.print_header()
        
        # Run all setup steps
        steps = [
            self.check_adb,
            self.check_kotlin,
            self.check_device_connection,
            self.setup_directories,
            self.copy_compiler_script,
            self.create_launcher_scripts,
            self.test_compilation_setup,
            self.generate_usage_guide
        ]
        
        for step in steps:
            if not step():
                print(f"\n⚠️  Setup step failed. You may need to resolve this manually.")
                print("   Check the error messages above for details.")
        
        # Final summary
        print("\n" + "=" * 70)
        print("📊 Setup Summary")
        print("=" * 70)
        print(f"✅ Successful steps: {self.success_count}/{self.total_steps}")
        
        if self.success_count == self.total_steps:
            print("🎉 COMPLETE SETUP SUCCESSFUL!")
            print("\n🚀 Next Steps:")
            print(f"1. Run: {self.setup_dir / f'start_compiler_bridge.{('bat' if self.system == 'windows' else 'sh')}'}")
            print("2. Connect your Android device")
            print("3. Open Kotlin Text Editor and start coding!")
            print(f"4. Read the usage guide: {self.setup_dir / 'USAGE_GUIDE.md'}")
        else:
            print("⚠️  PARTIAL SETUP COMPLETED")
            print("Some steps failed. Check error messages above.")
            print("You may need to install missing components manually.")
        
        print("\n📁 Setup files located at:")
        print(f"   {self.setup_dir}")

def main():
    """Main entry point"""
    setup = IntegrationSetup()
    setup.run_setup()

if __name__ == "__main__":
    main()
