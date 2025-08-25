#!/usr/bin/env python3
"""
Demo Kotlin Compiler Bridge - Works without Kotlin compiler
This version simulates compilation for demonstration purposes
"""

import os
import sys
import time
import json
import subprocess
import tempfile
from pathlib import Path

class DemoCompilerBridge:
    def __init__(self):
        self.adb_temp_dir = "/sdcard/kotlin_compiler/"
        self.polling_interval = 1
        self.is_running = False
        
    def check_adb(self):
        """Check if ADB is available"""
        try:
            result = subprocess.run(['adb', 'version'], capture_output=True, text=True)
            return result.returncode == 0
        except FileNotFoundError:
            return False
            
    def check_device(self):
        """Check if device is connected"""
        try:
            result = subprocess.run(['adb', 'devices'], capture_output=True, text=True)
            devices = result.stdout.strip().split('\n')[1:]
            connected_devices = [line for line in devices if line.strip() and 'device' in line]
            return len(connected_devices) > 0
        except Exception:
            return False
            
    def setup_device_directories(self):
        """Create necessary directories on device"""
        try:
            subprocess.run(['adb', 'shell', 'mkdir', '-p', self.adb_temp_dir], check=True)
            return True
        except subprocess.CalledProcessError:
            return False
            
    def check_for_compilation_request(self):
        """Check if there's a compilation request"""
        try:
            result = subprocess.run([
                'adb', 'shell', 'test', '-f', f'{self.adb_temp_dir}compile_request.json'
            ], capture_output=True)
            return result.returncode == 0
        except Exception:
            return False
            
    def get_compilation_request(self):
        """Get the compilation request"""
        try:
            with tempfile.NamedTemporaryFile(mode='w+', suffix='.json', delete=False) as temp_file:
                result = subprocess.run([
                    'adb', 'pull', f'{self.adb_temp_dir}compile_request.json', temp_file.name
                ], capture_output=True, text=True)
                
                if result.returncode != 0:
                    return None
                    
                with open(temp_file.name, 'r') as f:
                    request_data = json.load(f)
                
                os.unlink(temp_file.name)
                
                # Remove request file from device
                subprocess.run([
                    'adb', 'shell', 'rm', f'{self.adb_temp_dir}compile_request.json'
                ], capture_output=True)
                
                return request_data
        except Exception as e:
            print(f"❌ Error reading request: {e}")
            return None
            
    def get_source_file(self, source_filename):
        """Get source file from device"""
        try:
            with tempfile.NamedTemporaryFile(mode='w+', suffix='.kt', delete=False) as temp_file:
                result = subprocess.run([
                    'adb', 'pull', f'{self.adb_temp_dir}{source_filename}', temp_file.name
                ], capture_output=True, text=True)
                
                if result.returncode != 0:
                    return None
                    
                return temp_file.name
        except Exception:
            return None
            
    def demo_compile_kotlin(self, source_file_path, original_filename):
        """Demo compilation - analyzes code and gives feedback"""
        try:
            print(f"🔨 Analyzing {original_filename}...")
            
            # Read the source code
            with open(source_file_path, 'r') as f:
                source_code = f.read()
            
            # Demo analysis
            time.sleep(2)  # Simulate compilation time
            
            # Basic syntax checking
            errors = []
            lines = source_code.split('\n')
            
            for i, line in enumerate(lines, 1):
                line = line.strip()
                if line and not line.startswith('//'):
                    # Check for common syntax issues
                    if 'fun main(' in line and not line.endswith('{'):
                        if '{' not in line and not any('{' in lines[j] for j in range(i, min(i+2, len(lines)))):
                            errors.append({
                                'line': i,
                                'message': 'Missing opening brace for function'
                            })
                    
                    if line.count('(') != line.count(')'):
                        errors.append({
                            'line': i,
                            'message': 'Unmatched parentheses'
                        })
                    
                    if line.count('{') != line.count('}') and line.endswith('{'):
                        # This is okay - opening brace
                        pass
                    elif line.count('{') != line.count('}'):
                        errors.append({
                            'line': i,
                            'message': 'Unmatched braces'
                        })
            
            if errors:
                print(f"❌ Found {len(errors)} syntax issues")
                return {
                    'success': False,
                    'message': f'Demo analysis found {len(errors)} potential issues',
                    'output': '',
                    'error': f'Syntax analysis found {len(errors)} issues',
                    'errors': '\n'.join([f"Line {e['line']}: {e['message']}" for e in errors])
                }
            else:
                print(f"✅ Demo analysis passed!")
                
                # Count some metrics
                fun_count = source_code.count('fun ')
                val_count = source_code.count('val ')
                var_count = source_code.count('var ')
                
                return {
                    'success': True,
                    'message': 'Demo analysis completed successfully',
                    'output': f'Analyzed {len(lines)} lines of code\nFound {fun_count} functions, {val_count} val declarations, {var_count} var declarations\nCode structure looks good!',
                    'errors': '',
                    'bytecode_size': len(source_code) * 2  # Simulated bytecode size
                }
                
        except Exception as e:
            return {
                'success': False,
                'message': 'Demo analysis error',
                'output': '',
                'error': str(e),
                'errors': f'Analysis error: {str(e)}'
            }
            
    def send_result(self, result):
        """Send result back to device"""
        try:
            with tempfile.NamedTemporaryFile(mode='w', suffix='.json', delete=False) as temp_file:
                json.dump(result, temp_file, indent=2)
                temp_file_path = temp_file.name
                
            push_result = subprocess.run([
                'adb', 'push', temp_file_path, f'{self.adb_temp_dir}compilation_result.json'
            ], capture_output=True, text=True)
            
            os.unlink(temp_file_path)
            return push_result.returncode == 0
        except Exception:
            return False
            
    def cleanup(self):
        """Cleanup device files"""
        try:
            subprocess.run(['adb', 'shell', 'rm', '-rf', self.adb_temp_dir], capture_output=True)
        except:
            pass
            
    def run(self):
        """Main demo loop"""
        print("=" * 60)
        print("🎯 Kotlin Text Editor - Demo Compiler Bridge")
        print("=" * 60)
        print("📱 This demo version works without Kotlin compiler installed")
        print("🔍 It provides basic syntax analysis and feedback")
        
        if not self.check_adb():
            print("❌ ADB not found")
            return False
            
        print("✅ ADB found")
        
        if not self.check_device():
            print("❌ No Android device connected")
            print("📱 Please connect your Android device with USB debugging enabled")
            return False
            
        print("✅ Android device connected")
        
        if not self.setup_device_directories():
            print("❌ Failed to setup device directories")
            return False
            
        print("✅ Device directories ready")
        print()
        print("🎉 Demo Compiler Bridge is ready!")
        print("📱 Use the Android app to 'compile' Kotlin code")
        print("🔍 The demo will analyze your code and provide feedback")
        print("⏹️  Press Ctrl+C to stop")
        print("-" * 60)
        
        self.is_running = True
        
        try:
            while self.is_running:
                if self.check_for_compilation_request():
                    print("📨 New compilation request received!")
                    
                    request = self.get_compilation_request()
                    if request is None:
                        continue
                        
                    source_file = self.get_source_file(request.get('file', 'temp_source.kt'))
                    if source_file is None:
                        continue
                        
                    result = self.demo_compile_kotlin(
                        source_file, 
                        request.get('originalName', 'Main.kt')
                    )
                    
                    self.send_result(result)
                    os.unlink(source_file)
                    print("-" * 60)
                    
                time.sleep(self.polling_interval)
                
        except KeyboardInterrupt:
            print("\n⏹️  Demo bridge stopped by user")
            
        finally:
            self.cleanup()
            
        return True

def main():
    bridge = DemoCompilerBridge()
    bridge.run()

if __name__ == "__main__":
    main()

