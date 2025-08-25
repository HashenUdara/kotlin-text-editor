#!/usr/bin/env python3
"""
Test script for the Kotlin Text Editor Desktop Compiler Bridge

This script tests the bridge service by sending sample commands.
"""

import socket
import sys
import time

def send_command(host: str, port: int, command: str) -> str:
    """Send a command to the bridge service and return response"""
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.settimeout(10)
            sock.connect((host, port))
            
            # Send command
            sock.sendall((command + '\n').encode('utf-8'))
            
            # Receive response
            response = sock.recv(1024).decode('utf-8').strip()
            return response
            
    except Exception as e:
        return f"ERROR: {str(e)}"

def test_bridge_service(host: str = "localhost", port: int = 8765):
    """Test the bridge service functionality"""
    
    print("🧪 Testing Kotlin Text Editor Desktop Bridge Service")
    print(f"🔗 Connecting to {host}:{port}")
    print("=" * 50)
    
    # Test 1: Ping
    print("1️⃣ Testing PING command...")
    response = send_command(host, port, "PING")
    if response == "PONG":
        print("   ✅ PING test passed")
    else:
        print(f"   ❌ PING test failed: {response}")
        return False
    
    # Test 2: Status for non-existent job
    print("\n2️⃣ Testing STATUS command...")
    response = send_command(host, port, "STATUS test123")
    if "not found" in response:
        print("   ✅ STATUS test passed")
    else:
        print(f"   ❌ STATUS test failed: {response}")
    
    # Test 3: Invalid command
    print("\n3️⃣ Testing invalid command...")
    response = send_command(host, port, "INVALID_COMMAND")
    if "Unknown command" in response:
        print("   ✅ Invalid command test passed")
    else:
        print(f"   ❌ Invalid command test failed: {response}")
    
    print("\n🎉 Bridge service tests completed!")
    return True

def main():
    if len(sys.argv) > 1 and sys.argv[1] in ['-h', '--help']:
        print("Usage: python test-bridge.py [host] [port]")
        print("Default: localhost 8765")
        return
    
    host = sys.argv[1] if len(sys.argv) > 1 else "localhost"
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 8765
    
    test_bridge_service(host, port)

if __name__ == "__main__":
    main()

