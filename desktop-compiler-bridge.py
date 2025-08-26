#!/usr/bin/env python3
"""
Kotlin Text Editor - Desktop Compiler Bridge
============================================

This script acts as a bridge between the Android Kotlin Text Editor app and the desktop
compilation tools (kotlinc, javac). It receives source code via ADB, compiles it, and
returns the results.

Usage:
    python desktop-compiler-bridge.py [--workspace PATH] [--port PORT]

Requirements:
    - Python 3.7+
    - ADB (Android Debug Bridge)
    - kotlinc (Kotlin Compiler)
    - javac (Java Compiler)
"""

import os
import sys
import subprocess
import tempfile
import json
import time
import uuid
import shutil
import argparse
from pathlib import Path
from typing import Dict, List, Optional, Tuple
from dataclasses import dataclass, asdict
from datetime import datetime

@dataclass
class CompilationRequest:
    """Represents a compilation request"""
    id: str
    filename: str
    source_code: str
    language: str  # 'kotlin' or 'java'
    timestamp: float

@dataclass
class CompilationResult:
    """Represents compilation result"""
    id: str
    success: bool
    output_file: Optional[str] = None
    stdout: str = ""
    stderr: str = ""
    compilation_time: float = 0.0
    error_message: Optional[str] = None

class KotlinCompilerBridge:
    """Main bridge service for handling compilation requests"""
    
    def __init__(self, workspace_dir: str):
        self.workspace_dir = Path(workspace_dir)
        self.temp_dir = self.workspace_dir / "temp"
        self.output_dir = self.workspace_dir / "output"
        self.requests: Dict[str, CompilationRequest] = {}
        self.results: Dict[str, CompilationResult] = {}
        
        # Create directories
        self.workspace_dir.mkdir(parents=True, exist_ok=True)
        self.temp_dir.mkdir(exist_ok=True)
        self.output_dir.mkdir(exist_ok=True)
        
        print(f"[+] Workspace initialized: {self.workspace_dir}")
        print(f"[+] Temp directory: {self.temp_dir}")
        print(f"[+] Output directory: {self.output_dir}")

    def check_dependencies(self) -> Tuple[bool, List[str]]:
        """Check if required tools are available"""
        issues = []
        
        # Check ADB
        try:
            result = subprocess.run(['adb', 'version'], 
                                  capture_output=True, text=True, timeout=5, shell=True)
            if result.returncode != 0:
                issues.append("ADB is not working properly")
            else:
                print(f"[+] ADB: {result.stdout.split()[0]} {result.stdout.split()[4]}")
        except (subprocess.TimeoutExpired, FileNotFoundError):
            issues.append("ADB is not installed or not in PATH")
        
        # Check Kotlin compiler
        try:
            result = subprocess.run(['kotlinc', '-version'], 
                                  capture_output=True, text=True, timeout=10, shell=True)
            if result.returncode == 0:
                print(f"[+] Kotlin compiler: {result.stderr.strip()}")
            else:
                issues.append("kotlinc is not working properly")
        except (subprocess.TimeoutExpired, FileNotFoundError):
            issues.append("kotlinc is not installed or not in PATH")
        
        # Check Java compiler
        try:
            result = subprocess.run(['javac', '-version'], 
                                  capture_output=True, text=True, timeout=5, shell=True)
            if result.returncode == 0:
                print(f"[+] Java compiler: {result.stderr.strip()}")
            else:
                issues.append("javac is not working properly")
        except (subprocess.TimeoutExpired, FileNotFoundError):
            issues.append("javac is not installed or not in PATH")
        
        # Check ADB devices
        try:
            result = subprocess.run(['adb', 'devices'], 
                                  capture_output=True, text=True, timeout=5, shell=True)
            if result.returncode == 0:
                devices = [line for line in result.stdout.split('\n')[1:] 
                          if line.strip() and not line.startswith('*')]
                device_count = len([d for d in devices if 'device' in d])
                print(f"[+] ADB devices: {device_count} connected")
                if device_count == 0:
                    issues.append("No Android devices connected via ADB")
            else:
                issues.append("Could not check ADB devices")
        except (subprocess.TimeoutExpired, FileNotFoundError):
            issues.append("Could not check ADB devices")
        
        return len(issues) == 0, issues

    def create_compilation_request(self, filename: str, source_code: str) -> str:
        """Create a new compilation request"""
        # Determine language from file extension
        ext = Path(filename).suffix.lower()
        if ext == '.kt':
            language = 'kotlin'
        elif ext == '.java':
            language = 'java'
        else:
            raise ValueError(f"Unsupported file extension: {ext}")
        
        # Create request
        request_id = str(uuid.uuid4())[:8]
        request = CompilationRequest(
            id=request_id,
            filename=filename,
            source_code=source_code,
            language=language,
            timestamp=time.time()
        )
        
        self.requests[request_id] = request
        print(f"[*] Created compilation request: {request_id} ({language})")
        return request_id

    def compile_request(self, request_id: str) -> CompilationResult:
        """Compile a request and return the result"""
        if request_id not in self.requests:
            return CompilationResult(
                id=request_id,
                success=False,
                error_message=f"Request {request_id} not found"
            )
        
        request = self.requests[request_id]
        start_time = time.time()
        
        try:
            print(f"[*] Starting compilation: {request_id}")
            
            # Create temporary source file
            temp_file = self.temp_dir / request.filename
            temp_file.write_text(request.source_code, encoding='utf-8')
            
            # Compile based on language
            if request.language == 'kotlin':
                result = self._compile_kotlin(temp_file)
            elif request.language == 'java':
                result = self._compile_java(temp_file)
            else:
                result = CompilationResult(
                    id=request_id,
                    success=False,
                    error_message=f"Unsupported language: {request.language}"
                )
            
            result.compilation_time = time.time() - start_time
            self.results[request_id] = result
            
            # Clean up temp file
            if temp_file.exists():
                temp_file.unlink()
            
            status = "SUCCESS" if result.success else "FAILED"
            print(f"[*] Compilation {status}: {request_id} ({result.compilation_time:.2f}s)")
            
            return result
            
        except Exception as e:
            result = CompilationResult(
                id=request_id,
                success=False,
                error_message=str(e),
                compilation_time=time.time() - start_time
            )
            self.results[request_id] = result
            print(f"[!] Compilation ERROR: {request_id} - {str(e)}")
            return result

    def _compile_kotlin(self, source_file: Path) -> CompilationResult:
        """Compile Kotlin source file"""
        output_file = self.output_dir / f"{source_file.stem}.jar"
        
        try:
            # Run kotlinc
            cmd = [
                'kotlinc',
                str(source_file),
                '-include-runtime',
                '-d', str(output_file)
            ]
            
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=30,
                cwd=str(self.workspace_dir),
                shell=True
            )
            
            if result.returncode == 0:
                return CompilationResult(
                    id="",  # Will be set by caller
                    success=True,
                    output_file=str(output_file),
                    stdout=result.stdout,
                    stderr=result.stderr
                )
            else:
                return CompilationResult(
                    id="",  # Will be set by caller
                    success=False,
                    stdout=result.stdout,
                    stderr=result.stderr,
                    error_message="Kotlin compilation failed"
                )
                
        except subprocess.TimeoutExpired:
            return CompilationResult(
                id="",  # Will be set by caller
                success=False,
                error_message="Compilation timeout (30 seconds)"
            )
        except Exception as e:
            return CompilationResult(
                id="",  # Will be set by caller
                success=False,
                error_message=f"Compilation error: {str(e)}"
            )

    def _compile_java(self, source_file: Path) -> CompilationResult:
        """Compile Java source file"""
        output_dir = self.output_dir / "java_classes"
        output_dir.mkdir(exist_ok=True)
        
        try:
            # Run javac
            cmd = [
                'javac',
                '-d', str(output_dir),
                str(source_file)
            ]
            
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=30,
                cwd=str(self.workspace_dir),
                shell=True
            )
            
            if result.returncode == 0:
                # Find the generated class file
                class_files = list(output_dir.glob('*.class'))
                output_file = class_files[0] if class_files else None
                
                return CompilationResult(
                    id="",  # Will be set by caller
                    success=True,
                    output_file=str(output_file) if output_file else str(output_dir),
                    stdout=result.stdout,
                    stderr=result.stderr
                )
            else:
                return CompilationResult(
                    id="",  # Will be set by caller
                    success=False,
                    stdout=result.stdout,
                    stderr=result.stderr,
                    error_message="Java compilation failed"
                )
                
        except subprocess.TimeoutExpired:
            return CompilationResult(
                id="",  # Will be set by caller
                success=False,
                error_message="Compilation timeout (30 seconds)"
            )
        except Exception as e:
            return CompilationResult(
                id="",  # Will be set by caller
                success=False,
                error_message=f"Compilation error: {str(e)}"
            )

    def get_result(self, request_id: str) -> Optional[CompilationResult]:
        """Get compilation result by ID"""
        return self.results.get(request_id)

    def cleanup_old_files(self, max_age_hours: int = 24):
        """Clean up old temporary and output files"""
        current_time = time.time()
        max_age_seconds = max_age_hours * 3600
        
        cleaned = 0
        for file_path in self.output_dir.glob('*'):
            if file_path.is_file():
                age = current_time - file_path.stat().st_mtime
                if age > max_age_seconds:
                    file_path.unlink()
                    cleaned += 1
        
        if cleaned > 0:
            print(f"[*] Cleaned up {cleaned} old files")

class ADBCommandHandler:
    """Handles ADB commands from Android app"""
    
    def __init__(self, bridge: KotlinCompilerBridge):
        self.bridge = bridge
    
    def start_listening(self):
        """Start listening for ADB commands"""
        print("[*] Starting ADB command listener...")
        print("[*] Waiting for Android app commands...")
        print("[!] Use Ctrl+C to stop")
        
        try:
            while True:
                # Check for new commands via ADB
                self._check_adb_commands()
                time.sleep(1)  # Check every second
                
        except KeyboardInterrupt:
            print("\n[*] Stopping ADB listener...")
    
    def _check_adb_commands(self):
        """Check for pending ADB commands"""
        try:
            # Check if there's a command file in the app's external files directory
            app_files_dir = "/storage/emulated/0/Android/data/com.kotlintexteditor/files"
            command_file_path = f"{app_files_dir}/kotlin_editor_cmd.txt"
            
            result = subprocess.run([
                'adb', 'shell', 'ls', command_file_path
            ], capture_output=True, text=True, timeout=5, shell=True)
            
            if result.returncode == 0:
                # Command file exists, read it
                self._process_command_file(command_file_path, app_files_dir)
                
        except (subprocess.TimeoutExpired, subprocess.CalledProcessError):
            # No command file or ADB error, continue
            pass
    
    def _process_command_file(self, command_file_path, app_files_dir):
        """Process command from the device"""
        try:
            # Pull command file
            cmd_file = self.bridge.temp_dir / "command.txt"
            result = subprocess.run([
                'adb', 'pull', command_file_path, str(cmd_file)
            ], capture_output=True, text=True, timeout=10, shell=True)
            
            if result.returncode == 0 and cmd_file.exists():
                # Read and process command
                try:
                    raw_content = cmd_file.read_text()
                    print(f"[DEBUG] Raw command file content: '{raw_content}'")
                    print(f"[DEBUG] Content length: {len(raw_content)} chars")
                    print(f"[DEBUG] Content bytes: {raw_content.encode('utf-8')}")
                    
                    command_data = json.loads(raw_content)
                    self._handle_command(command_data, app_files_dir)
                    
                    # Remove command file from device
                    subprocess.run(['adb', 'shell', 'rm', command_file_path], 
                                 capture_output=True, timeout=5, shell=True)
                    
                    # Clean up local file
                    cmd_file.unlink()
                    
                except json.JSONDecodeError as je:
                    print(f"[!] JSON parsing error: {str(je)}")
                    print(f"[!] Raw content that failed: '{raw_content}'")
                    print(f"[!] Content repr: {repr(raw_content)}")
                    # Still clean up the bad file
                    subprocess.run(['adb', 'shell', 'rm', command_file_path], 
                                 capture_output=True, timeout=5, shell=True)
                    cmd_file.unlink()
                
        except Exception as e:
            print(f"[!] Error processing command: {str(e)}")
            print(f"[!] Exception type: {type(e).__name__}")
            import traceback
            print(f"[!] Traceback: {traceback.format_exc()}")
    
    def _handle_command(self, command_data: dict, app_files_dir: str):
        """Handle a specific command"""
        try:
            cmd_type = command_data.get('type')
            
            if cmd_type == 'compile':
                self._handle_compile_command(command_data, app_files_dir)
            elif cmd_type == 'get_result':
                self._handle_get_result_command(command_data, app_files_dir)
            elif cmd_type == 'run':
                self._handle_run_command(command_data, app_files_dir)
            elif cmd_type == 'ping':
                self._handle_ping_command(app_files_dir)
            else:
                print(f"[!] Unknown command type: {cmd_type}")
                
        except Exception as e:
            print(f"[!] Error handling command: {str(e)}")
    
    def _handle_compile_command(self, command_data: dict, app_files_dir: str):
        """Handle compilation command"""
        filename = command_data.get('filename', 'Main.kt')
        source_code = command_data.get('source_code', '')
        
        print(f"[<] Compile request: {filename}")
        
        # Create and compile request
        request_id = self.bridge.create_compilation_request(filename, source_code)
        result = self.bridge.compile_request(request_id)
        
        # Send result back to device
        self._send_result_to_device(result, app_files_dir)
    
    def _handle_get_result_command(self, command_data: dict, app_files_dir: str):
        """Handle get result command"""
        request_id = command_data.get('request_id')
        result = self.bridge.get_result(request_id)
        
        if result:
            self._send_result_to_device(result, app_files_dir)
        else:
            error_result = CompilationResult(
                id=request_id,
                success=False,
                error_message=f"Result not found: {request_id}"
            )
            self._send_result_to_device(error_result, app_files_dir)
    
    def _handle_ping_command(self, app_files_dir: str):
        """Handle ping command"""
        print("[<] Ping request")
        ping_result = {
            'type': 'pong',
            'timestamp': time.time(),
            'bridge_version': '1.0'
        }
        self._send_response_to_device(ping_result, app_files_dir)
    
    def _send_result_to_device(self, result: CompilationResult, app_files_dir: str):
        """Send compilation result back to device"""
        try:
            result_data = asdict(result)
            self._send_response_to_device(result_data, app_files_dir)
            
        except Exception as e:
            print(f"[!] Error sending result to device: {str(e)}")
    
    def _send_response_to_device(self, response_data: dict, app_files_dir: str):
        """Send response back to Android device"""
        try:
            # Write response to temp file
            response_file = self.bridge.temp_dir / "response.json"
            response_file.write_text(json.dumps(response_data, indent=2))
            
            # Push response to device app files directory
            response_path = f"{app_files_dir}/kotlin_editor_response.json"
            subprocess.run([
                'adb', 'push', str(response_file), response_path
            ], capture_output=True, timeout=10, shell=True)
            
            # Clean up
            response_file.unlink()
            
            print(f"[>] Response sent to device")
            
        except Exception as e:
            print(f"[!] Error sending response: {str(e)}")

def main():
    parser = argparse.ArgumentParser(description="Kotlin Text Editor Desktop Compiler Bridge")
    parser.add_argument("--workspace", default="~/kotlin-editor-bridge", 
                       help="Workspace directory (default: ~/kotlin-editor-bridge)")
    
    args = parser.parse_args()
    
    # Expand workspace path
    workspace_path = Path(args.workspace).expanduser().resolve()
    
    print("=" * 60)
    print("Kotlin Text Editor - Desktop Compiler Bridge")
    print("=" * 60)
    print(f"Workspace: {workspace_path}")
    print(f"Started at: {datetime.now()}")
    print("=" * 60)
    
    try:
        # Create bridge service
        bridge = KotlinCompilerBridge(str(workspace_path))
        
        # Check dependencies
        deps_ok, issues = bridge.check_dependencies()
        if not deps_ok:
            print("\n[!] Dependency issues found:")
            for issue in issues:
                print(f"   * {issue}")
            print("\nPlease fix these issues before starting the bridge service.")
            return 1
        
        print("\n[+] All dependencies are available")
        
        # Clean up old files
        bridge.cleanup_old_files()
        
        # Start ADB command handler
        handler = ADBCommandHandler(bridge)
        handler.start_listening()
        
        return 0
        
    except KeyboardInterrupt:
        print("\n[*] Bridge service stopped")
        return 0
    except Exception as e:
        print(f"\n[!] Bridge service error: {str(e)}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
