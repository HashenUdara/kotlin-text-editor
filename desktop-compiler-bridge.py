#!/usr/bin/env python3
"""
Kotlin Text Editor - Desktop Compiler Bridge Service

This service runs on the desktop and handles compilation requests from the Android app via ADB.
It receives Kotlin/Java source files, compiles them using kotlinc/javac, and returns results.

Requirements:
- Python 3.7+
- kotlinc installed and in PATH
- javac installed and in PATH
- ADB connection to Android device

Usage:
    python desktop-compiler-bridge.py [--port 8765] [--workspace ~/kotlin-editor-bridge]
"""

import os
import sys
import json
import time
import shutil
import subprocess
import threading
import socketserver
import socket
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Optional, Tuple
import argparse

class CompilationJob:
    """Represents a compilation job"""
    def __init__(self, compilation_id: str, source_file: str):
        self.compilation_id = compilation_id
        self.source_file = source_file
        self.state = "RUNNING"
        self.start_time = time.time()
        self.end_time = None
        self.output_path = None
        self.error_message = None
        self.error_details = None
        self.warnings = []
        self.errors = []

class KotlinCompilerBridge:
    """Main bridge service for handling compilation requests"""
    
    def __init__(self, workspace_path: str, port: int = 8765):
        self.workspace_path = Path(workspace_path)
        self.port = port
        self.compilation_jobs: Dict[str, CompilationJob] = {}
        
        # Set up workspace directories
        self.setup_workspace()
        
        print(f"[*] Kotlin Compiler Bridge Service")
        print(f"[*] Workspace: {self.workspace_path}")
        print(f"[*] Port: {self.port}")
        print(f"[*] Started at: {datetime.now()}")
    
    def setup_workspace(self):
        """Create workspace directory structure"""
        directories = [
            self.workspace_path / "source",
            self.workspace_path / "compiled", 
            self.workspace_path / "logs",
            self.workspace_path / "temp"
        ]
        
        for directory in directories:
            directory.mkdir(parents=True, exist_ok=True)
            
        print(f"[+] Workspace directories created")
    
    def check_dependencies(self) -> Tuple[bool, List[str]]:
        """Check if required tools are available"""
        issues = []
        
        # Check kotlinc (try both kotlinc and kotlinc.bat for Windows)
        kotlinc_found = False
        for kotlinc_cmd in ["kotlinc", "kotlinc.bat"]:
            try:
                result = subprocess.run([kotlinc_cmd, "-version"], 
                                      capture_output=True, text=True, timeout=10, shell=True)
                if result.returncode == 0:
                    print(f"[+] Kotlin compiler: {result.stderr.strip()}")
                    kotlinc_found = True
                    break
            except (subprocess.TimeoutExpired, FileNotFoundError):
                continue
        
        if not kotlinc_found:
            issues.append("kotlinc not found in PATH")
        
        # Check javac
        try:
            result = subprocess.run(["javac", "-version"], 
                                  capture_output=True, text=True, timeout=10, shell=True)
            if result.returncode != 0:
                issues.append("javac not working properly")
            else:
                print(f"[+] Java compiler: {result.stderr.strip()}")
        except (subprocess.TimeoutExpired, FileNotFoundError):
            issues.append("javac not found in PATH")
        
        # Check ADB
        try:
            result = subprocess.run(["adb", "devices"], 
                                  capture_output=True, text=True, timeout=10, shell=True)
            if result.returncode != 0:
                issues.append("adb not working properly")
            else:
                devices = [line for line in result.stdout.split('\n') 
                          if line.strip() and 'device' in line and not line.startswith('List')]
                print(f"[+] ADB devices: {len(devices)} connected")
        except (subprocess.TimeoutExpired, FileNotFoundError):
            issues.append("adb not found in PATH")
        
        return len(issues) == 0, issues
    
    def handle_ping(self) -> str:
        """Handle ping request"""
        return "PONG"
    
    def handle_send_source(self, filename: str, content: str) -> str:
        """Handle direct source code transfer"""
        try:
            local_path = self.workspace_path / "source" / filename
            
            # Write source code to file
            with open(local_path, 'w', encoding='utf-8') as f:
                f.write(content)
            
            print(f"[+] Received source file: {filename} ({len(content)} chars)")
            return "OK"
            
        except Exception as e:
            return f"ERROR: Source transfer failed: {str(e)}"
    
    def handle_compile(self, compilation_id: str, filename: str) -> str:
        """Handle compilation request"""
        try:
            source_path = self.workspace_path / "source" / filename
            
            if not source_path.exists():
                return f"ERROR: Source file not found: {filename}"
            
            # Create compilation job
            job = CompilationJob(compilation_id, filename)
            self.compilation_jobs[compilation_id] = job
            
            # Start compilation in background thread
            thread = threading.Thread(
                target=self._compile_file, 
                args=(job, source_path)
            )
            thread.daemon = True
            thread.start()
            
            print(f"[*] Started compilation: {compilation_id} - {filename}")
            return "OK"
            
        except Exception as e:
            return f"ERROR: Failed to start compilation: {str(e)}"
    
    def handle_status(self, compilation_id: str) -> str:
        """Handle status request"""
        job = self.compilation_jobs.get(compilation_id)
        if not job:
            return "ERROR: Compilation job not found"
        
        # Format: STATE|output_path|compilation_time|error_message|error_details
        compilation_time = ""
        if job.end_time:
            compilation_time = str(int((job.end_time - job.start_time) * 1000))
        
        if job.state == "COMPLETED":
            return f"COMPLETED|{job.output_path or ''}|{compilation_time}||"
        elif job.state == "FAILED":
            return f"FAILED|||{job.error_message or ''}|{job.error_details or ''}"
        else:
            return f"RUNNING||||"
    
    def _compile_file(self, job: CompilationJob, source_path: Path):
        """Compile a source file (runs in background thread)"""
        try:
            print(f"[*] Compiling: {job.source_file}")
            
            # Determine file type and compiler
            file_ext = source_path.suffix.lower()
            
            if file_ext == ".kt":
                success, output_path, error_msg, error_details = self._compile_kotlin(source_path)
            elif file_ext == ".java":
                success, output_path, error_msg, error_details = self._compile_java(source_path)
            else:
                job.state = "FAILED"
                job.error_message = f"Unsupported file type: {file_ext}"
                job.end_time = time.time()
                return
            
            job.end_time = time.time()
            
            if success:
                job.state = "COMPLETED"
                job.output_path = output_path
                print(f"[+] Compilation successful: {job.source_file} -> {output_path}")
            else:
                job.state = "FAILED" 
                job.error_message = error_msg
                job.error_details = error_details
                print(f"[-] Compilation failed: {job.source_file} - {error_msg}")
                
        except Exception as e:
            job.state = "FAILED"
            job.error_message = f"Compilation exception: {str(e)}"
            job.end_time = time.time()
            print(f"[!] Compilation error: {job.source_file} - {str(e)}")
    
    def _compile_kotlin(self, source_path: Path) -> Tuple[bool, Optional[str], Optional[str], Optional[str]]:
        """Compile Kotlin file"""
        try:
            # Output JAR path
            jar_name = source_path.stem + ".jar"
            output_path = self.workspace_path / "compiled" / jar_name
            
            # Compile command (use shell=True for Windows batch files)
            cmd = [
                "kotlinc",
                str(source_path),
                "-include-runtime",
                "-d", str(output_path)
            ]
            
            print(f"[*] Command: {' '.join(cmd)}")
            
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=60,  # 1 minute timeout
                cwd=self.workspace_path,
                shell=True  # Required for Windows batch files
            )
            
            if result.returncode == 0:
                return True, str(output_path), None, None
            else:
                error_output = result.stderr or result.stdout
                return False, None, "Kotlin compilation failed", error_output
                
        except subprocess.TimeoutExpired:
            return False, None, "Compilation timeout", "Compilation took longer than 60 seconds"
        except Exception as e:
            return False, None, "Compilation error", str(e)
    
    def _compile_java(self, source_path: Path) -> Tuple[bool, Optional[str], Optional[str], Optional[str]]:
        """Compile Java file"""
        try:
            # Output directory
            output_dir = self.workspace_path / "compiled" / source_path.stem
            output_dir.mkdir(exist_ok=True)
            
            # Compile command
            cmd = [
                "javac",
                "-d", str(output_dir),
                str(source_path)
            ]
            
            print(f"[*] Command: {' '.join(cmd)}")
            
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=60,
                cwd=self.workspace_path,
                shell=True  # Required for Windows
            )
            
            if result.returncode == 0:
                return True, str(output_dir), None, None
            else:
                error_output = result.stderr or result.stdout
                return False, None, "Java compilation failed", error_output
                
        except subprocess.TimeoutExpired:
            return False, None, "Compilation timeout", "Compilation took longer than 60 seconds"
        except Exception as e:
            return False, None, "Compilation error", str(e)

class BridgeRequestHandler(socketserver.StreamRequestHandler):
    """Handles individual requests to the bridge service"""
    
    def handle(self):
        try:
            # Read request line
            data = self.rfile.readline().decode('utf-8').strip()
            if not data:
                return
            
            print(f"[<] Request: {data[:50]}..." if len(data) > 50 else f"[<] Request: {data}")
            
            # Parse command
            parts = data.split()
            if not parts:
                self.send_response("ERROR: Empty command")
                return
            
            command = parts[0]
            bridge = self.server.bridge
            
            if command == "PING":
                response = bridge.handle_ping()
            elif command == "SEND_SOURCE" and len(parts) >= 3:
                # Format: SEND_SOURCE filename length
                # Followed by source code content
                filename = parts[1]
                try:
                    content_length = int(parts[2])
                    # Read the newline after the command
                    self.rfile.readline()
                    # Read the actual source code
                    source_content = self.rfile.read(content_length).decode('utf-8')
                    response = bridge.handle_send_source(filename, source_content)
                except (ValueError, UnicodeDecodeError) as e:
                    response = f"ERROR: Invalid source data: {str(e)}"
            elif command == "COMPILE" and len(parts) >= 3:
                compilation_id = parts[1]
                filename = parts[2]
                response = bridge.handle_compile(compilation_id, filename)
            elif command == "STATUS" and len(parts) >= 2:
                compilation_id = parts[1]
                response = bridge.handle_status(compilation_id)
            else:
                response = f"ERROR: Unknown command: {command}"
            
            self.send_response(response)
            
        except Exception as e:
            self.send_response(f"ERROR: Request handling failed: {str(e)}")
    
    def send_response(self, response: str):
        """Send response back to client"""
        try:
            self.wfile.write((response + '\n').encode('utf-8'))
            self.wfile.flush()
            print(f"[>] Response: {response}")
        except Exception as e:
            print(f"[-] Failed to send response: {str(e)}")

class BridgeServer(socketserver.ThreadingTCPServer):
    """TCP server for handling bridge requests"""
    
    def __init__(self, host: str, port: int, bridge: KotlinCompilerBridge):
        super().__init__((host, port), BridgeRequestHandler)
        self.bridge = bridge
        self.allow_reuse_address = True
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

def main():
    parser = argparse.ArgumentParser(description="Kotlin Text Editor Desktop Compiler Bridge")
    parser.add_argument("--port", type=int, default=8765, help="Server port (default: 8765)")
    parser.add_argument("--workspace", default="~/kotlin-editor-bridge", 
                       help="Workspace directory (default: ~/kotlin-editor-bridge)")
    parser.add_argument("--host", default="localhost", help="Server host (default: localhost)")
    
    args = parser.parse_args()
    
    # Expand workspace path
    workspace_path = Path(args.workspace).expanduser().resolve()
    
    print("[*] Starting Kotlin Text Editor Desktop Compiler Bridge")
    print("=" * 55)
    
    try:
        # Create bridge service
        bridge = KotlinCompilerBridge(str(workspace_path), args.port)
        
        # Check dependencies
        deps_ok, issues = bridge.check_dependencies()
        if not deps_ok:
            print("[-] Dependency issues found:")
            for issue in issues:
                print(f"   * {issue}")
            print("\nPlease fix these issues before starting the bridge service.")
            return 1
        
        print("\n[*] Starting TCP server...")
        
        # Create and start server
        try:
            server = BridgeServer(args.host, args.port, bridge)
        except OSError as e:
            if "Address already in use" in str(e) or "10048" in str(e):
                print(f"[-] Port {args.port} is already in use!")
                print(f"    Try stopping existing bridge service or use a different port:")
                print(f"    python desktop-compiler-bridge.py --port 8766")
                return 1
            else:
                raise e
        
        print(f"[+] Bridge service ready!")
        print(f"[*] Listening on {args.host}:{args.port}")
        print(f"[*] Waiting for Android app connections...")
        print(f"[!] Press Ctrl+C to stop")
        print("=" * 55)
        
        # Serve forever
        server.serve_forever()
        
    except KeyboardInterrupt:
        print("\n[*] Shutting down bridge service...")
        return 0
    except Exception as e:
        print(f"[!] Bridge service error: {str(e)}")
        return 1

if __name__ == "__main__":
    sys.exit(main())

