#!/usr/bin/env python3
import psycopg2
import bcrypt

# Database connection settings
DB_CONFIG = {
    'host': 'localhost',
    'database': 'hms',
    'user': 'postgres',
    'password': 'd433847adf9344c4a5e31f6fdb51be0a'
}

def verify_admin_login():
    """Verify the admin login works correctly"""
    
    test_credentials = {
        'email': 'admin@hms.local',
        'password': 'admin123'
    }
    
    try:
        # Connect to database
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Get the stored hash
        cursor.execute("SELECT password FROM account WHERE email = %s", (test_credentials['email'],))
        result = cursor.fetchone()
        
        if not result:
            print("❌ Admin user not found!")
            return False
            
        stored_hash = result[0]
        
        # Verify password
        is_valid = bcrypt.checkpw(
            test_credentials['password'].encode('utf-8'),
            stored_hash.encode('utf-8')
        )
        
        if is_valid:
            print("✅ Password verification successful!")
            print(f"Email: {test_credentials['email']}")
            print(f"Password: {test_credentials['password']}")
            print(f"Hash in DB: {stored_hash}")
        else:
            print("❌ Password verification failed!")
            
        cursor.close()
        conn.close()
        
        return is_valid
        
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

if __name__ == "__main__":
    print("🔍 Verifying admin login credentials...")
    verify_admin_login()
