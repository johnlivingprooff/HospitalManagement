#!/usr/bin/env python3
"""
Database Connection Troubleshooting Script
Run this script to diagnose database connectivity issues
"""

import os
import sys
import asyncio
import socket
from urllib.parse import urlparse
from dotenv import load_dotenv

# Add the project root to the path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

async def test_dns_resolution(hostname: str):
    """Test if hostname can be resolved"""
    try:
        print(f"🔍 Testing DNS resolution for: {hostname}")
        ip = socket.gethostbyname(hostname)
        print(f"✅ DNS resolution successful: {hostname} -> {ip}")
        return True
    except socket.gaierror as e:
        print(f"❌ DNS resolution failed: {e}")
        print("💡 This could mean:")
        print("   - No internet connection")
        print("   - DNS server issues")
        print("   - Hostname doesn't exist")
        return False

async def test_port_connection(hostname: str, port: int):
    """Test if we can connect to the port"""
    try:
        print(f"🔍 Testing port connection: {hostname}:{port}")
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(10)
        result = sock.connect_ex((hostname, port))
        sock.close()
        
        if result == 0:
            print(f"✅ Port connection successful: {hostname}:{port}")
            return True
        else:
            print(f"❌ Port connection failed: {hostname}:{port}")
            return False
    except Exception as e:
        print(f"❌ Port connection error: {e}")
        return False

async def test_database_connection():
    """Test database connection using SQLAlchemy"""
    try:
        print("🔍 Testing SQLAlchemy database connection...")
        
        from sqlalchemy import create_engine, text
        from app.core.config import settings
        
        print(f"📋 Database URL: {settings.DATABASE_URL}")
        
        engine = create_engine(settings.DATABASE_URL)
        
        with engine.connect() as conn:
            result = conn.execute(text("SELECT version()"))
            version = result.fetchone()[0]
            print(f"✅ Database connection successful!")
            print(f"📊 PostgreSQL version: {version}")
            return True
            
    except Exception as e:
        print(f"❌ Database connection failed: {e}")
        return False

async def check_environment():
    """Check environment variables"""
    load_dotenv()
    
    print("🔍 Checking environment variables...")
    
    database_url = os.getenv("DATABASE_URL")
    if not database_url:
        print("❌ DATABASE_URL not found in environment")
        return False
    
    print(f"✅ DATABASE_URL found: {database_url[:50]}...")
    
    # Parse the URL
    try:
        parsed = urlparse(database_url)
        print(f"📋 Parsed connection details:")
        print(f"   Scheme: {parsed.scheme}")
        print(f"   Host: {parsed.hostname}")
        print(f"   Port: {parsed.port}")
        print(f"   Database: {parsed.path[1:] if parsed.path else 'default'}")
        print(f"   Username: {parsed.username}")
        
        return {
            'hostname': parsed.hostname,
            'port': parsed.port or 5432,
            'database_url': database_url
        }
    except Exception as e:
        print(f"❌ Failed to parse DATABASE_URL: {e}")
        return False

async def suggest_solutions():
    """Suggest possible solutions"""
    print("\n🔧 TROUBLESHOOTING SUGGESTIONS:")
    print("=" * 50)
    
    print("\n1. 🌐 INTERNET CONNECTION:")
    print("   - Check if you can browse the internet")
    print("   - Try: ping google.com")
    
    print("\n2. 🏠 LOCAL DEVELOPMENT:")
    print("   - Use local PostgreSQL instead:")
    print("   - Install PostgreSQL locally")
    print("   - Update .env: DATABASE_URL=postgresql://postgres:password@localhost:5432/hms")
    
    print("\n3. ☁️ RENDER DATABASE:")
    print("   - Check Render dashboard for database status")
    print("   - Ensure database is not suspended")
    print("   - Verify the connection string is correct")
    
    print("\n4. 🔄 ALTERNATIVE DATABASES:")
    print("   - Try Railway PostgreSQL (railway.app)")
    print("   - Use Supabase (supabase.com)")
    print("   - Use ElephantSQL (elephantsql.com)")
    
    print("\n5. 🐳 DOCKER LOCAL:")
    print("   - Use Docker PostgreSQL:")
    print("   - docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=password postgres")

async def main():
    """Main troubleshooting function"""
    print("🏥 HMS Database Connection Troubleshooter")
    print("=" * 50)
    
    # Check environment
    env_check = await check_environment()
    if not env_check:
        await suggest_solutions()
        return
    
    hostname = env_check['hostname']
    port = env_check['port']
    
    # Test DNS resolution
    dns_ok = await test_dns_resolution(hostname)
    
    if dns_ok:
        # Test port connection
        port_ok = await test_port_connection(hostname, port)
        
        if port_ok:
            # Test database connection
            db_ok = await test_database_connection()
            
            if db_ok:
                print("\n🎉 ALL TESTS PASSED!")
                print("Your database connection should work fine.")
            else:
                print("\n⚠️  Database authentication or configuration issue")
        else:
            print("\n⚠️  Network connectivity issue")
    else:
        print("\n⚠️  DNS resolution issue")
    
    await suggest_solutions()

if __name__ == "__main__":
    asyncio.run(main())
