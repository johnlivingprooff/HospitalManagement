#!/usr/bin/env python3
import psycopg2
import bcrypt
from datetime import datetime

# Database connection settings
DB_CONFIG = {
    'host': 'localhost',
    'database': 'hms',
    'user': 'postgres',
    'password': 'd433847adf9344c4a5e31f6fdb51be0a'
}

def hash_password(password, rounds=12):
    """Generate BCrypt hash with specified rounds"""
    # Generate salt and hash the password
    salt = bcrypt.gensalt(rounds=rounds)
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

def create_admin_user():
    """Create a new admin user in the database"""
    
    # New admin user details
    new_admin = {
        'email': 'admin@hms.local',
        'password': 'admin123',  # You can change this
        'firstName': 'System',
        'lastName': 'Administrator',
        'dob': '1990-01-01',
        'sex': 'Male',
        'active': True,
        'system': True,
        'hidden': False,
        'role_modifiable': True,
        'account_type': 'Administrator'
    }
    
    print(f"Creating new admin user:")
    print(f"Email: {new_admin['email']}")
    print(f"Password: {new_admin['password']}")
    print(f"Hashing password with BCrypt (12 rounds)...")
    
    # Hash the password
    hashed_password = hash_password(new_admin['password'])
    print(f"Password hash: {hashed_password}")
    
    try:
        # Connect to database
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Check if user already exists
        cursor.execute("SELECT id FROM account WHERE email = %s", (new_admin['email'],))
        existing_user = cursor.fetchone()
        
        if existing_user:
            print(f"User with email {new_admin['email']} already exists (ID: {existing_user[0]})")
            print("Updating existing user...")
            
            cursor.execute("""
                UPDATE account 
                SET password = %s, 
                    firstName = %s, 
                    lastName = %s, 
                    active = %s, 
                    modified = %s
                WHERE email = %s
            """, (
                hashed_password,
                new_admin['firstName'],
                new_admin['lastName'],
                new_admin['active'],
                datetime.now(),
                new_admin['email']
            ))
            
            user_id = existing_user[0]
            print(f"✅ Updated existing admin user (ID: {user_id})")
            
        else:
            # Insert new user
            cursor.execute("""
                INSERT INTO account (
                    email, password, firstName, lastName, dob, created, modified,
                    sex, active, system, hidden, role_modifiable, account_type
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                RETURNING id
            """, (
                new_admin['email'],
                hashed_password,
                new_admin['firstName'],
                new_admin['lastName'],
                new_admin['dob'],
                datetime.now(),
                datetime.now(),
                new_admin['sex'],
                new_admin['active'],
                new_admin['system'],
                new_admin['hidden'],
                new_admin['role_modifiable'],
                new_admin['account_type']
            ))
            
            user_id = cursor.fetchone()[0]
            print(f"✅ Created new admin user (ID: {user_id})")
        
        # Commit the transaction
        conn.commit()
        
        # Verify the user was created/updated
        cursor.execute("""
            SELECT id, email, firstName, lastName, account_type, active 
            FROM account 
            WHERE email = %s
        """, (new_admin['email'],))
        
        user_info = cursor.fetchone()
        if user_info:
            print(f"\n📋 User Details:")
            print(f"ID: {user_info[0]}")
            print(f"Email: {user_info[1]}")
            print(f"Name: {user_info[2]} {user_info[3]}")
            print(f"Account Type: {user_info[4]}")
            print(f"Active: {user_info[5]}")
        
        cursor.close()
        conn.close()
        
        print(f"\n🎉 Success! You can now login with:")
        print(f"Email: {new_admin['email']}")
        print(f"Password: {new_admin['password']}")
        
    except Exception as e:
        print(f"❌ Error: {e}")
        if conn:
            conn.rollback()

if __name__ == "__main__":
    create_admin_user()
