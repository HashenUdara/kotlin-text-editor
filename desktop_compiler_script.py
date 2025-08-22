#!/usr/bin/env python3
"""
Kotlin Compiler Bridge for Android Text Editor
This script runs on the desktop and compiles Kotlin code sent from the Android app via ADB.

Prerequisites:
1. Install Kotlin compiler (kotlinc) on your system
2. Enable USB debugging on your Android device
3. Install Android SDK with ADB
4. Run this script on your desktop while the Android app is running

Usage:
python3 desktop_compiler_script.py
"""

import os
import sys
import time
import json
import subprocess
import tempfile
from pathlib import Path

class KotlinCompilerBridge:
    def __init__(self):
        self.adb_temp_dir = "/sdcard/kotlin_compiler/"
        self.polling_interval = 1  # Check for new files every second
        self.is_running = False
        
    def check_prerequisites(self):
        """Check if all required tools are available"""
        print("🔍 Checking prerequisites...")
        
        # Check if ADB is available
        try:
            result = subprocess.run(['adb', 'version'], capture_output=True, text=True)
            if result.returncode != 0:
                print("❌ ADB not found. Please install Android SDK and add ADB to PATH.")
                return False
            print("✅ ADB found")
        except FileNotFoundError:
            print("❌ ADB not found. Please install Android SDK and add ADB to PATH.")
            return False
            
        # Check if Kotlin compiler is available
        try:
            result = subprocess.run(['kotlinc', '-version'], capture_output=True, text=True)
            if result.returncode != 0:
                print("❌ Kotlin compiler not found. Please install kotlinc and add to PATH.")
                return False
            print("✅ Kotlin compiler found")
        except FileNotFoundError:
            print("❌ Kotlin compiler not found. Please install kotlinc and add to PATH.")
            return False
            
        # Check if device is connected
        try:
            result = subprocess.run(['adb', 'devices'], capture_output=True, text=True)
            devices = result.stdout.strip().split('\n')[1:]  # Skip header
            connected_devices = [line for line in devices if line.strip() and 'device' in line]
            
            if not connected_devices:
                print("❌ No Android devices connected. Please connect your device and enable USB debugging.")
                return False
            print(f"✅ Android device connected: {connected_devices[0].split()[0]}")
        except Exception as e:
            print(f"❌ Error checking connected devices: {e}")
            return False
            
        return True
        
    def setup_device_directories(self):
        """Create necessary directories on the Android device"""
        try:
            print("📁 Setting up device directories...")
            subprocess.run(['adb', 'shell', 'mkdir', '-p', self.adb_temp_dir], check=True)
            print("✅ Device directories created")
            return True
        except subprocess.CalledProcessError as e:
            print(f"❌ Failed to create device directories: {e}")
            return False
            
    def check_for_compilation_request(self):
        """Check if there's a new compilation request from the Android app"""
        try:
            # Check if compile_request.json exists on device
            result = subprocess.run([
                'adb', 'shell', 'test', '-f', f'{self.adb_temp_dir}compile_request.json'
            ], capture_output=True)
            
            return result.returncode == 0
        except Exception:
            return False
            
    def get_compilation_request(self):
        """Retrieve compilation request from device"""
        try:
            # Pull the request file
            with tempfile.NamedTemporaryFile(mode='w+', suffix='.json', delete=False) as temp_file:
                result = subprocess.run([
                    'adb', 'pull', f'{self.adb_temp_dir}compile_request.json', temp_file.name
                ], capture_output=True, text=True)
                
                if result.returncode != 0:
                    return None
                    
                # Read and parse the request
                temp_file.seek(0)
                with open(temp_file.name, 'r') as f:
                    request_data = json.load(f)
                
                # Clean up
                os.unlink(temp_file.name)
                
                # Remove the request file from device
                subprocess.run([
                    'adb', 'shell', 'rm', f'{self.adb_temp_dir}compile_request.json'
                ], capture_output=True)
                
                return request_data
                
        except Exception as e:
            print(f"❌ Error reading compilation request: {e}")
            return None
            
    def get_source_file(self, source_filename):
        """Retrieve source file from device"""
        try:
            # Create temporary file for source code
            with tempfile.NamedTemporaryFile(mode='w+', suffix='.kt', delete=False) as temp_file:
                result = subprocess.run([
                    'adb', 'pull', f'{self.adb_temp_dir}{source_filename}', temp_file.name
                ], capture_output=True, text=True)
                
                if result.returncode != 0:
                    print(f"❌ Failed to retrieve source file: {result.stderr}")
                    return None
                    
                return temp_file.name
                
        except Exception as e:
            print(f"❌ Error retrieving source file: {e}")
            return None
            
    def compile_kotlin_code(self, source_file_path, original_filename):
        """Compile Kotlin source code"""
        try:
            print(f"🔨 Compiling {original_filename}...")
            
            # Create output directory
            with tempfile.TemporaryDirectory() as output_dir:
                # Run Kotlin compiler
                result = subprocess.run([
                    'kotlinc', source_file_path, '-d', output_dir
                ], capture_output=True, text=True, timeout=30)
                
                if result.returncode == 0:
                    # Compilation successful
                    print(f"✅ Compilation successful!")
                    
                    # Check for generated class files
                    class_files = list(Path(output_dir).glob('*.class'))
                    
                    return {
                        'success': True,
                        'message': 'Compilation completed successfully',
                        'output': result.stdout,
                        'errors': '',
                        'class_files': len(class_files),
                        'bytecode_size': sum(f.stat().st_size for f in class_files)
                    }
                else:
                    # Compilation failed
                    print(f"❌ Compilation failed")
                    return {
                        'success': False,
                        'message': 'Compilation failed',
                        'output': result.stdout,
                        'error': result.stderr,
                        'errors': result.stderr
                    }
                    
        except subprocess.TimeoutExpired:
            return {
                'success': False,
                'message': 'Compilation timeout',
                'output': '',
                'error': 'Compilation took too long and was terminated',
                'errors': 'Compilation timeout after 30 seconds'
            }
        except Exception as e:
            return {
                'success': False,
                'message': 'Compilation error',
                'output': '',
                'error': str(e),
                'errors': f'Internal error: {str(e)}'
            }
            
    def send_compilation_result(self, result):
        """Send compilation result back to the Android device"""
        try:
            # Create temporary result file
            with tempfile.NamedTemporaryFile(mode='w', suffix='.json', delete=False) as temp_file:
                json.dump(result, temp_file, indent=2)
                temp_file_path = temp_file.name
                
            # Push result file to device
            push_result = subprocess.run([
                'adb', 'push', temp_file_path, f'{self.adb_temp_dir}compilation_result.json'
            ], capture_output=True, text=True)
            
            # Clean up local temp file
            os.unlink(temp_file_path)
            
            if push_result.returncode == 0:
                print("📤 Compilation result sent to device")
                return True
            else:
                print(f"❌ Failed to send result to device: {push_result.stderr}")
                return False
                
        except Exception as e:
            print(f"❌ Error sending compilation result: {e}")
            return False
            
    def run(self):
        """Main execution loop"""
        print("🚀 Kotlin Compiler Bridge starting...")
        
        # Check prerequisites
        if not self.check_prerequisites():
            print("❌ Prerequisites not met. Exiting.")
            return False
            
        # Setup device directories
        if not self.setup_device_directories():
            print("❌ Failed to setup device directories. Exiting.")
            return False
            
        print("✅ Kotlin Compiler Bridge is ready!")
        print("📱 Start the Android Kotlin Text Editor and try compiling some code.")
        print("⏹️  Press Ctrl+C to stop the bridge")
        print("-" * 60)
        
        self.is_running = True
        
        try:
            while self.is_running:
                # Check for compilation requests
                if self.check_for_compilation_request():
                    print("📨 New compilation request received!")
                    
                    # Get the request details
                    request = self.get_compilation_request()
                    if request is None:
                        print("❌ Failed to read compilation request")
                        continue
                        
                    # Get the source file
                    source_file = self.get_source_file(request.get('file', 'temp_source.kt'))
                    if source_file is None:
                        print("❌ Failed to retrieve source file")
                        continue
                        
                    # Compile the code
                    result = self.compile_kotlin_code(
                        source_file, 
                        request.get('originalName', 'Main.kt')
                    )
                    
                    # Send result back to device
                    self.send_compilation_result(result)
                    
                    # Clean up local source file
                    os.unlink(source_file)
                    
                    print("-" * 60)
                    
                # Wait before checking again
                time.sleep(self.polling_interval)
                
        except KeyboardInterrupt:
            print("\n⏹️  Kotlin Compiler Bridge stopped by user")
            self.is_running = False
            
        except Exception as e:
            print(f"\n❌ Unexpected error: {e}")
            self.is_running = False
            
        finally:
            # Cleanup
            try:
                subprocess.run([
                    'adb', 'shell', 'rm', '-rf', self.adb_temp_dir
                ], capture_output=True)
                print("🧹 Cleaned up device temporary files")
            except:
                pass
                
        return True

def main():
    """Main entry point"""
    print("=" * 60)
    print("🔗 Kotlin Compiler Bridge for Android Text Editor")
    print("=" * 60)
    
    bridge = KotlinCompilerBridge()
    bridge.run()
    
    print("👋 Goodbye!")

if __name__ == "__main__":
    main()
